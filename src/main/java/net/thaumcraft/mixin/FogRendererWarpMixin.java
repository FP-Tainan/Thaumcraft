package net.thaumcraft.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.thaumcraft.client.WarpClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** A névoa da distorção (o {@code fogDensityEvent} do {@code RenderEventHandler}). */
@Mixin(FogRenderer.class)
public abstract class FogRendererWarpMixin {
    @Inject(method = "setupFog", at = @At("RETURN"))
    private void thaumcraft$warpFog(Camera camera, int renderDistanceInChunks, DeltaTracker deltaTracker, float darkenWorldAmount,
                                    ClientLevel level, CallbackInfoReturnable<FogData> info) {
        WarpClient.fog(info.getReturnValue());
    }
}
