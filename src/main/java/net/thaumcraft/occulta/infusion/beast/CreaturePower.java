package net.thaumcraft.occulta.infusion.beast;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Um <b>poder de bicho</b>: a {@code CreaturePower} do Witchery.
 *
 * <p>É o que a {@linkplain net.thaumcraft.occulta.infusion.InfernalInfusion Infusão Infernal} faz de mais
 * estranho: ela não lhe dá poderes, ela o deixa <b>tirá-los de quem os tem</b>. Toma-se um bicho para si,
 * leva-se para onde se quiser, e então <b>mata-se</b> — e o que ele sabia fazer passa a ser seu.
 *
 * <p>Um de cada vez. Tomar o poder de outra espécie <b>apaga</b> o que se tinha, e o que o bicho dá são
 * <b>dez cargas</b> — um, se for bicho de capoeira — até um teto de <b>vinte</b>. Quem quiser bolas de fogo
 * de ghast tem de ir matando ghasts.
 *
 * <h2>Os quatro ganchos</h2>
 *
 * <ul>
 *   <li><b>usar</b>: largar a Mão sem agachar. É o poder propriamente dito;</li>
 *   <li><b>batida</b>: corre do lado do <b>cliente</b>, a cada batida, e é onde moram os poderes que mexem
 *       no <b>andar</b> — trepar paredes, voar, nadar, correr. O original faz isso assim porque mexer na
 *       velocidade de quem joga só fica macio se for do lado dele;</li>
 *   <li><b>golpe levado</b>: o creeper engole raios, o homem-porco aguenta fogo, a lula respira na água;</li>
 *   <li><b>queda</b>: o morcego cai de cinco, o slime não cai de lado nenhum.</li>
 * </ul>
 */
public class CreaturePower {
    /** Quantas cargas um bicho dá, se ele não disser outra coisa. */
    public static final int POR_BICHO = 10;

    /** O número dele, que é o que fica guardado no jogador. */
    public final int id;

    /** E de que bicho ele se tira. */
    public final EntityType<?> dequê;

    public CreaturePower(int id, EntityType<?> dequê) {
        this.id = id;
        this.dequê = dequê;
    }

    /** Quanto ele custa de carga de bicho. */
    public int custo(int segurou) {
        return 1;
    }

    /** Quantas cargas aquele bicho dá ao ser sacrificado. */
    public int porBicho() {
        return POR_BICHO;
    }

    /** <b>O poder.</b> */
    public void usa(ServerLevel level, ServerPlayer quem, int segurou, @Nullable HitResult onde) {
    }

    /** A batida do lado de cá, onde moram os poderes de andar. */
    public void batida(Player quem) {
    }

    /** O golpe levado. Devolve se o golpe foi <b>engolido</b>. */
    public boolean levouGolpe(ServerLevel level, ServerPlayer quem, DamageSource fonte) {
        return false;
    }

    /**
     * A queda: devolve de que altura ela conta, a partir da que ela tinha.
     *
     * <p>Devolver zero é não cair.
     */
    public float cai(ServerPlayer quem, float distância) {
        return distância;
    }
}
