package net.thaumcraft.forbidden;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.baubles.Baubles;
import net.thaumcraft.item.VisAmuletItem;
import net.thaumcraft.registry.TCComponents;

import java.util.List;

/**
 * A Coleira: o {@code ItemSubCollar} do Forbidden Magic 0.575.
 *
 * <p>Um amuleto de vis que guarda tanto quanto o grande, e que converte a dor em vis: cada ponto de dano que
 * quem a veste leva vale três centésimos num primário sorteado — seis, se quem bate estiver de chicote na mão.
 */
public class CollarItem extends VisAmuletItem {
    public CollarItem(Properties properties) {
        super(true, properties);
    }

    @Override
    public boolean canEquip(ItemStack stack, net.minecraft.world.entity.LivingEntity wearer) {
        return true;
    }

    /** O {@code onFeelPain}: a dor de quem a veste vira vis. */
    public static void onHurt(Player wearer, float damage, net.minecraft.world.damagesource.DamageSource source) {
        if (damage <= 0.0f) return;
        ItemStack collar = Baubles.get(wearer, Baubles.AMULET);
        if (!(collar.getItem() instanceof CollarItem coleira)) return;

        int doses = 3 * (int) damage;
        boolean chicote = source.getEntity() instanceof Player quemBate
                && quemBate.getMainHandItem().is(ForbiddenItems.RIDING_CROP);
        if (chicote) doses += 3;
        if (doses <= 0) return;

        List<Aspect> primals = Aspects.primals();
        AspectList guardado = new AspectList(collar.getOrDefault(TCComponents.WAND_VIS, new AspectList()));
        int maximo = coleira.maxVis(collar);
        for (int dose = 0; dose < doses; dose++) {
            Aspect primal = primals.get(wearer.getRandom().nextInt(primals.size()));
            if (guardado.getAmount(primal) < maximo) guardado.add(primal, 1);
        }
        collar.set(TCComponents.WAND_VIS, guardado);
    }
}
