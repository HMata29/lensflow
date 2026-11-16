
package com.lensflow.api.auth.controller;

import com.lensflow.api.auth.service.RegistrationService;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final RegistrationService registrationService;

    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    private final SessionAuthenticationStrategy sessionStrategy =
            new ChangeSessionIdAuthenticationStrategy();

    public AuthController(
            AuthenticationManager authenticationManager,
            RegistrationService registrationService
    ) {
        this.authenticationManager = authenticationManager;
        this.registrationService = registrationService;
    }

    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf(CsrfToken csrfToken) {
        csrfToken.getToken();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(
            @Valid @RequestBody RegistrationRequest request
    ) {
        String email = registrationService.register(
                request.email(),
                request.password(),
                request.firstName(),
                request.lastName(),
                request.studioName()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegistrationResponse(
                        email,
                        "Account creato correttamente"
                ));
    }

    
        @PostMapping("/login")
        public ResponseEntity<?> login(
                @Valid @RequestBody LoginRequest request,
                HttpServletRequest httpRequest,
                HttpServletResponse httpResponse
        ) {
        try {
                Authentication authentication =
                        authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                        request.email().trim()
                                                .toLowerCase(Locale.ROOT),
                                        request.password()
                                )
                        );

                httpRequest.getSession(true);

                sessionStrategy.onAuthentication(
                        authentication,
                        httpRequest,
                        httpResponse
                );

                SecurityContext context =
                        SecurityContextHolder.createEmptyContext();

                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);

                securityContextRepository.saveContext(
                        context,
                        httpRequest,
                        httpResponse
                );

                return ResponseEntity.ok(
                        new AuthResponse(
                                authentication.getName(),
                                authentication.getAuthorities()
                                        .stream()
                                        .map(authority ->
                                                authority.getAuthority())
                                        .toList()
                        )
                );

        } catch (AuthenticationException exception) {
                SecurityContextHolder.clearContext();

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse(
                                "Email o password non corrette"
                        ));
        }
        }


    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                new AuthResponse(
                        authentication.getName(),
                        authentication.getAuthorities()
                                .stream()
                                .map(authority ->
                                        authority.getAuthority())
                                .toList()
                )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request
    ) {
        SecurityContextHolder.clearContext();

        var session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.noContent().build();
    }

    public record RegistrationRequest(
            @NotBlank
            @Email
            @Size(max = 190)
            String email,

            @NotBlank
            @Size(min = 12, max = 72)
            String password,

            @NotBlank
            @Size(max = 100)
            String firstName,

            @NotBlank
            @Size(max = 100)
            String lastName,

            @Size(max = 150)
            String studioName
    ) {
    }

    public record LoginRequest(
            @NotBlank
            @Email
            String email,

            @NotBlank
            String password
    ) {
    }

    public record RegistrationResponse(
            String email,
            String message
    ) {
    }

    public record AuthResponse(
            String email,
            List<String> authorities
    ) {
    }

    public record ErrorResponse(
            String message
    ) {
    }
}
