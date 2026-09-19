package net.thaumcraft.item;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.entity.WardedBlockEntity;
import net.thaumcraft.registry.TCComponents;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * O arquiteto da 4.2.3.5: o {@code IArchitect} dos focos de troca e de proteção, a área guardada na varinha
 * ({@code WandManager.getAreaX/Y/Z}, {@code getAreaDim}) e a tecla de alternância ({@code toggleMisc}, o G).
 *
 * <p>Os lados são os números do jogo antigo (0 embaixo, 1 em cima, 2 norte, 3 sul, 4 oeste, 5 leste), como o
 * {@code get3DDataValue} de hoje.
 */
public final class Architect {
    public enum Axis { X, Y, Z }

    /** A tecla G: o original manda o número 1 do {@code PacketItemKeyToServer}. */
    public record Key(int key) implements CustomPacketPayload {
        public static final Type<Key> TYPE = new Type<>(Thaumcraft.id("item_key"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Key> CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, Key::key, Key::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    private Architect() {
    }

    public static void init() {
        PayloadTypeRegistry.serverboundPlay().register(Key.TYPE, Key.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Key.TYPE, (payload, context) -> context.server().execute(() -> {
            Player player = context.player();
            ItemStack held = player.getMainHandItem();
            if (payload.key() == 1 && held.getItem() instanceof WandItem) toggleMisc(held, player);
        }));
    }

    // ----------------------------------------------------------------- a área na varinha

    private static int[] raw(ItemStack wand) {
        List<Integer> list = wand.get(TCComponents.WAND_AREA);
        int[] out = {-1, -1, -1, 0};
        if (list != null) for (int i = 0; i < Math.min(4, list.size()); i++) out[i] = list.get(i);
        return out;
    }

    private static void store(ItemStack wand, int[] area) {
        wand.set(TCComponents.WAND_AREA, List.of(area[0], area[1], area[2], area[3]));
    }

    private static int max(ItemStack wand) {
        FocusItem focus = Focuses.on(wand);
        return focus == null ? 1 : focus.maxAreaSize(WandItem.focusStack(wand));
    }

    private static int area(ItemStack wand, int index) {
        int a = raw(wand)[index];
        int max = max(wand);
        return a < 0 || a > max ? max : a;
    }

    public static int areaX(ItemStack wand) {
        return area(wand, 0);
    }

    public static int areaY(ItemStack wand) {
        return area(wand, 1);
    }

    public static int areaZ(ItemStack wand) {
        return area(wand, 2);
    }

    public static int dim(ItemStack wand) {
        return raw(wand)[3];
    }

    /** O foco preso tem o arquiteto? (O {@code getArchitectBlocks} da varinha só responde nesse caso.) */
    public static boolean active(ItemStack wand) {
        FocusItem focus = Focuses.on(wand);
        return focus != null && (focus.type().equals("trade") || focus.type().equals("warding"))
                && FocusItem.isUpgradedWith(WandItem.focusStack(wand), FocusUpgradeTable.ARCHITECT);
    }

    /**
     * O {@code toggleMisc}: agachado, passa a dimensão que se muda (todas, uma, a outra — e a terceira, fora na troca);
     * de pé, cresce a dimensão escolhida em um, voltando a zero depois do máximo.
     */
    public static void toggleMisc(ItemStack wand, Player player) {
        if (!active(wand)) return;
        int[] area = {areaX(wand), areaY(wand), areaZ(wand), dim(wand)};
        if (player.isShiftKeyDown()) {
            if (++area[3] > ("trade".equals(Focuses.on(wand).type()) ? 2 : 3)) area[3] = 0;
        } else {
            switch (area[3]) {
                case 0 -> {
                    area[0]++;
                    area[2]++;
                    area[1]++;
                }
                case 1 -> area[0]++;
                case 2 -> area[2]++;
                case 3 -> area[1]++;
                default -> {
                }
            }
            int max = max(wand);
            for (int i = 0; i < 3; i++) if (area[i] > max) area[i] = 0;
        }
        store(wand, area);
    }

    // ----------------------------------------------------------------- os blocos

    /** O {@code getArchitectBlocks} do foco preso; {@code null} sem arquiteto. */
    @Nullable
    public static List<BlockPos> blocks(ItemStack wand, Level level, BlockPos pos, int side, Player player) {
        if (!active(wand)) return null;
        return "trade".equals(Focuses.on(wand).type()) ? tradeBlocks(wand, level, pos, side, player)
                : wardingBlocks(wand, level, pos, side, player, true);
    }

    /** O {@code BlockUtils.isBlockExposed}: algum vizinho não é opaco. */
    private static boolean exposed(Level level, BlockPos pos) {
        for (Direction d : Direction.values()) {
            if (!level.getBlockState(pos.relative(d)).canOcclude()) return true;
        }
        return false;
    }

    /** O {@code ItemFocusTrade.getArchitectBlocks}: os iguais à mostra, no plano da face, até a área. */
    public static List<BlockPos> tradeBlocks(ItemStack wand, Level level, BlockPos pos, int side, Player player) {
        BlockState target = level.getBlockState(pos);
        List<BlockPos> out = new ArrayList<>();
        Set<BlockPos> checked = new HashSet<>();
        int sx = side != 2 && side != 3 ? areaX(wand) : areaZ(wand);
        int sz = side != 2 && side != 3 ? areaZ(wand) : areaX(wand);
        tradeNeighbours(level, pos, target, pos, side, sx, sz, out, checked, player);
        return out;
    }

    private static void tradeNeighbours(Level level, BlockPos origin, BlockState target, BlockPos pos, int side, int sizeX,
                                        int sizeZ, List<BlockPos> list, Set<BlockPos> checked, Player player) {
        if (!checked.add(pos)) return;
        int dx = Math.abs(pos.getX() - origin.getX()), dy = Math.abs(pos.getY() - origin.getY()), dz = Math.abs(pos.getZ() - origin.getZ());
        switch (side) {
            case 0, 1 -> {
                if (dx > sizeX || dz > sizeZ) return;
            }
            case 2, 3 -> {
                if (dx > sizeX || dy > sizeZ) return;
            }
            default -> {
                if (dy > sizeX || dz > sizeZ) return;
            }
        }
        BlockState state = level.getBlockState(pos);
        if (state == target && exposed(level, pos) && !state.isAir() && state.getDestroySpeed(level, pos) >= 0.0f
                && level.mayInteract(player, pos)) {
            list.add(pos);
            for (Direction dir : Direction.values()) {
                if (dir.get3DDataValue() != side && dir.getOpposite().get3DDataValue() != side) {
                    tradeNeighbours(level, origin, target, pos.relative(dir), side, sizeX, sizeZ, list, checked, player);
                }
            }
        }
    }

    /**
     * O {@code ItemFocusWarding.getArchitectBlocks}: sem arquiteto, a área é zero (só o bloco da mira). Começando num
     * bloco protegido, junta os protegidos do mesmo dono; senão, os blocos maciços sem miolo.
     */
    public static List<BlockPos> wardingBlocks(ItemStack wand, Level level, BlockPos pos, int side, Player player, boolean architect) {
        List<BlockPos> out = new ArrayList<>();
        Set<BlockPos> checked = new HashSet<>();
        boolean tiles = level.getBlockEntity(pos) instanceof WardedBlockEntity;
        int sx = 0, sy = 0, sz = 0;
        if (architect && active(wand)) {
            sx = areaX(wand);
            sy = areaY(wand);
            sz = areaZ(wand);
        }
        if (side != 2 && side != 3) wardingNeighbours(level, pos, pos, side, sx, sy, sz, out, checked, player, tiles);
        else wardingNeighbours(level, pos, pos, side, sz, sy, sx, out, checked, player, tiles);
        return out;
    }

    private static void wardingNeighbours(Level level, BlockPos origin, BlockPos pos, int side, int sizeX, int sizeY, int sizeZ,
                                          List<BlockPos> list, Set<BlockPos> checked, Player player, boolean tiles) {
        if (!checked.add(pos)) return;
        int dx = Math.abs(pos.getX() - origin.getX()), dy = Math.abs(pos.getY() - origin.getY()), dz = Math.abs(pos.getZ() - origin.getZ());
        switch (side) {
            case 0, 1 -> {
                if (dx > sizeX || dz > sizeZ || dy > sizeY) return;
            }
            case 2, 3 -> {
                if (dx > sizeX || dy > sizeZ || dz > sizeY) return;
            }
            default -> {
                if (dy > sizeX || dz > sizeZ || dx > sizeY) return;
            }
        }
        var tile = level.getBlockEntity(pos);
        boolean solid = level.getBlockState(pos).isSolidRender();
        if (tiles && !(tile instanceof WardedBlockEntity)) return;
        if (!tiles && !(tile == null && solid)) return;
        if (tiles && tile instanceof WardedBlockEntity warded && warded.owner() != Focuses.wardOwner(player)) return;
        if (level.getBlockState(pos).isAir()) return;
        list.add(pos);
        for (Direction dir : Direction.values()) {
            wardingNeighbours(level, origin, pos.relative(dir), side, sizeX, sizeY, sizeZ, list, checked, player, tiles);
        }
    }

    /** O {@code showAxis}: que setas aparecem na mira, pela dimensão escolhida e pelo lado. */
    public static boolean showAxis(ItemStack wand, int side, Axis axis) {
        if (!active(wand)) return false;
        int dim = dim(wand);
        if ("trade".equals(Focuses.on(wand).type())) {
            return switch (side) {
                case 0, 1 -> axis == Axis.X && (dim == 0 || dim == 1) || axis == Axis.Z && (dim == 0 || dim == 2);
                case 2, 3 -> axis == Axis.Y && (dim == 0 || dim == 1) || axis == Axis.X && (dim == 0 || dim == 2);
                default -> axis == Axis.Y && (dim == 0 || dim == 1) || axis == Axis.Z && (dim == 0 || dim == 2);
            };
        }
        if (dim == 0) return true;
        return switch (side) {
            case 0, 1 -> axis == Axis.X && dim == 1 || axis == Axis.Z && dim == 2 || axis == Axis.Y && dim == 3;
            case 2, 3 -> axis == Axis.Y && dim == 1 || axis == Axis.X && dim == 2 || axis == Axis.Z && dim == 3;
            default -> axis == Axis.Y && dim == 1 || axis == Axis.Z && dim == 2 || axis == Axis.X && dim == 3;
        };
    }
}
