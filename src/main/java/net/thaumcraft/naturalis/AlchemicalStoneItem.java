package net.thaumcraft.naturalis;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.thaumcraft.registry.TCSounds;

import java.util.List;

/**
 * A Pedra do Catalisador Fenomorfo: o lado do {@code AlchemicalStoneItem} que muda blocos, no Magia Naturalis
 * 0.5.0.
 *
 * <p>Clicada num bloco, ela o troca pelo próximo da família dele — a lã vira a lã da cor seguinte, o tijolo de
 * pedra vira o musgoso, a tábua vira a de outra madeira. Agachado, em vez de trocar, ela deita ou levanta o bloco,
 * que é o que o original faz com os troncos.
 */
public class AlchemicalStoneItem extends Item {
    /** As famílias que a pedra sabe percorrer, na ordem de cada uma. */
    private static final List<List<Block>> FAMILIES = List.of(
            Blocks.WOOL.asList(),
            Blocks.CARPET.asList(),
            net.minecraft.world.level.block.Blocks.DYED_TERRACOTTA.asList(),
            net.minecraft.world.level.block.Blocks.STAINED_GLASS.asList(),
            List.of(Blocks.OAK_PLANKS, Blocks.SPRUCE_PLANKS, Blocks.BIRCH_PLANKS, Blocks.JUNGLE_PLANKS, Blocks.ACACIA_PLANKS,
                    Blocks.DARK_OAK_PLANKS),
            List.of(Blocks.STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS, Blocks.CHISELED_STONE_BRICKS),
            List.of(Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE),
            List.of(Blocks.SANDSTONE, Blocks.CHISELED_SANDSTONE, Blocks.CUT_SANDSTONE),
            List.of(Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL, Blocks.GRASS_BLOCK),
            List.of(Blocks.QUARTZ_BLOCK, Blocks.CHISELED_QUARTZ_BLOCK, Blocks.QUARTZ_PILLAR));

    public AlchemicalStoneItem(Properties properties) {
        super(properties);
    }

    /** A pedra sabe mexer neste bloco? */
    public static boolean morphs(BlockState state) {
        if (state.hasProperty(BlockStateProperties.AXIS)) return true;
        return family(state.getBlock()) != null;
    }

    private static List<Block> family(Block block) {
        for (List<Block> family : FAMILIES) {
            if (family.contains(block)) return family;
        }
        return null;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        BlockState state = level.getBlockState(pos);
        if (player == null) return InteractionResult.PASS;

        BlockState novo = null;
        // agachado, o bloco deita ou levanta — é o que o original faz com os troncos
        if (player.isShiftKeyDown() && state.hasProperty(BlockStateProperties.AXIS)) {
            novo = RotatedPillarBlock.rotatePillar(state, net.minecraft.world.level.block.Rotation.CLOCKWISE_90);
            if (novo == state) {
                novo = state.setValue(BlockStateProperties.AXIS,
                        switch (state.getValue(BlockStateProperties.AXIS)) {
                            case X -> net.minecraft.core.Direction.Axis.Y;
                            case Y -> net.minecraft.core.Direction.Axis.Z;
                            case Z -> net.minecraft.core.Direction.Axis.X;
                        });
            }
        } else {
            List<Block> family = family(state.getBlock());
            if (family != null) {
                Block next = family.get((family.indexOf(state.getBlock()) + 1) % family.size());
                novo = next.withPropertiesOf(state);
            }
        }
        if (novo == null || novo == state) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            level.setBlockAndUpdate(pos, novo);
            level.playSound(null, pos, TCSounds.ZAP.value(), SoundSource.BLOCKS, 0.5f, 1.0f);
        }
        return InteractionResult.SUCCESS;
    }
}
