import { createServer, type IncomingHttpHeaders, type IncomingMessage, type Server, type ServerResponse } from "node:http";
import type { AddressInfo } from "node:net";

/** A request as the stub received it. */
export interface RecordedRequest {
  readonly method: string | undefined;
  readonly url: string | undefined;
  readonly headers: IncomingHttpHeaders;
}

// Plays Recipe Catalog 1.0.0 (contracts/upstream/recipe-catalog.openapi.yaml) on a free local port, under the
// spec's server path /recipe-catalog; answers with what the test set and records every request.
export class StubRecipeCatalog {
  readonly requests: RecordedRequest[] = [];
  private status = 404;
  private body: unknown = { code: "NOT_FOUND", message: "No such how-to step" };
  private delayMs = 0;
  private dropsLeft = 0;
  /** Answers for particular paths (a recipe's steps); every other path gets the general answer. */
  private readonly answersByPath = new Map<string, { readonly status: number; readonly body: unknown }>();

  private constructor(private readonly server: Server) {}

  static async start(): Promise<StubRecipeCatalog> {
    let stub: StubRecipeCatalog | undefined;
    const server = createServer((request, response) => stub?.answer(request, response));
    stub = new StubRecipeCatalog(server);
    await new Promise<void>((resolve) => server.listen(0, "127.0.0.1", resolve));
    return stub;
  }

  get baseUrl(): string {
    return `http://127.0.0.1:${(this.server.address() as AddressInfo).port}/recipe-catalog`;
  }

  respondWith(status: number, body: unknown): void {
    this.status = status;
    this.body = body;
  }

  /** This path answers with this status and body, whatever the general answer is. */
  answerAt(path: string, status: number, body: unknown): void {
    this.answersByPath.set(path, { status, body });
  }

  /** Answers only after this many milliseconds — Recipe Catalog is slow. */
  delayBy(milliseconds: number): void {
    this.delayMs = milliseconds;
  }

  /** The next requests fail on the network: the connection is closed without an answer. */
  dropNext(requests: number): void {
    this.dropsLeft = requests;
  }

  /** Forgets the requests and answers again at once (between tests). */
  reset(): void {
    this.requests.length = 0;
    this.delayMs = 0;
    this.dropsLeft = 0;
    this.answersByPath.clear();
  }

  async stop(): Promise<void> {
    await new Promise((resolve) => this.server.close(resolve));
  }

  private answer(request: IncomingMessage, response: ServerResponse): void {
    this.requests.push({
      method: request.method,
      url: request.url,
      headers: request.headers,
    });
    if (this.dropsLeft > 0) {
      this.dropsLeft -= 1;
      request.socket.destroy();
      return;
    }
    const { status, body } = this.answersByPath.get(request.url ?? "") ?? { status: this.status, body: this.body };
    setTimeout(() => {
      response.writeHead(status, { "content-type": "application/json" });
      response.end(JSON.stringify(body));
    }, this.delayMs);
  }
}
