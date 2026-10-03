package net.thaumcraft.occulta;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.Thaumcraft;
import org.jetbrains.annotations.Nullable;

/**
 * <b>Deste não cai nada</b>: o {@code EntityUtil.setNoDrops} do Witchery.
 *
 * <p>É a marca que o ofício põe em tudo o que ele <b>faz nascer</b> — os mortos que o Cozimento da Ressurreição
 * levanta, o que o Espírito manifesta, o que uma maldição chama. O bicho é um bicho de verdade, mas <b>o que
 * ele tem no corpo não veio do mundo</b>, e por isso não volta para ele: mata-lo não dá carne, não dá osso, não
 * dá flecha.
 *
 * <p>Sem isto, qualquer feitiço que levante mortos é uma fábrica: um frasco, uma dúzia de zumbis, uma dúzia de
 * carnes podres, outra vez. O original marca cada um deles, e é só por isso que levantar mortos é um
 * <b>exército</b> e não um ofício de ferreiro.
 *
 * <p>No original a marca é um {@code boolean} no NBT do bicho e quem a lê é o {@code LivingDropsEvent}. Aqui é
 * um apego, e quem a lê é o {@link net.thaumcraft.mixin.LivingEntityNoDropsMixin}.
 */
public final class NoDrops {
    /** A marca. Não guarda nada: só o fato de estar lá. */
    public static final AttachmentType<Unit> MARCA = AttachmentRegistry.<Unit>builder()
            .initializer(() -> null)
            .persistent(Unit.CODEC)
            .buildAndRegister(Thaumcraft.id("no_drops"));

    private NoDrops() {
    }

    /** Sem uso fora do porte: obriga a classe a carregar, e com ela o apego. */
    public static void init() {
    }

    /** Põe a marca. */
    public static void marca(@Nullable Entity bicho) {
        if (bicho == null) return;
        bicho.setAttached(MARCA, Unit.INSTANCE);
    }

    /**
     * Se deste não cai nada.
     *
     * <p><b>Nunca de gente</b>, como no original: o {@code isNoDrops} pergunta {@code !(entity instanceof
     * EntityPlayer)} antes de olhar o NBT, e é de propósito — o que um jogador tem no corpo é dele, e nenhum
     * feitiço decide que ele morre com as mãos vazias.
     */
    public static boolean marcado(@Nullable Entity bicho) {
        if (bicho == null || bicho instanceof Player) return false;
        return bicho.hasAttached(MARCA);
    }
}
