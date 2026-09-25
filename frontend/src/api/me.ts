import { apiClient } from "./client";

export interface MeResponse {
  email: string;
  role: "OWNER" | "VIEWER";
  home: {
    id: number;
    name: string;
  };
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export async function getMe(): Promise<MeResponse> {
  const { data } = await apiClient.get<MeResponse>("/api/me");
  return data;
}

export async function changePassword(request: ChangePasswordRequest): Promise<void> {
  await apiClient.put("/api/me/password", request);
}
