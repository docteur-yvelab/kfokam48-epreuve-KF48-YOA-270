package com.kfokam48.presence.exception;

public class RelectureDejaCommenceeException extends BusinessException {
    public RelectureDejaCommenceeException() {
        super("RELECTURE_DEJA_COMMENCEE", "Le lien ne peut plus être remplacé : une relecture a déjà commencé.");
    }
}
