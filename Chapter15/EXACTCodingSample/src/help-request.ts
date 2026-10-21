export interface HelpRequest {
  chef: string;
  status: "open" | "cancelled";
  requestedAt: string;
  cancelledBy?: "system";
}

export interface Rejection {
  outcome: "rejected";
  reason: string;
}

export function rejected(reason: string): Rejection {
  return { outcome: "rejected", reason };
}
