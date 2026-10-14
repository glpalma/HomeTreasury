/** "YYYY-MM" for the current month in the browser's local time. */
export function currentMonth(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
}

/** Moves a "YYYY-MM" string by `delta` months (negative = back). */
export function shiftMonth(month: string, delta: number): string {
  const [year, mon] = month.split("-").map(Number);
  const shifted = new Date(year, mon - 1 + delta, 1);
  return `${shifted.getFullYear()}-${String(shifted.getMonth() + 1).padStart(2, "0")}`;
}

/** "October 2026" for "2026-10". */
export function formatMonth(month: string): string {
  const [year, mon] = month.split("-").map(Number);
  return new Date(year, mon - 1, 1).toLocaleDateString(undefined, {
    month: "long",
    year: "numeric",
  });
}

/**
 * Formats a calendar date ("YYYY-MM-DD"). Do NOT use `new Date("2026-10-08")` for this: that is
 * parsed as UTC midnight and renders as Oct 7 in Brazil. Build the Date from its parts instead.
 */
export function formatDay(isoDate: string): string {
  const [year, mon, day] = isoDate.slice(0, 10).split("-").map(Number);
  return new Date(year, mon - 1, day).toLocaleDateString(undefined, {
    month: "short",
    day: "numeric",
  });
}
