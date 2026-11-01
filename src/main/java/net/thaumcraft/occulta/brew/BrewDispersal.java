package net.thaumcraft.occulta.brew;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * O jeito de um cozimento se espalhar: o {@code Dispersal} do Witchery.
 *
 * <p>São quatro, e estão os quatro: <b>de uma vez</b>, que é o frasco que arrebenta; <b>em nuvem</b>, que é
 * o frasco que abre; <b>em poça</b>, que é o frasco que escorre; e <b>de gatilho</b>, que é o frasco que
 * espera.
 */
public interface BrewDispersal {
    /** O que acontece onde o frasco bateu. */
    void onImpact(ServerLevel level, List<Item> dentro, HitResult onde, BrewImpact espalha);

    /** Quem atirou o frasco, se foi gente: é o {@code modifiers.caster} do original. */
    @org.jetbrains.annotations.Nullable
    static net.minecraft.world.entity.player.Player quemAtirou(BrewImpact espalha) {
        return espalha.thrower instanceof net.minecraft.world.entity.player.Player gente ? gente : null;
    }

    /** O pedaço de nome que este jeito põe na frente do nome do cozimento. */
    String nameKey();

    /**
     * E o que este jeito faz num <b>rito de caldeirão</b>: o {@code onUpdateRitual} do original.
     *
     * <p>Os mesmos quatro jeitos têm duas vidas. Atirado de um frasco, o estouro apanha três blocos; posto
     * num rito de círculo, o mesmo jeito apanha uma aldeia. Não é o cozimento que muda — é o braço.
     *
     * <p>Por omissão o rito acaba na primeira batida, que é o que o original faz com o que não tem rito
     * próprio.
     */
    default net.thaumcraft.occulta.brew.BrewRitual.Estado noRito(
            ServerLevel level, net.minecraft.core.BlockPos alvo, List<Item> dentro, int coven,
            int pulsos, BrewImpact espalha) {
        return net.thaumcraft.occulta.brew.BrewRitual.Estado.PRONTO;
    }

    /**
     * De uma vez: o {@code DispersalInstant}.
     *
     * <p>Tudo o que estiver a até <b>três blocos</b> (mais o alcance) apanha o cozimento — e apanha menos quanto
     * mais longe estiver do estouro. Em quem levou o frasco em cheio, vale inteiro.
     */
    class Instant implements BrewDispersal {
        /** O alcance do estouro, antes do que os temperos lhe somam. */
        public static final double RANGE = 3.0;

        /** E o mesmo três, no rito, contado em blocos inteiros. */
        public static final int RANGE_NO_RITO = 3;

        @Override
        public void onImpact(ServerLevel level, List<Item> dentro, HitResult onde, BrewImpact espalha) {
            double raio = RANGE + espalha.extent;
            double raioQuadrado = raio * raio;
            Vec3 meio = onde.getLocation();
            Entity emCheio = onde instanceof EntityHitResult acertou ? acertou.getEntity() : null;

            AABB volta = new AABB(meio.x - raio, meio.y - raio, meio.z - raio,
                    meio.x + raio, meio.y + raio, meio.z + raio);
            for (LivingEntity quem : level.getEntitiesOfClass(LivingEntity.class, volta)) {
                double distância = quem.distanceToSqr(meio);
                if (distância > raioQuadrado) continue;
                // de perto vale inteiro; de longe, o que sobra do caminho
                boolean cheio = quem == emCheio;
                double parte = cheio ? 1.0 : 1.0 - Math.sqrt(distância) / raio;
                BrewModifiers temperos = new BrewModifiers(parte, 0.5 * parte);
                temperos.quemFez = quemAtirou(espalha);
                temperos.bioma = espalha.bioma;
                Brew.apply(level, quem, dentro, temperos);
            }

            if (onde instanceof BlockHitResult bateu) {
                BrewModifiers temperos = new BrewModifiers();
                temperos.quemFez = quemAtirou(espalha);
                temperos.bioma = espalha.bioma;
                Brew.applyToBlock(level, dentro, bateu.getBlockPos(), bateu.getDirection(),
                        Mth.ceil(raio), temperos);
            }
        }

        @Override
        public String nameKey() {
            return "tc.brew.dispersal.splash";
        }

        /**
         * No rito, ele cai <b>de uma vez só</b> em cima do alvo, com três blocos de raio mais o alcance.
         *
         * <p>É o jeito mais simples e o mais cruel: uma batida, e acabou. É este que se usa para
         * amaldiçoar alguém marcado — o cozimento aparece em cima da pessoa, onde quer que ela esteja.
         */
        @Override
        public net.thaumcraft.occulta.brew.BrewRitual.Estado noRito(
                ServerLevel level, net.minecraft.core.BlockPos alvo, List<Item> dentro, int coven,
                int pulsos, BrewImpact espalha) {
            int raio = RANGE_NO_RITO + espalha.extent;
            var temperos = net.thaumcraft.occulta.brew.BrewRitual.temperos(coven);
            Brew.applyToBlock(level, dentro, alvo, Direction.UP, raio, temperos);

            var roda = new AABB(alvo).inflate(raio);
            for (LivingEntity quem : level.getEntitiesOfClass(LivingEntity.class, roda)) {
                if (quem.distanceToSqr(alvo.getX() + 0.5, alvo.getY(), alvo.getZ() + 0.5)
                        > (double) raio * raio) {
                    continue;
                }
                Brew.apply(level, quem, dentro, net.thaumcraft.occulta.brew.BrewRitual.temperos(coven));
            }
            net.thaumcraft.occulta.brew.BrewRitual.fumo(level, alvo);
            return net.thaumcraft.occulta.brew.BrewRitual.Estado.PRONTO;
        }
    }

    /**
     * Em nuvem: o {@code DispersalGas}.
     *
     * <p>O frasco não estoura — ele <b>abre</b>. Onde bateu fica uma nuvem que cresce sozinha e demora a sumir,
     * e quem passa por dentro dela apanha o cozimento aos poucos.
     */
    /**
     * <b>O gatilho</b>: o {@code DispersalTriggered}.
     *
     * <p>Este frasco não arrebenta em ninguém. Ele <b>fica</b>: acertando um botão, uma alavanca, uma porta
     * ou uma placa de pressão, troca a peça por uma {@linkplain net.thaumcraft.occulta.curse.CursedTwins
     * gêmea amaldiçoada} e espera ali, calado, até alguém mexer.
     *
     * <p>Acertando qualquer outra coisa — uma parede, um bicho, o chão —, <b>não faz nada</b>. O frasco se
     * perde. É o único jeito de espalhar do mod que pode ser desperdiçado, e é de propósito: ele vale por
     * acertar o lugar certo.
     *
     * <p>Duas vezes no mesmo botão com a mesma receita <b>somam</b>: a peça fica armada para duas pessoas.
     *
     * <p>É a armadilha mais limpa que o mod tem, porque ela não se vê. Um botão amaldiçoado é exatamente um
     * botão.
     */
    class Triggered implements BrewDispersal {
        @Override
        public void onImpact(ServerLevel level, List<Item> dentro, HitResult onde, BrewImpact espalha) {
            if (!(onde instanceof BlockHitResult bateu)) return;
            var quem = espalha.thrower;
            String nome = quem == null ? "" : quem.getName().getString();

            /*
             * O original olha duas casas: a de <b>dentro</b> do que foi acertado e a que fica <b>à frente</b>
             * da face. É por isso que um frasco que bate no chão ao pé de uma placa apanha a placa, e um que
             * bate de frente numa alavanca apanha a alavanca.
             */
            if (net.thaumcraft.occulta.curse.CursedBlocks.prende(level, bateu.getBlockPos(), dentro, nome)) {
                return;
            }
            net.thaumcraft.occulta.curse.CursedBlocks.prende(level,
                    bateu.getBlockPos().relative(bateu.getDirection()), dentro, nome);
        }

        @Override
        public String nameKey() {
            return "tc.brew.dispersal.triggered";
        }
    }

    /**
     * <b>Em poça</b>: o {@code DispersalLiquid}.
     *
     * <p>Onde o frasco bateu fica uma {@linkplain BrewLiquidBlock poça} que <b>escorre</b> — desce pelo
     * primeiro buraco que achar e no chão plano se abre em cruz, gastando altura a cada passo.
     *
     * <p>E posto como <b>rito de círculo</b> em vez de frasco, ele faz <b>chover</b>: cem casas acima do
     * alvo começam a cair {@linkplain BrewDropletEntity gotas} do cozimento, espalhadas pelo raio do
     * rito. É a imagem mais bonita do ofício, e a mais larga: um raio de dezesseis blocos, mais oito por
     * grau de alcance.
     */
    class Liquid implements BrewDispersal {
        /** O raio da chuva, e o que cada grau de alcance lhe soma. */
        public static final int RAIO = 16;
        public static final int POR_GRAU = 8;

        /** De que altura as gotas caem, e quanto essa altura varia. */
        public static final int ALTURA = 100;
        public static final int VARIA = 20;

        /** E quanto tempo ela dura: dez batidas, mais cinco por grau de duração. */
        public static final int DURA = 10;
        public static final int POR_DURAÇÃO = 5;
        @Override
        public void onImpact(ServerLevel level, List<Item> dentro, HitResult onde, BrewImpact espalha) {
            net.minecraft.core.BlockPos lugar = BrewLiquidBlock.where(onde);
            if (!BrewLiquidBlock.fits(level, lugar)) return;
            level.setBlockAndUpdate(lugar,
                    net.thaumcraft.occulta.OccultaBlocks.BREW_LIQUID.defaultBlockState());
            if (level.getBlockEntity(lugar) instanceof BrewFluidBlockEntity poça) poça.start(dentro, espalha);
        }

        @Override
        public String nameKey() {
            return "tc.brew.dispersal.liquid";
        }

        /**
         * No rito, ele <b>chove</b>.
         *
         * <p>Por batida nascem entre um quarto e metade do raio em gotas, cada uma num ponto sorteado do
         * disco e a cem casas de altura. Elas caem sozinhas, e o que apanharem pelo caminho apanha o
         * cozimento.
         */
        @Override
        public net.thaumcraft.occulta.brew.BrewRitual.Estado noRito(
                ServerLevel level, net.minecraft.core.BlockPos alvo, List<Item> dentro, int coven,
                int pulsos, BrewImpact espalha) {
            int raio = RAIO + POR_GRAU * espalha.extent;
            int metade = raio / 4;
            int quantas = metade + level.getRandom().nextInt(Math.max(metade, 1));
            for (int n = 0; n < quantas; n++) {
                int x = alvo.getX() - raio + level.getRandom().nextInt(2 * raio);
                int z = alvo.getZ() - raio + level.getRandom().nextInt(2 * raio);
                if (alvo.distToCenterSqr(x + 0.5, alvo.getY(), z + 0.5) > (double) raio * raio) continue;
                double y = alvo.getY() + ALTURA + level.getRandom().nextInt(VARIA);
                level.addFreshEntity(new BrewDropletEntity(level, x + 0.5, y, z + 0.5, dentro));
            }
            return pulsos < DURA + espalha.lifetime * POR_DURAÇÃO
                    ? net.thaumcraft.occulta.brew.BrewRitual.Estado.ANDANDO
                    : net.thaumcraft.occulta.brew.BrewRitual.Estado.PRONTO;
        }
    }

    class Gas implements BrewDispersal {
        /** Até onde o anel cresce, por grau de alcance. */
        public static final int CRESCE_ATÉ = 8;

        /** Quanto se procura chão para cima e para baixo em cada coluna. */
        public static final int PROCURA_CHÃO = 8;

        /** A chance de o próprio lugar apanhar o cozimento, e a que distância do chão quem passa apanha. */
        public static final double O_LUGAR = 0.01;
        public static final double PERTO_DO_CHÃO = 1.5;

        @Override
        public void onImpact(ServerLevel level, List<Item> dentro, HitResult onde, BrewImpact espalha) {
            net.minecraft.core.BlockPos lugar = BrewGasBlock.where(onde);
            if (!BrewGasBlock.fits(level, lugar)) return;
            level.setBlockAndUpdate(lugar, net.thaumcraft.occulta.OccultaBlocks.BREW_GAS.defaultBlockState());
            if (level.getBlockEntity(lugar) instanceof BrewFluidBlockEntity nuvem) nuvem.start(dentro, espalha);
        }

        @Override
        public String nameKey() {
            return "tc.brew.dispersal.gas";
        }

        /**
         * No rito, ele é um <b>anel que cresce</b>: uma batida, um bloco de raio a mais.
         *
         * <p>Em cada coluna do anel procura-se chão — oito para cima e oito para baixo — e o que estiver
         * ali apanha o cozimento. O <b>lugar</b> em si só o apanha uma vez em cem, que é o original a
         * dizer «isto é um gás e não uma inundação».
         *
         * <p>E cresce até <b>oito</b> blocos, mais oito por grau de alcance. Um rito de nuvem com dois
         * graus varre um círculo de vinte e quatro blocos de raio, anel a anel.
         */
        @Override
        public net.thaumcraft.occulta.brew.BrewRitual.Estado noRito(
                ServerLevel level, net.minecraft.core.BlockPos alvo, List<Item> dentro, int coven,
                int pulsos, BrewImpact espalha) {
            BrewShapes.hollowCircle(alvo, pulsos, casa -> {
                net.minecraft.core.BlockPos chão = oChão(level, casa);
                if (chão == null) return;
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.WITCH,
                        chão.getX() + 0.5, chão.getY() + 1.0, chão.getZ() + 0.5, 4, 0.3, 0.3, 0.3, 0.0);
                if (level.getRandom().nextDouble() < O_LUGAR) {
                    Brew.applyToBlock(level, dentro, chão, Direction.UP, 1,
                            net.thaumcraft.occulta.brew.BrewRitual.temperos(coven));
                }
                for (LivingEntity quem : level.getEntitiesOfClass(LivingEntity.class,
                        new AABB(chão).inflate(PERTO_DO_CHÃO))) {
                    Brew.apply(level, quem, dentro,
                            net.thaumcraft.occulta.brew.BrewRitual.temperos(coven));
                }
            });
            return pulsos < CRESCE_ATÉ + espalha.extent * CRESCE_ATÉ
                    ? net.thaumcraft.occulta.brew.BrewRitual.Estado.ANDANDO
                    : net.thaumcraft.occulta.brew.BrewRitual.Estado.PRONTO;
        }

        /** Onde está o chão desta coluna: oito para cima, oito para baixo, ou nada. */
        private static net.minecraft.core.@org.jetbrains.annotations.Nullable BlockPos oChão(
                ServerLevel level, net.minecraft.core.BlockPos casa) {
            for (int dy = 0; dy < PROCURA_CHÃO; dy++) {
                if (!level.getBlockState(casa.above(dy)).isAir()
                        && level.getBlockState(casa.above(dy + 1)).isAir()) {
                    return casa.above(dy);
                }
                if (dy > 0 && !level.getBlockState(casa.below(dy)).isAir()
                        && level.getBlockState(casa.below(dy - 1)).isAir()) {
                    return casa.below(dy);
                }
            }
            return null;
        }
    }
}
