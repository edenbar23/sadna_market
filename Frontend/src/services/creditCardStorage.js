import CryptoJS from 'crypto-js';

class CreditCardStorage {
    constructor() {

        this.encryptionKey = this.getOrCreateEncryptionKey();
        this.storageKey = 'encrypted_payment_methods';
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
     * Save a credit card securely
     * @param {Object} cardData - Credit card information
     * @param {string} cardData.cardNumber - Card number
     * @param {string} cardData.expiryDate - MM/YY format
     * @param {string} cardData.cvv - CVV code
     * @param {string} cardData.cardHolder - Cardholder name
     * @param {string} cardData.cardType - Card type (credit/debit)
     * @param {Object} cardData.billingAddress - Billing address
     * @param {string} nickname - Optional nickname for the card
     */
    saveCard(cardData, nickname = '') {
        try {
            const cards = this.getSavedCards();

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
                // Encrypt sensitive data
                encryptedData: this.encrypt({
                    cardNumber: cardData.cardNumber,
                    cvv: cardData.cvv
                })
            };

            // Add to saved cards
            cards.push(cardToSave);

            // Save to localStorage
            localStorage.setItem(this.storageKey, JSON.stringify(cards));

            console.log('Credit card saved successfully');
            return cardToSave.id;

        } catch (error) {
            console.error('Failed to save credit card:', error);
            throw new Error('Failed to save payment method');
        }
    }

    /**
     * Get all saved cards (with sensitive data encrypted)
     * Returns cards with masked numbers for display
     */
    getSavedCards() {
        try {
            const cardsData = localStorage.getItem(this.storageKey);
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
     * Get a specific card with decrypted data for payment processing
     * @param {string} cardId - Card ID
     * @returns {Object} Full card data with decrypted sensitive information
     */
    getCardForPayment(cardId) {
        try {
            const cardsData = localStorage.getItem(this.storageKey);
            if (!cardsData) throw new Error('No saved cards found');

            const cards = JSON.parse(cardsData);
            const card = cards.find(c => c.id === cardId);

            if (!card) throw new Error('Card not found');

            // Decrypt sensitive data
            const decryptedData = this.decrypt(card.encryptedData);

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
     * Delete a saved card
     * @param {string} cardId - Card ID to delete
     */
    deleteCard(cardId) {
        try {
            const cardsData = localStorage.getItem(this.storageKey);
            if (!cardsData) return false;

            const cards = JSON.parse(cardsData);
            const filteredCards = cards.filter(card => card.id !== cardId);

            localStorage.setItem(this.storageKey, JSON.stringify(filteredCards));

            console.log('Credit card deleted successfully');
            return true;

        } catch (error) {
            console.error('Failed to delete credit card:', error);
            throw new Error('Failed to delete payment method');
        }
    }

    /**
     * Update card nickname
     * @param {string} cardId - Card ID
     * @param {string} newNickname - New nickname
     */
    updateCardNickname(cardId, newNickname) {
        try {
            const cardsData = localStorage.getItem(this.storageKey);
            if (!cardsData) throw new Error('No saved cards found');

            const cards = JSON.parse(cardsData);
            const cardIndex = cards.findIndex(c => c.id === cardId);

            if (cardIndex === -1) throw new Error('Card not found');

            cards[cardIndex].nickname = newNickname;
            localStorage.setItem(this.storageKey, JSON.stringify(cards));

            return true;
        } catch (error) {
            console.error('Failed to update card nickname:', error);
            throw new Error('Failed to update payment method');
        }
    }

    /**
     * Check if a card with the same last 4 digits already exists
     * @param {string} cardNumber - Card number to check
     */
    isDuplicateCard(cardNumber) {
        const lastFour = cardNumber.replace(/\s/g, '').slice(-4);
        const savedCards = this.getSavedCards();
        return savedCards.some(card => card.lastFour === lastFour);
    }

    /**
     * Clear all saved cards (useful for logout or security reasons)
     */
    clearAllCards() {
        try {
            localStorage.removeItem(this.storageKey);
            localStorage.removeItem('payment_encryption_key');
            console.log('All payment methods cleared');
            return true;
        } catch (error) {
            console.error('Failed to clear payment methods:', error);
            return false;
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
}

// Export singleton instance
export default new CreditCardStorage();