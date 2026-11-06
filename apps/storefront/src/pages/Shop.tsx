import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, money } from '../api';
import type { Page, Product } from '../types';
export const categories = ['Todos', 'Laptops', 'Monitores', 'Componentes', 'Perifericos', 'Audio', 'Tablets'];
export function productEmoji(category: string) {
    return ({ Laptops: '💻', Monitores: '🖥️', Componentes: '🧩', Perifericos: '⌨️', Audio: '🎧', Tablets: '📱' } as Record<string, string>)[category] ?? '⚡';
}
export function ProductCard({ p }: {
    p: Product;
}) {
    return <Link to={`/product/${p.id}`} className="product-card">
    <div className="product-visual"><span className="product-emoji">{productEmoji(p.category)}</span><span className="tag">{p.category}</span></div>
    <div className="product-body"><div className="muted tiny">{p.brand} · {p.sku}</div>
      <h3>{p.name}</h3><p className="price">{money(p.price)}</p>
      <div className="product-bottom"><span className={p.stock ? 'availability' : 'unavailable'}>{p.stock ? `${p.stock} disponibles` : 'Agotado'}</span><span className="arrow">Ver →</span></div>
    </div>
  </Link>;
}
export default function Shop() {
    const [search, setSearch] = useState('');
    const [debounced, setDebounced] = useState('');
    const [category, setCategory] = useState('Todos');
    const [specKey, setSpecKey] = useState('');
    const [specValue, setSpecValue] = useState('');
    const [appliedSpecs, setAppliedSpecs] = useState({ key: '', value: '' });
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<Product> | null>(null);
    const [error, setError] = useState('');
    useEffect(() => { const t = window.setTimeout(() => setDebounced(search), 250); return () => clearTimeout(t); }, [search]);
    useEffect(() => { setPage(0); }, [debounced, category, appliedSpecs]);
    useEffect(() => {
        let active = true;
        const params = new URLSearchParams({ page: String(page), size: '12' });
        if (debounced)
            params.set('search', debounced);
        if (category !== 'Todos')
            params.set('category', category);
        if (appliedSpecs.key && appliedSpecs.value) {
            params.set('specKey', appliedSpecs.key);
            params.set('specValue', appliedSpecs.value);
        }
        api<Page<Product>>(`/products?${params}`).then(result => { if (active) {
            setData(result);
            setError('');
        } })
            .catch(e => { if (active)
            setError((e as Error).message); });
        return () => { active = false; };
    }, [page, debounced, category, appliedSpecs]);
    return <>
    <section className="hero"><div className="hero-copy"><div className="eyebrow">TECNOLOGÍA QUE TE IMPULSA</div>
      <h1>Tu próximo upgrade empieza aquí.</h1><p>Componentes, laptops y periféricos en un solo lugar. Descubre el catálogo de nuestra tienda de demostración.</p>
      <a href="#catalogo" className="button light">Explorar productos <span>↗</span></a>
    </div><div className="hero-art"><span>⌘</span><div className="orb"></div><div className="chip">POWER YOUR IDEAS</div></div></section>
    <div className="features"><div><strong>⚡ Catálogo tecnológico</strong><small>Especificaciones detalladas</small></div><div><strong>🔒 Compra de prueba</strong><small>Sin transacciones reales</small></div><div><strong>📦 Stock en tiempo real</strong><small>Control de concurrencia</small></div></div>
    <section id="catalogo" className="catalog">
      <div className="section-head"><div><div className="eyebrow dark">EXPLORA NOVATECH</div><h2>Nuestros productos</h2></div><span className="muted">{data?.totalElements ?? 0} artículos encontrados</span></div>
      <div className="category-row">{categories.map(c => <button key={c} className={`category ${c === category ? 'selected' : ''}`} onClick={() => setCategory(c)}>{c}</button>)}</div>
      <div className="filters"><input aria-label="Buscar productos" className="input search" placeholder="Buscar por producto, marca o SKU…" value={search} onChange={e => setSearch(e.target.value)}/>
        <div className="spec-filter"><input className="input" aria-label="Atributo técnico" placeholder="Atributo (RAM)" value={specKey} onChange={e => setSpecKey(e.target.value)}/>
          <input className="input" aria-label="Valor técnico" placeholder="Valor (16GB)" value={specValue} onChange={e => setSpecValue(e.target.value)}/>
          <button className="ghost" onClick={() => setAppliedSpecs({ key: specKey.trim(), value: specValue.trim() })}>Filtrar</button>
          <button className="text-button" onClick={() => { setAppliedSpecs({ key: '', value: '' }); setSpecKey(''); setSpecValue(''); }}>Limpiar</button>
        </div></div>
      {error && <div className="alert error">{error}</div>}
      <div className="product-grid">{data?.content.map(p => <ProductCard key={p.id} p={p}/>)}</div>
      {!error && data?.content.length === 0 && <div className="empty">No encontramos productos con esos filtros.</div>}
      {(data?.totalPages ?? 0) > 1 && <div className="pagination"><button className="ghost" disabled={page === 0} onClick={() => setPage(p => p - 1)}>Anterior</button>
        <span>Página {page + 1} de {data?.totalPages}</span><button className="ghost" disabled={page + 1 >= (data?.totalPages ?? 0)} onClick={() => setPage(p => p + 1)}>Siguiente</button></div>}
    </section>
  </>;
}
