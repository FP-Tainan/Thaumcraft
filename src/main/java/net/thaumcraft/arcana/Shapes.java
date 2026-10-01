package net.thaumcraft.arcana;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * As Formas: as {@code am2.spell.shapes} do Ars Magica 2.
 *
 * <p>Cada uma acha o que a etapa atinge e, tendo achado, <b>passa a frase adiante</b>: tira a etapa da frente e
 * lança o resto. É por isso que um Projétil seguido de um Toque funciona — o projétil que bate acorda o toque.
 */
public final class Shapes {
    private Shapes() {
    }

    /**
     * <b>Autoconjuração</b>: o {@code Self}.
     *
     * <p>A mais simples e a mais barata — metade do custo. O alvo é quem lançou, e mais ninguém.
     */
    public static final SpellPart.Shape SELF = SpellParts.shape(new SpellPart.Shape() {
        @Override
        public String name() {
            return "self";
        }

        @Override
        public float manaMultiplier() {
            return 0.5f;
        }

        @Override
        public SpellCast.Result begin(ServerLevel level, Spell feitiço, LivingEntity quem,
                                      @Nullable Entity alvo, Vec3 onde) {
            SpellCast.Result saiu = SpellCast.onEntity(level, feitiço, quem, quem);
            if (!saiu.ok()) return saiu;
            return SpellCast.cast(level, feitiço.pop(), quem, alvo, onde);
        }
    });

    /**
     * <b>Toque</b>: o {@code Touch}.
     *
     * <p>Ela olha para onde o mago está olhando, a dois blocos e meio, e pega no primeiro que achar — bicho ou
     * bloco. Tendo um alvo já dado, usa esse.
     */
    public static final SpellPart.Shape TOUCH = SpellParts.shape(new SpellPart.Shape() {
        /** Até onde a mão chega: as dois blocos e meio do original. */
        public static final double REACH = 2.5;

        @Override
        public String name() {
            return "touch";
        }

        @Override
        public SpellCast.Result begin(ServerLevel level, Spell feitiço, LivingEntity quem,
                                      @Nullable Entity alvo, Vec3 onde) {
            if (alvo != null) {
                SpellCast.Result saiu = SpellCast.onEntity(level, feitiço, quem, alvo);
                if (!saiu.ok()) return saiu;
                return SpellCast.cast(level, feitiço.pop(), quem, alvo, onde);
            }

            HitResult bateu = look(level, quem, REACH, feitiço.has(SpellModifierKind.TARGET_NONSOLID_BLOCKS));
            if (bateu == null) return SpellCast.Result.EFFECT_FAILED;

            if (bateu instanceof EntityHitResult nele) {
                SpellCast.Result saiu = SpellCast.onEntity(level, feitiço, quem, nele.getEntity());
                if (!saiu.ok()) return saiu;
                return SpellCast.cast(level, feitiço.pop(), quem, nele.getEntity(), bateu.getLocation());
            }

            BlockHitResult nisso = (BlockHitResult) bateu;
            SpellCast.Result saiu = SpellCast.onBlock(level, feitiço, quem, nisso.getBlockPos(),
                    nisso.getDirection(), nisso.getLocation());
            if (!saiu.ok()) return saiu;
            return SpellCast.cast(level, feitiço.pop(), quem, null, bateu.getLocation());
        }
    });

    /**
     * <b>Área</b>: o {@code AoE}.
     *
     * <p>Ela não procura nada: pega em <b>tudo o que estiver em roda</b> do ponto, a um raio que os
     * modificadores mexem. É a Forma que se usa quando o que importa é o lugar e não o alvo.
     */
    public static final SpellPart.Shape AOE = SpellParts.shape(new SpellPart.Shape() {
        /** O raio de fábrica dela: as três blocos do original. */
        public static final double BASE_RADIUS = 3.0;

        @Override
        public String name() {
            return "aoe";
        }

        @Override
        public float manaMultiplier() {
            return 1.5f;
        }

        @Override
        public SpellCast.Result begin(ServerLevel level, Spell feitiço, LivingEntity quem,
                                      @Nullable Entity alvo, Vec3 onde) {
            double raio = feitiço.mul(level, SpellModifierKind.RADIUS, BASE_RADIUS);
            var roda = new net.minecraft.world.phys.AABB(onde, onde).inflate(raio);

            boolean pegou = false;
            for (Entity quem2 : level.getEntities(quem, roda, e -> e.distanceToSqr(onde) <= raio * raio)) {
                if (SpellCast.onEntity(level, feitiço, quem, quem2).ok()) pegou = true;
            }

            BlockPos casa = BlockPos.containing(onde);
            if (SpellCast.onBlock(level, feitiço, quem, casa,
                    net.minecraft.core.Direction.UP, onde).ok()) {
                pegou = true;
            }
            if (!pegou) return SpellCast.Result.EFFECT_FAILED;
            return SpellCast.cast(level, feitiço.pop(), quem, alvo, onde);
        }
    });

    /**
     * <b>Projétil</b>: o {@code Projectile}, e a Forma que define o Ars Magica 2 para quem o joga.
     *
     * <p>Ela não procura alvo nenhum: <b>atira</b>. O feitiço inteiro entra numa entidade que voa e que, ao
     * bater, corre as Essências desta etapa e lança dali o que sobra da frase. É a Forma que faz o projétil
     * seguido de Área explodir no sítio da batida.
     *
     * <p>Ela lê cinco modificadores: <b>Velocidade</b> (multiplica), <b>Gravidade</b> (soma — e o valor dela é
     * negativo, que é o que faz cair), <b>Ricochete</b> e <b>Perfuração</b> (somam) e <b>Alvos Não Sólidos</b>.
     * Sem nenhum deles, o projétil sai a um bloco por batida, a direito, e morre no primeiro que pegar.
     *
     * <p>Ela <b>dá sempre por boa</b>: lançar um projétil custa mana mesmo que ele nunca venha a bater em nada,
     * porque o que pegou foi o atirar. É o que o original faz ao devolver {@code SUCCESS} sem olhar para nada.
     */
    public static final SpellPart.Shape PROJECTILE = SpellParts.shape(new SpellPart.Shape() {
        @Override
        public String name() {
            return "projectile";
        }

        @Override
        public float manaMultiplier() {
            return 1.25f;
        }

        @Override
        public SpellCast.Result begin(ServerLevel level, Spell feitiço, LivingEntity quem,
                                      @Nullable Entity alvo, Vec3 onde) {
            double velocidade = feitiço.mul(level, SpellModifierKind.SPEED, SpellModifierKind.SPEED.base);
            var voa = new SpellProjectileEntity(level, quem, feitiço, velocidade);
            voa.setGravity(feitiço.add(level, SpellModifierKind.GRAVITY, SpellModifierKind.GRAVITY.base));
            voa.setBounces((int) feitiço.add(level, SpellModifierKind.BOUNCE, SpellModifierKind.BOUNCE.base));
            voa.setPierces((int) feitiço.add(level, SpellModifierKind.PIERCING, 0.0));
            voa.setTargetNonSolid(feitiço.has(SpellModifierKind.TARGET_NONSOLID_BLOCKS));
            level.addFreshEntity(voa);
            return SpellCast.Result.SUCCESS;
        }
    });

    /**
     * O <b>Canal</b>: a Autoconjuração que se <b>segura</b>.
     *
     * <p>Ela corre a etapa em quem a lança, como a Autoconjuração — mas só <b>de dez em dez batidas</b>, e
     * enquanto o botão estiver apertado. É a Forma de quem quer um efeito contínuo e barato em vez de um caro
     * de uma vez: uma Telecinese canalizada varre o chão enquanto se anda.
     *
     * <p>E há uma exceção do original: com a <b>Telecinese</b> ou o <b>Atrair</b> na frase, ela corre <b>a
     * cada batida</b>. As duas são essências que puxam coisas devagar, e de dez em dez batidas elas quase não
     * se notariam.
     */
    public static final SpellPart.Shape CHANNEL = SpellParts.shape(new SpellPart.Shape() {
        /** De dez em dez batidas, como no original. */
        public static final int EVERY = 10;

        @Override
        public String name() {
            return "channel";
        }

        @Override
        public boolean channeled() {
            return true;
        }

        @Override
        public boolean terminus() {
            return true;
        }

        @Override
        public SpellCast.Result begin(ServerLevel level, Spell feitiço, LivingEntity quem,
                                      @Nullable Entity alvo, Vec3 onde) {
            return this.begin(level, feitiço, quem, alvo, onde, 0);
        }

        @Override
        public SpellCast.Result begin(ServerLevel level, Spell feitiço, LivingEntity quem,
                                      @Nullable Entity alvo, Vec3 onde, int batidas) {
            Spell.Stage etapa = feitiço.first();
            if (etapa == null) return SpellCast.Result.MALFORMED;

            boolean depressa = etapa.essences().contains(Essences.TELEKINESIS)
                    || etapa.essences().contains(Essences.ATTRACT);
            if (!depressa && batidas % EVERY != 0) return SpellCast.Result.EFFECT_FAILED;

            return SpellCast.onEntity(level, feitiço, quem, quem);
        }
    });

    /** Sem uso fora do porte: obriga a classe a ser carregada, e com ela as Formas a se registrarem. */
    public static void init() {
    }

    /**
     * <b>Corrente</b>: o {@code Chain}, o feitiço que <b>salta de um alvo para o seguinte</b>.
     *
     * <p>Ela pega em quem o mago está olhando e, dali, procura o vivo mais perto que ainda não tenha sido
     * pego — e outra vez, e outra, até três. Cada salto alcança quatro blocos.
     *
     * <p>E em cada um dos alvos ela faz <b>duas coisas</b>: manda as Essências desta etapa <i>e</i> lança o
     * que sobra da frase dali. Uma Corrente seguida de uma Área abre uma Área em cada bicho da corrente.
     *
     * <p><b>Quem lançou nunca entra na corrente.</b> É o original, e é o que a torna segura de usar no meio
     * de uma briga.
     */
    public static final SpellPart.Shape CHAIN = SpellParts.shape(new SpellPart.Shape() {
        /** O quanto cada salto alcança: os quatro blocos do original. */
        public static final double BASE_RANGE = 4.0;
        /** E quantos alvos a corrente pega: os três. */
        public static final int BASE_TARGETS = 3;
        /** Até onde o mago pode estar olhando para começar a corrente. */
        public static final double LOOK = 8.0;

        @Override
        public String name() {
            return "chain";
        }

        @Override
        public float manaMultiplier() {
            return 1.5f;
        }

        @Override
        public SpellCast.Result begin(ServerLevel level, Spell feitiço, LivingEntity quem,
                                      @Nullable Entity alvo, Vec3 onde) {
            double salto = feitiço.mul(level, SpellModifierKind.RANGE, BASE_RANGE);
            int quantos = (int) feitiço.add(level, SpellModifierKind.PROCS, BASE_TARGETS);

            // o primeiro é quem já estava apontado, ou quem o mago está olhando
            Entity primeiro = alvo;
            if (primeiro == null) {
                HitResult bateu = look(level, quem, LOOK,
                        feitiço.has(SpellModifierKind.TARGET_NONSOLID_BLOCKS));
                if (bateu instanceof EntityHitResult nele) primeiro = nele.getEntity();
            }
            if (!(primeiro instanceof LivingEntity)) return SpellCast.Result.EFFECT_FAILED;

            var corrente = new java.util.ArrayList<Entity>();
            Entity atual = primeiro;
            while (atual != null && corrente.size() < quantos) {
                corrente.add(atual);
                atual = maisPerto(level, atual, quem, corrente, salto);
            }

            Spell sobra = feitiço.pop();
            boolean pegou = false;
            Entity anterior = null;
            for (Entity nele : corrente) {
                if (nele == quem) continue;

                // o facho de um elo ao seguinte; o primeiro parte de quem lançou
                Vec3 daqui = anterior == null
                        ? quem.position().add(0.0, quem.getBbHeight() / 2.0, 0.0)
                        : anterior.position().add(0.0, anterior.getBbHeight() / 2.0, 0.0);
                SpellFx.chain(level, feitiço, daqui,
                        nele.position().add(0.0, nele.getBbHeight() / 2.0, 0.0));
                anterior = nele;

                if (SpellCast.onEntity(level, feitiço, quem, nele).ok()) pegou = true;
                SpellCast.cast(level, sobra, quem, nele, nele.position());
            }
            return pegou ? SpellCast.Result.SUCCESS : SpellCast.Result.EFFECT_FAILED;
        }
    });

    /**
     * O vivo mais perto daquele que ainda não está na corrente, e que não é quem lançou.
     *
     * <p>É o laço do {@code Chain}: de cada elo, o próximo é o mais perto que sobrou.
     */
    private static @Nullable Entity maisPerto(ServerLevel level, Entity de, LivingEntity quem,
                                              java.util.List<Entity> já, double alcance) {
        Entity achado = null;
        double perto = Double.MAX_VALUE;
        for (Entity outro : level.getEntitiesOfClass(LivingEntity.class,
                de.getBoundingBox().inflate(alcance))) {
            if (outro == quem || já.contains(outro)) continue;
            double d = outro.distanceToSqr(de);
            if (d < perto) {
                perto = d;
                achado = outro;
            }
        }
        return achado;
    }

    /**
     * <b>Facho</b>: o {@code Beam}, a única Forma que se <b>segura</b> em vez de se lançar.
     *
     * <p>Enquanto o botão estiver preso, ela aponta para onde o mago olha e corre de novo a cada batida —
     * mas só <b>fere de dez em dez</b>. É o {@code useCount % 10} do original, e é o que separa o facho de um
     * moedor: ele queima devagar e sem parar, e não tudo de uma vez.
     *
     * <p>Por isso ela custa a <b>décima parte</b> de uma Forma comum: o preço é por batida, e ao fim de dez
     * batidas somou o de um feitiço inteiro. Segurar um facho é gastar mana o tempo todo.
     */
    public static final SpellPart.Shape BEAM = SpellParts.shape(new SpellPart.Shape() {
        /** De quantas em quantas batidas ele fere. */
        public static final int EVERY = 10;

        @Override
        public String name() {
            return "beam";
        }

        @Override
        public float manaMultiplier() {
            return 0.1f;
        }

        @Override
        public boolean channeled() {
            return true;
        }

        @Override
        public SpellCast.Result begin(ServerLevel level, Spell feitiço, LivingEntity quem,
                                      @Nullable Entity alvo, Vec3 onde) {
            return this.begin(level, feitiço, quem, alvo, onde, 0);
        }

        @Override
        public SpellCast.Result begin(ServerLevel level, Spell feitiço, LivingEntity quem,
                                      @Nullable Entity alvo, Vec3 onde, int batidas) {
            double alcance = feitiço.add(level, SpellModifierKind.RANGE, SpellModifierKind.RANGE.base);
            HitResult bateu = look(level, quem, alcance,
                    feitiço.has(SpellModifierKind.TARGET_NONSOLID_BLOCKS));

            // o facho existe todo tique, mas só dói de dez em dez
            boolean dói = batidas % EVERY == 0;
            if (!dói) return SpellCast.Result.SUCCESS;
            if (bateu == null) return SpellCast.Result.EFFECT_FAILED;

            if (bateu instanceof EntityHitResult nele) {
                SpellCast.Result saiu = SpellCast.onEntity(level, feitiço, quem, nele.getEntity());
                if (!saiu.ok()) return saiu;
                return SpellCast.cast(level, feitiço.pop(), quem, nele.getEntity(), bateu.getLocation());
            }

            BlockHitResult nisso = (BlockHitResult) bateu;
            SpellCast.Result saiu = SpellCast.onBlock(level, feitiço, quem, nisso.getBlockPos(),
                    nisso.getDirection(), nisso.getLocation());
            if (!saiu.ok()) return saiu;
            return SpellCast.cast(level, feitiço.pop(), quem, null, bateu.getLocation());
        }
    });

    // ------------------------------------------------------------------ as que ficam

    /**
     * As três Formas que criam área são <b>principum</b>: elas pedem outra Forma depois.
     *
     * <p>Uma Zona sozinha não faz nada — ela cria o lugar, e quem faz alguma coisa é a frase que vem a
     * seguir. No original, quem impede de escrever uma Zona no fim de uma frase é a Mesa de Inscrição; aqui,
     * que ainda não a tem, quem impede é isto.
     *
     * <p><b>Desvio declarado:</b> o original deixa lançar e cobra a mana de um feitiço que não faz nada. Aqui
     * a frase é recusada como malformada, de graça. É uma armadilha a menos e nenhuma perda.
     */
    private static @Nullable SpellCast.Result precisaDeMais(Spell feitiço) {
        return feitiço.pop().isEmpty() ? SpellCast.Result.MALFORMED : null;
    }

    /**
     * <b>Zona</b>: o {@code Zone}, um disco parado que corre a frase de segundo em segundo.
     *
     * <p>Dois blocos de raio, cinco segundos de vida, e a cada segundo ela manda as Essências do que sobrou
     * em quem estiver dentro <b>e</b> lança o que sobrou dali. É a Forma de quem quer segurar um corredor.
     *
     * <p>Ela é a mais cara das que atingem alguma coisa: <b>quatro vezes e meia</b>. Só as Contingências,
     * que não atingem nada e só esperam, custam mais.
     */
    public static final SpellPart.Shape ZONE = SpellParts.shape(new SpellPart.Shape() {
        /** O raio de fábrica: os dois blocos do original. */
        public static final double BASE_RADIUS = 2.0;
        /** E quanto ela dura: as 100 batidas. */
        public static final int BASE_LIFE = 100;

        @Override
        public String name() {
            return "zone";
        }

        @Override
        public float manaMultiplier() {
            return 4.5f;
        }

        @Override
        public boolean principum() {
            return true;
        }

        @Override
        public SpellCast.Result begin(ServerLevel level, Spell feitiço, LivingEntity quem,
                                      @Nullable Entity alvo, Vec3 onde) {
            SpellCast.Result mal = precisaDeMais(feitiço);
            if (mal != null) return mal;

            var área = new SpellEffectEntity(level, quem, feitiço.pop(), SpellEffectEntity.Kind.ZONE);
            área.setRadius((float) feitiço.add(level, SpellModifierKind.RADIUS, BASE_RADIUS));
            área.setGravity(feitiço.add(level, SpellModifierKind.GRAVITY, 0.0));
            área.setLife((int) feitiço.mul(level, SpellModifierKind.DURATION, BASE_LIFE));
            área.snapTo(onde.x, onde.y, onde.z, quem.getYRot(), 0.0f);
            level.addFreshEntity(área);
            return SpellCast.Result.SUCCESS;
        }
    });

    /**
     * <b>Parede</b>: o {@code Wall}, uma linha atravessada no caminho de quem vem.
     *
     * <p>Três blocos de raio para cada lado — seis de ponta a ponta —, cinco segundos, e ela corre a frase em
     * quem a cruzar. Ela nasce <b>atravessada ao olhar</b> de quem a lançou, que é o que a põe no caminho e
     * não ao longo dele.
     *
     * <p>Repare que o raio dela <b>multiplica</b> e o da Zona <b>soma</b>. É o original, e é o que faz o
     * modificador de Raio — que encolhe — apertar muito mais uma Parede do que uma Zona.
     */
    public static final SpellPart.Shape WALL = SpellParts.shape(new SpellPart.Shape() {
        public static final double BASE_RADIUS = 3.0;
        public static final int BASE_LIFE = 100;

        @Override
        public String name() {
            return "wall";
        }

        @Override
        public float manaMultiplier() {
            return 2.5f;
        }

        @Override
        public boolean principum() {
            return true;
        }

        @Override
        public SpellCast.Result begin(ServerLevel level, Spell feitiço, LivingEntity quem,
                                      @Nullable Entity alvo, Vec3 onde) {
            SpellCast.Result mal = precisaDeMais(feitiço);
            if (mal != null) return mal;

            var área = new SpellEffectEntity(level, quem, feitiço.pop(), SpellEffectEntity.Kind.WALL);
            área.setRadius((float) feitiço.mul(level, SpellModifierKind.RADIUS, BASE_RADIUS));
            área.setGravity(feitiço.add(level, SpellModifierKind.GRAVITY, 0.0));
            área.setLife((int) feitiço.mul(level, SpellModifierKind.DURATION, BASE_LIFE));
            área.snapTo(onde.x, onde.y, onde.z, quem.getYRot(), 0.0f);
            área.setWall(quem.getYRot());
            level.addFreshEntity(área);
            return SpellCast.Result.SUCCESS;
        }
    });

    /**
     * <b>Onda</b>: o {@code Wave}, a mesma Parede <b>andando para a frente</b>.
     *
     * <p>Um bloco de raio, um segundo de vida, meio bloco por batida. Ela é curta e rápida de propósito: o
     * que ela faz não é segurar um lugar, é <b>varrer</b> um. E é a única das três que mexe no mundo — ela
     * corre a frase em cada bloco por onde passa, e é por isso que uma Onda de Escavar abre uma vala.
     *
     * <p>A Perfuração aqui não faz o que o nome diz: ela deixa a Onda <b>atravessar paredes</b>. E cada
     * Gravidade posta faz a Onda descer <b>meio bloco</b> por batida, o que a manda escada abaixo.
     */
    public static final SpellPart.Shape WAVE = SpellParts.shape(new SpellPart.Shape() {
        public static final double BASE_RADIUS = 1.0;
        public static final int BASE_LIFE = 20;
        /** A velocidade dela é a do modificador, pela metade. */
        public static final double SPEED_FACTOR = 0.5;

        @Override
        public String name() {
            return "wave";
        }

        @Override
        public float manaMultiplier() {
            return 3.0f;
        }

        @Override
        public boolean principum() {
            return true;
        }

        @Override
        public SpellCast.Result begin(ServerLevel level, Spell feitiço, LivingEntity quem,
                                      @Nullable Entity alvo, Vec3 onde) {
            SpellCast.Result mal = precisaDeMais(feitiço);
            if (mal != null) return mal;

            var área = new SpellEffectEntity(level, quem, feitiço.pop(), SpellEffectEntity.Kind.WAVE);
            área.setRadius((float) feitiço.add(level, SpellModifierKind.RADIUS, BASE_RADIUS));
            área.setLife((int) feitiço.mul(level, SpellModifierKind.DURATION, BASE_LIFE));
            área.noPhysics = feitiço.has(SpellModifierKind.PIERCING);

            // cada Gravidade posta faz a Onda descer meio bloco por batida
            int gravidades = feitiço.count(SpellModifierKind.GRAVITY);
            área.setGravity(-gravidades * 0.5);

            área.snapTo(onde.x, onde.y + 1.0, onde.z, quem.getYRot(), 0.0f);
            área.setWave(quem.getYRot(), feitiço.add(level, SpellModifierKind.SPEED, 1.0) * SPEED_FACTOR);
            level.addFreshEntity(área);
            return SpellCast.Result.SUCCESS;
        }
    });

    /**
     * <b>Runa</b>: o {@code Rune}, o feitiço que se deixa no chão à espera de quem pise nele.
     *
     * <p>Ela é <b>principum</b> como as três de área, e pela mesma razão: o que fica desenhado no chão é o
     * <i>resto</i> da frase, e é ele que corre quando alguém pisa. Uma Runa sozinha não é armadilha nenhuma.
     *
     * <p>Quantas vezes ela aguenta, di-lo o modificador de <b>Repetições</b>: uma só, se não houver nenhum.
     * E <b>quem a pôs não a dispara</b>, o que é o que a torna usável para guardar uma porta.
     *
     * <p>Ela nasce <b>em cima</b> do bloco que o mago está olhando, e só se ele estiver olhando para um bloco
     * — apontar para um bicho não deixa runa nenhuma.
     */
    public static final SpellPart.Shape RUNE = SpellParts.shape(new SpellPart.Shape() {
        /** Quantas vezes ela aguenta de fábrica: a uma do original. */
        public static final int BASE_TRIGGERS = 1;
        /** Até onde o mago pode estar olhando para a deixar. */
        public static final double LOOK = 8.0;

        @Override
        public String name() {
            return "rune";
        }

        @Override
        public float manaMultiplier() {
            return 1.1f;
        }

        @Override
        public boolean principum() {
            return true;
        }

        @Override
        public SpellCast.Result begin(ServerLevel level, Spell feitiço, LivingEntity quem,
                                      @Nullable Entity alvo, Vec3 onde) {
            SpellCast.Result mal = precisaDeMais(feitiço);
            if (mal != null) return mal;

            HitResult bateu = look(level, quem, LOOK,
                    feitiço.has(SpellModifierKind.TARGET_NONSOLID_BLOCKS));
            if (!(bateu instanceof BlockHitResult nisso)) return SpellCast.Result.EFFECT_FAILED;

            BlockPos casa = nisso.getBlockPos().above();
            var runa = ArcanaBlocks.SPELL_RUNE.defaultBlockState()
                    .setValue(SpellRuneBlock.AFFINITY, feitiço.mainAffinity().ordinal());
            if (!runa.canSurvive(level, casa)) return SpellCast.Result.EFFECT_FAILED;
            if (!level.getBlockState(casa).canBeReplaced()) return SpellCast.Result.EFFECT_FAILED;

            level.setBlockAndUpdate(casa, runa);
            if (!(level.getBlockEntity(casa) instanceof SpellRuneBlockEntity guarda)) {
                return SpellCast.Result.EFFECT_FAILED;
            }
            guarda.setSpell(feitiço.pop());
            guarda.setTriggers((int) feitiço.add(level, SpellModifierKind.PROCS, BASE_TRIGGERS));
            guarda.setPlacedBy(quem);
            return SpellCast.Result.SUCCESS;
        }
    });

    // ------------------------------------------------------------------ a que vira ferramenta

    /**
     * O <b>Vínculo</b>: o {@code Binding} do Ars Magica 2, e a última das quinze Formas.
     *
     * <p>Ela não lança nada. O que ela faz é <b>trocar o feitiço na mão por uma ferramenta</b> — e essa
     * ferramenta custa mana a cada batida para se manter, e volta a ser o feitiço quando a mana acaba.
     *
     * <p>O feitiço vai <b>dentro</b> da ferramenta, e é por isso que desfazer e refazer não perde a frase.
     *
     * <p><b>Desvio declarado.</b> No original há <b>uma</b> Forma de Vínculo, e qual ferramenta ela faz sai
     * de um número guardado no feitiço, escolhido na Mesa de Inscrição. Este porte não tem números guardados
     * nas peças — cada peça é um item —, então <b>cada ferramenta é a sua própria Forma</b>. Escolher a peça
     * é escolher a ferramenta, que é como tudo o mais funciona aqui.
     */
    private static SpellPart.Shape binding(BoundToolItem.Kind qual) {
        return SpellParts.shape(new SpellPart.Shape() {
            @Override
            public String name() {
                return "binding_" + qual.name().toLowerCase(java.util.Locale.ROOT);
            }

            @Override
            public float manaMultiplier() {
                return 1.0f;
            }

            @Override
            public SpellCast.Result begin(ServerLevel level, Spell feitiço, LivingEntity quem,
                                          @Nullable Entity alvo, Vec3 onde) {
                if (!(quem instanceof net.minecraft.world.entity.player.Player gente)) {
                    return SpellCast.Result.EFFECT_FAILED;
                }

                // ela só vale sobre o próprio feitiço que a lançou, e na mão
                var naMão = gente.getMainHandItem();
                if (!(naMão.getItem() instanceof SpellItem)) return SpellCast.Result.EFFECT_FAILED;

                var item = ArcanaItems.BOUND.get(qual);
                if (item == null) return SpellCast.Result.EFFECT_FAILED;

                var ferramenta = new net.minecraft.world.item.ItemStack(item);
                ferramenta.set(ArcanaComponents.SPELL, feitiço);
                gente.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ferramenta);
                return SpellCast.Result.SUCCESS;
            }
        });
    }

    /** <b>Vínculo: Picareta</b>, de diamante. Um inteiro de mana por batida. */
    public static final SpellPart.Shape BINDING_PICKAXE = binding(BoundToolItem.Kind.PICKAXE);

    /** <b>Vínculo: Machado</b>, de diamante. */
    public static final SpellPart.Shape BINDING_AXE = binding(BoundToolItem.Kind.AXE);

    /** <b>Vínculo: Espada</b>, de diamante. */
    public static final SpellPart.Shape BINDING_SWORD = binding(BoundToolItem.Kind.SWORD);

    /** <b>Vínculo: Pá</b>, de ferro. Quatro décimos por batida. */
    public static final SpellPart.Shape BINDING_SHOVEL = binding(BoundToolItem.Kind.SHOVEL);

    /** <b>Vínculo: Enxada</b>, de pedra. Um décimo por batida, a mais barata de manter. */
    public static final SpellPart.Shape BINDING_HOE = binding(BoundToolItem.Kind.HOE);

    /** <b>Vínculo: Arco</b>, de ferro. O único que atira em vez de cavar ou bater. */
    public static final SpellPart.Shape BINDING_BOW = binding(BoundToolItem.Kind.BOW);

    // ------------------------------------------------------------------ as que esperam

    /**
     * As cinco <b>Contingências</b>: as {@code Contingency_*} do Ars Magica 2.
     *
     * <p>Elas são a única coisa do ramo que corre <b>sozinha</b>. Lançar uma não faz nada de visível: ela
     * escreve a frase dentro de quem a levou e ali fica, calada, até acontecer a coisa que espera.
     *
     * <p>Todas são <b>principum</b> — o que fica guardado é o <i>resto</i> da frase — e todas custam
     * <b>dez vezes</b> uma Forma comum, que é o preço de um feitiço que espera.
     *
     * <p>Se houver alvo, é <b>nele</b> que a Contingência fica, e não em quem a lançou. É o que deixa pôr um
     * paraquedas em outra pessoa.
     */
    private static SpellPart.Shape contingency(String nome, Contingency.Kind espera) {
        return SpellParts.shape(new SpellPart.Shape() {
            @Override
            public String name() {
                return nome;
            }

            @Override
            public float manaMultiplier() {
                return Contingency.MANA_MULTIPLIER;
            }

            @Override
            public boolean principum() {
                return true;
            }

            @Override
            public SpellCast.Result begin(ServerLevel level, Spell feitiço, LivingEntity quem,
                                          @Nullable Entity alvo, Vec3 onde) {
                SpellCast.Result mal = precisaDeMais(feitiço);
                if (mal != null) return mal;

                LivingEntity nele = alvo instanceof LivingEntity vivo ? vivo : quem;
                Contingency.arm(nele, espera, feitiço.pop());
                return SpellCast.Result.SUCCESS;
            }
        });
    }

    /** <b>Contingência: Queda</b>. Dispara quando o chão está perto demais para o que falta cair. */
    public static final SpellPart.Shape CONTINGENCY_FALL =
            contingency("contingency_fall", Contingency.Kind.FALL);

    /** <b>Contingência: Dano</b>. Dispara em qualquer pancada que chegue. */
    public static final SpellPart.Shape CONTINGENCY_DAMAGE =
            contingency("contingency_damage", Contingency.Kind.DAMAGE_TAKEN);

    /** <b>Contingência: Fogo</b>. Dispara enquanto se estiver ardendo. */
    public static final SpellPart.Shape CONTINGENCY_FIRE =
            contingency("contingency_fire", Contingency.Kind.ON_FIRE);

    /** <b>Contingência: Vida Baixa</b>. Dispara ao cair a um terço da vida. */
    public static final SpellPart.Shape CONTINGENCY_HEALTH =
            contingency("contingency_health", Contingency.Kind.HEALTH_LOW);

    /**
     * <b>Contingência: Morte</b>. Dispara no instante em que se morre, <b>antes</b> de morrer de verdade.
     *
     * <p>É a mais valiosa das cinco: com uma Cura atrás dela, é uma segunda vida.
     */
    public static final SpellPart.Shape CONTINGENCY_DEATH =
            contingency("contingency_death", Contingency.Kind.DEATH);

    // ------------------------------------------------------------------ a mira

    /**
     * O bicho mais perto no caminho de um ponto a outro: a varredura do {@code EntitySpellProjectile}.
     *
     * <p>Ela infla a caixa de cada bicho em <b>três décimos</b>, que é o número do original, e escolhe o que o
     * segmento corta primeiro. Está escrita à mão de propósito: o ajudante do jogo infla pelo raio de escolha do
     * bicho, que num porco é zero, e assim um projétil fino passava ao lado.
     */
    public static @Nullable EntityHitResult nearest(net.minecraft.world.level.Level level, Entity quem,
                                                   Vec3 daqui, Vec3 até,
                                                   java.util.function.Predicate<Entity> serve) {
        /** O quanto o original engorda a caixa de quem pode levar. */
        final double FOLGA = 0.3;

        var roda = new net.minecraft.world.phys.AABB(daqui, até).inflate(1.0);
        Entity achado = null;
        Vec3 onde = null;
        double perto = 0.0;

        for (Entity outro : level.getEntities(quem, roda, serve)) {
            var caixa = outro.getBoundingBox().inflate(FOLGA);
            var corte = caixa.clip(daqui, até);
            if (corte.isEmpty()) continue;
            double distância = daqui.distanceTo(corte.get());
            if (achado == null || distância < perto) {
                achado = outro;
                onde = corte.get();
                perto = distância;
            }
        }
        return achado == null ? null : new EntityHitResult(achado, onde);
    }

    /**
     * O que o mago está olhando, até àquela distância: o {@code getMovingObjectPosition} do original.
     *
     * <p>Ele olha bicho <b>e</b> bloco e devolve o que estiver mais perto, que é o que faz um feitiço de toque
     * pegar no bicho à frente da parede e não na parede atrás dele.
     */
    public static @Nullable HitResult look(ServerLevel level, LivingEntity quem, double alcance,
                                           boolean líquidos) {
        Vec3 olho = quem.getEyePosition();
        Vec3 fim = olho.add(quem.getLookAngle().scale(alcance));

        BlockHitResult bloco = level.clip(new ClipContext(olho, fim,
                ClipContext.Block.OUTLINE,
                líquidos ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, quem));
        Vec3 até = bloco.getType() == HitResult.Type.MISS ? fim : bloco.getLocation();

        EntityHitResult bicho = nearest(level, quem, olho, até,
                e -> !e.isSpectator() && e.isPickable());

        if (bicho != null) return bicho;
        return bloco.getType() == HitResult.Type.MISS ? null : bloco;
    }
}
