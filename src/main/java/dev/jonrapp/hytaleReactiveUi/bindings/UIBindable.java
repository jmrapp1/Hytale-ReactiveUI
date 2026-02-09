package dev.jonrapp.hytaleReactiveUi.bindings;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A reactive wrapper for values that automatically updates UI elements when changed.
 * <p>
 * {@code UIBindable} provides automatic data binding between Java fields and UI elements.
 * When the value is changed via {@link #set(Object)}, the bound UI element is automatically
 * updated with the new value.
 * <p>
 * This class is typically used with the {@link UIBinding} annotation for automatic
 * initialization and registration:
 * <pre>{@code
 * @UIBinding(selector = "#PlayerName.TextSpans")
 * private UIBindable<String> playerName;
 * 
 * // Automatically updates the UI element
 * playerName.set("NewName");
 * }</pre>
 *
 * @param <T> the type of value being bound
 */
public class UIBindable<T> {
    
    private T value;
    private final UIBindingManager bindingManager;
    private final String fieldName;
    
    /**
     * Constructs a new UIBindable with the specified binding manager and field name.
     * <p>
     * This constructor is typically called automatically by {@link UIBindingManager}
     * during the scanning process.
     *
     * @param bindingManager the binding manager that handles UI updates
     * @param fieldName the name of the field (used as the binding key)
     * @param initialValue the initial value, or null
     */
    public UIBindable(@Nonnull UIBindingManager bindingManager, @Nonnull String fieldName, @Nullable T initialValue) {
        this.bindingManager = bindingManager;
        this.fieldName = fieldName;
        this.value = initialValue;
    }
    
    /**
     * Gets the current value.
     *
     * @return the current value, or null if not set
     */
    @Nullable
    public T get() {
        return value;
    }
    
    /**
     * Sets a new value and automatically updates the bound UI element.
     * <p>
     * If the new value is different from the current value, this method:
     * <ol>
     * <li>Updates the internal value</li>
     * <li>Creates a new UI command builder</li>
     * <li>Sends an update to the client with the new value</li>
     * </ol>
     * The update is sent immediately and independently.
     *
     * @param newValue the new value to set
     */
    public void set(@Nullable T newValue) {
        if (this.value != newValue && (this.value == null || !this.value.equals(newValue))) {
            this.value = newValue;
            bindingManager.notifyValueChanged(fieldName);
        }
    }

    /**
     * Sets a new value and adds the update to an existing command builder.
     * <p>
     * This method is useful when batching multiple UI updates together, .
     * Unlike {@link #set(Object)}, this does not send the update immediately;
     * instead, it adds the update command to the provided builder.
     *
     * @param newValue the new value to set
     * @param commands the command builder to add the update to
     */
    public void set(@Nullable T newValue, UICommandBuilder commands) {
        if (this.value != newValue && (this.value == null || !this.value.equals(newValue))) {
            this.value = newValue;
            bindingManager.notifyValueChanged(fieldName, commands);
        }
    }
    
    /**
     * Converts the current value to a Hytale {@link Message} for UI rendering.
     * <p>
     * Conversion rules:
     * <ul>
     * <li>If value is null: returns an empty raw message</li>
     * <li>If value is already a Message: returns it directly</li>
     * <li>Otherwise: converts to string and wraps in a raw message</li>
     * </ul>
     *
     * @return the value as a Message suitable for UI display
     */
    @Nonnull
    public Message toMessage() {
        if (value == null) {
            return Message.raw("");
        }
        if (value instanceof Message) {
            return (Message) value;
        }
        return Message.raw(String.valueOf(value));
    }
    
    /**
     * Returns the string representation of the current value.
     *
     * @return the value as a string, or empty string if null
     */
    @Override
    public String toString() {
        return value != null ? value.toString() : "";
    }
}
