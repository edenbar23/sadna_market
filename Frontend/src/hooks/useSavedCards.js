import { useState, useEffect, useCallback } from 'react';
import creditCardStorage from '../services/creditCardStorage';
import { useAuthContext } from '../context/AuthContext';

export const useSavedCards = () => {
    const [savedCards, setSavedCards] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    // FIXED: Get current user from auth context
    const { user } = useAuthContext();
    const username = user?.username;

    // FIXED: Load saved cards for current user only
    const loadSavedCards = useCallback(() => {
        try {
            setLoading(true);
            setError(null);

            // FIXED: Only load cards if user is logged in
            if (!username) {
                console.log('No user logged in, clearing saved cards');
                setSavedCards([]);
                return;
            }

            console.log(`Loading saved cards for user: ${username}`);
            const cards = creditCardStorage.getSavedCards(username);
            setSavedCards(cards);
            console.log(`Loaded ${cards.length} cards for user: ${username}`);
        } catch (err) {
            console.error('Error loading saved cards:', err);
            setError(err.message);
            setSavedCards([]);
        } finally {
            setLoading(false);
        }
    }, [username]);

    // FIXED: Save a new card for current user
    const saveCard = useCallback(async (cardData, nickname = '') => {
        try {
            if (!username) {
                throw new Error('You must be logged in to save payment methods');
            }

            setLoading(true);
            setError(null);

            console.log(`Saving card for user: ${username}`);

            // Check for duplicates for this specific user
            if (creditCardStorage.isDuplicateCard(cardData.cardNumber, username)) {
                throw new Error('A card with these last 4 digits is already saved');
            }

            const cardId = creditCardStorage.saveCard(cardData, nickname, username);
            console.log(`Card saved successfully for user: ${username}, cardId: ${cardId}`);

            // Reload cards to update the list
            loadSavedCards();

            return cardId;
        } catch (err) {
            console.error('Error saving card:', err);
            setError(err.message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, [username, loadSavedCards]);

    // FIXED: Get card for payment for current user
    const getCardForPayment = useCallback((cardId) => {
        try {
            if (!username) {
                throw new Error('You must be logged in to access payment methods');
            }

            setError(null);
            console.log(`Getting card for payment - user: ${username}, cardId: ${cardId}`);
            return creditCardStorage.getCardForPayment(cardId, username);
        } catch (err) {
            console.error('Error getting card for payment:', err);
            setError(err.message);
            throw err;
        }
    }, [username]);

    // FIXED: Delete a card for current user
    const deleteCard = useCallback(async (cardId) => {
        try {
            if (!username) {
                throw new Error('You must be logged in to delete payment methods');
            }

            setLoading(true);
            setError(null);

            console.log(`Deleting card for user: ${username}, cardId: ${cardId}`);
            creditCardStorage.deleteCard(cardId, username);

            // Reload cards to update the list
            loadSavedCards();

            return true;
        } catch (err) {
            console.error('Error deleting card:', err);
            setError(err.message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, [username, loadSavedCards]);

    // FIXED: Update card nickname for current user
    const updateCardNickname = useCallback(async (cardId, newNickname) => {
        try {
            if (!username) {
                throw new Error('You must be logged in to update payment methods');
            }

            setLoading(true);
            setError(null);

            console.log(`Updating card nickname for user: ${username}, cardId: ${cardId}`);
            creditCardStorage.updateCardNickname(cardId, newNickname, username);

            // Reload cards to update the list
            loadSavedCards();

            return true;
        } catch (err) {
            console.error('Error updating card nickname:', err);
            setError(err.message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, [username, loadSavedCards]);

    // FIXED: Clear all cards for current user
    const clearAllCards = useCallback(async () => {
        try {
            if (!username) {
                console.log('No user logged in, nothing to clear');
                setSavedCards([]);
                return true;
            }

            setLoading(true);
            setError(null);

            console.log(`Clearing all cards for user: ${username}`);
            creditCardStorage.clearAllCards(username);
            setSavedCards([]);

            return true;
        } catch (err) {
            console.error('Error clearing cards:', err);
            setError(err.message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, [username]);

    // FIXED: Load cards when user changes or component mounts
    useEffect(() => {
        if (username) {
            console.log(`User changed to: ${username}, loading their cards`);
            loadSavedCards();
        } else {
            console.log('User logged out, clearing cards from state');
            setSavedCards([]);
            setError(null);
        }
    }, [username, loadSavedCards]);

    // FIXED: Clear cards when user logs out (security measure)
    useEffect(() => {
        // This effect runs when the component unmounts or user changes
        return () => {
            if (!user && username) {
                // User has logged out, clear their payment data from localStorage for security
                console.log(`Clearing payment data for logged out user: ${username}`);
                creditCardStorage.clearCardsOnLogout(username);
            }
        };
    }, [user, username]);

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