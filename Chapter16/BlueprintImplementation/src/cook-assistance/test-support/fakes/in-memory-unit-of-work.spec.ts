import { unitOfWorkContract } from "../contracts/unit-of-work.contract.js";
import { InMemoryUnitOfWork } from "./in-memory-unit-of-work.js";

// The in-memory fake of UnitOfWork must pass the shared contract (step 8, unit 1d).
unitOfWorkContract("in-memory fake", async () => new InMemoryUnitOfWork());
