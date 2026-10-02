package net.thaumcraft.arcana;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.Locale;
import java.util.function.Consumer;

/**
 * Uma <b>ferramenta vinculada</b>: as {@code ItemBound*} do Ars Magica 2.
 *
 * <p>É um feitiço que virou ferramenta. Ela não se fabrica e não se conserta: <b>ela se segura</b>, e segurar
 * custa mana <b>a cada batida</b>. Quando a mana acaba, ela se desfaz e volta a ser o feitiço que era.
 *
 * <p>Em troca, ela <b>nunca se gasta</b>: o original a conserta um ponto por batida enquanto a mantém. Uma
 * picareta vinculada de diamante não quebra nunca — mas come um ponto de mana por batida, que é <b>vinte por
 * segundo</b>, e ninguém a carrega sem pensar.
 *
 * <p>O preço depende do metal: <b>um décimo</b> por batida para a pedra, <b>quatro décimos</b> para o ferro e
 * <b>um inteiro</b> para o diamante. São os três números do {@code IBoundItem}, e é a única escolha que ela dá:
 * quanto ela vale contra quanto ela custa.
 */
public class BoundToolItem extends Item {
    /** O que ela é, e o que isso custa por batida. */
    public enum Kind {
        /** A picareta, de diamante: a mais cara e a que serve para tudo. */
        PICKAXE(1.0f),
        /** O machado, de diamante. */
        AXE(1.0f),
        /** A espada, de diamante. */
        SWORD(1.0f),
        /** A pá, de ferro. */
        SHOVEL(0.4f),
        /** A enxada, de pedra: a mais barata de manter. */
        HOE(0.1f),
        /**
         * E o <b>arco</b>, de ferro.
         *
         * <p>Ele é o único que não cava nem bate, e o único que não é desta classe: um arco do jogo tem de
         * herdar o {@code BowItem} para saber puxar a corda. Quem o faz é o {@link BoundBowItem}.
         */
        BOW(0.4f);

        /** O que ela come de mana por batida: o {@code maintainCost} do original. */
        public final float maintain;

        Kind(float maintain) {
            this.maintain = maintain;
        }

        public String id() {
            return "bound_" + this.name().toLowerCase(Locale.ROOT);
        }
    }

    private final Kind kind;

    public BoundToolItem(Properties properties, Kind qual) {
        super(properties);
        this.kind = qual;
    }

    public Kind kind() {
        return this.kind;
    }

    /**
     * A cada batida: cobra a mana, conserta um ponto, e se desfaz se não houver com que pagar.
     *
     * <p>É o {@code onUpdate} do original, e é o coração da ideia: uma ferramenta que só existe enquanto se
     * pode pagar por ela.
     */
    @Override
    public void inventoryTick(ItemStack coisa, net.minecraft.server.level.ServerLevel level, Entity quem,
                              net.minecraft.world.entity.EquipmentSlot casa) {
        if (!(quem instanceof Player gente)) return;
        if (gente.hasInfiniteMaterials()) return;

        Mana conta = Mana.of(gente);
        if (conta.mana() < this.kind.maintain) {
            unbind(coisa, gente);
            return;
        }

        Mana.set(gente, conta.withMana(conta.mana() - this.kind.maintain));

        // e ela se conserta sozinha enquanto se mantém
        if (coisa.isDamaged()) coisa.setDamageValue(coisa.getDamageValue() - 1);
    }

    /**
     * Desfaz o vínculo: a ferramenta volta a ser o feitiço que era.
     *
     * <p>O feitiço vem de dentro dela — foi guardado quando ela foi feita —, e é por isso que desfazer e
     * refazer não perde a frase.
     */
    public static void unbind(ItemStack coisa, Player quem) {
        Spell feitiço = coisa.getOrDefault(ArcanaComponents.SPELL, Spell.EMPTY);
        ItemStack volta = SpellItem.write(new ItemStack(ArcanaItems.SPELL), feitiço);

        for (int i = 0; i < quem.getInventory().getContainerSize(); i++) {
            if (quem.getInventory().getItem(i) == coisa) {
                quem.getInventory().setItem(i, volta);
                return;
            }
        }
        // não estando na mochila, some e o feitiço cai no chão
        coisa.setCount(0);
        quem.drop(volta, false);
    }

    /** Ela conta o que custa e que feitiço leva dentro. */
    @Override
    public void appendHoverText(ItemStack coisa, TooltipContext contexto, TooltipDisplay mostra,
                                Consumer<Component> linha, TooltipFlag bandeira) {
        super.appendHoverText(coisa, contexto, mostra, linha, bandeira);
        linha.accept(Component.translatable("tc.spell.bound.maintain",
                        String.format(Locale.ROOT, "%.1f", this.kind.maintain))
                .withStyle(ChatFormatting.BLUE));

        Spell feitiço = coisa.getOrDefault(ArcanaComponents.SPELL, Spell.EMPTY);
        if (!feitiço.isEmpty()) {
            linha.accept(Component.translatable("tc.spell.bound.holds")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

}
