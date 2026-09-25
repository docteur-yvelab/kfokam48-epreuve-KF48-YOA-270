package com.kfokam48.presence.exception;

public class SessionClotureeException extends BusinessException {
    public SessionClotureeException() {
        super("SESSION_CLOTUREE", "La session est clôturée, aucune modification n'est possible.");
    }
}