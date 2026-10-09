package net.thaumcraft.occulta.spirit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.thaumcraft.fluid.ThaumFluid;
import net.thaumcraft.occulta.OccultaItems;

/**
 * O bloco de um dos dois líquidos do outro lado: a parte do {@code BlockFlowingSpirit} que mexe em quem lhe cai
 * dentro.
 *
 * <p>Ele guarda <b>dois efeitos</b>, e escolhe entre eles pelo que a coisa é: o bom para a gente comum, o mau
 * para o morto-vivo, para a coisa do inferno e — no Espírito Fluente — para o Pesadelo. Nenhum dos dois se dá a
 * quem já o tem.
 */
public class SpiritLiquidBlock extends ThaumFluid.LiquidBlock {
    /** O que ele dá a quem é gente. */
    private final Holder<MobEffect> good;
    private final int goodTime;
    /** E o que dá ao que não é. */
    private final Holder<MobEffect> bad;
    private final int badTime;
    /** O grau dos dois: o {@code 1} do original, que é o segundo nível. */
    private static final int LEVEL = 1;

    /** Se ele é o Espírito Fluente: o que faz mal ao Pesadelo e desfaz o Algodão Perturbado. */
    private final boolean nightmareBane;

    public SpiritLiquidBlock(FlowingFluid fluid, Properties properties, Holder<MobEffect> good, int goodTime,
                             Holder<MobEffect> bad, int badTime, boolean nightmareBane) {
        super(fluid, properties);
        this.good = good;
        this.goodTime = goodTime;
        this.bad = bad;
        this.badTime = badTime;
        this.nightmareBane = nightmareBane;
    }

    /** O Espírito Fluente: cura a gente por cinco segundos, e enfraquece o resto por quinze. */
    public static SpiritLiquidBlock spirit(Properties properties) {
        return new SpiritLiquidBlock(SpiritFluids.FLOWING_SPIRIT, properties,
                MobEffects.REGENERATION, 100, MobEffects.WEAKNESS, 300, true);
    }

    /** E as Lágrimas Ocas, que fazem o contrário, por cinco segundos cada. */
    public static SpiritLiquidBlock tears(Properties properties) {
        return new SpiritLiquidBlock(SpiritFluids.HOLLOW_TEARS, properties,
                MobEffects.WEAKNESS, 100, MobEffects.REGENERATION, 100, false);
    }

    /**
     * Se aquela coisa é do lado que o líquido castiga.
     *
     * <p>O {@code CreatureUtil.isDemonic} do original conta, além dos quatro do jogo, os bichos do próprio
     * Witchery — o Demônio, o Leonard, o Senhor do Tormento, o Diabrete e a Lilith. <b>Oito dos nove estão
     * portados</b>, e a lista toda mora agora no rótulo {@code thaumcraft:demonic}; falta o Leonard, que é
     * o único que ainda não existe.
     */
    private static boolean wrongSide(LivingEntity quem, boolean nightmareBane) {
        if (quem.isInvertedHealAndHarm()) return true;
        if (net.thaumcraft.occulta.torment.Demonic.é(quem)) return true;
        return nightmareBane && quem instanceof NightmareEntity;
    }

    /**
     * O {@code onBlockAdded} do original: posto <b>no Mundo dos Espíritos</b>, <b>em fonte</b> e <b>sobre uma
     * camada de neve</b>, ele tenta acender um Portal do Espírito.
     *
     * <p>Só o Espírito Fluente faz isso — as Lágrimas Ocas não —, que é o {@code igniteSpiritPortals} do
     * original.
     */
    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState antes, boolean moveu) {
        super.onPlace(state, level, pos, antes, moveu);
        if (!this.nightmareBane || level.isClientSide()) return;
        if (!(level instanceof net.minecraft.server.level.ServerLevel server)) return;
        if (!SpiritWorld.is(server)) return;
        if (!state.getFluidState().isSource()) return;
        if (!level.getBlockState(pos.below()).is(SpiritPortalBlock.FRAME)) return;
        SpiritPortalBlock.tryToCreate(level, pos);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity quem,
                                InsideBlockEffectApplier efeitos, boolean atravessou) {
        super.entityInside(state, level, pos, quem, efeitos, atravessou);
        if (level.isClientSide()) return;

        if (quem instanceof LivingEntity vivo) {
            boolean mau = wrongSide(vivo, this.nightmareBane);
            Holder<MobEffect> qual = mau ? this.bad : this.good;
            if (!vivo.hasEffect(qual)) {
                vivo.addEffect(new MobEffectInstance(qual, mau ? this.badTime : this.goodTime, LEVEL));
            }
            return;
        }

        // o algodão que caiu no espírito perde o pesadelo que trazia
        if (this.nightmareBane && quem instanceof ItemEntity largado) {
            ItemStack coisa = largado.getItem();
            if (coisa.is(OccultaItems.DISTURBED_COTTON)) {
                largado.setItem(new ItemStack(OccultaItems.WISPY_COTTON, coisa.getCount()));
            }
        }
    }
}
