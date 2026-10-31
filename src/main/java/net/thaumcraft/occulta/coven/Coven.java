package net.thaumcraft.occulta.coven;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.Thaumcraft;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * As bruxas que entraram no coven de alguém: o {@code WITCCoven} do Witchery.
 *
 * <p>Um coven não se compra nem se fabrica — <b>ganha-se</b>. Fala-se com uma bruxa, ela pede uma coisa, e
 * quando se traz o que ela pediu ela entra. <b>Seis é o teto</b>, como no original.
 *
 * <p>E ele <b>pesa nos ritos</b>: há ritos do ofício que fazem mais com mais bruxas em volta. Mas isso só
 * conta <b>com a Pedra do Vidente na mão</b> — sem ela o coven existe e não comparece, e o rito corre no
 * mínimo. É o original, e é o que dá à pedra a razão de existir.
 *
 * <h2>O que se guarda, e por quê</h2>
 *
 * <p>Uma bruxa que entra no coven <b>sai do mundo</b>: ela some num rodopio e passa a ser um nome na lista
 * de quem a recrutou. Por isso a lista não guarda uma referência — guarda o <b>retrato</b> dela, que no
 * original são três coisas: o <b>nome</b>, a <b>cara</b> e o <b>pedido</b> que ela fazia. Com esses três
 * ela se refaz em qualquer lugar, e é assim que um coven recrutado na aldeia aparece num círculo a mil
 * blocos dali.
 *
 * <p>E a identidade dela, daí em diante, é o <b>nome</b> — como no original. A bruxa que se chama outra vez
 * é um bicho novo, com outro número; o que diz que é a mesma é chamar-se igual.
 */
public record Coven(List<Membro> bruxas) {
    /** O teto do original. */
    public static final int MAX = 6;

    public static final Coven VAZIO = new Coven(List.of());

    /**
     * O retrato de uma bruxa do coven: o {@code writeToPlayerNBT} do original.
     *
     * <p>O número dela fica guardado por companhia — serve para saber quem foi recrutada —, mas <b>não é
     * por ele que ela se reconhece</b>: uma bruxa chamada outra vez nasce com outro número.
     */
    public record Membro(UUID id, String nome, int cara, int pedido) {
        public static final Codec<Membro> CODEC = RecordCodecBuilder.create(campo -> campo.group(
                UUIDUtil.CODEC.fieldOf("id").forGetter(Membro::id),
                Codec.STRING.fieldOf("nome").forGetter(Membro::nome),
                Codec.INT.fieldOf("cara").forGetter(Membro::cara),
                Codec.INT.fieldOf("pedido").forGetter(Membro::pedido)
        ).apply(campo, Membro::new));
    }

    /**
     * O codec de um membro que ainda <b>lê a lista velha</b>.
     *
     * <p>Até a fatia da Pedra do Vidente a lista era só de números, porque não havia quem chamasse as
     * bruxas e o retrato delas não servia para nada. Um mundo salvo de antes tem a lista velha, e perdê-la
     * seria perder o coven de quem o jogou — de modo que um número solto ainda se lê, e vira um retrato
     * sem nome.
     *
     * <p>Uma bruxa assim <b>não se consegue chamar</b> (não tem nome por que a refazer), mas <b>conta</b>
     * para o tamanho do coven, que é o que ela já fazia.
     */
    private static final Codec<Membro> MEMBRO = Codec.either(Membro.CODEC, UUIDUtil.CODEC)
            .xmap(qual -> qual.map(novo -> novo, velho -> new Membro(velho, "", 0, -1)),
                    membro -> membro.nome().isEmpty()
                            ? com.mojang.datafixers.util.Either.right(membro.id())
                            : com.mojang.datafixers.util.Either.left(membro));

    public static final Codec<Coven> CODEC = MEMBRO.listOf()
            .xmap(Coven::new, Coven::bruxas).fieldOf("bruxas").codec();

    public static final AttachmentType<Coven> DATA = AttachmentRegistry.<Coven>builder()
            .initializer(() -> VAZIO)
            .persistent(CODEC)
            .copyOnDeath()
            .buildAndRegister(Thaumcraft.id("coven"));

    /** Sem uso fora do porte: obriga a classe a carregar, e com ela o registro do apego. */
    public static void init() {
    }

    public static Coven of(Player quem) {
        return quem.getAttachedOrCreate(DATA);
    }

    public static int tamanho(Player quem) {
        return of(quem).bruxas().size();
    }

    public static boolean cheio(Player quem) {
        return tamanho(quem) >= MAX;
    }

    /** Se aquela bruxa, pelo número dela, já está no coven. */
    public static boolean tem(Player quem, UUID bruxa) {
        return of(quem).bruxas().stream().anyMatch(membro -> membro.id().equals(bruxa));
    }

    /**
     * E se já há uma com aquele nome: é por isto que o original pergunta.
     *
     * <p>Chamada e morta, uma bruxa volta a ser um bicho novo com outro número — de modo que perguntar
     * pelo número deixaria a mesma bruxa entrar duas vezes.
     */
    public static boolean temNome(Player quem, String nome) {
        if (nome.isEmpty()) return false;
        return of(quem).bruxas().stream().anyMatch(membro -> membro.nome().equalsIgnoreCase(nome));
    }

    /**
     * Põe mais uma no coven.
     *
     * @return falso se já estava cheio ou se aquela bruxa já estava ali — que é quando o original a faz
     *         sentir-se enganada e virar contra quem falou com ela
     */
    public static boolean junta(Player quem, Membro bruxa) {
        Coven agora = of(quem);
        if (agora.bruxas().size() >= MAX) return false;
        if (tem(quem, bruxa.id()) || temNome(quem, bruxa.nome())) return false;
        List<Membro> novas = new ArrayList<>(agora.bruxas());
        novas.add(bruxa);
        quem.setAttached(DATA, new Coven(List.copyOf(novas)));
        return true;
    }

    /**
     * E tira-a: <b>morrer sai do coven</b>.
     *
     * <p>No original é o {@code onDeath} da bruxa que procura o nome dela na lista de quem a tinha e o
     * apaga. Um coven é de quem está vivo.
     */
    public static boolean sai(Player quem, String nome) {
        Coven agora = of(quem);
        List<Membro> ficam = agora.bruxas().stream()
                .filter(membro -> !membro.nome().equalsIgnoreCase(nome)).toList();
        if (ficam.size() == agora.bruxas().size()) return false;
        quem.setAttached(DATA, new Coven(ficam));
        return true;
    }
}
