# D3 — Séquence : Marquer sa présence

```mermaid
sequenceDiagram
    autonumber
    actor E as Étudiant
    participant F as Frontend (React)
    participant API as PresenceController
    participant S as PresenceService
    participant R as PresenceRepository
    participant DB as Base de données

    Note over E,DB: CAS NOMINAL - Code valide, non expiré, première présence

    E->>F: Saisit code + choisit son nom
    F->>API: POST /api/presences {code, etudiantId}
    API->>S: enregistrerPresence(code, etudiantId)
    S->>R: findByCode(code)
    R-->>S: Session (id=1, expirationAt=futur)
    S->>S: Vérifier expiration (now < expirationAt) ✓
    S->>R: existsBySessionIdAndEtudiantId(sessionId, etudiantId)
    R-->>S: false (pas encore présent)
    S->>R: save(Presence{session, etudiant, source=ETUDIANT})
    R-->>S: Presence{id=42, sessionId=1, etudiantId=3, source=ETUDIANT}
    S-->>API: PresenceDTO
    API-->>F: 201 Created {id, sessionId, etudiantId, source:"ETUDIANT"}
    F-->>E: Affichage "Présence enregistrée ✓"

    Note over E,DB: CAS D'ERREUR 1 - Code expiré (RG1)

    E->>F: Saisit code expiré
    F->>API: POST /api/presences {code, etudiantId}
    API->>S: enregistrerPresence(code, etudiantId)
    S->>R: findByCode(code)
    R-->>S: Session (expirationAt=past)
    S->>S: Vérifier expiration (now >= expirationAt) ✗
    S-->>API: CodeExpireException
    API-->>F: 410 Gone {code:"CODE_EXPIRE", message:"Le code de présence a expiré."}
    F-->>E: Affichage erreur "Code expiré"

    Note over E,DB: CAS D'ERREUR 2 - Déjà présent (RG6)

    E->>F: Saisit code valide (déjà utilisé)
    F->>API: POST /api/presences {code, etudiantId}
    API->>S: enregistrerPresence(code, etudiantId)
    S->>R: findByCode(code)
    R-->>S: Session (expirationAt=futur)
    S->>S: Vérifier expiration ✓
    S->>R: existsBySessionIdAndEtudiantId(sessionId, etudiantId)
    R-->>S: true (déjà présent)
    S-->>API: DejaPresentException
    API-->>F: 409 Conflict {code:"DEJA_PRESENT", message:"L'étudiant est déjà présent à cette session."}
    F-->>E: Affichage erreur "Déjà présent"
```

**Codes HTTP conformes au contrat imposé (`api/contrat.yaml`) :**
| Cas | Code HTTP | Code erreur | Message |
|-----|-----------|-------------|---------|
| Nominal | 201 | — | — |
| Code inconnu | 400 | `CODE_INCONNU` | "Code de présence inconnu." |
| **Code expiré (RG1)** | **410** | **`CODE_EXPIRE`** | **"Le code de présence a expiré."** |
| **Déjà présent (RG6)** | **409** | **`DEJA_PRESENT`** | **"L'étudiant est déjà présent à cette session."** |

**Règles de gestion vérifiées dans cette séquence :**
- RG1 : Vérification `now < expirationAt` → 410 si faux
- RG6 : Vérification unicité `(session, etudiant)` → 409 si true
- RG11 : `source = ETUDIANT` (vs FORMATEUR pour présence manuelle)
- Format d'erreur imposé : `{ "code": "CODE", "message": "..." }` (B4)

**Endpoints concernés :** `POST /api/presences` (imposé) + `POST /api/presences/manuelle` (extension pour RG11)