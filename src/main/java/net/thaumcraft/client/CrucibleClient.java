package net.thaumcraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.block.entity.CrucibleBlockEntity;
import net.thaumcraft.client.fx.Bubble;
import net.thaumcraft.client.fx.GenericFx;
import net.thaumcraft.registry.TCSounds;

import java.util.List;

/**
 * Os efeitos do crisol do lado de quem vê: o {@code drawEffects} do {@code TileCrucible} (a espuma da fervura, a que
 * escorre pela borda do crisol cheio demais e as bolhas da cor do que está dissolvido) e os eventos 1 (faíscas) e 2 (a
 * fervura de quando algo cai dentro: o som e vinte bolhas subindo com a força dada), com o {@code crucibleFroth},
 * {@code crucibleFrothDown}, {@code crucibleBubble}, {@code crucibleBoil} e {@code blockSparkle} do {@code ClientProxy}.
 */
public final class CrucibleClient {
    private CrucibleClient() {
    }

    public static void init() {
        CrucibleBlockEntity.clientEffects = CrucibleClient::draw;
        CrucibleBlockEntity.clientEvents = new CrucibleBlockEntity.ClientEvents() {
            @Override
            public void sparkle(CrucibleBlockEntity crucible) {
                BlockPos pos = crucible.getBlockPos();
                GenericFx.blockSparkle(pos.getX(), pos.getY(), pos.getZ(), -9999, 5);
            }

            @Override
            public void boil(CrucibleBlockEntity crucible, int strength) {
                BlockPos pos = crucible.getBlockPos();
                var level = Minecraft.getInstance().level;
                if (level == null) return;
                level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, TCSounds.SPILL.value(), SoundSource.BLOCKS, 0.2f, 1.0f, false);
                RandomSource random = level.getRandom();
                for (int q = 0; q < 10; q++) {
                    for (int a = 0; a < 2; a++) {
                        float r = 1.0f, g = 1.0f, b = 1.0f;
                        List<Aspect> aspects = crucible.aspects().getAspects();
                        if (!aspects.isEmpty()) {
                            int c = aspects.get(random.nextInt(aspects.size())).color();
                            r = (c >> 16 & 255) / 255.0f;
                            g = (c >> 8 & 255) / 255.0f;
                            b = (c & 255) / 255.0f;
                        }
                        Bubble.spawn(pos.getX() + 0.2 + random.nextFloat() * 0.6, pos.getY() + 0.1 + crucible.fluidHeight(),
                                pos.getZ() + 0.2 + random.nextFloat() * 0.6, r, g, b, 3, 0.003 * strength, random);
                    }
                }
            }
        };
    }

    private static void draw(CrucibleBlockEntity crucible) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        RandomSource random = level.getRandom();
        BlockPos pos = crucible.getBlockPos();
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        if (crucible.heat() > CrucibleBlockEntity.BOILING) {
            Bubble.froth(x + 0.2 + random.nextFloat() * 0.6, y + crucible.fluidHeight(), z + 0.2 + random.nextFloat() * 0.6, random);
            if (crucible.tagAmount() > 100) {
                for (int a = 0; a < 2; a++) {
                    Bubble.frothDown(x, y + 1, z + random.nextFloat(), random);
                    Bubble.frothDown(x + 1, y + 1, z + random.nextFloat(), random);
                    Bubble.frothDown(x + random.nextFloat(), y + 1, z, random);
                    Bubble.frothDown(x + random.nextFloat(), y + 1, z + 1, random);
                }
            }
        }
        List<Aspect> aspects = crucible.aspects().getAspects();
        if (random.nextInt(6) == 0 && !aspects.isEmpty()) {
            int c = aspects.get(random.nextInt(aspects.size())).color();
            int px = 5 + random.nextInt(22), py = 5 + random.nextInt(22);
            Bubble.spawn(x + px / 32.0 + 0.015625, y + 0.05 + crucible.fluidHeight(), z + py / 32.0 + 0.015625,
                    (c >> 16 & 255) / 255.0f, (c >> 8 & 255) / 255.0f, (c & 255) / 255.0f, 1, 0.002, random);
        }
    }
}
