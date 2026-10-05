package net.thaumcraft.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.thaumcraft.occulta.goblin.GoblinClothes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * O que a <b>roupa de goblin</b> faz ao golpe: o {@code onLivingHurt} do {@code ItemGoblinClothes}.
 *
 * <p>Duas coisas, e as duas no mesmo lugar do jogo em que o Witchery as punha:
 *
 * <ul>
 *   <li>uma <b>flecha da Aljava do Mog</b> faz <b>três vezes</b> o dano em quem estiver <b>no ar</b>, e
 *       deixa <b>Fraqueza</b> dez segundos em qualquer caso;</li>
 *   <li>um <b>murro de mão vazia</b> de quem tem a <b>Cinta do Gulg</b> faz <b>cinco</b> de dano — nem mais
 *       nem menos, encantamentos à parte — e atira quem apanha <b>um bloco para cima</b>.</li>
 * </ul>
 *
 * <p>Repare que o cinco da Cinta é um <b>valor fixo</b>, e não um aumento: com ela, um murro vale sempre o
 * mesmo. É de propósito — ela é para quem anda de mãos vazias.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityGoblinClothesMixin {
    @ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), argsOnly = true)
    private float thaumcraft$roupaDeGoblin(float dano, ServerLevel level, DamageSource fonte) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (GoblinClothes.daAljava(fonte)) return GoblinClothes.aFlecha(level, self, dano);
        if (GoblinClothes.daCinta(fonte)) return GoblinClothes.aCinta(self);
        return dano;
    }
}
