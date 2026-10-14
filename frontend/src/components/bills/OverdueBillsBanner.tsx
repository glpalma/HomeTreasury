import { Link } from "react-router-dom";
import { useBillStatus } from "../../hooks/useBillStatus";
import { currentMonth, formatDay } from "../../lib/months";

export function OverdueBillsBanner() {
  const status = useBillStatus(currentMonth());

  // Any error (no bank linked yet, provider down, …) → stay silent; the pages show their own message.
  const overdue = status.data?.bills.filter((bill) => bill.status === "OVERDUE") ?? [];
  if (overdue.length === 0) {
    return null;
  }

  const message =
    overdue.length === 1
      ? `${overdue[0].name} was due ${formatDay(overdue[0].payBy)} and no payment has been detected in the account yet.`
      : `${overdue.length} bills are overdue: ${overdue
          .map((bill) => `${bill.name} (due ${formatDay(bill.payBy)})`)
          .join(", ")}.`;

  return (
    <div className="border-b border-red-200 bg-red-50">
      <div className="mx-auto flex max-w-4xl items-center justify-between gap-4 px-6 py-3 text-sm text-red-800">
        <p className="font-medium">{message}</p>
        <Link to="/bills" className="shrink-0 font-medium underline">
          View bills
        </Link>
      </div>
    </div>
  );
}
