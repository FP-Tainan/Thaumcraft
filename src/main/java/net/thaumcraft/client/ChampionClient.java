package net.thaumcraft.client;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.client.fx.GenericFx;
import net.thaumcraft.client.fx.Spark;
import net.thaumcraft.client.fx.TaintFx;
import net.thaumcraft.client.fx.ThaumFx;
import net.thaumcraft.event.Champions;

/**
 * As faíscas de cada tipo de campeão: os {@code showFX} dos {@code ChampionMod*} da 4.2.3.5, chamados a cada tique do
 * monstro do lado de quem vê.
 */
public final class ChampionClient {
    private ChampionClient() {
    }

    public static void init() {
        Champions.ChampionFx.client = ChampionClient::show;
    }

    /** O {@code drawGenericParticles}: uma partícula da folha, num ponto sorteado dentro da caixa do monstro. */
    private static void generic(LivingEntity e, float hFrac, double mx, double my, double mz, float r, float g, float b, float alpha,
                                boolean loop, int start, int num, int inc, int age, float scale) {
        RandomSource rand = e.level().getRandom();
        float w = rand.nextFloat() * e.getBbWidth();
        float d = rand.nextFloat() * e.getBbWidth();
        float h = rand.nextFloat() * e.getBbHeight() * hFrac;
        var box = e.getBoundingBox();
        ThaumFx.add(new GenericFx(box.minX + w, box.minY + h, box.minZ + d, mx, my, mz, r, g, b, alpha, loop, start, num, inc, age, 0, scale));
    }

    static void show(LivingEntity e, Champions.Mod mod) {
        RandomSource rand = e.level().getRandom();
        switch (mod) {
            case BOLD -> {
                if (rand.nextBoolean()) return;
                float w = rand.nextFloat() * e.getBbWidth(), d = rand.nextFloat() * e.getBbWidth(), h = rand.nextFloat() * e.getBbHeight() / 3.0f;
                var box = e.getBoundingBox();
                float r = 0.3f - rand.nextFloat() * 0.1f, b = 0.8f + rand.nextFloat() * 0.2f;
                int argb = 0xFF000000 | (int) (r * 255) << 16 | (int) (b * 255);
                Spark.spawn(new Vec3(box.minX + w, box.minY + h, box.minZ + d), 0.2f, argb, rand);
            }
            case ARMOR -> {
                if (rand.nextInt(4) != 0) return;
                generic(e, 1.0f, 0.0, 0.0, 0.0, 0.9f, 0.9f, 0.9f + rand.nextFloat() * 0.1f, 0.7f, false, 112, 9, 1,
                        5 + rand.nextInt(4), 0.6f + rand.nextFloat() * 0.2f);
            }
            case FIERY -> generic(e, 1.0f, 0.0, 0.03, 0.0, 0.9f + rand.nextFloat() * 0.1f, 1.0f, 1.0f, 0.7f, false, 160, 10, 1,
                    8 + rand.nextInt(4), 0.7f + rand.nextFloat() * 0.2f);
            case GRIM -> {
                if (rand.nextBoolean()) return;
                generic(e, 1.0f, 0.0, -0.02, 0.0, rand.nextFloat() * 0.2f, rand.nextFloat() * 0.2f, rand.nextFloat() * 0.2f, 0.8f, false,
                        160, 10, 1, 8 + rand.nextInt(4), 0.6f + rand.nextFloat() * 0.4f);
            }
            case INFESTED -> {
                if (rand.nextBoolean()) TaintFx.slimeJump(e, 0);
            }
            case MIGHTY -> {
                if (rand.nextFloat() > 0.3f) return;
                int p = 176 + rand.nextInt(4) * 3;
                generic(e, 1.0f, 0.0, 0.0, 0.0, 0.8f + rand.nextFloat() * 0.2f, 0.8f + rand.nextFloat() * 0.2f, 0.8f + rand.nextFloat() * 0.2f,
                        0.7f, false, p, 3, 1, 4 + rand.nextInt(3), 1.0f + rand.nextFloat() * 0.3f);
            }
            case VENOMOUS -> {
                if (rand.nextBoolean()) return;
                generic(e, 1.0f, 0.0, 0.02, 0.0, 0.2f, 0.6f + rand.nextFloat() * 0.1f, 0.2f + rand.nextFloat() * 0.1f, 0.7f, false, 147, 4, 1,
                        8 + rand.nextInt(4), 0.5f + rand.nextFloat() * 0.2f);
            }
            case SICKLY -> {
                if (rand.nextBoolean()) return;
                generic(e, 1.0f, 0.0, -0.02, 0.0, 0.2f, 0.6f + rand.nextFloat() * 0.1f, 0.2f + rand.nextFloat() * 0.1f, 0.5f, false, 1, 4, 2,
                        5 + rand.nextInt(4), 0.9f + rand.nextFloat() * 0.3f);
            }
            case SPINE -> {
                if (rand.nextBoolean()) return;
                int p = 176 + rand.nextInt(4) * 3;
                generic(e, 1.0f, 0.0, 0.0, 0.0, 0.5f + rand.nextFloat() * 0.2f, 0.1f + rand.nextFloat() * 0.2f, 0.1f + rand.nextFloat() * 0.2f,
                        0.7f, false, p, 3, 1, 3, 1.2f + rand.nextFloat() * 0.3f);
            }
            case UNDYING -> {
                if (rand.nextBoolean()) return;
                generic(e, 1.0f, 0.0, 0.03, 0.0, 0.1f + rand.nextFloat() * 0.1f, 0.8f + rand.nextFloat() * 0.2f, 0.1f + rand.nextFloat() * 0.1f,
                        0.9f, true, 21, 4, 1, 4 + rand.nextInt(4), 0.5f + rand.nextFloat() * 0.2f);
            }
            case VAMPIRIC -> {
                if (rand.nextFloat() > 0.2f) return;
                generic(e, 1.0f, 0.0, 0.0, 0.0, 0.9f + rand.nextFloat() * 0.1f, 0.0f, 0.0f, 0.9f, false, 147, 4, 1, 8 + rand.nextInt(4),
                        0.5f + rand.nextFloat() * 0.2f);
            }
            case WARDED -> {
                if (rand.nextBoolean()) return;
                generic(e, 1.0f, 0.0, 0.0, 0.0, 0.5f + rand.nextFloat() * 0.1f, 0.5f + rand.nextFloat() * 0.1f, 0.5f + rand.nextFloat() * 0.1f,
                        0.6f, true, 21, 4, 1, 4 + rand.nextInt(4), 0.8f + rand.nextFloat() * 0.3f);
            }
            case WARP -> {
                if (rand.nextBoolean()) return;
                generic(e, 1.0f, 0.0, 0.0, 0.0, 0.8f + rand.nextFloat() * 0.2f, 0.0f, 0.9f + rand.nextFloat() * 0.1f, 0.7f, true, 72, 8, 1,
                        10 + rand.nextInt(4), 0.6f + rand.nextFloat() * 0.4f);
            }
        }
    }
}
