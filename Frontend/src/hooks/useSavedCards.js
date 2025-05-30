import { useState, useEffect, useCallback } from 'react';
import creditCardStorage from '../services/creditCardStorage';

export const useSavedCards = () => {
    const [savedCards, setSavedCards] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    // Load saved cards
    const loadSavedCards = useCallback(() => {
        try {
            setLoading(true);
            setError(null);
            const cards = creditCardStorage.getSavedCards();
            setSavedCards(cards);
        } catch (err) {
            setError(err.message);
            setSavedCards([]);
        } finally {
            setLoading(false);
        }
    }, []);

    // Save a new card
    const saveCard = useCallback(async (cardData, nickname = '') => {
        try {
            setLoading(true);
            setError(null);

            // Check for duplicates
            if (creditCardStorage.isDuplicateCard(cardData.cardNumber)) {
                throw new Error('A card with these last 4 digits is already saved');
            }

            const cardId = creditCardStorage.saveCard(cardData, nickname);

            // Reload cards to update the list
            loadSavedCards();

            return cardId;
        } catch (err) {
            setError(err.message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, [loadSavedCards]);

    // Get card for payment
    const getCardForPayment = useCallback((cardId) => {
        try {
            setError(null);
            return creditCardStorage.getCardForPayment(cardId);
        } catch (err) {
            setError(err.message);
            throw err;
        }
    }, []);

    // Delete a card
    const deleteCard = useCallback(async (cardId) => {
        try {
            setLoading(true);
            setError(null);

            creditCardStorage.deleteCard(cardId);

            // Reload cards to update the list
            loadSavedCards();

            return true;
        } catch (err) {
            setError(err.message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, [loadSavedCards]);

    // Update card nickname
    const updateCardNickname = useCallback(async (cardId, newNickname) => {
        try {
            setLoading(true);
            setError(null);

            creditCardStorage.updateCardNickname(cardId, newNickname);

            // Reload cards to update the list
            loadSavedCards();

            return true;
        } catch (err) {
            setError(err.message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, [loadSavedCards]);

    // Clear all cards
    const clearAllCards = useCallback(async () => {
        try {
            setLoading(true);
            setError(null);

            creditCardStorage.clearAllCards();
            setSavedCards([]);

            return true;
        } catch (err) {
            setError(err.message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    // Load cards on mount
    useEffect(() => {
        loadSavedCards();
    }, [loadSavedCards]);

    return {
        savedCards,
        loading,
        error,
        saveCard,
        getCardForPayment,
        deleteCard,
        updateCardNickname,
        clearAllCards,
        loadSavedCards
    };
};