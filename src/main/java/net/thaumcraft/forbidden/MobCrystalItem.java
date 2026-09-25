package net.thaumcraft.forbidden;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.thaumcraft.registry.TCComponents;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * O Cristal de Bicho: o {@code ItemMobCrystal} do Forbidden Magic 0.575.
 *
 * <p>Em branco ele não vale nada; marcado, ele diz que bicho a Gaiola da Ira vai fazer. A marca se pega
 * matando o bicho com o Garfo do Diabolista, com o cristal em branco na mochila.
 */
public class MobCrystalItem extends Item {
    public MobCrystalItem(Properties properties) {
        super(properties);
    }

    /** Que bicho este cristal guarda, se algum. */
    public static @Nullable Identifier mob(ItemStack stack) {
        String guardado = stack.get(TCComponents.CRYSTAL_MOB);
        return guardado == null ? null : Identifier.parse(guardado);
    }

    /** Um cristal já marcado com aquele bicho. */
    public static ItemStack of(Identifier mob) {
        ItemStack stack = new ItemStack(ForbiddenItems.MOB_CRYSTAL);
        stack.set(TCComponents.CRYSTAL_MOB, mob.toString());
        return stack;
    }

    /**
     * O {@code imprintCrystal}: quem mata com o garfo marca o primeiro cristal em branco que estiver com ele.
     *
     * @return se algum cristal se marcou
     */
    public static boolean imprint(Player killer, LivingEntity dead) {
        if (!killer.getMainHandItem().is(ForbiddenItems.DIABOLIST_FORK)) return false;
        if (!WrathMobs.known(dead.getType())) return false;
        Identifier bicho = BuiltInRegistries.ENTITY_TYPE.getKey(dead.getType());
        var mochila = killer.getInventory();
        for (int casa = 0; casa < mochila.getContainerSize(); casa++) {
            ItemStack stack = mochila.getItem(casa);
            if (!(stack.getItem() instanceof MobCrystalItem) || mob(stack) != null) continue;
            ItemStack marcado = of(bicho);
            if (stack.getCount() == 1) {
                mochila.setItem(casa, marcado);
            } else {
                stack.shrink(1);
                if (!mochila.add(marcado)) killer.drop(marcado, false);
            }
            return true;
        }
        return false;
    }

    /** A dica diz que bicho está lá dentro. */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        Identifier bicho = mob(stack);
        if (bicho == null) {
            lines.accept(Component.translatable("hint.thaumcraft.empty")
                    .withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
            return;
        }
        BuiltInRegistries.ENTITY_TYPE.getOptional(bicho)
                .ifPresent(type -> lines.accept(Component.translatable(type.getDescriptionId())));
    }

    @Override
    public Component getName(ItemStack stack) {
        // o cristal marcado brilha um pouco mais no nome, como o original faz com a raridade
        return super.getName(stack);
    }

    static {
        DataComponents.CUSTOM_NAME.getClass();
    }
}
