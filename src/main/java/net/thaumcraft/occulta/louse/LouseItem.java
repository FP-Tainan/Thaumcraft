package net.thaumcraft.occulta.louse;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaEntities;
import org.jetbrains.annotations.Nullable;

/**
 * O <b>Piolho Parasita</b> na mão: a {@code ItemParasyticLouse} do Witchery.
 *
 * <p>Ele é um bicho dobrado num item. Enche-se de <b>uma poção</b> na bancada, e larga-se — na face de um
 * bloco, como um ovo de bicho, ou <b>na água</b>, olhando para ela. O que nasce dali morde uma vez e
 * passa a poção adiante.
 *
 * <p>E há um terceiro jeito de o usar, que é o que faz dele equipamento e não brinquedo: <b>levado na
 * mochila</b>, ele se gasta sozinho quando quem o traz apanha um golpe — veja o {@link Lice}.
 */
public class LouseItem extends Item {
    public LouseItem(Properties properties) {
        super(properties);
    }

    /** A poção que este piolho leva, ou nenhuma. */
    public static PotionContents poção(ItemStack piolho) {
        return piolho.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
    }

    /** Se ele leva alguma. */
    public static boolean cheio(ItemStack piolho) {
        return !poção(piolho).equals(PotionContents.EMPTY);
    }

    /** E na mão ele diz <b>que poção</b> leva, que é a dica do original. */
    @Override
    public void appendHoverText(ItemStack piolho, TooltipContext contexto, TooltipDisplay mostra,
                                java.util.function.Consumer<Component> linha, TooltipFlag bandeira) {
        PotionContents tem = poção(piolho);
        if (tem.equals(PotionContents.EMPTY)) return;
        PotionContents.addPotionTooltip(tem.getAllEffects(), linha, 1.0f, contexto.tickRate());
    }

    /**
     * Largado na face de um bloco: nasce ali.
     *
     * <p>Com nome escrito, o bicho herda o nome — que é o que o original faz e o que torna um piolho
     * chamado uma coisa diferente de um piolho.
     */
    @Override
    public InteractionResult useOn(UseOnContext onde) {
        if (!(onde.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        BlockPos casa = onde.getClickedPos().relative(onde.getClickedFace());
        return põe(level, casa, onde.getItemInHand(), onde.getPlayer());
    }

    /** E olhando para água, nasce nela. */
    @Override
    public InteractionResult use(net.minecraft.world.level.Level mundo,
                                 net.minecraft.world.entity.player.Player quem,
                                 net.minecraft.world.InteractionHand mão) {
        ItemStack piolho = quem.getItemInHand(mão);
        if (!(mundo instanceof ServerLevel level)) return InteractionResult.SUCCESS;

        var olhou = getPlayerPOVHitResult(mundo, quem,
                net.minecraft.world.level.ClipContext.Fluid.SOURCE_ONLY);
        if (olhou.getType() != net.minecraft.world.phys.HitResult.Type.BLOCK) {
            return InteractionResult.PASS;
        }
        BlockPos casa = olhou.getBlockPos();
        if (!level.getBlockState(casa).is(Blocks.WATER)) return InteractionResult.PASS;
        if (!quem.mayInteract(level, casa)) return InteractionResult.PASS;
        return põe(level, casa, piolho, quem);
    }

    /** Põe o bicho naquela casa e gasta o item. */
    private static InteractionResult põe(ServerLevel level, BlockPos casa, ItemStack piolho,
                                         @Nullable net.minecraft.world.entity.player.Player quem) {
        var bicho = OccultaEntities.LOUSE.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        if (bicho == null) return InteractionResult.PASS;

        bicho.snapTo(casa.getX() + 0.5, casa.getY(), casa.getZ() + 0.5,
                level.getRandom().nextFloat() * 360.0f, 0.0f);
        bicho.poção(poção(piolho));
        bicho.setPersistenceRequired();
        Component nome = piolho.get(DataComponents.CUSTOM_NAME);
        if (nome != null) bicho.setCustomName(nome);
        bicho.finalizeSpawn(level, level.getCurrentDifficultyAt(casa),
                EntitySpawnReason.SPAWN_ITEM_USE, null);
        level.addFreshEntity(bicho);

        if (quem == null || !quem.hasInfiniteMaterials()) piolho.shrink(1);
        return InteractionResult.SUCCESS;
    }
}
