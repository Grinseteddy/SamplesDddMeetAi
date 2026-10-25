// ADR-CA-011 · coverage gate: every receive operation of the AsyncAPI spec has a consumer (or a planned entry).
export const CONSUMERS: Readonly<Record<string, string>> = {
  receiveGrandmaAvatarHelpProvided: "GrandmaAvatarHelpProvidedConsumer (queue cook-assistance.grandma-avatar-help-provided)",
};
