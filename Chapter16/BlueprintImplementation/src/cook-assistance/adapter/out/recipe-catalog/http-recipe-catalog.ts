import { type HowToStep, type RecipeCatalog, RecipeCatalogUnavailable } from "../../../application/ports/recipe-catalog.js";
import type { operations, paths } from "../../../../generated/openapi/recipe-catalog.js";
import type { HowToStepId, RecipeId } from "../../../domain/shared/ids.js";
import { type CircuitBreaker, CircuitOpen } from "./circuit-breaker.js";
import type { TokenProvider } from "./token-provider.js";

/** The upstream path, checked against the generated paths of the pinned spec. */
const STEP_PATH: keyof paths = "/recipes/{recipeId}/how-to-steps/{howToStepId}";
/** The contract version the adapter was built against; a new upstream version fails the build until it is pinned. */
const UPSTREAM_VERSION: operations["getRecipeHowToStepById"]["parameters"]["header"]["version"] = "1.0.0";
/** ADR-CA-015 · Recipe Catalog must answer within 150 ms (QS-08: commands p95 300 ms). */
const TIMEOUT_MS = 150;

/** fetch gave up because AbortSignal.timeout fired. */
function isTimeout(error: unknown): boolean {
  return error instanceof DOMException && error.name === "TimeoutError";
}

export interface HttpRecipeCatalogSettings {
  /** Recipe Catalog's server URL, e.g. https://larder.org/recipe-catalog (RECIPE_CATALOG_URL). */
  readonly baseUrl: string;
  readonly tokenProvider: TokenProvider;
  readonly breaker: CircuitBreaker;
}

// op: getRecipeHowToStepById (contracts/upstream/recipe-catalog.openapi.yaml 1.0.0) · ADR-CA-015
export class HttpRecipeCatalog implements RecipeCatalog {
  constructor(private readonly settings: HttpRecipeCatalogSettings) {}

  async findHowToStep(recipeId: RecipeId, howToStepId: HowToStepId): Promise<HowToStep | undefined> {
    const exists = await this.lookUp(STEP_PATH.replace("{recipeId}", recipeId).replace("{howToStepId}", howToStepId));
    return exists ? { recipeId, howToStepId } : undefined;
  }

  /** ADR-CA-015 · through the circuit breaker; an open circuit means unavailable, without a request. */
  private async lookUp(path: string): Promise<boolean> {
    try {
      return await this.settings.breaker.call(() => this.stepExists(path));
    } catch (error) {
      throw error instanceof CircuitOpen ? new RecipeCatalogUnavailable() : error;
    }
  }

  /** 2xx: the step exists; 404: the recipe has no such step (INV-18). Any other status (5xx, or our token refused:
   *  401/403) is a failure the breaker counts. */
  private async stepExists(path: string): Promise<boolean> {
    const response = await this.get(path);
    if (response.status === 404) {
      return false;
    }
    if (!response.ok) {
      throw new RecipeCatalogUnavailable();
    }
    return true;
  }

  /** ADR-CA-015 · a network failure is retried once; no answer within 150 ms is not (QS-08). Either way → unavailable. */
  private async get(path: string): Promise<Response> {
    try {
      return await this.send(path);
    } catch (error) {
      if (isTimeout(error)) {
        throw new RecipeCatalogUnavailable();
      }
      return this.send(path).catch(() => {
        throw new RecipeCatalogUnavailable();
      });
    }
  }

  /** One GET with our token and the pinned contract version, given up after 150 ms. */
  private async send(path: string): Promise<Response> {
    const token = await this.settings.tokenProvider.accessToken();
    return fetch(`${this.settings.baseUrl}${path}`, {
      headers: { version: UPSTREAM_VERSION, accept: "application/json", authorization: `Bearer ${token}` },
      signal: AbortSignal.timeout(TIMEOUT_MS),
    });
  }
}
