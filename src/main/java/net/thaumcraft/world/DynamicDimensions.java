package net.thaumcraft.world;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.mixin.MinecraftServerLevelsAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Abrir e fechar mundos com o jogo andando.
 *
 * <p>O jogo de hoje monta os mundos uma vez, na abertura, e não tem porta para se pedir mais um depois. Os bolsos
 * das Portas Dimensionais precisam exatamente disso — um mundo por bolso, feito na hora em que alguém atravessa a
 * porta —, então a porta abre-se aqui: um {@code ServerLevel} novo posto à mão na lista do servidor, com o feitio
 * de mundo e o gerador que se lhe der.
 *
 * <p>O mundo criado ganha a sua pasta debaixo da do mundo, guarda-se e volta a abrir-se sozinho; o que ele não faz
 * é aparecer na lista que o cliente recebeu quando entrou, e por isso quem for mandado para lá tem de receber o
 * feitio do mundo junto — o que o {@code ClientboundRespawnPacket} de hoje já leva.
 */
public final class DynamicDimensions {
    private DynamicDimensions() {
    }

    /** O mundo daquela chave, criando-o se ainda não houver. */
    public static ServerLevel getOrCreate(MinecraftServer server, ResourceKey<Level> key,
                                          ResourceKey<DimensionType> type, ChunkGenerator generator) {
        ServerLevel achado = server.getLevel(key);
        if (achado != null) return achado;
        return create(server, key, type, generator);
    }

    /** Abre um mundo novo e põe-no na lista do servidor. */
    public static @Nullable ServerLevel create(MinecraftServer server, ResourceKey<Level> key,
                                               ResourceKey<DimensionType> type, ChunkGenerator generator) {
        var acesso = (MinecraftServerLevelsAccessor) server;
        Holder.Reference<DimensionType> feitio = server.registryAccess()
                .lookupOrThrow(Registries.DIMENSION_TYPE).get(type).orElse(null);
        if (feitio == null) {
            Thaumcraft.LOGGER.error("não há feitio de mundo chamado {}", type.identifier());
            return null;
        }

        var dados = new DerivedLevelData(server.getWorldData(), server.getWorldData().overworldData());
        ServerLevel mundo = new ServerLevel(server, acesso.thaumcraft$executor(), acesso.thaumcraft$storage(),
                dados, key, new LevelStem(feitio, generator), false,
                net.minecraft.world.level.biome.BiomeManager.obfuscateSeed(server.overworld().getSeed()),
                List.of(), false);

        Map<ResourceKey<Level>, ServerLevel> lista = acesso.thaumcraft$levels();
        lista.put(key, mundo);
        
        Thaumcraft.LOGGER.info("mundo novo aberto: {}", key.identifier());
        return mundo;
    }

    /** Fecha um mundo aberto assim e tira-o da lista. Quem estiver lá dentro vai para o mundo de cima. */
    public static boolean remove(MinecraftServer server, ResourceKey<Level> key) {
        var acesso = (MinecraftServerLevelsAccessor) server;
        ServerLevel mundo = acesso.thaumcraft$levels().get(key);
        if (mundo == null || mundo == server.overworld()) return false;

        for (var jogador : List.copyOf(mundo.players())) {
            jogador.teleportTo(server.overworld(), jogador.getX(), jogador.getY(), jogador.getZ(),
                    java.util.Set.of(), jogador.getYRot(), jogador.getXRot(), false);
        }
        
        try {
            mundo.close();
        } catch (Exception erro) {
            Thaumcraft.LOGGER.error("não consegui fechar o mundo {}", key.identifier(), erro);
        }
        acesso.thaumcraft$levels().remove(key);
        Thaumcraft.LOGGER.info("mundo fechado: {}", key.identifier());
        return true;
    }

    /** A chave de um mundo do mod pelo nome. */
    public static ResourceKey<Level> key(String nome) {
        return ResourceKey.create(Registries.DIMENSION, Thaumcraft.id(nome));
    }

    /** E a chave de um mundo com nome e número, que é como os bolsos se vão chamar. */
    public static ResourceKey<Level> key(String nome, int numero) {
        return ResourceKey.create(Registries.DIMENSION,
                Identifier.fromNamespaceAndPath(Thaumcraft.MOD_ID, nome + "_" + numero));
    }
}
