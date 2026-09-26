package net.thaumcraft.shattered;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Os que já andavam nas fendas.
 *
 * <p><b>Isto é do porte, e não do original</b>, e a ideia é de quem joga: <i>a gente pode amarrar isso com os
 * endermans, assim isso explicaria como eles vagam entre as dimensões e como eles teleportam</i>. E de facto
 * explica — o Thaumcraft e as Portas Dimensionais já contavam a mesma história por dois lados, e faltava alguém
 * a atravessar de um para o outro. São eles.
 *
 * <p>Três coisas ficam ditas por isto, sem uma linha de texto:
 *
 * <ul>
 *   <li>uma fenda grande o bastante <b>põe cá fora</b> um enderman de vez em quando — ele não apareceu, chegou;</li>
 *   <li>um enderman morto ao pé de uma fenda, ou dentro de um Reino Fragmentado, deixa <b>Fio do Mundo</b> — é o
 *       que ele traz agarrado de tanto andar por onde o Véu está roto;</li>
 *   <li>e nas salas para lá de uma fenda presa há sempre um ou outro, porque é ali que eles moram.</li>
 * </ul>
 */
public final class RiftWalkers {
    /** De quanto em quanto uma fenda põe cá fora um deles, e a partir de que tamanho. */
    public static final int EMERGE_CHANCE = 12000;
    public static final float EMERGE_SIZE = 120.0f;
    /** E quantos podem andar por perto antes de a fenda parar de os mandar. */
    public static final int CROWD = 3;
    public static final double CROWD_RANGE = 16.0;

    /** Até que distância de uma fenda a morte de um deles deixa fio. */
    public static final double THREAD_RANGE = 12.0;
    public static final int THREAD_MIN = 1;
    public static final int THREAD_MAX = 2;

    private RiftWalkers() {
    }

    public static void init() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!(entity instanceof EnderMan)) return;
            if (!(entity.level() instanceof ServerLevel level)) return;
            // como no resto do mod, o que morre sozinho não deixa nada: é preciso ter sido às mãos de alguém
            if (entity.getLastHurtByPlayerMemoryTime() <= 0) return;
            if (!walksTheVeil(entity)) return;
            int quanto = THREAD_MIN + level.getRandom().nextInt(THREAD_MAX - THREAD_MIN + 1);
            entity.spawnAtLocation(level, new ItemStack(ShatteredItems.WORLD_THREAD, quanto));
        });
    }

    /** Este andava por onde o Véu está roto: ou ao pé de uma fenda, ou de dentro de um Reino Fragmentado. */
    public static boolean walksTheVeil(LivingEntity quem) {
        Level level = quem.level();
        if (ShatteredRealms.isOurs(level)) return true;
        BlockPos onde = quem.blockPosition();
        int alcance = (int) Math.ceil(THREAD_RANGE);
        for (BlockPos perto : BlockPos.betweenClosed(onde.offset(-alcance, -alcance, -alcance),
                onde.offset(alcance, alcance, alcance))) {
            if (level.getBlockState(perto).is(ShatteredBlocks.RIFT)) return true;
        }
        return false;
    }

    /**
     * De vez em quando, uma fenda já crescida põe um cá fora.
     *
     * <p>Chamada do tique da fenda solta, e só do lado do servidor. Uma fenda pequena não dá: ela ainda mal se
     * vê, e o que passa por um rasgão daquele tamanho não é ninguém.
     */
    public static void maybeEmerge(ServerLevel level, BlockPos onde, RiftBlockEntity fenda) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) return;
        if (fenda.size() < EMERGE_SIZE) return;
        if (level.getRandom().nextInt(EMERGE_CHANCE) != 0) return;

        AABB perto = new AABB(onde).inflate(CROWD_RANGE);
        if (level.getEntitiesOfClass(EnderMan.class, perto).size() >= CROWD) return;

        EnderMan quem = EntityTypes.ENDERMAN.create(level, EntitySpawnReason.EVENT);
        if (quem == null) return;
        quem.snapTo(onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5, level.getRandom().nextFloat() * 360.0f, 0.0f);
        if (!level.addFreshEntity(quem)) return;
        level.playSound(null, onde, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0f, 0.8f);
    }

    /** E um ou dois em cada sala para lá de uma fenda presa, que é onde eles moram. */
    public static void populate(ServerLevel bolsos, BlockPos canto, int largura, int comprimento) {
        if (bolsos.getDifficulty() == Difficulty.PEACEFUL) return;
        int quantos = 1 + bolsos.getRandom().nextInt(2);
        for (int i = 0; i < quantos; i++) {
            EnderMan quem = EntityTypes.ENDERMAN.create(bolsos, EntitySpawnReason.STRUCTURE);
            if (quem == null) continue;
            BlockPos onde = canto.offset(2 + bolsos.getRandom().nextInt(Math.max(1, largura - 4)), 1,
                    2 + bolsos.getRandom().nextInt(Math.max(1, comprimento - 4)));
            quem.snapTo(onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5,
                    bolsos.getRandom().nextFloat() * 360.0f, 0.0f);
            quem.setPersistenceRequired();
            bolsos.addFreshEntity(quem);
        }
    }
}
