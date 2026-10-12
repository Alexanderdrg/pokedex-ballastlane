import { Link, useNavigate, useParams } from 'react-router-dom';
import { ApiError } from '../api/client';
import { CustomizationForm } from '../components/CustomizationForm';
import { ErrorState, Spinner } from '../components/Status';
import { TypeList } from '../components/TypeBadge';
import { dexNumber, displayName } from '../format';
import { useLocalPokemon, useUpdatePokemon } from '../hooks/queries';

export function EditPokemonPage() {
  const id = Number(useParams().id);
  const navigate = useNavigate();
  const { data, isPending, isError, error } = useLocalPokemon(id);
  const update = useUpdatePokemon(id);

  // 412: the page already reloaded the latest data (see useUpdatePokemon), so tell the user what happened
  const updateError =
    update.error instanceof ApiError && update.error.status === 412
      ? new Error('Someone else saved this Pokémon while you were editing. The form now shows their changes: apply yours again and save.')
      : update.error;

  if (isPending) return <Spinner />;
  if (isError) {
    return (
      <section>
        <ErrorState error={error} />
        <p className="center">
          <Link to="/collection">← Back to collection</Link>
        </p>
      </section>
    );
  }

  return (
    <section className="edit">
      <Link to="/collection" className="back">
        ← Back to collection
      </Link>
      <div className="card edit-card">
        <header className="edit-head">
          {data.imageUrl && <img src={data.imageUrl} alt={data.name} width={120} height={120} />}
          <div>
            <span className="dex">{dexNumber(data.pokeApiId)}</span>
            <h1>{displayName(data.name)}</h1>
            <p className="muted">{data.category}</p>
            <TypeList types={data.types} />
            <p className="muted small">Last synced {new Date(data.syncedAt).toLocaleString()}</p>
          </div>
        </header>
        <CustomizationForm
          key={data.updatedAt}
          initial={{ localizedName: data.localizedName, region: data.region, tags: data.tags, notes: data.notes }}
          submitting={update.isPending}
          error={updateError}
          onSubmit={(customization) =>
            update.mutate({ customization, version: data.version }, { onSuccess: () => navigate('/collection') })
          }
          onCancel={() => navigate('/collection')}
        />
      </div>
    </section>
  );
}
