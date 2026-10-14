import { useQuery } from "@tanstack/react-query";
import { previewBill } from "../api/bills";
import { useDebouncedValue } from "./useDebouncedValue";

const MIN_KEYWORD_LENGTH = 3;

/** Previews what `keyword` would match; idle until the user has typed enough and paused. */
export function useBillPreview(keyword: string) {
  const debounced = useDebouncedValue(keyword.trim(), 500);
  const enabled = debounced.length >= MIN_KEYWORD_LENGTH;

  return useQuery({
    queryKey: ["bill-preview", debounced],
    queryFn: () => previewBill(debounced),
    enabled,
    staleTime: 60_000,
  });
}
