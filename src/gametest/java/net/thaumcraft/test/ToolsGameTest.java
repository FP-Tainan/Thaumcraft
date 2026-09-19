package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.entity.PrimalArrowEntity;
import net.thaumcraft.item.BoneBowItem;
import net.thaumcraft.item.PrimalArrowItem;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.research.WarpEvents;

/** As ferramentas soltas da 4.2.3.5: as elementais, o arco de osso e as flechas, a lâmina carmesim e o metal do vazio. */
public class ToolsGameTest {
    /** A pá elemental cava três por três na face que se olha, e o que cai vai atrás de quem cavou. */
    @GameTest(maxTicks = 60)
    public void theElementalShovelDigsThreeByThree(GameTestHelper helper) {
        for (int x = 0; x < 5; x++) for (int z = 0; z < 5; z++) helper.setBlock(new BlockPos(x, 1, z), Blocks.DIRT);
        var player = helper.makeMockServerPlayerInLevel();
        BlockPos centre = helper.absolutePos(new BlockPos(2, 1, 2));
        player.snapTo(centre.getX() + 0.5, centre.getY() + 2.0, centre.getZ() + 0.5, 0.0f, 90.0f);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TCItems.ELEMENTAL_SHOVEL));
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        player.gameMode.destroyBlock(centre);
        helper.succeedWhen(() -> {
            for (int x = 1; x <= 3; x++) for (int z = 1; z <= 3; z++) helper.assertBlockPresent(Blocks.AIR, new BlockPos(x, 1, z));
            helper.assertBlockPresent(Blocks.DIRT, new BlockPos(0, 1, 2));
        });
    }

    /** A enxada elemental ara três por três. */
    @GameTest
    public void theElementalHoeTillsThreeByThree(GameTestHelper helper) {
        for (int x = 0; x < 5; x++) for (int z = 0; z < 5; z++) helper.setBlock(new BlockPos(x, 1, z), Blocks.DIRT);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack hoe = new ItemStack(TCItems.ELEMENTAL_HOE);
        player.setItemInHand(InteractionHand.MAIN_HAND, hoe);
        BlockPos centre = helper.absolutePos(new BlockPos(2, 1, 2));
        hoe.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(centre).add(0, 0.5, 0), Direction.UP, centre, false)));
        for (int x = 1; x <= 3; x++) for (int z = 1; z <= 3; z++) helper.assertBlockPresent(Blocks.FARMLAND, new BlockPos(x, 1, z));
        helper.succeed();
    }

    /** O arco de osso arma em dez tiques; as flechas primordiais têm o tipo e o dano de cada primário. */
    @GameTest
    public void boneBowAndPrimalArrows(GameTestHelper helper) {
        if (BoneBowItem.power(10) != 1.0f) helper.fail("o arco de osso arma em dez tiques");
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var earth = (PrimalArrowItem) TCItems.PRIMAL_ARROWS.get("earth");
        var arrow = (PrimalArrowEntity) earth.createArrow(helper.getLevel(), new ItemStack(earth), player, new ItemStack(TCItems.BONE_BOW));
        if (arrow.arrowType() != 3) helper.fail("a flecha de terra é do tipo 3");
        if (Math.abs(((net.thaumcraft.mixin.AbstractArrowAccessor) arrow).thaumcraft$baseDamage() - 2.1 * 1.5) > 1.0e-6) helper.fail("a de terra fere uma vez e meia");
        if (arrow.pickup != net.minecraft.world.entity.projectile.arrow.AbstractArrow.Pickup.DISALLOWED) helper.fail("a flecha primordial não se recolhe");
        helper.succeed();
    }

    /** A lâmina carmesim enfraquece e dá fome; distorce dois. */
    @GameTest
    public void theCrimsonBladeSaps(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var zombie = helper.spawn(net.minecraft.world.entity.EntityTypes.ZOMBIE, new BlockPos(2, 2, 2));
        ItemStack blade = new ItemStack(TCItems.CRIMSON_SWORD);
        blade.getItem().hurtEnemy(blade, zombie, player);
        if (!zombie.hasEffect(MobEffects.WEAKNESS) || !zombie.hasEffect(MobEffects.HUNGER)) helper.fail("a lâmina carmesim enfraquece e dá fome");
        if (WarpEvents.finalWarp(blade, player) != 2) helper.fail("a lâmina carmesim distorce dois");
        helper.succeed();
    }

    /** O metal do vazio se conserta sozinho, um ponto por segundo. */
    @GameTest(maxTicks = 60)
    public void voidGearRepairsItself(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        ItemStack pick = new ItemStack(TCItems.GEAR.get("void_pickaxe"));
        pick.setDamageValue(10);
        player.setItemSlot(EquipmentSlot.MAINHAND, pick);
        if (WarpEvents.finalWarp(pick, player) != 1) helper.fail("a picareta do vazio distorce um");
        // o tique do inventário, de segundo em segundo (o jogador de mentira não anda sozinho)
        player.tickCount = 20;
        pick.getItem().inventoryTick(pick, helper.getLevel(), player, EquipmentSlot.MAINHAND);
        if (pick.getDamageValue() != 9) helper.fail("a picareta devia se consertar um ponto, está " + pick.getDamageValue());
        helper.succeed();
    }
}
