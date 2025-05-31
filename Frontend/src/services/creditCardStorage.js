import CryptoJS from 'crypto-js';

class CreditCardStorage {
    constructor() {
        this.encryptionKey = this.getOrCreateEncryptionKey();
        // FIXED: Base storage key - will be combined with username
        this.baseStorageKey = 'encrypted_payment_methods';
    }

    /**
     * FIXED: Get user-specific storage key
     * @param {string} username - Current user's username
     * @returns {string} - User-specific storage key
     */
    getUserStorageKey(username) {
        if (!username) {
            throw new Error('Username is required for payment storage');
        }
        // Create a hashed version of username for additional security
        const hashedUsername = CryptoJS.SHA256(username).toString().substring(0, 16);
        return `${this.baseStorageKey}_${hashedUsername}`;
    }

    /**
     * Generate or retrieve encryption key
     * This creates a unique key per browser/user
     */
    getOrCreateEncryptionKey() {
        let key = localStorage.getItem('payment_encryption_key');

        if (!key) {
            // Generate a new key based on browser fingerprint + timestamp
            const browserFingerprint = this.generateBrowserFingerprint();
            const timestamp = Date.now().toString();
            key = CryptoJS.SHA256(browserFingerprint + timestamp).toString();
            localStorage.setItem('payment_encryption_key', key);
        }

        return key;
    }

    /**
     * Generate a simple browser fingerprint
     * This helps make the encryption key unique per browser
     */
    generateBrowserFingerprint() {
        const navigator = window.navigator;
        const screen = window.screen;

        const fingerprint = [
            navigator.userAgent,
            navigator.language,
            screen.width + 'x' + screen.height,
            screen.colorDepth,
            new Date().getTimezoneOffset(),
            navigator.platform
        ].join('|');

        return CryptoJS.SHA256(fingerprint).toString();
    }

    /**
     * Encrypt sensitive data
     */
    encrypt(data) {
        try {
            const encrypted = CryptoJS.AES.encrypt(JSON.stringify(data), this.encryptionKey).toString();
            return encrypted;
        } catch (error) {
            console.error('Encryption failed:', error);
            throw new Error('Failed to encrypt payment data');
        }
    }

    /**
     * Decrypt sensitive data
     */
    decrypt(encryptedData) {
        try {
            const bytes = CryptoJS.AES.decrypt(encryptedData, this.encryptionKey);
            const decryptedData = bytes.toString(CryptoJS.enc.Utf8);
            return JSON.parse(decryptedData);
        } catch (error) {
            console.error('Decryption failed:', error);
            throw new Error('Failed to decrypt payment data');
        }
    }

    /**
     * Mask credit card number for display
     * Shows only last 4 digits
     */
    maskCardNumber(cardNumber) {
        if (!cardNumber || cardNumber.length < 4) return '****';
        const cleaned = cardNumber.replace(/\s/g, '');
        return '**** **** **** ' + cleaned.slice(-4);
    }

    /**
     * Generate a unique ID for each saved card
     */
    generateCardId() {
        return 'card_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
    }

    /**
     * FIXED: Save a credit card securely for a specific user
     * @param {Object} cardData - Credit card information
     * @param {string} cardData.cardNumber - Card number
     * @param {string} cardData.expiryDate - MM/YY format
     * @param {string} cardData.cvv - CVV code
     * @param {string} cardData.cardHolder - Cardholder name
     * @param {string} cardData.cardType - Card type (credit/debit)
     * @param {Object} cardData.billingAddress - Billing address
     * @param {string} nickname - Optional nickname for the card
     * @param {string} username - Current user's username
     */
    saveCard(cardData, nickname = '', username) {
        try {
            if (!username) {
                throw new Error('Username is required to save payment methods');
            }

            const userStorageKey = this.getUserStorageKey(username);
            const cards = this.getSavedCards(username);

            // Create card object with sensitive data
            const cardToSave = {
                id: this.generateCardId(),
                nickname: nickname || `Card ending in ${cardData.cardNumber.slice(-4)}`,
                cardType: cardData.cardType,
                lastFour: cardData.cardNumber.replace(/\s/g, '').slice(-4),
                expiryDate: cardData.expiryDate,
                cardHolder: cardData.cardHolder,
                billingAddress: cardData.billingAddress,
                savedAt: new Date().toISOString(),
                // FIXED: Include username in encrypted data for additional verification
                encryptedData: this.encrypt({
                    cardNumber: cardData.cardNumber,
                    cvv: cardData.cvv,
                    username: username // Additional security check
                })
            };

            // Add to saved cards
            cards.push(cardToSave);

            // Save to user-specific localStorage key
            localStorage.setItem(userStorageKey, JSON.stringify(cards));

            console.log(`Credit card saved successfully for user: ${username}`);
            return cardToSave.id;

        } catch (error) {
            console.error('Failed to save credit card:', error);
            throw new Error('Failed to save payment method');
        }
    }

    /**
     * FIXED: Get all saved cards for a specific user (with sensitive data encrypted)
     * Returns cards with masked numbers for display
     * @param {string} username - Current user's username
     */
    getSavedCards(username) {
        try {
            if (!username) {
                console.warn('No username provided, returning empty cards array');
                return [];
            }

            const userStorageKey = this.getUserStorageKey(username);
            const cardsData = localStorage.getItem(userStorageKey);

            if (!cardsData) return [];

            const cards = JSON.parse(cardsData);

            // Return cards with masked numbers for display
            return cards.map(card => ({
                ...card,
                maskedNumber: this.maskCardNumber('************' + card.lastFour),
                // Remove encrypted data from display object
                encryptedData: undefined
            }));

        } catch (error) {
            console.error('Failed to retrieve saved cards:', error);
            return [];
        }
    }

    /**
     * FIXED: Get a specific card with decrypted data for payment processing
     * @param {string} cardId - Card ID
     * @param {string} username - Current user's username
     * @returns {Object} Full card data with decrypted sensitive information
     */
    getCardForPayment(cardId, username) {
        try {
            if (!username) {
                throw new Error('Username is required to access payment methods');
            }

            const userStorageKey = this.getUserStorageKey(username);
            const cardsData = localStorage.getItem(userStorageKey);

            if (!cardsData) throw new Error('No saved cards found');

            const cards = JSON.parse(cardsData);
            const card = cards.find(c => c.id === cardId);

            if (!card) throw new Error('Card not found');

            // Decrypt sensitive data
            const decryptedData = this.decrypt(card.encryptedData);

            // FIXED: Verify that the decrypted username matches current user
            if (decryptedData.username !== username) {
                throw new Error('Unauthorized access to payment method');
            }

            // Return complete card data for payment
            return {
                id: card.id,
                cardNumber: decryptedData.cardNumber,
                expiryDate: card.expiryDate,
                cvv: decryptedData.cvv,
                cardHolder: card.cardHolder,
                cardType: card.cardType,
                billingAddress: card.billingAddress,
                nickname: card.nickname
            };

        } catch (error) {
            console.error('Failed to retrieve card for payment:', error);
            throw new Error('Failed to load payment method');
        }
    }

    /**
     * FIXED: Delete a saved card for a specific user
     * @param {string} cardId - Card ID to delete
     * @param {string} username - Current user's username
     */
    deleteCard(cardId, username) {
        try {
            if (!username) {
                throw new Error('Username is required to delete payment methods');
            }

            const userStorageKey = this.getUserStorageKey(username);
            const cardsData = localStorage.getItem(userStorageKey);

            if (!cardsData) return false;

            const cards = JSON.parse(cardsData);
            const filteredCards = cards.filter(card => card.id !== cardId);

            localStorage.setItem(userStorageKey, JSON.stringify(filteredCards));

            console.log(`Credit card deleted successfully for user: ${username}`);
            return true;

        } catch (error) {
            console.error('Failed to delete credit card:', error);
            throw new Error('Failed to delete payment method');
        }
    }

    /**
     * FIXED: Update card nickname for a specific user
     * @param {string} cardId - Card ID
     * @param {string} newNickname - New nickname
     * @param {string} username - Current user's username
     */
    updateCardNickname(cardId, newNickname, username) {
        try {
            if (!username) {
                throw new Error('Username is required to update payment methods');
            }

            const userStorageKey = this.getUserStorageKey(username);
            const cardsData = localStorage.getItem(userStorageKey);

            if (!cardsData) throw new Error('No saved cards found');

            const cards = JSON.parse(cardsData);
            const cardIndex = cards.findIndex(c => c.id === cardId);

            if (cardIndex === -1) throw new Error('Card not found');

            cards[cardIndex].nickname = newNickname;
            localStorage.setItem(userStorageKey, JSON.stringify(cards));

            return true;
        } catch (error) {
            console.error('Failed to update card nickname:', error);
            throw new Error('Failed to update payment method');
        }
    }

    /**
     * FIXED: Check if a card with the same last 4 digits already exists for a user
     * @param {string} cardNumber - Card number to check
     * @param {string} username - Current user's username
     */
    isDuplicateCard(cardNumber, username) {
        if (!username) return false;

        const lastFour = cardNumber.replace(/\s/g, '').slice(-4);
        const savedCards = this.getSavedCards(username);
        return savedCards.some(card => card.lastFour === lastFour);
    }

    /**
     * FIXED: Clear all saved cards for a specific user (useful for logout or security reasons)
     * @param {string} username - Current user's username (optional - if not provided, clears all)
     */
    clearAllCards(username = null) {
        try {
            if (username) {
                // Clear cards for specific user
                const userStorageKey = this.getUserStorageKey(username);
                localStorage.removeItem(userStorageKey);
                console.log(`All payment methods cleared for user: ${username}`);
            } else {
                // SECURITY: Only clear all if no username provided (emergency cleanup)
                // Find all payment-related keys and remove them
                const keysToRemove = [];
                for (let i = 0; i < localStorage.length; i++) {
                    const key = localStorage.key(i);
                    if (key && key.startsWith(this.baseStorageKey)) {
                        keysToRemove.push(key);
                    }
                }

                keysToRemove.forEach(key => localStorage.removeItem(key));
                localStorage.removeItem('payment_encryption_key');
                console.log('All payment methods cleared for all users');
            }
            return true;
        } catch (error) {
            console.error('Failed to clear payment methods:', error);
            return false;
        }
    }

    /**
     * FIXED: Clear cards when user logs out
     * This is a security measure to ensure cards don't persist after logout
     * @param {string} username - Username of the user logging out
     */
    clearCardsOnLogout(username) {
        if (username) {
            console.log(`Clearing payment methods for user ${username} on logout`);
            this.clearAllCards(username);
        }
    }

    /**
     * Get card brand from card number
     * @param {string} cardNumber - Card number
     */
    getCardBrand(cardNumber) {
        const cleanNumber = cardNumber.replace(/\s/g, '');

        if (/^4/.test(cleanNumber)) return 'Visa';
        if (/^5[1-5]/.test(cleanNumber)) return 'MasterCard';
        if (/^3[47]/.test(cleanNumber)) return 'American Express';
        if (/^6/.test(cleanNumber)) return 'Discover';
        if (/^3[0689]/.test(cleanNumber)) return 'Diners Club';

        return 'Unknown';
    }

    /**
     * Validate card expiry date
     * @param {string} expiryDate - MM/YY format
     */
    isCardExpired(expiryDate) {
        if (!expiryDate || !/^\d{2}\/\d{2}$/.test(expiryDate)) return true;

        const [month, year] = expiryDate.split('/');
        const currentDate = new Date();
        const currentYear = currentDate.getFullYear() % 100;
        const currentMonth = currentDate.getMonth() + 1;

        const cardYear = parseInt(year);
        const cardMonth = parseInt(month);

        if (cardYear < currentYear) return true;
        if (cardYear === currentYear && cardMonth < currentMonth) return true;

        return false;
    }

    /**
     * SECURITY: Emergency method to detect and clean up orphaned payment data
     * This should be called periodically or on app startup
     */
    securityAudit() {
        try {
            const allKeys = [];
            for (let i = 0; i < localStorage.length; i++) {
                const key = localStorage.key(i);
                if (key && key.startsWith(this.baseStorageKey)) {
                    allKeys.push(key);
                }
            }

            console.log(`Security audit: Found ${allKeys.length} payment storage keys`);

            // In a real application, you might want to verify these keys against
            // active user sessions or clean up old data
            return allKeys.length;
        } catch (error) {
            console.error('Security audit failed:', error);
            return 0;
        }
    }
}

// Export singleton instance
export default new CreditCardStorage();