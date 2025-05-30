import React, { useState, useEffect } from "react";
import "../styles/components.css";

export default function FiltersModal({ filters, onChange, onClose }) {
  const [priceRange, setPriceRange] = useState({
    min: filters.minPrice || 0,
    max: filters.maxPrice || 10000
  });

  // State for rating range
  const [ratingRange, setRatingRange] = useState({
    min: filters.minRank || 1,
    max: filters.maxRank || 5
  });

  // Update price range when filters change
  useEffect(() => {
    setPriceRange({
      min: filters.minPrice || 0,
      max: filters.maxPrice || 10000
    });
  }, [filters.minPrice, filters.maxPrice]);

  // Update rating range when filters change
  useEffect(() => {
    setRatingRange({
      min: filters.minRank || 1,
      max: filters.maxRank || 5
    });
  }, [filters.minRank, filters.maxRank]);

  // Handle price slider changes
  const handlePriceChange = (type, value) => {
    const newValue = parseInt(value, 10);
    setPriceRange(prev => {
      const updated = { ...prev, [type]: newValue };

      // Ensure min doesn't exceed max
      if (type === 'min' && newValue > prev.max) {
        updated.max = newValue;
        onChange("maxPrice", newValue.toString());
      }

      // Ensure max doesn't go below min
      if (type === 'max' && newValue < prev.min) {
        updated.min = newValue;
        onChange("minPrice", newValue.toString());
      }

      onChange(type === 'min' ? "minPrice" : "maxPrice", newValue.toString());
      return updated;
    });
  };

  // Handle star rating selection
  const handleRatingSelect = (star) => {
    // If the clicked star is less than current min, set it as min
    // If the clicked star is greater than current max, set it as max
    // Otherwise, determine if it's closer to min or max and update accordingly
    if (star < ratingRange.min) {
      setRatingRange(prev => ({ ...prev, min: star }));
      onChange("minRank", star.toString());
    } else if (star > ratingRange.max) {
      setRatingRange(prev => ({ ...prev, max: star }));
      onChange("maxRank", star.toString());
    } else {
      // Determine if closer to min or max
      const distToMin = Math.abs(star - ratingRange.min);
      const distToMax = Math.abs(star - ratingRange.max);

      if (distToMin <= distToMax) {
        setRatingRange(prev => ({ ...prev, min: star }));
        onChange("minRank", star.toString());
      } else {
        setRatingRange(prev => ({ ...prev, max: star }));
        onChange("maxRank", star.toString());
      }
    }
  };

  // Render stars for the combined rating range
  const renderRatingStars = () => {
    const stars = [];
    for (let i = 1; i <= 5; i++) {
      let starClass = 'star-empty';

      // If star is within the selected range, fill it
      if (i >= ratingRange.min && i <= ratingRange.max) {
        starClass = 'star-filled';
      }

      stars.push(
        <button
          key={i}
          type="button"
          className={`star ${starClass}`}
          onClick={() => handleRatingSelect(i)}
        >
          ★
        </button>
      );
    }
    return stars;
  };

  return (
    <div className="modal-overlay">
      <div className="modal-content filter-modal">
        <div className="modal-header">
          <h3>Advanced Filters</h3>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>

        <div className="modal-body">
          <div className="filter-section">
            <label>Category:</label>
            <input 
              type="text" 
              className="filter-input"
              value={filters.category} 
              onChange={(e) => onChange("category", e.target.value)} 
            />
          </div>

          <div className="filter-section">
            <label>Price Range:</label>
            <div className="price-slider-container">
              <div className="price-display">
                <span>${priceRange.min}</span>
                <span>${priceRange.max}</span>
              </div>
              <div className="range-slider">
                <input 
                  type="range" 
                  min="0" 
                  max="10000"
                  value={priceRange.min} 
                  onChange={(e) => handlePriceChange('min', e.target.value)}
                  className="slider min-slider"
                />
                <input 
                  type="range" 
                  min="0" 
                  max="10000"
                  value={priceRange.max}
                  onChange={(e) => handlePriceChange('max', e.target.value)}
                  className="slider max-slider"
                />
              </div>
            </div>
          </div>

          <div className="filter-section">
            <label>Rating Range: {ratingRange.min} - {ratingRange.max} stars</label>
            <div className="star-rating">
              {renderRatingStars()}
            </div>
            <div className="rating-range-info">
              <small>Click stars to select rating range</small>

            </div>
          </div>
        </div>

        <div className="modal-footer">
          <button className="submit-btn" onClick={onClose}>Apply Filters</button>
        </div>
      </div>
    </div>
  );
}
