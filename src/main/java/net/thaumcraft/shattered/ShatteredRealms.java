package net.thaumcraft.shattered;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.world.DynamicDimensions;

import java.util.Set;

/**
 * Os Reinos Fragmentados — as Portas Dimensionais, com o nome que a lore de quem joga lhes dá.
 *
 * <p>O original trata os bolsos como três mundos grandes, cada um com os bolsos lado a lado numa grelha; aqui,
 * agora que o porte sabe abrir mundo com o jogo andando ({@link DynamicDimensions}), cada bolso pode ser um mundo
 * seu. Esta fatia põe de pé o que os dois jeitos precisam: os feitios de mundo, saber se se está num bolso, e a
 * saída de emergência que o tecido eterno do Limbo dá.
 *
 * <p><b>O nome do ramo é provisório</b>: a lore chama os bolsos de <i>The Shattered Realms</i> e chama a pesquisa
 * que os abre de <i>Fractures in the Veil</i>, mas não diz como se há de chamar a aba. Fica {@code SHATTERED} até
 * quem manda dizer outra coisa.
 */
public final class ShatteredRealms {
    /** A aba do ramo no Thaumonomicon. */
    public static final String CATEGORY = "SHATTERED";

    /** Os três mundos do original, mais o Limbo. */
    public static final ResourceKey<Level> LIMBO = DynamicDimensions.key("limbo");
    public static final ResourceKey<Level> PRIVATE_POCKETS = DynamicDimensions.key("private_pockets");
    public static final ResourceKey<Level> PUBLIC_POCKETS = DynamicDimensions.key("public_pockets");
    public static final ResourceKey<Level> DUNGEON_POCKETS = DynamicDimensions.key("dungeon_pockets");

    /** Os feitios de mundo, que vão nos dados. */
    public static final ResourceKey<DimensionType> POCKET_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, Thaumcraft.id("pocket"));
    public static final ResourceKey<DimensionType> LIMBO_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, Thaumcraft.id("limbo"));

    private ShatteredRealms() {
    }

    public static void init() {
        FabricBlocks.init();
        ShatteredBlocks.init();
        ShatteredComponents.init();
        ShatteredEntities.init();
        ShatteredItems.init();
        // a aba do ramo no livro
        net.thaumcraft.api.ThaumcraftApi.category(CATEGORY,
                Thaumcraft.id("textures/misc/r_shattered.png"),
                Thaumcraft.id("textures/gui/gui_shattered_researchback.png"));
        ShatteredTable.research();
        net.thaumcraft.api.ThaumcraftApi.onSetup(ShatteredTable::recipes);
        Thaumcraft.LOGGER.info("Reinos Fragmentados: {} tecidos, {} coisas", FabricBlocks.count(), ShatteredItems.count());
    }

    /** Se aquele mundo é um dos bolsos — o {@code isDimDoorsPocketDimension} do original. */
    public static boolean isPocket(Level level) {
        ResourceKey<Level> chave = level.dimension();
        return chave == PRIVATE_POCKETS || chave == PUBLIC_POCKETS || chave == DUNGEON_POCKETS
                || chave.identifier().getPath().startsWith("pocket_");
    }

    /** E se é um mundo do ramo, contando o Limbo. */
    public static boolean isOurs(Level level) {
        return isPocket(level) || level.dimension() == LIMBO;
    }

    /** O Limbo, abrindo-o se ainda não houver. */
    public static @org.jetbrains.annotations.Nullable ServerLevel limbo(net.minecraft.server.MinecraftServer server) {
        var biomas = new net.minecraft.world.level.biome.FixedBiomeSource(
                server.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BIOME)
                        .getOrThrow(net.minecraft.world.level.biome.Biomes.THE_VOID));
        return DynamicDimensions.getOrCreate(server, LIMBO, LIMBO_TYPE, new LimboChunkGenerator(biomas));
    }

    /**
     * A saída do Limbo: o {@code EscapeTarget} do original devolve quem pisa o tecido eterno ao mundo de onde ele
     * veio. Sem uma marca de onde se estava — que chega noutra fatia — a saída é o mundo de cima.
     */
    public static void escape(ServerLevel level, Entity quem) {
        if (!isOurs(level)) return;
        ServerLevel casa = level.getServer().overworld();
        var onde = casa.getRespawnData().pos();
        if (quem instanceof ServerPlayer jogador) {
            jogador.teleportTo(casa, onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5, Set.of(),
                    jogador.getYRot(), jogador.getXRot(), false);
        } else {
            quem.teleportTo(casa, onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5, Set.of(),
                    quem.getYRot(), quem.getXRot(), false);
        }
    }
}
