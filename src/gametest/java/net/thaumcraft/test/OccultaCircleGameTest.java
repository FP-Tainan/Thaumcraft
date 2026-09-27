package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.ChalkItem;
import net.thaumcraft.occulta.CircleHeartBlockEntity;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.RitualCircles;
import net.thaumcraft.occulta.rite.RiteRegistry;

import java.util.List;

/**
 * Os círculos de giz: o que o giz risca, o que os anéis contam e os três primeiros ritos.
 */
public class OccultaCircleGameTest {
    /** O giz risca no chão, troca o que já estava riscado e não risca no ar. */
    @GameTest
    public void chalkDrawsOnTheGround(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos chão = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlockAndUpdate(chão, Blocks.STONE.defaultBlockState());
        BlockPos onde = chão.above();

        if (!ChalkItem.drawOn(level, onde, OccultaBlocks.RITUAL_GLYPH)) {
            helper.fail("o giz devia riscar sobre pedra");
        }
        if (!level.getBlockState(onde).is(OccultaBlocks.RITUAL_GLYPH)) helper.fail("e ficar o glifo de ritual");

        // riscar por cima com outro giz troca o glifo
        if (!ChalkItem.drawOn(level, onde, OccultaBlocks.INFERNAL_GLYPH)) {
            helper.fail("riscar por cima troca o glifo");
        }
        if (!level.getBlockState(onde).is(OccultaBlocks.INFERNAL_GLYPH)) helper.fail("e fica o infernal");

        // no ar, não
        BlockPos noAr = chão.above(3);
        if (ChalkItem.drawOn(level, noAr, OccultaBlocks.RITUAL_GLYPH)) helper.fail("giz não se risca no ar");

        level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(chão, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /** E o chão que some leva o risco com ele. */
    @GameTest
    public void theGlyphFallsWithTheGround(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos chão = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlockAndUpdate(chão, Blocks.STONE.defaultBlockState());
        ChalkItem.drawOn(level, chão.above(), OccultaBlocks.RITUAL_GLYPH);
        level.setBlockAndUpdate(chão, Blocks.AIR.defaultBlockState());
        if (!level.getBlockState(chão.above()).isAir()) {
            helper.fail("sem chão, o risco de giz não fica no ar");
        }
        helper.succeed();
    }

    /** Os três anéis contam-se como o desenho do original manda. */
    @GameTest
    public void theThreeRingsAreCounted(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(9, 3, 9));

        var vazio = RitualCircles.read(level, meio);
        if (vazio.inner().complete()) helper.fail("sem giz nenhum, não há anel nenhum");

        // risca-se o anel de dentro inteiro
        draw(level, meio, 'a', OccultaBlocks.RITUAL_GLYPH);
        var comDentro = RitualCircles.read(level, meio);
        if (comDentro.inner().ritual() != RitualCircles.INNER) {
            helper.fail("o anel de dentro tem dezesseis glifos; contei " + comDentro.inner().ritual());
        }
        if (!comDentro.inner().complete()) helper.fail("e com os dezesseis ele está inteiro");
        if (comDentro.middle().complete()) helper.fail("o do meio continua vazio");

        draw(level, meio, 'b', OccultaBlocks.OTHERWHERE_GLYPH);
        var comMeio = RitualCircles.read(level, meio);
        if (comMeio.middle().otherwhere() != RitualCircles.MIDDLE) {
            helper.fail("o do meio tem vinte e oito; contei " + comMeio.middle().otherwhere());
        }

        draw(level, meio, 'c', OccultaBlocks.INFERNAL_GLYPH);
        var tudo = RitualCircles.read(level, meio);
        if (tudo.outer().infernal() != RitualCircles.OUTER) {
            helper.fail("e o de fora, quarenta; contei " + tudo.outer().infernal());
        }
        helper.succeed();
    }

    /**
     * O rito de cozer: o círculo dele bate, e o passo que ele faz coze o que está no chão.
     *
     * <p>O poder do altar é outra prova: aqui o que se prova é o rito — que ele case com o anel de fora
     * infernal e com o que se ofereceu, e que o passo dele asse a carne.
     */
    @GameTest
    public void theCookingRiteCooksWhatIsOnTheGround(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(9, 3, 9));
        level.setBlockAndUpdate(meio, OccultaBlocks.CIRCLE_HEART.defaultBlockState());
        draw(level, meio, 'c', OccultaBlocks.INFERNAL_GLYPH);

        drop(level, meio, new ItemStack(Items.BLAZE_ROD));
        drop(level, meio, new ItemStack(OccultaItems.WOOD_ASH));
        drop(level, meio, new ItemStack(Items.COAL));
        drop(level, meio, new ItemStack(Items.BEEF, 3));

        var achados = RiteRegistry.find(level, meio, RitualCircles.read(level, meio),
                net.thaumcraft.occulta.rite.Sacrifice.onTheGround(level, meio));
        if (achados.isEmpty()) {
            helper.fail("com o anel de fora infernal e o que se ofereceu, o rito de cozer devia bater");
            return;
        }

        // e o passo dele coze o que está no chão, de vinte em vinte batidas
        var passo = new net.thaumcraft.occulta.rite.Rites.Cook(5.0f, 0.0).steps(0).getFirst();
        var rito = new net.thaumcraft.occulta.rite.ActiveRite(achados.getFirst().rite(), List.of(passo),
                null, 0);
        if (passo.run(level, meio, 20L, rito) != net.thaumcraft.occulta.rite.RiteStep.Result.COMPLETED) {
            helper.fail("com carne no chão, o passo de cozer acaba");
        }
        boolean assado = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                        new net.minecraft.world.phys.AABB(meio).inflate(6.0)).stream()
                .anyMatch(item -> item.getItem().is(Items.COOKED_BEEF));
        if (!assado) helper.fail("e a carne sai assada");

        // sem nada que se possa cozer, ele desiste e devolve
        if (passo.run(level, meio, 40L, rito) != net.thaumcraft.occulta.rite.RiteStep.Result.ABORTED_REFUND) {
            helper.fail("sem comida no chão, o rito desiste");
        }

        level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                        new net.minecraft.world.phys.AABB(meio).inflate(8.0))
                .forEach(net.minecraft.world.entity.Entity::discard);
        helper.succeed();
    }

    /** O passo do poder tira do altar mais perto, e desiste se não houver altar nenhum. */
    @GameTest
    public void thePowerStepDrawsFromTheAltar(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(2, 2, 2));
        var passo = new net.thaumcraft.occulta.rite.Sacrifice.TakePower(5.0f, 0);
        var rito = new net.thaumcraft.occulta.rite.ActiveRite(coven -> List.of(), List.of(), null, 0);
        if (passo.run(level, onde, 0L, rito) != net.thaumcraft.occulta.rite.RiteStep.Result.ABORTED_REFUND) {
            helper.fail("sem altar por perto, o rito desiste e devolve");
        }
        helper.succeed();
    }

    /** Sem rito que bata, o círculo não faz nada. */
    @GameTest
    public void withoutARiteNothingHappens(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(9, 3, 9));
        level.setBlockAndUpdate(meio, OccultaBlocks.CIRCLE_HEART.defaultBlockState());
        if (!(level.getBlockEntity(meio) instanceof CircleHeartBlockEntity coração)) {
            helper.fail("o glifo do meio devia ter alma");
            return;
        }
        coração.toggle(level, null);
        if (coração.busy()) helper.fail("sem giz nenhum em volta, não há rito nenhum a correr");
        helper.succeed();
    }

    /** E a tabela dos ritos tem o que a primeira leva trouxe. */
    @GameTest
    public void theRiteTableHasTheFirstThree(GameTestHelper helper) {
        for (String qual : List.of("tc.rite.cook", "tc.rite.fertility", "tc.rite.eclipse")) {
            if (RiteRegistry.get(qual) == null) helper.fail("falta o rito " + qual);
        }
        var cozer = RiteRegistry.get("tc.rite.cook");
        if (cozer.sacrifice().shown().size() != 3) {
            helper.fail("o de cozer pede três coisas; pede " + cozer.sacrifice().shown().size());
        }
        var eclipse = RiteRegistry.get("tc.rite.eclipse");
        if (!eclipse.when().contains(RiteRegistry.When.DAY)) helper.fail("o eclipse só acontece de dia");
        helper.succeed();
    }

    /** Risca um dos três anéis do desenho, com o giz que se pedir. */
    private static void draw(ServerLevel level, BlockPos meio, char anel, Block giz) {
        String[] desenho = {
                ".................", ".....ccccccc.....", "....c.......c....", "...c..bbbbb..c...",
                "..c..b.....b..c..", ".c..b..aaa..b..c.", ".c.b..a...a..b.c.", ".c.b.a.....a.b.c.",
                ".c.b.a.....a.b.c.", ".c.b.a.....a.b.c.", ".c.b..a...a..b.c.", ".c..b..aaa..b..c.",
                "..c..b.....b..c..", "...c..bbbbb..c...", "....c.......c....", ".....ccccccc.....",
                ".................",
        };
        int raio = 8;
        for (int z = 0; z < desenho.length; z++) {
            String linha = desenho[desenho.length - 1 - z];
            for (int x = 0; x < linha.length(); x++) {
                if (linha.charAt(x) != anel) continue;
                BlockPos onde = meio.offset(x - raio, 0, z - raio);
                level.setBlockAndUpdate(onde.below(), Blocks.STONE.defaultBlockState());
                level.setBlockAndUpdate(onde, giz.defaultBlockState());
            }
        }
    }

    /** Larga uma coisa no meio do círculo. */
    private static void drop(ServerLevel level, BlockPos meio, ItemStack stack) {
        var largado = new net.minecraft.world.entity.item.ItemEntity(level, meio.getX() + 0.5,
                meio.getY() + 0.5, meio.getZ() + 0.5, stack);
        largado.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        level.addFreshEntity(largado);
    }
}
