import { apiClient } from "./client";

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  homeName?: string;
  inviteCode?: string;
}

export interface LoginResponse {
  token: string;
  email: string;
  role: "OWNER" | "VIEWER";
}

export async function login(request: LoginRequest): Promise<LoginResponse> {
  const { data } = await apiClient.post<LoginResponse>("/api/auth/login", request);
  return data;
}

export async function register(request: RegisterRequest): Promise<LoginResponse> {
  const { data } = await apiClient.post<LoginResponse>("/api/auth/register", request);
  return data;
}
