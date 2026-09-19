package net.thaumcraft.world.outer;

import com.mojang.serialization.Codec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.thaumcraft.Thaumcraft;

/** Quantos chefes o mundo já teve: o {@code MapBossData} da 4.2.3.5, que decide qual vem na próxima fechadura. */
public final class BossCount extends SavedData {
    private static final Codec<BossCount> CODEC = Codec.INT.fieldOf("bossCount").codec().xmap(BossCount::new, b -> b.bossCount);
    private static final SavedDataType<BossCount> TYPE = new SavedDataType<>(Thaumcraft.id("boss_map_data"), BossCount::new, CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    public int bossCount;

    private BossCount() {
    }

    private BossCount(int count) {
        this.bossCount = count;
    }

    public static BossCount get(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TYPE);
    }
}
