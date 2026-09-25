package com.kfokam48.presence.exception;

public class EtudiantInconnuException extends BusinessException {
    public EtudiantInconnuException() {
        super("ETUDIANT_INCONNU", "Étudiant inconnu.");
    }
}
