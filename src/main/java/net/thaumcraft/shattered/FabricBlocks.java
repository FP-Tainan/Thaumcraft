package net.thaumcraft.shattered;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.thaumcraft.Thaumcraft;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Os tecidos dos Reinos Fragmentados: os quatro {@code BlockFabric} das Portas Dimensionais.
 *
 * <p><b>Arquivo gerado</b> por {@code scratchpad/dd-tecido.js} a partir do jar original — não se escreve à
 * mão. O tecido comum é o que forra os bolsos e se troca por qualquer bloco cheio lá dentro; o antigo é o
 * mesmo, mas não se quebra; o eterno é o chão do Limbo, que devolve para casa quem o pisa; e o desfiado é o
 * que o Limbo vai comendo. Todos brilham e nenhum deles cai.
 */
public final class FabricBlocks {
    /** O tecido comum, uma cor por casa. */
    public static final Map<DyeColor, Block> FABRIC = new LinkedHashMap<>();
    /** E o antigo, que não se quebra. */
    public static final Map<DyeColor, Block> ANCIENT = new LinkedHashMap<>();

    /** O chão do Limbo. */
    public static final Block ETERNAL = register("eternal_fabric", properties ->
            new EternalFabricBlock(properties.mapColor(MapColor.COLOR_LIGHT_GRAY).strength(-1.0f, 6000000.0f)
                    .lightLevel(state -> 15).sound(SoundType.STONE).noLootTable()));

    /** E o que o Limbo vai comendo. */
    public static final Block UNRAVELLED = register("unravelled_fabric", properties ->
            new Block(properties.mapColor(MapColor.COLOR_GRAY).strength(0.1f)
                    .sound(SoundType.STONE).noLootTable()));

    static {
        for (DyeColor cor : DyeColor.values()) {
            String nome = cor.getSerializedName();
            FABRIC.put(cor, register("fabric_" + nome, properties ->
                    new FabricBlock(properties.mapColor(cor.getMapColor()).strength(0.1f)
                            .lightLevel(state -> 15).sound(SoundType.STONE).noLootTable())));
            ANCIENT.put(cor, register("ancient_fabric_" + nome, properties ->
                    new Block(properties.mapColor(cor.getMapColor()).strength(-1.0f, 6000000.0f)
                            .lightLevel(state -> 15).sound(SoundType.STONE).noLootTable())));
        }
    }

    private FabricBlocks() {
    }

    private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory) {
        Identifier id = Thaumcraft.id(name);
        Block block = factory.apply(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, id)));
        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    /** Se aquele bloco é tecido de bolso, de qualquer cor. */
    public static boolean isFabric(Block block) {
        return FABRIC.containsValue(block) || ANCIENT.containsValue(block);
    }

    public static int count() {
        return FABRIC.size() + ANCIENT.size() + 2;
    }

    public static void init() {
    }
}
