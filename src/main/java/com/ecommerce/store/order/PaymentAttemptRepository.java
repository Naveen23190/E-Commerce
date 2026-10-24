package com.ecommerce.store.order;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PaymentAttempt p where p.id = :id and p.customer.id = :customerId")
    Optional<PaymentAttempt> findForCustomerForUpdate(@Param("id") String id, @Param("customerId") Long customerId);
}
