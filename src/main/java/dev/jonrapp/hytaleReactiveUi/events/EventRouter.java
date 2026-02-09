package dev.jonrapp.hytaleReactiveUi.events;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EventRouter {

    private final EventCodec eventCodec;
    private final Map<String, List<EventHandlerRegistration>> actionHandlers = new ConcurrentHashMap<>();
    private final Map<Object, Set<EventHandlerRegistration>> ownerRegistrations = new ConcurrentHashMap<>();

    public EventRouter(@Nonnull EventCodec eventCodec) {
        this.eventCodec = eventCodec;
    }

    public <T> EventHandlerRegistration registerHandler(@Nonnull String action, @Nonnull Object owner, @Nonnull EventHandler<T> handler) {
        EventHandlerRegistration registration = new EventHandlerRegistration(action, owner, handler);

        actionHandlers.computeIfAbsent(action, k -> new ArrayList<>()).add(registration);
        ownerRegistrations.computeIfAbsent(owner, k -> new HashSet<>()).add(registration);

        for (KeyedCodec<?> codec : handler.getParameterCodecs()) {
            eventCodec.register(codec);
        }

        return registration;
    }

    public void unregisterHandler(@Nonnull EventHandlerRegistration registration) {
        List<EventHandlerRegistration> handlers = actionHandlers.get(registration.action);
        if (handlers != null) {
            handlers.remove(registration);
            if (handlers.isEmpty()) {
                actionHandlers.remove(registration.action);
            }
        }

        Set<EventHandlerRegistration> ownerRegs = ownerRegistrations.get(registration.owner);
        if (ownerRegs != null) {
            ownerRegs.remove(registration);
            if (ownerRegs.isEmpty()) {
                ownerRegistrations.remove(registration.owner);
            }
        }

        for (KeyedCodec<?> codec : registration.handler.getParameterCodecs()) {
            eventCodec.unregister(codec.getKey());
        }
    }

    public void unregisterAllForOwner(@Nonnull Object owner) {
        Set<EventHandlerRegistration> registrations = ownerRegistrations.remove(owner);
        if (registrations != null) {
            for (EventHandlerRegistration registration : new ArrayList<>(registrations)) {
                unregisterHandler(registration);
            }
        }
    }

    public boolean routeEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull EventCodec.EventData data) {
        if (!data.contains("Action")) {
            return false;
        }

        String action = data.get("Action");
        List<EventHandlerRegistration> handlers = actionHandlers.get(action);

        if (handlers == null || handlers.isEmpty()) {
            return false;
        }

        for (EventHandlerRegistration registration : new ArrayList<>(handlers)) {
            EventContext context = new EventContext(ref, store, data);
            if (registration.handler.handle(context)) {
                return true;
            }
        }

        return false;
    }

    @Nonnull
    public EventCodec getEventCodec() {
        return eventCodec;
    }

    public static class EventHandlerRegistration {
        private final String action;
        private final Object owner;
        private final EventHandler<?> handler;

        private EventHandlerRegistration(@Nonnull String action, @Nonnull Object owner, @Nonnull EventHandler<?> handler) {
            this.action = action;
            this.owner = owner;
            this.handler = handler;
        }

        @Nonnull
        public String getAction() {
            return action;
        }

        @Nonnull
        public Object getOwner() {
            return owner;
        }
    }

    public static class EventContext {
        private final Ref<EntityStore> ref;
        private final Store<EntityStore> store;
        private final EventCodec.EventData data;

        public EventContext(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull EventCodec.EventData data) {
            this.ref = ref;
            this.store = store;
            this.data = data;
        }

        @Nonnull
        public Ref<EntityStore> getRef() {
            return ref;
        }

        @Nonnull
        public Store<EntityStore> getStore() {
            return store;
        }

        @Nullable
        public <T> T getParameter(@Nonnull String key) {
            return data.get(key);
        }

        public boolean hasParameter(@Nonnull String key) {
            return data.contains(key);
        }
    }
}
