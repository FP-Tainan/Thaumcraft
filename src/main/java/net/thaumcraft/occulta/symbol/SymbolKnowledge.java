package net.thaumcraft.occulta.symbol;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.Thaumcraft;

import java.util.List;
import java.util.Set;

/**
 * O que uma pessoa <b>sabe</b> dos símbolos: o {@code WITCSpellBook} do Witchery.
 *
 * <p>Quase todos os trinta e um símbolos se desenham assim que se tem uma infusão. <b>Quatro não.</b> O
 * Carnosa Diem, o Morsmordre, o Ignianima e o Tormentum pedem uma <b>chave</b>, e a chave não está em
 * livro nenhum: ela vem de um <b>gole</b>.
 *
 * <p>São os <b>Cozimentos de Alma</b> — da Fome, do Medo, da Angústia e do Tormento —, e bebe-se um por
 * feitiço. Depois disso fica sabido para sempre, e morrer não o tira.
 *
 * <h2>O que isto não é</h2>
 *
 * <p>Isto <b>não</b> é o que torna uma maldição <i>imperdoável</i>. O que torna é o contrário: o Avada
 * Kedavra, o Crucio e o Imperio <b>não têm chave nenhuma</b> — não há nada que se beba para os aprender,
 * e é por isso que eles só se lançam com a Infusão Infernal. Quem tem chave, aprende-se; quem não tem,
 * vende a alma.
 */
public final class SymbolKnowledge {
    /** O que uma pessoa já bebeu, pelas chaves dos símbolos. */
    public static final AttachmentType<List<String>> DATA =
            AttachmentRegistry.<List<String>>builder()
                    .initializer(List::of)
                    .persistent(Codec.STRING.listOf())
                    .copyOnDeath()
                    .buildAndRegister(Thaumcraft.id("symbol_knowledge"));

    private SymbolKnowledge() {
    }

    /** Se aquela pessoa sabe aquela chave. No criativo, sabe-se tudo. */
    public static boolean knows(Player quem, String chave) {
        if (quem.getAbilities().instabuild) return true;
        return quem.getAttachedOrCreate(DATA).contains(chave);
    }

    /** <b>Aprende.</b> É o que o gole faz, e não se desaprende. */
    public static void learn(Player quem, String chave) {
        var sabia = quem.getAttachedOrCreate(DATA);
        if (sabia.contains(chave)) return;
        var sabe = new java.util.ArrayList<>(sabia);
        sabe.add(chave);
        quem.setAttached(DATA, List.copyOf(sabe));
    }

    /** Tudo o que ela sabe, para o livro e para as provas. */
    public static Set<String> all(Player quem) {
        return Set.copyOf(quem.getAttachedOrCreate(DATA));
    }
}
