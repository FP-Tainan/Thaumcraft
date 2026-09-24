package net.thaumcraft.naturalis;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.block.entity.DeconstructionTableBlockEntity;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.research.Knowledges;

import java.util.function.Consumer;

/**
 * O Diário de Pesquisa: o {@code ResearchLogItem} do Magia Naturalis 0.5.0.
 *
 * <p>Agachado, clicando numa Mesa de Decomposição, ele anota um ponto do aspecto que a mesa está tirando; com ele
 * na mão, o clique comum despeja no caderno de quem o carrega tudo o que estiver anotado.
 */
public class ResearchLogItem extends Item {
    public ResearchLogItem(Properties properties) {
        super(properties);
    }

    /** O que está anotado no diário. */
    public static AspectList notes(ItemStack stack) {
        AspectList list = stack.get(TCComponents.RESEARCH_LOG);
        return list == null ? new AspectList() : list;
    }

    private static void notes(ItemStack stack, AspectList list) {
        stack.set(TCComponents.RESEARCH_LOG, list);
    }

    /**
     * O {@code addResearchPoint} do original: anota um ponto de um primário, até sessenta e quatro. Devolve
     * {@code false} se o diário já estava cheio daquele aspecto — é assim que a mesa sabe que ele acabou.
     */
    public static boolean note(ItemStack stack, Aspect aspect) {
        if (aspect == null || !Aspects.primals().contains(aspect)) return false;
        AspectList list = notes(stack).copy();
        if (list.getAmount(aspect) >= 64) return false;
        list.add(aspect, 1);
        notes(stack, list);
        return true;
    }

    /** O {@code onItemUse}: agachado numa mesa de decomposição, anota um ponto do aspecto dela. */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player == null || !player.isShiftKeyDown()) return InteractionResult.PASS;
        if (!(level.getBlockEntity(context.getClickedPos()) instanceof DeconstructionTableBlockEntity table)) {
            return InteractionResult.PASS;
        }
        Aspect aspect = table.aspect();
        if (aspect == null || !Aspects.primals().contains(aspect)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!note(context.getItemInHand(), aspect)) return InteractionResult.PASS;
        table.takeAspect();
        return InteractionResult.SUCCESS;
    }

    /** E o clique comum passa o que está anotado para o conhecimento de quem carrega o diário. */
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        AspectList list = notes(stack);
        if (list.size() == 0) return InteractionResult.PASS;
        var knowledge = Knowledges.of(player);
        for (Aspect aspect : list.getAspects()) {
            int amount = list.getAmount(aspect);
            if (amount <= 0) continue;
            knowledge.pool().add(aspect, amount);
            if (player instanceof ServerPlayer server) {
                net.thaumcraft.net.TCNetwork.aspectPool(server, aspect, amount, knowledge.points(aspect));
            }
        }
        Knowledges.save(player, knowledge);
        notes(stack, new AspectList());
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        AspectList list = notes(stack);
        if (list.size() == 0) {
            lines.accept(Component.translatable("hint.thaumcraft.empty").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        for (Aspect aspect : Aspects.primals()) {
            int amount = list.getAmount(aspect);
            if (amount > 0) lines.accept(Component.literal(amount + "x " + aspect.name()));
        }
    }
}
