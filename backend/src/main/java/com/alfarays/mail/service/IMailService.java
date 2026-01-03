package com.alfarays.mail.service;


import com.alfarays.mail.model.MailRequest;

public interface IMailService {
    void send(MailRequest request);
}

