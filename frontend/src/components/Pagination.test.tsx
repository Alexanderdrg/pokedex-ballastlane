import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Pagination } from './Pagination';

describe('Pagination', () => {
  it('disables previous buttons on the first page and moves forward', async () => {
    const onChange = vi.fn();
    render(<Pagination page={0} totalPages={5} onChange={onChange} />);

    expect(screen.getByRole('button', { name: /Prev/ })).toBeDisabled();
    expect(screen.getByText('Page 1 of 5')).toBeInTheDocument();
    await userEvent.click(screen.getByRole('button', { name: /Next/ }));
    expect(onChange).toHaveBeenCalledWith(1);
  });

  it('disables next on the last page', () => {
    render(<Pagination page={4} totalPages={5} onChange={vi.fn()} />);
    expect(screen.getByRole('button', { name: /Next/ })).toBeDisabled();
  });

  it('renders nothing for a single page', () => {
    const { container } = render(<Pagination page={0} totalPages={1} onChange={vi.fn()} />);
    expect(container).toBeEmptyDOMElement();
  });
});
