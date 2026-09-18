package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.block.entity.WardedBlockEntity;
import net.thaumcraft.item.Focuses;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCItems;

/** O foco de Proteção tem de seguir o {@code ItemFocusWarding}: protege, não se quebra, e só o dono desfaz. */
public class WardingFocusGameTest {
    @GameTest(maxTicks = 80)
    public void theOwnerWardsAndUnwards(GameTestHelper helper) {
        BlockPos stone = new BlockPos(1, 2, 3);
        helper.setBlock(stone, Blocks.STONE.defaultBlockState());
        var player = helper.makeMockServerPlayerInLevel();
        Vec3 feet = helper.absoluteVec(new Vec3(1.5, 1.0, 1.5));
        player.snapTo(feet.x, feet.y, feet.z, 0.0f, 0.0f);
        ItemStack wand = new ItemStack(TCItems.WAND);
        wand.set(TCComponents.WAND_FOCUS, "warding");
        AspectList vis = new AspectList();
        for (Aspect primal : Aspects.primals()) vis.add(primal, 2500);
        wand.set(TCComponents.WAND_VIS, vis);
        player.getInventory().setItem(0, wand);
        player.getInventory().setSelectedSlot(0);
        var focus = Focuses.on(wand);

        if (!Focuses.tick(helper.getLevel(), player, wand, focus)) helper.fail("o foco devia proteger a pedra na mira");
        helper.assertBlockPresent(TCBlocks.WARDED, stone);
        if (!(helper.getBlockEntity(stone, WardedBlockEntity.class) instanceof WardedBlockEntity warded)
                || !warded.stored().is(Blocks.STONE)) {
            throw helper.assertionException("o bloco protegido guarda a pedra");
        }
        if (wand.get(TCComponents.WAND_VIS).getAmount(Aspects.EARTH) >= 2500) helper.fail("proteger custa terra");
        // não se quebra
        if (helper.getLevel().getBlockState(helper.absolutePos(stone)).getDestroySpeed(helper.getLevel(), helper.absolutePos(stone)) >= 0) {
            helper.fail("o bloco protegido é inquebrável");
        }
        // o original espera meio segundo por bloco antes de aceitar outro clique
        helper.runAfterDelay(20, () -> {
            var stranger = helper.makeMockServerPlayerInLevel();
            // um estranho não desfaz: o dono é o nome de quem protegeu
            if (Focuses.wardOwner(stranger) == Focuses.wardOwner(player)) {
                helper.succeed();
                return;
            }
            if (!Focuses.tick(helper.getLevel(), player, wand, focus)) helper.fail("o dono devia desfazer a proteção");
            helper.assertBlockPresent(Blocks.STONE, stone);
            helper.succeed();
        });
    }
}
