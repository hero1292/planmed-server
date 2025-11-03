package ru.alexanyan.planmed.auth.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.alexanyan.planmed.auth.service.InviteService;
import ru.alexanyan.planmed.auth.service.RegistrationService;
import ru.alexanyan.planmed.auth.service.VerificationTokenService;

@RestController
@RequestMapping
@Validated
@AllArgsConstructor
public class AuthController {

    private final RegistrationService registrationService;
    private final VerificationTokenService verificationService;
    private final InviteService inviteService;

    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        registrationService.register(req.getLogin(), req.getPassword(), req.getRole(), req.getContactEmail());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping("/auth/resend-verification")
    public ResponseEntity<?> resend(@Valid @RequestBody ResendRequest req) {
        registrationService.resendVerification(req.getLoginOrEmail());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @GetMapping("/auth/verify")
    public ResponseEntity<?> verify(@RequestParam("token") String token) {
        var result = verificationService.verifyEmailToken(token);
        return switch (result) {
            case VERIFIED, ALREADY_VERIFIED -> ResponseEntity.noContent().build();
            case EXPIRED -> ResponseEntity.status(HttpStatus.GONE).build();
            case INVALID -> ResponseEntity.badRequest().build();
        };
    }

    @PostMapping("/admin/invites")
    public ResponseEntity<?> createInvite(@Valid @RequestBody InviteCreateRequest req) {
        inviteService.createInvite(req.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/admin/invites/accept")
    public ResponseEntity<?> acceptInvite(@RequestParam("token") String token) {
        var result = inviteService.acceptInvite(token);
        return switch (result) {
            case ACCEPTED, ALREADY_ACCEPTED -> ResponseEntity.noContent().build();
            case EXPIRED -> ResponseEntity.status(HttpStatus.GONE).build();
            case INVALID -> ResponseEntity.badRequest().build();
        };
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RegisterRequest {
        @NotBlank
        @Pattern(regexp = "^[a-zA-Z0-9._-]{3,64}$")
        private String login;

        @NotBlank
        private String password;

        @NotBlank
        @Pattern(regexp = "PATIENT|DOCTOR")
        private String role;

        @NotBlank
        @Email
        private String contactEmail;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResendRequest {
        @NotBlank
        private String loginOrEmail;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InviteCreateRequest {
        @NotBlank
        @Email
        private String email;
    }
}
