import React, { useState } from "react";
import "../index.css";

export default function AddProductModal({ storeId, onClose, onSubmit }) {
    const [productData, setProductData] = useState({
        name: "",
        price: "",
        category: "",
        description: "",
        imageUrl: "",
        quantity: "1"
    });

    const [errors, setErrors] = useState({});
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setProductData({
            ...productData,
            [name]: value
        });

        // Clear error for this field if it exists
        if (errors[name]) {
            setErrors({
                ...errors,
                [name]: ""
            });
        }
    };

    const validateForm = () => {
        const newErrors = {};

        if (!productData.name.trim()) {
            newErrors.name = "Product name is required";
        }

        if (!productData.price || isNaN(productData.price) || parseFloat(productData.price) <= 0) {
            newErrors.price = "Valid price is required";
        }

        if (!productData.quantity || isNaN(productData.quantity) || parseInt(productData.quantity) < 0) {
            newErrors.quantity = "Valid quantity is required";
        }

        return newErrors;
    };

    const handleSubmit = async () => {
        const formErrors = validateForm();

        if (Object.keys(formErrors).length > 0) {
            setErrors(formErrors);
            return;
        }

        setIsSubmitting(true);

        try {
            // Format the data properly for the backend
            const formattedData = {
                name: productData.name,
                price: parseFloat(productData.price),
                category: productData.category,
                description: productData.description,
                imageUrl: productData.imageUrl,
                // We don't include a productId when creating a new product
            };

            // Make sure storeId is a valid UUID string before submitting
            if (!storeId || storeId === "undefined") {
                throw new Error("Invalid store ID");
            }

            await onSubmit(formattedData, parseInt(productData.quantity));
        } catch (error) {
            console.error("Error adding product:", error);
            setErrors({ form: "Failed to add product. Please try again." });
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal-container">
                <div className="modal-header">
                    <h2>Add New Product</h2>
                    <button className="close-modal-btn" onClick={onClose}>×</button>
                </div>

                <div className="modal-body">
                    {errors.form && <div className="error-text">{errors.form}</div>}

                    <div className="form-group">
                        <label htmlFor="name">Product Name*</label>
                        <input
                            type="text"
                            id="name"
                            name="name"
                            value={productData.name}
                            onChange={handleChange}
                            placeholder="Enter product name"
                            disabled={isSubmitting}
                        />
                        {errors.name && <div className="error-text">{errors.name}</div>}
                    </div>

                    <div className="form-group">
                        <label htmlFor="price">Price*</label>
                        <input
                            type="number"
                            id="price"
                            name="price"
                            value={productData.price}
                            onChange={handleChange}
                            placeholder="Enter price"
                            step="0.01"
                            min="0.01"
                            disabled={isSubmitting}
                        />
                        {errors.price && <div className="error-text">{errors.price}</div>}
                    </div>

                    <div className="form-group">
                        <label htmlFor="quantity">Quantity*</label>
                        <input
                            type="number"
                            id="quantity"
                            name="quantity"
                            value={productData.quantity}
                            onChange={handleChange}
                            placeholder="Enter quantity"
                            min="1"
                            disabled={isSubmitting}
                        />
                        {errors.quantity && <div className="error-text">{errors.quantity}</div>}
                    </div>

                    <div className="form-group">
                        <label htmlFor="category">Category</label>
                        <input
                            type="text"
                            id="category"
                            name="category"
                            value={productData.category}
                            onChange={handleChange}
                            placeholder="Enter product category"
                            disabled={isSubmitting}
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="description">Description</label>
                        <textarea
                            id="description"
                            name="description"
                            value={productData.description}
                            onChange={handleChange}
                            placeholder="Enter product description"
                            rows={3}
                            disabled={isSubmitting}
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="imageUrl">Image URL</label>
                        <input
                            type="text"
                            id="imageUrl"
                            name="imageUrl"
                            value={productData.imageUrl}
                            onChange={handleChange}
                            placeholder="Enter image URL"
                            disabled={isSubmitting}
                        />
                    </div>
                </div>

                <div className="modal-footer">
                    <button
                        className="btn-cancel"
                        onClick={onClose}
                        disabled={isSubmitting}
                    >
                        Cancel
                    </button>
                    <button
                        className="btn-submit"
                        onClick={handleSubmit}
                        disabled={isSubmitting}
                    >
                        {isSubmitting ? "Adding..." : "Add Product"}
                    </button>
                </div>
            </div>
        </div>
    );
}