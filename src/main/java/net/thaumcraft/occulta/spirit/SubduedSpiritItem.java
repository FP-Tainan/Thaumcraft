package net.thaumcraft.occulta.spirit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SpellParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.thaumcraft.occulta.OccultaEntities;

/**
 * O <b>Espírito Dominado</b> e o <b>da Aldeia</b>: o {@code useSubduedSpirit} do {@code ItemGeneral}.
 *
 * <p>Usado num bloco, ele <b>solta o espírito que tem dentro</b> — ali mesmo, no bloco em que se clicou, e
 * não num anel à volta como quase tudo o mais do mod faz: o original chama o mesmo método de nascer com
 * <b>zero</b> de perto e de longe, e zero com zero é o próprio lugar.
 *
 * <p>O comum solta um espírito que <b>fica</b>. O da Aldeia solta um que tem <b>dez segundos</b>, vai na
 * direção da aldeia mais perto e, ao acabar, <b>devolve o item</b> — de modo que ele não se gasta, se quem
 * o soltou for atrás.
 *
 * @see SpiritEntity o prazo e o rumo
 */
public class SubduedSpiritItem extends Item {
    private final int feitio;

    public SubduedSpiritItem(Properties propriedades, int feitio) {
        super(propriedades);
        this.feitio = feitio;
    }

    /** Qual dos dois ele é. */
    public int feitio() {
        return this.feitio;
    }

    @Override
    public InteractionResult useOn(UseOnContext onde) {
        if (!(onde.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;

        BlockPos ali = onde.getClickedPos();
        var bicho = OccultaEntities.SPIRIT.create(level, EntitySpawnReason.TRIGGERED);
        if (bicho == null) return InteractionResult.PASS;

        bicho.snapTo(ali.getX() + 0.5, ali.getY() + 1.05, ali.getZ() + 0.5, 0.0f, 0.0f);
        bicho.setPersistenceRequired();
        level.addFreshEntity(bicho);
        if (this.feitio == SpiritEntity.DA_ALDEIA) bicho.vaiParaAAldeia(level, SpiritEntity.DA_ALDEIA);

        level.sendParticles(SpellParticleOption.create(ParticleTypes.INSTANT_EFFECT, 1.0f, 1.0f, 1.0f, 1.0f),
                bicho.getX(), bicho.getY() + bicho.getBbHeight() / 2.0, bicho.getZ(),
                16, 1.0, bicho.getBbHeight(), 1.0, 0.0);

        var quem = onde.getPlayer();
        if (quem == null || !quem.hasInfiniteMaterials()) onde.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }
}
