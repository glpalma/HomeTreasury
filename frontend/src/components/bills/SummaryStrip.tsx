import type { BillStatusResponse } from "../../api/bills";
import type { IncomeStatusResponse } from "../../api/incomes";
import { computeCoverage } from "../../lib/coverage";
import { formatCurrency } from "../../lib/format";

interface Props {
  bills: BillStatusResponse | undefined;
  /** Undefined while loading or when the income request failed. */
  incomes: IncomeStatusResponse | undefined;
}

const cardClass = "rounded-lg border border-slate-200 bg-white p-5 shadow-sm";

export function SummaryStrip({ bills, incomes }: Props) {
  if (!bills) {
    return null;
  }

  const paidCount = bills.bills.filter((b) => b.status === "PAID").length;
  const coverage = incomes ? computeCoverage(bills.bills, incomes) : null;
  const hasIncomes = incomes ? incomes.incomes.length > 0 : false;

  return (
    <div className="grid gap-4 sm:grid-cols-3">
      <div className={cardClass}>
        <p className="text-sm font-medium text-slate-500">Bills paid</p>
        <p className="mt-1 text-3xl font-semibold">
          {paidCount} <span className="text-lg font-normal text-slate-400">of {bills.bills.length}</span>
        </p>
      </div>

      <div className={cardClass}>
        <p className="text-sm font-medium text-slate-500">Expected bills total</p>
        <p className="mt-1 text-3xl font-semibold">
          {formatCurrency(coverage?.billsExpectedTotal ?? bills.bills.reduce((s, b) => s + b.expectedAmount, 0))}
        </p>
      </div>

      <div className={cardClass}>
        <p className="text-sm font-medium text-slate-500">Money in this month</p>
        {!incomes && <p className="mt-2 text-sm text-slate-500">Unavailable right now.</p>}
        {incomes && !hasIncomes && (
          <p className="mt-2 text-sm text-slate-500">No expected incomes apply to this month.</p>
        )}
        {incomes && hasIncomes && coverage && (
          <>
            <p className="mt-1 text-3xl font-semibold">
              {formatCurrency(coverage.incomesReceivedTotal)}{" "}
              <span className="text-lg font-normal text-slate-400">
                of {formatCurrency(coverage.incomesExpectedTotal)}
              </span>
            </p>
            <div className="mt-2 h-2 overflow-hidden rounded-full bg-slate-100">
              <div
                className="h-full bg-emerald-500"
                style={{
                  width: `${Math.min(
                    100,
                    coverage.incomesExpectedTotal > 0
                      ? (coverage.incomesReceivedTotal / coverage.incomesExpectedTotal) * 100
                      : 0
                  )}%`,
                }}
              />
            </div>
            <p className={`mt-2 text-xs font-medium ${coverage.covers ? "text-emerald-700" : "text-amber-700"}`}>
              {coverage.covers
                ? "Covers this month's bills."
                : `${formatCurrency(coverage.shortBy)} still needed to cover this month's bills.`}
            </p>
          </>
        )}
      </div>
    </div>
  );
}
