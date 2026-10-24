package com.ecommerce.store.product;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
    List<InventoryMovement> findTop100ByOrderByCreatedAtDesc();
    List<InventoryMovement> findTop100ByProductIdOrderByCreatedAtDesc(Long productId);
}
