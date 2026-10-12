import { Link } from 'react-router-dom';

export function NotFoundPage() {
  return (
    <section className="status">
      <h1>404</h1>
      <p>This page fled like a wild Abra.</p>
      <Link to="/" className="btn btn-primary">
        Back to the Pokédex
      </Link>
    </section>
  );
}
