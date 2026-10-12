export function TypeBadge({ type }: { type: string }) {
  return <span className={`type type-${type}`}>{type}</span>;
}

export function TypeList({ types }: { types: string[] }) {
  return (
    <div className="types">
      {types.map((t) => (
        <TypeBadge key={t} type={t} />
      ))}
    </div>
  );
}
