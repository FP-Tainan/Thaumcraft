package net.thaumcraft.occulta.broom;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.occulta.OccultaEntities;

/**
 * O que a vassoura faz fora dela mesma: pousar no chão, e não machucar quem desce dela.
 *
 * <p>No Witchery as duas coisas moram fora do {@code EntityBroom} — a de pôr no chão é um ramo do
 * {@code ItemGeneral.onItemUse}, e a da queda é o {@code EntityBroom.EventHooks.onLivingFall}, um ouvinte de
 * eventos. Aqui ficam juntas, pelo mesmo motivo: nenhuma das duas é da vassoura, as duas são <b>sobre</b> ela.
 */
public final class Brooms {
    private Brooms() {
    }

    public static void init() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((quem, fonte, quanto) -> {
            if (!fonte.is(net.minecraft.world.damagesource.DamageTypes.FALL)) return true;
            // quem chega de vassoura chega inteiro, por mais alto que tenha vindo
            return !(quem.getVehicle() instanceof BroomEntity);
        });
    }

    /**
     * Põe a vassoura no chão, de pé: o {@code placeBroom} do original.
     *
     * <p>Duas coisas dele que ficam: a vassoura sobe um bloco <b>por cima da neve</b>, em vez de ficar enterrada
     * nela; e o rumo dela é o de quem a pôs, <b>arredondado ao quarto de volta</b> — nunca de viés.
     */
    public static InteractionResult põe(UseOnContext uso) {
        Level level = uso.getLevel();
        Player quem = uso.getPlayer();
        if (quem == null) return InteractionResult.PASS;

        BlockPos onde = uso.getClickedPos();
        if (level.getBlockState(onde).is(Blocks.SNOW)) onde = onde.below();
        if (uso.getClickedFace() != Direction.DOWN) onde = onde.above();

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BroomEntity vassoura = OccultaEntities.BROOM.create(level,
                net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
        if (vassoura == null) return InteractionResult.PASS;

        float rumo = ((net.minecraft.util.Mth.floor(quem.getYRot() * 4.0f / 360.0f + 0.5) & 3) - 1) * 90;
        vassoura.snapTo(onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5, rumo, 0.0f);

        // onde não cabe, não se põe
        AABB caixa = vassoura.getBoundingBox().deflate(0.1);
        if (!level.noCollision(vassoura, caixa)) return InteractionResult.PASS;

        ItemStack naMão = uso.getItemInHand();
        if (naMão.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)) {
            vassoura.setCustomName(naMão.getHoverName());
        }
        level.addFreshEntity(vassoura);
        if (!quem.getAbilities().instabuild) naMão.shrink(1);
        return InteractionResult.SUCCESS;
    }
}
