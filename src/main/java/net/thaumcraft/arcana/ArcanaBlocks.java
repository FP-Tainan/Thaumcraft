package net.thaumcraft.arcana;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.thaumcraft.Thaumcraft;

import java.util.Set;
import java.util.function.Function;

/** Os blocos do Ars Arcana. */
public final class ArcanaBlocks {
    /**
     * A <b>Runa</b> desenhada no chão: um feitiço que espera quem pisar nela.
     *
     * <p>Ela não se parte a pico e não se empurra a pistão: ou dispara e se gasta, ou fica. É o que o original
     * faz, e é o que a torna uma armadilha e não um bloco.
     */
    public static final Block SPELL_RUNE = register("spell_rune", properties ->
            new SpellRuneBlock(properties
                    .mapColor(MapColor.NONE)
                    .noCollision()
                    .noOcclusion()
                    .instabreak()
                    .pushReaction(PushReaction.DESTROY)
                    .sound(SoundType.STONE)
                    .lightLevel(estado -> 3)));

    public static final BlockEntityType<SpellRuneBlockEntity> SPELL_RUNE_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("spell_rune"),
                    new BlockEntityType<>(SpellRuneBlockEntity::new, Set.of(SPELL_RUNE)));

    /**
     * A <b>Mesa de Inscrição</b>: a bancada do arcanista, onde as peças viram frase.
     *
     * <p>De madeira e pedra, como no original, e com a resistência de uma bancada de verdade — quem a põe
     * quer que ela fique.
     */
    public static final Block INSCRIPTION_TABLE = register("inscription_table", properties ->
            new InscriptionTableBlock(properties
                    .mapColor(MapColor.WOOD)
                    .strength(2.5f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

    public static final BlockEntityType<InscriptionTableBlockEntity> INSCRIPTION_TABLE_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("inscription_table"),
                    new BlockEntityType<>(InscriptionTableBlockEntity::new, Set.of(INSCRIPTION_TABLE)));

    /**
     * O <b>Óculus</b>: o pedestal onde se olha para o que se pode aprender.
     *
     * <p>De pedra, e acende um pouco, porque o olho dele é de vidro e guarda luz.
     */
    public static final Block OCCULUS = register("occulus", properties ->
            new OcculusBlock(properties
                    .mapColor(MapColor.STONE)
                    .strength(3.0f)
                    .sound(SoundType.STONE)
                    .lightLevel(estado -> 7)));

    private ArcanaBlocks() {
    }

    private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory) {
        Identifier id = Thaumcraft.id(name);
        Block block = factory.apply(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, id)));
        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    /** Sem uso fora do porte: obriga a classe a ser carregada, e com ela os blocos a se registrarem. */
    public static void init() {
    }
}
