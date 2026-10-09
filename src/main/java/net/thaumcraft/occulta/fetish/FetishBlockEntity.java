package net.thaumcraft.occulta.fetish;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A alma de um fetiche: o {@code TileEntityFetish} do Witchery.
 *
 * <p>Ela guarda três coisas, e as três se veem de fora:
 *
 * <ul>
 *   <li>o <b>efeito</b> preso a ela, que é o que o rito põe lá e o que faz o fetiche valer alguma coisa;</li>
 *   <li>a <b>tinta</b>, que se dá com um corante na mão;</li>
 *   <li>e as <b>três listas de conhecidos</b> — gente por nome, espécies por nome, e bichos um a um —, que
 *       se escrevem com o <b>Kit de Taglock</b> e se apagam com um <b>balde</b>.</li>
 * </ul>
 *
 * <h2>O alarme</h2>
 *
 * <p>De segundo em segundo, com um efeito que procure alguma coisa, ela olha em volta e decide se o alarme
 * se levanta. Há <b>seis modos</b>, que se rodam com a <b>Boline</b>:
 *
 * <table border="1">
 *   <caption>Os seis modos</caption>
 *   <tr><th>Modo</th><th>Levanta quando</th></tr>
 *   <tr><td>0</td><td>há <b>gente</b> que não está na lista</td></tr>
 *   <tr><td>1</td><td>há <b>gente</b> que está na lista</td></tr>
 *   <tr><td>2</td><td>há <b>o que for</b> que não está na lista</td></tr>
 *   <tr><td>3</td><td><b>nem todos</b> os conhecidos estão presentes</td></tr>
 *   <tr><td>4</td><td><b>nenhum</b> dos conhecidos está presente</td></tr>
 *   <tr><td>5</td><td>nunca — e é como ele nasce</td></tr>
 * </table>
 *
 * <p>Os modos três e quatro são o que fazem do Espantalho uma coisa diferente de um alarme: eles disparam
 * pela <b>ausência</b>. Um espantalho que conhece as suas vacas e avisa quando falta uma é a melhor ideia
 * do bloco, e está em duas linhas.
 *
 * <p>Ignora sempre: cadáveres, ilusões, <b>espíritos</b> e familiares. Um fetiche não se assusta com o que
 * a bruxa pôs lá.
 */
public class FetishBlockEntity extends BlockEntity {
    /** De quantas em quantas batidas ele olha em volta: o segundo do original. */
    public static final int LOOKS = 20;

    /** Os seis modos de alarme, e aquele com que ele nasce. */
    public static final int WHEN_PLAYER_NOT_KNOWN = 0;
    public static final int WHEN_PLAYER_KNOWN = 1;
    public static final int WHEN_ANYTHING_NOT_KNOWN = 2;
    public static final int WHEN_NOT_ALL_FOUND = 3;
    public static final int WHEN_NONE_FOUND = 4;
    public static final int OFF = 5;

    /** A tinta com que ele nasce: o cinza-claro do original. */
    public static final int DEFAULT_COLOR = 9;

    /** E o sinal de redstone que o Grito manda. */
    public static final int SIGNAL = 15;

    private boolean spectral;
    private int color = DEFAULT_COLOR;
    private int effectType = 0;
    private int alarmMode = OFF;
    private boolean lastAlarm;
    private long lastRun = Long.MIN_VALUE;
    private long ticks;

    private final List<String> knownPlayers = new ArrayList<>();
    private final List<String> knownTypes = new ArrayList<>();
    private final List<UUID> knownCreatures = new ArrayList<>();

    public FetishBlockEntity(BlockPos onde, BlockState feitio) {
        super(net.thaumcraft.occulta.OccultaBlocks.FETISH_ENTITY, onde, feitio);
    }

    @Override
    public void setLevel(net.minecraft.world.level.Level level) {
        super.setLevel(level);
        if (!level.isClientSide()) Fetishes.register(this);
    }

    @Override
    public void setRemoved() {
        Fetishes.remove(this);
        super.setRemoved();
    }

    // ------------------------------------------------------------------ o que ele é

    public boolean spectral() {
        return this.spectral;
    }

    public void spectral(boolean sim) {
        this.spectral = sim;
        this.sync();
    }

    public int color() {
        return this.color;
    }

    public void color(int qual) {
        this.color = qual;
        this.sync();
        this.syncSpectral();
    }

    public int effectType() {
        return this.effectType;
    }

    public void effectType(int qual) {
        this.effectType = qual;
        this.sync();
    }

    public int alarmMode() {
        return this.alarmMode;
    }

    public List<String> knownPlayers() {
        return this.knownPlayers;
    }

    public List<String> knownTypes() {
        return this.knownTypes;
    }

    public List<UUID> knownCreatures() {
        return this.knownCreatures;
    }

    /** Se o alarme está levantado agora, que é o que a redstone lê. */
    public boolean alarm() {
        return this.lastAlarm;
    }

    public int signal() {
        SpiritEffects qual = SpiritEffects.of(this);
        return qual != null && qual.redstone() && this.lastAlarm ? SIGNAL : 0;
    }

    // ------------------------------------------------------------------ a mão nele

    /** <b>A Boline roda o modo</b>, de zero a cinco e de volta ao zero. */
    public void cycleMode(Player quem) {
        this.alarmMode = (this.alarmMode + 1) % (OFF + 1);
        this.setChanged();
        this.syncSpectral();
        this.tell(quem);
    }

    /** <b>O balde limpa as três listas.</b> */
    public void clearKnown() {
        this.knownPlayers.clear();
        this.knownTypes.clear();
        this.knownCreatures.clear();
        this.setChanged();
        this.syncSpectral();
    }

    /**
     * <b>Um taglock junta ou tira</b> quem ele aponta.
     *
     * <p>As espécies <b>agrupáveis</b> entram por <b>nome de espécie</b> — qualquer vaca —, e todo o resto
     * por <b>nome próprio</b>, que é o que separa «as minhas vacas» de «aquela vaca».
     */
    public boolean toggleKnown(net.thaumcraft.occulta.TaglockItem.Taglock vínculo, Component nome) {
        if (vínculo.creature()) {
            String espécie = nome.getString();
            if (Fetishes.groupable(espécie)) {
                if (!this.knownTypes.remove(espécie)) this.knownTypes.add(espécie);
            } else if (!this.knownCreatures.remove(vínculo.owner())) {
                this.knownCreatures.add(vínculo.owner());
            }
        } else if (!this.knownPlayers.remove(vínculo.name())) {
            this.knownPlayers.add(vínculo.name());
        }
        this.setChanged();
        this.syncSpectral();
        return true;
    }

    /** E o fetiche escreve a lista no peito de quem mexeu, como o original faz. */
    public void tell(Player quem) {
        StringBuilder diz = new StringBuilder();
        for (String nome : this.knownPlayers) {
            if (!diz.isEmpty()) diz.append(", ");
            diz.append(nome);
        }
        for (String espécie : this.knownTypes) {
            if (!diz.isEmpty()) diz.append(", ");
            diz.append('#').append(espécie);
        }
        for (UUID quemÉ : this.knownCreatures) {
            if (!diz.isEmpty()) diz.append(", ");
            diz.append(quemÉ.toString(), 0, 8);
        }
        quem.sendSystemMessage(Component.translatable(
                "tc.scarecrow.operation." + Fetishes.MODE_KEYS[this.alarmMode], diz.toString()));
    }

    // ------------------------------------------------------------------ a batida

    public static void tick(net.minecraft.world.level.Level level, BlockPos onde, BlockState feitio,
                            FetishBlockEntity alma) {
        if (!(level instanceof ServerLevel servidor)) return;
        alma.ticks++;
        if (alma.ticks % LOOKS != 0L) return;
        alma.look(servidor);
    }

    /** <b>Olha em volta</b>, decide o alarme e deixa o efeito fazer o que faz. */
    public void look(ServerLevel level) {
        SpiritEffects qual = SpiritEffects.of(this);
        if (qual == null || qual.radius() <= 0.0) return;

        boolean achouAlgum = false;
        int achados = 0;
        int sobraram = 0;
        Set<String> espécies = new HashSet<>();
        List<LivingEntity> lista = new ArrayList<>();

        if (this.alarmMode != OFF) {
            double raio = qual.radius();
            BlockPos onde = this.getBlockPos();
            AABB roda = new AABB(onde.getX() + 0.5 - raio, onde.getY() + 0.5 - raio,
                    onde.getZ() + 0.5 - raio, onde.getX() + 0.5 + raio, onde.getY() + 0.5 + raio,
                    onde.getZ() + 0.5 + raio);

            List<? extends LivingEntity> emVolta =
                    this.alarmMode == WHEN_PLAYER_NOT_KNOWN || this.alarmMode == WHEN_PLAYER_KNOWN
                            ? level.getEntitiesOfClass(Player.class, roda)
                            : level.getEntitiesOfClass(LivingEntity.class, roda);
            sobraram = emVolta.size();

            for (LivingEntity quem : emVolta) {
                if (quem instanceof Player gente) {
                    if (this.knownPlayers.contains(gente.getName().getString())) {
                        achouAlgum = true;
                        achados++;
                        sobraram--;
                        if (this.alarmMode == WHEN_PLAYER_KNOWN) lista.add(gente);
                    } else if (this.alarmMode == WHEN_ANYTHING_NOT_KNOWN
                            || this.alarmMode == WHEN_PLAYER_NOT_KNOWN) {
                        lista.add(gente);
                    }
                    continue;
                }
                if (!(quem instanceof Mob bicho) || Fetishes.ignorable(level, bicho)) {
                    sobraram--;
                    continue;
                }
                String espécie = bicho.getType().getDescription().getString();
                if (this.knownTypes.contains(espécie)) {
                    achouAlgum = true;
                    espécies.add(espécie);
                    sobraram--;
                } else if (this.knownCreatures.contains(bicho.getUUID())) {
                    achouAlgum = true;
                    achados++;
                    sobraram--;
                } else if (this.alarmMode == WHEN_ANYTHING_NOT_KNOWN) {
                    lista.add(bicho);
                }
            }
        }

        boolean alarme = switch (this.alarmMode) {
            case WHEN_PLAYER_NOT_KNOWN, WHEN_ANYTHING_NOT_KNOWN -> sobraram > 0;
            case WHEN_PLAYER_KNOWN -> achouAlgum;
            case WHEN_NOT_ALL_FOUND -> achados != this.knownCreatures.size() + this.knownPlayers.size()
                    || this.knownTypes.size() != espécies.size();
            case WHEN_NONE_FOUND -> !achouAlgum;
            default -> false;
        };

        int descanso = qual.cooldown();
        long agora = level.getGameTime();
        if ((descanso == -1 || agora > this.lastRun + descanso) && qual.run(this, alarme, lista)) {
            this.lastRun = agora;
        }

        if (this.lastAlarm == alarme) return;
        this.lastAlarm = alarme;
        if (qual.redstone()) {
            level.updateNeighbourForOutputSignal(this.getBlockPos(), this.getBlockState().getBlock());
        }
    }

    // ------------------------------------------------------------------ a cópia do outro lado

    /**
     * O que se muda neste fetiche <b>se copia para a cópia dele</b> no mundo de cima: o
     * {@code syncSpectralEntities}.
     */
    public void syncSpectral() {
        if (!(this.level instanceof ServerLevel level)) return;
        if (!net.thaumcraft.occulta.spirit.SpiritWorld.is(level)) return;
        var outro = level.getServer().overworld();
        if (!(outro.getBlockEntity(this.getBlockPos()) instanceof FetishBlockEntity cópia)) return;
        if (cópia == this) return;
        cópia.copyFrom(this);
    }

    /** Copia deste o que não é a posição: a tinta, o modo e as três listas. */
    public void copyFrom(FetishBlockEntity outro) {
        this.color = outro.color;
        this.alarmMode = outro.alarmMode;
        this.effectType = outro.effectType;
        this.knownPlayers.clear();
        this.knownPlayers.addAll(outro.knownPlayers);
        this.knownTypes.clear();
        this.knownTypes.addAll(outro.knownTypes);
        this.knownCreatures.clear();
        this.knownCreatures.addAll(outro.knownCreatures);
        this.sync();
    }

    // ------------------------------------------------------------------ o que vai no item

    /** O que o fetiche largado leva dentro: a tinta, o modo e as três listas. */
    public Fetishes.Saved save() {
        return new Fetishes.Saved(this.color, this.alarmMode, List.copyOf(this.knownPlayers),
                List.copyOf(this.knownTypes), List.copyOf(this.knownCreatures));
    }

    /** E o que ele lê ao ser posto. */
    public void load(Fetishes.Saved guardado) {
        this.color = guardado.color();
        this.alarmMode = guardado.mode();
        this.knownPlayers.clear();
        this.knownPlayers.addAll(guardado.players());
        this.knownTypes.clear();
        this.knownTypes.addAll(guardado.types());
        this.knownCreatures.clear();
        this.knownCreatures.addAll(guardado.creatures());
        this.sync();
    }

    // ------------------------------------------------------------------ o disco e a rede

    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(),
                    this.getBlockState(), net.minecraft.world.level.block.Block.UPDATE_ALL);
        }
    }

    @Override
    public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registros) {
        return this.saveWithoutMetadata(registros);
    }

    @Override
    protected void saveAdditional(ValueOutput saída) {
        super.saveAdditional(saída);
        saída.putBoolean("Spectral", this.spectral);
        saída.putInt("BlockColor", this.color);
        saída.putInt("EffectTypeID", this.effectType);
        saída.putInt("AlarmMode", this.alarmMode);
        saída.putBoolean("LastAlarm", this.lastAlarm);
        saída.store("Known", Fetishes.Saved.CODEC, this.save());
    }

    @Override
    protected void loadAdditional(ValueInput entrada) {
        super.loadAdditional(entrada);
        this.spectral = entrada.getBooleanOr("Spectral", false);
        this.effectType = entrada.getIntOr("EffectTypeID", 0);
        this.lastAlarm = entrada.getBooleanOr("LastAlarm", false);
        var guardado = entrada.read("Known", Fetishes.Saved.CODEC);
        if (guardado.isPresent()) {
            this.load(guardado.get());
        } else {
            this.color = entrada.getIntOr("BlockColor", DEFAULT_COLOR);
            this.alarmMode = entrada.getIntOr("AlarmMode", OFF);
        }
    }
}
