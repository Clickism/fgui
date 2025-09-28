package eu.pb4.sgui.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.player.SkinTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ProfileComponent.Static.class)
public interface StaticAccessor {
    @Invoker("<init>")
    static ProfileComponent.Static createStatic(Either<GameProfile, ProfileComponent.Data> profileOrData, SkinTextures.SkinOverride mannequinInfo) {
        throw new UnsupportedOperationException();
    }
}
