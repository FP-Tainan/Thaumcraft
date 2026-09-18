package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.block.LevitatorBlock;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCBlocks;

/**
 * O levitador arcano: o {@code TileLifter} da 4.2.3.5.
 *
 * <p>Empurra para cima o que está sobre ele — itens, o que pode ser empurrado e cavalos — até dez blocos, mais
 * dez por levitador ligado empilhado embaixo, parando no primeiro bloco cheio. Quem sobe não se machuca ao cair;
 * agachado, o jogador desce devagar. Com sinal de redstone nele ou no bloco de cima, desliga.
 *
 * <p>Roda dos dois lados, como no original: o servidor move os itens e os bichos, e quem joga move a si mesmo.
 */
public class LevitatorBlockEntity extends BlockEntity {
    /** O agachar de quem joga, que só o lado de quem vê conhece; o cliente pendura aqui a consulta. */
    public static java.util.function.Predicate<Player> sneaking = player -> false;

    private int counter;
    public int rangeAbove;
    public boolean requiresUpdate = true;

    public LevitatorBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.LEVITATOR, pos, state);
    }

    public static boolean gettingPower(Level level, BlockPos pos) {
        return level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.above());
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LevitatorBlockEntity lifter) {
        lifter.counter++;
        if (lifter.requiresUpdate || lifter.counter % 100 == 0) {
            lifter.requiresUpdate = false;
            // o sinal no bloco de cima não avisa o levitador; a conta de tempos em tempos o apanha
            boolean powered = gettingPower(level, pos);
            if (!level.isClientSide() && powered != state.getValue(LevitatorBlock.POWERED)) {
                level.setBlock(pos, state.setValue(LevitatorBlock.POWERED, powered), net.minecraft.world.level.block.Block.UPDATE_ALL);
                return;
            }
            int max = 10;
            for (int count = 1; level.getBlockState(pos.below(count)).is(TCBlocks.LEVITATOR)
                    && !level.hasNeighborSignal(pos.below(count)); max += 10) {
                count++;
            }
            lifter.rangeAbove = 0;
            while (lifter.rangeAbove < max && !level.getBlockState(pos.above(1 + lifter.rangeAbove)).isSolidRender()) {
                lifter.rangeAbove++;
            }
        }
        if (lifter.rangeAbove <= 0 || state.getValue(LevitatorBlock.POWERED)) return;
        AABB area = new AABB(pos.getX(), pos.getY() + 1, pos.getZ(), pos.getX() + 1, pos.getY() + 1 + lifter.rangeAbove, pos.getZ() + 1);
        for (Entity entity : level.getEntitiesOfClass(Entity.class, area)) {
            if (!(entity instanceof ItemEntity || entity.isPushable() || entity instanceof AbstractHorse)) continue;
            // cada lado move o que é dele: o servidor, tudo menos os jogadores; quem joga, a si mesmo
            if (entity instanceof Player player) {
                if (!level.isClientSide() || !player.isLocalPlayer()) continue;
            } else if (level.isClientSide()) {
                continue;
            }
            Vec3 motion = entity.getDeltaMovement();
            if (entity instanceof Player player && sneaking.test(player)) {
                if (motion.y < 0.0) entity.setDeltaMovement(motion.x, motion.y * 0.9f, motion.z);
            } else if (motion.y < 0.35f) {
                entity.setDeltaMovement(motion.x, motion.y + 0.1f, motion.z);
            }
            entity.resetFallDistance();
        }
    }
}
