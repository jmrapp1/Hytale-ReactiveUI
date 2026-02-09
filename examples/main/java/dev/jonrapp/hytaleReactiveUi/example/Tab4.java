package dev.jonrapp.hytaleReactiveUi.example;

import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import dev.jonrapp.hytaleReactiveUi.elements.Element;
import dev.jonrapp.hytaleReactiveUi.events.EventBinding;
import dev.jonrapp.hytaleReactiveUi.utils.PlayerContext;

public class Tab4 extends Element<ExamplePage> {

    public Tab4(ExamplePage pageRef) {
        super(pageRef);
    }

    @Override
    protected void onCreate(String root, UICommandBuilder commands, UIEventBuilder events) {
        commands.append(root, "Example/Tab4.ui");

        bindEvent(
                CustomUIEventBindingType.Activating,
                "#BackBtn",
                events,
                EventBinding.action("tab-4-back-btn-clicked")
                        .onEvent(context -> handleBackBtn(PlayerContext.from(context)))
        );
    }

    private void handleBackBtn(PlayerContext context) {
        context.player().sendMessage(Message.raw("We're supposed to go back now!"));
        sendUpdate(new UICommandBuilder(), new UIEventBuilder(), false);
    }
}