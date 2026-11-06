import { createContext, useContext, useState, type ReactNode } from 'react';
import { getSession } from './api';
import type { Session } from './types';
type AuthValue = {
    session: Session | null;
    save: (session: Session) => void;
    logout: () => void;
};
const AuthContext = createContext<AuthValue | null>(null);
export function AuthProvider({ children }: {
    children: ReactNode;
}) {
    const [session, setSession] = useState<Session | null>(getSession);
    const save = (value: Session) => {
        localStorage.setItem('novatech-storefront-session', JSON.stringify(value));
        setSession(value);
    };
    const logout = () => { localStorage.removeItem('novatech-storefront-session'); setSession(null); };
    return <AuthContext.Provider value={{ session, save, logout }}>{children}</AuthContext.Provider>;
}
export function useAuth(): AuthValue {
    const value = useContext(AuthContext);
    if (!value)
        throw new Error('AuthProvider faltante');
    return value;
}
