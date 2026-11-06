import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api';
import { useAuth } from '../auth';
import type { Session } from '../types';
export default function LoginPage() {
    const { save } = useAuth();
    const navigate = useNavigate();
    const [mode, setMode] = useState<'login' | 'register'>('login');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [busy, setBusy] = useState(false);
    async function submit(event: FormEvent) {
        event.preventDefault();
        setBusy(true);
        setError('');
        try {
            const s = await api<Session>(`/auth/${mode}`, { method: 'POST', body: JSON.stringify({ email, password }) });
            if (s.role !== 'CUSTOMER') throw new Error('Esta cuenta pertenece al personal. Ingresa por el Backoffice en localhost:5174.');
            save(s);
            navigate('/');
        }
        catch (e) {
            setError((e as Error).message);
        }
        finally {
            setBusy(false);
        }
    }
    return <div className="auth-box"><div className="eyebrow dark">TU CUENTA</div><h1>{mode === 'login' ? 'Bienvenido de nuevo' : 'Crea tu cuenta'}</h1>
    <p className="muted">Accede al carrito, pedidos y pagos de demostración.</p>
    <div className="tabs"><button className={mode === 'login' ? 'selected' : ''} onClick={() => { setMode('login'); setError(''); }}>Ingresar</button>
    <button className={mode === 'register' ? 'selected' : ''} onClick={() => { setMode('register'); setError(''); }}>Registrarme</button></div>
    <form onSubmit={submit}><label>Correo electrónico<input className="input" required type="email" maxLength={180} value={email} onChange={e => setEmail(e.target.value)} placeholder="tu@correo.com"/></label>
      <label>Contraseña<input className="input" required minLength={mode === 'register' ? 8 : 1} type="password" value={password} onChange={e => setPassword(e.target.value)} placeholder="Mínimo 8 caracteres"/></label>
      {error && <div className="alert error">{error}</div>}
      <button className="button block" disabled={busy}>{busy ? 'Procesando…' : mode === 'login' ? 'Ingresar' : 'Crear cuenta'}</button></form>
    <p className="small-note">Esta aplicación es una demostración académica: no procesa pagos reales.</p>
  </div>;
}
