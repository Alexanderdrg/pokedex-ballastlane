import { afterEach, describe, expect, it, vi } from 'vitest';
import { screen } from '@testing-library/react';
import { CatalogPage } from './CatalogPage';
import { jsonResponse, renderWithProviders } from '../test/renderWithProviders';

describe('CatalogPage', () => {
  afterEach(() => vi.restoreAllMocks());

  it('renders a page of Pokemon cards from the API', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      jsonResponse({
        content: [
          {
            id: 25,
            name: 'pikachu',
            spriteUrl: 'p.png',
            category: 'Mouse Pokémon',
            types: ['electric'],
            weightKg: 6,
            abilities: ['static', 'lightning-rod'],
          },
        ],
        page: 0,
        size: 24,
        totalElements: 1302,
        totalPages: 55,
      }),
    );

    renderWithProviders(<CatalogPage />);

    expect(await screen.findByText('Pikachu')).toBeInTheDocument();
    expect(screen.getByText('Mouse Pokémon')).toBeInTheDocument();
    expect(screen.getByText('Static, Lightning Rod')).toBeInTheDocument();
    expect(screen.getByText('Page 1 of 55')).toBeInTheDocument();
    expect(globalThis.fetch).toHaveBeenCalledWith('/api/pokemon?page=0&size=24', expect.anything());
  });

  it('shows an error with retry when the API fails', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse({ message: 'PokeAPI is unavailable' }, 502));

    renderWithProviders(<CatalogPage />);

    expect(await screen.findByText('PokeAPI is unavailable')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Try again' })).toBeInTheDocument();
  });
});
