package com.ecommerce.store.product;

import com.ecommerce.store.common.ApiException;
import com.ecommerce.store.order.OrderRepository;
import com.ecommerce.store.user.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products/{productId}/reviews")
public class ProductReviewController {
    public record ReviewRequest(@NotNull @Min(1) @Max(5) Integer rating, @NotBlank @Size(max=1000) String comment) {}
    public record ReviewView(Long id, String customerName, int rating, String comment, Instant createdAt) {}
    private final ProductRepository products;
    private final ProductReviewRepository reviews;
    private final UserRepository users;
    private final OrderRepository orders;

    public ProductReviewController(ProductRepository products, ProductReviewRepository reviews,
                                   UserRepository users, OrderRepository orders) {
        this.products = products;
        this.reviews = reviews;
        this.users = users;
        this.orders = orders;
    }

    @GetMapping
    List<ReviewView> list(@PathVariable Long productId) {
        if (!products.existsById(productId)) throw ApiException.notFound("Product not found");
        return reviews.findByProductIdOrderByCreatedAtDesc(productId).stream().map(this::view).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CUSTOMER')")
    ReviewView create(@PathVariable Long productId, @Valid @RequestBody ReviewRequest request, Authentication auth) {
        Product product = products.findById(productId).orElseThrow(() -> ApiException.notFound("Product not found"));
        AppUser customer = users.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> ApiException.notFound("Account not found"));
        if (!orders.hasPurchasedProduct(customer.getId(), productId))
            throw ApiException.badRequest("You can review a product after ordering it");
        if (reviews.existsByProductIdAndCustomerId(productId, customer.getId()))
            throw ApiException.conflict("You have already reviewed this product");
        ProductReview review = reviews.save(new ProductReview(product, customer, request.rating(), request.comment().trim()));
        return view(review);
    }

    private ReviewView view(ProductReview review) {
        return new ReviewView(review.getId(), review.getCustomer().getName(), review.getRating(),
                review.getComment(), review.getCreatedAt());
    }
}
