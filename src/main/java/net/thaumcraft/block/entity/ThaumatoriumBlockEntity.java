package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectContainer;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.EssentiaTransport;
import net.thaumcraft.block.MnemonicMatrixBlock;
import net.thaumcraft.block.ThaumatoriumBlock;
import net.thaumcraft.crafting.CrucibleRecipe;
import net.thaumcraft.inventory.InventoryUtils;
import net.thaumcraft.inventory.ThaumatoriumMenu;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCParticles;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * O {@code TileThaumatorium} da 4.2.3.5: o crisol automático. Com fogo (ou lava, ou nitor) debaixo do crisol e sem
 * sinal de redstone, a cada cinco tiques ele olha o catalisador guardado, escolhe entre as receitas marcadas a que
 * ele fecha, puxa pelos canos (dos lados e de cima, nas duas metades, menos pela frente) o aspecto que ainda falta, com
 * força 128, e ao juntar tudo gasta um catalisador e solta o resultado pela frente — num inventário encostado ou no
 * chão. Cada matriz mnemônica virada para ele guarda mais duas receitas.
 */
public class ThaumatoriumBlockEntity extends BlockEntity implements WorldlyContainer, AspectContainer, EssentiaTransport,
        net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider<BlockPos> {
    public ItemStack inputStack = ItemStack.EMPTY;
    public AspectList essentia = new AspectList();
    public List<Integer> recipeHash = new ArrayList<>();
    public List<AspectList> recipeEssentia = new ArrayList<>();
    public List<String> recipePlayer = new ArrayList<>();
    public int currentCraft = -1;
    public int maxRecipes = 1;
    @Nullable
    public Aspect currentSuction;
    int venting;
    int counter;
    boolean heated;
    @Nullable
    CrucibleRecipe currentRecipe;
    /** O {@code eventHandler}: a tela aberta, que refaz a lista de receitas quando o catalisador muda. */
    @Nullable
    public ThaumatoriumMenu eventHandler;

    public ThaumatoriumBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.THAUMATORIUM, pos, state);
    }

    public Direction facing() {
        BlockState state = this.getBlockState();
        return state.hasProperty(ThaumatoriumBlock.FACING) ? state.getValue(ThaumatoriumBlock.FACING) : Direction.NORTH;
    }

    /** O {@code checkHeat}: o que há dois blocos abaixo (debaixo do crisol) é fogo, lava ou nitor. */
    boolean checkHeat() {
        BlockState below = this.level.getBlockState(this.worldPosition.below(2));
        return below.is(BlockTags.FIRE) || below.getFluidState().is(FluidTags.LAVA) || below.is(TCBlocks.NITOR);
    }

    /** O {@code getCurrentOutputRecipe}. */
    public ItemStack getCurrentOutputRecipe() {
        if (this.currentCraft >= 0 && this.currentCraft < this.recipeHash.size()) {
            CrucibleRecipe recipe = CrucibleRecipe.byHash(this.recipeHash.get(this.currentCraft));
            if (recipe != null) return recipe.result().copy();
        }
        return ItemStack.EMPTY;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ThaumatoriumBlockEntity tile) {
        if (!level.isClientSide()) tile.serverTick();
        else if (tile.venting > 0) tile.vent(level, pos);
    }

    private void serverTick() {
        if (this.counter == 0 || this.counter % 40 == 0) {
            this.heated = this.checkHeat();
            this.getUpgrades();
        }
        this.counter++;
        if (!this.heated || this.gettingPower() || this.counter % 5 != 0 || this.recipeHash.isEmpty()) return;
        if (this.inputStack.isEmpty()) {
            this.currentSuction = null;
            return;
        }
        if (this.currentCraft < 0 || this.currentCraft >= this.recipeHash.size() || this.currentRecipe == null
                || !this.currentRecipe.catalystMatches(this.inputStack)) {
            for (int a = 0; a < this.recipeHash.size(); a++) {
                CrucibleRecipe recipe = CrucibleRecipe.byHash(this.recipeHash.get(a));
                if (recipe != null && recipe.catalystMatches(this.inputStack)) {
                    this.currentCraft = a;
                    this.currentRecipe = recipe;
                    break;
                }
            }
        }
        if (this.currentCraft < 0 || this.currentCraft >= this.recipeHash.size()) return;
        Direction facing = this.facing();
        if (this.level.getBlockEntity(this.worldPosition.offset(facing.getStepX(), 0, facing.getStepZ())) instanceof Container inventory) {
            // se o inventário da frente não tem onde pôr, espera
            if (!InventoryUtils.insert(inventory, this.getCurrentOutputRecipe(), facing.getOpposite(), false).isEmpty()) return;
        }
        boolean done = true;
        this.currentSuction = null;
        AspectList wanted = this.recipeEssentia.get(this.currentCraft);
        for (Aspect aspect : wanted.getAspectsSorted()) {
            if (this.essentia.getAmount(aspect) < wanted.getAmount(aspect)) {
                this.currentSuction = aspect;
                done = false;
                break;
            }
        }
        if (done) this.completeRecipe();
        else if (this.currentSuction != null) this.fill();
    }

    /** O {@code completeRecipe}: gasta um catalisador, zera a essência e solta o resultado pela frente. */
    private void completeRecipe() {
        if (this.currentRecipe == null || this.currentCraft >= this.recipeHash.size()
                || !this.currentRecipe.matches(this.essentia, this.inputStack) || this.removeItem(0, 1).isEmpty()) {
            return;
        }
        this.essentia = new AspectList();
        ItemStack dropped = this.getCurrentOutputRecipe();
        Direction facing = this.facing();
        if (this.level.getBlockEntity(this.worldPosition.offset(facing.getStepX(), 0, facing.getStepZ())) instanceof Container inventory) {
            dropped = InventoryUtils.insert(inventory, dropped, facing.getOpposite(), true);
        }
        if (!dropped.isEmpty()) {
            ItemEntity ei = new ItemEntity(this.level,
                    this.worldPosition.getX() + 0.5 + facing.getStepX() * 0.66,
                    this.worldPosition.getY() + 0.33 + facing.getOpposite().getStepY(),
                    this.worldPosition.getZ() + 0.5 + facing.getStepZ() * 0.66, dropped.copy());
            ei.setDeltaMovement(0.075f * facing.getStepX(), 0.025f, 0.075f * facing.getStepZ());
            this.level.addFreshEntity(ei);
            this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 0, 0);
        }
        var random = this.level.getRandom();
        this.level.playSound(null, this.worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.25f,
                2.6f + (random.nextFloat() - random.nextFloat()) * 0.8f);
        this.currentCraft = -1;
        this.sync();
    }

    /** O {@code fill}: uma unidade do aspecto que falta, de um cano dos lados ou de cima, nas duas metades. */
    void fill() {
        Direction facing = this.facing();
        for (int y = 0; y <= 1; y++) {
            for (Direction dir : Direction.values()) {
                if (dir == facing || dir == Direction.DOWN || y == 0 && dir == Direction.UP) continue;
                BlockPos at = this.worldPosition.above(y).relative(dir);
                if (!(this.level.getBlockEntity(at) instanceof EssentiaTransport ic) || !ic.isConnectable(dir.getOpposite())) continue;
                if (ic.getEssentiaAmount(dir.getOpposite()) > 0 && ic.getSuctionAmount(dir.getOpposite()) < this.getSuctionAmount(null)
                        && this.getSuctionAmount(null) >= ic.getMinimumSuction()) {
                    int ess = ic.takeEssentia(this.currentSuction, 1, dir.getOpposite());
                    if (ess > 0) {
                        this.addToContainer(this.currentSuction, ess);
                        return;
                    }
                }
            }
        }
    }

    /** O {@code gettingPower}: sinal de redstone nele, no crisol ou na metade de cima. */
    public boolean gettingPower() {
        return this.level.hasNeighborSignal(this.worldPosition) || this.level.hasNeighborSignal(this.worldPosition.below())
                || this.level.hasNeighborSignal(this.worldPosition.above());
    }

    /** O {@code getUpgrades}: cada matriz mnemônica encostada (dos lados ou em cima, nas duas metades) e virada para ele. */
    public void getUpgrades() {
        int mr = 1;
        Direction facing = this.facing();
        for (int yy = 0; yy <= 1; yy++) {
            for (Direction dir : Direction.values()) {
                if (dir == Direction.DOWN || dir == facing) continue;
                BlockPos at = new BlockPos(this.worldPosition.getX() + dir.getStepX(), this.worldPosition.getY() + yy + dir.getStepY(),
                        this.worldPosition.getZ() + dir.getStepZ());
                BlockState there = this.level.getBlockState(at);
                if (there.is(TCBlocks.MNEMONIC_MATRIX) && there.getValue(MnemonicMatrixBlock.FACING) == dir.getOpposite()) mr += 2;
            }
        }
        if (mr != this.maxRecipes) {
            this.maxRecipes = mr;
            while (this.recipeHash.size() > this.maxRecipes) {
                int last = this.recipeHash.size() - 1;
                this.recipeHash.remove(last);
                if (this.recipeEssentia.size() > last) this.recipeEssentia.remove(last);
                if (this.recipePlayer.size() > last) this.recipePlayer.remove(last);
            }
            this.sync();
        }
    }

    /** O clique numa receita da tela: marca, ou desmarca se já estava marcada (o {@code enchantItem} do contêiner). */
    public void toggle(CrucibleRecipe recipe, Player player) {
        int hash = recipe.hash();
        int at = this.recipeHash.indexOf(hash);
        if (at >= 0) {
            this.recipeEssentia.remove(at);
            this.recipePlayer.remove(at);
            this.recipeHash.remove(at);
            this.currentCraft = -1;
        } else {
            this.recipeEssentia.add(recipe.cost().copy());
            this.recipePlayer.add(player.getName().getString());
            this.recipeHash.add(hash);
        }
        this.sync();
    }

    public void sync() {
        this.setChanged();
        if (this.level != null) this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }

    /** O vapor que sai pela frente quando algo cai no chão. */
    private void vent(Level level, BlockPos pos) {
        this.venting--;
        var random = level.getRandom();
        Direction facing = this.facing();
        float fx = 0.1f - random.nextFloat() * 0.2f, fz = 0.1f - random.nextFloat() * 0.2f, fy = 0.1f - random.nextFloat() * 0.2f;
        float fx2 = 0.1f - random.nextFloat() * 0.2f, fz2 = 0.1f - random.nextFloat() * 0.2f, fy2 = 0.1f - random.nextFloat() * 0.2f;
        level.addParticle(ColorParticleOption.create(TCParticles.VENT, 0xFFFFFFFF),
                pos.getX() + 0.5 + fx + facing.getStepX() / 2.0, pos.getY() + 0.5 + fy, pos.getZ() + 0.5 + fz + facing.getStepZ() / 2.0,
                facing.getStepX() / 4.0f + fx2, fy2, facing.getStepZ() / 4.0f + fz2);
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id >= 0) {
            if (this.level != null && this.level.isClientSide()) this.venting = 7;
            return true;
        }
        return super.triggerEvent(id, param);
    }

    // ----------------------------------------------------------------- a essência

    /** O {@code addToContainer}: só aceita o que a receita atual ainda pede. */
    @Override
    public int addToContainer(Aspect tt, int am) {
        if (this.currentRecipe == null) return am;
        int ce = this.currentRecipe.cost().getAmount(tt) - this.essentia.getAmount(tt);
        if (ce <= 0) return am;
        int add = Math.min(ce, am);
        this.essentia.add(tt, add);
        this.sync();
        return am - add;
    }

    @Override
    public boolean takeFromContainer(Aspect tt, int am) {
        if (this.essentia.getAmount(tt) < am) return false;
        this.essentia.remove(tt, am);
        this.sync();
        return true;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect tt, int am) {
        return this.essentia.getAmount(tt) >= am;
    }

    @Override
    public int containerContains(@Nullable Aspect tt) {
        return tt == null ? 0 : this.essentia.getAmount(tt);
    }

    @Override
    public boolean doesContainerAccept(Aspect tag) {
        return true;
    }

    @Override
    public AspectList getAspects() {
        return this.essentia;
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face != this.facing();
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face != this.facing();
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return false;
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
        this.currentSuction = aspect;
    }

    @Nullable
    @Override
    public Aspect getSuctionType(@Nullable Direction face) {
        return this.currentSuction;
    }

    @Override
    public int getSuctionAmount(@Nullable Direction face) {
        return this.currentSuction != null ? 128 : 0;
    }

    @Nullable
    @Override
    public Aspect getEssentiaType(@Nullable Direction face) {
        return null;
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction face) {
        return 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        return this.canOutputTo(face) && this.takeFromContainer(aspect, amount) ? amount : 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        return this.canInputFrom(face) ? amount - this.addToContainer(aspect, amount) : 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public boolean renderExtendedTube() {
        return false;
    }

    // ----------------------------------------------------------------- a casa do catalisador

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return this.inputStack.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.inputStack;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (this.inputStack.isEmpty()) return ItemStack.EMPTY;
        ItemStack out = this.inputStack.split(amount);
        this.changed();
        return out;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack out = this.inputStack;
        this.inputStack = ItemStack.EMPTY;
        return out;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.inputStack = stack;
        if (!stack.isEmpty() && stack.getCount() > this.getMaxStackSize()) stack.setCount(this.getMaxStackSize());
        this.changed();
    }

    private void changed() {
        this.setChanged();
        if (this.eventHandler != null) this.eventHandler.slotsChanged(this);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.level != null && this.level.getBlockEntity(this.worldPosition) == this
                && player.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        this.inputStack = ItemStack.EMPTY;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[]{0};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return true;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level != null) Containers.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), this.inputStack);
        this.inputStack = ItemStack.EMPTY;
    }

    // ----------------------------------------------------------------- a tela

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.thaumcraft.thaumatorium");
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return this.worldPosition;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ThaumatoriumMenu(id, inventory, this);
    }

    // ----------------------------------------------------------------- guardar

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.essentia = input.read("aspects", AspectList.CODEC).orElseGet(AspectList::new);
        this.maxRecipes = input.getIntOr("maxrec", 1);
        this.recipeEssentia = new ArrayList<>();
        this.recipeHash = new ArrayList<>();
        this.recipePlayer = new ArrayList<>();
        List<String> players = input.read("OutputPlayer", com.mojang.serialization.Codec.STRING.listOf()).orElseGet(List::of);
        int[] hashes = input.getIntArray("recipes").orElse(new int[0]);
        for (int hash : hashes) {
            CrucibleRecipe recipe = CrucibleRecipe.byHash(hash);
            if (recipe != null) {
                this.recipeEssentia.add(recipe.cost().copy());
                this.recipePlayer.add(this.recipeHash.size() < players.size() ? players.get(this.recipeHash.size()) : "");
                this.recipeHash.add(hash);
            }
        }
        this.inputStack = input.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.currentCraft = -1;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("aspects", AspectList.CODEC, this.essentia);
        output.putInt("maxrec", this.maxRecipes);
        output.putIntArray("recipes", this.recipeHash.stream().mapToInt(Integer::intValue).toArray());
        output.store("OutputPlayer", com.mojang.serialization.Codec.STRING.listOf(), this.recipePlayer);
        if (!this.inputStack.isEmpty()) output.store("Item", ItemStack.CODEC, this.inputStack);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }
}
