package net.thaumcraft.occulta.demon;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * O <b>Coração de Demônio</b> na mão: o {@code itemDemonHeart} do Witchery.
 *
 * <p>Duas coisas se fazem com ele, e as duas estão no mesmo botão. <b>Agachado</b>, ele se <b>põe no chão</b>
 * e passa a {@linkplain DemonHeartBlock bater}. De pé, ele se <b>come</b>.
 *
 * <h2>E comê-lo é um negócio de demônio</h2>
 *
 * <p>Por <b>dois minutos</b>: Vida Extra V, Regeneração II, Força III, Rapidez III e Resistência ao Fogo III.
 * É mais poder do que qualquer outra coisa deste mod dá de uma vez.
 *
 * <p>Junto com ele vêm <b>Cegueira</b> pelos mesmos dois minutos e <b>Fome II</b> por três.
 *
 * <p>E então o detalhe que faz disto a melhor piada do Witchery: comê-lo <b>põe fogo em quem come</b>, por
 * <b>dois minutos e doze segundos</b> — e a Resistência ao Fogo dura <b>dois minutos</b>. <b>O fogo passa da
 * proteção por doze segundos</b>, e é nesses doze segundos, cego, que o negócio se cobra.
 */
public class DemonHeartItem extends BlockItem {
    /** O que ele dá, e por quanto tempo: dois minutos. */
    public static final int QUANTO = 2400;

    /** A fome, que dura mais: três minutos. */
    public static final int A_FOME = 3600;

    /** E o fogo, que dura <b>doze segundos mais</b> do que a proteção contra ele. */
    public static final int O_FOGO = 2640;

    public DemonHeartItem(Properties properties) {
        super(net.thaumcraft.occulta.OccultaBlocks.DEMON_HEART, properties);
    }

    /** No ar, ele se come. */
    @Override
    public InteractionResult use(Level level, Player quem, InteractionHand mão) {
        return come(level, quem, quem.getItemInHand(mão));
    }

    /** Num bloco, ele se come — a não ser que quem o leva esteja <b>agachado</b>, e então ele se põe. */
    @Override
    public InteractionResult useOn(UseOnContext onde) {
        Player quem = onde.getPlayer();
        if (quem == null) return InteractionResult.PASS;
        if (quem.isShiftKeyDown()) return super.useOn(onde);
        return come(onde.getLevel(), quem, onde.getItemInHand());
    }

    /**
     * O negócio, inteiro.
     *
     * <p>A ordem importa e é a do original: os efeitos primeiro, o fogo depois. Com isso a Resistência ao
     * Fogo já está em vigor quando o fogo pega — e é por isso que os primeiros dois minutos não doem.
     */
    private static InteractionResult come(Level level, Player quem, ItemStack coração) {
        if (!(level instanceof net.minecraft.server.level.ServerLevel)) {
            return InteractionResult.SUCCESS;
        }

        quem.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, QUANTO, 4));
        quem.addEffect(new MobEffectInstance(MobEffects.REGENERATION, QUANTO, 1));
        quem.addEffect(new MobEffectInstance(MobEffects.STRENGTH, QUANTO, 2));
        quem.addEffect(new MobEffectInstance(MobEffects.SPEED, QUANTO, 2));
        quem.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, QUANTO, 2));
        quem.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, QUANTO));
        quem.addEffect(new MobEffectInstance(MobEffects.HUNGER, A_FOME, 1));
        quem.igniteForTicks(O_FOGO);

        level.playSound(null, quem.getX(), quem.getY(), quem.getZ(), SoundEvents.PLAYER_BURP,
                SoundSource.PLAYERS, 0.5f, level.getRandom().nextFloat() * 0.1f + 0.9f);
        coração.consume(1, quem);
        return InteractionResult.SUCCESS;
    }

}
