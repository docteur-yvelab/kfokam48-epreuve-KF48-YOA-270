package com.kfokam48.presence.exception;

public class ExerciceInconnuException extends BusinessException {
    public ExerciceInconnuException() {
        super("EXERCICE_INCONNU", "Exercice inconnu.");
    }
}
