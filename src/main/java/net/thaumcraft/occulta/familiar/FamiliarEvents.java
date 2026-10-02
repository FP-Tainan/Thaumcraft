package net.thaumcraft.occulta.familiar;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.player.Player;

/**
 * Os três ganchos do vínculo: <b>prender</b>, <b>não morrer</b> e <b>soltar</b>.
 *
 * <p>No Witchery são ouvintes de eventos do Forge; aqui são os eventos equivalentes do Fabric, no mesmo ponto
 * do jogo.
 */
public final class FamiliarEvents {
    private FamiliarEvents() {
    }

    public static void init() {
        UseEntityCallback.EVENT.register(FamiliarEvents::vinculaOGato);
        ServerLivingEntityEvents.ALLOW_DEATH.register(FamiliarEvents::naoMorre);
        ServerLivingEntityEvents.AFTER_DEATH.register(FamiliarEvents::quemCaiPerdeOFio);
    }

    /**
     * O <b>gato</b> vincula-se com um agachar de mão vazia, como o sapo e a coruja.
     *
     * <p>Ele não é bicho deste mod — é o do jogo —, e por isso o vínculo dele não cabe num
     * {@code mobInteract} nosso: tem de ser um gancho de fora. <b>É de propósito que seja o gato do jogo</b>:
     * o original aceita a jaguatirica dele, e quem herdou esse papel hoje é o gato, que até tem a variante
     * preta que um gato de bruxa pede.
     */
    private static InteractionResult vinculaOGato(Player quem, net.minecraft.world.level.Level level,
                                                  net.minecraft.world.InteractionHand mão,
                                                  net.minecraft.world.entity.Entity alvo,
                                                  net.minecraft.world.phys.EntityHitResult onde) {
        if (level.isClientSide() || !(alvo instanceof Cat gato)) return InteractionResult.PASS;
        if (!quem.isShiftKeyDown() || !quem.getItemInHand(mão).isEmpty()) return InteractionResult.PASS;
        if (!gato.isTame() || !gato.isOwnedBy(quem)) return InteractionResult.PASS;

        return Familiars.vincula(quem, gato) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    /**
     * <b>Um familiar não morre.</b>
     *
     * <p>Se fosse morrer, quem o tem leva o dobro da própria vida e cai no lugar dele; sem dono por perto, o
     * bicho fica com um de vida e continua. É o {@code handleLivingDeath} do original, e é o que dá peso a ter
     * um: o vínculo não é enfeite.
     */
    private static boolean naoMorre(LivingEntity bicho, net.minecraft.world.damagesource.DamageSource fonte,
                                    float dano) {
        if (!(bicho.level() instanceof ServerLevel level)) return true;
        if (!(bicho instanceof TamableAnimal)) return true;
        if (Familiars.deQueFeitio(bicho) == null) return true;
        if (Familiars.donoDe(level, bicho) == null
                && !ehFamiliarDeAlguemQueSaiu(level, bicho)) {
            return true;
        }
        return !Familiars.morreriaAgora(level, bicho);
    }

    /** Se este bicho é familiar de alguém — mesmo de quem não esteja por perto. */
    private static boolean ehFamiliarDeAlguemQueSaiu(ServerLevel level, LivingEntity bicho) {
        return Familiars.donoDe(level, bicho) != null;
    }

    /** E quem cai perde o fio: o familiar fica no mundo, solto. */
    private static void quemCaiPerdeOFio(LivingEntity quemCaiu,
                                         net.minecraft.world.damagesource.DamageSource fonte) {
        if (quemCaiu instanceof Player gente) Familiars.doneMorreu(gente);
    }
}
