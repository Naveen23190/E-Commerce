package com.ecommerce.store.order;

import com.ecommerce.store.cart.*;
import com.ecommerce.store.common.ApiException;
import com.ecommerce.store.common.PageResponse;
import com.ecommerce.store.product.*;
import com.ecommerce.store.user.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private static final int MAX_PAGE_SIZE = 100;
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private static final BigDecimal SHIPPING_FEE = new BigDecimal("99.00");
    private static final List<OrderStatus> FULFILLMENT_STEPS =
            List.of(OrderStatus.PLACED, OrderStatus.CONFIRMED, OrderStatus.PACKED,
                    OrderStatus.SHIPPED, OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED);
    public record CheckoutRequest(@NotBlank @Size(max=500) String shippingAddress,
                                  @NotBlank @Pattern(regexp="[1-9][0-9]{5}", message="Enter a valid 6-digit Indian PIN code") String pinCode,
                                  @NotBlank @Pattern(regexp="COD|UPI_DEMO|CARD_DEMO", message="Choose a valid demo payment method") String paymentMethod) {}
    public record RazorpayOrderRequest(@NotBlank @Size(max=500) String shippingAddress,
                                       @NotBlank @Pattern(regexp="[1-9][0-9]{5}") String pinCode) {}
    public record RazorpayVerifyRequest(@NotBlank String attemptId, @NotBlank String razorpayOrderId,
                                        @NotBlank String razorpayPaymentId, @NotBlank String razorpaySignature) {}
    public record RazorpayCheckout(String keyId, String gatewayOrderId, String attemptId, long amount,
                                   String currency, String merchantName, String description, String prefillEmail) {}
    public record OrderLine(Long productId, String productName, int quantity, BigDecimal unitPrice) {}
    public record StatusStep(String status, boolean complete) {}
    public record OrderView(Long id, BigDecimal total, BigDecimal subtotal, BigDecimal shippingFee, String status,
                            String shippingAddress, String pinCode, String paymentMethod, String paymentStatus,
                            java.time.Instant createdAt, List<OrderLine> items, List<StatusStep> tracking) {}
    private final OrderRepository orders;
    private final CartRepository carts;
    private final ProductRepository products;
    private final UserRepository users;
    private final InventoryService inventory;
    private final PaymentAttemptRepository paymentAttempts;
    private final RazorpayPaymentService razorpay;
    public OrderController(OrderRepository orders, CartRepository carts, ProductRepository products, UserRepository users,
                           InventoryService inventory, PaymentAttemptRepository paymentAttempts,
                           RazorpayPaymentService razorpay) {
        this.orders = orders; this.carts = carts; this.products = products; this.users = users;
        this.inventory = inventory;
        this.paymentAttempts = paymentAttempts;
        this.razorpay = razorpay;
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @Transactional
    OrderView checkout(@Valid @RequestBody CheckoutRequest request, Authentication auth) {
        if ("RAZORPAY_TEST".equals(request.paymentMethod()))
            throw ApiException.badRequest("Use the Razorpay test checkout flow to complete this payment method");
        AppUser user = users.findByEmailIgnoreCase(auth.getName()).orElseThrow(() -> ApiException.notFound("Account not found"));
        ShoppingCart cart = carts.findByUserId(user.getId()).orElseThrow(() -> ApiException.badRequest("Your cart is empty"));
        if (cart.getItems().isEmpty()) throw ApiException.badRequest("Your cart is empty");
        BigDecimal subtotal = BigDecimal.ZERO;
        List<Product> lockedProducts = new ArrayList<>();
        List<CartItem> cartLines = cart.getItems().stream()
                .sorted(Comparator.comparing(line -> line.getProduct().getId()))
                .toList();
        for (CartItem line : cartLines) {
            Product product = products.findByIdForUpdate(line.getProduct().getId())
                    .orElseThrow(() -> ApiException.conflict("A product in your cart is no longer available"));
            if (product.getStock() < line.getQuantity()) throw ApiException.conflict("Not enough stock for " + product.getName());
            int previousStock = product.getStock();
            product.reduceStock(line.getQuantity());
            inventory.recordChange(product, previousStock, "Customer checkout order", auth.getName());
            lockedProducts.add(product);
            subtotal = subtotal.add(product.getPrice().multiply(BigDecimal.valueOf(line.getQuantity())));
        }
        String paymentStatus = "COD".equals(request.paymentMethod()) ? "PAY_ON_DELIVERY" : "SIMULATED";
        CustomerOrder order = new CustomerOrder(user, subtotal, SHIPPING_FEE, request.shippingAddress().trim(),
                request.pinCode(), request.paymentMethod(), paymentStatus);
        for (int i = 0; i < lockedProducts.size(); i++) {
            Product product = lockedProducts.get(i);
            CartItem line = cartLines.get(i);
            order.add(new OrderItem(product.getId(), product.getName(), line.getQuantity(), product.getPrice()));
        }
        CustomerOrder saved = orders.save(order);
        cart.clear(); carts.save(cart);
        log.info("Demo order confirmation notification (no email sent) for order {}", saved.getId());
        return view(saved);
    }
    @PostMapping("/razorpay/order")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Transactional
    RazorpayCheckout createRazorpayOrder(@Valid @RequestBody RazorpayOrderRequest request, Authentication auth) {
        AppUser user = users.findByEmailIgnoreCase(auth.getName()).orElseThrow(() -> ApiException.notFound("Account not found"));
        ShoppingCart cart = carts.findByUserId(user.getId())
                .orElseThrow(() -> ApiException.badRequest("Your cart is empty"));
        if (cart.getItems().isEmpty()) throw ApiException.badRequest("Your cart is empty");
        List<CartItem> lines = cart.getItems().stream()
                .sorted(Comparator.comparing(line -> line.getProduct().getId()))
                .toList();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem line : lines) {
            Product product = line.getProduct();
            if (product.getStock() < line.getQuantity())
                throw ApiException.conflict("Not enough stock for " + product.getName());
            subtotal = subtotal.add(product.getPrice().multiply(BigDecimal.valueOf(line.getQuantity())));
        }
        long amountInPaise;
        try {
            amountInPaise = subtotal.add(SHIPPING_FEE).movePointRight(2).longValueExact();
        } catch (ArithmeticException exception) {
            throw ApiException.badRequest("Order total is outside the supported payment range");
        }
        String receipt = "ecart-" + UUID.randomUUID().toString().replace("-", "");
        RazorpayPaymentService.GatewayOrder gatewayOrder = razorpay.createOrder(amountInPaise, receipt);
        PaymentAttempt attempt = new PaymentAttempt(user, gatewayOrder.id(), subtotal, SHIPPING_FEE,
                request.shippingAddress().trim(), request.pinCode(), Instant.now().plus(Duration.ofMinutes(15)));
        for (CartItem line : lines) {
            Product product = line.getProduct();
            attempt.add(new PaymentAttemptItem(product.getId(), product.getName(),
                    line.getQuantity(), product.getPrice()));
        }
        PaymentAttempt savedAttempt = paymentAttempts.save(attempt);
        return new RazorpayCheckout(razorpay.keyId(), gatewayOrder.id(), savedAttempt.getId(), gatewayOrder.amount(),
                gatewayOrder.currency(), "E-Cart India", "Order " + savedAttempt.getId().substring(0, 8), user.getEmail());
    }

    @PostMapping("/razorpay/verify")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Transactional(noRollbackFor = ApiException.class)
    OrderView verifyRazorpayPayment(@Valid @RequestBody RazorpayVerifyRequest request, Authentication auth) {
        AppUser user = users.findByEmailIgnoreCase(auth.getName()).orElseThrow(() -> ApiException.notFound("Account not found"));
        PaymentAttempt attempt = paymentAttempts.findForCustomerForUpdate(request.attemptId(), user.getId())
                .orElseThrow(() -> ApiException.notFound("Payment attempt not found"));
        if (!attempt.getGatewayOrderId().equals(request.razorpayOrderId()))
            throw ApiException.badRequest("Payment does not match this checkout");
        if ("COMPLETED".equals(attempt.getStatus())) {
            if (!request.razorpayPaymentId().equals(attempt.getGatewayPaymentId()))
                throw ApiException.conflict("This checkout was already completed with another payment");
            return view(attempt.getCustomerOrder());
        }
        if (!"CREATED".equals(attempt.getStatus())) {
            if ("REFUND_PENDING".equals(attempt.getStatus()))
                throw ApiException.conflict("A refund for this captured test payment is still processing with Razorpay.");
            if ("REFUNDED".equals(attempt.getStatus()))
                throw ApiException.conflict("This test payment has already been refunded.");
            if ("REFUND_REQUIRED".equals(attempt.getStatus()))
                throw ApiException.conflict("This captured payment requires manual refund recovery. Contact support with checkout reference "
                        + attempt.getId() + " and payment reference " + attempt.getGatewayPaymentId() + ".");
            throw ApiException.conflict("This payment attempt has expired. Start checkout again.");
        }
        if (!razorpay.isValidSignature(request.razorpayOrderId(), request.razorpayPaymentId(),
                request.razorpaySignature()))
            throw ApiException.badRequest("Razorpay payment signature is invalid");
        RazorpayPaymentService.GatewayPayment payment = razorpay.payment(request.razorpayPaymentId());
        long expectedAmount = attempt.getSubtotal().add(attempt.getShippingFee()).movePointRight(2)
                .setScale(0, RoundingMode.UNNECESSARY).longValueExact();
        if (!request.razorpayPaymentId().equals(payment.id())
                || !request.razorpayOrderId().equals(payment.orderId())
                || !"INR".equals(payment.currency()) || !"captured".equalsIgnoreCase(payment.status()))
            throw ApiException.badRequest("Razorpay did not confirm a captured payment for this order");
        if (attempt.getExpiresAt().isBefore(Instant.now()))
            throw refundCapturedPayment(attempt, payment, "This checkout expired before payment verification.");
        if (payment.amount() != expectedAmount)
            throw refundCapturedPayment(attempt, payment, "The captured amount did not match this checkout.");

        List<PaymentAttemptItem> lines = attempt.getItems().stream()
                .sorted(Comparator.comparing(PaymentAttemptItem::getProductId))
                .toList();
        List<Product> lockedProducts = new ArrayList<>();
        BigDecimal currentSubtotal = BigDecimal.ZERO;
        for (PaymentAttemptItem line : lines) {
            Product product = products.findByIdForUpdate(line.getProductId())
                    .orElse(null);
            if (product == null)
                throw refundCapturedPayment(attempt, payment, "A product in this order is no longer available.");
            if (product.getStock() < line.getQuantity())
                throw refundCapturedPayment(attempt, payment, "There is no longer enough stock for " + product.getName() + ".");
            if (product.getPrice().compareTo(line.getUnitPrice()) != 0)
                throw refundCapturedPayment(attempt, payment, "The price of " + product.getName() + " changed during checkout.");
            currentSubtotal = currentSubtotal.add(product.getPrice().multiply(BigDecimal.valueOf(line.getQuantity())));
            lockedProducts.add(product);
        }
        if (currentSubtotal.compareTo(attempt.getSubtotal()) != 0)
            throw refundCapturedPayment(attempt, payment, "The cart total changed during checkout.");

        CustomerOrder order = new CustomerOrder(user, currentSubtotal, attempt.getShippingFee(),
                attempt.getShippingAddress(), attempt.getPinCode(), "RAZORPAY_TEST", "PAID_TEST");
        for (int index = 0; index < lines.size(); index++) {
            PaymentAttemptItem line = lines.get(index);
            Product product = lockedProducts.get(index);
            int previousStock = product.getStock();
            product.reduceStock(line.getQuantity());
            inventory.recordChange(product, previousStock, "Razorpay test order checkout", user.getEmail());
            order.add(new OrderItem(product.getId(), product.getName(), line.getQuantity(), product.getPrice()));
        }
        CustomerOrder savedOrder = orders.save(order);
        attempt.complete(payment.id(), savedOrder);
        paymentAttempts.save(attempt);
        clearCartIfMatches(user.getId(), lines);
        log.info("Razorpay test payment captured for order {} (test mode)", savedOrder.getId());
        return view(savedOrder);
    }

    private ApiException refundCapturedPayment(PaymentAttempt attempt, RazorpayPaymentService.GatewayPayment payment,
                                              String reason) {
        attempt.beginRefund(payment.id());
        try {
            RazorpayPaymentService.GatewayRefund refund =
                    razorpay.refund(payment.id(), payment.amount(), attempt.getId());
            attempt.completeRefund(refund.id(), refund.status());
            if ("REFUNDED".equals(attempt.getStatus())) {
                log.info("Razorpay test payment refunded for checkout attempt {}", attempt.getId());
                return ApiException.conflict(reason + " The captured test payment was refunded.");
            }
            log.warn("Razorpay test refund is pending for checkout attempt {} (refund {})",
                    attempt.getId(), refund.id());
            return ApiException.conflict(reason + " The test refund is pending with Razorpay.");
        } catch (ApiException exception) {
            attempt.requireManualRefund();
            log.error("Razorpay test refund needs manual recovery for attempt {} and payment {}",
                    attempt.getId(), payment.id());
            return manualRefundRequired(attempt, payment);
        } catch (IllegalStateException exception) {
            attempt.requireManualRefund();
            log.error("Razorpay returned an unconfirmed test refund for attempt {} and payment {}",
                    attempt.getId(), payment.id());
            return manualRefundRequired(attempt, payment);
        }
    }

    private ApiException manualRefundRequired(PaymentAttempt attempt,
                                              RazorpayPaymentService.GatewayPayment payment) {
        return new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                "The payment was captured but its refund could not be confirmed. Contact support with checkout reference "
                        + attempt.getId() + " and payment reference " + payment.id() + ".");
    }

    private void clearCartIfMatches(Long customerId, List<PaymentAttemptItem> purchasedLines) {
        carts.findByUserId(customerId).ifPresent(cart -> {
            boolean matches = cart.getItems().size() == purchasedLines.size()
                    && purchasedLines.stream().allMatch(line -> cart.getItems().stream().anyMatch(cartLine ->
                    cartLine.getProduct().getId().equals(line.getProductId())
                            && cartLine.getQuantity().equals(line.getQuantity())));
            if (matches) {
                cart.clear();
                carts.save(cart);
            }
        });
    }
    @GetMapping PageResponse<OrderView> history(Authentication auth,
                                         @RequestParam(defaultValue="0") int page,
                                         @RequestParam(defaultValue="5") int size) {
        validatePagination(page, size);
        AppUser user = users.findByEmailIgnoreCase(auth.getName()).orElseThrow(() -> ApiException.notFound("Account not found"));
        Pageable pageable = orderPageable(page, size);
        Page<Long> orderIds = orders.findOrderKeysByCustomerId(user.getId(), pageable).map(OrderRepository.OrderKey::getId);
        return PageResponse.from(loadOrderPage(orderIds, pageable));
    }
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    PageResponse<OrderView> allOrders(@RequestParam(defaultValue="0") int page,
                                      @RequestParam(defaultValue="5") int size) {
        validatePagination(page, size);
        Pageable pageable = orderPageable(page, size);
        Page<Long> orderIds = orders.findAllOrderKeys(pageable).map(OrderRepository.OrderKey::getId);
        return PageResponse.from(loadOrderPage(orderIds, pageable));
    }
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    OrderView updateStatus(@PathVariable Long orderId, @Valid @RequestBody StatusRequest request,
                           Authentication authentication) {
        CustomerOrder order = orders.findByIdForUpdate(orderId)
                .orElseThrow(() -> ApiException.notFound("Order not found"));
        OrderStatus current = OrderStatus.valueOf(order.getStatus());
        if (!current.canTransitionTo(request.status()))
            throw ApiException.badRequest("Order status can only move to the next fulfillment step; cancellation is only available before packing");
        if (request.status() == OrderStatus.CANCELLED) {
            if ("RAZORPAY_TEST".equals(order.getPaymentMethod()) && "PAID_TEST".equals(order.getPaymentStatus()))
                throw ApiException.conflict("Captured Razorpay test orders cannot be cancelled through admin fulfillment. Contact support for refund and cancellation handling.");
            List<OrderItem> items = order.getItems().stream()
                    .sorted(Comparator.comparing(OrderItem::getProductId))
                    .toList();
            for (OrderItem item : items) {
                Product product = products.findByIdForUpdate(item.getProductId())
                        .orElseThrow(() -> ApiException.conflict("Unable to restore stock because a product in this order is no longer available"));
                int previousStock = product.getStock();
                product.increaseStock(item.getQuantity());
                inventory.recordChange(product, previousStock, "Cancelled customer order " + order.getId(),
                        authentication.getName());
            }
        }
        order.updateStatus(request.status());
        log.info("Demo order status notification (no email sent) for order {}: {}", order.getId(), request.status());
        return view(order);
    }
    public record StatusRequest(@NotNull OrderStatus status) {}
    private void validatePagination(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE)
            throw ApiException.badRequest("Page must be zero or greater and size must be between 1 and 100");
    }
    private Pageable orderPageable(int page, int size) {
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")
                .and(Sort.by(Sort.Direction.DESC, "id")));
    }
    private Page<OrderView> loadOrderPage(Page<Long> orderIds, Pageable pageable) {
        if (orderIds.isEmpty()) return new PageImpl<>(List.of(), pageable, orderIds.getTotalElements());
        List<CustomerOrder> pageOrders = orders.findWithItemsByIdIn(orderIds.getContent());
        var byId = pageOrders.stream().collect(java.util.stream.Collectors.toMap(CustomerOrder::getId, order -> order));
        List<OrderView> views = orderIds.getContent().stream().map(byId::get).map(this::view).toList();
        return new PageImpl<>(views, pageable, orderIds.getTotalElements());
    }
    private OrderView view(CustomerOrder order) {
        OrderStatus current = OrderStatus.valueOf(order.getStatus());
        int completedThrough = FULFILLMENT_STEPS.indexOf(current);
        List<StatusStep> tracking = new ArrayList<>(FULFILLMENT_STEPS.stream()
                .map(step -> new StatusStep(step.name(), completedThrough >= FULFILLMENT_STEPS.indexOf(step)))
                .toList());
        if (current == OrderStatus.CANCELLED) tracking.add(new StatusStep(OrderStatus.CANCELLED.name(), true));
        return new OrderView(order.getId(), order.getTotal(), order.getSubtotal(), order.getShippingFee(),
                order.getStatus(), order.getShippingAddress(), order.getPinCode(), order.getPaymentMethod(),
                order.getPaymentStatus(), order.getCreatedAt(),
                order.getItems().stream().map(i -> new OrderLine(i.getProductId(), i.getProductName(), i.getQuantity(), i.getUnitPrice())).toList(),
                tracking);
    }
}
