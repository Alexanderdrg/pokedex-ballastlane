import { Route, Routes } from 'react-router-dom';
import { ProtectedRoute } from './auth/ProtectedRoute';
import { Layout } from './components/Layout';
import { AuthPage } from './pages/AuthPage';
import { CatalogPage } from './pages/CatalogPage';
import { CollectionPage } from './pages/CollectionPage';
import { EditPokemonPage } from './pages/EditPokemonPage';
import { NotFoundPage } from './pages/NotFoundPage';
import { PokemonDetailPage } from './pages/PokemonDetailPage';

export function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<CatalogPage />} />
        <Route path="pokemon/:idOrName" element={<PokemonDetailPage />} />
        <Route path="login" element={<AuthPage mode="login" />} />
        <Route path="register" element={<AuthPage mode="register" />} />
        <Route element={<ProtectedRoute />}>
          <Route path="collection" element={<CollectionPage />} />
          <Route path="collection/:id" element={<EditPokemonPage />} />
        </Route>
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  );
}
