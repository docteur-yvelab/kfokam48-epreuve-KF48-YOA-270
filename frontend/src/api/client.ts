import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import type { ErrorResponse } from '../types';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const etudiantId = localStorage.getItem('etudiantId');
    if (etudiantId && config.url?.includes('/relectures')) {
      config.headers['X-Etudiant-Id'] = etudiantId;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ErrorResponse>) => {
    if (error.response?.data) {
      const apiError = new Error(error.response.data.message) as Error & { response: AxiosError<ErrorResponse>['response'] };
      apiError.response = error.response;
      return Promise.reject(apiError);
    }
    return Promise.reject(error);
  }
);

export const sessionApi = {
  ouvrir: (data: { titre: string; promotionId: number }) =>
    api.post<{ id: number; code: string; ouvertureAt: string; expirationAt: string }>('/api/sessions', data),
  cloturer: (id: number) => api.patch(`/api/sessions/${id}/cloture`),
  getOuverte: (promotionId: number) => api.get(`/api/sessions?promotionId=${promotionId}`),
  getById: (id: number) => api.get(`/api/sessions/${id}`),
};

export const presenceApi = {
  marquer: (data: { code: string; etudiantId: number }) =>
    api.post<{ id: number; sessionId: number; etudiantId: number; source: string }>('/api/presences', data),
  manuelle: (data: { sessionId: number; etudiantId: number }) =>
    api.post('/api/presences/manuelle', data),
};

export const exerciceApi = {
  deposer: (data: { sessionId: number; etudiantId: number; lien: string }) =>
    api.post<{ id: number; statut: string }>('/api/exercices', data),
  getByEtudiant: (sessionId: number, etudiantId: number) =>
    api.get(`/api/exercices?sessionId=${sessionId}&etudiantId=${etudiantId}`),
  getById: (id: number) => api.get(`/api/exercices/${id}`),
  remplacerLien: (id: number, lien: string) =>
    api.put(`/api/exercices/${id}`, { lien }),
};

export const relectureApi = {
  soumettre: (exerciceId: number, ordreRelecteur: 1 | 2, data: { note: number; commentaire: string }) =>
    api.post(`/api/relectures/${exerciceId}`, { ...data, ordreRelecteur }),
  getByExercice: (exerciceId: number) => api.get(`/api/relectures/exercice/${exerciceId}`),
  getEnAttente: (etudiantId: number) => api.get(`/api/relectures/en-attente?etudiantId=${etudiantId}`),
  getMesRelectures: (etudiantId: number) => api.get(`/api/relectures/mes-relectures?etudiantId=${etudiantId}`),
};

export const promotionApi = {
  getEtudiants: (promotionId: number) => api.get(`/api/promotions/${promotionId}/etudiants`),
};

export const tableauApi = {
  getTableau: (promotionId: number) => api.get(`/api/tableau?promotionId=${promotionId}`),
};

export default api;