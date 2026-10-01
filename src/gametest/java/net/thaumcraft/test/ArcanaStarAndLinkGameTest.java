package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.GameType;
import net.thaumcraft.arcana.ArcanaEntities;
import net.thaumcraft.arcana.Essences;
import net.thaumcraft.arcana.Mana;
import net.thaumcraft.arcana.ManaLinks;
import net.thaumcraft.arcana.Modifiers;
import net.thaumcraft.arcana.Shapes;
import net.thaumcraft.arcana.ShootingStarEntity;
import net.thaumcraft.arcana.Spell;
import net.thaumcraft.arcana.SpellCast;

import java.util.List;

/**
 * As duas últimas peças do ramo: a <b>Estrela Cadente</b> e o <b>Elo de Mana</b>.
 *
 * <p>São as duas que faltavam dos dez segredos, e as duas que pedem coisa que nenhuma outra pedia — uma
 * entidade própria, e um cano novo para a mana.
 */
public class ArcanaStarAndLinkGameTest {
    /**
     * As estrelas chamadas <b>por esta prova</b>, e não as das vizinhas.
     *
     * <p>Uma estrela nasce no teto do mundo, e por isso a caixa onde se procura tem de ser alta — mas não
     * larga: as arenas das provas ficam lado a lado, e uma caixa larga pega as estrelas de outra prova. Foi o
     * que aconteceu, e o dano que saiu era o do feitiço da prova do lado.
     */
    private static List<ShootingStarEntity> minhas(ServerLevel level,
                                                   net.minecraft.world.entity.Entity quem) {
        var coluna = new net.minecraft.world.phys.AABB(
                quem.getX() - 4.0, level.getMinY(), quem.getZ() - 4.0,
                quem.getX() + 4.0, level.getMaxY() + 1, quem.getZ() + 4.0);
        return level.getEntities(ArcanaEntities.SHOOTING_STAR, coluna, e -> true);
    }

    /** A Estrela nasce no teto do mundo, por cima do lugar marcado. */
    @GameTest(maxTicks = 100)
    public void theStarComesFromTheTopOfTheWorld(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);
        // ele nasce na origem do mundo, e não na arena: sem isto as duas provas chamam estrelas no mesmo lugar
        quem.snapTo(helper.absoluteVec(new net.minecraft.world.phys.Vec3(2.5, 2.0, 2.5)), 0.0f, 0.0f);

        if (!Essences.FALLING_STAR.onEntity(level,
                Spell.of(Shapes.SELF, Essences.FALLING_STAR), quem, quem)) {
            helper.fail("a Estrela devia ser chamada");
            return;
        }

        var achadas = minhas(level, quem);
        if (achadas.isEmpty()) {
            helper.fail("e devia estar lá");
            return;
        }
        var estrela = achadas.getFirst();
        if (estrela.getY() < quem.getY() + 10.0) helper.fail("e vir de cima");
        if (estrela.damage() != 30.0f) helper.fail("e ferir trinta, e fere " + estrela.damage());

        // E a guarda do original contra duas estrelas NÃO pega aqui, e é de propósito: ele procura uma já
        // chamada a dez blocos de cinquenta acima do ponto, e a estrela nasce no teto do mundo. Ao nível do
        // chão esses dois lugares estão a centenas de blocos um do outro, e a segunda estrela sai.
        if (!Essences.FALLING_STAR.onEntity(level,
                Spell.of(Shapes.SELF, Essences.FALLING_STAR), quem, quem)) {
            helper.fail("ao nível do chão a guarda do original não pega, e a segunda sai");
        }

        for (var e : achadas) e.discard();
        helper.succeed();
    }

    /** O Dano multiplica o quinze antes de ele ser dobrado. */
    @GameTest(maxTicks = 100)
    public void damageMultipliesBeforeDoubling(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);

        quem.snapTo(helper.absoluteVec(new net.minecraft.world.phys.Vec3(2.5, 2.0, 2.5)), 0.0f, 0.0f);

        Spell forte = new Spell(List.of(new Spell.Stage(Shapes.SELF,
                List.of(Essences.FALLING_STAR), List.of(Modifiers.DAMAGE))));
        Essences.FALLING_STAR.onEntity(level, forte, quem, quem);

        var achadas = minhas(level, quem);
        if (achadas.isEmpty()) {
            helper.fail("devia haver uma");
            return;
        }
        // aqui o Dano MULTIPLICA, e não soma: é o getModifiedInt_Mul do original, e é a única essência
        // do ramo onde ele faz isso. 15 × 2,2 = 33, cortado para inteiro, e dobrado: 66
        if (achadas.getFirst().damage() != 66.0f) {
            helper.fail("com um Dano, fere 66, e fere " + achadas.getFirst().damage());
        }

        for (var e : achadas) e.discard();
        helper.succeed();
    }

    /** A Estrela cai, e cai cada vez mais depressa — até ao teto de dois por batida. */
    @GameTest(maxTicks = 100)
    public void theStarFallsFasterAndFaster(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);

        var estrela = new ShootingStarEntity(level, quem, 10.0f);
        estrela.snapTo(helper.absoluteVec(new net.minecraft.world.phys.Vec3(3.5, 40.0, 3.5)));
        level.addFreshEntity(estrela);

        double antes = estrela.getY();
        estrela.tick();
        double depois = estrela.getY();
        if (depois >= antes) helper.fail("a Estrela desce");

        for (int i = 0; i < 40; i++) estrela.tick();
        if (estrela.isAlive() && estrela.getDeltaMovement().y < ShootingStarEntity.MAIS_DEPRESSA - 0.001) {
            helper.fail("e não passa de dois por batida");
        }

        estrela.discard();
        helper.succeed();
    }

    /** O Elo alterna: lançado duas vezes no mesmo, se desfaz. */
    @GameTest(maxTicks = 100)
    public void theLinkToggles(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var lançou = helper.makeMockPlayer(GameType.CREATIVE);
        ServerPlayer levou = helper.makeMockServerPlayerInLevel();
        ManaLinks.set(levou, ManaLinks.NONE);

        Spell frase = Spell.of(Shapes.TOUCH, Essences.MANA_LINK);
        Essences.MANA_LINK.onEntity(level, frase, lançou, levou);
        if (!ManaLinks.of(levou).tem(lançou.getUUID())) {
            helper.fail("quem leva o feitiço é que ganha o elo");
        }

        Essences.MANA_LINK.onEntity(level, frase, lançou, levou);
        if (ManaLinks.of(levou).tem(lançou.getUUID())) helper.fail("e lançado outra vez, se desfaz");

        ManaLinks.set(levou, ManaLinks.NONE);
        helper.succeed();
    }

    /**
     * E a mana emprestada paga o feitiço quando a própria acaba.
     *
     * <p>Primeiro a sua, e só o que faltar é que sai da do outro: é a ordem do original, e é ela que faz do
     * Elo uma rede de emergência em vez de uma torneira.
     */
    @GameTest(maxTicks = 100)
    public void borrowedManaPaysWhatIsMissing(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer pobre = helper.makeMockServerPlayerInLevel();
        ServerPlayer rico = helper.makeMockServerPlayerInLevel();
        // em criativo nada se paga, e um jogador de mentira nasce em criativo
        pobre.setGameMode(GameType.SURVIVAL);
        rico.setGameMode(GameType.SURVIVAL);
        rico.snapTo(pobre.position().add(1.0, 0.0, 0.0), 0.0f, 0.0f);

        Mana.set(pobre, new Mana(50, 100.0f, 0.0f));
        Mana.set(rico, new Mana(50, 5000.0f, 0.0f));
        ManaLinks.set(pobre, ManaLinks.NONE.alterna(rico.getUUID()));

        if (!SpellCast.affords(pobre, 1000.0f, 0.0f)) {
            helper.fail("com o elo, o que ele sozinho não pagaria passa a pagar-se");
        }
        SpellCast.charge(pobre, 1000.0f, 0.0f);

        if (Mana.of(pobre).mana() != 0.0f) helper.fail("a sua acaba primeiro");
        if (Math.abs(Mana.of(rico).mana() - 4100.0f) > 0.01f) {
            helper.fail("e o resto sai da do outro: esperava 4100 e ficou " + Mana.of(rico).mana());
        }

        ManaLinks.set(pobre, ManaLinks.NONE);
        Mana.set(pobre, Mana.NONE);
        Mana.set(rico, Mana.NONE);
        helper.succeed();
    }

    /** E um elo longe demais não serve: o alcance do original é curto. */
    @GameTest(maxTicks = 100)
    public void alinkTooFarAwayIsNoHelp(GameTestHelper helper) {
        ServerPlayer pobre = helper.makeMockServerPlayerInLevel();
        ServerPlayer longe = helper.makeMockServerPlayerInLevel();
        pobre.setGameMode(GameType.SURVIVAL);
        longe.setGameMode(GameType.SURVIVAL);
        longe.snapTo(pobre.position().add(20.0, 0.0, 0.0), 0.0f, 0.0f);

        Mana.set(pobre, new Mana(50, 100.0f, 0.0f));
        Mana.set(longe, new Mana(50, 5000.0f, 0.0f));
        ManaLinks.set(pobre, ManaLinks.NONE.alterna(longe.getUUID()));

        if (SpellCast.affords(pobre, 1000.0f, 0.0f)) {
            helper.fail("a vinte blocos, o elo não chega");
        }

        ManaLinks.set(pobre, ManaLinks.NONE);
        Mana.set(pobre, Mana.NONE);
        Mana.set(longe, Mana.NONE);
        helper.succeed();
    }
}
