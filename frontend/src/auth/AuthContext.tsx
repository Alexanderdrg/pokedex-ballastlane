import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { setAuthToken, setUnauthorizedHandler } from '../api/client';
import type { AuthResponse, Role } from '../api/types';

const STORAGE_KEY = 'pokedex.session';

interface Session {
  token: string;
  username: string;
  role: Role;
  expiresAt: string;
}

interface AuthContextValue {
  user: Omit<Session, 'token'> | null;
  isAdmin: boolean;
  signIn: (auth: AuthResponse) => void;
  signOut: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

function loadSession(): Session | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return null;
    const session = JSON.parse(raw) as Session;
    return new Date(session.expiresAt).getTime() > Date.now() ? session : null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Session | null>(() => {
    const initial = loadSession();
    setAuthToken(initial?.token ?? null);
    return initial;
  });
  const queryClient = useQueryClient();

  const signOut = useCallback(() => {
    setAuthToken(null);
    setSession(null);
    try {
      localStorage.removeItem(STORAGE_KEY);
    } catch {
      /* storage unavailable */
    }
    queryClient.removeQueries({ queryKey: ['local'] });
  }, [queryClient]);

  const signIn = useCallback((auth: AuthResponse) => {
    const next: Session = { token: auth.token, username: auth.username, role: auth.role, expiresAt: auth.expiresAt };
    setAuthToken(next.token);
    setSession(next);
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
    } catch {
      /* storage unavailable: session lives in memory only */
    }
  }, []);

  // An expired or revoked token answers 401: drop the session
  useEffect(() => {
    setUnauthorizedHandler(signOut);
  }, [signOut]);

  const value = useMemo<AuthContextValue>(
    () => ({
      user: session ? { username: session.username, role: session.role, expiresAt: session.expiresAt } : null,
      isAdmin: session?.role === 'ADMIN',
      signIn,
      signOut,
    }),
    [session, signIn, signOut],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside <AuthProvider>');
  return ctx;
}
