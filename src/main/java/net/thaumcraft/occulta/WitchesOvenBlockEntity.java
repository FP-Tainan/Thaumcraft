package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * O miolo do Forno das Bruxas: o {@code TileEntityWitchesOven} do Witchery.
 *
 * <p>Cinco casas — o que vai ao fogo, o combustível, o que sai, o fumo que se guardou e os potes de barro. Ele
 * segue as receitas de fornalha do jogo, mas <b>só aceita o que vira carvão, comida ou cinza de madeira</b>, que
 * é o que o {@code canSmelt} do original deixa passar.
 *
 * <p>Cada coisa cozida <b>solta um cheiro</b>: com um pote de barro na casa dos potes, e com sorte, o cheiro fica
 * guardado num dos sete fumos. A conta está no {@link OccultaFumes}.
 *
 * <p>Os funis de fumos em volta fazem duas coisas: <b>apressam</b> o forno (vinte tiques a menos por funil, dos
 * cento e oitenta) e <b>melhoram a sorte</b> do fumo — mas só os dois dos lados, não o de cima.
 */
public class WitchesOvenBlockEntity extends BaseContainerBlockEntity {
    public static final int INPUT = 0, FUEL = 1, OUTPUT = 2, BYPRODUCT = 3, JARS = 4;
    public static final int SIZE = 5;

    /** Quanto tempo leva um cozimento sem funil nenhum, e quanto cada funil tira. */
    public static final int COOK_TIME = 180;
    public static final int FUNNEL_HASTE = 20;

    /** A sorte de sair fumo: a de casa, mais a dos funis dos lados. */
    public static final double BASE_CHANCE = 0.3;
    public static final double FUNNEL_CHANCE = 0.25;
    public static final double FILTERED_FUNNEL_CHANCE = 0.3;

    /** Os números que a tela precisa. */
    public static final int DATA_SIZE = 3;
    public static final int DATA_BURN = 0, DATA_BURN_TOTAL = 1, DATA_COOK = 2;

    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    public int burnTime;
    public int burnTimeTotal;
    public int cookTime;

    public WitchesOvenBlockEntity(BlockPos pos, BlockState state) {
        super(OccultaBlocks.WITCHES_OVEN_ENTITY, pos, state);
    }

    // ------------------------------------------------------------------ o que ele é

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.thaumcraft.witches_oven");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new WitchesOvenMenu(id, inventory, this, this.data());
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    /** O {@code isItemValidForSlot}: o que sai não entra, o combustível queima e nos potes só vão potes. */
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == OUTPUT || slot == BYPRODUCT) return false;
        if (slot == FUEL) return isFuel(this.level, stack);
        if (slot == JARS) return stack.is(OccultaItems.CLAY_JAR);
        return !stack.is(OccultaItems.CLAY_JAR);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        net.minecraft.world.ContainerHelper.loadAllItems(input, this.items);
        this.burnTime = input.getIntOr("BurnTime", 0);
        this.burnTimeTotal = input.getIntOr("BurnTimeTotal", 0);
        this.cookTime = input.getIntOr("CookTime", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        net.minecraft.world.ContainerHelper.saveAllItems(output, this.items);
        output.putInt("BurnTime", this.burnTime);
        output.putInt("BurnTimeTotal", this.burnTimeTotal);
        output.putInt("CookTime", this.cookTime);
    }

    public ContainerData data() {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case DATA_BURN -> WitchesOvenBlockEntity.this.burnTime;
                    case DATA_BURN_TOTAL -> WitchesOvenBlockEntity.this.burnTimeTotal;
                    case DATA_COOK -> WitchesOvenBlockEntity.this.cookTime;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case DATA_BURN -> WitchesOvenBlockEntity.this.burnTime = value;
                    case DATA_BURN_TOTAL -> WitchesOvenBlockEntity.this.burnTimeTotal = value;
                    case DATA_COOK -> WitchesOvenBlockEntity.this.cookTime = value;
                    default -> {
                    }
                }
            }

            @Override
            public int getCount() {
                return DATA_SIZE;
            }
        };
    }

    // ------------------------------------------------------------------ o que ele faz

    /** O {@code updateEntity} do original. */
    public static void tick(Level level, BlockPos pos, BlockState state, WitchesOvenBlockEntity forno) {
        boolean estavaAceso = forno.burnTime > 0;
        boolean mudou = false;
        if (forno.burnTime > 0) forno.burnTime--;

        if (forno.burnTime == 0 && forno.canSmelt()) {
            ItemStack lenha = forno.items.get(FUEL);
            forno.burnTimeTotal = forno.burnTime = burnTimeOf(level, lenha);
            if (forno.burnTime > 0) {
                mudou = true;
                if (!lenha.isEmpty()) {
                    var resto = lenha.getItem().getCraftingRemainder();
                    lenha.shrink(1);
                    if (lenha.isEmpty()) forno.items.set(FUEL, resto == null ? ItemStack.EMPTY : resto.create());
                }
            }
        }

        if (forno.burnTime > 0 && forno.canSmelt()) {
            forno.cookTime++;
            if (forno.cookTime >= forno.cookTime(level, pos, state)) {
                forno.cookTime = 0;
                forno.smelt(level, pos, state);
                mudou = true;
            }
        } else {
            forno.cookTime = 0;
        }

        if (estavaAceso != forno.burnTime > 0) {
            mudou = true;
            level.setBlock(pos, state.setValue(WitchesOvenBlock.LIT, forno.burnTime > 0), Block.UPDATE_ALL);
        }
        if (mudou) forno.setChanged();
    }

    /** Se aquilo queima: o jogo de hoje guarda os tempos de queima no próprio mundo. */
    public static boolean isFuel(Level level, ItemStack stack) {
        return level != null && burnTimeOf(level, stack) > 0;
    }

    private static int burnTimeOf(Level level, ItemStack lenha) {
        if (lenha.isEmpty()) return 0;
        return level.fuelValues().burnDuration(lenha);
    }

    /** O que a fornalha do jogo faria com o que está na primeira casa. */
    public ItemStack result(Level level) {
        ItemStack entra = this.items.get(INPUT);
        if (entra.isEmpty() || level == null) return ItemStack.EMPTY;
        if (!(level instanceof ServerLevel server)) return ItemStack.EMPTY;
        return server.recipeAccess().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(entra), level)
                .map(holder -> holder.value().assemble(new SingleRecipeInput(entra)))
                .orElse(ItemStack.EMPTY);
    }

    /**
     * O {@code canSmelt}: tem de haver receita de fornalha, e o que sai dela tem de ser <b>carvão, comida ou
     * cinza de madeira</b> — o forno das bruxas não é uma fundição.
     */
    public boolean canSmelt() {
        ItemStack sai = this.result(this.level);
        if (sai.isEmpty()) return false;
        if (!(sai.is(Items.COAL) || sai.is(Items.CHARCOAL) || sai.has(net.minecraft.core.component.DataComponents.FOOD)
                || sai.is(OccultaItems.WOOD_ASH))) {
            return false;
        }
        ItemStack pronto = this.items.get(OUTPUT);
        if (pronto.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(pronto, sai)) return false;
        int soma = pronto.getCount() + sai.getCount();
        return soma <= this.getMaxStackSize() && soma <= pronto.getMaxStackSize();
    }

    /** O {@code smeltItem}: o que sai vai para a casa dele, o cheiro vai para a do fumo e o que entrou some. */
    private void smelt(Level level, BlockPos pos, BlockState state) {
        if (!this.canSmelt()) return;
        ItemStack sai = this.result(level);
        ItemStack pronto = this.items.get(OUTPUT);
        if (pronto.isEmpty()) {
            this.items.set(OUTPUT, sai.copy());
        } else {
            pronto.grow(sai.getCount());
        }
        this.fume(level, pos, state);
        this.items.get(INPUT).shrink(1);
    }

    /**
     * O {@code generateByProduct}: com sorte, e havendo pote, o que queimou deixa o cheiro dele num fumo.
     *
     * <p>A sorte é três décimos, mais o que os funis dos lados acrescentam. O pote gasta-se.
     */
    private void fume(Level level, BlockPos pos, BlockState state) {
        if (this.items.get(JARS).isEmpty()) return;
        double sorte = Math.min(BASE_CHANCE + this.funnelChance(level, pos, state), 1.0);
        if (level.getRandom().nextDouble() > sorte) return;
        ItemStack fumo = OccultaFumes.of(this.items.get(INPUT));
        if (fumo.isEmpty()) return;

        ItemStack guardado = this.items.get(BYPRODUCT);
        if (guardado.isEmpty()) {
            this.items.set(BYPRODUCT, fumo);
        } else if (ItemStack.isSameItemSameComponents(guardado, fumo)
                && guardado.getCount() + fumo.getCount() < guardado.getMaxStackSize()) {
            guardado.grow(fumo.getCount());
        } else {
            return;
        }
        this.items.get(JARS).shrink(1);
    }

    /** O {@code getCookTime}: cento e oitenta tiques, menos vinte por funil — contando o de cima. */
    public int cookTime(Level level, BlockPos pos, BlockState state) {
        return COOK_TIME - FUNNEL_HASTE * this.funnels(level, pos, state);
    }

    /** Quantos funis o forno tem: os dois dos lados e o de cima, todos virados para onde ele está virado. */
    public int funnels(Level level, BlockPos pos, BlockState state) {
        int quantos = 0;
        for (BlockPos lado : sides(pos, state)) {
            if (isFunnel(level, lado, state)) quantos++;
        }
        if (isFunnel(level, pos.above(), state)) quantos++;
        return quantos;
    }

    /** O {@code getFumeFunnelsChance}: só os dos lados contam, e o com filtro conta mais. */
    public double funnelChance(Level level, BlockPos pos, BlockState state) {
        double sorte = 0.0;
        for (BlockPos lado : sides(pos, state)) {
            BlockState qual = level.getBlockState(lado);
            if (!isFunnel(level, lado, state)) continue;
            sorte += qual.is(OccultaBlocks.FILTERED_FUME_FUNNEL) ? FILTERED_FUNNEL_CHANCE : FUNNEL_CHANCE;
        }
        return sorte;
    }

    /** Os dois lados do forno: os de mão, não os da frente e de trás. */
    private static BlockPos[] sides(BlockPos pos, BlockState state) {
        Direction frente = state.getValue(WitchesOvenBlock.FACING);
        Direction mão = frente.getClockWise();
        return new BlockPos[]{pos.relative(mão), pos.relative(mão.getOpposite())};
    }

    /** Um funil só conta se estiver virado para onde o forno está virado, como no original. */
    private static boolean isFunnel(Level level, BlockPos onde, BlockState forno) {
        BlockState qual = level.getBlockState(onde);
        if (!(qual.getBlock() instanceof FumeFunnelBlock)) return false;
        return qual.getValue(FumeFunnelBlock.FACING) == forno.getValue(WitchesOvenBlock.FACING);
    }
}
