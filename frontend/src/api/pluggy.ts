import { apiClient } from "./client";

export interface PluggyItemResponse {
  itemId: string;
  connectorName: string | null;
  linkedAt: string;
  /** Pluggy's item state, e.g. "UPDATED", "UPDATING", "LOGIN_ERROR", "OUTDATED". Null if unknown. */
  status: string | null;
  /** Outcome of the last sync attempt, e.g. "SUCCESS". Null if unknown. */
  executionStatus: string | null;
  /** ISO instant Pluggy last finished fetching data; null if it never has. */
  lastUpdatedAt: string | null;
}

export interface PluggyConnectTokenResponse {
  accessToken: string;
}

export async function getPluggyItem(): Promise<PluggyItemResponse | null> {
  try {
    const { data } = await apiClient.get<PluggyItemResponse>("/api/pluggy/item");
    return data;
  } catch (error: any) {
    if (error.response?.status === 404) {
      return null;
    }
    throw error;
  }
}

export async function createConnectToken(): Promise<PluggyConnectTokenResponse> {
  const { data } = await apiClient.post<PluggyConnectTokenResponse>("/api/pluggy/connect-token");
  return data;
}

export async function linkPluggyItem(itemId: string): Promise<PluggyItemResponse> {
  const { data } = await apiClient.post<PluggyItemResponse>("/api/pluggy/item", { itemId });
  return data;
}
