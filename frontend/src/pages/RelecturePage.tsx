import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuthStore, useUIStore } from '../store';
import { useRelectureEnAttente, useSoumettreRelecture, useExercices, useEtudiants } from '../hooks/useApi';
import type { RelectureEnAttente, ExerciceDetail, RelectureDetail, Etudiant } from '../types';
import './RelecturePage.css';

export default function RelecturePage() {
  const { exerciceId, ordre } = useParams<{ exerciceId: string; ordre: string }>();
  const navigate = useNavigate();
  const { etudiant, setEtudiant, promotionId } = useAuthStore();
  const { loading, errors, setError, clearError } = useUIStore();

  const exerciceIdNum = exerciceId ? parseInt(exerciceId, 10) : null;
  const ordreRelecteur = ordre ? parseInt(ordre, 10) : null;
  const [selectedEtudiantId, setSelectedEtudiantId] = useState<number | null>(etudiant?.id || null);
  const [note, setNote] = useState<number | ''>('');
  const [commentaire, setCommentaire] = useState('');
  const [exercice, setExercice] = useState<ExerciceDetail | null>(null);
  const [showEtudiantPicker, setShowEtudiantPicker] = useState(!etudiant);
  const [maRelecture, setMaRelecture] = useState<RelectureDetail | null>(null);

  const { data: relecturesEnAttente } = useRelectureEnAttente(selectedEtudiantId || 0);
  const { data: etudiants } = useEtudiants(promotionId || 1);

  const soumettreRelecture = useSoumettreRelecture();

  // Auto-sélection étudiant
  useEffect(() => {
    if (etudiants && etudiants.length > 0 && !selectedEtudiantId && !showEtudiantPicker) {
      setSelectedEtudiantId(etudiants[0].id);
      setEtudiant(etudiants[0]);
      localStorage.setItem('etudiantId', String(etudiants[0].id));
    }
  }, [etudiants, selectedEtudiantId, showEtudiantPicker, setEtudiant]);

  // Charger l'exercice et trouver MA relecture (ordre 1 ou 2)
  useEffect(() => {
    if (exerciceIdNum && ordreRelecteur) {
      fetch(`${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/api/exercices/${exerciceIdNum}`)
        .then(res => res.ok ? res.json() : null)
        .then(data => {
          if (data) {
            setExercice(data);
            // Trouver MA relecture (celle avec mon ordreRelecteur)
            const maRelecture = data.relectures?.find((r: RelectureDetail) => r.ordreRelecteur === ordreRelecteur);
            setMaRelecture(maRelecture || null);
          }
        })
        .catch(console.error);
    }
  }, [exerciceIdNum, ordreRelecteur]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!exerciceIdNum || !ordreRelecteur || note === '' || !commentaire.trim()) return;
    clearError('soumettreRelecture');
    try {
      await soumettreRelecture.mutateAsync({ 
        exerciceId: exerciceIdNum, 
        ordreRelecteur, 
        data: { note: Number(note), commentaire: commentaire.trim(), ordreRelecteur } 
      });
      navigate('/etudiant');
    } catch (err) {
      // handled by hook
    }
  };

  if (showEtudiantPicker && etudiants && etudiants.length > 0) {
    return (
      <div className="relecture-page picker-mode">
        <div className="page-header">
          <h2>Relecture d'exercice</h2>
          <p>Sélectionnez votre profil pour continuer</p>
        </div>
        <div className="card">
          <h3>Qui êtes-vous ?</h3>
          <div className="etudiants-grid">
            {etudiants.map((e: Etudiant) => (
              <button
                key={e.id}
                className={`etudiant-card ${selectedEtudiantId === e.id ? 'selected' : ''}`}
                onClick={() => {
                  setSelectedEtudiantId(e.id);
                  setEtudiant(e);
                  localStorage.setItem('etudiantId', String(e.id));
                  setShowEtudiantPicker(false);
                }}
              >
                <span className="etudiant-nom">{e.prenom} {e.nom}</span>
              </button>
            ))}
          </div>
        </div>
      </div>
    );
  }

  if (!exerciceIdNum || !ordreRelecteur) {
    return (
      <div className="relecture-page">
        <div className="page-header">
          <h2>Relecture d'exercice</h2>
        </div>
        <div className="card empty-state">
          <div className="empty-icon">📝</div>
          <h3>Paramètres invalides</h3>
          <p>URL de relecture invalide.</p>
          <button className="btn btn-primary" onClick={() => navigate('/etudiant')}>
            Retour à l'espace étudiant
          </button>
        </div>
      </div>
    );
  }

  if (!maRelecture) {
    return (
      <div className="relecture-page">
        <div className="page-header">
          <h2>Relecture d'exercice</h2>
        </div>
        <div className="card empty-state">
          <div className="empty-icon">📝</div>
          <h3>Relecture non trouvée</h3>
          <p>Cette relecture n'existe pas ou vous n'êtes pas assigné à cet ordre.</p>
          <button className="btn btn-primary" onClick={() => navigate('/etudiant')}>
            Retour à l'espace étudiant
          </button>
        </div>
      </div>
    );
  }

  const dejaSoumis = maRelecture.note !== null && maRelecture.commentaire !== null;

  return (
    <div className="relecture-page">
      <div className="page-header">
        <h2>Relecture d'exercice <span className="ordre-badge">{ordreRelecteur}/2</span></h2>
        <p>Évaluez le travail de votre pair de manière constructive</p>
      </div>

      {/* Info exercice */}
      {exercice && (
        <section className="card exercice-preview">
          <h3>Exercice à relire</h3>
          <div className="exercice-link">
            <a href={exercice.lien} target="_blank" rel="noopener noreferrer">
              🔗 {truncate(exercice.lien, 80)}
            </a>
          </div>
          <p className="exercice-meta">Déposé le {formatDate(exercice.dateDepot)}</p>
          <p className="exercice-meta">Statut : {formatStatut(exercice.statut)}</p>
        </section>
      )}

      {/* Formulaire relecture */}
      <section className="card">
        <h3>Votre évaluation (Relecture {ordreRelecteur}/2)</h3>

        {dejaSoumis && (
          <div className="already-submitted">
            <h4>✅ Relecture déjà soumise</h4>
            <p><strong>Note :</strong> {maRelecture.note}/20</p>
            <p><strong>Commentaire :</strong> {maRelecture.commentaire}</p>
            <p><strong>Soumis le :</strong> {maRelecture.dateSoumission ? formatDate(maRelecture.dateSoumission) : '—'}</p>
            {maRelecture.noteProvisoire && (
              <p className="provisoire-badge">⚠️ Note provisoire (en attente de l'autre relecteur)</p>
            )}
            <p className="hint">Vous pouvez modifier votre note tant que la session n'est pas clôturée.</p>
          </div>
        )}

        {!dejaSoumis && (
          <form onSubmit={handleSubmit} className="relecture-form">
            <div className="form-group">
              <label htmlFor="note">Note <span className="required">*</span></label>
              <div className="note-input-wrapper">
                <input
                  type="number"
                  id="note"
                  min="0"
                  max="20"
                  value={note}
                  onChange={(e) => setNote(e.target.value === '' ? '' : Math.min(20, Math.max(0, parseInt(e.target.value, 10))))}
                  className="input input-note"
                  required
                  placeholder="/20"
                  disabled={loading.soumettreRelecture}
                />
                <span className="note-suffix">/ 20</span>
              </div>
              <p className="hint">Entier entre 0 et 20 (RG3)</p>
            </div>

            <div className="form-group">
              <label htmlFor="commentaire">Commentaire <span className="required">*</span></label>
              <textarea
                id="commentaire"
                value={commentaire}
                onChange={(e) => setCommentaire(e.target.value)}
                className="input textarea"
                rows={5}
                placeholder="Votre feedback constructif pour l'étudiant..."
                required
                disabled={loading.soumettreRelecture}
              />
              <p className="hint">Ce commentaire sera visible par l'étudiant (anonymement).</p>
            </div>

            <div className="form-actions">
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => navigate('/etudiant')}
                disabled={loading.soumettreRelecture}
              >
                Annuler
              </button>
              <button
                type="submit"
                className="btn btn-primary"
                disabled={loading.soumettreRelecture || note === '' || !commentaire.trim()}
              >
                {loading.soumettreRelecture ? 'Envoi...' : 'Soumettre la relecture'}
              </button>
            </div>
          </form>
        )}

        {errors.soumettreRelecture && <div className="error">{errors.soumettreRelecture}</div>}

        <div className="rules-reminder">
          <h4>Rappel des règles :</h4>
          <ul>
            <li>Vous ne pouvez pas relire votre propre exercice (RG2)</li>
            <li>La note doit être un entier entre 0 et 20 (RG3)</li>
            <li>Deux relecteurs distincts par exercice (RG4)</li>
            <li>Vous pourrez modifier votre note tant que la session n'est pas clôturée (RG13)</li>
            <li>Votre identité restera anonyme pour l'étudiant relu (RG15)</li>
          </ul>
        </div>
      </section>

      {/* Mes autres relectures en attente */}
      {relecturesEnAttente && relecturesEnAttente.length > 1 && (
        <section className="card">
          <h3>Mes autres relectures en attente</h3>
          <ul className="relectures-list">
            {relecturesEnAttente
              .filter((r: RelectureEnAttente) => r.id !== maRelecture?.id)
              .map((r: RelectureEnAttente) => (
                <li key={r.id} className="relecture-item">
                  <a href={`/relecture/${r.exerciceId}/${r.ordreRelecteur}`} className="relecture-link">
                    <span className="relecture-exercice">Exercice #{r.exerciceId} — Relecture {r.ordreRelecteur}/2</span>
                    <span className="relecture-date">Assigné le {formatDate(r.dateAssignation)}</span>
                  </a>
                </li>
              ))}
          </ul>
        </section>
      )}
    </div>
  );
}

function formatStatut(statut: string): string {
  switch (statut) {
    case 'DEPOSE': return 'Déposé';
    case 'EN_ATTENTE_RELECTURE': return 'En attente de relecture';
    case 'RELU': return 'Relu';
    default: return statut;
  }
}

function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}

function truncate(str: string, len: number): string {
  return str.length > len ? str.slice(0, len) + '…' : str;
}