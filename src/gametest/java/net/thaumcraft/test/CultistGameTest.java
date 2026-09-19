package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.block.entity.eldritch.EldritchAltarBlockEntity;
import net.thaumcraft.entity.eldritch.CultistClericEntity;
import net.thaumcraft.event.Champions;
import net.thaumcraft.item.CultistArmorItem;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCItems;

/**
 * O Culto Carmesim e os campeões da 4.2.3.5: o equipamento de cada cultista, o ritual dos clérigos em volta do altar, o
 * nome do pretor, e o que o campeão ganha e faz.
 */
public class CultistGameTest {
    /** O cavaleiro nasce com a placa dos cultistas; o clérigo, com o robe. */
    @GameTest(maxTicks = 20)
    public void cultistsWearTheirArmor(GameTestHelper helper) {
        var level = helper.getLevel();
        var knight = TCEntities.CULTIST_KNIGHT.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        knight.setPos(helper.absoluteVec(new net.minecraft.world.phys.Vec3(1.5, 2, 1.5)));
        knight.finalizeSpawn(level, level.getCurrentDifficultyAt(knight.blockPosition()), EntitySpawnReason.SPAWN_ITEM_USE, null);
        if (!knight.getItemBySlot(EquipmentSlot.CHEST).is(TCItems.CULTIST_PLATE_CHESTPLATE)) helper.fail("o cavaleiro veste a placa");
        if (knight.getMainHandItem().isEmpty()) helper.fail("o cavaleiro tem espada");
        if (knight.getAttributeBaseValue(Attributes.MAX_HEALTH) != 36.0) helper.fail("o cavaleiro tem trinta e seis de vida");
        var cleric = TCEntities.CULTIST_CLERIC.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        cleric.setPos(helper.absoluteVec(new net.minecraft.world.phys.Vec3(1.5, 2, 1.5)));
        cleric.finalizeSpawn(level, level.getCurrentDifficultyAt(cleric.blockPosition()), EntitySpawnReason.SPAWN_ITEM_USE, null);
        if (!cleric.getItemBySlot(EquipmentSlot.HEAD).is(TCItems.CULTIST_ROBE_HELMET)) helper.fail("o clérigo veste o capuz");
        if (!(TCItems.CULTIST_ROBE_CHESTPLATE instanceof CultistArmorItem robe) || robe.visDiscount(null, null, null) != 1 || robe.getWarp(null, null) != 1) {
            helper.fail("o robe dá um de desconto e um de distorção");
        }
        helper.succeed();
    }

    /** O altar que chama: quatro clérigos nas quinas, no ritual; batendo num, ele larga o ritual. */
    @GameTest(maxTicks = 200)
    public void altarCallsTheRitualists(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos altarPos = helper.absolutePos(new BlockPos(3, 2, 3));
        for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++) {
            level.setBlockAndUpdate(altarPos.offset(x, -1, z), Blocks.STONE.defaultBlockState());
        }
        level.setBlockAndUpdate(altarPos, TCBlocks.ELDRITCH_ALTAR.defaultBlockState());
        var altar = (EldritchAltarBlockEntity) level.getBlockEntity(altarPos);
        altar.setSpawner(true);
        altar.setSpawnType((byte) 0);
        helper.succeedWhen(() -> {
            var clerics = level.getEntitiesOfClass(CultistClericEntity.class, new net.minecraft.world.phys.AABB(altarPos).inflate(4));
            if (clerics.size() < 3) helper.fail("o altar devia chamar os clérigos, chamou " + clerics.size());
            for (var cleric : clerics) {
                if (!cleric.isRitualist()) helper.fail("os clérigos do altar ficam no ritual");
                if (!cleric.getHomePosition().equals(altarPos)) helper.fail("o altar é a casa deles");
            }
            var first = clerics.getFirst();
            first.hurtServer(level, level.damageSources().generic(), 1.0f);
            if (first.isRitualist()) helper.fail("batido, larga o ritual");
            clerics.forEach(net.minecraft.world.entity.Entity::discard);
        });
    }

    /** O campeão: trinta de vida a mais, o nome do tipo, e o tipo de cada um age. */
    @GameTest(maxTicks = 20)
    public void championsGetBuffed(GameTestHelper helper) {
        var level = helper.getLevel();
        var zombie = EntityTypes.ZOMBIE.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        zombie.setPos(helper.absoluteVec(new net.minecraft.world.phys.Vec3(1.5, 2, 1.5)));
        float before = zombie.getMaxHealth();
        Champions.makeChampion(zombie, false);
        if (Champions.mod(zombie) == null) helper.fail("devia ser campeão");
        if (zombie.getMaxHealth() != before + 30.0f) helper.fail("o campeão ganha trinta de vida");
        if (zombie.getCustomName() == null) helper.fail("o campeão leva o nome do tipo");
        // o blindado corta o golpe a dezenove vinte e cinco avos
        zombie.setAttached(Champions.CHAMPION, Champions.Mod.ARMOR.ordinal());
        var player = helper.makeMockServerPlayerInLevel();
        float got = Champions.hurt(zombie, level.damageSources().playerAttack(player), 25.0f);
        if (Math.abs(got - 19.0f) > 0.01f) helper.fail("o blindado devia deixar dezenove de vinte e cinco, deixou " + got);
        // o vampiro se cura com o golpe que dá
        zombie.setAttached(Champions.CHAMPION, Champions.Mod.VAMPIRIC.ordinal());
        zombie.setHealth(10.0f);
        Champions.hurt(player, level.damageSources().mobAttack(zombie), 8.0f);
        if (zombie.getHealth() < 14.0f) helper.fail("o vampiro devia se curar com o golpe");
        zombie.discard();
        helper.succeed();
    }

    /** O pretor é sempre campeão, e o nome dele leva o título e o tipo. */
    @GameTest(maxTicks = 20)
    public void praetorIsAlwaysAChampion(GameTestHelper helper) {
        var level = helper.getLevel();
        var leader = TCEntities.CULTIST_LEADER.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        leader.setPos(helper.absoluteVec(new net.minecraft.world.phys.Vec3(1.5, 2, 1.5)));
        leader.finalizeSpawn(level, level.getCurrentDifficultyAt(leader.blockPosition()), EntitySpawnReason.SPAWN_ITEM_USE, null);
        level.addFreshEntity(leader);
        if (Champions.mod(leader) == null) helper.fail("o pretor é sempre campeão");
        if (leader.getCustomName() == null) helper.fail("o pretor tem título");
        if (!leader.getItemBySlot(EquipmentSlot.CHEST).is(TCItems.CULTIST_LEADER_CHESTPLATE)) helper.fail("o pretor veste a armadura de pretor");
        leader.discard();
        helper.succeed();
    }
}
