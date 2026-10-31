package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * O <b>Galho Mutante</b>: o {@code ItemMutator} do Witchery.
 *
 * <p>Um galho com botões que <b>faz a natureza errar</b>. Toca-se com ele num bloco e, se o que estiver à
 * volta dele formar um dos <b>quinze desenhos</b>, aquilo vira outra coisa: um baú vira uma arca de
 * sanguessugas, uma cana vira uma sarça, uma teia com um lobo ao lado vira corujas.
 *
 * <p>É assim que se chega a quase tudo o que o ofício tem de vivo. O Apanha-Bicho, o Apanha-Erva, a Coruja,
 * o Sapo, a Lêndea, a Mentedrágora, o Macaco de Asas, a Rosa de Sangue, a Losna e as duas sarças <b>não
 * nascem no mundo e não saem de bancada nenhuma</b>: saem daqui.
 *
 * <p>Ele <b>gasta-se</b>: quinze toques e acabou. E como cada toque pede um desenho construído no chão,
 * quinze é mais do que parece.
 *
 * <h2>A ordem das quinze</h2>
 *
 * <p>As perguntas fazem-se na ordem do original, e ela importa em dois lugares. As <b>cinco teias</b>
 * disputam a mesma figura de partida, e a que se pergunta primeiro é a que ganha; e a <b>Lêndea</b> é o
 * {@code else} de tudo, de modo que ela só acontece quando nenhuma das catorze aconteceu.
 */
public class MutatingSprigItem extends Item {
    /** Quantos toques ele aguenta, e quanto cada um o gasta. */
    public static final int TOQUES = 15;
    public static final int POR_TOQUE = 1;

    public MutatingSprigItem(Properties properties) {
        super(properties);
    }

    /** O feitio dele: um só, de quinze toques, e fora do comum como no original. */
    public static Properties feitio(Properties properties) {
        return properties.stacksTo(1).durability(TOQUES)
                .rarity(net.minecraft.world.item.Rarity.UNCOMMON);
    }

    @Override
    public InteractionResult useOn(UseOnContext onde) {
        Level level = onde.getLevel();
        BlockPos casa = onde.getClickedPos();
        BlockState qual = level.getBlockState(casa);

        // o chão: grama vira micélio e volta, e terra debaixo de água vira barro
        if (qual.is(Blocks.GRASS_BLOCK) || qual.is(Blocks.MYCELIUM)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            level.setBlock(casa, qual.is(Blocks.GRASS_BLOCK)
                            ? Blocks.MYCELIUM.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState(),
                    Block.UPDATE_ALL);
            Mutations.faísca(level, casa);
            return gasta(onde, level);
        }
        if (qual.is(Blocks.DIRT) && level.getBlockState(casa.above()).is(Blocks.WATER)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            Mutations.barroEmCruz(level, casa);
            return gasta(onde, level);
        }

        // o baú armadilhado, que vira uma Arca de Sanguessugas no lugar dele
        if (Mutations.éBaúArmadilhado(level, casa)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            for (Direction lado : Direction.Plane.HORIZONTAL) {
                level.removeBlock(casa.relative(lado), false);
            }
            level.removeBlock(casa, false);
            level.setBlock(casa, OccultaBlocks.LEECH_CHEST.defaultBlockState(), Block.UPDATE_ALL);
            Mutations.pós(level, casa, 1.0, 2.0);
            return gasta(onde, level);
        }

        // a cana e o cato, que viram a coluna inteira em sarça
        if (Mutations.éCanaDeSarça(level, casa)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            Mutations.colunaVira(level, casa, Blocks.SUGAR_CANE, OccultaBlocks.ENDER_BRAMBLE,
                    Mutations.CANA_ALTA);
            Mutations.pós(level, casa, 3.0, 2.0);
            return gasta(onde, level);
        }

        // o trigo maduro, que vira Losna
        if (Mutations.éTrigoDeLosna(level, casa)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            for (Direction lado : Direction.Plane.HORIZONTAL) {
                level.removeBlock(casa.relative(lado), false);
            }
            level.setBlock(casa, OccultaBlocks.WORMWOOD.defaultBlockState(), Block.UPDATE_ALL);
            Mutations.pós(level, casa, 3.0, 2.0);
            return gasta(onde, level);
        }

        if (Mutations.éCatoDeSarça(level, casa)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            Mutations.colunaVira(level, casa, Blocks.CACTUS, OccultaBlocks.WILD_BRAMBLE,
                    Mutations.CATO_ALTO);
            Mutations.pós(level, casa, 3.0, 2.0);
            return gasta(onde, level);
        }

        // os dois baús que viram plantas
        if (Mutations.éBaúDeApanhaErva(level, casa)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            Mutations.osLadosViram(level, casa, OccultaBlocks.GRASSPER);
            Mutations.pós(level, casa, 3.0, 2.0);
            return gasta(onde, level);
        }
        if (Mutations.éArcaDeRosas(level, casa)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            Mutations.osLadosViram(level, casa, OccultaBlocks.BLOOD_ROSE);
            Mutations.pós(level, casa, 1.0, 2.0);
            return gasta(onde, level);
        }

        // e as cinco teias, pela ordem do original
        if (Mutations.éTeiaDeApanhaBicho(level, casa)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            Mutations.gasta(level, casa, net.minecraft.world.entity.monster.zombie.Zombie.class,
                    SoundEvents.ZOMBIE_DEATH);
            Mutations.osLadosViram(level, casa, OccultaBlocks.CRITTER_SNARE);
            Mutations.pós(level, casa, 3.0, 2.0);
            return gasta(onde, level);
        }
        if (Mutations.éTeiaDeCoruja(level, casa)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            Mutations.gasta(level, casa, net.minecraft.world.entity.animal.wolf.Wolf.class,
                    Mutations.morteDeLobo(level));
            Mutations.osLadosViramBicho(level, casa, CritterSnareBlock.Caught.BAT, OccultaEntities.OWL);
            Mutations.pós(level, casa, 3.0, 2.0);
            return gasta(onde, level);
        }
        if (Mutations.éTeiaDeSapo(level, casa)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            Mutations.gasta(level, casa, net.minecraft.world.entity.animal.feline.Ocelot.class,
                    SoundEvents.OCELOT_DEATH);
            Mutations.osLadosViramBicho(level, casa, CritterSnareBlock.Caught.SLIME, OccultaEntities.TOAD);
            Mutations.pós(level, casa, 3.0, 2.0);
            return gasta(onde, level);
        }
        if (Mutations.éTeiaDeMentedrágora(level, casa)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            Mutations.gasta(level, casa, net.minecraft.world.entity.monster.Creeper.class,
                    SoundEvents.CREEPER_DEATH);
            Mutations.gasta(level, casa, MandrakeEntity.class, SoundEvents.GHAST_DEATH);
            level.removeBlock(casa, false);
            for (Direction lado : Direction.Plane.HORIZONTAL) {
                level.setBlock(casa.relative(lado), OccultaBlocks.MINDRAKE.defaultBlockState(),
                        Block.UPDATE_ALL);
            }
            for (BlockPos quina : Mutations.quinas(casa)) Mutations.esvazia(level, quina);
            Mutations.pós(level, casa, 3.0, 2.0);
            return gasta(onde, level);
        }
        if (Mutations.éTeiaDeMacaco(level, casa)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            Mutations.gasta(level, casa, net.thaumcraft.occulta.familiar.OwlEntity.class,
                    SoundEvents.CREEPER_DEATH);
            Mutations.gasta(level, casa, net.minecraft.world.entity.animal.wolf.Wolf.class,
                    SoundEvents.GHAST_DEATH);
            Mutations.nasceOMacaco(level, casa);
            return gasta(onde, level);
        }

        /*
         * E a <b>Lêndea</b>, que é o {@code else} de tudo: o Apanha-Bicho do meio é que muda, e as duas
         * trepadeiras ao lado somem com a troca.
         */
        if (Mutations.éLaçoDeLêndea(level, casa)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            for (Direction lado : Direction.Plane.HORIZONTAL) {
                level.removeBlock(casa.relative(lado), false);
            }
            Mutations.viraBicho(level, casa, CritterSnareBlock.Caught.SILVERFISH, OccultaEntities.LOUSE);
            for (BlockPos quina : Mutations.quinas(casa)) Mutations.esvazia(level, quina);
            Mutations.pós(level, casa, 3.0, 2.0);
            return gasta(onde, level);
        }

        return InteractionResult.PASS;
    }

    /** Um toque a menos, e o galho parte-se ao fim de quinze. */
    private static InteractionResult gasta(UseOnContext onde, Level level) {
        if (!(level instanceof ServerLevel mundo)) return InteractionResult.SUCCESS;
        @Nullable Player quem = onde.getPlayer();
        if (quem == null || !quem.hasInfiniteMaterials()) {
            ItemStack galho = onde.getItemInHand();
            galho.hurtAndBreak(POR_TOQUE, mundo, quem instanceof ServerPlayer dono ? dono : null, item -> {
            });
        }
        return InteractionResult.SUCCESS;
    }

}
