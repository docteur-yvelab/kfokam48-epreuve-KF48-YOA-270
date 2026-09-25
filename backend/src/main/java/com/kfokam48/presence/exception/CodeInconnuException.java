package com.kfokam48.presence.exception;

public class CodeInconnuException extends BusinessException {
    public CodeInconnuException() {
        super("CODE_INCONNU", "Code de présence inconnu.");
    }
}