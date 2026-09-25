package com.kfokam48.presence.exception;

public class SessionDejaClotureeException extends BusinessException {
    public SessionDejaClotureeException() {
        super("SESSION_DEJA_CLOTUREE", "La session est déjà clôturée.");
    }
}
