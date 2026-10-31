package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.thaumcraft.occulta.CritterSnareBlock;
import net.thaumcraft.occulta.GrassperBlockEntity;
import net.thaumcraft.occulta.MutandisItem;
import net.thaumcraft.occulta.MutatingSprigItem;
import net.thaumcraft.occulta.Mutations;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.OccultaRituals;

import java.util.List;

/**
 * O <b>Galho Mutante</b>: as quinze mutações e de quem elas são.
 *
 * <p>A prova que carrega a fatia é a de <b>quem as faz</b>. Elas viveram emprestadas no Mutandis Extremis
 * enquanto o galho não estava portado, e o empréstimo estava declarado; se ele ficasse, o Mutandis seria um
 * pó que faz tudo e o galho seria um enfeite.
 */
public class OccultaMutatingSprigGameTest {
    /** Põe um Apanha-Erva com aquilo na boca. */
    private static void apanhaErva(ServerLevel level, BlockPos onde,
                                   net.minecraft.world.item.Item oquê) {
        level.setBlockAndUpdate(onde, OccultaBlocks.GRASSPER.defaultBlockState());
        if (level.getBlockEntity(onde) instanceof GrassperBlockEntity planta) {
            planta.põe(new ItemStack(oquê));
        }
    }

    /** As quatro diagonais, com uma coisa em cada boca, pela ordem. */
    private static void nasQuinas(ServerLevel level, BlockPos onde,
                                  net.minecraft.world.item.Item... quais) {
        BlockPos[] quinas = Mutations.quinas(onde);
        for (int i = 0; i < quinas.length; i++) apanhaErva(level, quinas[i], quais[i]);
    }

    private static void piso(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 0; x < 7; x++) {
            for (int z = 0; z < 7; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /**
     * <b>O galho existe, e as mutações são dele.</b>
     *
     * <p>Quinze toques, e o Mutandis Extremis já <b>não</b> faz nenhum dos desenhos: o que lhe ficou é a
     * troca sorteada de plantas e as duas do chão, que são as do {@code useMutandis} do original.
     */
    @GameTest(maxTicks = 20)
    public void theRitualsBelongToTheSprigNow(GameTestHelper helper) {
        ItemStack galho = new ItemStack(OccultaItems.MUTATING_SPRIG);
        if (!(galho.getItem() instanceof MutatingSprigItem)) {
            helper.fail("o Galho Mutante devia estar registado");
            return;
        }
        if (galho.getMaxDamage() != MutatingSprigItem.TOQUES) {
            helper.fail("quinze toques: tem " + galho.getMaxDamage());
            return;
        }
        if (galho.getMaxStackSize() != 1) helper.fail("e um só por casa");

        // e o Extremis continua trocando plantas, que é o que é dele
        var extremis = (MutandisItem) OccultaItems.MUTANDIS_EXTREMIS;
        if (!extremis.list().contains(Blocks.WHEAT)) helper.fail("o Extremis ainda troca trigo");
        helper.succeed();
    }

    /**
     * <b>Um baú armadilhado dá <i>uma</i> Arca de Sanguessugas</b>, e ela nasce no lugar dele.
     *
     * <p>É o detalhe que o porte tinha errado enquanto a mutação morava no Mutandis: lá nasciam
     * <b>quatro</b> arcas, uma em cada lado. O original limpa os quatro lados e põe <b>uma</b> arca no meio
     * — que é a diferença entre uma receita e uma máquina de arcas.
     */
    @GameTest(maxTicks = 40)
    public void oneTrappedChestMakesOneLeechChest(GameTestHelper helper) {
        piso(helper);
        var level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(3, 2, 3));

        level.setBlockAndUpdate(onde, Blocks.TRAPPED_CHEST.defaultBlockState());
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            level.setBlockAndUpdate(onde.relative(lado).below(), Blocks.WATER.defaultBlockState());
            trepadeira(level, onde, lado);
        }
        if (!Mutations.éBaúArmadilhado(level, onde)) {
            helper.fail("o desenho do baú armadilhado devia estar de pé");
            return;
        }

        toca(helper, onde);
        if (!level.getBlockState(onde).is(OccultaBlocks.LEECH_CHEST)) {
            helper.fail("a arca nasce no lugar do baú");
            return;
        }
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(onde.relative(lado)).is(OccultaBlocks.LEECH_CHEST)) {
                helper.fail("e é uma só: os lados ficam vazios");
                return;
            }
        }
        helper.succeed();
    }

    /**
     * <b>Trigo maduro cercado de Algodão-Fiapo vira Losna.</b>
     *
     * <p>É a única mutação que não pede Apanha-Erva nenhum nem gasta bicho nenhum, e é por isso que é a
     * primeira que se faz. A losna não nasce no mundo: ou sai daqui, ou não se tem.
     */
    @GameTest(maxTicks = 40)
    public void ripeWheatBecomesWormwood(GameTestHelper helper) {
        piso(helper);
        var level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(3, 2, 3));

        level.setBlockAndUpdate(onde.below(), Blocks.FARMLAND.defaultBlockState());
        level.setBlockAndUpdate(onde, Blocks.WHEAT.defaultBlockState()
                .setValue(BlockStateProperties.AGE_7, 7));
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            // o Algodão Sonhador só pega em terra, e a prova tem de lhe dar o chão dele
            level.setBlockAndUpdate(onde.relative(lado).below(), Blocks.DIRT.defaultBlockState());
            level.setBlockAndUpdate(onde.relative(lado), OccultaBlocks.WISPY_COTTON.defaultBlockState());
        }
        if (Mutations.éTrigoDeLosna(level, onde)) {
            helper.fail("sem a água nas quinas ainda não é");
            return;
        }
        for (BlockPos quina : Mutations.quinas(onde)) {
            level.setBlockAndUpdate(quina.below(), Blocks.WATER.defaultBlockState());
        }
        if (!Mutations.éTrigoDeLosna(level, onde)) {
            helper.fail("com a água nas quatro quinas, é");
            return;
        }

        // e trigo verde não serve: a mutação é da colheita e não da sementeira
        level.setBlockAndUpdate(onde, Blocks.WHEAT.defaultBlockState()
                .setValue(BlockStateProperties.AGE_7, 6));
        if (Mutations.éTrigoDeLosna(level, onde)) {
            helper.fail("trigo verde não serve");
            return;
        }
        level.setBlockAndUpdate(onde, Blocks.WHEAT.defaultBlockState()
                .setValue(BlockStateProperties.AGE_7, 7));

        toca(helper, onde);
        if (!level.getBlockState(onde).is(OccultaBlocks.WORMWOOD)) {
            helper.fail("e o que fica é a losna");
            return;
        }
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            if (!level.getBlockState(onde.relative(lado)).isAir()) {
                helper.fail("o algodão gastou-se na troca");
                return;
            }
        }
        helper.succeed();
    }

    /**
     * <b>Mandrágoras maduras à volta de uma teia viram Mentedrágoras</b> — e custam duas vidas.
     *
     * <p>A conta tem sentido: para fazer uma planta que anda e explode, dá-se uma planta que anda e uma
     * coisa que explode. É a mutação mais clara do mod a dizer o que está fazendo.
     */
    @GameTest(maxTicks = 60)
    public void mandrakesAndACreeperMakeMindrakes(GameTestHelper helper) {
        piso(helper);
        var level = helper.getLevel();
        BlockPos aqui = new BlockPos(3, 2, 3);
        BlockPos onde = helper.absolutePos(aqui);

        level.setBlockAndUpdate(onde.below(), Blocks.WATER.defaultBlockState());
        level.setBlockAndUpdate(onde, Blocks.COBWEB.defaultBlockState());
        var mandrágora = (CropBlock) OccultaBlocks.MANDRAKE;
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            level.setBlockAndUpdate(onde.relative(lado).below(), Blocks.FARMLAND.defaultBlockState());
            level.setBlockAndUpdate(onde.relative(lado),
                    mandrágora.getStateForAge(mandrágora.getMaxAge()));
        }
        nasQuinas(level, onde, OccultaItems.MUTANDIS_EXTREMIS, OccultaItems.MUTANDIS_EXTREMIS,
                OccultaItems.ATTUNED_STONE_CHARGED, OccultaItems.FOCUSED_WILL);

        /* A guarda de sempre: uma peça que caiu calada faria a prova mentir sobre a receita. */
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            if (!Mutations.maduro(level, onde.relative(lado), OccultaBlocks.MANDRAKE)) {
                helper.fail("as quatro mandrágoras ficam de pé e maduras");
                return;
            }
        }
        if (Mutations.quantosSeguram(level, onde, OccultaItems.FOCUSED_WILL) != 1) {
            helper.fail("e a Vontade Focada está numa das bocas");
            return;
        }

        if (Mutations.éTeiaDeMentedrágora(level, onde)) {
            helper.fail("sem bicho nenhum não é nada");
            return;
        }
        helper.spawn(EntityTypes.CREEPER, aqui);
        if (Mutations.éTeiaDeMentedrágora(level, onde)) {
            helper.fail("só com o creeper também não");
            return;
        }
        helper.spawn(net.thaumcraft.occulta.OccultaEntities.MANDRAKE, aqui);
        if (!Mutations.éTeiaDeMentedrágora(level, onde)) {
            helper.fail("com os dois, é");
            return;
        }

        toca(helper, onde);
        if (!level.getBlockState(onde).isAir()) {
            helper.fail("a teia some");
            return;
        }
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            if (!level.getBlockState(onde.relative(lado)).is(OccultaBlocks.MINDRAKE)) {
                helper.fail("e as quatro mandrágoras viram mentedrágoras");
                return;
            }
        }
        for (BlockPos quina : Mutations.quinas(onde)) {
            if (level.getBlockEntity(quina) instanceof GrassperBlockEntity planta
                    && !planta.naBoca().isEmpty()) {
                helper.fail("e os Apanha-Ervas ficam de boca vazia");
                return;
            }
        }
        helper.succeed();
    }

    /**
     * <b>Um Apanha-Bicho com uma lepisma dentro vira uma Lêndea Parasita.</b>
     *
     * <p>É a única mutação que não troca o que está à volta: o que muda é o <b>próprio</b> Apanha-Bicho. E
     * é a única que pede o <b>Mutandis comum</b> e não o Extremis — a lêndea é a coisa mais barata que se
     * faz com um desenho, e é de propósito.
     */
    @GameTest(maxTicks = 60)
    public void aSilverfishSnareBecomesALouse(GameTestHelper helper) {
        piso(helper);
        var level = helper.getLevel();
        BlockPos aqui = new BlockPos(3, 2, 3);
        BlockPos onde = helper.absolutePos(aqui);

        level.setBlockAndUpdate(onde.below(), Blocks.DIRT.defaultBlockState());
        level.setBlockAndUpdate(onde, OccultaBlocks.CRITTER_SNARE.defaultBlockState()
                .setValue(CritterSnareBlock.APANHADO, CritterSnareBlock.Caught.SILVERFISH));
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            trepadeira(level, onde, lado);
        }
        level.setBlockAndUpdate(onde.north().below(), Blocks.WATER.defaultBlockState());
        nasQuinas(level, onde, OccultaItems.MUTANDIS, OccultaItems.MUTANDIS,
                OccultaItems.ATTUNED_STONE_CHARGED, OccultaItems.DOG_TONGUE);

        if (!Mutations.éLaçoDeLêndea(level, onde)) {
            helper.fail("o desenho da lêndea devia estar de pé");
            return;
        }

        // e um Apanha-Bicho vazio não serve: o que vira lêndea é a lepisma que estava lá dentro
        level.setBlockAndUpdate(onde, OccultaBlocks.CRITTER_SNARE.defaultBlockState());
        if (Mutations.éLaçoDeLêndea(level, onde)) {
            helper.fail("um Apanha-Bicho vazio não vira nada");
            return;
        }
        level.setBlockAndUpdate(onde, OccultaBlocks.CRITTER_SNARE.defaultBlockState()
                .setValue(CritterSnareBlock.APANHADO, CritterSnareBlock.Caught.SILVERFISH));

        toca(helper, onde);
        if (!level.getBlockState(onde).isAir()) {
            helper.fail("o Apanha-Bicho some");
            return;
        }
        if (helper.getLevel()
                .getEntitiesOfClass(net.thaumcraft.occulta.louse.LouseEntity.class,
                        new net.minecraft.world.phys.AABB(onde).inflate(2.0)).isEmpty()) {
            helper.fail("e no lugar dele fica uma lêndea");
            return;
        }
        helper.succeed();
    }

    /** <b>E o galho gasta-se</b>: cada toque tira um, e ao fim de quinze ele parte-se. */
    @GameTest(maxTicks = 40)
    public void everyTouchWearsItDown(GameTestHelper helper) {
        piso(helper);
        var level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(3, 2, 3));
        level.setBlockAndUpdate(onde, Blocks.GRASS_BLOCK.defaultBlockState());

        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack galho = new ItemStack(OccultaItems.MUTATING_SPRIG);
        quem.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, galho);

        galho.getItem().useOn(new net.minecraft.world.item.context.UseOnContext(level, quem,
                net.minecraft.world.InteractionHand.MAIN_HAND, galho,
                new net.minecraft.world.phys.BlockHitResult(
                        net.minecraft.world.phys.Vec3.atCenterOf(onde), Direction.UP, onde, false)));

        if (!level.getBlockState(onde).is(Blocks.MYCELIUM)) {
            helper.fail("a grama vira micélio, e sempre — o galho não joga moeda ao ar");
            return;
        }
        if (galho.getOrDefault(DataComponents.DAMAGE, 0) != MutatingSprigItem.POR_TOQUE) {
            helper.fail("e o toque gasta um: gastou "
                    + galho.getOrDefault(DataComponents.DAMAGE, 0));
            return;
        }
        helper.succeed();
    }

    /** <b>E ele sai do caldeirão</b>: Mutandis Extremis com verruga do Nether e um galho de Ent. */
    @GameTest(maxTicks = 20)
    public void theCauldronMakesIt(GameTestHelper helper) {
        ItemStack saiu = OccultaRituals.result(OccultaItems.MUTANDIS_EXTREMIS,
                List.of(Items.NETHER_WART, OccultaItems.ENT_BRANCH));
        if (saiu.isEmpty() || !saiu.is(OccultaItems.MUTATING_SPRIG)) {
            helper.fail("o caldeirão devia dar o galho");
            return;
        }
        // e a Gota de Sorte, que pede mais coisas, continua ganhando quando elas lá estão
        ItemStack gota = OccultaRituals.result(OccultaItems.MUTANDIS_EXTREMIS,
                List.of(OccultaItems.MANDRAKE_ROOT, Items.NETHER_WART, OccultaItems.TEAR_OF_THE_GODDESS,
                        OccultaItems.REFINED_EVIL));
        if (!gota.is(OccultaItems.DROP_OF_LUCK)) {
            helper.fail("e a receita mais rica continua ganhando da mais pobre");
        }
        helper.succeed();
    }

    /**
     * Uma trepadeira presa a uma parede.
     *
     * <p>Ela precisa de um lado <b>firme</b> a que se agarrar, e nenhum dos blocos destes desenhos o é. A
     * prova põe uma pedra uma casa mais para fora e prende a trepadeira nela — sem isso ela cai na primeira
     * vez que um vizinho mexer, e o desenho desfaz-se calado.
     */
    private static void trepadeira(ServerLevel level, BlockPos onde, Direction lado) {
        level.setBlockAndUpdate(onde.relative(lado, 2), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(onde.relative(lado), Blocks.VINE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.VineBlock.PROPERTY_BY_DIRECTION.get(lado), true));
    }

    /** Toca com o galho naquela casa, como quem o usa. */
    private static void toca(GameTestHelper helper, BlockPos onde) {
        var level = helper.getLevel();
        var quem = helper.makeMockServerPlayerInLevel();
        ItemStack galho = new ItemStack(OccultaItems.MUTATING_SPRIG);
        quem.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, galho);
        galho.getItem().useOn(new net.minecraft.world.item.context.UseOnContext(level, quem,
                net.minecraft.world.InteractionHand.MAIN_HAND, galho,
                new net.minecraft.world.phys.BlockHitResult(
                        net.minecraft.world.phys.Vec3.atCenterOf(onde), Direction.UP, onde, false)));
    }
}
