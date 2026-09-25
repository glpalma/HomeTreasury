import { useQuery } from "@tanstack/react-query";
import { getDashboard } from "../api/treasury";

export function useDashboard(periodDays: number) {
  return useQuery({
    queryKey: ["dashboard", periodDays],
    queryFn: () => getDashboard(periodDays),
  });
}
