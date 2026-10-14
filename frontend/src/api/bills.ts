import { apiClient } from "./client";

export interface Bill {
  id: number;
  name: string;
  expectedAmount: number;
  /** Pay-by day of the month, 1–31. */
  dueDay: number;
  keyword: string;
  /** "YYYY-MM", first month this bill applies to. */
  startMonth: string;
  /** "YYYY-MM", last month it applies to; null while active. */
  endMonth: string | null;
}

export interface BillInput {
  name: string;
  expectedAmount: number;
  dueDay: number;
  keyword: string;
  startMonth: string;
}

export type BillStatus = "PAID" | "DUE_SOON" | "OVERDUE" | "UPCOMING";

export interface BillPayment {
  transactionId: string;
  /** "YYYY-MM-DD" */
  date: string;
  description: string | null;
  amount: number;
}

export interface BillMatch {
  /** One entry per matching transaction, oldest first. */
  payments: BillPayment[];
  /** Sum of every payment's amount. */
  amount: number;
  /** True when `amount` is more than 15% away from the expected amount. */
  amountDiffers: boolean;
}

export interface BillMonthStatus {
  billId: number;
  name: string;
  expectedAmount: number;
  dueDay: number;
  /** "YYYY-MM-DD" */
  payBy: string;
  status: BillStatus;
  match: BillMatch | null;
  /** The OWNER marked this bill paid by hand for this month. */
  manuallyMarked: boolean;
}

export interface BillStatusResponse {
  month: string;
  transactionsTruncated: boolean;
  bills: BillMonthStatus[];
}

export interface BillPreviewMatch {
  /** "YYYY-MM-DD" */
  date: string;
  description: string | null;
  amount: number;
}

export interface BillPreviewResponse {
  keyword: string;
  months: number;
  truncated: boolean;
  /** Newest first. */
  matches: BillPreviewMatch[];
  /** Null when there are no matches. */
  averageAmount: number | null;
}

export async function listBills(): Promise<Bill[]> {
  const { data } = await apiClient.get<Bill[]>("/api/bills");
  return data;
}

export async function createBill(input: BillInput): Promise<Bill> {
  const { data } = await apiClient.post<Bill>("/api/bills", input);
  return data;
}

export async function updateBill(args: { id: number; input: BillInput }): Promise<Bill> {
  const { data } = await apiClient.put<Bill>(`/api/bills/${args.id}`, args.input);
  return data;
}

export async function archiveBill(id: number): Promise<Bill> {
  const { data } = await apiClient.post<Bill>(`/api/bills/${id}/archive`);
  return data;
}

export async function getBillStatus(month: string): Promise<BillStatusResponse> {
  const { data } = await apiClient.get<BillStatusResponse>("/api/bills/status", {
    params: { month },
  });
  return data;
}

export async function setBillPaid(args: { billId: number; month: string; paid: boolean }): Promise<void> {
  await apiClient.put(`/api/bills/${args.billId}/months/${args.month}/paid`, { paid: args.paid });
}

export async function previewBill(keyword: string, months = 3): Promise<BillPreviewResponse> {
  const { data } = await apiClient.get<BillPreviewResponse>("/api/bills/preview", {
    params: { keyword, months },
  });
  return data;
}
