import { useState, type FormEvent } from "react";
import { useAuth } from "../context/AuthContext";
import { useChangePassword, useCreateInvite, useSetIdealBalance } from "../hooks/useSettings";
import { formatDate } from "../lib/format";

export function Settings() {
  const { user } = useAuth();
  const isOwner = user?.role === "OWNER";

  return (
    <div className="mx-auto max-w-2xl space-y-6 p-6">
      <h1 className="text-2xl font-semibold">Settings</h1>
      <ChangePasswordCard />
      {isOwner && <IdealBalanceCard />}
      {isOwner && <InviteCard />}
    </div>
  );
}

function ChangePasswordCard() {
  const mutation = useChangePassword();
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [success, setSuccess] = useState(false);

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault();
    setSuccess(false);
    mutation.mutate(
      { currentPassword, newPassword },
      {
        onSuccess: () => {
          setSuccess(true);
          setCurrentPassword("");
          setNewPassword("");
        },
      }
    );
  };

  return (
    <section className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
      <h2 className="mb-3 text-sm font-medium text-slate-500">Change password</h2>
      <form onSubmit={handleSubmit} className="space-y-3">
        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Current password</label>
          <input
            type="password"
            required
            value={currentPassword}
            onChange={(e) => setCurrentPassword(e.target.value)}
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-slate-500 focus:outline-none"
          />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">New password</label>
          <input
            type="password"
            required
            minLength={8}
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-slate-500 focus:outline-none"
          />
        </div>
        {mutation.isError && (
          <p className="text-sm text-red-600">Current password is incorrect.</p>
        )}
        {success && <p className="text-sm text-emerald-600">Password updated.</p>}
        <button
          type="submit"
          disabled={mutation.isPending}
          className="rounded-md bg-slate-900 px-3 py-2 text-sm font-medium text-white hover:bg-slate-700 disabled:opacity-50"
        >
          {mutation.isPending ? "Saving…" : "Update password"}
        </button>
      </form>
    </section>
  );
}

function IdealBalanceCard() {
  const mutation = useSetIdealBalance();
  const [idealBalance, setIdealBalance] = useState("");
  const [success, setSuccess] = useState(false);

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault();
    setSuccess(false);
    mutation.mutate(Number(idealBalance), {
      onSuccess: () => setSuccess(true),
    });
  };

  return (
    <section className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
      <h2 className="mb-3 text-sm font-medium text-slate-500">Ideal balance</h2>
      <form onSubmit={handleSubmit} className="flex items-end gap-3">
        <div className="flex-1">
          <label className="mb-1 block text-sm font-medium text-slate-700">Target amount</label>
          <input
            type="number"
            step="0.01"
            required
            value={idealBalance}
            onChange={(e) => setIdealBalance(e.target.value)}
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-slate-500 focus:outline-none"
          />
        </div>
        <button
          type="submit"
          disabled={mutation.isPending}
          className="rounded-md bg-slate-900 px-3 py-2 text-sm font-medium text-white hover:bg-slate-700 disabled:opacity-50"
        >
          {mutation.isPending ? "Saving…" : "Save"}
        </button>
      </form>
      {success && <p className="mt-2 text-sm text-emerald-600">Ideal balance updated.</p>}
      {mutation.isError && <p className="mt-2 text-sm text-red-600">Could not update ideal balance.</p>}
    </section>
  );
}

function InviteCard() {
  const mutation = useCreateInvite();

  return (
    <section className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
      <h2 className="mb-3 text-sm font-medium text-slate-500">Invite a member</h2>
      <button
        onClick={() => mutation.mutate()}
        disabled={mutation.isPending}
        className="rounded-md bg-slate-900 px-3 py-2 text-sm font-medium text-white hover:bg-slate-700 disabled:opacity-50"
      >
        {mutation.isPending ? "Generating…" : "Generate invite code"}
      </button>
      {mutation.data && (
        <div className="mt-3 rounded-md bg-slate-50 p-3 text-sm">
          <p>
            Code: <span className="font-mono text-base font-semibold tracking-widest">{mutation.data.code}</span>
          </p>
          <p className="mt-1 text-slate-500">Expires {formatDate(mutation.data.expiresAt)}.</p>
        </div>
      )}
      {mutation.isError && <p className="mt-2 text-sm text-red-600">Could not generate an invite.</p>}
    </section>
  );
}
