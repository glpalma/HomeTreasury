import { apiClient } from "./client";

export interface InviteResponse {
  code: string;
  expiresAt: string;
}

export async function setIdealBalance(idealBalance: number): Promise<void> {
  await apiClient.put("/api/home/idealBalance", { idealBalance });
}

export async function createInvite(): Promise<InviteResponse> {
  const { data } = await apiClient.post<InviteResponse>("/api/home/invites");
  return data;
}
