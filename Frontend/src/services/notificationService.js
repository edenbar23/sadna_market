// Frontend/src/services/notificationService.js

// Try different import approaches - one of these should work
let SockJS, Client;

try {
    // Try ES6 imports first
    import('sockjs-client').then(module => {
        SockJS = module.default || module;
    });
    import('@stomp/stompjs').then(module => {
        Client = module.Client || module.default?.Client || module;
    });
} catch (error) {
    try {
        // Fallback to require
        SockJS = require('sockjs-client').default || require('sockjs-client');
        const StompLib = require('@stomp/stompjs');
        Client = StompLib.Client || StompLib.default?.Client || StompLib;
    } catch (requireError) {
        console.warn('Failed to import WebSocket libraries:', requireError);
        // Fallback - define dummy classes to prevent errors
        SockJS = class { constructor() { console.warn('SockJS not available'); } };
        Client = class { constructor() { console.warn('STOMP Client not available'); } };
    }
}

class NotificationService {
    constructor() {
        this.client = null;
        this.connected = false;
        this.subscriptions = [];
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectDelay = 1000;
        this.notificationHandlers = [];
        this.connectionPromise = null;
    }

    async connect(username, token) {
        if (this.connected) {
            console.log('Already connected to notification service');
            return Promise.resolve();
        }

        if (this.connectionPromise) {
            return this.connectionPromise;
        }

        this.connectionPromise = new Promise(async (resolve, reject) => {
            try {
                // Dynamic import for better compatibility
                if (!SockJS || !Client) {
                    try {
                        const sockjsModule = await import('sockjs-client');
                        const stompModule = await import('@stomp/stompjs');

                        SockJS = sockjsModule.default || sockjsModule;
                        Client = stompModule.Client || stompModule.default?.Client || stompModule;
                    } catch (importError) {
                        console.warn('WebSocket libraries not available, skipping notification service:', importError);
                        resolve(); // Don't fail, just skip notifications
                        return;
                    }
                }

                // Create SockJS connection - Updated to use port 8081
                const socket = new SockJS('http://localhost:8081/ws');

                // Create STOMP client
                this.client = new Client({
                    webSocketFactory: () => socket,
                    connectHeaders: {
                        Authorization: `Bearer ${token}`,
                        username: username
                    },
                    debug: (str) => {
                        console.log('STOMP Debug:', str);
                    },
                    reconnectDelay: this.reconnectDelay,
                    heartbeatIncoming: 4000,
                    heartbeatOutgoing: 4000,
                });

                // Handle successful connection
                this.client.onConnect = (frame) => {
                    console.log('Connected to notification service:', frame);
                    this.connected = true;
                    this.reconnectAttempts = 0;
                    this.reconnectDelay = 1000;

                    // Subscribe to user-specific notifications
                    this.subscribeToNotifications(username);

                    // Send connection message to backend
                    try {
                        this.client.publish({
                            destination: '/app/connect',
                            body: JSON.stringify({ username: username })
                        });
                    } catch (publishError) {
                        console.warn('Failed to send connection message:', publishError);
                    }

                    this.connectionPromise = null;
                    resolve();
                };

                // Handle connection errors
                this.client.onStompError = (frame) => {
                    console.error('STOMP error:', frame);
                    this.connected = false;
                    this.connectionPromise = null;
                    // Don't reject, just log the error
                    console.warn('Notification service unavailable, continuing without real-time notifications');
                    resolve();
                };

                // Handle WebSocket errors
                this.client.onWebSocketError = (error) => {
                    console.error('WebSocket error:', error);
                    this.connected = false;
                    this.connectionPromise = null;
                    console.warn('WebSocket unavailable, continuing without real-time notifications');
                    resolve();
                };

                // Handle disconnection
                this.client.onDisconnect = () => {
                    console.log('Disconnected from notification service');
                    this.connected = false;
                    this.connectionPromise = null;
                    this.clearSubscriptions();
                };

                // Activate the client
                this.client.activate();

            } catch (error) {
                console.error('Failed to connect to notification service:', error);
                this.connectionPromise = null;
                // Don't reject - let the app continue without notifications
                console.warn('Continuing without real-time notifications');
                resolve();
            }
        });

        return this.connectionPromise;
    }

    subscribeToNotifications(username) {
        if (!this.client || !this.connected) {
            console.warn('Cannot subscribe - not connected');
            return;
        }

        try {
            const subscription = this.client.subscribe(
                `/user/${username}/queue/notifications`,
                (message) => {
                    try {
                        const notification = JSON.parse(message.body);
                        console.log('Received notification:', notification);
                        this.handleNotification(notification);
                    } catch (error) {
                        console.error('Error parsing notification:', error);
                    }
                }
            );

            this.subscriptions.push(subscription);
            console.log(`Subscribed to notifications for user: ${username}`);

        } catch (error) {
            console.error('Failed to subscribe to notifications:', error);
        }
    }

    handleNotification(notification) {
        // Show browser notification if permission granted
        this.showBrowserNotification(notification);

        // Call all registered handlers
        this.notificationHandlers.forEach(handler => {
            try {
                handler(notification);
            } catch (error) {
                console.error('Error in notification handler:', error);
            }
        });
    }

    showBrowserNotification(notification) {
        if (!('Notification' in window)) {
            console.log('This browser does not support desktop notification');
            return;
        }

        if (Notification.permission === 'granted') {
            const options = {
                body: notification.message,
                icon: '/assets/logo.png',
                tag: notification.notificationId,
                requireInteraction: false,
                silent: false
            };

            const browserNotification = new Notification(notification.title, options);

            setTimeout(() => {
                try {
                    browserNotification.close();
                } catch (error) {
                    // Ignore errors when closing notifications
                }
            }, 5000);

            browserNotification.onclick = () => {
                window.focus();
                try {
                    browserNotification.close();
                } catch (error) {
                    // Ignore errors when closing notifications
                }
            };
        }
    }

    async requestNotificationPermission() {
        if (!('Notification' in window)) {
            console.log('This browser does not support desktop notification');
            return Promise.resolve('unsupported');
        }

        if (Notification.permission !== 'denied') {
            try {
                return await Notification.requestPermission();
            } catch (error) {
                console.warn('Failed to request notification permission:', error);
                return Notification.permission;
            }
        }

        return Promise.resolve(Notification.permission);
    }

    addNotificationHandler(handler) {
        if (typeof handler === 'function') {
            this.notificationHandlers.push(handler);
        }
    }

    removeNotificationHandler(handler) {
        const index = this.notificationHandlers.indexOf(handler);
        if (index > -1) {
            this.notificationHandlers.splice(index, 1);
        }
    }

    handleReconnect(username, token) {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`Attempting to reconnect... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);

            setTimeout(() => {
                this.connect(username, token).catch(error => {
                    console.error('Reconnection failed:', error);
                });
            }, this.reconnectDelay);

            this.reconnectDelay = Math.min(this.reconnectDelay * 2, 30000);
        } else {
            console.error('Max reconnection attempts reached');
        }
    }

    disconnect() {
        if (this.client && this.connected) {
            console.log('Disconnecting from notification service');
            this.clearSubscriptions();
            try {
                this.client.deactivate();
            } catch (error) {
                console.warn('Error deactivating STOMP client:', error);
            }
            this.connected = false;
        }
        this.connectionPromise = null;
    }

    clearSubscriptions() {
        this.subscriptions.forEach(subscription => {
            try {
                subscription.unsubscribe();
            } catch (error) {
                console.error('Error unsubscribing:', error);
            }
        });
        this.subscriptions = [];
    }

    isConnected() {
        return this.connected;
    }
}

// Create singleton instance
const notificationService = new NotificationService();
export default notificationService;