import { helpRequestRepositoryContract } from "../contracts/help-request-repository.contract.js";
import { InMemoryHelpRequestRepository } from "./in-memory-help-request-repository.js";

// The in-memory fake of HelpRequestRepository must pass the shared contract (step 8, unit 1a).
helpRequestRepositoryContract("in-memory fake", async () => {
  const repository = new InMemoryHelpRequestRepository();
  return { repository, savedEventTypes: async () => repository.savedEvents.map((event) => event.type) };
});
