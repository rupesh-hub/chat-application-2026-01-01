package com.alfarays.events;

import com.alfarays.mail.model.MailRequest;
import com.alfarays.mail.service.IMailService;
import com.alfarays.token.service.ITokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import static com.alfarays.mail.enums.MailSubject.FORGET_PASSWORD_REQUEST;
import static com.alfarays.mail.enums.MailTemplate.FORGOT_PASSWORD_REQUEST;
import static com.alfarays.token.enums.DurationUnit.MINUTE;
import static com.alfarays.token.enums.TokenType.FORGOT_PASSWORD;

@Component
@Slf4j
@RequiredArgsConstructor
public class ForgotPasswordRequestEventListener {

    private final IMailService mailService;
    private final ITokenService tokenService;

    @Value("${application.email.reset_password_url}")
    private String resetPasswordURL;

    @Async
    @EventListener
    public void handleForgotPasswordRequest(ForgotPasswordRequestEvent event) {
        final String email = event.getEmail();

        var token = tokenService.create(
                email,
                FORGOT_PASSWORD,
                15,
                MINUTE
        );
        log.debug("Sending OTP {} to {}", token, email);
        var finalURL = String.format("%s?token=%s&email=%s", this.resetPasswordURL, token, email);
        mailService.send(
                MailRequest
                        .builder()
                        .from("alfarays@do-not-reply.dev")
                        .to(email)
                        .name(event.getName())
                        .subject(FORGET_PASSWORD_REQUEST.content())
                        .confirmationUrl(finalURL)
                        .template(FORGOT_PASSWORD_REQUEST)
                        .build()
        );
    }
}
