package net.thaumcraft.occulta.infusion;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

/**
 * Uma <b>Infusão</b>: a {@code Infusion} do Witchery.
 *
 * <p>É o maior passo que o ofício dá. Até aqui, tudo o que a bruxa faz está <b>fora</b> dela — o caldeirão,
 * o círculo, o altar, o boneco. A infusão é a primeira coisa que ela faz <b>a si própria</b>: um rito que a
 * mata quase por completo e, se ela sobreviver, a deixa com um <b>poder dentro do corpo</b> que ela gasta e
 * tem de voltar a encher.
 *
 * <h2>O que uma infusão é</h2>
 *
 * <ul>
 *   <li>um <b>número</b>, que é o que fica guardado no jogador;</li>
 *   <li>uma <b>carga</b> e um <b>teto</b> de carga, que o rito dá e os poderes gastam;</li>
 *   <li>e um punhado de <b>ganchos</b> que só a <b>Mão de Bruxa</b> sabe chamar.</li>
 * </ul>
 *
 * <p>Uma de cada vez: infundir-se outra vez <b>troca</b> a que se tinha. E não é de graça — o rito faz
 * <b>cem de dano mágico</b> a quem estiver no círculo, e só infunde quem sobreviver.
 *
 * <h2>A Mão de Bruxa</h2>
 *
 * <p>A infusão <b>não faz nada sozinha</b>. Tudo o que ela sabe fazer passa pela {@link
 * net.thaumcraft.occulta.WitchHandItem Mão de Bruxa}, que não é feita por ninguém: cai de uma bruxa morta,
 * uma vez em três — ou uma em duas, se quem a matou tinha a Arthana na mão.
 *
 * <p>Quer dizer que ter a infusão e não ter a mão é ter o poder e não ter como o usar. É de propósito.
 */
public abstract class Infusion {
    /** O número dela, que é o que fica guardado no jogador. */
    public final int id;

    protected Infusion(int id) {
        this.id = id;
    }

    /** O gancho da batida, com a Mão na mão. */
    public void bate(ServerLevel level, ServerPlayer quem, ItemStack mão) {
    }

    /** O gancho do soco num bicho, com a Mão na mão. */
    public void soca(ServerLevel level, ServerPlayer quem, ItemStack mão, Entity noquê) {
        falha(level, quem);
    }

    /** O gancho de cada batida com a Mão <b>segurada</b>. */
    public void segurando(ServerLevel level, ServerPlayer quem, ItemStack mão, int faltam) {
    }

    /** E o de a largar. */
    public void largou(ServerLevel level, ServerPlayer quem, ItemStack mão, int faltam) {
        falha(level, quem);
    }

    /** Quanto tempo a Mão se deixa segurar: as quatrocentas batidas do original. */
    public int quantoSeSegura() {
        return Infusions.SEGURA;
    }

    /**
     * <b>Gasta carga.</b>
     *
     * <p>No criativo não gasta nada. E, se não houver bastante, o original faz uma coisa cruel: além de
     * recusar, ele <b>apaga a carga que sobrava</b> — quem tenta um poder caro com pouco fica com zero.
     */
    protected boolean gasta(ServerLevel level, ServerPlayer quem, int quanto) {
        if (quem.getAbilities().instabuild) return true;
        int tem = Infusions.energia(quem);
        if (tem - quanto < 0) {
            falha(level, quem);
            Infusions.apaga(quem);
            return false;
        }
        Infusions.põeEnergia(quem, tem - quanto);
        return true;
    }

    /** O tambor de quando não dá. */
    public static void falha(ServerLevel level, ServerPlayer quem) {
        toca(level, quem, SoundEvents.NOTE_BLOCK_SNARE.value());
    }

    /** Um som junto de quem o faz, com o tom sorteado do original. */
    public static void toca(ServerLevel level, ServerPlayer quem, net.minecraft.sounds.SoundEvent qual) {
        level.playSound(null, quem.getX(), quem.getY(), quem.getZ(), qual, SoundSource.PLAYERS,
                0.5f, 0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f));
    }
}
