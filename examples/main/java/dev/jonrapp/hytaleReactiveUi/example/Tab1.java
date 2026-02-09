package dev.jonrapp.hytaleReactiveUi.example;

import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import dev.jonrapp.hytaleReactiveUi.elements.Element;

public class Tab1 extends Element<ExamplePage> {

    public Tab1(ExamplePage pageRef) {
        super(pageRef);
    }

    @Override
    protected void onCreate(String root, UICommandBuilder commands, UIEventBuilder events) {
        commands.append(root, "Example/Tab1.ui");

        // iterate and create elements
        for (int i = 0; i < 10; i++ ){
            IteratedElement element = new IteratedElement(pageRef, i);
            element.create("#IteratedList", i, commands, events);
        }
    }
}
