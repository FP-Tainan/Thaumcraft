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
