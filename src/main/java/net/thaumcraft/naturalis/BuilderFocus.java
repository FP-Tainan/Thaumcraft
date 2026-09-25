package net.thaumcraft.naturalis;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.item.FocusItem;
import net.thaumcraft.item.Focuses;
import net.thaumcraft.item.WandItem;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCSounds;

import java.util.ArrayList;
import java.util.List;

/**
 * O Foco de Construção: o {@code BuilderFocusItem} do Magia Naturalis 0.5.0.
 *
 * <p>Apontado para um bloco, ele levanta uma forma de blocos iguais a partir da face mirada — um cubo, um plano,
 * um plano estendido ou uma esfera — cobrando cinco de Ordo por bloco e tirando os blocos do inventário de quem
 * constrói (quem está no criativo não paga nem gasta).
 *
 * <p>O original trocava forma, tamanho e bloco por teclas próprias. Aqui, para não inventar tecla nova, isso se faz
 * com a varinha na mão: agachado, o clique no ar passa o tamanho de um em um, e o clique num bloco troca a forma e
 * marca aquele bloco como o de construir.
 */
public final class BuilderFocus {
    /** O que o foco cobra por bloco posto, como no original. */
    public static final AspectList COST = new AspectList().add(Aspects.ORDER, 5).add(Aspects.EARTH, 5);

    /** As quatro formas do original, na ordem em que ele as lista. */
    public enum Shape {
        CUBE, PLANE, PLANE_EXTEND, SPHERE;

        public Shape next() {
            return values()[(this.ordinal() + 1) % values().length];
        }
    }

    private BuilderFocus() {
    }

    public static void init() {
        Focuses.register("build", BuilderFocus::cast);
        // o getPossibleUpgradesByRank do original: Ampliação e Frugal em qualquer posto
        var postos = java.util.List.of(net.thaumcraft.item.FocusUpgradeTable.ENLARGE,
                net.thaumcraft.item.FocusUpgradeTable.FRUGAL);
        net.thaumcraft.api.FocusUpgrades.ranks("build",
                java.util.List.of(postos, postos, postos, postos, postos));
    }

    // ------------------------------------------------------------------ o que o foco guarda

    public static Shape shape(ItemStack focus) {
        Integer value = focus.get(TCComponents.BUILDER_SHAPE);
        return value == null ? Shape.CUBE : Shape.values()[Math.floorMod(value, Shape.values().length)];
    }

    public static int size(ItemStack focus) {
        Integer value = focus.get(TCComponents.BUILDER_SIZE);
        return value == null ? 1 : Math.max(1, value);
    }

    /** O maior tamanho que o foco alcança: três, e mais três por posto de Ampliação. */
    public static int maxSize(ItemStack focus) {
        return 3 + FocusItem.level(focus, net.thaumcraft.item.FocusUpgradeTable.ENLARGE) * 3 + 1;
    }

    /** O bloco que ele constrói, quando alguém marcou um; sem marca, ele copia o bloco da mira. */
    public static Block picked(ItemStack focus) {
        String name = focus.get(TCComponents.BUILDER_BLOCK);
        if (name == null) return null;
        return BuiltInRegistries.BLOCK.getValue(Identifier.parse(name));
    }

    // ------------------------------------------------------------------ o uso

    private static boolean cast(Level level, Player player, ItemStack wand, FocusItem focus) {
        ItemStack stack = WandItem.focusStack(wand);
        HitResult mira = Focuses.targetBlock(level, player);
        boolean sneaking = player.isShiftKeyDown();

        // agachado, o foco se ajusta em vez de construir
        if (sneaking) {
            if (mira instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK) {
                Shape novo = shape(stack).next();
                stack.set(TCComponents.BUILDER_SHAPE, novo.ordinal());
                Block block = level.getBlockState(hit.getBlockPos()).getBlock();
                stack.set(TCComponents.BUILDER_BLOCK, BuiltInRegistries.BLOCK.getKey(block).toString());
                say(player, Component.translatable("focus.build.shape")
                        .append(": " + novo.name().toLowerCase() + ", " + block.getName().getString()));
            } else {
                int tamanho = size(stack) + 1;
                if (tamanho > maxSize(stack)) tamanho = 1;
                stack.set(TCComponents.BUILDER_SIZE, tamanho);
                say(player, Component.translatable("focus.build.size").append(": " + tamanho));
            }
            return true;
        }

        if (!(mira instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) return false;
        Block block = picked(stack);
        if (block == null) block = level.getBlockState(hit.getBlockPos()).getBlock();
        if (block == net.minecraft.world.level.block.Blocks.AIR) return false;

        List<BlockPos> forma = plot(level, hit, shape(stack), size(stack));
        if (forma.isEmpty()) return false;

        int postos = 0;
        for (BlockPos pos : forma) {
            if (!player.getAbilities().instabuild) {
                if (!WandItem.consumeFocus(wand, COST, true, player)) break;
                if (!take(player, block)) break;
            }
            level.setBlockAndUpdate(pos, block.defaultBlockState());
            postos++;
        }
        if (postos == 0) {
            level.playSound(null, player, TCSounds.WAND_FAIL.value(), SoundSource.PLAYERS, 0.5f, 0.8f);
            return false;
        }
        level.playSound(null, player, TCSounds.WAND.value(), SoundSource.PLAYERS, 0.25f,
                0.9f + level.getRandom().nextFloat() * 0.2f);
        return true;
    }

    /** Onde a forma cai, a partir da face mirada. */
    private static List<BlockPos> plot(Level level, BlockHitResult hit, Shape shape, int size) {
        Direction face = hit.getDirection();
        BlockPos base = hit.getBlockPos().relative(face);
        List<BlockPos> saida = new ArrayList<>();
        int raio = size - 1;
        switch (shape) {
            case CUBE -> {
                BlockPos centro = base.relative(face, raio);
                for (int x = -raio; x <= raio; x++) {
                    for (int y = -raio; y <= raio; y++) {
                        for (int z = -raio; z <= raio; z++) saida.add(centro.offset(x, y, z));
                    }
                }
            }
            case SPHERE -> {
                BlockPos centro = base.relative(face, raio);
                for (int x = -raio; x <= raio; x++) {
                    for (int y = -raio; y <= raio; y++) {
                        for (int z = -raio; z <= raio; z++) {
                            if (x * x + y * y + z * z <= raio * raio + raio) saida.add(centro.offset(x, y, z));
                        }
                    }
                }
            }
            case PLANE, PLANE_EXTEND -> {
                // o plano é a camada colada na face; o estendido cresce para dentro dela
                int fundura = shape == Shape.PLANE ? 0 : raio;
                for (int a = -raio; a <= raio; a++) {
                    for (int b = -raio; b <= raio; b++) {
                        for (int c = 0; c <= fundura; c++) {
                            saida.add(offsetOnFace(base, face, a, b).relative(face, c));
                        }
                    }
                }
            }
        }
        saida.removeIf(pos -> !level.getBlockState(pos).canBeReplaced());
        return saida;
    }

    /** Anda no plano da face, que é o que o {@code plot2DPlane} do original faz. */
    private static BlockPos offsetOnFace(BlockPos base, Direction face, int a, int b) {
        return switch (face.getAxis()) {
            case Y -> base.offset(a, 0, b);
            case X -> base.offset(0, a, b);
            case Z -> base.offset(a, b, 0);
        };
    }

    /** Tira um bloco do inventário de quem constrói. */
    private static boolean take(Player player, Block block) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.is(block.asItem())) continue;
            stack.shrink(1);
            return true;
        }
        return false;
    }

    private static void say(Player player, Component message) {
        if (player instanceof ServerPlayer sent) sent.sendSystemMessage(message, true);
    }

    /** O que o foco mostra na dica: forma, tamanho e bloco. */
    public static void tooltip(ItemStack focus, java.util.function.Consumer<Component> lines) {
        lines.accept(Component.translatable("focus.build.shape").append(": " + shape(focus).name().toLowerCase()));
        lines.accept(Component.translatable("focus.build.size").append(": " + size(focus)));
        Block block = picked(focus);
        if (block != null) lines.accept(Component.translatable("focus.build.block").append(": ").append(block.getName()));
    }

    /** O bloco que a forma vai usar, para quem quiser desenhar a prévia. */
    public static BlockState preview(ItemStack focus, Level level, BlockPos pos) {
        Block block = picked(focus);
        return block == null ? level.getBlockState(pos) : block.defaultBlockState();
    }
}
