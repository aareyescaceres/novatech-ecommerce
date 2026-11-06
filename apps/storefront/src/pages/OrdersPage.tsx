import { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import { api, download, money } from '../api';
import type { Order } from '../types';
const names: Record<Order['status'], string> = { PENDING_PAYMENT: 'Pendiente de pago', PAID: 'Pagado', CANCELLED: 'Cancelado', EXPIRED: 'Expirado', REFUNDED: 'Devuelto' };
export default function OrdersPage() {
    const location = useLocation();
    const [orders, setOrders] = useState<Order[]>([]);
    const [error, setError] = useState('');
    const [notice, setNotice] = useState(location.state?.created ? `Pedido #${location.state.created} creado. Confirma su pago de prueba.` : '');
    async function load() { try {
        setOrders(await api<Order[]>('/orders'));
        setError('');
    }
    catch (e) {
        setError((e as Error).message);
    } }
    useEffect(() => { void load(); }, []);
    async function act(id: number, op: 'pay-demo' | 'cancel') {
        try {
            await api<Order>(`/orders/${id}/${op}`, { method: 'POST' });
            setNotice(op === 'pay-demo' ? 'Pago de demostración confirmado.' : 'Pedido cancelado y stock liberado.');
            await load();
        }
        catch (e) {
            setError((e as Error).message);
        }
    }
    async function pdf(id: number) { try {
        await download(`/orders/${id}/receipt.pdf`, `pedido-${id}.pdf`);
    }
    catch (e) {
        setError((e as Error).message);
    } }
    return <section className="page-pad"><div className="eyebrow dark">HISTORIAL</div><h1>Mis pedidos</h1><p className="muted">Verifica pagos simulados, cancelaciones y recibos PDF.</p>
    {notice && <div className="alert success">{notice}</div>}{error && <div className="alert error">{error}</div>}
    {!orders.length && <div className="empty">Aún no tienes pedidos.</div>}
    <div className="order-stack">{orders.map(o => <article className="order-card" key={o.id}>
      <div className="order-top"><div><strong>Pedido #{o.id}</strong><div className="muted tiny">{new Date(o.createdAt).toLocaleString('es-PE')}</div></div>
        <span className={`status ${o.status.toLowerCase()}`}>{names[o.status]}</span></div>
      <div className="order-lines">{o.items.map((i, k) => <div key={k} className="spec-line"><span>{i.name} <small>×{i.quantity}</small></span><strong>{money(i.lineTotal)}</strong></div>)}</div>
      <div className="order-footer"><strong>Total: {money(o.total)}</strong><div className="actions">
        {o.status === 'PENDING_PAYMENT' && <><button className="button small" onClick={() => act(o.id, 'pay-demo')}>Pagar (demo)</button><button className="ghost small" onClick={() => act(o.id, 'cancel')}>Cancelar</button></>}
        {o.status === 'PAID' && <button className="ghost small" onClick={() => pdf(o.id)}>Descargar PDF</button>}
      </div></div>
      {o.status === 'PENDING_PAYMENT' && <p className="small-note">Reserva válida hasta {new Date(o.expiresAt).toLocaleString('es-PE')}. La expiración automática puede tardar hasta un minuto adicional.</p>}
    </article>)}</div>
  </section>;
}
