package de.clickism.fgui.mixin;
import net.minecraft.world.item.component.ResolvableProfile;
import org.spongepowered.asm.mixin.Mixin;

//? if >=1.21.10 {
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.gen.Invoker;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
//?}

//? if >=1.21.10 {
@Mixin(ResolvableProfile.Static.class)
//?} else {
/*@Mixin(ResolvableProfile.class)
*///?}
public interface StaticAccessor {
    //? if >=1.21.10 {
    @Invoker("<init>")
    static ResolvableProfile.Static createStatic(Either<GameProfile, ResolvableProfile.Partial> profileOrData, PlayerSkin.Patch mannequinInfo) {
        throw new UnsupportedOperationException();
    }
    //?}
}
