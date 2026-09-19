package net.thaumcraft.block.eldritch;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.item.LootBagItem;

import java.util.ArrayList;
import java.util.List;

/**
 * A urna velha e o caixote abandonado: o {@code BlockLoot} da 4.2.3.5, cada um nas três raridades (comum, incomum e
 * rara). Quebrados, derramam de um mais a raridade até três mais a raridade coisas da mesma tabela das sacolas de
 * tesouro ({@code Utils.generateLoot}).
 */
public class LootBlock extends Block {
    public static final MapCodec<LootBlock> CODEC = simpleCodec(p -> new LootBlock(0, true, p));
    /** As três peças do {@code BlockLootUrnRenderer}: o pé, o bojo e a boca. */
    private static final VoxelShape URN = Shapes.or(Block.box(3, 0, 3, 13, 1, 13), Block.box(2, 1, 2, 14, 13, 14), Block.box(4, 13, 4, 12, 16, 12));
    private static final VoxelShape URN_BODY = Block.box(2, 1, 2, 14, 13, 14);
    private static final VoxelShape CRATE = Block.box(1, 0, 1, 15, 14, 15);

    public final int rarity;
    public final boolean urn;

    public LootBlock(int rarity, boolean urn, Properties properties) {
        super(properties);
        this.rarity = rarity;
        this.urn = urn;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.urn ? URN : CRATE;
    }

    /** A colisão do original ({@code getCollisionBoundingBoxFromPool}): a urna só o bojo, o caixote o cubo menor. */
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.urn ? URN_BODY : CRATE;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        var level = params.getLevel();
        List<ItemStack> out = new ArrayList<>();
        int q = 1 + this.rarity + level.getRandom().nextInt(3);
        for (int a = 0; a < q; a++) {
            ItemStack stack = LootBagItem.generateLoot(this.rarity, level.getRandom(), level.registryAccess());
            if (!stack.isEmpty()) out.add(stack.copy());
        }
        return out;
    }
}
