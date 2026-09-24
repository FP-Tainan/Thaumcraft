package net.thaumcraft.naturalis;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.item.KeyItem;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

/**
 * O Baú Arcano: o {@code ArcaneChestBlock} do Magia Naturalis 0.5.0. Feito de madeira-grande ou de prateada,
 * guarda muito mais que um baú comum, não liga para explosões e só abre para quem ele deixa.
 */
public class ArcaneChestBlock extends BaseEntityBlock {
    public static final MapCodec<ArcaneChestBlock> CODEC = simpleCodec(properties -> new ArcaneChestBlock(properties, Kind.GREATWOOD));

    /** O corpo do baú: um dedo para dentro de cada lado e um palmo abaixo do teto, como no original. */
    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0);

    /** Os dois baús do original, com o tamanho de cada um. */
    public enum Kind {
        GREATWOOD(6, 9, "block.thaumcraft.arcane_chest_greatwood"),
        SILVERWOOD(7, 11, "block.thaumcraft.arcane_chest_silverwood");

        private final int rows;
        private final int columns;
        private final String key;

        Kind(int rows, int columns, String key) {
            this.rows = rows;
            this.columns = columns;
            this.key = key;
        }

        public int rows() {
            return this.rows;
        }

        public int columns() {
            return this.columns;
        }

        public int size() {
            return this.rows * this.columns;
        }

        public String translationKey() {
            return this.key;
        }
    }

    private final Kind kind;

    public ArcaneChestBlock(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
        this.registerDefaultState(this.stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
    }

    public Kind kind() {
        return this.kind;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HorizontalDirectionalBlock.FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,
                context.getHorizontalDirection().getOpposite());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcaneChestBlockEntity(NaturalisBlocks.ARCANE_CHEST_ENTITY, this.kind, pos, state);
    }

    /** Quem desenha o baú é o renderizador, não o modelo do bloco. */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** Quem põe o baú fica dono dele — e o baú encolhido volta com tudo o que tinha. */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!(level.getBlockEntity(pos) instanceof ArcaneChestBlockEntity chest)) return;
        if (placer instanceof Player player) chest.claim(player);
        var guardado = stack.get(DataComponents.CONTAINER);
        if (guardado != null) guardado.copyInto(chest.getItems());
        var lista = stack.get(TCComponents.CHEST_ACCESS);
        if (lista != null) chest.restoreAccess(lista);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        // agachado o baú não abre, para poder pôr blocos em cima dele
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        if (!(level.getBlockEntity(pos) instanceof ArcaneChestBlockEntity chest)) return InteractionResult.PASS;
        if (!chest.mayOpen(player)) {
            if (!level.isClientSide()) denied(level, pos, player);
            return InteractionResult.SUCCESS;
        }
        if (!level.isClientSide()) player.openMenu(chest);
        return InteractionResult.SUCCESS;
    }

    /**
     * O {@code onBlockClicked} do original: bater no baú com uma chave arcana. Em branco, quem já entra grava a
     * chave para aquele baú; gravada, a chave dá a entrada a quem bateu com ela.
     */
    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) return;
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof KeyItem key)) return;
        if (!(level.getBlockEntity(pos) instanceof ArcaneChestBlockEntity chest)) return;
        CustomData custom = held.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = custom == null ? null : custom.copyTag();
        String loc = pos.getX() + "," + pos.getY() + "," + pos.getZ();
        boolean entra = chest.level(player) >= 0;
        if (tag == null || !tag.contains("location")) {
            // em branco: quem entra no baú grava uma chave para ele
            if (!entra) {
                denied(level, pos, player);
                return;
            }
            ItemStack made = new ItemStack(key);
            CompoundTag written = new CompoundTag();
            written.putString("location", loc);
            written.putByte("type", (byte) -1);
            made.set(DataComponents.CUSTOM_DATA, CustomData.of(written));
            if (!player.getAbilities().instabuild) held.shrink(1);
            if (!player.getInventory().add(made)) player.drop(made, false);
            level.playSound(null, pos, TCSounds.KEY.value(), SoundSource.BLOCKS, 1.0f, 0.9f);
            return;
        }
        if (!loc.equals(tag.getStringOr("location", ""))) {
            player.sendSystemMessage(Component.translatable("tc.key7")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
            return;
        }
        if (entra) {
            player.sendSystemMessage(Component.translatable("tc.key8")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
            return;
        }
        chest.allow(player.getUUID(), (byte) key.type());
        if (!player.getAbilities().instabuild) held.shrink(1);
        player.sendSystemMessage(Component.translatable("chat.thaumcraft.key.access.chest")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        level.playSound(null, pos, TCSounds.KEY.value(), SoundSource.BLOCKS, 1.0f, 0.9f);
    }

    @Override
    public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(
            Level level, BlockState state, net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
        return level.isClientSide()
                ? createTickerHelper(type, NaturalisBlocks.ARCANE_CHEST_ENTITY, ArcaneChestBlockEntity::lidAnimateTick)
                : null;
    }

    /** Para quem não entra, o baú nem se desgasta. */
    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof ArcaneChestBlockEntity chest && !chest.mayBreak(player)) return 0.0f;
        return super.getDestroyProgress(state, player, level, pos);
    }

    /** Quebrado, ele derrama o que tinha dentro. */
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        if (level.getBlockEntity(pos) instanceof ArcaneChestBlockEntity chest) {
            Containers.dropContents(level, pos, chest);
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.affectNeighborsAfterRemoval(state, level, pos, moved);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction side) {
        return net.minecraft.world.inventory.AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }

    private static void denied(Level level, BlockPos pos, Player player) {
        player.sendSystemMessage(Component.translatable("chat.thaumcraft.chest.access.denied")
                .withStyle(ChatFormatting.DARK_PURPLE));
        level.playSound(null, pos, TCSounds.DOOR_FAIL.value(), SoundSource.BLOCKS, 0.66f, 1.0f);
    }
}
