package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.visnet.VisNet;
import net.thaumcraft.api.wands.Wandable;
import net.thaumcraft.block.ArcaneBoreBlock;
import net.thaumcraft.crafting.SpecialMining;
import net.thaumcraft.inventory.ArcaneBoreMenu;
import net.thaumcraft.inventory.InventoryUtils;
import net.thaumcraft.item.FocusItem;
import net.thaumcraft.item.FocusUpgradeTable;
import net.thaumcraft.item.Focuses;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A broca arcana: o {@code TileArcaneBore} da 4.2.3.5.
 *
 * <p>Com redstone (nela ou na base), um foco de escavação e uma picareta que ainda não esteja por quebrar, ela gira
 * o bico numa espiral em volta do eixo e cava o primeiro bloco sólido à frente de cada ponto, até 64 blocos de fundo.
 * O que sai do bloco (e os itens soltos em volta dele) vai para um inventário encostado no bico da base ou é cuspido
 * por ele. Cada bloco gasta um de durabilidade da picareta. Sem Perditio (da rede de vis, ou de canos na base) ela
 * trabalha a um quarto da velocidade. Uma lâmpada arcana encostada na base vai deixando luzes pelo túnel.
 *
 * <p>O foco dá a sorte (tesouro), a largura (ampliar), a velocidade (potência), o toque suave e a radiestesia; a
 * picareta, a sorte, a eficiência e o toque suave dela.
 */
public class ArcaneBoreBlockEntity extends BaseContainerBlockEntity
        implements Wandable, net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider<BlockPos> {
    public int spiral;
    public float currentRadius;
    public int maxRadius = 2;
    public float vRadX, vRadZ, tRadX, tRadZ, mRadX, mRadZ;
    private int count;
    public int topRotation;
    private long soundDelay;
    /** Os dois fachos do original, do lado de quem vê. */
    public Object beam1, beam2;
    private int beamlength;
    private NonNullList<ItemStack> contents = NonNullList.withSize(2, ItemStack.EMPTY);
    public int rotX, rotZ, tarX, tarZ, speedX, speedZ;
    public boolean hasFocus, hasPickaxe;
    private int lastX, lastZ, lastY;
    private boolean toDig;
    private int digX, digY, digZ;
    private BlockState digBlock = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
    private float radInc;
    private int paused = 100;
    private int maxPause = 100;
    private boolean first = true;
    public int fortune;
    public int speed;
    public int area;
    private int blockCount;
    private float speedyTime;
    private long repairCounter;
    private net.thaumcraft.api.aspects.AspectList repairCost = new net.thaumcraft.api.aspects.AspectList();
    private net.thaumcraft.api.aspects.AspectList currentRepairVis = new net.thaumcraft.api.aspects.AspectList();

    public ArcaneBoreBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ARCANE_BORE, pos, state);
        this.setOrientation(state.getValue(ArcaneBoreBlock.FACING), true);
    }

    public Direction orientation() {
        return this.getBlockState().getValue(ArcaneBoreBlock.FACING);
    }

    public Direction baseOrientation() {
        return this.getBlockState().getValue(ArcaneBoreBlock.BASE);
    }

    private BlockPos basePos() {
        return this.worldPosition.relative(this.baseOrientation().getOpposite());
    }

    private @Nullable ArcaneBoreBaseBlockEntity base() {
        return this.level != null && this.level.getBlockEntity(this.basePos()) instanceof ArcaneBoreBaseBlockEntity base ? base : null;
    }

    public ItemStack focus() {
        return this.contents.get(0);
    }

    public ItemStack pickaxe() {
        return this.contents.get(1);
    }

    /** A radiestesia: a picareta do núcleo (quando existir) ou o foco com a melhoria. */
    public boolean dowsing() {
        return FocusItem.isUpgradedWith(this.focus(), FocusUpgradeTable.DOWSING);
    }

    public boolean silkTouch() {
        return this.enchantLevel(this.pickaxe(), Enchantments.SILK_TOUCH) > 0 || FocusItem.isUpgradedWith(this.focus(), FocusUpgradeTable.SILKTOUCH);
    }

    private int enchantLevel(ItemStack stack, net.minecraft.resources.ResourceKey<net.minecraft.world.item.enchantment.Enchantment> key) {
        if (stack.isEmpty() || this.level == null) return 0;
        var holder = this.level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).get(key);
        return holder.map(h -> EnchantmentHelper.getItemEnchantmentLevel(h, stack)).orElse(0);
    }

    public static boolean isPickaxe(ItemStack stack) {
        return stack.is(ItemTags.PICKAXES);
    }

    public static boolean isExcavationFocus(ItemStack stack) {
        return stack.getItem() instanceof FocusItem focus && "excavation".equals(focus.type());
    }

    // ----------------------------------------------------------------- o tique

    public static void tick(Level level, BlockPos pos, BlockState state, ArcaneBoreBlockEntity bore) {
        bore.update();
    }

    private void update() {
        Level level = this.level;
        if (!level.isClientSide() && this.speedyTime < 20.0f) {
            this.speedyTime += VisNet.drainVis(level, this.worldPosition, Aspects.ENTROPY, 100) / 5.0f;
            ArcaneBoreBaseBlockEntity base = this.base();
            if (this.speedyTime < 20.0f && base != null && base.drawEssentia()) this.speedyTime += 20.0f;
        }
        if (level.isClientSide() && this.first) {
            this.setOrientation(this.orientation(), true);
            this.first = false;
        }
        if (this.rotX < this.tarX) {
            this.rotX += this.speedX;
            if (this.rotX < this.tarX) this.speedX++;
            else this.speedX = (int) (this.speedX / 3.0f);
        } else if (this.rotX > this.tarX) {
            this.rotX += this.speedX;
            if (this.rotX > this.tarX) this.speedX--;
            else this.speedX = (int) (this.speedX / 3.0f);
        } else {
            this.speedX = 0;
        }
        if (this.rotZ < this.tarZ) {
            this.rotZ += this.speedZ;
            if (this.rotZ < this.tarZ) this.speedZ++;
            else this.speedZ = (int) (this.speedZ / 3.0f);
        } else if (this.rotZ > this.tarZ) {
            this.rotZ += this.speedZ;
            if (this.rotZ > this.tarZ) this.speedZ--;
            else this.speedZ = (int) (this.speedZ / 3.0f);
        } else {
            this.speedZ = 0;
        }
        if (this.gettingPower() && this.areItemsValid()) {
            this.dig();
        } else if (level.isClientSide()) {
            this.settle();
        }
        if (!level.isClientSide() && this.hasPickaxe) this.repairPickaxe();
    }

    /**
     * O conserto da picareta com Reparo, pagando com a rede de vis: a cada dois segundos, se já juntou o preço, conserta
     * um ponto por nível; a cada cinco tiques puxa da rede o que falta do preço.
     */
    private void repairPickaxe() {
        ItemStack pick = this.pickaxe();
        if (this.repairCounter++ % 40L == 0L && pick.isDamaged()) {
            int lvl = Math.min(2, net.thaumcraft.registry.TCEnchantments.level(this.level, net.thaumcraft.registry.TCEnchantments.REPAIR, pick));
            if (lvl > 0) {
                if (pick.is(net.thaumcraft.event.Enchantments.REPAIRABLE)) {
                    for (var e : net.thaumcraft.event.Enchantments.repairCost(pick, lvl).getAspects()) {
                        this.repairCost.merge(e, net.thaumcraft.event.Enchantments.repairCost(pick, lvl).getAmount(e));
                    }
                    boolean doIt = this.repairCost.size() > 0;
                    for (var a : this.repairCost.getAspects()) {
                        if (this.currentRepairVis.getAmount(a) < this.repairCost.getAmount(a)) {
                            doIt = false;
                            break;
                        }
                    }
                    if (doIt) {
                        for (var a : this.repairCost.getAspects()) this.currentRepairVis.reduce(a, this.repairCost.getAmount(a));
                        pick.setDamageValue(Math.max(0, pick.getDamageValue() - lvl));
                        this.setChanged();
                    }
                } else {
                    this.repairCost = new net.thaumcraft.api.aspects.AspectList();
                }
            }
        }
        if (this.repairCost.size() > 0 && this.repairCounter % 5L == 0L) {
            for (var a : this.repairCost.getAspects()) {
                if (this.currentRepairVis.getAmount(a) < this.repairCost.getAmount(a)) {
                    this.currentRepairVis.add(a, VisNet.drainVis(this.level, this.worldPosition, a, this.repairCost.getAmount(a)));
                }
            }
        }
    }

    /** Parada: o topo termina o quarto de volta e o bico volta ao centro. */
    private void settle() {
        if (this.topRotation % 90 != 0) this.topRotation += Math.min(10, 90 - this.topRotation % 90);
        this.vRadX *= 0.9f;
        this.vRadZ *= 0.9f;
    }

    private boolean areItemsValid() {
        ItemStack pick = this.pickaxe();
        boolean notNearBroken = !(this.hasPickaxe && pick.getDamageValue() + 1 >= pick.getMaxDamage());
        return this.hasFocus && this.hasPickaxe && pick.isDamageableItem() && notNearBroken;
    }

    /** O {@code markDirty} do original: recalcula o que o foco e a picareta dão. */
    private void recalc() {
        this.fortune = 0;
        this.area = 0;
        this.speed = 0;
        ItemStack focus = this.focus();
        if (isExcavationFocus(focus)) {
            this.fortune = FocusItem.level(focus, FocusUpgradeTable.TREASURE);
            this.area = FocusItem.level(focus, FocusUpgradeTable.ENLARGE);
            this.speed += FocusItem.level(focus, FocusUpgradeTable.POTENCY);
            this.hasFocus = true;
        } else {
            this.hasFocus = false;
        }
        ItemStack pick = this.pickaxe();
        if (isPickaxe(pick)) {
            this.hasPickaxe = true;
            int f = this.enchantLevel(pick, Enchantments.FORTUNE);
            if (f > this.fortune) this.fortune = f;
            this.speed += this.enchantLevel(pick, Enchantments.EFFICIENCY);
        } else {
            this.hasPickaxe = false;
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        this.recalc();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    private float hardness(BlockPos at) {
        return this.level.getBlockState(at).getDestroySpeed(this.level, at);
    }

    private void dig() {
        if (this.rotX != this.tarX || this.rotZ != this.tarZ) {
            if (this.level.isClientSide()) this.settle();
            return;
        }
        Level level = this.level;
        Direction o = this.orientation();
        if (!level.isClientSide()) {
            boolean dug = false;
            if (--this.count > 0) return;
            if (this.toDig) {
                this.toDig = false;
                BlockPos at = new BlockPos(this.digX, this.digY, this.digZ);
                BlockState state = level.getBlockState(at);
                if (!state.isAir()) {
                    this.harvest((ServerLevel) level, at, state);
                }
                ItemStack pick = this.pickaxe();
                pick.hurtAndBreak(1, (ServerLevel) level, null, item -> {
                });
                if (pick.isEmpty()) this.contents.set(1, ItemStack.EMPTY);
                level.removeBlock(at, false);
                this.lights();
                dug = true;
            }
            this.findNextBlockToDig();
            if (dug && this.speedyTime > 0.0f) this.speedyTime--;
            return;
        }
        // do lado de quem vê: o bico mira, o facho acende e as migalhas voam
        this.paused++;
        if (this.paused < this.maxPause && this.soundDelay < System.currentTimeMillis()) {
            this.soundDelay = System.currentTimeMillis() + 1200L + level.getRandom().nextInt(100);
            level.playLocalSound(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5,
                    TCSounds.RUMBLE.value(), SoundSource.BLOCKS, 0.25f, 0.9f + level.getRandom().nextFloat() * 0.2f, false);
        }
        if (this.beamlength > 0 && this.paused > this.maxPause) this.beamlength--;
        if (this.toDig) {
            this.paused = 0;
            this.beamlength = 64;
            BlockPos at = new BlockPos(this.digX, this.digY, this.digZ);
            this.maxPause = 10 + Math.max(10 - this.speed, (int) (this.hardness(at) * 2.0f) - this.speed * 2);
            if (this.speedyTime <= 0.0f) this.maxPause *= 4;
            this.toDig = false;
            double xd = this.worldPosition.getX() + 0.5 - (this.digX + 0.5);
            double yd = this.worldPosition.getY() + 0.5 - (this.digY + 0.5);
            double zd = this.worldPosition.getZ() + 0.5 - (this.digZ + 0.5);
            double horizontal = Math.sqrt(xd * xd + zd * zd);
            float rx = (float) (Math.atan2(zd, xd) * 180.0 / Math.PI);
            float rz = (float) -(Math.atan2(yd, horizontal) * 180.0 / Math.PI) + 90.0f;
            this.tRadX = Mth.wrapDegrees(this.rotX) + rx;
            if (o == Direction.EAST) {
                if (this.tRadX > 180.0f) this.tRadX -= 360.0f;
                if (this.tRadX < -180.0f) this.tRadX += 360.0f;
            }
            this.tRadZ = rz - this.rotZ;
            if (o.getAxis() == Direction.Axis.Y) {
                this.tRadZ += 180.0f;
                if (this.vRadX - this.tRadX >= 180.0f) this.vRadX -= 360.0f;
                if (this.vRadX - this.tRadX <= -180.0f) this.vRadX += 360.0f;
            }
            this.mRadX = Math.abs((this.vRadX - this.tRadX) / 6.0f);
            this.mRadZ = Math.abs((this.vRadZ - this.tRadZ) / 6.0f);
            if (this.speedyTime > 0.0f) this.speedyTime--;
        }
        if (this.paused < this.maxPause) {
            if (this.vRadX < this.tRadX) this.vRadX += this.mRadX;
            else if (this.vRadX > this.tRadX) this.vRadX -= this.mRadX;
            if (this.vRadZ < this.tRadZ) this.vRadZ += this.mRadZ;
            else if (this.vRadZ > this.tRadZ) this.vRadZ -= this.mRadZ;
        } else {
            this.vRadX *= 0.9f;
            this.vRadZ *= 0.9f;
        }
        this.mRadX *= 0.9f;
        this.mRadZ *= 0.9f;
        float vx = this.rotX + 90 - this.vRadX;
        float vz = this.rotZ + 90 - this.vRadZ;
        float dX = Mth.sin(vx / 180.0f * (float) Math.PI) * Mth.cos(vz / 180.0f * (float) Math.PI);
        float dZ = Mth.cos(vx / 180.0f * (float) Math.PI) * Mth.cos(vz / 180.0f * (float) Math.PI);
        float dY = Mth.sin(vz / 180.0f * (float) Math.PI);
        double cx = this.worldPosition.getX() + 0.5, cy = this.worldPosition.getY() + 0.5, cz = this.worldPosition.getZ() + 0.5;
        Vec3 from = new Vec3(cx + dX, cy + dY, cz + dZ);
        Vec3 to = new Vec3(cx + dX * this.beamlength, cy + dY * this.beamlength, cz + dZ * this.beamlength);
        BlockHitResult mop = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
                net.minecraft.world.phys.shapes.CollisionContext.empty()));
        int impact = 0;
        double bx = to.x, by = to.y, bz = to.z;
        BlockPos nozzle = this.worldPosition.relative(o);
        if (mop.getType() == HitResult.Type.BLOCK) {
            bx = mop.getLocation().x;
            by = mop.getLocation().y;
            bz = mop.getLocation().z;
            impact = 5;
            BlockPos hit = BlockPos.containing(bx, by, bz);
            if (!level.isEmptyBlock(hit)) clientEffects.digFx(level, hit, nozzle, level.getBlockState(hit));
        }
        this.topRotation += this.beamlength / 6;
        this.beam1 = clientEffects.beam(level, cx, cy, cz, bx, by, bz, 1, 0x00FF66, true, impact > 0 ? 2.0f : 0.0f, this.beam1, impact);
        this.beam2 = clientEffects.beam(level, cx, cy, cz, bx, by, bz, 2, 0xFF8855, false, impact > 0 ? 2.0f : 0.0f, this.beam2, impact);
        BlockPos dig = new BlockPos(this.digX, this.digY, this.digZ);
        if (level.isEmptyBlock(dig) && !this.digBlock.isAir()) {
            this.digSound(this.digBlock);
            for (int a = 0; a < 10; a++) clientEffects.digFx(level, dig, nozzle, this.digBlock);
            this.digBlock = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
    }

    private void digSound(BlockState state) {
        SoundType sound = state.getSoundType();
        this.level.playLocalSound(this.digX + 0.5, this.digY + 0.5, this.digZ + 0.5, sound.getBreakSound(), SoundSource.BLOCKS,
                (sound.getVolume() + 1.0f) / 2.0f, sound.getPitch() * 0.8f, false);
    }

    /** Colhe o bloco: com a sorte (ou a seda), junta os itens soltos em volta e manda tudo para a base. */
    private void harvest(ServerLevel level, BlockPos at, BlockState state) {
        int tfortune = this.fortune;
        boolean silk = this.silkTouch();
        if (silk) tfortune = 0;
        net.thaumcraft.net.TCNetwork.boreDig(level, this.worldPosition, 99, Block.getId(state));
        List<ItemStack> items = new ArrayList<>(Block.getDrops(state, level, at, level.getBlockEntity(at), null,
                Focuses.harvestTool(level, tfortune, silk)));
        for (ItemEntity e : level.getEntitiesOfClass(ItemEntity.class, new AABB(at).inflate(1.0))) {
            items.add(e.getItem().copy());
            e.discard();
        }
        ArcaneBoreBaseBlockEntity base = this.base();
        for (ItemStack is : items) {
            ItemStack dropped = is.copy();
            if (!silk && this.dowsing()) dropped = SpecialMining.refine(is, 0.2f + tfortune * 0.075f, level.getRandom());
            if (base == null) continue;
            Direction bo = base.orientation();
            BlockPos out = base.getBlockPos().offset(bo.getStepX(), 0, bo.getStepZ());
            if (level.getBlockEntity(out) instanceof Container inventory) {
                dropped = InventoryUtils.insert(inventory, dropped, bo.getOpposite(), true);
            }
            if (!dropped.isEmpty()) {
                Direction down = this.baseOrientation().getOpposite();
                ItemEntity ei = new ItemEntity(level, this.worldPosition.getX() + 0.5 + bo.getStepX() * 0.66,
                        this.worldPosition.getY() + 0.4 + down.getStepY(), this.worldPosition.getZ() + 0.5 + bo.getStepZ() * 0.66, dropped.copy());
                ei.setDeltaMovement(0.075f * bo.getStepX(), 0.025f, 0.075f * bo.getStepZ());
                level.addFreshEntity(ei);
            }
        }
    }

    /** Com uma lâmpada arcana encostada na base, uma luz a cada dois blocos do túnel, em zigue-zague. */
    private void lights() {
        ArcaneBoreBaseBlockEntity base = this.base();
        if (base == null) return;
        Direction o = this.orientation();
        for (Direction lbd : Direction.Plane.HORIZONTAL) {
            if (!(this.level.getBlockEntity(base.getBlockPos().relative(lbd)) instanceof ArcaneLampBlockEntity)) continue;
            int d = this.level.getRandom().nextInt(32) * 2;
            int xx = this.worldPosition.getX() + o.getStepX() + o.getStepX() * d;
            int yy = this.worldPosition.getY() + o.getStepY() + o.getStepY() * d;
            int zz = this.worldPosition.getZ() + o.getStepZ() + o.getStepZ() * d;
            int p = d / 2 % 4;
            if (o.getStepX() != 0) zz += p == 0 ? 3 : (p != 1 && p != 3 ? -3 : 0);
            else xx += p == 0 ? 3 : (p != 1 && p != 3 ? -3 : 0);
            if (p == 3 && o.getStepY() == 0) yy -= 2;
            BlockPos light = new BlockPos(xx, yy, zz);
            if (this.level.isEmptyBlock(light) && !this.level.getBlockState(light).is(TCBlocks.LAMP_LIGHT)
                    && this.level.getMaxLocalRawBrightness(light) < 15) {
                this.level.setBlock(light, TCBlocks.LAMP_LIGHT.defaultBlockState(), Block.UPDATE_ALL);
            }
            break;
        }
    }

    /** A espiral: o próximo ponto em volta do eixo e, dali para a frente, o primeiro bloco que dê para cavar. */
    private void findNextBlockToDig() {
        if (this.radInc == 0.0f) this.radInc = (this.maxRadius + this.area) / 360.0f;
        Direction o = this.orientation();
        int x = this.lastX, z = this.lastZ, y = this.lastY;
        while (x == this.lastX && z == this.lastZ && y == this.lastY) {
            this.spiral += 2;
            if (this.spiral >= 360) this.spiral -= 360;
            this.currentRadius += this.radInc;
            if (this.currentRadius > this.maxRadius + this.area || this.currentRadius < -(this.maxRadius + this.area)) this.radInc *= -1.0f;
            // o TCVec3 do original: um vetor de comprimento r no eixo Y, girado pela espiral e pela orientação
            double vx = 0.0, vy = this.currentRadius, vz = 0.0;
            float a = this.spiral / 180.0f * (float) Math.PI;
            double c = Mth.cos(a), s = Mth.sin(a);
            double nx = vx * c + vy * s, ny = vy * c - vx * s;
            vx = nx;
            vy = ny;
            a = (float) (Math.PI / 2) * o.getStepX();
            c = Mth.cos(a);
            s = Mth.sin(a);
            nx = vx * c + vz * s;
            double nz = vz * c - vx * s;
            vx = nx;
            vz = nz;
            a = (float) (Math.PI / 2) * o.getStepY();
            c = Mth.cos(a);
            s = Mth.sin(a);
            ny = vy * c + vz * s;
            nz = vz * c - vy * s;
            vy = ny;
            vz = nz;
            x = Mth.floor(this.worldPosition.getX() + o.getStepX() + 0.5 + vx);
            y = Mth.floor(this.worldPosition.getY() + o.getStepY() + 0.5 + vy);
            z = Mth.floor(this.worldPosition.getZ() + o.getStepZ() + 0.5 + vz);
        }
        this.lastX = x;
        this.lastZ = z;
        this.lastY = y;
        x += o.getStepX();
        y += o.getStepY();
        z += o.getStepZ();
        for (int depth = 0; depth < 64; depth++) {
            x += o.getStepX();
            y += o.getStepY();
            z += o.getStepZ();
            BlockPos at = new BlockPos(x, y, z);
            BlockState state = this.level.getBlockState(at);
            if (state.getDestroySpeed(this.level, at) < 0.0f) break;
            if (!state.isAir() && state.getFluidState().isEmpty() && !state.getCollisionShape(this.level, at).isEmpty()) {
                this.digX = x;
                this.digY = y;
                this.digZ = z;
                if (++this.blockCount > 2) this.blockCount = 0;
                this.count = Math.max(10 - this.speed, (int) (this.hardness(at) * 2.0f) - this.speed * 2);
                if (this.speedyTime < 1.0f) this.count *= 4;
                this.toDig = true;
                BlockPos nozzle = this.worldPosition.relative(o);
                BlockHitResult mop = this.level.clip(new ClipContext(Vec3.atCenterOf(nozzle), Vec3.atCenterOf(at), ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE, net.minecraft.world.phys.shapes.CollisionContext.empty()));
                if (mop.getType() == HitResult.Type.BLOCK) {
                    BlockPos hit = mop.getBlockPos();
                    BlockState hs = this.level.getBlockState(hit);
                    if (hs.getDestroySpeed(this.level, hit) > -1.0f && !hs.getCollisionShape(this.level, hit).isEmpty()) {
                        this.count = Math.max(10 - this.speed, (int) (this.hardness(hit) * 2.0f) - this.speed * 2);
                        if (this.speedyTime < 1.0f) this.count *= 4;
                        this.digX = hit.getX();
                        this.digY = hit.getY();
                        this.digZ = hit.getZ();
                    }
                }
                this.sendDigEvent();
                break;
            }
        }
    }

    public boolean gettingPower() {
        return this.level.hasNeighborSignal(this.worldPosition) || this.level.hasNeighborSignal(this.basePos());
    }

    /** O {@code setOrientation}: os ângulos-alvo do corpo para cada lado e o recomeço da espiral. */
    public void setOrientation(Direction or, boolean initial) {
        this.lastX = 0;
        this.lastZ = 0;
        switch (or) {
            case DOWN -> {
                this.tarZ = 180;
                this.tarX = 0;
            }
            case UP -> {
                this.tarZ = 0;
                this.tarX = 0;
            }
            case NORTH -> {
                this.tarZ = 90;
                this.tarX = 270;
            }
            case SOUTH -> {
                this.tarZ = 90;
                this.tarX = 90;
            }
            case WEST -> {
                this.tarZ = 90;
                this.tarX = 0;
            }
            case EAST -> {
                this.tarZ = 90;
                this.tarX = 180;
            }
        }
        if (initial) {
            this.rotX = this.tarX;
            this.rotZ = this.tarZ;
        }
        this.toDig = false;
        this.radInc = 0.0f;
        this.paused = 100;
        this.tRadX = 0.0f;
        this.tRadZ = 0.0f;
        this.mRadX = 0.0f;
        this.mRadZ = 0.0f;
        this.digX = 0;
        this.digY = 0;
        this.digZ = 0;
    }

    @Override
    public void setBlockState(BlockState state) {
        Direction before = this.orientation();
        super.setBlockState(state);
        if (state.getValue(ArcaneBoreBlock.FACING) != before) this.setOrientation(state.getValue(ArcaneBoreBlock.FACING), false);
    }

    /** A varinha: o bico passa a cavar para a face batida. */
    @Override
    public boolean onWand(Level level, ItemStack wand, Player player, BlockPos pos, Direction face) {
        if (!level.isClientSide()) {
            level.setBlock(pos, this.getBlockState().setValue(ArcaneBoreBlock.FACING, face), Block.UPDATE_ALL);
            this.setOrientation(face, false);
            level.playSound(null, pos, TCSounds.TOOL.value(), SoundSource.BLOCKS, 0.5f, 0.9f + level.getRandom().nextFloat() * 0.2f);
            this.setChanged();
        }
        player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        return true;
    }

    // ----------------------------------------------------------------- os avisos para quem vê

    /** O {@code PacketBoreDig}: o bloco da vez, relativo à broca, mandado a quem está perto. */
    private void sendDigEvent() {
        int x = this.digX - this.worldPosition.getX() + 64;
        int y = this.digY - this.worldPosition.getY() + 64;
        int z = this.digZ - this.worldPosition.getZ() + 64;
        if (this.level instanceof ServerLevel server) net.thaumcraft.net.TCNetwork.boreDig(server, this.worldPosition, 98, (x & 255) << 16 | (y & 255) << 8 | z & 255);
    }

    /** Os eventos 98 (o bloco da vez) e 99 (o som e as migalhas do bloco que saiu). */
    public boolean boreEvent(int id, int param) {
        if (id == 98) {
            if (this.level != null && this.level.isClientSide()) {
                this.digX = this.worldPosition.getX() + (param >> 16 & 255) - 64;
                this.digY = this.worldPosition.getY() + (param >> 8 & 255) - 64;
                this.digZ = this.worldPosition.getZ() + (param & 255) - 64;
                this.toDig = true;
                this.digBlock = this.level.getBlockState(new BlockPos(this.digX, this.digY, this.digZ));
            }
            return true;
        }
        if (id == 99) {
            if (this.level != null && this.level.isClientSide()) {
                BlockState state = Block.stateById(param);
                if (!state.isAir()) {
                    this.digSound(state);
                    BlockPos nozzle = this.worldPosition.relative(this.orientation());
                    for (int a = 0; a < 10; a++) clientEffects.digFx(this.level, new BlockPos(this.digX, this.digY, this.digZ), nozzle, state);
                }
            }
            return true;
        }
        return false;
    }

    public interface ClientEffects {
        /** O {@code beamBore}: renova o facho dado (ou acende um) e o devolve. */
        Object beam(Level level, double px, double py, double pz, double tx, double ty, double tz, int type, int colour, boolean reverse,
                    float endMod, Object old, int impact);

        /** O {@code boreDigFx}: uma migalha (ou, uma vez em dez, uma faísca verde) do bloco até o bico. */
        void digFx(Level level, BlockPos from, BlockPos to, BlockState state);
    }

    public static ClientEffects clientEffects = new ClientEffects() {
        @Override
        public Object beam(Level level, double px, double py, double pz, double tx, double ty, double tz, int type, int colour,
                           boolean reverse, float endMod, Object old, int impact) {
            return null;
        }

        @Override
        public void digFx(Level level, BlockPos from, BlockPos to, BlockState state) {
        }
    };

    // ----------------------------------------------------------------- o inventário

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.thaumcraft.arcane_bore");
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return this.worldPosition;
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new ArcaneBoreMenu(id, inventory, this);
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.contents;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.contents = items;
        this.recalc();
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);
        this.setChanged();
    }

    @Override
    public int getContainerSize() {
        return 2;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.contents = NonNullList.withSize(2, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.contents);
        this.speedyTime = input.getShortOr("SpeedyTime", (short) 0);
        this.recalc();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.contents);
        output.putShort("SpeedyTime", (short) this.speedyTime);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
