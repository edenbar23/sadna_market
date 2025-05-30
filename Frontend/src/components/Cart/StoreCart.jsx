import React from "react";
import ProductRow from "./ProductRow";

export default function StoreCart({
                                      store,
                                      selectedProducts,
                                      onProductSelectionChange,
                                      onSelectAllStore,
                                      onQuantityChange,
                                      onRemoveProduct,
                                      onCheckoutStore
                                  }) {
    const { storeId, storeName, products, totalQuantity, totalPrice } = store;
    const productsArray = Object.values(products || {});

    if (productsArray.length === 0) {
        return null;
    }

    // Check if all products in this store are selected
    const allProductsSelected = productsArray.every(product =>
        selectedProducts.includes(product.productId)
    );

    // Check if some products are selected
    const someProductsSelected = productsArray.some(product =>
        selectedProducts.includes(product.productId)
    );

    // Calculate selected items stats for this store
    const selectedProductsInStore = productsArray.filter(product =>
        selectedProducts.includes(product.productId)
    );

    const selectedQuantity = selectedProductsInStore.reduce((sum, product) => sum + product.quantity, 0);
    const selectedPrice = selectedProductsInStore.reduce((sum, product) => sum + (product.price * product.quantity), 0);

    const handleSelectAllStore = (e) => {
        const productIds = productsArray.map(product => product.productId);
        onSelectAllStore(storeId, productIds, e.target.checked);
    };

    const handleQuantityChange = (productId, newQuantity) => {
        onQuantityChange(storeId, productId, newQuantity);
    };

    const handleRemoveProduct = (productId) => {
        onRemoveProduct(storeId, productId);
    };

    const handleCheckoutStore = () => {
        const storeProductIds = productsArray.map(product => product.productId);
        onCheckoutStore(storeId, storeProductIds);
    };

    return (
        <div className="store-cart">
            <div className="store-header">
                <div className="store-selection">
                    <input
                        type="checkbox"
                        checked={allProductsSelected}
                        ref={input => {
                            if (input) input.indeterminate = someProductsSelected && !allProductsSelected;
                        }}
                        onChange={handleSelectAllStore}
                        className="store-checkbox"
                    />
                    <h2 className="store-title">{storeName || `Store ${storeId.substring(0, 8)}...`}</h2>
                </div>

                <div className="store-stats">
                    <div className="store-totals">
                        <span className="total-items">{productsArray.length} products</span>
                        <span className="total-quantity">{totalQuantity} items</span>
                        <span className="total-price">${totalPrice.toFixed(2)}</span>
                    </div>

                    {selectedProductsInStore.length > 0 && (
                        <div className="selected-stats">
                            <span className="selected-label">Selected:</span>
                            <span className="selected-quantity">{selectedQuantity} items</span>
                            <span className="selected-price">${selectedPrice.toFixed(2)}</span>
                        </div>
                    )}
                </div>
            </div>

            <div className="products-container">
                {productsArray.map((product) => (
                    <ProductRow
                        key={product.productId}
                        product={product}
                        isSelected={selectedProducts.includes(product.productId)}
                        onSelectionChange={onProductSelectionChange}
                        onQuantityChange={(newQty) => handleQuantityChange(product.productId, newQty)}
                        onRemove={() => handleRemoveProduct(product.productId)}
                    />
                ))}
            </div>

            <div className="store-actions">
                <button
                    className="checkout-store-btn"
                    onClick={handleCheckoutStore}
                    disabled={productsArray.length === 0}
                >
                    Checkout All Store Items (${totalPrice.toFixed(2)})
                </button>

                {selectedProductsInStore.length > 0 && (
                    <button
                        className="checkout-selected-btn"
                        onClick={() => onCheckoutStore(storeId, selectedProductsInStore.map(p => p.productId))}
                    >
                        Checkout Selected ({selectedProductsInStore.length} items - ${selectedPrice.toFixed(2)})
                    </button>
                )}
            </div>
        </div>
    );
}