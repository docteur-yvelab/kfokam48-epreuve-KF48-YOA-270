package com.kfokam48.presence.exception;

public class AucunEtudiantPresentException extends BusinessException {
    public AucunEtudiantPresentException() {
        super("AUCUN_ETUDIANT_PRESENT", "Aucun autre étudiant présent pour assigner la relecture.");
    }
}