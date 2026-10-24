package com.ecommerce.store.user;

import com.ecommerce.store.common.ApiException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/addresses")
public class DeliveryAddressController {
    public record AddressRequest(@NotBlank @Size(max = 40) String label,
                                 @NotBlank @Size(max = 500) String address,
                                 @NotBlank @Pattern(regexp = "[1-9][0-9]{5}") String pinCode,
                                 boolean defaultAddress) {}
    public record AddressView(Long id, String label, String address, String pinCode,
                              boolean defaultAddress, Instant createdAt) {}

    private final DeliveryAddressRepository addresses;
    private final UserRepository users;

    public DeliveryAddressController(DeliveryAddressRepository addresses, UserRepository users) {
        this.addresses = addresses;
        this.users = users;
    }

    @GetMapping
    List<AddressView> list(Authentication authentication) {
        AppUser customer = customer(authentication);
        return addresses.findByCustomerIdOrderByDefaultAddressDescCreatedAtDesc(customer.getId())
                .stream().map(this::view).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    AddressView create(@Valid @RequestBody AddressRequest request, Authentication authentication) {
        AppUser customer = customer(authentication);
        boolean makeDefault = request.defaultAddress() || addresses.countByCustomerId(customer.getId()) == 0;
        if (makeDefault) clearDefault(customer.getId());
        DeliveryAddress saved = addresses.save(new DeliveryAddress(customer, request.label().trim(),
                request.address().trim(), request.pinCode(), makeDefault));
        return view(saved);
    }

    @PutMapping("/{id}")
    @Transactional
    AddressView update(@PathVariable Long id, @Valid @RequestBody AddressRequest request,
                       Authentication authentication) {
        AppUser customer = customer(authentication);
        DeliveryAddress address = ownedAddress(id, customer.getId());
        if (request.defaultAddress()) {
            clearDefault(customer.getId());
            address.setDefaultAddress(true);
        }
        address.update(request.label().trim(), request.address().trim(), request.pinCode());
        return view(address);
    }

    @PutMapping("/{id}/default")
    @Transactional
    AddressView setDefault(@PathVariable Long id, Authentication authentication) {
        AppUser customer = customer(authentication);
        DeliveryAddress address = ownedAddress(id, customer.getId());
        clearDefault(customer.getId());
        address.setDefaultAddress(true);
        return view(address);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    void delete(@PathVariable Long id, Authentication authentication) {
        AppUser customer = customer(authentication);
        DeliveryAddress address = ownedAddress(id, customer.getId());
        boolean wasDefault = address.isDefaultAddress();
        addresses.delete(address);
        if (wasDefault) {
            addresses.findByCustomerIdOrderByDefaultAddressDescCreatedAtDesc(customer.getId()).stream()
                    .findFirst().ifPresent(next -> next.setDefaultAddress(true));
        }
    }

    private AppUser customer(Authentication authentication) {
        return users.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> ApiException.notFound("Account not found"));
    }

    private DeliveryAddress ownedAddress(Long addressId, Long customerId) {
        return addresses.findByIdAndCustomerId(addressId, customerId)
                .orElseThrow(() -> ApiException.notFound("Delivery address not found"));
    }

    private void clearDefault(Long customerId) {
        addresses.findByCustomerIdOrderByDefaultAddressDescCreatedAtDesc(customerId).stream()
                .filter(DeliveryAddress::isDefaultAddress)
                .forEach(address -> address.setDefaultAddress(false));
    }

    private AddressView view(DeliveryAddress address) {
        return new AddressView(address.getId(), address.getLabel(), address.getAddress(),
                address.getPinCode(), address.isDefaultAddress(), address.getCreatedAt());
    }
}
