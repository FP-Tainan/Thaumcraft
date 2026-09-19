package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.api.nodes.NodeType;
import net.thaumcraft.block.ObsidianTotemBlock;
import net.thaumcraft.block.eldritch.AncientRockBlock;
import net.thaumcraft.block.eldritch.EldritchInsetBlock;
import net.thaumcraft.block.entity.NodeBlockEntity;
import net.thaumcraft.block.entity.eldritch.EldritchAltarBlockEntity;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.world.RuinsFeature;

/**
 * As ruínas do mundo de cima da 4.2.3.5 ({@code WorldGenEldritchRing}, {@code WorldGenMound}, {@code WorldGenHilltopStones},
 * o {@code generateTotem}) e os blocos delas: o totem com as figuras da coluna, a rocha antiga em ladrilho, as pedras
 * engastadas, as urnas e o altar eldritch.
 */
public class RuinsGameTest {
    /** Um chão de grama de um lado a outro, bem acima dos outros testes. */
    private static BlockPos ground(GameTestHelper helper, int size) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(new BlockPos(0, 150, 0));
        for (int x = -size; x <= size; x++) for (int z = -size; z <= size; z++) {
            level.setBlockAndUpdate(base.offset(x, -1, z), Blocks.GRASS_BLOCK.defaultBlockState());
            for (int y = 0; y < 18; y++) level.setBlockAndUpdate(base.offset(x, y, z), Blocks.AIR.defaultBlockState());
        }
        return base;
    }

    private static void clear(GameTestHelper helper, BlockPos base, int size) {
        for (int x = -size; x <= size; x++) for (int z = -size; z <= size; z++) for (int y = -6; y < 18; y++) {
            helper.getLevel().setBlockAndUpdate(base.offset(x, y, z), Blocks.AIR.defaultBlockState());
        }
    }

    /** O anel: a laje, o altar com o obelisco três acima, os quatro de cima e oito capitéis na borda. */
    @GameTest(maxTicks = 40)
    public void eldritchRingIsBuilt(GameTestHelper helper) {
        BlockPos base = ground(helper, 5);
        ServerLevel level = helper.getLevel();
        BlockPos top = base.below();
        if (!RuinsFeature.eldritchRing(level, level.getRandom(), top.getX(), top.getY(), top.getZ(), 0, 0, 11, 11)) {
            helper.fail("o anel devia caber no chão plano");
        }
        if (!level.getBlockState(top.above()).is(TCBlocks.ELDRITCH_ALTAR)) helper.fail("sem altar no meio");
        if (!level.getBlockState(top.above(3)).is(TCBlocks.ELDRITCH_OBELISK)) helper.fail("sem obelisco três acima do altar");
        for (int a = 4; a <= 7; a++) if (!level.getBlockState(top.above(a)).is(TCBlocks.ELDRITCH_OBELISK_UPPER)) helper.fail("falta o obelisco " + a);
        int caps = 0;
        for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++) if (level.getBlockState(top.offset(x, 1, z)).is(TCBlocks.ELDRITCH_CAPSTONE)) caps++;
        if (caps != 8) helper.fail("o anel tem oito capitéis, tem " + caps);
        BlockState floor = level.getBlockState(top.offset(2, 0, 2));
        if (!floor.is(TCBlocks.OBSIDIAN_TILE) && !floor.is(Blocks.OBSIDIAN)) helper.fail("a laje é de obsidiana");
        // quebrado o altar, as peças vizinhas somem
        level.destroyBlock(top.above(), false);
        if (level.getBlockState(top.above(3)).is(TCBlocks.ELDRITCH_OBELISK)) helper.fail("quebrado o altar, o obelisco devia sumir");
        clear(helper, base, 5);
        helper.succeed();
    }

    /** Os olhos no altar: até quatro, e do terceiro em diante o altar chama guardiões. */
    @GameTest(maxTicks = 40)
    public void eyesGoOnTheAltar(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.getLevel().setBlockAndUpdate(pos, TCBlocks.ELDRITCH_ALTAR.defaultBlockState());
        var player = helper.makeMockServerPlayerInLevel();
        for (int n = 0; n < 5; n++) {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TCItems.ELDRITCH_EYE));
            var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos), net.minecraft.core.Direction.UP, pos, false);
            helper.getLevel().getBlockState(pos).useItemOn(player.getMainHandItem(), helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
        }
        var altar = (EldritchAltarBlockEntity) helper.getLevel().getBlockEntity(pos);
        if (altar.getEyes() != 4) helper.fail("quatro olhos no máximo, tem " + altar.getEyes());
        if (!altar.isSpawner() || altar.getSpawnType() != 1) helper.fail("com o terceiro olho o altar devia chamar guardiões");
        if (!player.getMainHandItem().isEmpty() && player.getMainHandItem().getCount() != 1) helper.fail("o quinto olho não devia ser gasto");
        helper.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /** O túmulo: as duas urnas (ou caixotes), o baú e os dois geradores, onde o original os põe. */
    @GameTest(maxTicks = 40)
    public void moundIsBuilt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos corner = helper.absolutePos(new BlockPos(0, 150, 0));
        int i = corner.getX(), j = corner.getY(), k = corner.getZ();
        for (int[] p : new int[][]{{9, 9}, {0, 0}, {18, 0}, {18, 18}, {0, 18}}) {
            level.setBlockAndUpdate(new BlockPos(i + p[0], j + 9, k + p[1]), Blocks.GRASS_BLOCK.defaultBlockState());
            level.setBlockAndUpdate(new BlockPos(i + p[0], j + 10, k + p[1]), Blocks.AIR.defaultBlockState());
        }
        if (!RuinsFeature.mound(level, level.getRandom(), i, j, k)) helper.fail("o túmulo devia caber");
        for (int dz : new int[]{7, 11}) {
            var block = level.getBlockState(new BlockPos(i + 9, j + 1, k + dz)).getBlock();
            if (!(block instanceof net.thaumcraft.block.eldritch.LootBlock)) helper.fail("sem urna nem caixote em " + dz);
        }
        BlockState chest = level.getBlockState(new BlockPos(i + 10, j + 1, k + 9));
        if (!chest.is(Blocks.CHEST) && !chest.is(Blocks.TRAPPED_CHEST)) helper.fail("sem baú na câmara");
        if (!level.getBlockState(new BlockPos(i + 4, j + 5, k + 4)).is(Blocks.SPAWNER)) helper.fail("sem gerador de esqueleto");
        if (!level.getBlockState(new BlockPos(i + 4, j + 5, k + 14)).is(Blocks.SPAWNER)) helper.fail("sem gerador de zumbi");
        for (int x = 0; x <= 18; x++) for (int z = 0; z <= 18; z++) for (int y = -2; y <= 16; y++) {
            level.setBlock(new BlockPos(i + x, j + y, k + z), Blocks.AIR.defaultBlockState(), 2 | 16);
        }
        helper.succeed();
    }

    /** O totem: ladrilho em baixo, a coluna, e o carregado com um nó sombrio em cima; as figuras seguem a coluna. */
    @GameTest(maxTicks = 40)
    public void totemGrowsWithADarkNode(GameTestHelper helper) {
        BlockPos base = ground(helper, 1);
        ServerLevel level = helper.getLevel();
        RuinsFeature.totem(level, level.getRandom(), base.getX(), base.getZ());
        if (!level.getBlockState(base.below()).is(TCBlocks.OBSIDIAN_TILE)) helper.fail("o totem nasce em cima de ladrilho");
        BlockPos charged = null;
        for (int y = 0; y < 6; y++) if (level.getBlockState(base.above(y)).is(TCBlocks.CHARGED_OBSIDIAN_TOTEM)) charged = base.above(y);
        if (charged == null) helper.fail("sem totem carregado");
        if (!(level.getBlockEntity(charged) instanceof NodeBlockEntity node) || node.type() != NodeType.DARK) helper.fail("o carregado guarda um nó sombrio");
        BlockState first = level.getBlockState(base);
        if (first.getValue(ObsidianTotemBlock.PART) != ObsidianTotemBlock.Part.SHADED) helper.fail("o de baixo tem totem em cima: base sombreada");
        BlockState last = level.getBlockState(charged);
        if (last.getValue(ObsidianTotemBlock.PART) != ObsidianTotemBlock.Part.CARVED) helper.fail("o de cima só tem totem embaixo: entalhado");
        clear(helper, base, 1);
        helper.succeed();
    }

    /** As pedras do topo: só acima de 85, com o círculo de totens e o baú com o gerador de fogo-fátuo por baixo. */
    @GameTest(maxTicks = 40)
    public void hilltopStonesAreBuilt(GameTestHelper helper) {
        BlockPos base = ground(helper, 4);
        ServerLevel level = helper.getLevel();
        BlockPos top = base.below();
        if (!RuinsFeature.hilltopStones(level, level.getRandom(), top.getX(), top.getY(), top.getZ())) helper.fail("devia caber");
        if (!level.getBlockState(top.above(2)).is(Blocks.CHEST)) helper.fail("sem baú no meio");
        if (!level.getBlockState(top).is(Blocks.SPAWNER)) helper.fail("sem gerador por baixo do baú");
        clear(helper, base, 4);
        helper.succeed();
    }

    /** A rocha antiga guarda a paridade do lugar; a pedra engastada, as faces soltas; a de glifos deixa um fragmento. */
    @GameTest(maxTicks = 40)
    public void ancientBlocksKnowWhereTheyAre(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        level.setBlockAndUpdate(pos, TCBlocks.ANCIENT_ROCK.defaultBlockState());
        BlockState rock = level.getBlockState(pos);
        if (rock.getValue(AncientRockBlock.ODD_X) != ((pos.getX() & 1) != 0) || rock.getValue(AncientRockBlock.ODD_Z) != ((pos.getZ() & 1) != 0)) {
            helper.fail("a rocha antiga devia saber a paridade do lugar");
        }
        BlockPos inset = pos.above(2);
        level.setBlockAndUpdate(inset, EldritchInsetBlock.shape(TCBlocks.GLOWING_CRUSTED_STONE.defaultBlockState(), level, inset));
        level.setBlockAndUpdate(inset.below(), Blocks.STONE.defaultBlockState());
        BlockState glowing = level.getBlockState(inset);
        if (glowing.getValue(EldritchInsetBlock.OPEN.get(net.minecraft.core.Direction.DOWN))) helper.fail("encostada na pedra, a face de baixo não é solta");
        if (!glowing.getValue(EldritchInsetBlock.OPEN.get(net.minecraft.core.Direction.UP))) helper.fail("sem nada em cima, a face de cima é solta");
        if (glowing.getLightEmission() != 12) helper.fail("a pedra incrustada luminosa tem luz doze");
        var drops = net.minecraft.world.level.block.Block.getDrops(TCBlocks.GLYPHED_STONE.defaultBlockState(), level, pos, null,
                null, new ItemStack(net.minecraft.world.item.Items.IRON_PICKAXE));
        if (drops.size() != 1 || !drops.getFirst().is(net.thaumcraft.registry.TCResources.get("knowledge_fragment"))) {
            helper.fail("a pedra de glifos deixa um fragmento de conhecimento, deixou " + drops);
        }
        var urn = net.minecraft.world.level.block.Block.getDrops(TCBlocks.LOOT_URNS.get(2).defaultBlockState(), level, pos, null,
                null, ItemStack.EMPTY);
        if (urn.size() < 3 || urn.size() > 5) helper.fail("a urna rara derrama de três a cinco coisas, derramou " + urn.size());
        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(inset, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(inset.below(), Blocks.AIR.defaultBlockState());
        helper.succeed();
    }
}
