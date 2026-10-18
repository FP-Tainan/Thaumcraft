package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.death.DeathEntity;
import net.thaumcraft.occulta.death.DeathsClothesItem;
import net.thaumcraft.occulta.death.DeathsHandItem;
import net.thaumcraft.occulta.fetish.SpiritEffects;

/**
 * A <b>Morte</b>, o <b>conjunto</b> dela e a <b>foice</b>.
 *
 * <p>A prova que carrega a fatia é a do <b>conjunto</b>: vestir as três roupas com a Mão na mão não é um
 * bônus — é uma identidade. O rito que chama a Morte, achando quem o vista, <b>não cria o bicho</b>:
 * teleporta a pessoa. Errando essa conta, a melhor ideia do ramo deixa de existir.
 */
public class OccultaDeathGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** <b>Os números dela são os do original.</b> */
    @GameTest(maxTicks = 20)
    public void herNumbersAreTheOriginals(GameTestHelper helper) {
        if (DeathEntity.VIDA != 1000.0) helper.fail("mil de vida");
        if (DeathEntity.QUANTO != 0.15f) helper.fail("e o toque leva quinze por cento");
        if (DeathEntity.TETO != 15.0f) helper.fail("e nada lhe tira mais de quinze");
        if (DeathEntity.PROCURA != 64.0) helper.fail("e ela procura a sessenta e quatro");
        if (DeathEntity.ENCARA != 5) helper.fail("e encara cinco batidas antes de atacar");
        helper.succeed();
    }

    /**
     * <b>O toque dela leva quinze por cento da vida máxima</b>, e passa pela armadura.
     *
     * <p>É o que faz dela uma coisa que não se enfrenta com ferro: contra ela, armadura não é resposta.
     */
    @GameTest(maxTicks = 40)
    public void herTouchTakesAShareOfTheWhole(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var ela = helper.spawn(OccultaEntities.DEATH, new BlockPos(3, 2, 3));

        // num bicho de vinte de vida, três — e o mesmo com armadura de diamante na frente
        var zumbi = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(5, 2, 5));
        zumbi.setHealth(zumbi.getMaxHealth());
        zumbi.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
        float tinha = zumbi.getHealth();
        ela.doHurtTarget(level, zumbi);
        float tirou = tinha - zumbi.getHealth();

        float esperado = zumbi.getMaxHealth() * DeathEntity.QUANTO;
        ela.discard();
        zumbi.discard();
        if (Math.abs(tirou - esperado) > 0.01f) {
            helper.fail("quinze por cento de vinte são três, e a armadura não conta: tirou " + tirou);
            return;
        }
        helper.succeed();
    }

    /** <b>E nada lhe tira mais de quinze</b>, por muito que se bata. */
    @GameTest(maxTicks = 40)
    public void nothingTakesMoreThanFifteenFromHer(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var ela = helper.spawn(OccultaEntities.DEATH, new BlockPos(3, 2, 3));
        ela.setHealth(ela.getMaxHealth());
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(5.5, 2, 5.5)));

        float tinha = ela.getHealth();
        ela.hurtServer(level, level.damageSources().playerAttack(quem), 1000.0f);
        float tirou = tinha - ela.getHealth();
        ela.discard();
        if (Math.abs(tirou - DeathEntity.TETO) > 0.01f) {
            helper.fail("mil de golpe tiram quinze: tiraram " + tirou);
            return;
        }
        helper.succeed();
    }

    /**
     * <b>O conjunto</b>: três peças vestidas, e quem as veste é a Morte.
     *
     * <p>É a prova que carrega a fatia. Duas peças não bastam; três bastam.
     */
    @GameTest(maxTicks = 40)
    public void theSetIsThreePieces(GameTestHelper helper) {
        piso(helper);
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(3.5, 2, 3.5)));

        if (DeathsClothesItem.conjuntoVestido(quem)) {
            helper.fail("nu, ninguém é a Morte");
            return;
        }
        quem.setItemSlot(EquipmentSlot.HEAD, new ItemStack(OccultaItems.DEATHS_HOOD));
        quem.setItemSlot(EquipmentSlot.CHEST, new ItemStack(OccultaItems.DEATHS_ROBE));
        if (DeathsClothesItem.conjuntoVestido(quem)) {
            helper.fail("duas peças não bastam");
            return;
        }
        quem.setItemSlot(EquipmentSlot.FEET, new ItemStack(OccultaItems.DEATHS_BOOTS));
        if (!DeathsClothesItem.conjuntoVestido(quem)) {
            helper.fail("e três bastam");
            return;
        }
        helper.succeed();
    }

    /**
     * <b>A foice só abre com o conjunto</b>, e aberta ela tira mais quanto mais faminto estiver quem a
     * segura.
     */
    @GameTest(maxTicks = 40)
    public void theScytheOnlyOpensWithTheSet(GameTestHelper helper) {
        piso(helper);
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(3.5, 2, 3.5)));

        ItemStack foice = new ItemStack(OccultaItems.DEATHS_HAND);
        quem.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, foice);
        foice.getItem().use(helper.getLevel(), quem, net.minecraft.world.InteractionHand.MAIN_HAND);
        if (DeathsHandItem.aberta(foice)) {
            helper.fail("sem as roupas, ela não abre");
            return;
        }

        quem.setItemSlot(EquipmentSlot.HEAD, new ItemStack(OccultaItems.DEATHS_HOOD));
        quem.setItemSlot(EquipmentSlot.CHEST, new ItemStack(OccultaItems.DEATHS_ROBE));
        quem.setItemSlot(EquipmentSlot.FEET, new ItemStack(OccultaItems.DEATHS_BOOTS));
        foice.getItem().use(helper.getLevel(), quem, net.minecraft.world.InteractionHand.MAIN_HAND);
        if (!DeathsHandItem.aberta(foice)) {
            helper.fail("com elas, abre");
            return;
        }

        // e os degraus da fome: de barriga cheia tira um sétimo, vazia tira metade
        if (DeathsHandItem.degrau(20).quanto() != 0.15f) helper.fail("cheio, um sétimo");
        if (DeathsHandItem.degrau(0).quanto() != 0.5f) helper.fail("e vazio, metade");
        if (DeathsHandItem.degrau(0).fome() <= DeathsHandItem.degrau(20).fome()) {
            helper.fail("e vazio devolve mais fome do que cheio");
        }
        helper.succeed();
    }

    /** <b>E o sexto efeito de fetiche é a senha dela</b>: cinco de cada fantasma, e nenhum espírito. */
    @GameTest(maxTicks = 20)
    public void theSixthFetishEffectIsHerPassword(GameTestHelper helper) {
        if (SpiritEffects.DEATH.spirits != 0) helper.fail("ela não pede espírito nenhum");
        if (SpiritEffects.DEATH.spectres != 5 || SpiritEffects.DEATH.banshees != 5
                || SpiritEffects.DEATH.poltergeists != 5) {
            helper.fail("e pede cinco de cada fantasma");
        }
        if (SpiritEffects.DEATH.id != 6) helper.fail("e é o sexto");
        helper.succeed();
    }
}
