package net.thaumcraft.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.thaumcraft.occulta.waystone.Waystones;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A batida do {@code EntityItemWaystone}: uma pedra largada no chão olha o giz em volta.
 *
 * <p>No original a pedra é uma <b>classe de item largado própria</b>, e o mod troca o item largado por ela no
 * {@code EntityJoinWorldEvent}. Aqui a batida é a de qualquer item largado, e a pergunta é pelo item — o que dá
 * o mesmo e poupa uma entidade. <b>Declarado no {@code PORTE.md}.</b>
 *
 * <p>Os dois segundos de espera e as quarenta batidas entre olhadas são os do original, e é por elas que isto
 * não custa nada: um item largado olha o chão uma vez a cada dois segundos, e só se for uma pedra.
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityWaystoneMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void thaumcraft$pedraDeCaminho(CallbackInfo info) {
        ItemEntity self = (ItemEntity) (Object) this;
        if (self.isRemoved() || !(self.level() instanceof ServerLevel level)) return;
        if (self.getAge() <= Waystones.ESPERA || self.getAge() % Waystones.OLHA_DE != 0) return;

        if (Waystones.tentaPrender(level, self)) return;
        Waystones.tentaLevar(level, self);
    }
}
