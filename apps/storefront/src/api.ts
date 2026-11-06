import type { Session } from './types';
export const money = (n: number): string => new Intl.NumberFormat('es-PE', {
    style: 'currency', currency: 'PEN', minimumFractionDigits: 2
}).format(n);
export function getSession(): Session | null {
    try {
        const value = localStorage.getItem('novatech-storefront-session');
        return value ? JSON.parse(value) as Session : null;
    }
    catch {
        return null;
    }
}
export async function api<T>(url: string, options: RequestInit = {}): Promise<T> {
    const session = getSession();
    const response = await fetch(`/api${url}`, {
        ...options,
        headers: {
            ...(options.body ? { 'Content-Type': 'application/json' } : {}),
            ...(session ? { Authorization: `Bearer ${session.token}` } : {}),
            ...options.headers
        }
    });
    if (!response.ok) {
        let reason = `Error HTTP ${response.status}`;
        try {
            const body = await response.json() as {
                error?: string;
            };
            reason = body.error ?? reason;
        }
        catch { /* El backend podria no devolver JSON si falla antes de Spring. */ }
        if (response.status === 401 && !url.startsWith('/auth/'))
            reason = 'La sesión venció. Inicia sesión otra vez.';
        throw new Error(reason);
    }
    return await response.json() as T;
}
export async function download(url: string, filename: string): Promise<void> {
    const session = getSession();
    const response = await fetch(`/api${url}`, {
        headers: session ? { Authorization: `Bearer ${session.token}` } : {}
    });
    if (!response.ok)
        throw new Error(`No se pudo descargar (${response.status})`);
    const blob = await response.blob();
    const objectUrl = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = objectUrl;
    link.download = filename;
    link.click();
    window.setTimeout(() => URL.revokeObjectURL(objectUrl), 1000);
}
