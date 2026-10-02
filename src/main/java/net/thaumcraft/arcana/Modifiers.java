package net.thaumcraft.arcana;

import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Os Modificadores: os {@code am2.spell.modifiers} do Ars Magica 2.
 *
 * <p>Nenhum deles faz nada sozinho: cada um mexe num número dos outros, e cobra por isso. O que eles cobram é
 * <b>multiplicativo e por vez</b> — pôr o mesmo modificador três vezes triplica o efeito e mais do que triplica
 * a conta. É o que faz de um feitiço de arcanista uma escolha e não uma lista de compras.
 *
 * <p>Todos os números são os do original.
 */
public final class Modifiers {
    private Modifiers() {
    }

    /** Um modificador simples: mexe num número só, por um valor fixo. */
    private record Simple(String name, SpellModifierKind kind, float value, float cost)
            implements SpellPart.Modifier {
        @Override
        public Set<SpellModifierKind> modifies() {
            return Set.of(this.kind);
        }

        @Override
        public float value(SpellModifierKind qual) {
            return this.value;
        }

        @Override
        public float manaMultiplier(int quantas) {
            return this.cost * quantas;
        }
    }

    /**
     * Um modificador que <b>não cobra nada</b>, por muitas vezes que se repita.
     *
     * <p>O original o escreve devolvendo {@code 1.0F} <i>sem</i> multiplicar pela quantidade, ao contrário de
     * todos os outros. Não é descuido: são os dois modificadores que mudam <i>como</i> o feitiço se comporta e
     * não <i>quanto</i> ele faz, e o original quis que fossem de graça.
     */
    private record Grátis(String name, SpellModifierKind kind, float value)
            implements SpellPart.Modifier {
        @Override
        public Set<SpellModifierKind> modifies() {
            return Set.of(this.kind);
        }

        @Override
        public float value(SpellModifierKind qual) {
            return this.value;
        }

        @Override
        public float manaMultiplier(int quantas) {
            return 1.0f;
        }
    }

    /**
     * <b>Dano</b>: soma <b>2,2</b> ao dano, e cobra trinta por cento a mais por vez.
     *
     * <p>Repare que ele <b>soma</b> e não multiplica — é o que o original faz ao chamar o
     * {@code getModifiedDouble_Add} no dano, e é o que impede um feitiço de dano de crescer sem fim.
     */
    public static final SpellPart.Modifier DAMAGE = SpellParts.modifier(
            new Simple("damage", SpellModifierKind.DAMAGE, 2.2f, 1.3f));

    /**
     * <b>Legião</b>: mais uma invocação de pé, por cópia — e <b>dobra</b> a conta da etapa de cada vez.
     *
     * <p><b>Isto é acréscimo, e não porte.</b> O Ars Magica 2 sobe o teto com a perícia {@code ExtraSummon},
     * que é do ramo dos Talentos — um ramo que não é feito de peças de feitiço e que este porte não trouxe.
     * Trazê-la como <b>peça</b> é a única forma de ela caber na árvore daqui, e de caminho responde à pergunta
     * que a Invocação deixa no ar: e se o mago quisesse um exército?
     *
     * <p><b>O preço é o maior de todos, e é de propósito.</b> Cada cópia dobra o que a etapa custa, e uma
     * Invocação já custa 400 de mana: dois soldados custam 1600, três custam 3200. Um exército tem de doer,
     * senão não é uma escolha — é só o que se faz sempre.
     *
     * <p>A figura é a do próprio Ars Magica 2: a da perícia {@code ExtraSummon}, que é esta ideia na arte dele.
     */
    public static final SpellPart.Modifier LEGION = SpellParts.modifier(
            new Simple("legion", SpellModifierKind.SUMMON_COUNT, 1.0f, 2.0f));

    /** <b>Alcance</b>: soma quatro blocos, por vinte por cento a mais. */
    public static final SpellPart.Modifier RANGE = SpellParts.modifier(
            new Simple("range", SpellModifierKind.RANGE, 4.0f, 1.2f));

    /** <b>Duração</b>: multiplica o tempo por <b>2,2</b>, por vinte e cinco por cento a mais. */
    public static final SpellPart.Modifier DURATION = SpellParts.modifier(
            new Simple("duration", SpellModifierKind.DURATION, 2.2f, 1.25f));

    /**
     * <b>Raio</b>: multiplica o raio por <b>0,7</b> — e sim, isso <b>encolhe</b>.
     *
     * <p>É o número do original e não é engano: no Ars Magica 2 o modificador de raio custa <b>duas vezes e
     * meia</b> por vez e serve para <i>apertar</i> uma área, para o feitiço não pegar quem não devia. Quem o
     * quer maior põe a Forma de Área, que já nasce com três blocos.
     */
    public static final SpellPart.Modifier RADIUS = SpellParts.modifier(
            new Simple("radius", SpellModifierKind.RADIUS, 0.7f, 2.5f));

    /** <b>Cura</b>: dobra o que se cura, e cobra uma vez a mais por vez. */
    public static final SpellPart.Modifier HEALING = SpellParts.modifier(
            new Simple("healing", SpellModifierKind.HEALING, 2.0f, 1.0f));

    /** <b>Força de Mineração</b>: deixa o feitiço cavar pedra mais dura. */
    public static final SpellPart.Modifier MINING_POWER = SpellParts.modifier(
            new Simple("mining_power", SpellModifierKind.MINING_POWER, 1.0f, 1.25f));

    /**
     * <b>Velocidade</b>: multiplica a velocidade do que voa por <b>2,6</b>, por quinze por cento a mais por vez.
     *
     * <p>É o modificador mais barato do original e o que mais muda como o feitiço se sente na mão: um projétil
     * sem ele se arrasta a um bloco por batida, e com ele atravessa uma sala antes de se ver.
     */
    public static final SpellPart.Modifier SPEED = SpellParts.modifier(
            new Simple("speed", SpellModifierKind.SPEED, 2.6f, 1.15f));

    /**
     * <b>Gravidade</b>: soma <b>menos seis centésimos</b> à gravidade, de graça.
     *
     * <p>O valor é negativo e é o sinal que o faz cair, porque o projétil do original lê a gravidade ao
     * contrário do que o nome sugere. E ele <b>não cobra nada</b>: multiplicador um. É o que faz dele o
     * modificador de quem quer um feitiço de arco — pôr três Gravidades põe o projétil caindo em curva.
     */
    public static final SpellPart.Modifier GRAVITY = SpellParts.modifier(
            new Grátis("gravity", SpellModifierKind.GRAVITY, -0.06f));

    /** <b>Ricochete</b>: dá ao projétil <b>dois</b> saltos, por vinte e cinco por cento a mais por vez. */
    public static final SpellPart.Modifier BOUNCE = SpellParts.modifier(
            new Simple("bounce", SpellModifierKind.BOUNCE, 2.0f, 1.25f));

    /**
     * <b>Perfuração</b>: faz o projétil atravessar <b>dois</b> a mais, por metade a mais por vez.
     *
     * <p>A conta de quem atravessa parte de <b>zero</b> e não do dois que o feitio traz: um projétil sem este
     * modificador morre no primeiro que pegar. O dois é o que <i>cada</i> Perfuração acrescenta.
     */
    public static final SpellPart.Modifier PIERCING = SpellParts.modifier(
            new Simple("piercing", SpellModifierKind.PIERCING, 2.0f, 1.5f));

    /**
     * <b>Alvos Não Sólidos</b>: faz o feitiço pegar em água, erva alta e no que não tem caixa, <b>de graça</b>.
     *
     * <p>Ele não tem valor nenhum — o que conta é <b>estar lá</b>. Sem ele, um projétil passa pela água como se
     * ela não existisse; com ele, bate nela.
     */
    public static final SpellPart.Modifier TARGET_NONSOLID_BLOCKS = SpellParts.modifier(
            new Grátis("target_nonsolid_blocks", SpellModifierKind.TARGET_NONSOLID_BLOCKS, 1.0f));

    /**
     * <b>Repetições</b>: soma <b>quatro</b> às vezes que uma coisa acontece, por 65 por cento a mais por vez.
     *
     * <p>Ele só vale para quem conta vezes — hoje, a Runa. Uma Runa com uma Repetição aguenta cinco pisadas
     * em vez de uma.
     */
    public static final SpellPart.Modifier PROCS = SpellParts.modifier(
            new Simple("procs", SpellModifierKind.PROCS, 4.0f, 1.65f));

    /**
     * <b>Prosperidade</b>: põe <b>Fortuna</b> no que o feitiço quebrar, por vinte e cinco por cento a mais.
     *
     * <p>Ela não muda nada do feitiço: muda o que <i>cai</i> do que ele quebra. O original faz isso encantando
     * a vara invisível com que o Escavar colhe o bloco — uma Prosperidade é Fortuna I, duas é Fortuna II.
     */
    public static final SpellPart.Modifier PROSPERITY = SpellParts.modifier(
            new Simple("prosperity", SpellModifierKind.FORTUNE_LEVEL, 1.0f, 1.25f));

    /**
     * <b>Toque de Pena</b>: põe <b>Toque Suave</b> no que o feitiço quebrar, pelo mesmo preço.
     *
     * <p>Estar lá é o que conta, como no original: pôr dois não faz Toque Suave II, que não existe. E ele
     * <b>manda na Prosperidade</b> — um bloco colhido com seda cai como ele é, e a Fortuna não tem o que
     * multiplicar.
     */
    public static final SpellPart.Modifier FEATHER_TOUCH = SpellParts.modifier(
            new Simple("feather_touch", SpellModifierKind.SILKTOUCH_LEVEL, 1.0f, 1.25f));

    /**
     * Um modificador do <b>céu</b>: o {@code Solar} e o {@code Lunar} do original.
     *
     * <p>São os dois únicos que mudam de valor <i>enquanto se joga</i>. Os outros valem sempre o mesmo; estes
     * leem a hora do dia e a fase da lua, e o que eles dão de manhã não é o que dão à meia-noite. É por isso
     * que o modificador precisa de saber em que mundo está — e é por isso que
     * {@link SpellPart.Modifier#value(SpellModifierKind, net.minecraft.world.level.Level)} existe.
     *
     * <p>Custam <b>quatro vezes</b> por vez, que é o preço mais alto de todos os modificadores do original.
     *
     * <p><b>As contas são as do original, com as esquisitices que ele tem</b>, e duas merecem ser ditas:
     *
     * <ol>
     *   <li>Ele passa o ângulo por {@code × 180/π} <i>antes</i> de chamar o seno — ou seja, converte de
     *       radianos para graus e entrega graus a uma função que espera radianos. O resultado não é a onda
     *       suave que o nome sugere: é uma coisa que salta. Mantido, porque é o que o jogo faz.</li>
     *   <li>O Solar pergunta se a hora está <b>depois de 23500 e antes de 12500</b>, que nenhum número é. A
     *       pergunta é sempre não, e o alcance e o raio dele valem sempre {@code |3 − 1| = 2}. É um engano do
     *       original de 2014, e o porte o mantém: consertá-lo mudaria o feitiço de quem joga.</li>
     * </ol>
     */
    private record Céu(String name, boolean solar) implements SpellPart.Modifier {
        /** Quatro vezes por vez: o preço mais alto do quadro. */
        private static final float CUSTO = 4.0f;

        @Override
        public Set<SpellModifierKind> modifies() {
            return Set.of(SpellModifierKind.RANGE, SpellModifierKind.RADIUS, SpellModifierKind.DAMAGE,
                    SpellModifierKind.DURATION, SpellModifierKind.HEALING);
        }

        /** O que ele daria sem céu nenhum: o valor de partida, sem a hora e sem a lua. */
        @Override
        public float value(SpellModifierKind qual) {
            return this.value(qual, null);
        }

        @Override
        public float value(SpellModifierKind qual, @Nullable net.minecraft.world.level.Level mundo) {
            return switch (qual) {
                case RANGE, RADIUS -> this.pelaLua(mundo, 3.0f);
                case DAMAGE -> this.pelaHora(mundo, 2.4f);
                case DURATION -> this.pelaHora(mundo, 5.0f);
                case HEALING -> this.pelaHora(mundo, 2.0f);
                default -> 1.0f;
            };
        }

        @Override
        public float manaMultiplier(int quantas) {
            return CUSTO * quantas;
        }

        /** O valor pela hora do dia. Sem mundo, a onda vale um e o valor fica como está. */
        private float pelaHora(@Nullable net.minecraft.world.level.Level mundo, float valor) {
            if (mundo == null) return valor;
            float x = mundo.getOverworldClockTime() % 24000L;
            double ângulo = this.solar
                    ? (x / 3800.0f * (x / 24000.0f) - 13000.0f) * (180.0 / Math.PI)
                    : (x / 4600.0f * (x / 21000.0f) - 900.0f) * (180.0 / Math.PI);
            float onda = (float) (this.solar ? Math.cos(ângulo) * 1.5 : Math.sin(ângulo) * 3.0) + 1.0f;
            if (onda < 0.0f) onda *= -0.5f;
            return valor * onda;
        }

        /** E o valor pela fase da lua — que no Solar nunca chega a ser perguntado, como no original. */
        private float pelaLua(@Nullable net.minecraft.world.level.Level mundo, float valor) {
            if (mundo == null) return valor;
            long hora = mundo.getOverworldClockTime() % 24000L;
            int lua = mundo.environmentAttributes()
                    .getDimensionValue(net.minecraft.world.attribute.EnvironmentAttributes.MOON_PHASE).index();
            int fase = this.solar ? 8 - (8 - lua) : 8 - lua;
            boolean naHora = this.solar
                    // o engano do original: nenhum número é maior que 23500 e menor que 12500
                    ? hora > 23500L && hora < 12500L
                    : hora > 12500L && hora < 23500L;
            return naHora ? valor + fase / 2 : Math.abs(valor - 1.0f);
        }
    }

    /**
     * <b>Lunar</b>: o que ele dá depende da <b>noite</b> e da fase da lua.
     *
     * <p>Entre o anoitecer e o amanhecer ele soma metade da fase ao alcance e ao raio; de dia ele vale dois, e
     * é só. O dano, a duração e a cura seguem a hora, e a conta do original os faz saltar.
     */
    public static final SpellPart.Modifier LUNAR = SpellParts.modifier(new Céu("lunar", false));

    /**
     * <b>Solar</b>: o irmão de dia, com o engano do original guardado.
     *
     * <p>Ele devia ser o contrário do Lunar. A pergunta que ele faz sobre a hora não tem resposta possível, e
     * por isso o alcance e o raio dele valem <b>sempre dois</b>. O dano, a duração e a cura seguem a hora, com
     * um cosseno de amplitude menor — esses funcionam.
     */
    public static final SpellPart.Modifier SOLAR = SpellParts.modifier(new Céu("solar", true));

    /**
     * <b>Poder de Bênção</b>: sobe um grau em todo efeito que a frase puser, por vinte e cinco por cento a mais.
     *
     * <p>É o modificador que faz a Pressa ser Pressa II e o Congelar prender de verdade. Ele não tem valor
     * nenhum: o que conta é <b>quantas vezes</b> aparece, e é isso que vira o grau do efeito.
     *
     * <p>É uma das dez perícias <b>prateadas</b> do original — não se compra com nível, se descobre.
     */
    public static final SpellPart.Modifier BUFF_POWER = SpellParts.modifier(
            new Simple("buff_power", SpellModifierKind.BUFF_POWER, 1.0f, 1.25f));

    /**
     * <b>Desmembramento</b>: <b>cinco por cento</b> de chance de a cabeça cair, por vinte e cinco por cento a mais.
     *
     * <p>E só a quem tem cabeça para cair: esqueleto, esqueleto do Nether, zumbi, creeper e gente. Ele não
     * muda o dano — ele só importa no instante em que alguém morre do feitiço.
     *
     * <p>Também é <b>prateado</b>.
     */
    public static final SpellPart.Modifier DISMEMBERING = SpellParts.modifier(
            new Simple("dismembering", SpellModifierKind.DISMEMBERING_LEVEL, 0.05f, 1.25f));

    /**
     * <b>Velocidade Acrescentada</b>: soma <b>meio</b> ao empurrão, por trinta por cento a mais por vez.
     *
     * <p>Ele não mexe no que voa — isso é a Velocidade. Ele mexe no que <b>empurra</b>: o Arremesso, o
     * Empurrão e o Repelir. É a diferença entre atirar alguém ao ar e atirar alguém para longe.
     */
    public static final SpellPart.Modifier VELOCITY_ADDED = SpellParts.modifier(
            new Simple("velocity_added", SpellModifierKind.VELOCITY_ADDED, 0.5f, 1.3f));

    /**
     * <b>Cor</b>: pinta o pó do feitiço da cor que se quiser, <b>de graça</b>.
     *
     * <p>Ele não muda número nenhum — muda o que se <b>vê</b>. Um feitiço de fogo com a Cor azul continua
     * queimando: o que ele deixa de fazer é <i>parecer</i> um feitiço de fogo.
     *
     * <p>É a única peça do ramo que precisa de uma <b>escolha</b> para valer alguma coisa, e a escolha é a
     * <b>tinta</b> que se põe ao lado dela na Mesa de Inscrição. Sem tinta ela não vira feitiço: a Mesa
     * recusa a frase e diz porquê.
     *
     * <p><b>Desvio declarado quanto ao lugar:</b> no original ela mora no quarto ramo do quadro, o dos
     * <b>Talentos</b> — que não é feito de peças de feitiço e não está portado. Aqui fica na Utilidade, ao pé
     * do Toque, que é onde um arcanista a encontraria cedo.
     */
    public static final SpellPart.Modifier COLOUR = SpellParts.modifier(
            new Grátis("colour", SpellModifierKind.COLOR, 1.0f));

    /**
     * As dezesseis cores das tintas, <b>tal como o original as escreve</b>.
     *
     * <p>São a tabela {@code ItemDye.dyeColors} do 1.7.10, na ordem dos danos de tinta daquela versão. O jogo
     * de hoje tem outras tabelas — a da ovelha, a do fogo de artifício, a do texto — e nenhuma delas dá estes
     * números. Como o que se vê é o ponto desta peça, ficam os do original.
     */
    private static final java.util.Map<net.minecraft.world.item.DyeColor, Integer> TINTAS =
            new java.util.EnumMap<>(java.util.Map.ofEntries(
                    java.util.Map.entry(net.minecraft.world.item.DyeColor.BLACK, 1973019),
                    java.util.Map.entry(net.minecraft.world.item.DyeColor.RED, 11743532),
                    java.util.Map.entry(net.minecraft.world.item.DyeColor.GREEN, 3887386),
                    java.util.Map.entry(net.minecraft.world.item.DyeColor.BROWN, 5320730),
                    java.util.Map.entry(net.minecraft.world.item.DyeColor.BLUE, 2437522),
                    java.util.Map.entry(net.minecraft.world.item.DyeColor.PURPLE, 8073150),
                    java.util.Map.entry(net.minecraft.world.item.DyeColor.CYAN, 2651799),
                    java.util.Map.entry(net.minecraft.world.item.DyeColor.LIGHT_GRAY, 11250603),
                    java.util.Map.entry(net.minecraft.world.item.DyeColor.GRAY, 4408131),
                    java.util.Map.entry(net.minecraft.world.item.DyeColor.PINK, 14188952),
                    java.util.Map.entry(net.minecraft.world.item.DyeColor.LIME, 4312372),
                    java.util.Map.entry(net.minecraft.world.item.DyeColor.YELLOW, 14602026),
                    java.util.Map.entry(net.minecraft.world.item.DyeColor.LIGHT_BLUE, 6719955),
                    java.util.Map.entry(net.minecraft.world.item.DyeColor.MAGENTA, 12801229),
                    java.util.Map.entry(net.minecraft.world.item.DyeColor.ORANGE, 15435844),
                    java.util.Map.entry(net.minecraft.world.item.DyeColor.WHITE, 15790320)));

    /**
     * A cor daquela tinta, ou nada se aquele item não for tinta.
     *
     * <p>O jogo de hoje já não tem como perguntar a um item de tinta qual é a cor dele: as dezesseis vivem
     * numa coleção por cor, e quem quer o caminho de volta faz o seu próprio.
     */
    public static @Nullable Integer dye(net.minecraft.world.item.ItemStack coisa) {
        return DE_VOLTA.get(coisa.getItem());
    }

    private static final java.util.Map<net.minecraft.world.item.Item, Integer> DE_VOLTA = deVolta();

    private static java.util.Map<net.minecraft.world.item.Item, Integer> deVolta() {
        var mapa = new java.util.IdentityHashMap<net.minecraft.world.item.Item, Integer>();
        for (var par : TINTAS.entrySet()) {
            mapa.put(net.minecraft.world.item.Items.DYE.pick(par.getKey()), par.getValue());
        }
        return java.util.Map.copyOf(mapa);
    }

    /** Sem uso fora do porte: obriga a classe a ser carregada, e com ela os Modificadores a se registrarem. */
    public static void init() {
    }
}
