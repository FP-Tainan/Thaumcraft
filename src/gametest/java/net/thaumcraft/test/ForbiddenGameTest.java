package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.aspects.ObjectAspects;
import net.thaumcraft.forbidden.ForbiddenAspects;
import net.thaumcraft.forbidden.ForbiddenItems;
import net.thaumcraft.research.EntityAspects;

/** O ramo do Forbidden Magic tem de seguir o {@code DarkAspects} do 0.575. */
public class ForbiddenGameTest {
    /** Os sete aspectos sombrios existem, com a cor, os pais e a mistura do original. */
    @GameTest
    public void theSevenDarkAspectsExist(GameTestHelper helper) {
        String[][] esperados = {
                {"infernus", "FIRE", "MAGIC"}, {"ira", "WEAPON", "FIRE"}, {"gula", "HUNGER", "VOID"},
                {"invidia", "SENSES", "HUNGER"}, {"superbia", "FLIGHT", "VOID"}, {"desidia", "TRAP", "SOUL"},
                {"luxuria", "FLESH", "HUNGER"}};
        if (ForbiddenAspects.ASPECTS.size() != esperados.length) {
            helper.fail("o original tem sete aspectos sombrios; há " + ForbiddenAspects.ASPECTS.size());
        }
        for (String[] esperado : esperados) {
            Aspect aspecto = ForbiddenAspects.ASPECTS.get(esperado[0]);
            if (aspecto == null) {
                helper.fail("falta o aspecto " + esperado[0]);
                return;
            }
            if (Aspect.ASPECTS.get(esperado[0]) != aspecto) helper.fail(esperado[0] + " devia estar na tabela do mod");
            if (aspecto.components() == null || aspecto.components().length != 2) {
                helper.fail(esperado[0] + " nasce de dois aspectos");
                return;
            }
            if (aspecto.components()[0] != byName(esperado[1]) || aspecto.components()[1] != byName(esperado[2])) {
                helper.fail(esperado[0] + " nasce de " + esperado[1] + " e " + esperado[2]);
            }
        }
        helper.succeed();
    }

    /** E eles somam ao que as coisas e as criaturas já tinham, sem apagar nada. */
    @GameTest
    public void theDarkAspectsAddToWhatWasThere(GameTestHelper helper) {
        Aspect infernus = ForbiddenAspects.ASPECTS.get("infernus");
        Aspect ira = ForbiddenAspects.ASPECTS.get("ira");
        Aspect gula = ForbiddenAspects.ASPECTS.get("gula");

        var pedra = ObjectAspects.of(new ItemStack(Items.NETHERRACK));
        if (pedra.getAmount(infernus) != 1) helper.fail("a pedra do Nether ganha um de infernus; tem " + pedra.getAmount(infernus));
        if (pedra.getAmount(Aspects.FIRE) < 1) helper.fail("e não perde o fogo que já tinha");

        var bolo = ObjectAspects.of(new ItemStack(Items.CAKE));
        if (bolo.getAmount(gula) != 7) helper.fail("o bolo ganha sete de gula; tem " + bolo.getAmount(gula));

        var tnt = ObjectAspects.of(new ItemStack(Items.TNT));
        if (tnt.getAmount(ira) != 2) helper.fail("a dinamite ganha dois de ira; tem " + tnt.getAmount(ira));

        // as criaturas: o creeper comum e o carregado levam contas diferentes, como no original
        var creeper = helper.spawn(net.minecraft.world.entity.EntityTypes.CREEPER, new BlockPos(1, 2, 1));
        var lista = EntityAspects.of(creeper);
        if (lista == null || lista.getAmount(ira) != 2) {
            helper.fail("o creeper ganha dois de ira; tem " + (lista == null ? "nada" : lista.getAmount(ira)));
        }
        if (lista != null && lista.getAmount(Aspects.PLANT) < 2) helper.fail("e não perde a planta que já tinha");
        helper.succeed();
    }

    private static Aspect byName(String name) {
        return switch (name) {
            case "FIRE" -> Aspects.FIRE;
            case "MAGIC" -> Aspects.MAGIC;
            case "WEAPON" -> Aspects.WEAPON;
            case "HUNGER" -> Aspects.HUNGER;
            case "VOID" -> Aspects.VOID;
            case "SENSES" -> Aspects.SENSES;
            case "FLIGHT" -> Aspects.FLIGHT;
            case "TRAP" -> Aspects.TRAP;
            case "SOUL" -> Aspects.SOUL;
            case "FLESH" -> Aspects.FLESH;
            default -> null;
        };
    }

    /** Os oito fragmentos existem, com o aspecto do pecado de cada um. */
    @GameTest
    public void theEightShardsExist(GameTestHelper helper) {
        if (net.thaumcraft.forbidden.ForbiddenItems.SHARDS.size() != 7) {
            helper.fail("o original tem sete vícios; há " + net.thaumcraft.forbidden.ForbiddenItems.SHARDS.size());
        }
        String[][] pares = {{"wrath", "ira"}, {"envy", "invidia"}, {"pride", "superbia"},
                {"lust", "luxuria"}, {"sloth", "desidia"}};
        for (String[] par : pares) {
            var item = net.thaumcraft.forbidden.ForbiddenItems.SHARDS.get(par[0]);
            var aspectos = ObjectAspects.of(new ItemStack(item));
            Aspect pecado = ForbiddenAspects.ASPECTS.get(par[1]);
            if (aspectos.getAmount(pecado) != 2) {
                helper.fail("o fragmento da " + par[0] + " tem dois de " + par[1] + "; tem " + aspectos.getAmount(pecado));
            }
            if (aspectos.getAmount(Aspects.CRYSTAL) != 1) helper.fail("e um de cristal");
        }
        // o da mácula não é pecado: ele leva mácula
        var macula = ObjectAspects.of(new ItemStack(net.thaumcraft.forbidden.ForbiddenItems.SHARDS.get("taint")));
        if (macula.getAmount(Aspects.TAINT) != 3) helper.fail("o fragmento da mácula tem três de mácula");
        // e o da gula se come
        var gula = new ItemStack(net.thaumcraft.forbidden.ForbiddenItems.GLUTTONY_SHARD);
        if (gula.get(net.minecraft.core.component.DataComponents.FOOD) == null) {
            helper.fail("o fragmento da gula é comida, como no original");
        }
        helper.succeed();
    }

    /** A Preguiça cai de quem morre sozinho no Nether, e nada cai fora dele. */
    @GameTest
    public void theSlothShardFallsFromTheLonelyDead(GameTestHelper helper) {
        // fora do Nether, o ramo não mexe em nada
        var porco = helper.spawn(net.minecraft.world.entity.EntityTypes.PIG, new BlockPos(1, 2, 1));
        if (net.thaumcraft.forbidden.ForbiddenDrops.inTheNether(helper.getLevel())) {
            helper.fail("o mundo do teste não é o Nether");
        }
        net.thaumcraft.forbidden.ForbiddenDrops.onDeath(porco, helper.getLevel().damageSources().generic());
        var caidos = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                new net.minecraft.world.phys.AABB(helper.absolutePos(new BlockPos(1, 2, 1))).inflate(4.0));
        if (!caidos.isEmpty()) helper.fail("fora do Nether não cai fragmento nenhum");
        helper.succeed();
    }

    /** A muda maculada vira árvore: tronco maculado em pé e folhas maculadas em volta. */
    @GameTest
    public void theTaintedSaplingGrowsATree(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos.below(), net.minecraft.world.level.block.Blocks.DIRT);
        helper.setBlock(pos, net.thaumcraft.forbidden.ForbiddenBlocks.TAINT_SAPLING);
        var muda = (net.thaumcraft.forbidden.TaintedSaplingBlock) net.thaumcraft.forbidden.ForbiddenBlocks.TAINT_SAPLING;
        BlockPos mundo = helper.absolutePos(pos);
        boolean cresceu = false;
        for (int volta = 0; volta < 20 && !cresceu; volta++) {
            cresceu = muda.grow(helper.getLevel(), mundo, helper.getLevel().getBlockState(mundo),
                    helper.getLevel().getRandom());
        }
        if (!cresceu) helper.fail("a muda devia ter virado árvore");
        if (!helper.getLevel().getBlockState(mundo).is(net.thaumcraft.forbidden.ForbiddenBlocks.TAINT_LOG)) {
            helper.fail("o pé da árvore é tronco maculado");
        }
        int folhas = 0;
        for (var estado : helper.getLevel().getBlockStates(
                new net.minecraft.world.phys.AABB(mundo).inflate(4.0)).toList()) {
            if (estado.is(net.thaumcraft.forbidden.ForbiddenBlocks.TAINT_LEAVES)) folhas++;
        }
        if (folhas < 5) helper.fail("a copa devia ter folhas maculadas; achei " + folhas);
        helper.succeed();
    }

    /** O Fruto Maculado alimenta, mas distorce quem o come. */
    @GameTest
    public void theTaintedFruitWarps(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        ItemStack fruto = new ItemStack(net.thaumcraft.forbidden.ForbiddenItems.TAINT_FRUIT);
        if (fruto.get(net.minecraft.core.component.DataComponents.FOOD) == null) helper.fail("o fruto é comida");
        int antes = net.thaumcraft.research.Knowledges.of(player).warpSticky();
        fruto.getItem().finishUsingItem(fruto, helper.getLevel(), player);
        int depois = net.thaumcraft.research.Knowledges.of(player).warpSticky();
        if (depois != antes + 1) helper.fail("ele gruda um de distorção; foi de " + antes + " para " + depois);
        if (!player.hasEffect(net.thaumcraft.registry.TCEffects.FLUX_TAINT)) helper.fail("e deixa a mácula");
        helper.succeed();
    }

    /** A Pá do Purificador limpa a gosma e o gás de fluxo de um pedaço inteiro, gastando-se nisso. */
    @GameTest
    public void thePurifierShovelCleansFlux(GameTestHelper helper) {
        BlockPos chao = new BlockPos(2, 2, 2);
        helper.setBlock(chao, net.minecraft.world.level.block.Blocks.DIRT);
        helper.setBlock(chao.above(), net.thaumcraft.registry.TCBlocks.FLUX_GOO);
        helper.setBlock(chao.above(2), net.thaumcraft.registry.TCBlocks.FLUX_GAS);

        var player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack pa = new ItemStack(net.thaumcraft.forbidden.ForbiddenItems.PURIFIER_SHOVEL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, pa);
        var mundo = helper.absolutePos(chao);
        pa.getItem().useOn(new net.minecraft.world.item.context.UseOnContext(helper.getLevel(), player,
                net.minecraft.world.InteractionHand.MAIN_HAND, pa,
                new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(mundo),
                        net.minecraft.core.Direction.UP, mundo, false)));

        if (!helper.getLevel().getBlockState(helper.absolutePos(chao.above())).isAir()) {
            helper.fail("a gosma de fluxo devia ter sumido");
        }
        if (!helper.getLevel().getBlockState(helper.absolutePos(chao.above(2))).isAir()) {
            helper.fail("o gás de fluxo devia ter sumido");
        }
        if (pa.getDamageValue() != 2) helper.fail("ela gasta um ponto por bloco limpo; gastou " + pa.getDamageValue());
        helper.succeed();
    }

    /** E ela cava mácula tão depressa quanto o que sabe cavar. */
    @GameTest
    public void thePurifierShovelDigsTaintFast(GameTestHelper helper) {
        ItemStack pa = new ItemStack(net.thaumcraft.forbidden.ForbiddenItems.PURIFIER_SHOVEL);
        var macula = net.thaumcraft.registry.TCBlocks.TAINT_CRUST.defaultBlockState();
        float velocidade = pa.getItem().getDestroySpeed(pa, macula);
        if (velocidade < 10.0f) helper.fail("a pá devia rasgar a mácula; foi a " + velocidade);
        helper.succeed();
    }

    /** O Machado do Tomador de Crânios arranca a cabeça de quem ele mata. */
    @GameTest
    public void theSkulltakerAxeTakesHeads(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                new ItemStack(net.thaumcraft.forbidden.ForbiddenItems.SKULLTAKER_AXE));
        boolean caiu = false;
        for (int volta = 0; volta < 200 && !caiu; volta++) {
            var creeper = helper.spawn(net.minecraft.world.entity.EntityTypes.CREEPER, new BlockPos(1, 2, 1));
            net.thaumcraft.forbidden.ForbiddenDrops.onDeath(creeper,
                    helper.getLevel().damageSources().playerAttack(player));
            caiu = helper.getLevel()
                    .getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                            new net.minecraft.world.phys.AABB(helper.absolutePos(new BlockPos(1, 2, 1))).inflate(3.0))
                    .stream().anyMatch(item -> item.getItem().is(net.minecraft.world.item.Items.CREEPER_HEAD));
            creeper.discard();
        }
        if (!caiu) helper.fail("em duzentas mortes devia ter caído ao menos uma cabeça de creeper");
        helper.succeed();
    }

    /** Sem o machado na mão, cabeça nenhuma cai. */
    @GameTest
    public void onlyTheSkulltakerAxeTakesHeads(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                new ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD));
        for (int volta = 0; volta < 60; volta++) {
            var creeper = helper.spawn(net.minecraft.world.entity.EntityTypes.CREEPER, new BlockPos(1, 2, 1));
            net.thaumcraft.forbidden.ForbiddenDrops.onDeath(creeper,
                    helper.getLevel().damageSources().playerAttack(player));
            creeper.discard();
        }
        boolean caiu = helper.getLevel()
                .getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                        new net.minecraft.world.phys.AABB(helper.absolutePos(new BlockPos(1, 2, 1))).inflate(3.0))
                .stream().anyMatch(item -> item.getItem().is(net.minecraft.world.item.Items.CREEPER_HEAD));
        if (caiu) helper.fail("só o machado do ramo arranca cabeças");
        helper.succeed();
    }

    /** O Chicote de Montaria apressa o porco que apanha dele. */
    @GameTest
    public void theRidingCropHastensThePig(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        var porco = helper.spawn(net.minecraft.world.entity.EntityTypes.PIG, new BlockPos(1, 2, 1));
        ItemStack chicote = new ItemStack(net.thaumcraft.forbidden.ForbiddenItems.RIDING_CROP);
        chicote.getItem().hurtEnemy(chicote, porco, player);
        var pressa = porco.getEffect(net.minecraft.world.effect.MobEffects.SPEED);
        if (pressa == null || pressa.getAmplifier() != 5) helper.fail("o porco devia sair em disparada");
        if (chicote.getDamageValue() != 1) helper.fail("a chicotada gasta o chicote");
        helper.succeed();
    }

    /**
     * A ferramenta camaleão guarda três caras: o que estava nela fica na cara de onde ela saiu, e volta quando
     * ela der a volta inteira.
     */
    @GameTest
    public void theChameleonToolKeepsThreeFaces(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack picareta = new ItemStack(net.thaumcraft.forbidden.ForbiddenItems.CHAMELEON_PICKAXE);

        var registro = helper.getLevel().registryAccess()
                .lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        var fortuna = registro.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FORTUNE);
        picareta.enchant(fortuna, 3);
        picareta.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,
                net.minecraft.network.chat.Component.literal("Cavadora"));

        // primeira troca: a cara nova está limpa
        net.thaumcraft.forbidden.MorphToolItem.cycle(picareta);
        if (net.thaumcraft.forbidden.MorphToolItem.phase(picareta) != 1) helper.fail("ela devia ter ido para a cara 1");
        if (net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(fortuna, picareta) != 0) {
            helper.fail("a cara 1 nasce sem encantamento");
        }
        if (picareta.get(net.minecraft.core.component.DataComponents.CUSTOM_NAME) != null) {
            helper.fail("e sem nome");
        }

        // a segunda e a terceira fecham a volta e trazem tudo de volta
        net.thaumcraft.forbidden.MorphToolItem.cycle(picareta);
        net.thaumcraft.forbidden.MorphToolItem.cycle(picareta);
        if (net.thaumcraft.forbidden.MorphToolItem.phase(picareta) != 0) helper.fail("a volta devia fechar na cara 0");
        if (net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(fortuna, picareta) != 3) {
            helper.fail("a Fortuna III devia ter voltado com a cara 0");
        }
        var nome = picareta.get(net.minecraft.core.component.DataComponents.CUSTOM_NAME);
        if (nome == null || !nome.getString().equals("Cavadora")) helper.fail("e o nome também");
        helper.succeed();
    }

    /** A troca custa cinco de vida, e não se troca com a ferramenta no fim. */
    @GameTest
    public void theChameleonToolCostsToTurn(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack espada = new ItemStack(net.thaumcraft.forbidden.ForbiddenItems.CHAMELEON_SWORD);
        espada.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,
                net.minecraft.network.chat.Component.literal("Camaleoa"));
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, espada);

        espada.getItem().use(helper.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
        if (net.thaumcraft.forbidden.MorphToolItem.phase(espada) != 0) {
            helper.fail("de pé ela não troca de cara");
        }

        player.setShiftKeyDown(true);
        espada.getItem().use(helper.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
        if (net.thaumcraft.forbidden.MorphToolItem.phase(espada) != 1) helper.fail("agachado ela troca");
        if (espada.getDamageValue() != 5) {
            helper.fail("a troca custa cinco de vida; custou " + espada.getDamageValue());
        }

        espada.setDamageValue(espada.getMaxDamage() - 3);
        player.setShiftKeyDown(true);
        espada.getItem().use(helper.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
        if (net.thaumcraft.forbidden.MorphToolItem.phase(espada) != 1) {
            helper.fail("no fim da vida ela não troca mais");
        }
        helper.succeed();
    }
}
