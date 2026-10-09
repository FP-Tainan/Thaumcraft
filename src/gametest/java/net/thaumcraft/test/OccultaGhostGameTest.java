package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.brazier.BrazierRecipes;
import net.thaumcraft.occulta.ghost.BansheeEntity;
import net.thaumcraft.occulta.ghost.PoltergeistEntity;
import net.thaumcraft.occulta.ghost.SpectreEntity;
import net.thaumcraft.occulta.ghost.SummonedUndeadEntity;
import net.thaumcraft.occulta.ghost.TouchOfDeath;

/**
 * Os três <b>fantasmas</b> do Braseiro: o <b>Espectro</b>, a <b>Banshee</b> e o <b>Poltergeist</b>.
 *
 * <p>A prova que carrega a fatia é a do <b>toque</b>. Os três fazem dano, e nenhum dos três faz dano por
 * número: o Espectro leva <b>quinze por cento da vida máxima</b> de quem toca e a Banshee <b>dez</b>, e os
 * dois levam isso <b>por fora da armadura</b>. Contra eles, uma couraça de netherita vale o mesmo que nada —
 * e vida a mais vale <b>menos</b> que nada, porque é dela que a conta tira.
 *
 * <p>E, do outro lado, eles levam <b>no máximo quinze por golpe</b>. Nenhuma arma os mata depressa. É o
 * desenho mais honesto do Witchery: um bicho de que se foge, não um bicho que se mata.
 */
public class OccultaGhostGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    // ------------------------------------------------------------------ os números

    /** Os números dos três são os do original. */
    @GameTest
    public void theirNumbersAreTheOriginals(GameTestHelper helper) {
        if (SummonedUndeadEntity.TETO != 15.0f) helper.fail("nenhum golpe passa de quinze");
        if (SummonedUndeadEntity.CALADOS != 3) helper.fail("e eles resmungam três vezes mais devagar");
        if (SummonedUndeadEntity.UM_EM_QUATRO != 4 || SummonedUndeadEntity.NO_MÁXIMO_METADE != 2) {
            helper.fail("o pó é um em quatro, e a Pilhagem só aperta até um em dois");
        }

        if (SpectreEntity.QUANTO != 0.15f || SpectreEntity.NO_MÍNIMO != 1.0f) {
            helper.fail("quinze por cento da vida do alvo, nunca menos de um");
        }
        if (SpectreEntity.COURAÇA != 2) helper.fail("e dois de armadura por cima da que ele vista");

        if (BansheeEntity.ALCANCE != 6.0) helper.fail("o grito pega a seis blocos");
        if (BansheeEntity.QUANTO != 0.1f) helper.fail("e leva um décimo da vida");
        if (BansheeEntity.PROCURA != 100 || BansheeEntity.GRITANDO != 20) {
            helper.fail("ela procura de cinco em cinco segundos e, gritando, de segundo em segundo");
        }

        if (PoltergeistEntity.PROCURA != 16.0 || PoltergeistEntity.MEXE != 3.0) {
            helper.fail("ele procura a dezesseis e mexe a três");
        }
        if (PoltergeistEntity.O_DONO != 8.0) helper.fail("e só abre baús com o dono a oito blocos");
        if (PoltergeistEntity.DE_CINCO_EM_CINCO != 100) helper.fail("de cinco em cinco segundos");
        helper.succeed();
    }

    // ------------------------------------------------------------------ o toque

    /**
     * <b>O toque do Espectro leva quinze por cento, e a armadura não o segura.</b>
     *
     * <p>Esta é a prova que carrega a fatia. O mesmo zumbi, pelado e de netherita da cabeça aos pés, perde
     * <b>exatamente o mesmo</b> — três de vinte. Não há equipamento no jogo que mude esse número.
     */
    @GameTest
    public void theSpectreTouchIgnoresArmour(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var espectro = helper.spawn(OccultaEntities.SPECTRE, new BlockPos(2, 2, 2));

        var pelado = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(3, 2, 2));
        pelado.setHealth(pelado.getMaxHealth());
        float tinha = pelado.getHealth();
        if (!espectro.doHurtTarget(level, pelado)) helper.fail("o toque devia pegar");
        float perdeuPelado = tinha - pelado.getHealth();
        float devia = pelado.getMaxHealth() * SpectreEntity.QUANTO;
        if (Math.abs(perdeuPelado - devia) > 0.01f) {
            helper.fail("quinze por cento de vinte são três; perdeu " + perdeuPelado);
        }

        var vestido = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(5, 2, 2));
        vestido.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.NETHERITE_HELMET));
        vestido.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.NETHERITE_CHESTPLATE));
        vestido.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.NETHERITE_LEGGINGS));
        vestido.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.NETHERITE_BOOTS));
        vestido.setHealth(vestido.getMaxHealth());
        vestido.invulnerableTime = 0;
        tinha = vestido.getHealth();
        if (!espectro.doHurtTarget(level, vestido)) helper.fail("e devia pegar no de netherita também");
        float perdeuVestido = tinha - vestido.getHealth();
        if (Math.abs(perdeuVestido - perdeuPelado) > 0.01f) {
            helper.fail("a armadura não devia valer nada: pelado " + perdeuPelado
                    + ", de netherita " + perdeuVestido);
        }

        espectro.discard();
        pelado.discard();
        vestido.discard();
        helper.succeed();
    }

    /**
     * <b>E vida a mais é pior.</b>
     *
     * <p>Um bicho de cem de vida perde quinze por toque; um de vinte perde três. Sete toques e os dois
     * morrem, e é essa a graça do número: o Espectro não é mais fácil para quem é mais forte.
     */
    @GameTest
    public void moreHealthIsWorseAgainstTheSpectre(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var espectro = helper.spawn(OccultaEntities.SPECTRE, new BlockPos(2, 2, 2));

        var grande = helper.spawn(EntityTypes.IRON_GOLEM, new BlockPos(5, 2, 5));
        grande.setHealth(grande.getMaxHealth());
        float tinha = grande.getHealth();
        espectro.doHurtTarget(level, grande);
        float perdeu = tinha - grande.getHealth();
        float devia = grande.getMaxHealth() * SpectreEntity.QUANTO;
        if (Math.abs(perdeu - devia) > 0.01f) {
            helper.fail("quinze por cento de cem são quinze; perdeu " + perdeu);
        }

        espectro.discard();
        grande.discard();
        helper.succeed();
    }

    /** <b>E o toque não pega em quem está invulnerável.</b> */
    @GameTest
    public void theTouchDoesNotReachTheInvulnerable(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var bicho = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(3, 2, 3));
        bicho.setInvulnerable(true);
        if (TouchOfDeath.toca(level, null, bicho, 10.0f)) helper.fail("não devia pegar");
        bicho.discard();
        helper.succeed();
    }

    // ------------------------------------------------------------------ o teto de quinze

    /** <b>Nenhum golpe passa de quinze</b>, venha de onde vier. */
    @GameTest
    public void nothingHurtsThemMoreThanFifteen(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var espectro = helper.spawn(OccultaEntities.SPECTRE, new BlockPos(2, 2, 2));
        espectro.setHealth(espectro.getMaxHealth());

        float tinha = espectro.getHealth();
        espectro.hurtServer(level, level.damageSources().magic(), 1000.0f);
        float perdeu = tinha - espectro.getHealth();
        if (perdeu <= 0.0f) helper.fail("ele é ferido");
        if (perdeu > SummonedUndeadEntity.TETO) helper.fail("mas nunca mais de quinze; perdeu " + perdeu);
        if (espectro.getHealth() < espectro.getMaxHealth() - SummonedUndeadEntity.TETO - 0.01f) {
            helper.fail("quarenta de vida e quinze por golpe são três golpes no mínimo");
        }

        espectro.discard();
        helper.succeed();
    }

    // ------------------------------------------------------------------ a Banshee

    /**
     * <b>O grito pega a seis blocos, e não a dez.</b>
     *
     * <p>É o bicho mais honesto do mod: correr resolve, lutar não. E a conta é uma só, de modo que a prova
     * é a mesma coisa duas vezes, com o alvo perto e longe.
     */
    @GameTest
    public void theScreamReachesSixBlocks(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var banshee = helper.spawn(OccultaEntities.BANSHEE, new BlockPos(1, 2, 1));
        BlockPos meio = helper.absolutePos(new BlockPos(1, 2, 1));

        var bicho = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(3, 2, 1));
        bicho.setHealth(bicho.getMaxHealth());
        banshee.setTarget(bicho);

        float tinha = bicho.getHealth();
        banshee.grita(level);
        float perdeu = tinha - bicho.getHealth();
        float devia = bicho.getMaxHealth() * BansheeEntity.QUANTO;
        if (Math.abs(perdeu - devia) > 0.01f) {
            helper.fail("um décimo de vinte são dois; perdeu " + perdeu);
        }
        if (!banshee.gritando()) helper.fail("e ela devia estar gritando");

        // e longe, nada
        bicho.setPos(meio.getX() + 0.5, meio.getY(), meio.getZ() + 10.5);
        bicho.setHealth(bicho.getMaxHealth());
        bicho.invulnerableTime = 0;
        tinha = bicho.getHealth();
        banshee.grita(level);
        if (bicho.getHealth() != tinha) helper.fail("a dez blocos, o grito não pega");
        if (banshee.gritando()) helper.fail("e ela se cala quando não há ninguém");

        banshee.discard();
        bicho.discard();
        helper.succeed();
    }

    /**
     * <b>E quem traz abafadores não ouve.</b>
     *
     * <p>A melhor piada do mod: o grito que fura netherita não fura duas almofadas de couro e lã.
     */
    @GameTest
    public void earmuffsStopTheScream(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var banshee = helper.spawn(OccultaEntities.BANSHEE, new BlockPos(1, 2, 1));
        var bicho = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(3, 2, 1));
        bicho.setItemSlot(EquipmentSlot.HEAD, new ItemStack(OccultaItems.EARMUFFS));
        bicho.setHealth(bicho.getMaxHealth());
        banshee.setTarget(bicho);

        if (!BansheeEntity.surdo(bicho)) helper.fail("com abafadores, ele é surdo");
        float tinha = bicho.getHealth();
        banshee.grita(level);
        if (bicho.getHealth() != tinha) helper.fail("e não perde vida nenhuma");
        if (!banshee.gritando()) helper.fail("mas ela grita de qualquer maneira — ele é que não ouve");

        banshee.discard();
        bicho.discard();
        helper.succeed();
    }

    /** <b>E ela não bate em ninguém: o dano de ataque dela é zero.</b> */
    @GameTest
    public void theBansheeNeverStrikes(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var banshee = helper.spawn(OccultaEntities.BANSHEE, new BlockPos(1, 2, 1));
        var bicho = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(2, 2, 1));
        bicho.setHealth(bicho.getMaxHealth());

        if (banshee.doHurtTarget(level, bicho)) helper.fail("ela não bate");
        if (bicho.getHealth() != bicho.getMaxHealth()) helper.fail("e nada lhe tira vida");

        banshee.discard();
        bicho.discard();
        helper.succeed();
    }

    // ------------------------------------------------------------------ o Poltergeist

    /**
     * <b>Ele nasce invisível, e a poção não acaba.</b>
     *
     * <p>A bandeira de invisível só é posta na batida seguinte à da poção — é o jogo que a acerta ao
     * andar, não a poção ao chegar —, e por isso esta prova espera duas.
     */
    @GameTest(maxTicks = 20)
    public void thePoltergeistIsNeverSeen(GameTestHelper helper) {
        piso(helper);
        var ele = helper.spawn(OccultaEntities.POLTERGEIST, new BlockPos(3, 2, 3));
        net.thaumcraft.occulta.Spawn.comOvo(helper.getLevel(), ele);
        var poção = ele.getEffect(net.minecraft.world.effect.MobEffects.INVISIBILITY);
        if (poção == null || !poção.isInfiniteDuration()) helper.fail("a poção é para sempre");
        helper.runAfterDelay(2, () -> {
            if (!ele.isInvisible()) helper.fail("e ele devia ser invisível");
            ele.discard();
            helper.succeed();
        });
    }

    /**
     * <b>Ele tira uma coisa do baú e a atira para fora — mas só com o dono por perto.</b>
     *
     * <p>Repare na ordem: sem dono, ele não mexe em baú nenhum. Não é um ladrão, é uma assombração
     * doméstica, e ela precisa de plateia.
     */
    @GameTest
    public void thePoltergeistEmptiesChestsOnlyForItsOwner(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(3, 2, 3));
        level.setBlockAndUpdate(onde, Blocks.CHEST.defaultBlockState());
        if (!(level.getBlockEntity(onde) instanceof ChestBlockEntity baú)) {
            helper.fail("o baú devia ter alma");
            return;
        }
        baú.setItem(0, new ItemStack(Items.DIAMOND, 4));
        baú.setChanged();

        var ele = helper.spawn(OccultaEntities.POLTERGEIST, new BlockPos(4, 2, 3));

        // sem dono, ele não toca no baú
        if (ele.oBaú(level)) helper.fail("sem dono por perto, ele não mexe em baú");
        if (baú.getItem(0).getCount() != 4) helper.fail("e nada sai de lá");

        ServerPlayer dono = helper.makeMockServerPlayerInLevel();
        dono.setPos(onde.getX() + 1.5, onde.getY(), onde.getZ() + 0.5);
        ele.quemChamou(dono.getUUID());

        if (!ele.oBaú(level)) helper.fail("com o dono a um bloco, ele mexe");
        if (baú.getItem(0).getCount() != 3) {
            helper.fail("devia ter saído um diamante; ficaram " + baú.getItem(0).getCount());
        }
        AABB roda = new AABB(onde).inflate(4.0);
        if (level.getEntitiesOfClass(ItemEntity.class, roda).isEmpty()) {
            helper.fail("e o diamante devia estar no chão");
        }
        if (ele.braço() <= 0) helper.fail("e o braço dele devia estar levantado");

        ele.discard();
        helper.succeed();
    }

    /** <b>E o que estiver no chão, ele chuta.</b> */
    @GameTest
    public void thePoltergeistKicksWhatIsOnTheGround(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(3, 2, 3));
        var largado = new ItemEntity(level, onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5,
                new ItemStack(Items.APPLE));
        largado.setDeltaMovement(0.0, 0.0, 0.0);
        level.addFreshEntity(largado);

        var ele = helper.spawn(OccultaEntities.POLTERGEIST, new BlockPos(4, 2, 3));
        ele.oQueEstáNoChão(level);
        if (largado.getDeltaMovement().lengthSqr() == 0.0) helper.fail("a maçã devia ter voado");
        if (ele.braço() <= 0) helper.fail("e o braço dele devia estar levantado");

        ele.discard();
        largado.discard();
        helper.succeed();
    }

    // ------------------------------------------------------------------ o Braseiro, que os chama

    /** As três receitas novas são as do original, e a do Poltergeist é a única que não pede altar. */
    @GameTest
    public void theThreeRecipesAreTheOriginals(GameTestHelper helper) {
        var espectro = BrazierRecipes.of("tc.brazier.spectre");
        var banshee = BrazierRecipes.of("tc.brazier.banshee");
        var poltergeist = BrazierRecipes.of("tc.brazier.poltergeist");
        var murchar = BrazierRecipes.of("tc.brazier.wilting");
        if (espectro == null || banshee == null || poltergeist == null || murchar == null) {
            helper.fail("as quatro deviam estar na lista");
            return;
        }
        if (BrazierRecipes.all().size() != 8) {
            helper.fail("e as oito do original; são " + BrazierRecipes.all().size());
        }
        if (espectro.burn() != BrazierRecipes.THIRTY_SECONDS
                || banshee.burn() != BrazierRecipes.THIRTY_SECONDS) {
            helper.fail("meio minuto cada uma");
        }
        if (poltergeist.burn() != BrazierRecipes.FORTY_FIVE_SECONDS) {
            helper.fail("e três quartos de minuto a do Poltergeist");
        }
        if (!espectro.power() || !banshee.power()) helper.fail("as duas primeiras pedem altar");
        if (poltergeist.power() || murchar.power()) {
            helper.fail("e a do Poltergeist e a do Murchar não pedem");
        }
        if (espectro.burnt() == null || banshee.burnt() == null || poltergeist.burnt() == null) {
            helper.fail("e as três fazem o que fazem quando acabam, não enquanto ardem");
        }

        // e as três coisas de cada uma
        if (!espectro.matches(java.util.List.of(
                new ItemStack(OccultaItems.WORMWOOD_SPRIG), new ItemStack(OccultaItems.BAT_WOOL),
                new ItemStack(OccultaItems.GRAVEYARD_DUST)))) {
            helper.fail("losna, lã de morcego e pó de cemitério");
        }
        if (!banshee.matches(java.util.List.of(
                new ItemStack(OccultaItems.WORMWOOD_SPRIG), new ItemStack(OccultaItems.CONDENSED_FEAR),
                new ItemStack(OccultaItems.GRAVEYARD_DUST)))) {
            helper.fail("losna, medo condensado e pó de cemitério");
        }
        if (!poltergeist.matches(java.util.List.of(
                new ItemStack(OccultaItems.WORMWOOD_SPRIG), new ItemStack(OccultaItems.REFINED_EVIL),
                new ItemStack(OccultaItems.FOCUSED_WILL)))) {
            helper.fail("losna, mal refinado e vontade concentrada");
        }
        helper.succeed();
    }

    /** <b>E a fogueira, quando acaba, põe o bicho no mundo — perto, e para ficar.</b> */
    @GameTest
    public void theFireLeavesAGhostBehind(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(3, 2, 3));

        var receita = BrazierRecipes.of("tc.brazier.spectre");
        if (receita == null || receita.burnt() == null) {
            helper.fail("a receita devia existir");
            return;
        }
        receita.burnt().onBurnt(level, onde);

        AABB roda = new AABB(onde).inflate(BrazierRecipes.CLOSE_MAX + 2);
        var apareceram = level.getEntitiesOfClass(SpectreEntity.class, roda);
        if (apareceram.isEmpty()) helper.fail("devia ter aparecido um espectro");
        for (var quem : apareceram) {
            if (!quem.isPersistenceRequired()) helper.fail("e ele devia ficar");
            if (!quem.apagado()) helper.fail("e nascer apagado");
            quem.discard();
        }
        for (var sobra : level.getEntitiesOfClass(PoltergeistEntity.class,
                new AABB(onde).inflate(BrazierRecipes.FAR_MAX + 2))) {
            sobra.discard();
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ a oitava receita

    /**
     * <b>O Murchar seca uma plantação e cura os mortos com o que lhe tirou.</b>
     *
     * <p>E guarda dois ao fazê-lo, que são oitocentos tiques a mais de fogueira: é a única das oito que se
     * alimenta do que faz.
     */
    @GameTest
    public void theWiltingDrainsCropsAndHealsTheDead(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(4, 3, 4));

        /*
         * A receita aponta um lugar ao acaso num quadrado de sete por sete; com o quadrado inteiro cheio de
         * trigo, qualquer ponta que ela aponte é trigo. Com cinco tiques, a altura dela é um abaixo daqui.
         */
        int raio = BrazierRecipes.WILT_RADIUS;
        BlockState trigo = Blocks.WHEAT.defaultBlockState()
                .setValue(CropBlock.AGE, 3);
        for (int dx = -raio; dx <= raio; dx++) {
            for (int dz = -raio; dz <= raio; dz++) {
                BlockPos pé = onde.offset(dx, -2, dz);
                level.setBlockAndUpdate(pé, Blocks.FARMLAND.defaultBlockState());
                level.setBlockAndUpdate(pé.above(), trigo);
            }
        }

        var morto = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(4, 3, 5));
        morto.setHealth(morto.getMaxHealth() / 2.0f);
        float tinha = morto.getHealth();

        var receita = BrazierRecipes.of("tc.brazier.wilting");
        if (receita == null) {
            helper.fail("a oitava devia estar na lista");
            return;
        }
        int guardou = receita.burning().onBurning(level, onde, BrazierRecipes.WILT_EVERY);
        if (guardou != BrazierRecipes.GUARDA) {
            helper.fail("devia ter guardado dois; guardou " + guardou);
        }

        int secadas = 0;
        for (int dx = -raio; dx <= raio; dx++) {
            for (int dz = -raio; dz <= raio; dz++) {
                var feitio = level.getBlockState(onde.offset(dx, -1, dz));
                if (feitio.is(Blocks.WHEAT) && feitio.getValue(CropBlock.AGE) == 2) secadas++;
            }
        }
        if (secadas != 1) helper.fail("uma planta, e uma só, devia ter recuado um passo; foram " + secadas);
        if (morto.getHealth() <= tinha) helper.fail("e o zumbi devia ter se curado");
        float curou = morto.getHealth() - tinha;
        float devia = morto.getMaxHealth() * BrazierRecipes.CURA;
        if (Math.abs(curou - devia) > 0.01f) {
            helper.fail("um décimo da vida dele; curou " + curou);
        }

        morto.discard();
        helper.succeed();
    }
}
