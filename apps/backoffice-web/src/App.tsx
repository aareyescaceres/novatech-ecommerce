import { Link, NavLink, Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './auth';
import DashboardPage from './pages/DashboardPage';
import LoginPage from './pages/LoginPage';
import ProductsPage from './pages/ProductsPage';
import OrdersPage from './pages/OrdersPage';
import ReportsPage from './pages/ReportsPage';

export default function App() {
  const {session, logout} = useAuth();
  if (!session) return <Routes>
    <Route path="/login" element={<LoginPage/>}/>
    <Route path="*" element={<Navigate to="/login" replace/>}/>
  </Routes>;
  const sales = session.role === 'ADMIN' || session.role === 'SALES_STAFF';
  const inventory = session.role === 'ADMIN' || session.role === 'INVENTORY_MANAGER';
  return <div className="workspace">
    <aside className="sidebar">
      <Link to="/" className="brand"><span className="brand-mark">N</span>
        <span>Nova<span className="brand-accent">Tech</span><small>BACKOFFICE</small></span>
      </Link>
      <div className="sidebar-heading">OPERACIONES</div>
      <nav className="sidebar-nav">
        <NavLink end to="/" className={({isActive}) => isActive ? 'active' : ''}>◫ <span>Resumen</span></NavLink>
        <NavLink to="/products" className={({isActive}) => isActive ? 'active' : ''}>▦ <span>Productos e inventario</span></NavLink>
        {sales && <NavLink to="/orders" className={({isActive}) => isActive ? 'active' : ''}>▤ <span>Pedidos y devoluciones</span></NavLink>}
        {sales && <NavLink to="/reports" className={({isActive}) => isActive ? 'active' : ''}>▥ <span>Reportes</span></NavLink>}
      </nav>
      <div className="sidebar-foot"><div className="env-chip">ENTORNO DE DESARROLLO</div><p>Operaciones y pagos simulados. No es una plataforma productiva.</p></div>
    </aside>
    <div className="main-area">
      <header className="top-header"><div><span className="eyebrow">NOVA / OPERACIONES</span><strong>Centro de administración</strong></div>
        <div className="profile"><div className="profile-avatar">{session.email.charAt(0).toUpperCase()}</div>
          <div className="profile-name"><strong>{session.email}</strong><small>{session.role === 'ADMIN' ? 'Administración' : session.role === 'SALES_STAFF' ? 'Personal de ventas' : 'Gestión de inventario'}</small></div>
          <button className="button subtle" onClick={logout}>Salir</button>
        </div>
      </header>
      <main className="content">
        <Routes>
          <Route path="/" element={<DashboardPage/>}/>
          <Route path="/products" element={<ProductsPage canEdit={inventory}/>}/>
          <Route path="/orders" element={sales ? <OrdersPage/> : <Navigate to="/" replace/>}/>
          <Route path="/reports" element={sales ? <ReportsPage/> : <Navigate to="/" replace/>}/>
          <Route path="/login" element={<Navigate to="/" replace/>}/>
          <Route path="*" element={<Navigate to="/" replace/>}/>
        </Routes>
      </main>
    </div>
  </div>;
}
