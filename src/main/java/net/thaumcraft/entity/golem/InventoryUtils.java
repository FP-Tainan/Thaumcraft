package net.thaumcraft.entity.golem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * O {@code InventoryUtils} da 4.2.3.5, na parte que os golens usam: pôr e tirar de um baú pela face marcada, com a
 * mesma ordem de casas e as mesmas comparações do original.
 *
 * <p>O que era {@code null} no original é a pilha vazia aqui. O "dicionário de minérios" de então virou as etiquetas
 * comuns ({@code c:}) de hoje: duas coisas "casam pelo dicionário" quando dividem alguma etiqueta {@code c:}. O
 * "dano" de então era também a variante (a lã de cada cor era a mesma coisa com dano diferente); hoje cada variante
 * é uma coisa própria, e o dano é só o desgaste.
 */
public final class InventoryUtils {
    private InventoryUtils() {
    }

    /** O {@code placeItemStackIntoInventory}: o que sobrou de pôr a pilha (vazia se coube tudo). */
    public static ItemStack placeItemStackIntoInventory(ItemStack stack, Container inventory, int side, boolean doit) {
        ItemStack itemstack = stack.copy();
        ItemStack rest = insertStack(inventory, itemstack, side, doit);
        if (!rest.isEmpty()) return rest.copy();
        if (doit) inventory.setChanged();
        return ItemStack.EMPTY;
    }

    /** O {@code insertStack}: primeiro nas casas que já têm a mesma coisa, depois nas vazias — e na outra metade do baú duplo. */
    public static ItemStack insertStack(Container inventory, ItemStack stack1, int side, boolean doit) {
        if (inventory instanceof WorldlyContainer sided && side > -1) {
            int[] slots = sided.getSlotsForFace(Direction.from3DDataValue(side));
            for (int j = 0; j < slots.length && !stack1.isEmpty(); j++) {
                if (!inventory.getItem(slots[j]).isEmpty() && isItemEqual(inventory.getItem(slots[j]), stack1)) {
                    stack1 = attemptInsertion(inventory, stack1, slots[j], side, doit);
                }
            }
            for (int j = 0; j < slots.length && !stack1.isEmpty(); j++) {
                stack1 = attemptInsertion(inventory, stack1, slots[j], side, doit);
            }
        } else {
            int k = inventory.getContainerSize();
            for (int l = 0; l < k && !stack1.isEmpty(); l++) {
                if (!inventory.getItem(l).isEmpty() && isItemEqual(inventory.getItem(l), stack1)) {
                    stack1 = attemptInsertion(inventory, stack1, l, side, doit);
                }
            }
            if (!stack1.isEmpty()) {
                ChestBlockEntity dc = inventory instanceof BlockEntity be ? getDoubleChest(be) : null;
                if (dc != null) {
                    for (int l = 0; l < dc.getContainerSize() && !stack1.isEmpty(); l++) {
                        if (!dc.getItem(l).isEmpty() && isItemEqual(dc.getItem(l), stack1)) {
                            stack1 = attemptInsertion(dc, stack1, l, side, doit);
                        }
                    }
                }
                if (!stack1.isEmpty()) {
                    for (int l = 0; l < k && !stack1.isEmpty(); l++) {
                        stack1 = attemptInsertion(inventory, stack1, l, side, doit);
                    }
                    // o original volta à outra metade só atrás das casas com a mesma coisa (nunca das vazias)
                    if (!stack1.isEmpty() && dc != null) {
                        for (int l = 0; l < dc.getContainerSize() && !stack1.isEmpty(); l++) {
                            if (!dc.getItem(l).isEmpty() && isItemEqual(dc.getItem(l), stack1)) {
                                stack1 = attemptInsertion(dc, stack1, l, side, doit);
                            }
                        }
                    }
                }
            }
        }
        return stack1.isEmpty() ? ItemStack.EMPTY : stack1;
    }

    private static ItemStack attemptInsertion(Container inventory, ItemStack stack, int slot, int side, boolean doit) {
        ItemStack slotStack = inventory.getItem(slot);
        if (canInsertItemToInventory(inventory, stack, slot, side)) {
            boolean flag = false;
            if (slotStack.isEmpty()) {
                if (inventory.getMaxStackSize() < stack.getCount()) {
                    ItemStack in = stack.split(inventory.getMaxStackSize());
                    if (doit) inventory.setItem(slot, in);
                } else {
                    if (doit) inventory.setItem(slot, stack);
                    stack = ItemStack.EMPTY;
                }
                flag = true;
            } else if (areItemStacksEqualStrict(slotStack, stack)) {
                int k = Math.min(inventory.getMaxStackSize() - slotStack.getCount(), stack.getMaxStackSize() - slotStack.getCount());
                int l = Math.min(stack.getCount(), k);
                stack.shrink(l);
                if (doit) slotStack.grow(l);
                flag = l > 0;
            }
            if (flag && doit) inventory.setChanged();
        }
        return stack;
    }

    /** O {@code getFirstItemInInventory}: a primeira coisa que houver, até {@code size} dela. */
    public static ItemStack getFirstItemInInventory(Container inventory, int size, int side, boolean doit) {
        ItemStack stack1 = ItemStack.EMPTY;
        int[] slots = slotsFor(inventory, side);
        for (int slot : slots) {
            if (stack1.isEmpty() && !inventory.getItem(slot).isEmpty()) {
                stack1 = inventory.getItem(slot).copyWithCount(size);
            }
            if (!stack1.isEmpty()) stack1 = attemptExtraction(inventory, stack1, slot, side, false, false, false, doit);
            if (!stack1.isEmpty()) break;
        }
        if (!stack1.isEmpty()) return stack1.copy();
        if (doit) inventory.setChanged();
        return ItemStack.EMPTY;
    }

    public static boolean inventoryContains(Container inventory, ItemStack stack, int side, boolean useOre, boolean ignoreDamage, boolean ignoreNBT) {
        return !extractStack(inventory, stack, side, useOre, ignoreDamage, ignoreNBT, false).isEmpty();
    }

    /** O {@code extractStack}: tira da primeira casa que casar, até o tamanho pedido. */
    public static ItemStack extractStack(Container inventory, ItemStack stack1, int side, boolean useOre, boolean ignoreDamage,
                                         boolean ignoreNBT, boolean doit) {
        ItemStack out = ItemStack.EMPTY;
        for (int slot : slotsFor(inventory, side)) {
            if (stack1.isEmpty() || !out.isEmpty()) break;
            out = attemptExtraction(inventory, stack1, slot, side, useOre, ignoreDamage, ignoreNBT, doit);
        }
        return out.isEmpty() ? ItemStack.EMPTY : out.copy();
    }

    /** O {@code attemptExtraction}: se a casa casar, sai o pedido (ou o que tiver, se tiver menos). */
    public static ItemStack attemptExtraction(Container inventory, ItemStack stack, int slot, int side, boolean useOre,
                                              boolean ignoreDamage, boolean ignoreNBT, boolean doit) {
        ItemStack slotStack = inventory.getItem(slot);
        if (!canExtractItemFromInventory(inventory, slotStack, slot, side)) return ItemStack.EMPTY;
        if (!areItemStacksEqual(slotStack, stack, useOre, ignoreDamage, ignoreNBT)) return ItemStack.EMPTY;
        ItemStack out = slotStack.copyWithCount(stack.getCount());
        int k = stack.getCount() - slotStack.getCount();
        if (k >= 0) {
            out.shrink(k);
            if (doit) inventory.setItem(slot, ItemStack.EMPTY);
        } else if (doit) {
            slotStack.shrink(out.getCount());
            inventory.setItem(slot, slotStack);
        }
        if (doit) inventory.setChanged();
        return out;
    }

    public static boolean canInsertItemToInventory(Container inventory, ItemStack stack, int slot, int side) {
        return !stack.isEmpty() && inventory.canPlaceItem(slot, stack)
                && (!(inventory instanceof WorldlyContainer sided) || side < 0
                || sided.canPlaceItemThroughFace(slot, stack, Direction.from3DDataValue(side)));
    }

    public static boolean canExtractItemFromInventory(Container inventory, ItemStack stack, int slot, int side) {
        return !stack.isEmpty() && (!(inventory instanceof WorldlyContainer sided) || side < 0
                || sided.canTakeItemThroughFace(slot, stack, Direction.from3DDataValue(side)));
    }

    /** As casas que a face enxerga (todas, para um baú comum). */
    public static int[] slotsFor(Container inventory, int side) {
        if (inventory instanceof WorldlyContainer sided && side > -1) return sided.getSlotsForFace(Direction.from3DDataValue(side));
        int[] all = new int[inventory.getContainerSize()];
        for (int a = 0; a < all.length; a++) all[a] = a;
        return all;
    }

    /** O {@code isItemEqual} de então: a mesma coisa e o mesmo dano. */
    public static boolean isItemEqual(ItemStack a, ItemStack b) {
        return !a.isEmpty() && !b.isEmpty() && a.is(b.getItem()) && a.getDamageValue() == b.getDamageValue();
    }

    /** O {@code areItemStackTagsEqual} de então: os componentes, fora o desgaste (que era o "dano"). */
    public static boolean tagsEqual(ItemStack a, ItemStack b) {
        if (a.isEmpty() || b.isEmpty()) return a.isEmpty() && b.isEmpty();
        ItemStack ca = a.copyWithCount(1), cb = b.copyWithCount(1);
        ca.remove(DataComponents.DAMAGE);
        cb.remove(DataComponents.DAMAGE);
        return ItemStack.isSameItemSameComponents(ca, cb);
    }

    public static boolean areItemStacksEqualStrict(ItemStack stack0, ItemStack stack1) {
        return areItemStacksEqual(stack0, stack1, false, false, false);
    }

    /** O {@code areItemStacksEqual}: coisa, dano (a não ser que se ignore) e componentes (a não ser que se ignore). */
    public static boolean areItemStacksEqual(ItemStack stack0, ItemStack stack1, boolean useOre, boolean ignoreDamage, boolean ignoreNBT) {
        if (stack0.isEmpty() || stack1.isEmpty()) return stack0.isEmpty() && stack1.isEmpty();
        if (useOre && oreMatch(stack0, stack1)) return true;
        boolean tags = ignoreNBT || tagsEqual(stack0, stack1);
        boolean damageDiffers = stack0.getDamageValue() != stack1.getDamageValue();
        if (ignoreDamage && stack0.isDamageableItem() && stack1.isDamageableItem()) damageDiffers = false;
        return stack0.is(stack1.getItem()) && !damageDiffers && tags;
    }

    /** O dicionário de minérios de então: as duas coisas dividem alguma etiqueta comum {@code c:}. */
    public static boolean oreMatch(ItemStack a, ItemStack b) {
        return commonTags(a.getItem()).stream().anyMatch(b::is);
    }

    /** As etiquetas comuns ({@code c:}) de uma coisa — o que o dicionário de minérios de então sabia dela. */
    public static List<TagKey<Item>> commonTags(Item item) {
        return item.builtInRegistryHolder().tags().filter(t -> t.location().getNamespace().equals("c")).toList();
    }

    /** O {@code getDoubleChest}: a outra metade de um baú duplo, se houver. */
    @Nullable
    public static ChestBlockEntity getDoubleChest(@Nullable BlockEntity tile) {
        if (!(tile instanceof ChestBlockEntity) || tile.getLevel() == null) return null;
        var state = tile.getBlockState();
        if (!state.hasProperty(ChestBlock.TYPE) || state.getValue(ChestBlock.TYPE) == ChestType.SINGLE) return null;
        BlockPos other = ChestBlock.getConnectedBlockPos(tile.getBlockPos(), state);
        return tile.getLevel().getBlockEntity(other) instanceof ChestBlockEntity chest ? chest : null;
    }

    /** O que há de baú (ou coisa que guarda) naquele bloco. */
    @Nullable
    public static Container containerAt(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof Container container ? container : null;
    }

    /** Onde fica o baú (para medir distâncias), que no original era o próprio {@code TileEntity}. */
    public static BlockPos posOf(Container container) {
        return container instanceof BlockEntity be ? be.getBlockPos() : BlockPos.ZERO;
    }
}
