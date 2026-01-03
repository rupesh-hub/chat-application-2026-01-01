package com.alfarays.authentication.resource;

import com.alfarays.authentication.model.AuthenticationRequest;
import com.alfarays.authentication.model.RegistrationRequest;
import com.alfarays.authentication.model.ResetPasswordRequest;
import com.alfarays.authentication.service.IAuthenticationService;
import com.alfarays.token.enums.TokenType;
import com.alfarays.util.GlobalResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@RestController
@RequestMapping("/authentication")
@RequiredArgsConstructor
public class AuthenticationResource {

    private final IAuthenticationService authenticationService;

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GlobalResponse<String>> register(
            @Valid @ModelAttribute RegistrationRequest request,
            @RequestPart(value = "profile", required = false) MultipartFile profile
    )
            throws MethodArgumentNotValidException, IOException {
        return ResponseEntity.ok(authenticationService.register(request, profile));
    }

    @PostMapping
    public ResponseEntity<?> login(@Valid @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @GetMapping("/exists-by-email")
    public ResponseEntity<GlobalResponse<Boolean>> existsByEmail(@RequestParam("email") String email) {
        return ResponseEntity.ok(authenticationService.existsByEmail(email));
    }

    @GetMapping("/confirm-email")
    public ResponseEntity<GlobalResponse<String>> confirmEmail(
            @RequestParam String token,
            @RequestParam("email") String email
    ) {
        GlobalResponse<String> globalResponse = authenticationService.activateAccount(email, token, TokenType.ACCOUNT_ACTIVATED);
        if(Objects.equals(globalResponse.getCode(), "500"))
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(globalResponse);
        return ResponseEntity.ok(globalResponse);
    }

    @GetMapping("/forgot-password-request")
    public ResponseEntity<GlobalResponse<String>> forgetPassword(@RequestParam("email") String email) {
        return ResponseEntity.ok(authenticationService.forgetPasswordRequest(email));
    }

    @GetMapping("/resend-confirmation-token")
    public ResponseEntity<GlobalResponse<String>> resendConfirmationToken(@RequestParam("email") String email,
                                                                          @RequestParam("token") String token) {
        return ResponseEntity.ok(authenticationService.resendConfirmationToken(email, token));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<GlobalResponse<String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        GlobalResponse<String> response = authenticationService.resetPassword(request);
        if(Objects.equals(response.getCode(), "500"))
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        return ResponseEntity.ok(response);
    }

}
