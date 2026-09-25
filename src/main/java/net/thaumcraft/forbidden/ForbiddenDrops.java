package net.thaumcraft.forbidden;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.thaumcraft.item.FocusItem;
import net.thaumcraft.item.FocusUpgradeTable;
import net.thaumcraft.item.WandItem;

/**
 * De onde vêm os fragmentos dos pecados: o {@code FMEventHandler} do Forbidden Magic 0.575.
 *
 * <p>Tudo se passa no Nether, e cada pecado tem o seu jeito de aparecer — a Ira sai de quem morre por uma arma
 * forte, a Soberba dos chefes, a Avareza de quem caça com sorte, a Preguiça de quem morre sozinho, a Inveja do
 * porco-zumbi que carregava um fragmento, e a Gula de quem come no Nether.
 */
public final class ForbiddenDrops {
    private ForbiddenDrops() {
    }

    /** O {@code dimension == -1} do original. */
    public static boolean inTheNether(Level level) {
        return level.dimension() == Level.NETHER;
    }

    /**
     * O {@code onDrops}: o que cai quando alguma coisa morre no Nether.
     *
     * @param killer quem deu o último golpe, se foi gente
     */
    public static void onDeath(LivingEntity dead, DamageSource source) {
        if (!(dead.level() instanceof ServerLevel serverLevel)) return;
        // o machado decepa em qualquer lugar; o resto é só no Nether
        beheading(dead, source);
        if (source.getEntity() instanceof Player quemMatou) ForbiddenEnchantments.onKill(dead, quemMatou);
        // quem mata com o garfo marca um cristal em branco
        if (source.getEntity() instanceof Player comGarfo) MobCrystalItem.imprint(comGarfo, dead);
        if (!inTheNether(serverLevel)) return;
        ServerLevel level = serverLevel;
        var random = level.getRandom();
        Player killer = source.getEntity() instanceof Player player ? player : null;

        // a Preguiça: morreu sozinho, sem gente por perto
        if (killer == null && random.nextInt(30) < 4) {
            drop(dead, new ItemStack(ForbiddenItems.SHARDS.get("sloth"), 1 + random.nextInt(3)));
        }
        if (killer == null) return;

        if (dead instanceof Enemy) {
            // a Ira: a chance cresce com o quanto a arma machuca e com os encantamentos de briga
            int wrath = 2 + wrathOf(killer, dead);
            if (random.nextInt(61) <= wrath) drop(dead, new ItemStack(ForbiddenItems.SHARDS.get("wrath")));

            // a Soberba: os chefes
            if (isBoss(dead)) {
                drop(dead, new ItemStack(ForbiddenItems.SHARDS.get("pride"), 2 + random.nextInt(1 + looting(killer))));
            }

            // a Avareza: quem caça com sorte, e a varinha com foco de tesouro
            int greed = greedOf(killer);
            if (greed > 0 && random.nextInt(20) <= greed) {
                drop(dead, new ItemStack(ForbiddenItems.SHARDS.get("greed")));
            }
        }

        // a Inveja: o porco-zumbi que carregava um fragmento de inveja larga o dele
        if (dead instanceof ZombifiedPiglin
                && dead.getItemBySlot(EquipmentSlot.MAINHAND).is(ForbiddenItems.SHARDS.get("envy"))) {
            drop(dead, new ItemStack(ForbiddenItems.SHARDS.get("envy")));
        }
    }

    /**
     * O Machado do Tomador de Crânios: quem morre por ele pode perder a cabeça. As chances são as do original —
     * o esqueleto uma em vinte e seis mais a pilhagem, o zumbi e o creeper o dobro dela, e a gente uma em onze.
     */
    private static void beheading(LivingEntity dead, DamageSource source) {
        if (!(source.getEntity() instanceof Player killer)) return;
        if (!killer.getMainHandItem().is(ForbiddenItems.SKULLTAKER_AXE)) return;
        var random = dead.level().getRandom();
        int pilhagem = looting(killer);
        ItemStack cabeca = switch (dead) {
            case net.minecraft.world.entity.monster.skeleton.WitherSkeleton ignored ->
                    random.nextInt(26) <= 3 + pilhagem ? new ItemStack(Items.WITHER_SKELETON_SKULL) : ItemStack.EMPTY;
            case net.minecraft.world.entity.monster.skeleton.Skeleton ignored ->
                    random.nextInt(26) <= 3 + pilhagem ? new ItemStack(Items.SKELETON_SKULL) : ItemStack.EMPTY;
            case net.minecraft.world.entity.monster.zombie.Zombie ignored ->
                    random.nextInt(26) <= 2 + 2 * pilhagem ? new ItemStack(Items.ZOMBIE_HEAD) : ItemStack.EMPTY;
            case net.minecraft.world.entity.monster.Creeper ignored ->
                    random.nextInt(26) <= 2 + 2 * pilhagem ? new ItemStack(Items.CREEPER_HEAD) : ItemStack.EMPTY;
            case Player morto -> cabecaDe(morto, random.nextInt(11) <= 1 + pilhagem);
            default -> ItemStack.EMPTY;
        };
        if (!cabeca.isEmpty()) drop(dead, cabeca);
    }

    /** A cabeça de quem joga leva o nome do dono, como no original. */
    private static ItemStack cabecaDe(Player morto, boolean saiu) {
        if (!saiu) return ItemStack.EMPTY;
        ItemStack cabeca = new ItemStack(Items.PLAYER_HEAD);
        cabeca.set(DataComponents.PROFILE, net.minecraft.world.item.component.ResolvableProfile.createResolved(morto.getGameProfile()));
        return cabeca;
    }

    /** O {@code onSpawn}: um porco-zumbi em cada cento e setenta e cinco nasce com um fragmento de inveja na mão. */
    public static void onSpawn(LivingEntity entity, ServerLevel level) {
        if (!inTheNether(level) || !(entity instanceof ZombifiedPiglin piglin)) return;
        if (level.getRandom().nextInt(175) != 1) return;
        piglin.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ForbiddenItems.SHARDS.get("envy")));
    }

    /** O {@code onEat}: comer no Nether faz cair um Fragmento da Gula, duas vezes em dez. */
    public static void onEat(Player player, ItemStack food) {
        // o anel da nutrição rende mais em qualquer lugar
        if (food.get(DataComponents.FOOD) != null) NutritionRingItem.onEat(player);
        if (!(player.level() instanceof ServerLevel level) || !inTheNether(level)) return;
        if (food.is(ForbiddenItems.GLUTTONY_SHARD) || food.get(DataComponents.FOOD) == null) return;
        if (level.getRandom().nextInt(10) >= 2) return;
        player.drop(new ItemStack(ForbiddenItems.GLUTTONY_SHARD), false);
    }

    /** O quanto a arma de quem matou soma à chance da Ira. */
    private static int wrathOf(Player killer, LivingEntity dead) {
        ItemStack held = killer.getMainHandItem();
        if (held.isEmpty()) return 0;
        int wrath = 0;
        {
            var dano = held.get(DataComponents.ATTRIBUTE_MODIFIERS);
            if (dano != null) {
                for (var entry : dano.modifiers()) {
                    if (entry.attribute() == net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) {
                        wrath += (int) entry.modifier().amount() + 4;
                    }
                }
            }
        }
        wrath += enchant(killer, held, net.minecraft.world.item.enchantment.Enchantments.SHARPNESS);
        wrath += enchant(killer, held, net.minecraft.world.item.enchantment.Enchantments.KNOCKBACK);
        if (!dead.fireImmune()) wrath += enchant(killer, held, net.minecraft.world.item.enchantment.Enchantments.FIRE_ASPECT);
        if (dead.getType().builtInRegistryHolder().is(net.minecraft.tags.EntityTypeTags.UNDEAD)) {
            wrath += enchant(killer, held, net.minecraft.world.item.enchantment.Enchantments.SMITE);
        }
        if (dead.getType().builtInRegistryHolder().is(net.minecraft.tags.EntityTypeTags.ARTHROPOD)) {
            wrath += enchant(killer, held, net.minecraft.world.item.enchantment.Enchantments.BANE_OF_ARTHROPODS);
        }
        // a varinha soma a Potência do foco que estiver nela
        if (held.getItem() instanceof WandItem) {
            ItemStack focus = WandItem.focusStack(held);
            if (focus.getItem() instanceof FocusItem) wrath += FocusItem.level(focus, FocusUpgradeTable.POTENCY);
        }
        return wrath;
    }

    /** O quanto de sorte quem matou carrega: a pilhagem e o foco de tesouro da varinha. */
    private static int greedOf(Player killer) {
        ItemStack held = killer.getMainHandItem();
        int greed = looting(killer);
        if (held.getItem() instanceof WandItem) {
            ItemStack focus = WandItem.focusStack(held);
            if (focus.getItem() instanceof FocusItem) greed += FocusItem.level(focus, FocusUpgradeTable.TREASURE);
        }
        return greed;
    }

    private static int looting(Player killer) {
        return enchant(killer, killer.getMainHandItem(), net.minecraft.world.item.enchantment.Enchantments.LOOTING);
    }

    private static int enchant(Player player, ItemStack stack, net.minecraft.resources.ResourceKey<
            net.minecraft.world.item.enchantment.Enchantment> which) {
        if (stack.isEmpty() || !(player.level() instanceof ServerLevel level)) return 0;
        var registry = level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        return registry.get(which).map(holder -> net.minecraft.world.item.enchantment.EnchantmentHelper
                .getItemEnchantmentLevel(holder, stack)).orElse(0);
    }

    /** Os chefes: o {@code IBossDisplayData} de então é quem tem barra de chefe hoje. */
    private static boolean isBoss(LivingEntity entity) {
        return entity instanceof net.minecraft.world.entity.boss.wither.WitherBoss
                || entity instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon
                || entity instanceof net.thaumcraft.event.Champions.Boss;
    }

    private static void drop(LivingEntity dead, ItemStack stack) {
        dead.spawnAtLocation((ServerLevel) dead.level(), stack);
    }

    /** O item que o porco-zumbi pode nascer segurando, para quem quiser conferir. */
    public static ItemStack envyShard() {
        return new ItemStack(ForbiddenItems.SHARDS.get("envy"), 1);
    }

    /** O que o original chamava de lixo: a torta de bolo e companhia não valem fragmento. */
    public static boolean isFood(ItemStack stack) {
        return stack.get(DataComponents.FOOD) != null && !stack.is(Items.CAKE);
    }
}
