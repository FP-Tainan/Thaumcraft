package net.thaumcraft.occulta.infusion.beast;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.thaumcraft.occulta.infusion.Infusions;
import org.jetbrains.annotations.Nullable;

/**
 * O poder do <b>creeper</b>: a {@code CreaturePowerCreeper} do Witchery.
 *
 * <p><b>Estourar.</b> Em cima de si próprio, com três de força — e, se a Mão tiver sido segurada por
 * <b>três segundos ou mais</b>, com <b>seis</b> e por <b>duas</b> cargas em vez de uma.
 *
 * <p>Repare: o estouro é <b>em você</b>. Este poder não é uma arma, é uma saída — ou um modo muito ruidoso
 * de cavar.
 *
 * <h2>E ele engole raios</h2>
 *
 * <p>Quem o tem e for <b>atingido por um raio</b> não leva o golpe: ele é engolido, e a <b>carga de
 * infusão</b> sobe <b>vinte e cinco</b>. É o que o creeper faz quando um raio lhe cai em cima — ele fica
 * mais forte —, e aqui é o único jeito de encher a infusão sem rito e sem estátua.
 *
 * <p><b>Desvio declarado:</b> o original descobre que o golpe veio de um raio <b>lendo a pilha de chamadas</b>
 * à procura do {@code onStruckByLightning}, porque em 2014 não havia como perguntar. Aqui se pergunta à
 * fonte do dano, que é o que ele queria saber.
 */
public class CreeperPower extends CreaturePower {
    /** A força do estouro, e a dele segurado. */
    public static final float FORÇA = 3.0f;
    public static final int SEGURADO = 60;

    /** E quanto o raio enche. */
    public static final int DO_RAIO = 25;

    public CreeperPower(int id) {
        super(id, EntityTypes.CREEPER);
    }

    @Override
    public int custo(int segurou) {
        return segurou >= SEGURADO ? 2 : 1;
    }

    @Override
    public void usa(ServerLevel level, ServerPlayer quem, int segurou, @Nullable HitResult onde) {
        float força = segurou >= SEGURADO ? FORÇA * 2.0f : FORÇA;
        level.explode(quem, quem.getX(), quem.getY(), quem.getZ(), força, false,
                Level.ExplosionInteraction.MOB);
    }

    @Override
    public boolean levouGolpe(ServerLevel level, ServerPlayer quem, DamageSource fonte) {
        if (!fonte.is(net.minecraft.world.damagesource.DamageTypes.LIGHTNING_BOLT)) return false;
        Infusions.enche(quem, DO_RAIO);
        return true;
    }
}
