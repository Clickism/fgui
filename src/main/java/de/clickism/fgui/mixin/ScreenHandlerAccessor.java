package de.clickism.fgui.mixin;
//? if >=1.21.5 {
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.RemoteSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerMenu.class)
public interface ScreenHandlerAccessor {
    @Accessor
    RemoteSlot getRemoteCarried();
}
//?}
