package de.clickism.fgui.mixin;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;

//? if >=1.21.5 {
import net.minecraft.world.inventory.RemoteSlot;
import org.spongepowered.asm.mixin.gen.Accessor;
//?}

@Mixin(AbstractContainerMenu.class)
public interface ScreenHandlerAccessor {
    //? if >=1.21.5 {
    @Accessor
    RemoteSlot getRemoteCarried();
    //?}
}
