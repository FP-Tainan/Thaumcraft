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

    /**
     * O <b>Impacto</b>: a picareta e a pá camaleão quebram três por três na face em que bateram, gastando um
     * ponto de vida por bloco e pulando o que a ferramenta não sabe quebrar.
     */
    @Override
    public boolean mineBlock(ItemStack stack, Level level, net.minecraft.world.level.block.state.BlockState state,
                             net.minecraft.core.BlockPos pos, net.minecraft.world.entity.LivingEntity miner) {
        boolean gastou = super.mineBlock(stack, level, state, pos, miner);
        if (!(level instanceof net.minecraft.server.level.ServerLevel server)
                || !(miner instanceof net.minecraft.server.level.ServerPlayer player)) {
            return gastou;
        }
        if (ForbiddenEnchantments.level(ForbiddenEnchantments.IMPACT, stack, level) <= 0) return gastou;
        if (!stack.isCorrectToolForDrops(state)) return gastou;

        // o plano de três por três é o da face que quem cava estava olhando
        net.minecraft.core.Direction face = net.minecraft.core.Direction.orderedByNearest(player)[0];
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                if (a == 0 && b == 0) continue;
                net.minecraft.core.BlockPos perto = switch (face.getAxis()) {
                    case Y -> pos.offset(a, 0, b);
                    case Z -> pos.offset(a, b, 0);
                    case X -> pos.offset(0, b, a);
                };
                var outro = level.getBlockState(perto);
                if (outro.isAir() || outro.getDestroySpeed(level, perto) < 0.0f) continue;
                if (!stack.isCorrectToolForDrops(outro)) continue;
                if (!player.getAbilities().instabuild) {
                    stack.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
                }
                server.destroyBlock(perto, true, player);
            }
        }
        return gastou;
    }

    /**
     * A <b>Tocada pelo Vazio</b>: a ferramenta se conserta sozinha, um ponto a cada dez tiques, e custa um ponto
     * de distorção a quem a carrega — o {@code onUpdate} e o {@code getWarp} do original.
     */
    @Override
    public void inventoryTick(ItemStack stack, net.minecraft.server.level.ServerLevel level,
                              net.minecraft.world.entity.Entity entity, net.minecraft.world.entity.EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (entity == null || entity.tickCount % 10 != 0 || !stack.isDamaged()) return;
        if (ForbiddenEnchantments.level(ForbiddenEnchantments.VOIDTOUCHED, stack, level) <= 0) return;
        stack.setDamageValue(stack.getDamageValue() - 1);
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
