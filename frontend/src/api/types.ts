export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface PokemonSummary {
  id: number;
  name: string;
  spriteUrl: string | null;
  category: string | null;
  types: string[];
  weightKg: number;
  abilities: string[];
}

export interface Evolution {
  speciesId: number;
  name: string;
  imageUrl: string;
  trigger: string | null;
  evolvesTo: Evolution[];
}

export interface PokemonDetail {
  id: number;
  name: string;
  imageUrl: string | null;
  spriteUrl: string | null;
  category: string | null;
  description: string | null;
  types: string[];
  abilities: string[];
  heightM: number;
  weightKg: number;
  stats: { name: string; baseValue: number }[];
  evolutionChain: Evolution | null;
}

export interface LocalPokemon {
  id: number;
  pokeApiId: number;
  name: string;
  imageUrl: string | null;
  spriteUrl: string | null;
  category: string | null;
  types: string[];
  abilities: string[];
  heightM: number;
  weightKg: number;
  localizedName: string | null;
  region: Region | null;
  tags: string[];
  notes: string | null;
  syncedAt: string;
  updatedAt: string;
  /** Optimistic concurrency token, sent back as If-Match when updating. */
  version: number;
}

export const REGIONS = ['KANTO', 'JOHTO', 'HOENN', 'SINNOH', 'UNOVA', 'KALOS', 'ALOLA', 'GALAR', 'PALDEA'] as const;
export type Region = (typeof REGIONS)[number];

export interface Customization {
  localizedName: string | null;
  region: Region | null;
  tags: string[];
  notes: string | null;
}

export type Role = 'USER' | 'ADMIN';

export interface AuthResponse {
  token: string;
  tokenType: string;
  expiresAt: string;
  username: string;
  role: Role;
}
