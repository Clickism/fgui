package eu.pb4.sgui.mixin;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.sync.TrackedSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ScreenHandler.class)
public interface ScreenHandlerAccessor {
    @Accessor
    TrackedSlot getTrackedCursorSlot();
}
