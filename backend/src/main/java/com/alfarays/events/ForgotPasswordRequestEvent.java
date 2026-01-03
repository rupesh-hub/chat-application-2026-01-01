package com.alfarays.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ForgotPasswordRequestEvent extends ApplicationEvent {

    private final String name;
    private final String email;

    public ForgotPasswordRequestEvent(Object source, String name, String email) {
        super(source);
        this.name = name;
        this.email = email;
    }

}
