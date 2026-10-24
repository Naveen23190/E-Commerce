package com.ecommerce.store.product;

import com.ecommerce.store.common.ApiException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@PreAuthorize("hasRole('ADMIN')")
public class InventoryController {
    public record AdjustmentRequest(@NotNull @Min(-100000) @Max(100000) Integer quantityChange,
                                    @NotBlank @Size(max = 200) String reason) {}
    public record MovementView(Long id, Long productId, String productName, int quantityChange,
                               int stockAfter, String reason, String actor, Instant createdAt) {}

    private final ProductRepository products;
    private final InventoryMovementRepository movements;
    private final InventoryService inventory;

    public InventoryController(ProductRepository products, InventoryMovementRepository movements,
                               InventoryService inventory) {
        this.products = products;
        this.movements = movements;
        this.inventory = inventory;
    }

    @GetMapping("/inventory/movements")
    List<MovementView> list() {
        return movements.findTop100ByOrderByCreatedAtDesc().stream().map(this::view).toList();
    }

    @GetMapping("/products/{productId}/inventory/movements")
    List<MovementView> byProduct(@PathVariable Long productId) {
        if (!products.existsById(productId)) throw ApiException.notFound("Product not found");
        return movements.findTop100ByProductIdOrderByCreatedAtDesc(productId).stream().map(this::view).toList();
    }

    @PostMapping("/products/{productId}/inventory/adjust")
    @Transactional
    MovementView adjust(@PathVariable Long productId, @Valid @RequestBody AdjustmentRequest request,
                        Authentication authentication) {
        if (request.quantityChange() == 0)
            throw ApiException.badRequest("Stock adjustment must be a non-zero quantity");
        Product product = products.findByIdForUpdate(productId)
                .orElseThrow(() -> ApiException.notFound("Product not found"));
        int updatedStock = product.getStock() + request.quantityChange();
        if (updatedStock < 0 || updatedStock > 100000)
            throw ApiException.badRequest("Adjustment would put stock outside the allowed range");
        product.setStock(updatedStock);
        InventoryMovement movement = inventory.recordChange(product, updatedStock - request.quantityChange(),
                request.reason().trim(), authentication.getName());
        return view(movement);
    }

    private MovementView view(InventoryMovement movement) {
        return new MovementView(movement.getId(), movement.getProductId(), movement.getProductName(),
                movement.getQuantityChange(), movement.getStockAfter(), movement.getReason(),
                movement.getActor(), movement.getCreatedAt());
    }
}
