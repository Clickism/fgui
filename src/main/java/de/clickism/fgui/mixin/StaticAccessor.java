package de.clickism.fgui.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.component.ResolvableProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ResolvableProfile.Static.class)
public interface StaticAccessor {
    @Invoker("<init>")
    static ResolvableProfile.Static createStatic(Either<GameProfile, ResolvableProfile.Partial> profileOrData, PlayerSkin.Patch mannequinInfo) {
        throw new UnsupportedOperationException();
    }
}
