package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.louse.BarkBelt;
import net.thaumcraft.occulta.louse.BeltPotionRecipe;
import net.thaumcraft.occulta.louse.BitingBelt;
import net.thaumcraft.occulta.louse.Lice;
import net.thaumcraft.occulta.louse.LouseEntity;
import net.thaumcraft.occulta.louse.LouseItem;

import java.util.List;

/**
 * O <b>Piolho Parasita</b> e os dois cintos: a poção que o piolho leva e gasta de uma vez, o cinto que
 * guarda duas e as manda para o lado certo, e o Cinto de Casca, que junta madeira do chão e apara golpes
 * com ela.
 */
public class OccultaLouseGameTest {
    /** Encher um piolho ou um cinto na bancada: o mesmo feitio, duas peças. */
    @GameTest
    public void aPotionGoesIntoTheLouseAndTheBelt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack frasco = new ItemStack(Items.POTION);
        frasco.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.POISON));

        // o piolho leva uma
        var bancada = CraftingInput.of(2, 1, List.of(new ItemStack(OccultaItems.LOUSE), frasco.copy()));
        if (!BeltPotionRecipe.INSTANCE.matches(bancada, level)) helper.fail("o piolho enche-se de poção");
        ItemStack cheio = BeltPotionRecipe.INSTANCE.assemble(bancada);
        if (!LouseItem.cheio(cheio)) helper.fail("e fica cheio");

        // e cheio não leva outra
        var outra = CraftingInput.of(2, 1, List.of(cheio, frasco.copy()));
        if (BeltPotionRecipe.INSTANCE.matches(outra, level)) helper.fail("mas só uma de cada vez");

        // o cinto leva duas, e à terceira recusa
        ItemStack cinto = new ItemStack(OccultaItems.BITING_BELT);
        for (int volta = 1; volta <= BitingBelt.CABEM; volta++) {
            var mesa = CraftingInput.of(2, 1, List.of(cinto, frasco.copy()));
            if (!BeltPotionRecipe.INSTANCE.matches(mesa, level)) {
                helper.fail("o cinto aceita a poção número " + volta);
                return;
            }
            cinto = BeltPotionRecipe.INSTANCE.assemble(mesa);
            if (BitingBelt.poções(cinto).size() != volta) helper.fail("e guarda " + volta);
        }
        var terceira = CraftingInput.of(2, 1, List.of(cinto, frasco.copy()));
        if (BeltPotionRecipe.INSTANCE.matches(terceira, level)) helper.fail("e à terceira recusa");

        // e a garrafa fica na bancada
        var mesa = CraftingInput.of(2, 1, List.of(new ItemStack(OccultaItems.LOUSE), frasco.copy()));
        boolean achou = false;
        for (var sobra : BeltPotionRecipe.INSTANCE.getRemainingItems(mesa)) {
            if (sobra.is(Items.GLASS_BOTTLE)) achou = true;
        }
        if (!achou) helper.fail("e a garrafa vazia fica na bancada");
        helper.succeed();
    }

    /** <b>A mordida</b>: passa a poção uma vez só, e depois o piolho fica vazio. */
    @GameTest
    public void theLouseBitesOnlyOnce(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        LouseEntity piolho = helper.spawn(OccultaEntities.LOUSE, new BlockPos(2, 2, 2));
        piolho.poção(new PotionContents(Potions.POISON));
        if (!piolho.cheio()) helper.fail("ele leva a poção dentro");

        // o alvo é um porco e não um zumbi: morto-vivo não se envenena, e a prova mede a poção
        var alvo = helper.spawn(EntityTypes.PIG, new BlockPos(2, 2, 3));
        piolho.doHurtTarget(level, alvo);
        if (!alvo.hasEffect(MobEffects.POISON)) helper.fail("e a mordida a passa");
        if (piolho.cheio()) helper.fail("e gasta-a: o piolho fica vazio");

        // a segunda mordida não dá nada
        alvo.removeEffect(MobEffects.POISON);
        piolho.doHurtTarget(level, alvo);
        if (alvo.hasEffect(MobEffects.POISON)) helper.fail("a segunda mordida não dá nada");

        alvo.discard();
        piolho.discard();
        helper.succeed();
    }

    /**
     * <b>Para onde a poção vai</b>: as agressivas para quem bateu, as outras para quem a trazia.
     *
     * <p>É a ideia do cinto, e é ela que faz a mesma peça ser armadura ou arma conforme o que se lhe puser
     * dentro.
     */
    @GameTest
    public void theAggressiveOnesGoToTheOtherSide(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        // quem bate é um porco pela mesma razão: um zumbi não se envenena
        var bateu = helper.spawn(EntityTypes.PIG, new BlockPos(2, 2, 2));

        // veneno: vai para quem bateu
        ItemStack cinto = BitingBelt.enche(new ItemStack(OccultaItems.BITING_BELT),
                new PotionContents(Potions.POISON));
        quem.setItemSlot(EquipmentSlot.LEGS, cinto);
        Lice.levouGolpe(level, quem, level.damageSources().mobAttack(bateu));
        if (!bateu.hasEffect(MobEffects.POISON)) helper.fail("o veneno vai para quem bateu");
        if (quem.hasEffect(MobEffects.POISON)) helper.fail("e não para quem o trazia");
        if (!BitingBelt.poções(quem.getItemBySlot(EquipmentSlot.LEGS)).isEmpty()) {
            helper.fail("e a poção gasta-se");
        }

        // cura: fica em quem o traz
        bateu.removeEffect(MobEffects.POISON);
        ItemStack outro = BitingBelt.enche(new ItemStack(OccultaItems.BITING_BELT),
                new PotionContents(Potions.SWIFTNESS));
        quem.setItemSlot(EquipmentSlot.LEGS, outro);
        Lice.levouGolpe(level, quem, level.damageSources().mobAttack(bateu));
        if (!quem.hasEffect(MobEffects.SPEED)) helper.fail("a pressa fica em quem traz o cinto");
        if (bateu.hasEffect(MobEffects.SPEED)) helper.fail("e não vai para quem bateu");

        quem.removeEffect(MobEffects.SPEED);
        quem.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
        bateu.discard();
        helper.succeed();
    }

    /** <b>O piolho na mochila fala primeiro</b>, e custa um de dano a quem o traz. */
    @GameTest
    public void theLouseInTheBagSpeaksFirst(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.getAbilities().invulnerable = false;
        var bateu = helper.spawn(EntityTypes.PIG, new BlockPos(2, 2, 2));

        ItemStack piolho = new ItemStack(OccultaItems.LOUSE);
        piolho.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.POISON));
        quem.getInventory().add(piolho);
        ItemStack cinto = BitingBelt.enche(new ItemStack(OccultaItems.BITING_BELT),
                new PotionContents(Potions.SWIFTNESS));
        quem.setItemSlot(EquipmentSlot.LEGS, cinto);

        Lice.levouGolpe(level, quem, level.damageSources().mobAttack(bateu));
        if (!bateu.hasEffect(MobEffects.POISON)) helper.fail("o piolho da mochila morde primeiro");
        if (quem.hasEffect(MobEffects.SPEED)) helper.fail("e o cinto fica calado nessa pancada");
        if (BitingBelt.poções(quem.getItemBySlot(EquipmentSlot.LEGS)).size() != 1) {
            helper.fail("e não gasta nada");
        }

        quem.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
        quem.getInventory().clearContent();
        bateu.discard();
        helper.succeed();
    }

    /**
     * <b>O Cinto de Casca</b>: junta em grama, para no teto, apara o golpe e larga pau — menos se o golpe
     * for de madeira.
     */
    @GameTest
    public void theBarkBeltGathersAndParries(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        ItemStack cinto = new ItemStack(OccultaItems.BARK_BELT);
        quem.setItemSlot(EquipmentSlot.LEGS, cinto);
        level.setBlockAndUpdate(quem.blockPosition().below(), Blocks.GRASS_BLOCK.defaultBlockState());

        // o teto: duas por peça de roupa de bruxa, e o cinto é uma
        if (BarkBelt.teto(quem) != BarkBelt.POR_PEÇA) {
            helper.fail("só o cinto vale duas, e vale " + BarkBelt.teto(quem));
        }
        quem.setItemSlot(EquipmentSlot.HEAD, new ItemStack(OccultaItems.WITCH_HAT));
        if (BarkBelt.teto(quem) != BarkBelt.POR_PEÇA * 2) helper.fail("com o chapéu, quatro");

        // junta até o teto
        BarkBelt.carga(cinto, 0);
        for (int volta = 0; volta < 500 && BarkBelt.carga(cinto) < BarkBelt.teto(quem); volta++) {
            BarkBelt.junta(level, quem);
            if (level.getGameTime() % BarkBelt.JUNTA_DE != 0) break;
        }
        BarkBelt.carga(cinto, BarkBelt.teto(quem));

        // e apara o golpe, largando pau
        var bateu = helper.spawn(EntityTypes.PIG, new BlockPos(2, 2, 2));
        int tinha = BarkBelt.carga(cinto);
        if (!BarkBelt.apara(level, quem, level.damageSources().mobAttack(bateu))) {
            helper.fail("com carga, ele apara o golpe");
        }
        if (BarkBelt.carga(cinto) >= tinha) helper.fail("e gasta carga a fazê-lo");

        // sem carga, não apara
        BarkBelt.carga(cinto, 0);
        if (BarkBelt.apara(level, quem, level.damageSources().mobAttack(bateu))) {
            helper.fail("sem carga, ele não apara nada");
        }

        // e um golpe de madeira passa mesmo com carga
        BarkBelt.carga(cinto, BarkBelt.teto(quem));
        bateu.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.WOODEN_SWORD));
        if (!BarkBelt.deMadeira(level.damageSources().mobAttack(bateu))) {
            helper.fail("uma espada de pau é golpe de madeira");
        }
        if (BarkBelt.apara(level, quem, level.damageSources().mobAttack(bateu))) {
            helper.fail("e madeira não se apara com madeira");
        }
        // mas um machado de pau não conta: o original só olha a espada
        bateu.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.WOODEN_AXE));
        if (BarkBelt.deMadeira(level.damageSources().mobAttack(bateu))) {
            helper.fail("e um machado de pau não conta");
        }

        quem.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
        quem.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        bateu.discard();
        helper.succeed();
    }
}
