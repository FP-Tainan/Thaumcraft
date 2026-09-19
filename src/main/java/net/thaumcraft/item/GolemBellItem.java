package net.thaumcraft.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.thaumcraft.entity.GolemEntity;
import net.thaumcraft.entity.golem.Marker;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCItems;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * O sino do golem: o {@code ItemGolemBell} da 4.2.3.5.
 *
 * <p>Tocado num golem, o sino passa a falar com ele e copia as marcas dele. Tocado num bloco, põe ou tira uma marca
 * naquela face (e muda a cor dela, se o golem tiver a melhoria de ordem: agachado, tira). As marcas vão direto para o
 * golem. Batido num golem, recolhe o golem como item — com tudo, ou (agachado) largando o núcleo e, por sorte, as
 * melhorias.
 */
public class GolemBellItem extends Item {
    /** O nome das dezesseis cores, em inglês como no original. */
    public static final String[] COLOR_NAMES = {"White", "Orange", "Magenta", "Light Blue", "Yellow", "Lime", "Pink", "Gray",
            "Light Gray", "Cyan", "Purple", "Blue", "Brown", "Green", "Red", "Black"};
    /** As cores das marcas: o {@code UtilsFX.colors}. */
    public static final int[] COLORS = {15790320, 15435844, 12801229, 6719955, 14602026, 4312372, 14188952, 4408131, 10526880,
            2651799, 8073150, 2437522, 5320730, 3887386, 11743532, 1973019};

    /** O golem a que o sino fala: o número dele e a casa (para desenhar a runa da casa). */
    public record Link(int golemId, BlockPos home, int face) {
        public static final Codec<Link> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("golemid").forGetter(Link::golemId),
                BlockPos.CODEC.fieldOf("home").forGetter(Link::home),
                Codec.INT.fieldOf("face").forGetter(Link::face)).apply(i, Link::new));
        public static final StreamCodec<ByteBuf, Link> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Link::golemId, BlockPos.STREAM_CODEC, Link::home, ByteBufCodecs.VAR_INT, Link::face, Link::new);
    }

    public GolemBellItem(Properties properties) {
        super(properties);
    }

    public static List<Marker> getMarkers(ItemStack stack) {
        return new ArrayList<>(stack.getOrDefault(TCComponents.GOLEM_MARKERS, List.of()));
    }

    @Nullable
    private static GolemEntity linked(ItemStack stack, Level level) {
        Link link = stack.get(TCComponents.GOLEM_LINK);
        if (link == null) return null;
        return level.getEntity(link.golemId()) instanceof GolemEntity g ? g : null;
    }

    private static void unlink(ItemStack stack) {
        stack.remove(TCComponents.GOLEM_LINK);
        stack.remove(TCComponents.GOLEM_MARKERS);
    }

    /** O {@code changeMarkers}: põe, troca de cor ou tira a marca daquela face. */
    public static void changeMarkers(ItemStack stack, Player player, Level level, BlockPos pos, int side) {
        List<Marker> markers = getMarkers(stack);
        GolemEntity golem = linked(stack, level);
        boolean multi = golem != null && golem.getUpgradeAmount(4) > 0;
        int count = markers.size();
        int index = -1;
        int color = 0;
        if (!multi) {
            index = markers.indexOf(new Marker(pos, level, side, -1));
        } else {
            for (int a = -1; a < 16; a++) {
                index = markers.indexOf(new Marker(pos, level, side, a));
                color = a;
                if (index != -1) break;
            }
        }
        if (index >= 0) {
            markers.remove(index);
            if (multi && !player.isShiftKeyDown() && ++color <= 15) {
                markers.add(new Marker(pos, level, side, color));
                count++;
                if (level.isClientSide()) {
                    Component text = color > -1 ? Component.translatable("tc.markerchange", COLOR_NAMES[color])
                            : Component.translatable("tc.markerchangeany");
                    player.sendOverlayMessage(text);
                }
            }
        } else {
            markers.add(new Marker(pos, level, side, -1));
        }
        if (count != markers.size() && !level.isClientSide()) {
            stack.set(TCComponents.GOLEM_MARKERS, List.copyOf(markers));
            if (stack.has(TCComponents.GOLEM_LINK)) {
                if (golem != null) golem.setMarkers(markers);
                else unlink(stack);
            }
        }
        level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.7f, 1.0f + level.getRandom().nextFloat() * 0.1f);
    }

    /** O {@code onItemUseFirst}: antes de o bloco reagir, olha (vendo também a água) e marca a face. */
    public static InteractionResult markFirst(Player player, Level level, InteractionHand hand, BlockHitResult ignored) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(TCItems.GOLEM_BELL)) return InteractionResult.PASS;
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() == HitResult.Type.BLOCK) changeMarkers(stack, player, level, hit.getBlockPos(), hit.getDirection().get3DDataValue());
        return InteractionResult.SUCCESS;
    }

    /** O {@code itemInteractionForEntity}: liga o sino ao golem e copia as marcas dele. */
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof GolemEntity golem)) return InteractionResult.PASS;
        // o sino na mão é o que se edita (no criativo, o interactLivingEntity recebe uma cópia)
        ItemStack held = player.getItemInHand(hand);
        unlink(held);
        if (!player.level().isClientSide()) {
            held.set(TCComponents.GOLEM_MARKERS, List.copyOf(golem.getMarkers()));
            held.set(TCComponents.GOLEM_LINK, new Link(golem.getId(), golem.home(), golem.homeFacing));
            player.level().playSound(null, golem, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.7f,
                    1.0f + player.level().getRandom().nextFloat() * 0.1f);
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * O mesmo para o baú itinerante: só o dono recolhe o da água; o item leva a melhoria (e, com a ordem, o que tem
     * dentro); agachado, a melhoria cai por sorte e o que tem dentro sempre cai.
     */
    private static InteractionResult pickUpTrunk(Player player, Level level, net.thaumcraft.entity.TravelingTrunkEntity trunk) {
        int upgrade = trunk.getUpgrade();
        if (upgrade == 3 && !trunk.getOwnerName().equals(player.getName().getString())) return InteractionResult.PASS;
        if (level.isClientSide()) {
            trunk.spawnAnim();
            return InteractionResult.SUCCESS;
        }
        ServerLevel server = (ServerLevel) level;
        boolean sneak = player.isShiftKeyDown();
        if (sneak && upgrade > -1 && level.getRandom().nextBoolean()) {
            trunk.spawnAtLocation(server, new ItemStack(TCItems.GOLEM_UPGRADES.get(upgrade)), 0.5f);
        }
        trunk.spawnAtLocation(server, TrunkSpawnerItem.pickUp(trunk, sneak), 0.5f);
        if (upgrade != 4 || sneak) trunk.inventory.dropAll();
        trunk.playSound(net.thaumcraft.registry.TCSounds.ZAP.value(), 0.5f, 1.0f);
        trunk.discard();
        return InteractionResult.SUCCESS;
    }

    /** O {@code onLeftClickEntity}: recolhe o golem (com tudo, ou agachado largando núcleo e melhorias). */
    public static InteractionResult pickUp(Player player, Level level, InteractionHand hand, Entity entity) {
        if (!player.getItemInHand(hand).is(TCItems.GOLEM_BELL)) return InteractionResult.PASS;
        if (entity instanceof net.thaumcraft.entity.TravelingTrunkEntity trunk && !trunk.isRemoved()) return pickUpTrunk(player, level, trunk);
        if (!(entity instanceof GolemEntity golem) || golem.isRemoved()) return InteractionResult.PASS;
        if (level.isClientSide()) {
            golem.spawnAnim();
            return InteractionResult.SUCCESS;
        }
        ServerLevel server = (ServerLevel) level;
        boolean sneak = player.isShiftKeyDown();
        ItemStack dropped = GolemPlacerItem.pickUp(golem, !sneak);
        if (sneak) {
            if (golem.getCore() > -1) {
                Item core = TCItems.GOLEM_CORES.get(net.thaumcraft.api.golems.GolemTypes.CORES[golem.getCore()]);
                golem.spawnAtLocation(server, new ItemStack(core), 0.5f);
            }
            for (byte b : golem.upgrades) {
                if (b > -1 && level.getRandom().nextBoolean()) golem.spawnAtLocation(server, new ItemStack(TCItems.GOLEM_UPGRADES.get(b)), 0.5f);
            }
        }
        golem.spawnAtLocation(server, dropped, 0.5f);
        golem.dropStuff();
        golem.playSound(net.thaumcraft.registry.TCSounds.ZAP.value(), 0.5f, 1.0f);
        golem.discard();
        return InteractionResult.SUCCESS;
    }
}
