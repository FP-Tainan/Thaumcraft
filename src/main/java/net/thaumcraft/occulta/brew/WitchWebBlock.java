package net.thaumcraft.occulta.brew;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * A <b>teia de bruxa</b>: o {@code BlockWitchWeb} do Witchery.
 *
 * <p>Ela é a teia de aranha do jogo com <b>duas diferenças</b>, e as duas contam: é <b>oito vezes mais dura
 * de rasgar</b>, e <b>não larga nada</b> — nem linha, nem a si mesma. Quem cai numa não sai dela com nada nas
 * mãos a não ser o tempo que perdeu.
 *
 * <p>No original ela usa a <b>própria textura</b> da teia do jogo e o mesmo material, e o que ela faz a quem
 * entra é o mesmo {@code setInWeb} que a teia do jogo faz. Por isso aqui ela é, de propósito, a teia de hoje:
 * o que a teia do jogo passou a fazer com o <b>tecelagem</b>, esta faz também, porque em 2014 ela era a teia
 * do jogo e mais nada.
 *
 * <p>Ela sai do cozimento de <b>teias</b>, e de mais lado nenhum: não se fabrica e não se apanha.
 */
public class WitchWebBlock extends Block {
    public static final MapCodec<WitchWebBlock> CODEC = simpleCodec(WitchWebBlock::new);

    /** O que ela faz à velocidade de quem entra: o mesmo da teia do jogo. */
    private static final Vec3 PRENDE = new Vec3(0.25, 0.05, 0.25);

    /** E o que faz a quem traz o tecelagem, que é soltar um pouco mais. */
    private static final Vec3 PRENDE_TECENDO = new Vec3(0.5, 0.25, 0.5);

    public WitchWebBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    /** Não se esbarra nela — anda-se dentro dela, devagar. */
    @Override
    protected void entityInside(BlockState feitio, Level level, BlockPos onde, Entity quem,
                                InsideBlockEffectApplier applier, boolean dentroMesmo) {
        Vec3 quanto = quem instanceof LivingEntity vivo && vivo.hasEffect(MobEffects.WEAVING)
                ? PRENDE_TECENDO : PRENDE;
        quem.makeStuckInBlock(feitio, quanto);
    }
}
