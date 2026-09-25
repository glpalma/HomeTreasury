import { apiClient } from "./client";

export type HealthStatus = "ABOVE" | "AT" | "BELOW";

export interface AccountSummary {
  id: string;
  name: string;
  type: string;
  balance: number;
  currencyCode: string;
  /**
   * CREDIT accounts only: the amount due on the invoice with the closest upcoming due date,
   * resolved from Pluggy's Bills product. Falls back to `balance` when the institution doesn't
   * support Bills. Null for BANK accounts.
   */
  billDue: number | null;
  /** ISO date string for the due date `billDue` was resolved from. Null if unresolved/BANK. */
  billDueDate: string | null;
}

export interface DashboardResponse {
  currentBalance: number;
  idealBalance: number;
  /** Sum of this cycle's due amount across all CREDIT-type accounts (this month's card bill). */
  creditCardDue: number;
  /**
   * currentBalance - idealBalance - creditCardDue: what's left after keeping the emergency
   * reserve intact AND paying this cycle's credit card bill in full.
   */
  delta: number;
  status: HealthStatus;
  growth: {
    periodDays: number;
    netChange: number;
    rate: number;
  };
  alarm: {
    underBalance: boolean;
  };
  accounts: AccountSummary[];
}

export async function getDashboard(periodDays: number): Promise<DashboardResponse> {
  const { data } = await apiClient.get<DashboardResponse>("/api/treasury/dashboard", {
    params: { periodDays },
  });
  return data;
}
