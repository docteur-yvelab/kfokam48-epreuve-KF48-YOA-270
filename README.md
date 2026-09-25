# KFOKAM48 - Épreuve Finale Fullstack

**Matricule :** KF48-YOA-270  
**Frontend choisi :** React 18 + Vite, parce que composants réutilisables, écosystème mature, hooks natifs pour état/effets, et Vite pour un démarrage rapide

---

## Structure du dépôt

```
/docs           CAHIER_DES_CHARGES.md · JOURNAL.md · diagrammes/
/api            contrat.yaml (OpenAPI 3.0) — 5 ops imposées + extensions
/backend        Spring Boot 3.2 (Java 17, Maven, mvnw)
/frontend       React 18 + Vite + TanStack Query + Zustand + React Router
```

---

## Démarrage rapide (3 commandes)

```bash
# 1. Backend
cd backend && ./mvnw spring-boot:run

# 2. Frontend (nouveau terminal)
cd frontend && npm install && npm run dev

# 3. Accès
# Frontend : http://localhost:5173
# API      : http://localhost:8080
# Swagger  : http://localhost:8080/swagger-ui.html
```

> **Données de démo** : Une promotion (id=1), 5 étudiants (YAO Jean, KOUAME Marie, TRAORE Pierre, DIALLO Fatou, BA Moussa), 1 session ouverte au démarrage via `V2__insert_demo_data.sql`.

---

## Contraintes techniques respectées

| Backend (B1-B6) | Frontend (F1-F3) |
|-----------------|------------------|
| Java 17, Maven, `mvnw` commité | Framework déclaré + justifié (1 ligne) |
| Contrat API `api/contrat.yaml` respecté (chemins, verbes, codes HTTP, format erreur) | 3 écrans : Formateur (session + tableau) / Étudiant (présence + dépôt) / Relecteur (note + commentaire) |
| Couches Controller / Service / Repository + DTO (aucune entité JPA en JSON) | Couche API dédiée `src/api/client.ts` (TanStack Query), pas de `fetch` dispersé |
| Validation `@Valid` + `@RestControllerAdvice` centralisé (format `{code, message}`) | États chargement/erreur gérés via `useUIStore` + TanStack Query |
| Flyway migrations versionnées (`V1__init_schema.sql`, `V2__insert_demo_data.sql`), `ddl-auto=validate` | Aucune règle métier dupliquée (moyenne, expiration, unicité côté backend) |
| 2 tests : `PresenceServiceTest` (unitaire expiration) + `PresenceControllerIT` (intégration 201/409/410) | Build passe (`npm run build` ✓) |

---

## API Endpoints

### Imposés (contrat)
| Méthode | Chemin | Description |
|---------|--------|-------------|
| POST | `/api/sessions` | Ouvrir session → `{id, code, ouvertureAt, expirationAt}` |
| POST | `/api/presences` | Marquer présence (code + etudiantId) → `{id, sessionId, etudiantId, source}` |
| POST | `/api/exercices` | Déposer exercice (sessionId, etudiantId, lien) → `{id, statut}` |
| POST | `/api/relectures/{id}` | Soumettre relecture (note 0-20, commentaire) |
| GET | `/api/tableau?promotionId=` | Tableau formateur par étudiant |

### Extensions (libres, ajoutées dans `api/contrat.yaml`)
| Méthode | Chemin | Description |
|---------|--------|-------------|
| PATCH | `/api/sessions/{id}/cloture` | Clôturer session (verrouille tout) |
| POST | `/api/presences/manuelle` | Présence manuelle formateur (source=FORMATEUR) |
| PUT | `/api/exercices/{id}` | Remplacer lien exercice (si statut DEPOSE) |
| GET | `/api/exercices?sessionId=&etudiantId=` | Lister exercices d'un étudiant |
| GET | `/api/exercices/{id}` | Détail exercice (pour relecture) |
| GET | `/api/relectures/exercice/{exerciceId}` | Relecture d'un exercice |
| GET | `/api/relectures/en-attente?etudiantId=` | Relectures assignées non rendues |
| GET | `/api/promotions/{id}/etudiants` | Liste étudiants (sélection nom) |

**Format d'erreur uniforme** : `{ "code": "CODE_EXPIRE", "message": "Le code de présence a expiré." }` pour TOUTES les erreurs (400, 403, 404, 409, 410, 429).

---

## Jalons Git (ordre imposé)

1. `[JALON] analyse` — **poussé** (commit `b3133d9`)
2. `[JALON] v0.1` — **poussé** (ce commit)
3. `[JALON] v1.0` — à venir (étape 4)

---

## Commandes utiles

```bash
# Backend tests
cd backend && ./mvnw test

# Frontend build
cd frontend && npm run build

# Frontend dev
cd frontend && npm run dev

# Vérifier contrat API
# (ouvrir api/contrat.yaml dans Swagger Editor)

# Démarrer avec données démo (H2 embedded)
cd backend && ./mvnw spring-boot:run
```

---

## Architecture frontend

```
frontend/src/
├── api/client.ts        # Couche API centralisée (axios + interceptors)
├── components/Layout.tsx    # Header/nav commun
├── pages/
│   ├── FormateurPage.tsx    # Ouvrir session + tableau récapitulatif
│   ├── EtudiantPage.tsx     # Présence + dépôt/remplacement exercice
│   └── RelecturePage.tsx    # Note + commentaire (anonyme)
├── hooks/useApi.ts      # TanStack Query hooks (useQuery/useMutation)
├── store/index.ts       # Zustand stores (auth + UI loading/errors)
└── types/index.ts       # Types TypeScript partagés (DTOs)
```

**Règles F3 respectées** :
- API calls uniquement dans `src/api/client.ts` + hooks `useApi.ts`
- États loading/error via `useUIStore` (Zustand) + TanStack Query `isLoading`/`isError`
- Moyenne affichée = `data.moyenne` (venant de l'API), **jamais recalculée côté frontend**