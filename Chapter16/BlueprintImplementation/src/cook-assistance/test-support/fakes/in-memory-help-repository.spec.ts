import { helpRepositoryContract } from "../contracts/help-repository.contract.js";
import { InMemoryHelpRepository } from "./in-memory-help-repository.js";

// The in-memory fake of HelpRepository must pass the shared contract (step 8, unit 1b).
helpRepositoryContract("in-memory fake", async () => {
  const repository = new InMemoryHelpRepository();
  return { repository, savedEventTypes: async () => repository.savedEvents.map((event) => event.type) };
});
