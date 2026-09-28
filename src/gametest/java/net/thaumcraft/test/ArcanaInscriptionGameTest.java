package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.arcana.ArcanaBlocks;
import net.thaumcraft.arcana.ArcanaItems;
import net.thaumcraft.arcana.Essences;
import net.thaumcraft.arcana.InscriptionTableBlockEntity;
import net.thaumcraft.arcana.Modifiers;
import net.thaumcraft.arcana.Shapes;
import net.thaumcraft.arcana.Spell;
import net.thaumcraft.arcana.SpellItem;
import net.thaumcraft.arcana.SpellPart;
import net.thaumcraft.arcana.SpellPartItem;
import net.thaumcraft.arcana.SpellParts;
import net.thaumcraft.arcana.SpellValidator;

import java.util.List;

/**
 * A gramática como regra: o validador e a Mesa de Inscrição.
 *
 * <p>Até aqui um feitiço malformado só dava em nada ao ser lançado. A partir daqui há quem diga por quê.
 */
public class ArcanaInscriptionGameTest {
    // ------------------------------------------------------------------ separar em etapas

    /** Cada Forma começa uma etapa nova, e o que vem depois dela é dessa etapa. */
    @GameTest
    public void eachShapeStartsAStage(GameTestHelper helper) {
        List<SpellPart> receita = List.of(
                Shapes.TOUCH, Essences.FIRE_DAMAGE, Modifiers.DAMAGE,
                Shapes.AOE, Essences.LIGHT);
        var etapas = SpellValidator.split(receita);

        if (etapas.size() != 2) helper.fail("duas Formas dão duas etapas, e deram " + etapas.size());
        if (etapas.get(0).size() != 3) helper.fail("a primeira leva a Forma, a Essência e o Modificador");
        if (etapas.get(1).size() != 2) helper.fail("e a segunda leva as duas dela");
        helper.succeed();
    }

    /** Peças soltas antes da primeira Forma ficam numa etapa sem Forma — que é inválida. */
    @GameTest
    public void partsBeforeAnyShapeAreTheirOwnStage(GameTestHelper helper) {
        var etapas = SpellValidator.split(List.of(Essences.FIRE_DAMAGE, Shapes.TOUCH, Essences.HEAL));
        if (etapas.size() != 2) helper.fail("a Essência solta faz uma etapa, e deram " + etapas.size());

        var saiu = SpellValidator.validate(List.of(Essences.FIRE_DAMAGE, Shapes.TOUCH, Essences.HEAL));
        if (saiu.ok()) helper.fail("e escrever a frase pelo fim não vale");
        helper.succeed();
    }

    // ------------------------------------------------------------------ as quatro regras

    /** Uma frase vazia não é errada: é só uma frase que ainda não se escreveu. */
    @GameTest
    public void nothingIsNotWrong(GameTestHelper helper) {
        var saiu = SpellValidator.validate(List.of());
        if (saiu.ok()) helper.fail("uma frase vazia não fecha");
        if (saiu.why() != null) helper.fail("mas também não se queixa: " + saiu.why().getString());
        helper.succeed();
    }

    /** A última etapa precisa de uma Essência; as do meio não. */
    @GameTest
    public void theLastStageNeedsAComponent(GameTestHelper helper) {
        if (SpellValidator.validate(List.of(Shapes.TOUCH)).ok()) {
            helper.fail("uma Forma sozinha não faz nada");
        }
        if (!SpellValidator.validate(List.of(Shapes.TOUCH, Essences.FIRE_DAMAGE)).ok()) {
            helper.fail("mas uma Forma com uma Essência fecha");
        }
        // a do meio pode ir sem Essência, porque quem faz alguma coisa é o fim da frase
        if (!SpellValidator.validate(List.of(Shapes.PROJECTILE, Shapes.AOE, Essences.FIRE_DAMAGE)).ok()) {
            helper.fail("e a etapa do meio pode ir sem Essência");
        }
        helper.succeed();
    }

    /** Uma Forma principum não pode ser a última, e a queixa diz qual é. */
    @GameTest
    public void aPrincipumShapeCannotBeLast(GameTestHelper helper) {
        var saiu = SpellValidator.validate(List.of(Shapes.ZONE, Essences.FIRE_DAMAGE));
        if (saiu.ok()) helper.fail("uma Zona no fim não fecha");
        if (saiu.blame() != Shapes.ZONE) helper.fail("e a culpa é da Zona");
        if (saiu.why() == null) helper.fail("e ela diz por quê");

        if (!SpellValidator.validate(List.of(Shapes.ZONE, Shapes.AOE, Essences.FIRE_DAMAGE)).ok()) {
            helper.fail("mas com uma Forma depois, fecha");
        }
        helper.succeed();
    }

    /** E toda Forma que é principum de verdade é recusada no fim. */
    @GameTest
    public void everyPrincipumShapeIsRefusedAtTheEnd(GameTestHelper helper) {
        for (SpellPart.Shape forma : SpellParts.shapes()) {
            if (!forma.principum()) continue;
            var saiu = SpellValidator.validate(List.of(forma, Essences.FIRE_DAMAGE));
            if (saiu.ok()) helper.fail("a " + forma.name() + " não pode ser a última");
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ escrever e ler de volta

    /** O que se escreve é o que sai: a frase vira feitiço com as etapas certas. */
    @GameTest
    public void whatIsWrittenIsWhatComesOut(GameTestHelper helper) {
        List<SpellPart> receita = List.of(
                Shapes.PROJECTILE, Modifiers.SPEED,
                Shapes.AOE, Essences.FIRE_DAMAGE, Modifiers.DAMAGE, Modifiers.DAMAGE);
        if (!SpellValidator.validate(receita).ok()) {
            helper.fail("esta frase devia fechar");
            return;
        }

        Spell feitiço = SpellValidator.build(receita);
        if (feitiço.stages().size() != 2) helper.fail("ela tem duas etapas");
        var primeira = feitiço.stages().get(0);
        if (primeira.shape() != Shapes.PROJECTILE) helper.fail("e a primeira é o Projétil");
        if (primeira.modifiers().size() != 1) helper.fail("com uma Velocidade");
        var segunda = feitiço.stages().get(1);
        if (segunda.shape() != Shapes.AOE) helper.fail("e a segunda é a Área");
        if (segunda.essences().size() != 1) helper.fail("com o Dano de Fogo");
        if (segunda.modifiers().size() != 2) helper.fail("e dois Danos");
        helper.succeed();
    }

    /** E o caminho de volta desfaz o de ida. */
    @GameTest
    public void readingBackGivesTheSameParts(GameTestHelper helper) {
        List<SpellPart> receita = List.of(
                Shapes.TOUCH, Essences.HEAL, Modifiers.HEALING,
                Shapes.AOE, Essences.LIGHT);
        Spell feitiço = SpellValidator.build(receita);
        List<SpellPart> volta = SpellValidator.parts(feitiço);
        if (!volta.equals(receita)) {
            helper.fail("as peças de volta são as mesmas: " + volta + " contra " + receita);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ as peças como itens

    /** Há um item por peça da gramática, e ele sabe que peça é. */
    @GameTest
    public void thereIsAnItemForEveryPart(GameTestHelper helper) {
        int quantas = SpellParts.count();
        if (ArcanaItems.PARTS.size() != quantas) {
            helper.fail("há " + quantas + " peças e " + ArcanaItems.PARTS.size() + " itens");
        }
        for (var par : ArcanaItems.PARTS.entrySet()) {
            if (ArcanaItems.partOf(par.getValue()) != par.getKey()) {
                helper.fail("o item de " + par.getKey().name() + " não sabe que peça é");
            }
            if (SpellPartItem.of(new ItemStack(par.getValue())) != par.getKey()) {
                helper.fail("e nem a pilha dele");
            }
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ a mesa

    /** A mesa lê a frase das casas e escreve o feitiço na casa de saída. */
    @GameTest(maxTicks = 60)
    public void theTableWritesTheSpell(GameTestHelper helper) {
        BlockPos onde = new BlockPos(2, 2, 2);
        helper.setBlock(onde, ArcanaBlocks.INSCRIPTION_TABLE.defaultBlockState());
        if (!(helper.getBlockEntity(onde, InscriptionTableBlockEntity.class)
                instanceof InscriptionTableBlockEntity mesa)) {
            helper.fail("a mesa devia ter o seu dado");
            return;
        }

        mesa.setItem(0, new ItemStack(ArcanaItems.itemOf(Shapes.TOUCH)));
        mesa.setItem(1, new ItemStack(ArcanaItems.itemOf(Essences.FIRE_DAMAGE)));

        if (!mesa.reading().ok()) helper.fail("a frase devia fechar");
        ItemStack saiu = mesa.getItem(InscriptionTableBlockEntity.RESULT);
        if (saiu.isEmpty()) {
            helper.fail("e sair um feitiço");
            return;
        }
        Spell feitiço = SpellItem.spellOf(saiu);
        if (feitiço.stages().size() != 1) helper.fail("de uma etapa");
        if (feitiço.first().shape() != Shapes.TOUCH) helper.fail("com a Forma de Toque");

        helper.setBlock(onde, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /** E uma frase que não fecha não deixa feitiço nenhum na casa de saída. */
    @GameTest(maxTicks = 60)
    public void aBrokenSentenceWritesNothing(GameTestHelper helper) {
        BlockPos onde = new BlockPos(3, 2, 3);
        helper.setBlock(onde, ArcanaBlocks.INSCRIPTION_TABLE.defaultBlockState());
        if (!(helper.getBlockEntity(onde, InscriptionTableBlockEntity.class)
                instanceof InscriptionTableBlockEntity mesa)) {
            helper.fail("a mesa devia ter o seu dado");
            return;
        }

        // uma Zona sozinha: principum no fim
        mesa.setItem(0, new ItemStack(ArcanaItems.itemOf(Shapes.ZONE)));
        mesa.setItem(1, new ItemStack(ArcanaItems.itemOf(Essences.FIRE_DAMAGE)));

        if (mesa.reading().ok()) helper.fail("uma Zona no fim não fecha");
        if (!mesa.getItem(InscriptionTableBlockEntity.RESULT).isEmpty()) {
            helper.fail("e não escreve feitiço nenhum");
        }
        if (mesa.reading().why() == null) helper.fail("mas diz por quê");

        // e pondo uma Área depois, fecha
        mesa.setItem(1, new ItemStack(ArcanaItems.itemOf(Shapes.AOE)));
        mesa.setItem(2, new ItemStack(ArcanaItems.itemOf(Essences.FIRE_DAMAGE)));
        if (!mesa.reading().ok()) helper.fail("com a Área no meio, fecha");
        if (mesa.getItem(InscriptionTableBlockEntity.RESULT).isEmpty()) {
            helper.fail("e sai o feitiço");
        }

        helper.setBlock(onde, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /**
     * A mesa <b>não gasta</b> as peças.
     *
     * <p>No original elas nem são coisas: são perícias, e quem as sabe escreve com elas quantos feitiços
     * quiser. Aqui viraram itens, mas o espírito fica.
     */
    @GameTest(maxTicks = 60)
    public void theTableDoesNotEatTheParts(GameTestHelper helper) {
        BlockPos onde = new BlockPos(4, 2, 4);
        helper.setBlock(onde, ArcanaBlocks.INSCRIPTION_TABLE.defaultBlockState());
        if (!(helper.getBlockEntity(onde, InscriptionTableBlockEntity.class)
                instanceof InscriptionTableBlockEntity mesa)) {
            helper.fail("a mesa devia ter o seu dado");
            return;
        }

        mesa.setItem(0, new ItemStack(ArcanaItems.itemOf(Shapes.SELF)));
        mesa.setItem(1, new ItemStack(ArcanaItems.itemOf(Essences.HEAL)));

        // tirar o feitiço e reler: a frase continua lá e escreve outro igual
        mesa.removeItemNoUpdate(InscriptionTableBlockEntity.RESULT);
        mesa.reread();

        if (mesa.getItem(0).isEmpty() || mesa.getItem(1).isEmpty()) {
            helper.fail("as peças ficam na mesa");
        }
        if (mesa.getItem(InscriptionTableBlockEntity.RESULT).isEmpty()) {
            helper.fail("e a mesa escreve outro feitiço igual");
        }

        helper.setBlock(onde, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /** Só peças de feitiço entram nas casas da frase. */
    @GameTest(maxTicks = 60)
    public void onlyPartsGoInTheRow(GameTestHelper helper) {
        BlockPos onde = new BlockPos(5, 2, 5);
        helper.setBlock(onde, ArcanaBlocks.INSCRIPTION_TABLE.defaultBlockState());
        if (!(helper.getBlockEntity(onde, InscriptionTableBlockEntity.class)
                instanceof InscriptionTableBlockEntity mesa)) {
            helper.fail("a mesa devia ter o seu dado");
            return;
        }

        if (!mesa.canPlaceItem(0, new ItemStack(ArcanaItems.itemOf(Shapes.TOUCH)))) {
            helper.fail("uma peça entra");
        }
        if (mesa.canPlaceItem(0, new ItemStack(net.minecraft.world.item.Items.DIRT))) {
            helper.fail("e um punhado de terra não");
        }
        if (mesa.canPlaceItem(InscriptionTableBlockEntity.RESULT,
                new ItemStack(ArcanaItems.itemOf(Shapes.TOUCH)))) {
            helper.fail("e na casa do feitiço não entra nada");
        }

        helper.setBlock(onde, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }
}
