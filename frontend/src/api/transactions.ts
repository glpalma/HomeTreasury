import { apiClient } from "./client";

export interface BankTransaction {
  id: string;
  accountId: string;
  accountName: string;
  /** Calendar date, "YYYY-MM-DD". */
  date: string;
  description: string | null;
  counterpartName: string | null;
  /** Negative = money out, positive = money in. */
  amount: number;
  type: "DEBIT" | "CREDIT";
  status: "POSTED" | "PENDING" | null;
  operationType: string | null;
}

export interface TransactionsResponse {
  month: string;
  /** True when Pluggy has more transactions than the first page we read. */
  truncated: boolean;
  transactions: BankTransaction[];
}

export async function getTransactions(month: string): Promise<TransactionsResponse> {
  const { data } = await apiClient.get<TransactionsResponse>("/api/treasury/transactions", {
    params: { month },
  });
  return data;
}
