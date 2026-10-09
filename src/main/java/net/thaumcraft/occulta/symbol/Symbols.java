package net.thaumcraft.occulta.symbol;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * O <b>registro dos símbolos</b>: a {@code EffectRegistry} do Witchery.
 *
 * <p>Guarda, para cada <b>desenho</b>, que símbolo ele é e de que <b>grau</b>. O desenho é uma sequência de
 * traços — cima, baixo, direita, esquerda — e cada símbolo tem vários, um ou dois por grau.
 *
 * <p>Quem os lê é a {@linkplain net.thaumcraft.occulta.MysticBranchItem Vara Mística}, do lado de cá: a cada
 * batida ela olha quanto a cabeça girou, e quando o que foi desenhado bate com um desenho da tabela, o nome
 * do feitiço aparece. Largando a vara, ele sai.
 *
 * <h2>O que esta fatia traz</h2>
 *
 * <p><b>Vinte e quatro</b> dos trinta e um símbolos do original.
 *
 * <p>Dezoito deles <b>atiram uma bola</b> — Accio, Aguamenti, Alohomora, Attraho, Avada Kedavra, Cave
 * Inimicum, Confundus, Crucio, Defodio, Ennervate, Expelliarmus, Flipendo, Ignianima, Imperio, Incendio,
 * Lumos e Stupefy — e seis agem <b>a partir de quem os lança</b>: Carnosa Diem, Episkey, Flagrate,
 * Meteolojinx Recanto, Nox e Protego.
 *
 * <p>E <b>três deles são imperdoáveis</b>: o Avada Kedavra, o Crucio e o Imperio só se lançam com a
 * <b>Infusão Infernal</b> no corpo. É a única porta trancada da tabela.
 *
 * <p>Os sete que faltam pedem coisas que ainda não estão portadas: as <b>portas do ofício</b>
 * (Colloportus), o <b>Tormento</b> (Tormentum), a <b>Marca Negra</b> (Morsmordre) e o <b>Leonard</b> (os
 * quatro dele). Os números e os desenhos deles já estão levantados do original, traço por traço.
 */
public final class Symbols {
    /** Os quatro traços que a vara sabe ler. */
    public static final byte CIMA = 0;
    public static final byte BAIXO = 1;
    public static final byte DIREITA = 2;
    public static final byte ESQUERDA = 3;

    /** Quantos graus a cabeça tem de girar para contar um traço. */
    public static final float GIRO = 7.0f;

    /** E quantos traços um desenho pode ter. */
    public static final int MÁXIMO = 15;

    /** O número da Infusão Infernal, que é a única que lança os imperdoáveis. */
    public static final int A_INFERNAL = 4;

    private static final List<Symbol> TODOS = new ArrayList<>();
    private static final Map<String, Symbol> POR_DESENHO = new HashMap<>();
    private static final Map<String, Integer> GRAU_DO_DESENHO = new HashMap<>();

    static {
        /*
         * <b>Accio</b>: puxa para quem o lançou tudo o que estiver largado no chão à volta de onde a bola
         * bateu. Oito décimos de bloco no grau um, três no dois, <b>nove</b> no três — que é o grau que
         * limpa uma caverna inteira de uma vez.
         */
        põe(new Accio(1), "1,3,0,2,2,1", "1,3,0,2,2,2,1", "2,3,0,0,2,2,1,1", "2,3,0,0,2,2,2,1,1",
                "3,3,0,0,0,2,2,2,1,1,1", "3,3,0,0,0,2,2,2,2,1,1,1");

        /*
         * <b>Aguamenti</b>: água onde a bola bate. No Nether só funciona no <b>grau três</b> — e é a
         * exceção escrita à mão no original, porque lá a água some.
         */
        põe(new Aguamenti(2), "1,0,0,2,2,1", "1,0,0,2,2,2,1", "2,0,0,0,2,2,1,1", "2,0,0,0,2,2,2,1,1",
                "3,0,0,0,0,2,2,1,1,1", "3,0,0,0,0,2,2,2,1,1,1");

        /*
         * <b>Incendio</b>: fogo onde a bola bate, no grau um; e, nos graus dois e três, pega fogo a tudo o
         * que estiver a três ou seis blocos dali.
         *
         * <p>E ele é o que <b>acende o Homem de Vime</b> — o único jeito de o acender sem isqueiro.
         */
        põe(new Incendio(21), "1,3,0,0,1,1", "2,3,0,0,0,1,1,1", "3,3,0,0,0,0,1,1,1,1");

        /*
         * <b>Flipendo</b>: empurra. No grau um só o que a bola acertar; nos outros, tudo o que estiver a
         * três ou seis blocos — e no grau dois <b>incluindo quem o lançou</b>, que é um descuido do
         * original e fica como está.
         */
        põe(new Flipendo(17), "1,2,2,3", "1,2,2,2,3,3", "2,2,2,2,2,3,3,3", "3,2,2,2,2,2,3,3,3,3");

        /*
         * <b>Lumos</b>: um <b>Globo de Luz</b> posto onde a bola bate. É o feitiço mais barato do mod e o
         * que mais se usa: ele não gasta nada do inventário, e o globo fica.
         */
        põe(new Lumos(22), "1,1,1,2");

        /*
         * <b>Nox</b>: e o avesso dele. Tira <b>tudo o que der luz</b> num cubo de dez blocos à volta de
         * quem o lança — tochas, lanternas, fogueiras, globos.
         *
         * <p>Custa <b>cinquenta</b>, que é o mais caro desta leva, e o desenho dele é o único do original
         * que vem com <b>grau zero</b>. Não é engano: com grau zero o custo é <b>metade</b>, e o original
         * preferiu escrever o preço assim a mexer no número.
         */
        /*
         * <b>Attraho</b>: o avesso do Flipendo — puxa para si o que for vivo, em vez de o empurrar.
         */
        põe(new Attraho(47), "1,0,0,0,2,2,1,3");

        /*
         * <b>Avada Kedavra</b>, <b>Crucio</b> e <b>Imperio</b>: as três <b>imperdoáveis</b>, que só se
         * lançam com a <b>Infusão Infernal</b> no corpo. Matar, doer e escravizar — e nenhuma delas tem
         * um desenho que se acerte por acaso: a do Avada Kedavra tem <b>doze traços</b>.
         */
        põe(new AvadaKedavra(4), "1,1,2,2,0,0,3,3,3,3,1,1,2");
        põe(new Crucio(9), "1,1,3,1,1,2", "1,1,3,3,1,1,2,2", "2,1,3,1,1,1,2",
                "2,1,3,3,1,1,1,2,2", "3,1,3,3,3,1,1,1,1,2,2,2");
        põe(new Imperio(20), "2,1,1,1,1");

        /*
         * <b>Carnosa Diem</b> e <b>Ignianima</b>: as duas maldições que <b>não são imperdoáveis</b>,
         * porque o original as tranca atrás de um apontamento no livro em vez de uma infusão. Uma troca
         * vida por poder, a outra queima mais quanto pior estiver quem a lança.
         */
        põe(new CarnosaDiem(40), "2,2,0,1,1", "2,2,0,0,1,1,1,1", "2,2,2,0,1,1",
                "2,2,2,0,0,1,1,1,1", "2,2,2,2,0,1,1", "2,2,2,2,0,0,1,1,1,1");
        põe(new Ignianima(39), "3,3,0,1,1", "3,3,0,0,1,1,1,1", "3,3,3,0,1,1",
                "3,3,3,0,0,1,1,1,1", "3,3,3,3,0,1,1", "3,3,3,3,0,0,1,1,1,1");

        /*
         * <b>Cave Inimicum</b> e <b>Defodio</b>: os dois que mexem em bloco e não em bicho. Um endurece
         * o que é mole, o outro cava o que é duro — e os dois num <b>quadrado da face</b> que cresce com
         * o grau.
         */
        põe(new CaveInimicum(5), "1,0,3,0,0,2", "1,0,3,0,0,0,2", "1,0,3,3,0,0,2,2",
                "2,0,3,3,0,0,0,2,2", "3,0,3,3,3,0,0,0,0,2,2,2");
        põe(new Defodio(10), "1,0,0,3,1", "1,0,0,0,3,1,1", "1,0,0,3,3,1,2",
                "2,0,0,0,3,3,1,1,2", "2,0,0,0,0,3,3,1,1,1,2", "2,0,0,0,3,3,3,1,1,2,2",
                "3,0,0,0,0,3,3,3,1,1,1,2,2");

        /*
         * E os dois que não atiram nada: o <b>Flagrate</b>, que risca um glifo infernal na parede, e o
         * <b>Meteolojinx Recanto</b>, que <b>para a chuva</b> por cem cargas.
         */
        põe(new Flagrate(16), "2,0,2,3,0,2");
        põe(new MeteolojinxRecanto(23), "0,0,0,2,2,1,0,2,2,1,1");

        /*
         * <b>Alohomora</b>: abre ou fecha a porta em que a bola bate. É o menor feitiço do mod —
         * dois traços e meio — e o único que não tem grau nenhum acima do primeiro.
         */
        põe(new Alohomora(3), "2,0,2,2,1", "2,0,2,2,2,1", "2,0,0,2,2,1,1", "2,0,0,2,2,2,1,1");

        /*
         * <b>Confundus</b>: confunde. Dez segundos de náusea em quem a bola acertar, ou em tudo o que
         * estiver a dois ou quatro blocos.
         */
        põe(new Confundus(8), "1,3,3,0,0,2", "1,3,3,3,0,0,2,2", "2,3,3,3,0,0,0,2,2",
                "3,3,3,3,3,0,0,0,0,2,2,2");

        /*
         * <b>Ennervate</b>: o avesso do Confundus e de metade do que o mod sabe fazer — tira a
         * <b>lentidão</b>, a <b>fraqueza</b> e a <b>náusea</b> de quem a bola acertar.
         *
         * <p>E a bola dele <b>cai</b>: ela não voa a direito, vai descendo. É o original dizendo que quem
         * se cura dos outros tem de chegar perto.
         */
        põe(new Ennervate(12), "1,0,3,0,2,3,0,2", "2,0,3,3,0,2,2,3,3,0,2,2",
                "3,0,3,3,3,0,2,2,2,3,3,3,0,2,2,2");

        /*
         * <b>Episkey</b>: cura — e <b>cobra a comida por ela</b>. Quem é curado perde da barriga o que
         * ganhou de vida, e fica com náusea quatro segundos.
         *
         * <p>É o único feitiço de cura do mod, e é de propósito que ele não é de graça: curar alguém é
         * <b>passar-lhe a conta</b>.
         */
        põe(new Episkey(13), "1,2,0,3,1,1,2", "2,2,0,0,3,1,1,1,1,2", "2,2,2,0,3,3,1,1,2,2",
                "3,2,2,0,0,3,3,1,1,1,1,2,2");

        /*
         * <b>Expelliarmus</b>: desarma. O que estiver na mão de quem a bola acertar <b>cai no chão</b>.
         * E não vale contra quem o lançou.
         */
        põe(new Expelliarmus(15), "1,0,0,1", "1,0,0,0,1,1", "2,0,0,0,0,1,1,1",
                "3,0,0,0,0,0,1,1,1,1");

        /* <b>Impedimenta</b>: trava. Lentidão II por trinta segundos, e nunca em quem o lançou. */
        põe(new Impedimenta(19), "1,3,3,2", "1,3,3,3,2,2", "2,3,3,3,3,2,2,2",
                "3,3,3,3,3,3,2,2,2,2");

        /*
         * <b>Protego</b>: um <b>escudo de luz</b> à frente de quem o lança, com a mesma parede de três
         * colunas da Infusão da Luz. Quatro blocos de alcance, e só contra o chão.
         */
        põe(new Protego(31), "1,1,0", "1,1,1,0,0", "1,1,1,1,0,0,0");

        /*
         * <b>Stupefy</b>: estonteia. <b>Lentidão X por cinco minutos</b> em quem a bola acertar — o que
         * é, na prática, pô-lo de pé e parado onde está.
         *
         * <p>Custa <b>cinco</b>, e a bola dele também cai. É o feitiço que acaba uma luta sem a ganhar.
         */
        põe(new Stupefy(36), "1,2,2,0,3,0,2");

        põe(new Nox(26), "0,0,2,1,2,0");
    }

    private Symbols() {
    }

    /** Sem uso fora do porte: obriga a classe a carregar, e com ela a tabela. */
    public static void init() {
    }

    /**
     * Põe um símbolo na tabela com os desenhos dele.
     *
     * <p>Cada desenho vem como o original o escreve: o <b>grau</b> primeiro e os <b>traços</b> a seguir.
     */
    private static void põe(Symbol qual, String... desenhos) {
        TODOS.add(qual);
        for (String cada : desenhos) {
            String[] partes = cada.split(",");
            int grau = Integer.parseInt(partes[0]);
            String traços = String.join(",", Arrays.copyOfRange(partes, 1, partes.length));
            POR_DESENHO.put(traços, qual);
            GRAU_DO_DESENHO.put(traços, grau);
        }
    }

    /** O símbolo daquele número, ou nada. */
    public static @Nullable Symbol daquele(int id) {
        for (Symbol cada : TODOS) {
            if (cada.id == id) return cada;
        }
        return null;
    }

    /** O símbolo daquele desenho, ou nada. */
    public static @Nullable Symbol doDesenho(byte[] traços) {
        return POR_DESENHO.get(chave(traços));
    }

    /** E de que grau ele é. */
    public static int grauDoDesenho(byte[] traços) {
        return GRAU_DO_DESENHO.getOrDefault(chave(traços), 1);
    }

    /** Se este desenho já é um símbolo inteiro — a vara para de ler quando for. */
    public static boolean éUmSímbolo(byte[] traços) {
        return POR_DESENHO.containsKey(chave(traços));
    }

    /** Quantos há. */
    public static int quantos() {
        return TODOS.size();
    }

    /** E todos eles, para quem precisar de os percorrer. */
    public static List<Symbol> todos() {
        return List.copyOf(TODOS);
    }

    private static String chave(byte[] traços) {
        StringBuilder feito = new StringBuilder();
        for (int volta = 0; volta < traços.length; volta++) {
            if (volta > 0) feito.append(',');
            feito.append(traços[volta]);
        }
        return feito.toString();
    }

    /** O que o <b>Defodio</b> cava: os sete materiais moles do original, com os nomes de hoje. */
    public static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> CAVÁVEL =
            net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK,
                    net.thaumcraft.Thaumcraft.id("defodio"));

    /** De quantos blocos é o lado do quadrado que os feitiços de bloco pegam, por grau. */
    public static final int LADO_MÁXIMO = 3;

    /**
     * Quanto o <b>Ignianima</b> dói, conforme a vida de quem o lança.
     *
     * <p>Fora daqui só a prova a usa, e vale a pena: é a única conta do mod que premeia estar quase
     * morto, e uma conta dessas merece ser conferida de fora.
     */
    public static float ignianima(@Nullable LivingEntity quem) {
        return Ignianima.quanto(quem);
    }

    // ------------------------------------------------------------------ o que eles partilham

    /** O bloco do lado de cá da face em que a bola bateu, que é onde as coisas se põem. */
    public static BlockPos adiante(ServerLevel level, BlockHitResult bateu) {
        BlockPos onde = bateu.getBlockPos();
        Direction face = bateu.getDirection();
        BlockPos ali = onde.relative(face);
        if (face == Direction.UP && !level.getBlockState(onde).isSolidRender()) return ali.below();
        return ali;
    }

    /**
     * Corre uma coisa num <b>quadrado da face</b> em que a bola bateu: o {@code applyBlockEffect} do
     * original.
     *
     * <p>No grau um é um bloco só; nos outros é um quadrado de lado <b>dois vezes o grau menos um</b>,
     * desenhado <b>no plano da face</b> — de modo que acertar no chão pega um tapete e acertar numa
     * parede pega um painel.
     */
    public static void naFace(ServerLevel level, BlockHitResult bateu, int grau,
                              java.util.function.Consumer<BlockPos> oquê) {
        BlockPos meio = bateu.getBlockPos();
        if (grau <= 1) {
            if (podeMexer(level, meio)) oquê.accept(meio);
            return;
        }
        int r = Math.min(grau - 1, LADO_MÁXIMO);
        Direction face = bateu.getDirection();
        for (int k = -r; k <= r; k++) {
            for (int j = -r; j <= r; j++) {
                BlockPos ali = switch (face.getAxis()) {
                    case Y -> meio.offset(k, 0, j);
                    case Z -> meio.offset(k, j, 0);
                    case X -> meio.offset(0, k, j);
                };
                if (podeMexer(level, ali)) oquê.accept(ali);
            }
        }
    }

    /** E o que o ofício não mexe: ar, e o que a {@link net.thaumcraft.occulta.BlockProtect} recusa. */
    private static boolean podeMexer(ServerLevel level, BlockPos onde) {
        return !level.getBlockState(onde).isAir()
                && net.thaumcraft.occulta.BlockProtect.podeMexer(level, onde);
    }

    /** Onde a bola bateu, seja num bicho ou num bloco. */
    public static Vec3 ondeBateu(HitResult onde) {
        return onde.getLocation();
    }

    /**
     * Corre uma coisa em tudo o que for vivo à volta de onde a bola bateu.
     *
     * <p>Com raio zero, só no que ela acertou — que é como os graus um funcionam.
     */
    public static void emVolta(ServerLevel level, @Nullable LivingEntity quem, HitResult onde, double raio,
                               java.util.function.Consumer<LivingEntity> oquê) {
        if (raio <= 0.0) {
            if (onde instanceof EntityHitResult bateu
                    && bateu.getEntity() instanceof LivingEntity bicho) {
                oquê.accept(bicho);
            }
            return;
        }
        Vec3 meio = ondeBateu(onde);
        AABB caixa = new AABB(meio.x - raio, meio.y - raio, meio.z - raio,
                meio.x + raio, meio.y + raio, meio.z + raio);
        for (LivingEntity bicho : level.getEntitiesOfClass(LivingEntity.class, caixa)) {
            if (bicho.distanceToSqr(meio) > raio * raio) continue;
            oquê.accept(bicho);
        }
    }

    // ------------------------------------------------------------------ os seis

    /** <b>Accio</b>: puxa o que está largado no chão. */
    private static final class Accio extends ProjectileSymbol {
        Accio(int id) {
            super(id, "accio");
            this.cor(0x513666).tamanho(1.0f);
        }

        @Override
        public void aoBater(ServerLevel level, @Nullable LivingEntity quem, HitResult onde, int grau) {
            if (quem == null) return;
            double raio = grau == 1 ? 0.8 : (grau == 2 ? 3.0 : 9.0);
            Vec3 meio = ondeBateu(onde);
            AABB caixa = new AABB(meio.x - raio, meio.y - raio, meio.z - raio,
                    meio.x + raio, meio.y + raio, meio.z + raio);
            for (ItemEntity coisa : level.getEntitiesOfClass(ItemEntity.class, caixa)) {
                if (coisa.distanceToSqr(meio) > raio * raio) continue;
                coisa.setPos(quem.getX(), quem.getY() + 1.0, quem.getZ());
            }
        }
    }

    /** <b>Aguamenti</b>: água. */
    private static final class Aguamenti extends ProjectileSymbol {
        Aguamenti(int id) {
            super(id, "aguamenti");
            this.cor(0x11F8FF).tamanho(2.0f);
        }

        @Override
        public void aoBater(ServerLevel level, @Nullable LivingEntity quem, HitResult onde, int grau) {
            /*
             * No Nether ela só sai no <b>grau três</b>, e fora dele só no <b>grau um</b> — é a exceção
             * escrita à mão no original, e o que ela está dizendo é que água no Nether custa o triplo.
             */
            boolean noNether = level.dimension() == net.minecraft.world.level.Level.NETHER;
            if (noNether ? grau != 3 : grau != 1) return;

            if (onde instanceof EntityHitResult bateu) {
                põeSeCouber(level, bateu.getEntity().blockPosition(), Blocks.WATER);
                return;
            }
            if (onde instanceof BlockHitResult bateu) {
                põeSeCouber(level, adiante(level, bateu), Blocks.WATER);
            }
        }
    }

    /** <b>Incendio</b>: fogo. */
    private static final class Incendio extends ProjectileSymbol {
        Incendio(int id) {
            super(id, "incendio");
            this.cor(0xFF3737).tamanho(2.0f);
        }

        @Override
        public void aoBater(ServerLevel level, @Nullable LivingEntity quem, HitResult onde, int grau) {
            double raio = grau == 1 ? 0.0 : (grau == 2 ? 3.0 : 6.0);
            if (raio > 0.0) {
                emVolta(level, quem, onde, raio, bicho -> bicho.igniteForSeconds(1.0f));
                return;
            }
            if (onde instanceof EntityHitResult bateu) {
                bateu.getEntity().igniteForSeconds(1.0f);
                return;
            }
            if (!(onde instanceof BlockHitResult bateu)) return;

            /*
             * E antes do fogo, as duas exceções do original: a bola acende o <b>Homem de Vime</b> e o
             * <b>braseiro</b> em vez de lhes pôr fogo por cima.
             */
            BlockPos ali = bateu.getBlockPos();
            if (level.getBlockState(ali).is(net.thaumcraft.occulta.OccultaBlocks.WICKER_BUNDLE)
                    && net.thaumcraft.occulta.WickerBundleBlock.acende(level, ali,
                            quem == null ? 0.0f : quem.getYRot())) {
                return;
            }
            põeSeCouber(level, adiante(level, bateu), Blocks.FIRE);
        }
    }

    /** <b>Flipendo</b>: empurra. */
    private static final class Flipendo extends ProjectileSymbol {
        Flipendo(int id) {
            super(id, "flipendo");
            this.cor(0xFFFD99).tamanho(3.0f);
        }

        /** Quanto ele empurra, e quanto a mais se o alvo estiver lento. */
        public static final double EMPURRA = 2.0;
        public static final double SE_LENTO = 0.5;
        public static final double ACIMA = 0.3;

        @Override
        public void aoBater(ServerLevel level, @Nullable LivingEntity quem, HitResult onde, int grau) {
            double raio = grau == 1 ? 0.0 : (grau == 2 ? 3.0 : 6.0);
            Vec3 meio = ondeBateu(onde);
            Vec3 rumo = quem == null ? Vec3.ZERO : quem.getLookAngle();
            emVolta(level, quem, onde, raio, bicho -> {
                /*
                 * <b>No grau dois ele empurra quem o lançou também</b>, e é um descuido do original: a
                 * pergunta que ele faz é «se o raio for três, ou se o alvo não for quem lançou», e o «ou»
                 * deixa o próprio passar no grau dois. Fica como está.
                 */
                if (raio != 3.0 && bicho == quem) return;
                double quanto = EMPURRA
                        + (bicho.hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS) ? SE_LENTO : 0.0);
                bicho.push(rumo.x * quanto, ACIMA, rumo.z * quanto);
                bicho.hurtMarked = true;
            });
        }
    }

    /** <b>Lumos</b>: um Globo de Luz onde a bola bate. */
    private static final class Lumos extends ProjectileSymbol {
        Lumos(int id) {
            super(id, "lumos");
            this.cor(0xFFFFFA).tamanho(0.5f);
        }

        @Override
        public void aoBater(ServerLevel level, @Nullable LivingEntity quem, HitResult onde, int grau) {
            if (!(onde instanceof BlockHitResult bateu)) return;
            BlockPos ali = bateu.getBlockPos().relative(bateu.getDirection());
            var estava = level.getBlockState(ali);
            if (!estava.isAir() && !estava.canBeReplaced()) return;
            level.setBlockAndUpdate(ali,
                    net.thaumcraft.occulta.OccultaBlocks.GLOW_GLOBE.defaultBlockState());
        }
    }

    /** <b>Nox</b>: e o avesso dele. */
    private static final class Nox extends Symbol {
        /** Até onde ele apaga. */
        public static final int ALCANCE = 10;

        Nox(int id) {
            super(id, "nox", 50, false, false, 0);
        }

        @Override
        public void lança(ServerLevel level, ServerPlayer quem, int grau) {
            BlockPos meio = quem.blockPosition();
            for (BlockPos ali : BlockPos.betweenClosed(meio.offset(-ALCANCE, -ALCANCE, -ALCANCE),
                    meio.offset(ALCANCE, ALCANCE, ALCANCE))) {
                var feitio = level.getBlockState(ali);
                if (feitio.getLightEmission() <= 0) continue;
                level.destroyBlock(ali, true, quem);
            }
        }
    }
    /** <b>Alohomora</b>: abre ou fecha a porta em que bate. */
    private static final class Alohomora extends ProjectileSymbol {
        Alohomora(int id) {
            super(id, "alohomora");
            this.cor(0x513666).tamanho(0.5f);
        }

        @Override
        public void aoBater(ServerLevel level, @Nullable LivingEntity quem, HitResult onde, int grau) {
            if (!(onde instanceof BlockHitResult bateu)) return;
            BlockPos ali = bateu.getBlockPos();
            var feitio = level.getBlockState(ali);
            if (!(feitio.getBlock() instanceof net.minecraft.world.level.block.DoorBlock porta)) return;
            porta.setOpen(quem, level, feitio, ali,
                    !feitio.getValue(net.minecraft.world.level.block.DoorBlock.OPEN));
        }
    }

    /** <b>Confundus</b>: confunde. */
    private static final class Confundus extends ProjectileSymbol {
        /** Quanto a náusea dura. */
        public static final int DURA = 600;

        Confundus(int id) {
            super(id, "confundus");
            this.cor(0xFFE300).tamanho(1.5f);
        }

        @Override
        public void aoBater(ServerLevel level, @Nullable LivingEntity quem, HitResult onde, int grau) {
            double raio = grau == 1 ? 0.0 : (grau == 2 ? 2.0 : 4.0);
            emVolta(level, quem, onde, raio, bicho -> {
                if (bicho.hasEffect(net.minecraft.world.effect.MobEffects.NAUSEA)) return;
                bicho.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.NAUSEA, DURA, 0));
            });
        }
    }

    /** <b>Ennervate</b>: tira a lentidão, a fraqueza e a náusea. */
    private static final class Ennervate extends ProjectileSymbol {
        Ennervate(int id) {
            super(id, "ennervate", 1, false, false, 0);
            this.cor(0xFF187B).tamanho(1.5f);
        }

        @Override
        public void aoBater(ServerLevel level, @Nullable LivingEntity quem, HitResult onde, int grau) {
            double raio = grau == 1 ? 0.0 : (grau == 2 ? 2.0 : 4.0);
            emVolta(level, quem, onde, raio, bicho -> {
                bicho.removeEffect(net.minecraft.world.effect.MobEffects.SLOWNESS);
                bicho.removeEffect(net.minecraft.world.effect.MobEffects.WEAKNESS);
                bicho.removeEffect(net.minecraft.world.effect.MobEffects.NAUSEA);
            });
        }
    }

    /** <b>Episkey</b>: cura, e cobra a comida por ela. */
    private static final class Episkey extends Symbol {
        /** Quanto ele cura de cada vez, e quanto a náusea de depois dura. */
        public static final int CURA = 5;
        public static final int ENJOA = 80;

        Episkey(int id) {
            super(id, "episkey", 1, false, false, 0);
        }

        @Override
        public void lança(ServerLevel level, ServerPlayer quem, int grau) {
            double raio = grau == 1 ? 0.0 : (grau == 2 ? 2.0 : 4.0);
            if (raio <= 0.0) {
                cura(quem);
                return;
            }
            AABB caixa = quem.getBoundingBox().inflate(raio);
            for (LivingEntity bicho : level.getEntitiesOfClass(LivingEntity.class, caixa)) {
                if (bicho.distanceToSqr(quem) > raio * raio) continue;
                cura(bicho);
            }
        }

        /**
         * E a conta: quem é curado <b>perde da barriga</b> o que ganhou de vida.
         *
         * <p>Quem não tem barriga — tudo o que não é gente — conta como tendo cinco, e por isso se cura
         * sem pagar nada. É assim no original.
         */
        private static void cura(LivingEntity bicho) {
            boolean temBarriga = bicho instanceof net.minecraft.world.entity.player.Player;
            int comida = temBarriga
                    ? ((net.minecraft.world.entity.player.Player) bicho).getFoodData().getFoodLevel()
                    : CURA;
            if (comida <= 1 || bicho.getHealth() >= bicho.getMaxHealth()) return;
            int quanto = Math.min(CURA, comida);
            bicho.heal(quanto);
            if (temBarriga) {
                ((net.minecraft.world.entity.player.Player) bicho).getFoodData().eat(-quanto, 0.0f);
            }
            if (!bicho.hasEffect(net.minecraft.world.effect.MobEffects.NAUSEA)) {
                bicho.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.NAUSEA, ENJOA));
            }
        }
    }

    /** <b>Expelliarmus</b>: desarma. */
    private static final class Expelliarmus extends ProjectileSymbol {
        Expelliarmus(int id) {
            super(id, "expelliarmus");
            this.cor(0xFF9E42).tamanho(3.0f);
        }

        @Override
        public void aoBater(ServerLevel level, @Nullable LivingEntity quem, HitResult onde, int grau) {
            double raio = grau == 1 ? 0.0 : (grau == 2 ? 3.0 : 5.0);
            emVolta(level, quem, onde, raio, bicho -> {
                if (bicho == quem) return;
                if (bicho instanceof ServerPlayer gente) {
                    gente.drop(gente.getInventory().getSelectedItem(), true);
                    gente.getInventory().setSelectedItem(net.minecraft.world.item.ItemStack.EMPTY);
                    return;
                }
                var mão = bicho.getMainHandItem();
                if (mão.isEmpty()) return;
                bicho.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                        net.minecraft.world.item.ItemStack.EMPTY);
                bicho.spawnAtLocation(level, mão);
            });
        }
    }

    /** <b>Impedimenta</b>: trava. */
    private static final class Impedimenta extends ProjectileSymbol {
        /** Quanto a lentidão dura. */
        public static final int DURA = 600;

        Impedimenta(int id) {
            super(id, "impedimenta");
            this.cor(0x5E7FFF).tamanho(1.5f);
        }

        @Override
        public void aoBater(ServerLevel level, @Nullable LivingEntity quem, HitResult onde, int grau) {
            double raio = grau == 1 ? 0.0 : (grau == 2 ? 3.0 : 6.0);
            emVolta(level, quem, onde, raio, bicho -> {
                if (bicho == quem) return;
                if (bicho.hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS)) return;
                bicho.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.SLOWNESS, DURA, 1));
            });
        }
    }

    /** <b>Protego</b>: um escudo de luz à frente. */
    private static final class Protego extends Symbol {
        /** Até onde o olhar dele chega. */
        public static final double OLHAR = 4.0;

        Protego(int id) {
            super(id, "protego");
        }

        @Override
        public void lança(ServerLevel level, ServerPlayer quem, int grau) {
            Vec3 olhos = quem.getEyePosition();
            Vec3 rumo = olhos.add(quem.getLookAngle().scale(OLHAR));
            BlockHitResult bateu = level.clip(new net.minecraft.world.level.ClipContext(olhos, rumo,
                    net.minecraft.world.level.ClipContext.Block.COLLIDER,
                    net.minecraft.world.level.ClipContext.Fluid.NONE, quem));
            if (bateu.getType() == HitResult.Type.MISS) {
                net.thaumcraft.occulta.infusion.Infusion.falha(level, quem);
                return;
            }
            net.thaumcraft.occulta.infusion.LightInfusion.escudo(level, quem, bateu);
        }
    }

    /** <b>Stupefy</b>: estonteia. */
    private static final class Stupefy extends ProjectileSymbol {
        /** Quanto a lentidão dura, e de que grau ela é. */
        public static final int DURA = 6000;
        public static final int QUANTA = 9;

        Stupefy(int id) {
            super(id, "stupefy", 5, false, false, 0);
            this.cor(0x0004FF).tamanho(1.5f);
        }

        @Override
        public void aoBater(ServerLevel level, @Nullable LivingEntity quem, HitResult onde, int grau) {
            if (!(onde instanceof EntityHitResult bateu)) return;
            if (!(bateu.getEntity() instanceof LivingEntity bicho)) return;
            if (bicho.hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS)) return;
            bicho.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.SLOWNESS, DURA, QUANTA));
        }
    }

    /** Põe um bloco ali, se houver lugar. */
    /**
     * <b>Attraho</b>: o avesso do Flipendo. Puxa para quem o lançou tudo o que for vivo à volta de onde a
     * bola bateu — dois blocos no grau um, três no dois, <b>nove</b> no três.
     *
     * <p>Com o Accio, que puxa o que está largado, ele faz o par completo: um traz as coisas, o outro traz
     * a gente.
     */
    private static final class Attraho extends ProjectileSymbol {
        Attraho(int id) {
            super(id, "attraho");
            this.cor(0x513666).tamanho(1.0f);
        }

        @Override
        public void aoBater(ServerLevel level, @Nullable LivingEntity quem, HitResult onde, int grau) {
            if (quem == null) return;
            double raio = grau == 1 ? 2.0 : (grau == 2 ? 3.0 : 9.0);
            emVolta(level, quem, onde, raio, bicho ->
                    net.thaumcraft.occulta.rite.Rites.PushCircle.puxa(bicho, quem.getX(),
                            quem.getY(), quem.getZ(), (int) Math.ceil(raio)));
        }
    }

    /**
     * <b>Avada Kedavra</b>: a primeira das três <b>imperdoáveis</b>, e a que faz o que o nome diz.
     *
     * <p>Em gente, <b>mata na hora</b> — e só onde houver briga entre jogadores ligada. Em bicho, duzentos
     * de dano no que pode ser escravizado, numa bruxa, num Ent ou num golem de até duzentos de vida;
     * <b>vinte e cinco</b> em tudo o resto. No criativo, mata qualquer coisa.
     *
     * <p>Ela custa <b>cento e um</b>, que é mais do que o cantil inteiro de uma infusão recém-feita: não é
     * um feitiço que se use, é um que se guarda.
     */
    private static final class AvadaKedavra extends ProjectileSymbol {
        /** O que ela faz a um bicho grande, e a um comum. */
        public static final float AOS_GRANDES = 200.0f;
        public static final float AOS_OUTROS = 25.0f;

        AvadaKedavra(int id) {
            super(id, "avadakedavra", 101, true, true, 0);
            this.cor(0x00FF00).tamanho(2.0f);
        }

        @Override
        public void aoBater(ServerLevel level, @Nullable LivingEntity quem, HitResult onde, int grau) {
            if (!(onde instanceof EntityHitResult bateu)
                    || !(bateu.getEntity() instanceof LivingEntity alvo)) {
                return;
            }
            var fonte = level.damageSources().magic();

            if (alvo instanceof ServerPlayer gente) {
                if (!(quem instanceof net.minecraft.world.entity.player.Player lançou)
                        || lançou.canHarmPlayer(gente)) {
                    gente.hurtServer(level, fonte, Float.MAX_VALUE);
                }
                return;
            }
            if (!(alvo instanceof net.minecraft.world.entity.Mob bicho)) return;

            if (quem instanceof net.minecraft.world.entity.player.Player lançou
                    && lançou.getAbilities().instabuild) {
                bicho.hurtServer(level, fonte, Float.MAX_VALUE);
                return;
            }

            boolean grande = net.thaumcraft.occulta.enslave.Enslavement.podeSerEscravizado(bicho)
                    || bicho instanceof net.minecraft.world.entity.monster.Witch
                    || bicho instanceof net.thaumcraft.occulta.EntEntity
                    || bicho instanceof net.minecraft.world.entity.animal.golem.AbstractGolem;
            bicho.hurtServer(level, fonte,
                    grande && bicho.getMaxHealth() <= AOS_GRANDES ? AOS_GRANDES : AOS_OUTROS);
        }
    }

    /**
     * <b>Carnosa Diem</b>: o feitiço que se lança <b>em si próprio</b>.
     *
     * <p>Tira um décimo da vida de quem o lança e devolve <b>dez de carga de infusão</b>. É a única coisa
     * do mod que troca vida por poder sem passar por ninguém — e é por isso que ela é maldição sem ser
     * imperdoável: não faz mal a mais ninguém.
     */
    private static final class CarnosaDiem extends Symbol {
        /** Quanto da vida ela tira, e quanta carga devolve. */
        public static final float TIRA = 0.1f;
        public static final int DÁ = 10;

        CarnosaDiem(int id) {
            super(id, "carnosadiem", 1, true, false, 0, "carnosadiem");
        }

        @Override
        public void lança(ServerLevel level, ServerPlayer quem, int grau) {
            float dói = quem.getMaxHealth() * TIRA;
            quem.hurtServer(level, level.damageSources().magic(), dói);
            net.thaumcraft.occulta.infusion.Infusions.enche(quem, DÁ);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.DUST_PLUME,
                    quem.getX(), quem.getY() + 1.0, quem.getZ(), 16, 0.5, 1.0, 0.5, 0.0);
            level.playSound(null, quem.blockPosition(),
                    net.minecraft.sounds.SoundEvents.ENDER_DRAGON_GROWL,
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }

    /**
     * <b>Cave Inimicum</b>: constrói parede.
     *
     * <p>Onde a bola bate, o que for mole <b>endurece</b>: terra, grama, micélio, pedregulho e tábua viram
     * <b>pedra</b>; tijolo de pedra vira <b>tijolo</b>; areia vira <b>arenito</b>; argila vira
     * <b>terracota</b>; e uma <b>porta de madeira</b> vira uma <b>porta de ferro</b>.
     *
     * <p>Num quadrado da face, que cresce com o grau: um bloco, três por três, cinco por cinco.
     */
    private static final class CaveInimicum extends ProjectileSymbol {
        CaveInimicum(int id) {
            super(id, "caveinimicum");
            this.cor(0x303030).tamanho(3.0f);
        }

        @Override
        public void aoBater(ServerLevel level, @Nullable LivingEntity quem, HitResult onde, int grau) {
            if (!(onde instanceof BlockHitResult bateu)) return;
            naFace(level, bateu, grau, ali -> {
                BlockState feitio = level.getBlockState(ali);
                Block vira = endurece(feitio);
                if (vira != null) level.setBlockAndUpdate(ali, vira.defaultBlockState());
            });
        }

        /** No que aquilo endurece, ou nada. */
        private static @Nullable Block endurece(BlockState feitio) {
            if (feitio.is(net.minecraft.tags.BlockTags.DIRT) || feitio.is(Blocks.GRASS_BLOCK)
                    || feitio.is(Blocks.MYCELIUM) || feitio.is(Blocks.COBBLESTONE)
                    || feitio.is(net.minecraft.tags.BlockTags.PLANKS)) {
                return Blocks.STONE;
            }
            if (feitio.is(Blocks.STONE_BRICKS)) return Blocks.BRICKS;
            if (feitio.is(net.minecraft.tags.BlockTags.SAND)) return Blocks.SANDSTONE;
            if (feitio.is(Blocks.CLAY)) return Blocks.TERRACOTTA;
            if (feitio.is(net.minecraft.tags.BlockTags.WOODEN_DOORS)) return Blocks.IRON_DOOR;
            return null;
        }
    }

    /**
     * <b>Crucio</b>: a segunda das <b>imperdoáveis</b>. Dói, e mais nada.
     *
     * <p>Em gente, <b>quatro mais quatro por grau</b> — doze no grau três. Em bicho, quatro sempre: ela
     * não foi feita para bichos.
     */
    private static final class Crucio extends ProjectileSymbol {
        /** O que ela faz de base, e o que cada grau soma. */
        public static final float DÓI = 4.0f;

        Crucio(int id) {
            super(id, "crucio", 5, true, true, 0);
            this.cor(0x66007F).tamanho(2.0f);
        }

        @Override
        public void aoBater(ServerLevel level, @Nullable LivingEntity quem, HitResult onde, int grau) {
            if (!(onde instanceof EntityHitResult bateu)
                    || !(bateu.getEntity() instanceof LivingEntity alvo)) {
                return;
            }
            var fonte = level.damageSources().magic();
            if (alvo instanceof ServerPlayer gente) {
                if (!(quem instanceof net.minecraft.world.entity.player.Player lançou)
                        || lançou.canHarmPlayer(gente)) {
                    gente.hurtServer(level, fonte, DÓI + DÓI * (grau - 1));
                }
            } else if (alvo instanceof net.minecraft.world.entity.Mob bicho) {
                bicho.hurtServer(level, fonte, DÓI);
            }
        }
    }

    /**
     * <b>Defodio</b>: cava.
     *
     * <p>O que for terra, grama, argila, areia, neve, gelo ou pedra <b>desaparece e cai em item</b>, num
     * quadrado da face que cresce com o grau. É o feitiço de minerar do mod, e custa três — que é o preço
     * de não ter de trazer picareta.
     */
    private static final class Defodio extends ProjectileSymbol {
        Defodio(int id) {
            super(id, "defodio", 3, false, false, 0);
            this.cor(0x3D291C).tamanho(2.5f);
        }

        @Override
        public void aoBater(ServerLevel level, @Nullable LivingEntity quem, HitResult onde, int grau) {
            if (!(onde instanceof BlockHitResult bateu)) return;
            naFace(level, bateu, grau, ali -> {
                if (!level.getBlockState(ali).is(CAVÁVEL)) return;
                level.destroyBlock(ali, true);
            });
        }
    }

    /**
     * <b>Flagrate</b>: risca um <b>glifo infernal</b> na face do bloco para onde se olha.
     *
     * <p>É o único feitiço do mod que <b>desenha</b>, e vale pelo que poupa: um giz infernal gasta-se a
     * cada risco, e um círculo de rito leva dezenas deles.
     */
    private static final class Flagrate extends Symbol {
        /** Até onde o olhar dele chega. */
        public static final double OLHAR = 4.0;

        Flagrate(int id) {
            super(id, "flagrate", 1, false, false, 0);
        }

        @Override
        public void lança(ServerLevel level, ServerPlayer quem, int grau) {
            var onde = net.thaumcraft.occulta.infusion.InfernalInfusion.olhaParaOPoder(level, quem);
            if (onde instanceof BlockHitResult bateu
                    && net.thaumcraft.occulta.ChalkItem.drawOn(level,
                            bateu.getBlockPos().relative(bateu.getDirection()),
                            net.thaumcraft.occulta.OccultaBlocks.INFERNAL_GLYPH)) {
                return;
            }
            net.thaumcraft.occulta.infusion.Infusion.falha(level, quem);
        }
    }

    /**
     * <b>Ignianima</b>: queima — e queima <b>mais quanto pior</b> estiver quem a lança.
     *
     * <p>É a única conta do mod inteiro que premeia estar quase morto: com a vida cheia são dois de dano, e
     * com a vida no fundo são seis mais metade do que falta. Quem a lança sabendo disso lança-a sangrando.
     *
     * <p>Em gente, o dano é ainda <b>multiplicado pela vida máxima</b> dela sobre vinte — de modo que um
     * jogador com coração reforçado apanha mais, e não menos.
     */
    private static final class Ignianima extends ProjectileSymbol {
        /** O raio em que ela queima. */
        public static final double RAIO = 1.5;

        Ignianima(int id) {
            super(id, "ignianima", 2, true, false, 0, "ignianima");
            this.cor(0xFFE060).tamanho(3.0f);
        }

        @Override
        public void aoBater(ServerLevel level, @Nullable LivingEntity quem, HitResult onde, int grau) {
            Vec3 meio = ondeBateu(onde);
            AABB caixa = new AABB(meio.x - RAIO, meio.y - RAIO, meio.z - RAIO,
                    meio.x + RAIO, meio.y + RAIO, meio.z + RAIO);
            float base = quanto(quem);
            for (LivingEntity bicho : level.getEntitiesOfClass(LivingEntity.class, caixa)) {
                if (bicho.distanceToSqr(meio) > RAIO * RAIO) continue;
                float vezes = bicho instanceof net.minecraft.world.entity.player.Player
                        ? bicho.getMaxHealth() / 20.0f : 1.0f;
                bicho.hurtServer(level, level.damageSources().magic(), base * vezes);
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME,
                        bicho.getX(), bicho.getY() + 1.0, bicho.getZ(), 16, 0.5, 1.0, 0.5, 0.0);
            }
        }

        /** A escada do original: quanto pior a vida de quem lança, mais ela dói. */
        public static float quanto(@Nullable LivingEntity quem) {
            if (quem == null) return 4.0f;
            float vida = 20.0f * (quem.getHealth() / quem.getMaxHealth());
            if (vida > 19.0f) return 2.0f;
            if (vida > 15.0f) return 3.0f;
            if (vida > 10.0f) return 5.0f;
            return 6.0f + (12.0f - vida) / 2.0f;
        }
    }

    /**
     * <b>Imperio</b>: a terceira das <b>imperdoáveis</b>. <b>Escraviza</b> o que a bola acertar.
     *
     * <p>É o mesmo escravizar da Infusão Infernal — e é por isso que as três imperdoáveis só se lançam com
     * ela. Custa dez, que é o dobro do soco agachado, e é o preço de o fazer de longe.
     */
    private static final class Imperio extends ProjectileSymbol {
        Imperio(int id) {
            super(id, "imperio", 10, true, true, 0);
            this.cor(0xA30AFF).tamanho(1.5f);
        }

        @Override
        public void aoBater(ServerLevel level, @Nullable LivingEntity quem, HitResult onde, int grau) {
            if (!(quem instanceof net.minecraft.world.entity.player.Player lançou)) return;
            if (!(onde instanceof EntityHitResult bateu)
                    || !(bateu.getEntity() instanceof net.minecraft.world.entity.Mob bicho)) {
                return;
            }
            if (!net.thaumcraft.occulta.enslave.Enslavement.escraviza(bicho, lançou)) return;
            level.sendParticles(net.minecraft.core.particles.SpellParticleOption.create(
                            net.minecraft.core.particles.ParticleTypes.EFFECT, 1.0f, 1.0f, 1.0f, 1.0f),
                    bicho.getX(), bicho.getY() + 1.0, bicho.getZ(), 16, 0.5, 1.0, 0.5, 0.0);
            level.playSound(null, bicho.blockPosition(),
                    net.minecraft.sounds.SoundEvents.ZOMBIE_INFECT,
                    net.minecraft.sounds.SoundSource.HOSTILE, 1.0f, 1.0f);
        }
    }

    /**
     * <b>Meteolojinx Recanto</b>: <b>para a chuva</b>.
     *
     * <p>Custa <b>cem</b>, que é o dobro do Nox e o segundo preço mais alto do mod — e não há nele poder
     * nenhum: ele só muda o tempo. É o original dizendo o que vale um dia de sol.
     *
     * <p>Sem chuva, ele toca o tambor e não gasta nada.
     */
    private static final class MeteolojinxRecanto extends Symbol {
        MeteolojinxRecanto(int id) {
            super(id, "meteolojinxrecanto", 100, false, false, 0);
        }

        @Override
        public void lança(ServerLevel level, ServerPlayer quem, int grau) {
            if (!level.isRaining() && !level.isThundering()) {
                net.thaumcraft.occulta.infusion.Infusion.falha(level, quem);
                return;
            }
            // no jogo de hoje o tempo mora num guardado à parte, e não no mundo
            var tempo = level.getWeatherData();
            tempo.setClearWeatherTime(0);
            tempo.setRainTime(0);
            tempo.setRaining(false);
            tempo.setThundering(false);
        }
    }

    private static void põeSeCouber(ServerLevel level, BlockPos onde,
                                    net.minecraft.world.level.block.Block oquê) {
        var estava = level.getBlockState(onde);
        if (!estava.isAir() && !estava.canBeReplaced()) return;
        if (oquê instanceof BaseFireBlock && !level.getBlockState(onde.below()).isSolidRender()) return;
        level.setBlockAndUpdate(onde, oquê.defaultBlockState());
    }
}
