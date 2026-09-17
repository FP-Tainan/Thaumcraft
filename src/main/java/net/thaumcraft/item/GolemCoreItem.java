package net.thaumcraft.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Um núcleo de golem: o disco que diz ao golem qual é o serviço dele.
 *
 * <p>No original são doze, e o golem só faz o que o núcleo encaixado nele manda. Aqui os doze existem
 * como item, com os nomes do original, mas só dois já sabem trabalhar — juntar e colher. Os outros
 * esperam as peças que faltam, e isso está anotado em {@code docs/PORTE.md}.
 *
 * @param core o serviço, pelo nome que o original dá a ele
 */
public class GolemCoreItem extends Item {
    private final String core;

    public GolemCoreItem(Properties properties, String core) {
        super(properties);
        this.core = core;
    }

    public String core() {
        return this.core;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.thaumcraft.golem_core_" + this.core);
    }
}
