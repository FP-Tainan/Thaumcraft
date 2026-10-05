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
 * <p><b>Catorze</b> dos trinta e um símbolos do original. Onze deles <b>atiram uma bola</b> — Accio,
 * Aguamenti, Alohomora, Confundus, Ennervate, Expelliarmus, Flipendo, Impedimenta, Incendio, Lumos e
 * Stupefy — e três agem <b>a partir de quem os lança</b>: Episkey, Protego e Nox.
 *
 * <p>Os outros dezessete entram nas fatias seguintes: eles pedem coisas que ainda não estão portadas — as
 * portas do ofício, o Tormento, o Leonard, as maldições imperdoáveis. Os números e os desenhos deles já
 * estão levantados do original, traço por traço.
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
         * <b>Alohomora</b>: abre ou fecha a porta em que a bola bate. É o feitiço mais pequeno do mod —
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
         * <p>E a bola dele <b>cai</b>: ela não voa a direito, vai descendo. É o original a dizer que quem
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
    private static void põeSeCouber(ServerLevel level, BlockPos onde,
                                    net.minecraft.world.level.block.Block oquê) {
        var estava = level.getBlockState(onde);
        if (!estava.isAir() && !estava.canBeReplaced()) return;
        if (oquê instanceof BaseFireBlock && !level.getBlockState(onde.below()).isSolidRender()) return;
        level.setBlockAndUpdate(onde, oquê.defaultBlockState());
    }
}
