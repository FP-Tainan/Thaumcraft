package net.thaumcraft.occulta;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;

/**
 * O Altar da Bruxa: o {@code BlockAltar} do Witchery.
 *
 * <p>Um altar sozinho não é nada. <b>Seis deles</b>, encostados de lado formando dois por três, fazem um altar de
 * verdade — e o primeiro deles passa a ser o que manda: é ele que junta o poder da natureza em volta e o guarda
 * para quem precisar.
 *
 * <p>A conta que diz se os seis fazem um altar é a do original: cada bloco tem de ter <b>dois ou três</b> vizinhos
 * de altar ao lado, e o bando todo tem de ser <b>exatamente seis</b>. Dois por três é a única forma que fecha isso.
 */
public class AltarBlock extends BaseEntityBlock {
    public static final MapCodec<AltarBlock> CODEC = simpleCodec(AltarBlock::new);

    /** Se este bloco faz parte de um altar inteiro: muda a cara dele, como a marca do original. */
    public static final BooleanProperty JOINED = BooleanProperty.create("joined");

    /** Quantos blocos fazem um altar. */
    public static final int PIECES = 6;

    public AltarBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(JOINED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(JOINED);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AltarBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, OccultaBlocks.WITCH_ALTAR_ENTITY, AltarBlockEntity::tick);
    }

    /**
     * Posto um bloco, o bando é contado outra vez.
     *
     * <p><b>Só quando a pedra é nova.</b> Ao marcar quem manda, o miolo troca a cara da pedra — e trocar a cara é
     * uma mudança de estado, que o jogo de hoje também faz passar por aqui. Sem este cuidado, a conta recomeçava
     * do meio dela mesma e cada pedra acabava a achar que quem manda é outra.
     */
    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) {
        super.onPlace(state, level, pos, old, moving);
        if (old.is(this)) return;
        if (level instanceof ServerLevel server) updateMultiblock(server, pos, null);
    }

    /** Tirado um bloco, também — e o que saiu não conta mais. */
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moving) {
        updateMultiblock(level, pos, pos);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level instanceof ServerLevel server) updateMultiblock(server, pos, null);
    }

    /** O clique diz quanto poder o altar tem, que é o que a tela do original mostra. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof AltarBlockEntity altar)) return InteractionResult.PASS;
        AltarBlockEntity manda = altar.core();
        if (manda == null) {
            player.sendSystemMessage(Component.translatable("tc.occulta.altar.alone"));
            return InteractionResult.SUCCESS;
        }
        manda.refresh();
        player.sendSystemMessage(Component.translatable("tc.occulta.altar.power",
                (int) manda.power(), (int) manda.maxPower(), manda.rechargeScale()));
        return InteractionResult.SUCCESS;
    }

    /**
     * O {@code updateMultiblock} do original: anda pelos altares encostados, vê se o bando fecha um altar de
     * verdade e diz a todos eles quem manda.
     *
     * @param exclude o bloco que está saindo, que não conta
     */
    public static void updateMultiblock(ServerLevel level, BlockPos start, BlockPos exclude) {
        List<BlockPos> visited = new ArrayList<>();
        List<BlockPos> toVisit = new ArrayList<>();
        toVisit.add(start);
        boolean valid = true;

        while (!toVisit.isEmpty()) {
            BlockPos onde = toVisit.remove(0);
            int vizinhos = 0;
            for (Direction lado : Direction.Plane.HORIZONTAL) {
                BlockPos ao = onde.relative(lado);
                if (!level.getBlockState(ao).is(OccultaBlocks.WITCH_ALTAR)) continue;
                if (!visited.contains(ao) && !toVisit.contains(ao)) toVisit.add(ao);
                vizinhos++;
            }
            if (onde.equals(exclude)) continue;
            if (vizinhos < 2 || vizinhos > 3) valid = false;
            visited.add(onde);
        }

        BlockPos core = valid && visited.size() == PIECES ? visited.get(0) : null;
        for (BlockPos onde : visited) {
            if (level.getBlockEntity(onde) instanceof AltarBlockEntity altar) altar.setCore(core);
        }
        if (exclude != null && level.getBlockEntity(exclude) instanceof AltarBlockEntity saindo) {
            saindo.setCore(null);
        }
    }
}
