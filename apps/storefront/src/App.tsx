import { Link, Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './auth';
import Shop from './pages/Shop';
import ProductPage from './pages/ProductPage';
import LoginPage from './pages/LoginPage';
import CartPage from './pages/CartPage';
import OrdersPage from './pages/OrdersPage';
export default function App() {
    const { session, logout } = useAuth();
    return <>
    <div className="topbar">Envíos de demostración a todo el Perú · Pagos simulados · Proyecto académico</div>
    <header className="header">
      <div className="shell header-inner">
        <Link to="/" className="logo"><span className="logo-icon">N</span><span>Nova<span className="accent">Tech</span><small>STORE</small></span></Link>
        <nav className="nav">
          <Link to="/">Catálogo</Link>
          {session && <Link to="/cart">Carrito</Link>}
          {session && <Link to="/orders">Mis pedidos</Link>}
        </nav>
        <div className="account">
          {session ? <><span className="small-email" title={session.email}>{session.email}</span><button className="ghost small" onClick={logout}>Salir</button></> : <Link className="button small" to="/login">Ingresar</Link>}
        </div>
      </div>
    </header>
    <main className="shell main">
      <Routes>
        <Route path="/" element={<Shop />}/>
        <Route path="/product/:id" element={<ProductPage />}/>
        <Route path="/login" element={<LoginPage />}/>
        <Route path="/cart" element={session ? <CartPage /> : <Navigate to="/login"/>}/>
        <Route path="/orders" element={session ? <OrdersPage /> : <Navigate to="/login"/>}/>
        <Route path="*" element={<Navigate to="/"/>}/>
      </Routes>
    </main>
    <footer className="footer"><div className="shell footer-inner"><strong>NovaTech Store</strong><span>Proyecto independiente · Spring Boot + PostgreSQL + MongoDB + React</span><span>Sin compras ni pagos reales</span></div></footer>
  </>;
}
