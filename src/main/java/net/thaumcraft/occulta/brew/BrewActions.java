package net.thaumcraft.occulta.brew;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * Os feitios de ingrediente desta fatia: o que tempera, o que dá efeito e o que pinta.
 *
 * <p>São o {@code BrewActionModifier}, o {@code BrewPotionEffect} e o {@code BrewActionSetColor} do Witchery. O
 * que falta — o espalhamento, os efeitos que mexem no mundo e os rituais de círculo — vem nas fatias seguintes.
 */
public final class BrewActions {
    private BrewActions() {
    }

    /**
     * Um tempero: não gasta espaço nenhum e não faz nada sozinho — muda o efeito seguinte.
     *
     * <p>Os que abrem espaço também são deste feitio: eles não temperam nada, só alargam o caldeirão.
     */
    public static class Modifier extends BrewAction {
        private final java.util.function.Consumer<BrewCapacity> espaço;
        private final java.util.function.Consumer<BrewModifiers> tempero;
        private final int drinkSpeed;
        private java.util.function.Consumer<BrewImpact> espalhamento;

        public Modifier(Item key, BrewName.Part namePart, int power,
                        java.util.function.Consumer<BrewCapacity> espaço,
                        java.util.function.Consumer<BrewModifiers> tempero, int drinkSpeed) {
            super(key, namePart, power);
            this.espaço = espaço;
            this.tempero = tempero;
            this.drinkSpeed = drinkSpeed;
        }

        /** Um tempero comum: muda o efeito seguinte e mais nada. */
        public static Modifier tempering(Item key, BrewName.Part namePart, int power,
                                         java.util.function.Consumer<BrewModifiers> tempero) {
            return new Modifier(key, namePart, power, null, tempero, 0);
        }

        /** Um ingrediente de porte: abre espaço até o teto dele. */
        public static Modifier room(Item key, int power, int quanto, int teto) {
            return new Modifier(key, null, power, espaço -> espaço.openIf(quanto, teto), null, 0);
        }

        /** Um tempero do espalhamento: alarga o estouro ou o tempo que a coisa fica no chão. */
        public static Modifier impact(Item key, BrewName.Part namePart, int power,
                                      java.util.function.Consumer<BrewImpact> espalha) {
            Modifier feito = new Modifier(key, namePart, power, null, null, 0);
            feito.espalhamento = espalha;
            return feito;
        }

        /** E um que só muda o tempo que se leva a beber. */
        public static Modifier drink(Item key, int power, int quanto) {
            return new Modifier(key, null, power, null, null, quanto);
        }

        @Override
        public boolean augmentCapacity(BrewCapacity espaço) {
            if (this.espaço != null) this.espaço.accept(espaço);
            return true;
        }

        @Override
        public void augmentModifiers(BrewModifiers temperos) {
            if (this.tempero != null) this.tempero.accept(temperos);
        }

        @Override
        public void prepareImpact(BrewImpact espalha) {
            if (this.espalhamento != null) this.espalhamento.accept(espalha);
        }

        @Override
        public int drinkSpeedModifier() {
            return this.drinkSpeed;
        }
    }

    /**
     * Um efeito de poção: gasta espaço e, em quem bebe, põe o efeito.
     *
     * <p>Quando o cozimento está invertido — o olho de aranha fermentado —, o que entra é o <b>contrário</b> dele,
     * que no original é uma segunda poção escrita ao lado da primeira: a velocidade vira lentidão, a visão noturna
     * vira invisibilidade, a regeneração vira veneno.
     */
    public static class Potion extends BrewAction {
        public static final int NO_CEILING = BrewModifiers.STRENGTH_CEILING;

        private final Holder<MobEffect> effect;
        private final int duration;
        private final Holder<MobEffect> invertedEffect;
        private final int invertedDuration;
        private final int weight;
        private int strengthCeiling = BrewModifiers.STRENGTH_CEILING;
        private int baseStrength;

        public Potion(Item key, BrewName.Text namePart, int power, Holder<MobEffect> effect, int duration,
                      Holder<MobEffect> invertedEffect, int invertedDuration, int weight) {
            super(key, namePart.lasting(duration, invertedDuration), power);
            this.effect = effect;
            this.duration = duration;
            this.invertedEffect = invertedEffect;
            this.invertedDuration = invertedDuration;
            this.weight = weight;
        }

        public Potion(Item key, BrewName.Text namePart, int power, Holder<MobEffect> effect, int duration,
                      int weight) {
            this(key, namePart, power, effect, duration, effect, duration, weight);
        }

        public Potion ceiling(int teto) {
            this.strengthCeiling = teto;
            return this;
        }

        /**
         * A força de partida deste cozimento, para os que <b>já nascem num grau</b>.
         *
         * <p>Só o Colorido a usa, e é por ela que ele existe: dezesseis cozimentos do <b>mesmo</b> efeito, um
         * por tinta, e o que diz qual tinta é o <b>grau</b>. Sem isto, as dezesseis tintas dariam todas a
         * mesma cor.
         */
        public Potion base(int grau) {
            this.baseStrength = grau;
            return this;
        }

        @Override
        public boolean isEffect() {
            return true;
        }

        /** Que poção este cozimento põe: serve às provas, que de outro jeito só a veriam no corpo de alguém. */
        public Holder<MobEffect> effect() {
            return this.effect;
        }

        /** E qual ele põe <b>invertido</b>, que nos mais deles é a mesma. */
        public Holder<MobEffect> invertedEffect() {
            return this.invertedEffect;
        }

        @Override
        public boolean augmentCapacity(BrewCapacity espaço) {
            return espaço.consume(this.weight);
        }

        @Override
        public void applyToEntity(Level level, LivingEntity quem, BrewModifiers temperos) {
            if (temperos.disableEntityTarget) return;
            Holder<MobEffect> qual = temperos.inverted ? this.invertedEffect : this.effect;
            int quanto = temperos.inverted ? this.invertedDuration : this.duration;
            // o que é ruim não pega em quem traz máscara de gás
            if (temperos.protectedFromBadEffects && qual.value().getCategory()
                    == net.minecraft.world.effect.MobEffectCategory.HARMFUL) {
                temperos.reset();
                return;
            }
            apply(quem, temperos, qual, quanto, this.strengthCeiling, this.baseStrength);
            temperos.reset();
        }

        /** O {@code applyPotionEffect}: a força para no teto, e a duração estica com o tempero. */
        public static void apply(LivingEntity quem, BrewModifiers temperos, Holder<MobEffect> qual, int duração,
                                 int teto) {
            apply(quem, temperos, qual, duração, teto, 0);
        }

        /** O mesmo, com uma força de partida — que só o Colorido tem. */
        public static void apply(LivingEntity quem, BrewModifiers temperos, Holder<MobEffect> qual, int duração,
                                 int teto, int base) {
            int força = base + Math.min(temperos.getStrength(),
                    temperos.strengthCeilingDisabled ? NO_CEILING : teto);
            if (qual.value().isInstantaneous()) {
                if (quem.level() instanceof net.minecraft.server.level.ServerLevel level) {
                    qual.value().applyInstantaneousEffect(level, null, null, quem, força, temperos.powerScale);
                }
                return;
            }
            MobEffectInstance posto = new MobEffectInstance(qual, temperos.modifiedDuration(duração), força,
                    false, !temperos.noParticles);
            // as que o leite não tira vão pelo caminho do Incurable, como as da dobra
            if (net.thaumcraft.occulta.OccultaEffects.incurable(qual)) {
                net.thaumcraft.research.Incurable.add(quem, posto);
            } else {
                quem.addEffect(posto);
            }
        }
    }

    /**
     * O ingrediente que diz <b>como</b> o cozimento se espalha: o {@code BrewActionDispersal}.
     *
     * <p>Ele não gasta espaço nenhum e não tempera nada — só manda no jeito. Põe um pedaço na frente do nome
     * ("Cozimento Arremessável de...") e faz o frasco se atirar em vez de se beber.
     *
     * <p>Um espalhamento novo <b>desfaz</b> o anterior: dois não convivem no mesmo caldeirão.
     */
    public static class Dispersal extends BrewAction {
        private final BrewDispersal jeito;

        public Dispersal(Item key, int power, BrewDispersal jeito) {
            super(key, new BrewName.Text(jeito.nameKey(), BrewName.Position.PREFIX), power, true, -1);
            this.jeito = jeito;
        }

        public BrewDispersal jeito() {
            return this.jeito;
        }

        @Override
        public boolean augmentCapacity(BrewCapacity espaço) {
            return true;
        }

        @Override
        public void prepareImpact(BrewImpact espalha) {
            espalha.setDispersal(this.jeito);
        }
    }

    /** A lã tinta: manda na cor do caldo à força, e não faz mais nada. */
    public static class SetColor extends BrewAction {
        public SetColor(Item key, int power, int cor) {
            super(key, null, power, false, cor);
        }

        @Override
        public boolean augmentCapacity(BrewCapacity espaço) {
            return true;
        }
    }
}
