package net.thaumcraft.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.thaumcraft.client.WarpClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Os filtros de tela da distorção (os {@code ShaderGroup} do {@code RenderEventHandler}): depois do mundo desenhado, cada
 * poção ativa passa o seu por cima, na ordem do original.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererWarpMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private CrossFrameResourcePool resourcePool;

    @Shadow
    @Final
    private RenderTarget mainRenderTarget;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;doEntityOutline()V", shift = At.Shift.AFTER))
    private void thaumcraft$warpShaders(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo info) {
        for (var id : WarpClient.activeShaders()) {
            PostChain chain = this.minecraft.getShaderManager().getPostChain(id, LevelTargetBundle.MAIN_TARGETS);
            if (chain != null) chain.process(this.mainRenderTarget, this.resourcePool);
        }
    }
}
