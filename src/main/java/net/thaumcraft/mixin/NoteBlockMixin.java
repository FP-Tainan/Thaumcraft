package net.thaumcraft.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.block.entity.ArcaneEarBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** O ouvido arcano escuta: cada nota de bloco musical tocada no servidor vai para a lista do tique (o NoteBlockEvent.Play). */
@Mixin(NoteBlock.class)
public abstract class NoteBlockMixin {
    @Inject(method = "triggerEvent", at = @At("HEAD"))
    private void thaumcraft$heard(BlockState state, Level level, BlockPos pos, int id, int param, CallbackInfoReturnable<Boolean> result) {
        if (level instanceof ServerLevel server) {
            ArcaneEarBlockEntity.heard(server, pos, state.getValue(NoteBlock.INSTRUMENT).ordinal(), state.getValue(NoteBlock.NOTE));
        }
    }
}
