package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaComponents;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.hunter.BoltEntity;
import net.thaumcraft.occulta.hunter.CrossbowPistolItem;
import net.thaumcraft.occulta.hunter.HunterClothes;
import net.thaumcraft.occulta.wolf.Silver;

/**
 * O apetrecho do Caçador: os virotes, a besta que se carrega, e as roupas que são uma armadilha vestida.
 *
 * <p>A prova que carrega a fatia é a do <b>virote de prata</b>. Ele é a única coisa de longe que fere um
 * lobisomem — e até esta fatia o porte dizia, no próprio {@code Silver}, que esse caminho não existia.
 */
public class OccultaHunterGearGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Os números deles são os do original. */
    @GameTest(maxTicks = 20)
    public void theirNumbersAreTheOriginals(GameTestHelper helper) {
        if (CrossbowPistolItem.PARA_TROCAR != 10) helper.fail("dez batidas trocam o virote");
        if (CrossbowPistolItem.GASTA != 2) helper.fail("e cada tiro gasta dois da besta");
        if (CrossbowPistolItem.QUANTOS_PARTEM != 3) helper.fail("o que parte são três");
        if (CrossbowPistolItem.LEQUE != 20.0f) helper.fail("num leque de vinte graus");
        if (BoltEntity.SAGRADO_VALE != 1.5) helper.fail("o sagrado vale uma vez e meia");
        if (HunterClothes.DE_MAGIA != 0.25) helper.fail("a magia escapa uma em quatro");
        if (HunterClothes.DE_MALDIÇÃO != 0.9) helper.fail("e a maldição nove em dez");
        helper.succeed();
    }

    /** Cada tipo de virote sabe o que é, e volta ao chão como o que era. */
    @GameTest(maxTicks = 20)
    public void eachBoltKnowsWhatItIs(GameTestHelper helper) {
        if (!BoltEntity.éDeMadeira(BoltEntity.ESTACA)) helper.fail("a estaca é madeira");
        if (BoltEntity.drena(BoltEntity.ESTACA)) helper.fail("e não drena");

        if (!BoltEntity.drena(BoltEntity.DRENAGEM)) helper.fail("a drenagem drena");
        if (BoltEntity.drenaForte(BoltEntity.DRENAGEM)) helper.fail("mas não com força");
        if (!BoltEntity.éDeMadeira(BoltEntity.DRENAGEM)) helper.fail("e ainda é madeira");

        if (!BoltEntity.drenaForte(BoltEntity.DRENAGEM_FORTE)) helper.fail("a forte drena com força");
        if (!BoltEntity.éSagrado(BoltEntity.SAGRADO)) helper.fail("o sagrado é sagrado");
        if (BoltEntity.éDeMadeira(BoltEntity.SAGRADO)) helper.fail("e não é madeira");
        if (!BoltEntity.éDePrata(BoltEntity.PRATA)) helper.fail("e o de prata é prata");

        // e cada um volta ao chão como o item que era
        if (BoltEntity.munição(BoltEntity.ESTACA) != OccultaItems.STAKE_BOLT) {
            helper.fail("a estaca volta como virote de madeira");
        }
        if (BoltEntity.munição(BoltEntity.DRENAGEM_FORTE) != OccultaItems.ANTI_MAGIC_BOLT) {
            helper.fail("e a drenagem forte volta como anulador: a força era de quem atirou");
        }
        if (BoltEntity.munição(BoltEntity.PRATA) != OccultaItems.SILVER_BOLT) {
            helper.fail("e o de prata, como de prata");
        }
        helper.succeed();
    }

    /** <b>O virote de prata é dano de prata</b>, e nenhum outro é. */
    @GameTest(maxTicks = 40)
    public void theSilverBoltIsSilverDamage(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var lobo = helper.spawn(OccultaEntities.WOLFMAN, new BlockPos(3, 2, 3));

        BoltEntity prata = virote(helper, BoltEntity.PRATA);
        if (!Silver.éDePrata(level.damageSources().arrow(prata, null))) {
            helper.fail("o virote de prata é dano de prata");
        }

        BoltEntity estaca = virote(helper, BoltEntity.ESTACA);
        if (Silver.éDePrata(level.damageSources().arrow(estaca, null))) {
            helper.fail("mas o de madeira não");
        }

        prata.discard();
        estaca.discard();
        lobo.discard();
        helper.succeed();
    }

    /** <b>A drenagem forte limpa</b> — menos os três que são castigo. */
    @GameTest(maxTicks = 60)
    public void thePoweredDrainWipesEverythingButThePunishments(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
        porco.addEffect(new MobEffectInstance(MobEffects.SPEED, 600));
        porco.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600));
        porco.addEffect(new MobEffectInstance(MobEffects.POISON, 600));
        porco.addEffect(new MobEffectInstance(MobEffects.WITHER, 600));
        porco.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 600));

        BoltEntity forte = virote(helper, BoltEntity.DRENAGEM_FORTE);
        forte.setBaseDamage(0.0);
        forte.snapTo(porco.getX(), porco.getY() + 0.5, porco.getZ());
        forte.shoot(0.0, 0.0, 0.1, 0.1f, 0.0f);
        // a prova chama o efeito direto: o voo é do jogo, e não é o que esta fatia muda
        chama(forte, porco);

        if (porco.hasEffect(MobEffects.SPEED)) helper.fail("a rapidez some");
        if (porco.hasEffect(MobEffects.REGENERATION)) helper.fail("e a cura também");
        if (!porco.hasEffect(MobEffects.POISON)) helper.fail("mas o veneno fica");
        if (!porco.hasEffect(MobEffects.WITHER)) helper.fail("e o definhar");
        if (!porco.hasEffect(MobEffects.BLINDNESS)) helper.fail("e a cegueira: tirá-los seria curar");

        forte.discard();
        porco.discard();
        helper.succeed();
    }

    /** E a drenagem fraca não limpa nada. */
    @GameTest(maxTicks = 60)
    public void theWeakDrainWipesNothing(GameTestHelper helper) {
        piso(helper);
        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(5, 2, 5));
        porco.addEffect(new MobEffectInstance(MobEffects.SPEED, 600));

        BoltEntity fraca = virote(helper, BoltEntity.DRENAGEM);
        chama(fraca, porco);
        if (!porco.hasEffect(MobEffects.SPEED)) helper.fail("a drenagem fraca não limpa nada");

        fraca.discard();
        porco.discard();
        helper.succeed();
    }

    /** <b>O conjunto é o que vale</b>, e a peça solta não vale nada. */
    @GameTest(maxTicks = 40)
    public void itIsTheSetThatCounts(GameTestHelper helper) {
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);

        veste(quem, OccultaItems.HUNTER_HAT, OccultaItems.HUNTER_COAT,
                OccultaItems.HUNTER_LEGS, null);
        if (HunterClothes.vestidoInteiro(quem, false)) helper.fail("três peças não são conjunto");

        veste(quem, OccultaItems.HUNTER_HAT, OccultaItems.HUNTER_COAT,
                OccultaItems.HUNTER_LEGS, OccultaItems.HUNTER_BOOTS);
        if (!HunterClothes.vestidoInteiro(quem, false)) helper.fail("as quatro, sim");
        if (HunterClothes.vestidoInteiro(quem, true)) helper.fail("mas as lisas não são prateadas");
        if (!HunterClothes.semBonecas(quem)) helper.fail("e com o conjunto não há boneca nenhuma");

        veste(quem, OccultaItems.SILVERED_HUNTER_HAT, OccultaItems.SILVERED_HUNTER_COAT,
                OccultaItems.SILVERED_HUNTER_LEGS, OccultaItems.SILVERED_HUNTER_BOOTS);
        if (!HunterClothes.protegeDeLobo(quem)) helper.fail("o conjunto prateado protege do lobo");

        despe(quem);
        helper.succeed();
    }

    /** <b>E a da aurora é prateada também</b>: é o {@code (casa, true, true)} do original. */
    @GameTest(maxTicks = 40)
    public void theDawnSetIsSilveredToo(GameTestHelper helper) {
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);

        veste(quem, OccultaItems.GARLICKED_HUNTER_HAT, OccultaItems.GARLICKED_HUNTER_COAT,
                OccultaItems.GARLICKED_HUNTER_LEGS, OccultaItems.GARLICKED_HUNTER_BOOTS);
        if (!HunterClothes.protegeDeLobo(quem)) {
            helper.fail("a roupa da aurora é prateada e com alho: protege dos dois");
        }
        if (!HunterClothes.comAlho(new ItemStack(OccultaItems.GARLICKED_HUNTER_COAT))) {
            helper.fail("e o alho está nela");
        }
        if (HunterClothes.comAlho(new ItemStack(OccultaItems.SILVERED_HUNTER_COAT))) {
            helper.fail("mas não na prateada");
        }

        despe(quem);
        helper.succeed();
    }

    /** A peça certa contra a pancada certa — e só contra ela. */
    @GameTest(maxTicks = 40)
    public void theRightPieceAgainstTheRightBlow(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var lobo = helper.spawn(OccultaEntities.WOLFMAN, new BlockPos(2, 2, 2));
        var vampiro = helper.spawn(OccultaEntities.VAMPIRE, new BlockPos(5, 2, 5));
        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(5, 2, 2));

        ItemStack prateada = new ItemStack(OccultaItems.SILVERED_HUNTER_COAT);
        ItemStack aurora = new ItemStack(OccultaItems.GARLICKED_HUNTER_COAT);
        ItemStack lisa = new ItemStack(OccultaItems.HUNTER_COAT);

        if (!HunterClothes.peçaCerta(prateada, level.damageSources().mobAttack(lobo))) {
            helper.fail("a prateada é a certa contra o lobisomem");
        }
        if (HunterClothes.peçaCerta(prateada, level.damageSources().mobAttack(vampiro))) {
            helper.fail("mas não contra o vampiro");
        }
        if (!HunterClothes.peçaCerta(aurora, level.damageSources().mobAttack(vampiro))) {
            helper.fail("a da aurora é a certa contra o vampiro");
        }
        if (!HunterClothes.peçaCerta(aurora, level.damageSources().mobAttack(lobo))) {
            helper.fail("e contra o lobisomem também, porque é prateada");
        }
        if (HunterClothes.peçaCerta(lisa, level.damageSources().mobAttack(lobo))) {
            helper.fail("e a lisa não é a certa contra nada");
        }
        if (HunterClothes.peçaCerta(prateada, level.damageSources().mobAttack(porco))) {
            helper.fail("nem a prateada contra um porco");
        }

        lobo.discard();
        vampiro.discard();
        porco.discard();
        helper.succeed();
    }

    /** <b>A besta vazia não atira</b>: ela se carrega primeiro. */
    @GameTest(maxTicks = 40)
    public void anEmptyCrossbowDoesNotShoot(GameTestHelper helper) {
        ItemStack besta = new ItemStack(OccultaItems.CROSSBOW_PISTOL);
        if (CrossbowPistolItem.carregado(besta) != null) helper.fail("ela nasce vazia");

        besta.set(OccultaComponents.BOLT_LOADED, OccultaItems.SILVER_BOLT);
        if (CrossbowPistolItem.carregado(besta) != OccultaItems.SILVER_BOLT) {
            helper.fail("e guarda qual virote tem dentro");
        }

        // a roda é a do original, e tem os cinco
        if (CrossbowPistolItem.roda().size() != 5) {
            helper.fail("a roda tem os cinco virotes; tem " + CrossbowPistolItem.roda().size());
        }
        if (!CrossbowPistolItem.roda().contains(OccultaItems.SPLITTING_BOLT)) {
            helper.fail("e o que parte está nela");
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ o que as provas precisam

    private static BoltEntity virote(GameTestHelper helper, int tipo) {
        ServerLevel level = helper.getLevel();
        BoltEntity saiu = new BoltEntity(level, helper.absolutePos(new BlockPos(1, 2, 1)).getX(),
                helper.absolutePos(new BlockPos(1, 2, 1)).getY(),
                helper.absolutePos(new BlockPos(1, 2, 1)).getZ(),
                new ItemStack(BoltEntity.munição(tipo)), tipo);
        level.addFreshEntity(saiu);
        return saiu;
    }

    /** Chama o efeito do virote direto, sem o voo — que é do jogo, e não é o que esta fatia muda. */
    private static void chama(BoltEntity virote, LivingEntity quem) {
        virote.limpa(quem);
    }

    private static void veste(net.minecraft.world.entity.player.Player quem, Item chapéu, Item casaco,
                              Item calças, Item botas) {
        quem.setItemSlot(EquipmentSlot.HEAD, chapéu == null ? ItemStack.EMPTY : new ItemStack(chapéu));
        quem.setItemSlot(EquipmentSlot.CHEST, casaco == null ? ItemStack.EMPTY : new ItemStack(casaco));
        quem.setItemSlot(EquipmentSlot.LEGS, calças == null ? ItemStack.EMPTY : new ItemStack(calças));
        quem.setItemSlot(EquipmentSlot.FEET, botas == null ? ItemStack.EMPTY : new ItemStack(botas));
    }

    private static void despe(net.minecraft.world.entity.player.Player quem) {
        veste(quem, null, null, null, null);
    }
}
