package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.thaumcraft.occulta.rite.ActiveRite;
import net.thaumcraft.occulta.rite.RiteRegistry;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Um rito a correr sobrevive a desligar o mundo: guarda-se o nome dele e quantos passos faltam, e a fila é
 * remontada ao voltar.
 */
public class OccultaRitePersistGameTest {
    /** O que se guarda de um rito a correr volta a ser o mesmo rito, no mesmo ponto. */
    @GameTest
    public void aRunningRiteComesBack(GameTestHelper helper) {
        RiteRegistry.Entry qual = RiteRegistry.all().stream()
                .filter(r -> r.key().equals("tc.rite.necrostone"))
                .findFirst().orElse(null);
        if (qual == null) {
            helper.fail("o rito da prova devia existir");
            return;
        }

        UUID quem = UUID.randomUUID();
        ActiveRite rito = new ActiveRite(qual.key(), qual.rite(), qual.steps(0), quem, 0);
        int total = rito.steps().size();
        rito.offer(new ItemStack(Items.BONE, 3), helper.absolutePos(new net.minecraft.core.BlockPos(1, 1, 1)));

        ActiveRite.Saved guardado = rito.save();
        if (!guardado.key().equals(qual.key())) helper.fail("o nome guarda-se");
        if (guardado.remaining() != total) helper.fail("e quantos passos faltam");
        if (guardado.starter().orElse(null) == null || !guardado.starter().get().equals(quem)) {
            helper.fail("e quem o começou");
        }

        ActiveRite volta = ActiveRite.load(guardado);
        if (volta == null) {
            helper.fail("e o rito devia voltar");
            return;
        }
        if (volta.steps().size() != total) helper.fail("com a fila do mesmo tamanho");
        if (volta.offered().size() != 1 || !volta.offered().getFirst().stack().is(Items.BONE)) {
            helper.fail("e o que se ofereceu volta com ele");
        }
        if (volta.offered().getFirst().stack().getCount() != 3) helper.fail("sem perder a conta");
        helper.succeed();
    }

    /** Um rito guardado no meio volta no meio, e não do começo. */
    @GameTest
    public void aHalfRunRiteComesBackHalfRun(GameTestHelper helper) {
        RiteRegistry.Entry qual = RiteRegistry.all().stream()
                .filter(r -> r.steps(0).size() > 1)
                .findFirst().orElse(null);
        if (qual == null) {
            helper.fail("devia haver um rito de mais de um passo");
            return;
        }

        ActiveRite rito = new ActiveRite(qual.key(), qual.rite(), qual.steps(0), null, 0);
        int total = rito.steps().size();
        rito.steps().removeFirst();

        ActiveRite volta = ActiveRite.load(rito.save());
        if (volta == null) {
            helper.fail("devia voltar");
            return;
        }
        if (volta.steps().size() != total - 1) {
            helper.fail("no mesmo ponto, e voltou com " + volta.steps().size() + " de " + total);
        }
        helper.succeed();
    }

    /** E um rito cujo nome já não exista é largado, em vez de estourar. */
    @GameTest
    public void aRiteThatNoLongerExistsIsDropped(GameTestHelper helper) {
        var inventado = new ActiveRite.Saved("tc.rite.que.nao.existe", 1, Optional.empty(), 0,
                List.of(), Optional.empty());
        if (ActiveRite.load(inventado) != null) helper.fail("rito que não existe não volta");

        // e um número de passos fora do que o rito tem também não volta
        RiteRegistry.Entry qual = RiteRegistry.all().getFirst();
        var torto = new ActiveRite.Saved(qual.key(), 999, Optional.empty(), 0, List.of(), Optional.empty());
        if (ActiveRite.load(torto) != null) helper.fail("nem uma fila maior do que o rito tem");
        helper.succeed();
    }
}
