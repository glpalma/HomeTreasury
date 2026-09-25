import { useMutation, useQueryClient } from "@tanstack/react-query";
import { changePassword } from "../api/me";
import { createInvite, setIdealBalance } from "../api/home";

export function useChangePassword() {
  return useMutation({
    mutationFn: changePassword,
  });
}

export function useSetIdealBalance() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: setIdealBalance,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["dashboard"] });
    },
  });
}

export function useCreateInvite() {
  return useMutation({
    mutationFn: createInvite,
  });
}
