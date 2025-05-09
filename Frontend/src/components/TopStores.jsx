import React from "react";
import HorizontalScrollList from "./HorizontalScrollList";
import StoreCard from "./StoreCard";

const mockStores = [
  { name: "Nike", rating: 4.8, logo: "/assets/blank_store.png" },
  { name: "TechHub", rating: 4.6, logo: "/assets/blank_store.png" },
  { name: "GadgetWorld", rating: 4.7, logo: "/assets/blank_store.png" },
  { name: "AudioMax", rating: 4.5, logo: "/assets/blank_store.png" },
  { name: "Timepiece", rating: 4.9, logo: "/assets/blank_store.png" },
  { name: "TravelGear", rating: 4.4, logo: "/assets/blank_store.png" },
  { name: "Visionary", rating: 4.6, logo: "/assets/blank_store.png" },
  { name: "PhotoPro", rating: 4.8, logo: "/assets/blank_store.png" },
  { name: "WearableTech", rating: 4.7, logo: "/assets/blank_store.png" },
  { name: "GadgetHub", rating: 4.5, logo: "/assets/blank_store.png" },
  { name: "Fashionista", rating: 4.6, logo: "/assets/blank_store.png" },
  { name: "HomeEssentials", rating: 4.7, logo: "/assets/blank_store.png" },
  { name: "SportyStyle", rating: 4.8, logo: "/assets/blank_store.png" },
  { name: "ElectroWorld", rating: 4.9, logo: "/assets/blank_store.png" },
  { name: "BeautyBliss", rating: 4.5, logo: "/assets/blank_store.png" },
  // More mock stores...
];

export default function TopStores() {
  return (
    <HorizontalScrollList
      title="Top Stores"
      items={mockStores}
      renderItem={(store) => <StoreCard store={store} />}
    />
  );
}
