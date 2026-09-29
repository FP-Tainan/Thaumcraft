package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.thaumcraft.arcana.ArcanaComponents;
import net.thaumcraft.arcana.ArcanaItems;
import net.thaumcraft.arcana.BoundToolItem;
import net.thaumcraft.arcana.Essences;
import net.thaumcraft.arcana.Mana;
import net.thaumcraft.arcana.Shapes;
import net.thaumcraft.arcana.SkillTree;
import net.thaumcraft.arcana.Spell;
import net.thaumcraft.arcana.SpellCast;
import net.thaumcraft.arcana.SpellItem;
import net.thaumcraft.arcana.SpellPart;

import java.util.List;

/**
 * O Vínculo: a última das quinze Formas, e a única que não lança nada.
 *
 * <p>Ela troca o feitiço na mão por uma ferramenta — e essa ferramenta custa mana a cada batida, e volta a ser
 * o feitiço quando a mana acaba.
 */
public class ArcanaBindingGameTest {
    /** As cinco existem, uma por ferramenta, e cada uma tem a sua. */
    @GameTest
    public void thereIsOneBindingPerTool(GameTestHelper helper) {
        var cinco = List.of(Shapes.BINDING_PICKAXE, Shapes.BINDING_AXE, Shapes.BINDING_SWORD,
                Shapes.BINDING_SHOVEL, Shapes.BINDING_HOE);
        if (cinco.size() != BoundToolItem.Kind.values().length) {
            helper.fail("há " + BoundToolItem.Kind.values().length + " ferramentas e " + cinco.size()
                    + " Formas");
        }
        for (BoundToolItem.Kind qual : BoundToolItem.Kind.values()) {
            if (ArcanaItems.BOUND.get(qual) == null) {
                helper.fail("a ferramenta " + qual.name() + " devia existir");
            }
        }
        // e nenhuma delas é principum nem terminus: o Vínculo vale sozinho
        for (SpellPart.Shape forma : cinco) {
            if (forma.principum()) helper.fail(forma.name() + " vale sozinha");
            if (forma.terminus()) helper.fail(forma.name() + " não é terminus");
        }
        helper.succeed();
    }

    /**
     * O preço de manter depende do metal, e são os três números do original.
     *
     * <p>Um décimo para a pedra, quatro décimos para o ferro e um inteiro para o diamante. É a única escolha
     * que a ferramenta dá: quanto ela vale contra quanto ela custa.
     */
    @GameTest
    public void theCostDependsOnTheMetal(GameTestHelper helper) {
        if (BoundToolItem.Kind.HOE.maintain != 0.1f) helper.fail("a enxada é de pedra: um décimo");
        if (BoundToolItem.Kind.SHOVEL.maintain != 0.4f) helper.fail("a pá é de ferro: quatro décimos");
        if (BoundToolItem.Kind.PICKAXE.maintain != 1.0f) helper.fail("a picareta é de diamante: um inteiro");
        if (BoundToolItem.Kind.AXE.maintain != 1.0f) helper.fail("o machado também");
        if (BoundToolItem.Kind.SWORD.maintain != 1.0f) helper.fail("e a espada também");
        helper.succeed();
    }

    /** Lançar o Vínculo troca o feitiço na mão pela ferramenta, e o feitiço vai dentro dela. */
    @GameTest(maxTicks = 60)
    public void bindingTurnsTheSpellIntoATool(GameTestHelper helper) {
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);

        Spell vínculo = Spell.of(Shapes.BINDING_PICKAXE, Essences.DIG);
        quem.setItemInHand(InteractionHand.MAIN_HAND,
                SpellItem.write(new ItemStack(ArcanaItems.SPELL), vínculo));

        var saiu = SpellCast.cast(helper.getLevel(), vínculo, quem, null, quem.position());
        if (!saiu.ok()) {
            helper.fail("o Vínculo devia pegar, e deu " + saiu);
            return;
        }

        ItemStack naMão = quem.getMainHandItem();
        if (!(naMão.getItem() instanceof BoundToolItem ferramenta)) {
            helper.fail("a mão devia ter uma ferramenta vinculada");
            return;
        }
        if (ferramenta.kind() != BoundToolItem.Kind.PICKAXE) helper.fail("e ser uma picareta");

        // e o feitiço vai dentro dela
        Spell dentro = naMão.getOrDefault(ArcanaComponents.SPELL, Spell.EMPTY);
        if (dentro.isEmpty()) helper.fail("e levar o feitiço dentro");
        if (dentro.first().shape() != Shapes.BINDING_PICKAXE) helper.fail("que é o mesmo que a fez");

        quem.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        helper.succeed();
    }

    /** Sem um feitiço na mão, o Vínculo não tem o que vincular. */
    @GameTest(maxTicks = 60)
    public void bindingNeedsASpellInHand(GameTestHelper helper) {
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);
        quem.setItemInHand(InteractionHand.MAIN_HAND,
                new ItemStack(net.minecraft.world.item.Items.DIRT));

        var saiu = SpellCast.cast(helper.getLevel(),
                Spell.of(Shapes.BINDING_AXE, Essences.DIG), quem, null, quem.position());
        if (saiu.ok()) helper.fail("sem feitiço na mão, o Vínculo não pega");

        quem.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        helper.succeed();
    }

    /**
     * A ferramenta <b>desfaz-se</b> quando a mana acaba, e volta a ser o feitiço que era.
     *
     * <p>É o coração da ideia: uma ferramenta que só existe enquanto se pode pagar por ela.
     */
    @GameTest(maxTicks = 60)
    public void theToolUnbindsWhenManaRunsOut(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);

        Spell vínculo = Spell.of(Shapes.BINDING_PICKAXE, Essences.DIG);
        var ferramenta = new ItemStack(ArcanaItems.BOUND.get(BoundToolItem.Kind.PICKAXE));
        ferramenta.set(ArcanaComponents.SPELL, vínculo);
        quem.getInventory().setItem(0, ferramenta);

        // sem mana nenhuma, ela desfaz-se
        Mana.set(quem, Mana.NONE);
        BoundToolItem.unbind(ferramenta, quem);

        ItemStack volta = quem.getInventory().getItem(0);
        if (!(volta.getItem() instanceof SpellItem)) {
            helper.fail("ela devia voltar a ser feitiço, e virou " + volta);
            return;
        }
        Spell guardado = SpellItem.spellOf(volta);
        if (guardado.isEmpty()) helper.fail("e trazer a frase de volta");
        if (guardado.first().shape() != Shapes.BINDING_PICKAXE) helper.fail("a mesma frase");

        quem.getInventory().setItem(0, ItemStack.EMPTY);
        Mana.set(quem, Mana.NONE);
        helper.succeed();
    }

    /** As cinco estão na árvore, e todas depois da Luz. */
    @GameTest
    public void theFiveAreInTheTree(GameTestHelper helper) {
        for (var forma : List.of(Shapes.BINDING_PICKAXE, Shapes.BINDING_AXE, Shapes.BINDING_SWORD,
                Shapes.BINDING_SHOVEL, Shapes.BINDING_HOE)) {
            var perícia = SkillTree.of(forma);
            if (perícia == null) {
                helper.fail(forma.name() + " devia estar na árvore");
                continue;
            }
            if (perícia.branch() != SkillTree.Branch.UTILITY) {
                helper.fail(forma.name() + " fica em Utilidade, como no original");
            }
            if (perícia.needs().isEmpty()) helper.fail(forma.name() + " não é raiz");
        }
        // a primeira fica onde o Vínculo do original ficava
        var picareta = SkillTree.of(Shapes.BINDING_PICKAXE);
        if (picareta.x() != 275 || picareta.y() != 210) {
            helper.fail("a picareta fica onde o Vínculo do original ficava");
        }
        if (!picareta.needs().contains(Essences.LIGHT)) helper.fail("e depois da Luz");
        helper.succeed();
    }

    /**
     * Com o Vínculo, aperta ainda mais: são <b>trinta e sete</b> perícias para vinte e cinco pontos.
     *
     * <p>Escolher o que deixar de lado é o ramo inteiro.
     */
    @GameTest
    public void thereAreStillFewerPointsThanSkills(GameTestHelper helper) {
        int todos = 0;
        for (var cor : SkillTree.Point.values()) todos += SkillTree.pointsUpTo(cor, SkillTree.RED_UNTIL);
        if (SkillTree.entries().size() != 37) {
            helper.fail("devia haver trinta e sete perícias, e há " + SkillTree.entries().size());
        }
        if (todos >= SkillTree.entries().size()) {
            helper.fail("e menos pontos (" + todos + ") que perícias");
        }
        helper.succeed();
    }
}
