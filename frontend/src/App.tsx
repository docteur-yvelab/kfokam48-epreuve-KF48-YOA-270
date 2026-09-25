import { Routes, Route, Navigate } from 'react-router-dom';
import { QueryClientProvider } from '@tanstack/react-query';
import Layout from './components/Layout';
import FormateurPage from './pages/FormateurPage';
import EtudiantPage from './pages/EtudiantPage';
import RelecturePage from './pages/RelecturePage';

function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/formateur" element={<FormateurPage />} />
        <Route path="/etudiant" element={<EtudiantPage />} />
        <Route path="/relecture/:id" element={<RelecturePage />} />
        <Route path="/" element={<Navigate to="/etudiant" replace />} />
        <Route path="*" element={<Navigate to="/etudiant" replace />} />
      </Routes>
    </Layout>
  );
}

export default App;