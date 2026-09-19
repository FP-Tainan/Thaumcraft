package net.thaumcraft.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.registry.TCComponents;

import java.util.ArrayList;
import java.util.List;

/**
 * A pá do núcleo de terra: o {@code ItemElementalShovel} da 4.2.3.5. Cava três por três na face que se olha (o que cai
 * voa até quem cavou) e, usada num bloco, põe mais nove iguais a ele encostados na face, gastando do inventário — de
 * frente, deitados ou de lado, conforme a tecla G ({@code or}); agachado, mostra onde eles vão.
 */
public class ElementalShovelItem extends Item {
    public ElementalShovelItem(Properties properties) {
        super(properties);
    }

    public static int getOrientation(ItemStack stack) {
        Integer o = stack.get(TCComponents.SHOVEL_ORIENTATION);
        return o == null ? 0 : o;
    }

    public static void setOrientation(ItemStack stack, int o) {
        stack.set(TCComponents.SHOVEL_ORIENTATION, Math.floorMod(o, 3));
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        if (level instanceof ServerLevel server && state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            AreaMining.mine(stack, server, pos, miner, s -> s.is(BlockTags.MINEABLE_WITH_SHOVEL), 3);
        }
        return super.mineBlock(stack, level, state, pos, miner);
    }

    /** As nove casas encostadas na face, segundo a orientação guardada na pá. */
    private static List<BlockPos> spots(ItemStack stack, Level level, BlockPos pos, int side, Player player) {
        List<BlockPos> list = new ArrayList<>();
        Direction dir = Direction.from3DDataValue(side);
        int o = getOrientation(stack);
        for (int aa = -1; aa <= 1; aa++) {
            for (int bb = -1; bb <= 1; bb++) {
                int xx = 0, yy = 0, zz = 0;
                if (o == 1) {
                    yy = bb;
                    if (side <= 1) {
                        int l = Mth.floor(player.getYRot() * 4.0f / 360.0f + 0.5) & 3;
                        if (l != 0 && l != 2) zz = aa;
                        else xx = aa;
                    } else if (side <= 3) {
                        zz = aa;
                    } else {
                        xx = aa;
                    }
                } else if (o == 2) {
                    if (side <= 1) {
                        int l = Mth.floor(player.getYRot() * 4.0f / 360.0f + 0.5) & 3;
                        yy = bb;
                        if (l != 0 && l != 2) zz = aa;
                        else xx = aa;
                    } else {
                        zz = bb;
                        xx = aa;
                    }
                } else if (side <= 1) {
                    xx = aa;
                    zz = bb;
                } else if (side <= 3) {
                    xx = aa;
                    yy = bb;
                } else {
                    zz = aa;
                    yy = bb;
                }
                BlockPos at = pos.offset(xx + dir.getStepX(), yy + dir.getStepY(), zz + dir.getStepZ());
                BlockState there = level.getBlockState(at);
                if (there.isAir() || there.is(Blocks.VINE) || there.is(Blocks.SHORT_GRASS) || there.is(Blocks.DEAD_BUSH)
                        || !there.getFluidState().isEmpty() && there.getFluidState().isSource() || there.canBeReplaced()) {
                    list.add(at);
                }
            }
        }
        return list;
    }

    /** O {@code getArchitectBlocks}: agachado, onde os blocos vão. */
    public static List<BlockPos> architectBlocks(ItemStack stack, Level level, BlockPos pos, int side, Player player) {
        if (!player.isShiftKeyDown()) return List.of();
        return spots(stack, level, pos, side, player);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        if (player == null || level.getBlockEntity(pos) != null) return InteractionResult.PASS;
        BlockState state = level.getBlockState(pos);
        ItemStack stack = context.getItemInHand();
        boolean any = false;
        for (BlockPos at : spots(stack, level, pos, context.getClickedFace().get3DDataValue(), player)) {
            BlockState placed = null;
            if (player.getAbilities().instabuild || consume(player, state.getBlock().asItem(), level.isClientSide())) {
                placed = state;
            } else if (state.is(Blocks.GRASS_BLOCK) && consume(player, Blocks.DIRT.asItem(), level.isClientSide())) {
                placed = Blocks.DIRT.defaultBlockState();
            }
            if (placed == null) continue;
            any = true;
            if (level.isClientSide()) {
                level.playLocalSound(at, placed.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 0.6f, 0.9f + level.getRandom().nextFloat() * 0.2f, false);
                if (ToolFx.client != null) ToolFx.client.sparkle(at, placed == state ? 8401408 : 3, 4);
            } else {
                level.setBlock(at, placed, 3);
                stack.hurtAndBreak(1, player, context.getHand());
            }
        }
        return any ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    /** O {@code consumeInventoryItem}: tira um daquele item do inventário (no cliente, só confere). */
    private static boolean consume(Player player, Item item, boolean check) {
        if (item == net.minecraft.world.item.Items.AIR) return false;
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(item)) {
                if (!check) s.shrink(1);
                return true;
            }
        }
        return false;
    }
}
