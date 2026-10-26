/**
 * The service does not serve this topic yet: topics are built one increment at a time, Preparation step
 * first (spec 2.0.0 r2, `TopicNotYetSupported`). The HTTP adapter answers 422, the consumer refuses the message.
 */
export class TopicNotYetSupported extends Error {
  constructor(readonly topic: string) {
    super(`help with ${topic} is not offered yet`);
    this.name = "TopicNotYetSupported";
  }
}
