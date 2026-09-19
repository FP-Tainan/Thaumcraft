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
import net.thaumcraft.block.InfernalFurnaceBlock;
import net.thaumcraft.registry.TCBlocks;

import java.util.List;
import java.util.function.Predicate;

/**
 * As paredes da fornalha infernal: o {@code calculateTexture} do {@code BlockArcaneFurnace} da 4.2.3.5. Cada face das
 * paredes mostra um pedaço do desenho grande da face inteira do cubo (qual pedaço sai da posição do bloco na camada e
 * da camada), e em volta da boca os pedaços mudam para os da moldura. O modelo de base é um cubo com a
 * {@code furnace7}; aqui só se troca a textura de cada face.
 */
public class InfernalFurnaceModel implements BlockStateModel {
    private final BlockStateModel parent;
    private volatile TextureAtlasSprite[] sprites;

    public InfernalFurnaceModel(BlockStateModel parent) {
        this.parent = parent;
    }

    public static void init() {
        ModelLoadingPlugin.register(context -> context.modifyBlockModelAfterBake().register(ModelModifier.WRAP_PHASE,
                (model, modelContext) -> {
                    BlockState state = modelContext.state();
                    if (!state.is(TCBlocks.INFERNAL_FURNACE)) return model;
                    int part = state.getValue(InfernalFurnaceBlock.PART);
                    return part == 0 || part == 10 ? model : new InfernalFurnaceModel(model);
                }));
    }

    private TextureAtlasSprite[] sprites() {
        TextureAtlasSprite[] current = this.sprites;
        if (current != null) return current;
        TextureAtlasSprite[] made = new TextureAtlasSprite[27];
        var atlas = Minecraft.getInstance().getAtlasManager();
        for (int i = 0; i < 27; i++) {
            if (i == 8 || i == 24) continue;
            made[i] = atlas.get(new SpriteId(QuadAtlas.BLOCK.getTextureLocation(), Thaumcraft.id("block/infernal_furnace_" + i)));
        }
        this.sprites = made;
        return made;
    }

    private static int meta(BlockAndTintGetter level, BlockPos pos) {
        return InfernalFurnaceBlock.part(level.getBlockState(pos));
    }

    /** O {@code calculateLevel}: 9 no meio (fornalha igual em cima e embaixo), 18 embaixo, 0 em cima. */
    static int calculateLevel(BlockAndTintGetter world, BlockPos pos) {
        int meta = meta(world, pos);
        int metaA = meta(world, pos.above());
        if (metaA == 10 || metaA == 0) metaA = meta;
        int metaB = meta(world, pos.below());
        if (metaB == 10 || metaB == 0) metaB = meta;
        boolean blockA = metaA >= 0 && world.getBlockState(pos.above()).getBlock() instanceof InfernalFurnaceBlock;
        boolean blockB = metaB >= 0 && world.getBlockState(pos.below()).getBlock() instanceof InfernalFurnaceBlock;
        if (meta == metaA && meta == metaB && blockA && blockB) return 9;
        return meta != metaA || !blockA || meta == metaB && blockB ? 0 : 18;
    }

    private static boolean mouth(BlockAndTintGetter world, int x, int y, int z) {
        return meta(world, new BlockPos(x, y, z)) == 10;
    }

    /**
     * O {@code BlockUtils.isBlockTouchingOnSide}: nas faces de lado, a boca em qualquer um dos oito vizinhos no plano
     * da face (e é por isso que a face da frente inteira ganha a moldura); em cima e embaixo, só logo acima ou abaixo.
     */
    static boolean touchingMouth(BlockAndTintGetter world, BlockPos pos, int side) {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        if (side > 3 && (mouth(world, x, y, z + 1) || mouth(world, x, y, z - 1))) return true;
        if (side > 1 && side < 4 && (mouth(world, x + 1, y, z) || mouth(world, x - 1, y, z))) return true;
        if (side > 1 && (mouth(world, x, y + 1, z) || mouth(world, x, y - 1, z))) return true;
        for (int dy : new int[]{1, -1}) {
            if (side > 3 && (mouth(world, x, y + dy, z + 1) || mouth(world, x, y + dy, z - 1))) return true;
            if (side > 1 && side < 4 && (mouth(world, x + 1, y + dy, z) || mouth(world, x - 1, y + dy, z))) return true;
        }
        if (side == 0) return mouth(world, x, y - 1, z);
        if (side == 1) return mouth(world, x, y + 1, z);
        return false;
    }

    /** O {@code calculateTexture}, de 0 (baixo) a 5 (leste), como no original. */
    static int calculateTexture(BlockAndTintGetter world, BlockPos pos, int side) {
        int meta = meta(world, pos);
        int level = calculateLevel(world, pos);
        int add = touchingMouth(world, pos, side) ? 3 : 0;
        switch (side) {
            case 0:
            case 1:
                if (side == 1 && level == 18) {
                    switch (meta) {
                        case 2:
                            return 16;
                        case 4:
                            return 17;
                        case 6:
                            return 26;
                        case 8:
                            return 25;
                        default:
                            break;
                    }
                }
                if (add != 3) {
                    if (meta == 5) return 10;
                    int i = (meta - 1) % 3 + (meta - 1) / 3 * 9;
                    return i < 0 ? -1 : i;
                }
                return add == 0 ? 7 : 6;
            case 2:
                return switch (meta) {
                    case 1 -> 2 + level + add;
                    case 2 -> 1 + level + add;
                    case 3 -> level + add;
                    default -> level != 9 ? 7 : 6;
                };
            case 3:
                return switch (meta) {
                    case 7 -> level + add;
                    case 8 -> 1 + level + add;
                    case 9 -> 2 + level + add;
                    default -> level != 9 ? 7 : 6;
                };
            case 4:
                return switch (meta) {
                    case 1 -> level + add;
                    case 4 -> 1 + level + add;
                    case 7 -> 2 + level + add;
                    default -> level != 9 ? 7 : 6;
                };
            case 5:
                return switch (meta) {
                    case 3 -> 2 + level + add;
                    case 6 -> 1 + level + add;
                    case 9 -> level + add;
                    default -> level != 9 ? 7 : 6;
                };
            default:
                return add == 0 ? 7 : 6;
        }
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
                          Predicate<Direction> cullTest) {
        emitter.pushTransform(new Swap(this.sprites(), level, pos));
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
        for (int side = 0; side < 6; side++) key = key * 28 + (calculateTexture(level, pos, side) + 1);
        return key;
    }

    /** Troca a textura de cada face pela que o original escolheria para ela. */
    private record Swap(TextureAtlasSprite[] sprites, BlockAndTintGetter level, BlockPos pos) implements QuadTransform {
        @Override
        public boolean transform(MutableQuadView quad) {
            Direction face = quad.nominalFace() != null ? quad.nominalFace() : quad.lightFace();
            if (face == null) return true;
            int index = calculateTexture(this.level, this.pos, face.get3DDataValue());
            TextureAtlasSprite from = this.sprites[7];
            // o original não desenha a face sem textura (a do índice negativo, que não acontece numa fornalha inteira)
            if (index < 0 || index >= this.sprites.length || this.sprites[index] == null) return true;
            TextureAtlasSprite to = this.sprites[index];
            if (from == null || from == to) return true;
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
