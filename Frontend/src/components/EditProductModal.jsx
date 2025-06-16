import React, { useState, useEffect } from "react";
import { useAuthContext } from "../context/AuthContext";
import "../index.css";

export default function EditProductModal({ product, onClose, onSubmit }) {
    const { user } = useAuthContext();
    const [productData, setProductData] = useState({
        id: "",
        name: "",
        price: "",
        quantity: "",
        description: "",
        category: "",
        imageUrl: ""
    });

    const [errors, setErrors] = useState({});
    const [isSubmitting, setIsSubmitting] = useState(false);

    // Initialize form with product data when component mounts
    useEffect(() => {
        if (product) {
            setProductData({
                id: product.productId,
                name: product.name || "",
                price: product.price ? product.price.toString() : "",
                quantity: product.quantity ? product.quantity.toString() : "",
                description: product.description || "",
                category: product.category || "",
                imageUrl: product.imageUrl || ""
            });
        }
    }, [product]);

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

        // Product ID validation
        if (!productData.id) {
            newErrors.id = "Product ID is required";
        }

        // Name validation
        if (!productData.name.trim()) {
            newErrors.name = "Product name is required";
        } else if (productData.name.trim().length < 2) {
            newErrors.name = "Product name must be at least 2 characters long";
        }

        // Price validation
        if (!productData.price) {
            newErrors.price = "Price is required";
        } else if (isNaN(productData.price) || parseFloat(productData.price) <= 0) {
            newErrors.price = "Price must be a positive number";
        } else if (parseFloat(productData.price) > 1000000) {
            newErrors.price = "Price cannot exceed $1,000,000";
        }

        // Quantity validation
        if (!productData.quantity) {
            newErrors.quantity = "Quantity is required";
        } else if (isNaN(productData.quantity) || parseInt(productData.quantity) < 0) {
            newErrors.quantity = "Quantity must be a non-negative number";
        } else if (parseInt(productData.quantity) > 10000) {
            newErrors.quantity = "Quantity cannot exceed 10,000";
        }

        // Category validation (optional but with format check)
        if (productData.category && productData.category.trim().length > 50) {
            newErrors.category = "Category cannot exceed 50 characters";
        }

        // Description validation (optional but with length check)
        if (productData.description && productData.description.trim().length > 500) {
            newErrors.description = "Description cannot exceed 500 characters";
        }

        // Image URL validation (optional but with format check)
        if (productData.imageUrl && !isValidUrl(productData.imageUrl)) {
            newErrors.imageUrl = "Please enter a valid URL";
        }

        return newErrors;
    };

    // Helper function to validate URLs
    const isValidUrl = (url) => {
        try {
            new URL(url);
            return true;
        } catch {
            return false;
        }
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
                id: productData.id,
                name: productData.name.trim(),
                price: parseFloat(productData.price),
                category: productData.category.trim(),
                description: productData.description.trim(),
                imageUrl: productData.imageUrl.trim(),
                quantity: parseInt(productData.quantity)
            };

            await onSubmit(formattedData);
            onClose();
        } catch (error) {
            console.error("Error updating product:", error);
            setErrors({ 
                form: error.message || "Failed to update product. Please check all fields and try again." 
            });
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal-container">
                <div className="modal-header">
                    <h2>Edit Product</h2>
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
                            min="0"
                            disabled={isSubmitting}
                        />
                        {errors.quantity && <div className="error-text">{errors.quantity}</div>}
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
                        {isSubmitting ? "Saving..." : "Save Changes"}
                    </button>
                </div>
            </div>
        </div>
    );
}