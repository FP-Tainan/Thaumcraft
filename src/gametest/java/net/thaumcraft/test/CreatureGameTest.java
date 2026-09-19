package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.aspects.ObjectAspects;
import net.thaumcraft.entity.FireBatEntity;
import net.thaumcraft.entity.GiantBrainyZombieEntity;
import net.thaumcraft.entity.WispEntity;
import net.thaumcraft.item.CrystalEssenceItem;
import net.thaumcraft.item.FocusItem;
import net.thaumcraft.item.Focuses;
import net.thaumcraft.item.WispEssenceItem;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCItems;

/** As criaturas básicas têm de seguir o {@code EntityBrainyZombie}, o {@code EntityWisp} e o {@code EntityFireBat}. */
public class CreatureGameTest {
    @GameTest
    public void theAngryZombieIsTougher(GameTestHelper helper) {
        var zombie = helper.spawn(TCEntities.BRAINY_ZOMBIE, new BlockPos(1, 1, 1));
        if (zombie.getMaxHealth() != 25.0f) helper.fail("o zumbi zangado tem 25 de vida: " + zombie.getMaxHealth());
        if (zombie.getAttributeValue(Attributes.ATTACK_DAMAGE) != 5.0) helper.fail("e morde com cinco");
        if (zombie.getArmorValue() < 3) helper.fail("e tem três de armadura a mais: " + zombie.getArmorValue());
        if (zombie.getAttributeValue(Attributes.SPAWN_REINFORCEMENTS_CHANCE) != 0.0) helper.fail("e não chama reforços");
        helper.succeed();
    }

    @GameTest
    public void theFuriousZombieGrowsWithAnger(GameTestHelper helper) {
        GiantBrainyZombieEntity giant = helper.spawn(TCEntities.GIANT_BRAINY_ZOMBIE, new BlockPos(1, 1, 1));
        float before = giant.getBbHeight();
        if (Math.abs(before - 1.8f * 2.2f) > 0.01f) helper.fail("calmo, tem 1,8 vezes 2,2 de altura: " + before);
        giant.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 1.0f);
        if (Math.abs(giant.anger() - 1.1f) > 0.001f) helper.fail("cada golpe dá um décimo de raiva: " + giant.anger());
        if (giant.getBbHeight() <= before) helper.fail("e a raiva o faz crescer");
        helper.succeed();
    }

    @GameTest
    public void theWispLeavesItsEssence(GameTestHelper helper) {
        WispEntity wisp = helper.spawn(TCEntities.WISP, new BlockPos(1, 2, 1));
        wisp.setAspect(Aspects.FIRE);
        wisp.kill(helper.getLevel());
        helper.succeedWhen(() -> {
            var drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, wisp.getBoundingBox().inflate(3.0),
                    e -> e.getItem().is(TCItems.WISP_ESSENCE));
            if (drops.isEmpty()) throw helper.assertionException("o fogo-fátuo larga a essência etérea");
            Aspect aspect = CrystalEssenceItem.aspectOf(drops.get(0).getItem());
            if (aspect != Aspects.FIRE) throw helper.assertionException("do aspecto dele: " + aspect);
        });
    }

    @GameTest
    public void theEssenceCarriesTwoOfItsAspect(GameTestHelper helper) {
        AspectList tags = ObjectAspects.of(WispEssenceItem.of(Aspects.WATER));
        if (tags.getAmount(Aspects.WATER) != 2) helper.fail("dois do aspecto: " + tags.getAmount(Aspects.WATER));
        if (tags.getAmount(Aspects.AURA) != 2) helper.fail("e dois de aura: " + tags.getAmount(Aspects.AURA));
        if (WispEssenceItem.variants().size() != Aspects.all().size()) helper.fail("a aba traz uma de cada aspecto");
        helper.succeed();
    }

    @GameTest
    public void theFireBatShrugsOffFireAndBlasts(GameTestHelper helper) {
        FireBatEntity bat = helper.spawn(TCEntities.FIREBAT, new BlockPos(1, 2, 1));
        var sources = helper.getLevel().damageSources();
        if (bat.hurtServer(helper.getLevel(), sources.inFire(), 4.0f)) helper.fail("o fogo não o fere");
        if (bat.hurtServer(helper.getLevel(), sources.explosion(null, null), 4.0f)) helper.fail("nem a explosão");
        if (bat.getMaxHealth() != 5.0f) helper.fail("tem cinco de vida");
        if (!bat.fireImmune()) helper.fail("e não pega fogo");
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void aSummonedBatWithoutTargetFades(GameTestHelper helper) {
        FireBatEntity bat = helper.spawn(TCEntities.FIREBAT, new BlockPos(1, 2, 1));
        bat.setIsBatHanging(false);
        bat.setIsSummoned(true);
        if (bat.getAttributeValue(Attributes.ATTACK_DAMAGE) != 2.0) helper.fail("invocado morde com dois");
        helper.succeedWhen(() -> {
            if (bat.isAlive()) throw helper.assertionException("sem alvo, o invocado se desfaz");
        });
    }

    @GameTest(maxTicks = 40)
    public void theNineHellsFocusSendsABat(GameTestHelper helper) {
        for (int x = 0; x < 3; x++) for (int z = 0; z < 8; z++) helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE.defaultBlockState());
        var player = helper.makeMockServerPlayerInLevel();
        Vec3 spot = helper.absoluteVec(new Vec3(1.5, 1.0, 0.5));
        player.snapTo(spot.x, spot.y, spot.z, 0.0f, 8.0f);
        var cow = helper.spawn(EntityTypes.COW, new BlockPos(1, 1, 6));
        cow.setNoAi(true);
        // mira no meio da vaca
        player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, cow.getBoundingBox().getCenter());
        ItemStack wand = new ItemStack(TCItems.WAND);
        wand.set(TCComponents.WAND_FOCUS, "hellbat");
        AspectList vis = new AspectList();
        for (Aspect primal : Aspects.primals()) vis.add(primal, 2500);
        wand.set(TCComponents.WAND_VIS, vis);
        FocusItem focus = Focuses.on(wand);
        if (focus == null) throw helper.assertionException("o foco dos Nove Infernos devia estar registrado");
        if (!Focuses.tick(helper.getLevel(), player, wand, focus)) helper.fail("com a vaca na mira, o foco solta um morcego");
        if (Focuses.tick(helper.getLevel(), player, wand, focus)) helper.fail("um por segundo");
        AspectList left = wand.get(TCComponents.WAND_VIS);
        // a ponteira de ferro cobra dez por cento a mais
        if (2500 - left.getAmount(Aspects.FIRE) != 220) helper.fail("ignis 200: " + (2500 - left.getAmount(Aspects.FIRE)));
        if (2500 - left.getAmount(Aspects.AIR) != 110) helper.fail("aer 100: " + (2500 - left.getAmount(Aspects.AIR)));
        var bats = helper.getLevel().getEntities(TCEntities.FIREBAT, player.getBoundingBox().inflate(4.0), e -> true);
        if (bats.size() != 1) helper.fail("sai um morcego; saíram " + bats.size());
        FireBatEntity bat = bats.get(0);
        if (!bat.isSummoned() || bat.isBatHanging()) helper.fail("invocado e voando");
        if (bat.getTarget() != cow) helper.fail("atrás da vaca");
        helper.succeed();
    }

    @GameTest
    public void theBrainIsWolfFood(GameTestHelper helper) {
        ItemStack brain = new ItemStack(TCItems.ZOMBIE_BRAIN);
        var food = brain.get(net.minecraft.core.component.DataComponents.FOOD);
        if (food == null || food.nutrition() != 4) helper.fail("o cérebro enche quatro de fome");
        if (!brain.is(net.minecraft.tags.ItemTags.MEAT)) helper.fail("e é carne de lobo");
        AspectList tags = ObjectAspects.of(brain);
        if (tags.getAmount(Aspects.MIND) != 4) helper.fail("cognitio 4: " + tags.getAmount(Aspects.MIND));
        helper.succeed();
    }
}
