package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * O Mutandis e o Mutandis Extremis: o {@code useMutandis} do {@code ItemGeneral} do Witchery.
 *
 * <p>Passado numa planta, ele a <b>troca por outra</b> da mesma lista, sorteada — é assim, e só assim, que se
 * chega às mudas do ofício, porque elas não nascem no mundo nem saem de receita.
 *
 * <p>A lista comum são as mudas (as do jogo e as três do ofício) e as plantinhas de chão: feto, nenúfar, os dois
 * cogumelos e as duas flores. O <b>Extremis</b> acrescenta as plantações — cenoura, batata, trigo, cana, as três
 * do ofício que dão em terra e em água, a abóbora, a melancia, o cato e a verruga do Nether —, e ainda troca
 * grama por micélio e, sobre a água, terra por barro.
 *
 * <p>Quando a planta trocada tem idade, a nova nasce com a mesma idade, sem passar do que ela aguenta: é o
 * {@code Math.min(metadata, ...)} do original.
 *
 * <p><b>Do original ficam de fora</b> o musgo-de-brasa e o musgo-espanhol, que são plantas do ramo ainda não
 * portadas, e a lista que o arquivo de ajustes deixava quem joga acrescentar.
 */
public class MutandisItem extends Item {
    private final boolean extremis;

    public MutandisItem(Properties properties, boolean extremis) {
        super(properties);
        this.extremis = extremis;
    }

    public boolean extremis() {
        return this.extremis;
    }

    /** Até onde a coluna sobe: quinze na cana, quatro no cato, que são os do original. */
    public static final int CANA_ALTA = 15;
    public static final int CATO_ALTO = 4;

    /**
     * <b>De onde vêm as duas sarças</b>: uma cana cercada de <b>musgo-espanhol</b>, com <b>água</b> nas
     * quatro quinas de baixo.
     *
     * <p>Vira <b>Sarça do Fim</b>, a coluna inteira, até quinze de altura — e o musgo à volta some, porque
     * foi ele que se gastou na troca.
     *
     * <p>E ela pede ainda <b>quatro Apanha-Ervas</b> nas diagonais, cada um segurando uma <b>pérola do
     * fim</b> — o que esteve declarado como buraco enquanto o Apanha-Erva não existia, e já não está.
     *
     * <p><b>Fica de fora, declarado:</b> no original quem faz esta mutação é a <b>Vara Mutante</b> e não o
     * Mutandis. A vara não está portada, e o Mutandis Extremis faz aqui o que ela fazia lá.
     */
    public static boolean éCanaDeSarça(Level level, BlockPos onde) {
        return level.getBlockState(onde).is(Blocks.SUGAR_CANE)
                && cercada(level, onde, net.minecraft.world.item.Items.ENDER_PEARL);
    }

    /**
     * E um <b>cato</b> do mesmo jeito vira <b>Sarça Selvagem</b>, até quatro de altura.
     *
     * <p>E ela pede <b>dois Apanha-Ervas com farinha de osso</b> e <b>dois com pó de blaze</b> nas
     * diagonais, que é a conta do original. A <b>Vara Mutante</b> continua a ser o buraco, como na cana.
     */
    public static boolean éCatoDeSarça(Level level, BlockPos onde) {
        return level.getBlockState(onde).is(Blocks.CACTUS)
                && cercada(level, onde, net.minecraft.world.item.Items.BONE_MEAL,
                        net.minecraft.world.item.Items.BLAZE_POWDER);
    }

    /**
     * <b>De onde vem o Baú de Sanguessugas</b>: um <b>baú armadilhado</b> vazio, com <b>quatro
     * trepadeiras</b> à volta e <b>água nas quatro quinas de baixo</b>.
     *
     * <p>O original pede o baú <b>armadilhado</b> e não o comum, e a escolha é dele: o que vai nascer dali é
     * uma armadilha, e ela começa numa armadilha.
     */
    public static boolean éBaúArmadilhado(Level level, BlockPos onde) {
        if (!level.getBlockState(onde).is(Blocks.TRAPPED_CHEST)) return false;
        for (net.minecraft.core.Direction lado : net.minecraft.core.Direction.Plane.HORIZONTAL) {
            if (!level.getBlockState(onde.relative(lado)).is(Blocks.VINE)) return false;
            if (!level.getBlockState(onde.relative(lado).below()).is(Blocks.WATER)) return false;
        }
        return baúVazio(level, onde);
    }

    public static boolean éBaúDeApanhaErva(Level level, BlockPos onde) {
        if (!level.getBlockState(onde).is(Blocks.CHEST)) return false;
        if (!level.getBlockState(onde.below()).is(Blocks.WATER)) return false;
        for (net.minecraft.core.Direction lado : net.minecraft.core.Direction.Plane.HORIZONTAL) {
            if (!level.getBlockState(onde.relative(lado)).is(Blocks.SHORT_GRASS)) return false;
        }
        return baúVazio(level, onde);
    }

    /**
     * <b>De onde vem o Apanha-Bicho</b>: uma <b>teia</b>, com <b>quatro mudas de amieiro</b> à volta, <b>água
     * por baixo</b> — e um <b>zumbi</b> ao lado, que é o que se gasta.
     *
     * <p>É a primeira de três mutações que partem de uma teia, e a única que não pede Apanha-Ervas. As mudas
     * viram os quatro Apanha-Bichos; o zumbi e a teia somem.
     */
    public static boolean éTeiaDeApanhaBicho(Level level, BlockPos onde) {
        if (!level.getBlockState(onde).is(Blocks.COBWEB)) return false;
        if (!level.getBlockState(onde.below()).is(Blocks.WATER)) return false;
        for (net.minecraft.core.Direction lado : net.minecraft.core.Direction.Plane.HORIZONTAL) {
            if (!level.getBlockState(onde.relative(lado)).is(OccultaBlocks.ALDER_SAPLING)) return false;
        }
        return bichoPerto(level, onde, net.minecraft.world.entity.monster.zombie.Zombie.class) != null;
    }

    /**
     * <b>De onde vem a Coruja</b>: a mesma teia, com <b>dois Apanha-Bichos de morcego</b> ao lado, água por
     * baixo, <b>três Apanha-Ervas com Mutandis Extremis</b> e <b>um com a Pedra Sintonizada carregada</b> nas
     * diagonais — e um <b>lobo</b>, que é o que se gasta.
     *
     * <p>É a receita mais longa do ramo das plantas, e vale olhar para ela inteira: um morcego apanhado numa
     * planta, um lobo ao lado, e a planta trocando um pelo outro. <b>Cada</b> Apanha-Bicho de morcego vira
     * uma coruja, de modo que quem puser os quatro leva quatro.
     */
    public static boolean éTeiaDeCoruja(Level level, BlockPos onde) {
        return éTeiaDeBicho(level, onde, CritterSnareBlock.Caught.BAT)
                && bichoPerto(level, onde, net.minecraft.world.entity.animal.wolf.Wolf.class) != null;
    }

    /** <b>E o Sapo</b>: a mesma coisa com <b>gosma</b> no lugar do morcego e um <b>jaguatirica</b> ao lado. */
    public static boolean éTeiaDeSapo(Level level, BlockPos onde) {
        return éTeiaDeBicho(level, onde, CritterSnareBlock.Caught.SLIME)
                && bichoPerto(level, onde, net.minecraft.world.entity.animal.feline.Ocelot.class) != null;
    }

    /** O que a coruja e o sapo têm em comum, que é quase tudo. */
    private static boolean éTeiaDeBicho(Level level, BlockPos onde, CritterSnareBlock.Caught oquê) {
        if (!level.getBlockState(onde).is(Blocks.COBWEB)) return false;
        if (!level.getBlockState(onde.below()).is(Blocks.WATER)) return false;
        if (quantosLados(level, onde, oquê) < APANHA_BICHOS) return false;
        if (quantosSeguram(level, onde, OccultaItems.MUTANDIS_EXTREMIS) < APANHA_ERVAS) return false;
        return quantosSeguram(level, onde, OccultaItems.ATTUNED_STONE_CHARGED) >= 1;
    }

    /** Quantos Apanha-Bichos do feitio certo há nos quatro lados. */
    public static int quantosLados(Level level, BlockPos onde, CritterSnareBlock.Caught oquê) {
        int quantos = 0;
        for (net.minecraft.core.Direction lado : net.minecraft.core.Direction.Plane.HORIZONTAL) {
            BlockState feitio = level.getBlockState(onde.relative(lado));
            if (feitio.is(OccultaBlocks.CRITTER_SNARE)
                    && feitio.getValue(CritterSnareBlock.APANHADO) == oquê) {
                quantos++;
            }
        }
        return quantos;
    }

    /** E quantos dos quatro Apanha-Ervas das diagonais seguram esta coisa. */
    public static int quantosSeguram(Level level, BlockPos onde, net.minecraft.world.item.Item oquê) {
        int quantos = 0;
        for (BlockPos quina : quinas(onde)) {
            if (GrassperBlock.segura(level, quina, oquê)) quantos++;
        }
        return quantos;
    }

    /**
     * O bicho que a mutação gasta, na caixa do original.
     *
     * <p>Ela é pequena e torta: <b>um bloco para cada lado</b>, <b>um para baixo</b> e <b>dois para cima</b>.
     * O bicho tem de estar quase em cima da teia.
     */
    @Nullable
    public static <T extends net.minecraft.world.entity.Entity> T bichoPerto(
            Level level, BlockPos onde, Class<T> qual) {
        var caixa = new net.minecraft.world.phys.AABB(
                onde.getX() - 1, onde.getY() - 1, onde.getZ() - 1,
                onde.getX() + 1, onde.getY() + 2, onde.getZ() + 1);
        var achados = level.getEntitiesOfClass(qual, caixa);
        return achados.isEmpty() ? null : achados.getFirst();
    }

    /** Quantos lados e quantas diagonais as duas receitas de bicho pedem. */
    public static final int APANHA_BICHOS = 2;
    public static final int APANHA_ERVAS = 3;

    /** As quatro diagonais, que é onde os Apanha-Ervas moram. */
    private static BlockPos[] quinas(BlockPos onde) {
        return new BlockPos[]{onde.offset(1, 0, 1), onde.offset(1, 0, -1),
                onde.offset(-1, 0, 1), onde.offset(-1, 0, -1)};
    }

    /**
     * As quatro de musgo ao lado, as quatro de água na quina de baixo — e os <b>quatro Apanha-Ervas</b> nas
     * diagonais, cada um com a coisa certa na boca.
     *
     * <p>Esta última parte estava declarada como buraco desde a fatia das sarças, porque o Apanha-Erva não
     * existia. Agora existe, e a conta é a do original.
     */
    private static boolean cercada(Level level, BlockPos onde, net.minecraft.world.item.Item... naBoca) {
        for (net.minecraft.core.Direction lado : net.minecraft.core.Direction.Plane.HORIZONTAL) {
            if (!level.getBlockState(onde.relative(lado)).is(OccultaBlocks.SPANISH_MOSS)) return false;
            if (!level.getBlockState(onde.relative(lado).below()).is(Blocks.WATER)) return false;
        }
        return nasQuinas(level, onde, naBoca);
    }

    /**
     * O que os quatro Apanha-Ervas das diagonais têm de segurar.
     *
     * <p>Dando-se <b>uma</b> coisa, os quatro seguram a mesma; dando-se <b>duas</b>, dois seguram cada —
     * e a ordem não importa, que é como o original conta.
     */
    private static boolean nasQuinas(Level level, BlockPos onde, net.minecraft.world.item.Item... quais) {
        BlockPos[] quinas = quinas(onde);
        if (quais.length == 1) {
            for (BlockPos quina : quinas) {
                if (!GrassperBlock.segura(level, quina, quais[0])) return false;
            }
            return true;
        }
        int[] conta = new int[quais.length];
        for (BlockPos quina : quinas) {
            for (int i = 0; i < quais.length; i++) {
                if (GrassperBlock.segura(level, quina, quais[i])) {
                    conta[i]++;
                    break;
                }
            }
        }
        for (int quantos : conta) {
            if (quantos < 2) return false;
        }
        return true;
    }

    /** Se este baú está vazio: o que lá estiver não se perde por um descuido. */
    private static boolean baúVazio(Level level, BlockPos onde) {
        if (!(level.getBlockEntity(onde) instanceof net.minecraft.world.Container baú)) return false;
        for (int casa = 0; casa < baú.getContainerSize(); casa++) {
            if (!baú.getItem(casa).isEmpty()) return false;
        }
        return true;
    }

    /** A coluna inteira, de cima para baixo, e o musgo que se gastou. */
    private static void vira(Level level, BlockPos onde, Block oquê, Block emQuê, int alto) {
        for (net.minecraft.core.Direction lado : net.minecraft.core.Direction.Plane.HORIZONTAL) {
            level.removeBlock(onde.relative(lado), false);
        }
        for (int i = alto; i >= 0; i--) {
            BlockPos casa = onde.above(i);
            if (!level.getBlockState(casa).is(oquê)) continue;
            level.setBlock(casa, emQuê.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    /**
     * <b>De onde vem a primeira Rosa de Sangue</b>: um <b>baú vazio</b>, com <b>quatro flores</b> à volta e
     * <b>água por baixo</b>, passado a Mutandis Extremis. O baú some, e no lugar das quatro flores ficam
     * quatro rosas.
     *
     * <p><b>Declarado:</b> no original esta corrente tem um elo no meio. O baú comum vira um <b>Apanha-Erva
     * </b>, e é o <b>Baú de Sanguessugas</b> — que também guarda nomes, e também é feito assim — que vira as
     * rosas. Nenhum dos dois está portado, e por isso o baú comum faz aqui o que o Baú de Sanguessugas fazia
     * lá. Quando eles vierem, a corrente ganha o elo de volta e esta conta passa para o baú certo.
     */
    public static boolean éBaúDeRosas(Level level, BlockPos onde) {
        if (!level.getBlockState(onde).is(OccultaBlocks.LEECH_CHEST)) return false;
        if (!level.getBlockState(onde.below()).is(Blocks.WATER)) return false;

        int flores = 0;
        for (net.minecraft.core.Direction lado : net.minecraft.core.Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(onde.relative(lado)).is(net.minecraft.tags.BlockTags.SMALL_FLOWERS)) {
                flores++;
            }
        }
        if (flores < 4) return false;

        // e o baú tem de estar vazio: o que lá estiver não se perde por um descuido
        return baúVazio(level, onde);
    }

    /** As plantas que o Mutandis comum troca entre si. */
    public static List<Block> common() {
        List<Block> lista = new ArrayList<>(List.of(
                Blocks.OAK_SAPLING, Blocks.SPRUCE_SAPLING, Blocks.BIRCH_SAPLING, Blocks.JUNGLE_SAPLING,
                Blocks.ACACIA_SAPLING, Blocks.DARK_OAK_SAPLING,
                OccultaBlocks.ROWAN_SAPLING, OccultaBlocks.ALDER_SAPLING, OccultaBlocks.HAWTHORN_SAPLING,
                Blocks.FERN, Blocks.LILY_PAD, Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM,
                Blocks.POPPY, Blocks.DANDELION));
        return lista;
    }

    /** E as que o Extremis acrescenta: o que dá de comer. */
    public static List<Block> extremisOnly() {
        return List.of(Blocks.CARROTS, Blocks.POTATOES, Blocks.WHEAT, Blocks.SUGAR_CANE,
                OccultaBlocks.BELLADONNA, OccultaBlocks.MANDRAKE, OccultaBlocks.WATER_ARTICHOKE,
                Blocks.PUMPKIN_STEM, Blocks.MELON_STEM, Blocks.CACTUS, Blocks.NETHER_WART);
    }

    /** A lista inteira que este Mutandis alcança. */
    public List<Block> list() {
        List<Block> lista = common();
        if (this.extremis) lista.addAll(extremisOnly());
        return lista;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos onde = context.getClickedPos();
        BlockState qual = level.getBlockState(onde);

        // o Extremis também mexe no chão: grama vira micélio e volta
        if (this.extremis && (qual.is(Blocks.GRASS_BLOCK) || qual.is(Blocks.MYCELIUM))) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            if (level.getRandom().nextInt(2) == 0) {
                level.setBlock(onde, qual.is(Blocks.GRASS_BLOCK)
                        ? Blocks.MYCELIUM.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState(),
                        Block.UPDATE_ALL);
            }
            this.spend(context, level, onde);
            return InteractionResult.SUCCESS;
        }

        // e terra debaixo de água vira barro, ela e as quatro ao lado
        if (this.extremis && qual.is(Blocks.DIRT) && level.getBlockState(onde.above()).is(Blocks.WATER)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            if (level.getRandom().nextInt(2) == 0) {
                clay(level, onde);
                for (net.minecraft.core.Direction lado : net.minecraft.core.Direction.Plane.HORIZONTAL) {
                    clay(level, onde.relative(lado));
                }
            }
            this.spend(context, level, onde);
            return InteractionResult.SUCCESS;
        }

        // e um baú vazio, com quatro flores à volta e água por baixo, vira quatro Rosas de Sangue
        if (this.extremis && éBaúDeRosas(level, onde)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            level.removeBlock(onde, false);
            for (net.minecraft.core.Direction lado : net.minecraft.core.Direction.Plane.HORIZONTAL) {
                level.setBlock(onde.relative(lado),
                        OccultaBlocks.BLOOD_ROSE.defaultBlockState(), Block.UPDATE_ALL);
            }
            this.spend(context, level, onde);
            return InteractionResult.SUCCESS;
        }

        // um baú armadilhado, com quatro trepadeiras à volta e água nas quinas, vira quatro Baús de Sanguessugas
        if (this.extremis && éBaúArmadilhado(level, onde)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            level.removeBlock(onde, false);
            for (net.minecraft.core.Direction lado : net.minecraft.core.Direction.Plane.HORIZONTAL) {
                level.setBlock(onde.relative(lado),
                        OccultaBlocks.LEECH_CHEST.defaultBlockState(), Block.UPDATE_ALL);
            }
            this.spend(context, level, onde);
            return InteractionResult.SUCCESS;
        }

        // e um baú vazio, com quatro tufos de grama à volta e água por baixo, vira quatro Apanha-Ervas
        if (this.extremis && éBaúDeApanhaErva(level, onde)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            level.removeBlock(onde, false);
            for (net.minecraft.core.Direction lado : net.minecraft.core.Direction.Plane.HORIZONTAL) {
                level.setBlock(onde.relative(lado),
                        OccultaBlocks.GRASSPER.defaultBlockState(), Block.UPDATE_ALL);
            }
            this.spend(context, level, onde);
            return InteractionResult.SUCCESS;
        }

        // uma teia, com quatro mudas de amieiro à volta, água por baixo e um zumbi ao lado
        if (this.extremis && éTeiaDeApanhaBicho(level, onde)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            gasta(level, onde, net.minecraft.world.entity.monster.zombie.Zombie.class,
                    SoundEvents.ZOMBIE_DEATH);
            level.removeBlock(onde, false);
            for (net.minecraft.core.Direction lado : net.minecraft.core.Direction.Plane.HORIZONTAL) {
                level.setBlock(onde.relative(lado),
                        OccultaBlocks.CRITTER_SNARE.defaultBlockState(), Block.UPDATE_ALL);
            }
            this.spend(context, level, onde);
            return InteractionResult.SUCCESS;
        }

        /*
         * E as duas que saem dela: a <b>Coruja</b>, de morcegos e um lobo, e o <b>Sapo</b>, de gosma e um
         * jaguatirica. Cada Apanha-Bicho do feitio certo vira um bicho, de modo que quem puser os quatro
         * leva quatro — e os quatro Apanha-Ervas ficam de boca vazia, porque foi o que seguravam que se
         * gastou.
         */
        if (this.extremis && éTeiaDeCoruja(level, onde)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            gasta(level, onde, net.minecraft.world.entity.animal.wolf.Wolf.class, morteDeLobo(level));
            vira(level, onde, CritterSnareBlock.Caught.BAT, OccultaEntities.OWL);
            this.spend(context, level, onde);
            return InteractionResult.SUCCESS;
        }
        if (this.extremis && éTeiaDeSapo(level, onde)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            gasta(level, onde, net.minecraft.world.entity.animal.feline.Ocelot.class,
                    SoundEvents.OCELOT_DEATH);
            vira(level, onde, CritterSnareBlock.Caught.SLIME, OccultaEntities.TOAD);
            this.spend(context, level, onde);
            return InteractionResult.SUCCESS;
        }

        /*
         * E as duas sarças: uma <b>cana</b> ou um <b>cato</b> cercados de musgo-espanhol, com água nas
         * quatro quinas de baixo, viram a coluna inteira em sarça — a cana em <b>Sarça do Fim</b>, o cato em
         * <b>Sarça Selvagem</b>.
         */
        if (this.extremis && éCanaDeSarça(level, onde)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            vira(level, onde, Blocks.SUGAR_CANE, OccultaBlocks.ENDER_BRAMBLE, CANA_ALTA);
            this.spend(context, level, onde);
            return InteractionResult.SUCCESS;
        }
        if (this.extremis && éCatoDeSarça(level, onde)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            vira(level, onde, Blocks.CACTUS, OccultaBlocks.WILD_BRAMBLE, CATO_ALTO);
            this.spend(context, level, onde);
            return InteractionResult.SUCCESS;
        }

        List<Block> lista = this.list();
        if (!lista.contains(qual.getBlock())) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        lista.remove(qual.getBlock());
        Block nova = lista.get(level.getRandom().nextInt(lista.size()));
        level.setBlock(onde, aged(nova, qual), Block.UPDATE_ALL);
        this.spend(context, level, onde);
        return InteractionResult.SUCCESS;
    }

    /**
     * O bicho que a mutação <b>gasta</b>, com os pós e o barulho da morte dele.
     *
     * <p>O original não o mata: ele o <b>faz desaparecer</b>, e toca o som de morte por cima. A diferença
     * importa — nada cai dele, e nada o conta como morto.
     */
    private static void gasta(Level level, BlockPos onde,
                              Class<? extends net.minecraft.world.entity.Entity> qual,
                              net.minecraft.sounds.SoundEvent som) {
        var bicho = bichoPerto(level, onde, qual);
        if (bicho == null || !(level instanceof ServerLevel mundo)) return;
        mundo.sendParticles(ParticleTypes.ITEM_SLIME, bicho.getX(), bicho.getY() + 1.0, bicho.getZ(),
                16, 1.5, 1.0, 1.5, 0.0);
        mundo.playSound(null, bicho.blockPosition(), som, SoundSource.NEUTRAL, 1.0f, 1.0f);
        bicho.discard();
    }

    /**
     * A morte de um lobo, que o jogo de hoje guarda num <b>feitio de som</b> e não num campo.
     *
     * <p>O original toca o {@code mob.wolf.death} e pronto. Hoje cada lobo tem a sua voz — há sete feitios
     * de som —, e qual delas ele tem não se pergunta de fora. Toca-se a do <b>clássico</b>, que é a voz que
     * um lobo tem quando ninguém escolheu outra. Fica <b>declarado</b>.
     */
    private static net.minecraft.sounds.SoundEvent morteDeLobo(Level level) {
        var registro = level.registryAccess()
                .lookupOrThrow(net.minecraft.core.registries.Registries.WOLF_SOUND_VARIANT);
        return registro.getOrThrow(net.minecraft.world.entity.animal.wolf.WolfSoundVariants.CLASSIC)
                .value().adultSounds().deathSound().value();
    }

    /**
     * Troca <b>cada</b> Apanha-Bicho do feitio certo pelo bicho que ele dá, e esvazia os quatro
     * Apanha-Ervas das diagonais.
     *
     * <p>É o {@code convertToEntity} do original junto com o {@code clearGrassperAt}: o que estava preso
     * sai vivo, e o que estava na boca dos Apanha-Ervas se gastou na troca.
     */
    private static void vira(Level level, BlockPos onde, CritterSnareBlock.Caught oquê,
                             net.minecraft.world.entity.EntityType<?> qual) {
        if (!(level instanceof ServerLevel mundo)) return;
        level.removeBlock(onde, false);
        for (net.minecraft.core.Direction lado : net.minecraft.core.Direction.Plane.HORIZONTAL) {
            BlockPos ali = onde.relative(lado);
            BlockState feitio = level.getBlockState(ali);
            if (!feitio.is(OccultaBlocks.CRITTER_SNARE)) continue;
            if (feitio.getValue(CritterSnareBlock.APANHADO) != oquê) continue;
            level.removeBlock(ali, false);
            var bicho = qual.create(mundo, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
            if (bicho == null) continue;
            bicho.snapTo(ali.getX() + 0.5, ali.getY() + 0.001, ali.getZ() + 0.5, 1.0f, 0.0f);
            if (bicho instanceof net.minecraft.world.entity.Mob mob) {
                mob.setPersistenceRequired();
                mob.finalizeSpawn(mundo, mundo.getCurrentDifficultyAt(ali),
                        net.minecraft.world.entity.EntitySpawnReason.TRIGGERED, null);
            }
            mundo.addFreshEntity(bicho);
        }
        for (BlockPos quina : quinas(onde)) esvazia(mundo, quina);
    }

    /** Tira o que um Apanha-Erva tem na boca, com os pós de gosma do original. */
    private static void esvazia(ServerLevel level, BlockPos onde) {
        if (!level.getBlockState(onde).is(OccultaBlocks.GRASSPER)) return;
        if (!(level.getBlockEntity(onde) instanceof GrassperBlockEntity planta)) return;
        if (planta.naBoca().isEmpty()) return;
        planta.põe(net.minecraft.world.item.ItemStack.EMPTY);
        level.sendParticles(ParticleTypes.ITEM_SLIME, onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5,
                16, 1.0, 2.0, 1.0, 0.0);
    }

    /** A nova planta com a idade da velha, sem passar do que ela aguenta. */
    private static BlockState aged(Block nova, BlockState velha) {
        BlockState estado = nova.defaultBlockState();
        var idade = net.minecraft.world.level.block.state.properties.BlockStateProperties.AGE_7;
        if (!(nova instanceof net.minecraft.world.level.block.CropBlock crop)) return estado;
        if (!(velha.getBlock() instanceof net.minecraft.world.level.block.CropBlock velhaCrop)) return estado;
        int quanto = Math.min(velhaCrop.getAge(velha), crop.getMaxAge());
        return crop.getStateForAge(quanto);
    }

    private static void clay(Level level, BlockPos onde) {
        if (!level.getBlockState(onde).is(Blocks.DIRT)) return;
        if (!level.getBlockState(onde.above()).is(Blocks.WATER)) return;
        level.setBlock(onde, Blocks.CLAY.defaultBlockState(), Block.UPDATE_ALL);
    }

    private void spend(UseOnContext context, Level level, BlockPos onde) {
        context.getItemInHand().consume(1, context.getPlayer());
        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.HAPPY_VILLAGER, onde.getX() + 0.5, onde.getY() + 1.0, onde.getZ() + 0.5,
                    12, 0.4, 0.4, 0.4, 0.0);
        }
        level.playSound(null, onde, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 1.8f);
    }
}
