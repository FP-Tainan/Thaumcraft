package net.thaumcraft.forbidden;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.item.ScribingToolsItem;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.research.Knowledges;

/**
 * O Tinteiro de Cristal: o {@code ItemCrystalwell} do Forbidden Magic 0.575.
 *
 * <p>Escreve como as ferramentas de sempre, mas o que sobra dele no fim vale mais do que ele: gasta a última
 * gota e, ao ser usado, ele devolve de quatro a sete pontos de cada primário para o caderno de quem o gastou —
 * e volta a ser pena e tinteiro comuns.
 */
public class CrystalwellItem extends ScribingToolsItem {
    public CrystalwellItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getDamageValue() < stack.getMaxDamage()) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            var conhecimento = Knowledges.of(player);
            for (Aspect primal : Aspects.primals()) {
                conhecimento.award(primal, level.getRandom().nextInt(4) + 4);
            }
            Knowledges.save(player, conhecimento);
        }
        player.swing(hand);
        player.setItemInHand(hand, new ItemStack(TCItems.SCRIBING_TOOLS));
        return InteractionResult.SUCCESS;
    }
}
