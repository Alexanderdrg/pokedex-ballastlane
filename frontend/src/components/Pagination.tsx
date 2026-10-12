interface Props {
  page: number;
  totalPages: number;
  onChange: (page: number) => void;
  disabled?: boolean;
}

export function Pagination({ page, totalPages, onChange, disabled }: Props) {
  if (totalPages <= 1) return null;
  return (
    <nav className="pagination" aria-label="Pagination">
      <button type="button" className="btn" disabled={disabled || page === 0} onClick={() => onChange(0)}>
        « First
      </button>
      <button type="button" className="btn" disabled={disabled || page === 0} onClick={() => onChange(page - 1)}>
        ‹ Prev
      </button>
      <span className="page-indicator">
        Page {page + 1} of {totalPages}
      </span>
      <button
        type="button"
        className="btn"
        disabled={disabled || page >= totalPages - 1}
        onClick={() => onChange(page + 1)}
      >
        Next ›
      </button>
    </nav>
  );
}
