package com.kfokam48.presence.exception;

public class LienInvalideException extends BusinessException {
    public LienInvalideException() {
        super("LIEN_INVALIDE", "Le lien fourni n'est pas une URI valide.");
    }
}