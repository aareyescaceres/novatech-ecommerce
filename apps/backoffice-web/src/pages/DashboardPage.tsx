import { useEffect, useState } from 'react';
import { api, money } from '../api';
import { useAuth } from '../auth';
import type { Product, Summary } from '../types';

export default function DashboardPage() {
  const {session} = useAuth();
  const canViewSales = session?.role !== 'INVENTORY_MANAGER';
  const [summary, setSummary] = useState<Summary | null>(null);
  const [products, setProducts] = useState<Product[]>([]);
  const [error, setError] = useState('');
  useEffect(() => {
    Promise.all([
      api<Product[]>('/admin/products'),
      canViewSales ? api<Summary>('/admin/reports/summary') : Promise.resolve(null)
    ]).then(([p, s]) => { setProducts(p); setSummary(s); }).catch(e => setError((e as Error).message));
  }, [canViewSales]);
  const low = products.filter(p => p.active && p.stock <= 5);
  const active = products.filter(p => p.active);
  return <div className="page-stack">
    <div className="page-heading"><div><div className="eyebrow">VISIÓN GENERAL</div><h1>Resumen de operaciones</h1>
      <p>Información actual del catálogo y la actividad comercial.</p></div><span className="date-pill">NovaTech · Perú</span></div>
    {error && <div className="alert error">{error}</div>}
    <div className="metrics-grid">
      <div className="metric-card"><span>Productos activos</span><strong>{summary?.activeProducts ?? active.length}</strong><small>Artículos disponibles en el catálogo</small></div>
      <div className="metric-card"><span>Unidades en almacén</span><strong>{summary?.unitsInStock ?? active.reduce((n,p) => n+p.stock,0)}</strong><small>Existencias actuales</small></div>
      {canViewSales && <><div className="metric-card"><span>Pedidos pagados</span><strong>{summary?.paidOrders ?? '—'}</strong><small>Ventas completadas (demo)</small></div>
      <div className="metric-card"><span>Total vendido</span><strong className="money-metric">{summary ? money(summary.sales) : '—'}</strong><small>Importe histórico simulado</small></div></>}
      {!canViewSales && <div className="metric-card"><span>Productos con stock bajo</span><strong>{low.length}</strong><small>Stock igual o inferior a 5 unidades</small></div>}
    </div>
    <section className="panel"><div className="panel-heading"><div><h2>Atención al inventario</h2><p>Productos activos con cinco unidades o menos</p></div></div>
      {low.length === 0 ? <div className="empty">No hay artículos con stock bajo.</div> :
      <div className="table-scroll"><table><thead><tr><th>SKU</th><th>Producto</th><th>Categoría</th><th>Stock</th></tr></thead><tbody>
        {low.map(p => <tr key={p.id}><td className="mono">{p.sku}</td><td>{p.name}</td><td>{p.category}</td><td><span className="pill warning">{p.stock} uds.</span></td></tr>)}
      </tbody></table></div>}
    </section>
  </div>;
}
