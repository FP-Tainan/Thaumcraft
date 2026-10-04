package net.thaumcraft.test;

import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.door.DoorKeys;
import net.thaumcraft.occulta.door.KeyringRecipe;
import net.thaumcraft.occulta.door.RowanDoorBlock;

/**
 * As <b>duas portas do ofício</b>, e a chave que nasce com uma delas.
 *
 * <p>A prova que carrega a fatia é a da <b>chave no bolso</b>: a porta de sorveira não abre para quem não a
 * tem, abre para quem a tem — e a procura é no <b>inventário inteiro</b>, não na mão. Uma chave de casa não
 * se leva na mão, e uma tranca que obrigasse a isso não seria uma tranca: seria um estorvo.
 */
public class OccultaDoorsGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Põe uma porta de sorveira em pé naquela casa e devolve onde a metade de baixo ficou. */
    private static GlobalPos porta(GameTestHelper helper, BlockPos onde) {
        ServerLevel level = helper.getLevel();
        BlockPos baixo = helper.absolutePos(onde);
        level.setBlockAndUpdate(baixo, OccultaBlocks.ROWAN_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, Direction.NORTH)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        level.setBlockAndUpdate(baixo.above(), OccultaBlocks.ROWAN_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, Direction.NORTH)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        return new GlobalPos(level.dimension(), baixo);
    }

    private static boolean aberta(GameTestHelper helper, GlobalPos qual) {
        return helper.getLevel().getBlockState(qual.pos()).getValue(DoorBlock.OPEN);
    }

    private static void toca(GameTestHelper helper, Player quem, BlockPos casa) {
        var feitio = helper.getLevel().getBlockState(casa);
        feitio.useWithoutItem(helper.getLevel(), quem,
                new BlockHitResult(Vec3.atCenterOf(casa), Direction.NORTH, casa, false));
    }

    /**
     * <b>A chave está no bolso, e a porta sabe.</b>
     *
     * <p>Sem ela a porta não se mexe; com ela, abre. E a chave não precisa de estar na mão: o original varre
     * o inventário inteiro de quem toca a porta.
     */
    @GameTest(maxTicks = 40)
    public void theRowanDoorOnlyOpensForItsOwnKey(GameTestHelper helper) {
        piso(helper);
        GlobalPos qual = porta(helper, new BlockPos(3, 2, 3));
        Player quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(3.5, 2.0, 2.5)));

        toca(helper, quem, qual.pos());
        if (aberta(helper, qual)) helper.fail("de mãos vazias a porta de sorveira não se mexe");

        // a chave de outra porta não serve
        GlobalPos outra = new GlobalPos(helper.getLevel().dimension(), qual.pos().offset(20, 0, 20));
        quem.getInventory().setItem(7, DoorKeys.chave(outra));
        toca(helper, quem, qual.pos());
        if (aberta(helper, qual)) helper.fail("nem a chave de outra porta");

        // e a dela serve, do fundo do inventário
        quem.getInventory().setItem(7, DoorKeys.chave(qual));
        toca(helper, quem, qual.pos());
        if (!aberta(helper, qual)) helper.fail("com a chave dela no bolso, abre");

        helper.succeed();
    }

    /**
     * E a chave vale pelas <b>duas metades</b>.
     *
     * <p>Ela sabe só uma casa — a de baixo —, e quem toca a porta toca onde quer. Sem esta conta, a porta
     * abriria pela cintura e não pela cabeça.
     */
    @GameTest(maxTicks = 40)
    public void theKeyWorksFromEitherHalf(GameTestHelper helper) {
        piso(helper);
        GlobalPos qual = porta(helper, new BlockPos(5, 2, 5));
        ServerLevel level = helper.getLevel();

        var emBaixo = level.getBlockState(qual.pos());
        var emCima = level.getBlockState(qual.pos().above());
        if (!DoorKeys.onde(level, qual.pos(), emBaixo).equals(qual)) {
            helper.fail("a metade de baixo é ela mesma");
        }
        if (!DoorKeys.onde(level, qual.pos().above(), emCima).equals(qual)) {
            helper.fail("e a de cima aponta para a de baixo");
        }

        Player quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(5.5, 2.0, 4.5)));
        quem.getInventory().setItem(3, DoorKeys.chave(qual));
        toca(helper, quem, qual.pos().above());
        if (!aberta(helper, qual)) helper.fail("tocada pela cabeça, abre na mesma");
        helper.succeed();
    }

    /**
     * <b>Quebrada sem a chave, ela não volta a ser porta.</b>
     *
     * <p>Ficam <b>vinte e quatro gravetos</b>, que é o original dizendo, sem uma linha de texto, que arrombar
     * uma porta destrói a porta.
     */
    @GameTest(maxTicks = 40)
    public void breakingItWithoutTheKeyLeavesSticks(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        if (RowanDoorBlock.GRAVETOS != 24) helper.fail("são vinte e quatro gravetos");

        GlobalPos qual = porta(helper, new BlockPos(2, 2, 6));
        Player quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(2.5, 2.0, 5.5)));

        var caixa = new net.minecraft.world.phys.AABB(qual.pos()).inflate(4.0);
        level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, caixa)
                .forEach(net.minecraft.world.entity.Entity::discard);

        level.getBlockState(qual.pos()).getBlock()
                .playerWillDestroy(level, qual.pos(), level.getBlockState(qual.pos()), quem);
        level.destroyBlock(qual.pos(), false);

        int gravetos = 0;
        int portas = 0;
        for (var caiu : level.getEntitiesOfClass(
                net.minecraft.world.entity.item.ItemEntity.class, caixa)) {
            if (caiu.getItem().is(Items.STICK)) gravetos += caiu.getItem().getCount();
            if (caiu.getItem().is(OccultaItems.ROWAN_DOOR)) portas++;
            caiu.discard();
        }
        if (gravetos != RowanDoorBlock.GRAVETOS) {
            helper.fail("ficam vinte e quatro gravetos, e ficaram " + gravetos);
        }
        if (portas != 0) helper.fail("e porta nenhuma");
        helper.succeed();
    }

    /** E quem tinha a chave leva a porta de volta. */
    @GameTest(maxTicks = 40)
    public void breakingItWithTheKeyGivesTheDoorBack(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        GlobalPos qual = porta(helper, new BlockPos(6, 2, 2));
        Player quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(6.5, 2.0, 1.5)));
        quem.getInventory().setItem(0, DoorKeys.chave(qual));

        var caixa = new net.minecraft.world.phys.AABB(qual.pos()).inflate(4.0);
        level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, caixa)
                .forEach(net.minecraft.world.entity.Entity::discard);

        level.getBlockState(qual.pos()).getBlock()
                .playerWillDestroy(level, qual.pos(), level.getBlockState(qual.pos()), quem);
        level.destroyBlock(qual.pos(), false);

        boolean voltou = false;
        for (var caiu : level.getEntitiesOfClass(
                net.minecraft.world.entity.item.ItemEntity.class, caixa)) {
            if (caiu.getItem().is(OccultaItems.ROWAN_DOOR)) voltou = true;
            caiu.discard();
        }
        if (!voltou) helper.fail("quem tinha a chave leva a porta");
        helper.succeed();
    }

    /**
     * O <b>chaveiro</b>: duas chaves numa argola, e cada porta uma vez só.
     *
     * <p>E a argola vale por todas: a porta não pergunta se é chave ou chaveiro, pergunta se ali dentro está
     * o lugar dela.
     */
    @GameTest(maxTicks = 20)
    public void theKeyringHoldsEachDoorOnce(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GlobalPos uma = new GlobalPos(level.dimension(), new BlockPos(10, 64, 10));
        GlobalPos outra = new GlobalPos(level.dimension(), new BlockPos(20, 64, 20));

        var mesa = CraftingInput.of(2, 1, List.of(DoorKeys.chave(uma), DoorKeys.chave(outra)));
        if (!KeyringRecipe.INSTANCE.matches(mesa, level)) helper.fail("duas chaves fazem um chaveiro");
        ItemStack argola = KeyringRecipe.INSTANCE.assemble(mesa);
        if (!argola.is(OccultaItems.DOOR_KEYRING)) helper.fail("e o que sai é um chaveiro");
        if (DoorKeys.doChaveiro(argola).size() != 2) {
            helper.fail("com as duas portas, e tem " + DoorKeys.doChaveiro(argola).size());
        }

        // a mesma chave outra vez não dobra nada
        var denovo = CraftingInput.of(2, 1, List.of(argola, DoorKeys.chave(uma)));
        if (!KeyringRecipe.INSTANCE.matches(denovo, level)) helper.fail("chaveiro mais chave também faz");
        ItemStack maior = KeyringRecipe.INSTANCE.assemble(denovo);
        if (DoorKeys.doChaveiro(maior).size() != 2) {
            helper.fail("e cada porta entra uma vez só, e tem " + DoorKeys.doChaveiro(maior).size());
        }

        // e a argola abre as duas
        if (!DoorKeys.abre(maior, uma) || !DoorKeys.abre(maior, outra)) {
            helper.fail("o chaveiro vale por todas as chaves que tem");
        }

        // uma chave sozinha não faz chaveiro nenhum
        var sozinha = CraftingInput.of(1, 1, List.of(DoorKeys.chave(uma)));
        if (KeyringRecipe.INSTANCE.matches(sozinha, level)) helper.fail("uma chave sozinha não faz argola");
        helper.succeed();
    }

    /** E a de amieiro é só uma porta: abre para quem a empurrar. */
    @GameTest(maxTicks = 20)
    public void theAlderDoorIsJustADoor(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(4, 2, 6));
        level.setBlockAndUpdate(onde, OccultaBlocks.ALDER_DOOR.defaultBlockState()
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        level.setBlockAndUpdate(onde.above(), OccultaBlocks.ALDER_DOOR.defaultBlockState()
                .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));

        Player quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(4.5, 2.0, 5.5)));
        toca(helper, quem, onde);
        if (!level.getBlockState(onde).getValue(DoorBlock.OPEN)) {
            helper.fail("a de amieiro abre para quem a empurrar");
        }
        helper.succeed();
    }
}
