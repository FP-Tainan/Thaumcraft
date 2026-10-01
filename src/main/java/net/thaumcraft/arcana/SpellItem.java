package net.thaumcraft.arcana;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * Um feitiço na mão: o {@code ItemSpellBase} do Ars Magica 2.
 *
 * <p>Ele não é uma coisa: é uma <b>frase</b>, e o que ele faz depende inteiramente do que está escrito nele. Um
 * item sem feitiço dentro não faz nada, e é isso que ele diz a quem o olha.
 */
public class SpellItem extends Item {
    public SpellItem(Properties properties) {
        super(properties);
    }

    /** O feitiço escrito naquele item, ou nenhum. */
    public static Spell spellOf(ItemStack coisa) {
        return coisa.getOrDefault(ArcanaComponents.SPELL, Spell.EMPTY);
    }

    /** Escreve um feitiço num item. */
    public static ItemStack write(ItemStack coisa, Spell feitiço) {
        coisa.set(ArcanaComponents.SPELL, feitiço);
        return coisa;
    }

    /**
     * Quanto tempo se pode segurar um feitiço canalizado: tanto quanto se quiser.
     *
     * <p>O que o faz parar não é o tempo — é a mana acabar, ou quem o segura soltar.
     */
    @Override
    public int getUseDuration(ItemStack coisa, LivingEntity quem) {
        return 72000;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack coisa) {
        return ItemUseAnimation.BOW;
    }

    /** Se a primeira Forma da frase é das que se seguram. */
    public static boolean isChanneled(Spell feitiço) {
        Spell.Stage etapa = feitiço.first();
        return etapa != null && etapa.shape().channeled();
    }

    @Override
    public InteractionResult use(Level level, Player quem, InteractionHand mão) {
        ItemStack naMão = quem.getItemInHand(mão);
        Spell feitiço = spellOf(naMão);
        if (feitiço.isEmpty()) return InteractionResult.PASS;

        // um feitiço canalizado não se lança: segura-se, e quem o corre é o onUseTick
        if (isChanneled(feitiço)) {
            quem.startUsingItem(mão);
            return InteractionResult.CONSUME;
        }

        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;

        SpellCast.Result saiu = SpellCast.cast(server, feitiço, quem, null, quem.getEyePosition());
        if (!saiu.ok()) {
            quem.sendSystemMessage(Component.translatable("tc.spell." + saiu.name().toLowerCase(
                    java.util.Locale.ROOT)));
            return InteractionResult.FAIL;
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Cada batida de um feitiço segurado: o {@code onUsingTick} do original.
     *
     * <p>Ele corre a frase de novo, dizendo <b>há quantas batidas</b> se está segurando — e é a Forma quem
     * decide o que fazer com esse número. O Facho fere de dez em dez.
     *
     * <p>Quando a mana acaba ou o desgaste enche, o feitiço para sozinho: solta-se a mão de quem o segura.
     */
    @Override
    public void onUseTick(Level level, LivingEntity quem, ItemStack coisa, int falta) {
        if (!(level instanceof ServerLevel server)) return;
        Spell feitiço = spellOf(coisa);
        if (feitiço.isEmpty() || !isChanneled(feitiço)) {
            quem.stopUsingItem();
            return;
        }

        int batidas = this.getUseDuration(coisa, quem) - falta;
        SpellCast.Result saiu = SpellCast.cast(server, feitiço, quem, null, quem.getEyePosition(), batidas);
        if (saiu == SpellCast.Result.NOT_ENOUGH_MANA || saiu == SpellCast.Result.BURNED_OUT) {
            if (quem instanceof Player gente) {
                gente.sendSystemMessage(Component.translatable("tc.spell."
                        + saiu.name().toLowerCase(java.util.Locale.ROOT)));
            }
            quem.stopUsingItem();
        }
    }

    /** O que ele diz de si: a frase escrita nele, etapa a etapa. */
    @Override
    public void appendHoverText(ItemStack coisa, TooltipContext contexto, TooltipDisplay mostra,
                                Consumer<Component> linha, TooltipFlag bandeira) {
        super.appendHoverText(coisa, contexto, mostra, linha, bandeira);
        Spell feitiço = spellOf(coisa);
        if (feitiço.isEmpty()) {
            linha.accept(Component.translatable("tc.spell.empty")
                    .withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
            return;
        }

        int n = 1;
        for (Spell.Stage etapa : feitiço.stages()) {
            linha.accept(Component.translatable("tc.spell.stage", n++,
                            Component.translatable("tc.spell.shape." + etapa.shape().name()))
                    .withStyle(net.minecraft.ChatFormatting.AQUA));
            for (SpellPart.Essence essência : etapa.essences()) {
                linha.accept(Component.literal("  ").append(
                                Component.translatable("tc.spell.essence." + essência.name()))
                        .withStyle(net.minecraft.ChatFormatting.GRAY));
            }
            for (SpellPart.Modifier mod : etapa.modifiers()) {
                // a Cor diz-se a si mesma: a linha dela sai pintada da cor que escolheram
                Integer cor = etapa.data().get(mod.name());
                var nome = Component.literal("  + ").append(
                        Component.translatable("tc.spell.modifier." + mod.name()));
                linha.accept(cor == null
                        ? nome.withStyle(net.minecraft.ChatFormatting.DARK_AQUA)
                        : nome.withStyle(estilo -> estilo.withColor(cor)));
            }
        }

        // e o que este feitiço aprendeu no mundo, que não está na frase
        var põe = coisa.get(ArcanaComponents.PLACE_BLOCK);
        if (põe != null) {
            linha.accept(Component.translatable("tc.spell.place_block",
                            põe.getBlock().getName())
                    .withStyle(net.minecraft.ChatFormatting.GREEN));
        }
        var leva = coisa.get(ArcanaComponents.APPROPRIATED);
        if (leva != null) {
            if (leva.bloco().isPresent()) {
                linha.accept(Component.translatable("tc.spell.appropriated_block",
                                leva.bloco().get().getBlock().getName())
                        .withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
            } else if (leva.bicho().isPresent()) {
                linha.accept(Component.translatable("tc.spell.appropriated_entity",
                                Component.literal(leva.bicho().get().getStringOr("id", "?")))
                        .withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
            }
        }
        // a Afinidade não está escrita em lugar nenhum: ela se conta das Essências
        Affinity puxa = feitiço.mainAffinity();
        if (puxa != Affinity.NONE) {
            linha.accept(Component.translatable("tc.spell.affinity",
                            Component.translatable(puxa.key()))
                    .withStyle(estilo -> estilo.withColor(puxa.color)));
        }
        linha.accept(Component.translatable("tc.spell.cost",
                        String.format(java.util.Locale.ROOT, "%.0f", feitiço.manaCost(null, null)))
                .withStyle(net.minecraft.ChatFormatting.BLUE));
    }
}
