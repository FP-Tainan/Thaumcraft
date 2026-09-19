package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.api.wands.Wandable;
import net.thaumcraft.item.JarredNode;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCComponents;

/**
 * O nó no jarro: o {@code TileJarNode} da 4.2.3.5. Guarda o nó como estava — aspectos, tipo e feitio — sem se refazer
 * nem fazer nada; a varinha quebra o vidro e o solta onde está. Quebrado com a mão, o jarro sai com o nó dentro.
 */
public class NodeJarBlockEntity extends NodeBlockEntity implements Wandable {
    public NodeJarBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.NODE_JAR, pos, state);
    }

    /** O {@code onWandRightClick}: o jarro se quebra e o nó volta a ser nó, com o que tinha agora como teto. */
    @Override
    public boolean onWand(Level level, ItemStack wand, Player player, BlockPos pos, Direction face) {
        if (!level.isClientSide()) {
            var aspects = this.aspects().copy();
            var type = this.type();
            var modifier = this.modifier();
            level.setBlock(pos, TCBlocks.NODE.defaultBlockState(), Block.UPDATE_ALL);
            if (level.getBlockEntity(pos) instanceof NodeBlockEntity node) node.setup(aspects, type, modifier);
            level.levelEvent(2001, pos, Block.getId(TCBlocks.NODE_JAR.defaultBlockState()));
            level.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.0f, 0.9f + level.getRandom().nextFloat() * 0.2f);
        }
        player.swing(InteractionHand.MAIN_HAND);
        return true;
    }

    public JarredNode jarred() {
        return JarredNode.of(this.aspects(), this.type(), this.modifier(), "");
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(TCComponents.JARRED_NODE, this.jarred());
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        JarredNode node = components.get(TCComponents.JARRED_NODE);
        if (node != null) this.setup(node.aspects(), node.nodeType(), node.nodeModifier());
    }

    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        super.removeComponentsFromTag(output);
    }
}
