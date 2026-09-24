package net.thaumcraft.naturalis;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.api.wands.Wandable;
import net.thaumcraft.inventory.ArcaneChestMenu;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * O Baú Arcano: o {@code ArcaneChestBlockEntity} do Magia Naturalis 0.5.0.
 *
 * <p>Guarda cinquenta e quatro coisas, se for de madeira-grande, ou setenta e sete, se for de prateada. Quem o
 * põe vira dono dele, e só o dono — e quem ele deixar entrar com uma chave — consegue abri-lo. A varinha
 * encolhe o baú de volta em item, com tudo o que ele tinha dentro e a lista de quem podia abrir.
 */
public class ArcaneChestBlockEntity extends net.minecraft.world.level.block.entity.BaseContainerBlockEntity
        implements Wandable, net.minecraft.world.level.block.entity.LidBlockEntity {
    /**
     * Quem mais pode abrir: o {@code UserAccess} do original. O nível é o da chave que deu a entrada — zero só
     * abre, um abre e empresta, dois abre, empresta, quebra e encolhe.
     */
    public record Access(UUID id, byte level) {
    }

    private final ArcaneChestBlock.Kind kind;
    private NonNullList<ItemStack> items;
    private @Nullable UUID owner;
    private String ownerName = "";
    private final List<Access> access = new ArrayList<>();

    /** A tampa, como a do baú comum: quem está com ele aberto e quanto ela já levantou. */
    private final net.minecraft.world.level.block.entity.ContainerOpenersCounter openers =
            new net.minecraft.world.level.block.entity.ContainerOpenersCounter() {
                @Override
                protected void onOpen(Level level, BlockPos pos, BlockState state) {
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.CHEST_OPEN,
                            net.minecraft.sounds.SoundSource.BLOCKS, 0.5f, level.getRandom().nextFloat() * 0.1f + 0.9f);
                }

                @Override
                protected void onClose(Level level, BlockPos pos, BlockState state) {
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.CHEST_CLOSE,
                            net.minecraft.sounds.SoundSource.BLOCKS, 0.5f, level.getRandom().nextFloat() * 0.1f + 0.9f);
                }

                @Override
                protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int previous, int current) {
                    level.blockEvent(pos, state.getBlock(), 1, current);
                }

                @Override
                public boolean isOwnContainer(Player player) {
                    return player.containerMenu instanceof ArcaneChestMenu menu && menu.chest() == ArcaneChestBlockEntity.this;
                }
            };
    private final net.minecraft.world.level.block.entity.ChestLidController lid =
            new net.minecraft.world.level.block.entity.ChestLidController();

    public ArcaneChestBlockEntity(BlockEntityType<?> type, ArcaneChestBlock.Kind kind, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.kind = kind;
        this.items = NonNullList.withSize(kind.size(), ItemStack.EMPTY);
    }

    public ArcaneChestBlock.Kind kind() {
        return this.kind;
    }

    public @Nullable UUID owner() {
        return this.owner;
    }

    public String ownerName() {
        return this.ownerName;
    }

    public List<Access> access() {
        return List.copyOf(this.access);
    }

    /** Quem põe o baú fica dono dele. */
    public void claim(Player player) {
        this.owner = player.getUUID();
        this.ownerName = player.getName().getString();
        this.setChanged();
    }

    /** Devolve o baú encolhido ao estado em que estava: os donos não mudam, só a lista volta. */
    public void restoreAccess(List<Access> list) {
        this.access.clear();
        this.access.addAll(list);
        this.setChanged();
    }

    /** A chave deixou alguém entrar. Devolve {@code false} se essa pessoa já entrava. */
    public boolean allow(UUID who, byte level) {
        if (this.owner != null && this.owner.equals(who)) return false;
        for (Access user : this.access) {
            if (user.id().equals(who)) return false;
        }
        this.access.add(new Access(who, level));
        this.setChanged();
        return true;
    }

    /** O nível de quem abre: dois para o dono e para quem está no criativo, o da chave para o resto. */
    public int level(Player player) {
        if (this.owner == null || this.owner.equals(player.getUUID()) || player.getAbilities().instabuild) return 2;
        for (Access user : this.access) {
            if (user.id().equals(player.getUUID())) return user.level();
        }
        return -1;
    }

    public boolean mayOpen(Player player) {
        return this.level(player) >= 0;
    }

    /** Quebrar e encolher é só do dono e de quem tem a chave de maior confiança. */
    public boolean mayBreak(Player player) {
        return this.level(player) > 1;
    }

    // ------------------------------------------------------------------ o que ele guarda

    @Override
    public int getContainerSize() {
        return this.items.size();
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
    protected Component getDefaultName() {
        return Component.translatable(this.kind.translationKey());
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new ArcaneChestMenu(id, inventory, this, this.kind.rows(), this.kind.columns());
    }

    @Override
    public boolean stillValid(Player player) {
        return super.stillValid(player) && this.mayOpen(player);
    }

    // ------------------------------------------------------------------ a tampa

    /** No cliente, a tampa sobe e desce sozinha. */
    public static void lidAnimateTick(Level level, BlockPos pos, BlockState state, ArcaneChestBlockEntity chest) {
        chest.lid.tickLid();
    }

    @Override
    public boolean triggerEvent(int id, int value) {
        if (id == 1) {
            this.lid.shouldBeOpen(value > 0);
            return true;
        }
        return super.triggerEvent(id, value);
    }

    @Override
    public float getOpenNess(float partial) {
        return this.lid.getOpenness(partial);
    }

    @Override
    public void startOpen(net.minecraft.world.entity.ContainerUser user) {
        if (this.remove || user.getLivingEntity().isSpectator()) return;
        this.openers.incrementOpeners(user.getLivingEntity(), this.getLevel(), this.getBlockPos(), this.getBlockState(),
                user.getContainerInteractionRange());
    }

    @Override
    public void stopOpen(net.minecraft.world.entity.ContainerUser user) {
        if (this.remove || user.getLivingEntity().isSpectator()) return;
        this.openers.decrementOpeners(user.getLivingEntity(), this.getLevel(), this.getBlockPos(), this.getBlockState());
    }

    @Override
    public java.util.List<net.minecraft.world.entity.ContainerUser> getEntitiesWithContainerOpen() {
        return this.openers.getEntitiesWithContainerOpen(this.getLevel(), this.getBlockPos());
    }

    /** O bloco pede isto de tempos em tempos, como o baú comum. */
    public void recheckOpen() {
        if (!this.remove) this.openers.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
    }

    // ------------------------------------------------------------------ a varinha encolhe o baú

    @Override
    public boolean onWand(Level level, ItemStack wand, Player player, BlockPos pos, Direction face) {
        if (level.isClientSide()) return true;
        if (!this.mayBreak(player)) {
            player.sendSystemMessage(Component.translatable("chat.thaumcraft.chest.resist")
                    .withStyle(net.minecraft.ChatFormatting.DARK_PURPLE));
            return true;
        }
        ItemStack shrunk = new ItemStack(this.kind == ArcaneChestBlock.Kind.SILVERWOOD
                ? NaturalisItems.ARCANE_CHEST_SILVERWOOD : NaturalisItems.ARCANE_CHEST_GREATWOOD);
        ArcaneChestItem.store(shrunk, this.items, this.access);
        this.items.clear();
        level.removeBlockEntity(pos);
        level.removeBlock(pos, false);
        net.minecraft.world.Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, shrunk);
        level.playSound(null, pos, net.thaumcraft.registry.TCSounds.WAND.value(),
                net.minecraft.sounds.SoundSource.BLOCKS, 0.5f, 1.0f);
        return true;
    }

    // ------------------------------------------------------------------ o que fica gravado

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(this.kind.size(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
        this.owner = input.read("owner", UUIDUtil.CODEC).orElse(null);
        this.ownerName = input.getStringOr("ownerName", "");
        this.access.clear();
        input.read("access", ArcaneChestItem.ACCESS_CODEC).ifPresent(this.access::addAll);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        if (this.owner != null) output.store("owner", UUIDUtil.CODEC, this.owner);
        output.putString("ownerName", this.ownerName);
        if (!this.access.isEmpty()) output.store("access", ArcaneChestItem.ACCESS_CODEC, this.access);
    }
}
