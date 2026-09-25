package net.thaumcraft.naturalis;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.item.FocusItem;
import net.thaumcraft.item.FocusUpgradeTable;
import net.thaumcraft.item.Focuses;
import net.thaumcraft.item.WandItem;
import net.thaumcraft.registry.TCSounds;

/**
 * O Foco do Revenante: o {@code RevenantFocusItem} do Magia Naturalis 0.5.0.
 *
 * <p>Apontado a uma criatura a até trinta e dois blocos, ele levanta um Revenante Feroz do chão, ao lado de quem
 * lança, e o manda em cima dela. Cada posto de Potência dá meio coração de dano a mais ao revenante.
 */
public final class RevenantFocus {
    /** O {@code VIS_COST} do original. */
    public static final AspectList COST = new AspectList()
            .add(Aspects.EARTH, 450).add(Aspects.ENTROPY, 350).add(Aspects.WATER, 200);
    /** A cor do foco, o {@code getFocusColor} do original. */
    public static final int COLOUR = 0x38537E;
    /** O alcance da mira, como no original. */
    private static final double RANGE = 32.0;

    private RevenantFocus() {
    }

    public static void init() {
        Focuses.register("revenant", RevenantFocus::cast);
        // o getPossibleUpgradesByRank do original: Potência e Frugal em qualquer posto
        var postos = java.util.List.of(FocusUpgradeTable.POTENCY, FocusUpgradeTable.FRUGAL);
        net.thaumcraft.api.FocusUpgrades.ranks("revenant",
                java.util.List.of(postos, postos, postos, postos, postos));
    }

    private static boolean cast(Level level, Player player, ItemStack wand, FocusItem focus) {
        player.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
        if (!(level instanceof ServerLevel server)) return true;
        // o original não levanta revenante contra outro revenante
        Entity mirado = Focuses.pointedEntity(level, player, RANGE, RevenantEntity.class);
        if (!(mirado instanceof LivingEntity alvo)) return false;
        ItemStack stack = WandItem.focusStack(wand);
        if (!WandItem.consumeFocus(wand, COST, true, player)) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), TCSounds.WAND_FAIL.value(),
                    SoundSource.PLAYERS, 0.2f, 0.8f + level.getRandom().nextFloat() * 0.1f);
            return false;
        }
        RevenantEntity revenante = NaturalisEntities.REVENANT.create(server, EntitySpawnReason.MOB_SUMMONED);
        if (revenante == null) return false;
        // ele nasce ao lado de quem lança, um passo à frente, como no original
        double frente = 0.5;
        var olhar = player.getViewVector(1.0f);
        double x = player.getX() - Math.cos(player.getYRot() / 180.0f * Math.PI) * 0.16f + olhar.x * frente;
        double z = player.getZ() - Math.sin(player.getYRot() / 180.0f * Math.PI) * 0.16f + olhar.z * frente;
        revenante.snapTo(x, player.getBoundingBox().minY + 0.1, z, player.getYRot(), 0.0f);
        revenante.owner(player.getUUID());
        // a Potência do foco engrossa o braço dele
        int potency = FocusItem.level(stack, FocusUpgradeTable.POTENCY);
        var dano = revenante.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        if (dano != null) dano.setBaseValue(2.0 + 0.5 * potency);
        revenante.setTarget(alvo);
        if (!server.addFreshEntity(revenante)) return false;
        server.levelEvent(null, 1026, revenante.blockPosition(), 0);
        level.playSound(null, revenante.getX(), revenante.getY(), revenante.getZ(), TCSounds.ICE.value(),
                SoundSource.PLAYERS, 0.2f, 0.95f + level.getRandom().nextFloat() * 0.1f);
        return true;
    }

    /** Se a criatura da mira pode ser caçada — o {@code getPointedEntity} do original, sem o próprio revenante. */
    public static boolean huntable(Entity entity) {
        return entity instanceof LivingEntity && !(entity instanceof RevenantEntity);
    }
}
