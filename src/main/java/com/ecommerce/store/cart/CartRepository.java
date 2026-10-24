package com.ecommerce.store.cart;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<ShoppingCart, Long> {
    @EntityGraph(attributePaths = {"items", "items.product"})
    Optional<ShoppingCart> findByUserId(Long userId);
}
