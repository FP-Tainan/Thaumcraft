package net.thaumcraft.client.render;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadAtlas;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadTransform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.registry.TCBlocks;

import java.util.List;
import java.util.function.Predicate;

/**
 * O vidro protegido emendado: o {@code getIcon} do {@code BlockCosmeticOpaque} da 4.2.3.5. Para cada face, os oito
 * vizinhos no plano dela (um bit cada, na ordem do original) escolhem, pela tabela do {@code UtilsFX}, uma das 47
 * texturas; o modelo de base desenha e aqui só se troca a textura da face.
 */
public class WardedGlassModel implements BlockStateModel {
    private final BlockStateModel parent;
    private volatile TextureAtlasSprite[] sprites;

    public WardedGlassModel(BlockStateModel parent) {
        this.parent = parent;
    }

    public static void init() {
        ModelLoadingPlugin.register(context -> context.modifyBlockModelAfterBake().register(ModelModifier.WRAP_PHASE,
                (model, modelContext) -> modelContext.state().is(TCBlocks.WARDED_GLASS) ? new WardedGlassModel(model) : model));
    }

    private TextureAtlasSprite[] sprites() {
        TextureAtlasSprite[] current = this.sprites;
        if (current != null) return current;
        TextureAtlasSprite[] made = new TextureAtlasSprite[47];
        var atlas = Minecraft.getInstance().getAtlasManager();
        for (int i = 0; i < 47; i++) {
            made[i] = atlas.get(new SpriteId(QuadAtlas.BLOCK.getTextureLocation(), Thaumcraft.id("block/warded_glass_" + (i + 1))));
        }
        this.sprites = made;
        return made;
    }

    /** Este vizinho é vidro protegido? */
    private static boolean glass(BlockAndTintGetter level, int x, int y, int z) {
        return level.getBlockState(new BlockPos(x, y, z)).is(TCBlocks.WARDED_GLASS);
    }

    /** O índice do original: os oito vizinhos no plano da face, como bits, e a tabela. */
    static int index(BlockAndTintGetter level, BlockPos pos, Direction face) {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        boolean[] b = new boolean[8];
        int side = face.get3DDataValue();
        if (side == 0 || side == 1) {
            b[0] = glass(level, x - 1, y, z - 1);
            b[1] = glass(level, x, y, z - 1);
            b[2] = glass(level, x + 1, y, z - 1);
            b[3] = glass(level, x - 1, y, z);
            b[4] = glass(level, x + 1, y, z);
            b[5] = glass(level, x - 1, y, z + 1);
            b[6] = glass(level, x, y, z + 1);
            b[7] = glass(level, x + 1, y, z + 1);
        } else if (side == 2 || side == 3) {
            int a = side == 2 ? 1 : -1, c = side == 3 ? 1 : -1;
            b[0] = glass(level, x + a, y + 1, z);
            b[1] = glass(level, x, y + 1, z);
            b[2] = glass(level, x + c, y + 1, z);
            b[3] = glass(level, x + a, y, z);
            b[4] = glass(level, x + c, y, z);
            b[5] = glass(level, x + a, y - 1, z);
            b[6] = glass(level, x, y - 1, z);
            b[7] = glass(level, x + c, y - 1, z);
        } else {
            int a = side == 5 ? 1 : -1, c = side == 4 ? 1 : -1;
            b[0] = glass(level, x, y + 1, z + a);
            b[1] = glass(level, x, y + 1, z);
            b[2] = glass(level, x, y + 1, z + c);
            b[3] = glass(level, x, y, z + a);
            b[4] = glass(level, x, y, z + c);
            b[5] = glass(level, x, y - 1, z + a);
            b[6] = glass(level, x, y - 1, z);
            b[7] = glass(level, x, y - 1, z + c);
        }
        int id = 0;
        for (int i = 0; i < 8; i++) if (b[i]) id |= 1 << i;
        return WardedGlassTable.REF[id];
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
                          Predicate<Direction> cullTest) {
        TextureAtlasSprite[] sprites = this.sprites();
        emitter.pushTransform(new Swap(sprites, level, pos));
        try {
            this.parent.emitQuads(emitter, level, pos, state, random, cullTest);
        } finally {
            emitter.popTransform();
        }
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
        this.parent.collectParts(random, parts);
    }

    @Override
    public net.minecraft.client.resources.model.sprite.Material.Baked particleMaterial() {
        return this.parent.particleMaterial();
    }

    @Override
    public int materialFlags() {
        return this.parent.materialFlags();
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        long key = 0;
        for (Direction face : Direction.values()) key = key * 47 + index(level, pos, face);
        return key;
    }

    /** Troca a textura de cada face pela da vizinhança dela. */
    private record Swap(TextureAtlasSprite[] sprites, BlockAndTintGetter level, BlockPos pos) implements QuadTransform {
        @Override
        public boolean transform(MutableQuadView quad) {
            Direction face = quad.nominalFace() != null ? quad.nominalFace() : quad.lightFace();
            if (face == null) return true;
            TextureAtlasSprite from = this.sprites[0];
            TextureAtlasSprite to = this.sprites[index(this.level, this.pos, face)];
            if (from == null || to == null || from == to) return true;
            for (int vertex = 0; vertex < 4; vertex++) {
                float u = unbake(quad.u(vertex), from.getU0(), from.getU1());
                float v = unbake(quad.v(vertex), from.getV0(), from.getV1());
                quad.uv(vertex, to.getU(u), to.getV(v));
            }
            return true;
        }

        private static float unbake(float value, float from, float to) {
            float size = to - from;
            return size == 0 ? 0 : (value - from) / size;
        }
    }
}
