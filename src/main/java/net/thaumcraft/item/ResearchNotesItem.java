package net.thaumcraft.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.thaumcraft.registry.TCSounds;
import net.thaumcraft.research.Knowledges;
import net.thaumcraft.research.PlayerKnowledge;
import net.thaumcraft.research.Research;
import net.thaumcraft.research.ResearchManager;
import net.thaumcraft.research.ResearchNote;
import net.thaumcraft.research.ResearchNotes;
import net.thaumcraft.research.Researches;

import java.util.function.Consumer;

/**
 * As notas de pesquisa: o {@code ItemResearchNotes} da 4.2.3.5.
 *
 * <p>Enquanto o tabuleiro não está resolvido, são "Notas de Pesquisa"; resolvido, o pergaminho vira uma
 * "Descoberta", e lido — com o botão — ensina a pesquisa e some.
 */
public class ResearchNotesItem extends Item {
    public ResearchNotesItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.has(net.thaumcraft.registry.TCComponents.UNKNOWN_NOTE) && ResearchNotes.get(stack) == null) {
            if (!level.isClientSide()) reveal(level, player, hand, stack);
            return InteractionResult.SUCCESS;
        }
        ResearchNote note = ResearchNotes.get(stack);
        if (note == null || !note.complete()) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        PlayerKnowledge knowledge = Knowledges.of(player);
        Research research = Researches.get(note.key());
        if (research == null || knowledge.hasResearch(note.key())) return InteractionResult.PASS;
        if (!ResearchManager.canUnlock(knowledge, research)) {
            player.sendSystemMessage(Component.translatable("tc.researcherror"));
            return InteractionResult.FAIL;
        }
        ResearchManager.completeWithSiblings(player, knowledge, research);
        Knowledges.save(player, knowledge);
        stack.shrink(1);
        level.playSound(null, player, TCSounds.LEARN.value(), SoundSource.PLAYERS, 0.75f, 1.0f);
        return InteractionResult.SUCCESS;
    }

    /**
     * O conhecimento desconhecido, lido: vira a nota de uma pesquisa escondida ({@code findHiddenResearch}); sem nenhuma
     * a achar, a teoria era falsa — a nota some e devolve de sete a nove fragmentos.
     */
    private static void reveal(Level level, Player player, InteractionHand hand, ItemStack stack) {
        String key = ResearchManager.findHiddenResearch(player);
        if (key.equals("FAIL")) {
            stack.shrink(1);
            ItemStack fragments = new ItemStack(net.thaumcraft.registry.TCResources.get("knowledge_fragment"), 7 + level.getRandom().nextInt(3));
            level.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(level, player.getX(), player.getY() + player.getEyeHeight() / 2.0f,
                    player.getZ(), fragments));
            level.playSound(null, player, TCSounds.ERASE.value(), SoundSource.PLAYERS, 0.75f, 1.0f);
            return;
        }
        ItemStack note = ResearchNotes.create(key, new java.util.Random(level.getRandom().nextLong()));
        if (note.isEmpty()) return;
        note.setCount(stack.getCount());
        player.setItemInHand(hand, note);
        level.playSound(null, player, TCSounds.WRITE.value(), SoundSource.PLAYERS, 0.75f, 1.0f);
    }

    @Override
    public Component getName(ItemStack stack) {
        ResearchNote note = ResearchNotes.get(stack);
        boolean complete = note != null && note.complete();
        return Component.translatable(complete ? "item.thaumcraft.discovery" : "item.thaumcraft.research_notes")
                .withStyle(complete ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.AQUA);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        if (stack.has(net.thaumcraft.registry.TCComponents.UNKNOWN_NOTE)) {
            tooltip.accept(Component.translatable("item.researchnotes.unknown.1").withStyle(ChatFormatting.GOLD));
            tooltip.accept(Component.translatable("item.researchnotes.unknown.2").withStyle(ChatFormatting.BLUE));
        }
        ResearchNote note = ResearchNotes.get(stack);
        if (note == null) return;
        Research research = Researches.get(note.key());
        if (research == null) return;
        tooltip.accept(research.name().copy().withStyle(ChatFormatting.GOLD));
        tooltip.accept(Component.translatable("tc.research_text." + note.key()).withStyle(ChatFormatting.ITALIC));
        // a pesquisa proibida avisa o tamanho do perigo (o tc.forbidden, de um a cinco)
        int warp = research.warp();
        if (warp > 0) {
            warp = Math.min(5, warp);
            tooltip.accept(Component.translatable("tc.forbidden", Component.translatable("tc.forbidden.level." + warp)).withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    /** Nota comum é rara, descoberta é épica, como no original. */
    public static Rarity rarity(ItemStack stack) {
        ResearchNote note = ResearchNotes.get(stack);
        return note != null && note.complete() ? Rarity.EPIC : Rarity.RARE;
    }
}
