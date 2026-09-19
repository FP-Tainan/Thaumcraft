package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.block.entity.NodeBlockEntity;
import net.thaumcraft.item.WispEssenceItem;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

/**
 * O totem de obsidiana carregado: o {@code BlockCosmeticSolid} 8 da 4.2.3.5, o topo dos totens que o mundo gera, com um
 * nó de aura sombrio dentro. Tem a figura do totem comum; quebrado, estoura como um nó, vira ladrilho de obsidiana e
 * solta essências de fogo-fátuo dos aspectos que o nó guardava (uma a cada dez, dos que tinham pelo menos cinco).
 */
public class ChargedObsidianTotemBlock extends BaseEntityBlock {
    public static final MapCodec<ChargedObsidianTotemBlock> CODEC = simpleCodec(ChargedObsidianTotemBlock::new);

    public ChargedObsidianTotemBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ObsidianTotemBlock.PART, ObsidianTotemBlock.Part.BASE)
                .setValue(ObsidianTotemBlock.SUM, 9));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ObsidianTotemBlock.PART, ObsidianTotemBlock.SUM);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return ObsidianTotemBlock.shape(this.defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighbourPos, BlockState neighbour, RandomSource random) {
        return direction.getAxis() == Direction.Axis.Y ? ObsidianTotemBlock.shape(state, level, pos) : state;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NodeBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.NODE, NodeBlockEntity::tick);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.isClientSide()) {
            // o addDestroyEffects: o estouro do nó e o som de receita falhada
            net.thaumcraft.client.NodeClient.burst(level, new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), false);
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, TCSounds.CRAFT_FAIL.value(),
                    net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f, false);
        } else if (!player.isCreative() && level.getBlockEntity(pos) instanceof NodeBlockEntity node) {
            // o harvestBlock: as essências do nó
            var aspects = node.aspects();
            for (Aspect aspect : aspects.getAspects()) {
                int amount = aspects.getAmount(aspect);
                for (int a = 0; a <= amount / 10; a++) {
                    if (amount >= 5) Block.popResource(level, pos, WispEssenceItem.of(aspect));
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    /** Pilha que o bloco derruba: o ladrilho de obsidiana (o {@code damageDropped} 8 → 1). */
    public static ItemStack drop() {
        return new ItemStack(net.thaumcraft.registry.TCBlocks.OBSIDIAN_TILE);
    }
}
