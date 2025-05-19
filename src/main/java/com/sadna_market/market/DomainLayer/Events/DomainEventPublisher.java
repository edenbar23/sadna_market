package com.sadna_market.market.DomainLayer.Events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Central event publisher for domain events
 */
@Component
@Scope("singleton")
public class DomainEventPublisher {
    private static final Logger logger = LoggerFactory.getLogger(DomainEventPublisher.class);

    // Thread-safe collections for concurrent event handling
    private static final Map<Class<?>, List<Consumer<?>>> subscribers = new ConcurrentHashMap<>();

    /**
     * Subscribe to a specific event type
     *
     * @param <T> Event type
     * @param eventType Class of the event
     * @param handler Consumer that will handle the event
     */
    public static <T extends DomainEvent> void subscribe(Class<T> eventType, Consumer<T> handler) {
        logger.debug("Subscribing to event type: {}", eventType.getSimpleName());
        subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(handler);
    }

    /**
     * Unsubscribe a handler from an event type
     *
     * @param <T> Event type
     * @param eventType Class of the event
     * @param handler Handler to remove
     */
    public static <T extends DomainEvent> void unsubscribe(Class<T> eventType, Consumer<T> handler) {
        logger.debug("Unsubscribing from event type: {}", eventType.getSimpleName());
        if (subscribers.containsKey(eventType)) {
            subscribers.get(eventType).remove(handler);
        }
    }

    /**
     * Publish an event to all subscribers
     *
     * @param <T> Event type
     * @param event The event to publish
     */
    public static <T extends DomainEvent> void publish(T event) {
        if (event == null) {
            logger.warn("Attempted to publish null event");
            return;
        }

        logger.info("Publishing event: {} (ID: {})", event.getClass().getSimpleName(), event.getEventId());

        List<Consumer<?>> handlers = subscribers.getOrDefault(event.getClass(), new CopyOnWriteArrayList<>());
        if (handlers.isEmpty()) {
            logger.debug("No subscribers for event type: {}", event.getClass().getSimpleName());
        } else {
            logger.debug("Notifying {} subscriber(s) for event type: {}", handlers.size(), event.getClass().getSimpleName());

            for (Consumer<?> handler : handlers) {
                try {
                    ((Consumer<T>) handler).accept(event);
                } catch (Exception e) {
                    logger.error("Error in event handler for event type {}: {}", event.getClass().getSimpleName(), e.getMessage(), e);
                    // Continue with other handlers despite errors
                    throw new RuntimeException("Error in event handler", e);
                }
            }
        }
    }

    /**
     * Clear all subscribers - primarily for testing purposes
     */
    public static void clearAllSubscribers() {
        logger.debug("Clearing all event subscribers");
        subscribers.clear();
    }

    /**
     * Get the count of subscribers for an event type - for testing purposes
     */
    public static int getSubscriberCount(Class<?> eventType) {
        return subscribers.getOrDefault(eventType, List.of()).size();
    }
}