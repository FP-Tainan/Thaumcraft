package net.thaumcraft.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * A lista de mundos do servidor e a gaveta onde eles se guardam, para se poder abrir um mundo novo com o jogo já
 * andando — é o que os bolsos das Portas Dimensionais vão precisar.
 */
@Mixin(MinecraftServer.class)
public interface MinecraftServerLevelsAccessor {
    @Accessor("levels")
    Map<ResourceKey<Level>, ServerLevel> thaumcraft$levels();

    @Accessor("storageSource")
    LevelStorageSource.LevelStorageAccess thaumcraft$storage();

    @Accessor("executor")
    Executor thaumcraft$executor();
}
