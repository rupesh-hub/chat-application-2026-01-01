package com.alfarays.user.resource;


import com.alfarays.exceptions.AuthorizationException;
import com.alfarays.user.model.ChangePasswordRequest;
import com.alfarays.user.model.UserFilterDTO;
import com.alfarays.user.model.UserResponse;
import com.alfarays.user.model.UserUpdateRequest;
import com.alfarays.user.service.IUserService;
import com.alfarays.util.GlobalResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserResource {

    private final IUserService userService;

    @GetMapping("/by.id/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GlobalResponse<UserResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.byId(id));
    }

    @GetMapping("/by.email/{email}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GlobalResponse<UserResponse>> getByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.byEmail(email));
    }

    @GetMapping("/current")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GlobalResponse<UserResponse>> profile(Authentication authentication) {
        if(null == authentication) throw new AuthorizationException("Authentication failure ! Please sign in first.");
        else if(!authentication.isAuthenticated()) throw new AuthorizationException("Please sign in first.");
        return ResponseEntity.ok(userService.byEmail(authentication.getName()));
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GlobalResponse<UserResponse>> update(
            @RequestBody final UserUpdateRequest request,
            Authentication authentication
    ) throws IOException {
        if(null == authentication) throw new AuthorizationException("Authentication failure ! Please sign in first.");
        else if(!authentication.isAuthenticated()) throw new AuthorizationException("Please sign in first.");
        return ResponseEntity.ok(userService.update(request, authentication.getName()));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GlobalResponse<Boolean>> delete(@RequestParam(name = "username") String username) throws IOException {
        return ResponseEntity.ok(userService.delete(username));
    }

    @GetMapping
    public GlobalResponse<List<UserResponse>> filterUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String firstname,
            @RequestParam(required = false) String lastname,
            @RequestParam(required = false) String email
    ) {
        UserFilterDTO filter = new UserFilterDTO();
        filter.setQuery(search);
        filter.setFirstname(firstname);
        filter.setLastname(lastname);
        filter.setEmail(email);
        Pageable pageable = PageRequest.of(page, size);
        return userService.filterUsers(filter, pageable);
    }


    @PostMapping("/change-password")
    public ResponseEntity<GlobalResponse<String>> passwordReset(
            @RequestBody @Valid ChangePasswordRequest request,
            Authentication authentication
    ) {
        if(null == authentication) throw new AuthorizationException("Authentication failure ! Please sign in first.");
        else if(!authentication.isAuthenticated()) throw new AuthorizationException("Please sign in first.");
        return ResponseEntity.ok(userService.changePassword(request, authentication.getName()));
    }

    @PutMapping("/change-profile")
    public ResponseEntity<GlobalResponse<String>> changeProfilePicture(
            @RequestPart(value = "profile", required = false) MultipartFile profile,
            Authentication authentication
    ) {
        if(null == authentication) throw new AuthorizationException("Authentication failure ! Please sign in first.");
        else if(!authentication.isAuthenticated()) throw new AuthorizationException("Please sign in first.");
        return ResponseEntity.ok(
                this.userService.changeProfilePicture(profile, authentication.getName())
        );
    }
}
