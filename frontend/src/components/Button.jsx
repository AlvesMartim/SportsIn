import { useNavigate } from "react-router-dom";
import "../styles/button.css";

export default function Button({
  icon,
  buttonTitle,
  description,
  goTo,
  onClick,
  variant = "default",
  size = "default",
  disabled = false,
  loading = false,
  className = "",
}) {
  const navigate = useNavigate();

  const handleClick = () => {
    if (disabled || loading) return;
    if (onClick) {
      onClick();
    } else if (goTo) {
      navigate(goTo);
    }
  };

  const buttonClasses = [
    "menu-button",
    `menu-button--${variant}`,
    `menu-button--${size}`,
    disabled && "menu-button--disabled",
    loading && "menu-button--loading",
    className,
  ]
    .filter(Boolean)
    .join(" ");

  return (
    <button className={buttonClasses} onClick={handleClick} disabled={disabled || loading}>
      {loading ? (
        <div className="menu-button__loader">
          <div className="spinner" />
        </div>
      ) : (
        <>
          {icon && <div className="menu-button__icon">{icon}</div>}
          <div className="menu-button__content">
            <span className="menu-button__title">{buttonTitle}</span>
            {description && <span className="menu-button__description">{description}</span>}
          </div>
          <div className="menu-button__arrow">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M9 18l6-6-6-6" />
            </svg>
          </div>
        </>
      )}
    </button>
  );
}
