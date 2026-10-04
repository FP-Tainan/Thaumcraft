package net.thaumcraft.occulta.demon;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A batida do Coração de Demônio: a {@code TileEntityDemonHeart} do Witchery.
 *
 * <p>Ela conta as batidas e, de vinte e cinco em vinte e cinco, toca <b>uma batida de coração</b> no lugar
 * onde está. É tudo o que ela faz, e é o que faz do coração a coisa mais inquietante que se pode guardar
 * numa casa: ele não ataca ninguém, não dá nada, não pede nada — <b>só bate</b>.
 *
 * <p>O som é do lado de cá: um som mandado pelo servidor chegaria a toda a gente ao mesmo tempo e perderia o
 * que ele tem de bom, que é vir <b>daquele canto</b> e de mais lado nenhum.
 */
public class DemonHeartBlockEntity extends BlockEntity {
    private long batidas;

    public DemonHeartBlockEntity(BlockPos onde, BlockState state) {
        super(net.thaumcraft.occulta.OccultaBlocks.DEMON_HEART_ENTITY, onde, state);
    }

    /**
     * Quantas batidas do relógio já contou.
     *
     * <p>É o que o desenhista usa para saber <b>onde a onda vai</b>: o inchaço do músculo e o som da batida
     * saem da mesma conta, e é por isso que se vê o coração fazer o barulho que se ouve.
     */
    public long batidas() {
        return this.batidas;
    }

    /** Uma batida do relógio; de vinte e cinco em vinte e cinco, uma do coração. */
    public void bate() {
        this.batidas++;
        if (this.batidas % DemonHeartBlock.BATE_DE != 0L) return;
        if (this.level == null) return;

        this.level.playLocalSound(this.getBlockPos().getX() + 0.5, this.getBlockPos().getY() + 0.5,
                this.getBlockPos().getZ() + 0.5, net.thaumcraft.occulta.OccultaSounds.HEARTBEAT.value(),
                SoundSource.BLOCKS, 0.8f, 1.0f, false);
    }
}
