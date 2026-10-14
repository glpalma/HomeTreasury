import { apiClient } from "./client";
import type { BillMatch } from "./bills";

export interface ExpectedIncome {
  id: number;
  name: string;
  expectedAmount: number;
  /** Day of the month the money is expected by, 1–31. */
  expectedDay: number;
  keyword: string;
  /** "YYYY-MM" */
  startMonth: string;
  /** "YYYY-MM" or null while active. */
  endMonth: string | null;
}

export interface ExpectedIncomeInput {
  name: string;
  expectedAmount: number;
  expectedDay: number;
  keyword: string;
  startMonth: string;
}

export type IncomeStatus = "RECEIVED" | "EXPECTED" | "MISSING";

export interface IncomeMonthStatus {
  incomeId: number;
  name: string;
  expectedAmount: number;
  expectedDay: number;
  /** "YYYY-MM-DD" */
  expectedBy: string;
  status: IncomeStatus;
  /** Same shape as a bill match: the CREDIT transaction found, or null. */
  match: BillMatch | null;
}

export interface IncomeStatusResponse {
  month: string;
  transactionsTruncated: boolean;
  /** Sum of the expected amounts of every income active in the month. */
  expectedTotal: number;
  /** Sum of the amounts actually found. */
  receivedTotal: number;
  incomes: IncomeMonthStatus[];
}

export async function listIncomes(): Promise<ExpectedIncome[]> {
  const { data } = await apiClient.get<ExpectedIncome[]>("/api/incomes");
  return data;
}

export async function createIncome(input: ExpectedIncomeInput): Promise<ExpectedIncome> {
  const { data } = await apiClient.post<ExpectedIncome>("/api/incomes", input);
  return data;
}

export async function getIncomeStatus(month: string): Promise<IncomeStatusResponse> {
  const { data } = await apiClient.get<IncomeStatusResponse>("/api/incomes/status", {
    params: { month },
  });
  return data;
}
