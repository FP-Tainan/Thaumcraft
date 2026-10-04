package net.thaumcraft.occulta;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.occulta.brew.WitchProjectile;

/**
 * A <b>Mina de Planta</b>: o {@code BlockPlantMine} do Witchery.
 *
 * <p>Uma flor que <b>não é uma flor</b>. Ela parece uma papoula, um dente-de-leão ou um arbusto seco — as
 * três plantas mais inofensivas que o jogo tem — e quem passa por cima dela leva o que o projétil de bruxa
 * leva: <b>teia</b>, <b>tinta</b>, <b>espinhos</b> ou um <b>galho</b> crescendo debaixo dos pés.
 *
 * <p>São <b>doze</b>, que é o que dá cruzar as três caras com os quatro efeitos — e a cara <b>não diz nada</b>
 * sobre o efeito. Uma papoula de teias e uma papoula de espinhos são a mesma papoula. Quem a planta sabe o
 * que ela é; quem passa, não.
 *
 * <p>E ela é <b>duríssima de explodir</b>: mil de resistência, mais do que a obsidiana. Não se abre caminho
 * num campo de minas com TNT.
 *
 * <p>Nada cai dela ao ser quebrada: o original devolve item nenhum, de modo que uma mina desarmada é uma mina
 * <b>gasta</b>.
 */
public class PlantMineBlock extends VegetationBlock {
    public static final MapCodec<PlantMineBlock> CODEC = simpleCodec(PlantMineBlock::new);

    /** O que ela parece, e o que ela faz. */
    public static final EnumProperty<Look> CARA = EnumProperty.create("look", Look.class);
    public static final EnumProperty<Effect> EFEITO = EnumProperty.create("effect", Effect.class);

    /** A forma dela: quase nada, e baixa. */
    private static final VoxelShape FORMA = Block.box(5.0, 0.0, 5.0, 11.0, 9.6, 11.0);

    public PlantMineBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(CARA, Look.ROSE).setValue(EFEITO, Effect.WEBS));
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CARA, EFEITO);
    }

    @Override
    protected VoxelShape getShape(BlockState feitio, BlockGetter level, BlockPos onde,
                                  CollisionContext quem) {
        return FORMA;
    }

    /**
     * <b>O pé que a encontra.</b>
     *
     * <p>Ela some primeiro e explode depois — é a ordem do original, e importa: o galho que cresce no lugar
     * dela precisa do lugar vazio.
     */
    @Override
    protected void entityInside(BlockState feitio, Level level, BlockPos onde, Entity quem,
                                InsideBlockEffectApplier efeitos, boolean emCima) {
        if (!(level instanceof ServerLevel mundo)) return;

        Effect oquê = feitio.getValue(EFEITO);
        mundo.removeBlock(onde, false);
        mundo.sendParticles(ParticleTypes.ENCHANTED_HIT, onde.getX() + 0.5, onde.getY() + 0.05,
                onde.getZ() + 0.5, 20, 0.5, 1.0, 0.5, 0.0);
        mundo.playSound(null, onde, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 0.5f,
                0.4f / (mundo.getRandom().nextFloat() * 0.4f + 0.8f));

        AABB daqui = new AABB(onde);
        switch (oquê) {
            case WEBS -> WitchProjectile.teia(mundo, onde, Direction.UP, false);
            case INK -> WitchProjectile.tinta(mundo, onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5,
                    quem, daqui, false);
            case THORNS -> WitchProjectile.cato(mundo, onde, WitchProjectile.CATO);
            case SPROUTING -> WitchProjectile.galho(mundo, onde, Direction.UP, 10, daqui);
        }
    }

    /** Em que chão ela pega: os cinco do original, e nenhum outro. */
    @Override
    protected boolean mayPlaceOn(BlockState oquê, BlockGetter level, BlockPos onde) {
        return oquê.is(Blocks.GRASS_BLOCK) || oquê.is(Blocks.DIRT) || oquê.is(Blocks.FARMLAND)
                || oquê.is(Blocks.SAND) || oquê.is(Blocks.MYCELIUM);
    }

    /** E o clique do meio traz a mina que está ali, com a cara e o efeito dela. */
    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos onde, BlockState feitio,
                                          boolean comAlma) {
        ItemStack qual = super.getCloneItemStack(level, onde, feitio, comAlma);
        qual.set(net.minecraft.core.component.DataComponents.BLOCK_STATE,
                net.minecraft.world.item.component.BlockItemStateProperties.EMPTY
                        .with(CARA, feitio.getValue(CARA)).with(EFEITO, feitio.getValue(EFEITO)));
        return qual;
    }

    /** As três caras dela, que são três plantas do próprio jogo. */
    public enum Look implements StringRepresentable {
        ROSE("rose"),
        DANDELION("dandelion"),
        GRASS("grass");

        private final String nome;

        Look(String nome) {
            this.nome = nome;
        }

        @Override
        public String getSerializedName() {
            return this.nome;
        }
    }

    /** E os quatro efeitos, que são os do projétil de bruxa. */
    public enum Effect implements StringRepresentable {
        WEBS("webs"),
        INK("ink"),
        THORNS("thorns"),
        SPROUTING("sprouting");

        private final String nome;

        Effect(String nome) {
            this.nome = nome;
        }

        @Override
        public String getSerializedName() {
            return this.nome;
        }
    }
}
