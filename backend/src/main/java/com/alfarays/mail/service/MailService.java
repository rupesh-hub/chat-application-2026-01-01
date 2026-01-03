package com.alfarays.mail.service;


import com.alfarays.exceptions.AuthorizationException;
import com.alfarays.mail.model.MailRequest;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.MessagingException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.mail.javamail.MimeMessageHelper.MULTIPART_MODE_MIXED;


@Service
@RequiredArgsConstructor
@Slf4j
public class MailService implements IMailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;


    @Async
    @Override
    public void send(MailRequest request) {

        try {
            log.debug("Preparing to send email to: {}", request.getTo());
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, MULTIPART_MODE_MIXED, UTF_8.name());
            Map<String, Object> properties = new HashMap<>();
            properties.put("name", request.getName());
            properties.put("confirmationUrl", request.getConfirmationUrl());
            properties.put("activation_code", request.getActivationCode());
            String template = request.getTemplate().getName();
            Context context = new Context();
            context.setVariables(properties);
            helper.setFrom(request.getFrom());
            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());
            helper.setText(templateEngine.process(template, context), true);
            mailSender.send(mime);
            log.info("Email sent successfully to: {}", request.getTo());
        } catch(MessagingException e) {
            log.error("Failed to send email to: {}", request.getTo(), e);
            throw new AuthorizationException("Failed to send email", e);
        } catch(Exception e) {
            log.error("Unexpected error when sending email to: {}", request.getTo(), e);
            throw new AuthorizationException("Unexpected error when sending email", e);
        }

    }
}
