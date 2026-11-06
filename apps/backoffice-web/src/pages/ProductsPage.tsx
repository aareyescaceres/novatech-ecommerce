import { useEffect, useState, type FormEvent } from 'react';
import { api, money } from '../api';
import type { Product, ProductInput } from '../types';

const BLANK: ProductInput = {sku:'', name:'', brand:'', category:'Laptops', price:0, stock:0, active:true, description:'', specs:{}};
function copyProduct(p: Product): ProductInput {
  return {sku:p.sku, name:p.name, brand:p.brand, category:p.category, price:p.price, stock:p.stock, active:p.active, description:p.description, specs:p.specs};
}
export default function ProductsPage({canEdit}: {canEdit: boolean}) {
  const [products, setProducts] = useState<Product[]>([]);
  const [selected, setSelected] = useState<number | null>(null);
  const [form, setForm] = useState<ProductInput>({...BLANK});
  const [specsText, setSpecsText] = useState('{}');
  const [stock, setStock] = useState(0);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [search, setSearch] = useState('');
  async function refresh() { setProducts(await api<Product[]>('/admin/products')); }
  useEffect(() => { refresh().catch(e => setError((e as Error).message)); }, []);
  function edit(p: Product) {
    if (!canEdit) return;
    setSelected(p.id); setForm(copyProduct(p)); setSpecsText(JSON.stringify(p.specs, null, 2)); setStock(p.stock);
    window.scrollTo({top:0, behavior:'smooth'});
  }
  function clear() {setSelected(null);setForm({...BLANK});setSpecsText('{}');setStock(0);}
  function field<K extends keyof ProductInput>(name: K, value: ProductInput[K]) {setForm(current => ({...current, [name]:value}));}
  async function save(event: FormEvent) {
    event.preventDefault(); if (!canEdit) return; setError(''); setMessage('');
    try {
      const specs: unknown = JSON.parse(specsText);
      if (!specs || Array.isArray(specs) || typeof specs !== 'object' || Object.values(specs).some(v=>typeof v !== 'string'))
        throw new Error('Las especificaciones deben ser un objeto JSON con valores de texto.');
      const body = JSON.stringify({...form, specs});
      await api<Product>(selected === null ? '/admin/products' : `/admin/products/${selected}`,
        {method: selected === null ? 'POST' : 'PUT', body});
      setMessage('Producto guardado correctamente.'); clear(); await refresh();
    } catch (e) {setError((e as Error).message);}
  }
  async function updateStock() {
    if (selected === null || !canEdit) return;
    try {
      await api<Product>(`/admin/products/${selected}/stock`, {method:'PATCH', body:JSON.stringify({stock})});
      setMessage('Stock actualizado correctamente.'); setError(''); await refresh();
    } catch(e) {setError((e as Error).message);}
  }
  const filtered = products.filter(p => `${p.name} ${p.sku} ${p.brand}`.toLowerCase().includes(search.toLowerCase()));
  return <div className="page-stack"><div className="page-heading"><div><div className="eyebrow">CATÁLOGO E INVENTARIO</div>
    <h1>Gestión de productos</h1><p>{canEdit ? 'Edita fichas técnicas y controla existencias.' : 'Consulta el inventario. Tu perfil no tiene permiso para modificarlo.'}</p></div>
    <span className="date-pill">{products.length} productos</span></div>
    {error && <div className="alert error" role="alert">{error}</div>}{message && <div className="alert success">{message}</div>}
    {canEdit && <section className="panel"><div className="panel-heading"><div><h2>{selected === null ? 'Crear producto' : `Editar producto #${selected}`}</h2>
      <p>El stock de productos existentes se modifica por separado con un bloqueo SQL.</p></div>
      {selected !== null && <button className="button subtle" onClick={clear}>Nuevo producto</button>}</div>
      <form className="product-form" onSubmit={save}>
        <label>SKU<input className="input" maxLength={80} required value={form.sku} onChange={e => field('sku',e.target.value)}/></label>
        <label>Nombre<input className="input" maxLength={200} required value={form.name} onChange={e => field('name',e.target.value)}/></label>
        <label>Marca<input className="input" maxLength={100} required value={form.brand} onChange={e => field('brand',e.target.value)}/></label>
        <label>Categoría<select className="input" value={form.category} onChange={e => field('category',e.target.value)}>
          {['Laptops','Monitores','Componentes','Perifericos','Audio','Tablets'].map(x=><option key={x}>{x}</option>)}</select></label>
        <label>Precio (S/)<input className="input" required min={0} step="0.01" type="number" value={form.price} onChange={e => field('price',Number(e.target.value))}/></label>
        {selected === null && <label>Stock inicial<input className="input" min={0} type="number" value={form.stock} onChange={e => field('stock',Number(e.target.value))}/></label>}
        <label className="full">Descripción<textarea className="input" maxLength={3000} rows={3} value={form.description} onChange={e => field('description',e.target.value)}/></label>
        <label className="full">Especificaciones técnicas en JSON<textarea className="input mono" rows={4} value={specsText} onChange={e => setSpecsText(e.target.value)}/></label>
        <label className="inline-checkbox"><input type="checkbox" checked={form.active} onChange={e=>field('active',e.target.checked)}/> Producto activo</label>
        <div className="form-actions"><button className="button primary" type="submit">{selected === null ? 'Crear producto' : 'Guardar cambios'}</button></div>
      </form>
      {selected !== null && <div className="stock-adjust"><div><h3>Ajuste de inventario</h3><p>Esta operación utiliza una transacción y bloqueo de fila en PostgreSQL.</p></div>
        <input aria-label="Nuevo stock" className="input" min={0} type="number" value={stock} onChange={e=>setStock(Number(e.target.value))}/>
        <button className="button dark" type="button" onClick={updateStock}>Actualizar stock</button></div>}
    </section>}
    <section className="panel"><div className="panel-heading"><div><h2>Inventario general</h2><p>Productos activos e inactivos</p></div>
      <input className="input search" placeholder="Buscar nombre, SKU o marca" value={search} onChange={e=>setSearch(e.target.value)}/></div>
      <div className="table-scroll"><table><thead><tr><th>SKU</th><th>Producto</th><th>Categoría</th><th>Precio</th><th>Stock</th><th>Estado</th>{canEdit && <th>Acción</th>}</tr></thead>
      <tbody>{filtered.map(p=><tr key={p.id}><td className="mono">{p.sku}</td><td><strong>{p.name}</strong><small className="subtext">{p.brand}</small></td><td>{p.category}</td>
        <td>{money(p.price)}</td><td><span className={p.stock <= 5 ? 'pill warning' : 'pill neutral'}>{p.stock} uds.</span></td><td><span className={p.active ? 'pill success' : 'pill neutral'}>{p.active ? 'Activo' : 'Inactivo'}</span></td>
        {canEdit && <td><button className="button link" onClick={()=>edit(p)}>Editar</button></td>}</tr>)}</tbody></table>
        {filtered.length === 0 && <div className="empty">No se encontraron productos.</div>}</div></section>
  </div>;
}
