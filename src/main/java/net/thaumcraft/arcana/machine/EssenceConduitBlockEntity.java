package net.thaumcraft.arcana.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.arcana.ArcanaBlocks;
import net.thaumcraft.arcana.power.PowerType;
import net.thaumcraft.arcana.power.PoweredBlockEntity;

/**
 * O <b>Conduto de Essência</b>: o {@code TileEntityEssenceConduit} da 1.4.0.009.
 *
 * <p>É o que faz a rede chegar longe. Um nó só enxerga dez casas; pondo condutos de dez em dez, a energia
 * atravessa o que for preciso. Ele <b>passa os três tipos</b> e não dá nenhum — é um degrau, não uma
 * fonte.
 *
 * <p>Guarda <b>um</b> de cada tipo e pede <b>um</b> de cada vez. Não é um depósito: é um pé de caminho.
 *
 * <p>E a <b>redstone o fecha</b>: com corrente, ele deixa de pedir. É assim que se desliga um ramo da rede
 * sem desmontar nada.
 */
public class EssenceConduitBlockEntity extends PoweredBlockEntity {
    /** Quanto ele aguenta: <b>um</b>. */
    public static final int CAPACITY = 1;
    /** E quanto ele pede de cada vez: <b>um</b>. */
    public static final int CHARGE_RATE = 1;

    private boolean redstonePowered;

    /** O giro do cristal, que é do cliente e não se guarda. */
    private float rotationX, rotationY, rotationZ;
    private float incX, incY, incZ;
    private boolean primeiraVolta = true;

    public EssenceConduitBlockEntity(BlockPos onde, BlockState feitio) {
        super(ArcanaBlocks.ESSENCE_CONDUIT_ENTITY, onde, feitio, CAPACITY);
    }

    @Override
    public void serverTick(ServerLevel level) {
        this.redstonePowered = level.hasNeighborSignal(this.getBlockPos());
        super.serverTick(level);
    }

    @Override
    public int chargeRate() {
        return CHARGE_RATE;
    }

    @Override
    public boolean canRequestPower() {
        return !this.redstonePowered;
    }

    @Override
    public boolean canProvidePower(PowerType type) {
        return false;
    }

    @Override
    public boolean canRelayPower(PowerType type) {
        return true;
    }

    /**
     * Onde nascem as faíscas, por eixo.
     *
     * <p>O conduto cola-se à parede em que foi posto, e o cristal dele vai com ele: o original move o
     * ponto <b>dois décimos para um lado ou oito para o outro</b>, conforme a face.
     */
    @Override
    public float particleOffset(int axis) {
        Direction lado = this.getBlockState().getValue(EssenceConduitBlock.FACING);
        return switch (axis) {
            case 0 -> lado == Direction.WEST ? 0.2f : lado == Direction.EAST ? 0.8f : 0.5f;
            case 1 -> lado == Direction.DOWN ? 0.2f : lado == Direction.UP ? 0.8f : 0.5f;
            case 2 -> lado == Direction.NORTH ? 0.2f : lado == Direction.SOUTH ? 0.8f : 0.5f;
            default -> 0.5f;
        };
    }

    // ---------------------------------------------------------------- o giro do cristal, no cliente

    /** O giro de agora, que cada conduto começa num lugar diferente e anda a um passo diferente. */
    public void clientTick() {
        if (this.primeiraVolta) {
            var sorte = this.level.getRandom();
            this.rotationX = sorte.nextInt(360);
            this.rotationY = sorte.nextInt(360);
            this.rotationZ = sorte.nextInt(360);
            this.incX = sorte.nextFloat() * 0.002f + 0.005f;
            this.incY = sorte.nextFloat() * 0.002f + 0.005f;
            this.incZ = sorte.nextFloat() * 0.002f + 0.005f;
            this.primeiraVolta = false;
        }
        this.rotationX = volta(this.rotationX + this.incX);
        this.rotationY = volta(this.rotationY + this.incY);
        this.rotationZ = volta(this.rotationZ + this.incZ);
    }

    private static float volta(float quanto) {
        if (quanto >= 360.0f) return 0.0f;
        if (quanto < 0.0f) return 359.0f;
        return quanto;
    }

    public float rotationX() {
        return this.rotationX;
    }

    public float rotationY() {
        return this.rotationY;
    }

    public float rotationZ() {
        return this.rotationZ;
    }
}
