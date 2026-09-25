import { useState, type FormEvent } from "react";
import { useNavigate, Link } from "react-router-dom";
import { register as registerRequest } from "../api/auth";
import { useAuth } from "../context/AuthContext";

type Mode = "create" | "join";

export function Register() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [mode, setMode] = useState<Mode>("create");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [homeName, setHomeName] = useState("");
  const [inviteCode, setInviteCode] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    setIsSubmitting(true);
    try {
      const response = await registerRequest({
        email,
        password,
        homeName: mode === "create" ? homeName : undefined,
        inviteCode: mode === "join" ? inviteCode : undefined,
      });
      login(response);
      navigate("/dashboard");
    } catch (err: any) {
      const status = err.response?.status;
      if (status === 409) {
        setError("An account with that email already exists.");
      } else if (status === 400) {
        setError("Invalid or expired invite code.");
      } else {
        setError("Something went wrong. Try again.");
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center px-4">
      <div className="w-full max-w-sm rounded-lg border border-slate-200 bg-white p-8 shadow-sm">
        <h1 className="mb-6 text-2xl font-semibold">Create your account</h1>

        <div className="mb-4 flex rounded-md border border-slate-300 p-1 text-sm">
          <button
            type="button"
            onClick={() => setMode("create")}
            className={`flex-1 rounded-sm py-1.5 font-medium ${
              mode === "create" ? "bg-slate-900 text-white" : "text-slate-600"
            }`}
          >
            Create a Home
          </button>
          <button
            type="button"
            onClick={() => setMode("join")}
            className={`flex-1 rounded-sm py-1.5 font-medium ${
              mode === "join" ? "bg-slate-900 text-white" : "text-slate-600"
            }`}
          >
            Join with code
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="email" className="mb-1 block text-sm font-medium text-slate-700">
              Email
            </label>
            <input
              id="email"
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-slate-500 focus:outline-none"
            />
          </div>
          <div>
            <label htmlFor="password" className="mb-1 block text-sm font-medium text-slate-700">
              Password
            </label>
            <input
              id="password"
              type="password"
              required
              minLength={8}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-slate-500 focus:outline-none"
            />
            <p className="mt-1 text-xs text-slate-500">At least 8 characters.</p>
          </div>

          {mode === "create" ? (
            <div>
              <label htmlFor="homeName" className="mb-1 block text-sm font-medium text-slate-700">
                Home name
              </label>
              <input
                id="homeName"
                type="text"
                required
                value={homeName}
                onChange={(e) => setHomeName(e.target.value)}
                className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-slate-500 focus:outline-none"
              />
            </div>
          ) : (
            <div>
              <label htmlFor="inviteCode" className="mb-1 block text-sm font-medium text-slate-700">
                Invite code
              </label>
              <input
                id="inviteCode"
                type="text"
                required
                value={inviteCode}
                onChange={(e) => setInviteCode(e.target.value.toUpperCase())}
                className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm uppercase tracking-widest focus:border-slate-500 focus:outline-none"
              />
            </div>
          )}

          {error && <p className="text-sm text-red-600">{error}</p>}
          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full rounded-md bg-slate-900 px-3 py-2 text-sm font-medium text-white hover:bg-slate-700 disabled:opacity-50"
          >
            {isSubmitting ? "Creating account…" : "Create account"}
          </button>
        </form>
        <p className="mt-4 text-center text-sm text-slate-600">
          Already have an account?{" "}
          <Link to="/login" className="font-medium text-slate-900 underline">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}
