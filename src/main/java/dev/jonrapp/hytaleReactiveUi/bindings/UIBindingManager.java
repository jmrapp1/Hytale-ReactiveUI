package dev.jonrapp.hytaleReactiveUi.bindings;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static dev.jonrapp.hytaleReactiveUi.utils.UiUtils.selectors;

/**
 * Manages automatic data binding between Java fields and UI elements.
 * <p>
 * The UIBindingManager scans objects for {@link UIBinding} annotated fields,
 * automatically initializes {@link UIBindable} instances, and handles UI updates
 * when bound values change.
 * <p>
 * Typical usage:
 * <pre>{@code
 * // In an Element or Page:
 * @UIBinding(selector = "#PlayerName.TextSpans")
 * private UIBindable<String> playerName;
 * 
 * // The binding manager automatically:
 * // 1. Initializes the UIBindable field
 * // 2. Registers it for automatic updates
 * // 3. Updates the UI when playerName.set() is called
 * }</pre>
 */
public class UIBindingManager {
    
    private final Map<String, BindingInfo> bindings = new HashMap<>();
    private final Consumer<UICommandBuilder> updateCallback;
    private String rootSelector = "";
    
    /**
     * Constructs a new UIBindingManager with the specified update callback.
     *
     * @param updateCallback the callback to invoke when sending UI updates
     */
    public UIBindingManager(@Nonnull Consumer<UICommandBuilder> updateCallback) {
        this.updateCallback = updateCallback;
    }
    
    /**
     * Sets the root selector to prepend to all binding selectors.
     * <p>
     * This allows bindings to be scoped to a specific part of the UI hierarchy.
     * For example, if the root selector is "#Container", a binding with selector
     * "#Text" will resolve to "#Container #Text".
     *
     * @param rootSelector the root selector to prepend, or empty string for no prefix
     */
    public void setRootSelector(@Nonnull String rootSelector) {
        this.rootSelector = rootSelector;
    }
    
    /**
     * Scans the target object for {@link UIBinding} annotated fields and registers them.
     * <p>
     * This method:
     * <ol>
     * <li>Finds all fields annotated with {@link UIBinding}</li>
     * <li>Initializes null {@link UIBindable} fields automatically</li>
     * <li>Registers each binding for automatic UI updates</li>
     * </ol>
     * This method should be called once during object initialization.
     *
     * @param target the object to scan for binding annotations
     * @throws RuntimeException if field access fails
     */
    public void scanAndBind(@Nonnull Object target) {
        Class<?> clazz = target.getClass();

        // init all annotated instance vars first
        initializeBindableFields(clazz, target);
        
        for (Field field : clazz.getDeclaredFields()) {
            UIBinding annotation = field.getAnnotation(UIBinding.class);
            if (annotation != null) {
                field.setAccessible(true);
                
                try {
                    Object fieldValue = field.get(target);
                    if (fieldValue instanceof UIBindable<?>) {
                        bindings.put(field.getName(), new BindingInfo(
                            field,
                            target,
                            annotation.selector(),
                            (UIBindable<?>) fieldValue
                        ));
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to access field: " + field.getName(), e);
                }
            }
        }
    }

    /**
     * Initializes all {@link UIBindable} fields that are null.
     * <p>
     * This method is called internally during scanning to ensure all annotated
     * fields have valid UIBindable instances before registration.
     *
     * @param clazz the class to scan for fields
     * @param target the object instance to initialize fields on
     * @throws RuntimeException if field initialization fails
     */
    private void initializeBindableFields(@Nonnull Class<?> clazz, @Nonnull Object target) {
        for (Field field : clazz.getDeclaredFields()) {
            UIBinding annotation = field.getAnnotation(UIBinding.class);
            if (annotation != null && UIBindable.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                try {
                    UIBindable<?> bindable = new UIBindable<>(this, field.getName(), null);
                    field.set(target, bindable);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to initialize UIBindable field: " + field.getName(), e);
                }
            }
        }
    }
    
    /**
     * Notifies the manager that a bound value has changed.
     * <p>
     * This creates a new command builder, applies the binding update, and sends
     * it to the client immediately.
     *
     * @param fieldName the name of the field that changed
     */
    public void notifyValueChanged(@Nonnull String fieldName) {
        notifyValueChanged(fieldName, null);
    }

    /**
     * Notifies the manager that a bound value has changed, using an existing command builder.
     * <p>
     * If commands is null, behaves like {@link #notifyValueChanged(String)}.
     * Otherwise, adds the update to the provided builder without sending it.
     *
     * @param fieldName the name of the field that changed
     * @param commands the command builder to add the update to, or null to send immediately
     */
    public void notifyValueChanged(@Nonnull String fieldName, UICommandBuilder commands) {
        BindingInfo binding = bindings.get(fieldName);
        if (binding != null) {
            if (commands == null) {
                // create a new command builder and send off update
                commands = new UICommandBuilder();
                applyBinding(commands, binding);
                updateCallback.accept(commands);
            } else {
                // use existing command builder
                applyBinding(commands, binding);
            }
        }
    }
    
    /**
     * Updates all registered bindings at once.
     * <p>
     * This method creates a single command builder, applies all binding updates,
     * and sends them to the client in one batch. Useful for initial UI setup or
     * when multiple values have changed.
     */
    public void updateAll() {
        UICommandBuilder commands = new UICommandBuilder();
        
        for (BindingInfo binding : bindings.values()) {
            applyBinding(commands, binding);
        }
        
        updateCallback.accept(commands);
    }
    
    /**
     * Applies a single binding update to the command builder.
     *
     * @param commands the command builder to add the update to
     * @param binding the binding information
     */
    private void applyBinding(@Nonnull UICommandBuilder commands, @Nonnull BindingInfo binding) {
        Message message = binding.bindable.toMessage();
        String fullSelector = buildSelector(binding.selector);
        commands.set(fullSelector, message);
    }
    
    /**
     * Builds the full selector by combining the root selector with the binding selector.
     *
     * @param selector the binding's selector
     * @return the full selector path
     */
    @Nonnull
    private String buildSelector(@Nonnull String selector) {
        if (rootSelector.isEmpty()) {
            return selector;
        }
        return selectors(rootSelector, selector);
    }

    /**
     * Internal record holding information about a registered binding.
     */
    private record BindingInfo(Field field, Object target, String selector, UIBindable<?> bindable) {
            private BindingInfo(@Nonnull Field field, @Nonnull Object target, @Nonnull String selector,
                                @Nonnull UIBindable<?> bindable) {
                this.field = field;
                this.target = target;
                this.selector = selector;
                this.bindable = bindable;
            }
        }
}
