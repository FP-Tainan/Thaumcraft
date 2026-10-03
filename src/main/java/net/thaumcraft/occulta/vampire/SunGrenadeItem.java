package net.thaumcraft.occulta.vampire;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * <b>A Granada Solar</b>: o {@code ItemSunGrenade} do Witchery, no modo zero.
 *
 * <p>Atira-se como uma bola de neve, e o que sai dela é {@linkplain SunGrenadeEntity um pedaço de dia</b>}.
 * Ela não se gasta: o que fica no chão devolve a <b>Esfera de Quartzo</b> ao fim do minuto, e a esfera volta
 * ao Coletor de Luz para se encher outra vez.
 *
 * <p>Um jogador que não seja vampiro acha-a um candeeiro portátil e nada mais. Um vampiro percebe depressa
 * que ela é a única coisa no jogo que lhe pode queimar a pele <b>em dose certa</b> — e, dez doses depois,
 * que era disso que ele precisava.
 */
public class SunGrenadeItem extends Item {
    public SunGrenadeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player quem, InteractionHand mão) {
        ItemStack oquê = quem.getItemInHand(mão);
        level.playSound(null, quem.getX(), quem.getY(), quem.getZ(), SoundEvents.SNOWBALL_THROW,
                SoundSource.NEUTRAL, 0.5f, 0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f));

        if (level instanceof net.minecraft.server.level.ServerLevel) {
            SunGrenadeEntity granada = new SunGrenadeEntity(level, quem, oquê);
            /*
             * O arremesso é o do original: um pouco mais devagar do que uma bola de neve, e <b>apontado vinte
             * graus acima</b> do olhar. Ela não é uma pedrada — é uma coisa que se <b>larga</b> adiante.
             */
            granada.shootFromRotation(quem, quem.getXRot(), quem.getYRot(), -20.0f, 0.75f, 1.0f);
            level.addFreshEntity(granada);
        }

        quem.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));
        oquê.consume(1, quem);
        return InteractionResult.SUCCESS;
    }
}
