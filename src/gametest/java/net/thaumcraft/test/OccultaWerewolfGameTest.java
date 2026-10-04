package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaEffects;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.wolf.Moon;
import net.thaumcraft.occulta.wolf.Silver;
import net.thaumcraft.occulta.wolf.WolfmanEntity;

/**
 * O Lobisomem: a prata que o fere, a lua que o faz, e o acônito que o segura.
 *
 * <p>A prova que carrega a fatia é a da <b>prata</b>. Um lobisomem que levasse dano normal seria só um zumbi
 * com mais vida; o que faz dele o que ele é são as oitenta pancadas que custa matá-lo sem a arma certa.
 */
public class OccultaWerewolfGameTest {
    private static void piso(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Os números do bicho são os do original. */
    @GameTest(maxTicks = 20)
    public void hisNumbersAreTheOriginals(GameTestHelper helper) {
        if (WolfmanEntity.SEM_PRATA != 1.0f) {
            helper.fail("o que não é prata tira um, tira " + WolfmanEntity.SEM_PRATA);
        }
        if (WolfmanEntity.PRATA_VEZES != 1.5f) helper.fail("a prata multiplica por 1,5");
        if (WolfmanEntity.PRATA_TETO != 15.0f) helper.fail("e o teto dela é quinze");
        if (WolfmanEntity.COURO_GROSSO != 10) helper.fail("e o couro grosso são dez de armadura");
        helper.succeed();
    }

    /**
     * <b>Sem prata, uma pancada tira um.</b>
     *
     * <p>Seja ela de que for: a prova bate com uma espada de diamante, que no jogo tira sete.
     */
    @GameTest(maxTicks = 40)
    public void withoutSilverEveryBlowTakesOne(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var lobo = helper.spawn(OccultaEntities.WOLFMAN, new BlockPos(3, 2, 3));
        float tinha = lobo.getHealth();

        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(2.5, 2, 3.5)));
        quem.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                new ItemStack(Items.DIAMOND_SWORD));

        lobo.hurtServer(level, level.damageSources().playerAttack(quem), 7.0f);
        float tirou = tinha - lobo.getHealth();
        if (tirou > WolfmanEntity.SEM_PRATA + 0.001f) {
            helper.fail("sem prata, sete de espada tiram um — tiraram " + tirou);
        }

        lobo.discard();
        helper.succeed();
    }

    /** E com prata tira a sério: vez e meia. */
    @GameTest(maxTicks = 40)
    public void withSilverItBitesForReal(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var lobo = helper.spawn(OccultaEntities.WOLFMAN, new BlockPos(3, 2, 3));
        float tinha = lobo.getHealth();

        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(2.5, 2, 3.5)));
        var espada = new ItemStack(OccultaItems.SILVER_SWORD);
        if (!espada.is(Silver.ARMAS)) {
            helper.fail("a espada de prata tem de estar na etiqueta das armas de prata");
            return;
        }
        quem.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, espada);

        lobo.hurtServer(level, level.damageSources().playerAttack(quem), 6.0f);
        float tirou = tinha - lobo.getHealth();
        if (tirou <= WolfmanEntity.SEM_PRATA + 0.001f) {
            helper.fail("com prata ele leva dano a sério, e levou " + tirou);
        }

        lobo.discard();
        helper.succeed();
    }

    /** O couro grosso: dez de armadura por cima, com o teto do jogo. */
    @GameTest(maxTicks = 20)
    public void heWearsThickHide(GameTestHelper helper) {
        piso(helper);
        var lobo = helper.spawn(OccultaEntities.WOLFMAN, new BlockPos(3, 2, 3));
        if (lobo.getArmorValue() < WolfmanEntity.COURO_GROSSO) {
            helper.fail("ele tem dez de armadura de si, e tem " + lobo.getArmorValue());
        }
        if (lobo.getArmorValue() > WolfmanEntity.ARMADURA_MÁXIMA) {
            helper.fail("e nunca passa do teto do jogo");
        }
        lobo.discard();
        helper.succeed();
    }

    /** Ele não apanha veneno: tira-o de si. */
    @GameTest(maxTicks = 80)
    public void poisonDoesNotStickToHim(GameTestHelper helper) {
        piso(helper);
        var lobo = helper.spawn(OccultaEntities.WOLFMAN, new BlockPos(3, 2, 3));
        lobo.addEffect(new MobEffectInstance(MobEffects.POISON, 600, 0));
        if (!lobo.hasEffect(MobEffects.POISON)) {
            helper.fail("o veneno entra");
            return;
        }
        helper.succeedWhen(() -> helper.assertTrue(!lobo.hasEffect(MobEffects.POISON),
                "e sai sozinho, de dois em dois segundos"));
    }

    /** A fase zero é a lua cheia, e a conta tem oito fases. */
    @GameTest(maxTicks = 20)
    public void phaseZeroIsTheFullMoon(GameTestHelper helper) {
        if (Moon.CHEIA != 0) helper.fail("a cheia é a fase zero");
        int fase = Moon.fase(helper.getLevel());
        if (fase < 0 || fase > 7) helper.fail("a lua tem oito fases, e deu " + fase);
        if (Moon.cheia(helper.getLevel()) != (fase == 0)) {
            helper.fail("e cheia quer dizer fase zero");
        }
        helper.succeed();
    }

    /**
     * O acônito segura a transformação.
     *
     * <p>A prova não espera a lua: ela pergunta ao aldeão com o efeito no corpo, e ele fica aldeão.
     */
    @GameTest(maxTicks = 60)
    public void wolfsbaneHoldsTheChange(GameTestHelper helper) {
        piso(helper);
        var aldeão = helper.spawn(OccultaEntities.WERE_VILLAGER, new BlockPos(3, 2, 3));
        aldeão.addEffect(new MobEffectInstance(OccultaEffects.WOLFSBANE, 1200, 0));
        if (!aldeão.hasEffect(OccultaEffects.WOLFSBANE)) {
            helper.fail("o acônito entra");
            return;
        }
        helper.succeedWhen(() -> helper.assertTrue(aldeão.isAlive(),
                "e com ele no corpo o aldeão continua aldeão"));
    }

    /** E o aldeão que vira guarda a profissão que tinha, para quando voltar. */
    @GameTest(maxTicks = 40)
    public void turningKeepsTheProfession(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var aldeão = helper.spawn(EntityTypes.VILLAGER, new BlockPos(3, 2, 3));
        var qualEra = aldeão.getVillagerData();

        WolfmanEntity.doAldeão(level, aldeão, false);
        if (aldeão.isAlive()) helper.fail("o aldeão sai");

        var lobos = level.getEntitiesOfClass(WolfmanEntity.class,
                new net.minecraft.world.phys.AABB(helper.absolutePos(new BlockPos(3, 2, 3))).inflate(3.0));
        if (lobos.isEmpty()) {
            helper.fail("e um lobisomem fica no lugar dele");
            return;
        }
        if (lobos.getFirst().profissão() == null) {
            helper.fail("que guarda a profissão que ele tinha");
        }
        if (!qualEra.equals(lobos.getFirst().profissão())) {
            helper.fail("e guarda a mesma");
        }
        lobos.forEach(l -> l.discard());
        helper.succeed();
    }
}
