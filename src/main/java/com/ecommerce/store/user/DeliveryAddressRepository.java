package com.ecommerce.store.user;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Long> {
    List<DeliveryAddress> findByCustomerIdOrderByDefaultAddressDescCreatedAtDesc(Long customerId);
    Optional<DeliveryAddress> findByIdAndCustomerId(Long id, Long customerId);
    long countByCustomerId(Long customerId);
    void deleteByCustomerId(Long customerId);
}
