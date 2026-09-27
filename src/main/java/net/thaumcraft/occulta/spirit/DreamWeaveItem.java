package net.thaumcraft.occulta.spirit;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.occulta.OccultaBlocks;

/**
 * Uma Teia de Sonho: o {@code ItemGeneral.DreamWeave} do Witchery.
 *
 * <p>Ela não se usa na mão: <b>ela é o apanhador</b>. Pregada a uma parede, vira um Apanhador de Sonhos com
 * aquele feitio dentro — é o que o {@code placeDreamCatcher} do original faz, e por isso o apanhador não tem
 * item próprio: não existe apanhador vazio. Cada teia tem <b>duas caras</b>: a do sonho bom e a do pesadelo
 * — e é a mesma teia que dá as duas, conforme a noite corra bem ou mal.
 */
public class DreamWeaveItem extends Item {
    /**
     * As cinco teias do original, com os dois efeitos e os números de cada uma.
     *
     * @param dream     o que ela dá em sonho bom
     * @param nightmare e o que ela dá em pesadelo
     * @param duration  quanto tempo
     * @param amplifier e em que grau
     */
    public enum Weave {
        /** Andar depressa — ou de rastros. */
        MOVE(MobEffects.SPEED, MobEffects.SLOWNESS, 7200, 0),
        /** Cavar depressa — ou nem isso. */
        DIG(MobEffects.HASTE, MobEffects.MINING_FATIGUE, 7200, 0),
        /** Acordar farto — ou com fome. */
        EAT(MobEffects.SATURATION, MobEffects.HUNGER, 4800, 0),
        /** A que <b>apanha</b> os pesadelos: deixa fraco, e às cegas se falhar. */
        NIGHTMARE(MobEffects.WEAKNESS, MobEffects.BLINDNESS, 1200, 0),
        /** E a que aperta o sonho: enxerga-se no escuro, ou não se enxerga nada. */
        INTENSITY(MobEffects.NIGHT_VISION, MobEffects.BLINDNESS, 300, 0);

        public final Holder<MobEffect> dream;
        public final Holder<MobEffect> nightmare;
        public final int duration;
        public final int amplifier;

        Weave(Holder<MobEffect> dream, Holder<MobEffect> nightmare, int duration, int amplifier) {
            this.dream = dream;
            this.nightmare = nightmare;
            this.duration = duration;
            this.amplifier = amplifier;
        }

        /**
         * O que ela dá àquela pessoa: o {@code applyEffect} do original.
         *
         * <p>Apertada por uma teia de <b>intensidade</b> por perto, o sonho bom sobe de grau — menos o da fartura,
         * que em vez de subir de grau dura <b>dois minutos a mais</b>, porque grau de fartura não quer dizer nada.
         * O tempo dos outros, apertado, encolhe: é a troca que o original faz.
         */
        public void apply(Player quem, boolean bom, boolean apertado) {
            if (!bom) {
                quem.addEffect(new MobEffectInstance(this.nightmare, this.duration,
                        apertado ? this.amplifier + 1 : this.amplifier));
                return;
            }
            boolean fartura = this.dream == MobEffects.SATURATION;
            int tempo = apertado ? (fartura ? this.duration + 2400 : this.duration - 2400) : this.duration;
            int grau = apertado && !fartura ? this.amplifier + 1 : this.amplifier;
            quem.addEffect(new MobEffectInstance(this.dream, Math.max(tempo, 20), grau));
        }
    }

    private final Weave weave;

    public DreamWeaveItem(Weave weave, Properties properties) {
        super(properties);
        this.weave = weave;
    }

    public Weave weave() {
        return this.weave;
    }

    /** Que teia aquela coisa é, ou nada. */
    public static Weave of(ItemStack coisa) {
        return coisa.getItem() instanceof DreamWeaveItem teia ? teia.weave() : null;
    }

    /**
     * Pregar a teia na parede: o {@code placeDreamCatcher} do original.
     *
     * <p>Só <b>de lado</b>, e só em face firme — o original recusa o de baixo de cara e, no de cima, mede o
     * lugar mas nunca chega a pousar nada, o que dá no mesmo que recusar. Aqui a recusa é dos dois, dita de uma
     * vez.
     */
    @Override
    public InteractionResult useOn(UseOnContext onde) {
        Direction lado = onde.getClickedFace();
        if (lado.getAxis().isVertical()) return InteractionResult.PASS;

        Level level = onde.getLevel();
        BlockPos parede = onde.getClickedPos();
        BlockPos casa = parede.relative(lado);
        if (!level.getBlockState(parede).isFaceSturdy(level, parede, lado)) return InteractionResult.PASS;
        if (!level.getBlockState(casa).canBeReplaced()) return InteractionResult.PASS;

        BlockState feitio = OccultaBlocks.DREAM_CATCHER.defaultBlockState()
                .setValue(DreamCatcherBlock.FACING, lado);
        if (!feitio.canSurvive(level, casa)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        level.setBlock(casa, feitio, 3);
        if (level.getBlockEntity(casa) instanceof DreamCatcherBlockEntity alma) alma.setWeave(this.weave);
        level.playSound(null, casa, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
        if (onde.getPlayer() == null || !onde.getPlayer().hasInfiniteMaterials()) {
            onde.getItemInHand().shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
