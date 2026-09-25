package com.kfokam48.presence.exception;

public class RelectureDejaRendueException extends BusinessException {
    public RelectureDejaRendueException() {
        super("RELECTURE_DEJA_RENDUE", "La relecture a déjà été rendue pour cet exercice.");
    }
}