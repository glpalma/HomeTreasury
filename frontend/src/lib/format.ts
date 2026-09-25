const formattersByCurrency = new Map<string, Intl.NumberFormat>();

export function formatCurrency(value: number, currencyCode = "BRL"): string {
  let formatter = formattersByCurrency.get(currencyCode);
  if (!formatter) {
    formatter = new Intl.NumberFormat(undefined, { style: "currency", currency: currencyCode });
    formattersByCurrency.set(currencyCode, formatter);
  }
  return formatter.format(value);
}

export function formatPercent(rate: number): string {
  return `${(rate * 100).toFixed(2)}%`;
}

export function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString(undefined, {
    year: "numeric",
    month: "short",
    day: "numeric",
  });
}
