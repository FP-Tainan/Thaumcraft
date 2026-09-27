package net.thaumcraft.occulta.rite;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.thaumcraft.occulta.RitualCircles;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * A lista dos ritos: o {@code RiteRegistry} do Witchery.
 *
 * <p>Cada rito pede três coisas: os <b>anéis de giz</b> que tem de haver em volta, o que se <b>oferece</b> e,
 * às vezes, a <b>hora</b> — há ritos que só acontecem de noite, ou na chuva, ou na trovoada.
 *
 * <p>Quem bate no glifo do meio percorre esta lista e começa <b>todos</b> os que batem: é assim no original, e é
 * por isso que um círculo bem desenhado pode fazer duas coisas de uma vez.
 */
public final class RiteRegistry {
    /** A hora que um rito pede. */
    public enum When {
        DAY, NIGHT, RAIN, THUNDER
    }

    /** O que um anel tem de ter: quantos glifos de cada giz. */
    public record Ring(int ritual, int otherwhere, int infernal) {
        public static final Ring NONE = new Ring(0, 0, 0);

        public boolean satisfiedBy(RitualCircles.Ring achado) {
            return achado.ritual() >= this.ritual && achado.otherwhere() >= this.otherwhere
                    && achado.infernal() >= this.infernal;
        }

        public boolean empty() {
            return this.ritual == 0 && this.otherwhere == 0 && this.infernal == 0;
        }
    }

    /**
     * Um rito da lista.
     *
     * @param key       o nome dele no idioma
     * @param rite      o que ele faz
     * @param sacrifice o que ele pede
     * @param inner     o anel de dentro, o do meio e o de fora
     * @param when      a hora, se pedir alguma
     */
    public record Entry(String key, Rite rite, Sacrifice sacrifice, Ring inner, Ring middle, Ring outer,
                        EnumSet<When> when) {
        /** Se o mundo, o círculo e o chão batem com o que este rito pede. */
        public boolean matches(ServerLevel level, BlockPos meio, RitualCircles.Circles círculos,
                               List<ItemEntity> noChão) {
            if (!this.inner.satisfiedBy(círculos.inner())) return false;
            if (!this.middle.satisfiedBy(círculos.middle())) return false;
            if (!this.outer.satisfiedBy(círculos.outer())) return false;
            for (When hora : this.when) {
                boolean bate = switch (hora) {
                    case DAY -> level.isBrightOutside();
                    case NIGHT -> !level.isBrightOutside();
                    case RAIN -> level.isRaining();
                    case THUNDER -> level.isThundering();
                };
                if (!bate) return false;
            }
            return this.sacrifice.matches(level, meio, noChão);
        }

        /** A fila de passos deste rito: primeiro o que ele pede, depois o que ele faz. */
        public List<RiteStep> steps(int coven) {
            List<RiteStep> fila = new ArrayList<>();
            this.sacrifice.steps(fila);
            fila.addAll(this.rite.steps(coven));
            return fila;
        }
    }

    private static final List<Entry> RITES = new ArrayList<>();

    private RiteRegistry() {
    }

    public static List<Entry> all() {
        return List.copyOf(RITES);
    }

    public static Entry get(String key) {
        for (Entry rito : RITES) {
            if (rito.key().equals(key)) return rito;
        }
        return null;
    }

    public static Entry register(Entry rito) {
        RITES.add(rito);
        return rito;
    }

    /** Um rito sem hora marcada. */
    public static Entry register(String key, Rite rite, Sacrifice sacrifice, Ring inner, Ring middle, Ring outer) {
        return register(new Entry(key, rite, sacrifice, inner, middle, outer, EnumSet.noneOf(When.class)));
    }

    /** Os que batem com o que há ali. */
    public static List<Entry> find(ServerLevel level, BlockPos meio, RitualCircles.Circles círculos,
                                   List<ItemEntity> noChão) {
        List<Entry> achados = new ArrayList<>();
        for (Entry rito : RITES) {
            if (rito.matches(level, meio, círculos, noChão)) achados.add(rito);
        }
        return achados;
    }
}
