import { useState } from "react";
import type { Bill, BillMonthStatus } from "../../api/bills";
import { useAuth } from "../../context/AuthContext";
import { useArchiveBill, useSetBillPaid, useUpdateBill } from "../../hooks/useBills";
import { formatCurrency } from "../../lib/format";
import { formatDay, formatMonth } from "../../lib/months";
import { BillForm } from "./BillForm";
import { BillStatusChip } from "./BillStatusChip";

interface Props {
  month: string;
  bills: BillMonthStatus[];
  /** Full definitions (keyword, start/end month) — needed to pre-fill the edit form. */
  definitions: Bill[];
}

export function BillsTable({ month, bills, definitions }: Props) {
  const { user } = useAuth();
  const isOwner = user?.role === "OWNER";
  const setPaid = useSetBillPaid();
  const updateBill = useUpdateBill();
  const archiveBill = useArchiveBill();
  const [editingId, setEditingId] = useState<number | null>(null);

  if (bills.length === 0) {
    return <p className="text-sm text-slate-500">No bills apply to this month.</p>;
  }

  const handleArchive = (bill: BillMonthStatus) => {
    const ok = window.confirm(
      `Archive "${bill.name}"? It will stop appearing from next month on. Past months keep it.`
    );
    if (ok) {
      archiveBill.mutate(bill.billId);
    }
  };

  return (
    <table className="w-full text-left text-sm">
      <thead className="text-xs uppercase text-slate-400">
        <tr>
          <th className="pb-2 font-medium">Status</th>
          <th className="pb-2 font-medium">Bill</th>
          <th className="pb-2 font-medium">Pay by</th>
          <th className="pb-2 text-right font-medium">Expected</th>
          <th className="pb-2 pl-4 font-medium">Found in bank</th>
          <th className="pb-2 pl-4 text-center font-medium">Paid manually</th>
          {isOwner && <th className="pb-2 pl-4 font-medium" />}
        </tr>
      </thead>
      <tbody className="divide-y divide-slate-100">
        {bills.map((bill) => {
          const definition = definitions.find((d) => d.id === bill.billId);

          if (editingId === bill.billId && definition) {
            return (
              <tr key={bill.billId}>
                <td colSpan={isOwner ? 7 : 6} className="py-3">
                  <p className="mb-2 text-xs text-slate-400">
                    Changes apply to every month, including past ones — status is always judged with the bill as
                    defined now.
                  </p>
                  <BillForm
                    initial={{
                      name: definition.name,
                      expectedAmount: definition.expectedAmount,
                      dueDay: definition.dueDay,
                      keyword: definition.keyword,
                      startMonth: definition.startMonth,
                    }}
                    submitLabel="Save changes"
                    onSubmit={async (input) => {
                      await updateBill.mutateAsync({ id: bill.billId, input });
                      setEditingId(null);
                    }}
                    onCancel={() => setEditingId(null)}
                  />
                </td>
              </tr>
            );
          }

          return (
            <tr key={bill.billId}>
              <td className="py-2 pr-3">
                <BillStatusChip status={bill.status} />
              </td>
              <td className="py-2 pr-3 font-medium text-slate-800">
                {bill.name}
                {definition?.endMonth && (
                  <p className="text-xs font-normal text-slate-400">archived · ends {formatMonth(definition.endMonth)}</p>
                )}
              </td>
              <td className="py-2 pr-3 text-slate-600">{formatDay(bill.payBy)}</td>
              <td className="py-2 text-right text-slate-700">{formatCurrency(bill.expectedAmount)}</td>
              <td className="py-2 pl-4 text-xs text-slate-500">
                {bill.match ? (
                  <>
                    <span className="text-sm font-medium text-slate-700">{formatCurrency(bill.match.amount)}</span>{" "}
                    on {formatDay(bill.match.payments[bill.match.payments.length - 1].date)}
                    {bill.match.amountDiffers && (
                      <span className="ml-2 rounded-full bg-amber-100 px-2 py-0.5 text-amber-800">amount differs</span>
                    )}
                    <p className="text-slate-400">
                      {bill.match.payments.length > 1
                        ? `${bill.match.payments.length} payments`
                        : bill.match.payments[0].description}
                    </p>
                  </>
                ) : (
                  "—"
                )}
              </td>
              <td className="py-2 pl-4 text-center">
                <input
                  type="checkbox"
                  aria-label={`Mark ${bill.name} as paid manually`}
                  checked={bill.manuallyMarked}
                  disabled={!isOwner || setPaid.isPending}
                  onChange={(e) => setPaid.mutate({ billId: bill.billId, month, paid: e.target.checked })}
                />
              </td>
              {isOwner && (
                <td className="py-2 pl-4 text-right text-xs">
                  <button
                    type="button"
                    className="font-medium text-slate-600 hover:underline"
                    onClick={() => setEditingId(bill.billId)}
                  >
                    Edit
                  </button>
                  {!definition?.endMonth && (
                    <button
                      type="button"
                      className="ml-3 font-medium text-red-600 hover:underline"
                      disabled={archiveBill.isPending}
                      onClick={() => handleArchive(bill)}
                    >
                      Archive
                    </button>
                  )}
                </td>
              )}
            </tr>
          );
        })}
      </tbody>
    </table>
  );
}
