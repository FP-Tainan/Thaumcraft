package net.thaumcraft.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.entity.GolemEntity;
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

/**
 * O serviço do golem: ele junta, ele colhe, e ele leva tudo para casa.
 *
 * <p>É um vaivém de três tempos, o mesmo do original. Com a mão vazia, ele procura serviço — coisa caída
 * no chão se o núcleo dele é o de juntar, plantação madura se é o de colher. Com a mão cheia, ou quando
 * não há mais serviço à vista, ele vai para a casa dele e despeja. Sem casa marcada, ele junta e fica
 * segurando, como o do original faz até alguém lhe dizer para onde levar.
 *
 * <p>Os outros dez núcleos do original ainda não têm serviço por aqui — eles chegam com as peças que
 * faltam. Isto está anotado em {@code docs/PORTE.md}.
 */
public class GolemWorkGoal extends Goal {
    /** Até onde ele enxerga serviço. */
    private static final double WORK_RANGE = 12.0;
    /** Até onde ele vai atrás da casa dele. */
    private static final double HOME_RANGE = 48.0;
    /** De quantos em quantos tiques ele olha em volta de novo. */
    private static final int LOOK_EVERY = 20;

    private final GolemEntity golem;
    @Nullable
    private ItemEntity loose;
    @Nullable
    private BlockPos crop;
    private int look;

    public GolemWorkGoal(GolemEntity golem) {
        this.golem = golem;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        String core = this.golem.core();
        return core != null && (core.equals("gather") || core.equals("harvest"));
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void stop() {
        this.loose = null;
        this.crop = null;
        this.golem.getNavigation().stop();
    }

    @Override
    public void tick() {
        Level level = this.golem.level();
        if (this.golem.room() <= 0) {
            this.goHome(level);
            return;
        }

        if (--this.look <= 0) {
            this.look = LOOK_EVERY;
            this.lookAround(level);
        }

        if (this.loose != null && this.loose.isAlive()) {
            this.walkTo(this.loose.blockPosition());
            if (this.golem.distanceToSqr(this.loose) < 2.0) this.pickUp(level);
            return;
        }
        if (this.crop != null) {
            this.walkTo(this.crop);
            if (this.golem.blockPosition().distSqr(this.crop) < 4.0) this.reap(level);
            return;
        }
        // sem serviço à vista, ele leva o que tem para casa
        if (!this.golem.carried().isEmpty()) this.goHome(level);
    }

    /** Procura serviço em volta, conforme o núcleo. */
    private void lookAround(Level level) {
        this.loose = null;
        this.crop = null;
        String core = this.golem.core();
        if (core == null) return;

        if (core.equals("gather")) {
            AABB box = this.golem.getBoundingBox().inflate(WORK_RANGE);
            List<ItemEntity> around = level.getEntitiesOfClass(ItemEntity.class, box,
                    found -> found.isAlive() && !found.getItem().isEmpty() && this.fits(found.getItem()));
            ItemEntity nearest = null;
            double best = Double.MAX_VALUE;
            for (ItemEntity found : around) {
                double away = this.golem.distanceToSqr(found);
                if (away >= best) continue;
                nearest = found;
                best = away;
            }
            this.loose = nearest;
            return;
        }

        if (core.equals("harvest")) {
            BlockPos from = this.golem.blockPosition();
            int reach = (int) WORK_RANGE;
            BlockPos best = null;
            double nearest = Double.MAX_VALUE;
            for (int x = -reach; x <= reach; x++) {
                for (int z = -reach; z <= reach; z++) {
                    for (int y = -3; y <= 3; y++) {
                        BlockPos at = from.offset(x, y, z);
                        if (!isRipe(level, at)) continue;
                        double away = at.distSqr(from);
                        if (away >= nearest) continue;
                        best = at;
                        nearest = away;
                    }
                }
            }
            this.crop = best;
        }
    }

    /** Aquilo cabe na mão dele? Uma mão só segura uma coisa de cada tipo. */
    private boolean fits(ItemStack stack) {
        ItemStack held = this.golem.carried();
        if (held.isEmpty()) return true;
        return ItemStack.isSameItemSameComponents(held, stack) && this.golem.room() > 0;
    }

    /** Aquela plantação está no ponto? */
    private static boolean isRipe(Level level, BlockPos at) {
        BlockState state = level.getBlockState(at);
        return state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state);
    }

    private void walkTo(BlockPos at) {
        this.golem.getLookControl().setLookAt(at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5);
        if (this.golem.getNavigation().isDone()) {
            this.golem.getNavigation().moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 1.0);
        }
    }

    /** Pega o que está no chão. */
    private void pickUp(Level level) {
        if (this.loose == null || !this.loose.isAlive()) return;
        ItemStack found = this.loose.getItem();
        int room = this.golem.room();
        if (room <= 0) return;

        int taken = Math.min(room, found.getCount());
        ItemStack held = this.golem.carried();
        if (held.isEmpty()) {
            this.golem.setCarried(found.copyWithCount(taken));
        } else {
            ItemStack grown = held.copy();
            grown.grow(taken);
            this.golem.setCarried(grown);
        }
        found.shrink(taken);
        if (found.isEmpty()) this.loose.discard();
        else this.loose.setItem(found);
        level.playSound(null, this.golem.blockPosition(), TCSounds.JAR.value(),
                SoundSource.NEUTRAL, 0.4f, 1.6f);
        this.loose = null;
    }

    /** Colhe a plantação e replanta, como o golem do original faz. */
    private void reap(Level level) {
        if (this.crop == null) return;
        BlockPos at = this.crop;
        this.crop = null;
        if (!isRipe(level, at)) return;
        if (!(level instanceof net.minecraft.server.level.ServerLevel server)) return;

        BlockState state = server.getBlockState(at);
        Block block = state.getBlock();
        List<ItemStack> drops = Block.getDrops(state, server, at, null);
        // qual e a semente daquela planta: e o que a planta recem-nascida larga
        List<ItemStack> seedDrops = Block.getDrops(block.defaultBlockState(), server, at, null);
        var seed = seedDrops.isEmpty() ? null : seedDrops.getFirst().getItem();

        // uma semente volta para a terra, e o resto vai para a mão
        boolean replanted = false;
        for (ItemStack drop : drops) {
            if (!replanted && seed != null && drop.is(seed)) {
                drop.shrink(1);
                replanted = true;
            }
            if (drop.isEmpty()) continue;
            if (this.fits(drop) && this.golem.room() > 0) {
                int taken = Math.min(this.golem.room(), drop.getCount());
                ItemStack held = this.golem.carried();
                if (held.isEmpty()) this.golem.setCarried(drop.copyWithCount(taken));
                else {
                    ItemStack grown = held.copy();
                    grown.grow(taken);
                    this.golem.setCarried(grown);
                }
                drop.shrink(taken);
            }
            if (!drop.isEmpty()) Block.popResource(server, at, drop);
        }
        server.setBlockAndUpdate(at, block.defaultBlockState());
        server.playSound(null, at, TCSounds.JAR.value(), SoundSource.BLOCKS, 0.5f, 1.2f);
    }

    /** Leva o que tem na mão para a casa dele. */
    private void goHome(Level level) {
        BlockPos home = this.golem.home();
        if (home == null) return;
        if (this.golem.blockPosition().distSqr(home) > HOME_RANGE * HOME_RANGE) {
            // longe demais: ele desiste daquela casa
            this.golem.setHome(null);
            return;
        }
        this.walkTo(home);
        if (this.golem.blockPosition().distSqr(home) > 4.0) return;

        if (!(level.getBlockEntity(home) instanceof Container chest)) {
            this.golem.setHome(null);
            return;
        }
        ItemStack held = this.golem.carried();
        if (held.isEmpty()) return;
        ItemStack left = put(chest, held);
        this.golem.setCarried(left);
        level.playSound(null, home, TCSounds.JAR.value(), SoundSource.BLOCKS, 0.5f, 0.9f);
    }

    /** Despeja o que dá no baú e devolve o que sobrou. */
    private static ItemStack put(Container chest, ItemStack stack) {
        ItemStack left = stack.copy();
        for (int slot = 0; slot < chest.getContainerSize() && !left.isEmpty(); slot++) {
            ItemStack there = chest.getItem(slot);
            if (there.isEmpty()) {
                chest.setItem(slot, left.copy());
                return ItemStack.EMPTY;
            }
            if (!ItemStack.isSameItemSameComponents(there, left)) continue;
            int room = Math.min(chest.getMaxStackSize(), there.getMaxStackSize()) - there.getCount();
            if (room <= 0) continue;
            int moved = Math.min(room, left.getCount());
            there.grow(moved);
            chest.setItem(slot, there);
            left.shrink(moved);
        }
        return left;
    }
}
