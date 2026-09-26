package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.EnderMan;
import net.thaumcraft.shattered.RiftBlockEntity;
import net.thaumcraft.shattered.RiftWalkers;
import net.thaumcraft.shattered.ShatteredBlocks;

/**
 * Os endermans e as fendas.
 *
 * <p>A ideia de quem joga: são eles que atravessam os rasgões, e é por isso que andam entre os mundos. Aqui
 * confere-se o que dá para conferir sem esperar pelo acaso — quem conta como andarilho do Véu, e que uma fenda
 * crescida põe um cá fora quando a sorte calha.
 */
public class RiftWalkersGameTest {
    /** Ao pé de uma fenda, um enderman é dos que andam no Véu; longe dela, não. */
    @GameTest
    public void theOnesByARiftWalkTheVeil(GameTestHelper helper) {
        BlockPos fenda = new BlockPos(1, 2, 1);
        helper.setBlock(fenda, ShatteredBlocks.RIFT);

        EnderMan perto = EntityTypes.ENDERMAN.create(helper.getLevel(), EntitySpawnReason.EVENT);
        perto.snapTo(helper.absoluteVec(new net.minecraft.world.phys.Vec3(2.5, 2.0, 1.5)), 0.0f, 0.0f);
        helper.getLevel().addFreshEntity(perto);
        if (!RiftWalkers.walksTheVeil(perto)) helper.fail("este estava ao pé de uma fenda");

        helper.getLevel().removeBlock(helper.absolutePos(fenda), false);
        if (RiftWalkers.walksTheVeil(perto)) helper.fail("e este já não tem fenda nenhuma por perto");

        perto.discard();
        helper.succeed();
    }

    /** Uma fenda pequena não põe ninguém cá fora, por mais voltas que se dê. */
    @GameTest
    public void theSmallRiftSendsNobody(GameTestHelper helper) {
        BlockPos onde = new BlockPos(1, 2, 1);
        helper.setBlock(onde, ShatteredBlocks.RIFT);
        var fenda = helper.getBlockEntity(onde, RiftBlockEntity.class);
        if (fenda.size() >= RiftWalkers.EMERGE_SIZE) helper.fail("a fenda nasce pequena");

        for (int volta = 0; volta < 4000; volta++) {
            RiftWalkers.maybeEmerge(helper.getLevel(), helper.absolutePos(onde), fenda);
        }
        // só a casa da fenda e o que lhe encosta: os testes correm lado a lado, e um olhar largo apanhava os
        // endermans do teste do lado
        if (!helper.getLevel().getEntitiesOfClass(EnderMan.class, perto(helper, onde)).isEmpty()) {
            helper.fail("uma fenda daquele tamanho não põe ninguém cá fora");
        }
        helper.succeed();
    }

    /** E uma já crescida põe, e depois pára quando a vizinhança enche. */
    @GameTest
    public void theGrownRiftSendsThemOutAndThenStops(GameTestHelper helper) {
        BlockPos onde = new BlockPos(1, 2, 1);
        helper.setBlock(onde, ShatteredBlocks.RIFT);
        var fenda = helper.getBlockEntity(onde, RiftBlockEntity.class);
        // cresce-a até passar da conta, como o tique faz sozinho ao fim de um bom bocado
        while (fenda.size() < RiftWalkers.EMERGE_SIZE) fenda.grow();

        var perto = perto(helper, onde);
        for (int volta = 0; volta < 400000; volta++) {
            RiftWalkers.maybeEmerge(helper.getLevel(), helper.absolutePos(onde), fenda);
        }
        int quantos = helper.getLevel().getEntitiesOfClass(EnderMan.class, perto).size();
        if (quantos == 0) helper.fail("com tantas voltas, algum havia de ter saído");
        if (quantos > RiftWalkers.CROWD) helper.fail("e a fenda pára nos " + RiftWalkers.CROWD + "; achei " + quantos);

        for (EnderMan quem : helper.getLevel().getEntitiesOfClass(EnderMan.class, perto)) quem.discard();
        helper.succeed();
    }

    /** A casa da fenda e o que lhe encosta, e nada mais: os testes correm com vizinhos à vista. */
    private static net.minecraft.world.phys.AABB perto(GameTestHelper helper, BlockPos onde) {
        return new net.minecraft.world.phys.AABB(helper.absolutePos(onde)).inflate(2.5);
    }
}
