import { afterEach, describe, expect, it, vi } from 'vitest';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Route, Routes } from 'react-router-dom';
import { EditPokemonPage } from './EditPokemonPage';
import { jsonResponse, renderWithProviders } from '../test/renderWithProviders';
import type { LocalPokemon } from '../api/types';

const bulbasaur = (overrides: Partial<LocalPokemon>): LocalPokemon => ({
  id: 1,
  pokeApiId: 1,
  name: 'bulbasaur',
  imageUrl: null,
  spriteUrl: null,
  category: 'Seed Pokémon',
  types: ['grass'],
  abilities: ['overgrow'],
  heightM: 0.7,
  weightKg: 6.9,
  localizedName: null,
  region: null,
  tags: [],
  notes: null,
  syncedAt: '2026-01-01T10:00:00Z',
  updatedAt: '2026-01-01T10:00:00Z',
  version: 0,
  ...overrides,
});

describe('EditPokemonPage', () => {
  afterEach(() => vi.restoreAllMocks());

  it('sends the version it loaded and recovers when someone else saved first', async () => {
    const fetchMock = vi
      .spyOn(globalThis, 'fetch')
      // 1. the form loads version 0
      .mockResolvedValueOnce(jsonResponse(bulbasaur({ notes: 'old notes' })))
      // 2. meanwhile someone saved version 1, so our PUT is rejected
      .mockResolvedValueOnce(jsonResponse({ status: 412, message: 'modified by someone else' }, 412))
      // 3. the page reloads the latest data
      .mockResolvedValueOnce(
        jsonResponse(bulbasaur({ notes: 'their notes', version: 1, updatedAt: '2026-01-01T10:05:00Z' })),
      );

    renderWithProviders(
      <Routes>
        <Route path="collection/:id" element={<EditPokemonPage />} />
      </Routes>,
      { route: '/collection/1' },
    );

    const notes = await screen.findByLabelText(/notes/i);
    await userEvent.clear(notes);
    await userEvent.type(notes, 'my notes');
    await userEvent.click(screen.getByRole('button', { name: 'Save changes' }));

    const [url, init] = fetchMock.mock.calls[1];
    expect(url).toBe('/api/local-pokemon/1');
    expect(init!.method).toBe('PUT');
    expect((init!.headers as Headers).get('If-Match')).toBe('"0"');

    expect(await screen.findByRole('alert')).toHaveTextContent('Someone else saved this Pokémon');
    expect(await screen.findByDisplayValue('their notes')).toBeInTheDocument();
  });
});
