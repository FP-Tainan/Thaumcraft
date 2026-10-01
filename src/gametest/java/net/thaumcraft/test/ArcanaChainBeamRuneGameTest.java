package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.arcana.Affinity;
import net.thaumcraft.arcana.ArcanaBlocks;
import net.thaumcraft.arcana.Essences;
import net.thaumcraft.arcana.Mana;
import net.thaumcraft.arcana.Modifiers;
import net.thaumcraft.arcana.Shapes;
import net.thaumcraft.arcana.Spell;
import net.thaumcraft.arcana.SpellCast;
import net.thaumcraft.arcana.SpellItem;
import net.thaumcraft.arcana.SpellRuneBlock;
import net.thaumcraft.arcana.SpellRuneBlockEntity;

import java.util.List;

/**
 * As três últimas Formas de mira: a Corrente, que salta; o Facho, que se segura; e a Runa, que espera.
 */
public class ArcanaChainBeamRuneGameTest {
    // ------------------------------------------------------------------ a Corrente

    /** A Corrente salta do alvo para o vivo mais perto, e de novo, até três. */
    @GameTest(maxTicks = 80)
    public void theChainJumpsToTheNearest(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // três porcos em fila, a dois blocos um do outro, dentro do salto de quatro
        var um = helper.spawn(EntityTypes.PIG, new BlockPos(1, 2, 3));
        var dois = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
        var três = helper.spawn(EntityTypes.PIG, new BlockPos(5, 2, 3));
        for (var p : List.of(um, dois, três)) p.setNoAi(true);
        float[] tinham = {um.getHealth(), dois.getHealth(), três.getHealth()};

        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);
        Spell corrente = Spell.of(Shapes.CHAIN, Essences.FROST_DAMAGE);

        // o primeiro alvo vai dado, para a prova não depender de para onde o mago olha
        var saiu = SpellCast.cast(level, corrente, quem, um, um.position());
        if (!saiu.ok()) {
            helper.fail("a Corrente devia pegar, e deu " + saiu);
            return;
        }

        if (um.getHealth() >= tinham[0]) helper.fail("o primeiro leva");
        if (dois.getHealth() >= tinham[1]) helper.fail("e o segundo, que está a dois blocos");
        if (três.getHealth() >= tinham[2]) helper.fail("e o terceiro");
        helper.succeed();
    }

    /** E ela para nos três, mesmo havendo mais perto. */
    @GameTest(maxTicks = 80)
    public void theChainStopsAtThree(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var fila = new java.util.ArrayList<net.minecraft.world.entity.LivingEntity>();
        for (int i = 0; i < 5; i++) {
            var porco = helper.spawn(EntityTypes.PIG, new BlockPos(1 + i, 2, 3));
            porco.setNoAi(true);
            fila.add(porco);
        }
        var tinham = fila.stream().map(net.minecraft.world.entity.LivingEntity::getHealth).toList();

        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);
        SpellCast.cast(level, Spell.of(Shapes.CHAIN, Essences.FROST_DAMAGE), quem, fila.getFirst(),
                fila.getFirst().position());

        int feridos = 0;
        for (int i = 0; i < fila.size(); i++) {
            if (fila.get(i).getHealth() < tinham.get(i)) feridos++;
        }
        if (feridos != 3) helper.fail("a Corrente pega três, e pegou " + feridos);
        helper.succeed();
    }

    /** Quem lançou nunca entra na corrente. */
    @GameTest(maxTicks = 80)
    public void theChainSkipsTheCaster(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        Mana.set(quem, Mana.NONE.withLevel(60));
        Mana.set(quem, Mana.of(quem).withMana(Mana.of(quem).maxMana()));

        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
        porco.setNoAi(true);
        // o mago encostado no porco: ele estaria no alcance do salto
        quem.snapTo(porco.getX() + 0.5, porco.getY(), porco.getZ(), 0.0f, 0.0f);
        float tinha = quem.getHealth();

        SpellCast.cast(level, Spell.of(Shapes.CHAIN, Essences.FROST_DAMAGE), quem, porco, porco.position());

        if (quem.getHealth() < tinha) helper.fail("a Corrente não pega em quem a lançou");
        Mana.set(quem, Mana.NONE);
        helper.succeed();
    }

    // ------------------------------------------------------------------ o Facho

    /** O Facho é a única Forma que se segura, e custa a décima parte de uma comum. */
    @GameTest
    public void theBeamIsTheCheapChanneledOne(GameTestHelper helper) {
        if (!Shapes.BEAM.channeled()) helper.fail("o Facho se segura");
        if (Math.abs(Shapes.BEAM.manaMultiplier() - 0.1f) > 0.001f) {
            helper.fail("e custa a décima parte, e custa " + Shapes.BEAM.manaMultiplier());
        }
        // e são só duas as que se seguram: o Facho e o Canal, que entrou na fatia 23
        for (var forma : net.thaumcraft.arcana.SpellParts.shapes()) {
            if (forma != Shapes.BEAM && forma != Shapes.CHANNEL && forma.channeled()) {
                helper.fail("só o Facho e o Canal se seguram, e a " + forma.name() + " também");
            }
        }
        if (!Shapes.CHANNEL.channeled()) helper.fail("e o Canal se segura");
        // e o item sabe disso
        if (!SpellItem.isChanneled(Spell.of(Shapes.BEAM, Essences.FIRE_DAMAGE))) {
            helper.fail("o item devia saber que um Facho se segura");
        }
        if (SpellItem.isChanneled(Spell.of(Shapes.TOUCH, Essences.FIRE_DAMAGE))) {
            helper.fail("e que um Toque não");
        }
        helper.succeed();
    }

    /**
     * O Facho só dói <b>de dez em dez batidas</b>.
     *
     * <p>É o que separa o facho de um moedor: ele queima devagar e sem parar, e não tudo de uma vez.
     *
     * <p>O mago tem de estar <b>olhando</b> para o alvo, e não apontado a ele: ao contrário do Toque, o
     * Facho ignora um alvo já dado e traça sempre o olhar. É o original, e é o que o faz varrer.
     */
    @GameTest(maxTicks = 80)
    public void theBeamOnlyBitesEveryTenTicks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
        porco.setNoAi(true);
        float tinha = porco.getHealth();

        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.CREATIVE);
        // dois blocos atrás do porco, olhando para ele
        Vec3 atrás = porco.position().subtract(0.0, 0.0, 2.0);
        quem.snapTo(atrás.x, porco.getY(), atrás.z, 0.0f, 0.0f);
        quem.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, porco.position()
                .add(0.0, porco.getBbHeight() / 2.0, 0.0));

        Spell facho = Spell.of(Shapes.BEAM, Essences.FROST_DAMAGE);

        // as nove batidas que não são múltiplas de dez não fazem nada
        for (int i = 1; i < 10; i++) {
            SpellCast.cast(level, facho, quem, null, quem.getEyePosition(), i);
        }
        if (porco.getHealth() < tinha) helper.fail("entre uma e nove, o facho não dói");

        // e a décima dói
        var saiu = SpellCast.cast(level, facho, quem, null, quem.getEyePosition(), 10);
        if (porco.getHealth() >= tinha) {
            helper.fail("mas a décima dói, e deu " + saiu);
        }
        helper.succeed();
    }
    // ------------------------------------------------------------------ a Runa

    /** A Runa é principum: sozinha não é armadilha nenhuma. */
    @GameTest(maxTicks = 60)
    public void theRuneNeedsSomethingAfterIt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);

        if (!Shapes.RUNE.principum()) helper.fail("a Runa é principum");
        var saiu = SpellCast.cast(level, Spell.of(Shapes.RUNE, Essences.FIRE_DAMAGE), quem, null,
                quem.position());
        if (saiu != SpellCast.Result.MALFORMED) {
            helper.fail("e sozinha é malformada, e deu " + saiu);
        }
        helper.succeed();
    }

    /**
     * A Runa posta no chão guarda o que sobrou da frase, a Afinidade e quem a pôs.
     *
     * <p>Ela é posta à mão aqui, e não lançada, porque lançá-la depende de para onde o mago olha — e a
     * construção da prova nasce girada ao acaso.
     */
    @GameTest(maxTicks = 60)
    public void theRuneRemembersWhatItIs(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);

        BlockPos casa = new BlockPos(2, 2, 2);
        helper.setBlock(casa, ArcanaBlocks.SPELL_RUNE.defaultBlockState()
                .setValue(SpellRuneBlock.AFFINITY, Affinity.FIRE.ordinal()));
        if (!(helper.getBlockEntity(casa, SpellRuneBlockEntity.class) instanceof SpellRuneBlockEntity runa)) {
            helper.fail("a runa devia ter o seu dado");
            return;
        }

        Spell sobra = Spell.of(Shapes.TOUCH, Essences.FROST_DAMAGE);
        runa.setSpell(sobra);
        runa.setTriggers(2);
        runa.setPlacedBy(quem);

        if (runa.spell().stages().size() != 1) helper.fail("ela guarda a frase");
        if (runa.triggers() != 2) helper.fail("e quantas vezes aguenta");
        if (runa.permanent()) helper.fail("e duas vezes não é para sempre");
        if (!quem.getUUID().equals(runa.placedBy())) helper.fail("e quem a pôs");

        // gastar as duas vezes acaba com ela
        if (runa.spend()) helper.fail("a primeira vez não a acaba");
        if (!runa.spend()) helper.fail("mas a segunda sim");

        helper.setBlock(casa, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /** Um número de vezes negativo quer dizer para sempre, e ela nunca se gasta. */
    @GameTest(maxTicks = 60)
    public void aNegativeCountMeansForever(GameTestHelper helper) {
        BlockPos casa = new BlockPos(3, 2, 3);
        helper.setBlock(casa, ArcanaBlocks.SPELL_RUNE.defaultBlockState());
        if (!(helper.getBlockEntity(casa, SpellRuneBlockEntity.class) instanceof SpellRuneBlockEntity runa)) {
            helper.fail("a runa devia ter o seu dado");
            return;
        }
        runa.setTriggers(-1);
        if (!runa.permanent()) helper.fail("menos um é para sempre");
        for (int i = 0; i < 10; i++) {
            if (runa.spend()) helper.fail("e para sempre não se gasta");
        }
        helper.setBlock(casa, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /** As Repetições dizem quantas vezes a Runa aguenta. */
    @GameTest
    public void procsDecideHowManyTimes(GameTestHelper helper) {
        Spell uma = new Spell(List.of(
                new Spell.Stage(Shapes.RUNE, List.of(), List.of()),
                new Spell.Stage(Shapes.TOUCH, List.of(Essences.FIRE_DAMAGE), List.of())));
        if ((int) uma.add(net.thaumcraft.arcana.SpellModifierKind.PROCS, 1) != 1) {
            helper.fail("sem modificador, uma vez");
        }

        Spell mais = new Spell(List.of(
                new Spell.Stage(Shapes.RUNE, List.of(), List.of(Modifiers.PROCS, Modifiers.PROCS)),
                new Spell.Stage(Shapes.TOUCH, List.of(Essences.FIRE_DAMAGE), List.of())));
        int quantas = (int) mais.add(net.thaumcraft.arcana.SpellModifierKind.PROCS, 1);
        if (quantas <= 1) helper.fail("com duas Repetições, mais de uma vez: " + quantas);
        helper.succeed();
    }

    /** E a Runa não sobrevive sem chão debaixo dela. */
    @GameTest(maxTicks = 60)
    public void theRuneNeedsGround(GameTestHelper helper) {
        BlockPos chão = new BlockPos(4, 2, 4);
        BlockPos casa = chão.above();
        helper.setBlock(chão, Blocks.STONE.defaultBlockState());
        helper.setBlock(casa, ArcanaBlocks.SPELL_RUNE.defaultBlockState());
        if (helper.getBlockState(casa).isAir()) {
            helper.fail("com chão, ela fica");
            return;
        }

        helper.setBlock(chão, Blocks.AIR.defaultBlockState());
        helper.succeedWhen(() -> {
            if (!helper.getBlockState(casa).isAir()) helper.fail("sem chão, ela some");
        });
    }

    private static Vec3 nada() {
        return Vec3.ZERO;
    }
}
