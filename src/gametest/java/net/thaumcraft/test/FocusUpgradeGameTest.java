package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.crafting.SpecialMining;
import net.thaumcraft.entity.ShockOrbEntity;
import net.thaumcraft.item.FocusItem;
import net.thaumcraft.item.FocusUpgradeTable;
import net.thaumcraft.item.WandItem;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCItems;

/**
 * As melhorias de foco da 4.2.3.5: os postos, as regras de cada foco, o que muda no custo, na espera e no jeito de
 * disparar, e o que vai junto quando o foco entra e sai da varinha.
 */
public class FocusUpgradeGameTest {
    private static ItemStack focus(String type) {
        return new ItemStack(TCItems.FOCI.get(type));
    }

    private static FocusItem item(String type) {
        return (FocusItem) TCItems.FOCI.get(type);
    }

    @GameTest
    public void theTableIsTheOriginals(GameTestHelper helper) {
        if (FocusUpgradeTable.BY_ID.size() != 21) helper.fail("o original tem 21 melhorias, há " + FocusUpgradeTable.BY_ID.size());
        // o getPossibleUpgradesByRank do ItemFocusFire: a bola de fogo e o jato entram no terceiro posto
        if (!item("fire").possibleByRank(focus("fire"), 3).contains(FocusUpgradeTable.FIREBALL)) helper.fail("bola de fogo no terceiro posto");
        if (item("fire").possibleByRank(focus("fire"), 1).contains(FocusUpgradeTable.FIREBALL)) helper.fail("e só nele");
        // a mineração especial: os dez pares do Config
        if (SpecialMining.size() < 10) helper.fail("a mineração especial tem ao menos os dez pares do original");
        helper.succeed();
    }

    @GameTest
    public void aSlotTakesOneUpgrade(GameTestHelper helper) {
        ItemStack fire = focus("fire");
        if (!FocusItem.apply(fire, FocusUpgradeTable.POTENCY, 1)) helper.fail("o primeiro posto aceita");
        if (FocusItem.apply(fire, FocusUpgradeTable.FRUGAL, 1)) helper.fail("mas só uma vez");
        FocusItem.apply(fire, FocusUpgradeTable.POTENCY, 2);
        if (FocusItem.level(fire, FocusUpgradeTable.POTENCY) != 2) helper.fail("duas potências são nível 2");
        helper.succeed();
    }

    @GameTest
    public void theFireballChangesTheFireFocus(GameTestHelper helper) {
        ItemStack fire = focus("fire");
        FocusItem f = item("fire");
        if (!f.isContinuous(fire) || f.cooldown(fire) != 0) helper.fail("sem melhoria, o fogo é jato sem espera");
        FocusItem.apply(fire, FocusUpgradeTable.FIREBALL, 3);
        AspectList cost = f.cost(fire);
        // costBall: FIRE 66, ENTROPY 33
        if (cost.getAmount(Aspects.FIRE) != 66 || cost.getAmount(Aspects.ENTROPY) != 33) helper.fail("a bola custa 66 de fogo e 33 de entropia");
        if (f.isContinuous(fire)) helper.fail("a bola é tiro único");
        if (f.cooldown(fire) != 1000) helper.fail("com um segundo de espera");
        // o alquímico só uma vez na bola
        if (!f.canApply(fire, null, FocusUpgradeTable.ALCHEMISTSFIRE, 4)) helper.fail("o primeiro fogo alquímico entra");
        FocusItem.apply(fire, FocusUpgradeTable.ALCHEMISTSFIRE, 4);
        if (f.canApply(fire, null, FocusUpgradeTable.ALCHEMISTSFIRE, 5)) helper.fail("o segundo, na bola, não");
        helper.succeed();
    }

    @GameTest
    public void theShockFocusOnlyEnlargesWhatSpreads(GameTestHelper helper) {
        ItemStack shock = focus("shock");
        FocusItem f = item("shock");
        if (f.canApply(shock, null, FocusUpgradeTable.ENLARGE, 4)) helper.fail("o raio simples não amplia");
        FocusItem.apply(shock, FocusUpgradeTable.EARTHSHOCK, 3);
        if (!f.canApply(shock, null, FocusUpgradeTable.ENLARGE, 4)) helper.fail("o choque de terra amplia");
        AspectList cost = f.cost(shock);
        if (cost.getAmount(Aspects.AIR) != 75 || cost.getAmount(Aspects.EARTH) != 25) helper.fail("o choque custa 75 de ar e 25 de terra");
        if (f.isContinuous(shock) || f.cooldown(shock) != 1000) helper.fail("tiro único, um segundo de espera");
        helper.succeed();
    }

    @GameTest
    public void frugalDiscountsTheFocus(GameTestHelper helper) {
        ItemStack wand = new ItemStack(TCItems.WAND);
        wand.set(TCComponents.WAND_ROD, "greatwood");
        wand.set(TCComponents.WAND_CAP, "gold");
        wand.set(TCComponents.WAND_FOCUS, "fire");
        float before = WandItem.focusModifier(wand, null, Aspects.FIRE);
        wand.set(TCComponents.FOCUS_UPGRADES, java.util.List.of(FocusUpgradeTable.FRUGAL.id(), FocusUpgradeTable.FRUGAL.id(),
                (short) -1, (short) -1, (short) -1));
        if (WandItem.focusFrugal(wand) != 2) helper.fail("duas frugais");
        if (Math.abs(before - WandItem.focusModifier(wand, null, Aspects.FIRE) - 0.2f) > 0.001f) helper.fail("dão vinte por cento");
        helper.succeed();
    }

    @GameTest
    public void upgradesTravelWithTheFocus(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wand = new ItemStack(TCItems.WAND);
        wand.set(TCComponents.WAND_ROD, "wood");
        wand.set(TCComponents.WAND_CAP, "iron");
        player.getInventory().setItem(0, wand);
        player.getInventory().setSelectedSlot(0);
        ItemStack fire = focus("fire");
        FocusItem.apply(fire, FocusUpgradeTable.POTENCY, 1);
        player.getInventory().setItem(5, fire);
        net.thaumcraft.item.FocusSwap.change(wand, player, "");
        if (WandItem.focusPotency(wand) != 1) helper.fail("a potência entra com o foco na varinha");
        net.thaumcraft.item.FocusSwap.change(wand, player, net.thaumcraft.item.FocusSwap.REMOVE);
        if (wand.has(TCComponents.FOCUS_UPGRADES)) helper.fail("e sai com ele");
        boolean back = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (s.is(TCItems.FOCI.get("fire")) && FocusItem.level(s, FocusUpgradeTable.POTENCY) == 1) back = true;
        }
        if (!back) helper.fail("o foco volta com a melhoria");
        helper.succeed();
    }

    /** O EntityShockOrb: bate no chão, fere quem vê em volta e deixa o chão eletrificado. */
    @GameTest(maxTicks = 100)
    public void theEarthShockElectrifiesTheGround(GameTestHelper helper) {
        for (int x = 0; x < 7; x++) for (int z = 0; z < 7; z++) helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE.defaultBlockState());
        var pig = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 5));
        pig.setNoAi(true);
        float health = pig.getHealth();
        ShockOrbEntity orb = new ShockOrbEntity(TCEntities.SHOCK_ORB, helper.getLevel());
        Vec3 at = helper.absoluteVec(new Vec3(3.5, 3.5, 3.5));
        orb.setPos(at.x, at.y, at.z);
        orb.setDeltaMovement(0.0, -0.3, 0.0);
        helper.getLevel().addFreshEntity(orb);
        helper.succeedWhen(() -> {
            if (orb.isAlive()) helper.fail("o orbe ainda não bateu");
            if (pig.getHealth() >= health) helper.fail("o porco em volta leva o choque");
            boolean field = false;
            for (int x = 0; x < 7; x++) for (int z = 0; z < 7; z++) {
                if (helper.getBlockState(new BlockPos(x, 2, z)).is(TCBlocks.SPARK_FIELD)) field = true;
            }
            if (!field) helper.fail("o chão fica eletrificado");
        });
    }

    @GameTest(maxTicks = 60)
    public void theStaticFieldHurts(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.STONE.defaultBlockState());
        helper.setBlock(new BlockPos(1, 2, 1), TCBlocks.SPARK_FIELD.defaultBlockState());
        var pig = helper.spawn(EntityTypes.PIG, new BlockPos(1, 2, 1));
        pig.setNoAi(true);
        float health = pig.getHealth();
        helper.succeedWhen(() -> {
            if (pig.getHealth() >= health) helper.fail("o campo estático fere quem passa");
        });
    }

    @GameTest
    public void dowsingFindsNativeClusters(GameTestHelper helper) {
        var random = helper.getLevel().getRandom();
        ItemStack out = SpecialMining.refine(new ItemStack(Items.IRON_ORE), 1000.0f, random);
        if (!out.is(net.thaumcraft.registry.TCResources.get("native_iron_cluster"))) helper.fail("minério de ferro vira aglomerado nativo de ferro, deu " + out);
        if (!SpecialMining.refine(new ItemStack(Items.STONE), 1000.0f, random).is(Items.STONE)) helper.fail("pedra continua pedra");
        helper.succeed();
    }
}
