package net.thaumcraft.occulta.curse;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * As <b>gêmeas amaldiçoadas</b> das peças do mundo: os {@code BlockButtonBase}, {@code BlockLeverBase},
 * {@code BlockDoorBase} e {@code BlockPressurePlateBase} do Witchery.
 *
 * <p>Cada uma é, em tudo o que se vê, a peça que ela copia — mesmo desenho, mesma queda, mesmo barulho. O
 * que ela tem a mais é uma <b>alma</b> com um cozimento preso dentro e a lembrança de quem o atirou.
 *
 * <p>Mexida, ela dispara o cozimento em quem mexeu e <b>volta a ser a peça comum</b>. É por isso que elas não
 * aparecem no criativo nem se fabricam: ninguém as põe no mundo — elas <b>acontecem</b> a uma peça que já lá
 * estava.
 *
 * <p>Nenhuma delas tem item próprio. Quebrada, cai a peça comum; apanhada com o botão do meio, vem a peça
 * comum. Do lado de fora, a maldição não existe.
 */
public final class CursedTwins {
    private CursedTwins() {
    }

    /** O botão amaldiçoado, que dispara ao ser apertado. */
    public static class CursedButton extends ButtonBlock implements EntityBlock {
        private final Supplier<Block> volta;

        public CursedButton(BlockSetType tipo, int quanto, Supplier<Block> volta, Properties properties) {
            super(tipo, quanto, properties);
            this.volta = volta;
        }

        @Override
        public @Nullable BlockEntity newBlockEntity(BlockPos onde, BlockState feitio) {
            return new CursedBlockEntity(onde, feitio);
        }

        @Override
        protected InteractionResult useWithoutItem(BlockState feitio, Level level, BlockPos onde, Player quem,
                                                   BlockHitResult bateu) {
            InteractionResult deu = super.useWithoutItem(feitio, level, onde, quem, bateu);
            if (level instanceof ServerLevel mundo) {
                CursedBlocks.dispara(mundo, onde, quem, this.volta.get());
            }
            return deu;
        }

        @Override
        protected ItemStack getCloneItemStack(LevelReader level, BlockPos onde, BlockState feitio,
                                              boolean incluiAlma) {
            return new ItemStack(this.volta.get());
        }
    }

    /** A alavanca amaldiçoada, que dispara ao ser puxada. */
    public static class CursedLever extends LeverBlock implements EntityBlock {
        private final Supplier<Block> volta;

        public CursedLever(Supplier<Block> volta, Properties properties) {
            super(properties);
            this.volta = volta;
        }

        @Override
        public @Nullable BlockEntity newBlockEntity(BlockPos onde, BlockState feitio) {
            return new CursedBlockEntity(onde, feitio);
        }

        @Override
        protected InteractionResult useWithoutItem(BlockState feitio, Level level, BlockPos onde, Player quem,
                                                   BlockHitResult bateu) {
            InteractionResult deu = super.useWithoutItem(feitio, level, onde, quem, bateu);
            if (level instanceof ServerLevel mundo) {
                CursedBlocks.dispara(mundo, onde, quem, this.volta.get());
            }
            return deu;
        }

        @Override
        protected ItemStack getCloneItemStack(LevelReader level, BlockPos onde, BlockState feitio,
                                              boolean incluiAlma) {
            return new ItemStack(this.volta.get());
        }
    }

    /**
     * A porta amaldiçoada, que dispara ao ser aberta.
     *
     * <p>A maldição mora na <b>metade de baixo</b>, como a chave da porta de sorveira: a porta tem duas casas
     * e a alma é uma só.
     */
    public static class CursedDoor extends DoorBlock implements EntityBlock {
        private final Supplier<Block> volta;

        public CursedDoor(BlockSetType tipo, Supplier<Block> volta, Properties properties) {
            super(tipo, properties);
            this.volta = volta;
        }

        @Override
        public @Nullable BlockEntity newBlockEntity(BlockPos onde, BlockState feitio) {
            return new CursedBlockEntity(onde, feitio);
        }

        @Override
        protected InteractionResult useWithoutItem(BlockState feitio, Level level, BlockPos onde, Player quem,
                                                   BlockHitResult bateu) {
            InteractionResult deu = super.useWithoutItem(feitio, level, onde, quem, bateu);
            if (level instanceof ServerLevel mundo) {
                CursedBlocks.dispara(mundo, onde, quem, this.volta.get());
            }
            return deu;
        }

        @Override
        protected ItemStack getCloneItemStack(LevelReader level, BlockPos onde, BlockState feitio,
                                              boolean incluiAlma) {
            return new ItemStack(this.volta.get());
        }
    }

    /**
     * E a placa de pressão amaldiçoada, que é a pior das quatro.
     *
     * <p>Ela dispara <b>sozinha</b>, em quem lhe pisar em cima, sem que ninguém tenha de querer nada. As
     * outras três precisam que alguém decida mexer; esta só precisa que alguém <b>passe</b>.
     */
    public static class CursedPlate extends PressurePlateBlock implements EntityBlock {
        private final Supplier<Block> volta;

        public CursedPlate(BlockSetType tipo, Supplier<Block> volta, Properties properties) {
            super(tipo, properties);
            this.volta = volta;
        }

        @Override
        public @Nullable BlockEntity newBlockEntity(BlockPos onde, BlockState feitio) {
            return new CursedBlockEntity(onde, feitio);
        }

        /**
         * O {@code onEntityCollidedWithBlock}: ela dispara quando <b>passa de solta a pisada</b>, e não a
         * cada batida em que há alguém em cima. Senão, quem ficasse parado nela levava o cozimento sem parar.
         */
        @Override
        protected void entityInside(BlockState feitio, Level level, BlockPos onde, Entity quem,
                                    InsideBlockEffectApplier efeitos, boolean oquê) {
            boolean estavaSolta = !feitio.getValue(POWERED);
            super.entityInside(feitio, level, onde, quem, efeitos, oquê);
            if (!estavaSolta || !(level instanceof ServerLevel mundo)) return;
            if (!mundo.getBlockState(onde).getValue(POWERED)) return;
            CursedBlocks.dispara(mundo, onde, quem, this.volta.get());
        }

        @Override
        protected ItemStack getCloneItemStack(LevelReader level, BlockPos onde, BlockState feitio,
                                              boolean incluiAlma) {
            return new ItemStack(this.volta.get());
        }
    }
}
