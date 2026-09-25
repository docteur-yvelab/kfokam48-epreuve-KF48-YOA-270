import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { Etudiant } from '../types';

interface AuthState {
  etudiant: Etudiant | null;
  promotionId: number | null;
  setEtudiant: (etudiant: Etudiant) => void;
  setPromotionId: (id: number) => void;
  clear: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      etudiant: null,
      promotionId: 1,
      setEtudiant: (etudiant) => set({ etudiant }),
      setPromotionId: (promotionId) => set({ promotionId }),
      clear: () => set({ etudiant: null }),
    }),
    { name: 'presence-auth' }
  )
);

interface UIState {
  loading: Record<string, boolean>;
  errors: Record<string, string>;
  setLoading: (key: string, loading: boolean) => void;
  setError: (key: string, error: string) => void;
  clearError: (key: string) => void;
}

export const useUIStore = create<UIState>((set) => ({
  loading: {},
  errors: {},
  setLoading: (key, loading) => set((state) => ({
    loading: { ...state.loading, [key]: loading }
  })),
  setError: (key, error) => set((state) => ({
    errors: { ...state.errors, [key]: error }
  })),
  clearError: (key) => set((state) => {
    const newErrors = { ...state.errors };
    delete newErrors[key];
    return { errors: newErrors };
  }),
}));