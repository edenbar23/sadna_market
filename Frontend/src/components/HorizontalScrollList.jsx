import React from "react";
import "../styles/components.css"; // Ensure horizontal-scroll styles live here

export default function HorizontalScrollList({ title, items, renderItem }) {
  return (
    <section className="scroll-section">
      <h2 className="section-title">{title}</h2>
      <div className="horizontal-scroll">
        {items.map((item, index) => (
          <div key={index} className="scroll-item">
            {renderItem(item)}
          </div>
        ))}
      </div>
    </section>
  );
}
