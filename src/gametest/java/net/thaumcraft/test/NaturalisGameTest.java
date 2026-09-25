package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.naturalis.NaturalisItems;
import net.thaumcraft.naturalis.SickleItem;
import net.thaumcraft.research.Researches;
import net.thaumcraft.research.WarpEvents;

import java.util.Arrays;

/**
 * O ramo do Magia Naturalis: a aba, as foices e o que elas ceifam.
 */
public class NaturalisGameTest {
    /** A aba do ramo existe no livro, com as pesquisas do original. */
    @GameTest
    public void theBranchHasItsOwnTab(GameTestHelper helper) {
        if (Researches.get("MN_INTRO") == null) helper.fail("a pesquisa de entrada do ramo devia existir");
        var sickles = Researches.get("MN_SICKLES");
        if (sickles == null) helper.fail("a pesquisa das foices devia existir");
        else if (!sickles.category().equals(net.thaumcraft.naturalis.Naturalis.CATEGORY)) {
            helper.fail("as foices deviam estar na aba do ramo; estão em " + sickles.category());
        }
        int quantas = Researches.of(net.thaumcraft.naturalis.Naturalis.CATEGORY).size();
        if (quantas < 10) helper.fail("a aba do ramo devia ter as dez pesquisas geradas; tem " + quantas);
        helper.succeed();
    }

    /** A foice de táumio sai da receita do original: três lingotes e um graveto. */
    @GameTest
    public void theThaumiumSickleHasItsRecipe(GameTestHelper helper) {
        ItemStack ingot = new ItemStack(net.thaumcraft.registry.TCResources.get("thaumium_ingot"));
        ItemStack stick = new ItemStack(net.minecraft.world.item.Items.STICK);
        ItemStack none = ItemStack.EMPTY;
        var input = CraftingInput.of(3, 3, Arrays.asList(
                none, ingot, none,
                none, none, ingot,
                stick, ingot, none));
        var found = helper.getLevel().getServer().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
        if (found.isEmpty()) helper.fail("a foice de táumio devia fechar receita");
        else if (!found.get().value().assemble(input).is(NaturalisItems.THAUMIUM_SICKLE)) {
            helper.fail("devia sair a foice de táumio");
        }
        helper.succeed();
    }

    /** A foice leva junto o mato encostado; a de táumio alcança dois blocos além do primeiro. */
    @GameTest
    public void theSickleReapsWhatIsBesideIt(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(NaturalisItems.THAUMIUM_SICKLE));
        BlockPos base = new BlockPos(1, 2, 1);
        for (int i = 0; i < 4; i++) helper.setBlock(base.offset(i, 0, 0), Blocks.SHORT_GRASS);
        ItemStack sickle = player.getMainHandItem();
        BlockPos absolute = helper.absolutePos(base);
        sickle.getItem().mineBlock(sickle, helper.getLevel(), helper.getLevel().getBlockState(absolute), absolute, player);
        helper.getLevel().destroyBlock(absolute, false);
        int left = 0;
        for (int i = 0; i < 4; i++) {
            if (helper.getLevel().getBlockState(helper.absolutePos(base.offset(i, 0, 0))).is(Blocks.SHORT_GRASS)) left++;
        }
        if (left > 1) helper.fail("a foice devia ter levado o mato do lado; sobraram " + left);
        helper.succeed();
    }

    /** E ela só ceifa o que é de ceifar: pedra não é da conta dela. */
    @GameTest
    public void theSickleOnlyCutsPlants(GameTestHelper helper) {
        if (SickleItem.cuts(Blocks.STONE.defaultBlockState())) helper.fail("a foice não corta pedra");
        if (!SickleItem.cuts(Blocks.OAK_LEAVES.defaultBlockState())) helper.fail("a foice corta folha");
        if (!SickleItem.cuts(Blocks.WHEAT.defaultBlockState())) helper.fail("a foice corta plantação");
        if (!SickleItem.cuts(Blocks.COBWEB.defaultBlockState())) helper.fail("a foice corta teia");
        helper.succeed();
    }

    /** As sete madeiras arcanas existem, contam como tábua e caem inteiras. */
    @GameTest
    public void theArcaneWoodIsAllThere(GameTestHelper helper) {
        var madeiras = net.thaumcraft.naturalis.NaturalisBlocks.shown().stream()
                .filter(b -> {
                    String nome = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(b).getPath();
                    return nome.contains("wood") && !nome.contains("chest");
                }).toList();
        if (madeiras.size() != 7) helper.fail("o original tem sete feitios de madeira arcana; há " + madeiras.size());
        for (var bloco : madeiras) {
            var id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(bloco);
            if (!net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(id)) {
                helper.fail(id + " devia ter item de bloco");
            }
        }
        for (var nome : new String[]{"greatwood_planks_horizontal", "silverwood_planks_horizontal", "silverwood_planks_vertical"}) {
            var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.thaumcraft.Thaumcraft.id(nome));
            if (!new ItemStack(item).is(net.minecraft.tags.ItemTags.PLANKS)) helper.fail(nome + " devia contar como tábua");
        }
        helper.succeed();
    }

    /** Os dois óculos revelam os nós e descontam o que o original dizia. */
    @GameTest
    public void theGogglesRevealAndDiscount(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        for (var item : new net.minecraft.world.item.Item[]{NaturalisItems.SPECTACLES, NaturalisItems.DARK_CRYSTAL_GOGGLES}) {
            player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, new ItemStack(item));
            if (!net.thaumcraft.item.Revealing.can(player)) helper.fail(item + " devia revelar os nós");
        }
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, ItemStack.EMPTY);
        var oculos = (net.thaumcraft.api.wands.VisDiscountGear) NaturalisItems.SPECTACLES;
        if (oculos.visDiscount(new ItemStack(NaturalisItems.SPECTACLES), player, null) != 6) {
            helper.fail("os Óculos descontam seis por cento");
        }
        var escuros = (net.thaumcraft.api.wands.VisDiscountGear) NaturalisItems.DARK_CRYSTAL_GOGGLES;
        ItemStack stack = new ItemStack(NaturalisItems.DARK_CRYSTAL_GOGGLES);
        if (escuros.visDiscount(stack, player, null) != 5) helper.fail("os de cristal escuro descontam cinco");
        int perditio = escuros.visDiscount(stack, player, net.thaumcraft.api.aspects.Aspects.ENTROPY);
        if (perditio != 7 && perditio != 9) helper.fail("em Perditio eles descontam sete ou nove; descontam " + perditio);
        helper.succeed();
    }

    /** O diário anota o que a mesa de decomposição está tirando e depois despeja no conhecimento. */
    @GameTest
    public void theResearchLogKeepsPoints(GameTestHelper helper) {
        ItemStack diario = new ItemStack(NaturalisItems.RESEARCH_LOG);
        var anotado = net.thaumcraft.naturalis.ResearchLogItem.notes(diario);
        if (anotado.size() != 0) helper.fail("o diário começa vazio");
        var lista = new net.thaumcraft.api.aspects.AspectList().add(net.thaumcraft.api.aspects.Aspects.AIR, 3);
        diario.set(net.thaumcraft.registry.TCComponents.RESEARCH_LOG, lista);
        var player = helper.makeMockServerPlayerInLevel();
        int antes = net.thaumcraft.research.Knowledges.of(player).points(net.thaumcraft.api.aspects.Aspects.AIR);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, diario);
        diario.getItem().use(helper.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
        int depois = net.thaumcraft.research.Knowledges.of(player).points(net.thaumcraft.api.aspects.Aspects.AIR);
        if (depois != antes + 3) helper.fail("o diário devia ter passado três de Aer; passou " + (depois - antes));
        if (net.thaumcraft.naturalis.ResearchLogItem.notes(diario).size() != 0) helper.fail("e devia ficar em branco");
        helper.succeed();
    }

    /** A pedra do catalisador troca o bloco pelo próximo da família dele. */
    @GameTest
    public void theCatalystStoneCyclesBlocks(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos, Blocks.WOOL.white());
        ItemStack pedra = new ItemStack(NaturalisItems.MUTATION_STONE);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, pedra);
        var alvo = helper.absolutePos(pos);
        var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(alvo),
                net.minecraft.core.Direction.UP, alvo, false);
        pedra.getItem().useOn(new net.minecraft.world.item.context.UseOnContext(player,
                net.minecraft.world.InteractionHand.MAIN_HAND, hit));
        if (helper.getLevel().getBlockState(alvo).is(Blocks.WOOL.white())) helper.fail("a lã devia ter mudado de cor");
        if (!net.thaumcraft.naturalis.AlchemicalStoneItem.morphs(Blocks.STONE_BRICKS.defaultBlockState())) {
            helper.fail("o tijolo de pedra é de mudar");
        }
        helper.succeed();
    }

    /** O foco de construção entra no mapa de focos e guarda forma, tamanho e bloco. */
    @GameTest
    public void theBuilderFocusKeepsItsShape(GameTestHelper helper) {
        if (net.thaumcraft.registry.TCItems.FOCI.get("build") != NaturalisItems.BUILDER_FOCUS) {
            helper.fail("o foco de construção devia estar no mapa de focos do Thaumcraft");
        }
        ItemStack foco = new ItemStack(NaturalisItems.BUILDER_FOCUS);
        if (net.thaumcraft.naturalis.BuilderFocus.shape(foco) != net.thaumcraft.naturalis.BuilderFocus.Shape.CUBE) {
            helper.fail("ele começa no cubo");
        }
        if (net.thaumcraft.naturalis.BuilderFocus.size(foco) != 1) helper.fail("e no tamanho um");
        if (net.thaumcraft.naturalis.BuilderFocus.maxSize(foco) != 4) {
            helper.fail("sem Ampliação ele vai até quatro; vai até " + net.thaumcraft.naturalis.BuilderFocus.maxSize(foco));
        }
        helper.succeed();
    }

    /** O jarro guarda o bicho, e a varinha o solta de volta. */
    @GameTest
    public void theJarKeepsAndReleasesAMob(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        var porco = helper.spawn(net.minecraft.world.entity.EntityTypes.PIG, new BlockPos(1, 2, 1));
        ItemStack jarro = new ItemStack(NaturalisItems.PRISON_JAR);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, jarro);
        jarro.getItem().interactLivingEntity(jarro, player, porco, net.minecraft.world.InteractionHand.MAIN_HAND);
        if (!porco.isRemoved()) helper.fail("o porco devia ter entrado no jarro");
        ItemStack cheio = ItemStack.EMPTY;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            var s2 = player.getInventory().getItem(slot);
            if (s2.has(net.thaumcraft.registry.TCComponents.JARRED_MOB)) cheio = s2;
        }
        if (cheio.isEmpty()) helper.fail("devia haver um jarro com bicho no inventário");
        helper.succeed();
    }

    /** A foice do vazio distorce quem a carrega, como no original. */
    @GameTest
    public void theVoidSickleWarps(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        ItemStack stack = new ItemStack(NaturalisItems.VOID_SICKLE);
        int warp = NaturalisItems.VOID_SICKLE instanceof WarpEvents.WarpingGear gear ? gear.getWarp(stack, player) : -1;
        if (warp != 1) helper.fail("a foice do vazio distorce um; distorce " + warp);
        helper.succeed();
    }

    /** O baú arcano é do primeiro que o põe, e só ele abre. */
    @GameTest
    public void theArcaneChestBelongsToWhoPlacedIt(GameTestHelper helper) {
        var dono = helper.makeMockServerPlayerInLevel();
        var outro = helper.makeMockServerPlayerInLevel();
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos, net.thaumcraft.naturalis.NaturalisBlocks.ARCANE_CHEST_GREATWOOD);
        var bau = helper.getBlockEntity(pos, net.thaumcraft.naturalis.ArcaneChestBlockEntity.class);
        if (bau.getContainerSize() != 54) helper.fail("o de madeira-grande guarda 54; guarda " + bau.getContainerSize());
        bau.claim(dono);
        if (!bau.mayOpen(dono)) helper.fail("o dono abre o próprio baú");
        outro.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        if (bau.mayOpen(outro)) helper.fail("quem não é dono não abre");
        if (!bau.allow(outro.getUUID(), (byte) 0)) helper.fail("a chave devia dar entrada");
        if (!bau.mayOpen(outro)) helper.fail("com a chave ele abre");
        if (bau.mayBreak(outro)) helper.fail("mas com a chave simples ele não quebra o baú");
        helper.succeed();
    }

    /** O de madeira-prateada é maior, e a varinha encolhe o baú com tudo dentro. */
    @GameTest
    public void theWandShrinksTheChest(GameTestHelper helper) {
        var dono = helper.makeMockServerPlayerInLevel();
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos, net.thaumcraft.naturalis.NaturalisBlocks.ARCANE_CHEST_SILVERWOOD);
        var bau = helper.getBlockEntity(pos, net.thaumcraft.naturalis.ArcaneChestBlockEntity.class);
        if (bau.getContainerSize() != 77) helper.fail("o de prateada guarda 77; guarda " + bau.getContainerSize());
        bau.claim(dono);
        bau.setItem(0, new ItemStack(net.minecraft.world.item.Items.DIAMOND, 5));
        BlockPos mundo = helper.absolutePos(pos);
        bau.onWand(helper.getLevel(), new ItemStack(net.thaumcraft.registry.TCItems.WAND), dono, mundo,
                net.minecraft.core.Direction.UP);
        if (!helper.getLevel().getBlockState(mundo).isAir()) helper.fail("o baú devia ter sumido do lugar");
        var caidos = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                new net.minecraft.world.phys.AABB(mundo).inflate(2.0));
        var encolhido = caidos.stream().map(net.minecraft.world.entity.item.ItemEntity::getItem)
                .filter(i -> i.is(net.thaumcraft.naturalis.NaturalisItems.ARCANE_CHEST_SILVERWOOD)).findFirst();
        if (encolhido.isEmpty()) {
            helper.fail("o baú encolhido devia ter caído no chão");
            return;
        }
        var guardado = encolhido.get().get(net.minecraft.core.component.DataComponents.CONTAINER);
        if (guardado == null || guardado.nonEmptyItemCopyStream().findFirst().isEmpty()) {
            helper.fail("o baú encolhido devia levar os diamantes junto");
        }
        helper.succeed();
    }

    /** A chave do endosso junta gente e põe todo mundo na lista do baú de uma vez. */
    @GameTest
    public void theKeyOfEndorsingCarriesAList(GameTestHelper helper) {
        var dono = helper.makeMockServerPlayerInLevel();
        var convidado = helper.makeMockServerPlayerInLevel();
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos, net.thaumcraft.naturalis.NaturalisBlocks.ARCANE_CHEST_GREATWOOD);
        var bau = helper.getBlockEntity(pos, net.thaumcraft.naturalis.ArcaneChestBlockEntity.class);
        bau.claim(dono);
        ItemStack chave = new ItemStack(net.thaumcraft.naturalis.NaturalisItems.KEY_OF_ENDORSING);
        dono.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, chave);
        net.thaumcraft.naturalis.ArcaneKeyItem.bind(dono, helper.getLevel(),
                net.minecraft.world.InteractionHand.MAIN_HAND, convidado, null);
        if (net.thaumcraft.naturalis.ArcaneKeyItem.bond(chave).endorsed().size() != 1) {
            helper.fail("a chave devia ter anotado o convidado");
        }
        chave.getItem().useOn(new net.minecraft.world.item.context.UseOnContext(helper.getLevel(), dono,
                net.minecraft.world.InteractionHand.MAIN_HAND, chave,
                new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(helper.absolutePos(pos)),
                        net.minecraft.core.Direction.UP, helper.absolutePos(pos), false)));
        convidado.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        if (!bau.mayOpen(convidado)) helper.fail("o convidado devia ter entrado na lista do baú");
        helper.succeed();
    }

    /** As quatro cruzes em volta da mesa de transcrição, a dois blocos, com mesa de decomposição em cada uma. */
    private static final BlockPos[] CRUZ = {new BlockPos(2, 0, 0), new BlockPos(-2, 0, 0),
            new BlockPos(0, 0, 2), new BlockPos(0, 0, -2)};

    /** A mesa de transcrição copia para o diário o primário que uma mesa de decomposição em volta tirou. */
    @GameTest
    public void theTranscribingTableCopiesTheAspect(GameTestHelper helper) {
        BlockPos mesa = new BlockPos(3, 2, 3);
        helper.setBlock(mesa, net.thaumcraft.naturalis.NaturalisBlocks.TRANSCRIBING_TABLE);
        var transcricao = helper.getBlockEntity(mesa, net.thaumcraft.naturalis.TranscribingTableBlockEntity.class);
        // as quatro cruzes têm mesa, para o sorteio do original sempre cair numa delas
        var mesas = new java.util.ArrayList<net.thaumcraft.block.entity.DeconstructionTableBlockEntity>();
        for (BlockPos volta : CRUZ) {
            BlockPos onde = mesa.offset(volta);
            helper.setBlock(onde, net.thaumcraft.registry.TCBlocks.DECONSTRUCTION_TABLE);
            var decomposicao = helper.getBlockEntity(onde, net.thaumcraft.block.entity.DeconstructionTableBlockEntity.class);
            decomposicao.data().set(1, net.thaumcraft.block.entity.DeconstructionTableBlockEntity.indexOf(
                    net.thaumcraft.api.aspects.Aspects.AIR));
            mesas.add(decomposicao);
        }
        ItemStack diario = new ItemStack(NaturalisItems.RESEARCH_LOG);
        transcricao.setItem(0, diario);

        // o sorteio pode cair no próprio lugar da mesa, que não vale; vinte voltas dão de sobra
        for (int volta = 0; volta < 20; volta++) {
            transcricao.data().set(0, 1);
            net.thaumcraft.naturalis.TranscribingTableBlockEntity.tick(helper.getLevel(), helper.absolutePos(mesa),
                    helper.getBlockState(mesa), transcricao);
            if (net.thaumcraft.naturalis.ResearchLogItem.notes(transcricao.getItem(0))
                    .getAmount(net.thaumcraft.api.aspects.Aspects.AIR) > 0) {
                break;
            }
        }
        if (net.thaumcraft.naturalis.ResearchLogItem.notes(transcricao.getItem(0))
                .getAmount(net.thaumcraft.api.aspects.Aspects.AIR) < 1) {
            helper.fail("o diário devia ter ao menos um ponto de ar anotado");
        }
        if (mesas.stream().noneMatch(m -> m.aspect() == null)) {
            helper.fail("a mesa de onde ele copiou devia ter ficado sem o primário");
        }
        helper.succeed();
    }

    /** E o diário cheio desce sozinho para a casa de baixo. */
    @GameTest
    public void theFullLogMovesToTheOutputSlot(GameTestHelper helper) {
        BlockPos mesa = new BlockPos(3, 2, 3);
        helper.setBlock(mesa, net.thaumcraft.naturalis.NaturalisBlocks.TRANSCRIBING_TABLE);
        var transcricao = helper.getBlockEntity(mesa, net.thaumcraft.naturalis.TranscribingTableBlockEntity.class);
        for (BlockPos volta : CRUZ) {
            BlockPos onde = mesa.offset(volta);
            helper.setBlock(onde, net.thaumcraft.registry.TCBlocks.DECONSTRUCTION_TABLE);
            helper.getBlockEntity(onde, net.thaumcraft.block.entity.DeconstructionTableBlockEntity.class)
                    .data().set(1, net.thaumcraft.block.entity.DeconstructionTableBlockEntity.indexOf(
                            net.thaumcraft.api.aspects.Aspects.EARTH));
        }
        ItemStack diario = new ItemStack(NaturalisItems.RESEARCH_LOG);
        var cheio = new net.thaumcraft.api.aspects.AspectList();
        for (var primal : net.thaumcraft.api.aspects.Aspects.primals()) {
            cheio.add(primal, net.thaumcraft.naturalis.TranscribingTableBlockEntity.FULL);
        }
        diario.set(net.thaumcraft.registry.TCComponents.RESEARCH_LOG, cheio);
        if (!net.thaumcraft.naturalis.TranscribingTableBlockEntity.full(diario)) helper.fail("este diário está cheio");
        transcricao.setItem(0, diario);
        for (int volta = 0; volta < 40 && transcricao.getItem(1).isEmpty(); volta++) {
            transcricao.data().set(0, 1);
            net.thaumcraft.naturalis.TranscribingTableBlockEntity.tick(helper.getLevel(), helper.absolutePos(mesa),
                    helper.getBlockState(mesa), transcricao);
        }
        if (transcricao.getItem(1).isEmpty()) helper.fail("o diário cheio devia ter descido para a casa de baixo");
        if (!transcricao.getItem(0).isEmpty()) helper.fail("e a casa de cima devia ter ficado vazia");
        helper.succeed();
    }

    /** O revenante é de quem o levantou, não o ataca e desmancha quando fica sem alvo. */
    @GameTest
    public void theRevenantServesWhoRaisedIt(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        var revenante = helper.spawn(net.thaumcraft.naturalis.NaturalisEntities.REVENANT, new BlockPos(1, 2, 1));
        revenante.owner(player.getUUID());
        if (!revenante.isBaby()) helper.fail("o revenante é um zumbi pequeno");
        if (revenante.canAttack(player)) helper.fail("ele não ataca quem o levantou");
        var porco = helper.spawn(net.minecraft.world.entity.EntityTypes.PIG, new BlockPos(2, 2, 1));
        if (!revenante.canAttack(porco)) helper.fail("mas ataca quem não é o dono");
        revenante.setTarget(porco);
        if (revenante.ownerEntity() != player) helper.fail("ele sabe de quem é");
        // sem alvo ele se desfaz
        revenante.setTarget(null);
        revenante.tick();
        if (revenante.isAlive() && revenante.getHealth() > 0.0f) {
            helper.fail("sem alvo ele devia ter se desmanchado; ficou com " + revenante.getHealth());
        }
        helper.succeed();
    }

    /** E o foco dele cobra terra, entropia e água, e aceita Potência e Frugal nos cinco postos. */
    @GameTest
    public void theRevenantFocusCostsAndUpgrades(GameTestHelper helper) {
        var custo = net.thaumcraft.naturalis.RevenantFocus.COST;
        if (custo.getAmount(net.thaumcraft.api.aspects.Aspects.EARTH) != 450
                || custo.getAmount(net.thaumcraft.api.aspects.Aspects.ENTROPY) != 350
                || custo.getAmount(net.thaumcraft.api.aspects.Aspects.WATER) != 200) {
            helper.fail("o custo do original é 450 de terra, 350 de entropia e 200 de água");
        }
        if (!(NaturalisItems.REVENANT_FOCUS instanceof net.thaumcraft.item.FocusItem foco)) {
            helper.fail("o foco do revenante devia ser um foco");
            return;
        }
        ItemStack stack = new ItemStack(NaturalisItems.REVENANT_FOCUS);
        for (int posto = 1; posto <= 5; posto++) {
            var cabem = foco.possibleByRank(stack, posto);
            if (cabem.size() != 2 || !cabem.contains(net.thaumcraft.item.FocusUpgradeTable.POTENCY)
                    || !cabem.contains(net.thaumcraft.item.FocusUpgradeTable.FRUGAL)) {
                helper.fail("no posto " + posto + " cabem Potência e Frugal; cabem " + cabem);
            }
        }
        helper.succeed();
    }

    /** O Geo-Pilone só trabalha em cima do vão e dos três totens de obsidiana. */
    @GameTest
    public void theGeoPylonNeedsItsTotem(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 5, 1);
        helper.setBlock(pos, net.thaumcraft.naturalis.NaturalisBlocks.GEO_PYLON);
        if (net.thaumcraft.naturalis.GeoPylonBlockEntity.standing(helper.getLevel(), helper.absolutePos(pos))) {
            helper.fail("sem os totens ele não devia estar de pé");
        }
        for (int i = 1; i <= 3; i++) {
            helper.setBlock(pos.below(1 + i), net.thaumcraft.registry.TCBlocks.OBSIDIAN_TOTEM);
        }
        if (!net.thaumcraft.naturalis.GeoPylonBlockEntity.standing(helper.getLevel(), helper.absolutePos(pos))) {
            helper.fail("com o vão e os três totens ele devia estar de pé");
        }
        // e um bloco no vão derruba a conta de novo
        helper.setBlock(pos.below(), net.minecraft.world.level.block.Blocks.STONE);
        if (net.thaumcraft.naturalis.GeoPylonBlockEntity.standing(helper.getLevel(), helper.absolutePos(pos))) {
            helper.fail("com o vão tapado ele não está de pé");
        }
        helper.succeed();
    }

    /** O amostrador guarda a terra do lugar e a passa ao pilone; a terra cobra o que ela carrega de aura. */
    @GameTest
    public void theBiomeSamplerAttunesThePylon(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos, net.thaumcraft.naturalis.NaturalisBlocks.GEO_PYLON);
        var pylon = helper.getBlockEntity(pos, net.thaumcraft.naturalis.GeoPylonBlockEntity.class);
        ItemStack amostrador = new ItemStack(NaturalisItems.BIOME_SAMPLER);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, amostrador);

        // agachado, ele anota a terra daqui
        player.setShiftKeyDown(true);
        BlockPos chao = helper.absolutePos(pos.below());
        amostrador.getItem().useOn(new net.minecraft.world.item.context.UseOnContext(helper.getLevel(), player,
                net.minecraft.world.InteractionHand.MAIN_HAND, amostrador,
                new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(chao),
                        net.minecraft.core.Direction.UP, chao, false)));
        var terra = net.thaumcraft.naturalis.BiomeSamplerItem.sampled(amostrador);
        if (terra == null) helper.fail("o amostrador devia ter guardado a terra daqui");
        var conta = amostrador.get(net.thaumcraft.registry.TCComponents.SAMPLED_COST);
        if (conta == null) helper.fail("e o que ela cobraria");

        // de pé, ele afina o pilone
        player.setShiftKeyDown(false);
        BlockPos mundo = helper.absolutePos(pos);
        amostrador.getItem().useOn(new net.minecraft.world.item.context.UseOnContext(helper.getLevel(), player,
                net.minecraft.world.InteractionHand.MAIN_HAND, amostrador,
                new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(mundo),
                        net.minecraft.core.Direction.UP, mundo, false)));
        if (pylon.target() != terra) helper.fail("o pilone devia estar afinado à terra do amostrador");
        helper.succeed();
    }

    /** A varinha liga e desliga o pilone. */
    @GameTest
    public void theWandTogglesThePylon(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos, net.thaumcraft.naturalis.NaturalisBlocks.GEO_PYLON);
        var pylon = helper.getBlockEntity(pos, net.thaumcraft.naturalis.GeoPylonBlockEntity.class);
        if (!pylon.idle()) helper.fail("ele nasce parado");
        pylon.onWand(helper.getLevel(), new ItemStack(net.thaumcraft.registry.TCItems.WAND), player,
                helper.absolutePos(pos), net.minecraft.core.Direction.UP);
        if (pylon.idle()) helper.fail("a varinha devia tê-lo ligado");
        pylon.onWand(helper.getLevel(), new ItemStack(net.thaumcraft.registry.TCItems.WAND), player,
                helper.absolutePos(pos), net.minecraft.core.Direction.UP);
        if (!pylon.idle()) helper.fail("e desligado de novo");
        helper.succeed();
    }

    /** A Pedra do Catalisador troca o bloco na bancada e não se gasta. */
    @GameTest
    public void theMutationStoneSurvivesTheCraft(GameTestHelper helper) {
        var pares = net.thaumcraft.crafting.MutationRecipe.pairs();
        if (pares.size() != 38) helper.fail("o original tem 38 trocas; há " + pares.size());
        var entrada = net.minecraft.world.item.crafting.CraftingInput.of(2, 1, java.util.List.of(
                new ItemStack(NaturalisItems.MUTATION_STONE),
                new ItemStack(net.minecraft.world.item.Items.WOOL.white())));
        var receita = net.thaumcraft.crafting.MutationRecipe.INSTANCE;
        if (!receita.matches(entrada, helper.getLevel())) helper.fail("a pedra devia trocar a lã branca");
        ItemStack saida = receita.assemble(entrada);
        if (!saida.is(net.minecraft.world.item.Items.WOOL.black())) {
            helper.fail("a lã branca devia virar preta; virou " + saida);
        }
        var sobra = receita.getRemainingItems(entrada);
        if (!sobra.get(0).is(NaturalisItems.MUTATION_STONE)) helper.fail("a pedra devia voltar para a bancada");
        helper.succeed();
    }

    /** A Criadora de Mácula é dura, ligeira e não pega o veneno da mácula. */
    @GameTest
    public void theTaintBreederIsToughAndImmune(GameTestHelper helper) {
        var criadora = helper.spawn(net.thaumcraft.naturalis.NaturalisEntities.TAINT_BREEDER, new BlockPos(2, 2, 2));
        if (criadora.getMaxHealth() != 42.0f) helper.fail("o original lhe dá 42 de vida; tem " + criadora.getMaxHealth());
        if (!(criadora instanceof net.thaumcraft.api.TaintedMob)) helper.fail("ela é uma criatura da mácula");
        var veneno = new net.minecraft.world.effect.MobEffectInstance(net.thaumcraft.registry.TCEffects.FLUX_TAINT, 100);
        if (criadora.canBeAffected(veneno)) helper.fail("o veneno da mácula não pega nela");
        if (!criadora.canBeAffected(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.SLOWNESS, 100))) {
            helper.fail("mas a lentidão pega");
        }
        helper.succeed();
    }

    /** E, ferida e com alguém para caçar, ela põe aranhas de mácula no mundo. */
    @GameTest
    public void theTaintBreederBreedsWhenHurt(GameTestHelper helper) {
        var criadora = helper.spawn(net.thaumcraft.naturalis.NaturalisEntities.TAINT_BREEDER, new BlockPos(2, 2, 2));
        var porco = helper.spawn(net.minecraft.world.entity.EntityTypes.PIG, new BlockPos(3, 2, 2));
        criadora.setTarget(porco);
        criadora.setHealth(20.0f);
        int antes = helper.getLevel().getEntitiesOfClass(net.thaumcraft.entity.taint.TaintSpiderEntity.class,
                new net.minecraft.world.phys.AABB(helper.absolutePos(new BlockPos(2, 2, 2))).inflate(8.0)).size();
        // ela pare de vinte em vinte tiques; uma volta em cada vintena basta
        for (int volta = 0; volta < 3; volta++) criadora.aiStep();
        int depois = helper.getLevel().getEntitiesOfClass(net.thaumcraft.entity.taint.TaintSpiderEntity.class,
                new net.minecraft.world.phys.AABB(helper.absolutePos(new BlockPos(2, 2, 2))).inflate(8.0)).size();
        if (depois <= antes) helper.fail("ferida e caçando, ela devia ter posto aranha no mundo");
        helper.succeed();
    }

    /** O Baú Maligno é de quem o chamou, guarda trinta e seis coisas e o sino o recolhe com elas. */
    @GameTest
    public void theEvilTrunkComesAndGoes(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        BlockPos onde = new BlockPos(2, 2, 2);
        ItemStack chamado = new ItemStack(NaturalisItems.TRUNK_SPAWNER_DEMONIC);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, chamado);
        BlockPos chao = helper.absolutePos(onde.below());
        chamado.getItem().useOn(new net.minecraft.world.item.context.UseOnContext(helper.getLevel(), player,
                net.minecraft.world.InteractionHand.MAIN_HAND, chamado,
                new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(chao),
                        net.minecraft.core.Direction.UP, chao, false)));
        var baus = helper.getLevel().getEntitiesOfClass(net.thaumcraft.naturalis.EvilTrunkEntity.class,
                new net.minecraft.world.phys.AABB(helper.absolutePos(onde)).inflate(4.0));
        if (baus.size() != 1) {
            helper.fail("o item devia ter chamado um baú; chamou " + baus.size());
            return;
        }
        var trunk = baus.get(0);
        if (trunk.kind() != net.thaumcraft.naturalis.EvilTrunkEntity.Kind.DEMONIC) {
            helper.fail("o feitio do baú é o do item; veio " + trunk.kind());
        }
        if (!trunk.isOwner(player)) helper.fail("e ele é de quem o chamou");
        if (trunk.inventory.getContainerSize() != 36) {
            helper.fail("ele guarda 36 coisas; guarda " + trunk.inventory.getContainerSize());
        }
        trunk.inventory.setItem(0, new ItemStack(net.minecraft.world.item.Items.DIAMOND, 3));

        // o sino o recolhe, e o item leva o que havia dentro
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                new ItemStack(net.thaumcraft.registry.TCItems.GOLEM_BELL));
        net.thaumcraft.naturalis.EvilTrunkEntity.pickUp(player, helper.getLevel(),
                net.minecraft.world.InteractionHand.MAIN_HAND, trunk, null);
        if (!trunk.isRemoved()) helper.fail("o sino devia ter recolhido o baú");
        var caidos = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                new net.minecraft.world.phys.AABB(helper.absolutePos(onde)).inflate(4.0));
        var encolhido = caidos.stream().map(net.minecraft.world.entity.item.ItemEntity::getItem)
                .filter(i -> i.is(NaturalisItems.TRUNK_SPAWNER_DEMONIC)).findFirst();
        if (encolhido.isEmpty()) {
            helper.fail("o baú recolhido devia ter caído no chão");
            return;
        }
        var guardado = encolhido.get().get(net.minecraft.core.component.DataComponents.CONTAINER);
        if (guardado == null || guardado.nonEmptyItemCopyStream().findFirst().isEmpty()) {
            helper.fail("e devia levar os diamantes junto");
        }
        helper.succeed();
    }

    /** As teclas do foco de construção: o tamanho dá a volta, a forma anda e o jeito troca. */
    @GameTest
    public void theBuilderFocusKeysCycle(GameTestHelper helper) {
        ItemStack foco = new ItemStack(NaturalisItems.BUILDER_FOCUS);
        int maior = net.thaumcraft.naturalis.BuilderFocus.maxSize(foco);
        for (int volta = 1; volta < maior; volta++) {
            net.thaumcraft.naturalis.BuilderFocus.cycleSize(foco, 1);
            if (net.thaumcraft.naturalis.BuilderFocus.size(foco) != volta + 1) {
                helper.fail("a tecla devia ter subido para " + (volta + 1));
            }
        }
        // no maior, ela volta ao um
        net.thaumcraft.naturalis.BuilderFocus.cycleSize(foco, 1);
        if (net.thaumcraft.naturalis.BuilderFocus.size(foco) != 1) helper.fail("no maior ela volta ao um");
        net.thaumcraft.naturalis.BuilderFocus.cycleSize(foco, -1);
        if (net.thaumcraft.naturalis.BuilderFocus.size(foco) != maior) helper.fail("e do um vai ao maior");

        var forma = net.thaumcraft.naturalis.BuilderFocus.shape(foco);
        net.thaumcraft.naturalis.BuilderFocus.cycleShape(foco);
        if (net.thaumcraft.naturalis.BuilderFocus.shape(foco) != forma.next()) helper.fail("a forma devia ter andado");

        if (net.thaumcraft.naturalis.BuilderFocus.mode(foco) != net.thaumcraft.naturalis.BuilderFocus.Mode.PICKED) {
            helper.fail("ele começa no jeito do bloco marcado");
        }
        net.thaumcraft.naturalis.BuilderFocus.cycleMode(foco);
        if (net.thaumcraft.naturalis.BuilderFocus.mode(foco) != net.thaumcraft.naturalis.BuilderFocus.Mode.UNIFORM) {
            helper.fail("e o Ctrl o passa ao jeito do bloco da mira");
        }
        helper.succeed();
    }

    /** As sombras das pesquisas do Thaumcraft estão na aba do ramo, ocas e irmãs das de verdade. */
    @GameTest
    public void theBranchHasTheThaumcraftShadows(GameTestHelper helper) {
        String[][] sombras = {{"MN_TC_GOGGLES", "GOGGLES"}, {"MN_TC_WARDED_ARCANA", "WARDEDARCANA"},
                {"MN_TC_FOCUS_TRADE", "FOCUSTRADE"}, {"MN_TC_CRUCIBLE", "CRUCIBLE"},
                {"MN_TC_FOCUS_POUCH", "FOCUSPOUCH"}, {"MN_TC_TRAVEL_TRUNK", "TRAVELTRUNK"}};
        for (String[] par : sombras) {
            var sombra = net.thaumcraft.research.Researches.get(par[0]);
            var original = net.thaumcraft.research.Researches.get(par[1]);
            if (sombra == null) {
                helper.fail("falta a sombra " + par[0]);
                return;
            }
            if (!sombra.category().equals(net.thaumcraft.naturalis.Naturalis.CATEGORY)) {
                helper.fail(par[0] + " devia estar na aba do ramo");
            }
            if (!sombra.is(net.thaumcraft.research.Research.Mark.STUB)
                    || !sombra.is(net.thaumcraft.research.Research.Mark.HIDDEN)) {
                helper.fail(par[0] + " é oca e escondida, como no original");
            }
            if (sombra.pages().size() != original.pages().size()) {
                helper.fail(par[0] + " lê as páginas da pesquisa de que é sombra");
            }
            if (!original.siblings().contains(par[0])) {
                helper.fail(par[1] + " devia ter a sombra por irmã");
            }
        }
        helper.succeed();
    }

    /**
     * O jarro leva o bicho do começo ao fim: pega-o da mão, guarda-o no bloco que se põe e devolve-o ao item
     * quando alguém quebra o vidro. É o que faz o bicho aparecer lá dentro para quem olha.
     */
    @GameTest
    public void thePrisonJarCarriesTheMob(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        BlockPos pos = new BlockPos(2, 2, 2);
        var porco = helper.spawn(net.minecraft.world.entity.EntityTypes.PIG, pos.above());
        ItemStack jarro = new ItemStack(net.thaumcraft.naturalis.NaturalisBlocks.PRISON_JAR);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, jarro);
        jarro.getItem().interactLivingEntity(jarro, player, porco, net.minecraft.world.InteractionHand.MAIN_HAND);
        if (!porco.isRemoved()) helper.fail("o porco devia ter entrado no jarro");

        ItemStack cheio = ItemStack.EMPTY;
        for (int casa = 0; casa < player.getInventory().getContainerSize(); casa++) {
            ItemStack naCasa = player.getInventory().getItem(casa);
            if (naCasa.has(net.thaumcraft.registry.TCComponents.JARRED_MOB)) cheio = naCasa;
        }
        if (cheio.isEmpty()) helper.fail("o jarro cheio devia ter ido para a mochila de quem o usou");

        // o bloco posto guarda o mesmo bicho, que é o que o desenhista mostra dentro do vidro
        helper.setBlock(pos, net.thaumcraft.naturalis.NaturalisBlocks.PRISON_JAR);
        BlockPos mundo = helper.absolutePos(pos);
        net.thaumcraft.naturalis.NaturalisBlocks.PRISON_JAR.setPlacedBy(helper.getLevel(), mundo,
                helper.getLevel().getBlockState(mundo), player, cheio);
        if (!(helper.getLevel().getBlockEntity(mundo) instanceof net.thaumcraft.naturalis.PrisonJarBlockEntity jar)
                || !jar.hasStored()) {
            helper.fail("o jarro posto devia estar com o bicho dentro");
            helper.succeed();
            return;
        }
        var dentro = jar.stored();
        if (dentro == null || !dentro.getString("id").orElse("").equals("minecraft:pig")) {
            helper.fail("quem está no jarro é o porco, e não " + (dentro == null ? "nada" : dentro.getString("id")));
        }

        // e quebrado, ele devolve o bicho ao item
        net.thaumcraft.naturalis.NaturalisBlocks.PRISON_JAR.playerWillDestroy(helper.getLevel(), mundo,
                helper.getLevel().getBlockState(mundo), player);
        boolean caiu = helper.getLevel()
                .getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                        new net.minecraft.world.phys.AABB(mundo).inflate(2.0))
                .stream().anyMatch(item -> item.getItem().has(net.thaumcraft.registry.TCComponents.JARRED_MOB));
        if (!caiu) helper.fail("o jarro quebrado devia cair com o bicho ainda dentro");
        helper.succeed();
    }
}
