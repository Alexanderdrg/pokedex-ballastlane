import { Link } from 'react-router-dom';
import type { Evolution } from '../api/types';
import { displayName } from '../format';

const FAN_THRESHOLD = 3;

function EvoNode({ node, currentId }: { node: Evolution; currentId: number }) {
  return (
    <Link
      to={`/pokemon/${node.name}`}
      className={`evo-node ${node.speciesId === currentId ? 'current' : ''}`}
      aria-current={node.speciesId === currentId ? 'page' : undefined}
    >
      <img src={node.imageUrl} alt={node.name} width={88} height={88} loading="lazy" />
      <span>{displayName(node.name)}</span>
    </Link>
  );
}

/**
 * Renders an evolution chain left to right. Small branches (e.g. Gloom) stack next to their parent;
 * large fans (e.g. Eevee) are laid out as a grid below the base form.
 */
export function EvolutionTree({ node, currentId }: { node: Evolution; currentId: number }) {
  if (node.evolvesTo.length > FAN_THRESHOLD) {
    return (
      <div className="evo-fan">
        <EvoNode node={node} currentId={currentId} />
        <div className="evo-fan-grid">
          {node.evolvesTo.map((child) => (
            <div key={child.speciesId} className="evo-fan-item">
              <EvolutionTree node={child} currentId={currentId} />
              {child.trigger && <small>{child.trigger}</small>}
            </div>
          ))}
        </div>
      </div>
    );
  }
  return (
    <div className="evo-row">
      <EvoNode node={node} currentId={currentId} />
      {node.evolvesTo.length > 0 && (
        <div className="evo-branches">
          {node.evolvesTo.map((child) => (
            <div key={child.speciesId} className="evo-branch">
              <div className="evo-arrow">
                <span aria-hidden="true">→</span>
                {child.trigger && <small>{child.trigger}</small>}
              </div>
              <EvolutionTree node={child} currentId={currentId} />
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
