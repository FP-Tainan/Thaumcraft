package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.symbol.DarkMarkEntity;
import net.thaumcraft.occulta.symbol.ProjectileSymbol;
import net.thaumcraft.occulta.symbol.Symbol;
import net.thaumcraft.occulta.symbol.Symbols;

import org.jetbrains.annotations.Nullable;

/**
 * Os <b>dois símbolos que faltavam</b>: o <b>Colloportus</b>, que troca uma porta pela do ofício, e o
 * <b>Morsmordre</b>, que deixa a <b>Marca Negra</b> no céu.
 */
public class OccultaDarkMarkGameTest {
    /** <b>Os dois estão no livro</b>, e o Morsmordre com o que o original lhe dá. */
    @GameTest(maxTicks = 20)
    public void bothSpellsAreInTheBook(GameTestHelper helper) {
        if (acha("colloportus") == null) helper.fail("o Colloportus está no livro");
        Symbol marca = acha("morsmordre");
        if (marca == null) {
            helper.fail("e o Morsmordre também");
            return;
        }
        // o Morsmordre é maldição, não é imperdoável, e destranca-se com um gole
        if (!marca.maldição) helper.fail("o Morsmordre é maldição");
        if (marca.imperdoável) helper.fail("e não é imperdoável — destranca-se com um gole");
        if (!"morsmordre".equals(marca.chave)) helper.fail("e a chave dele é o Cozimento de Alma do Medo");
        helper.succeed();
    }

    /** <b>O Colloportus troca a porta</b> por uma de Sorveira, e pega nas duas metades dela. */
    @GameTest(maxTicks = 20, padding = 8)
    public void colloportusSwapsTheDoorForARowanOne(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pé = helper.absolutePos(new BlockPos(3, 2, 3));
        level.setBlockAndUpdate(pé.below(), Blocks.STONE.defaultBlockState());
        var porta = Blocks.OAK_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, Direction.NORTH)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
        level.setBlockAndUpdate(pé, porta);
        level.setBlockAndUpdate(pé.above(), porta.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));

        var quem = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(3, 2, 5));
        // acerta-se a metade de cima de propósito: quem troca tem de ser a de baixo
        ((ProjectileSymbol) acha("colloportus")).aoBater(level, quem,
                new BlockHitResult(Vec3.atCenterOf(pé.above()), Direction.SOUTH, pé.above(), false), 1);

        boolean baixo = level.getBlockState(pé).is(OccultaBlocks.ROWAN_DOOR);
        boolean cima = level.getBlockState(pé.above()).is(OccultaBlocks.ROWAN_DOOR);
        quem.discard();
        level.setBlockAndUpdate(pé.above(), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(pé, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(pé.below(), Blocks.AIR.defaultBlockState());
        if (!baixo) helper.fail("a porta vira Porta de Sorveira");
        else if (!cima) helper.fail("e as duas metades dela");
        else helper.succeed();
    }

    /**
     * <b>A Marca Negra fica no céu</b>: não cai, não se mata, empurra o que anda perto e some quando o
     * tempo dela acaba.
     */
    @GameTest(maxTicks = 100, padding = 12)
    public void theDarkMarkHangsThereAndPushesWhatWalks(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos chão = helper.absolutePos(new BlockPos(5, 2, 3));
        level.setBlockAndUpdate(chão, Blocks.STONE.defaultBlockState());

        Vec3 alto = Vec3.atCenterOf(helper.absolutePos(new BlockPos(3, 6, 3)));
        DarkMarkEntity marca = DarkMarkEntity.põe(level, alto);
        if (marca == null) {
            helper.fail("a Marca Negra põe-se no mundo");
            return;
        }

        // nada lhe faz mal
        marca.hurtServer(level, level.damageSources().magic(), 1000.0f);
        if (!marca.isAlive()) helper.fail("não se mata uma Marca Negra");

        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(5, 3, 3));
        double longe = porco.distanceToSqr(alto.x, porco.getY(), alto.z);

        helper.runAfterDelay(40, () -> {
            double agora = porco.distanceToSqr(alto.x, porco.getY(), alto.z);
            boolean caiu = marca.getY() < alto.y - 0.5;
            boolean viva = marca.isAlive();

            // e o tempo dela acaba: adianta-se o relógio em vez de esperar cinco minutos
            marca.vividas(DarkMarkEntity.DURA);
            helper.runAfterDelay(10, () -> {
                boolean sumiu = !marca.isAlive();
                porco.discard();
                if (marca.isAlive()) marca.discard();
                level.setBlockAndUpdate(chão, Blocks.AIR.defaultBlockState());
                if (caiu) helper.fail("ela não cai");
                else if (!viva) helper.fail("e não some antes do tempo");
                else if (agora <= longe) helper.fail("ela empurra para longe o que anda perto");
                else if (!sumiu) helper.fail("e some quando o tempo dela acaba");
                else helper.succeed();
            });
        });
    }

    /** Acha um símbolo pelo nome dele. */
    private static @Nullable Symbol acha(String nome) {
        for (Symbol qual : Symbols.todos()) {
            if (qual.nome.equals(nome)) return qual;
        }
        return null;
    }
}
