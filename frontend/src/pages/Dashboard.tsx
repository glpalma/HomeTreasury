import { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { useDashboard } from "../hooks/useDashboard";
import { usePluggyItem, useLinkPluggyItem } from "../hooks/usePluggyItem";
import { StatusBadge } from "../components/StatusBadge";
import { formatCurrency, formatDate, formatPercent } from "../lib/format";

const PERIOD_OPTIONS = [7, 30, 90];

function dashboardErrorMessage(error: unknown): string {
  const status = (error as any)?.response?.status;
  const detail = (error as any)?.response?.data?.detail;

  if (status === 404) {
    return "No bank account is linked yet.";
  }
  if (status === 502) {
    return (
      detail ??
      "The bank data provider is unavailable. If you just connected an account, it may still be syncing — try again in a minute."
    );
  }
  return detail ?? "Could not load the dashboard. Please try again.";
}

export function Dashboard() {
  const { user } = useAuth();
  const [periodDays, setPeriodDays] = useState(30);

  const dashboard = useDashboard(periodDays);
  const bankAccounts = dashboard.data?.accounts.filter((a) => a.type === "BANK") ?? [];
  const creditCards = dashboard.data?.accounts.filter((a) => a.type === "CREDIT") ?? [];
  const pluggyItem = usePluggyItem();
  const { openWidget, isLinking } = useLinkPluggyItem();
  const [connectError, setConnectError] = useState<string | null>(null);

  const handleConnect = async () => {
    setConnectError(null);
    try {
      await openWidget();
    } catch (err) {
      setConnectError(err instanceof Error ? err.message : "Could not connect to your bank.");
    }
  };

  return (
    <div className="mx-auto max-w-4xl space-y-6 p-6">
      <h1 className="text-2xl font-semibold">Dashboard</h1>

      {dashboard.isLoading && <p className="text-slate-500">Loading dashboard…</p>}

      {dashboard.isError && (
        <div className="rounded-md border border-amber-300 bg-amber-50 p-4 text-sm text-amber-800">
          {dashboardErrorMessage(dashboard.error)}
        </div>
      )}

      {dashboard.data && (
        <>
          {dashboard.data.alarm.underBalance && (
            <div className="rounded-md border border-red-300 bg-red-50 p-4 text-sm font-medium text-red-800">
              After paying this cycle's credit card bill in full, this Home's balance would dip
              below its emergency reserve (ideal balance).
            </div>
          )}

          <div className="grid gap-4 sm:grid-cols-2">
            <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
              <div className="mb-2 flex items-center justify-between">
                <h2 className="text-sm font-medium text-slate-500">Balance</h2>
                <StatusBadge status={dashboard.data.status} />
              </div>
              <p className="text-3xl font-semibold">{formatCurrency(dashboard.data.currentBalance)}</p>
              <p className="mt-1 text-xs text-slate-400">
                Summed across {bankAccounts.length} bank {bankAccounts.length === 1 ? "account" : "accounts"}.
                Credit card balances are shown separately and excluded.
              </p>
              <dl className="mt-4 space-y-1 text-sm text-slate-600">
                <div className="flex justify-between">
                  <dt>Emergency reserve (ideal)</dt>
                  <dd>{formatCurrency(dashboard.data.idealBalance)}</dd>
                </div>
                <div className="flex justify-between">
                  <dt>Credit card bill due</dt>
                  <dd>{formatCurrency(dashboard.data.creditCardDue)}</dd>
                </div>
                <div className="flex justify-between border-t border-slate-100 pt-1 font-medium">
                  <dt>Available after reserve &amp; bill</dt>
                  <dd className={dashboard.data.delta < 0 ? "text-red-600" : "text-emerald-600"}>
                    {formatCurrency(dashboard.data.delta)}
                  </dd>
                </div>
              </dl>
            </div>

            <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
              <div className="mb-2 flex items-center justify-between">
                <h2 className="text-sm font-medium text-slate-500">Growth</h2>
                <select
                  value={periodDays}
                  onChange={(e) => setPeriodDays(Number(e.target.value))}
                  className="rounded-md border border-slate-300 px-2 py-1 text-xs"
                >
                  {PERIOD_OPTIONS.map((days) => (
                    <option key={days} value={days}>
                      Last {days} days
                    </option>
                  ))}
                </select>
              </div>
              <p
                className={`text-3xl font-semibold ${
                  dashboard.data.growth.rate < 0 ? "text-red-600" : "text-emerald-600"
                }`}
              >
                {formatPercent(dashboard.data.growth.rate)}
              </p>
              <dl className="mt-4 space-y-1 text-sm text-slate-600">
                <div className="flex justify-between">
                  <dt>Net change</dt>
                  <dd>{formatCurrency(dashboard.data.growth.netChange)}</dd>
                </div>
                <div className="flex justify-between">
                  <dt>Period</dt>
                  <dd>{dashboard.data.growth.periodDays} days</dd>
                </div>
              </dl>
            </div>
          </div>

          <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
            <h2 className="mb-3 text-sm font-medium text-slate-500">
              Bank accounts feeding this balance ({bankAccounts.length})
            </h2>
            {bankAccounts.length === 0 ? (
              <p className="text-sm text-slate-500">No bank accounts returned for this connection.</p>
            ) : (
              <ul className="divide-y divide-slate-100">
                {bankAccounts.map((account) => (
                  <li key={account.id} className="flex items-center justify-between py-2 text-sm">
                    <p className="font-medium text-slate-800">{account.name}</p>
                    <p className="font-medium text-slate-700">
                      {formatCurrency(account.balance, account.currencyCode)}
                    </p>
                  </li>
                ))}
              </ul>
            )}
          </div>

          {creditCards.length > 0 && (
            <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
              <h2 className="mb-1 text-sm font-medium text-slate-500">
                Credit cards ({creditCards.length})
              </h2>
              <p className="mb-3 text-xs text-slate-400">
                Amount due on the next invoice — a liability, not spendable cash, so it is not
                included in the balance above.
              </p>
              <ul className="divide-y divide-slate-100">
                {creditCards.map((account) => (
                  <li key={account.id} className="py-2 text-sm">
                    <div className="flex items-center justify-between">
                      <p className="font-medium text-slate-800">{account.name}</p>
                      <p className="font-medium text-amber-700">
                        {formatCurrency(account.billDue ?? account.balance, account.currencyCode)} due
                        {account.billDueDate && ` (${formatDate(account.billDueDate)})`}
                      </p>
                    </div>
                    <p className="mt-1 text-xs text-slate-400">
                      Raw account balance (used limit, may include future installments):{" "}
                      {formatCurrency(account.balance, account.currencyCode)}
                    </p>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </>
      )}

      <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
        <h2 className="mb-2 text-sm font-medium text-slate-500">Bank connection</h2>
        {pluggyItem.isLoading && <p className="text-sm text-slate-500">Checking connection…</p>}
        {!pluggyItem.isLoading && pluggyItem.data && (
          <p className="text-sm text-slate-700">
            Connected to <span className="font-medium">{pluggyItem.data.connectorName ?? "your bank"}</span> since{" "}
            {formatDate(pluggyItem.data.linkedAt)}.
          </p>
        )}
        {!pluggyItem.isLoading && !pluggyItem.data && (
          <div className="flex items-center justify-between">
            <p className="text-sm text-slate-600">No bank account connected yet.</p>
            {user?.role === "OWNER" ? (
              <button
                onClick={handleConnect}
                disabled={isLinking}
                className="rounded-md bg-slate-900 px-3 py-1.5 text-sm font-medium text-white hover:bg-slate-700 disabled:opacity-50"
              >
                {isLinking ? "Connecting…" : "Connect your bank"}
              </button>
            ) : (
              <p className="text-sm text-slate-500">Ask the Home owner to connect a bank account.</p>
            )}
          </div>
        )}
        {connectError && <p className="mt-2 text-sm text-red-600">{connectError}</p>}
      </div>
    </div>
  );
}
