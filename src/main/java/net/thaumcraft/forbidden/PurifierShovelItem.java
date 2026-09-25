package net.thaumcraft.forbidden;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.block.TaintBlock;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCSounds;

/**
 * A Pá do Purificador: o {@code ItemTaintShovel} do Forbidden Magic 0.575.
 *
 * <p>Ela cava mácula como se fosse terra, limpa a gosma e o gás de fluxo de um pedaço inteiro à volta com um
 * clique, e de vez em quando tira das terras torcidas um Fragmento de Mácula.
 */
public class PurifierShovelItem extends Item {
    public PurifierShovelItem(Properties properties) {
        super(properties);
    }

    /**
     * O {@code onItemUse} do original: apaga toda a gosma e todo o gás de fluxo de onze por nove por onze em
     * volta do bloco clicado, gastando um ponto por bloco limpo, até quinze.
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos alvo = context.getClickedPos();
        if (player == null) return super.useOn(context);

        int limpos = 0;
        for (BlockPos pos : BlockPos.betweenClosed(alvo.offset(-5, -4, -5), alvo.offset(5, 4, 5))) {
            BlockState estado = level.getBlockState(pos);
            if (!estado.is(TCBlocks.FLUX_GOO) && !estado.is(TCBlocks.FLUX_GAS)) continue;
            limpos++;
            if (!level.isClientSide()) level.setBlock(pos.immutable(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
        if (limpos == 0) return super.useOn(context);

        context.getItemInHand().hurtAndBreak(Math.min(limpos, 15), player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        player.swing(context.getHand());
        level.playSound(null, alvo, TCSounds.WAND_FAIL.value(), SoundSource.PLAYERS, 0.2f,
                0.2f + level.getRandom().nextFloat() * 0.2f);
        return InteractionResult.SUCCESS;
    }

    /** Mácula é mole para ela: cava tão depressa quanto a terra que ela sabe cavar. */
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        float normal = super.getDestroySpeed(stack, state);
        if (normal > 1.0f || !TaintBlock.isTaint(state)) return normal;
        return net.thaumcraft.item.TCMaterials.ELEMENTAL.speed();
    }

    /**
     * O {@code onHarvest} do original: cavando mácula que não deixa nada, uma vez em trinta e uma sai um
     * Fragmento de Mácula — e a Fortuna e o Toque Suave melhoram a sorte.
     */
    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        boolean gastou = super.mineBlock(stack, level, state, pos, miner);
        if (level.isClientSide() || !TaintBlock.isTaint(state)) return gastou;
        var encantamentos = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        int fortuna = net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
                encantamentos.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FORTUNE), stack);
        int seda = net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
                encantamentos.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH), stack);
        int sorte = 1 + 2 * fortuna + (seda > 0 ? 6 : 0);
        if (level.getRandom().nextInt(31) <= sorte) {
            Block.popResource(level, pos, new ItemStack(ForbiddenItems.SHARDS.get("taint")));
        }
        return gastou;
    }
}
