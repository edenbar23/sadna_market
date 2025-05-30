// src/components/SavedCardsManager.jsx
import React, { useState } from 'react';
import { useSavedCards } from '../hooks/useSavedCards';

export default function SavedCardsManager() {
    const {
        savedCards,
        loading,
        error,
        deleteCard,
        updateCardNickname,
        clearAllCards
    } = useSavedCards();

    const [editingCard, setEditingCard] = useState('');
    const [newNickname, setNewNickname] = useState('');

    const handleEditNickname = (card) => {
        setEditingCard(card.id);
        setNewNickname(card.nickname);
    };

    const handleSaveNickname = async (cardId) => {
        if (!newNickname.trim()) {
            setEditingCard('');
            return;
        }

        try {
            await updateCardNickname(cardId, newNickname.trim());
            setEditingCard('');
            setNewNickname('');
        } catch (error) {
            alert('Failed to update nickname: ' + error.message);
        }
    };

    const handleCancelEdit = () => {
        setEditingCard('');
        setNewNickname('');
    };

    const handleDeleteCard = async (cardId) => {
        if (!confirm('Are you sure you want to delete this payment method? This action cannot be undone.')) {
            return;
        }

        try {
            await deleteCard(cardId);
        } catch (error) {
            alert('Failed to delete payment method: ' + error.message);
        }
    };

    const handleClearAllCards = async () => {
        if (!confirm('Are you sure you want to delete ALL saved payment methods? This action cannot be undone.')) {
            return;
        }

        try {
            await clearAllCards();
            alert('All payment methods have been cleared.');
        } catch (error) {
            alert('Failed to clear payment methods: ' + error.message);
        }
    };

    const formatSavedDate = (dateString) => {
        try {
            return new Date(dateString).toLocaleDateString('en-US', {
                year: 'numeric',
                month: 'short',
                day: 'numeric'
            });
        } catch {
            return 'Unknown';
        }
    };

    if (loading) {
        return (
            <div className="saved-cards-manager">
                <div className="loading-indicator">Loading saved payment methods...</div>
            </div>
        );
    }

    return (
        <div className="saved-cards-manager">
            <div className="manager-header">
                <h3>Saved Payment Methods</h3>
                {savedCards.length > 0 && (
                    <button
                        onClick={handleClearAllCards}
                        className="btn btn-danger btn-small"
                    >
                        Clear All
                    </button>
                )}
            </div>

            {error && (
                <div className="error-message">
                    Error: {error}
                </div>
            )}

            {savedCards.length === 0 ? (
                <div className="no-cards-message">
                    <p>No saved payment methods yet.</p>
                    <p>Add a payment method during checkout to save it for future use.</p>
                </div>
            ) : (
                <div className="saved-cards-list">
                    {savedCards.map(card => (
                        <div key={card.id} className="saved-card-item">
                            <div className="card-main-info">
                                <div className="card-nickname-section">
                                    {editingCard === card.id ? (
                                        <div className="nickname-edit-section">
                                            <input
                                                type="text"
                                                value={newNickname}
                                                onChange={(e) => setNewNickname(e.target.value)}
                                                onKeyDown={(e) => {
                                                    if (e.key === 'Enter') handleSaveNickname(card.id);
                                                    if (e.key === 'Escape') handleCancelEdit();
                                                }}
                                                className="nickname-edit-input"
                                                autoFocus
                                            />
                                            <div className="nickname-edit-actions">
                                                <button
                                                    onClick={() => handleSaveNickname(card.id)}
                                                    className="btn btn-success btn-tiny"
                                                    disabled={!newNickname.trim()}
                                                >
                                                    ✓
                                                </button>
                                                <button
                                                    onClick={handleCancelEdit}
                                                    className="btn btn-secondary btn-tiny"
                                                >
                                                    ✕
                                                </button>
                                            </div>
                                        </div>
                                    ) : (
                                        <div className="nickname-display-section">
                                            <h4 className="card-nickname">{card.nickname}</h4>
                                            <button
                                                onClick={() => handleEditNickname(card)}
                                                className="btn-edit-nickname"
                                                title="Edit nickname"
                                            >
                                                ✏️
                                            </button>
                                        </div>
                                    )}
                                </div>

                                <div className="card-details-section">
                                    <div className="card-number-display">{card.maskedNumber}</div>
                                    <div className="card-meta">
                                        <span className="card-expiry">Expires {card.expiryDate}</span>
                                        <span className="card-holder-name">{card.cardHolder}</span>
                                        <span className="card-saved-date">Saved {formatSavedDate(card.savedAt)}</span>
                                    </div>
                                </div>
                            </div>

                            <div className="card-actions-section">
                                <button
                                    onClick={() => handleDeleteCard(card.id)}
                                    className="btn btn-danger btn-small"
                                    title="Delete this payment method"
                                >
                                    Delete
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            <div className="security-notice">
                <div className="security-icon">🔒</div>
                <div className="security-text">
                    <strong>Security Notice:</strong> Your payment information is encrypted and stored locally on your device.
                    This data is not transmitted to our servers and will be lost if you clear your browser data.
                </div>
            </div>
        </div>
    );
}