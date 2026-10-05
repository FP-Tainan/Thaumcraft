package net.thaumcraft.occulta;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * O <b>Cálice</b> na mão: os dois itens do original, o vazio e o cheio.
 *
 * <p>São dois itens e um bloco só. O que cada um faz de diferente é <b>com que feitio o põe</b>: o cheio
 * põe o bloco cheio, o vazio põe o bloco vazio, e depois disso são o mesmo bloco. No original esta mesma conta é
 * feita a seguir à colocação, mexendo na alma do bloco já posto; aqui ela entra no feitio antes de o bloco
 * existir, que dá o mesmo e poupa uma troca.
 */
public class ChaliceItem extends BlockItem {
    private final boolean cheio;

    public ChaliceItem(boolean cheio, Properties properties) {
        super(OccultaBlocks.CHALICE, properties);
        this.cheio = cheio;
    }

    @Override
    protected @Nullable BlockState getPlacementState(BlockPlaceContext onde) {
        BlockState feitio = super.getPlacementState(onde);
        return feitio == null ? null : feitio.setValue(ChaliceBlock.CHEIO, this.cheio);
    }
}
