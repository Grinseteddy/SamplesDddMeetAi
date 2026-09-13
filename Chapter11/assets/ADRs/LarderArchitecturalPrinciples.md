# AP0001—Monolith before Microservices

**Statement:** We will always start with a deployment monolith before cutting services out. However, the modules are modular regarding DDD principles.

**Rational:** In the beginning, we do not know exactly the load and performance issues the single services have to face. Therefore, we will cut later with more experience.

**Implications:** We will have later costs for cutting out services. The teams need to be disciplined to follow the strict boundaries of the Bounded Contexts.

**Status:** Adopted

**Last amended:** 2026-09-10

# AP0002 —Asynchronous Communication before Synchronous

**Statement:** For communication between Bounded Contexts asynchronous communication is preferred.

**Rational:** Asynchronous communication guarantees the loose coupling between Bounded Contexts.

**Implications:** Loose coupling guarantees that we can later maintain and operate our application without large downtimes.

**Status:** Adopted

**Last amended:** 2026-09-10

# AP0003 —Using DDD

**Statement:** For design of our functional architecture, we will apply the principles of DDD.

**Rational:** Architecture coming out of the blue does not support the business requirements functionally.

**Implications:** We need to support the functions required by business. Moreover, we need to support the maintenance of those functions for the upcoming years.

**Status:** Adopted

**Last amended:** 2026-09-10

# AP0004 —Using Residuality

**Statement:** For design of our technical architecture, we will apply the principles of Residuality Theory.

**Rational:** Stressing an architecture with randomly selected stressors creates more sustaining architectures.

**Implications:** We do not know what future brings, therefore, we need the robust and best equipped architecture.

**Status:** Adopted

**Last amended:** 2026-09-10

# AP0005—Automatic Testing

**Statement:** We can test our system by 95% automatically.

**Rational:** Manual tests are too slow to allow us to test our system manually.

**Implications:** We need to follow a Test-Driven-Approach consequently.

**Status:** Adopted

**Last amended:** 2026-09-10


# AP0005 —Micro-UIs

**Statement:** The Bounded Context publish their own UI as micro-UIs.

**Rational:** The teams developing the Bounded Contexts must be as independent as possible. The UIs are part of the Bounded Contexts projects. The micro UIs can use a common platform, so that they appear out of one hand.

**Implications:** Using a large monolithic frontend would us slow down.

**Status:** Adopted

**Last amended:** 2026-09-10

# AP0006 —Synchronous communication for UIS

**Statement:** UI communication is done synchronously.

**Rational:** UIs require synchronous communication.

**Implications:** UIs must communicate with humans, who expect to get immediate answers.

**Status:** Adopted

**Last amended:** 2026-09-10

# AP0007 —Monitoring

**Statement:** The system can be monitored to identify system-critical states reliable and fast.

**Rational:** The operation of the software must be almost completely automated. We need to get a reliable alarm system based on a reliable monitoring system

**Implications:** Using a large monolithic frontend would us slow down.

**Status:** Adopted

**Last amended:** 2026-09-10

# AP0008 —Fine grained access rights

**Statement:** Domain artifacts are controlled by a fine-grained access control.

**Rational:** The software will increase with time, and we need to respect the personal and author rights of our users.

**Implications:** With more coarse grained rights, we could contradict legal regulations.

**Status:** Adopted

**Last amended:** 2026-09-10

