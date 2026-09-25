package net.thaumcraft.forbidden;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCSounds;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * As ferramentas camaleão: os {@code ItemMorphPickaxe}, {@code ItemMorphSword}, {@code ItemMorphShovel} e
 * {@code ItemMorphAxe} do Forbidden Magic 0.575.
 *
 * <p>Cada uma guarda <b>três caras</b>. Agachado, com o botão direito, a ferramenta troca de cara: os
 * encantamentos e o nome que ela tinha ficam guardados na cara de onde ela saiu, e voltam os da cara para onde
 * ela foi. Assim uma picareta só serve de picareta de seda, de fortuna e de eficiência, conforme o serviço — e
 * a troca custa cinco de vida e faz o barulho de varinha falhada.
 */
public class MorphToolItem extends Item {
    /** As três cores do olho, do original: vinho, azul e ouro. */
    public static final int[] EYE_COLOURS = {0x980000, 0x0010CC, 0xE5E100};

    /** A troca custa isto de vida, e não se troca com menos que isso na ferramenta. */
    private static final int COST = 5;

    public MorphToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown() || !hasSomethingToKeep(stack)) return InteractionResult.PASS;
        if (stack.getMaxDamage() - stack.getDamageValue() <= COST) return InteractionResult.PASS;

        cycle(stack);
        stack.hurtAndBreak(COST, player, hand == InteractionHand.MAIN_HAND
                ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        player.swing(hand);
        level.playSound(null, player, TCSounds.WAND_FAIL.value(), SoundSource.PLAYERS, 0.2f,
                0.2f + level.getRandom().nextFloat() * 0.2f);
        return InteractionResult.SUCCESS;
    }

    /** Só faz sentido trocar de cara quando há alguma coisa para guardar ou já guardada. */
    private static boolean hasSomethingToKeep(ItemStack stack) {
        return !stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty()
                || stack.has(DataComponents.CUSTOM_NAME)
                || stack.has(TCComponents.MORPH_SLOTS);
    }

    /** Guarda a cara de agora, avança uma e veste a que estava guardada ali. */
    public static void cycle(ItemStack stack) {
        List<MorphSlot> slots = new ArrayList<>(stack.getOrDefault(TCComponents.MORPH_SLOTS,
                List.of(MorphSlot.EMPTY, MorphSlot.EMPTY, MorphSlot.EMPTY)));
        while (slots.size() < EYE_COLOURS.length) slots.add(MorphSlot.EMPTY);
        int phase = stack.getOrDefault(TCComponents.MORPH_PHASE, 0);

        slots.set(phase, new MorphSlot(stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY),
                Optional.ofNullable(stack.get(DataComponents.CUSTOM_NAME))));
        phase = (phase + 1) % EYE_COLOURS.length;
        MorphSlot vestir = slots.get(phase);

        if (vestir.enchantments().isEmpty()) stack.remove(DataComponents.ENCHANTMENTS);
        else stack.set(DataComponents.ENCHANTMENTS, vestir.enchantments());
        vestir.name().ifPresentOrElse(nome -> stack.set(DataComponents.CUSTOM_NAME, nome),
                () -> stack.remove(DataComponents.CUSTOM_NAME));

        stack.set(TCComponents.MORPH_SLOTS, List.copyOf(slots));
        stack.set(TCComponents.MORPH_PHASE, phase);
        // é por esta cor que o olho da figura sabe em que cara a ferramenta está
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of(),
                List.of(EYE_COLOURS[phase])));
    }

    /** Em que cara a ferramenta está. */
    public static int phase(ItemStack stack) {
        return stack.getOrDefault(TCComponents.MORPH_PHASE, 0);
    }

    /** A dica diz em que cara ela está, que é o que o olho mostra. */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                net.minecraft.world.item.component.TooltipDisplay display,
                                java.util.function.Consumer<Component> lines, net.minecraft.world.item.TooltipFlag flag) {
        lines.accept(Component.translatable("item.thaumcraft.morph.phase", phase(stack) + 1)
                .withStyle(net.minecraft.ChatFormatting.DARK_PURPLE));
    }
}
