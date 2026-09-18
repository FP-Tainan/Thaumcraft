package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.aspects.ObjectAspects;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;

/** As coisas do próprio mod têm os aspectos do {@code ConfigAspects} original. */
public class ModAspectsGameTest {
    @GameTest
    public void theModObjectsHaveTheirAspects(GameTestHelper helper) {
        var shard = ObjectAspects.of(TCItems.SHARDS.get("fire"));
        if (shard.getAmount(Aspects.FIRE) != 2 || shard.getAmount(Aspects.MAGIC) != 1) {
            helper.fail("o fragmento de fogo é ignis 2, praecantatio 1 e vitreus; veio " + shard);
        }
        var log = ObjectAspects.of(TCBlocks.SILVERWOOD_LOG.asItem());
        if (log.getAmount(Aspects.TREE) != 3 || log.getAmount(Aspects.ORDER) != 1) {
            helper.fail("a tora de pinheiro-de-prata é arbor 3, praecantatio 1 e ordo 1; veio " + log);
        }
        var fibres = ObjectAspects.of(TCBlocks.TAINT_FIBRES.asItem());
        if (fibres.getAmount(Aspects.LIFE) != 1) helper.fail("vale a primeira anotação das fibras; veio " + fibres);
        if (ObjectAspects.of(TCBlocks.SHIMMERLEAF.asItem()).isEmpty()) helper.fail("a folha-cintilante tem aspecto");
        helper.succeed();
    }
}
