package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * A alma de uma casa de barreira: a {@code TileEntityBarrier} do Witchery.
 *
 * <p>Ela guarda três coisas: <b>quanto tempo falta</b>, <b>se trava gente</b> e <b>de quem é</b>. A conta desce
 * a cada batida, e no zero a casa some — é isso que faz a barreira precisar do rito a sustentá-la: parado o
 * rito, a parede se desfaz sozinha em segundo e meio.
 */
public class BarrierBlockEntity extends BlockEntity {
    private int ticks = BarrierBlock.TICKS_TO_LIVE;
    private boolean blocksPlayers;
    private @Nullable UUID owner;

    public BarrierBlockEntity(BlockPos pos, BlockState state) {
        super(OccultaBlocks.BARRIER_ENTITY, pos, state);
    }

    /** Renova a conta desta casa: é o que o rito faz de vinte em vinte batidas. */
    public void renew(int ticks, boolean blocksPlayers, @Nullable UUID owner) {
        this.ticks = ticks;
        this.blocksPlayers = blocksPlayers;
        if (owner != null) this.owner = owner;
        this.setChanged();
    }

    public int remaining() {
        return this.ticks;
    }

    public boolean blocksPlayers() {
        return this.blocksPlayers;
    }

    /** Se aquela pessoa passa por esta casa. */
    public boolean lets(Player quem) {
        if (!this.blocksPlayers) return true;
        if (quem.hasInfiniteMaterials() && quem.isCrouching()) return true;
        return this.owner != null && this.owner.equals(quem.getUUID());
    }

    public void tick() {
        if (!(this.level instanceof ServerLevel level)) return;
        if (--this.ticks > 0) return;
        level.setBlockAndUpdate(this.worldPosition, Blocks.AIR.defaultBlockState());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.ticks = input.getIntOr("ticks", BarrierBlock.TICKS_TO_LIVE);
        this.blocksPlayers = input.getBooleanOr("blocks_players", false);
        this.owner = input.read("owner", UUIDUtil.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("ticks", this.ticks);
        output.putBoolean("blocks_players", this.blocksPlayers);
        if (this.owner != null) output.store("owner", UUIDUtil.CODEC, this.owner);
    }
}
