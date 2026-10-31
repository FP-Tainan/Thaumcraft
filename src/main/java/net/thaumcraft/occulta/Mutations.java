package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

/**
 * As <b>quinze mutações</b> da Galho Mutante: o {@code ItemMutator} do Witchery.
 *
 * <p>São quinze, e todas têm o mesmo feitio: um bloco que se clica, um <b>desenho de blocos à volta</b>
 * dele, e às vezes um <b>bicho perto</b> que morre na troca. Nada disto se faz com uma receita de bancada;
 * faz-se no chão, construindo a figura e tocando-lhe com o galho.
 *
 * <p>Os desenhos usam três anéis: os <b>quatro lados</b>, onde ficam as plantas; a <b>casa de baixo</b> ou
 * as <b>quatro de baixo</b>, onde fica a água; e as <b>quatro diagonais</b>, onde ficam os Apanha-Ervas,
 * cada um com uma coisa certa na boca. O que eles seguram <b>gasta-se</b>: depois da mutação ficam de boca
 * vazia.
 *
 * <p>Elas vivem aqui, e não no item, porque três delas — a grama, o micélio e o barro — são também do
 * <b>Mutandis Extremis</b>, que as faz na moeda ao ar enquanto o galho as faz sempre.
 */
public final class Mutations {
    /** Até onde a coluna sobe: quinze na cana, quatro no cato, que são os do original. */
    public static final int CANA_ALTA = 15;
    public static final int CATO_ALTO = 4;

    /** Quantos lados e quantas diagonais as duas receitas de bicho pedem. */
    public static final int APANHA_BICHOS = 2;
    public static final int APANHA_ERVAS = 3;

    /** E os quatro cacaus maduros e os dois Extremis que o Macaco de Asas pede. */
    public static final int CACAUS = 4;
    public static final int EXTREMIS_DO_MACACO = 2;

    /** Quantos Apanha-Ervas a Mentedrágora e a Lêndea pedem de cada coisa. */
    public static final int EXTREMIS_DA_MENTEDRAGORA = 2;
    public static final int MUTANDIS_DA_LENDEA = 2;

    private Mutations() {
    }

    // ------------------------------------------------------------------ o chão

    /** <b>Terra debaixo de água vira barro</b>, uma casa de cada vez. */
    public static void barro(Level level, BlockPos onde) {
        if (!level.getBlockState(onde).is(Blocks.DIRT)) return;
        if (!level.getBlockState(onde.above()).is(Blocks.WATER)) return;
        level.setBlock(onde, Blocks.CLAY.defaultBlockState(), Block.UPDATE_ALL);
    }

    /** E ela com as quatro ao lado, que é como as duas coisas que a fazem a fazem. */
    public static void barroEmCruz(Level level, BlockPos onde) {
        barro(level, onde);
        for (Direction lado : Direction.Plane.HORIZONTAL) barro(level, onde.relative(lado));
    }

    // ------------------------------------------------------------------ as duas sarças

    /**
     * <b>De onde vêm as duas sarças</b>: uma cana cercada de <b>musgo-espanhol</b>, com <b>água</b> nas
     * quatro casas de baixo e <b>quatro Apanha-Ervas</b> nas diagonais, cada um com uma <b>pérola do
     * fim</b>.
     *
     * <p>Vira <b>Sarça do Fim</b>, a coluna inteira, até quinze de altura — e o musgo à volta some, porque
     * foi ele que se gastou na troca.
     */
    public static boolean éCanaDeSarça(Level level, BlockPos onde) {
        return level.getBlockState(onde).is(Blocks.SUGAR_CANE)
                && cercada(level, onde, Items.ENDER_PEARL);
    }

    /**
     * E um <b>cato</b> do mesmo jeito vira <b>Sarça Selvagem</b>, até quatro de altura.
     *
     * <p>Ele pede <b>dois Apanha-Ervas com farinha de osso</b> e <b>dois com pó de blaze</b> nas diagonais,
     * que é a conta do original.
     */
    public static boolean éCatoDeSarça(Level level, BlockPos onde) {
        return level.getBlockState(onde).is(Blocks.CACTUS)
                && cercada(level, onde, Items.BONE_MEAL, Items.BLAZE_POWDER);
    }

    /** A coluna inteira, de cima para baixo, e o musgo que se gastou. */
    public static void colunaVira(Level level, BlockPos onde, Block oquê, Block emQuê, int alto) {
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            level.removeBlock(onde.relative(lado), false);
        }
        for (BlockPos quina : quinas(onde)) esvazia(level, quina);
        for (int i = alto; i >= 0; i--) {
            BlockPos casa = onde.above(i);
            if (!level.getBlockState(casa).is(oquê)) continue;
            level.setBlock(casa, emQuê.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    // ------------------------------------------------------------------ o trigo

    /**
     * <b>De onde vem a Losna</b>: um pé de <b>trigo maduro</b>, com <b>quatro Algodões-Fiapo</b> à volta e
     * <b>água nas quatro quinas de baixo</b>.
     *
     * <p>É a única mutação que não pede Apanha-Erva nenhum e não gasta bicho nenhum — e é por isso que ela
     * é a primeira que se faz. A losna não nasce no mundo: ou sai daqui, ou não se tem.
     */
    public static boolean éTrigoDeLosna(Level level, BlockPos onde) {
        BlockState feitio = level.getBlockState(onde);
        if (!feitio.is(Blocks.WHEAT)) return false;
        if (feitio.getValue(BlockStateProperties.AGE_7) != 7) return false;
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            if (!level.getBlockState(onde.relative(lado)).is(OccultaBlocks.WISPY_COTTON)) return false;
        }
        for (BlockPos quina : quinas(onde)) {
            if (!level.getBlockState(quina.below()).is(Blocks.WATER)) return false;
        }
        return true;
    }

    // ------------------------------------------------------------------ os três baús

    /**
     * <b>De onde vem a Arca de Sanguessugas</b>: um <b>baú armadilhado</b> vazio, com <b>quatro
     * trepadeiras</b> à volta e <b>água nas quatro casas de baixo</b>.
     *
     * <p>O original pede o baú <b>armadilhado</b> e não o comum, e a escolha é dele: o que vai nascer dali é
     * uma armadilha, e ela começa numa armadilha.
     */
    public static boolean éBaúArmadilhado(Level level, BlockPos onde) {
        if (!level.getBlockState(onde).is(Blocks.TRAPPED_CHEST)) return false;
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            if (!level.getBlockState(onde.relative(lado)).is(Blocks.VINE)) return false;
            if (!level.getBlockState(onde.relative(lado).below()).is(Blocks.WATER)) return false;
        }
        return baúVazio(level, onde);
    }

    /** E um <b>baú comum</b> vazio, com <b>quatro tufos de grama</b> e água por baixo, dá Apanha-Ervas. */
    public static boolean éBaúDeApanhaErva(Level level, BlockPos onde) {
        if (!level.getBlockState(onde).is(Blocks.CHEST)) return false;
        if (!level.getBlockState(onde.below()).is(Blocks.WATER)) return false;
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            if (!level.getBlockState(onde.relative(lado)).is(Blocks.SHORT_GRASS)) return false;
        }
        return baúVazio(level, onde);
    }

    /**
     * <b>De onde vem a Rosa de Sangue</b>: a <b>Arca de Sanguessugas</b> vazia, com <b>quatro flores</b> à
     * volta e <b>água por baixo</b>. A arca some, e no lugar das quatro flores ficam quatro rosas.
     *
     * <p>É o segundo elo da corrente dos baús: um baú armadilhado vira arca, e a arca vira rosas.
     *
     * <p><b>Declarado:</b> o original pede o bloco das <b>flores vermelhas</b> de 2014, que eram nove numa
     * só — papoula, orquídea, túlipas e as outras. Hoje são nove blocos, e o rótulo
     * {@code #minecraft:small_flowers} é o que os junta; ele inclui também o <b>dente-de-leão</b>, que lá
     * era outro bloco e ficava de fora.
     */
    public static boolean éArcaDeRosas(Level level, BlockPos onde) {
        if (!level.getBlockState(onde).is(OccultaBlocks.LEECH_CHEST)) return false;
        if (!level.getBlockState(onde.below()).is(Blocks.WATER)) return false;
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            if (!level.getBlockState(onde.relative(lado)).is(net.minecraft.tags.BlockTags.SMALL_FLOWERS)) {
                return false;
            }
        }
        return baúVazio(level, onde);
    }

    /** Se este baú está vazio: o que lá estiver não se perde por um descuido. */
    public static boolean baúVazio(Level level, BlockPos onde) {
        if (!(level.getBlockEntity(onde) instanceof Container baú)) return false;
        for (int casa = 0; casa < baú.getContainerSize(); casa++) {
            if (!baú.getItem(casa).isEmpty()) return false;
        }
        return true;
    }

    /** O do meio some e os quatro lados viram aquilo. */
    public static void osLadosViram(Level level, BlockPos onde, Block oquê) {
        level.removeBlock(onde, false);
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            level.setBlock(onde.relative(lado), oquê.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    // ------------------------------------------------------------------ as cinco teias

    /**
     * <b>De onde vem o Apanha-Bicho</b>: uma <b>teia</b>, com <b>quatro mudas de amieiro</b> à volta,
     * <b>água por baixo</b> — e um <b>zumbi</b> ao lado, que é o que se gasta.
     *
     * <p>É a primeira de cinco mutações que partem de uma teia, e a única que não pede Apanha-Ervas.
     */
    public static boolean éTeiaDeApanhaBicho(Level level, BlockPos onde) {
        if (!éTeiaSobreÁgua(level, onde)) return false;
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            if (!level.getBlockState(onde.relative(lado)).is(OccultaBlocks.ALDER_SAPLING)) return false;
        }
        return bichoPerto(level, onde, net.minecraft.world.entity.monster.zombie.Zombie.class) != null;
    }

    /**
     * <b>De onde vem a Coruja</b>: a mesma teia, com <b>dois Apanha-Bichos de morcego</b> ao lado,
     * <b>três Apanha-Ervas com Mutandis Extremis</b> e <b>um com a Pedra Sintonizada carregada</b> nas
     * diagonais — e um <b>lobo</b>, que é o que se gasta.
     *
     * <p>Vale olhar para ela inteira: um morcego preso numa planta, um lobo ao lado, e a planta trocando um
     * pelo outro. <b>Cada</b> Apanha-Bicho de morcego vira uma coruja, de modo que quem puser os quatro
     * leva quatro.
     */
    public static boolean éTeiaDeCoruja(Level level, BlockPos onde) {
        return éTeiaDeBicho(level, onde, CritterSnareBlock.Caught.BAT)
                && bichoPerto(level, onde, net.minecraft.world.entity.animal.wolf.Wolf.class) != null;
    }

    /** <b>E o Sapo</b>: a mesma coisa com <b>gosma</b> no lugar do morcego e uma <b>jaguatirica</b> ao lado. */
    public static boolean éTeiaDeSapo(Level level, BlockPos onde) {
        return éTeiaDeBicho(level, onde, CritterSnareBlock.Caught.SLIME)
                && bichoPerto(level, onde, net.minecraft.world.entity.animal.feline.Ocelot.class) != null;
    }

    /** O que a coruja e o sapo têm em comum, que é quase tudo. */
    private static boolean éTeiaDeBicho(Level level, BlockPos onde, CritterSnareBlock.Caught oquê) {
        if (!éTeiaSobreÁgua(level, onde)) return false;
        if (quantosLados(level, onde, oquê) < APANHA_BICHOS) return false;
        if (quantosSeguram(level, onde, OccultaItems.MUTANDIS_EXTREMIS) < APANHA_ERVAS) return false;
        return quantosSeguram(level, onde, OccultaItems.ATTUNED_STONE_CHARGED) >= 1;
    }

    /**
     * <b>De onde vem a Mentedrágora</b>: a mesma teia, com <b>quatro mandrágoras maduras</b> à volta,
     * <b>dois Apanha-Ervas com Mutandis Extremis</b>, um com a <b>Pedra Sintonizada carregada</b> e um com
     * a <b>Vontade Focada</b> — e <b>duas vidas</b>: um <b>creeper</b> e uma <b>mandrágora viva</b>.
     *
     * <p>A conta tem sentido: para fazer uma planta que anda e explode, dá-se uma planta que anda e uma
     * coisa que explode. É a mutação mais clara do mod a dizer o que está fazendo.
     */
    public static boolean éTeiaDeMentedrágora(Level level, BlockPos onde) {
        if (!éTeiaSobreÁgua(level, onde)) return false;
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            if (!maduro(level, onde.relative(lado), OccultaBlocks.MANDRAKE)) return false;
        }
        if (quantosSeguram(level, onde, OccultaItems.MUTANDIS_EXTREMIS) < EXTREMIS_DA_MENTEDRAGORA) {
            return false;
        }
        if (quantosSeguram(level, onde, OccultaItems.ATTUNED_STONE_CHARGED) < 1) return false;
        if (quantosSeguram(level, onde, OccultaItems.FOCUSED_WILL) < 1) return false;
        if (bichoPerto(level, onde, net.minecraft.world.entity.monster.Creeper.class) == null) {
            return false;
        }
        return bichoPerto(level, onde, MandrakeEntity.class) != null;
    }

    /**
     * <b>De onde vem o Macaco de Asas</b>, que é a receita mais cara de todas.
     *
     * <p>A mesma teia, mas com <b>quatro vagens de cacau maduras</b> à volta em vez de plantas, e com os
     * quatro Apanha-Ervas repartidos: <b>dois com Mutandis Extremis</b>, um com a <b>Pedra Sintonizada
     * carregada</b> e um com uma <b>flor vermelha</b>. Quatro Apanha-Ervas, dois mais um mais um — a conta
     * fecha certa, e é de propósito.
     *
     * <p>E pede <b>duas vidas</b>: uma <b>coruja</b> e um <b>lobo</b>, que somem os dois.
     */
    public static boolean éTeiaDeMacaco(Level level, BlockPos onde) {
        if (!éTeiaSobreÁgua(level, onde)) return false;
        if (quantosCacaus(level, onde) < CACAUS) return false;
        if (quantosSeguram(level, onde, OccultaItems.MUTANDIS_EXTREMIS) < EXTREMIS_DO_MACACO) return false;
        if (quantosSeguram(level, onde, OccultaItems.ATTUNED_STONE_CHARGED) < 1) return false;
        if (quantosSeguram(level, onde, Items.POPPY) < 1) return false;
        if (bichoPerto(level, onde, net.thaumcraft.occulta.familiar.OwlEntity.class) == null) return false;
        return bichoPerto(level, onde, net.minecraft.world.entity.animal.wolf.Wolf.class) != null;
    }

    private static boolean éTeiaSobreÁgua(Level level, BlockPos onde) {
        return level.getBlockState(onde).is(Blocks.COBWEB)
                && level.getBlockState(onde.below()).is(Blocks.WATER);
    }

    // ------------------------------------------------------------------ a lêndea

    /**
     * <b>De onde vem a Lêndea Parasita</b>: um <b>Apanha-Bicho com uma lepisma dentro</b>, com <b>duas
     * trepadeiras</b> ao lado, <b>água numa das casas de baixo</b>, <b>dois Apanha-Ervas com Mutandis</b> —
     * o comum, e é a única mutação que pede o comum —, um com a <b>Pedra Sintonizada carregada</b> e um com
     * uma <b>Língua de Cão</b>.
     *
     * <p>É a única que não troca o que está à volta: o que muda é o <b>próprio</b> Apanha-Bicho, e a
     * lepisma que estava presa sai dele virada em lêndea.
     */
    public static boolean éLaçoDeLêndea(Level level, BlockPos onde) {
        BlockState feitio = level.getBlockState(onde);
        if (!feitio.is(OccultaBlocks.CRITTER_SNARE)) return false;
        if (feitio.getValue(CritterSnareBlock.APANHADO) != CritterSnareBlock.Caught.SILVERFISH) {
            return false;
        }

        int trepadeiras = 0;
        int águas = 0;
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(onde.relative(lado)).is(Blocks.VINE)) trepadeiras++;
            if (level.getBlockState(onde.relative(lado).below()).is(Blocks.WATER)) águas++;
        }
        if (trepadeiras < 2 || águas < 1) return false;

        if (quantosSeguram(level, onde, OccultaItems.MUTANDIS) < MUTANDIS_DA_LENDEA) return false;
        if (quantosSeguram(level, onde, OccultaItems.ATTUNED_STONE_CHARGED) < 1) return false;
        return quantosSeguram(level, onde, OccultaItems.DOG_TONGUE) >= 1;
    }

    // ------------------------------------------------------------------ as contas do desenho

    /** Quantos Apanha-Bichos do feitio certo há nos quatro lados. */
    public static int quantosLados(Level level, BlockPos onde, CritterSnareBlock.Caught oquê) {
        int quantos = 0;
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            BlockState feitio = level.getBlockState(onde.relative(lado));
            if (feitio.is(OccultaBlocks.CRITTER_SNARE)
                    && feitio.getValue(CritterSnareBlock.APANHADO) == oquê) {
                quantos++;
            }
        }
        return quantos;
    }

    /** E quantos dos quatro Apanha-Ervas das diagonais seguram esta coisa. */
    public static int quantosSeguram(Level level, BlockPos onde, Item oquê) {
        int quantos = 0;
        for (BlockPos quina : quinas(onde)) {
            if (GrassperBlock.segura(level, quina, oquê)) quantos++;
        }
        return quantos;
    }

    /** Quantas das quatro vagens de cacau à volta estão <b>maduras</b>. */
    public static int quantosCacaus(Level level, BlockPos onde) {
        int quantos = 0;
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            BlockState feitio = level.getBlockState(onde.relative(lado));
            if (!feitio.is(Blocks.COCOA)) continue;
            if (feitio.getValue(CocoaBlock.AGE) == 2) quantos++;
        }
        return quantos;
    }

    /** Se aquela casa tem aquela lavoura <b>no fim do crescimento</b>. */
    public static boolean maduro(Level level, BlockPos onde, Block qual) {
        BlockState feitio = level.getBlockState(onde);
        if (!feitio.is(qual)) return false;
        if (!(qual instanceof CropBlock lavoura)) return false;
        return lavoura.getAge(feitio) >= lavoura.getMaxAge();
    }

    /** As quatro diagonais, que é onde os Apanha-Ervas moram. */
    public static BlockPos[] quinas(BlockPos onde) {
        return new BlockPos[]{onde.offset(1, 0, 1), onde.offset(1, 0, -1),
                onde.offset(-1, 0, 1), onde.offset(-1, 0, -1)};
    }

    /**
     * As quatro de musgo ao lado, as quatro de água por baixo — e os <b>quatro Apanha-Ervas</b> nas
     * diagonais, cada um com a coisa certa na boca.
     */
    private static boolean cercada(Level level, BlockPos onde, Item... naBoca) {
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            if (!level.getBlockState(onde.relative(lado)).is(OccultaBlocks.SPANISH_MOSS)) return false;
            if (!level.getBlockState(onde.relative(lado).below()).is(Blocks.WATER)) return false;
        }
        return nasQuinas(level, onde, naBoca);
    }

    /**
     * O que os quatro Apanha-Ervas das diagonais têm de segurar.
     *
     * <p>Dando-se <b>uma</b> coisa, os quatro seguram a mesma; dando-se <b>duas</b>, dois seguram cada — e
     * a ordem não importa, que é como o original conta.
     */
    private static boolean nasQuinas(Level level, BlockPos onde, Item... quais) {
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

    /**
     * O bicho que a mutação gasta, na caixa do original.
     *
     * <p>Ela é pequena e torta: <b>um bloco para cada lado</b>, <b>um para baixo</b> e <b>dois para cima</b>.
     * O bicho tem de estar quase em cima do que se vai mudar.
     */
    @Nullable
    public static <T extends Entity> T bichoPerto(Level level, BlockPos onde, Class<T> qual) {
        var caixa = new AABB(onde.getX() - 1, onde.getY() - 1, onde.getZ() - 1,
                onde.getX() + 1, onde.getY() + 2, onde.getZ() + 1);
        var achados = level.getEntitiesOfClass(qual, caixa);
        return achados.isEmpty() ? null : achados.getFirst();
    }

    // ------------------------------------------------------------------ o que a mutação faz

    /**
     * O bicho que a mutação <b>gasta</b>, com os pós e o barulho da morte dele.
     *
     * <p>O original não o mata: ele o <b>faz desaparecer</b>, e toca o som de morte por cima. A diferença
     * importa — nada cai dele, e nada o conta como morto.
     */
    public static void gasta(Level level, BlockPos onde, Class<? extends Entity> qual, SoundEvent som) {
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
    public static SoundEvent morteDeLobo(Level level) {
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
    public static void osLadosViramBicho(Level level, BlockPos onde, CritterSnareBlock.Caught oquê,
                                         EntityType<?> qual) {
        if (!(level instanceof ServerLevel mundo)) return;
        level.removeBlock(onde, false);
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            viraBicho(mundo, onde.relative(lado), oquê, qual);
        }
        for (BlockPos quina : quinas(onde)) esvazia(mundo, quina);
    }

    /** E um Apanha-Bicho só, que é o que a Lêndea pede. */
    public static void viraBicho(Level level, BlockPos onde, CritterSnareBlock.Caught oquê,
                                 EntityType<?> qual) {
        if (!(level instanceof ServerLevel mundo)) return;
        BlockState feitio = level.getBlockState(onde);
        if (!feitio.is(OccultaBlocks.CRITTER_SNARE)) return;
        if (feitio.getValue(CritterSnareBlock.APANHADO) != oquê) return;
        level.removeBlock(onde, false);

        var bicho = qual.create(mundo, EntitySpawnReason.TRIGGERED);
        if (bicho == null) return;
        bicho.snapTo(onde.getX() + 0.5, onde.getY() + 0.001, onde.getZ() + 0.5, 1.0f, 0.0f);
        if (bicho instanceof Mob mob) {
            mob.setPersistenceRequired();
            mob.finalizeSpawn(mundo, mundo.getCurrentDifficultyAt(onde), EntitySpawnReason.TRIGGERED, null);
        }
        mundo.addFreshEntity(bicho);
    }

    /** A teia e as quatro vagens somem, e no lugar da teia nasce o macaco. */
    public static void nasceOMacaco(Level level, BlockPos onde) {
        if (!(level instanceof ServerLevel mundo)) return;
        level.removeBlock(onde, false);
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            level.removeBlock(onde.relative(lado), false);
        }
        for (BlockPos quina : quinas(onde)) esvazia(mundo, quina);

        var macaco = OccultaEntities.WINGED_MONKEY.create(mundo, EntitySpawnReason.TRIGGERED);
        if (macaco == null) return;
        macaco.snapTo(onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5, 0.0f, 0.0f);
        macaco.setPersistenceRequired();
        macaco.finalizeSpawn(mundo, mundo.getCurrentDifficultyAt(onde), EntitySpawnReason.TRIGGERED, null);
        mundo.addFreshEntity(macaco);
        pós(level, onde, 3.0, 2.0);
    }

    /** Tira o que um Apanha-Erva tem na boca, com os pós de gosma do original. */
    public static void esvazia(Level level, BlockPos onde) {
        if (!(level instanceof ServerLevel mundo)) return;
        if (!mundo.getBlockState(onde).is(OccultaBlocks.GRASSPER)) return;
        if (!(mundo.getBlockEntity(onde) instanceof GrassperBlockEntity planta)) return;
        if (planta.naBoca().isEmpty()) return;
        planta.põe(ItemStack.EMPTY);
        mundo.sendParticles(ParticleTypes.ITEM_SLIME, onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5,
                16, 1.0, 2.0, 1.0, 0.0);
    }

    /** Os pós de gosma que toda mutação deixa, com o barulho da gosma grande do original. */
    public static void pós(Level level, BlockPos onde, double largo, double alto) {
        if (!(level instanceof ServerLevel mundo)) return;
        mundo.sendParticles(ParticleTypes.ITEM_SLIME, onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5,
                8, largo, alto, largo, 0.0);
        mundo.playSound(null, onde, SoundEvents.SLIME_DEATH, SoundSource.NEUTRAL, 1.0f, 1.0f);
    }

    /** E os pós de feitiço, que são os das três mutações de chão. */
    public static void faísca(Level level, BlockPos onde) {
        if (!(level instanceof ServerLevel mundo)) return;
        mundo.sendParticles(net.minecraft.core.particles.SpellParticleOption.create(
                        ParticleTypes.INSTANT_EFFECT, 1.0f, 1.0f, 1.0f, 1.0f),
                onde.getX() + 0.5, onde.getY() + 1.0, onde.getZ() + 0.5, 8, 1.0, 1.0, 1.0, 0.0);
        mundo.playSound(null, onde, SoundEvents.SLIME_DEATH, SoundSource.NEUTRAL, 1.0f, 1.0f);
    }
}
