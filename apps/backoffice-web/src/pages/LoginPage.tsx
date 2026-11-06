import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api';
import { useAuth } from '../auth';
import type { Session } from '../types';

export default function LoginPage() {
  const {save} = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  async function login(event: FormEvent) {
    event.preventDefault(); setBusy(true); setError('');
    try {
      const session = await api<Session>('/auth/login', {method: 'POST', body: JSON.stringify({email, password})});
      if (!['ADMIN','SALES_STAFF','INVENTORY_MANAGER'].includes(session.role)) throw new Error('Solo pueden ingresar cuentas del personal.');
      save(session); navigate('/');
    } catch (e) { setError((e as Error).message); }
    finally { setBusy(false); }
  }

  return <div className="login-page"><div className="login-visual">
    <div className="brand"><span className="brand-mark">N</span><span>Nova<span className="brand-accent">Tech</span><small>BACKOFFICE</small></span></div>
    <div className="login-visual-copy"><span>GESTIÓN DE NEGOCIO</span><h1>Un espacio para cada operación.</h1>
      <p>Inventario, pedidos, reportes y decisiones informadas. Todo desde una aplicación exclusiva para el personal.</p></div>
    <small>Proyecto académico • No procesa pagos reales</small>
  </div><div className="login-form-wrap"><form className="login-card" onSubmit={login}>
    <div className="eyebrow">ACCESO RESTRINGIDO</div><h2>Bienvenido al equipo</h2>
    <p>Ingresa con tu cuenta de administración, ventas o almacén.</p>
    <label>Correo institucional<input className="input" type="email" required value={email} onChange={e => setEmail(e.target.value)} placeholder="personal@novatech.local"/></label>
    <label>Contraseña<input className="input" type="password" required value={password} onChange={e => setPassword(e.target.value)} placeholder="Tu contraseña"/></label>
    {error && <div className="alert error" role="alert">{error}</div>}
    <button className="button primary block" disabled={busy}>{busy ? 'Verificando…' : 'Ingresar al panel'}</button>
    <p className="helper">Las cuentas demo se describen en el README. Cámbialas antes de publicar.</p>
  </form></div></div>;
}
