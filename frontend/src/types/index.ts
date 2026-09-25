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

export interface ExerciceDetail {
  id: number;
  sessionId: number;
  etudiantId: number;
  lien: string;
  statut: 'DEPOSE' | 'EN_ATTENTE_RELECTURE' | 'RELU';
  dateDepot: string;
  relecture?: {
    id: number;
    relecteurId: number;
    note: number;
    commentaire: string;
    dateSoumission: string;
  };
}

export interface RelectureRequest {
  note: number;
  commentaire: string;
}

export interface RelectureResponse {
  id: number;
  exerciceId: number;
  relecteurId: number;
  note: number;
  commentaire: string;
  dateSoumission: string;
  dateModification?: string;
}

export interface RelectureEnAttente {
  id: number;
  exerciceId: number;
  exerciceLien: string;
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