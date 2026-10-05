package net.thaumcraft.occulta.infusion;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.enslave.Enslavement;

/**
 * A <b>Infusão Infernal</b>: a {@code InfusionInfernal} do Witchery.
 *
 * <p>É a terceira das quatro, e a que muda <b>o que você é para os outros</b>. As outras duas lhe dão coisas
 * para fazer; esta lhe dá <b>gente</b>.
 *
 * <h2>O que ela dá</h2>
 *
 * <ul>
 *   <li><b>agachado, socar um bicho</b>: ele passa a ser <b>seu</b>. Custa <b>cinco</b>;</li>
 *   <li><b>socar um bicho sem agachar</b>: <b>todos os seus</b>, num raio de cinquenta blocos, largam o que
 *       estavam fazendo e vão <b>atrás dele</b>. Custa <b>um</b>;</li>
 *   <li><b>agachado, largar a Mão olhando o chão</b>: todos os seus <b>largam o alvo e vão para ali</b>. É
 *       de graça, e é o que faz um exército ser um exército e não uma matilha.</li>
 * </ul>
 *
 * <p>Repare na diferença entre o segundo e o terceiro: um manda <b>atacar</b>, o outro manda <b>ir</b>. Com
 * os dois, quem tem esta infusão deixa de lutar — ele <b>aponta</b>.
 *
 * <p><b>Fica de fora, por agora:</b> o <b>sacrifício</b> — agachado, socar outra vez um bicho que já é seu
 * o mata e <b>lhe toma o poder</b>. Os poderes de bicho são um ramo inteiro do original, com vinte e cinco
 * deles e uma barra própria, e entram numa fatia só sua. Até lá, sacrificar um escravo toca o tambor de
 * «não dá».
 */
public class InfernalInfusion extends Infusion {
    /** A que distância os seus o ouvem. */
    public static final double EXÉRCITO = 50.0;

    /** E quão acima e abaixo. */
    public static final double ALTURA = 15.0;

    /** O alcance do olhar que manda ir. */
    public static final double OLHAR = 15.0;

    /** O que cada coisa custa. */
    public static final int CUSTO_ESCRAVIZAR = 5;
    public static final int CUSTO_APONTAR = 1;

    public InfernalInfusion(int id) {
        super(id);
    }

    // ------------------------------------------------------------------ o soco

    @Override
    public void soca(ServerLevel level, ServerPlayer quem, ItemStack mão, Entity noquê) {
        if (!(noquê instanceof LivingEntity bicho)) {
            falha(level, quem);
            return;
        }
        if (quem.isShiftKeyDown()) {
            escraviza(level, quem, bicho);
            return;
        }
        if (!this.gasta(level, quem, CUSTO_APONTAR)) return;
        aponta(level, quem, bicho);
    }

    /**
     * <b>Agachado, o soco toma o bicho para si.</b>
     *
     * <p>E o que já é seu, socado outra vez, seria <b>sacrificado</b> — mas isso pede os poderes de bicho,
     * que ainda não estão portados. Por agora o tambor.
     */
    private void escraviza(ServerLevel level, ServerPlayer quem, LivingEntity bicho) {
        if (!Enslavement.podeSerEscravizado(bicho) || !(bicho instanceof Mob escravo)) {
            falha(level, quem);
            return;
        }
        if (Enslavement.escravoDe(escravo, quem)) {
            falha(level, quem);
            return;
        }
        if (!this.gasta(level, quem, CUSTO_ESCRAVIZAR)) return;

        Enslavement.escraviza(escravo, quem);
        escravo.setTarget(null);
        level.sendParticles(ParticleTypes.WITCH, escravo.getX(), escravo.getY() + 1.0, escravo.getZ(),
                16, 1.0, 2.0, 1.0, 0.0);
        toca(level, quem, SoundEvents.ZOMBIE_INFECT);
    }

    /** <b>Todos os seus vão atrás daquele.</b> */
    private static void aponta(ServerLevel level, ServerPlayer quem, LivingEntity noquê) {
        int quantos = 0;
        for (Mob escravo : perto(level, quem)) {
            if (!Enslavement.escravoDe(escravo, quem)) continue;
            quantos++;
            escravo.setTarget(noquê);
            if (escravo instanceof PathfinderMob caça) {
                caça.setLastHurtByMob(noquê);
            }
        }
        if (quantos == 0) return;
        level.sendParticles(ParticleTypes.CRIT, noquê.getX(), noquê.getY() + 1.0, noquê.getZ(),
                16, 0.5, 2.0, 0.5, 0.0);
        toca(level, quem, SoundEvents.PLAYER_BREATH);
    }

    // ------------------------------------------------------------------ o olhar que manda ir

    @Override
    public void largou(ServerLevel level, ServerPlayer quem, ItemStack mão, int faltam) {
        if (!quem.isShiftKeyDown()) {
            /*
             * Sem agachar, isto usaria o <b>poder de bicho</b> que se tomou do último sacrifício. Enquanto
             * esse ramo não entra, não há o que usar.
             */
            falha(level, quem);
            return;
        }

        Vec3 olhos = quem.getEyePosition();
        Vec3 rumo = olhos.add(quem.getLookAngle().scale(OLHAR));
        BlockHitResult bateu = level.clip(new ClipContext(olhos, rumo, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, quem));
        if (bateu.getType() == HitResult.Type.MISS
                || bateu.getDirection() != net.minecraft.core.Direction.UP) {
            falha(level, quem);
            return;
        }
        mandaIr(level, quem, bateu.getBlockPos().above());
    }

    /**
     * <b>Todos os seus largam o alvo e vão para ali.</b>
     *
     * <p>É de graça — o original não lhe tira carga nenhuma —, e é o que faz um exército ser um exército e
     * não uma matilha: sem isto, os seus só sabem atacar.
     */
    public static void mandaIr(ServerLevel level, ServerPlayer quem, BlockPos onde) {
        int quantos = 0;
        for (Mob escravo : perto(level, quem)) {
            if (!Enslavement.escravoDe(escravo, quem)) continue;
            quantos++;
            escravo.setTarget(null);
            escravo.setLastHurtByMob(null);
            if (escravo.getNavigation().moveTo(onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5, 1.0)) {
                continue;
            }
            var caminho = escravo.getNavigation().createPath(onde, 1);
            if (caminho != null) escravo.getNavigation().moveTo(caminho, 1.0);
        }
        if (quantos == 0) {
            falha(level, quem);
            return;
        }
        level.sendParticles(net.minecraft.core.particles.SpellParticleOption.create(
                        ParticleTypes.INSTANT_EFFECT, 1.0f, 1.0f, 1.0f, 1.0f),
                onde.getX() + 0.5, onde.getY() + 0.5, onde.getZ() + 0.5, 16, 0.5, 2.0, 0.5, 0.0);
        toca(level, quem, SoundEvents.ITEM_PICKUP);
    }

    /** Os bichos que estão perto bastante para o ouvir. */
    private static java.util.List<Mob> perto(ServerLevel level, ServerPlayer quem) {
        AABB caixa = new AABB(quem.getX() - EXÉRCITO, quem.getY() - ALTURA, quem.getZ() - EXÉRCITO,
                quem.getX() + EXÉRCITO, quem.getY() + ALTURA, quem.getZ() + EXÉRCITO);
        return level.getEntitiesOfClass(Mob.class, caixa);
    }
}
