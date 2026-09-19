package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.block.entity.CrucibleBlockEntity;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.research.Knowledges;
import net.thaumcraft.research.PlayerKnowledge;

/**
 * O caminho de quem joga no caldeirão: com a essência dentro e o item jogado pela mão de quem sabe a pesquisa, tem de
 * sair o que a receita manda (o Nitor) — e não só dissolver.
 */
public class CrucibleCraftGameTest {
    @GameTest
    public void thrownGlowstoneBecomesNitor(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos, TCBlocks.CRUCIBLE.defaultBlockState());
        CrucibleBlockEntity crucible = helper.getBlockEntity(pos, CrucibleBlockEntity.class);
        crucible.setWater(true);
        crucible.aspects().add(Aspects.ENERGY, 5).add(Aspects.FIRE, 5).add(Aspects.LIGHT, 5);

        var player = helper.makeMockServerPlayerInLevel();
        PlayerKnowledge knowledge = Knowledges.of(player);
        knowledge.completeResearch("NITOR");
        Knowledges.save(player, knowledge);

        Vec3 at = helper.absoluteVec(new Vec3(1.5, 3.0, 1.5));
        ItemEntity thrown = new ItemEntity(helper.getLevel(), at.x, at.y, at.z, new ItemStack(Items.GLOWSTONE_DUST));
        thrown.setThrower(player);
        helper.getLevel().addFreshEntity(thrown);
        crucible.attemptSmelt(thrown);

        boolean nitor = helper.getLevel().getEntitiesOfClass(ItemEntity.class, helper.getBounds().inflate(4.0))
                .stream().anyMatch(e -> e.getItem().is(TCItems.NITOR));
        if (!nitor) helper.fail("o caldeirão devia ter devolvido o Nitor; dentro ficou " + crucible.aspects());
        helper.succeed();
    }
}
