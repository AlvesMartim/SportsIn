import { useNavigate } from "react-router-dom";

const menuButtonStyle = {
  minWidth: "220px",
  padding: "16px 20px",
  borderRadius: "16px",
  border: "1px solid #333",
  background:
    "radial-gradient(circle at top left, #1e88e5 0, #111 40%, #111 100%)",
  color: "white",
  cursor: "pointer",
  display: "flex",
  flexDirection: "column",
  alignItems: "flex-start",
  gap: "6px",
  fontSize: "16px",
  fontWeight: 600,
};

const menuButtonSmall = {
  fontSize: "12px",
  opacity: 0.8,
  marginTop: "2px",
};

export default function Button({ icon, buttonTitle, description, goTo }) {
  const navigate = useNavigate();

  const handleClick = () => {
    if (goTo) {
      navigate(goTo);
    }
  };

  return (
    <button style={menuButtonStyle} onClick={handleClick}>
      <div style={{ fontSize: "22px" }}>{icon}</div>
      <span>{buttonTitle}</span>
      {description && <small style={menuButtonSmall}>{description}</small>}
    </button>
  );
}