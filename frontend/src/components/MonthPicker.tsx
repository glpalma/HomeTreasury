import { currentMonth, formatMonth, shiftMonth } from "../lib/months";

// Pluggy only keeps ~12 months of history, so browsing further back would just show empty months.
const MAX_MONTHS_BACK = 12;

interface Props {
  value: string;
  onChange: (month: string) => void;
}

export function MonthPicker({ value, onChange }: Props) {
  const oldest = shiftMonth(currentMonth(), -MAX_MONTHS_BACK);
  const buttonClass =
    "rounded-md border border-slate-300 px-3 py-1.5 text-sm font-medium text-slate-700 hover:bg-slate-100 disabled:opacity-40";

  return (
    <div className="flex items-center gap-2">
      <button
        type="button"
        className={buttonClass}
        disabled={value <= oldest}
        onClick={() => onChange(shiftMonth(value, -1))}
        aria-label="Previous month"
      >
        ←
      </button>
      <span className="min-w-36 text-center text-sm font-medium text-slate-800">{formatMonth(value)}</span>
      <button
        type="button"
        className={buttonClass}
        onClick={() => onChange(shiftMonth(value, 1))}
        aria-label="Next month"
      >
        →
      </button>
    </div>
  );
}
