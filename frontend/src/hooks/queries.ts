import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { ApiError } from '../api/client';
import { catalogApi, localApi } from '../api/endpoints';
import type { Customization } from '../api/types';

export const CATALOG_PAGE_SIZE = 24;
export const LOCAL_PAGE_SIZE = 12;

export function useCatalogPage(page: number) {
  return useQuery({
    queryKey: ['catalog', page],
    queryFn: () => catalogApi.list(page, CATALOG_PAGE_SIZE),
    placeholderData: keepPreviousData,
    staleTime: 10 * 60_000,
  });
}

export function usePokemonDetail(idOrName: string) {
  return useQuery({
    queryKey: ['pokemon', idOrName],
    queryFn: () => catalogApi.detail(idOrName),
    staleTime: 10 * 60_000,
  });
}

export function useLocalPage(page: number, search: string) {
  return useQuery({
    queryKey: ['local', 'list', page, search],
    queryFn: () => localApi.list(page, LOCAL_PAGE_SIZE, search),
    placeholderData: keepPreviousData,
  });
}

export function useLocalPokemon(id: number) {
  return useQuery({ queryKey: ['local', 'item', id], queryFn: () => localApi.get(id) });
}

export function useImportPokemon() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (idOrName: string) => localApi.import(idOrName),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['local'] }),
  });
}

export function useUpdatePokemon(id: number) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ customization, version }: { customization: Customization; version: number }) =>
      localApi.update(id, customization, version),
    onSuccess: (saved) => {
      qc.setQueryData(['local', 'item', id], saved);
      return qc.invalidateQueries({ queryKey: ['local', 'list'] });
    },
    onError: (error) => {
      // Someone else saved first: load their version so the form shows what is really stored
      if (error instanceof ApiError && error.status === 412) {
        return qc.invalidateQueries({ queryKey: ['local', 'item', id] });
      }
    },
  });
}

export function useRefreshPokemon() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => localApi.refresh(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['local'] }),
  });
}

export function useDeletePokemon() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => localApi.remove(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['local'] }),
  });
}
