package com.ecommerce.store.product;

import com.ecommerce.store.common.ApiException;
import com.ecommerce.store.common.PageResponse;
import com.ecommerce.store.cart.CartItemRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Function;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private static final int MAX_PAGE_SIZE = 100;
    public record ProductView(Long id, String name, String description, String category, BigDecimal price,
                              Integer stock, String imageUrl, java.time.Instant createdAt,
                              double averageRating, long reviewCount) {}
    public record ProductRequest(@NotBlank @Size(max=140) String name, @NotBlank @Size(max=2000) String description,
                                 @NotBlank @Size(max=80) String category, @NotNull @DecimalMin("0.01") BigDecimal price,
                                 @NotNull @Min(0) @Max(100000) Integer stock, @Size(max=500) String imageUrl) {}
    private final ProductRepository products;
    private final CartItemRepository cartItems;
    private final InventoryService inventory;
    private final ProductReviewRepository reviews;
    public ProductController(ProductRepository products, CartItemRepository cartItems, InventoryService inventory,
                             ProductReviewRepository reviews) {
        this.products = products; this.cartItems = cartItems; this.inventory = inventory; this.reviews = reviews;
    }
    @GetMapping PageResponse<ProductView> list(@RequestParam(required=false) String query,
                                    @RequestParam(required=false) String category,
                                    @RequestParam(required=false) BigDecimal minimumPrice,
                                    @RequestParam(required=false) BigDecimal maximumPrice,
                                    @RequestParam(defaultValue="0") double minimumRating,
                                    @RequestParam(defaultValue="false") boolean inStockOnly,
                                    @RequestParam(defaultValue="name") String sort,
                                    @RequestParam(defaultValue="asc") String direction,
                                    @RequestParam(defaultValue="0") int page,
                                    @RequestParam(defaultValue="12") int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE)
            throw ApiException.badRequest("Page must be zero or greater and size must be between 1 and 100");
        if ((minimumPrice != null && minimumPrice.signum() < 0)
                || (maximumPrice != null && maximumPrice.signum() < 0)
                || (minimumPrice != null && maximumPrice != null && minimumPrice.compareTo(maximumPrice) > 0)
                || minimumRating < 0 || minimumRating > 5)
            throw ApiException.badRequest("Enter a valid price range and rating from 0 to 5");
        String sortProperty = List.of("name", "price", "createdAt").contains(sort) ? sort : "name";
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortProperty).and(Sort.by("id")));
        Page<Product> result = products.search(blankToEmpty(query), blankToEmpty(category), minimumPrice,
                maximumPrice, minimumRating, inStockOnly, pageable);
        return new PageResponse<>(productViews(result.getContent()), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages(), result.isFirst(), result.isLast());
    }
    @GetMapping("/categories")
    List<String> categories() {
        return products.findDistinctCategories();
    }
    @GetMapping("/{id}") ProductView get(@PathVariable Long id) {
        Product product = products.findById(id).orElseThrow(() -> ApiException.notFound("Product not found"));
        return productViews(List.of(product)).get(0);
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('ADMIN')") @Transactional
    Product create(@Valid @RequestBody ProductRequest request, Authentication authentication) {
        Product product = products.save(new Product(request.name().trim(), request.description().trim(), request.category().trim(),
                request.price(), request.stock(), image(request.imageUrl())));
        inventory.recordChange(product, 0, "Initial stock on product creation", authentication.getName());
        return product;
    }
    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    Product update(@PathVariable Long id, @Valid @RequestBody ProductRequest request, Authentication authentication) {
        Product product = products.findByIdForUpdate(id).orElseThrow(() -> ApiException.notFound("Product not found"));
        int previousStock = product.getStock();
        product.update(request.name().trim(), request.description().trim(), request.category().trim(),
                request.price(), request.stock(), image(request.imageUrl()));
        inventory.recordChange(product, previousStock, "Stock updated through product management", authentication.getName());
        return products.save(product);
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasRole('ADMIN')")
    void delete(@PathVariable Long id) {
        if (!products.existsById(id)) throw ApiException.notFound("Product not found");
        if (cartItems.existsByProduct_Id(id)) throw ApiException.conflict("Remove this product from customer carts before deleting it");
        products.deleteById(id);
    }
    private String blankToEmpty(String value) { return value == null || value.isBlank() ? "" : value.trim(); }
    private String image(String value) {
        return value == null || value.isBlank()
                ? "https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?auto=format&fit=crop&w=1200&q=85"
                : value.trim();
    }

    private List<ProductView> productViews(List<Product> productList) {
        if (productList.isEmpty()) return List.of();
        List<Long> ids = productList.stream().map(Product::getId).toList();
        Map<Long, ProductReviewRepository.RatingSummary> summaries = reviews.summarizeByProductIds(ids).stream()
                .collect(Collectors.toMap(ProductReviewRepository.RatingSummary::getProductId, Function.identity()));
        return productList.stream().map(product -> {
            ProductReviewRepository.RatingSummary summary = summaries.get(product.getId());
            return new ProductView(product.getId(), product.getName(), product.getDescription(), product.getCategory(),
                    product.getPrice(), product.getStock(), product.getImageUrl(), product.getCreatedAt(),
                    summary == null ? 0 : summary.getAverageRating(),
                    summary == null ? 0 : summary.getReviewCount());
        }).toList();
    }
}
