import { useState, type FormEvent } from "react";
import type { BillInput } from "../../api/bills";
import { currentMonth } from "../../lib/months";
import { BillPreviewPanel } from "./BillPreviewPanel";

interface Props {
  /** Pre-fills the form (edit mode). Omit for a blank "add" form. */
  initial?: BillInput;
  submitLabel: string;
  /** Resolves when saved; rejects (axios error) when the server refuses. */
  onSubmit: (input: BillInput) => Promise<unknown>;
  onCancel?: () => void;
  /** Clear the fields after a successful save (add mode). */
  resetOnSuccess?: boolean;
}

const inputClass =
  "w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-slate-500 focus:outline-none";

export function BillForm({ initial, submitLabel, onSubmit, onCancel, resetOnSuccess }: Props) {
  const [name, setName] = useState(initial?.name ?? "");
  const [expectedAmount, setExpectedAmount] = useState(initial ? String(initial.expectedAmount) : "");
  const [dueDay, setDueDay] = useState(initial ? String(initial.dueDay) : "");
  const [keyword, setKeyword] = useState(initial?.keyword ?? "");
  const [startMonth, setStartMonth] = useState(initial?.startMonth ?? currentMonth());
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setSaving(true);
    setError(null);
    try {
      await onSubmit({
        name: name.trim(),
        expectedAmount: Number(expectedAmount),
        dueDay: Number(dueDay),
        keyword: keyword.trim(),
        startMonth,
      });
      if (resetOnSuccess) {
        setName("");
        setExpectedAmount("");
        setDueDay("");
        setKeyword("");
        setStartMonth(currentMonth());
      }
    } catch (err) {
      const detail = (err as any)?.response?.data?.detail;
      setError(detail ?? "Could not save the bill.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-3">
      <div>
        <label className="mb-1 block text-sm font-medium text-slate-700">Name</label>
        <input required className={inputClass} value={name} onChange={(e) => setName(e.target.value)} />
      </div>
      <div className="grid grid-cols-2 gap-3">
        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Expected amount</label>
          <input
            required
            type="number"
            step="0.01"
            min="0.01"
            className={inputClass}
            value={expectedAmount}
            onChange={(e) => setExpectedAmount(e.target.value)}
          />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Pay by day of month</label>
          <input
            required
            type="number"
            min="1"
            max="31"
            className={inputClass}
            value={dueDay}
            onChange={(e) => setDueDay(e.target.value)}
          />
        </div>
      </div>
      <div>
        <label className="mb-1 block text-sm font-medium text-slate-700">
          Keyword to find in the bank description
        </label>
        <input
          required
          className={inputClass}
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          placeholder="e.g. ENEL"
        />
        <p className="mt-1 text-xs text-slate-400">
          Use text you saw on the Transactions page. Case and accents are ignored.
        </p>
      </div>
      <BillPreviewPanel keyword={keyword} onUseAverage={(average) => setExpectedAmount(average.toFixed(2))} />
      <div>
        <label className="mb-1 block text-sm font-medium text-slate-700">Starts in</label>
        <input
          required
          type="month"
          className={inputClass}
          value={startMonth}
          onChange={(e) => setStartMonth(e.target.value)}
        />
      </div>
      {error && <p className="text-sm text-red-600">{error}</p>}
      <div className="flex gap-2">
        <button
          type="submit"
          disabled={saving}
          className="rounded-md bg-slate-900 px-3 py-2 text-sm font-medium text-white hover:bg-slate-700 disabled:opacity-50"
        >
          {saving ? "Saving…" : submitLabel}
        </button>
        {onCancel && (
          <button
            type="button"
            onClick={onCancel}
            className="rounded-md border border-slate-300 px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100"
          >
            Cancel
          </button>
        )}
      </div>
    </form>
  );
}
