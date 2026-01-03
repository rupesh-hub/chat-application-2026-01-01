package com.alfarays.security;

import com.alfarays.user.entity.User;
import com.alfarays.user.model.PrincipleUser;
import com.alfarays.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. First, check if the user exists
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new BadCredentialsException("Bad credentials!"));

        // 2. Check if the account is disabled
        if(Boolean.FALSE.equals(user.getEnabled())) {
            throw new DisabledException("Account is disabled. Please verify your email to activate your account.");
        }

        // 3. Return the principal
        return new PrincipleUser(user);
    }

}
