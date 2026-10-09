package net.thaumcraft.occulta.curse;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;

/**
 * O <b>Cozimento do Grotesco</b>: o {@code witcheryGrotesque} do Witchery.
 *
 * <p>Bebe-se, e por <b>quatro minutos</b> quem o bebeu fica insuportável de se chegar perto: tudo o que for
 * vivo e estiver a menos de <b>quatro blocos</b> é <b>empurrado para longe</b>, sem parar, enquanto durar.
 *
 * <p>Não é dano e não é medo — é só distância. E é por isso que ele é o ingrediente das maldições: para
 * amaldiçoar alguém não se precisa de força, precisa-se de que ninguém chegue perto do círculo.
 *
 * <p><b>Quatro coisas não se empurram</b>, e são as do original: os <b>chefes</b>, os <b>golens</b> e as
 * <b>bruxas</b>. O quarto é o <b>demônio</b>, que não está portado.
 */
public final class Grotesque {
    /**
     * Quantas <b>contagens</b> dura: as mil e duzentas do original.
     *
     * <p>E não são batidas: a contagem do original corre <b>de quatro em quatro</b>, porque o
     * {@code handleBrewGrotesqueEffect} vive dentro de um {@code counter % 4 == 0}. Mil e duzentas contagens
     * são, então, <b>quatro mil e oitocentas batidas</b> — quatro minutos, e não um. O autor escreveu 1200
     * querendo um minuto e ficou com quatro; o número é dele, e fica.
     */
    public static final int DURA = 1200;

    /** E até onde ele empurra. */
    public static final double ALCANCE = 4.0;

    /** A força do empurrão. */
    public static final double EMPURRÃO = 0.3;

    /** Quantas contagens restam a quem o bebeu. */
    public static final AttachmentType<Integer> DATA = AttachmentRegistry.<Integer>builder()
            .initializer(() -> 0)
            .persistent(Codec.INT)
            .buildAndRegister(Thaumcraft.id("grotesque"));

    private Grotesque() {
    }

    /** Sem uso fora do porte: obriga a classe a carregar, e com ela o apego. */
    public static void init() {
    }

    /** Começa o minuto. */
    public static void bebeu(Player quem) {
        quem.setAttached(DATA, DURA);
    }

    /** Quantas contagens ainda faltam. */
    public static int resta(Player quem) {
        return quem.getAttachedOrCreate(DATA);
    }

    /**
     * A contagem: empurra o que estiver perto, e conta o tempo. Quem a chama é o
     * {@code LivingEntityCurseMixin}, de quatro em quatro batidas.
     *
     * <p>O empurrão é <b>na linha que sai de quem bebeu</b>, e não para um lado qualquer: é o
     * {@code RiteProtectionCircleRepulsive.push} do original, que o Grotesco reaproveita.
     */
    public static void tick(ServerLevel level, Player quem) {
        int resta = resta(quem);
        if (resta <= 0) return;

        for (LivingEntity bicho : level.getEntitiesOfClass(LivingEntity.class,
                new AABB(quem.blockPosition()).inflate(ALCANCE))) {
            if (bicho == quem || !empurrável(bicho)) continue;
            if (bicho.distanceToSqr(quem) > ALCANCE * ALCANCE) continue;
            empurra(bicho, quem.position());
        }

        quem.setAttached(DATA, resta - 1);
    }

    /** Os chefes, os golens e as bruxas ficam onde estão. */
    private static boolean empurrável(LivingEntity bicho) {
        if (!(bicho instanceof Mob)) return false;
        if (bicho instanceof WitherBoss || bicho instanceof EnderDragon) return false;
        if (bicho instanceof net.minecraft.world.entity.animal.golem.AbstractGolem) return false;
        return !(bicho instanceof Witch);
    }

    /** Empurra um bicho para longe de um ponto. */
    public static void empurra(LivingEntity bicho, Vec3 deOnde) {
        Vec3 rumo = bicho.position().subtract(deOnde);
        double quão = rumo.horizontalDistance();
        if (quão < 1.0e-4) {
            rumo = new Vec3(bicho.getRandom().nextDouble() - 0.5, 0.0,
                    bicho.getRandom().nextDouble() - 0.5);
            quão = Math.max(rumo.horizontalDistance(), 1.0e-4);
        }
        bicho.push(rumo.x / quão * EMPURRÃO, 0.1, rumo.z / quão * EMPURRÃO);
        bicho.hurtMarked = true;
    }
}
