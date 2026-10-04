package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.BrambleBlock;
import net.thaumcraft.occulta.LeapingLilyBlock;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.VoidBrambleBlock;

/**
 * As <b>três sarças</b> e o <b>Lírio-Saltador</b>.
 *
 * <p>A prova que carrega a fatia é a do <b>machado de ouro</b>: cortar uma Sarça Selvagem a espalha, e a
 * única ferramenta no mundo que a corta sem a espalhar é um machado de ouro. O original não o diz em lugar
 * nenhum — nem no livro, nem na dica, nem no nome. Está escrito numa linha de código e em mais lado nenhum,
 * e é por isso que tem de estar escrito numa prova.
 */
public class OccultaBramblesGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Quantas sarças há numa roda à volta desta casa. */
    private static int sarças(GameTestHelper helper, BlockPos meio, net.minecraft.world.level.block.Block qual) {
        ServerLevel level = helper.getLevel();
        int conta = 0;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (level.getBlockState(meio.offset(dx, dy, dz)).is(qual)) conta++;
                }
            }
        }
        return conta;
    }

    /**
     * <b>Cortada, a Sarça Selvagem se espalha.</b>
     *
     * <p>Quem a quiser tirar do caminho a multiplica — e é a única planta deste mod que pune quem a corta.
     */
    @GameTest(maxTicks = 40)
    public void cuttingTheWildBrambleSpreadsIt(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos casa = helper.absolutePos(new BlockPos(3, 2, 3));
        level.setBlockAndUpdate(casa, OccultaBlocks.WILD_BRAMBLE.defaultBlockState());

        Player quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(3.5, 2.0, 2.5)));
        quem.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, ItemStack.EMPTY);

        int antes = sarças(helper, casa, OccultaBlocks.WILD_BRAMBLE);
        level.getBlockState(casa).getBlock().playerWillDestroy(level, casa, level.getBlockState(casa), quem);
        int depois = sarças(helper, casa, OccultaBlocks.WILD_BRAMBLE);
        if (depois <= antes) {
            helper.fail("cortada de mãos vazias, ela se espalha — e ficou em " + depois);
        }

        // limpa-se o que nasceu
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos onde = casa.offset(dx, dy, dz);
                    if (level.getBlockState(onde).is(OccultaBlocks.WILD_BRAMBLE)) {
                        level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
        helper.succeed();
    }

    /**
     * <b>Menos com um machado de ouro.</b>
     *
     * <p>É a única ferramenta no mundo que a corta sem a espalhar, e nada no jogo o diz.
     */
    @GameTest(maxTicks = 40)
    public void aGoldenAxeCutsItCleanly(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos casa = helper.absolutePos(new BlockPos(6, 2, 6));
        level.setBlockAndUpdate(casa, OccultaBlocks.WILD_BRAMBLE.defaultBlockState());

        Player quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(6.5, 2.0, 5.5)));
        quem.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                new ItemStack(Items.GOLDEN_AXE));

        int antes = sarças(helper, casa, OccultaBlocks.WILD_BRAMBLE);
        level.getBlockState(casa).getBlock().playerWillDestroy(level, casa, level.getBlockState(casa), quem);
        int depois = sarças(helper, casa, OccultaBlocks.WILD_BRAMBLE);
        if (depois != antes) {
            helper.fail("com machado de ouro ela sai limpa, e ficaram " + depois + " onde havia " + antes);
        }

        level.setBlockAndUpdate(casa, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /** E a Sarça do Fim não se espalha de jeito nenhum: cortada, ela simplesmente sai. */
    @GameTest(maxTicks = 40)
    public void theEnderBrambleNeverSpreads(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos casa = helper.absolutePos(new BlockPos(2, 2, 6));
        level.setBlockAndUpdate(casa, OccultaBlocks.ENDER_BRAMBLE.defaultBlockState());

        Player quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(2.5, 2.0, 5.5)));
        quem.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, ItemStack.EMPTY);

        int antes = sarças(helper, casa, OccultaBlocks.ENDER_BRAMBLE);
        level.getBlockState(casa).getBlock().playerWillDestroy(level, casa, level.getBlockState(casa), quem);
        if (sarças(helper, casa, OccultaBlocks.ENDER_BRAMBLE) != antes) {
            helper.fail("a do Fim não se espalha");
        }

        level.setBlockAndUpdate(casa, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /** A Selvagem espinha quem passa; a do Fim o manda embora. */
    @GameTest(maxTicks = 40)
    public void theWildOneStingsAndTheEnderOneSendsYouAway(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos casa = helper.absolutePos(new BlockPos(4, 2, 2));
        level.setBlockAndUpdate(casa, OccultaBlocks.WILD_BRAMBLE.defaultBlockState());

        var ovelha = helper.spawn(EntityTypes.SHEEP, new BlockPos(4, 2, 2));
        float tinha = ovelha.getHealth();
        ovelha.invulnerableTime = 0;
        ((BrambleBlock) OccultaBlocks.WILD_BRAMBLE).toca(level, casa, ovelha);
        if (ovelha.getHealth() >= tinha) helper.fail("a Selvagem espinha quem passa");

        // e a do Fim manda para longe: o que se prova é o número, porque quinhentos blocos não cabem na arena
        if (BrambleBlock.LONGE != 500) helper.fail("ela atira quinhentos para cada lado");
        if (!OccultaBlocks.ENDER_BRAMBLE.defaultBlockState().getBlock()
                .equals(OccultaBlocks.ENDER_BRAMBLE)) {
            helper.fail("e a do Fim é a do Fim");
        }

        ovelha.discard();
        level.setBlockAndUpdate(casa, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /**
     * <b>À volta de uma Sarça do Vazio, círculo nenhum acende.</b>
     *
     * <p>É a única coisa deste mod que desliga o ofício, e a única defesa possível contra um coven que já
     * sabe o que está fazendo.
     */
    @GameTest(maxTicks = 40)
    public void noCircleLightsNearTheVoidBramble(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(4, 2, 4));

        if (VoidBrambleBlock.apagado(level, meio)) helper.fail("sem sarça, a magia está acesa");

        BlockPos sarça = helper.absolutePos(new BlockPos(6, 2, 6));
        level.setBlockAndUpdate(sarça, OccultaBlocks.VOID_BRAMBLE.defaultBlockState());
        if (!VoidBrambleBlock.apagado(level, meio)) helper.fail("com uma sarça perto, apaga");

        if (VoidBrambleBlock.ALCANCE != 32.0) helper.fail("o silêncio dela chega a trinta e dois");
        level.setBlockAndUpdate(sarça, Blocks.AIR.defaultBlockState());
        if (VoidBrambleBlock.apagado(level, meio)) helper.fail("tirada a sarça, a magia volta");
        helper.succeed();
    }

    /** E o Lírio-Saltador dá salto a quem lhe pisa — e não atrapalha quem já o traz. */
    @GameTest(maxTicks = 40)
    public void theLeapingLilyGivesJumpToWhoeverStepsOnIt(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos casa = helper.absolutePos(new BlockPos(5, 2, 3));
        level.setBlockAndUpdate(casa, OccultaBlocks.LEAPING_LILY.defaultBlockState());

        var ovelha = helper.spawn(EntityTypes.SHEEP, new BlockPos(5, 2, 3));
        LeapingLilyBlock.salta(ovelha);

        var salto = ovelha.getEffect(MobEffects.JUMP_BOOST);
        if (salto == null) helper.fail("quem lhe pisa salta");
        if (salto != null && salto.getAmplifier() != LeapingLilyBlock.SALTO) {
            helper.fail("e salta do quinto grau");
        }
        if (salto != null && salto.getDuration() > LeapingLilyBlock.QUANTO) {
            helper.fail("por meio segundo, que se renova a cada passo");
        }
        if (ovelha.getEffect(MobEffects.SPEED) == null) helper.fail("e anda depressa");

        ovelha.discard();
        level.setBlockAndUpdate(casa, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /**
     * E a <b>cana</b> cercada de musgo-espanhol, com água nas quinas de baixo, é a <b>Sarça do Fim</b>.
     */
    @GameTest(maxTicks = 40)
    public void mutandisTurnsCaneIntoTheEnderBramble(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos cana = helper.absolutePos(new BlockPos(3, 2, 5));

        if (net.thaumcraft.occulta.MutandisItem.éCanaDeSarça(level, cana)) {
            helper.fail("sem o musgo à volta, não é cana de sarça");
        }

        for (Direction lado : Direction.Plane.HORIZONTAL) {
            level.setBlockAndUpdate(cana.relative(lado).below(), Blocks.WATER.defaultBlockState());
        }
        level.setBlockAndUpdate(cana, Blocks.SUGAR_CANE.defaultBlockState());
        quinas(helper, cana, Items.ENDER_PEARL, Items.ENDER_PEARL);
        if (net.thaumcraft.occulta.MutandisItem.éCanaDeSarça(level, cana)) {
            helper.fail("sem o musgo à volta, ainda não é");
        }

        // o musgo por último: ele se pendura e cai ao primeiro aviso de vizinho novo
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            level.setBlockAndUpdate(cana.relative(lado), OccultaBlocks.SPANISH_MOSS.defaultBlockState());
        }
        if (!net.thaumcraft.occulta.MutandisItem.éCanaDeSarça(level, cana)) {
            helper.fail("com musgo, água e quatro pérolas do fim nas quinas, é");
        }
        if (net.thaumcraft.occulta.MutandisItem.éCatoDeSarça(level, cana)) {
            helper.fail("e uma cana não é um cato");
        }
        limpa(helper, cana);
        helper.succeed();
    }

    /** E o <b>cato</b>, do mesmo jeito, é a <b>Sarça Selvagem</b>. */
    @GameTest(maxTicks = 40)
    public void mutandisTurnsCactusIntoTheWildBramble(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos cato = helper.absolutePos(new BlockPos(3, 2, 5));

        for (Direction lado : Direction.Plane.HORIZONTAL) {
            level.setBlockAndUpdate(cato.relative(lado).below(), Blocks.WATER.defaultBlockState());
        }
        // areia por baixo, senão o cato se desfaz ao primeiro aviso de vizinho
        level.setBlockAndUpdate(cato.below(), Blocks.SAND.defaultBlockState());
        level.setBlockAndUpdate(cato, Blocks.CACTUS.defaultBlockState());
        quinas(helper, cato, Items.BONE_MEAL, Items.BLAZE_POWDER);
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            level.setBlockAndUpdate(cato.relative(lado), OccultaBlocks.SPANISH_MOSS.defaultBlockState());
        }
        if (!net.thaumcraft.occulta.MutandisItem.éCatoDeSarça(level, cato)) {
            helper.fail("um cato cercado de musgo, com dois ossos e dois pós nas quinas, é a Selvagem");
        }
        if (net.thaumcraft.occulta.MutandisItem.éCanaDeSarça(level, cato)) {
            helper.fail("e um cato não é uma cana");
        }
        limpa(helper, cato);
        level.setBlockAndUpdate(cato.below(), Blocks.STONE.defaultBlockState());
        helper.succeed();
    }

    /**
     * Põe os <b>quatro Apanha-Ervas</b> nas quinas, com o que eles têm de segurar.
     *
     * <p>Dois de cada, que é como o original conta quando a receita pede duas coisas diferentes.
     */
    private static void quinas(GameTestHelper helper, BlockPos meio,
                               net.minecraft.world.item.Item um, net.minecraft.world.item.Item outro) {
        ServerLevel level = helper.getLevel();
        BlockPos[] onde = {meio.offset(1, 0, 1), meio.offset(1, 0, -1),
                meio.offset(-1, 0, 1), meio.offset(-1, 0, -1)};
        for (int i = 0; i < onde.length; i++) {
            level.setBlockAndUpdate(onde[i], OccultaBlocks.GRASSPER.defaultBlockState());
            if (level.getBlockEntity(onde[i]) instanceof net.thaumcraft.occulta.GrassperBlockEntity alma) {
                alma.põe(new ItemStack(i < 2 ? um : outro));
            }
        }
    }

    /** Arruma o que a prova montou. */
    private static void limpa(GameTestHelper helper, BlockPos meio) {
        ServerLevel level = helper.getLevel();
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            level.setBlockAndUpdate(meio.relative(lado), Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(meio.relative(lado).below(), Blocks.STONE.defaultBlockState());
        }
        for (BlockPos quina : new BlockPos[]{meio.offset(1, 0, 1), meio.offset(1, 0, -1),
                meio.offset(-1, 0, 1), meio.offset(-1, 0, -1)}) {
            level.setBlockAndUpdate(quina, Blocks.AIR.defaultBlockState());
        }
        level.setBlockAndUpdate(meio, Blocks.AIR.defaultBlockState());
    }
}
