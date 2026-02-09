package dev.jonrapp.hytaleReactiveUi.pages;

import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.jonrapp.hytaleReactiveUi.elements.Element;

import javax.annotation.Nonnull;

/**
 * Higher-level page class that adds support for managing a primary element.
 * <p>
 * This class extends {@link ReactiveUiBasePage} and provides convenient functionality for
 * pages that display a single primary element at a time, such as tabbed interfaces
 * or wizard-style pages where different views are swapped in and out.
 * <p>
 * The primary element concept allows you to:
 * <ul>
 * <li>Easily switch between different UI views/elements</li>
 * <li>Automatically handle cleanup of the previous element</li>
 * <li>Avoid duplicate element creation</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * public class MyPage extends ReactiveUIPage {
 *     public void showTab1() {
 *         showPrimaryElement(new Tab1Element(this));
 *     }
 *     
 *     public void showTab2() {
 *         showPrimaryElement(new Tab2Element(this));
 *     }
 *
 *     // Defaults already to #Content!
 *     // This has just been overridden to explain where the primary elements are displayed.
 *     @Override
 *     public String getRootContentSelector() {
 *         return "#Content";
 *     }
 * }
 * }</pre>
 */
public abstract class ReactiveUiPage extends ReactiveUiBasePage {

    private Element primaryElement;

    /**
     * Constructs a new ReactiveUI page for the specified player.
     *
     * @param playerRef the reference to the player viewing this page
     * @param lifetime  the lifetime scope of this custom page
     */
    public ReactiveUiPage(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime) {
        super(playerRef, lifetime);
    }

    /**
     * Shows the specified element as the primary element, replacing any existing primary element.
     * <p>
     * This method:
     * <ol>
     * <li>Checks if the element is already the primary element (no-op if so)</li>
     * <li>Unloads the previous primary element if one exists</li>
     * <li>Clears the content area</li>
     * <li>Creates and displays the new element</li>
     * </ol>
     * The element will be created at the selector returned by {@link #getRootContentSelector()}.
     *
     * @param element the element to show as the primary element
     */
    public void showPrimaryElement(Element element) {
        if (this.primaryElement == element) {
            return;
        }
        if (this.primaryElement != null) {
            this.primaryElement.onUnload();
        }
        this.primaryElement = element;

        clear();
        this.primaryElement.create(getRootContentSelector());
    }

    /**
     * Clears the content area before showing a new primary element.
     */
    private void clear() {
        UICommandBuilder commands = new UICommandBuilder();
        commands.clear(getRootContentSelector());
        sendUpdate(commands, false);
    }

    /**
     * Gets the root selector where the primary element should be created.
     * <p>
     * This selector identifies the container in your UI where primary elements
     * will be displayed. For example, if your page has a content area with
     * ID "Content", you would return "#Content".
     *
     * @return the CSS selector for the primary element container
     */
    public String getRootContentSelector()
    {
        return "#Content";
    }
}
