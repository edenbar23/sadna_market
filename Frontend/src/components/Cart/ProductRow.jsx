// ProductRow.jsx
export default function ProductRow({ product, onQuantityChange, onRemove }) {
  const handleDecrease = () => {
    if (product.quantity > 1) {
      onQuantityChange(product.quantity - 1); // Decrease quantity
    }
  };

  const handleIncrease = () => {
    onQuantityChange(product.quantity + 1); // Increase quantity
  };

  return (
    <div className="product-row">
      <img src={product.image} alt={product.name} className="product-img" />
      <div className="product-info">
        <h4>{product.name}</h4>
        <p>${product.price}</p>
      </div>
      <div className="quantity-controls">
        <button onClick={handleDecrease}>-</button>
        <span>{product.quantity}</span>
        <button onClick={handleIncrease}>+</button>
      </div>
      <button onClick={onRemove} className="remove-btn">Remove</button>
    </div>
  );
}
