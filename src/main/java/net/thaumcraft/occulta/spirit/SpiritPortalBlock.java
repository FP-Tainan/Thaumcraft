package net.thaumcraft.occulta.spirit;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.occulta.OccultaBlocks;

/**
 * O Portal do Espírito: a {@code BlockSpiritPortal} do Witchery.
 *
 * <p>Ele não se faz: <b>acende-se</b>. No Mundo dos Espíritos, monta-se uma moldura de <b>camadas de neve</b> à
 * volta de um vão de dois por dois e se derrama lá dentro uma fonte de <b>Espírito Fluente</b>. O vão se fecha
 * de portal, e quem o atravessa <b>volta ao mundo de cá em fantasma</b>.
 *
 * <p>Mas só quem tem crédito: o <b>Rito da Manifestação</b> dá segundos de manifestação, e sem eles o portal
 * deixa passar e não faz nada. É a conta que o {@code canPlayerManifest} guarda.
 */
public class SpiritPortalBlock extends Block {
    public static final MapCodec<SpiritPortalBlock> CODEC = simpleCodec(SpiritPortalBlock::new);

    /** Em que eixo o vão está deitado, como o portal do Nether do jogo. */
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    /** A cor dele: o {@code 65382} que o original devolve, que é um verde de água. */
    public static final int TINT = 0xFF00FF66;

    /**
     * O que faz a moldura: <b>neve</b>, nas duas formas que o jogo tem.
     *
     * <p><b>Desvio declarado:</b> o original aceita só a <b>camada</b> de neve — o {@code portalFrameBlock} é o
     * {@code Blocks.field_150433_aE}. Só que uma camada de neve precisa de chão firme por baixo, e a fileira de
     * cima da moldura fica sobre o <b>vão</b>, que é ar: a moldura do original <b>não se consegue montar</b>.
     * Aqui vale também o <b>bloco</b> de neve, que se empilha, e a moldura passa a ser construível sem deixar
     * de ser de neve.
     */
    public static final Block FRAME = Blocks.SNOW_BLOCK;

    /** Se aquilo serve de moldura. */
    public static boolean frame(BlockState feitio) {
        return feitio.is(Blocks.SNOW) || feitio.is(Blocks.SNOW_BLOCK);
    }

    /** O vão é de dois por dois, como no original — e não dois por três, como o do Nether. */
    public static final int WIDTH = 2;
    public static final int HEIGHT = 2;

    private static final VoxelShape X_SHAPE = Block.box(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);
    private static final VoxelShape Z_SHAPE = Block.box(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);

    public SpiritPortalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(AXIS) == Direction.Axis.Z ? Z_SHAPE : X_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                           CollisionContext context) {
        return Shapes.empty();
    }

    /** Ele não se apanha: quebrado, some. */
    @Override
    protected java.util.List<net.minecraft.world.item.ItemStack> getDrops(
            BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder params) {
        return java.util.List.of();
    }

    /**
     * Tirada a moldura, o portal cai: é o {@code onNeighborBlockChange} do original.
     *
     * <p>A conta dele é a mesma do portal do Nether: desce até ao pé do vão, confere que há moldura por baixo,
     * conta a altura, confere a moldura por cima e a dos dois lados. Faltando qualquer uma, a casa some.
     */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block vizinho,
                                   @org.jetbrains.annotations.Nullable
                                   net.minecraft.world.level.redstone.Orientation lado, boolean moveu) {
        super.neighborChanged(state, level, pos, vizinho, lado, moveu);
        if (level.isClientSide()) return;
        if (!standing(level, pos, state.getValue(AXIS))) {
            level.removeBlock(pos, false);
        }
    }

    /** Se o vão daquela casa ainda está inteiro, com moldura em baixo, em cima e dos dois lados. */
    private static boolean standing(Level level, BlockPos pos, Direction.Axis eixo) {
        int dx = eixo == Direction.Axis.X ? 1 : 0;
        int dz = eixo == Direction.Axis.Z ? 1 : 0;

        BlockPos pé = pos;
        while (level.getBlockState(pé.below()).is(OccultaBlocks.SPIRIT_PORTAL)) pé = pé.below();
        if (!frame(level.getBlockState(pé.below()))) return false;

        int alto = 1;
        while (alto < HEIGHT + 1 && level.getBlockState(pé.above(alto)).is(OccultaBlocks.SPIRIT_PORTAL)) alto++;
        if (alto != HEIGHT || !frame(level.getBlockState(pé.above(alto)))) return false;

        // e de lado: ou a casa ao lado é portal, ou é moldura
        for (int sinal = -1; sinal <= 1; sinal += 2) {
            BlockPos ao = pos.offset(dx * sinal, 0, dz * sinal);
            if (!level.getBlockState(ao).is(OccultaBlocks.SPIRIT_PORTAL)
                    && !frame(level.getBlockState(ao))) {
                return false;
            }
        }
        return true;
    }

    /** Onde ele está, o chão não existe: quem pisa, passa. */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity quem,
                                InsideBlockEffectApplier efeitos, boolean atravessou) {
        super.entityInside(state, level, pos, quem, efeitos, atravessou);
        if (!(level instanceof ServerLevel server) || !SpiritWorld.is(server)) return;
        if (!(quem instanceof ServerPlayer gente)) return;
        if (quem.isPassenger() || quem.isVehicle()) return;
        SpiritWorld.manifest(gente);
    }

    /**
     * Tenta acender um portal naquele lugar: o {@code tryToCreatePortal} do original, passo por passo.
     *
     * <p>Ele mede se a moldura está de pé num dos dois eixos, desliza uma casa se a batida caiu na metade
     * errada do vão, e então confere as dezesseis casas em volta: as de fora têm de ser moldura, e as de dentro
     * têm de estar vazias ou já cheias de Espírito Fluente.
     *
     * @return se acendeu
     */
    public static boolean tryToCreate(LevelAccessor level, BlockPos onde) {
        int dx = 0;
        int dz = 0;
        if (frame(level.getBlockState(onde.west())) || frame(level.getBlockState(onde.east()))) dx = 1;
        if (frame(level.getBlockState(onde.north())) || frame(level.getBlockState(onde.south()))) dz = 1;
        if (dx == dz) return false;

        BlockPos canto = onde;
        if (level.getBlockState(onde.offset(-dx, 0, -dz)).isAir()) canto = onde.offset(-dx, 0, -dz);

        for (int l = -1; l <= WIDTH; l++) {
            for (int y = -1; y <= HEIGHT; y++) {
                boolean borda = l == -1 || l == WIDTH || y == -1 || y == HEIGHT;
                // os quatro cantos do quadrado não contam, como no original
                if ((l == -1 || l == WIDTH) && (y == -1 || y == HEIGHT)) continue;
                BlockPos casa = canto.offset(dx * l, y, dz * l);
                BlockState feitio = level.getBlockState(casa);
                if (borda) {
                    if (!frame(feitio)) return false;
                } else if (!feitio.isAir() && !feitio.is(OccultaBlocks.FLOWING_SPIRIT)) {
                    return false;
                }
            }
        }

        BlockState portal = OccultaBlocks.SPIRIT_PORTAL.defaultBlockState()
                .setValue(AXIS, dx == 1 ? Direction.Axis.X : Direction.Axis.Z);
        for (int l = 0; l < WIDTH; l++) {
            for (int y = 0; y < HEIGHT; y++) {
                level.setBlock(canto.offset(dx * l, y, dz * l), portal, Block.UPDATE_CLIENTS);
            }
        }
        return true;
    }
}
