package net.thaumcraft.forbidden;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
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

import java.util.List;

/**
 * O foco do Forbidden Magic 0.575: o {@code ItemFocusBlink}.
 *
 * <p>Ele leva quem o usa para onde a varinha aponta, a até cento e vinte e oito blocos — e, com as melhorias
 * que só ele tem, deixa fogo do inferno no lugar aonde chega ou troca de lugar com os bichos que estiverem lá.
 */
public final class ForbiddenFoci {
    /** O fogo do inferno: quem estiver perto de onde se chega queima. */
    public static final FocusUpgradeTable.Type HELLFIRE = FocusUpgrades.register((short) 80, "hellfire", "hellfire",
            new AspectList().add(ForbiddenAspects.ASPECTS.get("infernus"), 1));

    /** O pandemônio: os monstros que estiverem lá vão parar de onde se saiu. */
    public static final FocusUpgradeTable.Type PANDEMONIUM = FocusUpgrades.register((short) 81, "pandemonium", "pandemonium",
            new AspectList().add(Aspects.DARKNESS, 1));

    /** Os três custos do original: o comum, o do fogo e o da troca. */
    public static final AspectList COST_BLINK = new AspectList().add(Aspects.ENTROPY, 300);
    public static final AspectList COST_HELLFIRE = new AspectList().add(Aspects.ENTROPY, 300).add(Aspects.FIRE, 100);
    public static final AspectList COST_PANDEMONIUM = new AspectList().add(Aspects.ENTROPY, 300).add(Aspects.ORDER, 100);

    /** O alcance do {@code BlockUtils.getTargetBlock} deste foco: cento e vinte e oito blocos. */
    private static final double REACH = 128.0;

    private ForbiddenFoci() {
    }

    public static void init() {
        List<FocusUpgradeTable.Type> so = List.of(FocusUpgradeTable.FRUGAL);
        FocusUpgrades.ranks("blink", List.of(
                so,
                List.of(HELLFIRE, PANDEMONIUM, FocusUpgradeTable.FRUGAL),
                List.of(FocusUpgradeTable.POTENCY, FocusUpgradeTable.FRUGAL, FocusUpgradeTable.ENLARGE),
                List.of(FocusUpgradeTable.POTENCY, FocusUpgradeTable.FRUGAL, FocusUpgradeTable.ENLARGE),
                List.of(FocusUpgradeTable.POTENCY, FocusUpgradeTable.FRUGAL, FocusUpgradeTable.ENLARGE)));
        Focuses.register("blink", ForbiddenFoci::blink);
    }

    /** O custo muda com a melhoria posta, como no {@code getVisCost} do original. */
    public static AspectList cost(ItemStack focus) {
        if (FocusItem.level(focus, HELLFIRE) > 0) return COST_HELLFIRE;
        if (FocusItem.level(focus, PANDEMONIUM) > 0) return COST_PANDEMONIUM;
        return COST_BLINK;
    }

    /** O {@code onFocusRightClick}: o salto até onde a varinha aponta. */
    private static boolean blink(Level level, Player player, ItemStack wand, FocusItem focus) {
        Vec3 olhos = player.getEyePosition();
        Vec3 longe = olhos.add(player.getViewVector(1.0f).scale(REACH));
        HitResult mira = level.clip(new ClipContext(olhos, longe, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (!(mira instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) return false;
        ItemStack focusStack = WandItem.focusStack(wand);
        if (!WandItem.consumeFocus(wand, cost(focusStack), true, player)) return false;

        // o lugar aonde se chega: a face em que a mira bateu, afastada meio bloco como no original
        Vec3 onde = hit.getLocation();
        double x = onde.x, y = onde.y, z = onde.z;
        switch (hit.getDirection()) {
            case DOWN -> y -= 2.0;
            case NORTH -> z -= 0.5;
            case SOUTH -> z += 0.5;
            case WEST -> x -= 0.5;
            case EAST -> x += 0.5;
            default -> {
            }
        }

        double alcance = 3.0 + 1.5 * FocusItem.level(focusStack, FocusUpgradeTable.ENLARGE);
        Vec3 saida = player.position();
        if (level instanceof ServerLevel server) {
            AABB volta = new AABB(x - alcance, y - alcance, z - alcance, x + alcance, y + alcance, z + alcance);
            if (FocusItem.level(focusStack, HELLFIRE) > 0) {
                int potencia = FocusItem.level(focusStack, FocusUpgradeTable.POTENCY);
                for (LivingEntity bicho : level.getEntitiesOfClass(LivingEntity.class, volta, e -> e != player)) {
                    bicho.hurtServer(server, player.damageSources().inFire(), 3 + 3 * potencia);
                    bicho.igniteForSeconds(3 + 3 * potencia);
                }
            } else if (FocusItem.level(focusStack, PANDEMONIUM) > 0) {
                for (LivingEntity bicho : level.getEntitiesOfClass(LivingEntity.class, volta, e -> e instanceof Enemy)) {
                    bicho.teleportTo(saida.x, saida.y, saida.z);
                }
            }
        }

        level.levelEvent(null, 1032, BlockPos.containing(player.position()), 0);
        player.teleportTo(x, y, z);
        level.levelEvent(null, 1032, BlockPos.containing(player.position()), 0);
        player.resetFallDistance();
        return true;
    }
}
