package net.thaumcraft.occulta.familiar;

import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;

import java.util.List;

/**
 * Os três familiares do Witchery, e o que cada um destranca.
 *
 * <p>No original isto é um número guardado no NBT de quem o tem — um para o gato, dois para o sapo, três para a
 * coruja —, e as três <i>maestrias</i> saem dele: o {@code hasActiveCurseMasteryFamiliar} pergunta se é um, o
 * de cozimento se é dois, o de vassoura se é três.
 *
 * <p><b>Cada bicho destranca uma coisa, e é isso que faz valer escolher:</b>
 *
 * <ul>
 *   <li>o <b>gato</b> dobra o escuro de uma maldição — dois minutos viram cinco;</li>
 *   <li>o <b>sapo</b> tira um frasco a mais de cada caldeirão;</li>
 *   <li>a <b>coruja</b> é a mestria da vassoura — e a vassoura <b>não está portada</b>, por isso ela ainda não
 *       destranca nada. Fica porque o bicho é do original e porque, no dia em que a vassoura vier, é só ela
 *       estar aqui.</li>
 * </ul>
 *
 * <p>E cada um traz a sua lista de nomes, que são as do original.
 */
public enum FamiliarKind implements StringRepresentable {
    /** O gato preto: a maestria da maldição. */
    CAT("cat", 1, List.of("Pyewackett", "Salem", "Gobbolino", "Sabbath", "Norris", "Crookshanks",
            "Binx", "Voodoo", "Raven", "Simpkin", "Fishbone", "Kismet")),

    /** O sapo: a maestria do cozimento. */
    TOAD("toad", 2, List.of("Casper", "Wart", "Langston", "Croaker", "Prince Charming",
            "Frog-n-stien", "Randolph", "Evileye", "Churchill", "Santa", "Dillinger", "Spuds")),

    /** E a coruja: a da vassoura, que ainda não tem vassoura. */
    OWL("owl", 3, List.of("Archimedes", "Dumbledornithologist", "Al Travis", "Baltimore", "Cornelius",
            "Hadwig", "Hoot", "Merlin", "Owl Capone", "Pigwidgeon", "Athena", "Albertine"));

    public static final Codec<FamiliarKind> CODEC = StringRepresentable.fromEnum(FamiliarKind::values);

    private final String nome;
    private final int numeroDoOriginal;
    private final List<String> nomes;

    FamiliarKind(String nome, int numeroDoOriginal, List<String> nomes) {
        this.nome = nome;
        this.numeroDoOriginal = numeroDoOriginal;
        this.nomes = nomes;
    }

    @Override
    public String getSerializedName() {
        return this.nome;
    }

    /** O número que o original guarda: um, dois ou três. Serve às provas e ao que se lê do disco antigo. */
    public int numeroDoOriginal() {
        return this.numeroDoOriginal;
    }

    public String sorteiaNome(RandomSource sorte) {
        return this.nomes.get(sorte.nextInt(this.nomes.size()));
    }

    public int quantosNomes() {
        return this.nomes.size();
    }
}
