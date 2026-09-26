package net.thaumcraft.shattered;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A Porta Antiga: a que já estava no mundo antes de haver quem a visse.
 *
 * <p><b>Isto é do porte, e não do original.</b> Quem manda pediu <i>umas portas antigas pelo mundo que só dê pra
 * ver de óculos</i>, e mostrou o que queria: uma ombreira de pedra de pé num descampado, vazia; e, com os
 * {@link VeilSight Óculos do Véu} no rosto, uma porta dentro dela.
 *
 * <p>Por isso esta porta não se desenha como as outras. O bloco dela é invisível — quem a desenha é o
 * {@code DimensionalPortalRenderer}, e só a quem a possa ver. E, porque uma parede invisível seria uma
 * armadilha e não um segredo, ela também <b>não tem corpo</b>: quem passa pela ombreira sem os óculos atravessa-a
 * e não dá por nada. Abrir, só quem a vê.
 */
public class AncientDoorBlock extends DimensionalDoorBlock {
    public static final MapCodec<AncientDoorBlock> CODEC =
            com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(instance -> instance.group(
                    BlockSetType.CODEC.fieldOf("block_set_type").forGetter(bloco -> bloco.type()),
                    propertiesCodec()
            ).apply(instance, AncientDoorBlock::new));

    public AncientDoorBlock(BlockSetType type, Properties properties) {
        super(type, properties);
    }

    @Override
    public MapCodec<? extends DoorBlock> codec() {
        return CODEC;
    }

    /** Quem a desenha é o miolo dela, e só a quem a possa ver. */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    /** Sem corpo: uma parede invisível seria uma armadilha, e o que se quer é um segredo. */
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    /**
     * O corpo de ver: quem tem os óculos agarra-a com o rato, quem não tem não lhe acerta nem lhe vê a caixa.
     *
     * <p>É a única parte disto que sabe de quem está a olhar: a forma de um bloco não costuma saber, mas a
     * conta traz quem a pediu, e aqui isso é o que faz a porta ser mesmo um segredo e não um contorno a pairar
     * no meio da ombreira.
     */
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (context instanceof net.minecraft.world.phys.shapes.EntityCollisionContext quem
                && quem.getEntity() instanceof Player jogador && !VeilSight.can(jogador)) {
            return Shapes.empty();
        }
        return super.getShape(state, level, pos, context);
    }

    /** Abrir, só quem a vê: quem não tem os óculos não lhe pega. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player quem,
                                               BlockHitResult bateu) {
        if (!VeilSight.can(quem)) return InteractionResult.PASS;
        return super.useWithoutItem(state, level, pos, quem, bateu);
    }
}
