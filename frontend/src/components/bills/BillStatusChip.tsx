import type { BillStatus } from "../../api/bills";

const STYLES: Record<BillStatus, string> = {
  PAID: "bg-emerald-100 text-emerald-800",
  DUE_SOON: "bg-amber-100 text-amber-800",
  OVERDUE: "bg-red-100 text-red-800",
  UPCOMING: "bg-slate-100 text-slate-600",
};

const LABELS: Record<BillStatus, string> = {
  PAID: "Paid",
  DUE_SOON: "Due soon",
  OVERDUE: "Overdue",
  UPCOMING: "Upcoming",
};

export function BillStatusChip({ status }: { status: BillStatus }) {
  return (
    <span className={`rounded-full px-3 py-1 text-xs font-semibold ${STYLES[status]}`}>{LABELS[status]}</span>
  );
}
