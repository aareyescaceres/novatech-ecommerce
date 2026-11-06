export type Role = 'ADMIN' | 'SALES_STAFF' | 'INVENTORY_MANAGER';
export type Session = { token: string; id: number; email: string; role: Role };
export type Product = {
  id: number; sku: string; name: string; brand: string; category: string;
  price: number; stock: number; active: boolean; description: string;
  specs: Record<string, string>; detailsAvailable: boolean;
};
export type ProductInput = Omit<Product, 'id' | 'detailsAvailable'>;
export type Order = {
  id: number; status: 'PENDING_PAYMENT' | 'PAID' | 'CANCELLED' | 'EXPIRED' | 'REFUNDED';
  total: number; createdAt: string; expiresAt: string; paidAt: string | null;
  items: {sku: string; name: string; quantity: number; unitPrice: number; lineTotal: number}[];
};
export type Summary = {
  paidOrders: number; sales: number; unitsInStock: number; activeProducts: number;
};
