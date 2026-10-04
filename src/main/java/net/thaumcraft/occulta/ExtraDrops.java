package net.thaumcraft.occulta;

import java.util.List;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;

/**
 * <b>Deste cai mais uma coisa</b>: o {@code WITCExtraDrops} do Witchery.
 *
 * <p>É o avesso do {@link NoDrops}. Em vez de calar o que um bicho tem no corpo, <b>pendura-lhe uma coisa a
 * mais</b> — uma coisa que não é dele, que ninguém pôs no mundo, e que só existe para ser <b>trazida de
 * volta</b> a quem o soltou.
 *
 * <p>No original é uma lista no NBT do bicho, e quem a lê é o {@code LivingDeathEvent}: morto o bicho, cada
 * coisa pendurada cai no chão um bloco acima dele. É assim que a <b>bruxa do coven</b> prova que o seu bicho
 * de estimação morreu — ela não olha o mundo à procura dele, ela pede <b>o olho</b>.
 *
 * <p>Aqui é um apego com a lista de pilhas, e quem a lê é o {@link OccultaEvents}.
 */
public final class ExtraDrops {
    /** O que está pendurado neste bicho. */
    public static final AttachmentType<List<ItemStack>> PENDURADO =
            AttachmentRegistry.<List<ItemStack>>builder()
                    .initializer(List::of)
                    .persistent(ItemStack.CODEC.listOf())
                    .buildAndRegister(Thaumcraft.id("extra_drops"));

    private ExtraDrops() {
    }

    /** Sem uso fora do porte: obriga a classe a carregar, e com ela o apego. */
    public static void init() {
    }

    /** Pendura mais uma coisa neste bicho. */
    public static void pendura(LivingEntity bicho, ItemStack oquê) {
        if (oquê.isEmpty()) return;
        List<ItemStack> tinha = bicho.getAttachedOrCreate(PENDURADO);
        List<ItemStack> agora = new java.util.ArrayList<>(tinha.size() + 1);
        for (ItemStack cada : tinha) agora.add(cada.copy());
        agora.add(oquê.copy());
        bicho.setAttached(PENDURADO, List.copyOf(agora));
    }

    /**
     * E, morto o bicho, o que estava pendurado cai.
     *
     * <p>Um bloco acima dele, como no original — de modo que cai <b>por cima</b> do que o corpo deu, e quem
     * matou vê a coisa estranha primeiro.
     */
    public static void larga(ServerLevel level, LivingEntity bicho) {
        List<ItemStack> tinha = bicho.getAttached(PENDURADO);
        if (tinha == null || tinha.isEmpty()) return;
        for (ItemStack cada : tinha) {
            if (cada.isEmpty()) continue;
            level.addFreshEntity(new ItemEntity(level, bicho.getX(), bicho.getY() + 1.0, bicho.getZ(),
                    cada.copy()));
        }
        bicho.removeAttached(PENDURADO);
    }
}
