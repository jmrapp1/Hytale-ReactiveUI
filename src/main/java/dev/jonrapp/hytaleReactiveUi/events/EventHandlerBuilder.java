package dev.jonrapp.hytaleReactiveUi.events;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EventHandlerBuilder {

    private final List<KeyedCodec<?>> parameterCodecs = new ArrayList<>();

    private EventHandlerBuilder() {
    }

    @Nonnull
    public static EventHandlerBuilder create() {
        return new EventHandlerBuilder();
    }

    @Nonnull
    public <T> EventHandlerBuilder withParameter(@Nonnull String key, @Nonnull Codec<T> codec) {
        parameterCodecs.add(new KeyedCodec<>(key, codec));
        return this;
    }

    @Nonnull
    public EventHandler<Void> build(@Nonnull Consumer<EventRouter.EventContext> handler) {
        return new EventHandler<Void>() {
            @Override
            public boolean handle(@Nonnull EventRouter.EventContext context) {
                handler.accept(context);
                return true;
            }

            @Nonnull
            @Override
            public List<KeyedCodec<?>> getParameterCodecs() {
                return parameterCodecs;
            }
        };
    }

    @Nonnull
    public EventHandler<Void> buildConditional(@Nonnull java.util.function.Predicate<EventRouter.EventContext> handler) {
        return new EventHandler<Void>() {
            @Override
            public boolean handle(@Nonnull EventRouter.EventContext context) {
                return handler.test(context);
            }

            @Nonnull
            @Override
            public List<KeyedCodec<?>> getParameterCodecs() {
                return parameterCodecs;
            }
        };
    }
}
