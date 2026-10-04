package net.thaumcraft.occulta.door;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.phys.BlockHitResult;
import net.thaumcraft.occulta.OccultaItems;

/**
 * A <b>Porta de Sorveira</b>: a metade trancada do {@code BlockWitchDoor} do Witchery.
 *
 * <p>Ela <b>não abre</b> para quem não traz a chave dela. E a chave dela é uma só: a que <b>nasceu com a
 * porta</b>, no instante em que alguém a pôs no chão, marcada com aquelas três contas e aquele mundo. Não há
 * como fazer outra.
 *
 * <p>E quem a quebrar sem a chave fica com <b>vinte e quatro gravetos</b>: a porta não volta. É a única
 * tranca deste mod que não depende de nada vivo — nem de ofício, nem de poder, nem de altar. É madeira, e a
 * chave está no bolso de alguém.
 *
 * <p>Repare no que isso faz com uma casa: a porta de sorveira é a <b>fechadura</b> do ofício, e a chave é
 * uma coisa que se perde, se rouba e se dá. O Witchery não a faz cara — seis tábuas — porque o preço dela não
 * é o da matéria: é ter de <b>cuidar</b> de uma chave.
 */
public class RowanDoorBlock extends DoorBlock {
    public static final MapCodec<RowanDoorBlock> CODEC = simpleCodec(RowanDoorBlock::new);

    /** O que sobra de uma porta arrombada. */
    public static final int GRAVETOS = 24;

    public RowanDoorBlock(Properties properties) {
        super(BlockSetType.OAK, properties);
    }

    @Override
    public MapCodec<? extends DoorBlock> codec() {
        return CODEC;
    }

    /** Sem a chave, a porta não se mexe. */
    @Override
    protected InteractionResult useWithoutItem(BlockState feitio, Level level, BlockPos onde, Player quem,
                                               BlockHitResult onde2) {
        if (!DoorKeys.temChave(quem, DoorKeys.onde(level, onde, feitio))) {
            if (!level.isClientSide()) {
                level.playSound(null, onde, SoundEvents.WOODEN_DOOR_CLOSE, SoundSource.BLOCKS,
                        0.5f, 1.4f);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(feitio, level, onde, quem, onde2);
    }

    /**
     * <b>Quebrada, ela não volta a ser porta</b> — a não ser para quem tinha a chave.
     *
     * <p>No original a queda da porta é dada à mão no {@code onBlockHarvested}, e a tabela de quedas dela é
     * <b>vazia</b>. Aqui é igual: a tabela não dá nada, e quem larga a porta ou os gravetos é este método.
     */
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos onde, BlockState feitio, Player quem) {
        if (level instanceof ServerLevel mundo && !quem.isCreative()) {
            boolean tinha = DoorKeys.temChave(quem, DoorKeys.onde(level, onde, feitio));
            Block.popResource(mundo, onde, tinha
                    ? new ItemStack(OccultaItems.ROWAN_DOOR)
                    : new ItemStack(Items.STICK, GRAVETOS));
        }
        return super.playerWillDestroy(level, onde, feitio, quem);
    }
}
