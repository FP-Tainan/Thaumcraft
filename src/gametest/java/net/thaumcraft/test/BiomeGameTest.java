package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.block.ManaPodBlock;
import net.thaumcraft.block.entity.ManaPodBlockEntity;
import net.thaumcraft.item.CrystalEssenceItem;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.world.BiomeAura;
import net.thaumcraft.world.BiomePainter;
import net.thaumcraft.world.TCBiomes;

/** Os biomas da 4.2.3.5 e o que nasce neles: a vagem de mana e o cogumelo-vis. */
public class BiomeGameTest {
    @GameTest
    public void theMagicalBiomesExistAndGrowInTheOverworld(GameTestHelper helper) {
        var biomes = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
        for (var key : new net.minecraft.resources.ResourceKey[]{TCBiomes.MAGICAL_FOREST, TCBiomes.TAINTED_LAND, TCBiomes.EERIE}) {
            if (biomes.get(key).isEmpty()) helper.fail("falta o bioma " + key);
        }
        // o mundo de teste é plano: a conferência é na lista de biomas da superfície de verdade
        var used = net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD.usedBiomes().toList();
        boolean forest = used.contains(TCBiomes.MAGICAL_FOREST);
        boolean taint = used.contains(TCBiomes.TAINTED_LAND);
        boolean eerie = used.contains(TCBiomes.EERIE);
        if (!forest || !taint) helper.fail("a Floresta Mágica e a Terra Maculada nascem no mundo");
        if (eerie) helper.fail("o Sinistro só nasce pintado pelo nó sombrio");
        var magical = biomes.getOrThrow(TCBiomes.MAGICAL_FOREST);
        // a Floresta Mágica é floresta (120) e mágica (100): média 110, como no original
        if (BiomeAura.auraOf(magical) != 110) helper.fail("aura da Floresta Mágica: " + BiomeAura.auraOf(magical));
        helper.succeed();
    }

    @GameTest
    public void aNodeCanPaintTheBiome(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        BiomePainter.paint(helper.getLevel(), pos, TCBiomes.EERIE);
        // o getBiome do jogo borra entre colunas vizinhas; aqui se lê o ponto exato
        if (!exact(helper, pos).is(TCBiomes.EERIE)) helper.fail("a coluna ficou Sinistra");
        BiomePainter.paint(helper.getLevel(), pos, TCBiomes.MAGICAL_FOREST);
        if (!exact(helper, pos.above(3)).is(TCBiomes.MAGICAL_FOREST)) helper.fail("e depois Floresta Mágica, a coluna toda");
        helper.succeed();
    }

    private static net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> exact(GameTestHelper helper, BlockPos pos) {
        return helper.getLevel().getNoiseBiome(net.minecraft.core.QuartPos.fromBlock(pos.getX()), net.minecraft.core.QuartPos.fromBlock(pos.getY()),
                net.minecraft.core.QuartPos.fromBlock(pos.getZ()));
    }

    @GameTest
    public void theManaPodGrowsAndGivesBeans(GameTestHelper helper) {
        BlockPos log = new BlockPos(1, 3, 1);
        BlockPos podPos = log.below();
        BiomePainter.paint(helper.getLevel(), helper.absolutePos(podPos), TCBiomes.MAGICAL_FOREST);
        helper.setBlock(log, Blocks.OAK_LOG.defaultBlockState());
        helper.setBlock(podPos, TCBlocks.MANA_POD.defaultBlockState());
        if (!(helper.getBlockEntity(podPos, ManaPodBlockEntity.class) instanceof ManaPodBlockEntity pod)) {
            throw helper.assertionException("a vagem tem a entidade dela");
        }
        for (int i = 0; i < 7; i++) pod.checkGrowth();
        var state = helper.getBlockState(podPos);
        if (state.getValue(ManaPodBlock.AGE) != 7) helper.fail("cresce até sete: " + state.getValue(ManaPodBlock.AGE));
        if (pod.aspect == null) helper.fail("no três ela escolhe o aspecto");
        if (state.getLightEmission() != 7) helper.fail("e brilha do tamanho que tem");
        var drops = net.minecraft.world.level.block.Block.getDrops(state, helper.getLevel(), helper.absolutePos(podPos), pod);
        if (drops.isEmpty() || !drops.get(0).is(net.thaumcraft.registry.TCItems.MANA_BEAN)) helper.fail("quebrada, dá feijão");
        if (CrystalEssenceItem.aspectOf(drops.get(0)) != pod.aspect) helper.fail("do aspecto dela");
        // sem a tora, ela cai
        helper.setBlock(log, Blocks.AIR.defaultBlockState());
        helper.succeedWhen(() -> helper.assertBlockNotPresent(TCBlocks.MANA_POD, podPos));
    }

    @GameTest
    public void theVishroomMakesYouDizzy(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 0, 1), Blocks.GRASS_BLOCK.defaultBlockState());
        helper.setBlock(new BlockPos(1, 1, 1), TCBlocks.VISHROOM.defaultBlockState());
        var pig = helper.spawn(EntityTypes.PIG, new BlockPos(1, 1, 1));
        helper.succeedWhen(() -> {
            if (!pig.hasEffect(MobEffects.NAUSEA)) throw helper.assertionException("quem encosta fica tonto");
        });
    }

    /**
     * O jogo recusa gerar o mundo se dois biomas põem as mesmas decorações em ordens diferentes; o mundo de teste é
     * plano e não pega isso, então aqui a conta é feita com todos os biomas da superfície.
     */
    @GameTest
    public void theDecorationsKeepTheGameOrder(GameTestHelper helper) {
        var biomes = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
        var used = net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD.usedBiomes()
                .map(biomes::getOrThrow).map(h -> (net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome>) h).toList();
        try {
            net.minecraft.world.level.biome.FeatureSorter.buildFeaturesPerStep(used, h -> h.value().getGenerationSettings().features(), true);
        } catch (IllegalStateException e) {
            helper.fail("ordem das decorações em conflito: " + e.getMessage());
        }
        helper.succeed();
    }
}
