import SearchBar from "@/components/SearchBar";
import TopProducts from "@/components/TopProducts";
import TopStores from "@/components/TopStores";
import "../index.css"; // Ensure styles are loaded

export default function MainPage() {
  return (
    <div className="bg-gray-50"> {/* Optional: you can make this a utility class in your own CSS */}
      <main className="container">
        <SearchBar />

        {/* Top Products */}
        <div className="section">
          <TopProducts />
        </div>

        {/* Top Stores */}
        <div className="section">
          <TopStores />
        </div>
      </main>
    </div>
  );
}
