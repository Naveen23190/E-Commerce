package com.ecommerce.store.user;

import com.ecommerce.store.common.ApiException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    public record UserProfile(Long id, String name, String email, String role) {}
    private final UserRepository users;
    public UserController(UserRepository users) { this.users = users; }
    @GetMapping("/me")
    UserProfile me(Authentication authentication) {
        AppUser user = users.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> ApiException.notFound("Account not found"));
        return new UserProfile(user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }
}
