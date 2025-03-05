package com.official.senestro.core.classes;

import androidx.annotation.NonNull;
import com.official.senestro.core.utils.AdvanceUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class SocketEventManager {
    private final HashMap<String, ArrayList<EventListener>> listeners = new HashMap<>();

    // Add a listener for a specific event
    public void on(@NonNull String event, @NonNull EventListener listener) {
        ArrayList<EventListener> callbacks = listeners.get(event);
        if (AdvanceUtils.isNull(callbacks)) {
            listeners.put(event, new ArrayList<>());
        }
        callbacks.add(listener);
    }

    // Remove a listener for a specific event
    public void off(@NonNull String event, @NonNull EventListener listener) {
        ArrayList<EventListener> callbacks = listeners.get(event);
        if (AdvanceUtils.notNull(callbacks)) {
            callbacks.remove(listener);
            if (callbacks.isEmpty()) {
                listeners.remove(event);
            }
        }
    }

    // Emit an event and pass data to listeners
    public void emit(@NonNull String event, @NonNull Object... args) {
        ArrayList<EventListener> eventListeners = listeners.get(event);
        if (AdvanceUtils.notNull(eventListeners)) {
            for (EventListener listener : eventListeners) {
                listener.onEvent(args);
            }
        }
    }

    // Interface for event listeners
    public interface EventListener {
        void onEvent(@NonNull Object... args);
    }
}
