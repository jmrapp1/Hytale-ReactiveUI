package dev.jonrapp.hytaleReactiveUi.elements;

import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import dev.jonrapp.hytaleReactiveUi.events.EventBinding;
import dev.jonrapp.hytaleReactiveUi.events.EventHandler;
import dev.jonrapp.hytaleReactiveUi.pages.ReactiveUiBasePage;
import dev.jonrapp.hytaleReactiveUi.support.UIEventSupport;

import javax.annotation.Nonnull;

import static dev.jonrapp.hytaleReactiveUi.utils.UiUtils.getArraySelector;
import static dev.jonrapp.hytaleReactiveUi.utils.UiUtils.selectors;

/**
 * Base class representing a UI element within a ReactiveUI page.
 * <p>
 * Elements are the building blocks of custom UI pages in Hytale. Each element manages
 * its own lifecycle, event handlers, UI updates, and data bindings. Elements can:
 * <ul>
 * <li>Register event handlers that respond to client-side interactions</li>
 * <li>Use {@link dev.jonrapp.hytaleReactiveUi.bindings.UIBindable} fields with {@link dev.jonrapp.hytaleReactiveUi.bindings.UIBinding} for automatic data binding</li>
 * <li>Be created as standalone elements or as items in arrays/lists</li>
 * </ul>
 * <p>
 * The generic type parameter allows type-safe access to the parent page.
 * <p>
 * Subclasses must implement {@link #onCreate(String, UICommandBuilder, UIEventBuilder)}
 * to define the element's UI structure and event bindings.
 *
 * @param <T> the type of the parent page
 */
public abstract class Element<T extends ReactiveUiBasePage> {

    protected final T pageRef;
    private final UIEventSupport eventSupport;

    /**
     * Constructs a new element associated with the specified page.
     * <p>
     * This constructor automatically:
     * <ol>
     * <li>Initializes the event support system</li>
     * <li>Scans for {@link dev.jonrapp.hytaleReactiveUi.bindings.UIBinding} annotated fields</li>
     * <li>Initializes {@link dev.jonrapp.hytaleReactiveUi.bindings.UIBindable} fields for automatic data binding</li>
     * </ol>
     *
     * @param pageRef the parent page that owns this element
     */
    public Element(T pageRef) {
        this.pageRef = pageRef;
        this.eventSupport = new UIEventSupport(pageRef.getEventRouter(), this::sendUpdate);
        eventSupport.getBindingManager().scanAndBind(this);
    }

    /**
     * Creates this element as a standalone element and sends the initial UI update to the client.
     * <p>
     * This method:
     * <ol>
     * <li>Sets the root selector for data bindings</li>
     * <li>Calls {@link #onCreate(String, UICommandBuilder, UIEventBuilder)}</li>
     * <li>Sends the resulting commands and event bindings to the client</li>
     * </ol>
     *
     * @param root the root selector or path where this element should be created in the UI hierarchy
     */
    public void create(String root) {
        eventSupport.getBindingManager().setRootSelector(root);
        
        UICommandBuilder commands = new UICommandBuilder();
        UIEventBuilder events = new UIEventBuilder();
        onCreate(root, commands, events);
        sendUpdate(commands, events, false);
    }

    /**
     * Creates this element as an item in an array/list at the specified index.
     * <p>
     * This method is used when creating multiple instances of the same element type,
     * such as items in a list. It:
     * <ol>
     * <li>Creates a container group with an indexed selector</li>
     * <li>Sets the root selector for data bindings to the container</li>
     * <li>Calls {@link #onCreate(String, UICommandBuilder, UIEventBuilder)}</li>
     * </ol>
     * Unlike {@link #create(String)}, this adds to existing builders rather than
     * creating new ones and sending immediately.
     *
     * @param root the root selector where the array container should be created
     * @param index the index of this element in the array
     * @param commands the command builder to add creation commands to
     * @param events the event builder to add event bindings to
     */
    public void create(String root, int index, UICommandBuilder commands, UIEventBuilder events) {
        String itemContainerSelector = getArraySelector("#" + getElementSelectorId(), index);
        commands.appendInline(root, "Group " + itemContainerSelector + " { } ");
        String itemRoot = selectors(root, itemContainerSelector);

        eventSupport.getBindingManager().setRootSelector(itemRoot);
        onCreate(itemRoot, commands, events);
    }

    /**
     * Called when the element is being created. Subclasses must implement this to define
     * the element's UI structure and event bindings.
     * <p>
     * This method should:
     * <ul>
     * <li>Add UI commands to create/configure the element's visual structure</li>
     * <li>Register event bindings for user interactions</li>
     * <li>Set initial values for {@link dev.jonrapp.hytaleReactiveUi.bindings.UIBindable} fields if needed</li>
     * </ul>
     *
     * @param root the root selector or path for this element
     * @param commands the command builder to add UI creation/update commands
     * @param events the event builder to register event bindings on the client
     */
    protected abstract void onCreate(String root, UICommandBuilder commands, UIEventBuilder events);

    /**
     * Called when the element is being unloaded or destroyed.
     * <p>
     * This method unregisters all event handlers associated with this element,
     * cleaning up resources and preventing memory leaks.
     */
    public void onUnload() {
        eventSupport.unloadEvents();
    }

    /**
     * Registers an event handler for the specified action.
     * <p>
     * The handler will be invoked when an event with the matching action is received
     * from the client. The registration is tracked so it can be automatically cleaned
     * up when the element is unloaded.
     *
     * @param <T> the type of data the handler expects
     * @param action the action identifier that triggers this handler
     * @param handler the event handler to register
     */
    public <T> void registerEventHandler(@Nonnull String action, @Nonnull EventHandler<T> handler) {
        eventSupport.registerEventHandler(action, this, handler);
    }

    /**
     * Binds an event to a UI element on the client.
     * <p>
     * This creates a client-side event binding that will send event data back to the server
     * when triggered (e.g., on click, on change, etc.).
     *
     * @param bindingType the type of event to bind (e.g., CLICK, CHANGE)
     * @param selector the CSS selector identifying the target UI element
     * @param events the event builder to add the binding to
     * @param eventBinding the event binding configuration
     */
    protected void bindEvent(@Nonnull CustomUIEventBindingType bindingType, @Nonnull String selector,
                             @Nonnull UIEventBuilder events, @Nonnull EventBinding eventBinding) {
        eventSupport.bindEvent(bindingType, selector, events, eventBinding, this);
    }

    /**
     * Sends a UI update to the client with commands only.
     *
     * @param commands the UI commands to send
     * @param clear whether to clear existing UI elements before applying updates
     */
    protected void sendUpdate(UICommandBuilder commands, boolean clear) {
        pageRef.sendUpdate(commands, clear);
    }

    /**
     * Sends a UI update to the client with both commands and event bindings.
     *
     * @param commands the UI commands to send
     * @param events the event bindings to register
     * @param clear whether to clear existing UI elements before applying updates
     */
    protected void sendUpdate(UICommandBuilder commands, UIEventBuilder events, boolean clear) {
        pageRef.sendUpdate(commands, events, clear);
    }

    /**
     * Gets the selector ID for this element type.
     * <p>
     * By default, this returns the simple class name. This is used when creating
     * array elements to generate unique selectors for each instance.
     *
     * @return the selector ID (defaults to the class simple name)
     */
    public String getElementSelectorId() {
        return this.getClass().getSimpleName();
    }

}
