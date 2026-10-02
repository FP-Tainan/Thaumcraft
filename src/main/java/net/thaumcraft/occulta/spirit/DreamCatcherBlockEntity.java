package net.thaumcraft.occulta.spirit;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.occulta.OccultaBlocks;
import org.jetbrains.annotations.Nullable;

/**
 * A alma do Apanhador de Sonhos: a {@code TileEntityDreamCatcher} do Witchery.
 *
 * <p>Ele guarda uma coisa: <b>que teia</b> está pregada nele. De segundo em segundo olha quem está a cinco de
 * distância e lhe dá o que a teia tem para dar — o sonho bom ou o pesadelo, conforme o que mais houver em volta.
 *
 * <p>A conta de quem manda é a do original, e é engenhosa: um apanhador com a <b>teia dos pesadelos</b> por perto
 * faz de tudo pesadelo; um com a <b>teia da intensidade</b> aperta o que houver. Os dois juntos fazem pesadelo
 * apertado, que é o que um quarto mal montado dá.
 */
public class DreamCatcherBlockEntity extends BlockEntity {
    /** A que distância ele pega, e de quantas em quantas batidas. */
    public static final int REACH = 5;
    public static final int EVERY = 20;

    private DreamWeaveItem.@Nullable Weave weave;
    private long ticks;

    public DreamCatcherBlockEntity(BlockPos pos, BlockState state) {
        super(OccultaBlocks.DREAM_CATCHER_ENTITY, pos, state);
    }

    public DreamWeaveItem.@Nullable Weave weave() {
        return this.weave;
    }

    public void setWeave(DreamWeaveItem.@Nullable Weave qual) {
        this.weave = qual;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    /** Se o apanhador daquele lugar traz a teia dos pesadelos. */
    public static boolean catchesNightmares(ServerLevel level, BlockPos onde) {
        return level.getBlockEntity(onde) instanceof DreamCatcherBlockEntity alma
                && alma.weave() == DreamWeaveItem.Weave.NIGHTMARE;
    }

    /** E se ele traz a da intensidade. */
    public static boolean sharpensDreams(ServerLevel level, BlockPos onde) {
        return level.getBlockEntity(onde) instanceof DreamCatcherBlockEntity alma
                && alma.weave() == DreamWeaveItem.Weave.INTENSITY;
    }

    public void tick() {
        if (!(this.level instanceof ServerLevel level) || this.weave == null) return;
        this.ticks++;
        if (this.ticks % EVERY != 1) return;

        boolean bom = true;
        boolean apertado = false;
        for (BlockPos casa : BlockPos.betweenClosed(this.worldPosition.offset(-REACH, -REACH, -REACH),
                this.worldPosition.offset(REACH, REACH, REACH))) {
            if (casa.equals(this.worldPosition)) continue;
            if (!level.getBlockState(casa).is(OccultaBlocks.DREAM_CATCHER)) continue;
            if (catchesNightmares(level, casa)) bom = false;
            else if (sharpensDreams(level, casa)) apertado = true;
        }

        AABB roda = new AABB(this.worldPosition).inflate(REACH);
        for (Player quem : level.getEntitiesOfClass(Player.class, roda)) {
            this.weave.apply(quem, bom, apertado);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        String nome = input.getStringOr("weave", "");
        this.weave = nome.isEmpty() ? null : DreamWeaveItem.Weave.valueOf(nome);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.weave != null) output.putString("weave", this.weave.name());
    }

    /** O que ele larga ao ser quebrado: a teia que trazia. */
    public ItemStack drop() {
        return this.weave == null ? ItemStack.EMPTY
                : new ItemStack(net.thaumcraft.occulta.OccultaItems.weave(this.weave));
    }

    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener>
            getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }
}
