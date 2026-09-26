package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * O miolo do Altar: o {@code TileEntityAltar} do Witchery.
 *
 * <p>Só o bloco que manda guarda alguma coisa. Ele tem um <b>teto</b>, que é o que a natureza em volta dá (ver o
 * {@link AltarPower}), e um <b>tanto guardado</b>, que sobe dez por segundo — vezes o que os enfeites em cima
 * dele valerem — até bater no teto.
 *
 * <p>Os enfeites são os do original, no que já existe aqui:
 *
 * <ul>
 *   <li>uma <b>caveira</b> em cima de qualquer bloco do altar: de esqueleto soma um ao teto e à velocidade, de
 *       esqueleto wither soma dois, de gente soma três;</li>
 *   <li>e uma <b>tocha</b>: soma um à velocidade.</li>
 * </ul>
 *
 * <p><b>Do original ficam de fora, por agora,</b> o candelabro, o cálice, a Arthana, o Ramo Místico, o pentáculo
 * de kobolditas e o Ovo do Infinito — que ainda não foram portados. Os dois primeiros somam ao teto e à
 * velocidade; os outros dão alcance, poder de encanto e multiplicam a velocidade.
 */
public class AltarBlockEntity extends BlockEntity {
    /** Quanto poder entra a cada segundo, antes dos enfeites. */
    public static final float RECHARGE = 10.0f;

    /** O alcance de um altar, antes da Arthana. */
    public static final int RANGE = 16;

    /** De quanto em quanto o altar recarrega e reconta o que tem em volta. */
    public static final int RECHARGE_EVERY = 20;
    public static final int RESCAN_EVERY = 100;

    @Nullable
    private BlockPos core;
    private float power;
    private float maxPower;
    private int powerScale = 1;
    private int rechargeScale = 1;
    private int rangeScale = 1;
    private int enhancement;
    private long ticks;

    public AltarBlockEntity(BlockPos pos, BlockState state) {
        super(OccultaBlocks.WITCH_ALTAR_ENTITY, pos, state);
    }

    // ------------------------------------------------------------------ quem manda

    public boolean isCore() {
        return this.core != null && this.core.equals(this.worldPosition);
    }

    public boolean isValid() {
        return this.core != null;
    }

    /** O bloco que manda no altar de que este faz parte, ou nada se ele está sozinho. */
    @Nullable
    public AltarBlockEntity core() {
        if (this.core == null || this.level == null) return null;
        if (this.isCore()) return this;
        return this.level.getBlockEntity(this.core) instanceof AltarBlockEntity manda ? manda : null;
    }

    /** O {@code setCore}: quem manda mudou, e com ele muda a cara do bloco e o que ele guarda. */
    public void setCore(@Nullable BlockPos core) {
        this.core = core;
        if (core == null) {
            this.power = 0.0f;
            this.maxPower = 0.0f;
            this.powerScale = 1;
            this.rechargeScale = 1;
            this.rangeScale = 1;
            this.enhancement = 0;
            PowerSources.remove(this);
        } else if (this.isCore()) {
            this.refresh();
            PowerSources.register(this);
        }
        if (this.level != null) {
            BlockState estado = this.getBlockState();
            // a cara só se troca quando muda mesmo: trocá-la por trocar acorda a conta do bando outra vez
            if (estado.hasProperty(AltarBlock.JOINED) && estado.getValue(AltarBlock.JOINED) != (core != null)) {
                this.level.setBlock(this.worldPosition, estado.setValue(AltarBlock.JOINED, core != null),
                        Block.UPDATE_ALL);
            }
        }
        this.setChanged();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        PowerSources.remove(this);
    }

    // ------------------------------------------------------------------ o que ele tem

    public float power() {
        return this.power;
    }

    public float maxPower() {
        return this.maxPower * this.powerScale;
    }

    public int rechargeScale() {
        return this.rechargeScale;
    }

    public int rangeScale() {
        return this.rangeScale;
    }

    public int enhancement() {
        return this.enhancement;
    }

    public float range() {
        return (float) RANGE * this.rangeScale;
    }

    /** Tira poder, se houver. */
    public boolean consume(float quanto) {
        AltarBlockEntity manda = this.core();
        if (manda == null) return false;
        if (manda.power < quanto) return false;
        manda.power -= quanto;
        manda.setChanged();
        return true;
    }

    /** Reconta o que há em volta e o que está posto em cima. */
    public void refresh() {
        if (!(this.level instanceof ServerLevel server) || !this.isCore()) return;
        this.maxPower = AltarPower.maxPower(server, this.worldPosition);
        this.artefacts(server);
        this.setChanged();
    }

    /**
     * O {@code updateArtefacts}: anda pelos seis blocos do altar e olha o que está posto em cima de cada um.
     */
    private void artefacts(ServerLevel level) {
        int novoPoder = 1;
        int novaVelocidade = 1;
        boolean caveira = false;
        boolean luz = false;

        for (BlockPos onde : this.pieces(level)) {
            BlockState acima = level.getBlockState(onde.above());
            if (!caveira && acima.getBlock() instanceof SkullBlock caveiras) {
                int quanto = switch (caveiras.getType()) {
                    case SkullBlock.Types.SKELETON -> 1;
                    case SkullBlock.Types.WITHER_SKELETON -> 2;
                    case SkullBlock.Types.PLAYER -> 3;
                    default -> 0;
                };
                if (quanto > 0) {
                    novoPoder += quanto;
                    novaVelocidade += quanto;
                    caveira = true;
                }
            } else if (!luz && (acima.is(Blocks.TORCH) || acima.is(Blocks.WALL_TORCH))) {
                luz = true;
                novaVelocidade++;
            }
        }

        this.powerScale = novoPoder;
        this.rechargeScale = novaVelocidade;
    }

    /** Os blocos do altar de que este faz parte, a partir de quem manda. */
    public List<BlockPos> pieces(Level level) {
        List<BlockPos> achados = new ArrayList<>();
        List<BlockPos> porVer = new ArrayList<>();
        porVer.add(this.worldPosition);
        while (!porVer.isEmpty() && achados.size() < AltarBlock.PIECES) {
            BlockPos onde = porVer.remove(0);
            if (achados.contains(onde)) continue;
            achados.add(onde);
            for (Direction lado : Direction.Plane.HORIZONTAL) {
                BlockPos ao = onde.relative(lado);
                if (level.getBlockState(ao).is(OccultaBlocks.WITCH_ALTAR) && !achados.contains(ao)) porVer.add(ao);
            }
        }
        return achados;
    }

    // ------------------------------------------------------------------ o que ele faz sozinho

    public static void tick(Level level, BlockPos pos, BlockState state, AltarBlockEntity altar) {
        altar.ticks++;
        if (!altar.isCore()) return;
        if (altar.ticks % RESCAN_EVERY == 0) altar.refresh();
        if (altar.ticks % RECHARGE_EVERY != 0) return;

        PowerSources.register(altar);
        float teto = altar.maxPower();
        if (altar.power < teto) {
            altar.power = Math.min(altar.power + RECHARGE * altar.rechargeScale, teto);
            altar.setChanged();
        } else if (altar.power > teto) {
            altar.power = teto;
            altar.setChanged();
        }
    }

    // ------------------------------------------------------------------ o que ele guarda

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.core = input.read("Core", BlockPos.CODEC).orElse(null);
        this.power = input.getFloatOr("Power", 0.0f);
        this.maxPower = input.getFloatOr("MaxPower", 0.0f);
        this.powerScale = input.getIntOr("PowerScale", 1);
        this.rechargeScale = input.getIntOr("RechargeScale", 1);
        this.rangeScale = input.getIntOr("RangeScale", 1);
        this.enhancement = input.getIntOr("Enhancement", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.core != null) output.store("Core", BlockPos.CODEC, this.core);
        output.putFloat("Power", this.power);
        output.putFloat("MaxPower", this.maxPower);
        output.putInt("PowerScale", this.powerScale);
        output.putInt("RechargeScale", this.rechargeScale);
        output.putInt("RangeScale", this.rangeScale);
        output.putInt("Enhancement", this.enhancement);
    }
}
