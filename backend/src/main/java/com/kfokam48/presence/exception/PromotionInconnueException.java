package com.kfokam48.presence.exception;

public class PromotionInconnueException extends BusinessException {
    public PromotionInconnueException() {
        super("PROMOTION_INCONNUE", "Promotion inconnue.");
    }
}