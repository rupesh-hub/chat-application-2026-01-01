package com.alfarays.mail.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MailSubject {

    ACCOUNT_ACTIVATION_REQUEST("Activate your account !"),
    PASSWORD_RESET_REQUEST("Password reset request !"),
    FORGET_PASSWORD_REQUEST("Forget password request !"),
    WELCOME("Welcome !"),
    PASSWORD_CHANGE_SUCCESS("Password change success !");

    private final String content;

    public String content() {
        return content;
    }

}
