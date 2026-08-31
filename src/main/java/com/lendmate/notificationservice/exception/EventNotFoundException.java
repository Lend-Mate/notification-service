package com.lendmate.notificationservice.exception;

public class EventNotFoundException extends RuntimeException {

    public EventNotFoundException() {}

    public EventNotFoundException(String msg) {
        super(msg);
    }
}
