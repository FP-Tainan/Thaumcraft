package net.thaumcraft.block.entity.eldritch;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.entity.eldritch.CultistClericEntity;
import net.thaumcraft.entity.eldritch.CultistEntity;
import net.thaumcraft.entity.eldritch.CultistKnightEntity;
import net.thaumcraft.registry.TCEntities;

/**
 * Quem o altar eldritch chama e o labirinto que ele reserva: as partes do {@code TileEldritchAltar} que dependem dos
 * cultistas, do guardião e das Terras de Fora.
 */
public final class EldritchAltarSpawns {
    private EldritchAltarSpawns() {
    }

    /**
     * O {@code spawnClerics}: um clérigo em cada quina do altar (a dois blocos), no ritual e com o altar por casa. Com
     * mais de dois de pé, o altar passa a chamar cavaleiros.
     */
    static void clerics(ServerLevel level, BlockPos pos, EldritchAltarBlockEntity altar) {
        int success = 0;
        int[][] corners = {{-2, -2}, {-2, 2}, {2, -2}, {2, 2}};
        for (int[] c : corners) {
            CultistClericEntity cleric = TCEntities.CULTIST_CLERIC.create(level, EntitySpawnReason.STRUCTURE);
            if (cleric == null) continue;
            BlockPos floor = pos.offset(c[0], -1, c[1]);
            if (!level.getBlockState(floor).isFaceSturdy(level, floor, net.minecraft.core.Direction.UP)) continue;
            cleric.setPos(pos.getX() + 0.5 + c[0], pos.getY(), pos.getZ() + 0.5 + c[1]);
            if (level.noCollision(cleric) && !level.containsAnyLiquid(cleric.getBoundingBox())) {
                cleric.setHomeTo(pos, 8);
                cleric.finalizeSpawn(level, level.getCurrentDifficultyAt(cleric.blockPosition()), EntitySpawnReason.STRUCTURE, null);
                cleric.spawnAnim();
                if (level.addFreshEntity(cleric)) {
                    success++;
                    cleric.setRitualist(true);
                }
            }
        }
        if (success > 2) altar.setSpawnedClerics(true);
    }

    /**
     * O {@code spawnGuards}: sem clérigo a até vinte e quatro blocos, o altar para de chamar; com menos de oito cultistas
     * por perto, um cavaleiro nasce a quatro a dez blocos dele.
     */
    static void guards(ServerLevel level, BlockPos pos, EldritchAltarBlockEntity altar) {
        AABB area = new AABB(pos).inflate(24.0, 16.0, 24.0);
        if (level.getEntitiesOfClass(CultistClericEntity.class, area).isEmpty()) {
            altar.setSpawner(false);
            return;
        }
        if (level.getEntitiesOfClass(CultistEntity.class, area).size() >= 8) return;
        CultistKnightEntity knight = TCEntities.CULTIST_KNIGHT.create(level, EntitySpawnReason.STRUCTURE);
        if (knight == null) return;
        var random = level.getRandom();
        int i1 = pos.getX() + Mth.nextInt(random, 4, 10) * Mth.nextInt(random, -1, 1);
        int j1 = pos.getY() + Mth.nextInt(random, 0, 3) * Mth.nextInt(random, -1, 1);
        int k1 = pos.getZ() + Mth.nextInt(random, 4, 10) * Mth.nextInt(random, -1, 1);
        BlockPos floor = new BlockPos(i1, j1 - 1, k1);
        if (!level.getBlockState(floor).isFaceSturdy(level, floor, net.minecraft.core.Direction.UP)) return;
        knight.setPos(i1, j1, k1);
        if (level.isUnobstructed(knight) && level.noCollision(knight) && !level.containsAnyLiquid(knight.getBoundingBox())) {
            knight.finalizeSpawn(level, level.getCurrentDifficultyAt(knight.blockPosition()), EntitySpawnReason.STRUCTURE, null);
            knight.spawnAnim();
            knight.setHomeTo(pos, 16);
            level.addFreshEntity(knight);
        }
    }

    /** O {@code spawnGuardian}: um guardião eldritch. Chega com o guardião. */
    static void guardian(ServerLevel level, BlockPos pos, EldritchAltarBlockEntity altar) {
    }

    /** O {@code checkForMaze}. Chega com as Terras de Fora. */
    static boolean checkForMaze(Level level, BlockPos pos, int w, int h) {
        return false;
    }
}
