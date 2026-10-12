import { Link } from 'react-router-dom';
import type { PokemonSummary } from '../api/types';
import { dexNumber, displayName } from '../format';
import { TypeList } from './TypeBadge';

export function PokemonCard({ pokemon }: { pokemon: PokemonSummary }) {
  return (
    <Link to={`/pokemon/${pokemon.name}`} className="card pokemon-card">
      <div className="sprite-wrap">
        {pokemon.spriteUrl ? (
          <img src={pokemon.spriteUrl} alt={pokemon.name} width={96} height={96} loading="lazy" />
        ) : (
          <div className="sprite-missing" aria-hidden="true">?</div>
        )}
      </div>
      <div className="card-body">
        <span className="dex">{dexNumber(pokemon.id)}</span>
        <h3>{displayName(pokemon.name)}</h3>
        {pokemon.category && <p className="muted">{pokemon.category}</p>}
        <TypeList types={pokemon.types} />
        <dl className="facts">
          <div>
            <dt>Weight</dt>
            <dd>{pokemon.weightKg} kg</dd>
          </div>
          <div>
            <dt>Abilities</dt>
            <dd>{pokemon.abilities.map(displayName).join(', ')}</dd>
          </div>
        </dl>
      </div>
    </Link>
  );
}
