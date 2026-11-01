package net.thaumcraft.arcana.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.arcana.ArcanaBlocks;
import net.thaumcraft.arcana.power.PowerNodes;
import net.thaumcraft.arcana.power.PowerType;
import net.thaumcraft.arcana.power.PoweredBlockEntity;

/**
 * A <b>Bateria de Mana</b>: o {@code TileEntityManaBattery} da 1.4.0.009.
 *
 * <p>O depósito da rede. <b>Duzentos e cinquenta mil</b> de teto, e puxa <b>mil</b> de cada vez — é a
 * única coisa do Ars Magica 2 que guarda energia em quantidade, e é o que permite que uma máquina
 * trabalhe depressa com uma fonte lenta atrás.
 *
 * <p>Ela <b>escolhe um tipo e fica com ele</b>: na primeira vez em que recebe alguma coisa, o tipo de que
 * tiver mais passa a ser o dela, e dali em diante só pede desse. É por isso que uma bateria que apanhou
 * uma gota de Trevas por engano deixa de encher com o Neutro do Obelisco — e é por isso que se liga uma
 * bateria a <b>uma</b> fonte.
 *
 * <p><b>A redstone não faz nada nela, e é assim no original.</b> O {@code tick} dela chama os dois métodos
 * que ligam e desligam o pedido conforme a corrente — e esses métodos escrevem num campo que ninguém lê.
 * Ver o {@code PoweredBlockEntity}.
 */
public class ManaBatteryBlockEntity extends PoweredBlockEntity {
    /** Quanto ela guarda: <b>250 000</b>. */
    public static final int CAPACITY = 250_000;
    /** E quanto puxa de cada vez: <b>mil</b>. */
    public static final int CHARGE_RATE = 1000;
    /** De quantas em quantas batidas ela se redesenha: <b>600</b>, que é meio minuto. */
    public static final int REDRAW = 600;

    private PowerType outputPowerType;
    private int tickCounter;

    public ManaBatteryBlockEntity(BlockPos onde, BlockState feitio) {
        super(ArcanaBlocks.MANA_BATTERY_ENTITY, onde, feitio, CAPACITY);
    }

    /** De que tipo ela é, ou nada enquanto nunca recebeu. */
    public PowerType powerType() {
        return this.outputPowerType;
    }

    @Override
    public void serverTick(ServerLevel level) {
        if (this.outputPowerType == null) {
            PowerType maior = PowerNodes.of(level).getHighestPowerType(this.getBlockPos());
            if (maior != null && PowerNodes.of(level).getPower(this.getBlockPos(), maior) > 0.0f) {
                this.outputPowerType = maior;
                this.setChanged();
                level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
            }
        }

        if (++this.tickCounter % REDRAW == 0) {
            this.tickCounter = 0;
            level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }

        super.serverTick(level);
    }

    @Override
    public int chargeRate() {
        return CHARGE_RATE;
    }

    @Override
    public boolean canProvidePower(PowerType type) {
        return true;
    }

    @Override
    public boolean canRelayPower(PowerType type) {
        return false;
    }

    /** Enquanto não tem tipo, aceita os três; depois, só o dela. */
    @Override
    public PowerType[] validPowerTypes() {
        return this.outputPowerType == null ? PowerType.ALL : new PowerType[]{this.outputPowerType};
    }

    @Override
    protected void saveAdditional(ValueOutput saída) {
        super.saveAdditional(saída);
        if (this.outputPowerType != null) saída.putInt("outputType", this.outputPowerType.id());
    }

    @Override
    protected void loadAdditional(ValueInput entrada) {
        super.loadAdditional(entrada);
        this.outputPowerType = PowerType.byId(entrada.getIntOr("outputType", 0));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registos) {
        return this.saveWithoutMetadata(registos);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
