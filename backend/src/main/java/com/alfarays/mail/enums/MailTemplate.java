package com.alfarays.mail.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum MailTemplate {

    WELCOME("welcome"),
    ACCOUNT_ACTIVATION("account-activation"),
    RESET_PASSWORD("reset-password"),
    FORGOT_PASSWORD_REQUEST("forgot-password-request");

    private final String name;

}
