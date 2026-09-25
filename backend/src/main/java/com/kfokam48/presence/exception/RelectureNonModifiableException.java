package com.kfokam48.presence.exception;

public class RelectureNonModifiableException extends BusinessException {
    public RelectureNonModifiableException() {
        super("RELECTURE_NON_MODIFIABLE", "La relecture ne peut plus être modifiée (session clôturée).");
    }
}