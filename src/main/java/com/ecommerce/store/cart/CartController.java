package com.ecommerce.store.cart;

import com.ecommerce.store.common.ApiException;
import com.ecommerce.store.product.*;
import com.ecommerce.store.user.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    public record QuantityRequest(@NotNull @Min(1) @Max(100) Integer quantity) {}
    public record CartLine(Long productId, String name, String imageUrl, BigDecimal unitPrice, int quantity, BigDecimal lineTotal, int stock) {}
    public record CartView(List<CartLine> items, BigDecimal total) {}
    private final CartRepository carts;
    private final ProductRepository products;
    private final UserRepository users;
    public CartController(CartRepository carts, ProductRepository products, UserRepository users) {
        this.carts = carts; this.products = products; this.users = users;
    }
    @GetMapping CartView get(Authentication auth) { return view(cart(auth)); }
    @PostMapping("/items/{productId}") CartView add(@PathVariable Long productId, @Valid @RequestBody QuantityRequest request, Authentication auth) {
        Product product = products.findById(productId).orElseThrow(() -> ApiException.notFound("Product not found"));
        if (request.quantity() > product.getStock()) throw ApiException.badRequest("Requested quantity exceeds available stock");
        ShoppingCart cart = cart(auth);
        CartItem item = cart.getItems().stream().filter(i -> i.getProduct().getId().equals(productId)).findFirst().orElse(null);
        int quantity = (item == null ? 0 : item.getQuantity()) + request.quantity();
        if (quantity > product.getStock()) throw ApiException.badRequest("Requested quantity exceeds available stock");
        if (item == null) cart.add(new CartItem(product, quantity)); else item.setQuantity(quantity);
        return view(carts.save(cart));
    }
    @PutMapping("/items/{productId}") CartView update(@PathVariable Long productId, @Valid @RequestBody QuantityRequest request, Authentication auth) {
        ShoppingCart cart = cart(auth);
        CartItem item = cart.getItems().stream().filter(i -> i.getProduct().getId().equals(productId)).findFirst()
                .orElseThrow(() -> ApiException.notFound("Cart item not found"));
        if (request.quantity() > item.getProduct().getStock()) throw ApiException.badRequest("Requested quantity exceeds available stock");
        item.setQuantity(request.quantity()); return view(carts.save(cart));
    }
    @DeleteMapping("/items/{productId}") CartView remove(@PathVariable Long productId, Authentication auth) {
        ShoppingCart cart = cart(auth);
        CartItem item = cart.getItems().stream().filter(i -> i.getProduct().getId().equals(productId)).findFirst()
                .orElseThrow(() -> ApiException.notFound("Cart item not found"));
        cart.remove(item); return view(carts.save(cart));
    }
    private ShoppingCart cart(Authentication auth) {
        AppUser user = users.findByEmailIgnoreCase(auth.getName()).orElseThrow(() -> ApiException.notFound("Account not found"));
        return carts.findByUserId(user.getId()).orElseGet(() -> carts.save(new ShoppingCart(user)));
    }
    private CartView view(ShoppingCart cart) {
        List<CartLine> items = cart.getItems().stream().map(item -> {
            var p = item.getProduct(); BigDecimal line = p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            return new CartLine(p.getId(), p.getName(), p.getImageUrl(), p.getPrice(), item.getQuantity(), line, p.getStock());
        }).toList();
        BigDecimal total = items.stream().map(CartLine::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartView(items, total);
    }
}
