package dev.jonrapp.hytaleReactiveUi.pages;

import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.CustomPage;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.jonrapp.hytaleReactiveUi.bindings.UIBindingManager;
import dev.jonrapp.hytaleReactiveUi.events.EventBinding;
import dev.jonrapp.hytaleReactiveUi.events.EventCodec;
import dev.jonrapp.hytaleReactiveUi.events.EventHandler;
import dev.jonrapp.hytaleReactiveUi.events.EventRouter;
import dev.jonrapp.hytaleReactiveUi.support.UIEventSupport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Base class for custom UI pages in Hytale using the ReactiveUI framework.
 * <p>
 * This class extends Hytale's {@link CustomUIPage} and provides an event-driven architecture
 * for handling UI interactions. It manages event routing and codec registration, allowing
 * UI elements to respond to client-side events through registered handlers.
 * <p>
 * Subclasses should implement their UI structure and event handling logic by utilizing
 * the {@link EventRouter} and {@link EventCodec} provided by this class.
 */
public abstract class ReactiveUiBasePage extends CustomUIPage {
    @Nonnull
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    @Nonnull
    private final EventCodec eventCodec;
    @Nonnull
    private final EventRouter eventRouter;
    @Nonnull
    private final UIEventSupport eventSupport;

    /**
     * Constructs a new ReactiveUI page for the specified player.
     *
     * @param playerRef the reference to the player viewing this page
     * @param lifetime the lifetime scope of this custom page
     */
    public ReactiveUiBasePage(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime) {
        super(playerRef, lifetime);
        this.eventCodec = new EventCodec();
        this.eventRouter = new EventRouter(this.eventCodec);
        this.eventSupport = new UIEventSupport(this.eventRouter, this::sendUpdate);
    }

    /**
     * Sends a UI update to the client with optional commands and event bindings.
     * <p>
     * This method executes on the world thread to ensure thread safety when accessing
     * player components. It updates the custom page displayed to the player.
     *
     * @param commandBuilder the UI commands to send (e.g., element creation, updates), or null
     * @param eventBuilder the event bindings to register on the client, or null
     * @param clear whether to clear existing UI elements before applying updates
     */
    public void sendUpdate(@Nullable UICommandBuilder commandBuilder, @Nullable UIEventBuilder eventBuilder, boolean clear) {
        Ref<EntityStore> ref = this.playerRef.getReference();
        if (ref != null) {
            Store<EntityStore> store = ref.getStore();
            World world = store.getExternalData().getWorld();
            world.execute(() -> {
                if (ref.isValid()) {
                    Player playerComponent = store.getComponent(ref, Player.getComponentType());

                    assert playerComponent != null;

                    playerComponent.getPageManager().updateCustomPage(new CustomPage(this.getClass().getName(), false, clear, this.lifetime, commandBuilder != null ? commandBuilder.getCommands() : UICommandBuilder.EMPTY_COMMAND_ARRAY, eventBuilder != null ? eventBuilder.getEvents() : UIEventBuilder.EMPTY_EVENT_BINDING_ARRAY));
                }
            });
        }
    }

    /**
     * Handles incoming event data from the client.
     * <p>
     * This method decodes the raw JSON event data sent from the client, validates it,
     * and routes it to the appropriate event handler via the {@link EventRouter}.
     * If the event data doesn't contain an "Action" field, a UI update is triggered.
     *
     * @param ref the entity reference
     * @param store the entity store
     * @param rawData the raw JSON string containing event data from the client
     * @throws RuntimeException if JSON decoding fails
     */
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, String rawData) {
        ExtraInfo extraInfo = ExtraInfo.THREAD_LOCAL.get();

        EventCodec.EventData data;
        try {
            data = this.eventCodec.getEventCodec().decodeJson(new RawJsonReader(rawData.toCharArray()), extraInfo);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        extraInfo.getValidationResults().logOrThrowValidatorExceptions(LOGGER);
        if (!data.contains("Action")) {
            sendUpdate();
            return;
        }

        this.eventRouter.routeEvent(ref, store, data);
    }

    /**
     * Sends a UI update to the client with commands only (no event bindings).
     *
     * @param commandBuilder the UI commands to send, or null
     * @param clear whether to clear existing UI elements before applying updates
     */
    public void sendUpdate(@Nullable UICommandBuilder commandBuilder, boolean clear) {
        this.sendUpdate(commandBuilder, null, clear);
    }

    /**
     * Gets the event codec used for encoding and decoding event data.
     *
     * @return the event codec instance
     */
    @Nonnull
    public EventCodec getEventCodec() {
        return eventCodec;
    }

    /**
     * Gets the event router used for registering and routing event handlers.
     *
     * @return the event router instance
     */
    @Nonnull
    public EventRouter getEventRouter() {
        return eventRouter;
    }

    public <T> void registerEventHandler(@Nonnull String action, @Nonnull EventHandler<T> handler) {
        eventSupport.registerEventHandler(action, this, handler);
    }

    protected void bindEvent(@Nonnull CustomUIEventBindingType bindingType, @Nonnull String selector,
                             @Nonnull UIEventBuilder events, @Nonnull EventBinding eventBinding) {
        eventSupport.bindEvent(bindingType, selector, events, eventBinding, this);
    }

    public void unloadEvents() {
        eventSupport.unloadEvents();
    }

    @Nonnull
    public UIBindingManager getBindingManager() {
        return eventSupport.getBindingManager();
    }
}
