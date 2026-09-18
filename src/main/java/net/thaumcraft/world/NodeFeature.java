package net.thaumcraft.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.nodes.NodeModifier;
import net.thaumcraft.api.nodes.NodeType;
import net.thaumcraft.block.entity.NodeBlockEntity;
import net.thaumcraft.registry.TCBlocks;

import java.util.ArrayList;
import java.util.List;

/**
 * Como um nó de aura nasce no mundo, com os sorteios da 4.2.3.5.
 *
 * <p>A conta do original, na ordem: um em dezoito nós sai de um tipo fora do comum, e aí é escuro, instável
 * ou puro em três partes cada e faminto numa só; um em nove ganha um feitio — brilhante, pálido ou
 * esmaecido. O tamanho vem da aura da terra, com a metade sorteada por cima da metade certa. Os aspectos
 * começam com o da terra valendo dois, ganham até mais três sorteados, e no fim o total é repartido entre
 * eles conforme o peso de cada um.
 */
public class NodeFeature extends Feature<NoneFeatureConfiguration> {
    /** Um em dezoito, no original. É a raridade que manda no tipo e no feitio. */
    public static final int SPECIAL_RARITY = 18;

    public NodeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        RandomSource random = context.random();
        if (!level.getBlockState(pos).isAir()) return false;

        NodeType type = rollType(random);
        NodeModifier modifier = rollModifier(random);
        AspectList aspects = rollAspects(level, pos, random, type);

        level.setBlock(pos, TCBlocks.NODE.defaultBlockState(), 3);
        if (!(level.getBlockEntity(pos) instanceof NodeBlockEntity node)) return false;
        node.setup(aspects, type, modifier);
        return true;
    }

    /**
     * O {@code createRandomNodeAt} da 4.2.3.5 fora da geração do mundo: faz nascer um nó sorteado aqui, se
     * a casa estiver livre. É o que a esfera do foco Primordial deixa de herança, uma vez em cem.
     */
    public static boolean createRandomNodeAt(net.minecraft.world.level.Level level, BlockPos pos, RandomSource random) {
        if (!level.getBlockState(pos).isAir()) return false;
        level.setBlock(pos, TCBlocks.NODE.defaultBlockState(), 3);
        return setupNode(level, pos, random, false);
    }

    /**
     * O miolo do {@code createRandomNodeAt}: sorteia o nó e o põe no bloco que já está ali — o nó de aura
     * solto ou o nó do pinheiro-de-prata.
     *
     * @param silverwood o do pinheiro: sempre puro, e com um quarto da aura da terra
     */
    public static boolean setupNode(net.minecraft.world.level.LevelAccessor level, BlockPos pos, RandomSource random,
                                    boolean silverwood) {
        NodeType type = silverwood ? NodeType.PURE : rollType(random);
        NodeModifier modifier = rollModifier(random);
        AspectList aspects = rollAspects(level, pos, random, type, silverwood);
        if (!(level.getBlockEntity(pos) instanceof NodeBlockEntity node)) return false;
        node.setup(aspects, type, modifier);
        return true;
    }

    /** O tipo: quase sempre comum, e de vez em quando um dos outros. */
    public static NodeType rollType(RandomSource random) {
        if (random.nextInt(SPECIAL_RARITY) != 0) return NodeType.NORMAL;
        return switch (random.nextInt(10)) {
            case 0, 1, 2 -> NodeType.DARK;
            case 3, 4, 5 -> NodeType.UNSTABLE;
            case 6, 7, 8 -> NodeType.PURE;
            default -> NodeType.HUNGRY;
        };
    }

    /** O feitio: um em nove ganha um, e aí é um dos três em partes iguais. */
    public static NodeModifier rollModifier(RandomSource random) {
        if (random.nextInt(SPECIAL_RARITY / 2) != 0) return null;
        return switch (random.nextInt(3)) {
            case 0 -> NodeModifier.BRIGHT;
            case 1 -> NodeModifier.PALE;
            default -> NodeModifier.FADING;
        };
    }

    /** De que o nó é feito e quanto ele guarda. */
    public static AspectList rollAspects(net.minecraft.world.level.LevelAccessor level, BlockPos pos,
                                         RandomSource random, NodeType type) {
        return rollAspects(level, pos, random, type, false);
    }

    /** @param quarter o nó do pinheiro-de-prata fica com um quarto da aura da terra */
    public static AspectList rollAspects(net.minecraft.world.level.LevelAccessor level, BlockPos pos,
                                         RandomSource random, NodeType type, boolean quarter) {
        int aura = BiomeAura.auraOf(level.getBiome(pos));
        if (quarter) aura /= 4;
        int value = random.nextInt(Math.max(1, aura / 2)) + aura / 2;

        AspectList list = new AspectList();
        Aspect land = BiomeAura.aspectOf(level.getBiome(pos), random);
        if (land != null) {
            list.add(land, 2);
        } else {
            // terra sem marca: um composto e um primário, como o original faz quando não reconhece o bioma
            list.add(anyCompound(random), 1);
            list.add(anyPrimal(random), 1);
        }

        // até mais três aspectos, cada um com meia chance, e de vez em quando um composto
        for (int i = 0; i < 3; i++) {
            if (!random.nextBoolean()) continue;
            list.merge(random.nextInt(SPECIAL_RARITY) == 0 ? anyCompound(random) : anyPrimal(random), 1);
        }

        // o tipo do nó deixa a sua marca
        switch (type) {
            case HUNGRY -> {
                list.merge(Aspects.HUNGER, 2);
                if (random.nextBoolean()) list.merge(Aspects.GREED, 1);
            }
            case PURE -> list.merge(random.nextBoolean() ? Aspects.LIFE : Aspects.ORDER, 2);
            case DARK -> {
                if (random.nextBoolean()) list.merge(Aspects.DEATH, 1);
                if (random.nextBoolean()) list.merge(Aspects.UNDEAD, 1);
                if (random.nextBoolean()) list.merge(Aspects.ENTROPY, 1);
                if (random.nextBoolean()) list.merge(Aspects.DARKNESS, 1);
            }
            default -> {
            }
        }

        return share(list, value, random);
    }

    /**
     * Reparte o total entre os aspectos que o nó tem.
     *
     * <p>É a conta do original: cada aspecto tira um peso — mais alto para o que veio da terra, que entrou
     * valendo dois — e fica com a fatia dele do total.
     */
    private static AspectList share(AspectList list, int value, RandomSource random) {
        List<Aspect> aspects = list.getAspectsSorted();
        int[] weights = new int[aspects.size()];
        float total = 0.0f;
        for (int i = 0; i < weights.length; i++) {
            weights[i] = list.getAmount(aspects.get(i)) == 2 ? 50 + random.nextInt(25) : 25 + random.nextInt(50);
            total += weights[i];
        }
        AspectList shared = new AspectList();
        for (int i = 0; i < weights.length; i++) {
            int amount = (int) (weights[i] / total * value);
            if (amount > 0) shared.add(aspects.get(i), amount);
        }
        // um nó sem nada dentro não é nó: garante ao menos um ponto do primeiro aspecto
        if (shared.isEmpty() && !aspects.isEmpty()) shared.add(aspects.get(0), 1);
        return shared;
    }

    private static Aspect anyPrimal(RandomSource random) {
        List<Aspect> primals = Aspects.primals();
        return primals.get(random.nextInt(primals.size()));
    }

    private static Aspect anyCompound(RandomSource random) {
        List<Aspect> compounds = new ArrayList<>();
        for (Aspect aspect : Aspects.all()) {
            if (!aspect.isPrimal()) compounds.add(aspect);
        }
        return compounds.get(random.nextInt(compounds.size()));
    }
}
