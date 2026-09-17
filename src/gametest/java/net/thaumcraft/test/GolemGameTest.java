package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.api.golems.GolemTypes;
import net.thaumcraft.entity.GolemEntity;
import net.thaumcraft.registry.TCEntities;

/**
 * Os golens têm de ser os do Thaumcraft 4.2.3.5.
 *
 * <p>O que estas provas guardam: a tabela das matérias (que é gerada e não pode derivar do original), o
 * corpo do golem acertado pela matéria, e o serviço — o golem de núcleo de juntar tem de sair do lugar,
 * pegar o que está no chão e levar para o baú que o sino marcou.
 */
public class GolemGameTest {
    /** A tabela das matérias é a do original. */
    @GameTest
    public void theTableCameFromTheOriginal(GameTestHelper helper) {
        if (GolemTypes.ALL.size() != 8) helper.fail("o original tem oito matérias de golem");
        if (GolemTypes.CORES.length != 12) helper.fail("o original tem doze núcleos");

        // conferidos na mão contra o EnumGolemType da 4.2.3.5
        GolemTypes.Type straw = GolemTypes.of("straw");
        if (straw.health() != 10) helper.fail("o de palha tem dez de vida");
        if (straw.carry() != 1) helper.fail("o de palha carrega uma coisa só");
        if (straw.fireResist()) helper.fail("o de palha pega fogo, claro");

        GolemTypes.Type thaumium = GolemTypes.of("thaumium");
        if (thaumium.health() != 40) helper.fail("o de táumio tem quarenta de vida");
        if (thaumium.carry() != 32) helper.fail("o de táumio carrega trinta e duas");
        if (thaumium.upgrades() != 2) helper.fail("no de táumio cabem duas melhorias");
        if (!thaumium.fireResist()) helper.fail("o de táumio não pega fogo");

        if (!GolemTypes.of("clay").fireResist()) helper.fail("o de argila não pega fogo");
        if (GolemTypes.of("stone").armor() != 12) helper.fail("o de pedra aguenta doze");
        helper.succeed();
    }

    /** A matéria manda no corpo do golem. */
    @GameTest
    public void theMaterialShapesTheGolem(GameTestHelper helper) {
        GolemEntity golem = TCEntities.GOLEM.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        if (golem == null) {
            helper.fail("não deu para fazer um golem");
            return;
        }
        golem.setMaterial("thaumium");
        if (golem.getMaxHealth() != 40.0f) {
            helper.fail("o de táumio devia ter quarenta de vida, tem " + golem.getMaxHealth());
        }
        if (golem.room() != 32) helper.fail("o de táumio devia caber trinta e duas na mão");
        if (!golem.fireImmune()) helper.fail("o de táumio não pega fogo");

        golem.setMaterial("straw");
        if (golem.getMaxHealth() != 10.0f) helper.fail("o de palha devia ter dez de vida");
        if (golem.room() != 1) helper.fail("o de palha carrega uma coisa só");
        if (golem.getAttributeValue(Attributes.MOVEMENT_SPEED) != GolemTypes.of("straw").speed()) {
            helper.fail("o passo do de palha devia ser o da tabela");
        }
        golem.discard();
        helper.succeed();
    }

    /**
     * O golem de juntar faz o serviço dele.
     *
     * <p>Põe-se um baú, marca-se como a casa dele, joga-se coisa no chão, e ele tem de ir lá, pegar e
     * levar. Se qualquer parte do vaivém quebrar, a coisa nunca chega ao baú.
     */
    @GameTest(maxTicks = 600)
    public void aGatheringGolemFillsItsChest(GameTestHelper helper) {
        BlockPos chestAt = new BlockPos(1, 2, 1);
        helper.setBlock(chestAt, Blocks.CHEST);

        GolemEntity golem = helper.spawn(TCEntities.GOLEM, new BlockPos(4, 2, 4));
        golem.setMaterial("iron");
        golem.setCore("gather");
        golem.setHome(helper.absolutePos(chestAt));

        // e um punhado de coisa no chão, longe dele
        helper.spawnItem(Items.DIAMOND, 5.5f, 2.5f, 5.5f);

        helper.succeedWhen(() -> {
            if (!(helper.getBlockEntity(chestAt, net.minecraft.world.level.block.entity.ChestBlockEntity.class)
                    instanceof Container chest)) {
                helper.fail("o baú sumiu");
                return;
            }
            boolean found = false;
            for (int slot = 0; slot < chest.getContainerSize(); slot++) {
                if (chest.getItem(slot).is(Items.DIAMOND)) found = true;
            }
            if (!found) {
                helper.fail("o golem não levou o diamante para o baú; ele carrega "
                        + golem.carried() + " e a casa dele é " + golem.home());
            }
        });
    }
}
