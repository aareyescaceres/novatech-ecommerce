import { useEffect, useState } from 'react';
import { api, money } from '../api';
import type { Order } from '../types';

export default function OrdersPage() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [busy, setBusy] = useState<number | null>(null);
  const refresh = async () => setOrders(await api<Order[]>('/admin/orders'));
  useEffect(()=>{refresh().catch(e=>setError((e as Error).message));},[]);
  async function refund(id: number) {
    if (!window.confirm(`¿Registrar devolución SIMULADA del pedido #${id}? Se restaurará el stock.`)) return;
    setBusy(id);setError('');setMessage('');
    try {
      await api<Order>(`/admin/orders/${id}/refund-demo`, {method:'POST'});
      setMessage(`Devolución simulada del pedido #${id} registrada.`);
      await refresh();
    } catch(e){setError((e as Error).message);}finally{setBusy(null);}
  }
  return <div className="page-stack"><div className="page-heading"><div><div className="eyebrow">VENTAS</div><h1>Pedidos y devoluciones</h1><p>Historial de operaciones. Las devoluciones no transfieren dinero.</p></div>
    <span className="date-pill">{orders.length} pedidos</span></div>
    {error && <div className="alert error">{error}</div>}{message && <div className="alert success">{message}</div>}
    <section className="panel"><div className="table-scroll"><table><thead><tr><th>ID</th><th>Fecha</th><th>Estado</th><th>Unidades</th><th>Total</th><th>Acciones</th></tr></thead><tbody>
      {orders.map(o=><tr key={o.id}><td className="mono">#{o.id}</td><td>{new Date(o.createdAt).toLocaleString('es-PE')}</td>
        <td><span className={o.status === 'PAID' ? 'pill success' : o.status === 'PENDING_PAYMENT' ? 'pill warning' : 'pill neutral'}>{o.status}</span></td>
        <td>{o.items.reduce((total,i)=>total+i.quantity,0)}</td><td><strong>{money(o.total)}</strong></td><td>{o.status==='PAID' && <button disabled={busy!==null} className="button link" onClick={()=>refund(o.id)}>{busy===o.id ? 'Procesando…':'Devolver (demo)'}</button>}</td>
      </tr>)}</tbody></table>{orders.length === 0 && <div className="empty">Todavía no se registraron pedidos.</div>}</div></section>
  </div>;
}
