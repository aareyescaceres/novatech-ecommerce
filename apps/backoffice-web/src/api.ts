import type { Session } from './types';

const SESSION_KEY = 'novatech-backoffice-session';
export const money = (n: number) => new Intl.NumberFormat('es-PE', {
  style: 'currency', currency: 'PEN'
}).format(n);

export function getSession(): Session | null {
  try {
    const raw = sessionStorage.getItem(SESSION_KEY);
    return raw ? JSON.parse(raw) as Session : null;
  } catch { return null; }
}
export function writeSession(session: Session | null): void {
  if (session) sessionStorage.setItem(SESSION_KEY, JSON.stringify(session));
  else sessionStorage.removeItem(SESSION_KEY);
}

export async function api<T>(path: string, options: RequestInit = {}): Promise<T> {
  const current = getSession();
  const response = await fetch(`/api${path}`, {
    ...options,
    headers: {
      ...(options.body ? { 'Content-Type': 'application/json' } : {}),
      ...(current ? { Authorization: `Bearer ${current.token}` } : {}),
      ...options.headers
    }
  });
  if (!response.ok) {
    let message = `Error HTTP ${response.status}`;
    try {
      const result = await response.json() as {error?: string; detail?: string; message?: string};
      message = result.error ?? result.detail ?? result.message ?? message;
    } catch { /* HTML o respuesta vacia */ }
    if (response.status === 401 && !path.startsWith('/auth/'))
      message = 'Sesión vencida. Ingresa de nuevo.';
    throw new Error(message);
  }
  return response.json() as Promise<T>;
}
export async function download(path: string, name: string): Promise<void> {
  const current = getSession();
  const response = await fetch(`/api${path}`, {
    headers: current ? { Authorization: `Bearer ${current.token}` } : {}
  });
  if (!response.ok) throw new Error(`No se pudo descargar el archivo (${response.status})`);
  const objectUrl = URL.createObjectURL(await response.blob());
  const link = document.createElement('a');
  link.href = objectUrl;
  link.download = name;
  link.click();
  window.setTimeout(() => URL.revokeObjectURL(objectUrl), 1000);
}
