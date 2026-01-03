package com.alfarays.mail.model;

import com.alfarays.mail.enums.MailTemplate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MailRequest {

    private String from;
    private String to;
    private String subject;
    private MailTemplate template;
    private String name;
    private String username;
    private String confirmationUrl;
    private String activationCode;
}
