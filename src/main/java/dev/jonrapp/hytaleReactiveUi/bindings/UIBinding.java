package dev.jonrapp.hytaleReactiveUi.bindings;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for marking fields that should be automatically bound to UI elements.
 * <p>
 * Fields annotated with {@code @UIBinding} must be of type {@link UIBindable} and will be
 * automatically initialized and registered with the {@link UIBindingManager} when
 * {@link UIBindingManager#scanAndBind(Object)} is called.
 * <p>
 * When the value of a {@link UIBindable} field changes via {@link UIBindable#set(Object)},
 * the UI element at the specified selector will be automatically updated with the new value.
 * <p>
 * Example usage:
 * <pre>{@code
 * @UIBinding(selector = "#PlayerName.TextSpans")
 * private UIBindable<String> playerName;
 * 
 * // Later in code:
 * playerName.set("NewName"); // Automatically updates the UI
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UIBinding {
    
    /**
     * The CSS selector identifying the UI element to bind to.
     * <p>
     * This selector will be combined with the root selector (if set) to form
     * the full path to the UI element.
     *
     * @return the CSS selector for the target UI element
     */
    String selector();

}
