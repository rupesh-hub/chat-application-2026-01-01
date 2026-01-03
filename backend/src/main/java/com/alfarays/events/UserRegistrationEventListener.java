package com.alfarays.events;

import com.alfarays.mail.model.MailRequest;
import com.alfarays.mail.service.IMailService;
import com.alfarays.token.enums.DurationUnit;
import com.alfarays.token.enums.TokenType;
import com.alfarays.token.service.ITokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import static com.alfarays.mail.enums.MailSubject.ACCOUNT_ACTIVATION_REQUEST;
import static com.alfarays.mail.enums.MailTemplate.ACCOUNT_ACTIVATION;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserRegistrationEventListener {

    private final IMailService mailService;
    private final ITokenService tokenService;

    @Async
    @EventListener
    public void handleUserRegistration(UserRegistrationEvent event) {
        final String email = event.getEmail();

        var token = tokenService.create(
                email,
                TokenType.ACCOUNT_ACTIVATED,
                15,
                DurationUnit.MINUTE
        );
        log.debug("Sending OTP {} to {}", token, email);

        mailService.send(
                MailRequest
                        .builder()
                        .from("alfarays@do-not-reply.dev")
                        .to(email)
                        .name(event.getName())
                        .subject(ACCOUNT_ACTIVATION_REQUEST.content())
                        .activationCode(token)
                        .template(ACCOUNT_ACTIVATION)
                        .build()
        );
    }

}
