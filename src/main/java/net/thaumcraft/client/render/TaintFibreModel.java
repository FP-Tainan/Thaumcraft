package net.thaumcraft.client.render;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadAtlas;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.TaintBlock;
import net.thaumcraft.block.TaintFibreBlock;
import net.thaumcraft.registry.TCBlocks;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

/**
 * As fibras da mácula no mundo: o {@code BlockTaintFibreRenderer} da 4.2.3.5. Toda forma forra, a meio centésimo de
 * distância, cada face firme em volta que não seja da própria mácula (com a {@code taint_fibres}, na cor do capim); a
 * película ainda põe, um pouco mais à frente, os brilhos {@code taint_over} acesos em uma face de cada vinte; o capim é
 * um par de planos cruzados deslocado ao acaso, e os talos, os quatro planos de uma plantação.
 */
public class TaintFibreModel implements BlockStateModel {
    private final BlockStateModel parent;
    private volatile Material.Baked[] sprites;

    public TaintFibreModel(BlockStateModel parent) {
        this.parent = parent;
    }

    public static void init() {
        ModelLoadingPlugin.register(context -> context.modifyBlockModelAfterBake().register(ModelModifier.WRAP_PHASE,
                (model, modelContext) -> modelContext.state().is(TCBlocks.TAINT_FIBRES) ? new TaintFibreModel(model) : model));
    }

    /** 0 a película, 1 e 2 o capim, 3 e 4 os talos, 5 a 7 os brilhos. */
    private Material.Baked[] sprites() {
        Material.Baked[] current = this.sprites;
        if (current != null) return current;
        String[] names = {"taint_fibres", "taintgrass1", "taintgrass2", "taint_spore_stalk_1", "taint_spore_stalk_2",
                "taint_over_1", "taint_over_2", "taint_over_3"};
        Material.Baked[] made = new Material.Baked[names.length];
        var atlas = Minecraft.getInstance().getAtlasManager();
        for (int i = 0; i < names.length; i++) {
            TextureAtlasSprite sprite = atlas.get(new SpriteId(QuadAtlas.BLOCK.getTextureLocation(), Thaumcraft.id("block/" + names[i])));
            made[i] = new Material.Baked(sprite, false);
        }
        this.sprites = made;
        return made;
    }

    /** O lado do vizinho nessa direção é firme e o vizinho não é o bloco da mácula. */
    private static boolean wall(BlockAndTintGetter level, BlockPos pos, Direction dir) {
        BlockPos at = pos.relative(dir);
        BlockState there = level.getBlockState(at);
        return there.isFaceSturdy(level, at, dir.getOpposite()) && !(there.getBlock() instanceof TaintBlock);
    }

    /** O número de lado do original para a direção. */
    private static int side(Direction dir) {
        return dir.get3DDataValue();
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
                          Predicate<Direction> cullTest) {
        if (!state.is(TCBlocks.TAINT_FIBRES)) return;
        Material.Baked[] sprites = this.sprites();
        int kind = state.getValue(TaintFibreBlock.KIND);
        for (Direction dir : Direction.values()) {
            if (!wall(level, pos, dir)) continue;
            film(emitter, dir.getOpposite(), 0.995f, sprites[0], 0, false);
        }
        if (kind == 0) {
            for (Direction dir : Direction.values()) {
                if (!wall(level, pos, dir)) continue;
                Random r = new Random(side(dir) + pos.getY() + (long) pos.getX() * pos.getZ());
                if (r.nextInt(100) < 95) continue;
                film(emitter, dir.getOpposite(), 0.98f, sprites[5 + r.nextInt(3)], -1, true);
            }
        }
        if ((kind == 1 || kind == 2) && wall(level, pos, Direction.DOWN)) {
            long i1 = pos.getX() * 3129871L ^ pos.getZ() * 116129781L ^ pos.getY();
            i1 = i1 * i1 * 42317861L + i1 * 11L;
            float dx = (float) (((i1 >> 16 & 15L) / 15.0f - 0.5) * 0.5);
            float dz = (float) (((i1 >> 24 & 15L) / 15.0f - 0.5) * 0.5);
            cross(emitter, sprites[kind], dx, dz);
        }
        if (kind == 3 || kind == 4) crops(emitter, sprites[kind]);
    }

    /** Uma face inteira voltada para {@code facing}, a {@code depth} do lado de lá. */
    private static void film(QuadEmitter emitter, Direction facing, float depth, Material.Baked sprite, int tint, boolean glow) {
        emitter.square(facing, 0, 0, 1, 1, depth);
        emitter.materialBake(sprite, MutableQuadView.BAKE_LOCK_UV);
        emitter.color(-1, -1, -1, -1);
        emitter.tintIndex(tint);
        emitter.chunkLayer(ChunkSectionLayer.TRANSLUCENT);
        emitter.ambientOcclusion(TriState.FALSE);
        emitter.diffuseShade(false);
        emitter.emissive(glow);
        emitter.emit();
    }

    /** O {@code drawCrossedSquares}: dois planos em X, cada um das duas faces, deslocados por (dx, dz). */
    private static void cross(QuadEmitter emitter, Material.Baked sprite, float dx, float dz) {
        float a = 0.5f - 0.45f, b = 0.5f + 0.45f;
        plane(emitter, sprite, a + dx, a + dz, b + dx, b + dz, 0, 1, 0);
        plane(emitter, sprite, a + dx, b + dz, b + dx, a + dz, 0, 1, 0);
    }

    /** O {@code renderBlockCropsImpl}: quatro planos, a um quarto e a três quartos, um dezesseis avos para baixo. */
    private static void crops(QuadEmitter emitter, Material.Baked sprite) {
        float y0 = -0.0625f, y1 = 1 - 0.0625f;
        for (float at : new float[]{0.25f, 0.75f}) {
            plane(emitter, sprite, at, 0, at, 1, y0, y1, -1);
            plane(emitter, sprite, 0, at, 1, at, y0, y1, -1);
        }
    }

    /** Um plano de pé de (x0, z0) a (x1, z1), das duas faces. */
    private static void plane(QuadEmitter emitter, Material.Baked sprite, float x0, float z0, float x1, float z1, float y0, float y1, int tint) {
        TextureAtlasSprite s = sprite.sprite();
        for (int face = 0; face < 2; face++) {
            float ax = face == 0 ? x0 : x1, az = face == 0 ? z0 : z1, bx = face == 0 ? x1 : x0, bz = face == 0 ? z1 : z0;
            emitter.pos(0, ax, y1, az).uv(0, s.getU0(), s.getV0());
            emitter.pos(1, ax, y0, az).uv(1, s.getU0(), s.getV1());
            emitter.pos(2, bx, y0, bz).uv(2, s.getU1(), s.getV1());
            emitter.pos(3, bx, y1, bz).uv(3, s.getU1(), s.getV0());
            emitter.color(-1, -1, -1, -1);
            emitter.tintIndex(tint);
            emitter.chunkLayer(ChunkSectionLayer.CUTOUT);
            emitter.ambientOcclusion(TriState.FALSE);
            emitter.diffuseShade(false);
            emitter.emissive(false);
            emitter.emit();
        }
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
    }

    @Override
    public Material.Baked particleMaterial() {
        return this.parent.particleMaterial();
    }

    @Override
    public int materialFlags() {
        return this.parent.materialFlags();
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return null;
    }
}
