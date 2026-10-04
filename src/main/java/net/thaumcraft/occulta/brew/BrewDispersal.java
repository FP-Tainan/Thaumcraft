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
 * <p>O original tem quatro — de uma vez, gás, líquido e gatilho. Esta fatia traz o primeiro, que é o do frasco
 * que arrebenta; os outros três ficam declarados para as fatias seguintes.
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
     * De uma vez: o {@code DispersalInstant}.
     *
     * <p>Tudo o que estiver a até <b>três blocos</b> (mais o alcance) apanha o cozimento — e apanha menos quanto
     * mais longe estiver do estouro. Em quem levou o frasco em cheio, vale inteiro.
     */
    class Instant implements BrewDispersal {
        /** O alcance do estouro, antes do que os temperos lhe somam. */
        public static final double RANGE = 3.0;

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
                Brew.apply(level, quem, dentro, temperos);
            }

            if (onde instanceof BlockHitResult bateu) {
                BrewModifiers temperos = new BrewModifiers();
                temperos.quemFez = quemAtirou(espalha);
                Brew.applyToBlock(level, dentro, bateu.getBlockPos(), bateu.getDirection(),
                        Mth.ceil(raio), temperos);
            }
        }

        @Override
        public String nameKey() {
            return "tc.brew.dispersal.splash";
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

    class Gas implements BrewDispersal {
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
    }
}
