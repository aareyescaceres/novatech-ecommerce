# NovaTech Store · Monorepositorio de ecommerce

Java 25, Spring Boot 4.1.2, Maven, PostgreSQL 17, MongoDB 7, React 19, TypeScript y Vite. Una tienda pública para clientes y un sistema privado para empleados, con **tres aplicaciones backend** independientes.

> Proyecto académico de tecnología. No está afiliado a ninguna empresa. **No procesa pagos reales, no emite comprobantes tributarios y no está preparado para producción.** La autenticación y las credenciales de muestra están configuradas exclusivamente para desarrollo local.

## 1. ¿Qué incluye realmente?

### Tienda: `apps/storefront`
- Registro, login y JWT para clientes.
- Catálogo paginado, búsqueda, categorías y filtro técnico MongoDB.
- Fichas de producto, carrito persistente, checkout y pedidos.
- Reserva temporal de inventario con control de concurrencia SQL.
- Pago demo, cancelación, consulta de pedidos y descarga PDF.

### Sistema del personal: `apps/backoffice-web`
- Login independiente, con almacenamiento de sesión separado de la tienda.
- Dashboard de métricas, indicadores de inventario y alertas de stock bajo.
- Alta/edición de productos, modificación de inventario y búsqueda.
- Listado de pedidos, devoluciones ficticias y exportación Excel.
- Menú adaptado al rol: administración, ventas o almacén.

### Servicios
- **`services/commerce-service` (8080):** fuente de verdad de usuarios, productos, stock, carrito y pedidos. Maneja SQL, MongoDB, Flyway, BCrypt, JWT, concurrencia y PDF. Mantiene el paquete Java `com.novatech.store` por compatibilidad con el proyecto previo.
- **`services/backoffice-service` (8082):** API del personal (*Backend for Frontend*). Valida tokens y roles, delega operaciones al servicio de comercio y consulta reportes. **No modifica tablas comerciales directamente.**
- **`services/reports-service` (8081):** consulta métricas e inventario por JDBC en modo lectura y genera `.xlsx` usando Apache POI. Mantiene, por ahora, acceso a la misma base SQL: es un acoplamiento documentado, no independencia absoluta de datos.

## 2. Estructura

```text
novatech-store/
├── apps/
│   ├── storefront/                # React cliente, puerto 5173
│   └── backoffice-web/            # React personal, puerto 5174
├── services/
│   ├── commerce-service/         # Spring Boot, puerto 8080
│   │   ├── pom.xml
│   │   └── src/main/java/com/novatech/store/
│   │       ├── admin/            # Endpoints internos con doble control
│   │       ├── catalog/          # PostgreSQL + detalles MongoDB
│   │       ├── cart/             # Carrito
│   │       ├── orders/           # Checkout, locks, pedidos y PDF
│   │       ├── security/         # JWT y RBAC
│   │       ├── user/             # Registro y login
│   │       └── shared/           # Manejo de errores
│   ├── backoffice-service/       # API de empleados, Java 25
│   │   └── src/main/java/com/novatech/backoffice/
│   │       ├── admin/            # Endpoints empleados
│   │       ├── auth/             # Login personal
│   │       ├── clients/          # Cliente HTTP a commerce
│   │       ├── reports/          # Cliente HTTP al servicio informes
│   │       └── security/         # Validacion de JWT y roles
│   └── reports-service/          # Java 25 + JDBC + XLSX
├── docker-compose.yml           # Solo infraestructura de desarrollo
├── .env.example
└── README.md
```

## 3. Requisitos y puesta en marcha

- Windows 11, **JDK 25**, Node.js 26 LTS, Docker Desktop.
- IntelliJ IDEA, WebStorm; Maven desde IntelliJ o Maven CLI.
- Docker ejecuta PostgreSQL y MongoDB; **no hace falta instalar los motores de bases de datos en Windows**.

En la carpeta raíz, con Docker Desktop iniciado:

```powershell
docker compose up -d
docker compose ps
```

Abre individualmente en IntelliJ estos tres `pom.xml` e inicia las clases **en este orden**:

1. `services/commerce-service` → `com.novatech.store.StoreApplication` (8080).
2. `services/reports-service` → `com.novatech.reports.ReportsApplication` (8081).
3. `services/backoffice-service` → `com.novatech.backoffice.BackofficeApplication` (8082).

En WebStorm abre las dos carpetas de apps, cada una en su ventana/ proyecto:

```powershell
# terminal dentro de apps/storefront
npm install
npm run dev
```

```powershell
# terminal dentro de apps/backoffice-web
npm install
npm run dev
```

Abre **http://localhost:5173** (tienda) y **http://localhost:5174** (personal). Ambas usan proxy Vite hacia sus propias APIs; no se necesita cambiar CORS para el flujo local.

## 4. Credenciales demostrativas (solo local)

| Perfil | Email | Contraseña | Aplicación |
|---|---|---|---|
| Administrador | `admin@novatech.local` | `AdminDemo2026!` | Backoffice |
| Ventas | `ventas@novatech.local` | `VentasDemo2026!` | Backoffice |
| Almacén | `almacen@novatech.local` | `AlmacenDemo2026!` | Backoffice |
| Cliente | Se registra en `/login` de la tienda | Elegida al registrarse | Storefront |

Las cuentas del personal se crean automáticamente cuando `SEED_DEMO=true` (por defecto, solo desarrollo). **Cambia las contraseñas y desactiva la semilla antes de publicar.** Si un usuario ya existe, la semilla no modifica su contraseña ni rol.

| Acción | ADMIN | SALES_STAFF | INVENTORY_MANAGER | CUSTOMER |
|---|---:|---:|---:|---:|
| Consultar productos internos | Sí | Sí | Sí | No |
| Crear/editar productos, stock | Sí | No | Sí | No |
| Ver pedidos y devoluciones demo | Sí | Sí | No | No |
| Ver indicadores y Excel | Sí | Sí | No | No |
| Comprar en la tienda | No | No | No | Sí |

El backend verifica los permisos en los dos extremos del flujo administrativo. Ocultar un menú en React **no** constituye un control de seguridad.

## 5. API y rutas

**Storefront → Commerce 8080:**

| Método | Ruta | Acceso |
|---|---|---|
| `POST` | `/api/auth/register` | Público |
| `POST` | `/api/auth/login` | Público |
| `GET` | `/api/products` y `/api/products/{id}` | Público |
| `GET`, `PUT` | `/api/cart`, `/api/cart/items` | CUSTOMER |
| `POST` | `/api/orders/checkout` | CUSTOMER |
| `GET` | `/api/orders`, `/api/orders/{id}` | CUSTOMER |
| `POST` | `/api/orders/{id}/pay-demo` | CUSTOMER |
| `POST` | `/api/orders/{id}/cancel` | CUSTOMER |
| `GET` | `/api/orders/{id}/receipt.pdf` | CUSTOMER |

**Backoffice web → Backoffice 8082:**

| Método | Ruta | Roles |
|---|---|---|
| `POST` | `/api/auth/login` | Público; solo devuelve sesion a empleados |
| `GET` | `/api/admin/products` | Empleados |
| `POST` | `/api/admin/products` | ADMIN / INVENTORY_MANAGER |
| `PUT` | `/api/admin/products/{id}` | ADMIN / INVENTORY_MANAGER |
| `PATCH` | `/api/admin/products/{id}/stock` | ADMIN / INVENTORY_MANAGER |
| `GET` | `/api/admin/orders` | ADMIN / SALES_STAFF |
| `POST` | `/api/admin/orders/{id}/refund-demo` | ADMIN / SALES_STAFF |
| `GET` | `/api/admin/reports/summary` | ADMIN / SALES_STAFF |
| `GET` | `/api/admin/reports/store.xlsx` | ADMIN / SALES_STAFF |

El BFF llama a **Commerce** por `/internal/commerce/admin/*` pasando JWT y `X-Internal-Token`. También llama a **Reports** por `/internal/reports/*`, usando un segundo secreto. Estos endpoints **no** se consumen desde React.
