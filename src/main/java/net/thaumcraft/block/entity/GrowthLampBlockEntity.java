package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.aspects.EssentiaTransport;
import net.thaumcraft.block.ArcaneLampBlock;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A lâmpada do crescimento: o {@code TileArcaneLampGrowth} da 4.2.3.5.
 *
 * <p>Bebe Herba pelo lado em que está presa: cada ponto vale cem empurrões de crescimento, e ela guarda mais um
 * de reserva. Enquanto tem carga, a cada tique escolhe uma coluna ao acaso num quadrado de treze de lado e desce
 * por ela até achar uma planta que ainda não esteja madura, a menos de seis blocos, e a faz crescer um passo. O
 * que acabou de crescer solta uma faísca verde.
 *
 * <p>No 1.7.10 o empurrão era um {@code scheduleBlockUpdate}, que nas plantas é o próprio tique de crescimento;
 * no jogo de hoje esse tique se chama {@code randomTick}, e é ele que a lâmpada chama.
 */
public class GrowthLampBlockEntity extends BlockEntity implements EssentiaTransport {
    private static final int DISTANCE = 6;

    private boolean reserve;
    private int charges = -1;
    private int drawDelay;
    private final List<BlockPos> checklist = new ArrayList<>();
    @Nullable
    private BlockPos last;
    @Nullable
    private BlockState lastState;

    public GrowthLampBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.GROWTH_LAMP, pos, state);
    }

    private Direction facing() {
        return this.getBlockState().getValue(ArcaneLampBlock.FACING);
    }

    public int charges() {
        return this.charges;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GrowthLampBlockEntity lamp) {
        if (lamp.charges <= 0) {
            if (lamp.reserve) {
                lamp.charges = 100;
                lamp.reserve = false;
                lamp.shine(level, pos);
            } else if (lamp.drawEssentia(level, pos)) {
                lamp.charges = 100;
                lamp.shine(level, pos);
            }
        }
        if (!lamp.reserve && lamp.drawEssentia(level, pos)) lamp.reserve = true;
        if (lamp.charges == 0) {
            lamp.charges = -1;
            lamp.shine(level, pos);
        }
        if (lamp.charges > 0) lamp.updatePlant((ServerLevel) level, pos);
    }

    /** Acende ou apaga conforme a carga, e guarda. */
    private void shine(Level level, BlockPos pos) {
        this.setChanged();
        BlockState state = level.getBlockState(pos);
        boolean lit = this.charges > 0;
        if (state.hasProperty(ArcaneLampBlock.LIT) && state.getValue(ArcaneLampBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(ArcaneLampBlock.LIT, lit), Block.UPDATE_ALL);
        }
    }

    private void updatePlant(ServerLevel level, BlockPos pos) {
        // o que cresceu no último empurrão solta a faísca verde
        if (this.last != null && level.getBlockState(this.last) != this.lastState) {
            net.thaumcraft.net.TCNetwork.blockSparkle(level, this.last, 0x40FF40);
            this.lastState = level.getBlockState(this.last);
        }
        if (this.checklist.isEmpty()) {
            for (int a = -DISTANCE; a <= DISTANCE; a++) {
                for (int b = -DISTANCE; b <= DISTANCE; b++) {
                    this.checklist.add(pos.offset(a, DISTANCE, b));
                }
            }
            Collections.shuffle(this.checklist, new java.util.Random(level.getRandom().nextLong()));
        }
        BlockPos column = this.checklist.remove(0);
        for (int y = column.getY(); y >= pos.getY() - DISTANCE; y--) {
            BlockPos at = new BlockPos(column.getX(), y, column.getZ());
            BlockState there = level.getBlockState(at);
            if (!there.isAir() && isPlant(there)
                    && pos.distToCenterSqr(at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5) < DISTANCE * DISTANCE
                    && !isGrownCrop(level, at, there)) {
                this.charges--;
                this.setChanged();
                this.last = at;
                this.lastState = there;
                there.randomTick(level, at, level.getRandom());
                return;
            }
        }
    }

    /** O {@code isPlant}: o que cresce (o {@code IGrowable}) ou é de matéria de planta, menos a grama. */
    static boolean isPlant(BlockState state) {
        Block block = state.getBlock();
        boolean plant = block instanceof BonemealableBlock || block instanceof VegetationBlock
                || block instanceof CactusBlock || block instanceof SugarCaneBlock;
        return plant && !state.is(BlockTags.DIRT) && !state.is(BlockTags.NYLIUM);
    }

    /**
     * O {@code CropUtils.isGrownCrop}: o que já não aceita farinha de osso (menos os caules de abóbora e melancia),
     * a verruga do Nether madura, o cacau maduro, a abóbora e a melancia inteiras, e a cana e o cacto de cima.
     */
    static boolean isGrownCrop(Level level, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        if (block instanceof BonemealableBlock growable && !growable.isValidBonemealTarget(level, pos, state)
                && !(block instanceof StemBlock)) {
            return true;
        }
        if (block instanceof NetherWartBlock && state.getValue(NetherWartBlock.AGE) >= 3) return true;
        if (block instanceof CocoaBlock && state.getValue(CocoaBlock.AGE) >= 2) return true;
        if (block == Blocks.MELON || block == Blocks.PUMPKIN) return true;
        return (block == Blocks.SUGAR_CANE || block == Blocks.CACTUS) && level.getBlockState(pos.below()).is(block);
    }

    /** Um ponto de Herba de quem está no lado preso, de cinco em cinco tiques. */
    private boolean drawEssentia(Level level, BlockPos pos) {
        if (++this.drawDelay % 5 != 0) return false;
        Direction facing = this.facing();
        if (!(level.getBlockEntity(pos.relative(facing)) instanceof EssentiaTransport source)) return false;
        Direction back = facing.getOpposite();
        if (!source.isConnectable(back) || !source.canOutputTo(back)) return false;
        return source.getSuctionAmount(back) < this.getSuctionAmount(facing) && source.takeEssentia(Aspects.PLANT, 1, back) == 1;
    }

    // ---- encanamento: só entra, pelo lado preso, e só Herba

    @Override
    public boolean isConnectable(Direction face) {
        return face == this.facing();
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face == this.facing();
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return false;
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
    }

    @Override
    public Aspect getSuctionType(@Nullable Direction face) {
        return Aspects.PLANT;
    }

    @Override
    public int getSuctionAmount(@Nullable Direction face) {
        return face != this.facing() || this.reserve && this.charges > 0 ? 0 : 128;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    @Nullable
    public Aspect getEssentiaType(@Nullable Direction face) {
        return null;
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction face) {
        return 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public boolean renderExtendedTube() {
        return false;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.reserve = input.getBooleanOr("reserve", false);
        this.charges = input.getIntOr("charges", -1);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("reserve", this.reserve);
        output.putInt("charges", this.charges);
    }
}
