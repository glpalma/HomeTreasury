import axios from "axios";
import { AUTH_STORAGE_KEY, type AuthUser } from "../context/AuthContext";

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
});

apiClient.interceptors.request.use((config) => {
  const raw = localStorage.getItem(AUTH_STORAGE_KEY);
  if (raw) {
    try {
      const user = JSON.parse(raw) as AuthUser;
      config.headers.Authorization = `Bearer ${user.token}`;
    } catch {
      // ignore malformed storage
    }
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem(AUTH_STORAGE_KEY);
    }
    return Promise.reject(error);
  }
);
