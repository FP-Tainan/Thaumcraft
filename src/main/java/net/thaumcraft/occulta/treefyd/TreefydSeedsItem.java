package net.thaumcraft.occulta.treefyd;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaEntities;

/**
 * A <b>semente de Treefyd</b>: o {@code itemSeedsTreefyd} do Witchery.
 *
 * <p>Planta-se na <b>face de cima</b> de um bloco onde cresça <b>erva alta</b>, e o que nasce não é uma
 * planta: é o {@link TreefydEntity}, com quem a plantou por dono. A erva alta fica no lugar, por cima do
 * chão, e é o que faz o Treefyd parecer que <b>brotou</b>.
 *
 * <p>Não se planta em lugar coberto: tem de haver ar por cima.
 *
 * <p>É também a coisa mais rara que um bicho vende — três em cem em qualquer loja do Amuleto da Polinésia,
 * e <b>dez</b> num creeper. É o jeito de o original a dar a quem não a procura.
 */
public class TreefydSeedsItem extends Item {
    public TreefydSeedsItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext onde) {
        if (onde.getClickedFace() != Direction.UP) return InteractionResult.PASS;
        if (!(onde.getLevel() instanceof ServerLevel mundo)) return InteractionResult.SUCCESS;

        Player quem = onde.getPlayer();
        if (quem == null) return InteractionResult.PASS;

        BlockPos acima = onde.getClickedPos().above();
        if (!Blocks.SHORT_GRASS.defaultBlockState().canSurvive(mundo, acima)) {
            return InteractionResult.PASS;
        }
        if (!mundo.getBlockState(acima).canBeReplaced()) return InteractionResult.PASS;

        mundo.setBlock(acima, Blocks.SHORT_GRASS.defaultBlockState(), Block.UPDATE_ALL);

        TreefydEntity nasceu = OccultaEntities.TREEFYD.create(mundo, EntitySpawnReason.TRIGGERED);
        if (nasceu == null) return InteractionResult.PASS;
        nasceu.snapTo(acima.getX() + 0.5, acima.getY(), acima.getZ() + 0.5, 0.0f, 0.0f);
        nasceu.dono(quem.getUUID());
        nasceu.setPersistenceRequired();
        mundo.addFreshEntity(nasceu);

        mundo.sendParticles(ParticleTypes.ITEM_SLIME, nasceu.getX(), nasceu.getY() + 1.0,
                nasceu.getZ(), 16, 0.5, 1.0, 0.5, 0.0);
        mundo.sendParticles(ParticleTypes.EXPLOSION, nasceu.getX(), nasceu.getY() + 1.0,
                nasceu.getZ(), 4, 0.5, 1.0, 0.5, 0.0);
        mundo.playSound(null, acima, SoundEvents.SILVERFISH_DEATH, SoundSource.BLOCKS, 1.0f, 1.0f);

        if (!quem.hasInfiniteMaterials()) onde.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }
}
