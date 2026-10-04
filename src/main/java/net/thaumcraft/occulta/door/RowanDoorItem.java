package net.thaumcraft.occulta.door;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.thaumcraft.occulta.OccultaBlocks;

/**
 * A <b>Porta de Sorveira</b> na mão.
 *
 * <p>Ela faz uma coisa que nenhuma outra porta do jogo faz: ao ser posta, <b>larga a chave dela aos pés de
 * quem a pôs</b>. A chave já vem marcada com o lugar exato onde a porta ficou, e não há como fazer outra.
 *
 * <p>É aí que o custo dela aparece. A porta custa seis tábuas; a chave, que vale por ela, custa <b>atenção</b>
 * — ela cai no chão como qualquer coisa, e quem não a apanhar fica do lado de fora da própria casa.
 */
public class RowanDoorItem extends DoubleHighBlockItem {
    public RowanDoorItem(Properties properties) {
        super(OccultaBlocks.ROWAN_DOOR, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext onde) {
        BlockPos casa = onde.getClickedPos();
        InteractionResult deu = super.place(onde);
        if (!deu.consumesAction()) return deu;

        Level level = onde.getLevel();
        if (level.isClientSide()) return deu;
        var quem = onde.getPlayer();
        if (quem == null) return deu;

        GlobalPos porta = new GlobalPos(level.dimension(), casa);
        level.addFreshEntity(new ItemEntity(level, quem.getX(), quem.getY() + 0.5, quem.getZ(),
                DoorKeys.chave(porta)));
        return deu;
    }
}
