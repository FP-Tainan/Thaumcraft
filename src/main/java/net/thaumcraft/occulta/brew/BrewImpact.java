package net.thaumcraft.occulta.brew;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Como um cozimento se espalha quando o frasco arrebenta: o {@code ModifiersImpact} do Witchery.
 *
 * <p>São duas contas e quem atirou. O <b>alcance</b> ({@code extent}) alarga o que o frasco apanha; a
 * <b>duração</b> ({@code lifetime}) é para o gás e a poça, que ficam no chão depois.
 *
 * <p>Quem manda no jeito de espalhar é o último ingrediente de espalhamento que caiu no caldeirão: a pólvora
 * espalha de uma vez, a lã de morcego faz nuvem, a losna faz poça e a cabeça de zumbi arma o gatilho.
 */
public class BrewImpact {
    public int extent;
    public int lifetime;

    @Nullable
    private BrewDispersal dispersal;

    /**
     * Se este cozimento <b>só</b> se espalha de uma vez: o {@code onlyInstant} do original.
     *
     * <p>Um efeito que muda o mundo não pode ficar no chão em poça nem no ar em nuvem — ou acontece no
     * estouro, ou não acontece. O ingrediente que o diz é a Nota de Bioma, e é por isso que ela anula a
     * pólvora, a lã de morcego e a losna sem as tirar da panela.
     */
    private boolean onlyInstant;

    /**
     * E em que bioma este cozimento quer transformar o lugar: o metadado da Nota de Bioma que caiu na
     * panela.
     *
     * <p>Ele viaja aqui e não na lista de ingredientes porque a lista guarda <b>coisas</b> e não frascos: o
     * que a nota tem escrito dentro não caberia nela. É o mesmo lugar onde viaja quem atirou.
     */
    @Nullable
    public net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome> bioma;

    @Nullable
    public final LivingEntity thrower;

    public BrewImpact(@Nullable LivingEntity thrower) {
        this.thrower = thrower;
    }

    public void setDispersal(BrewDispersal dispersal) {
        this.dispersal = dispersal;
    }

    /** O {@code setOnlyInstant}. */
    public void setOnlyInstant() {
        this.onlyInstant = true;
    }

    /**
     * O {@code getDispersal}: quem pediu só o estouro leva o estouro, seja o que for que esteja escolhido.
     */
    @Nullable
    public BrewDispersal dispersal() {
        return this.onlyInstant ? new BrewDispersal.Instant() : this.dispersal;
    }
}
