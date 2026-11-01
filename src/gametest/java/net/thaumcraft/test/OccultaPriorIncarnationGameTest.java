package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.mixin.ItemEntityAccessor;
import net.thaumcraft.occulta.rite.PriorIncarnation;
import net.thaumcraft.occulta.rite.RiteRegistry;

import java.util.List;
import java.util.Optional;

/**
 * A <b>Encarnação Anterior</b>: o rito que devolve o que sumiu.
 *
 * <p>A prova que carrega a fatia é a do <b>que sumiu de velho</b>. O rito não é a parte difícil — a parte
 * difícil são os três ganchos que, muito antes dele, decidem o que é seu: o carimbo na morte, a lista
 * quando a coisa some, e o carimbo que sai quando alguém a apanha. Errando qualquer um dos três, o rito
 * acende e não devolve nada, e não há como saber por quê.
 */
public class OccultaPriorIncarnationGameTest {
    private static void piso(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 0; x < 7; x++) {
            for (int z = 0; z < 7; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** <b>O rito está no livro</b>, com o anel de dentro em giz infernal e sem cobrar poder. */
    @GameTest(maxTicks = 20)
    public void theRiteIsInTheBook(GameTestHelper helper) {
        RiteRegistry.Entry rito = RiteRegistry.all().stream()
                .filter(qual -> qual.key().equals("tc.rite.priorincarnation")).findFirst().orElse(null);
        if (rito == null) {
            helper.fail("o rito devia estar no livro");
            return;
        }
        if (rito.inner().infernal() != 16) {
            helper.fail("o anel de dentro tem dezesseis glifos infernais");
            return;
        }
        if (rito.inner().ritual() != 0 || rito.inner().otherwhere() != 0) {
            helper.fail("e só infernais");
            return;
        }
        if (!rito.middle().empty() || !rito.outer().empty()) helper.fail("e os outros dois não entram");
        // é o único do mod que não cobra poder: o preço dele são as cinco coisas
        if (rito.sacrifice() instanceof net.thaumcraft.occulta.rite.Sacrifice.Both) {
            helper.fail("ele não cobra poder nenhum");
        }
        helper.succeed();
    }

    /**
     * <b>O que sume de velho entra na lista</b>, e o que se apanha sai dela.
     *
     * <p>A prova envelhece a coisa à força e deixa o jogo a apagar, que é o caminho de verdade: é a batida
     * do item largado que a faz sumir, e é nela que o gancho está.
     */
    @GameTest(maxTicks = 60)
    public void whatRotsAwayIsKept(GameTestHelper helper) {
        piso(helper);
        var level = helper.getLevel();
        var quem = helper.makeMockServerPlayerInLevel();
        quem.snapTo(helper.absoluteVec(new Vec3(3.5, 2, 3.5)));

        ItemStack diamante = new ItemStack(Items.DIAMOND, 3);
        PriorIncarnation.carimba(diamante, quem);
        if (!quem.getUUID().equals(PriorIncarnation.dequem(diamante))) {
            helper.fail("o carimbo diz de quem é");
            return;
        }

        // apanhado, o carimbo sai
        ItemStack cópia = diamante.copy();
        PriorIncarnation.apanhada(cópia);
        if (PriorIncarnation.dequem(cópia) != null) {
            helper.fail("e apanhado ele sai");
            return;
        }

        // e sumindo de velho, a coisa entra na lista
        ItemEntity largado = new ItemEntity(level, helper.absolutePos(new BlockPos(3, 2, 3)).getX() + 0.5,
                helper.absolutePos(new BlockPos(3, 2, 3)).getY() + 0.5,
                helper.absolutePos(new BlockPos(3, 2, 3)).getZ() + 0.5, diamante);
        ((ItemEntityAccessor) largado).thaumcraft$age(PriorIncarnation.SOME_AOS - 1);
        level.addFreshEntity(largado);

        helper.runAfterDelay(5, () -> {
            if (largado.isAlive()) {
                helper.fail("ao fim do prazo a coisa some");
                return;
            }
            var tem = PriorIncarnation.de(quem).coisas();
            if (tem.size() != 1) {
                helper.fail("e entra na lista de quem a perdeu: a lista tem " + tem.size());
                return;
            }
            if (!tem.getFirst().is(Items.DIAMOND) || tem.getFirst().getCount() != 3) {
                helper.fail("com o que era: " + tem.getFirst());
                return;
            }
            if (PriorIncarnation.dequem(tem.getFirst()) != null) {
                helper.fail("e sem o carimbo, que já serviu");
                return;
            }
            helper.succeed();
        });
    }

    /**
     * <b>O rito devolve, e só perto de onde se morreu.</b>
     *
     * <p>Dezesseis blocos é a conta do original. Quem morrer longe faz o rito e não recebe nada — e a
     * lista fica, para quando ele voltar ao lugar certo.
     */
    @GameTest(maxTicks = 60)
    public void itGivesBackOnlyNearTheDeath(GameTestHelper helper) {
        piso(helper);
        var level = helper.getLevel();
        var quem = helper.makeMockServerPlayerInLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(3, 2, 3));
        quem.snapTo(helper.absoluteVec(new Vec3(3.5, 2, 3.5)));

        // morreu longe: o rito não devolve nada
        quem.setAttached(PriorIncarnation.DATA, new PriorIncarnation.Perdido(
                List.of(new ItemStack(Items.DIAMOND, 3)),
                Optional.of(GlobalPos.of(level.dimension(), meio.offset(100, 0, 0)))));
        if (PriorIncarnation.devolve(level, meio)) {
            helper.fail("morrendo longe, o rito não devolve nada");
            return;
        }
        if (PriorIncarnation.de(quem).coisas().isEmpty()) {
            helper.fail("e a lista fica, para quando ele voltar ao lugar certo");
            return;
        }

        // e morreu perto: devolve
        quem.setAttached(PriorIncarnation.DATA, new PriorIncarnation.Perdido(
                List.of(new ItemStack(Items.DIAMOND, 3)),
                Optional.of(GlobalPos.of(level.dimension(), meio.offset(4, 0, 0)))));
        if (!PriorIncarnation.devolve(level, meio)) {
            helper.fail("morrendo perto, devolve");
            return;
        }
        if (!PriorIncarnation.de(quem).coisas().isEmpty()) {
            helper.fail("e a lista esvazia-se");
            return;
        }

        helper.runAfterDelay(5, () -> {
            var largados = level.getEntitiesOfClass(ItemEntity.class,
                    new net.minecraft.world.phys.AABB(meio).inflate(3.0));
            if (largados.stream().noneMatch(qual -> qual.getItem().is(Items.DIAMOND))) {
                helper.fail("e os diamantes ficam no círculo");
                return;
            }
            // e o esqueleto com o nome dele
            var ossos = level.getEntitiesOfClass(net.minecraft.world.entity.monster.skeleton.Skeleton.class,
                    new net.minecraft.world.phys.AABB(meio).inflate(3.0));
            if (ossos.isEmpty()) {
                helper.fail("e fica um esqueleto no lugar");
                return;
            }
            if (ossos.getFirst().getCustomName() == null
                    || !ossos.getFirst().getCustomName().getString().equals(quem.getName().getString())) {
                helper.fail("com o nome de quem morreu");
                return;
            }
            helper.succeed();
        });
    }

    /** <b>Morrer esquece a morte anterior</b>: não se junta o espólio de dez mortes para um rito só. */
    @GameTest(maxTicks = 20)
    public void dyingForgetsTheDeathBefore(GameTestHelper helper) {
        var level = helper.getLevel();
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setAttached(PriorIncarnation.DATA, new PriorIncarnation.Perdido(
                List.of(new ItemStack(Items.DIAMOND)),
                Optional.of(GlobalPos.of(level.dimension(), BlockPos.ZERO))));

        if (!PriorIncarnation.morreu(quem)) {
            helper.fail("com o inventário caindo, há o que carimbar");
            return;
        }
        if (!PriorIncarnation.de(quem).coisas().isEmpty()) {
            helper.fail("e a lista da morte anterior some");
            return;
        }
        if (PriorIncarnation.de(quem).onde().isEmpty()) {
            helper.fail("e o lugar da nova fica guardado");
        }
        helper.succeed();
    }
}
