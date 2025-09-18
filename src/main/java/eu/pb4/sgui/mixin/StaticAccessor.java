package eu.pb4.sgui.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.component.type.ProfileComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ProfileComponent.Static.class)
public interface StaticAccessor {
    @Invoker("<init>")
    static ProfileComponent.Static createStatic(Either<GameProfile, ProfileComponent.Data> profileOrData, SkinTextures.MannequinInfo mannequinInfo) {
        throw new UnsupportedOperationException();
    }
}
