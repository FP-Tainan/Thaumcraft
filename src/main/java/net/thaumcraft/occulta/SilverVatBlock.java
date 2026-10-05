package net.thaumcraft.occulta;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * A <b>Tina de Prata</b>: o {@code BlockSilverVat} do Witchery.
 *
 * <p>Uma bacia de ferro que se encosta a uma fornalha e <b>apanha o que escorre</b>. Cada vez que a pilha
 * de <b>lingotes de ouro</b> da casa de saída de uma máquina ao lado cresce, há <b>uma chance em cinco</b>
 * de aparecer um <b>pó de prata</b> dentro dela.
 *
 * <p>Ela não tem tela, não tem receita e não tem botão: <b>clica-se nela e tira-se o que lá está</b>. E é
 * de longe o jeito mais barato de arranjar prata no ofício — o outro é matar quem a traz.
 *
 * <p>Repare no que o desenho dela diz: ela <b>cria bicos</b> para os lados em que houver uma máquina, e o
 * nível da prata lá dentro <b>sobe em camadas</b>, uma por oito pós. De fora, vê-se se ela está trabalhando
 * e quanto já juntou, e não é preciso abrir nada.
 */
public class SilverVatBlock extends Block implements EntityBlock {
    public static final MapCodec<SilverVatBlock> CODEC = simpleCodec(SilverVatBlock::new);

    /** A altura dela: os sessenta e quatro centésimos do original. */
    public static final double ALTA = 10.24;

    private static final VoxelShape FORMA = Block.box(0.0, 0.0, 0.0, 16.0, ALTA, 16.0);

    public SilverVatBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<SilverVatBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState feitio, BlockGetter mundo, BlockPos onde,
                                  CollisionContext quem) {
        return FORMA;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState feitio) {
        return true;
    }

    /** <b>Clicar nela tira o que lá está</b>, e é a única coisa que se lhe faz. */
    @Override
    protected InteractionResult useWithoutItem(BlockState feitio, Level mundo, BlockPos onde,
                                               Player quem, net.minecraft.world.phys.BlockHitResult bateu) {
        if (!(mundo instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(onde) instanceof SilverVatBlockEntity tina)) {
            return InteractionResult.PASS;
        }
        ItemStack tem = tina.prata();
        if (tem.isEmpty()) return InteractionResult.PASS;

        var caiu = new net.minecraft.world.entity.item.ItemEntity(level, quem.getX(),
                quem.getY() + 1.0, quem.getZ(), tem.copy());
        caiu.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        level.addFreshEntity(caiu);
        tina.prata(ItemStack.EMPTY);
        return InteractionResult.SUCCESS;
    }

    /** E um comparador ao lado dela lê quanto ela tem. */
    @Override
    protected boolean hasAnalogOutputSignal(BlockState feitio) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState feitio, Level mundo, BlockPos onde,
                                       net.minecraft.core.Direction rumo) {
        if (!(mundo.getBlockEntity(onde) instanceof SilverVatBlockEntity tina)) return 0;
        ItemStack tem = tina.prata();
        if (tem.isEmpty()) return 0;
        return 1 + (int) (14.0f * tem.getCount() / tem.getMaxStackSize());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos onde, BlockState feitio) {
        return new SilverVatBlockEntity(onde, feitio);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level mundo,
                                                                            BlockState feitio,
                                                                            BlockEntityType<T> tipo) {
        if (mundo.isClientSide() || tipo != OccultaBlocks.SILVER_VAT_ENTITY) return null;
        return (nível, onde, feitio2, alma) -> {
            if (nível.getGameTime() % SilverVatBlockEntity.OLHA != 0L) return;
            if (alma instanceof SilverVatBlockEntity tina) {
                SilverVatBlockEntity.bate((ServerLevel) nível, onde, tina);
            }
        };
    }
}
