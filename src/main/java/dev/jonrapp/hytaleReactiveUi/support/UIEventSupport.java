package dev.jonrapp.hytaleReactiveUi.support;

import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import dev.jonrapp.hytaleReactiveUi.bindings.UIBindingManager;
import dev.jonrapp.hytaleReactiveUi.events.EventBinding;
import dev.jonrapp.hytaleReactiveUi.events.EventHandler;
import dev.jonrapp.hytaleReactiveUi.events.EventRouter;
import dev.jonrapp.hytaleReactiveUi.pages.ReactiveUiBasePage;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Helper class that consolidates event handling and data binding functionality.
 * <p>
 * UIEventSupport reduces boilerplate by providing a unified interface for:
 * <ul>
 * <li>Registering and managing event handlers</li>
 * <li>Binding events to UI elements</li>
 * <li>Managing UI data bindings via {@link UIBindingManager}</li>
 * <li>Automatic cleanup of event registrations</li>
 * </ul>
 * This class is used internally by both {@link ReactiveUiBasePage}
 * and {@link dev.jonrapp.hytaleReactiveUi.elements.Element} to provide consistent event handling.
 */
public class UIEventSupport {

    private final EventRouter eventRouter;
    private final List<EventRouter.EventHandlerRegistration> eventRegistrations = new ArrayList<>();
    private final UIBindingManager bindingManager;

    /**
     * Constructs a new UIEventSupport with the specified event router and update callback.
     *
     * @param eventRouter the event router for registering handlers
     * @param updateCallback the callback to invoke when sending UI updates
     */
    public UIEventSupport(@Nonnull EventRouter eventRouter, @Nonnull BiConsumer<UICommandBuilder, Boolean> updateCallback) {
        this.eventRouter = eventRouter;
        this.bindingManager = new UIBindingManager(commands -> updateCallback.accept(commands, false));
    }

    /**
     * Registers an event handler for the specified action.
     * <p>
     * The registration is tracked so it can be automatically cleaned up
     * when {@link #unloadEvents()} is called.
     *
     * @param <T> the type of data the handler processes
     * @param action the action identifier that triggers this handler
     * @param owner the object that owns this handler
     * @param handler the event handler to register
     */
    public <T> void registerEventHandler(@Nonnull String action, @Nonnull Object owner, @Nonnull EventHandler<T> handler) {
        EventRouter.EventHandlerRegistration registration = eventRouter.registerHandler(action, owner, handler);
        eventRegistrations.add(registration);
    }

    /**
     * Binds an event to a UI element on the client.
     * <p>
     * This method:
     * <ol>
     * <li>Creates an event handler from the binding configuration</li>
     * <li>Registers the handler with the event router</li>
     * <li>Adds the client-side event binding to the event builder</li>
     * </ol>
     *
     * @param bindingType the type of UI event to bind (e.g., CLICK, CHANGE)
     * @param selector the CSS selector identifying the target UI element
     * @param events the event builder to add the binding to
     * @param eventBinding the event binding configuration
     * @param owner the object that owns this event binding
     */
    public void bindEvent(@Nonnull CustomUIEventBindingType bindingType, @Nonnull String selector,
                          @Nonnull UIEventBuilder events, @Nonnull EventBinding eventBinding,
                          @Nonnull Object owner) {
        EventHandler<?> handler = eventBinding.createHandlerForPage();
        registerEventHandler(eventBinding.getAction(), owner, handler);

        EventData data = EventData.of("Action", eventBinding.getAction());
        eventBinding.getEventData().forEach(data::append);

        events.addEventBinding(bindingType, selector, data);
    }

    /**
     * Unregisters all event handlers that were registered through this support instance.
     * <p>
     * This method should be called when an element or page is being destroyed to
     * prevent memory leaks and ensure proper cleanup.
     */
    public void unloadEvents() {
        for (EventRouter.EventHandlerRegistration registration : eventRegistrations) {
            eventRouter.unregisterHandler(registration);
        }
        eventRegistrations.clear();
    }

    /**
     * Gets the binding manager for managing UI data bindings.
     *
     * @return the binding manager instance
     */
    @Nonnull
    public UIBindingManager getBindingManager() {
        return bindingManager;
    }
}
