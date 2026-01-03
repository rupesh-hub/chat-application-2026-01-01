package com.alfarays.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserRegistrationEvent extends ApplicationEvent {

    private final String email;
    private final String name;

    public UserRegistrationEvent(Object source, String email, String name) {
        super(source);
        this.email = email;
        this.name = name;
    }

}
