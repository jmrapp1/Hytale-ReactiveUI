package dev.jonrapp.hytaleReactiveUi.utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.jonrapp.hytaleReactiveUi.events.EventRouter;

/**
 * A convenience record for extracting common context from event callbacks.
 * Bundles the entity reference, store, player reference, and player component.
 */
public record PlayerContext(Ref<EntityStore> ref, Store<EntityStore> store, PlayerRef playerRef, Player player) {
    public static PlayerContext from(EventRouter.EventContext context) {
        var ref = context.getRef();
        var store = context.getStore();
        var playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        assert playerRef != null;
        var player = store.getComponent(ref, Player.getComponentType());
        assert player != null;
        return new PlayerContext(ref, store, playerRef, player);
    }
}