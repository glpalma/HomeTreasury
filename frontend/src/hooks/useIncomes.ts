import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { createIncome, getIncomeStatus, listIncomes } from "../api/incomes";

export function useIncomes() {
  return useQuery({
    queryKey: ["incomes"],
    queryFn: listIncomes,
  });
}

export function useIncomeStatus(month: string) {
  return useQuery({
    queryKey: ["income-status", month],
    queryFn: () => getIncomeStatus(month),
  });
}

export function useCreateIncome() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createIncome,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["incomes"] });
      queryClient.invalidateQueries({ queryKey: ["income-status"] });
    },
  });
}
