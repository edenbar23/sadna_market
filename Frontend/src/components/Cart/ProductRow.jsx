import React from 'react';

export default function ProductRow({
                                       product,
                                       isSelected,
                                       onSelectionChange,
                                       onQuantityChange,
                                       onRemove
                                   }) {
    const handleDecrease = () => {
        if (product.quantity === 1) {
            const confirmRemove = window.confirm(
                `Are you sure you want to remove "${product.name}" from your cart?`
            );

            if (confirmRemove) {
                onRemove();
            }
        } else if (product.quantity > 1) {
            onQuantityChange(product.quantity - 1);
        }
    };

    const handleIncrease = () => {
        onQuantityChange(product.quantity + 1);
    };

    const handleSelectionChange = (e) => {
        onSelectionChange(product.productId, e.target.checked);
    };

    return (
        <div className={`product-row ${isSelected ? 'product-row-selected' : ''}`}>
            <div className="product-selection">
                <input
                    type="checkbox"
                    checked={isSelected}
                    onChange={handleSelectionChange}
                    className="product-checkbox"
                />
            </div>

            <div className="product-image-container">
                <img src={product.image} alt={product.name} className="product-img" />
            </div>

            <div className="product-details">
                <div className="product-info">
                    <h4 className="product-name">{product.name}</h4>
                    <p className="product-price">${product.price.toFixed(2)} each</p>
                    <p className="product-total">Total: ${(product.price * product.quantity).toFixed(2)}</p>
                </div>
            </div>

            <div className="product-controls">
                <div className="quantity-controls">
                    <button
                        onClick={handleDecrease}
                        className={`quantity-btn ${product.quantity === 1 ? "quantity-btn-warning" : ""}`}
                        title={product.quantity === 1 ? "Click to remove item" : "Decrease quantity"}
                    >
                        -
                    </button>
                    <span className="quantity-display">{product.quantity}</span>
                    <button
                        onClick={handleIncrease}
                        className="quantity-btn"
                        title="Increase quantity"
                    >
                        +
                    </button>
                </div>

                <button onClick={onRemove} className="remove-btn" title="Remove from cart">
                    <span className="remove-icon">🗑️</span>
                </button>
            </div>
        </div>
    );
}