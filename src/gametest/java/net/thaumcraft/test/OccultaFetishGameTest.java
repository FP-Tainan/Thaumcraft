package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaComponents;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.TaglockItem;
import net.thaumcraft.occulta.fetish.FetishBlockEntity;
import net.thaumcraft.occulta.fetish.Fetishes;
import net.thaumcraft.occulta.fetish.SpiritEffects;
import net.thaumcraft.occulta.ghost.SpectreEntity;

import java.util.List;

/**
 * Os três <b>fetiches</b>: o Espantalho, a Escada de Bruxa e o Ídolo de Treant.
 *
 * <p>A prova que carrega a fatia é a de <b>quem cabe primeiro</b>. O rito não escolhe o efeito — ele pega
 * no <b>primeiro da lista cuja conta couber</b> no que estiver dentro do círculo. Quem quiser a Sentinela
 * e levar três espectros <b>e</b> duas banshees leva, em vez dela, a Proteção de Vodu, que é a primeira e
 * pede menos de cada. A ordem da lista é uma regra do jogo, e não um detalhe.
 */
public class OccultaFetishGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    private static FetishBlockEntity põe(GameTestHelper helper, BlockPos onde) {
        ServerLevel level = helper.getLevel();
        level.setBlockAndUpdate(onde, OccultaBlocks.SCARECROW.defaultBlockState());
        return level.getBlockEntity(onde) instanceof FetishBlockEntity alma ? alma : null;
    }

    // ------------------------------------------------------------------ os números

    /** O preço de cada efeito é o do original, e a ordem deles também. */
    @GameTest
    public void thePricesAreTheOriginals(GameTestHelper helper) {
        SpiritEffects.init();
        var conta = new int[][]{
                {1, 3, 1, 1, 1}, {2, 3, 3, 0, 0}, {3, 3, 0, 2, 0},
                {4, 3, 0, 0, 2}, {5, 3, 1, 1, 0}, {6, 0, 5, 5, 5},
        };
        for (int[] linha : conta) {
            SpiritEffects qual = SpiritEffects.byId(linha[0]);
            if (qual == null) {
                helper.fail("falta o efeito " + linha[0]);
                return;
            }
            if (qual.spirits != linha[1] || qual.spectres != linha[2]
                    || qual.banshees != linha[3] || qual.poltergeists != linha[4]) {
                helper.fail("a conta do efeito " + linha[0] + " é " + qual.spirits + ", "
                        + qual.spectres + ", " + qual.banshees + ", " + qual.poltergeists);
            }
        }
        if (SpiritEffects.ENHANCED_POPPETS.radius() != 0.0) helper.fail("a Proteção não procura nada");
        if (SpiritEffects.SENTINEL.radius() != 8.0) helper.fail("a Sentinela vê a oito");
        if (SpiritEffects.SCREAMER.radius() != 16.0) helper.fail("e o Grito a dezesseis");
        if (!SpiritEffects.SCREAMER.redstone()) helper.fail("e só ele manda redstone");
        if (SpiritEffects.SENTINEL.cooldown() != 600) helper.fail("a Sentinela descansa trinta segundos");
        if (SpiritEffects.TWISTER.cooldown() != 10) helper.fail("e a Desorientação dez batidas");
        if (SpiritEffects.DEATH.inBook()) helper.fail("e a Morte não entra no livro");
        helper.succeed();
    }

    /**
     * <b>Quem cabe primeiro.</b>
     *
     * <p>Esta é a prova que carrega a fatia. Três espíritos, três espectros, duas banshees e dois
     * poltergeists cabem em <b>tudo</b> — e o que sai é o <b>primeiro da lista</b>, que é a Proteção de
     * Vodu. Para ter a Sentinela, leva-se o que ela pede e <b>não mais</b>.
     */
    @GameTest
    public void theFirstThatFitsIsTheOneYouGet(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();

        var espíritos = bichos(helper, OccultaEntities.SPIRIT, 3, 1);
        var espectros = bichos(helper, OccultaEntities.SPECTRE, 3, 3);
        var banshees = bichos(helper, OccultaEntities.BANSHEE, 2, 5);
        var poltergeists = bichos(helper, OccultaEntities.POLTERGEIST, 2, 7);

        ItemStack fetiche = new ItemStack(OccultaItems.SCARECROW);
        var saiu = SpiritEffects.bind(level, fetiche, espíritos, espectros, banshees, poltergeists);
        if (saiu != SpiritEffects.ENHANCED_POPPETS) {
            helper.fail("cabendo tudo, sai a primeira da lista; saiu " + (saiu == null ? "nada" : saiu.key));
        }
        if (SpiritEffects.idOf(fetiche) != SpiritEffects.ENHANCED_POPPETS.id) {
            helper.fail("e o número dela fica escrito no fetiche");
        }

        // e gastou um de cada, que é o que ela pede
        int vivos = 0;
        for (var lista : List.of(espíritos, espectros, banshees, poltergeists)) {
            for (var quem : lista) {
                if (!quem.isRemoved()) vivos++;
            }
        }
        if (vivos != 0 + 2 + 1 + 1) {
            helper.fail("devia ter gastado três espíritos e um de cada fantasma; sobraram " + vivos);
        }

        for (var lista : List.of(espíritos, espectros, banshees, poltergeists)) {
            for (var quem : lista) quem.discard();
        }
        helper.succeed();
    }

    /** <b>E não cabendo nenhum, não se prende nada.</b> */
    @GameTest
    public void nothingBindsWithoutEnoughSpirits(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var poucos = bichos(helper, OccultaEntities.SPIRIT, 2, 1);

        ItemStack fetiche = new ItemStack(OccultaItems.SCARECROW);
        var saiu = SpiritEffects.bind(level, fetiche, poucos, List.of(), List.of(), List.of());
        if (saiu != null) helper.fail("com dois espíritos não se prende nada; prendeu " + saiu.key);
        if (SpiritEffects.idOf(fetiche) != 0) helper.fail("e o fetiche fica vazio");
        for (var quem : poucos) {
            if (quem.isRemoved()) helper.fail("e nenhum bicho se gasta");
            quem.discard();
        }
        helper.succeed();
    }

    /** <b>E a Sentinela sai quando é ela que cabe, e só ela.</b> */
    @GameTest
    public void theSentinelComesOutWhenOnlyItFits(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var espíritos = bichos(helper, OccultaEntities.SPIRIT, 3, 1);
        var espectros = bichos(helper, OccultaEntities.SPECTRE, 3, 3);

        ItemStack fetiche = new ItemStack(OccultaItems.SCARECROW);
        var saiu = SpiritEffects.bind(level, fetiche, espíritos, espectros, List.of(), List.of());
        if (saiu != SpiritEffects.SENTINEL) {
            helper.fail("sem banshee e sem poltergeist, sai a Sentinela; saiu "
                    + (saiu == null ? "nada" : saiu.key));
        }
        for (var lista : List.of(espíritos, espectros)) {
            for (var quem : lista) quem.discard();
        }
        helper.succeed();
    }

    private static <T extends net.minecraft.world.entity.Mob> List<T> bichos(
            GameTestHelper helper, net.minecraft.world.entity.EntityType<T> qual, int quantos, int z) {
        var feitos = new java.util.ArrayList<T>();
        for (int i = 0; i < quantos; i++) {
            feitos.add(helper.spawn(qual, new BlockPos(1 + i, 2, z)));
        }
        return feitos;
    }

    // ------------------------------------------------------------------ o alarme

    /** Os seis modos do alarme, e o que cada um faz. */
    @GameTest
    public void theSixModesAreTheOriginals(GameTestHelper helper) {
        if (FetishBlockEntity.OFF != 5) helper.fail("o modo desligado é o cinco");
        if (FetishBlockEntity.DEFAULT_COLOR != 9) helper.fail("e a tinta de nascença é o cinza-claro");
        if (FetishBlockEntity.LOOKS != 20) helper.fail("e ele olha de segundo em segundo");
        if (Fetishes.MODE_KEYS.length != 6) helper.fail("e os modos são seis");
        helper.succeed();
    }

    /**
     * <b>Ele nasce desligado, e a Boline roda os modos.</b>
     *
     * <p>Um espantalho que disparasse ao ser posto seria uma armadilha para quem o pôs.
     */
    @GameTest
    public void theBolineCyclesTheMode(GameTestHelper helper) {
        piso(helper);
        BlockPos onde = helper.absolutePos(new BlockPos(3, 2, 3));
        FetishBlockEntity alma = põe(helper, onde);
        if (alma == null) {
            helper.fail("o espantalho devia ter alma");
            return;
        }
        if (alma.alarmMode() != FetishBlockEntity.OFF) helper.fail("ele nasce desligado");

        var quem = helper.makeMockServerPlayerInLevel();
        alma.cycleMode(quem);
        if (alma.alarmMode() != 0) helper.fail("a primeira volta o leva ao zero");
        for (int volta = 0; volta < 5; volta++) alma.cycleMode(quem);
        if (alma.alarmMode() != FetishBlockEntity.OFF) {
            helper.fail("e seis voltas trazem-no de volta ao desligado");
        }

        helper.getLevel().removeBlock(onde, false);
        helper.succeed();
    }

    /**
     * <b>O alarme dispara pela ausência.</b>
     *
     * <p>É o que faz do Espantalho uma coisa diferente de um alarme comum: nos modos três e quatro ele
     * dispara quando os conhecidos <b>não estão</b>. Um espantalho que conhece as suas vacas e avisa
     * quando falta uma é a melhor ideia do bloco.
     */
    @GameTest
    public void theAlarmFiresOnAbsence(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(3, 2, 3));
        FetishBlockEntity alma = põe(helper, onde);
        if (alma == null) {
            helper.fail("o espantalho devia ter alma");
            return;
        }
        alma.effectType(SpiritEffects.SCREAMER.id);

        var vaca = helper.spawn(EntityTypes.COW, new BlockPos(3, 2, 5));
        var quem = helper.makeMockServerPlayerInLevel();
        alma.toggleKnown(new TaglockItem.Taglock(vaca.getUUID(),
                        vaca.getType().getDescription().getString(), true),
                vaca.getType().getDescription());
        if (alma.knownTypes().size() != 1) helper.fail("uma vaca entra por espécie, e não por nome");

        // modo quatro: dispara quando nenhum dos conhecidos está
        for (int volta = 0; volta < 5; volta++) alma.cycleMode(quem);
        if (alma.alarmMode() != FetishBlockEntity.WHEN_NONE_FOUND) {
            helper.fail("cinco voltas levam ao modo quatro; está no " + alma.alarmMode());
        }

        alma.look(level);
        if (alma.alarm()) helper.fail("com a vaca ali, ele se cala");

        vaca.discard();
        alma.look(level);
        if (!alma.alarm()) helper.fail("e sem ela, grita");

        level.removeBlock(onde, false);
        helper.succeed();
    }

    /** <b>E o Grito manda redstone enquanto está levantado.</b> */
    @GameTest
    public void theScreamerSendsRedstone(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(3, 2, 3));
        FetishBlockEntity alma = põe(helper, onde);
        if (alma == null) {
            helper.fail("o espantalho devia ter alma");
            return;
        }
        alma.effectType(SpiritEffects.SCREAMER.id);
        if (alma.signal() != 0) helper.fail("calado, não manda nada");

        var quem = helper.makeMockServerPlayerInLevel();
        quem.setPos(onde.getX() + 1.5, onde.getY(), onde.getZ() + 0.5);
        alma.cycleMode(quem); // modo zero: gente fora da lista
        alma.look(level);
        if (!alma.alarm()) helper.fail("com gente de fora a um bloco, ele levanta o alarme");
        if (alma.signal() != FetishBlockEntity.SIGNAL) helper.fail("e manda quinze");

        level.removeBlock(onde, false);
        helper.succeed();
    }

    /** <b>E ele não se assusta com o que a bruxa pôs lá.</b> */
    @GameTest
    public void itIgnoresWhatTheWitchPutThere(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var espírito = helper.spawn(OccultaEntities.SPIRIT, new BlockPos(3, 2, 3));
        if (!Fetishes.ignorable(level, espírito)) helper.fail("um espírito não conta");
        var zumbi = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(4, 2, 3));
        if (Fetishes.ignorable(level, zumbi)) helper.fail("mas um zumbi conta");
        espírito.discard();
        zumbi.discard();
        helper.succeed();
    }

    // ------------------------------------------------------------------ a Sentinela no mundo

    /** <b>A Sentinela manda dois espectros contra quem está sozinho.</b> */
    @GameTest(maxTicks = 40)
    public void theSentinelSendsTwoAtOne(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(3, 2, 3));
        FetishBlockEntity alma = põe(helper, onde);
        if (alma == null) {
            helper.fail("o espantalho devia ter alma");
            return;
        }
        alma.effectType(SpiritEffects.SENTINEL.id);

        var intruso = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(5, 2, 3));
        SpiritEffects.SENTINEL.run(alma, true, List.of(intruso));

        var apareceram = level.getEntitiesOfClass(SpectreEntity.class,
                new AABB(intruso.blockPosition()).inflate(4.0));
        if (apareceram.size() != 2) {
            helper.fail("contra um só, ela manda dois; mandou " + apareceram.size());
        }
        for (var quem : apareceram) {
            if (quem.getTarget() != intruso) helper.fail("e os dois vêm já marcados com ele");
            if (!quem.deEmpréstimo()) helper.fail("e os dois têm prazo");
            quem.discard();
        }

        intruso.discard();
        level.removeBlock(onde, false);
        helper.succeed();
    }

    // ------------------------------------------------------------------ o que ele larga

    /** <b>Ele se larga a si próprio com tudo dentro.</b> */
    @GameTest
    public void itDropsItselfWithEverythingInside(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(3, 2, 3));
        FetishBlockEntity alma = põe(helper, onde);
        if (alma == null) {
            helper.fail("o espantalho devia ter alma");
            return;
        }
        alma.effectType(SpiritEffects.SCREAMER.id);
        alma.color(14);
        alma.knownPlayers().add("Ninguém");

        var caiu = net.minecraft.world.level.block.Block.getDrops(
                level.getBlockState(onde), level, onde, alma);
        if (caiu.size() != 1) {
            helper.fail("devia largar uma coisa só; largou " + caiu.size());
            return;
        }
        ItemStack oquê = caiu.getFirst();
        if (!oquê.is(OccultaItems.SCARECROW)) helper.fail("e é um espantalho");
        if (SpiritEffects.idOf(oquê) != SpiritEffects.SCREAMER.id) helper.fail("com o Grito preso");
        var guardado = oquê.get(OccultaComponents.FETISH_DATA);
        if (guardado == null) {
            helper.fail("e com o que ele sabia dentro");
            return;
        }
        if (guardado.color() != 14) helper.fail("a tinta vai com ele");
        if (!guardado.players().contains("Ninguém")) helper.fail("e a lista também");

        level.removeBlock(onde, false);
        helper.succeed();
    }

    /**
     * <b>As três receitas de montagem são as do original.</b>
     *
     * <p>O Espantalho é lã e gravetos à volta de uma abóbora, com o Barbante Atormentado no meio; a
     * Escada é penas e linha à volta do Fio Fantasioso; e o Ídolo é carvalho à volta de um tronco do
     * ofício. Os três pedem uma coisa que só a Roca faz, e é isso que os prende ao ramo.
     */
    @GameTest
    public void theThreeAreCraftable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        confere(helper, level, "o Espantalho", OccultaItems.SCARECROW, java.util.Arrays.asList(
                new ItemStack(net.minecraft.world.item.Items.WOOL.white()),
                new ItemStack(net.minecraft.world.item.Items.CARVED_PUMPKIN),
                new ItemStack(net.minecraft.world.item.Items.WOOL.white()),
                new ItemStack(net.minecraft.world.item.Items.STICK),
                new ItemStack(OccultaItems.TORMENTED_TWINE),
                new ItemStack(net.minecraft.world.item.Items.STICK),
                new ItemStack(net.minecraft.world.item.Items.WOOL.white()),
                new ItemStack(net.minecraft.world.item.Items.STICK),
                new ItemStack(net.minecraft.world.item.Items.WOOL.white())));

        confere(helper, level, "a Escada de Bruxa", OccultaItems.WITCHS_LADDER,
                java.util.Arrays.asList(
                        new ItemStack(net.minecraft.world.item.Items.FEATHER),
                        new ItemStack(net.minecraft.world.item.Items.STRING),
                        new ItemStack(net.minecraft.world.item.Items.FEATHER),
                        new ItemStack(net.minecraft.world.item.Items.FEATHER),
                        new ItemStack(OccultaItems.FANCIFUL_THREAD),
                        new ItemStack(net.minecraft.world.item.Items.FEATHER),
                        new ItemStack(net.minecraft.world.item.Items.FEATHER),
                        new ItemStack(net.minecraft.world.item.Items.STRING),
                        new ItemStack(net.minecraft.world.item.Items.FEATHER)));

        confere(helper, level, "o Ídolo de Treant", OccultaItems.TREANT_IDOL,
                java.util.Arrays.asList(
                        new ItemStack(net.minecraft.world.item.Items.OAK_LOG),
                        new ItemStack(net.minecraft.world.item.Items.CARVED_PUMPKIN),
                        new ItemStack(net.minecraft.world.item.Items.OAK_LOG),
                        new ItemStack(OccultaItems.TORMENTED_TWINE),
                        new ItemStack(OccultaBlocks.ROWAN_LOG),
                        new ItemStack(OccultaItems.TORMENTED_TWINE),
                        new ItemStack(net.minecraft.world.item.Items.OAK_LOG),
                        ItemStack.EMPTY,
                        new ItemStack(net.minecraft.world.item.Items.OAK_LOG)));

        helper.succeed();
    }

    private static void confere(GameTestHelper helper, ServerLevel level, String nome,
                                net.minecraft.world.item.Item sai,
                                java.util.List<ItemStack> posto) {
        var mesa = net.minecraft.world.item.crafting.CraftingInput.of(3, 3, posto);
        var achou = level.getServer().getRecipeManager().getRecipeFor(
                net.minecraft.world.item.crafting.RecipeType.CRAFTING, mesa, level);
        if (achou.isEmpty()) {
            helper.fail(nome + " devia fechar receita");
            return;
        }
        ItemStack saiu = achou.get().value().assemble(mesa);
        if (!saiu.is(sai)) helper.fail("de " + nome + " devia sair ele próprio; saiu " + saiu);
    }

    /** E o balde apaga as listas. */
    @GameTest
    public void theBucketWipesTheLists(GameTestHelper helper) {
        piso(helper);
        BlockPos onde = helper.absolutePos(new BlockPos(3, 2, 3));
        FetishBlockEntity alma = põe(helper, onde);
        if (alma == null) {
            helper.fail("o espantalho devia ter alma");
            return;
        }
        alma.knownPlayers().add("Alguém");
        alma.knownTypes().add("Vaca");
        alma.clearKnown();
        if (!alma.knownPlayers().isEmpty() || !alma.knownTypes().isEmpty()
                || !alma.knownCreatures().isEmpty()) {
            helper.fail("o balde apaga as três");
        }
        helper.getLevel().removeBlock(onde, false);
        helper.succeed();
    }
}
