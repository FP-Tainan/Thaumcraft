package net.thaumcraft.mortuorum;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.thaumcraft.Thaumcraft;

/**
 * O {@code ForgeEventHandler} do Necromancy: o que o mundo faz por conta própria.
 *
 * <p>São duas coisas. Um em cada trinta zumbis e esqueletos que nascem do mundo não é zumbi nem esqueleto: é um
 * Rastejador da Noite ou um Isaac. E tudo o que morre tem uma chance pequena de deixar um órgão — sete em cem, e
 * quatro delas são músculo, que é o que mais há num corpo.
 */
public final class MortuorumEvents {
    /** O {@code rarityNightcrawlers} e o {@code rarityIsaacs} do original: um em trinta, cada um. */
    public static final int RARITY = 30;

    /** A marca de que aquele bicho já passou pelo sorteio, para não se sortear outra vez ao reabrir o mundo. */
    public static final AttachmentType<Boolean> ROLLED = AttachmentRegistry.<Boolean>builder()
            .persistent(com.mojang.serialization.Codec.BOOL)
            .buildAndRegister(Thaumcraft.id("mortuorum_rolled"));

    private MortuorumEvents() {
    }

    public static void init() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (!(entity instanceof Mob bicho)) return;
            if (!(bicho.getClass() == Zombie.class || bicho.getClass() == Skeleton.class)) return;
            if (bicho.hasAttached(ROLLED)) return;
            bicho.setAttached(ROLLED, true);
            var random = level.getRandom();
            if (random.nextInt(RARITY) == 0) {
                replace(level, bicho, MortuorumEntities.NIGHT_CRAWLER);
            } else if (random.nextInt(RARITY) == 0) {
                replace(level, bicho, MortuorumEntities.ISAAC_NORMAL);
            }
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!(entity.level() instanceof ServerLevel level)) return;
            if (!(entity instanceof Mob)) return;
            String órgão = organOf(level.getRandom().nextInt(100));
            if (órgão == null) return;
            entity.spawnAtLocation(level, new ItemStack(MortuorumItems.ORGANS.get(órgão)));
        });
    }

    /** Qual órgão cai, pelo número que saiu de cem — a mesma tabela do {@code LivingDeathEvent}. */
    public static String organOf(int sorte) {
        return switch (sorte) {
            case 0 -> "brains";
            case 1 -> "heart";
            case 2, 3, 4, 5 -> "muscle";
            case 6 -> "lungs";
            default -> null;
        };
    }

    private static void replace(ServerLevel level, Mob velho, net.minecraft.world.entity.EntityType<? extends Mob> tipo) {
        Mob novo = tipo.create(level, EntitySpawnReason.NATURAL);
        if (novo == null) return;
        novo.snapTo(velho.getX(), velho.getY(), velho.getZ(), velho.getYRot(), velho.getXRot());
        velho.discard();
        level.addFreshEntity(novo);
    }

    /** Se aquele bicho vivo é um dos que o ramo troca. */
    public static boolean replaceable(LivingEntity bicho) {
        return bicho.getClass() == Zombie.class || bicho.getClass() == Skeleton.class;
    }
}
