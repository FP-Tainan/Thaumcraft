package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaEffects;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.brew.BrewActions;
import net.thaumcraft.occulta.brew.BrewModifiers;
import net.thaumcraft.occulta.brew.BrewRegistry;

/**
 * O <b>Cozimento de Tirar os Maus Efeitos</b>: o único que sai do caldeirão para <b>tirar</b>.
 *
 * <p>A prova que carrega a fatia é a da <b>Doença</b>. Ela é incurável como todas as que o ofício põe de
 * propósito, e este cozimento é a <b>única coisa no mod</b> que a tira — e, do terceiro grau para cima,
 * limpa também as poças que a espalham. Sem essa segunda metade, curar alguém da doença era curá-lo até ele
 * dar o passo seguinte.
 */
public class OccultaCureBrewGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 6; x++) {
            for (int z = 0; z < 6; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Uma vaca de pé no meio da arena, para ter em quem tirar as coisas. */
    private static LivingEntity vaca(GameTestHelper helper) {
        piso(helper);
        return helper.spawn(EntityTypes.COW, new BlockPos(3, 2, 3));
    }

    private static BrewModifiers comForça(int quanto) {
        BrewModifiers temperos = new BrewModifiers();
        temperos.strength = quanto;
        return temperos;
    }

    /** <b>O Leite Purificado está na panela</b>, com os números do original. */
    @GameTest(maxTicks = 20)
    public void theMilkIsInThePot(GameTestHelper helper) {
        var ação = BrewRegistry.of(OccultaItems.PURIFIED_MILK);
        if (!(ação instanceof BrewActions.Cure)) {
            helper.fail("o Leite Purificado devia ser o cozimento que cura: é " + ação);
            return;
        }
        if (ação.power() != 200) {
            helper.fail("ele pede duzentos de poder: pede " + ação.power());
            return;
        }
        if (!ação.isEffect()) helper.fail("e ele é um efeito, que gasta espaço");
        else helper.succeed();
    }

    /**
     * <b>Ele tira o que se cura e até o grau dele</b> — e deixa o que passa dele.
     *
     * <p>A força aqui não é quanto ele dá: é <b>até onde ele alcança</b>.
     */
    @GameTest(maxTicks = 20)
    public void itOnlyReachesItsOwnLevel(GameTestHelper helper) {
        LivingEntity quem = vaca(helper);
        quem.addEffect(new MobEffectInstance(MobEffects.POISON, 600, 0));
        quem.addEffect(new MobEffectInstance(MobEffects.WITHER, 600, 2));
        quem.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 0));

        BrewRegistry.of(OccultaItems.PURIFIED_MILK)
                .applyToEntity(helper.getLevel(), quem, comForça(1));

        if (quem.hasEffect(MobEffects.POISON)) {
            helper.fail("o veneno de grau um sai com a força um");
            return;
        }
        if (!quem.hasEffect(MobEffects.WITHER)) {
            helper.fail("e o definhamento de grau três fica, que passa da força dele");
            return;
        }
        if (!quem.hasEffect(MobEffects.REGENERATION)) {
            helper.fail("e o que é bom não se tira: ele só tira o que faz mal");
            return;
        }
        helper.succeed();
    }

    /** <b>E o que o leite não tira, ele não tira</b> — menos a Doença, que é a razão de ele existir. */
    @GameTest(maxTicks = 20)
    public void itLeavesWhatMilkCannotTake(GameTestHelper helper) {
        LivingEntity quem = vaca(helper);
        quem.addEffect(new MobEffectInstance(OccultaEffects.PARALYSIS, 600, 0));
        quem.addEffect(new MobEffectInstance(OccultaEffects.DISEASED, 600, 0));

        // com força um ele não alcança nem a paralisia nem a doença
        BrewRegistry.of(OccultaItems.PURIFIED_MILK)
                .applyToEntity(helper.getLevel(), quem, comForça(1));
        if (!quem.hasEffect(OccultaEffects.PARALYSIS) || !quem.hasEffect(OccultaEffects.DISEASED)) {
            helper.fail("com força um ele não tira o que o leite não tira");
            return;
        }

        // com força dois, a doença sai — e só ela
        BrewRegistry.of(OccultaItems.PURIFIED_MILK)
                .applyToEntity(helper.getLevel(), quem, comForça(2));
        if (quem.hasEffect(OccultaEffects.DISEASED)) {
            helper.fail("do segundo grau para cima ele tira a doença");
            return;
        }
        if (!quem.hasEffect(OccultaEffects.PARALYSIS)) {
            helper.fail("e a paralisia fica: a doença é a única exceção");
            return;
        }
        helper.succeed();
    }

    /**
     * <b>E do terceiro grau para cima ele limpa as poças.</b>
     *
     * <p>É a metade que faz a cura valer: a poça da doença é o que a espalha, e curar alguém de pé numa
     * delas sem a limpar é curá-lo até ele dar o passo seguinte.
     */
    @GameTest(maxTicks = 20)
    public void atTheThirdLevelItDriesThePuddles(GameTestHelper helper) {
        LivingEntity quem = vaca(helper);
        ServerLevel level = helper.getLevel();
        BlockPos poça = helper.absolutePos(new BlockPos(1, 2, 1));
        var deDoença = OccultaBlocks.DISEASE.defaultBlockState();

        level.setBlockAndUpdate(poça, deDoença);
        quem.addEffect(new MobEffectInstance(OccultaEffects.DISEASED, 600, 0));
        BrewRegistry.of(OccultaItems.PURIFIED_MILK).applyToEntity(level, quem, comForça(2));
        if (!level.getBlockState(poça).is(OccultaBlocks.DISEASE)) {
            helper.fail("com força dois ele cura a pessoa e deixa a poça");
            return;
        }

        quem.addEffect(new MobEffectInstance(OccultaEffects.DISEASED, 600, 0));
        BrewRegistry.of(OccultaItems.PURIFIED_MILK).applyToEntity(level, quem, comForça(3));
        if (level.getBlockState(poça).is(OccultaBlocks.DISEASE)) {
            helper.fail("e com três ele seca a poça também");
            return;
        }
        if (quem.hasEffect(OccultaEffects.DISEASED)) {
            helper.fail("e cura a pessoa na mesma");
            return;
        }
        helper.succeed();
    }
}
