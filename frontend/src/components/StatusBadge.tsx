import type { HealthStatus } from "../api/treasury";

const STYLES: Record<HealthStatus, string> = {
  ABOVE: "bg-emerald-100 text-emerald-800",
  AT: "bg-slate-100 text-slate-700",
  BELOW: "bg-red-100 text-red-800",
};

const LABELS: Record<HealthStatus, string> = {
  ABOVE: "Above target",
  AT: "At target",
  BELOW: "Below target",
};

export function StatusBadge({ status }: { status: HealthStatus }) {
  return (
    <span className={`rounded-full px-3 py-1 text-xs font-semibold ${STYLES[status]}`}>
      {LABELS[status]}
    </span>
  );
}
