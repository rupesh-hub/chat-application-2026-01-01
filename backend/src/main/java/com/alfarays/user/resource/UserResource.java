package com.alfarays.user.resource;


import com.alfarays.authentication.model.RegistrationRequest;
import com.alfarays.user.model.UserFilterDTO;
import com.alfarays.user.model.UserResponse;
import com.alfarays.user.service.IUserService;
import com.alfarays.util.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GlobalResponse<Boolean>> update(
            @RequestBody final RegistrationRequest request,
            @RequestParam(name = "username") String username,
            @RequestParam(required = false) MultipartFile profile

    ) throws IOException {
        return ResponseEntity.ok(userService.update(request, username, profile));
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

}
