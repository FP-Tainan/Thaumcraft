package net.thaumcraft.block.eldritch;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.thaumcraft.block.entity.eldritch.EldritchAltarBlockEntity;
import net.thaumcraft.block.entity.eldritch.EldritchCapBlockEntity;
import net.thaumcraft.block.entity.eldritch.EldritchObeliskBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

/**
 * As peças do anel eldritch: os números 0 a 3 do {@code BlockEldritch} da 4.2.3.5 — o altar, o obelisco (o pé, com o
 * desenho, e os quatro blocos de cima, que só ocupam lugar) e o capstone. Nenhuma tem desenho de bloco: tudo é dos
 * desenhistas das entidades de bloco. Quebrada uma, as outras peças a até três blocos de lado e dois de altura somem, e
 * há um estouro que não quebra nada; nenhuma deixa nada.
 */
public class EldritchStoneBlock extends BaseEntityBlock implements EldritchRingPiece {
    public static final MapCodec<EldritchStoneBlock> CODEC = simpleCodec(p -> new EldritchStoneBlock(Kind.ALTAR, p));

    public enum Kind { ALTAR, OBELISK, CAPSTONE }

    public final Kind kind;

    public EldritchStoneBlock(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch (this.kind) {
            case ALTAR -> new EldritchAltarBlockEntity(pos, state);
            case OBELISK -> new EldritchObeliskBlockEntity(pos, state);
            case CAPSTONE -> new EldritchCapBlockEntity(pos, state);
        };
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return switch (this.kind) {
            case ALTAR -> level.isClientSide() ? null : createTickerHelper(type, TCBlockEntities.ELDRITCH_ALTAR, EldritchAltarBlockEntity::tick);
            case OBELISK -> createTickerHelper(type, TCBlockEntities.ELDRITCH_OBELISK, EldritchObeliskBlockEntity::tick);
            default -> null;
        };
    }

    /** O olho eldritch posto no altar: até quatro; do terceiro em diante o altar passa a chamar guardiões. */
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {
        if (this.kind != Kind.ALTAR || player.isShiftKeyDown() || !stack.is(TCItems.ELDRITCH_EYE)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof EldritchAltarBlockEntity altar && altar.getEyes() < 4) {
            if (altar.getEyes() >= 2) {
                altar.setSpawner(true);
                altar.setSpawnType((byte) 1);
            }
            altar.setEyes((byte) (altar.getEyes() + 1));
            altar.checkForMaze();
            stack.shrink(1);
            altar.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
            level.playSound(null, pos, TCSounds.CRYSTAL.value(), SoundSource.BLOCKS, 0.2f, 1.0f);
        }
        return InteractionResult.SUCCESS;
    }

    /** O {@code breakBlock}: as peças vizinhas do anel somem e há um estouro que não fura o chão. */
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        super.affectNeighborsAfterRemoval(state, level, pos, moved);
        EldritchRingPiece.breakRing(level, pos);
    }
}
