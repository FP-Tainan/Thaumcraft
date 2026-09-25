package net.thaumcraft.maleficium;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;

/**
 * O Lumos: o {@code BlockLumos} do Tainted Magic.
 *
 * <p>Uma luzinha que o foco e o anel de Lumos deixam pelo caminho — não se pega, não atrapalha, ilumina como uma
 * tocha e solta faíscas de vez em quando. Quebrada, estala como gelo e se desfaz em nove faíscas.
 */
public class LumosBlock extends BaseEntityBlock {
    public static final MapCodec<LumosBlock> CODEC = simpleCodec(LumosBlock::new);

    /** Os efeitos do lado de quem vê, que o cliente pendura aqui ao abrir. */
    public interface ClientEffects {
        void sparkle(Level level, BlockPos pos, RandomSource random, boolean breaking);
    }

    public static ClientEffects clientEffects = (level, pos, random, breaking) -> {
    };

    public LumosBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<LumosBlock> codec() {
        return CODEC;
    }

    /**
     * O bloco em si não se desenha — no original a textura dele é {@code thaumcraft:blank}. O que se vê é a chama
     * branca que o {@code LumosRenderer} acende.
     */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LumosBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // a caixa do original, de três a sete décimos, só para o cursor ter onde pegar
        return Block.box(4.8, 4.8, 4.8, 11.2, 11.2, 11.2);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean canBeReplaced(BlockState state, net.minecraft.world.item.context.BlockPlaceContext context) {
        return true;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(15) == 0) clientEffects.sparkle(level, pos, random, false);
    }

    /** Quebrado, estala como gelo e se desfaz em faíscas: o {@code breakBlock} do original. */
    @Override
    public net.minecraft.world.level.block.state.BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.player.Player player) {
        if (level.isClientSide()) {
            for (int i = 0; i < 9; i++) clientEffects.sparkle(level, pos, level.getRandom(), true);
        } else {
            level.playSound(null, pos, net.thaumcraft.registry.TCSounds.ICE.value(), SoundSource.BLOCKS,
                    0.3f, 1.1f + level.getRandom().nextFloat() * 0.1f);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
