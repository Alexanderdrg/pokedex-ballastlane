import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { EvolutionTree } from './EvolutionTree';

const eevee = {
  speciesId: 133,
  name: 'eevee',
  imageUrl: 'eevee.png',
  trigger: null,
  evolvesTo: [
    { speciesId: 134, name: 'vaporeon', imageUrl: 'v.png', trigger: 'Use Water stone', evolvesTo: [] },
    { speciesId: 197, name: 'umbreon', imageUrl: 'u.png', trigger: 'High friendship (night)', evolvesTo: [] },
  ],
};

describe('EvolutionTree', () => {
  it('renders branches with their triggers and highlights the current Pokemon', () => {
    render(
      <MemoryRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
        <EvolutionTree node={eevee} currentId={197} />
      </MemoryRouter>,
    );

    expect(screen.getByText('Vaporeon')).toBeInTheDocument();
    expect(screen.getByText('Use Water stone')).toBeInTheDocument();
    expect(screen.getByText('High friendship (night)')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Umbreon/ })).toHaveAttribute('aria-current', 'page');
  });
});
