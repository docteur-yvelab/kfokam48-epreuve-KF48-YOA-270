package com.kfokam48.presence.exception;

public class NoteInvalideException extends BusinessException {
    public NoteInvalideException() {
        super("NOTE_INVALIDE", "La note doit être un entier compris entre 0 et 20.");
    }
}