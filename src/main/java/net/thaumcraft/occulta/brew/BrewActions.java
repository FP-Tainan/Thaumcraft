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
     * O ingrediente que <b>fere</b>: o {@code BrewActionEffect} anônimo do <b>Ferir Demônios</b>.
     *
     * <p>É o único cozimento do original que faz <b>dano</b> e não põe poção nenhuma, e é por isso que ele
     * precisa de uma classe só dele. Bate em tudo — mas bate <b>muito mais</b> no que é do inferno.
     *
     * <p>A força dele tem um teto baixíssimo: <b>um</b>, ou três se o tempero que desliga o teto estiver no
     * caldeirão. É um deslocamento de bits, de modo que a diferença entre um e três é de vinte para oitenta.
     *
     * <p>E tem um terceiro caso, que é a razão de ele existir: contra o <b>Leonard</b>, ele não bate — ele
     * abre a <b>porta da fraqueza</b> dele, que salta os tetos de doze, quatro e um que o chefe tem. É a
     * maneira de o matar antes do anoitecer.
     */
    public static class Harm extends BrewAction {
        /** O que ele tira do que é do inferno, e do que não é. */
        public static final int NO_INFERNO = 10;
        public static final int NO_RESTO = 3;

        /** E o teto da força dele: um, ou três sem o teto. */
        public static final int TETO = 1;
        public static final int TETO_SEM_TETO = 3;

        private final int weight;

        public Harm(Item key, BrewName.Text namePart, int power, int weight) {
            super(key, namePart, power);
            this.weight = weight;
        }

        @Override
        public boolean isEffect() {
            return true;
        }

        @Override
        public boolean augmentCapacity(BrewCapacity espaço) {
            return espaço.consume(this.weight);
        }

        @Override
        public void applyToEntity(Level level, LivingEntity quem, BrewModifiers temperos) {
            if (temperos.disableEntityTarget) return;
            if (!(level instanceof net.minecraft.server.level.ServerLevel server)) return;

            int força = Math.min(temperos.getStrength(),
                    temperos.strengthCeilingDisabled ? TETO_SEM_TETO : TETO);
            if (quem instanceof net.thaumcraft.occulta.leonard.LeonardEntity leonard) {
                leonard.feriDeFraqueza(server, quanto(NO_INFERNO, força, temperos));
                temperos.reset();
                return;
            }

            boolean doInferno = net.thaumcraft.occulta.torment.Demonic.é(quem);
            quem.hurtServer(server,
                    server.damageSources().indirectMagic(temperos.quemFez, temperos.quemFez),
                    quanto(doInferno ? NO_INFERNO : NO_RESTO, força, temperos));
            temperos.reset();
        }

        /** O {@code (base << força) * escala}, arredondado para cima como no original. */
        public static int quanto(int base, int força, BrewModifiers temperos) {
            return net.minecraft.util.Mth.ceil((base << força) * temperos.powerScale);
        }
    }

    /**
     * <b>Atração de Bichos</b>, e invertida a <b>repulsa</b>: o {@code BrewActionEffect} do Coração de Ouro.
     *
     * <p>Todo bicho a <b>dezesseis blocos</b> por grau vem ter com quem bebeu — e, invertido, foge dele para
     * dez blocos de distância, teleportando-se se não achar caminho. É o cozimento mais barulhento do mod:
     * um frasco de grau alto num campo traz um rebanho inteiro atrás de quem o bebeu.
     *
     * <p>E ele <b>doma</b>. Um bicho domesticável que ainda não tenha dono passa a ser de quem bebeu, de
     * graça; invertido, um bicho domado de <b>outra pessoa</b> deixa de ser dela. É a única coisa no mod que
     * doma sem se dar nada ao bicho, e é por isso que o coração custa quinhentos de poder e o quarto grau.
     *
     * <p><b>Quem pode ser familiar escapa</b> — o gato, o sapo e a coruja. É o {@code couldBeFamiliar} do
     * original, e a razão é clara: um frasco não há de roubar o familiar de ninguém.
     *
     * <p><b>Dois pedaços do original que não vieram, e são declarados:</b> a conta do {@code maxCreatures},
     * que o original calcula e <b>nunca usa</b> — não há limite de bichos lá, e não há aqui; e a cor de
     * pelagem que ele sorteava ao domar uma <b>jaguatirica</b>, que em 2014 era o que virava gato. Hoje o
     * gato já nasce gato, e de qualquer modo ele é um dos que escapam.
     */
    public static class AnimalAttraction extends BrewAction {
        /** O alcance por grau, e a que distância o invertido atira os bichos. */
        public static final int POR_GRAU = 16;
        public static final int PARA_LONGE = 6;

        /** E a vida com que um bicho domado fica, que é o {@code setHealth(20)} do original. */
        public static final float DOMADO = 20.0f;

        private final int weight;

        public AnimalAttraction(Item key, BrewName.Part namePart, int power, int weight) {
            super(key, namePart, power);
            this.weight = weight;
        }

        @Override
        public boolean isEffect() {
            return true;
        }

        @Override
        public boolean augmentCapacity(BrewCapacity espaço) {
            return espaço.consume(this.weight);
        }

        /** O raio: dezesseis por grau, a partir de um. */
        public static double raio(int força) {
            return (força + 1) * (double) POR_GRAU;
        }

        @Override
        public void applyToEntity(Level level, LivingEntity quem, BrewModifiers temperos) {
            if (temperos.disableEntityTarget) return;
            if (!(level instanceof net.minecraft.server.level.ServerLevel server)) {
                temperos.reset();
                return;
            }
            boolean invertido = temperos.inverted;
            double raio = raio(temperos.getStrength());
            var caixa = quem.getBoundingBox().inflate(raio);

            for (LivingEntity outro : server.getEntitiesOfClass(LivingEntity.class, caixa)) {
                if (outro == quem || outro.distanceToSqr(quem) >= raio * raio) continue;
                if (outro instanceof net.minecraft.world.entity.TamableAnimal bicho) {
                    // quem pode ser familiar escapa: um frasco não rouba o familiar de ninguém
                    if (net.thaumcraft.occulta.familiar.Familiars.deQueFeitio(bicho) != null) continue;
                    if (!invertido) {
                        doma(bicho, quem);
                    } else if (bicho.isTame() && !quem.getUUID().equals(deQuemÉ(bicho))) {
                        desdoma(bicho);
                    }
                }
                if (outro instanceof net.minecraft.world.entity.animal.Animal bicho) {
                    if (invertido) foge(server, bicho, quem);
                    else vem(bicho, quem);
                }
            }
            temperos.reset();
        }

        /** De quem aquele bicho é, se é de alguém. */
        private static @org.jetbrains.annotations.Nullable java.util.UUID deQuemÉ(
                net.minecraft.world.entity.TamableAnimal bicho) {
            var dono = bicho.getOwnerReference();
            return dono == null ? null : dono.getUUID();
        }

        /** O {@code setTamed} do original: dono novo, caminho limpo, alvo limpo e vida cheia. */
        private static void doma(net.minecraft.world.entity.TamableAnimal bicho, LivingEntity dono) {
            if (bicho.isTame()) return;
            bicho.setTame(true, true);
            bicho.setOwner(dono);
            bicho.getNavigation().stop();
            bicho.setTarget(null);
            bicho.setHealth(DOMADO);
        }

        /** E o contrário: ele deixa de ser de quem era. */
        private static void desdoma(net.minecraft.world.entity.TamableAnimal bicho) {
            bicho.setTame(false, true);
            bicho.setOwnerReference(null);
            bicho.getNavigation().stop();
            bicho.setTarget(null);
        }

        /** Vem ter com quem bebeu. */
        private static void vem(net.minecraft.world.entity.animal.Animal bicho, LivingEntity quem) {
            bicho.getNavigation().moveTo(quem.getX(), quem.getY(), quem.getZ(), 1.0);
        }

        /**
         * E foge: para seis a onze blocos dali, no primeiro lugar com ar por cima — e, não achando
         * caminho, <b>por cima</b>, que é o {@code setPositionAndUpdate} do original.
         */
        private static void foge(net.minecraft.server.level.ServerLevel level,
                                 net.minecraft.world.entity.animal.Animal bicho, LivingEntity quem) {
            var sorte = level.getRandom();
            int x = net.minecraft.util.Mth.floor(quem.getX())
                    + (sorte.nextBoolean() ? 1 : -1) * (PARA_LONGE + sorte.nextInt(PARA_LONGE));
            int z = net.minecraft.util.Mth.floor(quem.getZ())
                    + (sorte.nextBoolean() ? 1 : -1) * (PARA_LONGE + sorte.nextInt(PARA_LONGE));
            /*
             * O original parte do <b>sessenta e dois</b>, que era a altura do mar de 2014, e sobe até achar
             * ar. Hoje a altura do mar é do mundo e não um número escrito à mão.
             */
            int y = level.getSeaLevel();
            while (y < level.getMaxY() && !level.isEmptyBlock(new net.minecraft.core.BlockPos(x, y + 1, z))) {
                y++;
            }
            if (bicho.getNavigation().moveTo(x, y, z, 1.0)) return;
            bicho.snapTo(0.5 + x, y, 0.5 + z, bicho.getYRot(), bicho.getXRot());
        }
    }

    /**
     * <b>Tirar os Maus Efeitos</b>: o {@code BrewActionEffect} do Leite Purificado.
     *
     * <p>É o contrário de tudo o mais que sai do caldeirão: ele não põe nada em ninguém — <b>tira</b>. E o
     * que ele tira depende da <b>força</b>, que aqui não é quanto ele dá mas <b>até que grau ele alcança</b>:
     * um efeito de grau maior do que a força dele fica onde está.
     *
     * <p>E há o que ele <b>não</b> tira: os que o leite não tira, que são os que o ofício põe em alguém de
     * propósito. Com uma exceção, e é a razão de este cozimento existir — a <b>Doença</b>. Do segundo grau
     * para cima ele tira-a, que é a única coisa no mod que a tira; e do <b>terceiro</b> para cima limpa
     * também as <b>poças</b> que a espalham, num cubo de dezenove casas à volta de quem bebeu. Sem isso,
     * curar alguém da doença era curá-lo até ele dar o passo seguinte.
     *
     * <p><b>Do original fica de fora, declarado:</b> a metade <b>ritual</b> dele — que tira até o
     * incurável e, falhando, <b>agrava</b> o que tirou —, porque os cozimentos de círculo de giz não estão
     * portados.
     */
    public static class Cure extends BrewAction {
        /** De que grau para cima ele alcança a Doença, e de qual limpa as poças dela. */
        public static final int CURA_A_DOENÇA = 2;
        public static final int LIMPA_AS_POÇAS = 3;

        /** E o cubo que ele limpa: nove para cada lado, que são dezenove casas de lado. */
        public static final int RAIO_DAS_POÇAS = 9;

        private final int weight;

        public Cure(Item key, BrewName.Text namePart, int power, int weight) {
            super(key, namePart, power);
            this.weight = weight;
        }

        @Override
        public boolean isEffect() {
            return true;
        }

        @Override
        public boolean augmentCapacity(BrewCapacity espaço) {
            return espaço.consume(this.weight);
        }

        @Override
        public void applyToEntity(Level level, LivingEntity quem, BrewModifiers temperos) {
            if (temperos.disableEntityTarget) return;
            if (!(level instanceof net.minecraft.server.level.ServerLevel server)) {
                temperos.reset();
                return;
            }
            int força = temperos.getStrength();
            java.util.List<net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect>> tirar =
                    new java.util.ArrayList<>();
            boolean doença = false;

            for (var efeito : quem.getActiveEffects()) {
                var qual = efeito.getEffect();
                if (qual.value().getCategory()
                        != net.minecraft.world.effect.MobEffectCategory.HARMFUL) {
                    continue;
                }
                if (!net.thaumcraft.occulta.OccultaEffects.incurable(qual)
                        && efeito.getAmplifier() <= força) {
                    tirar.add(qual);
                }
                if (qual == net.thaumcraft.occulta.OccultaEffects.DISEASED && força >= CURA_A_DOENÇA) {
                    tirar.add(qual);
                    doença = true;
                }
            }
            for (var qual : tirar) quem.removeEffect(qual);
            if (doença && força >= LIMPA_AS_POÇAS) limpaAsPoças(server, quem.blockPosition());
            temperos.reset();
        }

        /** O cubo de dezenove: toda poça de doença dentro dele some. */
        public static void limpaAsPoças(net.minecraft.server.level.ServerLevel level,
                                        net.minecraft.core.BlockPos meio) {
            for (net.minecraft.core.BlockPos onde : net.minecraft.core.BlockPos.betweenClosed(
                    meio.offset(-RAIO_DAS_POÇAS, -RAIO_DAS_POÇAS, -RAIO_DAS_POÇAS),
                    meio.offset(RAIO_DAS_POÇAS, RAIO_DAS_POÇAS, RAIO_DAS_POÇAS))) {
                if (level.getBlockState(onde).is(net.thaumcraft.occulta.OccultaBlocks.DISEASE)) {
                    level.removeBlock(onde, false);
                }
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
