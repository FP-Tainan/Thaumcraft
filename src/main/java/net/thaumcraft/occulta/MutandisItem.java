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
     * <p><b>Declarado:</b> no original esta mutação pede ainda <b>quatro Apanha-Ervas</b> nas diagonais, cada
     * um segurando uma <b>pérola do fim</b>, e quem a faz é a <b>Vara Mutante</b> e não o Mutandis. O
     * Apanha-Erva e a vara não estão portados; o Mutandis Extremis faz aqui o que a vara fazia lá, como já
     * faz com o Baú de Sanguessugas. Quando eles vierem, a conta volta ao que era.
     */
    public static boolean éCanaDeSarça(Level level, BlockPos onde) {
        return level.getBlockState(onde).is(Blocks.SUGAR_CANE) && cercada(level, onde);
    }

    /**
     * E um <b>cato</b> do mesmo jeito vira <b>Sarça Selvagem</b>, até quatro de altura.
     *
     * <p><b>Declarado:</b> no original ela pede ainda <b>dois Apanha-Ervas com farinha de osso</b> e
     * <b>dois com pó de blaze</b> nas diagonais. Vale aqui o mesmo que para a cana.
     */
    public static boolean éCatoDeSarça(Level level, BlockPos onde) {
        return level.getBlockState(onde).is(Blocks.CACTUS) && cercada(level, onde);
    }

    /** As quatro de musgo ao lado e as quatro de água na quina de baixo. */
    private static boolean cercada(Level level, BlockPos onde) {
        for (net.minecraft.core.Direction lado : net.minecraft.core.Direction.Plane.HORIZONTAL) {
            if (!level.getBlockState(onde.relative(lado)).is(OccultaBlocks.SPANISH_MOSS)) return false;
            if (!level.getBlockState(onde.relative(lado).below()).is(Blocks.WATER)) return false;
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
    private static boolean éBaúDeRosas(Level level, BlockPos onde) {
        if (!level.getBlockState(onde).is(Blocks.CHEST)) return false;
        if (!level.getBlockState(onde.below()).is(Blocks.WATER)) return false;

        int flores = 0;
        for (net.minecraft.core.Direction lado : net.minecraft.core.Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(onde.relative(lado)).is(net.minecraft.tags.BlockTags.SMALL_FLOWERS)) {
                flores++;
            }
        }
        if (flores < 4) return false;

        // e o baú tem de estar vazio: o que lá estiver não se perde por um descuido
        if (!(level.getBlockEntity(onde)
                instanceof net.minecraft.world.level.block.entity.ChestBlockEntity baú)) {
            return false;
        }
        for (int casa = 0; casa < baú.getContainerSize(); casa++) {
            if (!baú.getItem(casa).isEmpty()) return false;
        }
        return true;
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
