package net.thaumcraft.world;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.thaumcraft.entity.FireBatEntity;
import net.thaumcraft.registry.TCEntities;

/**
 * O {@code initEntitySpawns} da 4.2.3.5: onde cada criatura nasce sozinha.
 *
 * <ul>
 *   <li>o zumbi zangado, peso 10, em todo bioma da superfície que já tem monstros (aqui: onde nasce zumbi);</li>
 *   <li>o morcego de fogo, peso 10, de um a dois, no Nether — e, no Dia das Bruxas, peso 5 em todo lugar;</li>
 *   <li>o fogo-fátuo, peso 5, sozinho, no Nether.</li>
 * </ul>
 */
public final class CreatureSpawns {
    private CreatureSpawns() {
    }

    public static void init() {
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld().and(BiomeSelectors.spawnsOneOf(EntityTypes.ZOMBIE)),
                MobCategory.MONSTER, TCEntities.BRAINY_ZOMBIE, 10, 1, 1);
        BiomeModifications.addSpawn(BiomeSelectors.foundInTheNether(), MobCategory.MONSTER, TCEntities.FIREBAT, 10, 1, 2);
        if (FireBatEntity.halloween()) {
            BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), MobCategory.MONSTER, TCEntities.FIREBAT, 5, 1, 2);
        }
        BiomeModifications.addSpawn(BiomeSelectors.foundInTheNether(), MobCategory.MONSTER, TCEntities.WISP, 5, 1, 1);
    }
}
