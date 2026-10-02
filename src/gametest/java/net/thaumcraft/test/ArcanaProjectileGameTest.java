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
import net.thaumcraft.arcana.ManaClock;
import net.thaumcraft.arcana.Modifiers;
import net.thaumcraft.arcana.Shapes;
import net.thaumcraft.arcana.Spell;
import net.thaumcraft.arcana.SpellCast;
import net.thaumcraft.arcana.SpellModifierKind;
import net.thaumcraft.arcana.SpellProjectileEntity;

import java.util.List;

/**
 * O Projétil e o relógio da mana: a Forma que atira e o tempo que ela custa a repor.
 */
public class ArcanaProjectileGameTest {
    // ------------------------------------------------------------------ atirar

    /** Lançar um Projétil põe um feitiço a voar, com a frase inteira dentro. */
    @GameTest(maxTicks = 60)
    public void castingAProjectileSpawnsOne(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);

        Spell tiro = new Spell(List.of(
                new Spell.Stage(Shapes.PROJECTILE, List.of(), List.of()),
                new Spell.Stage(Shapes.AOE, List.of(Essences.LIGHT), List.of())));

        var saiu = SpellCast.cast(level, tiro, quem, null, quem.getEyePosition());
        if (!saiu.ok()) {
            helper.fail("atirar devia dar sempre por bom, e deu " + saiu);
            return;
        }

        var voando = level.getEntities(ArcanaEntities.SPELL_PROJECTILE,
                quem.getBoundingBox().inflate(4.0), e -> true);
        if (voando.isEmpty()) {
            helper.fail("devia haver um feitiço a voar");
            return;
        }
        SpellProjectileEntity voa = voando.getFirst();
        if (voa.spell().stages().size() != 2) helper.fail("e ele leva a frase inteira, as duas etapas");
        if (voa.getOwner() != quem) helper.fail("e sabe quem o lançou");
        if (voa.getDeltaMovement().length() < 0.5) {
            helper.fail("e sai a uma casa por batida: " + voa.getDeltaMovement().length());
        }
        voa.discard();
        helper.succeed();
    }

    /** A Velocidade multiplica por 2,6: é o número do original, e é o que se sente na mão. */
    @GameTest(maxTicks = 60)
    public void speedMultipliesTheFlight(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);

        Spell rápido = new Spell(List.of(new Spell.Stage(Shapes.PROJECTILE,
                List.of(Essences.FIRE_DAMAGE), List.of(Modifiers.SPEED))));
        if (Math.abs(rápido.mul(SpellModifierKind.SPEED, SpellModifierKind.SPEED.base) - 2.6) > 1.0e-4) {
            helper.fail("uma Velocidade dá 2,6");
        }

        Spell duasVezes = new Spell(List.of(new Spell.Stage(Shapes.PROJECTILE,
                List.of(Essences.FIRE_DAMAGE), List.of(Modifiers.SPEED, Modifiers.SPEED))));
        if (Math.abs(duasVezes.mul(SpellModifierKind.SPEED, SpellModifierKind.SPEED.base) - 6.76) > 1.0e-4) {
            helper.fail("e duas multiplicam duas vezes: 6,76");
        }

        SpellCast.cast(level, rápido, quem, null, quem.getEyePosition());
        var voando = level.getEntities(ArcanaEntities.SPELL_PROJECTILE,
                quem.getBoundingBox().inflate(8.0), e -> true);
        if (voando.isEmpty()) {
            helper.fail("devia haver um feitiço a voar");
            return;
        }
        double anda = voando.getFirst().getDeltaMovement().length();
        if (Math.abs(anda - 2.6) > 0.01) helper.fail("e o projétil anda 2,6 por batida, e andou " + anda);
        voando.forEach(net.minecraft.world.entity.Entity::discard);
        helper.succeed();
    }

    /**
     * A Gravidade é <b>de graça</b> e os Alvos Não Sólidos também: os dois únicos que não cobram.
     *
     * <p>É o número do original, e não é descuido: são os dois modificadores que mudam <i>como</i> o feitiço se
     * comporta e não <i>quanto</i> ele faz.
     */
    @GameTest
    public void gravityAndNonSolidAreFree(GameTestHelper helper) {
        Spell nu = Spell.of(Shapes.PROJECTILE, Essences.FIRE_DAMAGE);
        float base = nu.manaCost(null, null);

        for (var mod : List.of(Modifiers.GRAVITY, Modifiers.TARGET_NONSOLID_BLOCKS)) {
            Spell com = new Spell(List.of(new Spell.Stage(Shapes.PROJECTILE,
                    List.of(Essences.FIRE_DAMAGE), List.of(mod, mod, mod))));
            if (Math.abs(com.manaCost(null, null) - base) > 0.01f) {
                helper.fail("o " + mod.name() + " não cobra nada, nem três vezes");
            }
        }

        // e a Velocidade cobra, três vezes mais por três vezes posta
        Spell trêsVeloz = new Spell(List.of(new Spell.Stage(Shapes.PROJECTILE,
                List.of(Essences.FIRE_DAMAGE), List.of(Modifiers.SPEED, Modifiers.SPEED, Modifiers.SPEED))));
        if (Math.abs(trêsVeloz.manaCost(null, null) - base * 1.15f * 3.0f) > 0.05f) {
            helper.fail("e a Velocidade cobra 1,15 vezes a quantidade");
        }
        helper.succeed();
    }

    /** O projétil que bate num bicho corre a etapa dele e morre — se não atravessar. */
    @GameTest(maxTicks = 100)
    public void aProjectileHitsAndDies(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = new BlockPos(3, 2, 3);
        var porco = helper.spawn(EntityTypes.PIG, onde);
        float tinha = porco.getHealth();

        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);
        Spell tiro = Spell.of(Shapes.PROJECTILE, Essences.FIRE_DAMAGE);

        // o projétil posto à queima-roupa, apontado ao porco
        SpellProjectileEntity voa = new SpellProjectileEntity(level, quem, tiro, 1.0);
        voa.snapTo(porco.getX() - 1.5, porco.getEyeY(), porco.getZ(), 0.0f, 0.0f);
        voa.setDeltaMovement(new net.minecraft.world.phys.Vec3(1.0, 0.0, 0.0));
        level.addFreshEntity(voa);

        helper.succeedWhen(() -> {
            if (porco.getHealth() >= tinha) helper.fail("o projétil devia bater no porco");
            if (voa.isAlive()) helper.fail("e morrer na batida, porque não atravessa");
        });
    }

    /**
     * E o que atravessa pega dois de uma vez.
     *
     * <p>A linha de tiro sai <b>dos próprios porcos</b>: o rumo é a reta que vai do primeiro para o segundo, e o
     * tiro começa um bloco antes do primeiro. Assim a prova não depende de para que lado a construção está
     * virada — e os dois ficam sem cabeça (`setNoAi`) para não saírem do caminho no meio da prova.
     */
    @GameTest(maxTicks = 100)
    public void aPiercingProjectileGoesThrough(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var primeiro = helper.spawn(EntityTypes.PIG, new BlockPos(2, 2, 3));
        var segundo = helper.spawn(EntityTypes.PIG, new BlockPos(5, 2, 3));
        primeiro.setNoAi(true);
        segundo.setNoAi(true);
        float tinhaUm = primeiro.getHealth();
        float tinhaDois = segundo.getHealth();

        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);
        Spell tiro = new Spell(List.of(new Spell.Stage(Shapes.PROJECTILE,
                List.of(Essences.FROST_DAMAGE), List.of(Modifiers.PIERCING))));

        Vec3 mira = primeiro.position().add(0.0, primeiro.getBbHeight() / 2.0, 0.0);
        Vec3 alvo = segundo.position().add(0.0, segundo.getBbHeight() / 2.0, 0.0);
        Vec3 rumo = alvo.subtract(mira).normalize().scale(0.5);

        SpellProjectileEntity voa = new SpellProjectileEntity(level, quem, tiro, 0.5);
        voa.setPierces((int) tiro.add(SpellModifierKind.PIERCING, 0.0));
        voa.snapTo(mira.subtract(rumo.normalize()));
        voa.setDeltaMovement(rumo);
        level.addFreshEntity(voa);

        helper.succeedWhen(() -> {
            if (primeiro.getHealth() >= tinhaUm) helper.fail("devia bater no primeiro porco");
            if (segundo.getHealth() >= tinhaDois) helper.fail("e atravessar até ao segundo");
        });
    }

    /**
     * O projétil passa pela água como se ela não existisse, e só para nela se lhe disserem para parar.
     *
     * <p>São dois tiros iguais no mesmo lugar, e a única diferença é o modificador: um passa e o outro morre na
     * água. É o que a caixa do bloco decide — a água não tem caixa nenhuma.
     */
    @GameTest(maxTicks = 100)
    public void waterOnlyStopsItWhenAsked(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos poça = new BlockPos(3, 2, 3);
        helper.setBlock(poça, Blocks.WATER.defaultBlockState());

        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);
        Spell passa = Spell.of(Shapes.PROJECTILE, Essences.FIRE_DAMAGE);
        Spell para = new Spell(List.of(new Spell.Stage(Shapes.PROJECTILE,
                List.of(Essences.FIRE_DAMAGE), List.of(Modifiers.TARGET_NONSOLID_BLOCKS))));

        SpellProjectileEntity semNada = atira(helper, level, quem, passa, false);
        SpellProjectileEntity comTudo = atira(helper, level, quem, para, true);

        helper.runAfterDelay(8, () -> {
            if (!semNada.isAlive()) helper.fail("sem o modificador, ele devia atravessar a água");
            if (comTudo.isAlive()) helper.fail("e com ele devia parar nela");
            semNada.discard();
            comTudo.discard();
            helper.setBlock(poça, Blocks.AIR.defaultBlockState());
            helper.succeed();
        });
    }

    /** Um tiro posto à mão dois blocos antes da poça, apontado a ela. */
    private static SpellProjectileEntity atira(GameTestHelper helper, ServerLevel level, ServerPlayer quem,
                                               Spell feitiço, boolean nãoSólidos) {
        SpellProjectileEntity voa = new SpellProjectileEntity(level, quem, feitiço, 0.5);
        voa.setTargetNonSolid(nãoSólidos);
        voa.snapTo(helper.absoluteVec(new Vec3(1.5, 2.5, 3.5)));
        voa.setDeltaMovement(rumo(helper, 0.5));
        level.addFreshEntity(voa);
        return voa;
    }

    /** O para onde, virado como a construção está virada. */
    private static Vec3 rumo(GameTestHelper helper, double quanto) {
        return Vec3.atLowerCornerOf(helper.getAbsoluteDirection(
                net.minecraft.core.Direction.EAST).getUnitVec3i()).scale(quanto);
    }

    // ------------------------------------------------------------------ o relógio

    /**
     * O relógio enche mais depressa quanto mais alto o nível.
     *
     * <p>É onde este porte <b>corrige</b> o original: lá a divisão {@code nível/99} é inteira e dá zero para
     * todo nível abaixo de 99, e o nível não conta para nada.
     */
    @GameTest
    public void theClockGetsFasterWithLevel(GameTestHelper helper) {
        int zero = ManaClock.ticksForFullRegen(0);
        int meio = ManaClock.ticksForFullRegen(50);
        int cheio = ManaClock.ticksForFullRegen(Mana.MAX_LEVEL);

        if (zero != 1800) helper.fail("de nível zero leva 1800 batidas, e leva " + zero);
        if (cheio != 1200) helper.fail("e de 99 leva 1200, e leva " + cheio);
        if (!(meio < zero && meio > cheio)) helper.fail("e o meio fica no meio: " + meio);
        helper.succeed();
    }

    /** Uma batida do relógio põe mana e tira desgaste. */
    @GameTest(maxTicks = 60)
    public void aTickFillsAndCools(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        Mana.set(quem, new Mana(20, 0.0f, 100.0f));

        ManaClock.tick(quem);
        Mana agora = Mana.of(quem);
        if (agora.mana() <= 0.0f) helper.fail("a mana devia subir");
        if (agora.burnout() >= 100.0f) helper.fail("e o desgaste descer");

        // e quem está em criativo enche na hora
        quem.setGameMode(GameType.CREATIVE);
        Mana.set(quem, new Mana(20, 0.0f, 100.0f));
        ManaClock.tick(quem);
        Mana emCriativo = Mana.of(quem);
        if (emCriativo.mana() != emCriativo.maxMana()) helper.fail("em criativo enche na hora");
        if (emCriativo.burnout() != 0.0f) helper.fail("e sem desgaste nenhum");

        Mana.set(quem, Mana.NONE);
        helper.succeed();
    }

    /**
     * De nível zero o desgaste <b>não desce</b>.
     *
     * <p>É o original, e é o que faz o nível valer a pena de verdade: a conta do desgaste é
     * {@code 0,01 × nível × batidas}, e com nível zero ela dá zero. Um arcanista sem nível que se gastou fica
     * gasto — não é que tenha pouca mana, é que não se recupera.
     */
    @GameTest(maxTicks = 60)
    public void atLevelZeroBurnoutDoesNotDrop(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        Mana.set(quem, new Mana(0, 0.0f, 300.0f));

        for (int i = 0; i < 5; i++) ManaClock.tick(quem);
        if (Mana.of(quem).burnout() != 300.0f) helper.fail("de nível zero o desgaste fica onde está");

        Mana.set(quem, Mana.NONE);
        helper.succeed();
    }
}
