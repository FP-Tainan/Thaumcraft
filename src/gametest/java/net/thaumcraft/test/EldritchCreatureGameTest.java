package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.block.entity.eldritch.CrabSpawnerBlockEntity;
import net.thaumcraft.block.entity.eldritch.EldritchAltarBlockEntity;
import net.thaumcraft.entity.eldritch.EldritchCrabEntity;
import net.thaumcraft.entity.eldritch.EldritchGuardianEntity;
import net.thaumcraft.entity.eldritch.EldritchOrbEntity;
import net.thaumcraft.event.Champions;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCItems;

/**
 * Os de dentro das Terras de Fora (fatia 6.3): o caranguejo e o elmo dele, o zumbi habitado que estoura num caranguejo, a
 * abertura incrustada que os cospe, e o guardião com o orbe dele, chamado pelo altar.
 */
public class EldritchCreatureGameTest {
    private static Vec3 at(GameTestHelper helper, double x, double y, double z) {
        return helper.absoluteVec(new Vec3(x, y, z));
    }

    /** Com elmo, cinco de armadura e mais lento; batido até a metade da vida, o elmo quebra. */
    @GameTest(maxTicks = 20)
    public void crabLosesItsHelm(GameTestHelper helper) {
        var level = helper.getLevel();
        EldritchCrabEntity crab = TCEntities.ELDRITCH_CRAB.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        crab.setPos(at(helper, 1.5, 2, 1.5));
        level.addFreshEntity(crab);
        crab.setHelm(true);
        if (crab.getAttributeValue(Attributes.ARMOR) != 5.0) helper.fail("com elmo, cinco de armadura");
        if (crab.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) != 0.275) helper.fail("com elmo, mais lento");
        crab.hurtServer(level, level.damageSources().generic(), 4.0f);
        if (!crab.hasHelm()) helper.fail("uma pancada leve não quebra o elmo");
        crab.hurtServer(level, level.damageSources().generic(), 20.0f);
        if (crab.isAlive() && crab.hasHelm()) helper.fail("na metade da vida o elmo quebra");
        if (crab.canBeAffected(new net.minecraft.world.effect.MobEffectInstance(MobEffects.POISON, 20))) helper.fail("o caranguejo não se envenena");
        crab.discard();
        helper.succeed();
    }

    /** O zumbi habitado: placa dos cavaleiros e trinta de vida; morto, estoura e solta um caranguejo de elmo. */
    @GameTest(maxTicks = 60)
    public void inhabitedZombieBurstsIntoACrab(GameTestHelper helper) {
        var level = helper.getLevel();
        var zombie = TCEntities.INHABITED_ZOMBIE.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        zombie.setPos(at(helper, 1.5, 2, 1.5));
        zombie.finalizeSpawn(level, level.getCurrentDifficultyAt(zombie.blockPosition()), EntitySpawnReason.SPAWN_ITEM_USE, null);
        if (!zombie.getItemBySlot(EquipmentSlot.HEAD).is(TCItems.CULTIST_PLATE_HELMET)) helper.fail("o zumbi habitado usa o elmo dos cavaleiros");
        if (zombie.getMaxHealth() != 30.0f) helper.fail("o zumbi habitado tem trinta de vida");
        level.addFreshEntity(zombie);
        zombie.hurtServer(level, level.damageSources().generic(), 100.0f);
        helper.succeedWhen(() -> {
            var crabs = level.getEntitiesOfClass(EldritchCrabEntity.class, new AABB(zombie.blockPosition()).inflate(4));
            if (crabs.isEmpty()) helper.fail("do zumbi morto sai um caranguejo");
            if (!crabs.getFirst().hasHelm()) helper.fail("o caranguejo do zumbi sai de elmo");
            if (!zombie.isRemoved()) helper.fail("o zumbi some");
            crabs.forEach(net.minecraft.world.entity.Entity::discard);
        });
    }

    /** A abertura incrustada cospe um caranguejo sem elmo pela face para que está virada. */
    @GameTest(maxTicks = 20)
    public void crustedOpeningSpitsCrabs(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 3, 1));
        level.setBlockAndUpdate(pos, TCBlocks.CRUSTED_OPENING.defaultBlockState());
        var vent = (CrabSpawnerBlockEntity) level.getBlockEntity(pos);
        vent.setFacing(Direction.EAST);
        if (vent.isActivated()) helper.fail("sem ninguém por perto, a abertura fica quieta");
        vent.spawnCrab(level);
        var crabs = level.getEntitiesOfClass(EldritchCrabEntity.class, new AABB(pos).inflate(3));
        if (crabs.size() != 1) helper.fail("a abertura cospe um caranguejo");
        var crab = crabs.getFirst();
        if (crab.hasHelm()) helper.fail("o caranguejo da abertura sai sem elmo");
        if (crab.getX() < pos.getX() + 1) helper.fail("sai pela face leste");
        crab.discard();
        if (TCBlocks.CRUSTED_OPENING.defaultBlockState().getLightEmission() != 4) helper.fail("a abertura tem luz quatro");
        helper.succeed();
    }

    /** O orbe do guardião: dois terços do dano dele e fraqueza a quem estiver perto. */
    @GameTest(maxTicks = 80)
    public void guardianOrbWeakens(GameTestHelper helper) {
        var level = helper.getLevel();
        var guardian = TCEntities.ELDRITCH_GUARDIAN.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        guardian.setPos(at(helper, 1.5, 2, 1.5));
        guardian.setPersistenceRequired();
        level.addFreshEntity(guardian);
        if (guardian.getMaxHealth() != 50.0f) helper.fail("o guardião tem cinquenta de vida");
        var cow = EntityTypes.COW.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        cow.setPos(at(helper, 1.5, 2, 5.5));
        cow.setNoAi(true);
        level.addFreshEntity(cow);
        var orb = new EldritchOrbEntity(level, guardian);
        orb.setPos(at(helper, 1.5, 2.8, 3.5));
        orb.shoot(0.0, 0.0, 1.0, 1.0f, 0.0f);
        level.addFreshEntity(orb);
        helper.succeedWhen(() -> {
            if (!cow.hasEffect(MobEffects.WEAKNESS)) helper.fail("o orbe enfraquece quem pega");
            if (cow.getHealth() >= cow.getMaxHealth()) helper.fail("o orbe machuca");
            guardian.discard();
            cow.discard();
        });
    }

    /**
     * O altar que chama o guardião: nasce perto e fica preso a ele.
     *
     * <p>O altar tenta a cada quarenta tiques, e cada tentativa pode dar em nada — o lugar sorteado pode cair em
     * cima dele, na parede, ou onde o guardião não nasce. Em quatrocentos tiques são oito tentativas, e de vez em
     * quando as oito falhavam: o teste reprovava sem nada estar errado. Com mil e duzentos são quase trinta, e a
     * chance de todas falharem é de desprezar.
     */
    @GameTest(maxTicks = 1200)
    public void altarCallsTheGuardian(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos altarPos = helper.absolutePos(new BlockPos(3, 2, 3));
        for (int x = -11; x <= 11; x++) for (int z = -11; z <= 11; z++) {
            level.setBlockAndUpdate(altarPos.offset(x, -1, z), Blocks.STONE.defaultBlockState());
        }
        level.setBlockAndUpdate(altarPos, TCBlocks.ELDRITCH_ALTAR.defaultBlockState());
        var altar = (EldritchAltarBlockEntity) level.getBlockEntity(altarPos);
        altar.setSpawner(true);
        altar.setSpawnType((byte) 1);
        helper.succeedWhen(() -> {
            var guardians = level.getEntitiesOfClass(EldritchGuardianEntity.class, new AABB(altarPos).inflate(12),
                    g -> g.hasHome() && g.getHomePosition().equals(altarPos));
            if (guardians.isEmpty()) helper.fail("o altar devia chamar um guardião, preso a ele");
            guardians.forEach(net.minecraft.world.entity.Entity::discard);
        });
    }

    /** Os dois na lista dos campeões, o zumbi habitado com três de bônus. */
    @GameTest(maxTicks = 20)
    public void eldritchChampions(GameTestHelper helper) {
        var level = helper.getLevel();
        var zombie = TCEntities.INHABITED_ZOMBIE.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        zombie.setPos(at(helper, 1.5, 2, 1.5));
        Champions.makeChampion(zombie, false);
        if (Champions.mod(zombie) == null) helper.fail("o zumbi habitado pode ser campeão");
        var crab = TCEntities.ELDRITCH_CRAB.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        if (!crab.getType().builtInRegistryHolder().is(net.minecraft.tags.EntityTypeTags.ARTHROPOD)) helper.fail("o caranguejo é artrópode");
        if (!TCEntities.ELDRITCH_GUARDIAN.builtInRegistryHolder().is(net.minecraft.tags.EntityTypeTags.UNDEAD)) helper.fail("o guardião é morto-vivo");
        helper.succeed();
    }
}
