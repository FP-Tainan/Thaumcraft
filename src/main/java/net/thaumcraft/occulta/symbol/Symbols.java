package net.thaumcraft.occulta.symbol;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.BaseFireBlock;
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
 * <p><b>Seis</b> dos trinta e um símbolos do original, escolhidos para pôr a máquina inteira de pé: cinco
 * que atiram uma bola — <b>Accio</b>, <b>Aguamenti</b>, <b>Incendio</b>, <b>Flipendo</b> e <b>Lumos</b> — e
 * um que não atira nada e age à volta de quem o lança, o <b>Nox</b>.
 *
 * <p>Os outros vinte e cinco entram nas fatias seguintes. Os números e os desenhos deles já estão
 * levantados do original, traço por traço.
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

    private static String chave(byte[] traços) {
        StringBuilder feito = new StringBuilder();
        for (int volta = 0; volta < traços.length; volta++) {
            if (volta > 0) feito.append(',');
            feito.append(traços[volta]);
        }
        return feito.toString();
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
    /** Põe um bloco ali, se houver lugar. */
    private static void põeSeCouber(ServerLevel level, BlockPos onde,
                                    net.minecraft.world.level.block.Block oquê) {
        var estava = level.getBlockState(onde);
        if (!estava.isAir() && !estava.canBeReplaced()) return;
        if (oquê instanceof BaseFireBlock && !level.getBlockState(onde.below()).isSolidRender()) return;
        level.setBlockAndUpdate(onde, oquê.defaultBlockState());
    }
}
