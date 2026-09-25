export interface SessionResponse {
  id: number;
  code: string;
  ouvertureAt: string;
  expirationAt: string;
}

export interface SessionRequest {
  titre: string;
  promotionId: number;
}

export interface PresenceRequest {
  code: string;
  etudiantId: number;
}

export interface PresenceResponse {
  id: number;
  sessionId: number;
  etudiantId: number;
  source: 'ETUDIANT' | 'FORMATEUR';
}

export interface PresenceManuelleRequest {
  sessionId: number;
  etudiantId: number;
}

export interface ExerciceRequest {
  sessionId: number;
  etudiantId: number;
  lien: string;
}

export interface ExerciceResponse {
  id: number;
  statut: 'DEPOSE' | 'EN_ATTENTE_RELECTURE' | 'RELU';
}

export interface RelectureDetail {
  id: number;
  ordreRelecteur: 1 | 2;
  relecteurId: number;
  note: number | null;
  commentaire: string | null;
  dateSoumission: string | null;
  noteProvisoire: boolean;
}

export interface ExerciceDetail {
  id: number;
  sessionId: number;
  etudiantId: number;
  lien: string;
  statut: 'DEPOSE' | 'EN_ATTENTE_RELECTURE' | 'RELU';
  dateDepot: string;
  relectures: RelectureDetail[];
}

export interface RelectureRequest {
  note: number;
  commentaire: string;
  ordreRelecteur: 1 | 2;
}

export interface RelectureResponse {
  id: number;
  exerciceId: number;
  ordreRelecteur: 1 | 2;
  relecteurId: number;
  note: number | null;
  commentaire: string | null;
  dateSoumission: string | null;
  dateModification?: string;
  noteProvisoire: boolean;
}

export interface RelectureEnAttente {
  id: number;
  exerciceId: number;
  exerciceLien: string;
  ordreRelecteur: 1 | 2;
  dateAssignation: string;
}

export interface Etudiant {
  id: number;
  nom: string;
  prenom: string;
}

export interface TableauEtudiantResponse {
  etudiantId: number;
  nom: string;
  presences: number;
  exercicesDeposes: number;
  moyenne: number | null;
  moyenneProvisoire: boolean;
  relecturesEnAttente: number;
}

export interface ErrorResponse {
  code: string;
  message: string;
}

export interface ApiError extends Error {
  response?: {
    data: ErrorResponse;
    status: number;
  };
}