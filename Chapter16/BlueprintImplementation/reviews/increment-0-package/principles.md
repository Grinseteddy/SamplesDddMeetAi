# Architecture principles in scope (decision graph, all nine confirmed for Cook Assistance)

- **AP0001 — Monolith before Microservices** (testable: true): We will always start with a deployment monolith before cutting services out. However, the modules are modular regarding DDD principles.
- **AP0002 — Asynchronous Communication before Synchronous** (testable: true): For communication between Bounded Contexts asynchronous communication is preferred.
- **AP0003 — Using DDD** (testable: true): For design of our functional architecture, we will apply the principles of DDD.
- **AP0004 — Using Residuality** (testable: true): For design of our technical architecture, we will apply the principles of Residuality Theory.
- **AP0005 — Automatic Testing** (testable: true): We can test our system by 95% automatically.
- **AP0005 — Micro-UIs** (testable: true): The Bounded Context publish their own UI as micro-UIs.
- **AP0006 — Synchronous communication for UIs** (testable: true): UI communication is done synchronously.
- **AP0007 — Monitoring** (testable: true): The system can be monitored to identify system-critical states reliable and fast.
- **AP0008 — Fine grained access rights** (testable: true): Domain artifacts are controlled by a fine-grained access control.
