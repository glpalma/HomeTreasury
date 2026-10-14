import { useState, type FormEvent } from "react";
import type { ExpectedIncomeInput } from "../../api/incomes";
import { currentMonth } from "../../lib/months";

interface Props {
  submitLabel: string;
  /** Resolves when saved; rejects (axios error) when the server refuses. */
  onSubmit: (input: ExpectedIncomeInput) => Promise<unknown>;
}

const inputClass =
  "w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-slate-500 focus:outline-none";

export function IncomeForm({ submitLabel, onSubmit }: Props) {
  const [name, setName] = useState("");
  const [expectedAmount, setExpectedAmount] = useState("");
  const [expectedDay, setExpectedDay] = useState("");
  const [keyword, setKeyword] = useState("");
  const [startMonth, setStartMonth] = useState(currentMonth());
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
        expectedDay: Number(expectedDay),
        keyword: keyword.trim(),
        startMonth,
      });
      setName("");
      setExpectedAmount("");
      setExpectedDay("");
      setKeyword("");
      setStartMonth(currentMonth());
    } catch (err) {
      setError((err as any)?.response?.data?.detail ?? "Could not save the income.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-3">
      <div>
        <label className="mb-1 block text-sm font-medium text-slate-700">Name</label>
        <input
          required
          className={inputClass}
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder="e.g. Ana's contribution"
        />
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
          <label className="mb-1 block text-sm font-medium text-slate-700">Expected by day</label>
          <input
            required
            type="number"
            min="1"
            max="31"
            className={inputClass}
            value={expectedDay}
            onChange={(e) => setExpectedDay(e.target.value)}
          />
        </div>
      </div>
      <div>
        <label className="mb-1 block text-sm font-medium text-slate-700">
          Keyword to find in the bank description or sender name
        </label>
        <input
          required
          className={inputClass}
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          placeholder="e.g. ANA SILVA"
        />
        <p className="mt-1 text-xs text-slate-400">
          Use text you saw on an incoming transfer on the Transactions page. Case and accents are ignored.
        </p>
      </div>
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
      <button
        type="submit"
        disabled={saving}
        className="rounded-md bg-slate-900 px-3 py-2 text-sm font-medium text-white hover:bg-slate-700 disabled:opacity-50"
      >
        {saving ? "Saving…" : submitLabel}
      </button>
    </form>
  );
}
