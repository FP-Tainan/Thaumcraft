package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.curse.Curse;
import net.thaumcraft.occulta.curse.Grotesque;
import net.thaumcraft.occulta.rite.ActiveRite;
import net.thaumcraft.occulta.rite.RiteRegistry;
import net.thaumcraft.occulta.rite.Rites;

import java.util.List;

/**
 * As maldições: o número que fica em quem as tem, o que elas fazem, e a aposta que é tirá-las.
 *
 * <p>A prova que importa mais aqui é a de <b>tirar</b>. No original, tentar tirar uma maldição com um rito
 * fraco demais quase sempre a <b>piora</b> — e é fácil portar isso de forma que tirar seja sempre tirar. A
 * {@code removingAWeakRiteUsuallyMakesItWorse} é onde essa decisão fica guardada.
 */
public class OccultaCurseGameTest {
    private static void piso(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** As cinco estão lá, e os nove ritos também. */
    @GameTest(maxTicks = 20)
    public void theFiveCursesAndTheirRitesAreThere(GameTestHelper helper) {
        if (Curse.values().length != 5) {
            helper.fail("são cinco maldições, são " + Curse.values().length);
        }
        for (String chave : List.of("tc.rite.cursecreature", "tc.rite.curseinsanity",
                "tc.rite.cursenightmare", "tc.rite.cursesinking", "tc.rite.removecurse",
                "tc.rite.removeinsanity", "tc.rite.removesinking", "tc.rite.curenightmare",
                "tc.rite.cureoverheating")) {
            if (RiteRegistry.all().stream().noneMatch(r -> r.key().equals(chave))) {
                helper.fail("falta o rito " + chave);
                return;
            }
        }
        helper.succeed();
    }

    /** Uma maldição é um número que fica, e que não sobe sozinho ao ser posto outra vez mais fraco. */
    @GameTest(maxTicks = 20)
    public void aCurseIsANumberThatStays(GameTestHelper helper) {
        piso(helper);
        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
        if (Curse.level(porco, Curse.CURSED) != 0) helper.fail("ninguém começa amaldiçoado");

        Curse.put(porco, Curse.CURSED, 3);
        if (Curse.level(porco, Curse.CURSED) != 3) helper.fail("posta no três, fica no três");

        // pôr mais fraco não abaixa: é o max do original
        Curse.put(porco, Curse.CURSED, 1);
        if (Curse.level(porco, Curse.CURSED) != 3) {
            helper.fail("uma mais fraca não abaixa a que já está, e deu "
                    + Curse.level(porco, Curse.CURSED));
        }
        if (!Curse.any(porco)) helper.fail("e ele tem alguma");

        Curse.remove(porco, Curse.CURSED);
        if (Curse.any(porco)) helper.fail("tirada, não tem nenhuma");

        porco.discard();
        helper.succeed();
    }

    /**
     * <b>Tirar com um rito fraco quase sempre piora.</b>
     *
     * <p>A prova corre o sorteio duzentas vezes, porque o que ela mede é a <b>tendência</b>: um rito de grau
     * um contra uma maldição de grau cinco piora na maioria das vezes, e é isso que faz os ritos de tirar
     * quererem coven e gato — não para pôr, para <b>conseguir tirar</b>.
     */
    @GameTest(maxTicks = 60)
    public void removingWithAWeakRiteUsuallyMakesItWorse(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(3, 2, 3));

        var fraco = new Rites.CurseCreature(false, Curse.CURSED, 1);
        var passo = fraco.steps(0).getFirst();

        int piorou = 0;
        int saiu = 0;
        for (int volta = 0; volta < 200; volta++) {
            var porco = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
            Curse.put(porco, Curse.CURSED, 5);
            // sem vínculo o rito desiste, por isso a prova chama o miolo dele pela mão
            var rito = new ActiveRite("tc.rite.removecurse", fraco, List.of(), null, 0);
            tiraNoBraço(level, porco, fraco);
            int agora = Curse.level(porco, Curse.CURSED);
            if (agora == 0) saiu++;
            else if (agora > 5) piorou++;
            porco.discard();
        }

        if (piorou + saiu != 200) helper.fail("ou sai ou sobe, e deu " + piorou + " + " + saiu);
        if (piorou <= saiu) {
            helper.fail("um rito fraco piora mais vezes do que tira: piorou " + piorou
                    + " e tirou " + saiu);
        }
        helper.succeed();
    }

    /** E com um rito forte, quase sempre sai. */
    @GameTest(maxTicks = 60)
    public void removingWithAStrongRiteUsuallyWorks(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var forte = new Rites.CurseCreature(false, Curse.CURSED, 5);

        int saiu = 0;
        for (int volta = 0; volta < 200; volta++) {
            var porco = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
            Curse.put(porco, Curse.CURSED, 1);
            tiraNoBraço(level, porco, forte);
            if (Curse.level(porco, Curse.CURSED) == 0) saiu++;
            porco.discard();
        }

        // um em vinte sobe; em duzentas voltas isso é por volta de dez
        if (saiu < 170) helper.fail("um rito forte tira quase sempre, e tirou " + saiu + " em 200");
        if (saiu == 200) helper.fail("mas não sempre: um em vinte ainda piora");
        helper.succeed();
    }

    /** O Afundar não pega em gente — e é um engano do original, não deste porte. */
    @GameTest(maxTicks = 40)
    public void sinkingDoesNotTouchPeople(GameTestHelper helper) {
        piso(helper);
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(3.5, 2, 3.5)));
        Curse.put(quem, Curse.SINKING, 4);

        var antes = new Vec3(0.0, -0.5, 0.0);
        quem.setDeltaMovement(antes);
        Curse.tick(helper.getLevel(), quem);
        if (quem.getDeltaMovement().y != antes.y) {
            helper.fail("o Afundar não mexe em gente: o original guarda-o atrás de um "
                    + "!(entity instanceof EntityPlayer), e o ramo de jogador lá dentro nunca corre");
        }
        helper.succeed();
    }

    /** O Grotesco dura um minuto e empurra o que chega perto. */
    @GameTest(maxTicks = 40)
    public void theGrotesqueBrewPushesWhatComesClose(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(3.5, 2, 3.5)));

        if (Grotesque.resta(quem) != 0) helper.fail("ninguém começa grotesco");
        Grotesque.bebeu(quem);
        if (Grotesque.resta(quem) != Grotesque.DURA) {
            helper.fail("bebido, dura " + Grotesque.DURA + " batidas, e ficou " + Grotesque.resta(quem));
        }

        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(4, 2, 4));
        porco.setDeltaMovement(Vec3.ZERO);
        Grotesque.tick(level, quem);
        if (porco.getDeltaMovement().horizontalDistanceSqr() <= 0.0) {
            helper.fail("o que chega a quatro blocos é empurrado");
        }
        if (Grotesque.resta(quem) != Grotesque.DURA - 1) helper.fail("e o minuto corre");

        porco.discard();
        helper.succeed();
    }

    /**
     * E uma visão não machuca, não se machuca, e se apaga sozinha.
     *
     * <p>A prova baixa a vida dela antes de esperar: do cheio, apagar-se leva por volta de cento e cinquenta
     * batidas — são dez descontos de um em quinze —, e o que se quer medir aqui é que ela <b>se apaga</b>, e
     * não quanto tempo isso leva.
     */
    @GameTest(maxTicks = 200)
    public void anIllusionHurtsNobodyAndFadesAway(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var visão = helper.spawn(net.thaumcraft.occulta.OccultaEntities.ILLUSION_CREEPER,
                new BlockPos(3, 2, 3));

        float tinha = visão.getHealth();
        if (visão.hurtServer(level, level.damageSources().generic(), 100.0f)) {
            helper.fail("bater nela é bater no ar");
        }
        if (visão.getHealth() != tinha) helper.fail("e ela não perde vida de pancada");

        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(4, 2, 3));
        float porcoTinha = porco.getHealth();
        visão.doHurtTarget(level, porco);
        if (porco.getHealth() != porcoTinha) helper.fail("e ela não machuca ninguém");

        porco.discard();
        visão.setHealth(1.2f);
        helper.succeedWhen(() -> helper.assertTrue(!visão.isAlive(), "e ela se apaga sozinha"));
    }

    /** Faz o sorteio de tirar sem precisar de vínculo, que é o que uma prova não tem. */
    private static void tiraNoBraço(ServerLevel level, net.minecraft.world.entity.LivingEntity alvo,
                                    Rites.CurseCreature rito) {
        rito.tiraParaProva(level, alvo, 0);
    }
}
