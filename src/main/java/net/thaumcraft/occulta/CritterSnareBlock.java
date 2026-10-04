package net.thaumcraft.occulta;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * O <b>Apanha-Bicho</b>: o {@code BlockCritterSnare} do Witchery.
 *
 * <p>Uma planta que <b>engole o que é pequeno</b>. Um morcego, uma lepisma, uma bolha de gosma ou de magma do
 * <b>menor tamanho</b> — e só do menor — desaparecem nela, e ela passa a mostrar o que apanhou.
 *
 * <p>De vez em quando ela faz o <b>barulho do bicho lá dentro</b>, uma vez em vinte e quatro batidas de
 * desenho. É o detalhe que faz dela o que ela é: não é um enfeite com uma cor diferente, é uma planta com um
 * morcego vivo dentro reclamando.
 *
 * <p>Para o soltar, <b>agacha-se e clica</b>. O morcego sai por cima; os outros saem ao lado de quem abriu, do
 * lado para onde ele está. E a gosma que não encontre um tamanho de bolha que lhe sirva sai como <b>bola de
 * gosma</b> — que é o original desistindo de um jeito honesto.
 *
 * <p>Ela <b>cai com o que apanhou dentro</b>: quebrá-la devolve o Apanha-Bicho com o bicho ainda nele, e
 * pô-lo noutro lugar põe o bicho com ele. É assim que se leva um morcego para longe.
 */
public class CritterSnareBlock extends VegetationBlock {
    public static final MapCodec<CritterSnareBlock> CODEC = simpleCodec(CritterSnareBlock::new);

    /** O que ela tem dentro. */
    public static final EnumProperty<Caught> APANHADO = EnumProperty.create("caught", Caught.class);

    /** De quantas em quantas batidas de desenho o bicho lá dentro reclama. */
    public static final int RECLAMA_UMA_EM = 24;

    /** Quantas vezes o original tenta achar uma bolha do tamanho certo antes de desistir. */
    public static final int TENTATIVAS = 20;

    /** E qual é esse tamanho: o menor que uma bolha tem. */
    public static final int MENOR = 1;

    /** Ela é quase o bloco todo: meia casa de folga de cada lado, e inteira de alto. */
    private static final VoxelShape FORMA = Block.box(0.8, 0.0, 0.8, 15.2, 16.0, 15.2);

    public CritterSnareBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(APANHADO, Caught.EMPTY));
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(APANHADO);
    }

    @Override
    protected VoxelShape getShape(BlockState feitio, BlockGetter level, BlockPos onde,
                                  CollisionContext quem) {
        return FORMA;
    }

    /**
     * <b>O que ela engole</b>, e o que ela deixa passar.
     *
     * <p>Um morcego e uma lepisma, sempre. Uma gosma ou um magma, <b>só do menor tamanho</b> — as grandes não
     * cabem, e é por isso que ela não é uma armadilha para o que importa.
     *
     * <p>E ela só apanha estando <b>vazia</b>: um Apanha-Bicho com um morcego dentro é só uma planta.
     */
    @Override
    protected void entityInside(BlockState feitio, Level level, BlockPos onde, Entity quem,
                                InsideBlockEffectApplier efeitos, boolean emCima) {
        if (level.isClientSide() || feitio.getValue(APANHADO) != Caught.EMPTY) return;
        if (quem == null || !quem.isAlive()) return;

        Caught oquê = oQueÉ(quem);
        if (oquê == null) return;
        level.setBlock(onde, feitio.setValue(APANHADO, oquê), Block.UPDATE_ALL);
        quem.discard();
    }

    /** Em qual dos quatro feitios este bicho cabe, ou {@code null} se ele não cabe em nenhum. */
    @Nullable
    public static Caught oQueÉ(Entity quem) {
        if (quem instanceof net.minecraft.world.entity.ambient.Bat) return Caught.BAT;
        if (quem instanceof net.minecraft.world.entity.monster.Silverfish) return Caught.SILVERFISH;
        if (quem instanceof net.minecraft.world.entity.monster.cubemob.MagmaCube magma) {
            return magma.getSize() == MENOR ? Caught.MAGMA_CUBE : null;
        }
        if (quem instanceof net.minecraft.world.entity.monster.cubemob.Slime gosma) {
            return gosma.getSize() == MENOR ? Caught.SLIME : null;
        }
        return null;
    }

    /**
     * <b>Agachar e clicar solta o bicho.</b>
     *
     * <p>Sem agachar não acontece nada: o original pede o agachar de propósito, para ninguém esvaziar um
     * Apanha-Bicho por acidente ao passar a mão por ele.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState feitio, Level level, BlockPos onde, Player quem,
                                               BlockHitResult acertou) {
        Caught tinha = feitio.getValue(APANHADO);
        if (level.isClientSide() || tinha == Caught.EMPTY || !quem.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!(level instanceof net.minecraft.server.level.ServerLevel mundo)) return InteractionResult.PASS;

        level.setBlock(onde, feitio.setValue(APANHADO, Caught.EMPTY), Block.UPDATE_ALL);
        solta(mundo, onde, quem, tinha);
        return InteractionResult.SUCCESS;
    }

    /**
     * Põe o bicho de volta no mundo.
     *
     * <p>O <b>morcego</b> sai <b>por cima</b>, que é o único jeito de ele não ficar entalado. Os outros saem
     * <b>ao lado de quem abriu</b>, no lado para onde ele está — uma lepisma que saísse debaixo dos pés de
     * quem a soltou seria uma crueldade mesmo para o original.
     *
     * <p>E a <b>gosma</b> é o caso curioso: o original sorteia até vinte bolhas à procura de uma do menor
     * tamanho e, não achando nenhuma, <b>larga uma bola de gosma no chão</b>. É uma desistência honesta, e
     * fica aqui — com a diferença de que o jogo de hoje deixa pôr o tamanho da bolha à mão, de modo que as
     * vinte tentativas nunca falham e a bola de gosma é um caminho que já não se percorre. Fica escrito, e
     * <b>declarado</b>.
     */
    public static void solta(net.minecraft.server.level.ServerLevel level, BlockPos onde, Player quem,
                             Caught oquê) {
        double meioX = onde.getX() + 0.5;
        double meioZ = onde.getZ() + 0.5;
        double x = quem.getX() < onde.getX() ? onde.getX() - 0.5 : onde.getX() + 1.5;
        double z = quem.getZ() < onde.getZ() ? onde.getZ() - 0.5 : onde.getZ() + 1.5;

        switch (oquê) {
            case EMPTY -> {
            }
            case BAT -> põe(level, EntityTypes.BAT, meioX, onde.getY() + 1.5, meioZ);
            case SILVERFISH -> põe(level, EntityTypes.SILVERFISH, x, quem.getY() + 0.5, z);
            case SLIME -> bolha(level, EntityTypes.SLIME, x, quem.getY() + 0.5, z,
                    meioX, onde.getY() + 1.5, meioZ, Items.SLIME_BALL);
            case MAGMA_CUBE -> bolha(level, EntityTypes.MAGMA_CUBE, x, quem.getY() + 0.5, z,
                    meioX, onde.getY() + 1.5, meioZ, Items.MAGMA_CREAM);
        }
    }

    private static void põe(net.minecraft.server.level.ServerLevel level, EntityType<?> qual,
                            double x, double y, double z) {
        Entity bicho = qual.create(level, EntitySpawnReason.TRIGGERED);
        if (bicho == null) return;
        bicho.snapTo(x, y, z, 0.0f, 0.0f);
        level.addFreshEntity(bicho);
    }

    /** Uma bolha do menor tamanho — ou, não a havendo, o que ela deixaria cair. */
    private static void bolha(net.minecraft.server.level.ServerLevel level, EntityType<?> qual,
                              double x, double y, double z,
                              double caiX, double caiY, double caiZ, net.minecraft.world.item.Item sobra) {
        Entity bicho = qual.create(level, EntitySpawnReason.TRIGGERED);
        if (bicho instanceof net.minecraft.world.entity.monster.cubemob.AbstractCubeMob bolha) {
            bolha.setSize(MENOR, true);
            bolha.snapTo(x, y, z, 0.0f, 0.0f);
            level.addFreshEntity(bolha);
            return;
        }
        var caiu = new net.minecraft.world.entity.item.ItemEntity(level, caiX, caiY, caiZ,
                new ItemStack(sobra));
        level.addFreshEntity(caiu);
    }

    /**
     * <b>O barulho do bicho lá dentro</b>, uma vez em vinte e quatro.
     *
     * <p>É o {@code randomDisplayTick} do original, e é o detalhe que faz da planta o que ela é. Um
     * Apanha-Bicho cheio não se vê de longe; ouve-se.
     */
    @Override
    public void animateTick(BlockState feitio, Level level, BlockPos onde, RandomSource sorte) {
        if (sorte.nextInt(RECLAMA_UMA_EM) != 0) return;
        var som = voz(feitio.getValue(APANHADO));
        if (som == null) return;
        level.playLocalSound(onde.getX() + 0.5, onde.getY() + 0.5, onde.getZ() + 0.5, som,
                SoundSource.BLOCKS, 0.5f, 0.4f / (sorte.nextFloat() * 0.4f + 0.8f), false);
    }

    /** A voz de cada um deles, que é a do próprio bicho. */
    @Nullable
    public static net.minecraft.sounds.SoundEvent voz(Caught oquê) {
        return switch (oquê) {
            case EMPTY -> null;
            case BAT -> net.minecraft.sounds.SoundEvents.BAT_AMBIENT;
            case SILVERFISH -> net.minecraft.sounds.SoundEvents.SILVERFISH_AMBIENT;
            case SLIME -> net.minecraft.sounds.SoundEvents.SLIME_SQUISH_SMALL;
            case MAGMA_CUBE -> net.minecraft.sounds.SoundEvents.MAGMA_CUBE_SQUISH_SMALL;
        };
    }

    /** Ela quer <b>chão de verdade</b> por baixo: o original pede uma face sólida. */
    @Override
    protected boolean mayPlaceOn(BlockState oquê, BlockGetter level, BlockPos onde) {
        return oquê.isSolid();
    }

    /**
     * E o <b>clique do meio</b> também traz o bicho: o feitio vai no item, como vai na queda.
     *
     * <p>A queda é a tabela de despojos que copia o feitio; isto é para quem joga no criativo não ficar com
     * um Apanha-Bicho vazio na mão depois de apontar para um cheio.
     */
    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos onde, BlockState feitio,
                                          boolean comAlma) {
        ItemStack qual = super.getCloneItemStack(level, onde, feitio, comAlma);
        Caught leva = feitio.getValue(APANHADO);
        if (leva != Caught.EMPTY) {
            qual.set(net.minecraft.core.component.DataComponents.BLOCK_STATE,
                    net.minecraft.world.item.component.BlockItemStateProperties.EMPTY
                            .with(APANHADO, leva));
        }
        return qual;
    }

    /** Os quatro bichos que ela apanha, e o vazio. */
    public enum Caught implements StringRepresentable {
        EMPTY("empty"),
        BAT("bat"),
        SILVERFISH("silverfish"),
        SLIME("slime"),
        MAGMA_CUBE("magmacube");

        private final String nome;

        Caught(String nome) {
            this.nome = nome;
        }

        @Override
        public String getSerializedName() {
            return this.nome;
        }
    }
}
