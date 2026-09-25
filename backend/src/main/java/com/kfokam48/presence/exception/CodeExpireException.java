package com.kfokam48.presence.exception;

public class CodeExpireException extends BusinessException {
    public CodeExpireException() {
        super("CODE_EXPIRE", "Le code de présence a expiré.");
    }
}