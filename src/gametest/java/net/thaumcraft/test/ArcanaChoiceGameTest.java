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
import net.thaumcraft.arcana.ArcanaComponents;
import net.thaumcraft.arcana.ArcanaItems;
import net.thaumcraft.arcana.Essences;
import net.thaumcraft.arcana.InscriptionTableBlockEntity;
import net.thaumcraft.arcana.Modifiers;
import net.thaumcraft.arcana.Shapes;
import net.thaumcraft.arcana.Spell;
import net.thaumcraft.arcana.SpellFx;
import net.thaumcraft.arcana.SpellItem;
import net.thaumcraft.arcana.SpellValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * As três peças que precisam de uma <b>escolha</b>.
 *
 * <p>Todas as outras peças do ramo dizem tudo o que são. Estas três não: a <b>Cor</b> não diz qual cor, e o
 * <b>Colocar Bloco</b> e a <b>Apropriação</b> não dizem qual bloco. Cada uma resolve isso à sua maneira — a
 * Cor com a tinta que se põe ao lado dela na Mesa, as outras duas <b>aprendendo no mundo</b> e guardando o
 * que aprenderam dentro do próprio feitiço.
 */
public class ArcanaChoiceGameTest {
    // ------------------------------------------------------------------ a Cor

    /** A tinta ao lado da Cor vira a cor da frase. */
    @GameTest
    public void theDyeBesideTheColourBecomesTheSpell(GameTestHelper helper) {
        var postas = List.of(
                new SpellValidator.Posta(Shapes.PROJECTILE),
                new SpellValidator.Posta(Essences.FIRE_DAMAGE),
                new SpellValidator.Posta(Modifiers.COLOUR, 2437522));

        var leitura = SpellValidator.validateWithData(postas);
        if (!leitura.ok()) {
            helper.fail("com a tinta, a frase fecha: " + leitura.why());
            return;
        }

        Spell feitiço = SpellValidator.buildWithData(postas);
        Integer guardada = feitiço.data(Modifiers.COLOUR.name());
        if (guardada == null || guardada != 2437522) {
            helper.fail("e a cor fica escrita na etapa, e ficou " + guardada);
        }
        if (SpellFx.color(feitiço) != 2437522) {
            helper.fail("e é com ela que o pó se pinta, e pintou-se de " + SpellFx.color(feitiço));
        }
        helper.succeed();
    }

    /** Sem tinta, a Cor não vira feitiço nenhum — e a Mesa diz porquê. */
    @GameTest
    public void theColourWithoutADyeIsRefused(GameTestHelper helper) {
        var sem = List.of(
                new SpellValidator.Posta(Shapes.PROJECTILE),
                new SpellValidator.Posta(Essences.FIRE_DAMAGE),
                new SpellValidator.Posta(Modifiers.COLOUR));

        var leitura = SpellValidator.validateWithData(sem);
        if (leitura.ok()) helper.fail("sem tinta, a Cor não sabe o que fazer");
        if (leitura.blame() != Modifiers.COLOUR) helper.fail("e a culpa é dela");
        if (leitura.why() == null) helper.fail("e a Mesa diz porquê");
        helper.succeed();
    }

    /** E sem a Cor, o pó continua a sair da Afinidade. */
    @GameTest
    public void withoutTheColourTheAffinityStillPaints(GameTestHelper helper) {
        Spell fogo = Spell.of(Shapes.PROJECTILE, Essences.FIRE_DAMAGE);
        if (SpellFx.color(fogo) != net.thaumcraft.arcana.Affinity.FIRE.color) {
            helper.fail("um feitiço de fogo pinta-se de fogo");
        }
        helper.succeed();
    }

    /** A Mesa lê a tinta como escolha da peça que vem antes, e não como peça. */
    @GameTest
    public void thetableReadsTheDyeAsAChoice(GameTestHelper helper) {
        var casas = new ArrayList<ItemStack>();
        casas.add(new ItemStack(ArcanaItems.itemOf(Shapes.PROJECTILE)));
        casas.add(new ItemStack(ArcanaItems.itemOf(Essences.FIRE_DAMAGE)));
        casas.add(new ItemStack(ArcanaItems.itemOf(Modifiers.COLOUR)));
        casas.add(new ItemStack(Items.DYE.pick(net.minecraft.world.item.DyeColor.BLUE)));
        while (casas.size() < InscriptionTableBlockEntity.RECIPE_SIZE) casas.add(ItemStack.EMPTY);

        var postas = InscriptionTableBlockEntity.ler(casas, InscriptionTableBlockEntity.RECIPE_SIZE);
        if (postas.size() != 3) helper.fail("a tinta não é peça: esperava três e deu " + postas.size());
        if (postas.getLast().peça() != Modifiers.COLOUR) helper.fail("e a última peça é a Cor");
        if (postas.getLast().dado() == null) helper.fail("e ela fica com a tinta que veio a seguir");

        helper.succeed();
    }

    /** E a Mesa inteira escreve o feitiço com a cor dentro. */
    @GameTest(maxTicks = 60)
    public void thetableWritesTheColouredSpell(GameTestHelper helper) {
        // a mesa a sério, posta no mundo da prova: ela guarda-se a si mesma e pede o bloco certo por baixo
        helper.setBlock(new BlockPos(2, 1, 2), net.thaumcraft.arcana.ArcanaBlocks.INSCRIPTION_TABLE);
        var tábua = helper.getBlockEntity(new BlockPos(2, 1, 2), InscriptionTableBlockEntity.class);
        if (tábua == null) {
            helper.fail("a Mesa devia estar lá");
            return;
        }

        tábua.setItem(0, new ItemStack(ArcanaItems.itemOf(Shapes.PROJECTILE)));
        tábua.setItem(1, new ItemStack(ArcanaItems.itemOf(Essences.FIRE_DAMAGE)));
        tábua.setItem(2, new ItemStack(ArcanaItems.itemOf(Modifiers.COLOUR)));
        tábua.setItem(3, new ItemStack(Items.DYE.pick(net.minecraft.world.item.DyeColor.LIME)));
        tábua.reread();

        if (!tábua.reading().ok()) {
            helper.fail("a frase devia fechar: " + tábua.reading().why());
            return;
        }
        ItemStack saiu = tábua.getItem(InscriptionTableBlockEntity.RESULT);
        if (saiu.isEmpty()) {
            helper.fail("e sair feitiço");
            return;
        }
        Integer cor = SpellItem.spellOf(saiu).data(Modifiers.COLOUR.name());
        if (cor == null || cor != 4312372) helper.fail("com o verde-limão do original, e deu " + cor);
        helper.succeed();
    }

    // ------------------------------------------------------------------ o Colocar Bloco

    /** Agachado, o Colocar Bloco aprende o bloco; de pé, põe um igual. */
    @GameTest(maxTicks = 100)
    public void placeBlockLearnsCrouchingAndPlacesStanding(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);

        ItemStack feitiço = SpellItem.write(new ItemStack(ArcanaItems.SPELL),
                Spell.of(Shapes.TOUCH, Essences.PLACE_BLOCK));
        quem.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, feitiço);

        helper.setBlock(new BlockPos(2, 1, 2), Blocks.GOLD_BLOCK);
        BlockPos ouro = helper.absolutePos(new BlockPos(2, 1, 2));

        // de pé e sem saber nada, não faz nada
        quem.setShiftKeyDown(false);
        if (Essences.PLACE_BLOCK.onBlock(level, Spell.of(Shapes.TOUCH, Essences.PLACE_BLOCK), quem,
                ouro, Direction.UP, Vec3.ZERO)) {
            helper.fail("sem ter aprendido, não põe nada");
        }

        // agachado, aprende
        quem.setShiftKeyDown(true);
        if (!Essences.PLACE_BLOCK.onBlock(level, Spell.of(Shapes.TOUCH, Essences.PLACE_BLOCK), quem,
                ouro, Direction.UP, Vec3.ZERO)) {
            helper.fail("agachado, aprende");
            return;
        }
        var sabe = quem.getMainHandItem().get(ArcanaComponents.PLACE_BLOCK);
        if (sabe == null || !sabe.is(Blocks.GOLD_BLOCK)) helper.fail("e fica sabendo o ouro");

        // de pé, põe
        quem.setShiftKeyDown(false);
        BlockPos chão = helper.absolutePos(new BlockPos(4, 1, 4));
        helper.setBlock(new BlockPos(4, 1, 4), Blocks.STONE);
        if (!Essences.PLACE_BLOCK.onBlock(level, Spell.of(Shapes.TOUCH, Essences.PLACE_BLOCK), quem,
                chão, Direction.UP, Vec3.ZERO)) {
            helper.fail("de pé, põe");
            return;
        }
        if (!level.getBlockState(chão.above()).is(Blocks.GOLD_BLOCK)) {
            helper.fail("e o que nasce é o que ele aprendeu");
        }

        quem.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        helper.succeed();
    }

    // ------------------------------------------------------------------ a Apropriação

    /** A Apropriação tira o bloco do mundo com o que ele tem dentro, e põe-no de volta igual. */
    @GameTest(maxTicks = 100)
    public void appropriationCarriesTheChestAndWhatIsInIt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);

        ItemStack feitiço = SpellItem.write(new ItemStack(ArcanaItems.SPELL),
                Spell.of(Shapes.TOUCH, Essences.APPROPRIATION));
        quem.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, feitiço);

        helper.setBlock(new BlockPos(2, 1, 2), Blocks.CHEST);
        BlockPos baú = helper.absolutePos(new BlockPos(2, 1, 2));
        if (level.getBlockEntity(baú) instanceof net.minecraft.world.Container dentro) {
            dentro.setItem(0, new ItemStack(Items.DIAMOND, 7));
        }

        Spell frase = Spell.of(Shapes.TOUCH, Essences.APPROPRIATION);
        if (!Essences.APPROPRIATION.onBlock(level, frase, quem, baú, Direction.UP, Vec3.ZERO)) {
            helper.fail("devia levar o baú");
            return;
        }
        if (!level.getBlockState(baú).isAir()) helper.fail("e o lugar fica vazio");

        var levado = quem.getMainHandItem().get(ArcanaComponents.APPROPRIATED);
        if (levado == null || levado.bloco().isEmpty()) {
            helper.fail("e o feitiço leva o baú dentro");
            return;
        }

        // e de volta, com os diamantes lá
        helper.setBlock(new BlockPos(5, 1, 5), Blocks.STONE);
        BlockPos volta = helper.absolutePos(new BlockPos(5, 1, 5));
        if (!Essences.APPROPRIATION.onBlock(level, frase, quem, volta, Direction.UP, Vec3.ZERO)) {
            helper.fail("e devia pôr de volta");
            return;
        }
        if (!level.getBlockState(volta.above()).is(Blocks.CHEST)) helper.fail("um baú outra vez");
        if (!(level.getBlockEntity(volta.above()) instanceof net.minecraft.world.Container veio)
                || !veio.getItem(0).is(Items.DIAMOND) || veio.getItem(0).getCount() != 7) {
            helper.fail("e com os sete diamantes lá dentro");
        }
        if (quem.getMainHandItem().get(ArcanaComponents.APPROPRIATED) != null) {
            helper.fail("e o feitiço esvazia");
        }

        quem.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        helper.succeed();
    }

    /** Também leva bicho — e não leva gente. */
    @GameTest(maxTicks = 100)
    public void appropriationCarriesABeastButNotAPerson(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);

        ItemStack feitiço = SpellItem.write(new ItemStack(ArcanaItems.SPELL),
                Spell.of(Shapes.TOUCH, Essences.APPROPRIATION));
        quem.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, feitiço);
        Spell frase = Spell.of(Shapes.TOUCH, Essences.APPROPRIATION);

        // gente, não
        if (Essences.APPROPRIATION.onEntity(level, frase, quem, quem)) {
            helper.fail("um feitiço que guardasse uma pessoa seria outra coisa");
        }

        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
        porco.setNoAi(true);
        porco.setCustomName(net.minecraft.network.chat.Component.literal("Tião"));

        if (!Essences.APPROPRIATION.onEntity(level, frase, quem, porco)) {
            helper.fail("um porco, sim");
            return;
        }
        if (porco.isAlive()) helper.fail("e sai do mundo");

        var levado = quem.getMainHandItem().get(ArcanaComponents.APPROPRIATED);
        if (levado == null || levado.bicho().isEmpty()) helper.fail("e vai dentro do feitiço");

        quem.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        helper.succeed();
    }

    /** E leva uma coisa de cada vez: com alguma dentro, ela só sabe devolver. */
    @GameTest(maxTicks = 100)
    public void appropriationHoldsOneThingAtATime(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);

        ItemStack feitiço = SpellItem.write(new ItemStack(ArcanaItems.SPELL),
                Spell.of(Shapes.TOUCH, Essences.APPROPRIATION));
        quem.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, feitiço);
        Spell frase = Spell.of(Shapes.TOUCH, Essences.APPROPRIATION);

        helper.setBlock(new BlockPos(2, 1, 2), Blocks.GOLD_BLOCK);
        helper.setBlock(new BlockPos(3, 1, 2), Blocks.DIAMOND_BLOCK);
        BlockPos ouro = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockPos diamante = helper.absolutePos(new BlockPos(3, 1, 2));

        Essences.APPROPRIATION.onBlock(level, frase, quem, ouro, Direction.UP, Vec3.ZERO);
        // com o ouro dentro, bater no diamante põe o ouro de volta e não leva o diamante
        Essences.APPROPRIATION.onBlock(level, frase, quem, diamante, Direction.UP, Vec3.ZERO);

        if (!level.getBlockState(diamante).is(Blocks.DIAMOND_BLOCK)) {
            helper.fail("o diamante fica onde está");
        }
        if (!level.getBlockState(diamante.above()).is(Blocks.GOLD_BLOCK)) {
            helper.fail("e o ouro volta ao mundo");
        }

        quem.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        helper.succeed();
    }
}
