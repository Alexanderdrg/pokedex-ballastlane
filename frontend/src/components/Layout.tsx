import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export function Layout() {
  const { user, signOut } = useAuth();
  const navigate = useNavigate();

  return (
    <div className="app">
      <header className="topbar">
        <div className="topbar-inner">
          <NavLink to="/" className="brand" aria-label="Pokédex home">
            <img src="/pokeball.svg" alt="" width={28} height={28} />
            <span>Pokédex</span>
          </NavLink>
          <nav className="nav">
            <NavLink to="/" end>
              Browse
            </NavLink>
            <NavLink to="/collection">My collection</NavLink>
          </nav>
          <div className="session">
            {user ? (
              <>
                <span className="who">
                  {user.username}
                  {user.role === 'ADMIN' && <span className="role">admin</span>}
                </span>
                <button
                  type="button"
                  className="btn btn-ghost"
                  onClick={() => {
                    signOut();
                    navigate('/');
                  }}
                >
                  Log out
                </button>
              </>
            ) : (
              <NavLink to="/login" className="btn btn-primary">
                Log in
              </NavLink>
            )}
          </div>
        </div>
      </header>
      <main className="container">
        <Outlet />
      </main>
      <footer className="footer">
        Data from <a href="https://pokeapi.co" target="_blank" rel="noreferrer">PokeAPI</a>
      </footer>
    </div>
  );
}
