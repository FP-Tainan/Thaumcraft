package net.thaumcraft.research;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.entity.MindSpiderEntity;
import net.thaumcraft.net.TCNetwork;
import net.thaumcraft.registry.TCEffects;
import net.thaumcraft.registry.TCEntities;

import java.util.List;
import java.util.function.Consumer;

/**
 * Os eventos da distorção: o {@code WarpEvents} da 4.2.3.5.
 *
 * <p>De cem em cem segundos ({@code tickCount % 2000}), quem tem distorção e não está protegido pode ter um evento: a
 * chance é a raiz do contador em cem; o contador desce e o evento sai do sorteio de {@code 0} até a distorção (média da
 * total, da total e do contador, no máximo cem). Quanto mais alto o número, pior: sussurros e pontos de pesquisa,
 * cansaço de vis, taumarria, fome estranha, névoa, vista embaçada, desprezo do sol, fadiga, visão noturna, olhar mortal,
 * aranhas da mente (falsas e de verdade), cegueira... Depois, com distorção de verdade (a permanente e a que gruda)
 * acima de dez, vinte e cinco e cinquenta, as pesquisas proibidas se abrem. E a temporária perde um ponto.
 */
public final class WarpEvents {
    /** O guardião eldritch que vem na névoa ({@code spawnGuardian}); chega com o Eldritch. */
    public static Consumer<Player> guardianSpawner = player -> {
    };

    /** A distorção do equipamento (o {@code IWarpingGear}): a mão, a armadura e os quatro baubles. */
    public interface WarpingGear {
        int getWarp(ItemStack stack, Player player);
    }

    private WarpEvents() {
    }

    /** A cada tique do servidor: os eventos de cem em cem segundos e o olhar mortal de meio em meio. */
    public static void tick(ServerPlayer player) {
        if (player.tickCount > 0 && player.tickCount % 2000 == 0 && !player.hasEffect(TCEffects.WARP_WARD)) checkWarpEvent(player);
        if (player.tickCount % 10 == 0 && player.hasEffect(TCEffects.DEATH_GAZE)) checkDeathGaze(player);
    }

    private static void say(Player player, String key, ChatFormatting colour) {
        player.sendSystemMessage(Component.translatable(key).withStyle(colour, ChatFormatting.ITALIC));
    }

    private static void say(Player player, String key) {
        say(player, key, ChatFormatting.DARK_PURPLE);
    }

    /** Um efeito que o leite não tira (o {@code getCurativeItems().clear()}). */
    private static MobEffectInstance lasting(net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, int duration, int amplifier, boolean ambient) {
        return new MobEffectInstance(effect, duration, amplifier, ambient, true);
    }

    public static void checkWarpEvent(ServerPlayer player) {
        PlayerKnowledge knowledge = Knowledges.of(player);
        var random = player.getRandom();
        int warp = knowledge.warpTotal();
        int actualwarp = knowledge.warpPerm() + knowledge.warpSticky();
        warp += getWarpFromGear(player);
        int warpCounter = knowledge.warpCounter();
        int r = random.nextInt(100);
        if (warpCounter > 0 && warp > 0 && r <= Math.sqrt(warpCounter)) {
            warp = Math.min(100, (warp + warp + warpCounter) / 3);
            warpCounter = (int) (warpCounter - Math.max(5.0, Math.sqrt(warpCounter) * 2.0));
            knowledge.setWarpCounter(warpCounter);
            Knowledges.save(player, knowledge);
            int eff = random.nextInt(warp);
            // a máscara do diabo sorridente (a de número zero) tira um pouco
            if (net.thaumcraft.event.FortressMasks.mask(player) == 0) eff -= 2 + random.nextInt(4);
            TCNetwork.miscEvent(player, 0);
            if (eff > 0) {
                if (eff <= 4) {
                    grantResearch(player, 1);
                    say(player, "warp.text.3");
                } else if (eff > 8) {
                    if (eff <= 12) {
                        say(player, "warp.text.11");
                    } else if (eff <= 16) {
                        net.thaumcraft.research.Incurable.add(player, lasting(TCEffects.VIS_EXHAUST, 5000, Math.min(3, warp / 15), true));
                        say(player, "warp.text.1");
                    } else if (eff <= 20) {
                        net.thaumcraft.research.Incurable.add(player, lasting(TCEffects.THAUMARHIA, Math.min(32000, 10 * warp), 0, true));
                        say(player, "warp.text.15");
                    } else if (eff <= 24) {
                        net.thaumcraft.research.Incurable.add(player, lasting(TCEffects.UNNATURAL_HUNGER, 5000, Math.min(3, warp / 15), true));
                        say(player, "warp.text.2");
                    } else if (eff <= 28) {
                        say(player, "warp.text.12");
                    } else if (eff <= 32) {
                        spawnMist(player, warp, 1);
                    } else if (eff <= 36) {
                        player.addEffect(new MobEffectInstance(TCEffects.BLURRED_VISION, Math.min(32000, 10 * warp), 0, true, true));
                    } else if (eff <= 40) {
                        net.thaumcraft.research.Incurable.add(player, lasting(TCEffects.SUN_SCORNED, 5000, Math.min(3, warp / 15), true));
                        say(player, "warp.text.5");
                    } else if (eff <= 44) {
                        player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 1200, Math.min(3, warp / 15), true, true));
                        say(player, "warp.text.9");
                    } else if (eff <= 48) {
                        net.thaumcraft.research.Incurable.add(player, lasting(TCEffects.INFECTIOUS_VIS_EXHAUST, 6000, Math.min(3, warp / 15), false));
                        say(player, "warp.text.1");
                    } else if (eff <= 52) {
                        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, Math.min(40 * warp, 6000), 0, true, true));
                        say(player, "warp.text.10");
                    } else if (eff <= 56) {
                        net.thaumcraft.research.Incurable.add(player, lasting(TCEffects.DEATH_GAZE, 6000, Math.min(3, warp / 15), true));
                        say(player, "warp.text.4");
                    } else if (eff <= 60) {
                        suddenlySpiders(player, warp, false);
                    } else if (eff <= 64) {
                        say(player, "warp.text.13");
                    } else if (eff <= 68) {
                        spawnMist(player, warp, warp / 30);
                    } else if (eff <= 72) {
                        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, Math.min(32000, 5 * warp), 0, true, true));
                    } else if (eff == 76) {
                        if (Knowledges.of(player).warpSticky() > 0) Warp.addSticky(player, -1);
                        say(player, "warp.text.14");
                    } else if (eff <= 80) {
                        net.thaumcraft.research.Incurable.add(player, lasting(TCEffects.UNNATURAL_HUNGER, 6000, Math.min(3, warp / 15), true));
                        say(player, "warp.text.2");
                    } else if (eff <= 84) {
                        grantResearch(player, warp / 10);
                        say(player, "warp.text.3");
                    } else if (eff > 88) {
                        if (eff <= 92) suddenlySpiders(player, warp, true);
                        else spawnMist(player, warp, warp / 15);
                    }
                }
            }
            if (actualwarp > 10 && !knows(player, "BATHSALTS") && !knows(player, "@BATHSALTS")) {
                say(player, "warp.text.8");
                ResearchManager.clue(player, "@BATHSALTS");
            }
            if (actualwarp > 25 && !knows(player, "ELDRITCHMINOR")) {
                grantResearch(player, 10);
                ResearchManager.complete(player, "ELDRITCHMINOR");
            }
            if (actualwarp > 50 && !knows(player, "ELDRITCHMAJOR")) {
                grantResearch(player, 20);
                ResearchManager.complete(player, "ELDRITCHMAJOR");
            }
        }
        PlayerKnowledge after = Knowledges.of(player);
        after.addWarpTemp(-1);
        Knowledges.save(player, after);
    }

    private static boolean knows(Player player, String key) {
        return Knowledges.of(player).hasResearch(key);
    }

    /** A névoa ({@code PacketMiscEvent} 1) e, na névoa, até oito guardiões. */
    private static void spawnMist(ServerPlayer player, int warp, int guardian) {
        TCNetwork.miscEvent(player, 1);
        if (guardian > 0) {
            guardian = Math.min(8, guardian);
            for (int a = 0; a < guardian; a++) guardianSpawner.accept(player);
        }
        say(player, "warp.text.6");
    }

    /** Pontos de pesquisa dos primordiais, de um a {@code times}, com os avisos. */
    private static void grantResearch(ServerPlayer player, int times) {
        var random = player.getRandom();
        int amt = 1 + random.nextInt(Math.max(1, times));
        PlayerKnowledge knowledge = Knowledges.of(player);
        List<Aspect> primals = Aspects.primals();
        for (int a = 0; a < amt; a++) {
            Aspect aspect = primals.get(random.nextInt(6));
            knowledge.pool().add(aspect, 1);
            TCNetwork.aspectPool(player, aspect, 1, knowledge.points(aspect));
        }
        Knowledges.save(player, knowledge);
    }

    /** O {@code getRandomIntegerInRange(7, 24) * getRandomIntegerInRange(-1, 1)} das três coordenadas. */
    private static int offset(net.minecraft.util.RandomSource random) {
        return Mth.nextInt(random, 7, 24) * Mth.nextInt(random, -1, 1);
    }

    /** Um lugar para uma criatura nascer em volta do jogador: chão firme, sem ninguém nem nada no caminho, fora d'água. */
    private static boolean place(ServerPlayer player, Mob mob) {
        ServerLevel level = player.level();
        var random = player.getRandom();
        int i = Mth.floor(player.getX()), j = Mth.floor(player.getY()), k = Mth.floor(player.getZ());
        for (int l = 0; l < 50; l++) {
            int i1 = i + offset(random), j1 = j + offset(random), k1 = k + offset(random);
            BlockPos below = new BlockPos(i1, j1 - 1, k1);
            if (!level.getBlockState(below).isFaceSturdy(level, below, net.minecraft.core.Direction.UP)) continue;
            mob.snapTo(i1, j1, k1);
            if (level.isUnobstructed(mob) && level.noCollision(mob) && !level.containsAnyLiquid(mob.getBoundingBox())) return true;
        }
        return false;
    }

    /** As aranhas da mente: até cinquenta, falsas (só ele vê) ou de verdade. */
    private static void suddenlySpiders(ServerPlayer player, int warp, boolean real) {
        int spawns = Math.min(50, warp);
        for (int a = 0; a < spawns; a++) {
            MindSpiderEntity spider = TCEntities.MIND_SPIDER.create(player.level(), EntitySpawnReason.EVENT);
            if (spider == null || !place(player, spider)) continue;
            spider.setTarget(player);
            if (!real) {
                spider.setViewer(player.getName().getString());
                spider.setHarmless(true);
            }
            player.level().addFreshEntity(spider);
        }
        say(player, "warp.text.7");
    }

    /**
     * O olhar mortal: o que está no cone de visão do jogador (a até {@code 8 + 3 × nível}, no máximo 24) e à vista vira
     * contra ele e murcha por quatro segundos.
     */
    public static void checkDeathGaze(ServerPlayer player) {
        MobEffectInstance pe = player.getEffect(TCEffects.DEATH_GAZE);
        if (pe == null) return;
        int range = Math.min(8 + pe.getAmplifier() * 3, 24);
        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(range), e -> e != player)) {
            if (!entity.isPickable() || !entity.isAlive() || !isVisibleTo(0.75f, player, entity, range) || !player.hasLineOfSight(entity)) continue;
            if (entity instanceof Player && !player.level().isPvpAllowed()) continue;
            if (entity.hasEffect(MobEffects.WITHER)) continue;
            entity.setLastHurtByMob(player);
            entity.setLastHurtByPlayer(player, 100);
            if (entity instanceof Mob mob) mob.setTarget(player);
            entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 80));
        }
    }

    /** O {@code isVisibleTo} com o {@code isLyingInCone}: o meio da criatura dentro do cone de abertura {@code fov}. */
    public static boolean isVisibleTo(float fov, LivingEntity ent, LivingEntity ent2, float range) {
        Vec3 x = new Vec3(ent2.getX(), ent2.getBoundingBox().minY + ent2.getBbHeight() / 2.0f, ent2.getZ());
        Vec3 t = new Vec3(ent.getX(), ent.getBoundingBox().minY + ent.getEyeHeight(), ent.getZ());
        Vec3 b = ent.getLookAngle().scale(range).add(t);
        Vec3 apexToX = t.subtract(x), axis = t.subtract(b);
        double dot = apexToX.dot(axis);
        if (dot / apexToX.length() / axis.length() <= Math.cos(fov / 2.0f)) return false;
        return dot / axis.length() < axis.length();
    }

    /** O {@code getWarpFromGear}: a mão, a armadura e os baubles. */
    public static int getWarpFromGear(Player player) {
        int w = finalWarp(player.getMainHandItem(), player);
        for (var slot : new net.minecraft.world.entity.EquipmentSlot[]{net.minecraft.world.entity.EquipmentSlot.FEET,
                net.minecraft.world.entity.EquipmentSlot.LEGS, net.minecraft.world.entity.EquipmentSlot.CHEST, net.minecraft.world.entity.EquipmentSlot.HEAD}) {
            w += finalWarp(player.getItemBySlot(slot), player);
        }
        for (int a = 0; a < net.thaumcraft.baubles.Baubles.SIZE; a++) w += finalWarp(net.thaumcraft.baubles.Baubles.get(player, a), player);
        return w;
    }

    /** O {@code getFinalWarp} do {@code EventHandlerRunic}. */
    public static int finalWarp(ItemStack stack, Player player) {
        return !stack.isEmpty() && stack.getItem() instanceof WarpingGear gear ? gear.getWarp(stack, player) : 0;
    }
}
