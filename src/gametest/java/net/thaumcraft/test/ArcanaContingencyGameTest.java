package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.thaumcraft.arcana.Contingencies;
import net.thaumcraft.arcana.Contingency;
import net.thaumcraft.arcana.Essences;
import net.thaumcraft.arcana.Mana;
import net.thaumcraft.arcana.Shapes;
import net.thaumcraft.arcana.Spell;
import net.thaumcraft.arcana.SpellCast;

import java.util.List;

/**
 * As cinco Contingências: os feitiços que ficam guardados esperando alguma coisa acontecer.
 */
public class ArcanaContingencyGameTest {
    /** As cinco existem, são principum e custam as dez vezes do original. */
    @GameTest
    public void theFiveAreRegistered(GameTestHelper helper) {
        var cinco = List.of(Shapes.CONTINGENCY_FALL, Shapes.CONTINGENCY_DAMAGE, Shapes.CONTINGENCY_FIRE,
                Shapes.CONTINGENCY_HEALTH, Shapes.CONTINGENCY_DEATH);
        for (var forma : cinco) {
            if (!forma.principum()) helper.fail(forma.name() + " é principum");
            if (Math.abs(forma.manaMultiplier() - Contingency.MANA_MULTIPLIER) > 0.001f) {
                helper.fail(forma.name() + " custa dez vezes, e custa " + forma.manaMultiplier());
            }
        }
        // e há uma espera para cada uma, fora a nenhuma
        if (Contingency.Kind.values().length != cinco.size() + 1) {
            helper.fail("há cinco esperas mais a nenhuma");
        }
        helper.succeed();
    }

    /** Lançar uma Contingência guarda a frase e não faz mais nada. */
    @GameTest(maxTicks = 60)
    public void castingOneArmsIt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);
        Contingency.clear(quem);

        Spell guarda = new Spell(List.of(
                new Spell.Stage(Shapes.CONTINGENCY_DEATH, List.of(), List.of()),
                new Spell.Stage(Shapes.SELF, List.of(Essences.HEAL), List.of())));

        var saiu = SpellCast.cast(level, guarda, quem, null, quem.position());
        if (!saiu.ok()) {
            helper.fail("guardar uma Contingência devia dar certo, e deu " + saiu);
            return;
        }

        Contingency tem = Contingency.of(quem);
        if (!tem.armed()) helper.fail("e devia ficar guardada");
        if (tem.kind() != Contingency.Kind.DEATH) helper.fail("e ser a de Morte");
        if (tem.spell().stages().size() != 1) helper.fail("e guardar só o que sobrou da frase");
        if (tem.spell().first().shape() != Shapes.SELF) helper.fail("que é a Autoconjuração");

        Contingency.clear(quem);
        helper.succeed();
    }

    /** Sozinha, sem nada depois, ela é malformada como as outras principum. */
    @GameTest(maxTicks = 60)
    public void aloneItIsMalformed(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);

        var saiu = SpellCast.cast(level, Spell.of(Shapes.CONTINGENCY_FIRE, Essences.HEAL), quem, null,
                quem.position());
        if (saiu != SpellCast.Result.MALFORMED) {
            helper.fail("uma Contingência sozinha é malformada, e deu " + saiu);
        }
        helper.succeed();
    }

    /** Uma de cada vez: guardar outra apaga a primeira. */
    @GameTest(maxTicks = 60)
    public void onlyOneAtATime(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);

        Contingency.arm(quem, Contingency.Kind.DEATH, Spell.of(Shapes.SELF, Essences.HEAL));
        if (Contingency.of(quem).kind() != Contingency.Kind.DEATH) helper.fail("a primeira fica");

        Contingency.arm(quem, Contingency.Kind.ON_FIRE, Spell.of(Shapes.SELF, Essences.HEAL));
        if (Contingency.of(quem).kind() != Contingency.Kind.ON_FIRE) {
            helper.fail("e a segunda toma o lugar dela");
        }

        Contingency.clear(quem);
        if (Contingency.of(quem).armed()) helper.fail("e limpar apaga");
        helper.succeed();
    }

    /**
     * Ela <b>se gasta</b> ao disparar.
     *
     * <p>É o que impede uma Contingência de Dano de entrar num laço sem fim quando o feitiço dela fere quem a
     * levava: quando o feitiço corre, ela já não está mais lá.
     */
    @GameTest(maxTicks = 60)
    public void itSpendsItselfWhenItFires(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        Mana.set(quem, Mana.NONE.withLevel(60));
        Mana.set(quem, Mana.of(quem).withMana(Mana.of(quem).maxMana()));

        quem.setHealth(quem.getMaxHealth() - 6.0f);
        float ferido = quem.getHealth();
        Contingency.arm(quem, Contingency.Kind.DEATH, Spell.of(Shapes.SELF, Essences.HEAL));

        if (!Contingency.proc(level, quem, Contingency.Kind.DEATH)) helper.fail("ela devia disparar");
        if (quem.getHealth() <= ferido) helper.fail("e curar quem a levava");
        if (Contingency.of(quem).armed()) helper.fail("e sumir depois de disparar");

        // e disparar de novo não faz nada
        if (Contingency.proc(level, quem, Contingency.Kind.DEATH)) helper.fail("e não dispara duas vezes");

        limpa(quem);
        helper.succeed();
    }

    /** E ela só dispara na espera certa. */
    @GameTest(maxTicks = 60)
    public void itOnlyFiresOnItsOwnTrigger(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);

        Contingency.arm(quem, Contingency.Kind.ON_FIRE, Spell.of(Shapes.SELF, Essences.HEAL));
        if (Contingency.proc(level, quem, Contingency.Kind.DEATH)) {
            helper.fail("uma de Fogo não dispara na morte");
        }
        if (!Contingency.of(quem).armed()) helper.fail("e continua guardada");

        limpa(quem);
        helper.succeed();
    }

    /** Se houver alvo, a Contingência fica <b>nele</b>: dá para pôr um paraquedas em outra pessoa. */
    @GameTest(maxTicks = 60)
    public void itCanBeArmedOnSomeoneElse(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);
        var porco = helper.spawn(net.minecraft.world.entity.EntityTypes.PIG, new BlockPos(2, 2, 2));
        Contingency.clear(quem);
        Contingency.clear(porco);

        Spell guarda = new Spell(List.of(
                new Spell.Stage(Shapes.CONTINGENCY_FALL, List.of(), List.of()),
                new Spell.Stage(Shapes.SELF, List.of(Essences.HEAL), List.of())));
        SpellCast.cast(level, guarda, quem, porco, porco.position());

        if (!Contingency.of(porco).armed()) helper.fail("ela devia ficar no porco");
        if (Contingency.of(quem).armed()) helper.fail("e não em quem a lançou");

        Contingency.clear(porco);
        helper.succeed();
    }

    /** O chão se acha debaixo dos pés, que é o que a de Queda precisa saber. */
    @GameTest(maxTicks = 60)
    public void theGroundIsFound(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();

        // em cima de um bloco: o chão está a um passo
        helper.setBlock(new BlockPos(2, 2, 2), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        var acima = net.minecraft.world.phys.Vec3.atBottomCenterOf(
                helper.absolutePos(new BlockPos(2, 5, 2)));
        quem.snapTo(acima.x, acima.y, acima.z, 0.0f, 0.0f);

        double até = Contingencies.distânciaAtéOChão(level, quem);
        if (até <= 0 || até > 4) helper.fail("o chão devia estar a uns três blocos, e está a " + até);

        helper.setBlock(new BlockPos(2, 2, 2), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        limpa(quem);
        helper.succeed();
    }

    private static void limpa(ServerPlayer quem) {
        Contingency.clear(quem);
        Mana.set(quem, Mana.NONE);
    }
}
