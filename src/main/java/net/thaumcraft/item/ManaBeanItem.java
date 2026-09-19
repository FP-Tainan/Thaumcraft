package net.thaumcraft.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.block.ManaPodBlock;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCItems;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * O feijão de mana: o {@code ItemManaBean} da 4.2.3.5. Um ponto de um aspecto, na cor dele; comido (meio segundo,
 * mesmo sem fome), dá um efeito qualquer da lista de poções de então e, uma vez em quatro, um ponto de pesquisa do
 * aspecto. Plantado embaixo de uma tora, em bioma mágico, vira uma vagem de mana do mesmo aspecto.
 */
public class ManaBeanItem extends Item {
    /**
     * O {@code Potion.potionTypes} de 1.7.10: trinta e dois lugares, os de 1 a 23 ocupados, na ordem dos números de
     * então. Os vazios não dão nada, como lá.
     */
    private static final List<Holder<MobEffect>> POTIONS = new ArrayList<>(List.of(
            MobEffects.SPEED, MobEffects.SLOWNESS, MobEffects.HASTE, MobEffects.MINING_FATIGUE, MobEffects.STRENGTH,
            MobEffects.INSTANT_HEALTH, MobEffects.INSTANT_DAMAGE, MobEffects.JUMP_BOOST, MobEffects.NAUSEA, MobEffects.REGENERATION,
            MobEffects.RESISTANCE, MobEffects.FIRE_RESISTANCE, MobEffects.WATER_BREATHING, MobEffects.INVISIBILITY, MobEffects.BLINDNESS,
            MobEffects.NIGHT_VISION, MobEffects.HUNGER, MobEffects.WEAKNESS, MobEffects.POISON, MobEffects.WITHER,
            MobEffects.HEALTH_BOOST, MobEffects.ABSORPTION, MobEffects.SATURATION));

    public ManaBeanItem(Properties properties) {
        super(properties);
    }

    public static ItemStack of(Aspect aspect) {
        ItemStack stack = new ItemStack(TCItems.MANA_BEAN);
        stack.set(TCComponents.CRYSTAL_ASPECT, aspect.tag());
        return stack;
    }

    /** O {@code onUpdate}: um feijão sem aspecto (o da aba) tira um na sorte. */
    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (CrystalEssenceItem.aspectOf(stack) == null) {
            List<Aspect> all = new ArrayList<>(Aspects.all());
            stack.set(TCComponents.CRYSTAL_ASPECT, all.get(level.getRandom().nextInt(all.size())).tag());
        }
    }

    /** O {@code onFoodEaten}: um efeito ao acaso e, uma vez em quatro, um ponto do aspecto. */
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (level instanceof ServerLevel server && entity instanceof Player player) {
            int index = server.getRandom().nextInt(32) - 1;
            if (index >= 0 && index < POTIONS.size()) {
                Holder<MobEffect> effect = POTIONS.get(index);
                if (effect.value().isInstantaneous()) {
                    effect.value().applyInstantaneousEffect(server, player, player, player, 2, 3.0);
                } else {
                    player.addEffect(new MobEffectInstance(effect, 160 + server.getRandom().nextInt(80), 0));
                }
            }
            Aspect aspect = CrystalEssenceItem.aspectOf(stack);
            if (aspect != null && server.getRandom().nextFloat() < 0.25f) {
                var knowledge = net.thaumcraft.research.Knowledges.of(player);
                knowledge.pool().add(aspect, 1);
                net.thaumcraft.research.Knowledges.save(player, knowledge);
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }

    /** O {@code onItemUse}: na face de baixo de uma tora, em bioma mágico, planta a vagem. */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getClickedFace() != Direction.DOWN) return InteractionResult.PASS;
        Level level = context.getLevel();
        BlockPos log = context.getClickedPos();
        if (!level.getBiome(log).is(ManaPodBlock.MAGICAL)) return InteractionResult.FAIL;
        if (!level.getBlockState(log).is(ManaPodBlock.LOGS)) return InteractionResult.FAIL;
        BlockPos pos = log.below();
        if (!level.isEmptyBlock(pos)) return InteractionResult.SUCCESS;
        if (!level.isClientSide()) {
            BlockState pod = TCBlocks.MANA_POD.defaultBlockState();
            level.setBlock(pos, pod, 2);
            Aspect aspect = CrystalEssenceItem.aspectOf(context.getItemInHand());
            if (aspect != null && level.getBlockEntity(pos) instanceof net.thaumcraft.block.entity.ManaPodBlockEntity entity) {
                entity.aspect = aspect;
                entity.setChanged();
            }
            Player player = context.getPlayer();
            if (player == null || !player.getAbilities().instabuild) context.getItemInHand().shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> lines,
                                TooltipFlag flag) {
        Aspect aspect = CrystalEssenceItem.aspectOf(stack);
        if (aspect == null) return;
        lines.accept(CrystalEssenceItem.known.test(aspect) ? Component.literal(aspect.name().getString() + " x1")
                : Component.translatable("tc.aspect.unknown"));
    }
}
