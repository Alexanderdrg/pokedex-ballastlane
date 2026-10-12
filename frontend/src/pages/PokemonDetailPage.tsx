import { Link, useParams } from 'react-router-dom';
import { ApiError } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import { EvolutionTree } from '../components/EvolutionTree';
import { StatBars } from '../components/StatBars';
import { ErrorState, Spinner } from '../components/Status';
import { TypeList } from '../components/TypeBadge';
import { dexNumber, displayName } from '../format';
import { useImportPokemon, usePokemonDetail } from '../hooks/queries';

export function PokemonDetailPage() {
  const { idOrName = '' } = useParams();
  const { data, isPending, isError, error, refetch } = usePokemonDetail(idOrName.toLowerCase());

  if (isPending) return <Spinner />;
  if (isError) {
    return (
      <section>
        <ErrorState error={error} onRetry={error instanceof ApiError && error.status >= 500 ? () => refetch() : undefined} />
        <p className="center">
          <Link to="/">← Back to the Pokédex</Link>
        </p>
      </section>
    );
  }

  const p = data;
  return (
    <article className="detail">
      <Link to="/" className="back">
        ← Back
      </Link>
      <div className="detail-hero card">
        <div className="detail-art">
          {p.imageUrl && <img src={p.imageUrl} alt={p.name} width={280} height={280} />}
        </div>
        <div className="detail-info">
          <span className="dex">{dexNumber(p.id)}</span>
          <h1>{displayName(p.name)}</h1>
          {p.category && <p className="muted">{p.category}</p>}
          <TypeList types={p.types} />
          {p.description && <p className="description">{p.description}</p>}
          <dl className="facts facts-inline">
            <div>
              <dt>Height</dt>
              <dd>{p.heightM} m</dd>
            </div>
            <div>
              <dt>Weight</dt>
              <dd>{p.weightKg} kg</dd>
            </div>
            <div>
              <dt>Abilities</dt>
              <dd>{p.abilities.map(displayName).join(', ')}</dd>
            </div>
          </dl>
          <SaveToCollection idOrName={p.name} />
        </div>
      </div>

      <div className="detail-grid">
        <section className="card">
          <h2>Base stats</h2>
          <StatBars stats={p.stats} />
        </section>
        <section className="card">
          <h2>Evolution chain</h2>
          {p.evolutionChain ? (
            <div className="evo-scroll">
              <EvolutionTree node={p.evolutionChain} currentId={p.id} />
            </div>
          ) : (
            <p className="muted">No evolution data.</p>
          )}
        </section>
      </div>
    </article>
  );
}

function SaveToCollection({ idOrName }: { idOrName: string }) {
  const { user } = useAuth();
  const importMutation = useImportPokemon();

  if (!user) {
    return (
      <p className="muted">
        <Link to="/login">Log in</Link> to save this Pokémon to your collection.
      </p>
    );
  }
  if (importMutation.isSuccess) {
    return (
      <p className="success" role="status">
        Saved! <Link to={`/collection/${importMutation.data.id}`}>Customize it →</Link>
      </p>
    );
  }
  const err = importMutation.error;
  return (
    <div className="save-row">
      <button
        type="button"
        className="btn btn-primary"
        disabled={importMutation.isPending}
        onClick={() => importMutation.mutate(idOrName)}
      >
        {importMutation.isPending ? 'Saving…' : '+ Save to my collection'}
      </button>
      {err && (
        <span className={err instanceof ApiError && err.status === 409 ? 'muted' : 'field-error'} role="alert">
          {err instanceof ApiError && err.status === 409 ? (
            <>
              Already in your collection. <Link to="/collection">View collection</Link>
            </>
          ) : (
            err.message
          )}
        </span>
      )}
    </div>
  );
}
