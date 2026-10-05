package net.thaumcraft.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Se o bicho está <b>pedindo para pular</b>.
 *
 * <p>O campo é protegido, e os poderes de bicho do morcego e do slime precisam dele: o original lê a tecla
 * de pular direto, e o que o jogo guarda neste campo é exatamente o que essa tecla pediu.
 */
@Mixin(LivingEntity.class)
public interface LivingEntityJumpingAccessor {
    @Accessor("jumping")
    boolean thaumcraft$pulando();
}
