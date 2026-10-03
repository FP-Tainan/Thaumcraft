package net.thaumcraft.occulta.wolf;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.occulta.OccultaBlocks;

/**
 * A alma da Estátua do Lobisomem: a {@code TileEntityStatueWerewolf} do Witchery.
 *
 * <p>Ela <b>não guarda nada e não faz nada</b> — no original também não: a alma existe só para o jogo ter
 * onde pendurar o desenho da estátua, que é um modelo e não um bloco. O que a estátua sabe está todo no
 * {@linkplain WerewolfLadder degrau de quem fala com ela}, e isso é do jogador.
 */
public class WerewolfStatueBlockEntity extends BlockEntity {
    public WerewolfStatueBlockEntity(BlockPos onde, BlockState feitio) {
        super(OccultaBlocks.WEREWOLF_STATUE_ENTITY, onde, feitio);
    }
}
