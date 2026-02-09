package dev.jonrapp.hytaleReactiveUi.events;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages dynamic codec registration for event parameters.
 * <p>
 * This class maintains a registry of parameter codecs that are used to decode
 * event data received from the client. As event handlers are registered and unregistered,
 * their parameter codecs are added to or removed from this registry.
 * <p>
 * The codec is rebuilt lazily when event mappings change, ensuring that all registered
 * parameters can be properly decoded from incoming JSON event data.
 */
public class EventCodec {

    private final Map<String, KeyedCodec> eventMappings = new HashMap<>();

    protected BuilderCodec.Builder<EventData> eventDataCodecBuilder;
    protected BuilderCodec<EventData> eventDataCodec;
    private boolean eventsHaveChanged = true; // init to true to ensure codec is built on first use

    /**
     * Constructs a new EventCodec and registers the "Action" parameter codec.
     * <p>
     * The "Action" field is always required for event routing, so it is registered
     * by default to ensure it's available in all event data.
     */
    public EventCodec() {
        register(new KeyedCodec<>("Action", Codec.STRING));
    }

    /**
     * Registers a parameter codec for event decoding.
     * <p>
     * This method is called when an event handler with parameters is registered.
     * The codec will be included in the event data decoder.
     *
     * @param keyedCodec the keyed codec to register
     */
    public void register(KeyedCodec keyedCodec) {
        eventMappings.put(keyedCodec.getKey(), keyedCodec);
        eventsHaveChanged = true;
    }

    /**
     * Unregisters a parameter codec.
     * <p>
     * This method is called when an event handler is unregistered.
     * The codec will be removed from the event data decoder.
     *
     * @param key the key of the codec to unregister
     */
    public void unregister(String key) {
        eventMappings.remove(key);
        eventsHaveChanged = true;
    }

    /**
     * Gets the current event data codec, rebuilding it if necessary.
     * <p>
     * If event mappings have changed since the last call, this method rebuilds
     * the codec to include all currently registered parameter codecs.
     *
     * @return the event data codec for decoding JSON event data
     */
    public BuilderCodec<EventData> getEventCodec() {
        // build the new codec if it's changed
        if (eventsHaveChanged) {
            eventsHaveChanged = false;

            BuilderCodec.Builder<EventData> builder = BuilderCodec.builder(EventData.class, EventData::new);
            eventMappings.forEach((key, codec) -> {
                builder.append(codec,
                        (data, v) -> data.put(key, v),
                        data -> data.get(key)
                ).add();
            });
            eventDataCodec = builder.build();
        }

        // return the codec
        return eventDataCodec;
    }

    /**
     * Container for decoded event data.
     * <p>
     * This class holds the key-value pairs extracted from JSON event data
     * sent by the client. It provides type-safe access to event parameters.
     */
    public static class EventData {
        private Map<String, Object> values = new HashMap<>();

        /**
         * Stores a value in the event data.
         *
         * @param key the parameter key
         * @param value the parameter value
         */
        public void put(String key, Object value) {
            values.put(key, value);
        }

        /**
         * Retrieves a value from the event data.
         *
         * @param <T> the expected type of the value
         * @param key the parameter key
         * @return the parameter value, or null if not present
         */
        public <T> T get(String key) {
            return (T) values.get(key);
        }

        /**
         * Checks if the event data contains a parameter with the specified key.
         *
         * @param key the parameter key to check
         * @return true if the parameter exists, false otherwise
         */
        public boolean contains(String key) {
            return values.containsKey(key);
        }
    }

}
