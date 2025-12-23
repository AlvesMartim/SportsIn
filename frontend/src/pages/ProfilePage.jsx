import { useAuth } from "../context/AuthContext.jsx";

function ProfilePage() {
  const { user } = useAuth();

  return (
    <div
      style={{
        width: "100vw",
        minHeight: "calc(100vh - 64px)", // évite le conflit avec le Header global
        backgroundColor: "#111",
        color: "white",
      }}
    >
      <main style={{ padding: "32px" }}>
        <h1>Profil</h1>

        {user ? (
          <>
            <p>
              <strong>Email :</strong> {user.email}
            </p>
            <p>
              <strong>Pseudo :</strong> {user.username}
            </p>
          </>
        ) : (
          <p>Aucun utilisateur connecté.</p>
        )}
      </main>
    </div>
  );
}

export default ProfilePage;