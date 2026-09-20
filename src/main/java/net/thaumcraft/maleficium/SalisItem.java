package net.thaumcraft.maleficium;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * Os sais do Tainted Magic: o {@code ItemSalis} da 8.1.1.
 *
 * <p>Largado no chão, o sal fica cem tiques chiando e some — e com ele vira o tempo ou o dia. O Tempestas liga e
 * desliga a chuva (e às vezes a tempestade); o Aevum joga o dia para a noite e a noite para o dia.
 */
public class SalisItem extends Item {
    /** Qual dos dois sais é este. */
    public enum Kind {
        /** Salis Tempestas: a chuva. */
        TEMPESTAS,
        /** Salis Aevum: a hora do dia. */
        AEVUM
    }

    /** Quantos tiques o sal aguenta no chão antes de se gastar (o {@code entity.ticksExisted == 100}). */
    public static final int LIFE = 100;

    private final Kind kind;

    public SalisItem(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public Kind kind() {
        return this.kind;
    }

    /** O que o sal faz ao se gastar; roda no servidor. */
    public void spend(ItemEntity entity) {
        Level level = entity.level();
        if (!(level instanceof ServerLevel server)) return;
        switch (this.kind) {
            case TEMPESTAS -> {
                boolean raining = server.isRaining();
                var weather = server.getWeatherData();
                // o original zera o contador de tempo limpo quando a chuva começa, e dá um dia de sol quando ela pára
                weather.setClearWeatherTime(raining ? 24000 : 0);
                weather.setRainTime(raining ? 0 : 24000);
                weather.setRaining(!raining);
                if (!raining && server.getRandom().nextInt(10) == 0) {
                    weather.setThundering(true);
                    weather.setThunderTime(24000);
                }
            }
            // o relógio do mundo: de dia empurra para a noite, de noite para o amanhecer
            case AEVUM -> server.dimensionType().defaultClock().ifPresent(clock ->
                    server.clockManager().moveToTimeMarker(clock,
                            server.isBrightOutside() ? net.minecraft.world.clock.ClockTimeMarkers.NIGHT
                                    : net.minecraft.world.clock.ClockTimeMarkers.DAY));
        }
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                net.minecraft.sounds.SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL,
                0.3f, 1.0f + level.getRandom().nextFloat() * 0.25f);
        entity.discard();
    }
}
