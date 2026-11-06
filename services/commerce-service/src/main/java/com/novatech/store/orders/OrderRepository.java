package com.novatech.store.orders;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<PurchaseOrder, Long> {
    Optional<PurchaseOrder> findByUserIdAndCheckoutKey(Long userId, String checkoutKey);
    List<PurchaseOrder> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<PurchaseOrder> findAllByOrderByCreatedAtDesc();
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from PurchaseOrder o where o.id = :id")
    Optional<PurchaseOrder> lockById(@Param("id") Long id);
    @Query("select o.id from PurchaseOrder o where o.status = 'PENDING_PAYMENT' and o.expiresAt < :now")
    List<Long> expiredIds(@Param("now") Instant now);
}
