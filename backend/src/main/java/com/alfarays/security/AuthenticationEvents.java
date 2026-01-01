package com.alfarays.security;

import com.alfarays.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationEvents {

    private final UserRepository userRepository;

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        userRepository.findByEmail(event.getAuthentication().getName())
                .ifPresent(user -> {
                    user.setLastLogin(LocalDateTime.now());
                    userRepository.save(user);
                });
    }
}
