package dev.jonrapp.hytaleReactiveUi.events;

import com.hypixel.hytale.codec.KeyedCodec;

import javax.annotation.Nonnull;
import java.util.List;

public interface EventHandler<T> {

    boolean handle(@Nonnull EventRouter.EventContext context);

    @Nonnull
    List<KeyedCodec<?>> getParameterCodecs();
}
