package net.thaumcraft.naturalis;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * O que chama o Baú Maligno: o {@code EvilTrunkSpawnerItem} do Magia Naturalis 0.5.0. São quatro, um por feitio;
 * usado num bloco, o baú nasce ali, do feitio deste item e já com o que o item estiver guardando.
 */
public class EvilTrunkSpawnerItem extends Item {
    private final EvilTrunkEntity.Kind kind;

    public EvilTrunkSpawnerItem(EvilTrunkEntity.Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public EvilTrunkEntity.Kind kind() {
        return this.kind;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        var player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        BlockPos onde = context.getClickedPos().relative(context.getClickedFace());
        EvilTrunkEntity trunk = NaturalisEntities.EVIL_TRUNK.create(server, EntitySpawnReason.SPAWN_ITEM_USE);
        if (trunk == null) return InteractionResult.PASS;
        trunk.kind(this.kind);
        ItemStack stack = context.getItemInHand();
        var guardado = stack.get(DataComponents.CONTAINER);
        if (guardado != null) guardado.copyInto(trunk.inventory.all());
        trunk.owner(player);
        var nome = stack.get(DataComponents.CUSTOM_NAME);
        if (nome != null) trunk.setCustomName(nome);
        trunk.snapTo(onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5,
                server.getRandom().nextFloat() * 360.0f, 0.0f);
        if (server.addFreshEntity(trunk)) {
            trunk.playAmbientSound();
            if (!player.getAbilities().instabuild) stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        if (stack.has(DataComponents.CONTAINER)) {
            lines.accept(Component.translatable("item.thaumcraft.trunk_spawner.text.1").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
