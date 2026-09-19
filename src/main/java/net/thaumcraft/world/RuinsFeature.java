package net.thaumcraft.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.api.nodes.NodeType;
import net.thaumcraft.block.ObsidianTotemBlock;
import net.thaumcraft.block.entity.BannerBlockEntity;
import net.thaumcraft.block.entity.eldritch.EldritchAltarBlockEntity;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCEntities;

import java.util.Set;

/**
 * As ruínas que o {@code generateSurface} da 4.2.3.5 espalha pelo mundo de cima, uma tentativa por pedaço: um túmulo
 * (uma vez em cento e cinquenta), senão um anel eldritch (uma em sessenta e seis), senão um círculo de pedras no alto
 * dos morros (uma em quarenta), cada um com um nó sombrio; e, se nada disso deu nó, um totem de obsidiana uma vez em
 * trezentos e sessenta ({@code nodeRarity} vezes dez).
 *
 * <p>Diferenças: o túmulo tem dezenove blocos de lado e aqui não pode passar da vizinhança que a geração de hoje deixa
 * escrever, então o canto dele sorteia até a décima terceira casa do pedaço em vez da décima quinta. O nó solto do
 * mundo nasce noutra etapa ({@link NodeFeature}), e por isso não segura o totem como no original.
 */
public class RuinsFeature extends Feature<NoneFeatureConfiguration> {
    private static final int NODE_RARITY = 36;

    public RuinsFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        // o original não põe nada no mundo plano
        if (context.chunkGenerator() instanceof net.minecraft.world.level.levelgen.FlatLevelSource) return false;
        BlockPos origin = context.origin();
        int chunkX = origin.getX() >> 4, chunkZ = origin.getZ() >> 4;
        boolean aura = false;
        int lx = random.nextInt(16), lz = random.nextInt(16);
        int x = chunkX * 16 + lx, z = chunkZ * 16 + lz;
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) - 9;
        if (y < level.getMaxY()) {
            if (random.nextInt(150) == 0) {
                int mx = chunkX * 16 + Math.min(lx, 13), mz = chunkZ * 16 + Math.min(lz, 13);
                int my = level.getHeight(Heightmap.Types.MOTION_BLOCKING, mx, mz) - 9;
                if (mound(level, random, mx, my, mz)) {
                    aura = true;
                    NodeFeature.createNodeAt(level, new BlockPos(mx + 9, my + 8, mz + 9), random, NodeType.DARK);
                }
            } else if (random.nextInt(66) == 0) {
                y += 8;
                int w = 11 + random.nextInt(6) * 2;
                int h = 11 + random.nextInt(6) * 2;
                if (eldritchRing(level, random, x, y, z, chunkX, chunkZ, w, h)) {
                    aura = true;
                    NodeFeature.createNodeAt(level, new BlockPos(x, y + 2, z), random, NodeType.DARK);
                    EldritchRings.reserveMaze(level, chunkX, chunkZ, w, h, random.nextLong());
                }
            } else if (random.nextInt(40) == 0) {
                y += 9;
                if (hilltopStones(level, random, x, y, z)) {
                    aura = true;
                    NodeFeature.createNodeAt(level, new BlockPos(x, y + 5, z), random, NodeType.DARK);
                }
            }
        }
        totem(level, random, chunkX, chunkZ, aura);
        return true;
    }

    // ------------------------------------------------------------------ o totem de obsidiana

    /** O {@code generateTotem}: uma coluna de até quatro totens em cima de ladrilho, o de cima carregado com um nó sombrio. */
    private static void totem(WorldGenLevel level, RandomSource random, int chunkX, int chunkZ, boolean aura) {
        if (aura || random.nextInt(NODE_RARITY * 10) != 0) return;
        totem(level, random, chunkX * 16 + random.nextInt(16), chunkZ * 16 + random.nextInt(16));
    }

    /** O totem naquela coluna, se o chão servir. */
    public static void totem(WorldGenLevel level, RandomSource random, int x, int z) {
        int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) - 1;
        if (top > level.getMaxY()) return;
        if (level.getBlockState(new BlockPos(x, top, z)).is(net.minecraft.tags.BlockTags.LEAVES)) {
            do {
                top--;
            } while (!level.getBlockState(new BlockPos(x, top, z)).is(Blocks.GRASS_BLOCK) && top > 40);
        }
        if (isSnowOrGrass(level.getBlockState(new BlockPos(x, top, z)))) top--;
        BlockState ground = level.getBlockState(new BlockPos(x, top, z));
        if (!(ground.is(Blocks.GRASS_BLOCK) || ground.is(Blocks.SAND) || ground.is(Blocks.RED_SAND) || isDirt(ground)
                || ground.is(Blocks.STONE) || ground.is(Blocks.NETHERRACK))) return;
        int count = 1;
        while (openForTotem(level, x, top + count, z) && count < 3) count++;
        if (count < 2) return;
        set(level, new BlockPos(x, top, z), TCBlocks.OBSIDIAN_TILE.defaultBlockState());
        count = 1;
        boolean charged = false;
        while (openForTotem(level, x, top + count, z) && count < 5) {
            set(level, new BlockPos(x, top + count, z), TCBlocks.OBSIDIAN_TOTEM.defaultBlockState());
            if (count > 1 && random.nextInt(4) == 0) {
                set(level, new BlockPos(x, top + count, z), TCBlocks.CHARGED_OBSIDIAN_TOTEM.defaultBlockState());
                NodeFeature.createNodeAt(level, new BlockPos(x, top + count, z), random, NodeType.DARK);
                count = 5;
                charged = true;
            }
            if (++count >= 5 && !charged) {
                set(level, new BlockPos(x, top + 5, z), TCBlocks.CHARGED_OBSIDIAN_TOTEM.defaultBlockState());
                NodeFeature.createNodeAt(level, new BlockPos(x, top + 5, z), random, NodeType.DARK);
            }
        }
        // as figuras dos lados dependem da coluna inteira
        for (int dy = 0; dy <= 6; dy++) shapeTotem(level, new BlockPos(x, top + dy, z));
    }

    private static boolean openForTotem(WorldGenLevel level, int x, int y, int z) {
        BlockState state = level.getBlockState(new BlockPos(x, y, z));
        return state.isAir() || isSnowOrGrass(state);
    }

    private static void shapeTotem(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (ObsidianTotemBlock.isTotem(state)) level.setBlock(pos, ObsidianTotemBlock.shape(state, level, pos), Block.UPDATE_CLIENTS);
    }

    // ------------------------------------------------------------------ onde se pode construir

    private static boolean isSnowOrGrass(BlockState state) {
        return state.is(Blocks.SNOW) || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.FERN) || state.is(Blocks.DEAD_BUSH);
    }

    private static boolean isDirt(BlockState state) {
        return state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.PODZOL);
    }

    /** O {@code LocationIsValidSpawn}: chão firme de um dos tipos aceitos, com ar em cima, a no máximo dois blocos. */
    private static boolean validSpawn(WorldGenLevel level, int i, int j, int k, Set<String> accepted) {
        int distanceToAir = 0;
        while (!level.getBlockState(new BlockPos(i, j + distanceToAir, k)).isAir()) {
            if (++distanceToAir > 2) return false;
        }
        j += distanceToAir - 1;
        BlockState block = level.getBlockState(new BlockPos(i, j, k));
        BlockState above = level.getBlockState(new BlockPos(i, j + 1, k));
        BlockState below = level.getBlockState(new BlockPos(i, j - 1, k));
        if (!above.isAir()) return false;
        if (kind(block, accepted)) return true;
        return isSnowOrGrass(block) && kind(below, accepted);
    }

    private static boolean kind(BlockState state, Set<String> accepted) {
        if (state.is(Blocks.STONE)) return accepted.contains("stone");
        if (state.is(Blocks.GRASS_BLOCK)) return accepted.contains("grass");
        if (isDirt(state)) return accepted.contains("dirt");
        if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) return accepted.contains("sand");
        if (state.is(Blocks.PACKED_ICE)) return accepted.contains("packed_ice");
        if (state.is(Blocks.GRAVEL)) return accepted.contains("gravel");
        return false;
    }

    private static final Set<String> GROUND = Set.of("stone", "grass", "dirt");
    private static final Set<String> RING_GROUND = Set.of("stone", "sand", "packed_ice", "grass", "gravel", "dirt");

    // ------------------------------------------------------------------ o anel eldritch

    /** O {@code WorldGenEldritchRing}: a laje de obsidiana, o altar com o obelisco em cima e os capitéis em volta. */
    public static boolean eldritchRing(WorldGenLevel level, RandomSource rand, int i, int j, int k, int chunkX, int chunkZ, int w, int h) {
        if (!(validSpawn(level, i - 3, j, k - 3, RING_GROUND) && validSpawn(level, i, j, k, RING_GROUND)
                && validSpawn(level, i + 3, j, k, RING_GROUND) && validSpawn(level, i + 3, j, k + 3, RING_GROUND)
                && validSpawn(level, i, j, k + 3, RING_GROUND) && !EldritchRings.mazesInRange(level, chunkX, chunkZ, w, h))) {
            return false;
        }
        for (int x = i - 3; x <= i + 3; x++) {
            for (int z = k - 3; z <= k + 3; z++) {
                if ((x == i - 3 || x == i + 3) && (z == k - 3 || z == k + 3)) continue;
                for (int q = -4; q < 5; q++) {
                    BlockPos at = new BlockPos(x, j + q, z);
                    if (q > 0) {
                        set(level, at, Blocks.AIR.defaultBlockState());
                    } else {
                        set(level, at, rand.nextInt(4) == 0 ? Blocks.OBSIDIAN.defaultBlockState() : TCBlocks.OBSIDIAN_TILE.defaultBlockState());
                    }
                }
                if (x == i && z == k) {
                    set(level, new BlockPos(x, j + 1, z), TCBlocks.ELDRITCH_ALTAR.defaultBlockState());
                    set(level, new BlockPos(x, j, z), TCBlocks.OBSIDIAN_TILE.defaultBlockState());
                    int r = rand.nextInt(10);
                    if (level.getBlockEntity(new BlockPos(x, j + 1, z)) instanceof EldritchAltarBlockEntity altar) {
                        if (r >= 1 && r <= 4) {
                            altar.setSpawner(true);
                            altar.setSpawnType((byte) 0);
                            // os estandartes dos cultistas nos quatro lados, virados para o altar
                            banner(level, new BlockPos(x, j + 1, z - 3), 8);
                            banner(level, new BlockPos(x, j + 1, z + 3), 0);
                            banner(level, new BlockPos(x + 3, j + 1, z), 12);
                            banner(level, new BlockPos(x - 3, j + 1, z), 4);
                        } else if (r == 6 || r == 7) {
                            altar.setSpawner(true);
                            altar.setSpawnType((byte) 1);
                        }
                    }
                    set(level, new BlockPos(x, j + 3, z), TCBlocks.ELDRITCH_OBELISK.defaultBlockState());
                    for (int a = 4; a <= 7; a++) set(level, new BlockPos(x, j + a, z), TCBlocks.ELDRITCH_OBELISK_UPPER.defaultBlockState());
                } else if (((x == i - 3 || x == i + 3) && Math.abs((z - k) % 2) == 1 || (z == k - 3 || z == k + 3) && Math.abs((x - i) % 2) == 1)
                        && Math.abs(x - i) != Math.abs(z - k)) {
                    set(level, new BlockPos(x, j, z), TCBlocks.OBSIDIAN_TILE.defaultBlockState());
                    set(level, new BlockPos(x, j + 1, z), TCBlocks.ELDRITCH_CAPSTONE.defaultBlockState());
                }
            }
        }
        return true;
    }

    private static void banner(WorldGenLevel level, BlockPos pos, int facing) {
        set(level, pos, TCBlocks.BANNER.defaultBlockState());
        if (level.getBlockEntity(pos) instanceof BannerBlockEntity banner) banner.setFacing((byte) facing);
    }

    // ------------------------------------------------------------------ as pedras do topo dos morros

    /** O {@code WorldGenHilltopStones}: um círculo de totens no alto (acima de 85), com baú e gerador de fogo-fátuo por baixo. */
    public static boolean hilltopStones(WorldGenLevel level, RandomSource rand, int i, int j, int k) {
        if (j < 85) return false;
        if (!(validSpawn(level, i - 2, j, k - 2, GROUND) && validSpawn(level, i, j, k, GROUND) && validSpawn(level, i + 2, j, k, GROUND)
                && validSpawn(level, i + 2, j, k + 2, GROUND) && validSpawn(level, i, j, k + 2, GROUND))) return false;
        var biome = level.getBiome(new BlockPos(i, j, k));
        // o topBlock do bioma: aqui, o chão de verdade do meio do círculo
        BlockState filler = level.getBlockState(new BlockPos(i, level.getHeight(Heightmap.Types.OCEAN_FLOOR, i, k) - 1, k));
        if (filler.isAir()) filler = Blocks.GRASS_BLOCK.defaultBlockState();
        // o getEnableSnow: onde neva, não nascem trepadeiras
        boolean genVines = !biome.value().coldEnoughToSnow(new BlockPos(i, j, k), level.getSeaLevel());
        for (int x = i - 3; x <= i + 3; x++) {
            for (int z = k - 3; z <= k + 3; z++) {
                if ((x == i - 3 || x == i + 3) && (z == k - 3 || z == k + 3)) continue;
                set(level, new BlockPos(x, j, z), rand.nextBoolean() ? TCBlocks.OBSIDIAN_TILE.defaultBlockState() : Blocks.OBSIDIAN.defaultBlockState());
                boolean stop = false;
                for (int y = 1; y < 5; y++) {
                    BlockPos under = new BlockPos(x, j - y, z);
                    BlockState below = level.getBlockState(under);
                    if (below.isAir() || isSnowOrGrass(below) || below.is(Blocks.POPPY) || below.is(Blocks.DANDELION)) set(level, under, filler);
                    if (x == i && z == k && y == 1) {
                        set(level, new BlockPos(x, j + y, z), TCBlocks.OBSIDIAN_TILE.defaultBlockState());
                        BlockPos chest = new BlockPos(x, j + y + 1, z);
                        set(level, chest, Blocks.CHEST.defaultBlockState());
                        fillDungeonChest(level, chest, rand, 2);
                        BlockPos spawner = new BlockPos(x, j + y - 1, z);
                        set(level, spawner, Blocks.SPAWNER.defaultBlockState());
                        spawner(level, spawner, TCEntities.WISP, rand);
                    }
                    if (!stop && ((x == i - 3 || x == i + 3) && Math.abs((z - k) % 2) == 1 || (z == k - 3 || z == k + 3) && Math.abs((x - i) % 2) == 1)) {
                        set(level, new BlockPos(x, j + y, z), TCBlocks.OBSIDIAN_TOTEM.defaultBlockState());
                        if (y >= 2 && rand.nextBoolean()) {
                            stop = true;
                            if (genVines) {
                                if (rand.nextInt(3) == 0 && level.isEmptyBlock(new BlockPos(x - 1, j + y, z))) vines(level, x - 1, j + y, z, Direction.EAST);
                                if (rand.nextInt(3) == 0 && level.isEmptyBlock(new BlockPos(x + 1, j + y, z))) vines(level, x + 1, j + y, z, Direction.WEST);
                                if (rand.nextInt(3) == 0 && level.isEmptyBlock(new BlockPos(x, j + y, z - 1))) vines(level, x, j + y, z - 1, Direction.SOUTH);
                                if (rand.nextInt(3) == 0 && level.isEmptyBlock(new BlockPos(x, j + y, z + 1))) vines(level, x, j + y, z + 1, Direction.NORTH);
                            }
                        }
                    }
                }
            }
        }
        for (int x = i - 3; x <= i + 3; x++) {
            for (int z = k - 3; z <= k + 3; z++) {
                for (int y = 0; y <= 5; y++) shapeTotem(level, new BlockPos(x, j + y, z));
            }
        }
        return true;
    }

    /** O {@code growVines}: a trepadeira presa ao totem ao lado, descendo até quatro blocos pelo ar. */
    private static void vines(WorldGenLevel level, int x, int y, int z, Direction toward) {
        BlockState vine = Blocks.VINE.defaultBlockState().setValue(VineBlock.getPropertyForFace(toward), true);
        set(level, new BlockPos(x, y, z), vine);
        for (int n = 4; level.isEmptyBlock(new BlockPos(x, --y, z)) && n > 0; n--) set(level, new BlockPos(x, y, z), vine);
    }

    // ------------------------------------------------------------------ o túmulo

    /** O {@code WorldGenMound}: o monte de terra com a câmara, as urnas, o baú (às vezes com armadilha) e dois geradores. */
    public static boolean mound(WorldGenLevel level, RandomSource rand, int i, int j, int k) {
        if (!(validSpawn(level, i + 9, j + 9, k + 9, GROUND) && validSpawn(level, i, j + 9, k, GROUND)
                && validSpawn(level, i + 18, j + 9, k, GROUND) && validSpawn(level, i + 18, j + 9, k + 18, GROUND)
                && validSpawn(level, i, j + 9, k + 18, GROUND))) return false;
        for (String entry : MoundData.DATA.split(";")) {
            String[] p = entry.split(",");
            set(level, new BlockPos(i + Integer.parseInt(p[0]), j + Integer.parseInt(p[1]), k + Integer.parseInt(p[2])),
                    MoundData.STATES[Integer.parseInt(p[3])]);
        }
        for (int dz : new int[]{7, 11}) {
            float rr = rand.nextFloat();
            int rarity = rr < 0.1f ? 2 : (rr < 0.33f ? 1 : 0);
            boolean crate = rand.nextFloat() < 0.3f;
            set(level, new BlockPos(i + 9, j + 1, k + dz), (crate ? TCBlocks.LOOT_CRATES : TCBlocks.LOOT_URNS).get(rarity).defaultBlockState());
        }
        BlockPos chest = new BlockPos(i + 10, j + 1, k + 9);
        if (rand.nextInt(3) == 0) {
            set(level, chest, Blocks.TRAPPED_CHEST.defaultBlockState());
            set(level, new BlockPos(i + 10, j - 1, k + 9), Blocks.TNT.defaultBlockState());
        } else {
            set(level, chest, Blocks.CHEST.defaultBlockState());
        }
        fillDungeonChest(level, chest, rand, rand.nextInt(5) == 0 ? 2 : 1);
        spawner(level, new BlockPos(i + 4, j + 5, k + 4), net.minecraft.world.entity.EntityTypes.SKELETON, rand);
        spawner(level, new BlockPos(i + 4, j + 5, k + 14), net.minecraft.world.entity.EntityTypes.ZOMBIE, rand);
        return true;
    }

    // ------------------------------------------------------------------ o que as três têm em comum

    private static void set(WorldGenLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, Block.UPDATE_CLIENTS);
    }

    private static void spawner(WorldGenLevel level, BlockPos pos, EntityType<?> type, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) spawner.setEntityId(type, random);
    }

    /** O baú do calabouço ({@code dungeonChest}), enchido tantas vezes quantas o original enche. */
    private static void fillDungeonChest(WorldGenLevel level, BlockPos pos, RandomSource random, int times) {
        if (!(level.getBlockEntity(pos) instanceof Container chest)) return;
        ServerLevel server = level.getLevel();
        LootTable table = server.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.SIMPLE_DUNGEON);
        for (int n = 0; n < times; n++) {
            LootParams params = new LootParams.Builder(server).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                    .create(LootContextParamSets.CHEST);
            table.fill(chest, params, random.nextLong());
        }
    }
}
