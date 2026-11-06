import { createContext, useContext, useState, type ReactNode } from 'react';
import { getSession, writeSession } from './api';
import type { Session } from './types';

type AuthContextValue = {
  session: Session | null;
  save: (session: Session) => void;
  logout: () => void;
};
const Context = createContext<AuthContextValue | null>(null);
export function AuthProvider({children}: {children: ReactNode}) {
  const [session, setSession] = useState<Session | null>(getSession);
  function save(value: Session) { writeSession(value); setSession(value); }
  function logout() { writeSession(null); setSession(null); }
  return <Context.Provider value={{session, save, logout}}>{children}</Context.Provider>;
}
export function useAuth() {
  const value = useContext(Context);
  if (!value) throw new Error('AuthProvider es necesario');
  return value;
}
