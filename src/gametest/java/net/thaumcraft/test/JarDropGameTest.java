package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.block.entity.JarBlockEntity;
import net.thaumcraft.item.JarContents;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCComponents;

import java.util.List;

/**
 * O jarro quebrado leva a essência com ele. É o que faz dar para carregar essência de um lugar a outro.
 *
 * <p>No original o jarro cheio cai como {@code ItemJarFilled}, com o aspecto, a quantidade e o rótulo, e
 * não empilha; o jarro vazio e sem rótulo cai como jarro comum.
 */
public class JarDropGameTest {
    /** Quebrou, caiu cheio: mesmo aspecto, mesma quantidade, mesmo rótulo, pilha de um. */
    @GameTest
    public void aBrokenJarKeepsItsEssentia(GameTestHelper helper) {
        BlockPos at = new BlockPos(1, 1, 1);
        helper.setBlock(at, TCBlocks.JAR);
        JarBlockEntity jar = helper.getBlockEntity(at, JarBlockEntity.class);
        jar.setLabel(Aspects.AIR);
        if (jar.addToContainer(Aspects.AIR, 40) != 0) helper.fail("o jarro devia aceitar quarenta de aer");

        helper.getLevel().destroyBlock(helper.absolutePos(at), true);

        ItemStack dropped = dropped(helper, at);
        if (dropped == null) {
            helper.fail("o jarro quebrado não caiu");
            return;
        }
        JarContents contents = dropped.get(TCComponents.JAR_CONTENTS);
        if (contents == null) {
            helper.fail("o jarro caiu vazio: a essência se perdeu");
            return;
        }
        if (contents.heldAspect() != Aspects.AIR) helper.fail("caiu com " + contents.aspect() + ", devia ser aer");
        if (contents.amount() != 40) helper.fail("caiu com " + contents.amount() + ", devia ser quarenta");
        if (contents.labelAspect() != Aspects.AIR) helper.fail("o rótulo se perdeu");
        if (dropped.getOrDefault(DataComponents.MAX_STACK_SIZE, 64) != 1) helper.fail("jarro cheio não empilha");
        helper.succeed();
    }

    /** Pôr de volta devolve tudo como estava. */
    @GameTest
    public void placingItBackRestoresIt(GameTestHelper helper) {
        ItemStack carried = new ItemStack(TCBlocks.JAR);
        carried.set(TCComponents.JAR_CONTENTS, JarContents.of(Aspects.FIRE, 64, null));

        BlockPos at = new BlockPos(1, 1, 1);
        helper.setBlock(at, TCBlocks.JAR);
        JarBlockEntity jar = helper.getBlockEntity(at, JarBlockEntity.class);
        jar.applyComponentsFromItemStack(carried);

        if (jar.aspect() != Aspects.FIRE) helper.fail("o jarro posto devia ter ignis dentro");
        if (jar.amount() != 64) helper.fail("o jarro posto devia ter sessenta e quatro, tem " + jar.amount());
        helper.succeed();
    }

    /** Jarro vazio e sem rótulo cai como jarro comum, que empilha. */
    @GameTest
    public void anEmptyJarIsJustAJar(GameTestHelper helper) {
        BlockPos at = new BlockPos(1, 1, 1);
        helper.setBlock(at, TCBlocks.JAR);
        helper.getLevel().destroyBlock(helper.absolutePos(at), true);

        ItemStack dropped = dropped(helper, at);
        if (dropped == null) {
            helper.fail("o jarro vazio não caiu");
            return;
        }
        if (dropped.has(TCComponents.JAR_CONTENTS)) helper.fail("jarro vazio não leva conteúdo");
        if (dropped.getMaxStackSize() == 1) helper.fail("jarro vazio empilha");
        helper.succeed();
    }

    private static ItemStack dropped(GameTestHelper helper, BlockPos at) {
        List<ItemEntity> items = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                new AABB(helper.absolutePos(at)).inflate(2.0));
        return items.isEmpty() ? null : items.get(0).getItem();
    }
}
