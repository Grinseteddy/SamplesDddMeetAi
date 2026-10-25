import { processedMessagesContract } from "../contracts/processed-messages.contract.js";
import { InMemoryProcessedMessages } from "./in-memory-processed-messages.js";

// The in-memory fake of ProcessedMessages must pass the shared contract (step 8, unit 1c).
processedMessagesContract("in-memory fake", async () => new InMemoryProcessedMessages());
