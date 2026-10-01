package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.arcana.ArcanaEffects;
import net.thaumcraft.arcana.Essences;
import net.thaumcraft.arcana.MarkData;
import net.thaumcraft.arcana.Shapes;
import net.thaumcraft.arcana.Spell;

/**
 * As que deslocam, as que mexem no mundo e as que mexem no céu.
 *
 * <p>Estas três famílias têm em comum não ferirem ninguém: o que elas mudam é <b>onde se está</b> e <b>o que
 * está em volta</b>. É a metade do Ars Magica 2 que não é combate, e é a que faz dele um mod de magia e não
 * um mod de armas.
 */
public class ArcanaWorldGameTest {
    // ------------------------------------------------------------------ as que deslocam

    /** O Piscar leva para a frente — e, contra uma parede, encosta à parede em vez de atravessar. */
    @GameTest(maxTicks = 100)
    public void blinkStopsAtTheWall(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);

        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(1, 2, 1));
        porco.setNoAi(true);
        Vec3 estava = porco.position();

        Essences.BLINK.onEntity(level, Spell.of(Shapes.TOUCH, Essences.BLINK), quem, porco);
        double andou = porco.position().distanceTo(estava);

        if (andou <= 0.5) helper.fail("o Piscar devia levar a algum lado, e andou " + andou);
        if (!porco.isAlive()) helper.fail("e não devia matar ninguém");

        porco.discard();
        helper.succeed();
    }

    /** A Distorção Astral prende: com ela, nada se desloca. */
    @GameTest(maxTicks = 100)
    public void distortionStopsTheBlink(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);

        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(1, 2, 1));
        porco.setNoAi(true);
        porco.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                ArcanaEffects.ASTRAL_DISTORTION, 200, 0));
        Vec3 estava = porco.position();

        boolean saiu = Essences.BLINK.onEntity(level, Spell.of(Shapes.TOUCH, Essences.BLINK), quem, porco);

        if (porco.position().distanceTo(estava) > 0.01) helper.fail("com a Distorção não se pisca");
        if (!saiu) helper.fail("mas o original cobra o feitiço na mesma");

        porco.discard();
        helper.succeed();
    }

    /** A Marca guarda o lugar, e o Chamado traz de volta a ele. */
    @GameTest(maxTicks = 100)
    public void markAndRecallAreAPair(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // o de mentira do servidor não se pode teleportar: o jogo procura a ligação dele e não há nenhuma
        var quem = helper.makeMockPlayer(GameType.CREATIVE);

        // sem marca posta, o Chamado recusa
        MarkData.set(quem, MarkData.NONE);
        if (Essences.RECALL.onEntity(level, Spell.of(Shapes.SELF, Essences.RECALL), quem, quem)) {
            helper.fail("sem marca, o Chamado não leva a lado nenhum");
        }

        Vec3 marcado = helper.absoluteVec(new Vec3(2.5, 2.0, 2.5));
        quem.snapTo(marcado, 0.0f, 0.0f);
        if (!Essences.MARK.onEntity(level, Spell.of(Shapes.SELF, Essences.MARK), quem, quem)) {
            helper.fail("a Marca devia pegar");
        }
        if (!MarkData.of(quem).posta()) helper.fail("e ficar posta");

        quem.snapTo(helper.absoluteVec(new Vec3(6.5, 2.0, 6.5)), 0.0f, 0.0f);
        if (!Essences.RECALL.onEntity(level, Spell.of(Shapes.SELF, Essences.RECALL), quem, quem)) {
            helper.fail("e o Chamado devia trazer de volta");
        }
        if (quem.position().distanceTo(marcado) > 0.5) {
            helper.fail("de volta ao lugar marcado, e ficou em " + quem.position());
        }

        MarkData.set(quem, MarkData.NONE);
        helper.succeed();
    }

    /** Trocar de Lugar troca mesmo os dois. */
    @GameTest(maxTicks = 100)
    public void transplaceSwapsThem(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var quem = helper.makeMockPlayer(GameType.CREATIVE);
        quem.snapTo(helper.absoluteVec(new Vec3(1.5, 2.0, 1.5)), 0.0f, 0.0f);

        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(5, 2, 5));
        porco.setNoAi(true);
        Vec3 meu = quem.position();
        Vec3 dele = porco.position();

        Essences.TRANSPLACE.onEntity(level, Spell.of(Shapes.TOUCH, Essences.TRANSPLACE), quem, porco);

        if (quem.position().distanceTo(dele) > 0.5) helper.fail("quem lança vai para onde o outro estava");
        if (porco.position().distanceTo(meu) > 0.5) helper.fail("e o outro para onde quem lançou estava");

        porco.discard();
        helper.succeed();
    }

    // ------------------------------------------------------------------ as que mexem no mundo

    /** Arar faz de terra e grama terra arada, e mais nada. */
    @GameTest(maxTicks = 100)
    public void plowOnlyWorksOnDirt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);
        Spell frase = Spell.of(Shapes.TOUCH, Essences.PLOW);

        helper.setBlock(new BlockPos(2, 1, 2), Blocks.DIRT);
        helper.setBlock(new BlockPos(3, 1, 2), Blocks.STONE);

        BlockPos terra = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockPos pedra = helper.absolutePos(new BlockPos(3, 1, 2));

        if (!Essences.PLOW.onBlock(level, frase, quem, terra, Direction.UP, Vec3.ZERO)) {
            helper.fail("a terra ara-se");
        }
        if (!level.getBlockState(terra).is(Blocks.FARMLAND)) helper.fail("e fica terra arada");
        if (Essences.PLOW.onBlock(level, frase, quem, pedra, Direction.UP, Vec3.ZERO)) {
            helper.fail("e a pedra não");
        }
        helper.succeed();
    }

    /** A Forja cozinha o bloco: areia vira vidro, e gelo vira água. */
    @GameTest(maxTicks = 100)
    public void forgeSmeltsTheBlock(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);
        Spell frase = Spell.of(Shapes.TOUCH, Essences.FORGE);

        helper.setBlock(new BlockPos(2, 1, 2), Blocks.SAND);
        helper.setBlock(new BlockPos(3, 1, 2), Blocks.ICE);
        BlockPos areia = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockPos gelo = helper.absolutePos(new BlockPos(3, 1, 2));

        Essences.FORGE.onBlock(level, frase, quem, areia, Direction.UP, Vec3.ZERO);
        Essences.FORGE.onBlock(level, frase, quem, gelo, Direction.UP, Vec3.ZERO);

        if (!level.getBlockState(areia).is(Blocks.GLASS)) helper.fail("a areia vira vidro");
        if (!level.getBlockState(gelo).is(Blocks.WATER)) helper.fail("e o gelo vira água");
        helper.succeed();
    }

    /** A Seca tira a água: a pedra racha, a terra vira areia, e a água some. */
    @GameTest(maxTicks = 100)
    public void droughtDriesEverything(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);
        Spell frase = Spell.of(Shapes.TOUCH, Essences.DROUGHT);

        helper.setBlock(new BlockPos(2, 1, 2), Blocks.DIRT);
        helper.setBlock(new BlockPos(3, 1, 2), Blocks.STONE);
        BlockPos terra = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockPos pedra = helper.absolutePos(new BlockPos(3, 1, 2));

        Essences.DROUGHT.onBlock(level, frase, quem, terra, Direction.UP, Vec3.ZERO);
        Essences.DROUGHT.onBlock(level, frase, quem, pedra, Direction.UP, Vec3.ZERO);

        if (!level.getBlockState(terra).is(Blocks.SAND)) helper.fail("a terra vira areia");
        if (!level.getBlockState(pedra).is(Blocks.COBBLESTONE)) helper.fail("e a pedra vira pedregulho");
        helper.succeed();
    }

    /** Criar Água põe água na face em que bateu — e enche o caldeirão, se for num caldeirão. */
    @GameTest(maxTicks = 100)
    public void createWaterFillsTheCauldron(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);
        Spell frase = Spell.of(Shapes.TOUCH, Essences.CREATE_WATER);

        helper.setBlock(new BlockPos(2, 1, 2), Blocks.STONE);
        helper.setBlock(new BlockPos(4, 1, 2), Blocks.CAULDRON);
        BlockPos chão = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockPos caldeirão = helper.absolutePos(new BlockPos(4, 1, 2));

        Essences.CREATE_WATER.onBlock(level, frase, quem, chão, Direction.UP, Vec3.ZERO);
        Essences.CREATE_WATER.onBlock(level, frase, quem, caldeirão, Direction.UP, Vec3.ZERO);

        if (!level.getBlockState(chão.above()).is(Blocks.WATER)) helper.fail("põe água por cima do chão");
        if (!level.getBlockState(caldeirão).is(Blocks.WATER_CAULDRON)) helper.fail("e enche o caldeirão");
        helper.succeed();
    }

    /** Plantar usa a primeira semente da mochila, e gasta uma. */
    @GameTest(maxTicks = 100)
    public void plantUsesASeedFromTheBag(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);
        quem.getInventory().clearContent();
        quem.getInventory().add(new ItemStack(Items.WHEAT_SEEDS, 3));

        helper.setBlock(new BlockPos(2, 1, 2), Blocks.FARMLAND);
        // o trigo não nasce no escuro, e a arena da prova é subterrânea
        helper.setBlock(new BlockPos(3, 2, 2), Blocks.GLOWSTONE);
        BlockPos arado = helper.absolutePos(new BlockPos(2, 1, 2));

        if (!Essences.PLANT.onBlock(level, Spell.of(Shapes.TOUCH, Essences.PLANT), quem, arado,
                Direction.UP, Vec3.ZERO)) {
            helper.fail("devia plantar");
            return;
        }
        if (!level.getBlockState(arado.above()).is(Blocks.WHEAT)) helper.fail("e nasce trigo");
        if (quem.getInventory().countItem(Items.WHEAT_SEEDS) != 2) helper.fail("e gasta uma semente");

        quem.getInventory().clearContent();
        helper.succeed();
    }

    /** O Outono do Mago derruba as folhas em roda. */
    @GameTest(maxTicks = 100)
    public void autumnDropsTheLeaves(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);

        for (int x = 1; x <= 3; x++) {
            for (int z = 1; z <= 3; z++) {
                helper.setBlock(new BlockPos(x, 2, z), Blocks.OAK_LEAVES);
            }
        }
        BlockPos meio = helper.absolutePos(new BlockPos(2, 2, 2));
        Essences.WIZARDS_AUTUMN.onBlock(level, Spell.of(Shapes.TOUCH, Essences.WIZARDS_AUTUMN), quem,
                meio, Direction.UP, Vec3.ZERO);

        for (int x = 1; x <= 3; x++) {
            for (int z = 1; z <= 3; z++) {
                if (level.getBlockState(helper.absolutePos(new BlockPos(x, 2, z)))
                        .is(Blocks.OAK_LEAVES)) {
                    helper.fail("as folhas em roda caem todas");
                    return;
                }
            }
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ as que mexem no céu

    /** Afastar a Chuva só faz alguma coisa se estiver chovendo. */
    @GameTest(maxTicks = 100)
    public void banishingRainNeedsRain(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);
        var tempo = level.getWeatherData();

        // a força da chuva sobe devagar a cada batida; a prova põe as duas coisas de uma vez
        tempo.setRaining(false);
        level.setRainLevel(0.0f);
        if (Essences.BANISH_RAIN.onEntity(level, Spell.of(Shapes.SELF, Essences.BANISH_RAIN), quem, quem)) {
            helper.fail("com tempo bom, não há chuva para afastar");
        }

        tempo.setRaining(true);
        tempo.setRainTime(6000);
        level.setRainLevel(1.0f);
        if (!Essences.BANISH_RAIN.onEntity(level, Spell.of(Shapes.SELF, Essences.BANISH_RAIN), quem, quem)) {
            helper.fail("e com chuva, se afasta ela");
        }
        if (tempo.isRaining()) helper.fail("e deixa de chover");

        helper.succeed();
    }

    /** A Tempestade, com tempo bom, chama a chuva. */
    @GameTest(maxTicks = 100)
    public void theStormCallsTheRain(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);
        var tempo = level.getWeatherData();
        tempo.setRaining(false);
        level.setRainLevel(0.0f);

        if (!Essences.STORM.onEntity(level, Spell.of(Shapes.SELF, Essences.STORM), quem, quem)) {
            helper.fail("a Tempestade devia pegar");
        }
        if (!tempo.isRaining()) helper.fail("e começar a chover");

        tempo.setRaining(false);
        level.setRainLevel(0.0f);
        helper.succeed();
    }

    /** As duas do relógio custam vinte e cinco mil, que é o preço mais alto do ramo. */
    @GameTest
    public void movingTheSkyIsTheMostExpensiveThing(GameTestHelper helper) {
        float maisCaro = 0.0f;
        String qual = "";
        for (var peça : net.thaumcraft.arcana.SpellParts.essences()) {
            if (peça.manaCost() > maisCaro) {
                maisCaro = peça.manaCost();
                qual = peça.name();
            }
        }
        if (Essences.DAYLIGHT.manaCost() != 25000.0f) helper.fail("a Luz do Dia custa vinte e cinco mil");
        if (Essences.MOONRISE.manaCost() != 25000.0f) helper.fail("e o Anoitecer também");
        if (maisCaro != 25000.0f) helper.fail("e nada custa mais do que elas, e custa: " + qual);
        helper.succeed();
    }
}
