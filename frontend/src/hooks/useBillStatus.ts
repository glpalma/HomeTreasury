import { useQuery } from "@tanstack/react-query";
import { getBillStatus } from "../api/bills";

export function useBillStatus(month: string) {
  return useQuery({
    queryKey: ["bill-status", month],
    queryFn: () => getBillStatus(month),
  });
}
