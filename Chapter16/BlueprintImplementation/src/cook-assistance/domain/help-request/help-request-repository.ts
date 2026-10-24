import type { HelpRequest, HelpRequestId } from "./help-request.js";

/** Where help requests are kept (port; implemented by an adapter in adapter/out/persistence). */
export interface HelpRequestRepository {
  /** The help request with this id, or undefined if there is none. */
  findById(id: HelpRequestId): Promise<HelpRequest | undefined>;
}
