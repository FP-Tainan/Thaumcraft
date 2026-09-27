package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.rite.ActiveRite;
import net.thaumcraft.occulta.rite.RiteRegistry;
import net.thaumcraft.occulta.rite.RiteStep;
import net.thaumcraft.occulta.rite.Rites;

import java.util.List;

/**
 * Os quatro ritos do tempo e da terra: a tempestade, o cozer, a terra que sobe e a terra que se parte.
 */
public class OccultaWorldRitesGameTest {
    /** Os quatro estão na lista, e cada um pede o que o original pede. */
    @GameTest
    public void theFourAreRegistered(GameTestHelper helper) {
        record Par(String chave, net.minecraft.world.item.Item pede) {
        }
        for (Par par : List.of(
                new Par("tc.rite.storm", Items.WOODEN_SWORD),
                new Par("tc.rite.cookfood", Items.BLAZE_ROD),
                new Par("tc.rite.raiseearth", OccultaItems.BREW_OF_SPROUTING),
                new Par("tc.rite.partearth", OccultaItems.BREW_OF_EROSION))) {
            var rito = RiteRegistry.all().stream()
                    .filter(r -> r.key().equals(par.chave())).findFirst().orElse(null);
            if (rito == null) {
                helper.fail("falta o rito " + par.chave());
                return;
            }
            if (Rites.shown(rito).stream().noneMatch(c -> c.is(par.pede()))) {
                helper.fail(par.chave() + " pede " + par.pede());
            }
        }
        helper.succeed();
    }

    /** O de Cozer coze a comida largada em volta, e parte dela vira carvão. */
    @GameTest(maxTicks = 60)
    public void cookingCooksWhatIsOnTheGround(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(2, 2, 2));
        var largado = new ItemEntity(level, meio.getX() + 0.5, meio.getY() + 0.5, meio.getZ() + 0.5,
                new ItemStack(Items.BEEF, 16));
        level.addFreshEntity(largado);

        var rito = new ActiveRite("tc.rite.cookfood", new Rites.CookFood(5.0, 0.08), List.of(), null, 0);
        var passo = new Rites.CookFood(5.0, 0.08).steps(0).getFirst();
        // o passo só corre de vinte em vinte batidas
        RiteStep.Result saiu = passo.run(level, meio, 20L, rito);
        if (saiu != RiteStep.Result.COMPLETED) {
            helper.fail("com comida no chão, o rito devia cozer, e deu " + saiu);
            return;
        }
        if (largado.isAlive()) helper.fail("e o cru desaparece");

        int cozidos = 0;
        int carvão = 0;
        for (ItemEntity coisa : level.getEntitiesOfClass(ItemEntity.class,
                new net.minecraft.world.phys.AABB(meio).inflate(6.0))) {
            if (coisa.getItem().is(Items.COOKED_BEEF)) cozidos += coisa.getItem().getCount();
            if (coisa.getItem().is(Items.CHARCOAL)) carvão += coisa.getItem().getCount();
        }
        if (cozidos + carvão != 16) {
            helper.fail("dezesseis entram e dezesseis saem, e saíram " + (cozidos + carvão));
        }
        helper.succeed();
    }

    /** E sem nada que se coza, ele desiste e devolve o que se ofereceu. */
    @GameTest
    public void cookingWithNothingGivesUp(GameTestHelper helper) {
        BlockPos meio = helper.absolutePos(new BlockPos(2, 2, 2));
        var rito = new ActiveRite("tc.rite.cookfood", new Rites.CookFood(5.0, 0.08), List.of(), null, 0);
        var passo = new Rites.CookFood(5.0, 0.08).steps(0).getFirst();
        if (passo.run(helper.getLevel(), meio, 20L, rito) != RiteStep.Result.ABORTED_REFUND) {
            helper.fail("sem comida, o rito desiste devolvendo");
        }
        helper.succeed();
    }

    /** O caminho da terra que se parte é sempre o mesmo para o mesmo círculo, e é isso que o deixa continuar. */
    @GameTest
    public void thePathIsTheSameForTheSameCircle(GameTestHelper helper) {
        BlockPos meio = helper.absolutePos(new BlockPos(2, 2, 2));
        List<BlockPos> uma = Rites.PartEarth.path(meio, 60);
        List<BlockPos> outra = Rites.PartEarth.path(meio, 60);
        if (uma.size() != 60) helper.fail("o caminho tem sessenta passos, e tem " + uma.size());
        if (!uma.equals(outra)) helper.fail("e é sempre o mesmo para o mesmo lugar");

        List<BlockPos> doLado = Rites.PartEarth.path(meio.offset(40, 0, 40), 60);
        if (uma.equals(doLado)) helper.fail("e diferente para outro lugar");

        // e ele anda mesmo: o fim está longe do começo
        BlockPos fim = uma.getLast();
        if (fim.distSqr(meio) < 100.0) helper.fail("e o caminho afasta-se do círculo");
        helper.succeed();
    }

    /**
     * A terra que se parte abre buraco onde o caminho passa.
     *
     * <p>O caminho anda para longe do círculo, e por isso a prova enche de terra <b>as casas do próprio
     * caminho</b> em vez de uma laje: é lá que o rito vai cavar.
     */
    @GameTest(maxTicks = 60)
    public void partingTheEarthDigs(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(3, 4, 3));
        var qual = new Rites.PartEarth(60, 1, 10);
        List<BlockPos> caminho = Rites.PartEarth.path(meio, 60);

        // terra nas primeiras casas do caminho, com folga em volta e em baixo
        List<BlockPos> olhar = new java.util.ArrayList<>();
        for (int i = 0; i < 12; i++) {
            BlockPos ponto = caminho.get(i);
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    for (int dy = 0; dy < 4; dy++) {
                        BlockPos casa = ponto.offset(dx, -dy, dz);
                        level.setBlock(casa, Blocks.DIRT.defaultBlockState(), 2);
                    }
                }
            }
            if (i >= 5) olhar.add(ponto);
        }

        var rito = new ActiveRite("tc.rite.partearth", qual, List.of(), null, 0);
        var passo = qual.steps(0).getFirst();
        for (int i = 0; i < 6; i++) {
            if (passo.run(level, meio, 20L, rito) == RiteStep.Result.COMPLETED) break;
        }

        boolean buraco = false;
        for (BlockPos ponto : olhar) {
            if (level.getBlockState(ponto).isAir()) buraco = true;
        }
        if (!buraco) helper.fail("o rito devia deixar buraco nas casas do caminho");
        helper.succeed();
    }

    /** A fase de um rito guarda-se: um rito de muitas batidas volta no ponto em que estava. */
    @GameTest
    public void theStageSurvivesTheSave(GameTestHelper helper) {
        var qual = new Rites.Storm(0, 3, 8);
        var rito = new ActiveRite("tc.rite.storm", qual, qual.steps(0), null, 0);
        rito.advance();
        rito.advance();
        rito.advance();
        if (rito.stage() != 3) helper.fail("três batidas, três fases");

        var volta = ActiveRite.load(rito.save());
        if (volta == null) {
            helper.fail("o rito devia voltar");
            return;
        }
        if (volta.stage() != 3) helper.fail("e na mesma fase, e voltou na " + volta.stage());
        helper.succeed();
    }

    // ------------------------------------------------------------------ a pedra carregada e o vulcão

    /** O Rito da Carga está na lista e pede a Pedra Sintonizada. */
    @GameTest
    public void theRiteOfChargingIsThere(GameTestHelper helper) {
        var rito = RiteRegistry.all().stream()
                .filter(r -> r.key().equals("tc.rite.chargestone")).findFirst().orElse(null);
        if (rito == null) {
            helper.fail("o Rito da Carga devia estar na lista");
            return;
        }
        var pede = Rites.shown(rito);
        for (var coisa : new net.minecraft.world.item.Item[]{
                OccultaItems.ATTUNED_STONE, Items.GLOWSTONE_DUST, Items.REDSTONE,
                OccultaItems.WOOD_ASH, OccultaItems.QUICKLIME}) {
            if (pede.stream().noneMatch(c -> c.is(coisa))) helper.fail("ele pede " + coisa);
        }
        helper.succeed();
    }

    /** O do Vulcão pede a pedra <b>carregada</b>, que é o que o separa dos outros. */
    @GameTest
    public void theVolcanoNeedsTheChargedStone(GameTestHelper helper) {
        var rito = RiteRegistry.all().stream()
                .filter(r -> r.key().equals("tc.rite.volcano")).findFirst().orElse(null);
        if (rito == null) {
            helper.fail("o Rito do Vulcão devia estar na lista");
            return;
        }
        var pede = Rites.shown(rito);
        if (pede.stream().noneMatch(c -> c.is(OccultaItems.ATTUNED_STONE_CHARGED))) {
            helper.fail("ele pede a pedra carregada");
        }
        if (pede.stream().anyMatch(c -> c.is(OccultaItems.ATTUNED_STONE))) {
            helper.fail("e não a comum");
        }
        helper.succeed();
    }

    /** Sem lava por baixo, o vulcão desiste e devolve o que se ofereceu. */
    @GameTest(maxTicks = 60)
    public void theVolcanoNeedsLavaBelow(GameTestHelper helper) {
        BlockPos meio = helper.absolutePos(new BlockPos(2, 2, 2));
        var qual = new Rites.Volcano(8, 8);
        var rito = new ActiveRite("tc.rite.volcano", qual, List.of(), null, 0);
        var passo = qual.steps(0).getFirst();

        if (passo.run(helper.getLevel(), meio, 15L, rito) != RiteStep.Result.ABORTED_REFUND) {
            helper.fail("sem lava por baixo, o rito desiste devolvendo");
        }
        helper.succeed();
    }

    /** E com uma poça de verdade por baixo, ele começa. */
    @GameTest(maxTicks = 60)
    public void theVolcanoStartsOverLava(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(2, 3, 2));

        // uma poça de lava de três por três, duas casas abaixo do círculo
        BlockPos fundo = meio.below(2);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.setBlock(fundo.offset(dx, 0, dz),
                        net.minecraft.world.level.block.Blocks.LAVA.defaultBlockState(), 2);
            }
        }
        if (!Rites.Volcano.lavaBelow(level, meio)) {
            helper.fail("a poça devia contar como lava por baixo");
            return;
        }

        var qual = new Rites.Volcano(2, 3);
        var rito = new ActiveRite("tc.rite.volcano", qual, List.of(), null, 0);
        var passo = qual.steps(0).getFirst();
        if (passo.run(level, meio, 15L, rito) != RiteStep.Result.UPKEEP) {
            helper.fail("com lava, o rito começa e sustenta-se");
        }
        helper.succeed();
    }

    /** Uma casa de lava sozinha não é poça: o rito quer lava em volta dela. */
    @GameTest
    public void aSingleLavaBlockIsNotAPool(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(4, 3, 4));
        level.setBlock(meio.below(2), net.minecraft.world.level.block.Blocks.LAVA.defaultBlockState(), 2);
        if (Rites.Volcano.lavaBelow(level, meio)) helper.fail("um pingo de lava não é poça");
        helper.succeed();
    }
}
