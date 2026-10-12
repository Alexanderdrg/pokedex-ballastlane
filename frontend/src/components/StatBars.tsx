import { STAT_LABELS } from '../format';

const MAX_BASE_STAT = 255;

export function StatBars({ stats }: { stats: { name: string; baseValue: number }[] }) {
  const total = stats.reduce((sum, s) => sum + s.baseValue, 0);
  return (
    <table className="stats">
      <tbody>
        {stats.map((s) => (
          <tr key={s.name}>
            <th scope="row">{STAT_LABELS[s.name] ?? s.name}</th>
            <td className="stat-value">{s.baseValue}</td>
            <td className="stat-bar-cell">
              <div
                className="stat-bar"
                role="meter"
                aria-label={STAT_LABELS[s.name] ?? s.name}
                aria-valuemin={0}
                aria-valuemax={MAX_BASE_STAT}
                aria-valuenow={s.baseValue}
              >
                <span style={{ width: `${Math.min(100, (s.baseValue / MAX_BASE_STAT) * 100)}%` }} />
              </div>
            </td>
          </tr>
        ))}
        <tr className="stat-total">
          <th scope="row">Total</th>
          <td className="stat-value">{total}</td>
          <td />
        </tr>
      </tbody>
    </table>
  );
}
