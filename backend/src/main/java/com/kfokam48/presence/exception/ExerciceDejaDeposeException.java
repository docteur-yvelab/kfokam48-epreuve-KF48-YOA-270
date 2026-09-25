package com.kfokam48.presence.exception;

public class ExerciceDejaDeposeException extends BusinessException {
    public ExerciceDejaDeposeException() {
        super("EXERCICE_DEJA_DEPOSE", "Un exercice a déjà été déposé pour cette session.");
    }
}