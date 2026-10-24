package com.ecommerce.store.product;

import org.springframework.stereotype.Service;

@Service
public class InventoryService {
    private final InventoryMovementRepository movements;

    public InventoryService(InventoryMovementRepository movements) {
        this.movements = movements;
    }

    public InventoryMovement recordChange(Product product, int previousStock, String reason, String actor) {
        int change = product.getStock() - previousStock;
        if (change == 0) return null;
        return movements.save(new InventoryMovement(product, change, reason, actor));
    }
}
