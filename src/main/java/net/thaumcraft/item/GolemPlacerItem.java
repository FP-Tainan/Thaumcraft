package net.thaumcraft.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.thaumcraft.entity.GolemEntity;
import net.thaumcraft.entity.golem.Marker;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCEntities;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * O golem guardado na mão: o {@code ItemGolemPlacer} da 4.2.3.5 — um item por matéria. Posto na face de um bloco, o
 * golem nasce ali com a casa naquele lugar, e o bloco tocado vira o baú da casa dele. Quando o sino o recolhe, o item
 * leva junto o núcleo, as melhorias, os enfeites, as marcas e as casas fantasmas.
 */
public class GolemPlacerItem extends Item {
    /** Uma casa fantasma guardada: a coisa (de um) e quantas (até 256, mais que uma pilha). */
    public record Ghost(int slot, ItemStack item, int count) {
        public static final Codec<Ghost> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("slot").forGetter(Ghost::slot),
                ItemStack.CODEC.fieldOf("item").forGetter(Ghost::item),
                Codec.INT.fieldOf("count").forGetter(Ghost::count)).apply(i, Ghost::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Ghost> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Ghost::slot, ItemStack.STREAM_CODEC, Ghost::item, ByteBufCodecs.VAR_INT, Ghost::count, Ghost::new);
    }

    private final String material;

    public GolemPlacerItem(Properties properties, String material) {
        super(properties);
        this.material = material;
    }

    public String material() {
        return this.material;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.thaumcraft.golem_" + this.material);
    }

    /** O {@code onItemUseFirst}: chamado antes de o bloco reagir (senão o baú se abriria em vez de o golem nascer). */
    public static InteractionResult placeFirst(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof GolemPlacerItem placer) || player.isShiftKeyDown()) return InteractionResult.PASS;
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        BlockPos clicked = hit.getBlockPos();
        BlockState state = level.getBlockState(clicked);
        Direction side = hit.getDirection();
        BlockPos at = clicked.relative(side);
        double lift = 0.0;
        if (side == Direction.UP && state.getBlock() instanceof FenceBlock || state.is(Blocks.NETHER_BRICK_FENCE)) lift = 0.5;
        if (placer.spawnCreature(server, at.getX() + 0.5, at.getY() + lift, at.getZ() + 0.5, side.get3DDataValue(), stack, player)
                && !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    /** O {@code spawnCreature}: o golem nasce com tudo o que o item guardava. */
    public boolean spawnCreature(ServerLevel level, double x, double y, double z, int side, ItemStack stack, Player player) {
        GolemEntity golem = TCEntities.GOLEM.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        if (golem == null) return false;
        golem.init(GolemEntity.typeIndex(this.material), stack.has(TCComponents.GOLEM_ADVANCED));
        float yaw = level.getRandom().nextFloat() * 360.0f;
        golem.snapTo(x, y, z, yaw, 0.0f);
        golem.setYHeadRot(yaw);
        golem.setHome(BlockPos.containing(x, y, z));
        Integer core = stack.get(TCComponents.GOLEM_CORE);
        if (core != null) golem.setCore((byte) (int) core);
        List<Byte> saved = stack.get(TCComponents.GOLEM_UPGRADES);
        if (saved != null) {
            for (int a = 0; a < golem.upgrades.length; a++) golem.upgrades[a] = a < saved.size() ? saved.get(a) : -1;
        }
        String deco = stack.getOrDefault(TCComponents.GOLEM_DECO, "");
        golem.decoration = deco;
        golem.setGolemDecoration(deco);
        golem.setup(side);
        golem.setOwner(player.getName().getString());
        golem.setMarkers(stack.getOrDefault(TCComponents.GOLEM_MARKERS, List.of()));
        for (int a = 0; a < golem.upgrades.length; a++) golem.setUpgrade(a, golem.upgrades[a]);
        golem.setupGolem();
        golem.setupGolemInventory();
        if (stack.has(DataComponents.CUSTOM_NAME)) {
            golem.setCustomName(stack.getHoverName());
            golem.setPersistenceRequired();
        }
        for (Ghost ghost : stack.getOrDefault(TCComponents.GOLEM_INVENTORY, List.<Ghost>of())) {
            if (ghost.slot() < golem.inventory.slotCount) golem.inventory.setItem(ghost.slot(), ghost.item().copyWithCount(ghost.count()));
        }
        level.addFreshEntity(golem);
        return true;
    }

    /** O item que o sino devolve ao recolher um golem (sem agachar: com tudo o que ele tinha). */
    public static ItemStack pickUp(GolemEntity golem, boolean keepEverything) {
        String name = golem.material();
        Item item = net.thaumcraft.registry.TCItems.GOLEM_PLACERS.get(name);
        ItemStack dropped = new ItemStack(item);
        if (golem.advanced) dropped.set(TCComponents.GOLEM_ADVANCED, net.minecraft.util.Unit.INSTANCE);
        if (!keepEverything) return dropped;
        if (golem.hasCustomName()) dropped.set(DataComponents.CUSTOM_NAME, golem.getCustomName());
        if (!golem.decoration.isEmpty()) dropped.set(TCComponents.GOLEM_DECO, golem.decoration);
        if (golem.getCore() > -1) dropped.set(TCComponents.GOLEM_CORE, (int) golem.getCore());
        List<Byte> ups = new ArrayList<>();
        for (byte b : golem.upgrades) ups.add(b);
        dropped.set(TCComponents.GOLEM_UPGRADES, ups);
        dropped.set(TCComponents.GOLEM_MARKERS, List.copyOf(golem.getMarkers()));
        List<Ghost> ghosts = new ArrayList<>();
        for (int a = 0; a < golem.inventory.slotCount; a++) {
            ItemStack in = golem.inventory.getItem(a);
            if (!in.isEmpty()) ghosts.add(new Ghost(a, in.copyWithCount(1), in.getCount()));
        }
        dropped.set(TCComponents.GOLEM_INVENTORY, ghosts);
        return dropped;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        Integer core = stack.get(TCComponents.GOLEM_CORE);
        if (core != null && core >= 0 && core < net.thaumcraft.api.golems.GolemTypes.CORES.length) {
            lines.accept(Component.translatable("item.thaumcraft.golem_core").append(": ")
                    .append(Component.translatable("item.thaumcraft.golem_core_" + net.thaumcraft.api.golems.GolemTypes.CORES[core])
                            .withStyle(ChatFormatting.GOLD)));
        }
        if (stack.has(TCComponents.GOLEM_ADVANCED)) lines.accept(Component.translatable("tc.adv"));
        List<Byte> ups = stack.get(TCComponents.GOLEM_UPGRADES);
        if (ups != null) {
            var text = Component.empty().withStyle(ChatFormatting.BLUE);
            for (byte b : ups) {
                if (b > -1) text.append(Component.translatable("item.thaumcraft.golem_upgrade_" + GolemUpgradeItem.NAMES[b])).append(" ");
            }
            lines.accept(text);
        }
        List<Marker> markers = stack.get(TCComponents.GOLEM_MARKERS);
        if (markers != null) {
            lines.accept(Component.literal(markers.size() + " ").append(Component.translatable("tc.markedloc")).withStyle(ChatFormatting.DARK_PURPLE));
        }
        String deco = stack.get(TCComponents.GOLEM_DECO);
        if (deco != null) {
            var text = Component.empty().withStyle(ChatFormatting.DARK_GREEN);
            String[] order = {"H", "G", "B", "F", "R", "V", "P"};
            for (int i = 0; i < order.length; i++) {
                if (deco.contains(order[i])) {
                    text.append(Component.translatable("item.thaumcraft.golem_decoration_" + GolemDecorationItem.NAMES[i])).append(" ");
                }
            }
            lines.accept(text);
        }
    }
}
