package net.thaumcraft.naturalis;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import net.thaumcraft.registry.TCComponents;

import java.util.function.Consumer;

/**
 * O jarro na mão: o {@code PrisonJarBlockItem} do Magia Naturalis 0.5.0. Clicado numa criatura, ele a guarda
 * inteira — menos gente e menos chefe, como no original — e a dica passa a dizer quem está lá dentro.
 */
public class PrisonJarItem extends BlockItem {
    public PrisonJarItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if (!(player.level() instanceof ServerLevel level)) return InteractionResult.PASS;
        if (stack.has(TCComponents.JARRED_MOB)) return InteractionResult.PASS;
        if (!(entity instanceof Mob mob) || mob.getType().getCategory() == net.minecraft.world.entity.MobCategory.MISC) {
            return InteractionResult.PASS;
        }
        // chefe não cabe em jarro
        if (mob instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon
                || mob instanceof net.minecraft.world.entity.boss.wither.WitherBoss) return InteractionResult.PASS;

        try (var escopo = new net.minecraft.util.ProblemReporter.ScopedCollector(net.thaumcraft.Thaumcraft.LOGGER)) {
            var saida = net.minecraft.world.level.storage.TagValueOutput.createWithContext(escopo, level.registryAccess());
            if (!mob.save(saida)) return InteractionResult.PASS;
            CompoundTag guardado = saida.buildResult();
            ItemStack cheio = stack.copyWithCount(1);
            cheio.set(TCComponents.JARRED_MOB, guardado);
            stack.shrink(1);
            if (!player.getInventory().add(cheio)) player.drop(cheio, false);
            mob.discard();
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        CompoundTag guardado = stack.get(TCComponents.JARRED_MOB);
        if (guardado == null) {
            lines.accept(Component.translatable("hint.thaumcraft.empty")
                    .withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
            return;
        }
        guardado.getString("id").ifPresent(id -> {
            var type = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE
                    .getOptional(net.minecraft.resources.Identifier.parse(id));
            type.ifPresent(value -> lines.accept(Component.translatable(value.getDescriptionId())));
        });
    }
}
