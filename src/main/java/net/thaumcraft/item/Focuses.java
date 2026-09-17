package net.thaumcraft.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCItems;

/**
 * O que cada foco faz quando a varinha aponta.
 *
 * <p>Por ora são dois. O de fogo é um jato de chamas contínuo: enquanto o botão está apertado, a varinha
 * cobra dez centésimos de ignis por tique e cospe fogo pela frente, incendiando o que alcança. O de
 * escavação quebra o bloco na mira a doze blocos de distância, cobrando quinze centésimos de terra por
 * bloco. Os dois custos são os do original.
 */
public final class Focuses {
    /** Até onde o jato de fogo chega. */
    private static final double FIRE_REACH = 6.0;
    /** Até onde a escavação alcança, bem além do braço. */
    private static final double DIG_REACH = 12.0;

    private Focuses() {
    }

    /** O item de foco daquele tipo, para devolver à mão de quem trocou. */
    public static Item byType(String type) {
        return TCItems.FOCI.get(type);
    }

    /** O foco preso nesta varinha, se houver. */
    public static FocusItem on(ItemStack wand) {
        String type = wand.get(TCComponents.WAND_FOCUS);
        if (type == null) return null;
        Item found = byType(type);
        return found instanceof FocusItem focus ? focus : null;
    }

    /**
     * Um tique de foco em uso.
     *
     * @return se a varinha deu conta de pagar e o foco agiu
     */
    public static boolean tick(Level level, Player player, ItemStack wand, FocusItem focus) {
        if (!WandItem.consumeRaw(wand, focus.cost(), true)) return false;
        if (focus.type().equals("fire")) {
            breatheFire(level, player);
            return true;
        }
        if (focus.type().equals("excavation")) {
            return excavate(level, player);
        }
        return false;
    }

    /**
     * A escavação: quebra o bloco na mira, mais longe do que o braço alcança.
     *
     * <p>No original o foco vai roendo o bloco como se fosse uma picareta, e quebra quando termina; aqui
     * ele quebra de uma vez, a cada meio segundo, cobrando o mesmo que o original cobra por bloco.
     */
    private static boolean excavate(Level level, Player player) {
        if (level.getGameTime() % 10 != 0) return true;
        var hit = player.pick(DIG_REACH, 1.0f, false);
        if (!(hit instanceof net.minecraft.world.phys.BlockHitResult block)) return true;
        var pos = block.getBlockPos();
        var state = level.getBlockState(pos);
        if (state.isAir()) return true;
        // o que não se quebra na mão também não se quebra daqui
        if (state.getDestroySpeed(level, pos) < 0.0f) return true;

        if (level instanceof ServerLevel server) {
            server.destroyBlock(pos, true, player);
            server.sendParticles(ParticleTypes.ENCHANT,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 12, 0.4, 0.4, 0.4, 0.4);
        }
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.BLOCKS, 0.5f, 1.4f);
        return true;
    }

    /** O jato de chamas: o que estiver na frente pega fogo. */
    private static void breatheFire(Level level, Player player) {
        Vec3 eyes = player.getEyePosition();
        Vec3 aim = player.getViewVector(1.0f);
        for (int step = 1; step <= FIRE_REACH; step++) {
            Vec3 at = eyes.add(aim.scale(step));
            if (level instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.FLAME, at.x, at.y, at.z, 3, 0.15, 0.15, 0.15, 0.02);
                server.sendParticles(ParticleTypes.SMALL_FLAME, at.x, at.y, at.z, 2, 0.2, 0.2, 0.2, 0.01);
            }
            if (!level.getBlockState(net.minecraft.core.BlockPos.containing(at)).isAir()) break;
        }

        AABB box = new AABB(eyes, eyes.add(aim.scale(FIRE_REACH))).inflate(1.0);
        for (Entity target : level.getEntitiesOfClass(Entity.class, box, entity -> entity != player)) {
            // só o que estiver mesmo na frente, e não tudo dentro da caixa
            Vec3 toTarget = target.position().add(0.0, target.getBbHeight() / 2.0, 0.0).subtract(eyes);
            if (toTarget.length() > FIRE_REACH) continue;
            if (toTarget.normalize().dot(aim) < 0.94) continue;
            target.igniteForSeconds(4.0f);
            target.hurt(level.damageSources().onFire(), 1.0f);
        }
        if (level.getGameTime() % 4 == 0) {
            level.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE,
                    SoundSource.PLAYERS, 0.4f, 1.6f);
        }
    }
}
