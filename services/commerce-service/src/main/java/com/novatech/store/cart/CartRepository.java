package com.novatech.store.cart;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserId(Long userId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cart c where c.user.id = :userId")
    Optional<Cart> lockByUserId(@Param("userId") Long userId);
}
