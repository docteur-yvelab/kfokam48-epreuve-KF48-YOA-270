package com.kfokam48.presence.exception;

public class AucuneRelectureException extends BusinessException {
    public AucuneRelectureException() {
        super("AUCUNE_RELECTURE", "Aucune relecture trouvée pour cet exercice.");
    }
}