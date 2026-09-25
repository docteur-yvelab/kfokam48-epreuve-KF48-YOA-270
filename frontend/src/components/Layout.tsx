import { Outlet, Link, useLocation } from 'react-router-dom';
import { useAuthStore } from '../store';
import './Layout.css';

export default function Layout() {
  const location = useLocation();
  const { etudiant, promotionId } = useAuthStore();

  const navLinks = [
    { path: '/formateur', label: 'Formateur', exact: false },
    { path: '/etudiant', label: 'Étudiant', exact: false },
  ];

  return (
    <div className="layout">
      <header className="header">
        <div className="header-content">
          <h1 className="logo">PresenceKFOKAM</h1>
          <nav className="nav">
            {navLinks.map((link) => (
              <Link
                key={link.path}
                to={link.path}
                className={location.pathname.startsWith(link.path) ? 'active' : ''}
              >
                {link.label}
              </Link>
            ))}
            {etudiant && (
              <span className="user-info">
                {etudiant.prenom} {etudiant.nom} (Promo {promotionId})
              </span>
            )}
          </nav>
        </div>
      </header>
      <main className="main">
        <Outlet />
      </main>
      <footer className="footer">
        KFOKAM48 - Épreuve Finale Fullstack
      </footer>
    </div>
  );
}