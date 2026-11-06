package com.novatech.store.orders;

import com.novatech.store.cart.CartRepository;
import com.novatech.store.catalog.ProductRepository;
import com.novatech.store.shared.ApiException;
import com.novatech.store.user.AppUser;
import com.novatech.store.user.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class OrderServiceTest {
    @Test
    void duplicatePaymentDoesNotChargeAgain() {
        OrderRepository orders = mock(OrderRepository.class);
        OrderService service = new OrderService(orders, mock(CartRepository.class), mock(ProductRepository.class), mock(UserRepository.class), 15);
        AppUser user = new AppUser();
        user.id = 5L;
        PurchaseOrder order = new PurchaseOrder();
        order.id = 11L;
        order.user = user;
        order.status = "PENDING_PAYMENT";
        order.total = new BigDecimal("40.00");
        order.createdAt = Instant.now();
        order.expiresAt = Instant.now().plusSeconds(300);
        when(orders.lockById(11L)).thenReturn(Optional.of(order));
        assertEquals("PAID", service.payDemo(5L, 11L).status());
        Instant paidAt = order.paidAt;
        assertEquals("PAID", service.payDemo(5L, 11L).status());
        assertEquals(paidAt, order.paidAt);
        verify(orders, times(2)).lockById(11L);
    }

    @Test
    void anotherCustomerCannotPayThisOrder() {
        OrderRepository orders = mock(OrderRepository.class);
        OrderService service = new OrderService(orders, mock(CartRepository.class), mock(ProductRepository.class), mock(UserRepository.class), 15);
        AppUser user = new AppUser();
        user.id = 5L;
        PurchaseOrder order = new PurchaseOrder();
        order.user = user;
        when(orders.lockById(11L)).thenReturn(Optional.of(order));
        assertThrows(ApiException.class,() -> service.payDemo(999L, 11L));
    }
}
