import React from "react";
import HorizontalScrollList from "./HorizontalScrollList";
import ProductCard from "./ProductCard";

const mockProducts = [
  { id: "p1", name: "Shoes", store: "Nike", rating: 4.5, price: 120, image: "/assets/blank_product.png" },
  { id: "p2", name: "Laptop", store: "TechHub", rating: 4.7, price: 899, image: "/assets/blank_product.png" },
  { id: "p3", name: "Smartphone", store: "GadgetWorld", rating: 4.6, price: 699, image: "/assets/blank_product.png" },
  { id: "p4", name: "Headphones", store: "AudioMax", rating: 4.8, price: 199, image: "/assets/blank_product.png" },
  { id: "p5", name: "Watch", store: "Timepiece", rating: 4.4, price: 299, image: "/assets/blank_product.png" },
  { id: "p6", name: "Backpack", store: "TravelGear", rating: 4.3, price: 89, image: "/assets/blank_product.png" },
  { id: "p7", name: "Sunglasses", store: "Visionary", rating: 4.5, price: 149, image: "/assets/blank_product.png" },
  { id: "p8", name: "Camera", store: "PhotoPro", rating: 4.9, price: 1299, image: "/assets/blank_product.png" },
  { id: "p9", name: "Smartwatch", store: "WearableTech", rating: 4.6, price: 349, image: "/assets/blank_product.png" },
  { id: "p10", name: "Tablet", store: "GadgetHub", rating: 4.7, price: 499, image: "/assets/blank_product.png" }
  // More mock items...
];

export default function TopProducts() {
  return (
    <HorizontalScrollList
      title="Top Products"
      items={mockProducts}
      renderItem={(product) => <ProductCard product={product} />}
    />
  );
}
