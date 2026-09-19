package net.thaumcraft.block.entity.eldritch;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.block.TaintFibreBlock;
import net.thaumcraft.event.Champions;
import net.thaumcraft.net.TCNetwork;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCSounds;
import net.thaumcraft.world.outer.BossCount;
import net.thaumcraft.world.outer.Cell;
import net.thaumcraft.world.outer.Labyrinth;
import net.thaumcraft.world.outer.MazeBlocks;

import java.util.function.Consumer;

/**
 * A fechadura antiga: o {@code TileEldritchLock} da 4.2.3.5, no meio da porta da sala do chefe. Com a tábua rúnica posta
 * ({@link #count} sai de -1), bombeia por cinco segundos; aí o intransponível em volta se desfaz, a fechadura some e a
 * sala ganha o seu chefe — na ordem que o mundo guarda (o {@code MapBossData}): o golem eldritch, o guardião-mor, o culto
 * carmesim ou a mácula.
 */
public class AncientLockBlockEntity extends BlockEntity {
    /** O golem e o guardião-mor chegam com os chefes (6.5); até lá a sala fica montada e vazia. */
    public static Consumer<Room> golem = room -> {
    }, warden = room -> {
    };

    /** O que o chefe precisa saber: o mundo, o meio da sala, o outro ponto (o do obelisco do guardião) e a fechadura. */
    public record Room(ServerLevel level, int x, int y, int z, int x2, int z2, BlockPos lock) {
    }

    public int count = -1;
    private static final int[][] PED = {{2, 2, 2}, {0, -1, 1}, {3, 3, 3}};

    public AncientLockBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ANCIENT_LOCK, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AncientLockBlockEntity te) {
        if (te.count == -1) return;
        te.count++;
        if (te.count % 5 == 0) level.playSound(null, pos, TCSounds.PUMP.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
        if (te.count > 100) te.doBossSpawn();
    }

    public Direction getFacing() {
        return this.getBlockState().getValue(net.thaumcraft.block.eldritch.AncientLockBlock.FACING);
    }

    /** A tábua rúnica posta: começa a contar. */
    public void unlock() {
        this.count = 0;
        this.setChanged();
        if (this.level != null) this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }

    private void doBossSpawn() {
        Level level = this.level;
        BlockPos pos = this.worldPosition;
        level.playSound(null, pos, TCSounds.ICE.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
        if (!(level instanceof ServerLevel server)) return;
        int cx = pos.getX() >> 4, cz = pos.getZ() >> 4;
        int centerx = cx, centerz = cz, exit = 0;
        Labyrinth maze = Labyrinth.get(server.getServer());
        for (int a = -2; a <= 2; a++) {
            for (int b = -2; b <= 2; b++) {
                Cell c = maze == null ? null : maze.cell(cx + a, cz + b);
                if (c != null && c.feature == 2) {
                    centerx = cx + a;
                    centerz = cz + b;
                }
                if (c != null && c.feature >= 2 && c.feature <= 5 && (c.north || c.south || c.east || c.west)) exit = c.feature;
            }
        }
        BossCount bosses = BossCount.get(server.getServer());
        bosses.bossCount++;
        if (level.getRandom().nextFloat() < 0.25f) bosses.bossCount++;
        bosses.setDirty();
        switch (bosses.bossCount % 4) {
            case 0 -> this.golemRoom(server, centerx, centerz, exit);
            case 1 -> this.wardenRoom(server, centerx, centerz, exit);
            case 2 -> this.cultistRoom(server, centerx, centerz);
            default -> this.taintRoom(server, centerx, centerz);
        }
        for (int a = -2; a <= 2; a++) {
            for (int b = -2; b <= 2; b++) {
                for (int c = -2; c <= 2; c++) {
                    BlockPos p = pos.offset(a, b, c);
                    if (level.getBlockState(p).is(TCBlocks.IMPASSABLE)) {
                        TCNetwork.blockSparkle(server, p, 4194368);
                        level.removeBlock(p, false);
                    }
                }
            }
        }
        level.removeBlock(pos, false);
    }

    private void announce(ServerLevel level, String key) {
        for (var player : level.players()) {
            if (player.distanceToSqr(this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ()) < 300.0) {
                player.sendSystemMessage(Component.translatable(key));
            }
        }
    }

    private static void set(ServerLevel level, int x, int y, int z, BlockState state) {
        level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_ALL);
    }

    private static void obelisk(ServerLevel level, int x, int y, int z) {
        set(level, x, y, z, MazeBlocks.eldritch(1));
        for (int i = 1; i <= 4; i++) set(level, x, y + i, z, MazeBlocks.eldritch(2));
    }

    /** O pedestal de escadas em volta de uma pedra incrustada luminosa. */
    private static void pedestal(ServerLevel level, int x, int y, int z) {
        for (int a = 0; a < 3; a++) {
            for (int b = 0; b < 3; b++) {
                set(level, x - 1 + b, y, z - 1 + a, PED[a][b] < 0 ? MazeBlocks.eldritch(4) : MazeBlocks.stairs(PED[a][b]));
            }
        }
    }

    private static int rarity(float rr, float rare, float uncommon) {
        return rr < rare ? 2 : rr < uncommon ? 1 : 0;
    }

    private void wardenRoom(ServerLevel level, int cx, int cz, int exit) {
        this.announce(level, "tc.boss.warden");
        var random = level.getRandom();
        int x = cx * 16 + 16, y = 50, z = cz * 16 + 16;
        int x2 = x, z2 = z;
        switch (exit) {
            case 2 -> {
                x2 += 8;
                z2 += 8;
            }
            case 3 -> {
                x2 -= 8;
                z2 += 8;
            }
            case 4 -> {
                x2 += 8;
                z2 -= 8;
            }
            case 5 -> {
                x2 -= 8;
                z2 -= 8;
            }
            default -> {
            }
        }
        obelisk(level, x2, y + 4, z);
        obelisk(level, x, y + 4, z2);
        set(level, x2, y + 2, z, MazeBlocks.eldritch(3));
        set(level, x, y + 2, z2, MazeBlocks.eldritch(3));
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                if (a != 0 && b != 0 && random.nextFloat() < 0.9f) {
                    set(level, x2 + a, y + 2, z + b, MazeBlocks.loot(false, rarity(random.nextFloat(), 0.1f, 0.3f)));
                }
                if (a != 0 && b != 0 && random.nextFloat() < 0.9f) {
                    set(level, x + a, y + 2, z2 + b, MazeBlocks.loot(false, rarity(random.nextFloat(), 0.1f, 0.3f)));
                }
            }
        }
        for (int[] c : new int[][]{{-2, -2}, {-2, 2}, {2, 2}, {2, -2}}) set(level, x + c[0], y + 3, z + c[1], MazeBlocks.eldritch(10));
        for (int[] c : new int[][]{{-2, -2}, {-2, 2}, {2, 2}, {2, -2}}) set(level, x + c[0], y + 2, z + c[1], MazeBlocks.cosmetic(15));
        pedestal(level, x2, y + 2, z2);
        warden.accept(new Room(level, x, y, z, x2, z2, this.worldPosition));
    }

    private void golemRoom(ServerLevel level, int cx, int cz, int exit) {
        this.announce(level, "tc.boss.golem");
        var random = level.getRandom();
        int x = cx * 16 + 16, y = 50, z = cz * 16 + 16;
        int x2 = 0, z2 = 0;
        switch (exit) {
            case 2 -> {
                x2 = 8;
                z2 = 8;
            }
            case 3 -> {
                x2 = -8;
                z2 = 8;
            }
            case 4 -> {
                x2 = 8;
                z2 = -8;
            }
            case 5 -> {
                x2 = -8;
                z2 = -8;
            }
            default -> {
            }
        }
        obelisk(level, x + x2, y + 4, z + z2);
        obelisk(level, x - x2, y + 4, z + z2);
        obelisk(level, x + x2, y + 4, z - z2);
        set(level, x + x2, y + 2, z + z2, MazeBlocks.eldritch(3));
        set(level, x - x2, y + 2, z + z2, MazeBlocks.eldritch(3));
        set(level, x + x2, y + 2, z - z2, MazeBlocks.eldritch(3));
        pedestal(level, x, y + 2, z);
        for (int a = -10; a <= 10; a++) {
            for (int b = -10; b <= 10; b++) {
                if ((a < -2 && b < -2 || a > 2 && b > 2 || a < -2 && b > 2 || a > 2 && b < -2) && random.nextFloat() < 0.15f
                        && level.isEmptyBlock(new BlockPos(x + a, y + 2, z + b))) {
                    int md = rarity(random.nextFloat(), 0.05f, 0.2f);
                    set(level, x + a, y + 2, z + b, MazeBlocks.loot(random.nextFloat() < 0.3f, md));
                }
            }
        }
        golem.accept(new Room(level, x, y, z, x2, z2, this.worldPosition));
    }

    private void cultistRoom(ServerLevel level, int cx, int cz) {
        this.announce(level, "tc.boss.crimson");
        var random = level.getRandom();
        int x = cx * 16 + 16, y = 50, z = cz * 16 + 16;
        for (int a = -4; a <= 4; a++) {
            for (int b = -4; b <= 4; b++) {
                if ((Math.abs(a) != 2 && Math.abs(b) != 2 || !random.nextBoolean())
                        && (Math.abs(a) != 3 && Math.abs(b) != 3 || !(random.nextFloat() > 0.33f))
                        && (Math.abs(a) != 4 && Math.abs(b) != 4 || !(random.nextFloat() > 0.25f))) {
                    set(level, x + b, y + 1, z + a, MazeBlocks.eldritch(7));
                }
            }
        }
        for (int a = 0; a < 5; a++) {
            for (int b = 0; b < 5; b++) {
                if (a == 0 || a == 4 || b == 0 || b == 4) {
                    int px = x - 8 + b * 4, pz = z - 8 + a * 4;
                    set(level, px, y + 2, pz, MazeBlocks.cosmetic(11));
                    set(level, px, y + 3, pz, MazeBlocks.eldritch(5));
                    set(level, px, y + 4, pz, MazeBlocks.slab(1));
                    set(level, px, y + 10, pz, MazeBlocks.cosmetic(11));
                    set(level, px, y + 9, pz, MazeBlocks.eldritch(5));
                    set(level, px, y + 8, pz, MazeBlocks.slab(9));
                }
            }
        }
        var boss = TCEntities.CULTIST_PORTAL.create(level, EntitySpawnReason.EVENT);
        if (boss != null) {
            boss.snapTo(x + 0.5, y + 2, z + 0.5, 0.0f, 0.0f);
            level.addFreshEntity(boss);
        }
    }

    private void taintRoom(ServerLevel level, int cx, int cz) {
        this.announce(level, "tc.boss.taint");
        var random = level.getRandom();
        int x = cx * 16 + 16, y = 50, z = cz * 16 + 16;
        var taint = level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(net.thaumcraft.world.TCBiomes.TAINTED_LAND);
        for (int a = -12; a <= 12; a++) {
            for (int b = -12; b <= 12; b++) {
                net.thaumcraft.world.BiomePainter.paint(level, new BlockPos(x + b, y, z + a), taint);
                for (int c = 0; c < 9; c++) {
                    BlockPos p = new BlockPos(x + b, y + 2 + c, z + a);
                    if (level.isEmptyBlock(p) && MazeBlocks.nextToSolid(level, p) && random.nextInt(3) != 0) {
                        level.setBlock(p, TCBlocks.TAINT_FIBRES.defaultBlockState().setValue(TaintFibreBlock.KIND, random.nextInt(4) == 0 ? 1 : 0),
                                Block.UPDATE_ALL);
                    }
                }
                if (random.nextFloat() < 0.15) {
                    set(level, x + b, y + 2, z + a, TCBlocks.TAINT_CRUST.defaultBlockState());
                    if (random.nextFloat() < 0.2) set(level, x + b, y + 3, z + a, TCBlocks.TAINT_CRUST.defaultBlockState());
                }
                if ((Math.abs(a) != 4 && Math.abs(b) != 4 || !random.nextBoolean())
                        && (Math.abs(a) < 5 && Math.abs(b) < 5 || !(random.nextFloat() > 0.33f))
                        && (Math.abs(a) < 7 && Math.abs(b) < 7 || !(random.nextFloat() > 0.25f))) {
                    set(level, x + b, y + 1, z + a, TCBlocks.TAINT_SOIL.defaultBlockState());
                }
            }
        }
        boolean hard = level.getDifficulty() == Difficulty.HARD;
        Mob boss1 = tentacle(level, hard);
        place(level, boss1, x + 0.5, y + 3, z + 0.5);
        boolean giant2 = random.nextBoolean();
        place(level, tentacle(level, giant2), x + 3.5, y + 3, z + 3.5);
        place(level, tentacle(level, !giant2), x - 2.5, y + 3, z + 3.5);
        boolean giant4 = random.nextBoolean();
        place(level, tentacle(level, giant4), x + 3.5, y + 3, z - 2.5);
        place(level, tentacle(level, !giant4), x - 2.5, y + 3, z - 2.5);
    }

    /** O tentáculo, ou o gigante (que chega com os chefes; até lá, o comum). */
    public static java.util.function.Function<ServerLevel, Mob> giantTentacle = level -> TCEntities.TAINTACLE.create(level, EntitySpawnReason.EVENT);

    private static Mob tentacle(ServerLevel level, boolean giant) {
        return giant ? giantTentacle.apply(level) : TCEntities.TAINTACLE.create(level, EntitySpawnReason.EVENT);
    }

    private static void place(ServerLevel level, Mob mob, double x, double y, double z) {
        if (mob == null) return;
        mob.snapTo(x, y, z, 0.0f, 0.0f);
        if (mob instanceof net.minecraft.world.entity.monster.Monster monster) Champions.makeChampion(monster, true);
        level.addFreshEntity(mob);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.count = input.getShortOr("count", (short) -1);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putShort("count", (short) this.count);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
