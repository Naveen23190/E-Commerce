package com.ecommerce.store.product;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    interface RatingSummary {
        Long getProductId();
        Double getAverageRating();
        Long getReviewCount();
    }

    @EntityGraph(attributePaths = "customer")
    List<ProductReview> findByProductIdOrderByCreatedAtDesc(Long productId);
    boolean existsByProductIdAndCustomerId(Long productId, Long customerId);

    @Query("select r.product.id as productId, avg(r.rating) as averageRating, count(r.id) as reviewCount " +
            "from ProductReview r where r.product.id in :productIds group by r.product.id")
    List<RatingSummary> summarizeByProductIds(@Param("productIds") List<Long> productIds);
}
