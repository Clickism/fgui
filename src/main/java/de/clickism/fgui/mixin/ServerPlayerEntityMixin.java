package de.clickism.fgui.mixin;

import com.mojang.authlib.GameProfile;
import de.clickism.fgui.impl.PlayerExtensions;
import de.clickism.fgui.virtual.SguiScreenHandlerFactory;
import de.clickism.fgui.virtual.VirtualScreenHandlerInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalInt;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

//? if neoforge {
/*import java.util.function.Consumer;
import net.minecraft.network.RegistryFriendlyByteBuf;
*///?}

//? if <=1.21.5 {
/*import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
*///?}

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin extends Player implements PlayerExtensions {
    @Unique
    private boolean sgui$ignoreNext = false;

    //? if >=1.21.8 {
    public ServerPlayerEntityMixin(Level world, GameProfile gameProfile) {
        super(world, gameProfile);
    }
    //?} else {
    /*public ServerPlayerEntityMixin(Level world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }
    *///?}

    @Shadow
    public abstract void doCloseContainer();

    @Inject(
            //? if fabric {
            method = "openMenu",
            //?} else
            //method = "openMenu(Lnet/minecraft/world/MenuProvider;Ljava/util/function/Consumer;)Ljava/util/OptionalInt;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;closeContainer()V",
                    shift = At.Shift.BEFORE
            )
    )
    private void sgui$dontForceCloseFor(MenuProvider factory,
                                        //? if neoforge
                                        //Consumer<RegistryFriendlyByteBuf> extraDataWriter,
                                        CallbackInfoReturnable<OptionalInt> cir) {
        if (factory instanceof SguiScreenHandlerFactory<?> sguiScreenHandlerFactory && !sguiScreenHandlerFactory.gui().resetMousePosition()) {
            this.sgui$ignoreNext = true;
        }
    }

    @Inject(method = "closeContainer", at = @At("HEAD"), cancellable = true)
    private void sgui$ignoreClosing(CallbackInfo ci) {
        if (this.sgui$ignoreNext) {
            this.sgui$ignoreNext = false;
            this.doCloseContainer();
            ci.cancel();
        }
    }

    @Inject(method = "die", at = @At("TAIL"))
    private void sgui$onDeath(DamageSource source, CallbackInfo ci) {
        if (this.containerMenu instanceof VirtualScreenHandlerInterface handler) {
            handler.getGui().close(true);
        }
    }

    @Override
    public void sgui$ignoreNextClose() {
        this.sgui$ignoreNext = true;
    }
}
