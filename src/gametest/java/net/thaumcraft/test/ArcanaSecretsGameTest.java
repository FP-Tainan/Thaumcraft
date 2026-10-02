package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameType;
import net.thaumcraft.arcana.Essences;
import net.thaumcraft.arcana.Mana;
import net.thaumcraft.arcana.Modifiers;
import net.thaumcraft.arcana.Shapes;
import net.thaumcraft.arcana.SkillData;
import net.thaumcraft.arcana.SkillTree;
import net.thaumcraft.arcana.Spell;
import net.thaumcraft.arcana.SpellCast;
import net.thaumcraft.arcana.SpellUnlocks;

import java.util.ArrayList;
import java.util.List;

/**
 * Os segredos: as perícias que não se compram e se descobrem.
 *
 * <p>É a coisa mais fácil de partir sem ninguém dar por isso. Um segredo que não abre nunca é um segredo que
 * não existe — e ninguém repararia, porque ninguém sabe que ele lá está.
 */
public class ArcanaSecretsGameTest {
    /** Lançar a combinação certa abre o segredo, dá o ponto prateado e ensina a perícia. */
    @GameTest(maxTicks = 100)
    public void castingTheRightSentenceOpensTheSecret(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var quem = helper.makeMockPlayer(GameType.CREATIVE);
        SkillData.set(quem, SkillData.NONE);
        Mana.set(quem, new Mana(50, 5000.0f, 0.0f));

        // o Desmembramento sai de um feitiço com Perfuração e Dano na mesma etapa
        Spell frase = new Spell(List.of(new Spell.Stage(Shapes.SELF,
                List.of(Essences.PHYSICAL_DAMAGE),
                List.of(Modifiers.PIERCING, Modifiers.DAMAGE))));

        if (SkillData.of(quem).knows(Modifiers.DISMEMBERING)) helper.fail("ninguém começa sabendo");
        SpellUnlocks.descobre(frase, quem);

        if (!SkillData.of(quem).knows(Modifiers.DISMEMBERING)) {
            helper.fail("e a combinação certa abre o Desmembramento");
        }
        if (SkillData.of(quem).silver() != 1) helper.fail("e dá um ponto prateado");
        if (SkillData.of(quem).used(SkillTree.Point.SILVER) != 1) {
            helper.fail("e o gasta na perícia que abriu");
        }
        if (SkillData.of(quem).free(SkillTree.Point.SILVER, 50) != 0) {
            helper.fail("e não sobra prateado nenhum");
        }

        SkillData.set(quem, SkillData.NONE);
        Mana.set(quem, Mana.NONE);
        helper.succeed();
    }

    /** E as peças têm de estar na <b>mesma</b> etapa: espalhadas por duas, não abrem nada. */
    @GameTest(maxTicks = 100)
    public void thePartsMustBeInTheSameStage(GameTestHelper helper) {
        var quem = helper.makeMockPlayer(GameType.CREATIVE);
        SkillData.set(quem, SkillData.NONE);

        Spell espalhado = new Spell(List.of(
                new Spell.Stage(Shapes.TOUCH, List.of(Essences.PHYSICAL_DAMAGE),
                        List.of(Modifiers.PIERCING)),
                new Spell.Stage(Shapes.SELF, List.of(Essences.HEAL), List.of(Modifiers.DAMAGE))));

        SpellUnlocks.descobre(espalhado, quem);
        if (SkillData.of(quem).knows(Modifiers.DISMEMBERING)) {
            helper.fail("espalhadas por duas etapas, as peças não abrem nada");
        }

        SkillData.set(quem, SkillData.NONE);
        helper.succeed();
    }

    /** Uma essência de segredo não sai da mão de quem não a descobriu. */
    @GameTest(maxTicks = 100)
    public void anUndiscoveredEssenceWillNotCast(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var quem = helper.makeMockPlayer(GameType.CREATIVE);
        SkillData.set(quem, SkillData.NONE);

        Spell nevasca = Spell.of(Shapes.SELF, Essences.BLIZZARD);
        if (!SpellUnlocks.tranca(nevasca, quem)) helper.fail("a Nevasca está trancada a quem não a descobriu");

        var saiu = SpellCast.cast(level, nevasca, quem, quem, quem.position());
        if (saiu != SpellCast.Result.UNDISCOVERED) helper.fail("e o feitiço nem começa, e deu " + saiu);

        // e depois de descoberta, sai
        SkillData.set(quem, SkillData.NONE.withSilver(1).learn(SkillTree.of(Essences.BLIZZARD), 50));
        if (SpellUnlocks.tranca(nevasca, quem)) helper.fail("e quem a descobriu a lança");

        SkillData.set(quem, SkillData.NONE);
        helper.succeed();
    }

    /** Um modificador de segredo não tranca nada: é o original que faz essa distinção. */
    @GameTest(maxTicks = 100)
    public void asecretModifierDoesNotLockTheSpell(GameTestHelper helper) {
        var quem = helper.makeMockPlayer(GameType.CREATIVE);
        SkillData.set(quem, SkillData.NONE);

        Spell comProsperidade = new Spell(List.of(new Spell.Stage(Shapes.TOUCH,
                List.of(Essences.DIG), List.of(Modifiers.PROSPERITY))));
        if (SpellUnlocks.tranca(comProsperidade, quem)) {
            helper.fail("um modificador de segredo não tranca o feitiço");
        }

        SkillData.set(quem, SkillData.NONE);
        helper.succeed();
    }

    /** Todos os segredos abrem peças que existem, e pedem peças que existem. */
    @GameTest
    public void everySecretIsReachable(GameTestHelper helper) {
        var nomes = new ArrayList<String>();
        for (var segredo : SpellUnlocks.all()) {
            if (SkillTree.of(segredo.abre()) == null) {
                helper.fail(segredo.abre().name() + " não está no quadro");
            }
            if (SkillTree.of(segredo.abre()).point() != SkillTree.Point.SILVER) {
                helper.fail(segredo.abre().name() + " devia ser prateada");
            }
            if (segredo.precisa().isEmpty()) helper.fail(segredo.abre().name() + " abre com nada");
            if (nomes.contains(segredo.abre().name())) {
                helper.fail(segredo.abre().name() + " está duas vezes");
            }
            nomes.add(segredo.abre().name());
        }
        helper.succeed();
    }

    /** E toda perícia prateada do quadro tem um segredo que a abre — senão ela seria inalcançável. */
    @GameTest
    public void everySilverSkillHasASecret(GameTestHelper helper) {
        for (var perícia : SkillTree.entries()) {
            if (perícia.point() != SkillTree.Point.SILVER) continue;
            boolean tem = false;
            for (var segredo : SpellUnlocks.all()) {
                if (segredo.abre() == perícia.part()) tem = true;
            }
            if (!tem) {
                helper.fail(perícia.part().name() + " é prateada e não há como a descobrir");
            }
        }
        helper.succeed();
    }
}
