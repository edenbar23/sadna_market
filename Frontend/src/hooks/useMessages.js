import { useState, useCallback } from 'react';
import * as messageAPI from '../api/message';
import { useAuthContext } from '../context/AuthContext';

export const useMessages = () => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const { user, token } = useAuthContext();

    const sendMessageToStore = useCallback(async (storeId, content) => {
        if (!user || !token) {
            setError('You must be logged in to send messages');
            return null;
        }

        setLoading(true);
        setError(null);
        try {
            const response = await messageAPI.sendMessage(
                user.username,
                storeId,
                content,
                token
            );
            return response.data;
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to send message');
            throw err;
        } finally {
            setLoading(false);
        }
    }, [user, token]);

    const replyToStoreMessage = useCallback(async (messageId, content) => {
        if (!user || !token) {
            setError('You must be logged in to reply to messages');
            return null;
        }

        setLoading(true);
        setError(null);
        try {
            const response = await messageAPI.replyToMessage(
                messageId,
                user.username,
                content,
                token
            );
            return response.data;
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to reply to message');
            throw err;
        } finally {
            setLoading(false);
        }
    }, [user, token]);

    const getConversationWithStore = useCallback(async (storeId) => {
        if (!user || !token) {
            setError('You must be logged in to view conversations');
            return null;
        }

        setLoading(true);
        setError(null);
        try {
            const response = await messageAPI.getUserStoreConversation(
                user.username,
                storeId,
                token
            );
            return response.data;
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to get conversation');
            throw err;
        } finally {
            setLoading(false);
        }
    }, [user, token]);

    const getUserMessagesList = useCallback(async () => {
        if (!user || !token) {
            setError('You must be logged in to view messages');
            return null;
        }

        setLoading(true);
        setError(null);
        try {
            const response = await messageAPI.getUserMessages(user.username, token);
            return response.data;
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to get messages');
            throw err;
        } finally {
            setLoading(false);
        }
    }, [user, token]);

    const getStoreMessagesList = useCallback(async (storeId) => {
        if (!user || !token) {
            setError('You must be logged in to view store messages');
            return null;
        }

        setLoading(true);
        setError(null);
        try {
            const response = await messageAPI.getStoreMessages(
                storeId,
                user.username,
                token
            );
            return response.data;
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to get store messages');
            throw err;
        } finally {
            setLoading(false);
        }
    }, [user, token]);

    const markAsRead = useCallback(async (messageId) => {
        if (!user || !token) {
            setError('You must be logged in to mark messages as read');
            return false;
        }

        setLoading(true);
        setError(null);
        try {
            await messageAPI.markMessageAsRead(messageId, user.username, token);
            return true;
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to mark message as read');
            return false;
        } finally {
            setLoading(false);
        }
    }, [user, token]);

    const reportMessage = useCallback(async (messageId, reason) => {
        if (!user || !token) {
            setError('You must be logged in to report messages');
            return false;
        }

        setLoading(true);
        setError(null);
        try {
            await messageAPI.reportViolation(
                messageId,
                user.username,
                reason,
                token
            );
            return true;
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to report message');
            return false;
        } finally {
            setLoading(false);
        }
    }, [user, token]);

    return {
        loading,
        error,
        sendMessageToStore,
        replyToStoreMessage,
        getConversationWithStore,
        getUserMessagesList,
        getStoreMessagesList,
        markAsRead,
        reportMessage
    };
};