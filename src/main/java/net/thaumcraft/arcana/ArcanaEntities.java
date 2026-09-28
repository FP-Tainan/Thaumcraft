package net.thaumcraft.arcana;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.thaumcraft.Thaumcraft;

/**
 * Os bichos do Ars Arcana — que, por enquanto, é um só e nem é bicho.
 *
 * <p>O Projétil do Ars Magica 2 é uma entidade e não uma partícula, porque ele <b>leva o feitiço dentro de
 * si</b>: quem bate nele não recebe um dano decidido no momento do disparo, recebe a frase inteira rodando
 * no ponto da batida.
 */
public final class ArcanaEntities {
    /**
     * O <b>feitiço voando</b>: a {@code EntitySpellProjectile}.
     *
     * <p>Um quarto de bloco de lado, como no original, e alcance de vista de sessenta e quatro blocos — que é o
     * que o {@code isInRangeToRenderDist} do original dá para uma caixa deste tamanho.
     */
    public static final EntityType<SpellProjectileEntity> SPELL_PROJECTILE = register("spell_projectile",
            EntityType.Builder.<SpellProjectileEntity>of(SpellProjectileEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(4)
                    .updateInterval(1));

    private ArcanaEntities() {
    }

    private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(
            String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Thaumcraft.id(name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    /** Sem uso fora do porte: obriga a classe a ser carregada, e com ela o Projétil a se registrar. */
    public static void init() {
    }
}
