import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { archiveBill, createBill, listBills, setBillPaid, updateBill } from "../api/bills";

export function useBills() {
  return useQuery({
    queryKey: ["bills"],
    queryFn: listBills,
  });
}

function useInvalidateBills() {
  const queryClient = useQueryClient();
  return () => {
    queryClient.invalidateQueries({ queryKey: ["bills"] });
    queryClient.invalidateQueries({ queryKey: ["bill-status"] });
  };
}

export function useCreateBill() {
  const invalidate = useInvalidateBills();
  return useMutation({ mutationFn: createBill, onSuccess: invalidate });
}

export function useUpdateBill() {
  const invalidate = useInvalidateBills();
  return useMutation({ mutationFn: updateBill, onSuccess: invalidate });
}

export function useArchiveBill() {
  const invalidate = useInvalidateBills();
  return useMutation({ mutationFn: archiveBill, onSuccess: invalidate });
}

export function useSetBillPaid() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: setBillPaid,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["bill-status"] });
    },
  });
}
