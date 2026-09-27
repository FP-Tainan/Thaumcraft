package net.thaumcraft.occulta.spirit;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;

/**
 * Quem anda em espírito não morre: acorda. É a parte do {@code onLivingHurt} do Witchery que cancela o golpe
 * mortal e marca a pessoa para acordar.
 *
 * <p>No Mundo dos Espíritos e em fantasma no mundo de cá, o golpe que mataria é <b>apagado</b> e em vez dele o
 * espírito volta ao corpo. Faz sentido e é a regra que torna o outro lado jogável: o corpo está deitado no
 * mundo de cá, à vista de qualquer um, e morrer <b>lá</b> mataria o que está <b>aqui</b>.
 *
 * <p>Quem está em modo criativo não entra nesta conta, como no original.
 */
public final class SpiritDeath {
    private SpiritDeath() {
    }

    public static void init() {
        ServerLivingEntityEvents.ALLOW_DEATH.register((quem, fonte, dano) -> {
            if (!(quem instanceof ServerPlayer gente)) return true;
            if (gente.hasInfiniteMaterials()) return true;

            SpiritWalk agora = SpiritWalk.of(gente);
            if (agora.ghost()) {
                gente.setHealth(1.0f);
                SpiritWorld.unmanifest(gente);
                return false;
            }
            if (agora.walking() && SpiritWorld.is(gente.level())) {
                gente.setHealth(1.0f);
                SpiritWorld.wakeUp(gente);
                return false;
            }
            return true;
        });
    }
}
