package net.thaumcraft.event;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.EldritchMob;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.research.Warp;
import net.thaumcraft.world.OuterLands;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Os campeões: os monstros especiais da 4.2.3.5 ({@code ChampionModifier}, {@code EntityUtils.makeChampion} e os ganchos do
 * {@code EventHandlerEntity} e do {@code EventHandlerRunic}). Ao nascer, um monstro da lista tem uma pequena chance
 * (um em cem, com os descontos da dificuldade, do bioma e das Terras de Fora) de virar campeão de um dos treze tipos:
 * ganha trinta de vida, o triplo do dano, o nome do tipo antes do seu e um efeito — uns agem sozinhos a cada tique,
 * outros no golpe que ele dá, outros no golpe que ele leva. Morto por alguém, solta experiência e uma sacola de tesouro.
 *
 * <p>O original guardava o tipo num atributo; aqui é um anexo do monstro, sincronizado com quem o vê (as faíscas de
 * cada tipo são desenhadas no cliente).
 */
public final class Champions {
    /** Ainda não sorteado. */
    public static final int UNSET = -2;
    /** Sorteado, e não é campeão. */
    public static final int NONE = -1;

    public static final AttachmentType<Integer> CHAMPION = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT)
            .syncWith(ByteBufCodecs.VAR_INT.cast(), AttachmentSyncPredicate.all())
            .buildAndRegister(Thaumcraft.id("champion"));

    /** Os treze tipos, na ordem do original. O tipo diz quando o efeito age: -1 nunca, 0 a cada tique, 1 no golpe dado, 2 no levado. */
    public enum Mod {
        BOLD("bold", -1), SPINE("spine", 2), ARMOR("armor", 2), MIGHTY("mighty", -1), GRIM("grim", 1), WARDED("warded", 0),
        WARP("warp", 1), UNDYING("undying", 0), FIERY("fiery", 1), SICKLY("sickly", 1), VENOMOUS("venomous", 1),
        VAMPIRIC("vampiric", 1), INFESTED("infested", 2);

        public final String name;
        public final int type;

        Mod(String name, int type) {
            this.name = name;
            this.type = type;
        }

        public Component displayName() {
            return Component.translatable("champion.mod." + this.name);
        }
    }

    private static final AttributeModifier CHAMPION_HEALTH = new AttributeModifier(Thaumcraft.id("champion_health"), 30.0,
            AttributeModifier.Operation.ADD_VALUE);
    private static final AttributeModifier CHAMPION_DAMAGE = new AttributeModifier(Thaumcraft.id("champion_damage"), 2.0,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    private static final AttributeModifier BOLD_BUFF = new AttributeModifier(Thaumcraft.id("champion_bold"), 0.3,
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    private static final AttributeModifier MIGHTY_BUFF = new AttributeModifier(Thaumcraft.id("champion_mighty"), 3.0,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    /**
     * A lista do original ({@code championModWhitelist} e o {@code championWhiteList} das mensagens entre mods): quem pode
     * ser campeão e com que bônus de chance.
     */
    private static final Map<Predicate<LivingEntity>, Integer> WHITELIST = new LinkedHashMap<>();

    /** Os chefes (o pretor e os das Terras de Fora): sempre campeões, e com o nome feito por eles. */
    public interface Boss {
        void generateName();
    }

    static {
        WHITELIST.put(e -> e instanceof Zombie, 0);
        WHITELIST.put(e -> e instanceof Spider, 0);
        WHITELIST.put(e -> e instanceof Blaze, 0);
        WHITELIST.put(e -> e instanceof EnderMan, 0);
        WHITELIST.put(e -> e instanceof AbstractSkeleton, 0);
        WHITELIST.put(e -> e instanceof Witch, 1);
        WHITELIST.put(e -> e instanceof net.thaumcraft.entity.eldritch.EldritchCrabEntity, 0);
        WHITELIST.put(e -> e instanceof net.thaumcraft.entity.taint.TaintacleEntity, 2);
        WHITELIST.put(e -> e instanceof net.thaumcraft.entity.WispEntity, 1);
        WHITELIST.put(e -> e instanceof net.thaumcraft.entity.eldritch.InhabitedZombieEntity, 3);
        WHITELIST.put(e -> e instanceof net.thaumcraft.entity.PechEntity, 1);
        WHITELIST.put(e -> e instanceof net.thaumcraft.entity.eldritch.CultistEntity, 1);
        WHITELIST.put(e -> e instanceof Boss, 200);
    }

    /** Mais um da lista (o {@code championWhiteList} que outros mods mandavam). */
    public static void whitelist(Predicate<LivingEntity> who, int bonus) {
        WHITELIST.put(who, bonus);
    }

    private Champions() {
    }

    public static void init() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof Monster mob && !entity.hasAttached(CHAMPION)) roll(mob, level);
        });
        ServerLivingEntityEvents.AFTER_DEATH.register(Champions::drops);
    }

    /** O tipo do campeão, ou {@link #NONE}/{@link #UNSET}. */
    public static int type(LivingEntity entity) {
        return entity.getAttachedOrElse(CHAMPION, UNSET);
    }

    public static Mod mod(LivingEntity entity) {
        int t = type(entity);
        return t >= 0 && t < Mod.values().length ? Mod.values()[t] : null;
    }

    /** O sorteio do {@code entityJoinWorld}. */
    private static void roll(Monster mob, ServerLevel level) {
        int c = level.getRandom().nextInt(100);
        if (level.getDifficulty() == Difficulty.EASY) c += 2;
        if (level.getDifficulty() == Difficulty.HARD) c -= 2;
        if (OuterLands.is(level)) c -= 3;
        var biome = level.getBiome(mob.blockPosition());
        if (biome.is(BiomeTags.IS_NETHER) || biome.is(BiomeTags.IS_END) || biome.is(net.thaumcraft.world.TCBiomes.EERIE)
                || biome.is(net.minecraft.world.level.biome.Biomes.DARK_FOREST)) c -= 2;
        if (OuterLands.dangerous(level, mob.blockPosition())) c -= 10;
        int cc = 0;
        boolean whitelisted = false;
        for (var entry : WHITELIST.entrySet()) {
            if (!entry.getKey().test(mob)) continue;
            whitelisted = true;
            cc = Math.max(cc, entry.getValue() - 1);
        }
        c -= cc;
        AttributeInstance health = mob.getAttribute(Attributes.MAX_HEALTH);
        if (whitelisted && c <= 0 && health != null && health.getBaseValue() >= 10.0) {
            makeChampion(mob, false);
        } else {
            mob.setAttached(CHAMPION, NONE);
        }
    }

    /** O {@code makeChampion}: sorteia o tipo e dá os bônus. */
    public static void makeChampion(Monster mob, boolean persist) {
        int type = mob.getRandom().nextInt(Mod.values().length);
        if (mob instanceof net.minecraft.world.entity.monster.Creeper) type = 0;
        mob.setAttached(CHAMPION, type);
        if (!(mob instanceof Boss boss)) {
            add(mob, Attributes.MAX_HEALTH, CHAMPION_HEALTH);
            add(mob, Attributes.ATTACK_DAMAGE, CHAMPION_DAMAGE);
            mob.heal(25.0f);
            mob.setCustomName(Mod.values()[type].displayName().copy().append(" ").append(mob.getName()));
        } else {
            boss.generateName();
        }
        if (persist) mob.setPersistenceRequired();
        switch (Mod.values()[type]) {
            case BOLD -> add(mob, Attributes.MOVEMENT_SPEED, BOLD_BUFF);
            case MIGHTY -> add(mob, Attributes.ATTACK_DAMAGE, MIGHTY_BUFF);
            case WARDED -> {
                AttributeInstance health = mob.getAttribute(Attributes.MAX_HEALTH);
                int bh = health == null ? 0 : (int) health.getBaseValue() / 2;
                mob.setAbsorptionAmount(mob.getAbsorptionAmount() + bh);
            }
            default -> {
            }
        }
    }

    private static void add(LivingEntity mob, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
                            AttributeModifier modifier) {
        AttributeInstance instance = mob.getAttribute(attribute);
        if (instance == null) return;
        instance.removeModifier(modifier.id());
        instance.addPermanentModifier(modifier);
    }

    // ------------------------------------------------------------------ os efeitos

    /** A cada tique: o morto-vivo se cura, o protegido refaz o escudo. No cliente, as faíscas do tipo. */
    public static void tick(LivingEntity entity) {
        if (!(entity instanceof Monster) || entity.isRemoved()) return;
        Mod mod = mod(entity);
        if (mod == null) return;
        if (entity.level().isClientSide()) {
            ChampionFx.client.show(entity, mod);
            return;
        }
        if (mod.type != 0) return;
        switch (mod) {
            case UNDYING -> {
                if (entity.tickCount % 20 == 0) entity.heal(1.0f);
            }
            case WARDED -> {
                if (entity.hurtTime <= 0 && entity.tickCount % 25 == 0) {
                    AttributeInstance health = entity.getAttribute(Attributes.MAX_HEALTH);
                    int bh = health == null ? 0 : (int) health.getBaseValue() / 2;
                    if (entity.getAbsorptionAmount() < bh) entity.setAbsorptionAmount(entity.getAbsorptionAmount() + 1.0f);
                }
            }
            default -> {
            }
        }
    }

    /**
     * O dano que chega a alguém (o {@code LivingHurtEvent} do {@code EventHandlerRunic}): no monstro campeão (ou
     * eldritch) com escudo, o clarão; o campeão que apanha de alguém vivo reage (espinhos, armadura, infestação); e o
     * golpe de um campeão leva o efeito dele (murchar, distorção, fogo, fome, veneno, vampirismo).
     */
    public static float hurt(LivingEntity victim, DamageSource source, float amount) {
        if (!(victim instanceof Player) && victim instanceof Monster mob && (mod(mob) != null || mob instanceof EldritchMob)) {
            Mod mod = mod(mob);
            if ((mod == Mod.WARDED || mob instanceof EldritchMob) && mob.getAbsorptionAmount() > 0.0f) {
                int target = -1;
                if (source.getEntity() != null) target = source.getEntity().getId();
                if (source.is(DamageTypes.FALL)) target = -2;
                if (source.is(DamageTypes.FALLING_BLOCK)) target = -3;
                RunicShield.flash(mob, target, 32.0);
            } else if (mod != null && mod.type == 2 && source.getDirectEntity() instanceof LivingEntity attacker) {
                amount = effect(mod, mob, attacker, source, amount);
            }
        }
        if (amount > 0.0f && source.getDirectEntity() instanceof Monster attacker) {
            Mod mod = mod(attacker);
            if (mod != null && mod.type == 1) amount = effect(mod, attacker, victim, source, amount);
        }
        return amount;
    }

    /** O {@code performEffect} de cada tipo. */
    private static float effect(Mod mod, LivingEntity mob, LivingEntity target, DamageSource source, float amount) {
        var random = mob.getRandom();
        switch (mod) {
            case SPINE -> {
                if (target != null && !source.is(DamageTypes.THORNS) && mob.level() instanceof ServerLevel server) {
                    target.hurtServer(server, mob.damageSources().thorns(mob), 1 + random.nextInt(3));
                    target.playSound(SoundEvents.THORNS_HIT, 0.5f, 1.0f);
                }
            }
            case ARMOR -> {
                if (!source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR)) amount = amount * 19.0f / 25.0f;
            }
            case GRIM -> {
                if (random.nextFloat() < 0.4f) target.addEffect(new MobEffectInstance(MobEffects.WITHER, 200));
            }
            case WARP -> {
                if (random.nextFloat() < 0.33f && target instanceof Player player) Warp.add(player, 1 + random.nextInt(3), true);
            }
            case FIERY -> {
                if (random.nextFloat() < 0.4f) target.igniteForSeconds(4);
            }
            case SICKLY -> {
                if (random.nextFloat() < 0.4f) target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 500));
            }
            case VENOMOUS -> {
                if (random.nextFloat() < 0.4f) target.addEffect(new MobEffectInstance(MobEffects.POISON, 100));
            }
            case VAMPIRIC -> mob.heal(Math.max(2.0f, amount / 2.0f));
            case INFESTED -> {
                if (random.nextFloat() < 0.4f && mob.level() instanceof ServerLevel server) {
                    var spider = net.thaumcraft.registry.TCEntities.TAINT_SPIDER.create(server, net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
                    if (spider != null) {
                        spider.snapTo(mob.getX(), mob.getY() + mob.getBbHeight() / 2.0f, mob.getZ(), random.nextFloat() * 360.0f, 0.0f);
                        server.addFreshEntity(spider);
                    }
                    mob.playSound(net.thaumcraft.registry.TCSounds.GORE.value(), 0.5f, 1.0f);
                }
            }
            default -> {
            }
        }
        return amount;
    }

    // ------------------------------------------------------------------ o que o campeão deixa

    /**
     * O {@code livingDrops}: o campeão (não o chefe) morto por alguém solta de cinco a sete de experiência e uma sacola de
     * tesouro — comum, incomum ou rara, puxada pela pilhagem.
     */
    private static void drops(LivingEntity entity, DamageSource source) {
        if (!(entity instanceof Monster) || entity instanceof Boss || mod(entity) == null) return;
        if (entity.getLastHurtByPlayer() == null || source.getEntity() instanceof net.fabricmc.fabric.api.entity.FakePlayer) return;
        if (!(entity.level() instanceof ServerLevel level)) return;
        int i = 5 + level.getRandom().nextInt(3);
        ExperienceOrb.award(level, entity.position(), i);
        int looting = 0;
        if (source.getEntity() instanceof LivingEntity killer) {
            var enchantments = level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
            looting = net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
                    enchantments.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.LOOTING), killer.getMainHandItem());
        }
        int lb = Math.min(2, (int) Math.floor((level.getRandom().nextInt(9) + looting) / 5.0f));
        var bag = new ItemStack(lb == 0 ? TCItems.LOOT_BAG : lb == 1 ? TCItems.LOOT_BAG_UNCOMMON : TCItems.LOOT_BAG_RARE);
        level.addFreshEntity(new ItemEntity(level, entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ(), bag));
    }

    /** As faíscas de cada tipo; o desenho mora no cliente. */
    public static final class ChampionFx {
        public static Client client = (entity, mod) -> {
        };

        public interface Client {
            void show(LivingEntity entity, Mod mod);
        }

        private ChampionFx() {
        }
    }

    /** O nome do campeão (para os testes e o chefe). */
    public static Identifier id(Mod mod) {
        return Thaumcraft.id("champion_" + mod.name);
    }
}
