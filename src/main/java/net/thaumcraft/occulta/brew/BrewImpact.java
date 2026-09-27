package net.thaumcraft.occulta.brew;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Como um cozimento se espalha quando o frasco arrebenta: o {@code ModifiersImpact} do Witchery.
 *
 * <p>São duas contas e quem atirou. O <b>alcance</b> ({@code extent}) alarga o que o frasco apanha; a
 * <b>duração</b> ({@code lifetime}) é para o gás e o líquido, que ficam no chão depois — e ainda não chegaram.
 *
 * <p>Quem manda no jeito de espalhar é o último ingrediente de espalhamento que caiu no caldeirão: a pólvora
 * espalha de uma vez, e é a única desta fatia.
 */
public class BrewImpact {
    public int extent;
    public int lifetime;

    @Nullable
    private BrewDispersal dispersal;

    @Nullable
    public final LivingEntity thrower;

    public BrewImpact(@Nullable LivingEntity thrower) {
        this.thrower = thrower;
    }

    public void setDispersal(BrewDispersal dispersal) {
        this.dispersal = dispersal;
    }

    @Nullable
    public BrewDispersal dispersal() {
        return this.dispersal;
    }
}
