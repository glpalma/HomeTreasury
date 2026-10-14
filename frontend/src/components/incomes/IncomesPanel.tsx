import type { IncomeStatus } from "../../api/incomes";
import { useAuth } from "../../context/AuthContext";
import { useCreateIncome, useIncomeStatus } from "../../hooks/useIncomes";
import { apiErrorMessage } from "../../lib/errors";
import { formatCurrency } from "../../lib/format";
import { formatDay } from "../../lib/months";
import { IncomeForm } from "./IncomeForm";

const STATUS_STYLES: Record<IncomeStatus, string> = {
  RECEIVED: "bg-emerald-100 text-emerald-800",
  EXPECTED: "bg-slate-100 text-slate-600",
  MISSING: "bg-red-100 text-red-800",
};

const STATUS_LABELS: Record<IncomeStatus, string> = {
  RECEIVED: "Received",
  EXPECTED: "Expected",
  MISSING: "Missing",
};

interface Props {
  month: string;
}

export function IncomesPanel({ month }: Props) {
  const { user } = useAuth();
  const isOwner = user?.role === "OWNER";
  const status = useIncomeStatus(month);
  const createIncome = useCreateIncome();

  const incomes = status.data?.incomes ?? [];

  return (
    <div className="space-y-4 rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex items-center justify-between">
        <h2 className="text-sm font-medium text-slate-500">Expected incomes this month</h2>
        {status.data && incomes.length > 0 && (
          <p className="text-xs text-slate-500">
            {formatCurrency(status.data.receivedTotal)} of {formatCurrency(status.data.expectedTotal)} received
          </p>
        )}
      </div>

      {status.isError && (
        <p className="text-sm text-amber-800">{apiErrorMessage(status.error, "Could not load incomes.")}</p>
      )}
      {status.isLoading && <p className="text-sm text-slate-500">Checking incoming money…</p>}
      {status.data?.transactionsTruncated && (
        <p className="text-xs text-amber-700">
          Some transactions could not be loaded, so an income may look missing by mistake.
        </p>
      )}
      {status.data && incomes.length === 0 && (
        <p className="text-sm text-slate-500">No expected incomes apply to this month.</p>
      )}

      {incomes.length > 0 && (
        <ul className="divide-y divide-slate-100">
          {incomes.map((income) => (
            <li key={income.incomeId} className="flex items-center justify-between gap-3 py-2 text-sm">
              <div>
                <p className="font-medium text-slate-800">{income.name}</p>
                {income.match ? (
                  <p className="text-xs text-slate-400">
                    {income.match.payments.length > 1
                      ? `${income.match.payments.length} transfers · ${formatCurrency(income.match.amount)} total`
                      : `Arrived ${formatDay(income.match.payments[0].date)} · ${formatCurrency(income.match.amount)}`}
                    {income.match.amountDiffers && (
                      <span className="ml-2 rounded-full bg-amber-100 px-2 py-0.5 text-amber-800">amount differs</span>
                    )}
                  </p>
                ) : (
                  <p className="text-xs text-slate-400">Expected by {formatDay(income.expectedBy)}</p>
                )}
              </div>
              <div className="flex items-center gap-3">
                <span className="text-slate-600">{formatCurrency(income.expectedAmount)}</span>
                <span className={`rounded-full px-3 py-1 text-xs font-semibold ${STATUS_STYLES[income.status]}`}>
                  {STATUS_LABELS[income.status]}
                </span>
              </div>
            </li>
          ))}
        </ul>
      )}

      {isOwner && (
        <div className="border-t border-slate-100 pt-4">
          <h3 className="mb-3 text-sm font-medium text-slate-500">Add an expected income</h3>
          <IncomeForm submitLabel="Add income" onSubmit={(input) => createIncome.mutateAsync(input)} />
        </div>
      )}
    </div>
  );
}
