package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.aspects.EssentiaTransport;
import net.thaumcraft.block.ArcaneLampBlock;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A lâmpada da fertilidade: o {@code TileArcaneLampFertility} da 4.2.3.5.
 *
 * <p>Bebe Victus pelo lado em que está presa, até quatro cargas (quanto mais cheia, mais fraca a sucção). Com
 * duas ou mais, a cada quinze segundos procura, até sete blocos em volta, dois bichos adultos da mesma espécie
 * que não estejam no cio — se não houver mais de sete daquela espécie — e os põe no cio, gastando duas cargas.
 */
public class FertilityLampBlockEntity extends BlockEntity implements EssentiaTransport {
    private static final int DISTANCE = 7;

    private int charges;
    private int count;
    private int drawDelay;

    public FertilityLampBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.FERTILITY_LAMP, pos, state);
    }

    private Direction facing() {
        return this.getBlockState().getValue(ArcaneLampBlock.FACING);
    }

    public int charges() {
        return this.charges;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FertilityLampBlockEntity lamp) {
        if (lamp.charges < 4 && lamp.drawEssentia(level, pos)) {
            lamp.charges++;
            lamp.shine(level, pos);
        }
        if (lamp.charges > 1 && lamp.count++ % 300 == 0) lamp.updateAnimals(level, pos);
    }

    private void shine(Level level, BlockPos pos) {
        this.setChanged();
        BlockState state = level.getBlockState(pos);
        boolean lit = this.charges > 0;
        if (state.hasProperty(ArcaneLampBlock.LIT) && state.getValue(ArcaneLampBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(ArcaneLampBlock.LIT, lit), Block.UPDATE_ALL);
        }
    }

    private void updateAnimals(Level level, BlockPos pos) {
        List<Animal> animals = level.getEntitiesOfClass(Animal.class, new AABB(pos).inflate(DISTANCE));
        for (Animal animal : animals) {
            if (animal.getAge() != 0 || animal.isInLove()) continue;
            List<Animal> same = new ArrayList<>();
            for (Animal other : animals) {
                if (other.getClass().equals(animal.getClass())) same.add(other);
            }
            if (same.size() > 7) continue;
            Animal partner = null;
            for (Animal candidate : same) {
                if (candidate.getAge() != 0 || candidate.isInLove()) continue;
                if (partner != null) {
                    this.charges -= 2;
                    candidate.setInLove(null);
                    partner.setInLove(null);
                    this.shine(level, pos);
                    return;
                }
                partner = candidate;
            }
        }
    }

    /** Um ponto de Victus de quem está no lado preso, de cinco em cinco tiques. */
    private boolean drawEssentia(Level level, BlockPos pos) {
        if (++this.drawDelay % 5 != 0) return false;
        Direction facing = this.facing();
        if (!(level.getBlockEntity(pos.relative(facing)) instanceof EssentiaTransport source)) return false;
        Direction back = facing.getOpposite();
        if (!source.isConnectable(back) || !source.canOutputTo(back)) return false;
        return source.getSuctionAmount(back) < this.getSuctionAmount(facing) && source.takeEssentia(Aspects.LIFE, 1, back) == 1;
    }

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
        return Aspects.LIFE;
    }

    @Override
    public int getSuctionAmount(@Nullable Direction face) {
        return face == this.facing() ? 128 - this.charges * 10 : 0;
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
        this.charges = input.getIntOr("charges", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("charges", this.charges);
    }
}
