import { useState, useEffect } from 'react';
import { useAuthStore, useUIStore } from '../store';
import { useMarquerPresence, useDeposerExercice, useRemplacerLien, useSessions, useExercices, useEtudiants } from '../hooks/useApi';
import type { SessionResponse, ExerciceRequest, ExerciceDetail, RelectureDetail, Etudiant } from '../types';
import './EtudiantPage.css';

export default function EtudiantPage() {
  const { etudiant, setEtudiant, promotionId } = useAuthStore();
  const { loading, errors, setError, clearError } = useUIStore();

  const [selectedEtudiantId, setSelectedEtudiantId] = useState<number | null>(etudiant?.id || null);
  const [lien, setLien] = useState('');
  const [showDepotModal, setShowDepotModal] = useState(false);
  const [exerciceEnCours, setExerciceEnCours] = useState<ExerciceDetail | null>(null);
  const [showRemplacerModal, setShowRemplacerModal] = useState(false);
  const [nouveauLien, setNouveauLien] = useState('');

  const { data: sessions } = useSessions(promotionId || 1);
  const { data: exercices } = useExercices(sessions?.[0]?.id || 0, selectedEtudiantId || 0);
  const { data: etudiants } = useEtudiants(promotionId || 1);

  const sessionActuelle = sessions?.[0];
  const sessionOuverte = sessionActuelle && !sessionActuelle.cloturee;
  const sessionExpiree = sessionActuelle && new Date(sessionActuelle.expirationAt) < new Date();

  const marquerPresence = useMarquerPresence();
  const deposerExercice = useDeposerExercice();
  const remplacerLien = useRemplacerLien();

  // Auto-sélection du premier étudiant si aucun n'est choisi
  useEffect(() => {
    if (etudiants && etudiants.length > 0 && !selectedEtudiantId) {
      setSelectedEtudiantId(etudiants[0].id);
      if (!etudiant) {
        setEtudiant(etudiants[0]);
        localStorage.setItem('etudiantId', String(etudiants[0].id));
      }
    }
  }, [etudiants, selectedEtudiantId, etudiant, setEtudiant]);

  const handleMarquerPresence = async () => {
    if (!sessionActuelle || !selectedEtudiantId) return;
    clearError('marquerPresence');
    try {
      await marquerPresence.mutateAsync({ code: sessionActuelle.code, etudiantId: selectedEtudiantId });
    } catch (err) {
      // handled by hook
    }
  };

  const handleDeposerExercice = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!sessionActuelle || !selectedEtudiantId || !lien.trim()) return;
    clearError('deposerExercice');
    try {
      await deposerExercice.mutateAsync({
        sessionId: sessionActuelle.id,
        etudiantId: selectedEtudiantId,
        lien: lien.trim()
      });
      setLien('');
      setShowDepotModal(false);
    } catch (err) {
      // handled by hook
    }
  };

  const handleRemplacerLien = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!exerciceEnCours || !nouveauLien.trim()) return;
    clearError('remplacerLien');
    try {
      await remplacerLien.mutateAsync({ id: exerciceEnCours.id, lien: nouveauLien.trim() });
      setShowRemplacerModal(false);
      setNouveauLien('');
      setExerciceEnCours(null);
    } catch (err) {
      // handled by hook
    }
  };

  const monExercice = exercices?.[0];
  const peutDeposer = sessionOuverte && !monExercice;
  const peutRemplacer = monExercice && monExercice.statut === 'DEPOSE' && sessionOuverte;

  // Calculer la moyenne des 2 relectures pour l'exercice
  const calculerMoyenne = (relectures: RelectureDetail[]): { moyenne: number | null; provisoire: boolean } => {
    const notes = relectures
      .filter(r => r.note !== null && r.commentaire !== null)
      .map(r => r.note!);
    if (notes.length === 0) return { moyenne: null, provisoire: false };
    const moyenne = Math.round(notes.reduce((a, b) => a + b, 0) / notes.length);
    const provisoire = notes.length === 1;
    return { moyenne, provisoire };
  };

  const estConnecte = !!selectedEtudiantId;

  return (
    <div className="etudiant-page">
      <div className="page-header">
        <h2>Espace Étudiant</h2>
        <p>Marquez votre présence et déposez vos exercices</p>
      </div>

      {/* === ÉTAPE 1 : IDENTIFICATION (obligatoire avant toute action) === */}
      <section className="card identification-card">
        <h3>👤 Identification</h3>
        {!estConnecte ? (
          <>
            <p className="identification-hint">Sélectionnez votre nom pour accéder à votre espace :</p>
            <div className="etudiants-grid" role="listbox" aria-label="Liste des étudiants">
              {etudiants?.map((e: Etudiant) => (
                <button
                  key={e.id}
                  role="option"
                  className={`etudiant-card ${selectedEtudiantId === e.id ? 'selected' : ''}`}
                  onClick={() => {
                    setSelectedEtudiantId(e.id);
                    setEtudiant(e);
                    localStorage.setItem('etudiantId', String(e.id));
                  }}
                  aria-selected={selectedEtudiantId === e.id}
                >
                  <span className="etudiant-avatar">{e.prenom.charAt(0)}{e.nom.charAt(0)}</span>
                  <span className="etudiant-nom">{e.prenom} {e.nom}</span>
                </button>
              ))}
            </div>
            <p className="identification-note">💡 Pas de mot de passe nécessaire — choisissez simplement votre nom (Q1).</p>
          </>
        ) : (
          <div className="user-connected">
            <div className="user-avatar-large">{etudiant?.prenom.charAt(0)}{etudiant?.nom.charAt(0)}</div>
            <div className="user-info">
              <strong>{etudiant?.prenom} {etudiant?.nom}</strong>
              <span className="user-promo">Promotion {promotionId}</span>
            </div>
            <button 
              className="btn btn-outline btn-sm" 
              onClick={() => { setSelectedEtudiantId(null); setEtudiant(null); localStorage.removeItem('etudiantId'); }}
            >
              Changer d'utilisateur
            </button>
          </div>
        )}
      </section>

      {estConnecte && (
        <>
          {/* Section Présence */}
          <section className="card">
            <h3>✅ Marquer ma présence</h3>

            {sessionActuelle ? (
              <>
                <div className={`session-status ${sessionActuelle.cloturee ? 'cloturee' : (sessionExpiree ? 'expiree' : 'ouverte')}`}>
                  <span className="status-indicator" aria-hidden="true"></span>
                  <div>
                    <strong>{sessionActuelle.titre}</strong>
                    <span className="session-meta">
                      {sessionActuelle.cloturee ? 'Session clôturée' :
                        sessionExpiree ? `Code expiré depuis ${formatRelative(sessionActuelle.expirationAt)}` :
                        `Code valide jusqu'à ${formatTime(sessionActuelle.expirationAt)}`}
                    </span>
                  </div>
                  {sessionOuverte && !sessionExpiree && (
                    <code className="code-badge" title="Code à partager">{sessionActuelle.code}</code>
                  )}
                </div>

                {sessionOuverte && !sessionExpiree && (
                  <div className="action-area">
                    <button
                      className="btn btn-primary btn-large"
                      onClick={handleMarquerPresence}
                      disabled={loading.marquerPresence}
                    >
                      {loading.marquerPresence ? 'Enregistrement...' : 'Je suis présent ✅'}
                    </button>
                    <p className="hint">Le code ci-dessus est valide 15 minutes après l'ouverture de la session.</p>
                  </div>
                )}

                {sessionExpiree && !sessionActuelle.cloturee && (
                  <div className="warning">⚠️ Le code a expiré. Vous ne pouvez plus marquer votre présence pour cette session.</div>
                )}

                {sessionActuelle.cloturee && (
                  <div className="info">🔒 Cette session est clôturée. Aucune action possible.</div>
                )}
              </>
            ) : (
              <div className="empty-state">
                <p>⏳ Aucune session ouverte pour le moment.</p>
                <p className="hint">Attendez que le formateur ouvre une session.</p>
              </div>
            )}

            {errors.marquerPresence && <div className="error">{errors.marquerPresence}</div>}
          </section>

          {/* Section Dépôt d'exercice */}
          <section className="card">
            <h3>📎 Déposer mon exercice</h3>

            {sessionActuelle && !sessionActuelle.cloturee ? (
              <>
                {monExercice ? (
                  <div className="exercice-depose">
                    <div className="exercice-info">
                      <span className={`statut-badge ${monExercice.statut.toLowerCase()}`}>
                        {formatStatut(monExercice.statut)}
                      </span>
                      <div className="exercice-details">
                        <p><strong>Lien :</strong> <a href={monExercice.lien} target="_blank" rel="noopener noreferrer">{truncate(monExercice.lien, 60)}</a></p>
                        <p className="meta">Déposé le {formatDate(monExercice.dateDepot)}</p>
                        {monExercice.relectures && monExercice.relectures.length > 0 && (
                          <>
                            {monExercice.relectures.map((r: RelectureDetail) => (
                              <div key={r.ordreRelecteur} className="relecture-result">
                                <p className={`meta relecture-result ${r.noteProvisoire ? 'provisoire' : ''}`}>
                                  <strong>Relecture {r.ordreRelecteur}/2 :</strong> 
                                  {r.note !== null ? `${r.note}/20 — {r.commentaire}` : 'En attente'}
                                  {r.noteProvisoire && <span className="provisoire-badge"> (provisoire)</span>}
                                </p>
                              </div>
                            ))}
                            {(() => {
                              const { moyenne, provisoire } = calculerMoyenne(monExercice.relectures);
                              return moyenne !== null && (
                                <p className={`meta moyenne-display ${provisoire ? 'provisoire' : ''}`}>
                                  <strong>Moyenne : {moyenne}/20</strong>
                                  {provisoire && <span className="provisoire-badge"> (provisoire)</span>}
                                </p>
                              );
                            })()}
                          </>
                        )}
                      </div>
                    </div>
                    {peutRemplacer && (
                      <button
                        className="btn btn-secondary"
                        onClick={() => { setExerciceEnCours(monExercice); setShowRemplacerModal(true); }}
                      >
                        ✏️ Remplacer le lien
                      </button>
                    )}
                  </div>
                ) : (
                  <>
                    <p>Déposez le lien vers votre exercice (GitHub, Drive, etc.)</p>
                    <form onSubmit={handleDeposerExercice} className="form-depot">
                      <input
                        type="url"
                        placeholder="https://github.com/mon-repo / https://drive.google.com/..."
                        value={lien}
                        onChange={(e) => setLien(e.target.value)}
                        className="input input-large"
                        required
                        aria-label="Lien vers l'exercice"
                      />
                      <button
                        type="submit"
                        className="btn btn-primary"
                        disabled={loading.deposerExercice || !lien.trim()}
                      >
                        {loading.deposerExercice ? 'Dépôt...' : '📤 Déposer l\'exercice'}
                      </button>
                    </form>
                    <p className="hint">Possible jusqu'à la clôture de la session par le formateur.</p>
                  </>
                )}
              </>
            ) : sessionActuelle?.cloturee ? (
              <div className="info">🔒 Session clôturée. Plus de dépôt possible.</div>
            ) : (
              <div className="empty-state">
                <p>⏳ Aucune session ouverte.</p>
                <p className="hint">Le dépôt sera possible quand le formateur ouvrira une session.</p>
              </div>
            )}

            {errors.deposerExercice && <div className="error">{errors.deposerExercice}</div>}
            {errors.remplacerLien && <div className="error">{errors.remplacerLien}</div>}
          </section>
        </>
      )}

      {/* Modal Remplacer lien */}
      {showRemplacerModal && exerciceEnCours && (
        <div className="modal-overlay" onClick={() => { setShowRemplacerModal(false); setExerciceEnCours(null); }}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h3>✏️ Remplacer le lien</h3>
            <p>Exercice actuel : <code>{truncate(exerciceEnCours.lien, 50)}</code></p>
            <form onSubmit={handleRemplacerLien}>
              <input
                type="url"
                placeholder="Nouveau lien..."
                value={nouveauLien}
                onChange={(e) => setNouveauLien(e.target.value)}
                className="input"
                style={{ width: '100%', marginBottom: 16 }}
                required
                autoFocus
              />
              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => { setShowRemplacerModal(false); setExerciceEnCours(null); }}>Annuler</button>
                <button type="submit" className="btn btn-primary" disabled={loading.remplacerLien || !nouveauLien.trim()}>
                  {loading.remplacerLien ? 'Remplacement...' : 'Remplacer'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal Dépôt (optionnel) */}
      {showDepotModal && (
        <div className="modal-overlay" onClick={() => setShowDepotModal(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h3>📤 Déposer un exercice</h3>
            <form onSubmit={handleDeposerExercice}>
              <input
                type="url"
                placeholder="https://..."
                value={lien}
                onChange={(e) => setLien(e.target.value)}
                className="input"
                style={{ width: '100%', marginBottom: 16 }}
                required
                autoFocus
              />
              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => setShowDepotModal(false)}>Annuler</button>
                <button type="submit" className="btn btn-primary" disabled={loading.deposerExercice || !lien.trim()}>
                  {loading.deposerExercice ? 'Dépôt...' : 'Déposer'}
                </button>
              </div>
            </form>
          </div>
        </div>
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

function formatTime(dateStr: string): string {
  return new Date(dateStr).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
}

function formatRelative(dateStr: string): string {
  const diff = Date.now() - new Date(dateStr).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 60) return `${mins} min`;
  const hours = Math.floor(mins / 60);
  return `${hours}h${mins % 60}min`;
}

function truncate(str: string, len: number): string {
  return str.length > len ? str.slice(0, len) + '…' : str;
}

function calculerMoyenne(relectures: RelectureDetail[]): { moyenne: number | null; provisoire: boolean } {
  const notes = relectures
    .filter(r => r.note !== null && r.commentaire !== null)
    .map(r => r.note!);
  if (notes.length === 0) return { moyenne: null, provisoire: false };
  const moyenne = Math.round(notes.reduce((a, b) => a + b, 0) / notes.length);
  const provisoire = notes.length === 1;
  return { moyenne, provisoire };
}