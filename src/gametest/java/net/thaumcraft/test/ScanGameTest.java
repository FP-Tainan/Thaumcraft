package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.aspects.ObjectAspects;
import net.thaumcraft.research.PlayerKnowledge;
import net.thaumcraft.research.ScanManager;

/**
 * As regras do exame do thaumômetro têm de ser as do Thaumcraft 4.2.3.5.
 *
 * <p>São três, e estes testes são a cerca de cada uma: você só entende o que tem cabeça para entender;
 * cada coisa só rende ponto na primeira vez; e descobrir um aspecto de primeira rende dois a mais.
 */
public class ScanGameTest {
    /** A tabela de "do que as coisas são feitas" veio inteira do mod antigo. */
    @GameTest
    public void tableCameFromTheOriginal(GameTestHelper helper) {
        if (ObjectAspects.size() < 90) {
            helper.fail("só " + ObjectAspects.size() + " coisas anotadas; o original tem mais de noventa do jogo base");
        }
        if (!ObjectAspects.missing().isEmpty()) {
            helper.fail("o jogo de hoje não tem: " + String.join(", ", ObjectAspects.missing()));
        }
        // conferidas na mão contra o ConfigAspects do jar da 4.2.3.5 (não o da fonte do GitHub, que inventava)
        AspectList stone = ObjectAspects.of(new ItemStack(Blocks.STONE));
        if (stone.getAmount(Aspects.EARTH) != 2) helper.fail("pedra devia ter terra 2");
        AspectList ironOre = ObjectAspects.of(new ItemStack(Blocks.IRON_ORE));
        if (ironOre.getAmount(Aspects.METAL) != 3 || ironOre.getAmount(Aspects.EARTH) != 1) {
            helper.fail("minério de ferro devia ter terra 1 e metal 3, tem " + ironOre);
        }
        AspectList cobble = ObjectAspects.of(new ItemStack(Blocks.COBBLESTONE));
        if (cobble.getAmount(Aspects.EARTH) != 1 || cobble.getAmount(Aspects.ENTROPY) != 1) {
            helper.fail("pedregulho devia ter terra 1 e perditio 1, tem " + cobble);
        }
        helper.succeed();
    }

    /**
     * O que não tem anotação sai das receitas, como no {@code generateTags}: a picareta de ferro são três
     * lingotes (metal 4 cada) e dois gravetos (arbor 1 cada), três quartos disso, e o bônus de picareta de ferro.
     */
    @GameTest
    public void unlistedThingsComeFromTheirRecipes(GameTestHelper helper) {
        AspectList pick = ObjectAspects.of(new ItemStack(net.minecraft.world.item.Items.IRON_PICKAXE));
        if (pick.getAmount(Aspects.METAL) != 9 || pick.getAmount(Aspects.TREE) != 1 || pick.getAmount(Aspects.MINE) != 3) {
            helper.fail("picareta de ferro devia ter metal 9, arbor 1 e perfodio 3, tem " + pick);
        }
        helper.succeed();
    }

    /** Água e lava também se examinam, com os valores da versão parada do original. */
    @GameTest
    public void liquidsCanBeRead(GameTestHelper helper) {
        AspectList water = ObjectAspects.ofBlock(Blocks.WATER);
        if (water.getAmount(Aspects.WATER) != 4) {
            helper.fail("água devia ter aqua 4, tem " + water.getAmount(Aspects.WATER));
        }
        AspectList lava = ObjectAspects.ofBlock(Blocks.LAVA);
        if (lava.getAmount(Aspects.FIRE) != 3 || lava.getAmount(Aspects.EARTH) != 1) {
            helper.fail("lava devia ter ignis 3 e terra 1");
        }
        helper.succeed();
    }

    /** Coisa caída no chão vale pelo item que ela é, como no original. */
    @GameTest
    public void droppedItemsCountAsTheItem(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absoluteVec(new net.minecraft.world.phys.Vec3(1, 2, 1));
        var dropped = new net.minecraft.world.entity.item.ItemEntity(level, pos.x, pos.y, pos.z,
                new ItemStack(Blocks.IRON_ORE));
        level.addFreshEntity(dropped);

        AspectList fromDrop = ScanManager.aspectsOf(dropped);
        AspectList fromItem = ObjectAspects.of(new ItemStack(Blocks.IRON_ORE));
        if (fromDrop.getAmount(Aspects.METAL) != fromItem.getAmount(Aspects.METAL)
                || fromDrop.getAmount(Aspects.EARTH) != fromItem.getAmount(Aspects.EARTH)) {
            helper.fail("o minério caído no chão devia ter os aspectos do minério");
        }
        // e fica anotado com a mesma chave, para não render ponto duas vezes
        if (!ScanManager.keyOf((net.minecraft.world.entity.Entity) dropped)
                .equals(ScanManager.keyOf(new ItemStack(Blocks.IRON_ORE)))) {
            helper.fail("o caído e o item deviam ficar anotados com a mesma chave");
        }
        dropped.discard();
        helper.succeed();
    }

    /** Sem conhecer os dois de que um aspecto nasce, o aparelho não lê a coisa. */
    @GameTest
    public void youOnlyReadWhatYouCanUnderstand(GameTestHelper helper) {
        PlayerKnowledge knowledge = new PlayerKnowledge();
        // metallum nasce de terra com vitreus; quem não conhece os dois não lê minério de ferro
        AspectList ironOre = ObjectAspects.of(new ItemStack(Blocks.IRON_ORE));
        if (ScanManager.canUnderstand(knowledge, ironOre)) {
            helper.fail("leu o minério sem conhecer os aspectos de que ele é feito");
        }
        if (ScanManager.missingParent(knowledge, ironOre) == null) {
            helper.fail("devia dizer qual aspecto está faltando");
        }
        knowledge.discover(Aspects.EARTH);
        knowledge.discover(Aspects.CRYSTAL);
        if (!ScanManager.canUnderstand(knowledge, ironOre)) {
            helper.fail("conhecendo terra e vitreus, o minério devia ser legível");
        }
        // coisa só de aspecto primário se lê desde o começo
        if (!ScanManager.canUnderstand(new PlayerKnowledge(), ObjectAspects.of(new ItemStack(Blocks.STONE)))) {
            helper.fail("pedra é só terra: devia dar para ler de cara");
        }
        helper.succeed();
    }

    /** Descobrir um aspecto de primeira rende dois pontos a mais, e o teto encolhe o ganho. */
    @GameTest
    public void firstDiscoveryIsWorthMore(GameTestHelper helper) {
        PlayerKnowledge knowledge = new PlayerKnowledge();
        int first = knowledge.award(Aspects.EARTH, 2);
        if (first != 4) helper.fail("a primeira vez devia render 4 (2 mais 2), rendeu " + first);
        int second = knowledge.award(Aspects.EARTH, 2);
        if (second != 2) helper.fail("depois devia render o valor cheio, rendeu " + second);

        // acima do teto o ganho vira a raiz; bem acima, um só
        PlayerKnowledge rico = new PlayerKnowledge();
        rico.discover(Aspects.FIRE);
        rico.award(Aspects.FIRE, PlayerKnowledge.ASPECT_CAP);
        int pouco = rico.award(Aspects.FIRE, 9);
        if (pouco != 3) helper.fail("no teto, nove devia virar três, virou " + pouco);
        helper.succeed();
    }

    /** Cada coisa só rende ponto uma vez; da segunda em diante o aparelho só mostra. */
    @GameTest
    public void eachThingPaysOnlyOnce(GameTestHelper helper) {
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        PlayerKnowledge knowledge = net.thaumcraft.research.Knowledges.of(player);
        knowledge.discover(Aspects.EARTH);
        net.thaumcraft.research.Knowledges.save(player, knowledge);

        AspectList stone = ObjectAspects.of(new ItemStack(Blocks.STONE));
        String key = ScanManager.keyOf(new ItemStack(Blocks.STONE));
        ScanManager.Result um = ScanManager.scan(player, key, stone, Component());
        if (!um.scanned()) helper.fail("o primeiro exame devia valer");
        int depois = net.thaumcraft.research.Knowledges.of(player).points(Aspects.EARTH);

        ScanManager.scan(player, key, stone, Component());
        int agora = net.thaumcraft.research.Knowledges.of(player).points(Aspects.EARTH);
        if (agora != depois) helper.fail("o segundo exame da mesma coisa não devia render ponto");
        helper.succeed();
    }

    private static net.minecraft.network.chat.Component Component() {
        return new ItemStack(Items.STONE).getHoverName();
    }
}
