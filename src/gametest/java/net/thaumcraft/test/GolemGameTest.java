package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.golems.GolemTypes;
import net.thaumcraft.block.GolemFetterBlock;
import net.thaumcraft.block.entity.JarBlockEntity;
import net.thaumcraft.entity.GolemEntity;
import net.thaumcraft.entity.golem.Marker;
import net.thaumcraft.item.GolemBellItem;
import net.thaumcraft.item.GolemPlacerItem;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCItems;

import java.util.List;

/**
 * Os golens têm de ser os do Thaumcraft 4.2.3.5: a tabela das matérias, o corpo que a matéria e as melhorias dão, e o
 * serviço de cada núcleo feito de ponta a ponta no mundo de teste (juntar, encher, esvaziar, separar, colher, lenhar,
 * guardar, abater, decantar e a alquimia), a algema, o sino e o salvar.
 */
public class GolemGameTest {
    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
    }

    /** Um golem posto como o item põe: casa no bloco dele, e a face que aponta para o baú da casa. */
    private static GolemEntity golem(GameTestHelper helper, String material, int core, BlockPos home, Direction facing, int... upgrades) {
        GolemEntity g = TCEntities.GOLEM.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        g.init(GolemEntity.typeIndex(material), false);
        BlockPos abs = helper.absolutePos(home);
        g.snapTo(abs.getX() + 0.5, abs.getY(), abs.getZ() + 0.5, 0.0f, 0.0f);
        g.setHome(abs);
        g.setCore((byte) core);
        for (int a = 0; a < upgrades.length; a++) g.setUpgrade(a, (byte) upgrades[a]);
        g.setup(facing.get3DDataValue());
        helper.getLevel().addFreshEntity(g);
        return g;
    }

    private static void mark(GameTestHelper helper, GolemEntity g, BlockPos rel, Direction side, int color) {
        List<Marker> markers = new java.util.ArrayList<>(g.getMarkers());
        markers.add(new Marker(helper.absolutePos(rel), helper.getLevel(), side.get3DDataValue(), color));
        g.setMarkers(markers);
    }

    private static int count(Container c, net.minecraft.world.item.Item item) {
        int n = 0;
        for (int s = 0; s < c.getContainerSize(); s++) if (c.getItem(s).is(item)) n += c.getItem(s).getCount();
        return n;
    }

    /** A tabela das matérias é a do original. */
    @GameTest
    public void theTableCameFromTheOriginal(GameTestHelper helper) {
        if (GolemTypes.ALL.size() != 8) helper.fail("o original tem oito matérias de golem");
        if (GolemTypes.CORES.length != 12) helper.fail("o original tem doze núcleos");
        GolemTypes.Type straw = GolemTypes.of("straw");
        if (straw.health() != 10 || straw.carry() != 1 || straw.fireResist()) helper.fail("o de palha: dez de vida, uma coisa, pega fogo");
        GolemTypes.Type thaumium = GolemTypes.of("thaumium");
        if (thaumium.health() != 40 || thaumium.carry() != 32 || thaumium.upgrades() != 2 || !thaumium.fireResist()) {
            helper.fail("o de táumio: quarenta de vida, trinta e duas coisas, duas melhorias, não pega fogo");
        }
        if (!GolemTypes.of("clay").fireResist()) helper.fail("o de argila não pega fogo");
        if (GolemTypes.of("stone").armor() != 12) helper.fail("o de pedra aguenta doze");
        helper.succeed();
    }

    /** A matéria e as melhorias mandam no corpo: vida, couro, carga, dano, alcance e casas de melhoria. */
    @GameTest
    public void theMaterialAndUpgradesShapeTheGolem(GameTestHelper helper) {
        floor(helper);
        GolemEntity thaumium = golem(helper, "thaumium", 4, new BlockPos(2, 2, 2), Direction.UP);
        if (thaumium.getMaxHealth() != 40.0f) helper.fail("o de táumio tem quarenta de vida, tem " + thaumium.getMaxHealth());
        if (thaumium.getArmorValue() != 15) helper.fail("o de táumio aguenta quinze, aguenta " + thaumium.getArmorValue());
        if (thaumium.getCarryLimit() != 32) helper.fail("o de táumio carrega trinta e duas");
        // o dano: 2 + força (4) + terra (0)
        if (thaumium.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) != 6.0) {
            helper.fail("o de táumio bate seis");
        }
        GolemEntity straw = golem(helper, "straw", 2, new BlockPos(5, 2, 5), Direction.UP, 1);
        // a terra: 1 + max(4, 1) = 5
        if (straw.getCarryLimit() != 5) helper.fail("o de palha com terra carrega cinco, carrega " + straw.getCarryLimit());
        if (straw.getRange() != 16.0f) helper.fail("o alcance de fábrica é dezesseis");
        GolemEntity advanced = TCEntities.GOLEM.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        advanced.init(GolemEntity.typeIndex("thaumium"), true);
        if (advanced.upgradeSlots() != 3) helper.fail("o de táumio avançado tem três casas de melhoria");
        advanced.discard();
        helper.succeed();
    }

    /** O golem guardado: posto no baú, nasce com a casa ali; recolhido pelo sino, o item leva tudo e volta igual. */
    @GameTest
    public void thePlacerAndTheBellKeepEverything(GameTestHelper helper) {
        floor(helper);
        helper.setBlock(new BlockPos(3, 2, 3), Blocks.CHEST);
        ItemStack placer = new ItemStack(TCItems.GOLEM_PLACERS.get("iron"));
        placer.set(TCComponents.GOLEM_CORE, 0);
        placer.set(TCComponents.GOLEM_DECO, "H");
        placer.set(TCComponents.GOLEM_UPGRADES, List.of((byte) 1));
        placer.set(TCComponents.GOLEM_INVENTORY, List.of(new GolemPlacerItem.Ghost(0, new ItemStack(Items.STONE), 200)));
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        BlockPos home = helper.absolutePos(new BlockPos(3, 2, 4));
        ((GolemPlacerItem) placer.getItem()).spawnCreature(helper.getLevel(), home.getX() + 0.5, home.getY(), home.getZ() + 0.5,
                Direction.SOUTH.get3DDataValue(), placer, player);
        GolemEntity g = helper.getLevel().getEntitiesOfClass(GolemEntity.class, new net.minecraft.world.phys.AABB(home).inflate(1)).getFirst();
        if (!g.homeContainer().equals(helper.absolutePos(new BlockPos(3, 2, 3)))) helper.fail("o baú da casa devia ser o baú tocado");
        if (g.getCore() != 0 || !g.getGolemDecoration().equals("H") || g.getUpgradeAmount(1) != 1) helper.fail("o golem devia nascer com o que o item tinha");
        if (g.inventory.getItem(0).getCount() != 200) helper.fail("a casa fantasma pede duzentas pedras");
        if (g.getMaxHealth() != 40.0f) helper.fail("a cartola dá cinco de vida: o de ferro fica com quarenta");
        ItemStack back = GolemPlacerItem.pickUp(g, true);
        if (!back.is(TCItems.GOLEM_PLACERS.get("iron")) || back.getOrDefault(TCComponents.GOLEM_CORE, -1) != 0
                || back.getOrDefault(TCComponents.GOLEM_INVENTORY, List.<GolemPlacerItem.Ghost>of()).getFirst().count() != 200) {
            helper.fail("o sino devia devolver o golem com núcleo e casas");
        }
        helper.succeed();
    }

    /** O núcleo de juntar: vai ao que está no chão, pega e leva para o baú da casa. */
    @GameTest(maxTicks = 800)
    public void aGatheringGolemFillsItsChest(GameTestHelper helper) {
        floor(helper);
        BlockPos chestAt = new BlockPos(1, 2, 1);
        helper.setBlock(chestAt, Blocks.CHEST);
        GolemEntity g = golem(helper, "iron", 2, new BlockPos(1, 2, 2), Direction.SOUTH);
        helper.spawnItem(Items.DIAMOND, 5.5f, 2.5f, 5.5f);
        helper.succeedWhen(() -> {
            if (count(helper.getBlockEntity(chestAt, ChestBlockEntity.class), Items.DIAMOND) == 0) {
                helper.fail("o diamante não chegou; ele carrega " + g.getCarried());
            }
        });
    }

    /** O núcleo de encher: busca no baú marcado até a casa ter a quantidade pedida — nem uma a mais. */
    @GameTest(maxTicks = 900)
    public void aFillingGolemBringsThePreciseAmount(GameTestHelper helper) {
        floor(helper);
        BlockPos homeChest = new BlockPos(1, 2, 1), source = new BlockPos(6, 2, 6);
        helper.setBlock(homeChest, Blocks.CHEST);
        helper.setBlock(source, Blocks.CHEST);
        helper.getBlockEntity(source, ChestBlockEntity.class).setItem(0, new ItemStack(Items.COBBLESTONE, 20));
        GolemEntity g = golem(helper, "iron", 0, new BlockPos(1, 2, 2), Direction.SOUTH);
        g.inventory.setItem(0, new ItemStack(Items.COBBLESTONE, 4));
        mark(helper, g, source, Direction.UP, -1);
        helper.succeedWhen(() -> {
            int n = count(helper.getBlockEntity(homeChest, ChestBlockEntity.class), Items.COBBLESTONE);
            if (n != 4) helper.fail("a casa devia ter quatro pedregulhos, tem " + n + "; ele carrega " + g.getCarried());
        });
    }

    /** O núcleo de esvaziar: tira da casa e guarda no baú marcado. */
    @GameTest(maxTicks = 900)
    public void anEmptyingGolemMovesItOut(GameTestHelper helper) {
        floor(helper);
        BlockPos homeChest = new BlockPos(1, 2, 1), target = new BlockPos(6, 2, 6);
        helper.setBlock(homeChest, Blocks.CHEST);
        helper.setBlock(target, Blocks.CHEST);
        helper.getBlockEntity(homeChest, ChestBlockEntity.class).setItem(0, new ItemStack(Items.COBBLESTONE, 5));
        GolemEntity g = golem(helper, "iron", 1, new BlockPos(1, 2, 2), Direction.SOUTH);
        mark(helper, g, target, Direction.UP, -1);
        helper.succeedWhen(() -> {
            int n = count(helper.getBlockEntity(target, ChestBlockEntity.class), Items.COBBLESTONE);
            if (n != 5) helper.fail("o baú marcado devia ter os cinco, tem " + n);
        });
    }

    /** O núcleo de separar: leva cada coisa ao baú marcado que já tem aquela coisa, e deixa o resto em casa. */
    @GameTest(maxTicks = 900)
    public void aSortingGolemOnlyMovesWhatBelongs(GameTestHelper helper) {
        floor(helper);
        BlockPos homeChest = new BlockPos(1, 2, 1), dirtChest = new BlockPos(6, 2, 6);
        helper.setBlock(homeChest, Blocks.CHEST);
        helper.setBlock(dirtChest, Blocks.CHEST);
        var home = helper.getBlockEntity(homeChest, ChestBlockEntity.class);
        home.setItem(0, new ItemStack(Items.DIRT, 5));
        home.setItem(1, new ItemStack(Items.STONE, 5));
        helper.getBlockEntity(dirtChest, ChestBlockEntity.class).setItem(0, new ItemStack(Items.DIRT, 1));
        GolemEntity g = golem(helper, "iron", 10, new BlockPos(1, 2, 2), Direction.SOUTH);
        mark(helper, g, dirtChest, Direction.UP, -1);
        helper.succeedWhen(() -> {
            var sorted = helper.getBlockEntity(dirtChest, ChestBlockEntity.class);
            if (count(sorted, Items.DIRT) != 6) helper.fail("a terra devia ter ido toda, lá tem " + count(sorted, Items.DIRT));
            if (count(sorted, Items.STONE) != 0 || count(helper.getBlockEntity(homeChest, ChestBlockEntity.class), Items.STONE) != 5) {
                helper.fail("a pedra não tem baú marcado: fica em casa");
            }
        });
    }

    /** O núcleo de colher: colhe o trigo maduro; com a ordem, replanta. */
    @GameTest(maxTicks = 1200)
    public void aHarvestingGolemReplantsWithOrder(GameTestHelper helper) {
        floor(helper);
        BlockPos crop = new BlockPos(4, 2, 4);
        helper.setBlock(crop.below(), Blocks.FARMLAND);
        helper.setBlock(crop, Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7));
        golem(helper, "iron", 3, new BlockPos(2, 2, 2), Direction.UP, 4);
        helper.succeedWhen(() -> {
            var state = helper.getBlockState(crop);
            if (!state.is(Blocks.WHEAT) || state.getValue(CropBlock.AGE) != 0) helper.fail("o trigo devia ter sido colhido e replantado: " + state);
        });
    }

    /** O núcleo de lenhar: derruba o tronco inteiro, do mais longe para perto. */
    @GameTest(maxTicks = 1600)
    public void aLumberGolemFellsTheTree(GameTestHelper helper) {
        floor(helper);
        for (int y = 2; y <= 4; y++) helper.setBlock(new BlockPos(4, y, 4), Blocks.OAK_LOG);
        golem(helper, "iron", 7, new BlockPos(2, 2, 2), Direction.UP);
        helper.succeedWhen(() -> {
            for (int y = 2; y <= 4; y++) if (helper.getBlockState(new BlockPos(4, y, 4)).is(Blocks.OAK_LOG)) helper.fail("ainda há tronco em pé");
        });
    }

    /** O núcleo de guarda: vai atrás do monstro que aparece no alcance. */
    @GameTest(maxTicks = 300)
    public void aGuardGolemTargetsMonsters(GameTestHelper helper) {
        floor(helper);
        GolemEntity g = golem(helper, "iron", 4, new BlockPos(2, 2, 2), Direction.UP);
        Spider spider = helper.spawn(net.minecraft.world.entity.EntityTypes.SPIDER, new BlockPos(6, 2, 6));
        helper.succeedWhen(() -> {
            if (g.getTarget() != spider && spider.isAlive()) helper.fail("o guarda devia ter mirado na aranha");
        });
    }

    /** O açougueiro só abate se sobrar um casal: com dois bichos não mira ninguém; com três, o mais velho. */
    @GameTest(maxTicks = 200)
    public void aButcherKeepsABreedingPair(GameTestHelper helper) {
        floor(helper);
        GolemEntity g = golem(helper, "iron", 9, new BlockPos(1, 2, 1), Direction.UP);
        Cow old = helper.spawn(net.minecraft.world.entity.EntityTypes.COW, new BlockPos(5, 2, 5));
        old.tickCount = 500;
        helper.spawn(net.minecraft.world.entity.EntityTypes.COW, new BlockPos(6, 2, 5));
        helper.runAfterDelay(60, () -> {
            if (g.getTarget() != null) helper.fail("com dois bichos ele não devia mirar ninguém");
            helper.spawn(net.minecraft.world.entity.EntityTypes.COW, new BlockPos(5, 2, 6));
            helper.succeedWhen(() -> {
                if (g.getTarget() != old) helper.fail("com três, devia mirar o mais velho");
            });
        });
    }

    /** O de decantação leva a água da fonte marcada para o tanque da casa (o crisol aceita água por qualquer lado). */
    @GameTest(maxTicks = 900)
    public void aDecantingGolemFillsTheTank(GameTestHelper helper) {
        floor(helper);
        BlockPos crucible = new BlockPos(1, 2, 1), water = new BlockPos(6, 1, 6);
        helper.setBlock(crucible, TCBlocks.CRUCIBLE);
        helper.setBlock(water, Blocks.WATER);
        GolemEntity g = golem(helper, "iron", 5, new BlockPos(1, 2, 2), Direction.SOUTH);
        mark(helper, g, water, Direction.UP, -1);
        helper.succeedWhen(() -> {
            var c = helper.getBlockEntity(crucible, net.thaumcraft.block.entity.CrucibleBlockEntity.class);
            if (c.water() <= 0) {
                var running = g.runningGoals();
                var nav = g.getNavigation();
                helper.fail("o crisol devia ter recebido água; o golem leva " + g.fluidAmount + " mB, está em " + helper.relativePos(g.blockPosition())
                        + " indo para " + (nav.getTargetPos() == null ? null : helper.relativePos(nav.getTargetPos())) + " (feito " + nav.isDone() + ")"
                        + " exato " + g.position().subtract(net.minecraft.world.phys.Vec3.atLowerCornerOf(helper.absolutePos(BlockPos.ZERO)))
                        + " caminho " + (nav.getPath() == null ? "nenhum" : nav.getPath().getNodeCount() + " nós, no " + nav.getPath().getNextNodeIndex()
                        + " fim " + helper.relativePos(nav.getPath().getEndNode().asBlockPos()) + " alcança " + nav.getPath().canReach())
                        + " água " + helper.relativePos(helper.absolutePos(water))
                        + " vendo " + g.itemWatched + ", faltam " + net.thaumcraft.entity.golem.GolemHelper.getMissingLiquids(g)
                        + ", tarefas " + running + ", fonte " + helper.getBlockState(water));
            }
        });
    }

    /** O alquimista leva a essência do jarro da casa para o jarro marcado. */
    @GameTest(maxTicks = 900)
    public void anAlchemyGolemMovesEssentia(GameTestHelper helper) {
        floor(helper);
        BlockPos homeJar = new BlockPos(1, 2, 1), target = new BlockPos(6, 2, 6);
        helper.setBlock(homeJar, TCBlocks.JAR);
        helper.setBlock(target, TCBlocks.JAR);
        helper.getBlockEntity(homeJar, JarBlockEntity.class).addToContainer(Aspects.FIRE, 10);
        GolemEntity g = golem(helper, "iron", 6, new BlockPos(1, 2, 2), Direction.SOUTH);
        mark(helper, g, target, Direction.UP, -1);
        helper.succeedWhen(() -> {
            var jar = helper.getBlockEntity(target, JarBlockEntity.class);
            if (jar.amount() != 10) helper.fail("o jarro marcado devia ter os dez de fogo, tem " + jar.amount() + "; ele leva " + g.essentiaAmount);
        });
    }

    /** A algema: com redstone ela acende, e o golem em cima fica parado. */
    @GameTest(maxTicks = 100)
    public void theFetterStopsTheGolem(GameTestHelper helper) {
        floor(helper);
        helper.setBlock(new BlockPos(3, 1, 3), TCBlocks.GOLEM_FETTER);
        helper.setBlock(new BlockPos(4, 1, 3), Blocks.REDSTONE_BLOCK);
        GolemEntity g = golem(helper, "iron", 2, new BlockPos(3, 2, 3), Direction.UP);
        helper.succeedWhen(() -> {
            if (!helper.getBlockState(new BlockPos(3, 1, 3)).getValue(GolemFetterBlock.POWERED)) helper.fail("a algema devia acender");
            if (!g.inactive) helper.fail("o golem em cima devia parar");
        });
    }

    /** O sino: toca e marca, toca de novo e desmarca; com a ordem no golem ligado, as cores giram. */
    @GameTest
    public void theBellMarksAndCyclesColors(GameTestHelper helper) {
        floor(helper);
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack bell = new ItemStack(TCItems.GOLEM_BELL);
        BlockPos at = helper.absolutePos(new BlockPos(4, 2, 4));
        GolemBellItem.changeMarkers(bell, player, helper.getLevel(), at, 1);
        if (GolemBellItem.getMarkers(bell).size() != 1) helper.fail("devia ter uma marca");
        GolemBellItem.changeMarkers(bell, player, helper.getLevel(), at, 1);
        if (!GolemBellItem.getMarkers(bell).isEmpty()) helper.fail("tocar de novo desmarca");
        GolemEntity g = golem(helper, "iron", 1, new BlockPos(2, 2, 2), Direction.UP, 4);
        bell.set(TCComponents.GOLEM_LINK, new GolemBellItem.Link(g.getId(), g.home(), 1));
        GolemBellItem.changeMarkers(bell, player, helper.getLevel(), at, 1);
        GolemBellItem.changeMarkers(bell, player, helper.getLevel(), at, 1);
        List<Marker> m = GolemBellItem.getMarkers(bell);
        if (m.size() != 1 || m.getFirst().color() != 0) helper.fail("com a ordem, o segundo toque vira a marca branca (cor 0): " + m);
        if (g.getMarkers().size() != 1) helper.fail("as marcas vão direto para o golem ligado");
        helper.succeed();
    }

    /** O golem salvo e carregado volta igual: núcleo, melhorias, marcas e casas fantasmas acima de uma pilha. */
    @GameTest
    public void theGolemSurvivesSaving(GameTestHelper helper) {
        floor(helper);
        GolemEntity g = golem(helper, "thaumium", 0, new BlockPos(2, 2, 2), Direction.UP, 2);
        g.inventory.setItem(3, new ItemStack(Items.STONE, 200));
        mark(helper, g, new BlockPos(5, 2, 5), Direction.NORTH, 7);
        var tag = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING,
                helper.getLevel().registryAccess());
        g.saveWithoutId(tag);
        GolemEntity copy = TCEntities.GOLEM.create(helper.getLevel(), EntitySpawnReason.LOAD);
        copy.load(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,
                helper.getLevel().registryAccess(), tag.buildResult()));
        if (copy.getCore() != 0) helper.fail("o núcleo se perdeu");
        if (copy.getUpgradeAmount(2) != 1) helper.fail("a melhoria de fogo se perdeu");
        if (copy.inventory.slotCount != 12) helper.fail("com fogo, o de encher tem doze casas");
        if (copy.inventory.getItem(3).getCount() != 200) helper.fail("a casa pedia duzentas pedras");
        if (copy.getMarkers().size() != 1 || copy.getMarkers().getFirst().color() != 7) helper.fail("a marca cinza se perdeu");
        if (!copy.home().equals(g.home())) helper.fail("a casa se perdeu");
        copy.discard();
        helper.succeed();
    }
}
