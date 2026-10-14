import { useBillPreview } from "../../hooks/useBillPreview";
import { formatCurrency } from "../../lib/format";
import { formatDay } from "../../lib/months";

interface Props {
  keyword: string;
  /** Called with the average so the form can fill in the expected amount. */
  onUseAverage: (average: number) => void;
}

export function BillPreviewPanel({ keyword, onUseAverage }: Props) {
  const preview = useBillPreview(keyword);

  if (keyword.trim().length < 3) {
    return null;
  }

  return (
    <div className="rounded-md border border-slate-200 bg-slate-50 p-3 text-sm">
      <p className="mb-2 text-xs font-medium uppercase text-slate-400">
        Matches found in the last 3 months
      </p>

      {(preview.isLoading || preview.isFetching) && <p className="text-slate-500">Looking…</p>}
      {preview.isError && <p className="text-slate-500">Could not check your bank account right now.</p>}

      {preview.data && !preview.isFetching && (
        <>
          {preview.data.matches.length === 0 ? (
            <p className="text-slate-500">
              No debit matched “{preview.data.keyword}”. Check the exact text on the Transactions page.
            </p>
          ) : (
            <ul className="divide-y divide-slate-200">
              {preview.data.matches.map((match, index) => (
                <li key={`${match.date}-${index}`} className="flex items-center justify-between gap-3 py-1">
                  <span className="text-slate-500">{formatDay(match.date)}</span>
                  <span className="flex-1 truncate text-slate-700">{match.description}</span>
                  <span className="font-medium text-slate-800">{formatCurrency(match.amount)}</span>
                </li>
              ))}
            </ul>
          )}

          {preview.data.truncated && (
            <p className="mt-2 text-xs text-amber-700">Some transactions could not be loaded, so this list may be incomplete.</p>
          )}

          {preview.data.averageAmount !== null && (
            <div className="mt-2 flex items-center justify-between rounded-md bg-white px-3 py-2">
              <span className="text-slate-600">Average: {formatCurrency(preview.data.averageAmount)}</span>
              <button
                type="button"
                onClick={() => onUseAverage(preview.data!.averageAmount!)}
                className="rounded-md border border-slate-300 px-2 py-1 text-xs font-medium text-slate-700 hover:bg-slate-100"
              >
                Use average
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
