package com.kfokam48.presence.exception;

public class DejaPresentException extends BusinessException {
    public DejaPresentException() {
        super("DEJA_PRESENT", "Présence déjà enregistrée pour cette session.");
    }
}