package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.brew.BrewActions;
import net.thaumcraft.occulta.brew.BrewModifiers;
import net.thaumcraft.occulta.brew.BrewRegistry;
import net.thaumcraft.occulta.vampire.FollowerEntity;
import net.thaumcraft.occulta.vampire.FollowerNames;

/**
 * A <b>Mais Bela de Todas</b>, e o <b>Coração de Ouro</b> que cai dela.
 *
 * <p>O espelho não mente: perguntar-lhe quem é o mais belo pode fazer nascer <b>alguém</b> a cinquenta
 * blocos dali, com nome de conto de fadas, que daí em diante anda pelo mundo sendo mais belo do que quem
 * perguntou. É a coisa mais estranha que o Witchery faz.
 *
 * <p>A prova que carrega a fatia é a de que a Seguidora é <b>duas coisas</b> conforme o tipo. Elle não
 * arde, procura lava e vira Lilith; a Mais Bela arde, não procura nada, e dela cai um coração. Um tipo
 * lido ao contrário dava uma Elle incombustível a andar por um campo, ou uma estranha a virar Lilith.
 */
public class OccultaFairestGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 6; x++) {
            for (int z = 0; z < 6; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** <b>Os dois feitios da Seguidora</b>: Elle não arde, a Mais Bela arde. */
    @GameTest(maxTicks = 20)
    public void thereAreTwoKindsOfFollower(GameTestHelper helper) {
        piso(helper);
        FollowerEntity elle = helper.spawn(OccultaEntities.FOLLOWER, new BlockPos(2, 2, 2));
        if (elle.éAMaisBela() || !elle.fireImmune()) {
            helper.fail("a Seguidora nasce Elle, e Elle não arde");
            return;
        }

        elle.tipo(1);
        if (!elle.éAMaisBela() || elle.fireImmune()) {
            helper.fail("e a Mais Bela é gente: arde como gente");
            return;
        }
        if (FollowerEntity.BELAS != 4 || FollowerEntity.O_HOMEM != 4) {
            helper.fail("são quatro tipos dela, e o quarto é o homem");
            return;
        }
        helper.succeed();
    }

    /** <b>E ela nasce com nome</b>: o quarto tipo é homem, os outros três mulher. */
    @GameTest(maxTicks = 20)
    public void sheIsBornWithAName(GameTestHelper helper) {
        var sorte = helper.getLevel().getRandom();
        for (int tipo = 1; tipo <= FollowerEntity.BELAS; tipo++) {
            String nome = FollowerNames.sorteia(tipo, sorte);
            if (!nome.contains(" ") || nome.length() < 5) {
                helper.fail("o nome dela é «Nome Sobrenome»: saiu «" + nome + "»");
                return;
            }
        }
        // e as duas listas de primeiro nome não são a mesma: cem sorteios do tipo 4 e do tipo 1
        java.util.Set<String> deles = new java.util.HashSet<>();
        java.util.Set<String> delas = new java.util.HashSet<>();
        for (int n = 0; n < 100; n++) {
            deles.add(FollowerNames.sorteia(FollowerEntity.O_HOMEM, sorte).split(" ")[0]);
            delas.add(FollowerNames.sorteia(1, sorte).split(" ")[0]);
        }
        if (java.util.Collections.disjoint(deles, delas)) {
            helper.succeed();
            return;
        }
        helper.fail("a lista do quarto tipo é a dos homens, e as duas não se misturam");
    }

    /**
     * <b>E dela cai o Coração de Ouro</b>, que não cai de mais nada.
     *
     * <p>Uma vez em dez é raro demais para uma prova; o que se prova é que o <b>caminho</b> existe — que a
     * Mais Bela larga coisas e que Elle não larga nenhuma.
     */
    @GameTest(maxTicks = 40)
    public void theHeartFallsFromHerAlone(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        if (FollowerEntity.CORAÇÃO != 0.1f) {
            helper.fail("é uma vez em dez: é " + FollowerEntity.CORAÇÃO);
            return;
        }

        // cem mortes da Mais Bela: uma vez em dez, cair nenhuma é praticamente impossível
        int caíram = 0;
        for (int n = 0; n < 100; n++) {
            FollowerEntity ela = helper.spawn(OccultaEntities.FOLLOWER, new BlockPos(2, 2, 2));
            ela.tipo(1);
            ela.hurtServer(level, level.damageSources().genericKill(), 1000.0f);
            caíram += level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                            new net.minecraft.world.phys.AABB(helper.absolutePos(new BlockPos(2, 2, 2)))
                                    .inflate(4.0)).stream()
                    .filter(item -> item.getItem().is(OccultaItems.HEART_OF_GOLD))
                    .mapToInt(item -> item.getItem().getCount()).sum();
            level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                            new net.minecraft.world.phys.AABB(helper.absolutePos(new BlockPos(2, 2, 2)))
                                    .inflate(4.0))
                    .forEach(net.minecraft.world.entity.Entity::discard);
        }
        if (caíram == 0) {
            helper.fail("em cem mortes dela devia cair algum coração");
            return;
        }

        // e de Elle não cai nenhum, nunca
        for (int n = 0; n < 20; n++) {
            FollowerEntity elle = helper.spawn(OccultaEntities.FOLLOWER, new BlockPos(4, 2, 4));
            elle.hurtServer(level, level.damageSources().genericKill(), 1000.0f);
        }
        boolean algum = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                        new net.minecraft.world.phys.AABB(helper.absolutePos(new BlockPos(4, 2, 4)))
                                .inflate(4.0)).stream()
                .anyMatch(item -> item.getItem().is(OccultaItems.HEART_OF_GOLD));
        if (algum) helper.fail("de Elle não cai coração nenhum");
        else helper.succeed();
    }

    /** <b>O Coração de Ouro está na panela</b>, com os números do original. */
    @GameTest(maxTicks = 20)
    public void theHeartIsInThePot(GameTestHelper helper) {
        var ação = BrewRegistry.of(OccultaItems.HEART_OF_GOLD);
        if (!(ação instanceof BrewActions.AnimalAttraction)) {
            helper.fail("o Coração de Ouro devia ser a Atração de Bichos: é " + ação);
            return;
        }
        if (ação.power() != 500) {
            helper.fail("ele pede quinhentos de poder: pede " + ação.power());
            return;
        }
        if (BrewActions.AnimalAttraction.raio(0) != 16.0
                || BrewActions.AnimalAttraction.raio(3) != 64.0) {
            helper.fail("o alcance é dezesseis por grau a partir de um");
            return;
        }
        helper.succeed();
    }

    /**
     * <b>E ele doma</b> — menos o que pode ser familiar.
     *
     * <p>É a parte do original que custa os quinhentos de poder: um bicho sem dono passa a ser de quem
     * bebeu, de graça. E o gato escapa, porque um frasco não há de roubar o familiar de ninguém.
     */
    @GameTest(maxTicks = 40)
    public void itTamesButNotTheFamiliars(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        LivingEntity quem = helper.spawn(EntityTypes.ARMOR_STAND, new BlockPos(3, 2, 3));
        Wolf lobo = helper.spawn(EntityTypes.WOLF, new BlockPos(2, 2, 2));
        var gato = helper.spawn(EntityTypes.CAT, new BlockPos(4, 2, 4));

        BrewRegistry.of(OccultaItems.HEART_OF_GOLD).applyToEntity(level, quem, new BrewModifiers());

        if (!lobo.isTame()) {
            helper.fail("o lobo sem dono passa a ser de quem bebeu");
            return;
        }
        if (gato.isTame()) {
            helper.fail("e o gato escapa: um frasco não rouba o familiar de ninguém");
            return;
        }
        helper.succeed();
    }

    /**
     * E os bichos <b>vêm</b>, ou <b>fogem</b> se o cozimento estiver invertido.
     *
     * <p>A arena leva <b>folga</b> e o porco é mandado embora no fim: o invertido atira-o a seis ou mais
     * blocos dali, que é para fora de qualquer arena, e um porco largado na do lado estraga a prova de
     * outra pessoa.
     *
     * <p>E o frasco só se dá <b>depois de o porco assentar</b>: um bicho recém-nascido numa prova ainda
     * não tocou o chão, e quem não toca o chão não acha caminho nenhum.
     */
    @GameTest(maxTicks = 80, padding = 16)
    public void theBeastsComeOrFlee(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        LivingEntity quem = helper.spawn(EntityTypes.ARMOR_STAND, new BlockPos(3, 2, 3));
        Animal porco = helper.spawn(EntityTypes.PIG, new BlockPos(1, 2, 1));

        helper.runAfterDelay(20, () -> {
            BrewRegistry.of(OccultaItems.HEART_OF_GOLD)
                    .applyToEntity(level, quem, new BrewModifiers());
            if (porco.getNavigation().isDone()) {
                helper.fail("o porco devia ter posto caminho para quem bebeu");
                return;
            }

            // invertido, ele vai-se embora — e de tão longe que o caminho não dá: teleporta-se
            double antes = porco.distanceToSqr(quem);
            BrewModifiers aoContrário = new BrewModifiers();
            aoContrário.inverted = true;
            BrewRegistry.of(OccultaItems.HEART_OF_GOLD).applyToEntity(level, quem, aoContrário);
            boolean foi = porco.distanceToSqr(quem) > antes || !porco.getNavigation().isDone();
            porco.discard();
            if (!foi) {
                helper.fail("invertido ele manda o porco embora, de uma maneira ou de outra");
                return;
            }
            helper.succeed();
        });
    }
}
