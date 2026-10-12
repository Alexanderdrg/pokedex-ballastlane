import { useState, type FormEvent } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { ApiError } from '../api/client';
import { authApi } from '../api/endpoints';
import { useAuth } from '../auth/AuthContext';

export function AuthPage({ mode }: { mode: 'login' | 'register' }) {
  const isLogin = mode === 'login';
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const { signIn } = useAuth();
  const navigate = useNavigate();
  const from = (useLocation().state as { from?: string } | null)?.from ?? '/collection';

  const mutation = useMutation({
    mutationFn: () => (isLogin ? authApi.login(username, password) : authApi.register(username, password)),
    onSuccess: (auth) => {
      signIn(auth);
      navigate(from, { replace: true });
    },
  });

  const fieldErrors = mutation.error instanceof ApiError ? mutation.error.fieldErrors : {};

  const submit = (e: FormEvent) => {
    e.preventDefault();
    mutation.mutate();
  };

  return (
    <section className="auth">
      <div className="card auth-card">
        <h1>{isLogin ? 'Welcome back, trainer' : 'Create your trainer account'}</h1>
        <form className="form" onSubmit={submit}>
          <label>
            Username
            <input
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoComplete="username"
              required
              aria-invalid={!!fieldErrors.username}
            />
            {fieldErrors.username && <span className="field-error">{fieldErrors.username}</span>}
          </label>
          <label>
            Password
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete={isLogin ? 'current-password' : 'new-password'}
              required
              aria-invalid={!!fieldErrors.password}
            />
            {!isLogin && <small className="muted">At least 8 characters, with a letter and a digit.</small>}
            {fieldErrors.password && <span className="field-error">{fieldErrors.password}</span>}
          </label>
          {mutation.isError && Object.keys(fieldErrors).length === 0 && (
            <p className="form-error" role="alert">
              {mutation.error.message}
            </p>
          )}
          <button type="submit" className="btn btn-primary btn-block" disabled={mutation.isPending}>
            {mutation.isPending ? 'Please wait…' : isLogin ? 'Log in' : 'Sign up'}
          </button>
        </form>
        {isLogin ? (
          <>
            <p className="muted small">
              No account? <Link to="/register">Sign up</Link>
            </p>
            <div className="demo-creds">
              <strong>Demo accounts</strong>
              <span>
                <code>admin</code> / <code>admin123</code> (can delete)
              </span>
              <span>
                <code>ash</code> / <code>pikachu123</code>
              </span>
            </div>
          </>
        ) : (
          <p className="muted small">
            Already registered? <Link to="/login">Log in</Link>
          </p>
        )}
      </div>
    </section>
  );
}
