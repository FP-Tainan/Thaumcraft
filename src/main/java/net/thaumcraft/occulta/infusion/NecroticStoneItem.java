package net.thaumcraft.occulta.infusion;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * A <b>Pedra Necrótica</b> na mão: o {@code useNecroStone} do Witchery.
 *
 * <p>Por si ela é um ingrediente de rito, e nada mais. Mas na mão de quem bebeu o
 * {@linkplain InfusedBrews Cozimento da Cova}, ela é o <b>olhar que escraviza</b>: aponta-se a um
 * morto-vivo a até <b>quinze blocos</b> e ele passa a ser de quem olhou.
 *
 * <p>É de dez em dez segundos, e é o único caminho do mod em que escravizar não custa nada a não ser
 * ter bebido o cozimento. Por isso o cozimento sai de um rito com um zumbi vivo em sacrifício.
 *
 * <p><b>Do original fica de fora, declarado:</b> a outra metade do {@code useNecroStone} — a que, <b>sem</b>
 * o cozimento, manda os escravos que já se tem atacarem o que se aponta. Essa pede a lista de escravos de
 * quem joga, que este porte guarda do lado do <b>escravo</b> e não do dono.
 */
public class NecroticStoneItem extends Item {
    public NecroticStoneItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player quem, InteractionHand mão) {
        if (!(level instanceof ServerLevel server) || !(quem instanceof ServerPlayer gente)) {
            return InteractionResult.SUCCESS;
        }
        if (!InfusedBrews.tem(quem, InfusedBrews.COVA)) return InteractionResult.PASS;
        if (!InfusedBrews.aPedraPode(quem)) return InteractionResult.PASS;

        HitResult olhar = OverworldInfusion.olha(server, gente, InfusedBrews.ALCANCE_DA_COVA);
        if (!(olhar instanceof EntityHitResult acertou)) return InteractionResult.PASS;
        if (!(acertou.getEntity() instanceof Mob bicho)) return InteractionResult.PASS;
        if (!net.thaumcraft.occulta.Undead.é(bicho)) return InteractionResult.PASS;
        if (net.thaumcraft.occulta.enslave.Enslavement.escravoDe(bicho, quem)) {
            return InteractionResult.PASS;
        }

        if (!net.thaumcraft.occulta.enslave.Enslavement.escraviza(bicho, quem)) {
            return InteractionResult.PASS;
        }
        InfusedBrews.marcaAPedra(quem);
        // o verde de cova do original, que é o que a poção de escravo pinta
        server.sendParticles(net.minecraft.core.particles.ColorParticleOption.create(
                        ParticleTypes.ENTITY_EFFECT, 0xFF336633),
                bicho.getX(), bicho.getY() + 1.0, bicho.getZ(), 24, 0.5, 0.5, 0.5, 0.0);
        server.playSound(null, bicho.blockPosition(), SoundEvents.ZOMBIE_INFECT,
                SoundSource.HOSTILE, 1.0f, 1.0f);
        return InteractionResult.SUCCESS;
    }
}
