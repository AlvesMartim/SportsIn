import { useState, useEffect, useRef, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";
import { equipeAPI, joueurAPI, areneAPI, progressionAPI, messageAPI } from "../api/api.js";
import Header from "../components/Header.jsx";
import "../styles/team.css";

function TeamPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [playerTeam, setPlayerTeam] = useState(null);
  const [teamMembers, setTeamMembers] = useState([]);
  const [controlledArenas, setControlledArenas] = useState([]);
  const [availableTeams, setAvailableTeams] = useState([]);

  const [progression, setProgression] = useState(null);

  const [showCreateForm, setShowCreateForm] = useState(false);
  const [newTeamName, setNewTeamName] = useState("");
  const [newTeamColor, setNewTeamColor] = useState("#3b82f6");
  const [creating, setCreating] = useState(false);
  const [joiningTeam, setJoiningTeam] = useState(null);

  // Chat state
  const [chatMessages, setChatMessages] = useState([]);
  const [chatInput, setChatInput] = useState("");
  const [sendingMessage, setSendingMessage] = useState(false);
  const [contextMenu, setContextMenu] = useState(null);
  const chatEndRef = useRef(null);
  const chatPollRef = useRef(null);

  const TEAM_COLORS = [
    { value: "#3b82f6", label: "Bleu" },
    { value: "#22c55e", label: "Vert" },
    { value: "#ef4444", label: "Rouge" },
    { value: "#f59e0b", label: "Orange" },
    { value: "#8b5cf6", label: "Violet" },
    { value: "#ec4899", label: "Rose" },
    { value: "#06b6d4", label: "Cyan" },
    { value: "#84cc16", label: "Lime" },
  ];

  useEffect(() => {
    loadData();
  }, [user]);

  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);

      const teams = await equipeAPI.getAll();
      const savedTeamId = sessionStorage.getItem("insport_team_id");

      if (savedTeamId) {
        try {
          const team = await equipeAPI.getById(savedTeamId);
          setPlayerTeam(team);

          const members = await joueurAPI.getByEquipe(savedTeamId);
          setTeamMembers(members);

          const arenas = await areneAPI.getByEquipe(savedTeamId);
          setControlledArenas(arenas);

          try {
            const prog = await progressionAPI.getProgression(savedTeamId);
            setProgression(prog);
          } catch (e) {
            console.warn('Progression non disponible:', e);
          }
        } catch (e) {
          sessionStorage.removeItem("insport_team_id");
          setAvailableTeams(teams);
        }
      } else {
        setAvailableTeams(teams);
      }
    } catch (err) {
      setError("Erreur lors du chargement des données");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleJoinTeam = async (teamId) => {
    try {
      setJoiningTeam(teamId);
      if (user?.id) {
        await equipeAPI.join(teamId, user.id);
      }
      sessionStorage.setItem("insport_team_id", teamId);
      await loadData();
    } catch (err) {
      setError("Erreur lors de la jonction à l'équipe");
      console.error(err);
    } finally {
      setJoiningTeam(null);
    }
  };

  const handleCreateTeam = async (e) => {
    e.preventDefault();
    if (!newTeamName.trim()) return;

    try {
      setCreating(true);
      const newTeam = await equipeAPI.create({
        nom: newTeamName.trim(),
        couleur: newTeamColor,
      });

      // Join the newly created team
      if (user?.id) {
        await equipeAPI.join(newTeam.id, user.id);
      }

      sessionStorage.setItem("insport_team_id", newTeam.id);
      setNewTeamName("");
      setShowCreateForm(false);
      await loadData();
    } catch (err) {
      setError("Erreur lors de la création de l'équipe");
      console.error(err);
    } finally {
      setCreating(false);
    }
  };

  const handleLeaveTeam = async () => {
    try {
      if (user?.id) {
        await equipeAPI.leave(user.id);
      }
    } catch (err) {
      console.error("Erreur lors du départ de l'équipe", err);
    }
    sessionStorage.removeItem("insport_team_id");
    setPlayerTeam(null);
    setTeamMembers([]);
    setControlledArenas([]);
    loadData();
  };

  // --- Chat functions ---
  const loadChatMessages = useCallback(async (teamId) => {
    try {
      const msgs = await messageAPI.getByEquipe(teamId);
      setChatMessages(msgs);
    } catch (err) {
      console.warn("Erreur chargement chat:", err);
    }
  }, []);

  const scrollToBottom = () => {
    chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [chatMessages]);

  // Poll chat messages every 3s when team is active
  useEffect(() => {
    const teamId = sessionStorage.getItem("insport_team_id");
    if (playerTeam && teamId) {
      loadChatMessages(teamId);
      chatPollRef.current = setInterval(() => loadChatMessages(teamId), 3000);
    }
    return () => {
      if (chatPollRef.current) clearInterval(chatPollRef.current);
    };
  }, [playerTeam, loadChatMessages]);

  const handleSendMessage = async (e) => {
    e.preventDefault();
    if (!chatInput.trim() || sendingMessage) return;
    const teamId = sessionStorage.getItem("insport_team_id");
    if (!user?.id || !teamId) return;

    try {
      setSendingMessage(true);
      await messageAPI.send(user.id, teamId, chatInput.trim());
      setChatInput("");
      await loadChatMessages(teamId);
    } catch (err) {
      console.error("Erreur envoi message:", err);
    } finally {
      setSendingMessage(false);
    }
  };

  const handleDeleteMessage = async (messageId) => {
    try {
      await messageAPI.delete(messageId);
      const teamId = sessionStorage.getItem("insport_team_id");
      if (teamId) await loadChatMessages(teamId);
    } catch (err) {
      console.error("Erreur suppression message:", err);
    }
    setContextMenu(null);
  };

  const handleContextMenu = (e, msg) => {
    e.preventDefault();
    setContextMenu({ x: e.clientX, y: e.clientY, messageId: msg.id });
  };

  // Close context menu on click anywhere
  useEffect(() => {
    const close = () => setContextMenu(null);
    if (contextMenu) {
      document.addEventListener("click", close);
      return () => document.removeEventListener("click", close);
    }
  }, [contextMenu]);

  if (loading) {
    return (
      <div className="team-page">
        <Header />
        <main className="team-content">
          <div className="team-loading">
            <div className="spinner" />
            <p>Chargement...</p>
          </div>
        </main>
      </div>
    );
  }

  return (
    <div className="team-page">
      <Header />

      <main className="team-content">
        <div className="team-header">
          <button className="btn btn-ghost" onClick={() => navigate("/")}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M15 18l-6-6 6-6" />
            </svg>
            Retour
          </button>
          <h1>Mon équipe</h1>
        </div>

        {error && (
          <div className="team-error animate-slideDown">
            <span>⚠️</span>
            <p>{error}</p>
            <button onClick={() => setError(null)}>×</button>
          </div>
        )}

        {playerTeam ? (
          <div className="team-layout">
            {/* Left column: team info */}
            <div className="team-view animate-fadeIn">
            {/* Info de l'équipe */}
            <div className="team-card team-info-card">
              <div className="team-info-header">
                <div
                  className="team-avatar"
                  style={{ background: `linear-gradient(135deg, ${playerTeam.couleur || "#3b82f6"} 0%, ${playerTeam.couleur || "#3b82f6"}dd 100%)` }}
                >
                  {playerTeam.nom?.charAt(0).toUpperCase()}
                </div>
                <div className="team-info-details">
                  <h2>{playerTeam.nom}</h2>
                  <span className="badge badge-success">Membre actif</span>
                </div>
              </div>

              <div className="team-stats">
                <div className="team-stat">
                  <span className="team-stat__value">{teamMembers.length}</span>
                  <span className="team-stat__label">Membres</span>
                </div>
                <div className="team-stat">
                  <span className="team-stat__value">{controlledArenas.length}</span>
                  <span className="team-stat__label">Arènes</span>
                </div>
                <div className="team-stat">
                  <span className="team-stat__value">{progression ? progression.level : '--'}</span>
                  <span className="team-stat__label">Niveau</span>
                </div>
                <div className="team-stat">
                  <span className="team-stat__value">{progression ? progression.xp : '--'}</span>
                  <span className="team-stat__label">XP</span>
                </div>
              </div>
            </div>

            {/* Membres */}
            <div className="team-card">
              <div className="team-card__header">
                <h3>Membres de l'équipe</h3>
                <span className="badge badge-primary">{teamMembers.length}</span>
              </div>

              {teamMembers.length > 0 ? (
                <ul className="team-members-list">
                  {teamMembers.map((member) => (
                    <li key={member.id} className="team-member">
                      <div className="team-member__avatar">
                        {member.pseudo?.charAt(0).toUpperCase() || "?"}
                      </div>
                      <div className="team-member__info">
                        <span className="team-member__name">{member.pseudo || member.email}</span>
                        <span className="team-member__role">Joueur</span>
                      </div>
                    </li>
                  ))}
                </ul>
              ) : (
                <p className="team-empty">Aucun membre enregistré</p>
              )}
            </div>

            {/* Arènes */}
            {controlledArenas.length > 0 && (
              <div className="team-card">
                <div className="team-card__header">
                  <h3>Territoires contrôlés</h3>
                  <span className="badge badge-success">{controlledArenas.length}</span>
                </div>

                <ul className="team-arenas-list">
                  {controlledArenas.map((arena) => (
                    <li key={arena.id} className="team-arena">
                      <div className="team-arena__icon">📍</div>
                      <div className="team-arena__info">
                        <span className="team-arena__name">{arena.nom || `Arène ${arena.id}`}</span>
                        <span className="team-arena__sports">
                          {arena.sportsDisponibles?.join(", ") || "Multi-sports"}
                        </span>
                      </div>
                    </li>
                  ))}
                </ul>
              </div>
            )}

            {/* Progression */}
            <div className="team-card">
              <div className="team-card__header">
                <h3>Progression</h3>
                {progression && <span className="badge badge-primary">Niv. {progression.level}</span>}
              </div>
              {progression ? (
                <div style={{ marginBottom: 16 }}>
                  <div style={{ display: "flex", justifyContent: "space-between", fontSize: "0.85rem", color: "var(--gray-400)", marginBottom: 6 }}>
                    <span>{progression.xp} XP</span>
                    {progression.level < (progression.maxLevel || 10) ? (
                      <span>{progression.xpForNextLevel} XP avant niv. {progression.level + 1}</span>
                    ) : (
                      <span>Niveau max 🏆</span>
                    )}
                  </div>
                  <div style={{ width: "100%", height: 10, background: "rgba(255,255,255,0.08)", borderRadius: 999, overflow: "hidden" }}>
                    <div style={{
                      height: "100%",
                      borderRadius: 999,
                      background: "linear-gradient(90deg, #8b5cf6, #a78bfa)",
                      width: progression.level >= (progression.maxLevel || 10) ? "100%" : "50%",
                      transition: "width 0.6s ease",
                    }} />
                  </div>
                </div>
              ) : (
                <p style={{ color: "var(--gray-500)", textAlign: "center" }}>Chargement...</p>
              )}
              <button className="btn btn-secondary w-full" onClick={() => navigate("/progression")}>
                📈 Voir la progression et les perks
              </button>
            </div>

            <button className="btn btn-danger w-full" onClick={handleLeaveTeam}>
              Quitter l'équipe
            </button>
          </div>

            {/* Right column: Team Chat */}
            <div className="team-chat-column animate-fadeIn">
              <div className="team-card team-chat-card">
                <div className="team-card__header">
                  <h3>💬 Chat d'équipe</h3>
                  <span className="badge badge-primary">{chatMessages.length}</span>
                </div>

                <div className="team-chat-messages">
                  {chatMessages.length === 0 ? (
                    <div className="team-chat-empty">
                      <span>🗨️</span>
                      <p>Aucun message pour le moment</p>
                      <span className="text-muted">Soyez le premier à écrire !</span>
                    </div>
                  ) : (
                    chatMessages.map((msg) => {
                      const isOwn = msg.auteur?.id === user?.id;
                      return (
                        <div
                          key={msg.id}
                          className={`team-chat-msg ${isOwn ? "team-chat-msg--own" : ""}`}
                          onContextMenu={(e) => handleContextMenu(e, msg)}
                        >
                          {!isOwn && (
                            <div className="team-chat-msg__avatar">
                              {msg.auteur?.pseudo?.charAt(0).toUpperCase() || "?"}
                            </div>
                          )}
                          <div className="team-chat-msg__bubble">
                            {!isOwn && (
                              <span className="team-chat-msg__author">
                                {msg.auteur?.pseudo || "Inconnu"}
                              </span>
                            )}
                            <p className="team-chat-msg__text">{msg.contenu}</p>
                            <span className="team-chat-msg__time">
                              {msg.envoyeA
                                ? new Date(msg.envoyeA).toLocaleTimeString("fr-FR", {
                                    hour: "2-digit",
                                    minute: "2-digit",
                                  })
                                : ""}
                            </span>
                          </div>
                        </div>
                      );
                    })
                  )}
                  <div ref={chatEndRef} />
                </div>

                <form className="team-chat-input" onSubmit={handleSendMessage}>
                  <input
                    type="text"
                    className="input"
                    placeholder="Écrire un message..."
                    value={chatInput}
                    onChange={(e) => setChatInput(e.target.value)}
                    maxLength={500}
                  />
                  <button
                    type="submit"
                    className="btn btn-primary"
                    disabled={sendingMessage || !chatInput.trim()}
                  >
                    {sendingMessage ? (
                      <div className="spinner" style={{ width: 16, height: 16 }} />
                    ) : (
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M22 2L11 13" />
                        <path d="M22 2L15 22L11 13L2 9L22 2Z" />
                      </svg>
                    )}
                  </button>
                </form>
              </div>
            </div>

            {/* Context Menu */}
            {contextMenu && (
              <div
                className="team-chat-context-menu"
                style={{ top: contextMenu.y, left: contextMenu.x }}
              >
                <button
                  onClick={() => handleDeleteMessage(contextMenu.messageId)}
                >
                  🗑️ Supprimer le message pour tout le monde
                </button>
              </div>
            )}
          </div>
        ) : (
          <div className="team-join-view animate-fadeIn">
            {/* Rejoindre */}
            <div className="team-card">
              <div className="team-card__header">
                <h3>Équipes disponibles</h3>
                <span className="text-muted">{availableTeams.length} équipes</span>
              </div>

              {availableTeams.length > 0 ? (
                <ul className="team-available-list stagger">
                  {availableTeams.map((team) => (
                    <li key={team.id} className="team-available-item">
                      <div
                        className="team-available-item__avatar"
                        style={{ background: `linear-gradient(135deg, ${team.couleur || "#3b82f6"} 0%, ${team.couleur || "#3b82f6"}dd 100%)` }}
                      >
                        {team.nom?.charAt(0).toUpperCase()}
                      </div>
                      <div className="team-available-item__info">
                        <span className="team-available-item__name">{team.nom}</span>
                        <span className="team-available-item__members">
                          {team.membres?.length || 0} membres
                        </span>
                      </div>
                      <button
                        className="btn btn-primary btn-sm"
                        onClick={() => handleJoinTeam(team.id)}
                        disabled={joiningTeam === team.id}
                      >
                        {joiningTeam === team.id ? (
                          <div className="spinner" style={{ width: 16, height: 16 }} />
                        ) : (
                          "Rejoindre"
                        )}
                      </button>
                    </li>
                  ))}
                </ul>
              ) : (
                <div className="team-empty-state">
                  <span className="team-empty-state__icon">👥</span>
                  <p>Aucune équipe disponible</p>
                  <span className="text-muted">Sois le premier à en créer une !</span>
                </div>
              )}
            </div>

            {/* Créer */}
            <div className="team-card">
              <div className="team-card__header">
                <h3>Créer une équipe</h3>
              </div>

              {!showCreateForm ? (
                <button
                  className="btn btn-secondary w-full"
                  onClick={() => setShowCreateForm(true)}
                >
                  <span>+</span> Créer une nouvelle équipe
                </button>
              ) : (
                <form onSubmit={handleCreateTeam} className="team-create-form">
                  <div className="input-group">
                    <label className="input-label">Nom de l'équipe</label>
                    <input
                      type="text"
                      className="input"
                      placeholder="Ex: Les Champions"
                      value={newTeamName}
                      onChange={(e) => setNewTeamName(e.target.value)}
                      required
                      autoFocus
                    />
                  </div>

                  <div className="input-group">
                    <label className="input-label">Couleur de l'équipe</label>
                    <div className="team-color-picker">
                      {TEAM_COLORS.map((color) => (
                        <button
                          key={color.value}
                          type="button"
                          className={`team-color-option ${newTeamColor === color.value ? "active" : ""}`}
                          style={{ background: color.value }}
                          onClick={() => setNewTeamColor(color.value)}
                          title={color.label}
                        />
                      ))}
                    </div>
                  </div>

                  <div className="team-create-actions">
                    <button
                      type="submit"
                      className="btn btn-success"
                      disabled={creating || !newTeamName.trim()}
                    >
                      {creating ? "Création..." : "Créer l'équipe"}
                    </button>
                    <button
                      type="button"
                      className="btn btn-ghost"
                      onClick={() => {
                        setShowCreateForm(false);
                        setNewTeamName("");
                      }}
                    >
                      Annuler
                    </button>
                  </div>
                </form>
              )}
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

export default TeamPage;
