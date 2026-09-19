package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.inventory.ResearchTableMenu;
import net.thaumcraft.item.ScribingToolsItem;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.research.Hex;
import net.thaumcraft.research.Knowledges;
import net.thaumcraft.research.PlayerKnowledge;
import net.thaumcraft.research.Research;
import net.thaumcraft.research.ResearchNote;
import net.thaumcraft.research.ResearchNotes;
import net.thaumcraft.research.Researches;

import java.util.Map;

/**
 * A mesa de pesquisa: o {@code TileResearchTable} da 4.2.3.5, descompilado.
 *
 * <p>Guarda duas coisas — as ferramentas de escrita e a nota — e um punhado de aspectos de bônus que o lugar
 * dá: a cada meio minuto a mesa olha em volta, e o que ela acha perto (água, lava, terra, minério infundido,
 * redstone, estantes, noite escura, altura) pode render um ponto do aspecto daquilo, de graça, para quem
 * pesquisar ali.
 *
 * <p>Cada aspecto escrito na nota gasta um ponto — do bolso de quem pesquisa, ou do bônus da mesa quando o
 * bolso está vazio — e um risco de tinta. Apagar também gasta tinta.
 */
public class ResearchTableBlockEntity extends BaseContainerBlockEntity
        implements net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider<BlockPos> {
    public static final int INK = 0;
    public static final int NOTE = 1;

    private NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    private AspectList bonus = new AspectList();
    private int nextRecalc;

    public ResearchTableBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.RESEARCH_TABLE, pos, state);
    }

    public AspectList bonus() {
        return this.bonus;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ResearchTableBlockEntity table) {
        if (level.isClientSide() || table.nextRecalc++ <= 600) return;
        table.nextRecalc = 0;
        table.recalculateBonus((ServerLevel) level);
        table.sync();
    }

    /** O {@code recalculateBonus}: o que há em volta pode render um ponto de aspecto. */
    private void recalculateBonus(ServerLevel level) {
        var random = level.getRandom();
        BlockPos pos = this.getBlockPos();
        if (!level.isBrightOutside() && level.getBrightness(LightLayer.BLOCK, pos.above()) < 4
                && !level.canSeeSky(pos.above()) && random.nextInt(20) == 0) {
            this.bonus.merge(Aspects.ENTROPY, 1);
        }
        // o mundo do original tinha duzentos e cinquenta e seis de altura: meio, dois terços e três quartos
        if (pos.getY() > 128 && random.nextInt(20) == 0) this.bonus.merge(Aspects.AIR, 1);
        if (pos.getY() > 169 && random.nextInt(20) == 0) this.bonus.merge(Aspects.AIR, 1);
        if (pos.getY() > 192 && random.nextInt(20) == 0) this.bonus.merge(Aspects.AIR, 1);

        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
                for (int y = -8; y <= 8; y++) {
                    BlockPos at = pos.offset(x, y, z);
                    if (level.isOutsideBuildHeight(at)) continue;
                    BlockState found = level.getBlockState(at);
                    if (found.is(TCBlocks.INFUSED_STONE.get("air"))) {
                        if (this.bonus.getAmount(Aspects.AIR) < 1 && random.nextInt(20) == 0) {
                            this.bonus.merge(Aspects.AIR, 1);
                            return;
                        }
                    } else if (found.is(Blocks.LAVA) || found.is(BlockTags.FIRE) || found.is(TCBlocks.INFUSED_STONE.get("fire"))) {
                        if (this.bonus.getAmount(Aspects.FIRE) < 1 && random.nextInt(20) == 0) {
                            this.bonus.merge(Aspects.FIRE, 1);
                            return;
                        }
                    } else if (found.is(BlockTags.DIRT) || found.is(TCBlocks.INFUSED_STONE.get("earth"))) {
                        if (this.bonus.getAmount(Aspects.EARTH) < 1 && random.nextInt(20) == 0) {
                            this.bonus.merge(Aspects.EARTH, 1);
                            return;
                        }
                    } else if (found.getFluidState().is(Fluids.WATER) || found.is(TCBlocks.INFUSED_STONE.get("water"))) {
                        int chance = found.getFluidState().is(Fluids.WATER) ? 15 : 20;
                        if (this.bonus.getAmount(Aspects.WATER) < 1 && random.nextInt(chance) == 0) {
                            this.bonus.merge(Aspects.WATER, 1);
                            return;
                        }
                    } else if (found.isSignalSource() || found.is(Blocks.REDSTONE_WIRE) || found.is(Blocks.PISTON)
                            || found.is(Blocks.STICKY_PISTON) || found.is(TCBlocks.INFUSED_STONE.get("order"))) {
                        if (this.bonus.getAmount(Aspects.ORDER) < 1 && random.nextInt(20) == 0) {
                            this.bonus.merge(Aspects.ORDER, 1);
                            return;
                        }
                    } else if (found.is(TCBlocks.INFUSED_STONE.get("entropy"))) {
                        if (this.bonus.getAmount(Aspects.ENTROPY) < 1 && random.nextInt(20) == 0) {
                            this.bonus.merge(Aspects.ENTROPY, 1);
                            return;
                        }
                    }
                    if (found.is(Blocks.BOOKSHELF) && random.nextInt(300) == 0) {
                        java.util.List<Aspect> all = new java.util.ArrayList<>(Aspect.ASPECTS.values());
                        this.bonus.merge(all.get(random.nextInt(all.size())), 1);
                        return;
                    }
                }
            }
        }
    }

    /**
     * O {@code placeAspect}: escreve um aspecto numa casa vazia da nota, ou apaga o que quem pesquisa tinha
     * escrito (com o aspecto nulo).
     */
    public void placeAspect(int q, int r, Aspect aspect, Player player) {
        ItemStack ink = this.items.get(INK);
        ItemStack stack = this.items.get(NOTE);
        ResearchNote note = ResearchNotes.get(stack);
        if (!ResearchNotes.consumeInkFromTable(ink, false) || note == null || note.complete()) return;
        PlayerKnowledge knowledge = Knowledges.of(player);
        Map<String, ResearchNote.Cell> cells = note.byKey();
        Hex hex = new Hex(q, r);
        ResearchNote.Cell there = cells.get(hex.key());
        if (there == null) return;
        boolean r1 = knowledge.hasResearch("RESEARCHER1"), r2 = knowledge.hasResearch("RESEARCHER2");
        if (aspect != null) {
            if (there.type() != 0) return;
            if (r2 && this.level.getRandom().nextFloat() < 0.1f) {
                // o pesquisador mestre às vezes escreve sem gastar
                this.level.playSound(null, player, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.2f,
                        0.9f + this.level.getRandom().nextFloat() * 0.2f);
            } else if (knowledge.points(aspect) <= 0) {
                if (this.bonus.getAmount(aspect) <= 0) return;
                this.bonus.reduce(aspect, 1);
            } else {
                knowledge.spend(aspect, 1);
            }
            cells.put(hex.key(), new ResearchNote.Cell(q, r, 2, aspect.tag()));
        } else {
            if (there.type() != 2) return;
            float f = this.level.getRandom().nextFloat();
            // os pesquisadores experientes às vezes recuperam o ponto do que apagam
            if (there.aspectOrNull() != null && (r1 && f < 0.25f || r2 && f < 0.5f)) {
                this.level.playSound(null, player, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.2f,
                        0.9f + this.level.getRandom().nextFloat() * 0.2f);
                knowledge.award(there.aspectOrNull(), 1);
            }
            cells.put(hex.key(), new ResearchNote.Cell(q, r, 0, ""));
        }
        ResearchNote written = note.with(cells, false, note.copies());
        ResearchNotes.consumeInkFromTable(ink, true);
        ResearchNote solved = ResearchNotes.checkCompletion(written, knowledge);
        ResearchNotes.set(stack, solved != null ? solved : written);
        Knowledges.save(player, knowledge);
        if (solved != null) {
            this.level.playSound(null, this.getBlockPos(), net.thaumcraft.registry.TCSounds.LEARN.value(),
                    SoundSource.BLOCKS, 1.0f, 1.0f);
        }
        this.setChanged();
        this.sync();
    }

    /**
     * Junta dois aspectos que se tem num terceiro: gasta um de cada — do bolso, ou do bônus da mesa — e dá um
     * ponto do que eles formam, descobrindo-o se for novo. É o {@code PacketAspectCombinationToServer}.
     */
    public void combine(Player player, Aspect first, Aspect second) {
        PlayerKnowledge knowledge = Knowledges.of(player);
        boolean has1 = knowledge.points(first) > 0 || this.bonus.getAmount(first) > 0;
        boolean has2 = knowledge.points(second) > 0 || this.bonus.getAmount(second) > 0;
        if (!has1 || !has2) return;
        if (knowledge.points(first) > 0) knowledge.spend(first, 1);
        else this.bonus.reduce(first, 1);
        if (knowledge.points(second) > 0) knowledge.spend(second, 1);
        else this.bonus.reduce(second, 1);
        Aspect combo = Aspects.combination(first, second);
        if (combo != null) {
            // o checkAndSyncAspectKnowledge: o aviso do aspecto novo e do ponto que entrou
            boolean known = knowledge.hasDiscovered(combo);
            int given = knowledge.award(combo, 1);
            if (player instanceof net.minecraft.server.level.ServerPlayer server) {
                if (!known) net.thaumcraft.net.TCNetwork.aspectDiscovery(server, combo);
                if (given > 0) net.thaumcraft.net.TCNetwork.aspectPool(server, combo, given, knowledge.points(combo));
            }
        }
        Knowledges.save(player, knowledge);
        this.setChanged();
        this.sync();
    }

    /**
     * O {@code duplicate}: tira uma cópia da descoberta, gastando papel, tinta preta e os aspectos da
     * pesquisa mais um por cópia já feita.
     */
    public void duplicate(Player player) {
        ItemStack stack = this.items.get(NOTE);
        ResearchNote note = ResearchNotes.get(stack);
        if (note == null || !note.complete()) return;
        Inventory inventory = player.getInventory();
        if (!inventory.contains(new ItemStack(Items.PAPER)) || !inventory.contains(new ItemStack(Items.INK_SAC))) return;
        Research research = Researches.get(note.key());
        if (research == null) return;
        PlayerKnowledge knowledge = Knowledges.of(player);
        for (Aspect aspect : research.tags().getAspects()) {
            if (knowledge.points(aspect) < research.tags().getAmount(aspect) + note.copies()) return;
        }
        for (Aspect aspect : research.tags().getAspects()) {
            knowledge.spend(aspect, research.tags().getAmount(aspect) + note.copies());
        }
        Knowledges.save(player, knowledge);
        consume(inventory, Items.PAPER);
        consume(inventory, Items.INK_SAC);
        this.level.playSound(null, this.getBlockPos(), net.thaumcraft.registry.TCSounds.LEARN.value(),
                SoundSource.BLOCKS, 1.0f, 1.0f);
        ResearchNotes.set(stack, note.with(note.byKey(), true, note.copies() + 1));
        stack.grow(1);
        this.setChanged();
        this.sync();
    }

    private static void consume(Inventory inventory, net.minecraft.world.item.Item item) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot).is(item)) {
                inventory.getItem(slot).shrink(1);
                return;
            }
        }
    }

    public void sync() {
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        this.sync();
    }

    // ----------------------------------------------------------------- inventário

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == INK) return stack.getItem() instanceof ScribingToolsItem;
        if (slot == NOTE) {
            ResearchNote note = ResearchNotes.get(stack);
            return stack.is(TCItems.RESEARCH_NOTES) && (note == null || !note.complete());
        }
        return false;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.thaumcraft.research_table");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new ResearchTableMenu(id, inventory, this);
    }

    @Override
    public BlockPos getScreenOpeningData(net.minecraft.server.level.ServerPlayer player) {
        return this.getBlockPos();
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public int getContainerSize() {
        return 2;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(2, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
        this.nextRecalc = input.getIntOr("nextRecalc", 0);
        this.bonus = input.read("bonusAspects", AspectList.CODEC).orElseGet(AspectList::new);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items, true);
        output.putInt("nextRecalc", this.nextRecalc);
        output.store("bonusAspects", AspectList.CODEC, this.bonus);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
