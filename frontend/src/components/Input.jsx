export default function Input({ label, type="text", value, onChange, error }) {
  return (
    <div className="input-group">
      <label>{label}</label>
      <input 
        type={type} 
        value={value}
        onChange={(e) => onChange(e.target.value)}
      />
      {error && <span className="error">{error}</span>}
    </div>
  );
}
