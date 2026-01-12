import { useState, useEffect } from 'react';
import { equipeAPI, joueurAPI, areneAPI } from '../api/api';
import '../styles/api-test.css';

export default function ApiTestPage() {
  const [equipes, setEquipes] = useState([]);
  const [joueurs, setJoueurs] = useState([]);
  const [arenes, setArenes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [newEquipeName, setNewEquipeName] = useState('');
  const [newJoueurName, setNewJoueurName] = useState('');

  // Charger les données au montage
  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    setError('');
    try {
      const [equipesData, joueursData, arenesData] = await Promise.all([
        equipeAPI.getAll(),
        joueurAPI.getAll(),
        areneAPI.getAll(),
      ]);
      setEquipes(equipesData);
      setJoueurs(joueursData);
      setArenes(arenesData);
    } catch (err) {
      setError(`Erreur lors du chargement: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateEquipe = async (e) => {
    e.preventDefault();
    if (!newEquipeName.trim()) return;

    try {
      const newEquipe = await equipeAPI.create({ nom: newEquipeName });
      setEquipes([...equipes, newEquipe]);
      setNewEquipeName('');
    } catch (err) {
      setError(`Erreur: ${err.message}`);
    }
  };

  const handleCreateJoueur = async (e) => {
    e.preventDefault();
    if (!newJoueurName.trim()) return;

    try {
      const newJoueur = await joueurAPI.create({ pseudo: newJoueurName });
      setJoueurs([...joueurs, newJoueur]);
      setNewJoueurName('');
    } catch (err) {
      setError(`Erreur: ${err.message}`);
    }
  };

  const handleDeleteEquipe = async (id) => {
    try {
      await equipeAPI.delete(id);
      setEquipes(equipes.filter((e) => e.id !== id));
    } catch (err) {
      setError(`Erreur: ${err.message}`);
    }
  };

  const handleDeleteJoueur = async (id) => {
    try {
      await joueurAPI.delete(id);
      setJoueurs(joueurs.filter((j) => j.id !== id));
    } catch (err) {
      setError(`Erreur: ${err.message}`);
    }
  };

  return (
    <div className="api-test-container">
      <h1>🧪 Test de Connexion API</h1>

      {error && <div className="error-message">{error}</div>}

      <button onClick={loadData} disabled={loading} className="btn-refresh">
        {loading ? '⏳ Chargement...' : '🔄 Rafraîchir'}
      </button>

      <div className="grid-container">
        {/* EQUIPES */}
        <div className="card">
          <h2>👥 Équipes ({equipes.length})</h2>

          <form onSubmit={handleCreateEquipe} className="form-group">
            <input
              type="text"
              placeholder="Nom de la nouvelle équipe"
              value={newEquipeName}
              onChange={(e) => setNewEquipeName(e.target.value)}
              className="input-field"
            />
            <button type="submit" className="btn-primary">
              ➕ Ajouter
            </button>
          </form>

          <div className="list">
            {equipes.length === 0 ? (
              <p className="empty">Aucune équipe</p>
            ) : (
              equipes.map((equipe) => (
                <div key={equipe.id} className="item">
                  <div>
                    <strong>{equipe.nom}</strong>
                    <br />
                    <small>ID: {equipe.id}</small>
                  </div>
                  <button
                    onClick={() => handleDeleteEquipe(equipe.id)}
                    className="btn-delete"
                  >
                    🗑️
                  </button>
                </div>
              ))
            )}
          </div>
        </div>

        {/* JOUEURS */}
        <div className="card">
          <h2>🎮 Joueurs ({joueurs.length})</h2>

          <form onSubmit={handleCreateJoueur} className="form-group">
            <input
              type="text"
              placeholder="Pseudo du nouveau joueur"
              value={newJoueurName}
              onChange={(e) => setNewJoueurName(e.target.value)}
              className="input-field"
            />
            <button type="submit" className="btn-primary">
              ➕ Ajouter
            </button>
          </form>

          <div className="list">
            {joueurs.length === 0 ? (
              <p className="empty">Aucun joueur</p>
            ) : (
              joueurs.map((joueur) => (
                <div key={joueur.id} className="item">
                  <div>
                    <strong>{joueur.pseudo}</strong>
                    <br />
                    <small>ID: {joueur.id}</small>
                  </div>
                  <button
                    onClick={() => handleDeleteJoueur(joueur.id)}
                    className="btn-delete"
                  >
                    🗑️
                  </button>
                </div>
              ))
            )}
          </div>
        </div>

        {/* ARENES */}
        <div className="card">
          <h2>🏟️ Arènes ({arenes.length})</h2>
          <div className="list">
            {arenes.length === 0 ? (
              <p className="empty">Aucune arène</p>
            ) : (
              arenes.map((arene) => (
                <div key={arene.id} className="item">
                  <div>
                    <strong>{arene.nom}</strong>
                    <br />
                    <small>
                      Lat: {arene.latitude}, Lon: {arene.longitude}
                    </small>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>

      <div className="status-info">
        <h3>📊 Résumé Connexion</h3>
        <ul>
          <li>✅ Backend: <code>http://localhost:8080</code></li>
          <li>✅ Frontend: <code>http://localhost:5173</code></li>
          <li>✅ Base de données: <code>SQLite (sportsin.db)</code></li>
          <li>📦 Équipes: {equipes.length}</li>
          <li>🎮 Joueurs: {joueurs.length}</li>
          <li>🏟️ Arènes: {arenes.length}</li>
        </ul>
      </div>
    </div>
  );
}
