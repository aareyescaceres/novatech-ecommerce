import { useEffect, useState } from 'react';
import { api, download, money } from '../api';
import type { Summary } from '../types';

export default function ReportsPage() {
  const [data, setData] = useState<Summary | null>(null);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  useEffect(()=>{api<Summary>('/admin/reports/summary').then(setData).catch(e=>setError((e as Error).message));},[]);
  async function excel() {
    setBusy(true);setError('');
    try {await download('/admin/reports/store.xlsx','novatech-inventario-ventas.xlsx');}
    catch(e){setError((e as Error).message);}finally{setBusy(false);}
  }
  return <div className="page-stack"><div className="page-heading"><div><div className="eyebrow">INTELIGENCIA COMERCIAL</div><h1>Reportes</h1><p>Indicadores de ventas y exportación Excel generada por el servicio independiente.</p></div></div>
    {error && <div className="alert error">{error}</div>}
    <div className="metrics-grid"><div className="metric-card"><span>Ventas pagadas</span><strong className="money-metric">{data ? money(data.sales):'—'}</strong></div>
      <div className="metric-card"><span>Pedidos pagados</span><strong>{data?.paidOrders ?? '—'}</strong></div>
      <div className="metric-card"><span>Productos activos</span><strong>{data?.activeProducts ?? '—'}</strong></div>
      <div className="metric-card"><span>Stock disponible</span><strong>{data?.unitsInStock ?? '—'}</strong></div></div>
    <section className="panel export-card"><div className="export-icon">↓</div><div><h2>Exportar inventario y ventas</h2>
      <p>Descarga un archivo XLSX con dos hojas: inventario comercial y detalle de pedidos pagados. Creado con Apache POI en reports-service.</p></div>
      <button className="button primary" disabled={busy} onClick={excel}>{busy ? 'Generando…':'Descargar Excel'}</button>
    </section>
  </div>;
}
