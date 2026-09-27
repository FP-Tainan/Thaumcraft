package net.thaumcraft.occulta;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * O vodu: o que a boneca faz a quem está preso a ela, e o que a guarda disso.
 *
 * <p>Quem traz uma <b>Boneca Contra o Vodu</b> presa a si não sente nada do que se faça à sua figura — e a
 * boneca que o guardou gasta-se. De vez em quando, além disso, cai um raio em cima de quem tentou.
 */
public final class Voodoo {
    /** A chance de o raio cair em quem tentou, quando a proteção pega. */
    public static final int LIGHTNING_CHANCE = 4;

    private Voodoo() {
    }

    /** Quem esta boneca tem preso, se estiver por aí. */
    @Nullable
    public static LivingEntity bound(ServerLevel level, ItemStack boneca) {
        var vínculo = TaglockItem.bound(boneca);
        if (vínculo == null) return null;
        var quem = level.getServer().getPlayerList().getPlayer(vínculo.owner());
        if (quem != null) return quem;
        return level.getEntity(vínculo.owner()) instanceof LivingEntity vivo ? vivo : null;
    }

    /** Se aquela pessoa está guardada do vodu — e, estando, gasta a boneca que a guardou. */
    public static boolean guarded(ServerLevel level, LivingEntity alvo) {
        if (!(alvo instanceof Player gente)) return false;
        return Poppets.spend(level, gente, PoppetItem.Kind.VOODOO_PROTECTION);
    }

    /** O troco de quem mexe com quem está guardado. */
    public static void backfire(ServerLevel level, Player quemTentou) {
        level.playSound(null, quemTentou.blockPosition(), SoundEvents.WITCH_CELEBRATE, SoundSource.PLAYERS,
                1.0f, 0.6f);
        if (level.getRandom().nextInt(LIGHTNING_CHANCE) != 0) return;
        var raio = EntityTypes.LIGHTNING_BOLT.create(level, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
        if (raio == null) return;
        raio.snapTo(quemTentou.position());
        level.addFreshEntity(raio);
    }

    /** Gasta uma agulha de osso da mochila de quem espeta, se houver. */
    public static boolean takeNeedle(Player quem) {
        for (int i = 0; i < quem.getInventory().getContainerSize(); i++) {
            ItemStack stack = quem.getInventory().getItem(i);
            if (!stack.is(net.thaumcraft.mortuorum.MortuorumItems.BONE_NEEDLE)) continue;
            stack.shrink(1);
            return true;
        }
        return false;
    }
}
