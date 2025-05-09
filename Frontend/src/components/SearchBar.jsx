import "../index.css"; // Import your CSS file here
export default function SearchBar() {
  return (
    <div className="search-bar-container">
      <input
        type="text"
        placeholder="Search products..."
        className="search-input"
      />
      <button className="button">Search</button>
      <button className="button">Filters</button>
    </div>
  );
}
