import { request } from './client';
import type { AuthResponse, Customization, LocalPokemon, Page, PokemonDetail, PokemonSummary } from './types';

const qs = (params: Record<string, string | number | undefined>) =>
  new URLSearchParams(
    Object.entries(params)
      .filter(([, v]) => v !== undefined && v !== '')
      .map(([k, v]) => [k, String(v)]),
  ).toString();

export const catalogApi = {
  list: (page: number, size: number) => request<Page<PokemonSummary>>(`/pokemon?${qs({ page, size })}`),
  detail: (idOrName: string) => request<PokemonDetail>(`/pokemon/${encodeURIComponent(idOrName)}`),
};

export const localApi = {
  list: (page: number, size: number, search?: string) =>
    request<Page<LocalPokemon>>(`/local-pokemon?${qs({ page, size, search })}`),
  get: (id: number) => request<LocalPokemon>(`/local-pokemon/${id}`),
  import: (idOrName: string, customization?: Partial<Customization>) =>
    request<LocalPokemon>('/local-pokemon', { method: 'POST', body: JSON.stringify({ idOrName, ...customization }) }),
  refresh: (id: number) => request<LocalPokemon>(`/local-pokemon/${id}/sync`, { method: 'POST' }),
  /** `version` is the one the form was loaded with: the API answers 412 if someone saved a newer one. */
  update: (id: number, customization: Customization, version: number) =>
    request<LocalPokemon>(`/local-pokemon/${id}`, {
      method: 'PUT',
      headers: { 'If-Match': `"${version}"` },
      body: JSON.stringify(customization),
    }),
  remove: (id: number) => request<void>(`/local-pokemon/${id}`, { method: 'DELETE' }),
};

export const authApi = {
  login: (username: string, password: string) =>
    request<AuthResponse>('/auth/login', { method: 'POST', body: JSON.stringify({ username, password }) }),
  register: (username: string, password: string) =>
    request<AuthResponse>('/auth/register', { method: 'POST', body: JSON.stringify({ username, password }) }),
};
