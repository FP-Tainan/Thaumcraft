package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.block.entity.ManaPodBlockEntity;
import net.thaumcraft.item.ManaBeanItem;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A vagem de mana: o {@code BlockManaPod} da 4.2.3.5. Pendura embaixo de uma tora, só em bioma mágico, e cresce de zero
 * a sete (uma vez em trinta a cada tique ao acaso), brilhando do tamanho que tem. A partir do dois, quebrada, dá um
 * feijão de mana do aspecto dela (no sete, dois de cada três vezes dá dois). Quanto maior, mais dura.
 */
public class ManaPodBlock extends BaseEntityBlock {
    public static final MapCodec<ManaPodBlock> CODEC = simpleCodec(ManaPodBlock::new);
    public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
    /** As toras em que ela pendura: as do jogo de 2014 e as mágicas. */
    public static final TagKey<Block> LOGS = TagKey.create(net.minecraft.core.registries.Registries.BLOCK, Thaumcraft.id("mana_pod_logs"));
    /** O {@code BiomeDictionary.Type.MAGICAL}. */
    public static final TagKey<Biome> MAGICAL = TagKey.create(net.minecraft.core.registries.Registries.BIOME,
            net.minecraft.resources.Identifier.fromNamespaceAndPath("c", "is_magical"));
    /** O {@code setBlockBoundsBasedOnState}: do topo até 12, 10, 8, 6, 5, 4, 3 e 2 pixels. */
    private static final int[] BOTTOM = {12, 10, 8, 6, 5, 4, 3, 2};
    private static final VoxelShape[] SHAPES = new VoxelShape[8];

    static {
        for (int i = 0; i < 8; i++) SHAPES[i] = Block.box(4.0, BOTTOM[i], 4.0, 12.0, 16.0, 12.0);
    }

    public ManaPodBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ManaPodBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(AGE)];
    }

    /** O {@code canBlockStay}: embaixo de uma tora, em bioma mágico. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canHangAt(level, pos);
    }

    public static boolean canHangAt(LevelReader level, BlockPos pos) {
        return level.getBiome(pos).is(MAGICAL) && level.getBlockState(pos.above()).is(LOGS);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
                                     BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        return !this.canSurvive(state, level, pos) ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    /** O {@code updateTick}: sem apoio, cai; senão, uma vez em trinta, cresce. */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!this.canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
        } else if (level.getRandom().nextInt(30) == 0 && level.getBlockEntity(pos) instanceof ManaPodBlockEntity pod) {
            pod.checkGrowth();
        }
    }

    /** O {@code getBlockHardness}: a dureza dividida por oito menos o tamanho. */
    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return super.getDestroyProgress(state, player, level, pos) * (8 - state.getValue(AGE));
    }

    /** O {@code getDrops}: do tamanho dois em diante, o feijão do aspecto dela (Herba se não tiver). */
    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = new ArrayList<>();
        int age = state.getValue(AGE);
        if (age < 2) return drops;
        int count = age == 7 && params.getLevel().getRandom().nextFloat() > 0.33f ? 2 : 1;
        Aspect aspect = Aspects.PLANT;
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof ManaPodBlockEntity pod && pod.aspect != null) {
            aspect = pod.aspect;
        }
        for (int i = 0; i < count; i++) drops.add(ManaBeanItem.of(aspect));
        return drops;
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(net.thaumcraft.registry.TCItems.MANA_BEAN);
    }

    @Nullable
    public static ManaPodBlockEntity pod(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof ManaPodBlockEntity pod ? pod : null;
    }
}
