import { useState, type FormEvent } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { ApiError } from '../api/client';
import type { LocalPokemon } from '../api/types';
import { useAuth } from '../auth/AuthContext';
import { Pagination } from '../components/Pagination';
import { EmptyState, ErrorState, Spinner } from '../components/Status';
import { TypeList } from '../components/TypeBadge';
import { dexNumber, displayName } from '../format';
import { useDebounce } from '../hooks/useDebounce';
import { useDeletePokemon, useImportPokemon, useLocalPage, useRefreshPokemon } from '../hooks/queries';

export function CollectionPage() {
  const [params, setParams] = useSearchParams();
  const page = Math.max(0, Number(params.get('page') ?? 0) || 0);
  const [search, setSearch] = useState(params.get('q') ?? '');
  const debounced = useDebounce(search.trim());
  const { data, isPending, isError, error, refetch, isPlaceholderData } = useLocalPage(page, debounced);

  const onSearch = (value: string) => {
    setSearch(value);
    setParams(value ? { q: value } : {}, { replace: true });
  };

  return (
    <section>
      <div className="page-head">
        <div>
          <h1>My collection</h1>
          <p className="muted">Pokémon stored in the local database, with your own fields.</p>
        </div>
        <ImportForm />
      </div>

      <input
        className="filter"
        type="search"
        value={search}
        onChange={(e) => onSearch(e.target.value)}
        placeholder="Filter by name or localized name…"
        aria-label="Filter collection"
      />

      {isPending && <Spinner />}
      {isError && <ErrorState error={error} onRetry={() => refetch()} />}
      {data && data.content.length === 0 && (
        <EmptyState>
          {debounced ? `No Pokémon match "${debounced}".` : 'Your collection is empty. Import one above or from the Pokédex.'}
        </EmptyState>
      )}
      {data && data.content.length > 0 && (
        <>
          <ul className={`collection ${isPlaceholderData ? 'is-loading' : ''}`}>
            {data.content.map((p) => (
              <CollectionItem key={p.id} pokemon={p} />
            ))}
          </ul>
          <Pagination
            page={page}
            totalPages={data.totalPages}
            disabled={isPlaceholderData}
            onChange={(next) => setParams({ ...(search ? { q: search } : {}), page: String(next) })}
          />
        </>
      )}
    </section>
  );
}

function ImportForm() {
  const [value, setValue] = useState('');
  const mutation = useImportPokemon();

  const submit = (e: FormEvent) => {
    e.preventDefault();
    if (value.trim()) mutation.mutate(value.trim().toLowerCase(), { onSuccess: () => setValue('') });
  };

  return (
    <form className="search import" onSubmit={submit}>
      <input
        value={value}
        onChange={(e) => {
          setValue(e.target.value);
          mutation.reset();
        }}
        placeholder="Import by name or number"
        aria-label="Pokémon to import"
      />
      <button type="submit" className="btn btn-primary" disabled={mutation.isPending || !value.trim()}>
        {mutation.isPending ? 'Importing…' : 'Import'}
      </button>
      {mutation.isError && (
        <span className="field-error" role="alert">
          {mutation.error.message}
        </span>
      )}
      {mutation.isSuccess && (
        <span className="success" role="status">
          {displayName(mutation.data.name)} imported.
        </span>
      )}
    </form>
  );
}

function CollectionItem({ pokemon: p }: { pokemon: LocalPokemon }) {
  const { isAdmin } = useAuth();
  const remove = useDeletePokemon();
  const refresh = useRefreshPokemon();
  const busy = remove.isPending || refresh.isPending;
  const actionError = remove.error ?? refresh.error;

  const onDelete = () => {
    if (window.confirm(`Delete ${displayName(p.name)} from your collection?`)) remove.mutate(p.id);
  };

  return (
    <li className="card collection-item">
      {p.spriteUrl && <img src={p.spriteUrl} alt={p.name} width={80} height={80} loading="lazy" />}
      <div className="collection-main">
        <div className="collection-title">
          <span className="dex">{dexNumber(p.pokeApiId)}</span>
          <h3>{displayName(p.name)}</h3>
          {p.localizedName && <span className="muted">· {p.localizedName}</span>}
        </div>
        <TypeList types={p.types} />
        <div className="meta">
          {p.region && <span className="chip chip-region">{p.region.toLowerCase()}</span>}
          {p.tags.map((t) => (
            <span key={t} className="chip">
              #{t}
            </span>
          ))}
        </div>
        {p.notes && <p className="notes">{p.notes}</p>}
        {actionError && (
          <span className="field-error" role="alert">
            {actionError instanceof ApiError ? actionError.message : 'Action failed'}
          </span>
        )}
      </div>
      <div className="collection-actions">
        <Link to={`/collection/${p.id}`} className="btn btn-primary">
          Edit
        </Link>
        <button type="button" className="btn" disabled={busy} onClick={() => refresh.mutate(p.id)} title="Refresh from PokeAPI">
          {refresh.isPending ? 'Syncing…' : 'Sync'}
        </button>
        {isAdmin && (
          <button type="button" className="btn btn-danger" disabled={busy} onClick={onDelete}>
            Delete
          </button>
        )}
      </div>
    </li>
  );
}
