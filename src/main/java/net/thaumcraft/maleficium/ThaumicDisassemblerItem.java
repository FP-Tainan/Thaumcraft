package net.thaumcraft.maleficium;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.event.Enchantments;
import net.thaumcraft.registry.TCComponents;

import java.util.function.Consumer;

/**
 * O Desmontador Táumico: o {@code ItemThaumicDisassembler} do Tainted Magic 8.1.1. Ele não gasta uso: bebe entropia
 * das varinhas do inventário — cem centésimos por segundo, até cinquenta mil — e queima essa carga para cavar, para
 * lavrar a terra e para bater. Agachado, o clique direito passa de modo em modo: normal, devagar, depressa e desligado.
 */
public class ThaumicDisassemblerItem extends Item {
    /** A carga que cabe nele, em centésimos de entropia. */
    public static final int MAX_CHARGE = 50000;
    /** O que cada coisa cobra: cavar, lavrar e bater. */
    public static final int ENTROPY_USAGE_BASE = 5;
    public static final int ENTROPY_USAGE_HOE = 25;
    public static final int ENTROPY_USAGE_HIT = 50;
    /** O dano que a carga paga, como diz a dica do original. */
    public static final float CHARGED_DAMAGE = 20.0f;

    public ThaumicDisassemblerItem(Properties properties) {
        super(properties);
    }

    // ------------------------------------------------------------------ a carga e o modo

    public static int charge(ItemStack stack) {
        Integer charge = stack.get(TCComponents.DISASSEMBLER_CHARGE);
        return charge == null ? 0 : charge;
    }

    private static void charge(ItemStack stack, int amount) {
        stack.set(TCComponents.DISASSEMBLER_CHARGE, Math.max(0, Math.min(MAX_CHARGE, amount)));
    }

    public static int mode(ItemStack stack) {
        Integer mode = stack.get(TCComponents.DISASSEMBLER_MODE);
        return mode == null ? 0 : mode;
    }

    /** A velocidade de cada modo, com os números do original. */
    public static int efficiency(int mode) {
        return switch (mode) {
            case 0 -> 20;
            case 1 -> 8;
            case 2 -> 128;
            default -> 0;
        };
    }

    public static int efficiency(ItemStack stack) {
        return efficiency(mode(stack));
    }

    private static Component modeName(int mode) {
        String name = switch (mode) {
            case 0 -> "normal";
            case 1 -> "slow";
            case 2 -> "fast";
            default -> "off";
        };
        return Component.translatable("text.disassembler." + name)
                .withStyle(mode == 3 ? ChatFormatting.RED : ChatFormatting.GREEN);
    }

    // ------------------------------------------------------------------ a barra e a dica

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * charge(stack) / MAX_CHARGE);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x8B4DBF;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        lines.accept(Component.translatable("text.disassembler.charge").append(": ")
                .append(Component.literal(charge(stack) / 100 + "/" + MAX_CHARGE / 100).withStyle(ChatFormatting.DARK_GRAY)));
        lines.accept(Component.translatable("text.disassembler.mode").append(": ").append(modeName(mode(stack))));
        lines.accept(Component.translatable("text.disassembler.efficiency").append(": ")
                .append(Component.literal(String.valueOf(efficiency(stack)))
                        .withStyle(mode(stack) == 3 ? ChatFormatting.RED : ChatFormatting.GREEN)));
        lines.accept(Component.literal(" "));
        lines.accept(Component.literal("+" + (int) CHARGED_DAMAGE + " ")
                .append(Component.translatable("text.attackdamage")).withStyle(ChatFormatting.BLUE));
    }

    // ------------------------------------------------------------------ cavar e bater

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return charge(stack) > 0 && !state.is(Blocks.BEDROCK);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return charge(stack) == 0 ? 1.0f : efficiency(stack);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (state.getDestroySpeed(level, pos) != 0.0f) {
            charge(stack, charge(stack) - (int) (ENTROPY_USAGE_BASE * efficiency(stack) / 8.0));
        }
        return true;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (charge(stack) > 0) charge(stack, charge(stack) - ENTROPY_USAGE_HIT);
    }

    /** Com carga, o golpe vale vinte; sem ela, vale os quatro da mão. */
    @Override
    public float getAttackDamageBonus(Entity target, float damage, DamageSource source) {
        if (!(source.getEntity() instanceof LivingEntity attacker)) return 0.0f;
        ItemStack held = attacker.getMainHandItem();
        if (held.getItem() != this || charge(held) <= 0) return 0.0f;
        return Math.max(0.0f, CHARGED_DAMAGE - damage);
    }

    // ------------------------------------------------------------------ o modo e a lavoura

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) return InteractionResult.PASS;
        int next = mode(stack) < 3 ? mode(stack) + 1 : 0;
        stack.set(TCComponents.DISASSEMBLER_MODE, next);
        if (player instanceof net.minecraft.server.level.ServerPlayer sent) {
            sent.sendSystemMessage(Component.translatable("text.disassembler.mode").append(": ")
                    .append(modeName(next)).append(Component.literal(" (" + efficiency(next) + ")")
                            .withStyle(next == 3 ? ChatFormatting.RED : ChatFormatting.GREEN)), true);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player == null || player.isShiftKeyDown() || mode(stack) == 3) return InteractionResult.PASS;
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        boolean tilled = hoe(stack, player, level, pos, context);
        if (!tilled && !level.getBlockState(pos).is(Blocks.FARMLAND)) return InteractionResult.PASS;
        if (mode(stack) == 1) return InteractionResult.SUCCESS;
        int radius = mode(stack) == 0 ? 1 : 2;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x == 0 && z == 0) continue;
                hoe(stack, player, level, pos.offset(x, 0, z), context);
            }
        }
        return InteractionResult.SUCCESS;
    }

    /** O {@code useHoe}: lavra um bloco de terra, cobrando a entropia de quem não está no criativo. */
    private boolean hoe(ItemStack stack, Player player, Level level, BlockPos pos, UseOnContext context) {
        if (!player.getAbilities().instabuild && charge(stack) < ENTROPY_USAGE_HOE) return false;
        if (level instanceof ServerLevel server && !player.mayInteract(server, pos)) return false;
        if (context.getClickedFace() == Direction.DOWN) return false;
        if (!level.getBlockState(pos.above()).isAir()) return false;
        BlockState state = level.getBlockState(pos);
        if (!state.is(Blocks.GRASS_BLOCK) && !state.is(Blocks.DIRT)) return false;
        level.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0f, 0.8f);
        if (!level.isClientSide()) {
            level.setBlockAndUpdate(pos, Blocks.FARMLAND.defaultBlockState());
            if (!player.getAbilities().instabuild) charge(stack, charge(stack) - ENTROPY_USAGE_HOE);
        }
        return true;
    }

    // ------------------------------------------------------------------ a bebida de entropia

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (!(entity instanceof Player player) || charge(stack) >= MAX_CHARGE || entity.tickCount % 20 != 0) return;
        int amount = Math.min(100, MAX_CHARGE - charge(stack));
        if (Enchantments.consumeVisFromInventory(player, new AspectList().add(Aspects.ENTROPY, amount))) {
            charge(stack, charge(stack) + amount);
        }
    }
}
