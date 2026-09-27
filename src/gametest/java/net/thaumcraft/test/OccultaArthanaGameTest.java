package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.thaumcraft.occulta.OccultaDrops;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.rite.RiteRegistry;

import java.util.List;

/**
 * A Arthana, o que ela abre nos bichos, e a pedra que sai do Rito de Necromancia.
 */
public class OccultaArthanaGameTest {
    /** A faca é de ouro com a vida do ferro: é o que o original faz, e não é acaso. */
    @GameTest
    public void theKnifeIsGoldWithIronLife(GameTestHelper helper) {
        var material = net.thaumcraft.occulta.ArthanaItem.MATERIAL;
        if (material.durability() != net.minecraft.world.item.ToolMaterial.IRON.durability()) {
            helper.fail("ela aguenta o que o ferro aguenta, e aguenta " + material.durability());
        }
        var faca = new ItemStack(OccultaItems.ARTHANA);
        if (faca.getMaxDamage() != net.minecraft.world.item.ToolMaterial.IRON.durability()) {
            helper.fail("e a vida dela na mão é a mesma, e é " + faca.getMaxDamage());
        }
        helper.succeed();
    }

    /** As duas contas da queda: a pequena sem a faca e a grande com ela, números do original. */
    @GameTest
    public void theKnifeRaisesEveryChance(GameTestHelper helper) {
        record Par(String bicho, net.minecraft.world.item.Item deixa, float sem, float com) {
        }
        List<Par> pares = List.of(
                new Par("wolf", OccultaItems.DOG_TONGUE, OccultaDrops.TONGUE, OccultaDrops.TONGUE_ARTHANA),
                new Par("creeper", OccultaItems.CREEPER_HEART, OccultaDrops.HEART, OccultaDrops.HEART_ARTHANA),
                new Par("frog", OccultaItems.TOE_OF_FROG, OccultaDrops.TOE, OccultaDrops.TOE_ARTHANA),
                new Par("bat", OccultaItems.BAT_WOOL, OccultaDrops.BAT_WOOL, OccultaDrops.BAT_WOOL_ARTHANA));

        for (Par par : pares) {
            var sem = OccultaDrops.chance(par.bicho(), par.deixa(), false);
            var com = OccultaDrops.chance(par.bicho(), par.deixa(), true);
            if (sem.isEmpty() || com.isEmpty()) {
                helper.fail("falta uma das duas contas de " + par.bicho());
                return;
            }
            if (sem.get() != par.sem() || com.get() != par.com()) {
                helper.fail(par.bicho() + " devia dar " + par.sem() + " e " + par.com()
                        + ", e dá " + sem.get() + " e " + com.get());
                return;
            }
            if (com.get() <= sem.get()) helper.fail("e a faca sobe a conta de " + par.bicho());
        }
        helper.succeed();
    }

    /** O Pó Espectral e as caveiras só existem com a faca: sem ela não há conta nenhuma. */
    @GameTest
    public void whatOnlyTheKnifeOpens(GameTestHelper helper) {
        record Só(String bicho, net.minecraft.world.item.Item deixa) {
        }
        List<Só> sós = List.of(
                new Só("skeleton", OccultaItems.SPECTRAL_DUST),
                new Só("zombie", OccultaItems.SPECTRAL_DUST),
                new Só("skeleton", Items.SKELETON_SKULL),
                new Só("zombie", Items.ZOMBIE_HEAD),
                new Só("creeper", Items.CREEPER_HEAD));
        for (Só qual : sós) {
            if (OccultaDrops.chance(qual.bicho(), qual.deixa(), true).isEmpty()) {
                helper.fail("com a faca, o " + qual.bicho() + " devia dar " + qual.deixa());
                return;
            }
            if (OccultaDrops.chance(qual.bicho(), qual.deixa(), false).isPresent()) {
                helper.fail("e sem ela, não: " + qual.bicho() + " e " + qual.deixa());
                return;
            }
        }
        helper.succeed();
    }

    /** E a tabela do jogo obedece: um esqueleto morto com a faca pode largar pó, sem ela nunca. */
    @GameTest
    public void theLootTableAnswersToTheKnife(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(1, 2, 1));
        var esqueleto = helper.spawn(net.minecraft.world.entity.EntityTypes.SKELETON, new BlockPos(1, 2, 1));
        var quem = helper.makeMockServerPlayerInLevel();

        boolean achouComFaca = false;
        quem.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(OccultaItems.ARTHANA));
        for (int i = 0; i < 400 && !achouComFaca; i++) {
            achouComFaca = larga(level, onde, esqueleto, quem).stream()
                    .anyMatch(c -> c.is(OccultaItems.SPECTRAL_DUST));
        }
        if (!achouComFaca) helper.fail("em quatrocentas mortes com a faca, devia ter saído pó uma vez");

        quem.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
        for (int i = 0; i < 400; i++) {
            if (larga(level, onde, esqueleto, quem).stream().anyMatch(c -> c.is(OccultaItems.SPECTRAL_DUST))) {
                helper.fail("e sem a faca não sai nenhuma");
                return;
            }
        }
        esqueleto.discard();
        helper.succeed();
    }

    /** O que aquele bicho larga, morto por aquela pessoa. */
    private static List<ItemStack> larga(ServerLevel level, BlockPos onde,
                                         net.minecraft.world.entity.LivingEntity bicho,
                                         net.minecraft.server.level.ServerPlayer quem) {
        var fonte = level.damageSources().playerAttack(quem);
        var params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, bicho)
                .withParameter(LootContextParams.ORIGIN, net.minecraft.world.phys.Vec3.atCenterOf(onde))
                .withParameter(LootContextParams.DAMAGE_SOURCE, fonte)
                .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, quem)
                .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, quem)
                .withOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER, quem)
                .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.ENTITY);
        var chave = bicho.getType().getDefaultLootTable().orElseThrow();
        var tabela = level.getServer().reloadableRegistries().getLootTable(chave);
        return tabela.getRandomItems(params);
    }

    /** O Rito de Necromancia existe, corre de noite e pede o Pó Espectral. */
    @GameTest
    public void theRiteOfNecromancyIsThere(GameTestHelper helper) {
        var rito = RiteRegistry.all().stream()
                .filter(r -> r.key().equals("tc.rite.necrostone"))
                .findFirst().orElse(null);
        if (rito == null) {
            helper.fail("o Rito de Necromancia devia estar na lista");
            return;
        }
        if (!rito.when().contains(RiteRegistry.When.NIGHT)) helper.fail("e só correr de noite");

        var pede = net.thaumcraft.occulta.rite.Rites.shown(rito);
        if (pede.stream().noneMatch(c -> c.is(OccultaItems.SPECTRAL_DUST))) {
            helper.fail("e pedir o Pó Espectral");
        }
        if (pede.stream().noneMatch(c -> c.is(OccultaItems.ATTUNED_STONE))) {
            helper.fail("e a Pedra Sintonizada");
        }
        helper.succeed();
    }
}
