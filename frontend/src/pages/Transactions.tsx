import { useState } from "react";
import { MonthPicker } from "../components/MonthPicker";
import { useTransactions } from "../hooks/useTransactions";
import { apiErrorMessage } from "../lib/errors";
import { formatCurrency } from "../lib/format";
import { currentMonth, formatDay } from "../lib/months";

export function Transactions() {
  const [month, setMonth] = useState(currentMonth());
  const transactions = useTransactions(month);

  return (
    <div className="mx-auto max-w-4xl space-y-6 p-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">Transactions</h1>
        <MonthPicker value={month} onChange={setMonth} />
      </div>

      {transactions.isLoading && <p className="text-slate-500">Loading transactions…</p>}

      {transactions.isError && (
        <div className="rounded-md border border-amber-300 bg-amber-50 p-4 text-sm text-amber-800">
          {apiErrorMessage(transactions.error, "Could not load transactions.")}
        </div>
      )}

      {transactions.data?.truncated && (
        <div className="rounded-md border border-amber-300 bg-amber-50 p-4 text-sm text-amber-800">
          This month has more transactions than we can load at once, so the list below is incomplete.
        </div>
      )}

      {transactions.data && (
        <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
          {transactions.data.transactions.length === 0 ? (
            <p className="text-sm text-slate-500">No transactions in this month.</p>
          ) : (
            <table className="w-full text-left text-sm">
              <thead className="text-xs uppercase text-slate-400">
                <tr>
                  <th className="pb-2 font-medium">Date</th>
                  <th className="pb-2 font-medium">Description</th>
                  <th className="pb-2 font-medium">Type</th>
                  <th className="pb-2 text-right font-medium">Amount</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {transactions.data.transactions.map((tx) => (
                  <tr key={tx.id}>
                    <td className="py-2 pr-3 text-slate-600">{formatDay(tx.date)}</td>
                    <td className="py-2 pr-3">
                      <p className="font-medium text-slate-800">{tx.description ?? "—"}</p>
                      {tx.counterpartName && (
                        <p className="text-xs text-slate-400">
                          {tx.type === "CREDIT" ? "from" : "to"} {tx.counterpartName}
                        </p>
                      )}
                    </td>
                    <td className="py-2 pr-3 text-xs text-slate-500">
                      {tx.operationType ?? "—"}
                      {tx.status === "PENDING" && (
                        <span className="ml-2 rounded-full bg-amber-100 px-2 py-0.5 text-amber-800">pending</span>
                      )}
                    </td>
                    <td
                      className={`py-2 text-right font-medium ${
                        tx.amount < 0 ? "text-red-600" : "text-emerald-600"
                      }`}
                    >
                      {formatCurrency(tx.amount)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}
    </div>
  );
}
