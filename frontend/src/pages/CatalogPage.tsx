import { useState, type FormEvent } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Pagination } from '../components/Pagination';
import { PokemonCard } from '../components/PokemonCard';
import { ErrorState, Spinner } from '../components/Status';
import { useCatalogPage } from '../hooks/queries';

export function CatalogPage() {
  const [params, setParams] = useSearchParams();
  const page = Math.max(0, Number(params.get('page') ?? 0) || 0);
  const { data, isPending, isError, error, refetch, isPlaceholderData } = useCatalogPage(page);
  const [lookup, setLookup] = useState('');
  const navigate = useNavigate();

  const goTo = (next: number) => {
    setParams(next === 0 ? {} : { page: String(next) });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const onLookup = (e: FormEvent) => {
    e.preventDefault();
    const value = lookup.trim().toLowerCase();
    if (value) navigate(`/pokemon/${encodeURIComponent(value)}`);
  };

  return (
    <section>
      <div className="page-head">
        <div>
          <h1>Pokédex</h1>
          <p className="muted">
            {data ? `${data.totalElements.toLocaleString()} Pokémon from PokeAPI` : 'Browse every Pokémon'}
          </p>
        </div>
        <form className="search" onSubmit={onLookup} role="search">
          <input
            value={lookup}
            onChange={(e) => setLookup(e.target.value)}
            placeholder="Go to name or number…"
            aria-label="Pokémon name or number"
          />
          <button type="submit" className="btn btn-primary">
            Go
          </button>
        </form>
      </div>

      {isPending && <Spinner label="Catching Pokémon…" />}
      {isError && <ErrorState error={error} onRetry={() => refetch()} />}
      {data && (
        <>
          <div className={`grid ${isPlaceholderData ? 'is-loading' : ''}`} aria-busy={isPlaceholderData}>
            {data.content.map((p) => (
              <PokemonCard key={p.id} pokemon={p} />
            ))}
          </div>
          <Pagination page={page} totalPages={data.totalPages} onChange={goTo} disabled={isPlaceholderData} />
        </>
      )}
    </section>
  );
}
