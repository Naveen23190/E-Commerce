package com.ecommerce.store.product;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByNameIgnoreCase(String name);

    @Query(value = "select p from Product p where (:category = '' or lower(p.category) = lower(:category)) and " +
            "(:query = '' or lower(p.name) like lower(concat('%', :query, '%')) or lower(p.description) like lower(concat('%', :query, '%'))) and " +
            "(:minimumPrice is null or p.price >= :minimumPrice) and (:maximumPrice is null or p.price <= :maximumPrice) and " +
            "(:inStockOnly = false or p.stock > 0) and " +
            "(:minimumRating <= 0 or exists (select r.product.id from ProductReview r where r.product.id = p.id " +
            "group by r.product.id having avg(r.rating) >= :minimumRating))",
            countQuery = "select count(p) from Product p where (:category = '' or lower(p.category) = lower(:category)) and " +
                    "(:query = '' or lower(p.name) like lower(concat('%', :query, '%')) or lower(p.description) like lower(concat('%', :query, '%'))) and " +
                    "(:minimumPrice is null or p.price >= :minimumPrice) and (:maximumPrice is null or p.price <= :maximumPrice) and " +
                    "(:inStockOnly = false or p.stock > 0) and " +
                    "(:minimumRating <= 0 or exists (select r.product.id from ProductReview r where r.product.id = p.id " +
                    "group by r.product.id having avg(r.rating) >= :minimumRating))")
    Page<Product> search(@Param("query") String query, @Param("category") String category,
                         @Param("minimumPrice") BigDecimal minimumPrice, @Param("maximumPrice") BigDecimal maximumPrice,
                         @Param("minimumRating") double minimumRating, @Param("inStockOnly") boolean inStockOnly,
                         Pageable pageable);

    @Query("select distinct p.category from Product p order by p.category asc")
    List<String> findDistinctCategories();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);
}
