import { Routes, Route } from "react-router-dom";
import { useAuth } from "./context/AuthContext.jsx";

import HomePage from "./pages/HomePage.jsx";
import LoginPage from "./pages/LoginPage.jsx";
import MapPage from "./pages/MapPage.jsx";
import TeamPage from "./pages/TeamPage.jsx";
import ProfilePage from "./pages/ProfilePage.jsx";
import RegisterPage from "./pages/RegisterPage.jsx";

import GuardedRoute from "./components/GuardedRoute.jsx";
import Header from "./components/Header.jsx";

function App() {
  const { user } = useAuth();

  return (
    <>
      {/* On n’affiche le Header que si l’utilisateur est connecté */}
      {user && <Header />}

      <Routes>
        {/* Routes publiques */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        {/* Menu principal */}
        <Route
          path="/"
          element={
            <GuardedRoute>
              <HomePage />
            </GuardedRoute>
          }
        />

        {/* Carte */}
        <Route
          path="/map"
          element={
            <GuardedRoute>
              <MapPage />
            </GuardedRoute>
          }
        />

        {/* Équipe */}
        <Route
          path="/team"
          element={
            <GuardedRoute>
              <TeamPage />
            </GuardedRoute>
          }
        />

        {/* Profil */}
        <Route
          path="/profile"
          element={
            <GuardedRoute>
              <ProfilePage />
            </GuardedRoute>
          }
        />
      </Routes>
    </>
  );
}

export default App;