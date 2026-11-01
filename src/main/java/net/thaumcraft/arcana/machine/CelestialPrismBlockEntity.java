package net.thaumcraft.arcana.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.arcana.ArcanaBlocks;
import net.thaumcraft.arcana.power.PowerNodes;
import net.thaumcraft.arcana.power.PowerType;

import java.util.Map;

/**
 * O <b>Prisma Celeste</b>: o {@code TileEntityCelestialPrism} da 1.4.0.009.
 *
 * <p>A fonte que não queima nada — <b>bebe do céu</b>. Dá <b>Luz</b>, um quarto por batida, e é a mais
 * fraca das três: duas mil e quinhentas de teto contra as cinco mil do Obelisco. Em compensação, nunca
 * precisa de ninguém.
 *
 * <p><b>Os quatro topos que ela aceita mudam tudo.</b> Vidro vale 1,1; ouro, 1,4; diamante, 2,0; e a
 * <b>pedra-da-lua</b>, <b>3,0</b> — mas a pedra-da-lua inverte-lhe o relógio: com ela o prisma carrega
 * <b>de noite</b> e fica parado de dia. É a troca que o original oferece, e é por isso que o topo mais
 * forte é também o mais incômodo.
 *
 * <p>A noite dele é das <b>12 500</b> às <b>23 500</b> batidas do dia.
 */
public class CelestialPrismBlockEntity extends EssenceGeneratorBlockEntity {
    /** Quanto ele guarda: <b>2500</b>. */
    public static final int CAPACITY = 2500;
    /** E quanto faz por batida, sem nada em volta: <b>um quarto</b>. */
    public static final float POWER_PER_TICK = 0.25f;
    /** O começo e o fim da noite dele. */
    public static final long NIGHT_FROM = 12_500L;
    public static final long NIGHT_TO = 23_500L;

    private static final Multiblock FIGURA = new Multiblock("celestialprism_structure");
    private static final Multiblock.Group PILARES = FIGURA.createGroup("pillars", MUTEX_PILLARS);
    private static final Multiblock.Group VIDRO = FIGURA.createGroup("caps_glass", MUTEX_CAPS);
    private static final Multiblock.Group OURO = FIGURA.createGroup("caps_gold", MUTEX_CAPS);
    private static final Multiblock.Group DIAMANTE = FIGURA.createGroup("caps_diamond", MUTEX_CAPS);
    private static final Multiblock.Group LUA = FIGURA.createGroup("caps_moonstone", MUTEX_CAPS);
    private static final Multiblock.Group GIZ = FIGURA.createGroup("wizardChalkCircle", Multiblock.MAIN_MUTEX);

    private static final Map<Multiblock.Group, Float> TOPOS_VALEM =
            caps(VIDRO, 1.1f, OURO, 1.4f, DIAMANTE, 2.0f, LUA, 3.0f);

    static {
        FIGURA.add(0, 0, 0, ArcanaBlocks.CELESTIAL_PRISM);
        for (int x : new int[]{-2, 2}) {
            for (int z : new int[]{-2, 2}) {
                FIGURA.add(PILARES, x, 0, z, Blocks.QUARTZ_BLOCK);
                FIGURA.add(PILARES, x, 1, z, Blocks.QUARTZ_BLOCK);
                FIGURA.add(VIDRO, x, 2, z, Blocks.GLASS);
                FIGURA.add(OURO, x, 2, z, Blocks.GOLD_BLOCK);
                FIGURA.add(DIAMANTE, x, 2, z, Blocks.DIAMOND_BLOCK);
                FIGURA.add(LUA, x, 2, z, ArcanaBlocks.MATERIAL_BLOCKS.get("moonstone"));
            }
        }
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                FIGURA.add(GIZ, i, 0, j, ArcanaBlocks.WIZARD_CHALK);
            }
        }
    }

    /** Se os topos de pedra-da-lua estão montados: então ele é de noite. */
    private boolean onlyChargeAtNight;

    public CelestialPrismBlockEntity(BlockPos onde, BlockState feitio) {
        super(ArcanaBlocks.CELESTIAL_PRISM_ENTITY, onde, feitio, CAPACITY);
    }

    public static Multiblock structure() {
        return FIGURA;
    }

    /** Ele é dos que carregam de noite. */
    public boolean nightly() {
        return this.onlyChargeAtNight;
    }

    @Override
    public void checkNearbyBlockState() {
        super.checkNearbyBlockState();
        this.onlyChargeAtNight = this.figura().matchedGroups(ALL_MUTEXES, this.level, this.getBlockPos()).contains(LUA);
    }

    /** A noite do original: entre as 12 500 e as 23 500 batidas do dia. */
    public boolean isNight() {
        long batidas = this.level.getOverworldClockTime() % 24_000L;
        return batidas >= NIGHT_FROM && batidas <= NIGHT_TO;
    }

    /** Se ele está a carregar agora. */
    public boolean charging() {
        return this.onlyChargeAtNight == this.isNight();
    }

    @Override
    public void serverTick(ServerLevel level) {
        if (this.surroundingCheckTicks++ % SURROUNDING_CHECK == 0) this.surroundings(level);
        if (this.charging()) {
            PowerNodes.of(level).insertPower(this.getBlockPos(), this, PowerType.LIGHT,
                    POWER_PER_TICK * this.powerMultiplier);
        }
        super.serverTick(level);
    }

    @Override
    protected Multiblock figura() {
        return FIGURA;
    }

    @Override
    protected Multiblock.Group pilares() {
        return PILARES;
    }

    @Override
    protected Multiblock.Group giz() {
        return GIZ;
    }

    @Override
    protected Map<Multiblock.Group, Float> topos() {
        return TOPOS_VALEM;
    }

    @Override
    protected PowerType type() {
        return PowerType.LIGHT;
    }

    /** O Prisma abençoa com grau um. */
    @Override
    protected int blessingAmplifier() {
        return 1;
    }
}
