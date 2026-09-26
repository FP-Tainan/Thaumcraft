package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.thaumcraft.shattered.FabricBlocks;
import net.thaumcraft.shattered.ShatteredItems;
import net.thaumcraft.shattered.ShatteredRealms;
import net.thaumcraft.world.DynamicDimensions;

/** Os Reinos Fragmentados: os tecidos e o que eles fazem. */
public class ShatteredGameTest {
    /** A fenda come o mundo em volta e deixa Fio do Mundo, que é de onde o ramo todo começa. */
    @GameTest
    public void theRiftEatsTheWorldAndLeavesThread(GameTestHelper helper) {
        BlockPos fenda = new BlockPos(3, 3, 3);
        var level = helper.getLevel();
        // uma bola de pedra em volta da fenda
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    helper.setBlock(fenda.offset(dx, dy, dz), net.minecraft.world.level.block.Blocks.STONE);
                }
            }
        }
        helper.setBlock(fenda, net.thaumcraft.shattered.ShatteredBlocks.RIFT);

        // mordidas que cheguem para se ver o buraco e algum fio
        var absoluto = helper.absolutePos(fenda);
        for (int vez = 0; vez < 400; vez++) {
            net.thaumcraft.shattered.RiftDecay.bite(level, absoluto, level.getRandom());
        }
        boolean buraco = false;
        for (int dx = -2; dx <= 2 && !buraco; dx++) {
            for (int dy = -2; dy <= 2 && !buraco; dy++) {
                for (int dz = -2; dz <= 2 && !buraco; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    if (level.getBlockState(absoluto.offset(dx, dy, dz)).isAir()) buraco = true;
                }
            }
        }
        if (!buraco) helper.fail("a fenda devia ter comido alguma pedra");

        var fios = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                new net.minecraft.world.phys.AABB(absoluto).inflate(6.0),
                item -> item.getItem().is(ShatteredItems.WORLD_THREAD));
        if (fios.isEmpty()) helper.fail("e deixado algum Fio do Mundo");
        for (var fio : fios) fio.discard();

        // e não come o que é do ramo
        if (net.thaumcraft.shattered.RiftDecay.canDecay(
                net.thaumcraft.shattered.ShatteredBlocks.RIFT.defaultBlockState())) {
            helper.fail("a fenda não come outra fenda");
        }
        if (net.thaumcraft.shattered.RiftDecay.canDecay(
                net.thaumcraft.shattered.FabricBlocks.FABRIC.get(
                        net.minecraft.world.item.DyeColor.BLACK).defaultBlockState())) {
            helper.fail("nem o tecido");
        }
        if (net.thaumcraft.shattered.RiftDecay.canDecay(
                net.minecraft.world.level.block.Blocks.BEDROCK.defaultBlockState())) {
            helper.fail("nem a rocha-mãe");
        }
        helper.succeed();
    }

    /** O Monólito não se mata, não se empurra e nunca está vivo. */
    @GameTest
    public void theMonolithIsNotAlive(GameTestHelper helper) {
        var limbo = ShatteredRealms.limbo(helper.getLevel().getServer());
        if (limbo == null) {
            helper.fail("o Limbo devia abrir");
            return;
        }
        var lousa = net.thaumcraft.shattered.ShatteredEntities.MONOLITH.create(limbo,
                net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        if (lousa == null) {
            helper.fail("o Monólito devia nascer");
            return;
        }
        lousa.snapTo(8.5, 80.0, 8.5, 0.0f, 0.0f);
        limbo.addFreshEntity(lousa);

        if (!lousa.isInvulnerable()) helper.fail("ele é invulnerável");
        if (lousa.isPushable()) helper.fail("e não se empurra");
        if (!lousa.isDangerous()) helper.fail("e no Limbo ele é perigoso");
        if (lousa.face() != 0) helper.fail("nasce de olho fechado");

        // bater nele só o irrita
        lousa.hurtServer(limbo, lousa.damageSources().generic(), 10.0f);
        if (lousa.aggro() != 0 && lousa.face() == 0) helper.fail("bater nele abre-lhe o olho");

        lousa.discard();
        net.thaumcraft.world.DynamicDimensions.remove(helper.getLevel().getServer(), ShatteredRealms.LIMBO);
        helper.succeed();
    }

    /** O Limbo abre-se, tem chão de tecido eterno e terra de tecido desfiado por cima. */
    @GameTest
    public void theLimboIsMadeOfFabric(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var limbo = ShatteredRealms.limbo(server);
        if (limbo == null) {
            helper.fail("o Limbo devia abrir");
            return;
        }
        if (!ShatteredRealms.isOurs(limbo)) helper.fail("e ser um mundo do ramo");
        if (ShatteredRealms.isPocket(limbo)) helper.fail("mas não um bolso");

        // o chão: tudo até ao oitavo é tecido eterno
        BlockPos fundo = new BlockPos(8, 0, 8);
        if (!limbo.getBlockState(fundo).is(net.thaumcraft.shattered.FabricBlocks.ETERNAL)) {
            helper.fail("o fundo do Limbo é tecido eterno; achei " + limbo.getBlockState(fundo));
        }
        // e o relevo por cima é tecido desfiado
        int alto = limbo.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, 8, 8);
        if (alto <= net.thaumcraft.shattered.LimboChunkGenerator.FLOOR) {
            helper.fail("o relevo do Limbo fica acima do chão; ficou em " + alto);
        }
        if (!limbo.getBlockState(new BlockPos(8, alto - 1, 8)).is(net.thaumcraft.shattered.FabricBlocks.UNRAVELLED)) {
            helper.fail("e é de tecido desfiado");
        }

        net.thaumcraft.world.DynamicDimensions.remove(server, ShatteredRealms.LIMBO);
        helper.succeed();
    }

    /** O desfiar desce um degrau de cada vez, e não toca no que é do Limbo. */
    @GameTest
    public void theDecayGoesDownOneStepAtATime(GameTestHelper helper) {
        BlockPos onde = new BlockPos(2, 2, 2);
        var level = helper.getLevel();

        helper.setBlock(onde, net.minecraft.world.level.block.Blocks.STONE);
        net.thaumcraft.shattered.LimboDecay.decay(level, helper.absolutePos(onde));
        helper.assertBlockPresent(net.minecraft.world.level.block.Blocks.COBBLESTONE, onde);
        net.thaumcraft.shattered.LimboDecay.decay(level, helper.absolutePos(onde));
        helper.assertBlockPresent(net.minecraft.world.level.block.Blocks.GRAVEL, onde);
        net.thaumcraft.shattered.LimboDecay.decay(level, helper.absolutePos(onde));
        helper.assertBlockPresent(net.thaumcraft.shattered.FabricBlocks.UNRAVELLED, onde);
        // e daí não desce mais
        net.thaumcraft.shattered.LimboDecay.decay(level, helper.absolutePos(onde));
        helper.assertBlockPresent(net.thaumcraft.shattered.FabricBlocks.UNRAVELLED, onde);

        // o tecido eterno e as portas não se desfazem
        if (net.thaumcraft.shattered.LimboDecay.canDecay(
                net.thaumcraft.shattered.FabricBlocks.ETERNAL.defaultBlockState())) {
            helper.fail("o tecido eterno não se desfia");
        }
        if (net.thaumcraft.shattered.LimboDecay.canDecay(
                net.thaumcraft.shattered.ShatteredBlocks.OAK_DIMENSIONAL_DOOR.defaultBlockState())) {
            helper.fail("nem as portas");
        }
        // e o que não é cheio some
        helper.setBlock(onde, net.minecraft.world.level.block.Blocks.TORCH);
        net.thaumcraft.shattered.LimboDecay.decay(level, helper.absolutePos(onde));
        helper.assertBlockPresent(net.minecraft.world.level.block.Blocks.AIR, onde);
        helper.succeed();
    }

    /** A Assinatura de Fenda marca um lugar, liga-o a outro e gasta-se; a estabilizada não. */
    @GameTest
    public void theSignatureLinksTwoPlaces(GameTestHelper helper) {
        BlockPos um = new BlockPos(1, 2, 1);
        BlockPos dois = new BlockPos(4, 2, 4);
        var quem = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack assinatura = new ItemStack(ShatteredItems.RIFT_SIGNATURE);

        usa(helper, quem, assinatura, um);
        if (net.thaumcraft.shattered.RiftSignatureItem.source(assinatura) == null) {
            helper.fail("a primeira vez devia marcar o lugar");
        }
        if (helper.getLevel().getBlockState(helper.absolutePos(um)).is(net.thaumcraft.shattered.ShatteredBlocks.RIFT)) {
            helper.fail("e ainda não rasgar fenda nenhuma");
        }

        usa(helper, quem, assinatura, dois);
        helper.assertBlockPresent(net.thaumcraft.shattered.ShatteredBlocks.RIFT, um);
        helper.assertBlockPresent(net.thaumcraft.shattered.ShatteredBlocks.RIFT, dois);
        if (net.thaumcraft.shattered.RiftSignatureItem.source(assinatura) != null) {
            helper.fail("e a assinatura devia esquecer o lugar");
        }

        // e cada fenda aponta para a outra
        var daqui = helper.getBlockEntity(um, net.thaumcraft.shattered.RiftBlockEntity.class);
        var dali = helper.getBlockEntity(dois, net.thaumcraft.shattered.RiftBlockEntity.class);
        if (daqui == null || dali == null) {
            helper.fail("as duas fendas deviam ter miolo");
            return;
        }
        if (daqui.destination() == null || !daqui.destination().pos().equals(helper.absolutePos(dois))) {
            helper.fail("a primeira aponta para a segunda");
        }
        if (dali.destination() == null || !dali.destination().pos().equals(helper.absolutePos(um))) {
            helper.fail("e a segunda para a primeira");
        }
        helper.succeed();
    }

    /** O Fecha-Fendas fecha a fenda solta. */
    @GameTest
    public void theRemoverClosesTheRift(GameTestHelper helper) {
        BlockPos onde = new BlockPos(2, 2, 2);
        helper.setBlock(onde, net.thaumcraft.shattered.ShatteredBlocks.RIFT);
        var quem = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        usa(helper, quem, new ItemStack(ShatteredItems.RIFT_REMOVER), onde);
        helper.assertBlockNotPresent(net.thaumcraft.shattered.ShatteredBlocks.RIFT, onde);
        helper.succeed();
    }

    private static void usa(GameTestHelper helper, net.minecraft.world.entity.player.Player quem,
                            ItemStack coisa, BlockPos onde) {
        var alvo = new net.minecraft.world.phys.BlockHitResult(
                net.minecraft.world.phys.Vec3.atCenterOf(helper.absolutePos(onde)),
                net.minecraft.core.Direction.UP, helper.absolutePos(onde), false);
        coisa.getItem().useOn(new net.minecraft.world.item.context.UseOnContext(
                helper.getLevel(), quem, net.minecraft.world.InteractionHand.MAIN_HAND, coisa, alvo));
    }

    /** A porta dimensional traz uma fenda na metade de baixo, e só nela. */
    @GameTest
    public void theDoorCarriesARift(GameTestHelper helper) {
        BlockPos baixo = new BlockPos(2, 2, 2);
        var estado = net.thaumcraft.shattered.ShatteredBlocks.OAK_DIMENSIONAL_DOOR.defaultBlockState();
        helper.setBlock(baixo, estado);
        helper.setBlock(baixo.above(), estado.setValue(
                net.minecraft.world.level.block.DoorBlock.HALF,
                net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER));

        if (!(helper.getBlockEntity(baixo, net.thaumcraft.shattered.RiftBlockEntity.class)
                instanceof net.thaumcraft.shattered.RiftBlockEntity)) {
            helper.fail("a metade de baixo devia ter a fenda");
        }
        if (helper.getLevel().getBlockEntity(helper.absolutePos(baixo.above())) != null) {
            helper.fail("e a de cima, não");
        }
        helper.succeed();
    }

    /** Atravessada, a porta abre um bolso e leva quem passa para lá — e a porta de volta traz de volta. */
    @GameTest
    public void theDoorOpensAPocketAndComesBack(GameTestHelper helper) {
        BlockPos baixo = new BlockPos(2, 2, 2);
        helper.setBlock(baixo, net.thaumcraft.shattered.ShatteredBlocks.OAK_DIMENSIONAL_DOOR);
        var fenda = helper.getBlockEntity(baixo, net.thaumcraft.shattered.RiftBlockEntity.class);
        if (fenda == null) {
            helper.fail("a porta devia ter fenda");
            return;
        }
        if (fenda.destination() != null) helper.fail("e nascer sem destino");

        var bicho = helper.spawn(net.minecraft.world.entity.EntityTypes.PIG, new BlockPos(2, 2, 3));
        if (!fenda.teleport(bicho)) {
            helper.fail("a travessia devia dar certo");
            return;
        }
        var destino = fenda.destination();
        if (destino == null) helper.fail("e a fenda devia ficar a apontar para o bolso");
        else if (destino.level() != ShatteredRealms.PUBLIC_POCKETS) helper.fail("que fica no mundo dos bolsos");

        var bolsos = helper.getLevel().getServer().getLevel(ShatteredRealms.PUBLIC_POCKETS);
        if (bolsos == null) {
            helper.fail("o mundo dos bolsos devia estar aberto");
            return;
        }
        // um bicho que muda de mundo é copiado para lá, e não levado; o que se procura é a cópia
        var chegados = bolsos.getEntitiesOfClass(net.minecraft.world.entity.animal.pig.Pig.class,
                new net.minecraft.world.phys.AABB(destino.pos()).inflate(8.0));
        if (chegados.isEmpty()) helper.fail("e quem atravessou devia estar lá");
        for (var chegado : chegados) chegado.discard();

        // a sala: paredes de tecido antigo e ar no meio
        BlockPos dentro = destino.pos();
        if (!bolsos.getBlockState(dentro).isAir()) helper.fail("onde se chega é ar");
        var porta = bolsos.getBlockState(dentro.north());
        if (!porta.is(net.thaumcraft.shattered.ShatteredBlocks.OAK_DIMENSIONAL_DOOR)) {
            helper.fail("e atrás fica a porta de volta; achei " + porta);
        }

        // e a porta de volta aponta para onde se entrou
        if (bolsos.getBlockEntity(dentro.north()) instanceof net.thaumcraft.shattered.RiftBlockEntity volta) {
            var paraCasa = volta.destination();
            if (paraCasa == null) helper.fail("a porta de volta devia ter destino");
            else if (paraCasa.level() != helper.getLevel().dimension()) helper.fail("e ele é o mundo de onde se veio");
        } else {
            helper.fail("a porta de volta devia ter fenda");
        }

        bicho.discard();
        net.thaumcraft.world.DynamicDimensions.remove(helper.getLevel().getServer(), ShatteredRealms.PUBLIC_POCKETS);
        helper.succeed();
    }

    /** São dezesseis cores de tecido comum, dezesseis de antigo, mais o eterno e o desfiado. */
    @GameTest
    public void theFabricsAreAllThere(GameTestHelper helper) {
        if (FabricBlocks.FABRIC.size() != 16) helper.fail("o tecido comum tem dezesseis cores");
        if (FabricBlocks.ANCIENT.size() != 16) helper.fail("e o antigo também");
        if (FabricBlocks.count() != 34) helper.fail("com o eterno e o desfiado, são trinta e quatro");
        // e onze coisas que não são bloco: os dois fios, as três marcas de fenda, a lâmina, o ferro e as quatro
        // peças de armadura
        if (ShatteredItems.count() != FabricBlocks.count() + net.thaumcraft.shattered.ShatteredBlocks.doors().size() + 11) {
            helper.fail("cada bloco tem o seu item; achei " + ShatteredItems.count());
        }
        if (!FabricBlocks.isFabric(FabricBlocks.FABRIC.get(DyeColor.BLACK))) helper.fail("o preto é tecido");
        if (FabricBlocks.isFabric(FabricBlocks.ETERNAL)) helper.fail("o eterno não é tecido de bolso");
        helper.succeed();
    }

    /** Fora de um bolso, o tecido não troca de bloco. */
    @GameTest
    public void theFabricOnlyGivesWayInsideAPocket(GameTestHelper helper) {
        BlockPos onde = new BlockPos(2, 2, 2);
        helper.setBlock(onde, FabricBlocks.FABRIC.get(DyeColor.BLACK));
        var quemTenta = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack pedra = new ItemStack(Items.STONE);
        FabricBlocks.FABRIC.get(DyeColor.BLACK).defaultBlockState().useItemOn(pedra, helper.getLevel(), quemTenta,
                net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.phys.BlockHitResult(
                        net.minecraft.world.phys.Vec3.atCenterOf(helper.absolutePos(onde)),
                        net.minecraft.core.Direction.UP, helper.absolutePos(onde), false));
        helper.assertBlockPresent(FabricBlocks.FABRIC.get(DyeColor.BLACK), onde);
        if (pedra.getCount() != 1) helper.fail("e nem gasta o bloco de quem tentou");
        helper.succeed();
    }

    /** E dentro dele, troca. */
    @GameTest
    public void theFabricGivesWayInsideAPocket(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var chave = ShatteredRealms.PUBLIC_POCKETS;
        var gerador = new net.minecraft.world.level.levelgen.FlatLevelSource(
                net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings.getDefault(
                        server.registryAccess().lookupOrThrow(Registries.BIOME),
                        server.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET),
                        server.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE)));
        var bolso = DynamicDimensions.getOrCreate(server, chave, BuiltinDimensionTypes.OVERWORLD, gerador);
        if (bolso == null) {
            helper.fail("o bolso devia abrir");
            return;
        }
        if (!ShatteredRealms.isPocket(bolso)) helper.fail("e saber que é bolso");

        BlockPos onde = new BlockPos(0, 70, 0);
        bolso.setBlockAndUpdate(onde, FabricBlocks.FABRIC.get(DyeColor.BLACK).defaultBlockState());
        var quemTroca = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack pedra = new ItemStack(Items.STONE, 2);
        FabricBlocks.FABRIC.get(DyeColor.BLACK).defaultBlockState().useItemOn(pedra, bolso, quemTroca,
                net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.phys.BlockHitResult(
                        net.minecraft.world.phys.Vec3.atCenterOf(onde),
                        net.minecraft.core.Direction.UP, onde, false));
        if (!bolso.getBlockState(onde).is(Blocks.STONE)) {
            helper.fail("dentro do bolso o tecido cede o lugar; ficou " + bolso.getBlockState(onde));
        }
        if (pedra.getCount() != 1) helper.fail("e gasta o bloco de quem o pôs");

        DynamicDimensions.remove(server, chave);
        helper.succeed();
    }

    /** O tecido antigo não se quebra. */
    @GameTest
    public void theAncientFabricDoesNotBreak(GameTestHelper helper) {
        var antigo = FabricBlocks.ANCIENT.get(DyeColor.BLACK).defaultBlockState();
        if (antigo.getDestroySpeed(helper.getLevel(), BlockPos.ZERO) >= 0.0f) {
            helper.fail("o tecido antigo não se quebra a pico");
        }
        var comum = FabricBlocks.FABRIC.get(DyeColor.BLACK).defaultBlockState();
        if (comum.getDestroySpeed(helper.getLevel(), BlockPos.ZERO) < 0.0f) {
            helper.fail("mas o comum sim");
        }
        helper.succeed();
    }

    /** A fenda solta sorteia o rosto dela e cresce sozinha, até ao tamanho máximo. */
    @GameTest(maxTicks = 100)
    public void theRiftGrowsAndGetsAFace(GameTestHelper helper) {
        BlockPos onde = new BlockPos(1, 2, 1);
        helper.setBlock(onde, net.thaumcraft.shattered.ShatteredBlocks.RIFT);
        var fenda = helper.getBlockEntity(onde, net.thaumcraft.shattered.RiftBlockEntity.class);
        if (fenda.riftYaw() >= 0.0f) helper.fail("a fenda nasce sem rosto; quem lho dá é o primeiro tique");

        helper.runAfterDelay(5, () -> {
            if (fenda.riftYaw() < 0.0f || fenda.riftYaw() >= 360.0f) {
                helper.fail("o giro dela fica entre zero e trezentos e sessenta: " + fenda.riftYaw());
            }
            if (fenda.curveId() < 0) helper.fail("e ela escolhe uma das formas");
            if (fenda.size() <= 0.0f) helper.fail("e cresce: " + fenda.size());

            // presa, ela para de crescer
            fenda.setStabilized(true);
            float parou = fenda.size();
            helper.runAfterDelay(5, () -> {
                if (fenda.size() != parou) helper.fail("presa, ela não cresce mais: " + fenda.size());
                helper.succeed();
            });
        });
    }
}
