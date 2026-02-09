package dev.jonrapp.hytaleReactiveUi.example;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.jonrapp.hytaleReactiveUi.events.EventBinding;
import dev.jonrapp.hytaleReactiveUi.pages.ReactiveUiPage;

import javax.annotation.Nonnull;

public class ExamplePage extends ReactiveUiPage {

    public ExamplePage(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss);
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commands, @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        commands.append("Example/ExamplePage.ui");

        bindEvent(
                CustomUIEventBindingType.Activating,
                "#Tab1",
                events,
                EventBinding.action("tab-1-selected")
                        .onEvent(context -> showPrimaryElement(new Tab1(this)))
        );

        bindEvent(
                CustomUIEventBindingType.Activating,
                "#Tab2",
                events,
                EventBinding.action("tab-2-selected")
                        .onEvent(context -> showPrimaryElement(new Tab2(this)))
        );

        showPrimaryElement(new Tab1(this));
    }


    @Override
    public String getRootContentSelector() {
        return "#Content";
    }
}
