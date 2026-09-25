import { useState } from 'react';
import { useAuthStore, useUIStore } from '../store';
import { useOuvrirSession, useCloturerSession, useTableau, useEtudiants, usePresenceManuelle, useSessions } from '../hooks/useApi';
import type { SessionRequest, TableauEtudiantResponse, Etudiant } from '../types';
import './FormateurPage.css';

export default function FormateurPage() {
  const { promotionId } = useAuthStore();
  const { loading, errors, setError, clearError } = useUIStore();

  const [showOuvrirModal, setShowOuvrirModal] = useState(false);
  const [titre, setTitre] = useState('');
  const [sessionCode, setSessionCode] = useState<string | null>(null);
  const [sessionId, setSessionId] = useState<number | null>(null);
  const [showPresenceManuelle, setShowPresenceManuelle] = useState(false);
  const [etudiantIdPresence, setEtudiantIdPresence] = useState<number | null>(null);

  const { data: sessions, isLoading: loadingSessions } = useSessions(promotionId || 1);
  const { data: tableau, isLoading: loadingTableau } = useTableau(promotionId || 1);
  const { data: etudiants } = useEtudiants(promotionId || 1);

  const ouvrirSession = useOuvrirSession();
  const cloturerSession = useCloturerSession();
  const presenceManuelle = usePresenceManuelle();

  const handleOuvrirSession = async (e: React.FormEvent) => {
    e.preventDefault();
    clearError('ouvrirSession');
    try {
      const res = await ouvrirSession.mutateAsync({ titre, promotionId: promotionId || 1 });
      setSessionCode(res.code);
      setSessionId(res.id);
      setShowOuvrirModal(false);
      setTitre('');
    } catch (err) {
      // error handled by hook
    }
  };

  const handleCloturer = async (id: number) => {
    if (!confirm('Clôturer cette session ? Cette action est irréversible.')) return;
    clearError('cloturerSession');
    try {
      await cloturerSession.mutateAsync(id);
    } catch (err) {
      // error handled by hook
    }
  };

  const handlePresenceManuelle = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!etudiantIdPresence) return;
    clearError('presenceManuelle');
    try {
      await presenceManuelle.mutateAsync({ sessionId: sessionId!, etudiantId: etudiantIdPresence });
      setShowPresenceManuelle(false);
      setEtudiantIdPresence(null);
    } catch (err) {
      // error handled by hook
    }
  };

  const sessionActuelle = sessions?.[0];

  return (
    <div className="formateur-page">
      <div className="page-header">
        <h2>Espace Formateur</h2>
        <p>Gérez vos sessions et suivez les présences</p>
      </div>

      {/* Section Ouvrir Session */}
      <section className="card">
        <h3>Ouvrir une nouvelle session</h3>
        {sessionActuelle && !sessionActuelle.cloturee && (
          <div className="session-active">
            <p><strong>Session en cours :</strong> {sessionActuelle.titre}</p>
            <p><strong>Code de présence :</strong> <code>{sessionActuelle.code}</code></p>
            <p><strong>Expire à :</strong> {new Date(sessionActuelle.expirationAt).toLocaleString()}</p>
            <button
              className="btn btn-danger"
              onClick={() => handleCloturer(sessionActuelle.id)}
              disabled={loading.cloturerSession}
            >
              {loading.cloturerSession ? 'Clôture...' : 'Clôturer la session'}
            </button>
          </div>
        )}
        {(!sessionActuelle || sessionActuelle.cloturee) && (
          <div className="form-inline">
            <input
              type="text"
              placeholder="Titre de la session (ex: Java Spring Boot)"
              value={titre}
              onChange={(e) => setTitre(e.target.value)}
              className="input"
              style={{ flex: 1, maxWidth: 400 }}
            />
            <button
              className="btn btn-primary"
              onClick={() => setShowOuvrirModal(true)}
              disabled={loading.ouvrirSession || !titre.trim()}
            >
              {loading.ouvrirSession ? 'Ouverture...' : 'Ouvrir la session'}
            </button>
          </div>
        )}
        {errors.ouvrirSession && <div className="error">{errors.ouvrirSession}</div>}
      </section>

      {/* Modal Code de session */}
      {sessionCode && sessionId && (
        <div className="modal-overlay" onClick={() => { setSessionCode(null); setSessionId(null); }}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h3>Session ouverte !</h3>
            <p>Partagez ce code aux étudiants :</p>
            <div className="code-display">{sessionCode}</div>
            <p className="code-note">Valide jusqu'à <strong>{sessions?.[0]?.expirationAt ? new Date(sessions[0].expirationAt).toLocaleTimeString() : ''}</strong> (15 min)</p>
            <button className="btn btn-primary" onClick={() => { setSessionCode(null); setSessionId(null); }}>Fermer</button>
          </div>
        </div>
      )}

      {/* Section Présence manuelle */}
      <section className="card">
        <h3>Ajouter une présence manuelle</h3>
        {sessionActuelle && !sessionActuelle.cloturee && (
          <form onSubmit={handlePresenceManuelle} className="form-inline">
            <select
              value={etudiantIdPresence || ''}
              onChange={(e) => setEtudiantIdPresence(Number(e.target.value))}
              className="input"
              style={{ maxWidth: 300 }}
            >
              <option value="">Choisir un étudiant...</option>
              {etudiants?.map((e: Etudiant) => (
                <option key={e.id} value={e.id}>{e.nom} {e.prenom}</option>
              ))}
            </select>
            <button
              type="submit"
              className="btn btn-secondary"
              disabled={loading.presenceManuelle || !etudiantIdPresence}
            >
              {loading.presenceManuelle ? 'Ajout...' : 'Ajouter présence'}
            </button>
          </form>
        )}
        {(!sessionActuelle || sessionActuelle.cloturee) && (
          <p className="text-muted">Aucune session ouverte pour ajouter une présence manuelle.</p>
        )}
        {errors.presenceManuelle && <div className="error">{errors.presenceManuelle}</div>}
      </section>

      {/* Tableau Récapitulatif */}
      <section className="card">
        <h3>Tableau récapitulatif</h3>
        {loadingTableau ? (
          <div className="loading">Chargement du tableau...</div>
        ) : tableau && tableau.length > 0 ? (
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Étudiant</th>
                  <th>Présences</th>
                  <th>Exercices déposés</th>
                  <th>Moyenne</th>
                  <th>Relectures en attente</th>
                </tr>
              </thead>
              <tbody>
                {tableau.map((row: TableauEtudiantResponse) => (
                  <tr key={row.etudiantId}>
                    <td>{row.nom}</td>
                    <td className="center">{row.presences}</td>
                    <td className="center">{row.exercicesDeposes}</td>
                    <td className="center">
                      {row.moyenne !== null ? (
                        <>
                          {row.moyenne.toFixed(1)}
                          {row.moyenneProvisoire && <span className="provisoire-badge"> (provisoire)</span>}
                        </>
                      ) : '—'}
                    </td>
                    <td className="center">
                      {row.relecturesEnAttente > 0 ? (
                        <span className="badge warning">{row.relecturesEnAttente}</span>
                      ) : (
                        '—'
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <p className="text-muted">Aucune donnée pour cette promotion.</p>
        )}
      </section>
    </div>
  );
}