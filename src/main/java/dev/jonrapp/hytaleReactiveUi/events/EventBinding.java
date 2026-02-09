package dev.jonrapp.hytaleReactiveUi.events;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import dev.jonrapp.hytaleReactiveUi.elements.Element;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Fluent builder for creating and configuring event bindings.
 * <p>
 * EventBinding provides a convenient API for defining how UI events should be handled.
 * It allows you to specify:
 * <ul>
 * <li>The action identifier for the event</li>
 * <li>Typed parameters with codecs to extract from the event data</li>
 * <li>Additional data to send with the event</li>
 * <li>The handler function to execute when the event occurs</li>
 * </ul>
 * <p>
 * Event bindings are typically created using the static {@link #action(String)} method
 * and configured through method chaining before being bound to a UI element.
 * <p>
 * Example usage:
 * <pre>{@code
 * EventBinding.action("button-clicked")
 *     .withEventData("itemId", Codec.STRING, "item123")
 *     .onEvent(context -> {
 *         String itemId = context.getParameter("itemId");
 *         // Handle the event
 *     });
 * }</pre>
 */
public class EventBinding {

    private final String action;
    private final List<KeyedCodec<?>> parameterCodecs = new ArrayList<>();
    private final Map<String, String> eventData = new HashMap<>();
    private Consumer<EventRouter.EventContext> handler;
    private Predicate<EventRouter.EventContext> conditionalHandler;

    /**
     * Private constructor. Use {@link #action(String)} to create instances.
     *
     * @param action the action identifier for this event binding
     */
    private EventBinding(@Nonnull String action) {
        this.action = action;
    }

    /**
     * Creates a new event binding for the specified action.
     *
     * @param action the action identifier that will trigger this event
     * @return a new EventBinding instance for method chaining
     */
    @Nonnull
    public static EventBinding action(@Nonnull String action) {
        return new EventBinding(action);
    }

    /**
     * Adds a typed parameter with its codec and value to the event data.
     * <p>
     * This method both:
     * <ul>
     * <li>Registers the codec for decoding the parameter from incoming events</li>
     * <li>Sets the value to be sent with the event from the client</li>
     * </ul>
     * This allows proper type-safe encoding and decoding of event parameters.
     *
     * @param <T> the type of the parameter
     * @param key the parameter key in the event data
     * @param codec the codec to use for encoding/decoding the parameter value
     * @param value the value to send with the event
     * @return this EventBinding for method chaining
     */
    @Nonnull
    public <T> EventBinding withEventData(@Nonnull String key, @Nonnull Codec<T> codec, @Nonnull String value) {
        parameterCodecs.add(new KeyedCodec<>(key, codec));
        eventData.put(key, value);
        return this;
    }

    /**
     * Sets the handler to be invoked when this event is triggered.
     * <p>
     * The handler will always be considered successful (event consumed).
     *
     * @param handler the consumer to handle the event context
     * @return this EventBinding for method chaining
     */
    @Nonnull
    public EventBinding onEvent(@Nonnull Consumer<EventRouter.EventContext> handler) {
        this.handler = handler;
        return this;
    }

    /**
     * Sets a conditional handler to be invoked when this event is triggered.
     * <p>
     * The handler returns a boolean indicating whether the event was successfully
     * handled. If true, the event is considered consumed and no further handlers
     * will be invoked. If false, event routing continues to the next handler.
     *
     * @param handler the predicate to handle the event context
     * @return this EventBinding for method chaining
     */
    @Nonnull
    public EventBinding onEventConditional(@Nonnull Predicate<EventRouter.EventContext> handler) {
        this.conditionalHandler = handler;
        return this;
    }

    /**
     * Binds this event configuration to a UI element on the client.
     * <p>
     * This method creates the event handler, registers it with the element,
     * and adds the client-side event binding to the event builder.
     *
     * @param bindingType the type of UI event to bind (e.g., CLICK, CHANGE)
     * @param selector the CSS selector identifying the target UI element
     * @param events the event builder to add the binding to
     * @param element the element that owns this event binding
     */
    public void bindTo(@Nonnull CustomUIEventBindingType bindingType, @Nonnull String selector,
                       @Nonnull UIEventBuilder events, @Nonnull Element element) {
        EventHandler<?> eventHandler = createHandler();
        element.registerEventHandler(action, eventHandler);

        EventData data = EventData.of("Action", action);
        this.eventData.entrySet().forEach((entry) -> {
            data.append(entry.getKey(), entry.getValue());
        });

        events.addEventBinding(bindingType, selector, data);
    }

    /**
     * Creates the event handler from this binding's configuration.
     *
     * @return a new EventHandler instance
     */
    @Nonnull
    private EventHandler<?> createHandler() {
        return new EventHandler<Void>() {
            @Override
            public boolean handle(@Nonnull EventRouter.EventContext context) {
                if (conditionalHandler != null) {
                    return conditionalHandler.test(context);
                } else if (handler != null) {
                    handler.accept(context);
                    return true;
                }
                return false;
            }

            @Nonnull
            @Override
            public List<KeyedCodec<?>> getParameterCodecs() {
                return parameterCodecs;
            }
        };
    }

    /**
     * Gets the action identifier for this event binding.
     *
     * @return the action identifier
     */
    @Nonnull
    public String getAction() {
        return action;
    }

    /**
     * Gets the additional event data to be sent with this event.
     *
     * @return a map of event data key-value pairs
     */
    @Nonnull
    public Map<String, String> getEventData() {
        return eventData;
    }

    /**
     * Creates an event handler for use at the page level (not element-specific).
     *
     * @return a new EventHandler instance
     */
    @Nonnull
    public EventHandler<?> createHandlerForPage() {
        return createHandler();
    }
}
