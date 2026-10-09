package net.thaumcraft.occulta;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * A <b>Bússola de Gente</b>: o {@code ItemEntityLocator} do Witchery.
 *
 * <p>Uma bússola de verdade, com <b>trinta e três caras</b>: uma para «não aponta para nada» e trinta e
 * duas para os trinta e dois rumos. Ela lê um <b>vínculo</b> preso nela e aponta para quem o vínculo pega
 * — e é a única coisa do ofício que <b>segue uma pessoa</b> em vez de lhe fazer mal.
 *
 * <p>Estando quem se procura <b>noutra dimensão</b>, ela <b>gira ao acaso</b>. É o jeito honesto de o
 * original dizer «não sei»: a agulha não para, e quem olha percebe.
 *
 * <p>Ela refaz-se na bancada: uma bússola destas mais um <b>vínculo</b> dá outra, apontada para outra
 * pessoa. É o único jeito de a mudar de alvo.
 *
 * <h2>Desvios declarados</h2>
 *
 * <p><b>A conta corre no servidor.</b> No original ela corre no cliente, que escreve o dano do item
 * sozinho — hoje o desenho de um item vem de um componente, e um componente escrito só no cliente volta a
 * ser o que o servidor diz na batida seguinte. A conta é a mesma, e vale por sincronizar: uma bússola na
 * mão de outra pessoa aponta certo.
 *
 * <p><b>A cara sai pelo {@code custom_model_data}</b>, como as dez do Talismã de Círculo, porque é a
 * propriedade que o jogo de hoje tem para isto.
 */
public class EntityLocatorItem extends Item {
    /** Trinta e três caras: a de «nada» e trinta e duas de rumo. */
    public static final int CARAS = 33;

    /** De dez em dez batidas do relógio do mundo, que é o ritmo do original. */
    public static final int DE_DEZ_EM_DEZ = 10;

    public EntityLocatorItem(Properties properties) {
        super(properties);
    }

    /** Qual cara este item está mostrando. */
    public static int cara(ItemStack bússola) {
        CustomModelData qual = bússola.get(DataComponents.CUSTOM_MODEL_DATA);
        if (qual == null || qual.floats().isEmpty()) return 0;
        return (int) (float) qual.floats().getFirst();
    }

    /** Escreve a cara, se ela mudou. */
    private static void cara(ItemStack bússola, int qual) {
        if (cara(bússola) == qual) return;
        bússola.set(DataComponents.CUSTOM_MODEL_DATA,
                new CustomModelData(List.of((float) qual), List.of(), List.of(), List.of()));
    }

    /**
     * <b>A conta da agulha</b>, que é a de uma bússola.
     *
     * <p>{@code -((guinada - 90) × π/180 - atan2(dz, dx))}, partida em trinta e duas fatias — e o que sai
     * é a cara, de um a trinta e dois. Sem vínculo, zero; noutra dimensão, um rumo ao acaso.
     */
    @Override
    public void inventoryTick(ItemStack bússola, ServerLevel level, Entity quem, @Nullable
                              net.minecraft.world.entity.EquipmentSlot casa) {
        if (level.getGameTime() % DE_DEZ_EM_DEZ != 2L) return;
        conta(bússola, level, quem);
    }

    /** A conta, feita fora do relógio para se poder pedir. */
    public static void conta(ItemStack bússola, ServerLevel level, Entity quem) {
        var preso = TaglockItem.bound(bússola);
        if (preso == null) {
            cara(bússola, 0);
            return;
        }

        LivingEntity alvo = acha(level, preso.owner());
        double rumo;
        if (alvo == null) {
            rumo = Math.random() * Math.PI * 2.0;
        } else {
            double dx = alvo.getX() - quem.getX();
            double dz = alvo.getZ() - quem.getZ();
            double guinada = quem.getYRot() % 360.0;
            rumo = -((guinada - 90.0) * Math.PI / 180.0 - Math.atan2(dz, dx));
        }

        int fatias = CARAS - 1;
        int qual = (int) ((rumo / (Math.PI * 2) + 1.0) * fatias) % fatias;
        while (qual < 0) qual = (qual + fatias) % fatias;
        cara(bússola, qual + 1);
    }

    /**
     * Quem o vínculo pega, <b>nesta dimensão</b>.
     *
     * <p>Procura-se só aqui de propósito: quem estiver noutro lugar não se acha, e é por isso que a agulha
     * gira. É o {@code dimension != dimension} do original, escrito do outro lado.
     */
    private static @Nullable LivingEntity acha(ServerLevel level, java.util.UUID quem) {
        Entity achado = level.getEntity(quem);
        return achado instanceof LivingEntity vivo ? vivo : null;
    }

    @Override
    public void appendHoverText(ItemStack bússola, TooltipContext contexto, TooltipDisplay mostra,
                                Consumer<Component> linha, net.minecraft.world.item.TooltipFlag bandeira) {
        var preso = TaglockItem.bound(bússola);
        if (preso == null) return;
        linha.accept(Component.translatable("tc.taglock.bound", preso.name())
                .withStyle(ChatFormatting.GRAY));
    }
}
