package net.thaumcraft.occulta.infusion;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A <b>Infusão do Outro Lugar</b>: a {@code InfusionOtherwhere} do Witchery.
 *
 * <p>É a infusão do <b>enderman</b>, e é a que mais muda o que se pode fazer num mundo. Dá quatro coisas, e
 * as quatro são formas de <b>não estar onde se está</b>:
 *
 * <ul>
 *   <li><b>segurar e largar</b> a Mão: salta para onde se está olhando. Quanto mais tempo se segura, mais
 *       longe se vê — quarenta blocos de partida, e mais vinte por segundo. Custa <b>um</b>;</li>
 *   <li><b>agachado, segurar seis décimos de segundo e largar</b>: guarda <b>o lugar de voltar</b>;</li>
 *   <li><b>agachado, largar antes disso</b>: volta para ele, de onde quer que se esteja. Custa <b>dois</b>;</li>
 *   <li><b>socar um bicho</b>: atira-o — e a si — <b>oito blocos para cima</b>. Custa <b>dois</b>. E
 *       agachado, o leva consigo para o lugar de voltar, por <b>quatro</b>.</li>
 * </ul>
 *
 * <p>Repare no último: ele é a razão de a infusão existir. Levar <b>outra pessoa</b> para onde se quer, à
 * força, de qualquer distância, é o poder mais bruto que o mod dá — e custa quatro de duzentos.
 *
 * <h2>O que o salto vê</h2>
 *
 * <p>A conta do alcance é do original e é esquisita de boa: o alcance <b>cresce enquanto se segura</b>, de
 * segundo em segundo, e a cada segundo o jogo <b>diz se há onde chegar</b> — um tinir se há, um estouro se
 * não há. Quem segura a Mão está sondando o mundo à frente, e o ouve.
 *
 * <p><b>Desvio declarado:</b> o original tranca a Mão por <b>mil e quinhentos milésimos de segundo</b>
 * depois de cada salto, num número que ele escreve na própria peça. Aqui a trava é a do jogo — a mesma
 * que uma bola de ender usa —, e por isso <b>se vê</b> no inventário. São trinta batidas, que é o mesmo
 * tempo.
 */
public class OtherwhereInfusion extends Infusion {
    /** Quanto tempo agachado até ele poder guardar o lugar de voltar. */
    public static final int GUARDA = 60;

    /** O alcance de partida do salto, e o que cada segundo lhe soma. */
    public static final int ALCANCE = 40;
    public static final int POR_SEGUNDO = 20;

    /** Quanto o salto para cima sobe. */
    public static final double ACIMA = 8.0;

    /** A trava depois de um salto: os mil e quinhentos milésimos do original, em batidas. */
    public static final int TRAVA = 30;

    /** O que cada coisa custa. */
    public static final int CUSTO_SALTO = 1;
    public static final int CUSTO_VOLTA = 2;
    public static final int CUSTO_ACIMA = 2;
    public static final int CUSTO_LEVAR = 4;

    public OtherwhereInfusion(int id) {
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
            levaConsigo(level, quem, bicho);
            return;
        }
        if (!this.gasta(level, quem, CUSTO_ACIMA)) return;

        double sobeEle = quantoCabe(level, quem);
        double sobeOutro = quantoCabe(level, bicho);
        if (sobeEle <= 0.0 || sobeOutro <= 0.0) return;
        quem.resetFallDistance();
        quem.teleportTo(quem.getX(), quem.getY() + sobeEle, quem.getZ());
        bicho.resetFallDistance();
        bicho.teleportTo(bicho.getX(), bicho.getY() + sobeOutro, bicho.getZ());
    }

    /** Agachado, o soco leva o bicho consigo para o lugar de voltar. */
    private void levaConsigo(ServerLevel level, ServerPlayer quem, LivingEntity bicho) {
        Infusions.Volta volta = Infusions.volta(quem);
        if (volta == null || !this.gasta(level, quem, CUSTO_LEVAR)) {
            falha(level, quem);
            return;
        }
        ServerLevel destino = level.getServer().getLevel(volta.onde());
        if (destino == null) {
            falha(level, quem);
            return;
        }
        põeAli(destino, quem, volta.lugar());
        põeAli(destino, bicho, volta.lugar());
    }

    private static void põeAli(ServerLevel destino, Entity quem, BlockPos onde) {
        quem.resetFallDistance();
        quem.teleportTo(destino, onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5,
                java.util.Set.of(), quem.getYRot(), quem.getXRot(), true);
    }

    /**
     * Quanto céu há acima dele, até oito.
     *
     * <p>O original o mede com um raio para cima e tira dois do que acha, para a pessoa não ficar com a
     * cabeça no teto.
     */
    private static double quantoCabe(ServerLevel level, Entity quem) {
        Vec3 pés = new Vec3(quem.getX(), quem.getY(), quem.getZ());
        Vec3 teto = pés.add(0.0, ACIMA + 2.0, 0.0);
        BlockHitResult bateu = level.clip(new ClipContext(pés, teto, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, quem));
        if (bateu.getType() == HitResult.Type.MISS) return ACIMA;
        return Math.min(bateu.getLocation().y - quem.getY() - 2.0, ACIMA);
    }

    // ------------------------------------------------------------------ segurar e largar

    @Override
    public void segurando(ServerLevel level, ServerPlayer quem, ItemStack mão, int faltam) {
        int passou = this.quantoSeSegura() - faltam;
        if (quem.isShiftKeyDown()) {
            if (passou != GUARDA) return;
            quem.sendSystemMessage(Component.translatable("tc.occulta.infuse.cansetrecall")
                    .withStyle(ChatFormatting.GRAY));
            toca(level, quem, SoundEvents.NOTE_BLOCK_PLING.value());
            return;
        }
        if (passou <= 0 || passou % 20 != 0) return;

        if (olha(level, quem, passou) != null) {
            toca(level, quem, SoundEvents.EXPERIENCE_ORB_PICKUP);
            quem.sendSystemMessage(Component.translatable("tc.occulta.infuse.canteleport")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            toca(level, quem, SoundEvents.ITEM_PICKUP);
        }
    }

    @Override
    public void largou(ServerLevel level, ServerPlayer quem, ItemStack mão, int faltam) {
        int passou = this.quantoSeSegura() - faltam;

        if (quem.isShiftKeyDown() && passou >= GUARDA) {
            Infusions.guardaVolta(quem);
            BlockPos lugar = quem.blockPosition();
            quem.sendSystemMessage(Component.translatable("tc.occulta.infuse.setrecall",
                    lugar.getX(), lugar.getY(), lugar.getZ()).withStyle(ChatFormatting.GRAY));
            toca(level, quem, SoundEvents.FIRE_EXTINGUISH);
            return;
        }

        if (quem.isShiftKeyDown()) {
            Infusions.Volta volta = Infusions.volta(quem);
            if (volta == null || !this.gasta(level, quem, CUSTO_VOLTA)) {
                falha(level, quem);
                return;
            }
            ServerLevel destino = level.getServer().getLevel(volta.onde());
            if (destino == null) {
                falha(level, quem);
                return;
            }
            põeAli(destino, quem, volta.lugar());
            quem.getCooldowns().addCooldown(mão, TRAVA);
            return;
        }

        HitResult onde = olha(level, quem, passou);
        if (onde == null) {
            falha(level, quem);
            quem.sendSystemMessage(Component.translatable("tc.occulta.infuse.cannotteleport")
                    .withStyle(ChatFormatting.RED));
            return;
        }
        if (!this.gasta(level, quem, CUSTO_SALTO)) return;

        pó(level, quem);
        salta(level, quem, onde);
        pó(level, quem);
        quem.getCooldowns().addCooldown(mão, TRAVA);
    }

    private static void pó(ServerLevel level, ServerPlayer quem) {
        level.sendParticles(ParticleTypes.PORTAL, quem.getX(), quem.getY() + 1.0, quem.getZ(),
                16, 0.5, 2.0, 0.5, 0.0);
        toca(level, quem, SoundEvents.ENDERMAN_TELEPORT);
    }

    /**
     * Para onde ele está olhando, até o alcance daquele instante.
     *
     * <p>Quarenta de partida, e mais vinte por segundo segurado — que é o que faz segurar a Mão valer a pena.
     */
    private static @org.jetbrains.annotations.Nullable HitResult olha(ServerLevel level, ServerPlayer quem,
                                                                      int passou) {
        double longe = ALCANCE + (double) POR_SEGUNDO * (passou / 20);
        Vec3 olhos = quem.getEyePosition();
        Vec3 rumo = olhos.add(quem.getLookAngle().scale(longe));
        BlockHitResult bateu = level.clip(new ClipContext(olhos, rumo, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, quem));
        return bateu.getType() == HitResult.Type.MISS ? null : bateu;
    }

    /** E o põe do lado de cá da face em que bateu, como o original faz. */
    private static void salta(ServerLevel level, ServerPlayer quem, HitResult onde) {
        Vec3 ali = onde.getLocation();
        double x = ali.x;
        double y = ali.y;
        double z = ali.z;
        if (onde instanceof BlockHitResult bloco) {
            switch (bloco.getDirection()) {
                case DOWN -> y -= 2.0;
                case NORTH -> z -= 0.5;
                case SOUTH -> z += 0.5;
                case WEST -> x -= 0.5;
                case EAST -> x += 0.5;
                default -> {
                }
            }
        }
        quem.resetFallDistance();
        quem.teleportTo(x, y, z);
    }
}
