package net.thaumcraft.shattered;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponentType;
import net.thaumcraft.Thaumcraft;

/** O que as coisas dos Reinos Fragmentados guardam dentro de si. */
public final class ShatteredComponents {
    /** O lugar que a Assinatura de Fenda marcou. */
    public static final DataComponentType<RiftBlockEntity.Destination> RIFT_SOURCE = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE, Thaumcraft.id("rift_source"),
            DataComponentType.<RiftBlockEntity.Destination>builder()
                    .persistent(RiftBlockEntity.Destination.CODEC)
                    .build());

    private ShatteredComponents() {
    }

    public static void init() {
    }

    /** O codec só existe do lado de quem guarda; o item mostra o brilho por ele. */
    public static Codec<RiftBlockEntity.Destination> codec() {
        return RiftBlockEntity.Destination.CODEC;
    }
}
