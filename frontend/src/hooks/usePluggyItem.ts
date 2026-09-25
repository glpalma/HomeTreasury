import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { createConnectToken, getPluggyItem, linkPluggyItem } from "../api/pluggy";

// Pinned to a version confirmed to exist on Pluggy's CDN (docs.pluggy.ai uses this same tag
// as their canonical example). Not every SDK version published to npm is mirrored there, so
// don't bump this without first confirming the URL resolves (HEAD request, expect 200).
const PLUGGY_CONNECT_SDK_URL = "https://cdn.pluggy.ai/pluggy-connect/v2.7.0/pluggy-connect.js";

export function usePluggyItem() {
  return useQuery({
    queryKey: ["pluggy-item"],
    queryFn: getPluggyItem,
  });
}

function loadPluggyConnectScript(): Promise<void> {
  if (typeof window !== "undefined" && (window as any).PluggyConnect) {
    return Promise.resolve();
  }
  return new Promise((resolve, reject) => {
    const script = document.createElement("script");
    script.src = PLUGGY_CONNECT_SDK_URL;
    script.async = true;
    script.onload = () => resolve();
    script.onerror = () => reject(new Error("Failed to load Pluggy Connect widget"));
    document.body.appendChild(script);
  });
}

export function useLinkPluggyItem() {
  const queryClient = useQueryClient();

  const linkMutation = useMutation({
    mutationFn: linkPluggyItem,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["pluggy-item"] });
    },
  });

  const openWidget = async () => {
    const { accessToken } = await createConnectToken();
    await loadPluggyConnectScript();

    return new Promise<void>((resolve, reject) => {
      const PluggyConnect = (window as any).PluggyConnect;
      const widget = new PluggyConnect({
        connectToken: accessToken,
        includeSandbox: true,
        onSuccess: (data: { item: { id: string } }) => {
          linkMutation
            .mutateAsync(data.item.id)
            .then(() => resolve())
            .catch(reject);
        },
        onError: (error: { message?: string } | unknown) => {
          const message =
            typeof error === "object" && error !== null && "message" in error
              ? String((error as { message?: string }).message)
              : "Pluggy Connect failed";
          reject(new Error(message));
        },
        onClose: () => {
          resolve();
        },
      });
      widget.init();
    });
  };

  return {
    openWidget,
    isLinking: linkMutation.isPending,
    error: linkMutation.error,
  };
}
