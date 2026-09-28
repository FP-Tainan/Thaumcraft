package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.arcana.ArcanaComponents;
import net.thaumcraft.arcana.ArcanaItems;
import net.thaumcraft.arcana.Essences;
import net.thaumcraft.arcana.Mana;
import net.thaumcraft.arcana.Modifiers;
import net.thaumcraft.arcana.Shapes;
import net.thaumcraft.arcana.Spell;
import net.thaumcraft.arcana.SpellCast;
import net.thaumcraft.arcana.SpellItem;
import net.thaumcraft.arcana.SpellModifierKind;
import net.thaumcraft.arcana.SpellParts;

import java.util.List;

/**
 * O núcleo do Ars Arcana: a gramática, os números dela, a mana e o lançamento.
 */
public class ArcanaSpellGameTest {
    // ------------------------------------------------------------------ a gramática

    /** As três classes de palavra estão registadas, e cada peça acha-se pelo nome. */
    @GameTest
    public void theGrammarIsRegistered(GameTestHelper helper) {
        if (SpellParts.shapes().isEmpty()) helper.fail("devia haver Formas");
        if (SpellParts.essences().isEmpty()) helper.fail("e Essências");
        if (SpellParts.modifiers().isEmpty()) helper.fail("e Modificadores");

        for (var peça : SpellParts.shapes()) {
            if (SpellParts.byName(peça.name()) != peça) helper.fail("a Forma " + peça.name() + " acha-se pelo nome");
        }
        for (var peça : SpellParts.essences()) {
            if (SpellParts.byName(peça.name()) != peça) helper.fail("a Essência " + peça.name() + " também");
        }
        for (var peça : SpellParts.modifiers()) {
            if (SpellParts.byName(peça.name()) != peça) helper.fail("e o Modificador " + peça.name());
        }
        helper.succeed();
    }

    /** Um feitiço escrito num item volta a ser o mesmo feitiço: é o que o guardar por nome garante. */
    @GameTest
    public void aSpellSurvivesTheWriting(GameTestHelper helper) {
        Spell escrito = new Spell(List.of(new Spell.Stage(Shapes.TOUCH,
                List.of(Essences.FIRE_DAMAGE, Essences.LIGHT),
                List.of(Modifiers.DAMAGE, Modifiers.DAMAGE))));

        ItemStack coisa = SpellItem.write(new ItemStack(ArcanaItems.SPELL), escrito);
        Spell lido = SpellItem.spellOf(coisa);

        if (lido.stages().size() != 1) helper.fail("uma etapa entra, uma etapa sai");
        var etapa = lido.first();
        if (etapa == null || etapa.shape() != Shapes.TOUCH) helper.fail("e a Forma é a mesma");
        if (etapa.essences().size() != 2) helper.fail("e as duas Essências voltam");
        if (etapa.modifiers().size() != 2) helper.fail("e os dois Modificadores, contados duas vezes");
        helper.succeed();
    }

    /** A frase corre etapa a etapa: tirar a da frente deixa o resto. */
    @GameTest
    public void popTakesTheFirstStage(GameTestHelper helper) {
        Spell três = new Spell(List.of(
                new Spell.Stage(Shapes.SELF, List.of(Essences.HEAL), List.of()),
                new Spell.Stage(Shapes.TOUCH, List.of(Essences.DIG), List.of()),
                new Spell.Stage(Shapes.AOE, List.of(Essences.LIGHT), List.of())));

        Spell duas = três.pop();
        if (duas.stages().size() != 2) helper.fail("de três ficam duas");
        if (duas.first() == null || duas.first().shape() != Shapes.TOUCH) helper.fail("e a da frente é a segunda");

        Spell uma = duas.pop();
        if (uma.stages().size() != 1) helper.fail("de duas fica uma");
        if (!uma.pop().isEmpty()) helper.fail("e de uma não fica nada");
        helper.succeed();
    }

    // ------------------------------------------------------------------ os números

    /** O Dano <b>soma</b> e a Duração <b>multiplica</b>: são contas diferentes, e é de propósito. */
    @GameTest
    public void modifiersAddAndMultiply(GameTestHelper helper) {
        Spell comDano = new Spell(List.of(new Spell.Stage(Shapes.TOUCH,
                List.of(Essences.FIRE_DAMAGE), List.of(Modifiers.DAMAGE))));
        double dano = comDano.add(SpellModifierKind.DAMAGE, 6.0);
        if (Math.abs(dano - 8.2) > 1.0e-4) helper.fail("seis mais 2,2 dá 8,2, e deu " + dano);

        Spell comDois = new Spell(List.of(new Spell.Stage(Shapes.TOUCH,
                List.of(Essences.FIRE_DAMAGE), List.of(Modifiers.DAMAGE, Modifiers.DAMAGE))));
        double doisDanos = comDois.add(SpellModifierKind.DAMAGE, 6.0);
        if (Math.abs(doisDanos - 10.4) > 1.0e-4) helper.fail("e dois somam duas vezes: " + doisDanos);

        Spell comTempo = new Spell(List.of(new Spell.Stage(Shapes.SELF,
                List.of(Essences.LIGHT), List.of(Modifiers.DURATION))));
        double tempo = comTempo.mul(SpellModifierKind.DURATION, 600.0);
        if (Math.abs(tempo - 1320.0) > 1.0e-4) helper.fail("seiscentos vezes 2,2 dá 1320, e deu " + tempo);
        helper.succeed();
    }

    /** E o custo multiplica: a Forma primeiro, e cada Modificador pelo número de vezes que aparece. */
    @GameTest
    public void theCostMultiplies(GameTestHelper helper) {
        Spell simples = Spell.of(Shapes.TOUCH, Essences.LIGHT);
        float baseCusto = simples.manaCost(null, null);
        if (Math.abs(baseCusto - 50.0f) > 0.01f) {
            helper.fail("a Luz de toque custa cinquenta, e custa " + baseCusto);
        }

        // a Autoconjuração é metade
        Spell emSi = Spell.of(Shapes.SELF, Essences.LIGHT);
        if (Math.abs(emSi.manaCost(null, null) - 25.0f) > 0.01f) {
            helper.fail("e em si mesmo, metade: " + emSi.manaCost(null, null));
        }

        // e um modificador encarece
        Spell comMod = new Spell(List.of(new Spell.Stage(Shapes.TOUCH,
                List.of(Essences.LIGHT), List.of(Modifiers.DURATION))));
        if (comMod.manaCost(null, null) <= baseCusto) helper.fail("um modificador encarece o feitiço");
        helper.succeed();
    }

    // ------------------------------------------------------------------ a mana

    /** A conta da mana é a do original: nível elevado a 1,5, vezes o degrau, mais quinhentos. */
    @GameTest
    public void theManaFormulaIsTheOriginals(GameTestHelper helper) {
        if (Math.abs(Mana.maxManaFor(0) - 500.0f) > 0.01f) {
            helper.fail("de nível zero há quinhentos, e há " + Mana.maxManaFor(0));
        }
        if (Mana.maxManaFor(50) <= Mana.maxManaFor(10)) helper.fail("e cresce com o nível");
        if (Mana.maxManaFor(99) <= Mana.maxManaFor(50)) helper.fail("e dispara no fim");
        if (Mana.maxManaFor(200) != Mana.maxManaFor(99)) helper.fail("e para no 99");
        helper.succeed();
    }

    /** Gastar tira mana e soma desgaste, e nenhum dos dois passa dos limites. */
    @GameTest
    public void spendingCostsBoth(GameTestHelper helper) {
        Mana conta = Mana.NONE.withLevel(10);
        conta = conta.withMana(conta.maxMana());
        float tinha = conta.mana();

        conta = conta.spend(100.0f, 38.0f);
        if (Math.abs(conta.mana() - (tinha - 100.0f)) > 0.01f) helper.fail("gastou cem de mana");
        if (Math.abs(conta.burnout() - 38.0f) > 0.01f) helper.fail("e deixou 38 de desgaste");

        // e nada passa do chão nem do teto
        conta = conta.spend(999999.0f, 999999.0f);
        if (conta.mana() != 0.0f) helper.fail("a mana não fica negativa");
        if (conta.burnout() > conta.maxBurnout()) helper.fail("e o desgaste não passa do teto");
        helper.succeed();
    }

    /** O desgaste é 38 por cento do custo: o {@code getBurnoutFromMana}. */
    @GameTest
    public void burnoutIsAShareOfTheCost(GameTestHelper helper) {
        if (Math.abs(Essences.burnoutFromMana(100.0f) - 38.0f) > 0.01f) {
            helper.fail("38 por cento de cem é 38");
        }
        if (Math.abs(Essences.HEAL.burnout() - Essences.burnoutFromMana(Essences.HEAL.manaCost())) > 0.01f) {
            helper.fail("e a Cura segue a mesma conta");
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ lançar

    /** Sem mana, o feitiço não sai — e não cobra nada. */
    @GameTest(maxTicks = 60)
    public void withoutManaNothingHappens(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        Mana.set(quem, Mana.NONE);

        Spell caro = Spell.of(Shapes.SELF, Essences.HEAL);
        var saiu = SpellCast.cast(helper.getLevel(), caro, quem, null, quem.getEyePosition());
        if (saiu != SpellCast.Result.NOT_ENOUGH_MANA) {
            helper.fail("sem mana devia dizer que falta mana, e disse " + saiu);
        }
        if (Mana.of(quem).mana() != 0.0f) helper.fail("e não cobrar nada");
        Mana.set(quem, Mana.NONE);
        helper.succeed();
    }

    /** Com mana, ele sai — e cobra o que disse que ia cobrar. */
    @GameTest(maxTicks = 60)
    public void castingCostsWhatItSaid(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        Mana.set(quem, Mana.NONE.withLevel(20));
        Mana.set(quem, Mana.of(quem).withMana(Mana.of(quem).maxMana()));
        float tinha = Mana.of(quem).mana();

        quem.setHealth(quem.getMaxHealth() - 5.0f);
        Spell cura = Spell.of(Shapes.SELF, Essences.HEAL);
        float custo = cura.manaCost(quem, null);

        var saiu = SpellCast.cast(level, cura, quem, null, quem.getEyePosition());
        if (!saiu.ok()) {
            helper.fail("com mana o feitiço devia sair, e deu " + saiu);
            Mana.set(quem, Mana.NONE);
            return;
        }
        if (Math.abs(Mana.of(quem).mana() - (tinha - custo)) > 0.01f) {
            helper.fail("e cobrar o que disse: " + custo);
        }
        if (Mana.of(quem).burnout() <= 0.0f) helper.fail("e deixar desgaste");
        Mana.set(quem, Mana.NONE);
        helper.succeed();
    }

    /** Um feitiço que não acha em que pegar não cobra nada: é o que o original faz. */
    @GameTest(maxTicks = 60)
    public void aSpellThatMissesIsFree(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        Mana.set(quem, Mana.NONE.withLevel(20));
        Mana.set(quem, Mana.of(quem).withMana(Mana.of(quem).maxMana()));
        float tinha = Mana.of(quem).mana();

        // a Cura em quem está com a vida cheia não pega
        quem.setHealth(quem.getMaxHealth());
        var saiu = SpellCast.cast(helper.getLevel(), Spell.of(Shapes.SELF, Essences.HEAL), quem, null,
                quem.getEyePosition());
        if (saiu.ok()) helper.fail("com a vida cheia, a Cura não pega");
        if (Math.abs(Mana.of(quem).mana() - tinha) > 0.01f) helper.fail("e um feitiço que falha é de graça");
        Mana.set(quem, Mana.NONE);
        helper.succeed();
    }

    /** O Dano de Fogo queima quem leva. */
    @GameTest(maxTicks = 60)
    public void fireDamageBurns(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = new BlockPos(2, 2, 2);
        var porco = helper.spawn(EntityTypes.PIG, onde);
        float tinha = porco.getHealth();

        var quem = helper.makeMockServerPlayerInLevel();
        Spell fogo = Spell.of(Shapes.TOUCH, Essences.FIRE_DAMAGE);
        var saiu = SpellCast.onEntity(level, fogo, quem, porco);

        if (!saiu.ok()) {
            helper.fail("o fogo devia pegar no porco");
            return;
        }
        if (porco.getHealth() >= tinha) helper.fail("e tirar-lhe vida");
        if (!porco.isOnFire()) helper.fail("e pô-lo a arder");
        helper.succeed();
    }

    /** E o Escavar quebra o bloco em que bate. */
    @GameTest(maxTicks = 60)
    public void digBreaksTheBlock(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos casa = new BlockPos(1, 2, 1);
        helper.setBlock(casa, Blocks.DIRT.defaultBlockState());
        BlockPos certo = helper.absolutePos(casa);

        var quem = helper.makeMockServerPlayerInLevel();
        var saiu = SpellCast.onBlock(level, Spell.of(Shapes.TOUCH, Essences.DIG), quem, certo,
                Direction.UP, Vec3.atCenterOf(certo));

        if (!saiu.ok()) {
            helper.fail("o Escavar devia pegar na terra");
            return;
        }
        if (!helper.getBlockState(casa).isAir()) helper.fail("e deixá-la vazia");

        // e não quebra o que não se quebra
        helper.setBlock(casa, Blocks.BEDROCK.defaultBlockState());
        if (SpellCast.onBlock(level, Spell.of(Shapes.TOUCH, Essences.DIG), quem, certo,
                Direction.UP, Vec3.atCenterOf(certo)).ok()) {
            helper.fail("e a rocha-mãe fica onde está");
        }
        helper.setBlock(casa, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /** O item do feitiço nasce vazio, e vazio ele não faz nada. */
    @GameTest
    public void anEmptySpellIsEmpty(GameTestHelper helper) {
        ItemStack vazio = new ItemStack(ArcanaItems.SPELL);
        if (!SpellItem.spellOf(vazio).isEmpty()) helper.fail("um feitiço por escrever está vazio");
        if (vazio.has(ArcanaComponents.SPELL)) helper.fail("e não traz componente nenhum");
        if (vazio.getMaxStackSize() != 1) helper.fail("e não empilha, porque não há dois iguais");
        helper.succeed();
    }
}
