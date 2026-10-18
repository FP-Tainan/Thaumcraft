package net.thaumcraft.occulta.rite;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

/**
 * O <b>registo dos marcadores de área</b>: a {@code BlockAreaMarker.AreaMarkerRegistry} do Witchery.
 *
 * <p>Ele existe por uma razão só: um rito que acontece <b>num lugar</b> tem de saber se a pessoa que ele
 * quer atingir está perto de um marcador <b>noutro lugar</b>. Procurar o bloco seria procurar o mundo
 * inteiro; por isso os marcadores se apresentam ao carregar e se despedem ao sumir, e o rito pergunta à
 * lista.
 *
 * <h2>E há a lista dos que mataram</h2>
 *
 * <p>Esta é a melhor ideia do bloco, e é fácil não a ver: um marcador <b>deixa de proteger quem matou
 * alguém perto dele</b>. Quem usa a proteção para caçar em paz perde-a no primeiro assassinato — e não a
 * recupera, porque o nome fica guardado no marcador para sempre.
 */
public final class AreaMarkers {
    /** Os dois feitios, e contra que rito cada um vale. */
    public enum Kind {
        /** O <b>lobo</b>: contra o rito da maldição dirigida. */
        CURSE,
        /** E a <b>mandrágora</b>: contra o puxão por teleporte. */
        TELEPORT,
    }

    /** O raio que o ajuste do original traz, nos dois: trinta e dois blocos. */
    public static final int RAIO = 32;

    /**
     * Um marcador vivo: onde está, de que feitio é, e quem já matou perto dele.
     *
     * <p>É uma <b>classe</b> e não um registo de propósito: a lista dos que mataram <b>muda</b>, e um
     * registo com um conjunto mutável lá dentro muda de código de dispersão a cada morte — e some do
     * conjunto que o guarda. Aqui o que vale é a identidade.
     */
    public static final class Marcador {
        private final ResourceKey<Level> mundo;
        private final BlockPos onde;
        private final Kind feitio;
        private final Set<UUID> matou;

        public Marcador(ResourceKey<Level> mundo, BlockPos onde, Kind feitio, Set<UUID> matou) {
            this.mundo = mundo;
            this.onde = onde;
            this.feitio = feitio;
            this.matou = matou;
        }

        public ResourceKey<Level> mundo() {
            return this.mundo;
        }

        public BlockPos onde() {
            return this.onde;
        }

        public Kind feitio() {
            return this.feitio;
        }

        public Set<UUID> matou() {
            return this.matou;
        }
    }

    private static final java.util.List<Marcador> VIVOS = new java.util.ArrayList<>();

    private AreaMarkers() {
    }

    /**
     * Liga o ouvido da morte: é por ele que a <b>lista dos que mataram</b> se enche.
     *
     * <p>O original pendura isto num gancho de morte do Forge e pergunta pela <b>causa direta</b> do
     * golpe. Aqui é o mesmo gancho do jogo de hoje, com a mesma pergunta.
     */
    public static void init() {
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AFTER_DEATH.register(
                (quemMorreu, fonte) -> {
                    if (fonte.getDirectEntity() instanceof LivingEntity quemMatou) {
                        matou(quemMorreu, quemMatou);
                    }
                });
    }

    public static void register(Marcador qual) {
        VIVOS.removeIf(m -> m.mundo().equals(qual.mundo()) && m.onde().equals(qual.onde()));
        VIVOS.add(qual);
    }

    public static void remove(Marcador qual) {
        VIVOS.removeIf(m -> m == qual);
    }

    /** Quantos marcadores estão carregados, que é o que a prova pergunta. */
    public static int quantos() {
        return VIVOS.size();
    }

    /** Esquece todos: a suíte precisa disto entre provas, e o original faz o mesmo ao ligar o servidor. */
    public static void esqueceTudo() {
        VIVOS.clear();
    }

    /**
     * <b>Se algum marcador do feitio certo protege esta pessoa.</b>
     *
     * <p>Protege estando a menos de <b>trinta e dois blocos</b>, no <b>mesmo mundo</b> — e <b>não</b>
     * protegendo quem já matou alguém perto dele.
     */
    public static boolean protege(@Nullable LivingEntity quem, Kind feitio) {
        if (quem == null) return false;
        ResourceKey<Level> mundo = quem.level().dimension();
        for (Marcador qual : VIVOS) {
            if (qual.feitio() != feitio || !qual.mundo().equals(mundo)) continue;
            if (qual.matou().contains(quem.getUUID())) continue;
            if (!perto(qual, quem)) continue;
            return true;
        }
        return false;
    }

    /**
     * <b>Quem matou alguém perto de um marcador fica marcado nele.</b> O original conta só a morte de
     * <b>gente</b> às mãos de <b>gente</b>, e não a de bichos.
     */
    public static void matou(LivingEntity quemMorreu, LivingEntity quemMatou) {
        if (!(quemMorreu instanceof net.minecraft.world.entity.player.Player)) return;
        if (!(quemMatou instanceof net.minecraft.world.entity.player.Player)) return;
        if (quemMorreu == quemMatou) return;
        ResourceKey<Level> mundo = quemMatou.level().dimension();
        for (Marcador qual : VIVOS) {
            if (!qual.mundo().equals(mundo) || !perto(qual, quemMatou)) continue;
            qual.matou().add(quemMatou.getUUID());
        }
    }

    /**
     * A distância do original, que é <b>chata de propósito</b>: ela mede em X e Z e <b>finge que o Y é
     * um</b> nos dois lados. Um marcador no fundo de uma mina protege quem está no céu por cima dele.
     */
    private static boolean perto(Marcador qual, LivingEntity quem) {
        double dx = quem.getX() - qual.onde().getX();
        double dz = quem.getZ() - qual.onde().getZ();
        return dx * dx + dz * dz <= (double) RAIO * RAIO;
    }
}
