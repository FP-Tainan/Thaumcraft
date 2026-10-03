package net.thaumcraft.occulta.vampire;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * <b>As Páginas Rasgadas</b>: o ramo do {@code LivingDropsEvent} que enche o Livro do Vampiro.
 *
 * <p>Elas não estão no mundo à espera. Elas <b>só caem de quem morre pela mão de alguém que já traz um livro
 * incompleto</b> — e é a única coisa do mod que funciona assim. Quem nunca achou o primeiro exemplar numa
 * livraria de aldeia nunca verá uma página cair, por mais que mate.
 *
 * <p>E as chances dizem onde o erudito andou a perder o caderno:
 *
 * <table border="1">
 *   <caption>De quem caem</caption>
 *   <tr><th>de quem</th><th>quantas vezes</th></tr>
 *   <tr><td>um <b>chefe</b></td><td><b>sempre</b></td></tr>
 *   <tr><td>um <b>aldeão</b></td><td>uma em dez</td></tr>
 *   <tr><td>um <b>zumbi-porco</b> ou um <b>enderman</b></td><td>nove em cem</td></tr>
 *   <tr><td>qualquer <b>morto-vivo</b></td><td>duas em cem</td></tr>
 * </table>
 *
 * <p>Olhe a lista com cuidado e ela conta uma história: as páginas estão com os <b>aldeões</b>, com os
 * <b>mortos</b>, e com as coisas que <b>andam entre mundos</b>. Quem quiser o livro inteiro tem de fazer
 * exatamente o que o vampiro do diário fez — passar pelas aldeias, pelo Nether e pelo Fim.
 *
 * <p>Nove páginas a uma em dez, e o livro inteiro é o trabalho de uma vida.
 */
public final class TornPage {
    /** A chance de um aldeão, a de um zumbi-porco ou enderman, e a de um morto-vivo qualquer. */
    public static final double DE_ALDEÃO = 0.1;
    public static final double DE_ENTRE_MUNDOS = 0.09;
    public static final double DE_MORTO_VIVO = 0.02;

    private TornPage() {
    }

    /**
     * O que cai de quem acabou de morrer, se quem o matou trouxer um livro por acabar.
     *
     * <p>O original olha o inventário inteiro e aceita <b>qualquer</b> exemplar incompleto; não é preciso
     * tê-lo na mão. Um vampiro que ande com o livro na mochila vai juntando páginas sem reparar — e é assim
     * que a maior parte das pessoas acha a segunda.
     */
    public static void doMorto(ServerLevel level, Player quem, LivingEntity quemMorreu) {
        if (!VampireBookItem.traz(quem)) return;
        if (!cai(level, quemMorreu)) return;

        var página = new net.minecraft.world.entity.item.ItemEntity(level,
                quemMorreu.getX(), quemMorreu.getY() + 1.0, quemMorreu.getZ(),
                new ItemStack(net.thaumcraft.occulta.OccultaItems.TORN_PAGE));
        página.setDefaultPickUpDelay();
        level.addFreshEntity(página);
    }

    /** Se deste cai uma página. */
    public static boolean cai(ServerLevel level, LivingEntity quem) {
        var sorte = level.getRandom();
        if (quem instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon
                || quem instanceof net.minecraft.world.entity.boss.wither.WitherBoss) {
            return true;
        }
        if (quem instanceof net.minecraft.world.entity.monster.zombie.ZombifiedPiglin
                || quem instanceof net.minecraft.world.entity.monster.EnderMan) {
            return sorte.nextDouble() < DE_ENTRE_MUNDOS;
        }
        if (quem instanceof net.minecraft.world.entity.npc.villager.Villager
                || quem instanceof net.thaumcraft.occulta.village.VillageGuardEntity) {
            return sorte.nextDouble() < DE_ALDEÃO;
        }
        if (quem.getType().builtInRegistryHolder().is(EntityTypeTags.UNDEAD)) {
            return sorte.nextDouble() < DE_MORTO_VIVO;
        }
        return false;
    }
}
