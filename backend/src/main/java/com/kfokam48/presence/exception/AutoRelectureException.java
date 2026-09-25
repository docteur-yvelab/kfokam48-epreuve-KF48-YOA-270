package com.kfokam48.presence.exception;

public class AutoRelectureException extends BusinessException {
    public AutoRelectureException() {
        super("AUTO_RELECTURE", "Un étudiant ne peut pas relire son propre exercice.");
    }
}