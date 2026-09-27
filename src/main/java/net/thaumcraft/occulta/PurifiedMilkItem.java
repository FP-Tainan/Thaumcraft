package net.thaumcraft.occulta;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * O Leite Purificado: o {@code itemPurifiedMilk} do Witchery.
 *
 * <p>Leite passado pelo Odor de Pureza, em pote de barro. Bebendo-o, <b>uma vez em duas</b> ele tira um efeito
 * qualquer de quem o bebeu — um só, e o que der na telha. É leite de bruxa: não limpa, escolhe.
 */
public class PurifiedMilkItem extends Item {
    /** De quantas em quantas vezes ele pega: uma em duas, como no original. */
    public static final int CHANCE = 2;

    public PurifiedMilkItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack coisa, Level level, LivingEntity quem) {
        ItemStack sobra = super.finishUsingItem(coisa, level, quem);
        if (level.isClientSide() || level.getRandom().nextInt(CHANCE) != 0) return sobra;

        List<MobEffectInstance> tem = List.copyOf(quem.getActiveEffects());
        if (tem.isEmpty()) return sobra;
        quem.removeEffect(tem.get(level.getRandom().nextInt(tem.size())).getEffect());
        return sobra;
    }
}
