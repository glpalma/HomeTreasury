import { useState } from "react";
import { BillForm } from "../components/bills/BillForm";
import { BillsTable } from "../components/bills/BillsTable";
import { SummaryStrip } from "../components/bills/SummaryStrip";
import { IncomesPanel } from "../components/incomes/IncomesPanel";
import { MonthPicker } from "../components/MonthPicker";
import { useAuth } from "../context/AuthContext";
import { useBills, useCreateBill } from "../hooks/useBills";
import { useBillStatus } from "../hooks/useBillStatus";
import { useIncomeStatus } from "../hooks/useIncomes";
import { apiErrorMessage } from "../lib/errors";
import { currentMonth } from "../lib/months";

export function Bills() {
  const { user } = useAuth();
  const isOwner = user?.role === "OWNER";
  const [month, setMonth] = useState(currentMonth());
  const status = useBillStatus(month);
  const incomeStatus = useIncomeStatus(month);
  const definitions = useBills();
  const createBill = useCreateBill();

  return (
    <div className="mx-auto max-w-4xl space-y-6 p-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">Bills</h1>
        <MonthPicker value={month} onChange={setMonth} />
      </div>

      {status.isLoading && <p className="text-slate-500">Checking bills against your bank account…</p>}

      {status.isError && (
        <div className="rounded-md border border-amber-300 bg-amber-50 p-4 text-sm text-amber-800">
          {apiErrorMessage(status.error, "Could not load bills.")}
        </div>
      )}

      {status.data?.transactionsTruncated && (
        <div className="rounded-md border border-amber-300 bg-amber-50 p-4 text-sm text-amber-800">
          This month has more transactions than we can load at once, so some bills may look unpaid by mistake.
        </div>
      )}

      <SummaryStrip bills={status.data} incomes={incomeStatus.data} />

      {status.data && (
        <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
          <h2 className="mb-3 text-sm font-medium text-slate-500">Bills this month</h2>
          <BillsTable month={month} bills={status.data.bills} definitions={definitions.data ?? []} />
        </div>
      )}

      {isOwner && (
        <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
          <h2 className="mb-3 text-sm font-medium text-slate-500">Add a bill</h2>
          <BillForm
            submitLabel="Add bill"
            resetOnSuccess
            onSubmit={(input) => createBill.mutateAsync(input)}
          />
        </div>
      )}

      <IncomesPanel month={month} />
    </div>
  );
}
