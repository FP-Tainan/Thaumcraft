package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.shattered.RiftBlockEntity;
import net.thaumcraft.shattered.ShatteredBlocks;
import net.thaumcraft.shattered.ShatteredItems;
import net.thaumcraft.shattered.VeilSight;

/**
 * Os Óculos do Véu e quem vê o quê.
 *
 * <p>A regra que quem joga pediu: as fendas que já estavam no mundo só aparecem a quem tem os óculos no rosto; o
 * que o thaumaturgo mesmo fez — a porta que assentou, a fenda que rasgou com a Assinatura — está sempre à vista.
 */
public class VeilSightGameTest {
    /** A fenda que ninguém fez é do mundo; a porta que alguém assentou não é. */
    @GameTest
    public void theWorldsRiftsAreNaturalAndOursAreNot(GameTestHelper helper) {
        BlockPos solta = new BlockPos(1, 2, 1);
        helper.setBlock(solta, ShatteredBlocks.RIFT);
        if (!helper.getBlockEntity(solta, RiftBlockEntity.class).natural()) {
            helper.fail("a fenda que ninguém fez é do mundo");
        }

        // assentar a porta pelo item dela, que é o que chama o setPlacedBy
        BlockPos chão = new BlockPos(3, 1, 3);
        helper.setBlock(chão, Blocks.STONE);
        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        var pilha = new ItemStack(ShatteredBlocks.OAK_DIMENSIONAL_DOOR);
        usa(helper, quem, pilha, chão, Direction.UP);
        BlockPos porta = chão.above();
        helper.assertBlockPresent(ShatteredBlocks.OAK_DIMENSIONAL_DOOR, porta);
        if (helper.getBlockEntity(porta, RiftBlockEntity.class).natural()) {
            helper.fail("a porta que alguém assentou não é do mundo");
        }
        helper.succeed();
    }

    /** Sem os óculos, a fenda do mundo é ar; com eles no rosto, aparece. E a nossa aparece sempre. */
    @GameTest
    public void theGogglesAreWhatOpensTheEye(GameTestHelper helper) {
        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        if (VeilSight.can(quem)) helper.fail("de cabeça descoberta ninguém enxerga o Véu");
        if (VeilSight.sees(quem, true)) helper.fail("e a fenda do mundo é ar para ele");
        if (!VeilSight.sees(quem, false)) helper.fail("mas a que ele fez está à vista");

        quem.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ShatteredItems.VEIL_GOGGLES));
        if (!VeilSight.can(quem)) helper.fail("com os Óculos do Véu, sim");
        if (!VeilSight.sees(quem, true)) helper.fail("e então a fenda do mundo aparece");

        // os da Descoberta sozinhos não bastam: é o Fio do Mundo neles que abre o olho
        quem.setItemSlot(EquipmentSlot.HEAD, new ItemStack(net.thaumcraft.registry.TCItems.GOGGLES));
        if (VeilSight.can(quem)) helper.fail("os da Descoberta sozinhos não enxergam o Véu");
        helper.succeed();
    }

    /** E os Óculos do Véu não perdem nada dos outros: continuam a revelar os nós de aura. */
    @GameTest
    public void theVeilGogglesStillRevealNodes(GameTestHelper helper) {
        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ShatteredItems.VEIL_GOGGLES));
        if (!net.thaumcraft.item.Revealing.can(quem)) {
            helper.fail("os Óculos do Véu são os da Descoberta melhorados, e revelam como eles");
        }
        helper.succeed();
    }

    /** A Porta Antiga: sem corpo, sem desenho de bloco, e só quem tem os óculos lhe pega. */
    @GameTest
    public void theAncientDoorIsOnlyThereForWhoSeesIt(GameTestHelper helper) {
        BlockPos baixo = new BlockPos(1, 2, 1);
        var porta = ShatteredBlocks.ANCIENT_DIMENSIONAL_DOOR.defaultBlockState()
                .setValue(net.minecraft.world.level.block.DoorBlock.FACING, Direction.SOUTH);
        helper.setBlock(baixo, porta);
        helper.setBlock(baixo.above(), porta.setValue(net.minecraft.world.level.block.DoorBlock.HALF,
                net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER));

        var estado = helper.getBlockState(baixo);
        if (estado.getRenderShape() != net.minecraft.world.level.block.RenderShape.INVISIBLE) {
            helper.fail("o bloco dela não se desenha: quem a desenha é o miolo");
        }
        if (!estado.getCollisionShape(helper.getLevel(), helper.absolutePos(baixo)).isEmpty()) {
            helper.fail("e não tem corpo: uma parede invisível seria uma armadilha");
        }

        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        var bateu = new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(baixo)), Direction.SOUTH,
                helper.absolutePos(baixo), false);
        estado.useWithoutItem(helper.getLevel(), quem, bateu);
        if (helper.getBlockState(baixo).getValue(net.minecraft.world.level.block.DoorBlock.OPEN)) {
            helper.fail("de cabeça descoberta ninguém lhe pega");
        }

        quem.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ShatteredItems.VEIL_GOGGLES));
        helper.getBlockState(baixo).useWithoutItem(helper.getLevel(), quem, bateu);
        if (!helper.getBlockState(baixo).getValue(net.minecraft.world.level.block.DoorBlock.OPEN)) {
            helper.fail("com os Óculos do Véu, sim");
        }
        helper.succeed();
    }

    private static void usa(GameTestHelper helper, Player quem, ItemStack coisa, BlockPos onde, Direction lado) {
        quem.setItemInHand(InteractionHand.MAIN_HAND, coisa);
        var alvo = new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(onde)), lado,
                helper.absolutePos(onde), false);
        coisa.useOn(new UseOnContext(helper.getLevel(), quem, InteractionHand.MAIN_HAND, coisa, alvo));
    }
}
