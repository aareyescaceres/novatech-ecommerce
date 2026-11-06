export type Session = {
    token: string;
    id: number;
    email: string;
    role: 'ADMIN' | 'CUSTOMER';
};
export type Product = {
    id: number;
    sku: string;
    name: string;
    brand: string;
    category: string;
    price: number;
    stock: number;
    active: boolean;
    description: string;
    specs: Record<string, string>;
    detailsAvailable: boolean;
};
export type Page<T> = {
    content: T[];
    number: number;
    totalPages: number;
    totalElements: number;
};
export type CartLine = {
    productId: number;
    name: string;
    sku: string;
    price: number;
    quantity: number;
    stock: number;
    subtotal: number;
};
export type Cart = {
    items: CartLine[];
    total: number;
};
export type Order = {
    id: number;
    status: 'PENDING_PAYMENT' | 'PAID' | 'CANCELLED' | 'EXPIRED' | 'REFUNDED';
    total: number;
    createdAt: string;
    expiresAt: string;
    paidAt: string | null;
    items: {
        sku: string;
        name: string;
        unitPrice: number;
        quantity: number;
        lineTotal: number;
    }[];
};
export type ProductInput = Omit<Product, 'id' | 'detailsAvailable'>;
export type Summary = {
    paidOrders: number;
    sales: number;
    unitsInStock: number;
    activeProducts: number;
};
