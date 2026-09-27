package net.thaumcraft.occulta;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * O Coração de Creeper: o {@code itemCreeperHeart} do Witchery.
 *
 * <p>Cai do creeper morto por alguém. Come-se — e <b>estoura</b>: no original, três de estouro com fogo se quem
 * manda o servidor deixar, e um sem fogo se não deixar. Fica também um instante de resistência ao fogo, que é o
 * que o {@code Drinkable} dele declara — e que não serve de grande consolo.
 *
 * <p><b>Escolha declarada:</b> sem arquivo de ajustes, este porte usa o <b>manso</b> dos dois: um de estouro e
 * sem fogo. É o que o original faz para quem desligou o estouro grande, e é o que não arruína a casa de quem o
 * comeu por curiosidade.
 */
public class CreeperHeartItem extends Item {
    /** O tamanho do estouro. */
    public static final float BLAST = 1.0f;

    public CreeperHeartItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack coisa, Level level, LivingEntity quem) {
        ItemStack sobra = super.finishUsingItem(coisa, level, quem);
        if (level instanceof ServerLevel server) {
            server.explode(quem, quem.getX(), quem.getY(), quem.getZ(), BLAST,
                    Level.ExplosionInteraction.NONE);
        }
        return sobra;
    }
}
