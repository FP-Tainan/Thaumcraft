package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.block.entity.AlchemicalFurnaceBlockEntity;
import net.thaumcraft.block.entity.AlembicBlockEntity;
import net.thaumcraft.block.entity.JarBlockEntity;
import net.thaumcraft.block.entity.TubeBlockEntity;
import net.thaumcraft.registry.TCBlocks;

/**
 * A essência encanada tem de se comportar como no Thaumcraft 4.2.3.5.
 *
 * <p>São três coisas que não podem quebrar sem ninguém notar: o tamanho de cada recipiente, a regra da
 * sucção (quem puxa mais forte ganha, e o tubo perde um de força a cada peça) e o caminho inteiro, do
 * forno ao jarro.
 */
public class EssentiaGameTest {
    /** Cada recipiente cabe o que o original diz que cabe. */
    @GameTest
    public void eachVesselHoldsWhatTheOriginalHolds(GameTestHelper helper) {
        if (JarBlockEntity.CAPACITY != 64) helper.fail("o jarro do original guarda sessenta e quatro");
        if (AlembicBlockEntity.CAPACITY != 32) helper.fail("o alambique do original guarda trinta e dois");
        if (AlchemicalFurnaceBlockEntity.MAX_VIS != 50) helper.fail("o forno do original segura cinquenta");
        helper.succeed();
    }

    /**
     * O jarro puxa, o alambique não.
     *
     * <p>É a diferença que faz a tubulação andar num sentido só: o jarro sem rótulo puxa com trinta e
     * dois, com rótulo com sessenta e quatro, e o alambique com zero — porque quem enche o alambique é o
     * forno, empurrando de baixo.
     */
    @GameTest
    public void onlyTheJarPulls(GameTestHelper helper) {
        BlockPos at = new BlockPos(1, 1, 1);
        helper.setBlock(at, TCBlocks.JAR);
        JarBlockEntity jar = helper.getBlockEntity(at, JarBlockEntity.class);

        if (jar.getSuctionAmount(Direction.UP) != 32) {
            helper.fail("o jarro sem rótulo puxa com trinta e dois, puxou com "
                    + jar.getSuctionAmount(Direction.UP));
        }
        jar.setLabel(Aspects.FIRE);
        if (jar.getSuctionAmount(Direction.UP) != 64) {
            helper.fail("o jarro com rótulo puxa com sessenta e quatro");
        }
        if (jar.isConnectable(Direction.NORTH)) helper.fail("o jarro só se liga por cima");
        // e um jarro cheio para de puxar, senão ele engasgaria a linha
        jar.addToContainer(Aspects.FIRE, JarBlockEntity.CAPACITY);
        if (jar.getSuctionAmount(Direction.UP) != 0) helper.fail("jarro cheio não puxa mais");

        BlockPos other = new BlockPos(3, 1, 1);
        helper.setBlock(other, TCBlocks.ALEMBIC);
        AlembicBlockEntity alembic = helper.getBlockEntity(other, AlembicBlockEntity.class);
        if (alembic.getSuctionAmount(Direction.UP) != 0) helper.fail("o alambique não puxa nada");
        if (alembic.canInputFrom(Direction.UP)) helper.fail("o alambique não aceita nada pelo cano");
        if (alembic.canOutputTo(Direction.DOWN)) helper.fail("o alambique não solta nada por baixo");
        helper.succeed();
    }

    /** O rótulo fecha o jarro para um aspecto só. */
    @GameTest
    public void theLabelClosesTheJar(GameTestHelper helper) {
        BlockPos at = new BlockPos(1, 1, 1);
        helper.setBlock(at, TCBlocks.JAR);
        JarBlockEntity jar = helper.getBlockEntity(at, JarBlockEntity.class);
        jar.setLabel(Aspects.WATER);

        if (jar.addToContainer(Aspects.FIRE, 5) != 5) helper.fail("jarro de aqua não aceita ignis");
        if (jar.addToContainer(Aspects.WATER, 5) != 0) helper.fail("jarro de aqua aceita aqua");
        if (jar.amount() != 5) helper.fail("devia ter cinco dentro, tem " + jar.amount());
        // e com coisa dentro ele não troca de rótulo
        if (jar.setLabel(Aspects.FIRE)) helper.fail("jarro com aqua dentro não vira jarro de ignis");
        helper.succeed();
    }

    /**
     * O caminho inteiro: o forno faz, o alambique recolhe, o tubo leva e o jarro guarda.
     *
     * <p>Esta é a prova que importa. Ela monta a linha de verdade no mundo de teste — forno no chão,
     * alambique em cima, tubo ao lado e jarro embaixo do tubo — e deixa o jogo andar sozinho. Se qualquer
     * peça da conta da sucção quebrar, a essência para no meio do caminho e a prova falha.
     */
    @GameTest(maxTicks = 400)
    public void essentiaWalksFromFurnaceToJar(GameTestHelper helper) {
        BlockPos furnaceAt = new BlockPos(1, 1, 1);
        BlockPos alembicAt = furnaceAt.above();
        BlockPos tubeAt = alembicAt.north();
        BlockPos jarAt = tubeAt.below();

        helper.setBlock(furnaceAt, TCBlocks.ALCHEMICAL_FURNACE);
        helper.setBlock(alembicAt, TCBlocks.ALEMBIC);
        helper.setBlock(tubeAt, TCBlocks.TUBE);
        helper.setBlock(jarAt, TCBlocks.JAR);

        AlchemicalFurnaceBlockEntity furnace = helper.getBlockEntity(furnaceAt, AlchemicalFurnaceBlockEntity.class);
        // uma pedra tem terra dentro; e carvão para o fogo
        furnace.setItem(AlchemicalFurnaceBlockEntity.INPUT_SLOT, new ItemStack(Items.COBBLESTONE, 8));
        furnace.setItem(AlchemicalFurnaceBlockEntity.FUEL_SLOT, new ItemStack(Items.COAL, 4));

        helper.succeedWhen(() -> {
            JarBlockEntity jar = helper.getBlockEntity(jarAt, JarBlockEntity.class);
            Aspect held = jar.aspect();
            if (held == null || jar.amount() <= 0) {
                helper.fail("a essência não chegou ao jarro");
            }
        });
    }

    /** O tubo perde um de força a cada peça, que é o que dá alcance à tubulação. */
    @GameTest(maxTicks = 200)
    public void suctionFadesAlongTheTubes(GameTestHelper helper) {
        BlockPos jarAt = new BlockPos(1, 1, 1);
        helper.setBlock(jarAt, TCBlocks.JAR);
        BlockPos first = jarAt.above();
        BlockPos second = first.above();
        helper.setBlock(first, TCBlocks.TUBE);
        helper.setBlock(second, TCBlocks.TUBE);

        helper.succeedWhen(() -> {
            TubeBlockEntity near = helper.getBlockEntity(first, TubeBlockEntity.class);
            TubeBlockEntity far = helper.getBlockEntity(second, TubeBlockEntity.class);
            int close = near.getSuctionAmount(null);
            int away = far.getSuctionAmount(null);
            // o de baixo bebe a fome do jarro menos um; o de cima, a do de baixo menos um
            if (close != 31) helper.fail("o tubo colado no jarro devia puxar com trinta e um, puxa " + close);
            if (away != 30) helper.fail("o tubo seguinte devia puxar com trinta, puxa " + away);
        });
    }
}
