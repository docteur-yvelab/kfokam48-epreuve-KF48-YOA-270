import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { sessionApi, presenceApi, exerciceApi, relectureApi, promotionApi, tableauApi } from '../api/client';
import { useAuthStore, useUIStore } from '../store';
import type { SessionRequest, PresenceRequest, PresenceManuelleRequest, ExerciceRequest, RelectureRequest } from '../types';

export function useSessions(promotionId: number) {
  return useQuery({
    queryKey: ['sessions', promotionId],
    queryFn: () => sessionApi.getOuverte(promotionId),
    enabled: !!promotionId,
  });
}

export function useOuvrirSession() {
  const queryClient = useQueryClient();
  const { setLoading, setError } = useUIStore();

  return useMutation({
    mutationFn: (data: SessionRequest) => sessionApi.ouvrir(data),
    onMutate: () => setLoading('ouvrirSession', true),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['sessions'] });
      setError('ouvrirSession', '');
    },
    onError: (error: Error & { response?: { data: { message: string } } }) => {
      setError('ouvrirSession', error.response?.data?.message || error.message);
    },
    onSettled: () => setLoading('ouvrirSession', false),
  });
}

export function useCloturerSession() {
  const queryClient = useQueryClient();
  const { setLoading, setError } = useUIStore();

  return useMutation({
    mutationFn: (id: number) => sessionApi.cloturer(id),
    onMutate: () => setLoading('cloturerSession', true),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['sessions'] });
      queryClient.invalidateQueries({ queryKey: ['tableau'] });
      setError('cloturerSession', '');
    },
    onError: (error: Error & { response?: { data: { message: string } } }) => {
      setError('cloturerSession', error.response?.data?.message || error.message);
    },
    onSettled: () => setLoading('cloturerSession', false),
  });
}

export function useMarquerPresence() {
  const queryClient = useQueryClient();
  const { setLoading, setError } = useUIStore();

  return useMutation({
    mutationFn: (data: PresenceRequest) => presenceApi.marquer(data),
    onMutate: () => setLoading('marquerPresence', true),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tableau'] });
      setError('marquerPresence', '');
    },
    onError: (error: Error & { response?: { data: { message: string } } }) => {
      setError('marquerPresence', error.response?.data?.message || error.message);
    },
    onSettled: () => setLoading('marquerPresence', false),
  });
}

export function usePresenceManuelle() {
  const queryClient = useQueryClient();
  const { setLoading, setError } = useUIStore();

  return useMutation({
    mutationFn: (data: PresenceManuelleRequest) => presenceApi.manuelle(data),
    onMutate: () => setLoading('presenceManuelle', true),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tableau'] });
      setError('presenceManuelle', '');
    },
    onError: (error: Error & { response?: { data: { message: string } } }) => {
      setError('presenceManuelle', error.response?.data?.message || error.message);
    },
    onSettled: () => setLoading('presenceManuelle', false),
  });
}

export function useExercices(sessionId: number, etudiantId: number) {
  return useQuery({
    queryKey: ['exercices', sessionId, etudiantId],
    queryFn: () => exerciceApi.getByEtudiant(sessionId, etudiantId),
    enabled: !!sessionId && !!etudiantId,
  });
}

export function useDeposerExercice() {
  const queryClient = useQueryClient();
  const { setLoading, setError } = useUIStore();

  return useMutation({
    mutationFn: (data: ExerciceRequest) => exerciceApi.deposer(data),
    onMutate: () => setLoading('deposerExercice', true),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['exercices'] });
      queryClient.invalidateQueries({ queryKey: ['tableau'] });
      setError('deposerExercice', '');
    },
    onError: (error: Error & { response?: { data: { message: string } } }) => {
      setError('deposerExercice', error.response?.data?.message || error.message);
    },
    onSettled: () => setLoading('deposerExercice', false),
  });
}

export function useRemplacerLien() {
  const queryClient = useQueryClient();
  const { setLoading, setError } = useUIStore();

  return useMutation({
    mutationFn: ({ id, lien }: { id: number; lien: string }) => exerciceApi.remplacerLien(id, lien),
    onMutate: () => setLoading('remplacerLien', true),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['exercices'] });
      setError('remplacerLien', '');
    },
    onError: (error: Error & { response?: { data: { message: string } } }) => {
      setError('remplacerLien', error.response?.data?.message || error.message);
    },
    onSettled: () => setLoading('remplacerLien', false),
  });
}

export function useRelectureEnAttente(etudiantId: number) {
  return useQuery({
    queryKey: ['relectures-en-attente', etudiantId],
    queryFn: () => relectureApi.getEnAttente(etudiantId),
    enabled: !!etudiantId,
  });
}

export function useSoumettreRelecture() {
  const queryClient = useQueryClient();
  const { setLoading, setError } = useUIStore();

  return useMutation({
    mutationFn: ({ exerciceId, ordreRelecteur, data }: { exerciceId: number; ordreRelecteur: 1 | 2; data: RelectureRequest }) => 
      relectureApi.soumettre(exerciceId, ordreRelecteur, data),
    onMutate: () => setLoading('soumettreRelecture', true),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['relectures-en-attente'] });
      queryClient.invalidateQueries({ queryKey: ['exercices'] });
      queryClient.invalidateQueries({ queryKey: ['tableau'] });
      setError('soumettreRelecture', '');
    },
    onError: (error: Error & { response?: { data: { message: string } } }) => {
      setError('soumettreRelecture', error.response?.data?.message || error.message);
    },
    onSettled: () => setLoading('soumettreRelecture', false),
  });
}

export function useEtudiants(promotionId: number) {
  return useQuery({
    queryKey: ['etudiants', promotionId],
    queryFn: () => promotionApi.getEtudiants(promotionId),
    enabled: !!promotionId,
  });
}

export function useTableau(promotionId: number) {
  return useQuery({
    queryKey: ['tableau', promotionId],
    queryFn: () => tableauApi.getTableau(promotionId),
    enabled: !!promotionId,
  });
}