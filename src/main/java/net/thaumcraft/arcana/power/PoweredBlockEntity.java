package net.thaumcraft.arcana.power;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * O que todo bloco da rede faz sozinho: o {@code TileEntityAMPower} da 1.4.0.009.
 *
 * <p>Duas coisas, e mais nada. <b>Entrar na rede</b> quando é posto e <b>sair</b> quando é tirado; e, de
 * <b>vinte em vinte batidas</b>, pedir pelos caminhos que tem guardados o que couber nele.
 *
 * <p>Quem pede é sempre o destino. Uma fonte não empurra energia para ninguém: ela fica cheia à espera de
 * que alguém a venha buscar, e é por isso que ligar a Chave de Cristal se faz <b>da fonte para a
 * máquina</b> e não ao contrário.
 */
public abstract class PoweredBlockEntity extends BlockEntity implements PowerNode {
    /** De quantas em quantas batidas se pede: <b>vinte</b>. */
    public static final int REQUEST_INTERVAL = 20;

    protected final int capacity;
    private int tickCounter;

    protected PoweredBlockEntity(BlockEntityType<?> tipo, BlockPos onde, BlockState feitio, int capacity) {
        super(tipo, onde, feitio);
        this.capacity = capacity;
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (level instanceof ServerLevel servidor) PowerNodes.of(servidor).register(this.getBlockPos());
    }

    @Override
    public void setRemoved() {
        if (this.level instanceof ServerLevel servidor) PowerNodes.of(servidor).remove(this.getBlockPos());
        super.setRemoved();
    }

    /** O {@code updateEntity}: o relógio do pedido, e nada mais. */
    public void serverTick(ServerLevel level) {
        if (!this.canRequestPower() || this.tickCounter++ < this.getRequestInterval()) return;
        this.tickCounter = 0;
        PowerNodes rede = PowerNodes.of(level);
        for (PowerType qual : this.validPowerTypes()) {
            float trouxe = rede.requestPower(level, this.getBlockPos(), this, qual, this.chargeRate());
            if (trouxe > 0.0f) rede.insertPower(this.getBlockPos(), this, qual, trouxe);
        }
    }

    public int getRequestInterval() {
        return REQUEST_INTERVAL;
    }

    public void setPower(PowerType type, float quanto) {
        if (this.level instanceof ServerLevel servidor) {
            PowerNodes.of(servidor).setPower(this.getBlockPos(), this, type, quanto);
        }
    }

    /** Quanto deste tipo há aqui dentro, agora. */
    public float power(PowerType type) {
        return this.level instanceof ServerLevel servidor
                ? PowerNodes.of(servidor).getPower(this.getBlockPos(), type) : 0.0f;
    }

    @Override
    public float capacity() {
        return this.capacity;
    }

    @Override
    public boolean canProvidePower(PowerType type) {
        return false;
    }

    @Override
    public PowerType[] validPowerTypes() {
        return PowerType.ALL;
    }

    /**
     * <b>Pede, por padrão.</b>
     *
     * <p>Quem não quer pedir sobrescreve isto: o Obelisco devolve falso, o conduto devolve falso enquanto
     * houver redstone.
     *
     * <p><b>Um achado do original que fica escrito aqui.</b> O {@code TileEntityAMPower} tem um campo
     * {@code canRequestPower} com dois métodos para o ligar e desligar — e <b>nada o lê</b>: o campo é
     * privado e o método {@code canRequestPower()} devolve {@code true} sem olhar para ele. Isso faz com
     * que a Bateria de Mana, que chama esses métodos conforme a redstone, <b>peça sempre</b>, com ou sem
     * redstone. O campo não veio para cá: trazê-lo era trazer código morto, e a conduta é a mesma.
     */
    @Override
    public boolean canRequestPower() {
        return true;
    }

    @Override
    public boolean isSource() {
        return false;
    }
}
