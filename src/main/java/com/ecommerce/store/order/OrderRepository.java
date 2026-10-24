package com.ecommerce.store.order;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {
    interface OrderKey {
        Long getId();
        Instant getCreatedAt();
    }

    @Query(value = "select o.id as id, o.createdAt as createdAt from CustomerOrder o where o.customer.id = :customerId",
            countQuery = "select count(o.id) from CustomerOrder o where o.customer.id = :customerId")
    Page<OrderKey> findOrderKeysByCustomerId(@Param("customerId") Long customerId, Pageable pageable);
    @Query(value = "select o.id as id, o.createdAt as createdAt from CustomerOrder o",
            countQuery = "select count(o.id) from CustomerOrder o")
    Page<OrderKey> findAllOrderKeys(Pageable pageable);
    @Query("select distinct o from CustomerOrder o left join fetch o.items where o.id in :orderIds")
    List<CustomerOrder> findWithItemsByIdIn(@Param("orderIds") List<Long> orderIds);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from CustomerOrder o where o.id = :orderId")
    Optional<CustomerOrder> findByIdForUpdate(@Param("orderId") Long orderId);
    @Query("select case when count(item) > 0 then true else false end from CustomerOrder o " +
            "join o.items item where o.customer.id = :customerId and item.productId = :productId")
    boolean hasPurchasedProduct(@Param("customerId") Long customerId, @Param("productId") Long productId);
}
