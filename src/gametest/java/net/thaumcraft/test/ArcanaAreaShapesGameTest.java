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
import net.thaumcraft.arcana.ArcanaEntities;
import net.thaumcraft.arcana.Essences;
import net.thaumcraft.arcana.Mana;
import net.thaumcraft.arcana.Shapes;
import net.thaumcraft.arcana.Spell;
import net.thaumcraft.arcana.SpellCast;
import net.thaumcraft.arcana.SpellEffectEntity;

import java.util.List;

/**
 * As três Formas que criam área: a Zona, a Parede e a Onda.
 *
 * <p>Todas as três são <b>principum</b> — elas criam um lugar e pedem outra Forma depois —, e todas as três
 * são a mesma entidade com um feitio diferente.
 */
public class ArcanaAreaShapesGameTest {
    /**
     * Uma Forma principum sozinha no fim da frase é uma frase incompleta.
     *
     * <p><b>Este é o desvio declarado desta fatia.</b> O original deixa lançar e cobra a mana de um feitiço
     * que não faz nada, porque quem impediria de escrevê-lo é a Mesa de Inscrição. Aqui, que ainda não a tem,
     * a frase é recusada de graça.
     */
    @GameTest(maxTicks = 60)
    public void aPrincipumShapeAloneIsMalformed(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);

        for (var forma : List.of(Shapes.ZONE, Shapes.WALL, Shapes.WAVE)) {
            if (!forma.principum()) helper.fail(forma.name() + " é principum");
            var saiu = SpellCast.cast(level, Spell.of(forma, Essences.FIRE_DAMAGE), quem, null,
                    quem.position());
            if (saiu != SpellCast.Result.MALFORMED) {
                helper.fail(forma.name() + " sozinha devia ser malformada, e deu " + saiu);
            }
        }

        // e as outras não são principum: elas valem sozinhas
        for (var forma : List.of(Shapes.SELF, Shapes.TOUCH, Shapes.AOE, Shapes.PROJECTILE)) {
            if (forma.principum()) helper.fail(forma.name() + " vale sozinha");
        }
        helper.succeed();
    }

    /** Com uma Forma depois, a Zona nasce — e leva o que sobrou da frase, não a frase inteira. */
    @GameTest(maxTicks = 60)
    public void theZoneCarriesWhatIsLeft(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);

        Spell frase = new Spell(List.of(
                new Spell.Stage(Shapes.ZONE, List.of(), List.of()),
                new Spell.Stage(Shapes.AOE, List.of(Essences.LIGHT), List.of())));

        var saiu = SpellCast.cast(level, frase, quem, null, quem.position());
        if (!saiu.ok()) {
            helper.fail("a Zona com uma Forma depois devia sair, e deu " + saiu);
            return;
        }

        var áreas = level.getEntities(ArcanaEntities.SPELL_EFFECT,
                quem.getBoundingBox().inflate(6.0), e -> true);
        if (áreas.isEmpty()) {
            helper.fail("devia haver uma área");
            return;
        }
        SpellEffectEntity área = áreas.getFirst();
        if (área.kind() != SpellEffectEntity.Kind.ZONE) helper.fail("e ela é uma Zona");
        if (área.spell().stages().size() != 1) {
            helper.fail("e leva só o que sobrou: uma etapa, e leva " + área.spell().stages().size());
        }
        if (área.spell().first().shape() != Shapes.AOE) helper.fail("e a etapa que sobrou é a Área");

        áreas.forEach(net.minecraft.world.entity.Entity::discard);
        helper.succeed();
    }

    /**
     * O raio da Zona <b>soma</b> e o da Parede <b>multiplica</b>.
     *
     * <p>É o original, e não é detalhe: o modificador de Raio multiplica por 0,7, ou seja <i>encolhe</i>. Numa
     * Parede ele aperta de verdade; numa Zona, que soma, ele nem chega a ser lido do mesmo jeito.
     */
    @GameTest(maxTicks = 60)
    public void theZoneAddsAndTheWallMultiplies(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);

        float zona = raioDe(helper, level, quem, Shapes.ZONE);
        if (Math.abs(zona - 2.0f) > 0.01f) helper.fail("a Zona nasce com dois blocos, e nasceu com " + zona);

        float parede = raioDe(helper, level, quem, Shapes.WALL);
        if (Math.abs(parede - 3.0f) > 0.01f) helper.fail("a Parede com três, e nasceu com " + parede);

        float onda = raioDe(helper, level, quem, Shapes.WAVE);
        if (Math.abs(onda - 1.0f) > 0.01f) helper.fail("e a Onda com um, e nasceu com " + onda);
        helper.succeed();
    }

    private static float raioDe(GameTestHelper helper, ServerLevel level, ServerPlayer quem,
                                net.thaumcraft.arcana.SpellPart.Shape forma) {
        Spell frase = new Spell(List.of(
                new Spell.Stage(forma, List.of(), List.of()),
                new Spell.Stage(Shapes.AOE, List.of(Essences.LIGHT), List.of())));
        SpellCast.cast(level, frase, quem, null, quem.position());

        var áreas = level.getEntities(ArcanaEntities.SPELL_EFFECT,
                quem.getBoundingBox().inflate(8.0), e -> true);
        if (áreas.isEmpty()) {
            helper.fail("a " + forma.name() + " devia ter nascido");
            return -1.0f;
        }
        float raio = áreas.getFirst().radius();
        áreas.forEach(net.minecraft.world.entity.Entity::discard);
        return raio;
    }

    /** A Zona corre o feitiço em quem estiver dentro dela, de segundo em segundo. */
    @GameTest(maxTicks = 100)
    public void theZoneBurnsWhoStandsInIt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
        porco.setNoAi(true);
        float tinha = porco.getHealth();

        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);
        Spell frase = new Spell(List.of(
                new Spell.Stage(Shapes.ZONE, List.of(), List.of()),
                new Spell.Stage(Shapes.AOE, List.of(Essences.FROST_DAMAGE), List.of())));

        var área = new SpellEffectEntity(level, quem, frase.pop(), SpellEffectEntity.Kind.ZONE);
        área.setRadius(3.0f);
        área.setLife(100);
        área.snapTo(porco.getX(), porco.getY(), porco.getZ(), 0.0f, 0.0f);
        level.addFreshEntity(área);

        helper.succeedWhen(() -> {
            if (porco.getHealth() >= tinha) helper.fail("a Zona devia ferir quem está dentro");
        });
    }

    /** E a Onda anda, e deixa o feitiço no chão por onde passa. */
    @GameTest(maxTicks = 100)
    public void theWaveMovesAndMarksTheGround(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);

        // um chão de terra para a Onda de Escavar abrir vala
        for (int x = 1; x <= 5; x++) {
            for (int z = 2; z <= 4; z++) {
                helper.setBlock(new BlockPos(x, 2, z), Blocks.DIRT.defaultBlockState());
            }
        }

        Spell frase = new Spell(List.of(
                new Spell.Stage(Shapes.WAVE, List.of(), List.of()),
                new Spell.Stage(Shapes.AOE, List.of(Essences.DIG), List.of())));

        var onda = new SpellEffectEntity(level, quem, frase.pop(), SpellEffectEntity.Kind.WAVE);
        onda.setRadius(1.0f);
        onda.setLife(40);
        onda.noPhysics = true;
        Vec3 partida = Vec3.atCenterOf(helper.absolutePos(new BlockPos(1, 2, 3)));
        onda.snapTo(partida.x, partida.y, partida.z, 0.0f, 0.0f);
        // o rumo sai do próprio terreno da prova, porque a construção nasce girada ao acaso
        Vec3 destino = Vec3.atCenterOf(helper.absolutePos(new BlockPos(5, 2, 3)));
        float giro = (float) (Math.toDegrees(Math.atan2(destino.z - partida.z, destino.x - partida.x)) - 90.0);
        onda.setWave(giro, 0.5);
        level.addFreshEntity(onda);

        double deOnde = partida.x;
        double deZ = partida.z;
        helper.succeedWhen(() -> {
            if (Math.hypot(onda.getX() - deOnde, onda.getZ() - deZ) < 1.0) {
                helper.fail("a Onda devia ter andado");
            }
            if (!helper.getBlockState(new BlockPos(2, 2, 3)).isAir()) {
                helper.fail("e cavado o chão por onde passou");
            }
        });
    }

    /** O ponto mais perto de um segmento é o que a Parede usa para medir quem a cruza. */
    @GameTest
    public void theClosestPointOnTheLineIsRight(GameTestHelper helper) {
        Vec3 a = new Vec3(0.0, 0.0, 0.0);
        Vec3 b = new Vec3(10.0, 0.0, 0.0);

        Vec3 meio = SpellEffectEntity.naLinha(a, b, new Vec3(5.0, 0.0, 3.0));
        if (Math.abs(meio.x - 5.0) > 0.001 || Math.abs(meio.z) > 0.001) {
            helper.fail("quem está ao lado do meio cai no meio, e caiu em " + meio);
        }

        // e quem está além da ponta cai na ponta, porque é um segmento e não uma reta sem fim
        Vec3 ponta = SpellEffectEntity.naLinha(a, b, new Vec3(50.0, 0.0, 0.0));
        if (Math.abs(ponta.x - 10.0) > 0.001) helper.fail("além da ponta, cai na ponta: " + ponta);

        Vec3 outra = SpellEffectEntity.naLinha(a, b, new Vec3(-50.0, 0.0, 0.0));
        if (Math.abs(outra.x) > 0.001) helper.fail("e do outro lado, na outra ponta: " + outra);
        helper.succeed();
    }

    /** Os blocos entre duas pontas saem sem buraco e sem repetir. */
    @GameTest
    public void theLineCoversEveryBlock(GameTestHelper helper) {
        var blocos = SpellEffectEntity.entre(new Vec3(0.5, 64.0, 0.5), new Vec3(5.5, 64.0, 0.5));
        if (blocos.size() != 6) helper.fail("de zero a cinco são seis blocos, e deram " + blocos.size());
        if (blocos.size() != blocos.stream().distinct().count()) helper.fail("e nenhum repete");

        // e uma linha de tamanho zero dá um bloco só
        var um = SpellEffectEntity.entre(new Vec3(0.5, 64.0, 0.5), new Vec3(0.5, 64.0, 0.5));
        if (um.size() != 1) helper.fail("uma linha parada é um bloco só, e deu " + um.size());
        helper.succeed();
    }

    /** As três custam o que o original diz: 4,5 a Zona, 2,5 a Parede e 3 a Onda. */
    @GameTest
    public void theThreeCostWhatTheOriginalSays(GameTestHelper helper) {
        var custos = new java.util.LinkedHashMap<net.thaumcraft.arcana.SpellPart.Shape, Float>();
        custos.put(Shapes.ZONE, 4.5f);
        custos.put(Shapes.WALL, 2.5f);
        custos.put(Shapes.WAVE, 3.0f);

        for (var par : custos.entrySet()) {
            if (Math.abs(par.getKey().manaMultiplier() - par.getValue()) > 0.001f) {
                helper.fail("a " + par.getKey().name() + " custa " + par.getValue()
                        + ", e custa " + par.getKey().manaMultiplier());
            }
        }
        // a Zona é a mais cara das três que criam área — só as Contingências, a 10×, custam mais
        for (var forma : List.of(Shapes.SELF, Shapes.TOUCH, Shapes.AOE, Shapes.PROJECTILE,
                Shapes.WALL, Shapes.WAVE, Shapes.CHAIN, Shapes.BEAM, Shapes.RUNE)) {
            if (forma.manaMultiplier() > Shapes.ZONE.manaMultiplier()) {
                helper.fail("a Zona devia custar mais que a " + forma.name());
            }
        }
        if (Shapes.CONTINGENCY_DEATH.manaMultiplier() <= Shapes.ZONE.manaMultiplier()) {
            helper.fail("e as Contingências, que esperam, custam mais que ela");
        }
        helper.succeed();
    }

    /** Quantas vezes um modificador aparece: é isso que manda a Onda descer. */
    @GameTest
    public void countingModifiersWorks(GameTestHelper helper) {
        Spell nenhuma = Spell.of(Shapes.WAVE, Essences.DIG);
        if (nenhuma.count(net.thaumcraft.arcana.SpellModifierKind.GRAVITY) != 0) {
            helper.fail("sem modificador, zero");
        }

        Spell três = new Spell(List.of(new Spell.Stage(Shapes.WAVE, List.of(Essences.DIG),
                List.of(net.thaumcraft.arcana.Modifiers.GRAVITY,
                        net.thaumcraft.arcana.Modifiers.GRAVITY,
                        net.thaumcraft.arcana.Modifiers.GRAVITY))));
        if (três.count(net.thaumcraft.arcana.SpellModifierKind.GRAVITY) != 3) {
            helper.fail("três Gravidades contam três");
        }
        if (três.count(net.thaumcraft.arcana.SpellModifierKind.SPEED) != 0) {
            helper.fail("e nenhuma Velocidade conta zero");
        }
        helper.succeed();
    }

    private static void limpa(ServerPlayer quem) {
        Mana.set(quem, Mana.NONE);
    }
}
