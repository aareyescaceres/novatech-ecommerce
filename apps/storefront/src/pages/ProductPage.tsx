import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api, money } from '../api';
import { useAuth } from '../auth';
import { productEmoji } from './Shop';
import type { Cart, Product } from '../types';
export default function ProductPage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const { session } = useAuth();
    const [p, setP] = useState<Product | null>(null);
    const [quantity, setQuantity] = useState(1);
    const [message, setMessage] = useState('');
    const [error, setError] = useState('');
    useEffect(() => { api<Product>(`/products/${id}`).then(setP).catch(e => setError((e as Error).message)); }, [id]);
    async function add() {
        if (!session) {
            navigate('/login');
            return;
        }
        if (!p)
            return;
        try {
            const current = await api<Cart>('/cart');
            const existing = current.items.find(x => x.productId === p.id)?.quantity ?? 0;
            await api<Cart>('/cart/items', { method: 'PUT', body: JSON.stringify({ productId: p.id, quantity: existing + quantity }) });
            setMessage('Producto agregado correctamente al carrito.');
            setError('');
        }
        catch (e) {
            setError((e as Error).message);
            setMessage('');
        }
    }
    if (error && !p)
        return <div className="alert error">{error}</div>;
    if (!p)
        return <div className="empty">Cargando ficha del producto…</div>;
    return <div className="product-detail">
    <div className="breadcrumbs"><Link to="/">Catálogo</Link> / {p.category} / {p.sku}</div>
    <div className="detail-grid"><div className="detail-image"><span>{productEmoji(p.category)}</span><div>{p.category.toUpperCase()}</div></div>
      <div className="detail-info"><div className="eyebrow dark">{p.brand} · {p.sku}</div><h1>{p.name}</h1>
        <p className="muted">{p.description || 'Ficha comercial disponible. Especificaciones opcionales en MongoDB.'}</p>
        <div className="detail-price">{money(p.price)}</div>
        <div className={p.stock ? 'availability' : 'unavailable'}>{p.stock ? `${p.stock} unidades en stock` : 'Producto agotado'}</div>
        <div className="buy-row"><input className="input qty" type="number" min="1" max={Math.min(99, p.stock)} value={quantity} onChange={e => setQuantity(Number(e.target.value))}/>
          <button className="button" disabled={p.stock === 0 || quantity < 1 || quantity > Math.min(99, p.stock)} onClick={add}>Agregar al carrito</button></div>
        {message && <div className="alert success">{message} <Link to="/cart">Ver carrito</Link></div>}
        {error && <div className="alert error">{error}</div>}
        <div className="specs"><h3>Ficha técnica</h3>{Object.keys(p.specs).length ? Object.entries(p.specs).map(([k, v]) => <div className="spec-line" key={k}><span>{k}</span><strong>{v}</strong></div>) : <p className="muted">Sin especificaciones adicionales.</p>}</div>
        <p className="small-note">Las compras y pagos de este sitio son simulados.</p>
      </div></div>
  </div>;
}
