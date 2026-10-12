import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ApiError } from '../api/client';
import { CustomizationForm } from './CustomizationForm';

const initial = { localizedName: 'Pikachu (JP)', region: null, tags: ['mascot'], notes: null };

describe('CustomizationForm', () => {
  it('submits normalised values', async () => {
    const onSubmit = vi.fn();
    render(<CustomizationForm initial={initial} submitting={false} error={null} onSubmit={onSubmit} onCancel={vi.fn()} />);

    await userEvent.selectOptions(screen.getByLabelText('Region'), 'JOHTO');
    const tags = screen.getByLabelText(/Tags/);
    await userEvent.clear(tags);
    await userEvent.type(tags, ' Mascot, electric-team , mascot');
    await userEvent.click(screen.getByRole('button', { name: 'Save changes' }));

    expect(onSubmit).toHaveBeenCalledWith({
      localizedName: 'Pikachu (JP)',
      region: 'JOHTO',
      tags: ['mascot', 'electric-team'],
      notes: null,
    });
  });

  it('blocks invalid tags on the client', async () => {
    const onSubmit = vi.fn();
    render(<CustomizationForm initial={initial} submitting={false} error={null} onSubmit={onSubmit} onCancel={vi.fn()} />);

    await userEvent.type(screen.getByLabelText(/Tags/), ', bad tag!');
    await userEvent.click(screen.getByRole('button', { name: 'Save changes' }));

    expect(onSubmit).not.toHaveBeenCalled();
    expect(screen.getByText(/Invalid tag "bad tag!"/)).toBeInTheDocument();
  });

  it('shows field errors returned by the server', () => {
    const error = new ApiError(400, 'Invalid Pokemon customization', { region: 'must be one of [...]' });
    render(<CustomizationForm initial={initial} submitting={false} error={error} onSubmit={vi.fn()} onCancel={vi.fn()} />);

    expect(screen.getByText('must be one of [...]')).toBeInTheDocument();
    expect(screen.getByRole('combobox')).toHaveAttribute('aria-invalid', 'true');
  });
});
