import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api, money } from '../api';
import type { Cart, Order } from '../types';
export default function CartPage() {
    const navigate = useNavigate();
    const [cart, setCart] = useState<Cart | null>(null);
    const [error, setError] = useState('');
    const [busy, setBusy] = useState(false);
    useEffect(() => { api<Cart>('/cart').then(setCart).catch(e => setError((e as Error).message)); }, []);
    async function change(productId: number, quantity: number) {
        try {
            setCart(await api<Cart>('/cart/items', { method: 'PUT', body: JSON.stringify({ productId, quantity }) }));
            setError('');
        }
        catch (e) {
            setError((e as Error).message);
        }
    }
    async function checkout() {
        setBusy(true);
        setError('');
        try {
            // El UUID es la clave de idempotencia de ESTA pulsacion de compra.
            const order = await api<Order>('/orders/checkout', { method: 'POST', body: JSON.stringify({ key: crypto.randomUUID() }) });
            navigate('/orders', { state: { created: order.id } });
        }
        catch (e) {
            setError((e as Error).message);
        }
        finally {
            setBusy(false);
        }
    }
    return <div className="page-pad"><div className="section-head"><div><div className="eyebrow dark">TU SELECCIÓN</div><h1>Carrito de compras</h1></div><Link className="ghost button-like" to="/">Seguir comprando</Link></div>
    {error && <div className="alert error">{error}</div>}
    {!cart ? <div className="empty">Cargando carrito…</div> : !cart.items.length ? <div className="empty">Tu carrito está vacío. <Link to="/">Explora el catálogo</Link>.</div> :
            <div className="checkout-layout"><div className="cart-list">{cart.items.map(item => <div className="cart-item" key={item.productId}>
        <div className="cart-icon">📦</div><div className="grow"><Link className="item-name" to={`/product/${item.productId}`}>{item.name}</Link><div className="muted tiny">{item.sku} · {money(item.price)} por unidad</div>
        <button className="text-button danger" onClick={() => change(item.productId, 0)}>Eliminar</button></div>
        <input aria-label={`Cantidad de ${item.name}`} type="number" className="input qty" min="1" max={Math.min(99, item.stock)} value={item.quantity} onChange={e => { const q = Number(e.target.value); if (q >= 1 && q <= Math.min(99, item.stock))
                    void change(item.productId, q); }}/>
        <strong>{money(item.subtotal)}</strong></div>)}</div>
        <aside className="summary"><h3>Resumen de compra</h3><div className="spec-line"><span>Subtotal</span><strong>{money(cart.total)}</strong></div><div className="spec-line"><span>Envío</span><strong>Simulado</strong></div><hr /><div className="total-line"><span>Total</span><strong>{money(cart.total)}</strong></div>
          <button className="button block" disabled={busy} onClick={checkout}>{busy ? 'Procesando…' : 'Realizar pedido de prueba'}</button>
          <p className="small-note">El stock se reserva durante 15 minutos. Confirma después el pago simulado desde Mis pedidos.</p>
        </aside></div>}
  </div>;
}
