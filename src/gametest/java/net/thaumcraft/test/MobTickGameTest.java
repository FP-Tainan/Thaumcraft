package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;

import java.util.ArrayList;
import java.util.List;

/**
 * Todo bicho do mod tem de aguentar tiquear com a cabeça dele a funcionar.
 *
 * <p>Nascer não basta: os desejos só correm quando o bicho tiqueia, e é aí que ele vai ler o que precisa. O
 * Lacaio derrubava o servidor logo no primeiro tique porque o {@code TemptGoal} do jogo de hoje lê o alcance
 * num atributo — coisa que na 1.7.10 estava fixa no código do {@code EntityAITempt} — e ninguém lho tinha
 * declarado. Um bicho parado num teste nunca mostra isso; um bicho a andar mostra-o na hora.
 */
public class MobTickGameTest {
    /** Quantos tiques se deixa cada bicho andar. */
    private static final int TICKS = 10;

    @GameTest(maxTicks = 200)
    public void everyMobSurvivesItsOwnThinking(GameTestHelper helper) {
        // quem joga tem de estar por perto: há desejos que só acordam com gente à vista
        helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);

        List<String> nascidos = new ArrayList<>();
        List<Mob> bichos = new ArrayList<>();
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            var id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (!id.getNamespace().equals("thaumcraft")) continue;
            if (type.getCategory() == MobCategory.MISC) continue;

            Entity bicho = type.create(helper.getLevel(), EntitySpawnReason.COMMAND);
            if (!(bicho instanceof Mob mob)) continue;
            var onde = helper.absoluteVec(new net.minecraft.world.phys.Vec3(1.5, 2.0, 1.5));
            mob.snapTo(onde.x, onde.y, onde.z, 0.0f, 0.0f);
            mob.setNoGravity(true);
            helper.getLevel().addFreshEntity(mob);
            bichos.add(mob);
            nascidos.add(id.getPath());
        }
        if (nascidos.isEmpty()) helper.fail("não nasceu bicho nenhum: os nomes do mod mudaram?");

        // se algum desejo rebentar, o tique do mundo leva a exceção e o teste cai com ela
        helper.runAfterDelay(TICKS, () -> {
            bichos.forEach(Entity::discard);
            helper.succeed();
        });
    }

    /** E o Lacaio em particular: é ele quem tem o desejo do Cérebro no Espeto. */
    @GameTest(maxTicks = 100)
    public void theMinionWantsTheBrainOnAStick(GameTestHelper helper) {
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.item.ItemStack(net.thaumcraft.mortuorum.MortuorumItems.BRAIN_ON_A_STICK));

        var lacaio = helper.spawn(net.thaumcraft.mortuorum.MortuorumEntities.MINION, new BlockPos(1, 2, 1));
        double alcance = lacaio.getAttributeValue(
                net.minecraft.world.entity.ai.attributes.Attributes.TEMPT_RANGE);
        if (alcance != 10.0) helper.fail("o alcance do desejo é dez, como o do original: " + alcance);

        helper.runAfterDelay(20, () -> {
            lacaio.discard();
            helper.succeed();
        });
    }
}
