package com.alfarays.authentication.resource;

import com.alfarays.authentication.model.AuthenticationRequest;
import com.alfarays.authentication.model.RegistrationRequest;
import com.alfarays.authentication.service.IAuthenticationService;
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

}
