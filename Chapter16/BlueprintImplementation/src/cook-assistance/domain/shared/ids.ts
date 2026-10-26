// Typed identifiers. References to other contexts' entities use their own ID type, never a bare string.

type Brand<T, B extends string> = T & { readonly __brand: B };

/** A cook of Larder (Cook Profile context). */
export type CookId = Brand<string, "CookId">;
export const CookId = { of: (value: string) => value as CookId };

/** A recipe (Recipe Catalog context). */
export type RecipeId = Brand<string, "RecipeId">;
export const RecipeId = { of: (value: string) => value as RecipeId };

/** One step of a recipe (Recipe Catalog context). */
export type HowToStepId = Brand<string, "HowToStepId">;
export const HowToStepId = { of: (value: string) => value as HowToStepId };
