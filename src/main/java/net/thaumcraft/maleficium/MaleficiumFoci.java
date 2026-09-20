package net.thaumcraft.maleficium;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.api.FocusUpgrades;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.item.FocusItem;
import net.thaumcraft.item.FocusUpgradeTable;
import net.thaumcraft.item.Focuses;
import net.thaumcraft.item.WandItem;
import net.thaumcraft.maleficium.entity.DarkMatterEntity;
import net.thaumcraft.maleficium.entity.DiffusionEntity;
import net.thaumcraft.maleficium.entity.HomingShardEntity;
import net.thaumcraft.registry.TCEffects;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCSounds;
import net.thaumcraft.research.Warp;

import java.util.List;

/**
 * Os seis focos do Maleficium e as cinco melhorias que só eles aceitam: o {@code ItemFocus*} e o
 * {@code TMFocusUpgrades} do Tainted Magic 8.1.1.
 *
 * <p>Os custos, os alcances e os efeitos são os do original. O que cada melhoria faz está no foco que a aceita:
 * a <i>sanidade</i> tira a distorção que o foco traz, o <i>anticorpo</i> tira o veneno, a <i>corrosiva</i> faz
 * definhar, a <i>persistente</i> faz a lasca procurar outro alvo e a <i>difusão</i> troca o tiro por uma névoa.
 */
public final class MaleficiumFoci {
    // os números das melhorias novas: bem acima das vinte e uma do Thaumcraft, como o original faz com a config
    public static final FocusUpgradeTable.Type SANITY = FocusUpgrades.register((short) 64, "sanity", "sanity",
            new AspectList().add(Aspects.MIND, 1).add(Aspects.HEAL, 1));
    public static final FocusUpgradeTable.Type ANTIBODY = FocusUpgrades.register((short) 65, "antibody", "antibody",
            new AspectList().add(Aspects.TAINT, 1).add(Aspects.HEAL, 1));
    public static final FocusUpgradeTable.Type CORROSIVE = FocusUpgrades.register((short) 66, "corrosive", "corrosive",
            new AspectList().add(Aspects.TAINT, 1).add(Aspects.POISON, 1));
    public static final FocusUpgradeTable.Type PERSISTENT = FocusUpgrades.register((short) 67, "persistent", "persistent",
            new AspectList().add(Aspects.ARMOR, 1).add(Aspects.MOTION, 1).add(Aspects.ENERGY, 1));
    public static final FocusUpgradeTable.Type DIFFUSION = FocusUpgrades.register((short) 68, "diffusion", "diffusion",
            new AspectList().add(Aspects.DARKNESS, 1).add(Aspects.ELDRITCH, 2).add(Aspects.AURA, 4));

    /** Os custos de cada foco, como o original os declara. */
    public static final AspectList COST_TAINT_SWARM = new AspectList().add(Aspects.EARTH, 50).add(Aspects.WATER, 50);
    public static final AspectList COST_DARK_MATTER = new AspectList().add(Aspects.ENTROPY, 150).add(Aspects.FIRE, 100);
    public static final AspectList COST_SHOCKWAVE = new AspectList().add(Aspects.AIR, 150).add(Aspects.ENTROPY, 100);
    public static final AspectList COST_VIS_SHARD = new AspectList().add(Aspects.FIRE, 10).add(Aspects.ENTROPY, 10).add(Aspects.AIR, 10);
    public static final AspectList COST_LUMOS = new AspectList().add(Aspects.AIR, 10).add(Aspects.FIRE, 25);
    public static final AspectList COST_MAGE_MACE = new AspectList().add(Aspects.WEAPON, 1);

    private MaleficiumFoci() {
    }

    public static void init() {
        // os postos de cada foco, do getPossibleUpgradesByRank do original
        List<FocusUpgradeTable.Type> basico = List.of(FocusUpgradeTable.FRUGAL, FocusUpgradeTable.POTENCY, FocusUpgradeTable.ENLARGE);
        FocusUpgrades.ranks("dark_matter", List.of(basico, basico,
                List.of(FocusUpgradeTable.FRUGAL, FocusUpgradeTable.POTENCY, FocusUpgradeTable.ENLARGE, CORROSIVE, SANITY),
                basico,
                List.of(FocusUpgradeTable.FRUGAL, FocusUpgradeTable.POTENCY, FocusUpgradeTable.ENLARGE, DIFFUSION)));
        FocusUpgrades.ranks("taint_swarm", List.of(basico, basico,
                List.of(FocusUpgradeTable.FRUGAL, FocusUpgradeTable.POTENCY, ANTIBODY), basico, basico));
        FocusUpgrades.ranks("shockwave", List.of(basico, basico, basico, basico, basico));
        FocusUpgrades.ranks("vis_shard", List.of(basico, basico,
                List.of(FocusUpgradeTable.FRUGAL, FocusUpgradeTable.POTENCY, PERSISTENT), basico, basico));
        FocusUpgrades.ranks("lumos", List.of(List.of(FocusUpgradeTable.FRUGAL), List.of(FocusUpgradeTable.FRUGAL),
                List.of(FocusUpgradeTable.FRUGAL), List.of(FocusUpgradeTable.FRUGAL), List.of(FocusUpgradeTable.FRUGAL)));
        FocusUpgrades.ranks("mage_mace", List.of(List.of(FocusUpgradeTable.POTENCY), List.of(FocusUpgradeTable.POTENCY),
                List.of(FocusUpgradeTable.POTENCY), List.of(FocusUpgradeTable.POTENCY), List.of(FocusUpgradeTable.POTENCY)));

        Focuses.register("taint_swarm", MaleficiumFoci::taintSwarm);
        Focuses.register("dark_matter", MaleficiumFoci::darkMatter);
        Focuses.register("shockwave", MaleficiumFoci::shockwave);
        Focuses.register("vis_shard", MaleficiumFoci::visShard);
        Focuses.register("lumos", MaleficiumFoci::lumos);
        // a maça do mago não atira nada: ela pesa na varinha, e isso é conta de dano
        Focuses.register("mage_mace", (level, player, wand, focus) -> false);
    }

    /** Quantas vezes a melhoria aparece no foco preso na varinha. */
    private static boolean has(ItemStack wand, FocusUpgradeTable.Type type) {
        return FocusItem.isUpgradedWith(WandItem.focusStack(wand), type);
    }

    private static int potency(ItemStack wand) {
        return FocusItem.level(WandItem.focusStack(wand), FocusUpgradeTable.POTENCY);
    }

    private static int enlarge(ItemStack wand) {
        return FocusItem.level(WandItem.focusStack(wand), FocusUpgradeTable.ENLARGE);
    }

    /** O enxame de mácula: manda um enxame do Thaumcraft atrás de quem a varinha aponta. */
    private static boolean taintSwarm(Level level, Player player, ItemStack wand, FocusItem focus) {
        Entity alvo = Focuses.pointedEntity(level, player, 32.0);
        if (!(alvo instanceof LivingEntity living)) return false;
        if (!WandItem.consumeFocus(wand, focus.cost(WandItem.focusStack(wand)), true, player)) return false;

        var swarm = TCEntities.TAINT_SWARM.create(level, net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
        if (swarm == null) return false;
        Vec3 look = player.getLookAngle();
        swarm.snapTo(player.getX() + look.x / 2.0, player.getEyeY() + look.y / 2.0, player.getZ() + look.z / 2.0,
                player.getYRot(), player.getXRot());
        var attack = swarm.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        if (attack != null) attack.setBaseValue(5.0 + potency(wand));
        swarm.setTarget(living);
        level.addFreshEntity(swarm);

        // sem o anticorpo, um em cada três lançamentos envenena quem lançou
        if (!has(wand, ANTIBODY) && level.getRandom().nextInt(3) == 0) {
            player.addEffect(new MobEffectInstance(TCEffects.FLUX_TAINT, 40, 2));
        }
        return true;
    }

    /** A matéria escura: o tiro concentrado, ou a névoa quando o foco tem a difusão. */
    private static boolean darkMatter(Level level, Player player, ItemStack wand, FocusItem focus) {
        if (!WandItem.consumeFocus(wand, focus.cost(WandItem.focusStack(wand)), true, player)) return false;
        boolean corrosive = has(wand, CORROSIVE);
        if (has(wand, DIFFUSION)) {
            for (int i = 0; i < 2 + potency(wand); i++) {
                float scatter = has(wand, FocusUpgradeTable.ENLARGE) ? 12.0f + enlarge(wand) : 9.0f;
                DiffusionEntity cloud = new DiffusionEntity(level, player, scatter, 12.0f + potency(wand), corrosive);
                cloud.setPos(cloud.position().add(cloud.getDeltaMovement()));
                level.addFreshEntity(cloud);
            }
            if (!has(wand, SANITY) && level.getRandom().nextInt(1000) == 0) Warp.addSticky(player, 1);
            return true;
        }
        DarkMatterEntity orb = new DarkMatterEntity(level, player, 16.0f + potency(wand), enlarge(wand), corrosive);
        level.addFreshEntity(orb);
        if (!has(wand, SANITY) && level.getRandom().nextInt(20) == 0) Warp.addSticky(player, 1);
        level.playSound(null, player, TCSounds.EG_ATTACK.value(), SoundSource.PLAYERS, 0.4f,
                1.0f + level.getRandom().nextFloat() * 0.1f);
        return true;
    }

    /** A onda de choque: empurra tudo em volta e machuca quem estiver perto. */
    private static boolean shockwave(Level level, Player player, ItemStack wand, FocusItem focus) {
        if (!WandItem.consumeFocus(wand, focus.cost(WandItem.focusStack(wand)), true, player)) return false;
        int potency = potency(wand);
        double alcance = 15.0 + enlarge(wand);
        for (LivingEntity alvo : level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(alcance), e -> e != player && e.isAlive() && !e.isInvulnerable())) {
            double dist = alvo.distanceTo(player);
            if (dist < 7.0 && level instanceof ServerLevel server) {
                alvo.hurtServer(server, player.damageSources().magic(), 2.0f + potency);
            }
            Vec3 fora = alvo.position().subtract(player.position()).normalize();
            alvo.push(fora.x * (5.0 + potency), 1.5 + potency * 0.1, fora.z * (5.0 + potency));
            alvo.hurtMarked = true;
        }
        level.playSound(null, player, TCSounds.SHOCKWAVE.value(), SoundSource.PLAYERS, 5.0f,
                1.5f * level.getRandom().nextFloat());
        return true;
    }

    /** A lasca de vis: sai torta e persegue quem a varinha apontou. */
    private static boolean visShard(Level level, Player player, ItemStack wand, FocusItem focus) {
        Entity alvo = Focuses.pointedEntity(level, player, 32.0);
        if (!(alvo instanceof LivingEntity living)) return false;
        if (!WandItem.consumeFocus(wand, focus.cost(WandItem.focusStack(wand)), true, player)) return false;
        HomingShardEntity shard = new HomingShardEntity(level, player, living, potency(wand), has(wand, PERSISTENT));
        level.addFreshEntity(shard);
        level.playSound(null, shard, TCSounds.SHARD.value(), SoundSource.PLAYERS, 0.3f,
                1.1f + level.getRandom().nextFloat() * 0.1f);
        return true;
    }

    /** O Lumos: põe a luzinha na face que a varinha apontou. */
    private static boolean lumos(Level level, Player player, ItemStack wand, FocusItem focus) {
        HitResult mira = Focuses.targetBlock(level, player);
        if (!(mira instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) return false;
        BlockPos pos = hit.getBlockPos();
        if (!level.getBlockState(pos).canBeReplaced()) pos = pos.relative(hit.getDirection());
        if (!level.getBlockState(pos).canBeReplaced()) return false;
        if (!WandItem.consumeFocus(wand, focus.cost(WandItem.focusStack(wand)), true, player)) return false;
        level.setBlock(pos, MaleficiumBlocks.LUMOS.defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_ALL);
        level.playSound(null, player, TCSounds.ICE.value(), SoundSource.PLAYERS, 0.3f,
                1.1f + level.getRandom().nextFloat() * 0.1f);
        return true;
    }

    /** O quanto a maça do mago soma ao dano da varinha: o {@code MAGE_MACE_DMG_INC_BASE} do original. */
    public static double maceDamage(ItemStack wand) {
        return 8.0 + potency(wand) * 2.0;
    }

    /** O nome que o foco usa para dizer que direção o {@code Direction} da mira aponta. */
    static Direction face(BlockHitResult hit) {
        return hit.getDirection();
    }
}
