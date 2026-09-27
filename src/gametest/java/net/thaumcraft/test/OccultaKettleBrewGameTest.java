package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.kettle.KettleBrewItem;
import net.thaumcraft.occulta.kettle.KettleBrews;
import net.thaumcraft.occulta.kettle.KettleRecipes;

/**
 * Os frascos que o Caldeirão de Pote faz, e o que cada um deixa onde bate.
 */
public class OccultaKettleBrewGameTest {
    /** Cada frasco sabe de que feitio é, e todos estão na tabela do pote. */
    @GameTest
    public void everyBrewKnowsItsKindAndHasARecipe(GameTestHelper helper) {
        var frascos = java.util.List.of(OccultaItems.BREW_OF_VINES, OccultaItems.BREW_OF_THORNS,
                OccultaItems.BREW_OF_INK, OccultaItems.BREW_OF_SPROUTING, OccultaItems.BREW_OF_EROSION,
                OccultaItems.BREW_OF_LOVE, OccultaItems.BREW_OF_RAISING);
        java.util.Set<KettleBrews.Kind> feitios = new java.util.HashSet<>();
        for (var frasco : frascos) {
            var qual = KettleBrewItem.kindOf(new ItemStack(frasco));
            if (qual == null) helper.fail("um frasco do pote sabe de que feitio é");
            else if (!feitios.add(qual)) helper.fail("e dois frascos não são do mesmo feitio");
            if (KettleRecipes.of(frasco) == null) helper.fail("e cada um sai de uma receita do pote");
        }
        if (feitios.size() != KettleBrews.Kind.values().length) {
            helper.fail("há um feitio de frasco sem item: " + feitios.size()
                    + " de " + KettleBrews.Kind.values().length);
        }
        helper.succeed();
    }

    /** O de Vinhas veste a parede em que bate, e não faz nada no chão. */
    @GameTest
    public void vinesDressTheWall(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos parede = helper.absolutePos(new BlockPos(2, 3, 2));
        for (int dy = 0; dy < 3; dy++) {
            level.setBlockAndUpdate(parede.above(dy), Blocks.STONE.defaultBlockState());
        }

        var bateu = new BlockHitResult(Vec3.atCenterOf(parede.north()), Direction.NORTH, parede, false);
        if (!KettleBrews.Kind.VINES.impact(level, bateu, null)) helper.fail("a vinha devia pegar na parede");
        if (!level.getBlockState(parede.north()).is(Blocks.VINE)) {
            helper.fail("e a vinha nasce no lado em que ele bateu");
        }

        // no teto e no chão ele não pega
        var deCima = new BlockHitResult(Vec3.atCenterOf(parede.above(3)), Direction.UP, parede.above(2), false);
        if (KettleBrews.Kind.VINES.impact(level, deCima, null)) helper.fail("no chão a vinha não pega");

        for (int dy = 0; dy < 3; dy++) {
            level.setBlockAndUpdate(parede.above(dy), Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(parede.north().above(dy), Blocks.AIR.defaultBlockState());
        }
        helper.succeed();
    }

    /** O de Espinhos planta cacto onde há chão, e não planta sobre pedra do nether. */
    @GameTest
    public void thornsPlantCactus(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos chão = helper.absolutePos(new BlockPos(4, 2, 2));
        level.setBlockAndUpdate(chão, Blocks.DIRT.defaultBlockState());

        var bateu = new BlockHitResult(Vec3.atCenterOf(chão.above()), Direction.UP, chão, false);
        if (!KettleBrews.Kind.THORNS.impact(level, bateu, null)) helper.fail("o cacto devia pegar na terra");
        if (!level.getBlockState(chão).is(Blocks.SAND)) helper.fail("a terra vira areia debaixo dele");
        if (!level.getBlockState(chão.above()).is(Blocks.CACTUS)) helper.fail("e o cacto nasce por cima");

        for (int dy = 0; dy <= KettleBrews.CACTUS_HEIGHT; dy++) {
            level.setBlockAndUpdate(chão.above(dy), Blocks.AIR.defaultBlockState());
        }
        helper.succeed();
    }

    /** O de Tinta cega quem está em roda. */
    @GameTest
    public void inkBlindsWhatIsNear(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var bicho = helper.spawn(EntityTypes.PIG, new BlockPos(2, 2, 4));
        bicho.setNoAi(true);

        var onde = new BlockHitResult(bicho.position(), Direction.UP, bicho.blockPosition(), false);
        if (!KettleBrews.Kind.INK.impact(level, onde, null)) helper.fail("a tinta devia pegar em alguém");
        if (!bicho.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS)) {
            helper.fail("e quem está perto fica cego");
        }
        bicho.discard();
        helper.succeed();
    }

    /** O de Brotação faz crescer um galho para onde ele foi. */
    @GameTest
    public void sproutingGrowsABranch(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos chão = helper.absolutePos(new BlockPos(4, 2, 4));
        level.setBlockAndUpdate(chão, Blocks.DIRT.defaultBlockState());

        var bateu = new BlockHitResult(Vec3.atCenterOf(chão.above()), Direction.UP, chão, false);
        if (!KettleBrews.Kind.SPROUTING.impact(level, bateu, null)) helper.fail("o galho devia crescer");
        if (level.getBlockState(chão.above()).isAir()) helper.fail("e a primeira casa dele é tronco");

        for (int dy = 1; dy <= KettleBrews.BRANCH; dy++) {
            level.setBlockAndUpdate(chão.above(dy), Blocks.AIR.defaultBlockState());
        }
        level.setBlockAndUpdate(chão, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /** O de Erosão come o que está em volta e devolve em obsidiana o que havia dela. */
    @GameTest
    public void erosionEatsAndGivesBackObsidian(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(6, 3, 6));
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.setBlockAndUpdate(meio.offset(dx, 0, dz), Blocks.OBSIDIAN.defaultBlockState());
            }
        }

        var bateu = new BlockHitResult(Vec3.atCenterOf(meio), Direction.UP, meio, false);
        if (!KettleBrews.Kind.EROSION.impact(level, bateu, null)) helper.fail("a erosão devia pegar");
        if (!level.getBlockState(meio).isAir()) helper.fail("e come o que estava lá");

        boolean achou = false;
        for (var largado : level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                new net.minecraft.world.phys.AABB(meio).inflate(4.0))) {
            if (largado.getItem().is(net.minecraft.world.item.Items.OBSIDIAN)) achou = true;
            largado.discard();
        }
        if (!achou) helper.fail("e a obsidiana que havia volta em item");
        helper.succeed();
    }

    /** O de Amor apaixona os bichos em roda. */
    @GameTest
    public void loveMakesAnimalsBreed(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var bicho = helper.spawn(EntityTypes.PIG, new BlockPos(6, 2, 2));
        bicho.setNoAi(true);

        var onde = new BlockHitResult(bicho.position(), Direction.UP, bicho.blockPosition(), false);
        if (!KettleBrews.Kind.LOVE.impact(level, onde, null)) helper.fail("o amor devia pegar num bicho");
        if (!bicho.isInLove()) helper.fail("e o bicho apaixona-se");
        bicho.discard();
        helper.succeed();
    }

    /** E o de Erguer os Mortos levanta um morto onde bate. */
    @GameTest
    public void raisingRaisesTheDead(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos chão = helper.absolutePos(new BlockPos(6, 2, 4));
        level.setBlockAndUpdate(chão, Blocks.DIRT.defaultBlockState());

        var bateu = new BlockHitResult(Vec3.atCenterOf(chão.above()), Direction.UP, chão, false);
        if (!KettleBrews.Kind.RAISING.impact(level, bateu, null)) helper.fail("o morto devia levantar-se");
        helper.runAfterDelay(2, () -> {
            var mortos = level.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class,
                    new net.minecraft.world.phys.AABB(chão).inflate(3.0));
            if (mortos.isEmpty()) helper.fail("e fica de pé onde o frasco bateu");
            for (var morto : mortos) morto.discard();
            level.setBlockAndUpdate(chão, Blocks.AIR.defaultBlockState());
            helper.succeed();
        });
    }

    /** O frasco atirado por alguém vira o bicho que voa. */
    @GameTest
    public void aThrownBrewFlies(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                new ItemStack(OccultaItems.BREW_OF_INK, 2));
        OccultaItems.BREW_OF_INK.use(level, quem, net.minecraft.world.InteractionHand.MAIN_HAND);
        if (quem.getMainHandItem().getCount() != 1) helper.fail("atirar um frasco gasta um frasco");
        helper.succeed();
    }
}
