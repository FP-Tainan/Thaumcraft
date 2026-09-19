package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.event.Champions;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCItems;

/** Os chefes das Terras de Fora (fatia 6.5): o construto sem cabeça, o frenesi do guardião-mor e a pérola do gigante. */
public class BossGameTest {
    private static Vec3 at(GameTestHelper helper, double x, double y, double z) {
        return helper.absoluteVec(new Vec3(x, y, z));
    }

    /** O golpe que mataria o construto arranca a cabeça e não passa; o seguinte, sim. */
    @GameTest(maxTicks = 20)
    public void golemLosesItsHead(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 0; x < 5; x++) for (int z = 0; z < 5; z++) helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE.defaultBlockState());
        var golem = TCEntities.ELDRITCH_GOLEM.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        golem.setPos(at(helper, 2.5, 1, 2.5));
        golem.setPersistenceRequired();
        level.addFreshEntity(golem);
        if (golem.getMaxHealth() < 250.0f) helper.fail("o construto tem duzentos e cinquenta de vida");
        golem.hurtServer(level, level.damageSources().generic(), 10000.0f);
        if (!golem.isAlive()) helper.fail("o primeiro golpe fatal só arranca a cabeça");
        if (!golem.isHeadless()) helper.fail("sem cabeça");
        if (golem.getEyeHeight() < 3.3f) helper.fail("sem cabeça, os olhos sobem para o pescoço");
        golem.discard();
        helper.succeed();
    }

    /** O guardião-mor sem escudo entra em frenesi e fica invulnerável; por onde anda, o campo sugador. */
    @GameTest(maxTicks = 40)
    public void wardenFrenzy(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 0; x < 5; x++) for (int z = 0; z < 5; z++) helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE.defaultBlockState());
        var warden = TCEntities.ELDRITCH_WARDEN.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        warden.setPos(at(helper, 2.5, 1, 2.5));
        warden.setPersistenceRequired();
        warden.setNoAi(false);
        level.addFreshEntity(warden);
        warden.hurtServer(level, level.damageSources().generic(), 5.0f);
        if (!warden.isInvulnerable()) helper.fail("sem escudo, o primeiro golpe põe em frenesi");
        helper.succeedWhen(() -> {
            if (level.getBlockStates(new AABB(warden.blockPosition()).inflate(1)).noneMatch(s -> s.is(TCBlocks.SAPPING_FIELD))) {
                helper.fail("o campo sugador em volta dos pés");
            }
            warden.discard();
        });
    }

    /** O guardião-mor nasce com dois terços da vida de escudo e um nome antigo. */
    @GameTest(maxTicks = 20)
    public void wardenShieldAndTitle(GameTestHelper helper) {
        var level = helper.getLevel();
        var warden = TCEntities.ELDRITCH_WARDEN.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        warden.setPos(at(helper, 2.5, 1, 2.5));
        warden.finalizeSpawn(level, level.getCurrentDifficultyAt(warden.blockPosition()), EntitySpawnReason.SPAWN_ITEM_USE, null);
        if (warden.getAbsorptionAmount() < 130.0f) helper.fail("o escudo de dois terços da vida, tem " + warden.getAbsorptionAmount());
        Champions.makeChampion(warden, false);
        warden.generateName();
        if (warden.getCustomName() == null) helper.fail("o guardião-mor leva nome e título");
        helper.succeed();
    }

    /** O último tentáculo gigante deixa a pérola primordial. */
    @GameTest(maxTicks = 40)
    public void giantTentacleDropsThePearl(GameTestHelper helper) {
        var level = helper.getLevel();
        var giant = TCEntities.TAINTACLE_GIANT.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        giant.setPos(at(helper, 2.5, 1, 2.5));
        giant.finalizeSpawn(level, level.getCurrentDifficultyAt(giant.blockPosition()), EntitySpawnReason.SPAWN_ITEM_USE, null);
        if (Champions.mod(giant) == null) helper.fail("o gigante nasce campeão");
        level.addFreshEntity(giant);
        giant.setHealth(1.0f);
        giant.hurtServer(level, level.damageSources().generic(), 5.0f);
        helper.succeedWhen(() -> {
            var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(giant.blockPosition()).inflate(8));
            if (drops.stream().noneMatch(i -> i.getItem().is(TCItems.PRIMORDIAL_PEARL))) helper.fail("a pérola primordial");
            drops.forEach(net.minecraft.world.entity.Entity::discard);
        });
    }
}
