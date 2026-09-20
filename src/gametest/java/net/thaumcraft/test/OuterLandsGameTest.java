package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.nodes.NodeType;
import net.thaumcraft.block.eldritch.AncientLockBlock;
import net.thaumcraft.block.eldritch.EldritchNothingBlock;
import net.thaumcraft.block.entity.eldritch.AncientLockBlockEntity;
import net.thaumcraft.block.entity.eldritch.EldritchAltarBlockEntity;
import net.thaumcraft.entity.PermanentItemEntity;
import net.thaumcraft.entity.eldritch.EldritchGuardianEntity;
import net.thaumcraft.item.WandTriggers;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.research.ResearchManager;
import net.thaumcraft.world.outer.Cell;
import net.thaumcraft.world.outer.Labyrinth;
import net.thaumcraft.world.outer.MazeFeature;
import net.thaumcraft.world.outer.MazeGenerator;
import net.thaumcraft.world.outer.MazeWorld;

import java.util.ArrayDeque;

/**
 * As Terras de Fora (fatia 6.4): o traçado do labirinto, as salas (portal, chave), a fechadura, a pedra rúnica, o óculo e
 * a passagem pelo portal.
 */
public class OuterLandsGameTest {
    /** O traçado: o portal no meio, a sala do chefe dois por dois num canto, uma sala da chave, e tudo ligado ao portal. */
    @GameTest(maxTicks = 20)
    public void mazeLayout(GameTestHelper helper) {
        for (long seed = 1; seed <= 20; seed++) {
            MazeGenerator gen = new MazeGenerator(17, 15, seed);
            if (!gen.generate()) continue;
            int px = 1 + 17 / 2, py = 1 + 15 / 2;
            if (new Cell((short) gen.grid[py][px]).feature != 1) helper.fail("o portal fica no meio");
            int keys = 0, boss = 0, reachable = 0, open = 0;
            for (int y = 0; y < 15; y++) {
                for (int x = 0; x < 17; x++) {
                    Cell c = new Cell((short) gen.grid[y][x]);
                    if (c.feature == 6) keys++;
                    if (c.feature >= 2 && c.feature <= 5) boss++;
                    if (gen.grid[y][x] != 0) open++;
                    // as passagens são de mão dupla
                    if (c.east && x + 1 < 17 && !new Cell((short) gen.grid[y][x + 1]).west) helper.fail("passagem de mão única no leste");
                    if (c.south && y + 1 < 15 && !new Cell((short) gen.grid[y + 1][x]).north) helper.fail("passagem de mão única no sul");
                }
            }
            if (keys != 1) helper.fail("uma sala da chave, achei " + keys);
            if (boss != 4) helper.fail("a sala do chefe tem quatro partes, achei " + boss);
            boolean[][] seen = new boolean[15][17];
            ArrayDeque<int[]> queue = new ArrayDeque<>();
            queue.add(new int[]{px, py});
            seen[py][px] = true;
            while (!queue.isEmpty()) {
                int[] p = queue.poll();
                reachable++;
                Cell c = new Cell((short) gen.grid[p[1]][p[0]]);
                int[][] steps = {{c.north ? 1 : 0, 0, -1}, {c.south ? 1 : 0, 0, 1}, {c.east ? 1 : 0, 1, 0}, {c.west ? 1 : 0, -1, 0}};
                for (int[] s : steps) {
                    if (s[0] == 0) continue;
                    int nx = p[0] + s[1], ny = p[1] + s[2];
                    if (nx < 0 || ny < 0 || nx >= 17 || ny >= 15 || seen[ny][nx]) continue;
                    seen[ny][nx] = true;
                    queue.add(new int[]{nx, ny});
                }
            }
            // a sala do chefe só se liga por uma das quatro partes; as outras três ficam fechadas por dentro
            if (reachable < open - 3) helper.fail("do portal se chega a " + reachable + " de " + open + " casas");
        }
        helper.succeed();
    }

    /** O labirinto reservado: o portal cai no chunk em que foi pedido. */
    @GameTest(maxTicks = 20)
    public void labyrinthIsCentred(GameTestHelper helper) {
        Labyrinth maze = Labyrinth.get(helper.getLevel().getServer());
        int cx = 40000, cz = -40000;
        maze.build(cx, cz, 15, 15, 99L);
        Cell centre = maze.cell(cx, cz);
        if (centre == null || centre.feature != 1) helper.fail("o portal fica no chunk do anel");
        if (!maze.mazesInRange(cx + 5, cz, 1, 1)) helper.fail("o labirinto ocupa os chunks em volta");
        helper.succeed();
    }

    /** A sala do portal: o capitel, o portal e o obelisco no meio, a casca do nada acesa por dentro. */
    @GameTest(maxTicks = 40)
    public void portalRoom(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos origin = helper.absolutePos(BlockPos.ZERO);
        int cx = origin.getX() >> 4, cz = origin.getZ() >> 4;
        Cell cell = new Cell((short) (256 | MazeGenerator.N | MazeGenerator.S));
        MazeFeature.generate(new MazeWorld(level, level.getRandom()), cx, cz, cell);
        BlockPos portal = new BlockPos(cx * 16 + 8, MazeFeature.FLOOR + 3, cz * 16 + 8);
        if (!level.getBlockState(portal).is(TCBlocks.ELDRITCH_PORTAL)) helper.fail("o portal no meio da sala");
        if (!level.getBlockState(portal.below()).is(TCBlocks.ELDRITCH_CAPSTONE)) helper.fail("o capitel embaixo do portal");
        if (!level.getBlockState(portal.above()).is(TCBlocks.ELDRITCH_OBELISK)) helper.fail("o obelisco em cima");
        // a casca do nada: por dentro, a camada que dá para a sala não existe (é pedra), e a que dá para o corredor aberto, sim
        int exposed = 0;
        for (int x = cx * 16; x < cx * 16 + 16; x++) {
            for (int z = cz * 16; z < cz * 16 + 16; z++) {
                for (int y = MazeFeature.FLOOR; y <= MazeFeature.FLOOR + 12; y++) {
                    var s = level.getBlockState(new BlockPos(x, y, z));
                    if (s.is(TCBlocks.ELDRITCH_NOTHING) && s.getValue(EldritchNothingBlock.EXPOSED)) exposed++;
                }
            }
        }
        if (exposed == 0) helper.fail("a casca do nada devia aparecer nas passagens abertas");
        helper.succeed();
    }

    /**
     * A parte da sala do chefe sem saída: o original escrevia a porta na origem do mundo quando a casa não tinha lado
     * nenhum aberto, e hoje isso derruba a construção do mundo ("Requested chunk unavailable"). Aqui ela não escreve nada.
     */
    @GameTest(maxTicks = 40)
    public void bossRoomWithoutDoorwayKeepsAwayFromTheOrigin(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos origin = helper.absolutePos(BlockPos.ZERO);
        int cx = (origin.getX() >> 4) + 6, cz = origin.getZ() >> 4;
        var antes = level.getBlockState(new BlockPos(0, MazeFeature.FLOOR + 2, 0));
        // parte de cima à esquerda da sala do chefe, sem norte, sul, leste nem oeste
        Cell cell = new Cell((short) (2 << 8));
        MazeFeature.generate(new MazeWorld(level, level.getRandom()), cx, cz, cell);
        if (!level.getBlockState(new BlockPos(0, MazeFeature.FLOOR + 2, 0)).equals(antes)) {
            helper.fail("a sala do chefe mexeu na origem do mundo");
        }
        helper.succeed();
    }

    /** A sala da chave: a tábua rúnica boiando em cima do capitel, com os guardiões em volta. */
    @GameTest(maxTicks = 40)
    public void keyRoom(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos origin = helper.absolutePos(BlockPos.ZERO);
        int cx = (origin.getX() >> 4) + 3, cz = origin.getZ() >> 4;
        Cell cell = new Cell((short) (6 << 8 | MazeGenerator.W));
        MazeFeature.generate(new MazeWorld(level, level.getRandom()), cx, cz, cell);
        BlockPos cap = new BlockPos(cx * 16 + 8, MazeFeature.FLOOR + 2, cz * 16 + 8);
        if (!level.getBlockState(cap).is(TCBlocks.ELDRITCH_CAPSTONE)) helper.fail("o capitel da chave");
        var box = new AABB(cap).inflate(6);
        var tablets = level.getEntitiesOfClass(PermanentItemEntity.class, box);
        if (tablets.isEmpty() || !tablets.getFirst().getItem().is(TCItems.RUNED_TABLET)) helper.fail("a tábua rúnica boiando");
        var guardians = level.getEntitiesOfClass(EldritchGuardianEntity.class, box);
        if (guardians.size() < 2) helper.fail("dois a quatro guardiões, achei " + guardians.size());
        tablets.forEach(net.minecraft.world.entity.Entity::discard);
        guardians.forEach(net.minecraft.world.entity.Entity::discard);
        helper.succeed();
    }

    /** A fechadura: com a tábua, bombeia e, passados cinco segundos, some junto com o intransponível em volta. */
    @GameTest(maxTicks = 200)
    public void lockOpens(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos lock = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlockAndUpdate(lock, TCBlocks.ANCIENT_LOCK.defaultBlockState().setValue(AncientLockBlock.FACING, Direction.NORTH));
        level.setBlockAndUpdate(lock.east(), TCBlocks.IMPASSABLE.defaultBlockState());
        var player = helper.makeMockServerPlayerInLevel();
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(TCItems.RUNED_TABLET));
        var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(lock), Direction.NORTH, lock, false);
        level.getBlockState(lock).useItemOn(player.getMainHandItem(), level, player, net.minecraft.world.InteractionHand.MAIN_HAND, hit);
        if (!(level.getBlockEntity(lock) instanceof AncientLockBlockEntity te) || te.count < 0) throw helper.assertionException("a tábua abre a fechadura");
        helper.succeedWhen(() -> {
            if (level.getBlockState(lock).is(TCBlocks.ANCIENT_LOCK)) helper.fail("a fechadura ainda está lá");
            if (level.getBlockState(lock.east()).is(TCBlocks.IMPASSABLE)) helper.fail("o intransponível some");
            level.getEntitiesOfClass(net.minecraft.world.entity.Entity.class, new AABB(lock).inflate(40),
                    e -> !(e instanceof net.minecraft.world.entity.player.Player)).forEach(net.minecraft.world.entity.Entity::discard);
        });
    }

    /** A pedra rúnica: quem passa a três blocos leva choque. */
    @GameTest(maxTicks = 200)
    public void runedStoneShocks(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), TCBlocks.RUNED_STONE.defaultBlockState());
        var player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        var spot = helper.absoluteVec(new net.minecraft.world.phys.Vec3(2.5, 1.0, 1.5));
        player.snapTo(spot.x, spot.y, spot.z);
        float before = player.getHealth();
        helper.succeedWhen(() -> {
            if (player.getHealth() >= before) helper.fail("a pedra rúnica dá choque");
        });
    }

    /** O óculo: altar com os quatro olhos e nó sombrio em cima, com a varinha, vira portal. */
    @GameTest(maxTicks = 20)
    public void oculusOpens(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos altarPos = helper.absolutePos(new BlockPos(2, 1, 2));
        level.setBlockAndUpdate(altarPos, TCBlocks.ELDRITCH_ALTAR.defaultBlockState());
        var altar = (EldritchAltarBlockEntity) level.getBlockEntity(altarPos);
        altar.setEyes((byte) 4);
        net.thaumcraft.world.NodeFeature.createNodeAt(level, altarPos.above(), level.getRandom(), NodeType.DARK);
        level.setBlockAndUpdate(altarPos.above(2), TCBlocks.ELDRITCH_OBELISK.defaultBlockState());
        Labyrinth.get(level.getServer()).build(altarPos.getX() >> 4, altarPos.getZ() >> 4, 15, 15, 7L);
        var player = helper.makeMockServerPlayerInLevel();
        ResearchManager.complete(player, "OCULUS");
        ItemStack wand = new ItemStack(TCItems.WAND);
        AspectList vis = new AspectList();
        for (Aspect primal : Aspects.primals()) vis.add(primal, 50000);
        wand.set(TCComponents.WAND_VIS, vis);
        if (!(level.getBlockEntity(altarPos.above()) instanceof net.thaumcraft.block.entity.NodeBlockEntity node) || node.type() != NodeType.DARK) throw helper.assertionException("o nó sombrio em cima");
        if (!ResearchManager.knows(player, "OCULUS")) helper.fail("sabe o óculo");
        var result = WandTriggers.use(level, player, altarPos, wand);
        if (!level.getBlockState(altarPos.above()).is(TCBlocks.ELDRITCH_PORTAL)) helper.fail("o nó vira o portal: " + result + " olhos " + altar.getEyes() + " aberto " + altar.isOpen() + " labirinto " + altar.checkForMaze());
        if (!altar.isOpen()) helper.fail("o altar fica aberto");
        helper.succeed();
    }
}
