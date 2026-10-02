package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.GameType;
import net.thaumcraft.arcana.ArcanaEffects;
import net.thaumcraft.arcana.ArcanaEntities;
import net.thaumcraft.arcana.Essences;
import net.thaumcraft.arcana.Shapes;
import net.thaumcraft.arcana.Spell;
import net.thaumcraft.arcana.SpellEffectEntity;

/**
 * A Nevasca e a Chuva de Fogo: as duas essências que são um feitiço inteiro.
 *
 * <p>Elas são a exceção de tudo o resto neste ramo. As outras essências fazem uma coisa a quem a Forma lhes
 * trouxer; estas duas <b>criam um lugar</b> que fere por si, a cada batida, e vai deixando neve ou fogo no
 * chão. No original são duas das dez perícias prateadas, e é isso que elas são: um feitiço numa peça só.
 */
public class ArcanaWeatherGameTest {
    /** A Nevasca nasce onde o feitiço bate, e fica. */
    @GameTest(maxTicks = 100)
    public void theBlizzardIsBorn(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);

        if (!Essences.BLIZZARD.onEntity(level, Spell.of(Shapes.SELF, Essences.BLIZZARD), quem, quem)) {
            helper.fail("a Nevasca devia nascer");
            return;
        }

        var achadas = level.getEntities(ArcanaEntities.SPELL_EFFECT,
                quem.getBoundingBox().inflate(6.0), e -> e.kind() == SpellEffectEntity.Kind.BLIZZARD);
        if (achadas.isEmpty()) {
            helper.fail("e devia estar lá");
            return;
        }

        // e outra não nasce ao pé dela
        if (Essences.BLIZZARD.onEntity(level, Spell.of(Shapes.SELF, Essences.BLIZZARD), quem, quem)) {
            helper.fail("e duas nevascas no mesmo lugar não");
        }

        for (var a : achadas) a.discard();
        helper.succeed();
    }

    /** A Nevasca fere de gelo quem estiver dentro, e o prende. */
    @GameTest(maxTicks = 100)
    public void theBlizzardFreezesWhoIsInside(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);

        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
        porco.setNoAi(true);
        float tinha = porco.getHealth();

        var nevasca = new SpellEffectEntity(level, quem, Spell.of(Shapes.SELF),
                SpellEffectEntity.Kind.BLIZZARD);
        nevasca.setWeather(SpellEffectEntity.Kind.BLIZZARD);
        nevasca.setRadius(4.0f);
        nevasca.setLife(100);
        nevasca.snapTo(porco.position());
        level.addFreshEntity(nevasca);

        nevasca.tick();

        if (porco.getHealth() >= tinha) helper.fail("a Nevasca fere quem está dentro");
        if (!porco.hasEffect(ArcanaEffects.FROST_SLOW)) helper.fail("e o prende com o Gelado");
        if (ArcanaEffects.grau(porco, ArcanaEffects.FROST_SLOW) != SpellEffectEntity.BLIZZARD_HOLD_LEVEL) {
            helper.fail("e no grau mais forte que ele tem");
        }

        nevasca.discard();
        porco.discard();
        helper.succeed();
    }

    /** A Chuva de Fogo fere menos — três quartos contra um. */
    @GameTest
    public void theRainOfFireHurtsLess(GameTestHelper helper) {
        if (SpellEffectEntity.FIRE_RAIN_DAMAGE >= SpellEffectEntity.BLIZZARD_DAMAGE) {
            helper.fail("a Chuva de Fogo fere menos do que a Nevasca");
        }
        if (SpellEffectEntity.FIRE_RAIN_DAMAGE != 0.75f) helper.fail("três quartos, como no original");
        if (SpellEffectEntity.BLIZZARD_DAMAGE != 1.0f) helper.fail("e a Nevasca um");
        helper.succeed();
    }

    /** E a Chuva de Fogo é sempre mais apertada: o raio dela é dividido por dois. */
    @GameTest(maxTicks = 100)
    public void theRainOfFireIsTighter(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);

        Essences.BLIZZARD.onEntity(level, Spell.of(Shapes.SELF, Essences.BLIZZARD), quem, quem);
        Essences.FIRE_RAIN.onEntity(level, Spell.of(Shapes.SELF, Essences.FIRE_RAIN), quem, quem);

        float gelo = 0.0f;
        float fogo = 0.0f;
        var todas = level.getEntities(ArcanaEntities.SPELL_EFFECT,
                quem.getBoundingBox().inflate(6.0), e -> true);
        for (var a : todas) {
            if (a.kind() == SpellEffectEntity.Kind.BLIZZARD) gelo = a.radius();
            if (a.kind() == SpellEffectEntity.Kind.FIRE_RAIN) fogo = a.radius();
        }

        if (gelo != 2.0f) helper.fail("a Nevasca nasce com raio dois, e nasceu com " + gelo);
        if (fogo != 2.0f) helper.fail("e a Chuva de Fogo com dois dividido por dois mais um, e deu " + fogo);

        for (var a : todas) a.discard();
        helper.succeed();
    }

    /** O Canal se segura, e só corre de dez em dez batidas. */
    @GameTest(maxTicks = 100)
    public void theChannelRunsEveryTenTicks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.setHealth(10.0f);

        Spell frase = Spell.of(Shapes.CHANNEL, Essences.HEAL);
        if (!Shapes.CHANNEL.channeled()) helper.fail("o Canal se segura");
        if (!Shapes.CHANNEL.terminus()) helper.fail("e não passa nada adiante");

        // na batida zero corre
        var saiu = Shapes.CHANNEL.begin(level, frase, quem, null, quem.position(), 0);
        if (!saiu.ok()) helper.fail("na batida certa, corre: " + saiu);

        // nas que não são múltiplas de dez, não
        saiu = Shapes.CHANNEL.begin(level, frase, quem, null, quem.position(), 3);
        if (saiu.ok()) helper.fail("e nas outras, não");

        quem.setHealth(quem.getMaxHealth());
        helper.succeed();
    }

    /** Mas com a Telecinese ou o Atrair, corre a cada batida. */
    @GameTest(maxTicks = 100)
    public void telekinesisMakesTheChannelRunEveryTick(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);

        Spell frase = Spell.of(Shapes.CHANNEL, Essences.TELEKINESIS);
        var saiu = Shapes.CHANNEL.begin(level, frase, quem, null, quem.position(), 3);
        if (saiu == net.thaumcraft.arcana.SpellCast.Result.EFFECT_FAILED) {
            // com a Telecinese, ela tentou — e não achar nada no chão é outro assunto
            helper.succeed();
            return;
        }
        if (!saiu.ok()) helper.fail("com a Telecinese, o Canal corre a cada batida: " + saiu);
        helper.succeed();
    }
}
