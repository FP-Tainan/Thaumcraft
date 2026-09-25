package net.thaumcraft.crimson;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCResources;

/**
 * O miolo do Altar Antigo: o {@code TileActivator} do Crimson Warfare.
 *
 * <p>Posta a semente, o altar conta trezentos tiques — quinze segundos — e no fim ela desaparece e sobe de lá um
 * dos três, sorteado: o portal carmesim, o golem eldritch ou o guardião. O altar se desfaz com o chamado, que é
 * o que o original faz.
 *
 * <p><b>Diferença declarada:</b> no original a semente fica no mundo como um item largado, marcado com uma
 * etiqueta, e o altar a procura em volta a cada tique. Aqui ela fica guardada no próprio altar — o que se vê e o
 * que acontece é o mesmo, e não há item solto para se perder ou para alguém apanhar.
 */
public class AncientAltarBlockEntity extends BlockEntity {
    /** O {@code tick >= 300} do original. */
    public static final int DELAY = 300;

    private ItemStack seed = ItemStack.EMPTY;
    private int ticks;

    public AncientAltarBlockEntity(BlockPos pos, BlockState state) {
        super(CrimsonBlocks.ANCIENT_ALTAR_ENTITY, pos, state);
    }

    public boolean hasSeed() {
        return !this.seed.isEmpty();
    }

    public ItemStack seed() {
        return this.seed;
    }

    public int ticks() {
        return this.ticks;
    }

    /** Põe a semente no altar e começa a conta. */
    public void put(ItemStack stack) {
        this.seed = stack.copyWithCount(1);
        this.ticks = 0;
        this.setChanged();
        if (this.level != null) this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }

    /** Tira a semente de volta, que o original não deixa fazer e aqui é o desfazer do engano. */
    public ItemStack take() {
        ItemStack saída = this.seed;
        this.seed = ItemStack.EMPTY;
        this.ticks = 0;
        this.setChanged();
        if (this.level != null) this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        return saída;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AncientAltarBlockEntity altar) {
        if (!altar.hasSeed()) return;
        if (!(level instanceof ServerLevel server)) return;
        altar.ticks++;
        if (altar.ticks < DELAY) return;
        altar.seed = ItemStack.EMPTY;
        altar.ticks = 0;
        altar.call(server, pos);
    }

    /** O sorteio dos três do {@code bossSwitch}. */
    private void call(ServerLevel level, BlockPos pos) {
        EntityType<? extends Mob> quem = switch (level.getRandom().nextInt(3)) {
            case 0 -> TCEntities.CULTIST_PORTAL;
            case 1 -> TCEntities.ELDRITCH_GOLEM;
            default -> TCEntities.ELDRITCH_WARDEN;
        };
        // o altar se desfaz com o chamado
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        Mob bicho = quem.create(level, EntitySpawnReason.TRIGGERED);
        if (bicho == null) return;
        bicho.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.getRandom().nextFloat() * 360.0f, 0.0f);
        bicho.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.TRIGGERED, null);
        level.addFreshEntity(bicho);
    }

    /** A Semente do Vazio é a única coisa que o altar aceita. */
    public static boolean isSeed(ItemStack stack) {
        return stack.is(TCResources.get("void_seed"));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.seed = input.read("Seed", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.ticks = input.getIntOr("Ticks", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!this.seed.isEmpty()) output.store("Seed", ItemStack.CODEC, this.seed);
        output.putInt("Ticks", this.ticks);
    }

    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }
}
