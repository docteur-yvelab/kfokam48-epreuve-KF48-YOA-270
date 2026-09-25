package com.kfokam48.presence.exception;

public class SessionInconnueException extends BusinessException {
    public SessionInconnueException() {
        super("SESSION_INCONNUE", "Session inconnue.");
    }
}
