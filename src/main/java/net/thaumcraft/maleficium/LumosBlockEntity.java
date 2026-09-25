package net.thaumcraft.maleficium;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * O miolo do Lumos.
 *
 * <p>Não guarda nada: existe só para o desenhista ter onde se pendurar, como o do Nitor. No original o Lumos é
 * uma luz invisível que solta uma faísca de vez em quando, e quem joga pediu que ele acendesse também uma chama
 * branca — um Nitor branco — para se enxergar de longe.
 */
public class LumosBlockEntity extends BlockEntity {
    public LumosBlockEntity(BlockPos pos, BlockState state) {
        super(MaleficiumBlocks.LUMOS_ENTITY, pos, state);
    }
}
