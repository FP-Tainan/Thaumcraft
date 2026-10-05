package net.thaumcraft.occulta.divine;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityTypes;

/**
 * <b>Vais levar uma flecha</b>: a {@code PredictionArrow} do Witchery.
 *
 * <p>Ela estende a da briga e lhe troca só a pergunta: em vez de olhar <b>quem</b> bateu, olha <b>com o
 * quê</b>. Qualquer flecha serve — de esqueleto, de outro jogador, de um dispensador.
 *
 * <p>Mas o que ela faz nascer, passado o prazo, continua sendo um <b>esqueleto</b>, que é a herança que ela
 * recebe. O original fez assim de propósito: a profecia da flecha não promete um arqueiro, mas quando o mod
 * tem de a cumprir à força, manda um.
 */
public class ArrowPrediction extends FightPrediction {
    public ArrowPrediction(int id, int peso, double forçaPorBatida, String recado) {
        super(id, peso, forçaPorBatida, recado, EntityTypes.SKELETON, false);
    }

    @Override
    public boolean cumprida(ServerLevel level, ServerPlayer quem, DamageSource fonte, boolean atrasada,
                            boolean velha) {
        return fonte.is(DamageTypes.ARROW);
    }
}
