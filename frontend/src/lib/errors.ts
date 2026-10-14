/** Turns an axios error from any HomeTreasury endpoint into a sentence for the user. */
export function apiErrorMessage(error: unknown, fallback = "Something went wrong. Please try again."): string {
  const status = (error as any)?.response?.status;
  const detail = (error as any)?.response?.data?.detail;

  if (status === 404) {
    return detail ?? "No bank account is linked yet.";
  }
  if (status === 502) {
    return (
      detail ??
      "The bank data provider is unavailable. If you just connected an account, it may still be syncing — try again in a minute."
    );
  }
  return detail ?? fallback;
}
