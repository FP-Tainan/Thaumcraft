package net.thaumcraft.crimson;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.thaumcraft.Thaumcraft;

/**
 * As coisas do Crimson Warfare: só o item do altar, e esse não se ganha jogando — o original põe o altar no mundo
 * e não o deixa cair nem fazer. Fica para quem estiver no criativo, e para se poder ver o desenho dele.
 */
public final class CrimsonItems {
    public static final Item ANCIENT_ALTAR = register("ancient_altar");

    private CrimsonItems() {
    }

    private static Item register(String name) {
        Identifier id = Thaumcraft.id(name);
        var properties = new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, id))
                .useBlockDescriptionPrefix();
        return Registry.register(BuiltInRegistries.ITEM, id, new BlockItem(CrimsonBlocks.ANCIENT_ALTAR, properties));
    }

    public static void init() {
    }
}
