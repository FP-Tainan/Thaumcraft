package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.entity.HoleBlockEntity;
import net.thaumcraft.client.fx.Sparkle;
import net.thaumcraft.registry.TCBlocks;

/**
 * O buraco do Buraco Portátil: o {@code TileHoleRenderer} da 4.2.3.5.
 *
 * <p>O original desenha, em cada parede sólida do buraco, o mesmo céu de estrelas em camadas que o jogo da
 * época usava no portal do End — só que com as texturas do Thaumcraft, o túnel e o campo de partículas. O jogo
 * de hoje tem esse mesmo efeito como shader, e aqui ele é usado com as duas texturas do mod.
 */
public class HoleRenderer implements BlockEntityRenderer<HoleBlockEntity, HoleRenderer.State> {
    private static final Identifier TUNNEL = Thaumcraft.id("textures/misc/tunnel.png");
    private static final Identifier FIELD = Thaumcraft.id("textures/misc/particlefield.png");
    /** O shader do portal com as cores do original: {@code shaders/core/hole}. */
    private static final com.mojang.blaze3d.pipeline.RenderPipeline PIPELINE = RenderPipelines.register(
            com.mojang.blaze3d.pipeline.RenderPipeline.builder()
                    .withLocation(Thaumcraft.id("pipeline/hole"))
                    .withBindGroupLayout(net.minecraft.client.renderer.BindGroupLayouts.GLOBALS)
                    .withBindGroupLayout(net.minecraft.client.renderer.BindGroupLayouts.MATRICES_PROJECTION)
                    .withBindGroupLayout(net.minecraft.client.renderer.BindGroupLayouts.FOG)
                    .withVertexShader(Thaumcraft.id("core/hole"))
                    .withFragmentShader(Thaumcraft.id("core/hole"))
                    .withBindGroupLayout(net.minecraft.client.renderer.BindGroupLayouts.SAMPLER0_SAMPLER1)
                    .withVertexBinding(0, com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION)
                    .withPrimitiveTopology(com.mojang.blaze3d.PrimitiveTopology.QUADS)
                    .withDepthStencilState(com.mojang.blaze3d.pipeline.DepthStencilState.DEFAULT)
                    .build());
    private static final RenderType HOLE = RenderType.create("thaumcraft_hole",
            RenderSetup.builder(PIPELINE)
                    .withTexture("Sampler0", TUNNEL)
                    .withTexture("Sampler1", FIELD)
                    .createRenderSetup());
    /** O original põe a parede a um milésimo do bloco; aqui a cinco centésimos, que a profundidade de hoje separa da parede. */
    private static final float OFFSET = 0.95f;

    public static class State extends BlockEntityRenderState {
        final boolean[] walls = new boolean[6];
    }

    public HoleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(HoleBlockEntity hole, State state, float partial, Vec3 camera,
                                   net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(hole, state, crumbling);
        Level level = hole.getLevel();
        for (Direction dir : Direction.values()) {
            state.walls[dir.get3DDataValue()] = level != null && wall(level, hole.getBlockPos().relative(dir));
        }
    }

    /** Parede é o vizinho que tapa a vista e não é outro buraco. */
    static boolean wall(Level level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return state.canOcclude() && !state.is(TCBlocks.HOLE);
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        // o desenho roda depois; leva uma cópia de quais paredes há
        boolean[] walls = state.walls.clone();
        collector.submitCustomGeometry(pose, HOLE, (matrix, consumer) -> {
            float lo = 1.0f - OFFSET, hi = OFFSET;
            for (Direction dir : Direction.values()) {
                if (!walls[dir.get3DDataValue()]) continue;
                float[][] q = switch (dir) {
                    case UP -> new float[][]{{0, hi, 1}, {0, hi, 0}, {1, hi, 0}, {1, hi, 1}};
                    case DOWN -> new float[][]{{0, lo, 0}, {0, lo, 1}, {1, lo, 1}, {1, lo, 0}};
                    case NORTH -> new float[][]{{0, 0, lo}, {0, 1, lo}, {1, 1, lo}, {1, 0, lo}};
                    case SOUTH -> new float[][]{{1, 0, hi}, {1, 1, hi}, {0, 1, hi}, {0, 0, hi}};
                    case WEST -> new float[][]{{lo, 0, 1}, {lo, 1, 1}, {lo, 1, 0}, {lo, 0, 0}};
                    case EAST -> new float[][]{{hi, 0, 0}, {hi, 1, 0}, {hi, 1, 1}, {hi, 0, 1}};
                };
                // dos dois lados: por dentro é a parede do túnel; por fora, some atrás do bloco de verdade
                for (int i = 0; i < 4; i++) consumer.addVertex(matrix, q[i][0], q[i][1], q[i][2]);
                for (int i = 3; i >= 0; i--) consumer.addVertex(matrix, q[i][0], q[i][1], q[i][2]);
            }
        });
    }

    /**
     * O {@code surroundwithsparkles}: faíscas azuis pipocando nas quinas em que a borda do buraco encontra a
     * parede, a cada tique.
     */
    public static void sparkles(Level level, BlockPos pos, HoleBlockEntity hole) {
        var r = level.getRandom();
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        boolean yp = wall(level, pos.above()), xp = wall(level, pos.east()), zp = wall(level, pos.south());
        boolean yn = wall(level, pos.below()), xn = wall(level, pos.west()), zn = wall(level, pos.north());
        boolean b1 = !level.getBlockState(pos.above()).is(TCBlocks.HOLE);
        boolean b2 = !level.getBlockState(pos.below()).is(TCBlocks.HOLE);
        boolean b3 = !level.getBlockState(pos.north()).is(TCBlocks.HOLE);
        boolean b4 = !level.getBlockState(pos.south()).is(TCBlocks.HOLE);
        boolean b5 = !level.getBlockState(pos.west()).is(TCBlocks.HOLE);
        boolean b6 = !level.getBlockState(pos.east()).is(TCBlocks.HOLE);
        if (!xp && yp && b6) spark(r, x + 1, y + 1, z + r.nextFloat());
        if (!xn && yp && b5) spark(r, x, y + 1, z + r.nextFloat());
        if (!zp && yp && b4) spark(r, x + r.nextFloat(), y + 1, z + 1);
        if (!zn && yp && b3) spark(r, x + r.nextFloat(), y + 1, z);
        if (!xp && yn && b6) spark(r, x + 1, y, z + r.nextFloat());
        if (!xn && yn && b5) spark(r, x, y, z + r.nextFloat());
        if (!zp && yn && b4) spark(r, x + r.nextFloat(), y, z + 1);
        if (!zn && yn && b3) spark(r, x + r.nextFloat(), y, z);
        if (!yp && xp && b1) spark(r, x + 1, y + 1, z + r.nextFloat());
        if (!yn && xp && b2) spark(r, x + 1, y, z + r.nextFloat());
        if (!zp && xp && b4) spark(r, x + 1, y + r.nextFloat(), z + 1);
        if (!zn && xp && b3) spark(r, x + 1, y + r.nextFloat(), z);
        if (!yp && xn && b1) spark(r, x, y + 1, z + r.nextFloat());
        if (!yn && xn && b2) spark(r, x, y, z + r.nextFloat());
        if (!zp && xn && b4) spark(r, x, y + r.nextFloat(), z + 1);
        if (!zn && xn && b3) spark(r, x, y + r.nextFloat(), z);
        if (!xp && zp && b6) spark(r, x + 1, y + r.nextFloat(), z + 1);
        if (!xn && zp && b5) spark(r, x, y + r.nextFloat(), z + 1);
        if (!yp && zp && b1) spark(r, x + r.nextFloat(), y + 1, z + 1);
        if (!yn && zp && b2) spark(r, x + r.nextFloat(), y, z + 1);
        if (!xp && zn && b6) spark(r, x + 1, y + r.nextFloat(), z);
        if (!xn && zn && b5) spark(r, x, y + r.nextFloat(), z);
        if (!yp && zn && b1) spark(r, x + r.nextFloat(), y + 1, z);
        if (!yn && zn && b2) spark(r, x + r.nextFloat(), y, z);
    }

    private static void spark(net.minecraft.util.RandomSource random, double x, double y, double z) {
        Sparkle.spawn(random, x, y, z, 1.5f, 2, 0.0f);
    }
}
