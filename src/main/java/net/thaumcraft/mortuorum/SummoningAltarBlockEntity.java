package net.thaumcraft.mortuorum;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * O miolo do Altar de Invocação: o {@code TileEntityAltar} do Necromancy.
 *
 * <p>Sete casas: o Pote de Sangue, a Alma num Pote e as cinco peças — cabeça, tronco, pernas e os dois braços.
 * Com o sangue e a alma no lugar, agachar e clicar no altar acorda o lacaio, que sai de lá com dono.
 */
public class SummoningAltarBlockEntity extends BaseContainerBlockEntity {
    public static final int BLOOD = 0, SOUL = 1, HEAD = 2, TORSO = 3, LEGS = 4, ARM_RIGHT = 5, ARM_LEFT = 6;

    private NonNullList<ItemStack> items = NonNullList.withSize(7, ItemStack.EMPTY);

    public SummoningAltarBlockEntity(BlockPos pos, BlockState state) {
        super(MortuorumBlocks.SUMMONING_ALTAR_ENTITY, pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.thaumcraft.summoning_altar");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new SummoningAltarMenu(id, inventory, this);
    }

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
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
    }

    /** O {@code canSpawn}: sem o sangue e sem a alma, o altar não acorda ninguém. */
    public boolean canSpawn() {
        return this.getItem(BLOOD).is(MortuorumItems.JAR_OF_BLOOD)
                && this.getItem(SOUL).is(MortuorumItems.SOUL_IN_A_JAR);
    }

    /** As peças que estão postas, na ordem do lacaio. */
    public MinionParts parts() {
        return new MinionParts(partName(this.getItem(HEAD)), partName(this.getItem(TORSO)),
                partName(this.getItem(ARM_LEFT)), partName(this.getItem(ARM_RIGHT)), partName(this.getItem(LEGS)));
    }

    private static String partName(ItemStack stack) {
        for (var entry : MortuorumItems.PART_ITEMS.entrySet()) {
            if (stack.is(entry.getValue())) return entry.getKey();
        }
        return "";
    }

    /** O {@code spawn}: acorda o lacaio com as peças postas e gasta uma de cada casa. */
    public boolean spawn(ServerLevel level, Player owner) {
        if (!this.canSpawn()) return false;
        MinionParts parts = this.parts();
        if (parts.isEmpty()) return false;

        MinionEntity minion = MortuorumEntities.MINION.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (minion == null) return false;
        minion.snapTo(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.0, this.worldPosition.getZ() + 0.5,
                level.getRandom().nextFloat() * 360.0f, 0.0f);
        minion.setParts(parts);
        minion.tame(owner);
        level.addFreshEntity(minion);
        level.playSound(null, this.worldPosition, net.minecraft.sounds.SoundEvents.WITHER_SPAWN,
                net.minecraft.sounds.SoundSource.BLOCKS, 1.0f,
                1.0f / (level.getRandom().nextFloat() * 0.4f + 0.8f));
        owner.sendSystemMessage(Component.translatable("message.thaumcraft.minion.bidding"));

        if (!owner.getAbilities().instabuild) {
            for (int slot = 0; slot < this.getContainerSize(); slot++) this.removeItem(slot, 1);
        }
        this.setChanged();
        return true;
    }

    /**
     * O que está no altar tem de chegar ao cliente: é dele que sai o corpo pré-montado que aparece deitado na
     * mesa. No original o desenhista lê o tile direto — aqui o tile vive no servidor, então manda-se o feitio.
     */
    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }
}
