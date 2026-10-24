package com.ecommerce.store.auth;

import com.ecommerce.store.common.ApiException;
import com.ecommerce.store.security.JwtService;
import com.ecommerce.store.user.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    public record RegisterRequest(@NotBlank @Size(max=120) String name, @Email @NotBlank @Size(max=190) String email,
                                 @NotBlank @Size(min=10,max=72) String password) {}
    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
    public record AuthResponse(String token, String name, String email, String role) {}
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwt;
    public AuthController(UserRepository users, PasswordEncoder encoder, AuthenticationManager authenticationManager, JwtService jwt) {
        this.users = users; this.encoder = encoder; this.authenticationManager = authenticationManager; this.jwt = jwt;
    }
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) throw ApiException.conflict("An account with that email already exists");
        AppUser user = users.save(new AppUser(request.name().trim(), email, encoder.encode(request.password()), Role.CUSTOMER));
        return response(user);
    }
    @PostMapping("/login")
    AuthResponse login(@Valid @RequestBody LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        try { authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password())); }
        catch (AuthenticationException ex) { throw new ApiException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect"); }
        return response(users.findByEmailIgnoreCase(email).orElseThrow());
    }
    private AuthResponse response(AppUser user) {
        var principal = org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPassword()).roles(user.getRole().name()).build();
        return new AuthResponse(jwt.issue(principal), user.getName(), user.getEmail(), user.getRole().name());
    }
}
