package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.nodes.NodeModifier;
import net.thaumcraft.api.nodes.NodeType;
import net.thaumcraft.block.NodeStabilizerBlock;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCBlocks;

import java.util.ArrayList;
import java.util.List;

/**
 * Um nó de aura: o {@code TileNode} da 4.2.3.5.
 *
 * <p>Cada nó tem, por aspecto, um teto (a base) e o que tem agora. De tempos em tempos devolve um ponto a um aspecto
 * que esteja faltando — a cada 600 tiques o comum, 400 o brilhante, 900 o pálido; o esmaecido nunca. Um nó que fica
 * sem nada de um aspecto vai perdendo o teto dele (a cada 1200 tiques) até o aspecto morrer, e às vezes fica mais
 * pálido; sem aspecto nenhum, o nó some. Nós vizinhos (até quatro blocos) disputam vis: o mais cheio suga um ponto do
 * mais vazio, às vezes crescendo com isso, com um raio entre os dois.
 *
 * <p>O estabilizador embaixo trava o nó: o comum dobra o tempo de refazer e impede que ele sugue os vizinhos; o
 * avançado multiplica por vinte. Nenhum travado é sugado por outro nó. O instável solta orbes de vis (travado, às vezes
 * se acalma); o esmaecido travado às vezes volta a pálido; o faminto puxa e fere quem chega perto e come os blocos em
 * volta; o maculado espalha fibras de mácula.
 */
public class NodeBlockEntity extends BlockEntity {
    /** De quanto em quanto tempo um nó comum devolve um ponto. */
    public static final int REGEN_NORMAL = 600;
    public static final int REGEN_BRIGHT = 400;
    public static final int REGEN_PALE = 900;
    /** O esmaecido não devolve nada: o que se tirou está tirado. */
    public static final int REGEN_FADING = 0;

    private AspectList base = new AspectList();
    private AspectList aspects = new AspectList();
    private NodeType type = NodeType.NORMAL;
    private NodeModifier modifier;
    private int count;
    private int regeneration = -1;
    private int wait;
    private long lastActive;
    private boolean catchUp;
    private int lock;

    public NodeBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.NODE, pos, state);
    }

    /** Para o nó no jarro, que é um nó de outro tipo de peça. */
    protected NodeBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** Quantos tiques o nó espera entre um ponto e o próximo, sem a trava. Zero quer dizer que ele não se refaz. */
    public int regenerationInterval() {
        if (this.modifier == NodeModifier.BRIGHT) return REGEN_BRIGHT;
        if (this.modifier == NodeModifier.PALE) return REGEN_PALE;
        if (this.modifier == NodeModifier.FADING) return REGEN_FADING;
        return REGEN_NORMAL;
    }

    /** A trava do estabilizador de baixo: 0 nenhuma, 1 o comum, 2 o avançado. */
    public int lock() {
        return this.lock;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NodeBlockEntity node) {
        boolean change = node.hungryFirst(level, pos);
        node.count++;
        node.checkLock(level, pos, state);
        if (level.isClientSide()) return;
        if (node.base.isEmpty()) {
            // nó posto na mão não vem com nada dentro: aqui ele ganha o que teria se tivesse nascido sozinho
            // (o do tronco do pinheiro-de-prata, como os que nascem nele)
            net.thaumcraft.world.NodeFeature.setupNode(level, pos, level.getRandom(),
                    state.is(net.thaumcraft.registry.TCBlocks.SILVERWOOD_KNOT));
            return;
        }
        ServerLevel server = (ServerLevel) level;
        change |= node.discharge(server, pos, state);
        change |= node.recharge(server, pos, state);
        if (node.isRemoved()) return;
        change |= node.taint(server, pos);
        change |= node.stability(server, pos);
        change |= node.dark(server, pos);
        change |= node.pure(server, pos, state);
        change |= node.hungrySecond(server, pos);
        if (change) node.sync();
    }

    // ---------------------------------------------------------------------------------------------- a trava

    /** O {@code checkLock}: de cinquenta em cinquenta tiques, olha o estabilizador de baixo. */
    private void checkLock(Level level, BlockPos pos, BlockState state) {
        if (!(this.count <= 1 || this.count % 50 == 0) || !state.is(TCBlocks.NODE)) return;
        int old = this.lock;
        this.lock = 0;
        BlockPos below = pos.below();
        if (level.getBlockState(below).getBlock() instanceof NodeStabilizerBlock stabilizer && !level.hasNeighborSignal(below)) {
            this.lock = stabilizer.lock();
        }
        if (old != this.lock) this.regeneration = -1;
    }

    // ---------------------------------------------------------------------------------------------- os vizinhos

    /** O {@code handleDischarge}: suga um ponto de um nó vizinho mais vazio. */
    private boolean discharge(ServerLevel level, BlockPos pos, BlockState state) {
        if (!state.is(TCBlocks.NODE) || this.lock == 1 || this.modifier == NodeModifier.FADING) return false;
        boolean shiny = this.type == NodeType.HUNGRY || this.modifier == NodeModifier.BRIGHT;
        int inc = this.modifier == null ? 2 : shiny ? 1 : this.modifier == NodeModifier.PALE ? 3 : 2;
        if (this.count % inc != 0) return false;
        RandomSource random = level.getRandom();
        int x = random.nextInt(5) - random.nextInt(5);
        int y = random.nextInt(5) - random.nextInt(5);
        int z = random.nextInt(5) - random.nextInt(5);
        if (this.modifier == NodeModifier.PALE && random.nextBoolean()) return false;
        if (x == 0 && y == 0 && z == 0) return false;
        BlockPos there = pos.offset(x, y, z);
        if (!level.getBlockState(there).is(TCBlocks.NODE) || !(level.getBlockEntity(there) instanceof NodeBlockEntity other)) return false;
        if (other.lock > 0) return false;
        int otherAvg = (other.aspects.visSize() + other.base.visSize()) / 2;
        int thisAvg = (this.aspects.visSize() + this.base.visSize()) / 2;
        if (otherAvg >= thisAvg || other.base.size() == 0) return false;
        Aspect aspect = other.base.getAspects().get(random.nextInt(other.base.size()));
        boolean moved = false;
        if (this.aspects.getAmount(aspect) < this.base.getAmount(aspect) && other.aspects.reduce(aspect, 1)) {
            this.addToContainer(aspect, 1);
            moved = true;
        } else if (other.aspects.reduce(aspect, 1)) {
            if (random.nextInt(1 + (int) (this.base.getAmount(aspect) / (shiny ? 1.5 : 1.0))) == 0) {
                this.base.add(aspect, 1);
                if (this.modifier == NodeModifier.PALE && random.nextInt(100) == 0) {
                    this.modifier = null;
                    this.regeneration = -1;
                }
                if (random.nextInt(3) == 0) other.setBase(aspect, other.base.getAmount(aspect) - 1);
            }
            moved = true;
        }
        if (!moved) return false;
        other.wait = other.regeneration / 2;
        other.sync();
        net.thaumcraft.net.TCNetwork.blockZap(level, pos, Vec3.atCenterOf(there), Vec3.atCenterOf(pos));
        return true;
    }

    // ---------------------------------------------------------------------------------------------- refazer e minguar

    /** O {@code handleRecharge}: devolve pontos, recupera o tempo em que ficou descarregado e deixa morrer o que acabou. */
    private boolean recharge(ServerLevel level, BlockPos pos, BlockState state) {
        boolean change = false;
        RandomSource random = level.getRandom();
        if (this.regeneration < 0) {
            this.regeneration = this.regenerationInterval();
            if (this.lock == 1) this.regeneration *= 2;
            if (this.lock == 2) this.regeneration *= 20;
        }
        if (this.catchUp) {
            this.catchUp = false;
            int inc = this.regeneration * 75;
            int amount = inc > 0 ? (int) ((System.currentTimeMillis() - this.lastActive) / inc) : 0;
            for (int a = 0; a < Math.min(amount, this.base.visSize()); a++) {
                if (this.rechargeOne(random)) change = true;
            }
        }
        if (this.count % 1200 == 0) {
            for (Aspect aspect : new ArrayList<>(this.base.getAspects())) {
                if (this.aspects.getAmount(aspect) > 0) continue;
                this.setBase(aspect, this.base.getAmount(aspect) - 1);
                if (random.nextInt(20) == 0 || this.base.getAmount(aspect) <= 0) {
                    this.base.remove(aspect);
                    if (random.nextInt(5) == 0) {
                        if (this.modifier == NodeModifier.BRIGHT) this.modifier = null;
                        else if (this.modifier == null) this.modifier = NodeModifier.PALE;
                        if (this.modifier == NodeModifier.PALE && random.nextInt(5) == 0) this.modifier = NodeModifier.FADING;
                    }
                    this.nodeChange();
                    break;
                }
                this.nodeChange();
            }
            if (this.base.isEmpty()) {
                // sem aspecto nenhum, o nó some: o do ar vira ar, o do tronco vira tora comum
                if (state.is(TCBlocks.SILVERWOOD_KNOT)) {
                    level.setBlockAndUpdate(pos, TCBlocks.SILVERWOOD_LOG.defaultBlockState());
                } else {
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                }
                return false;
            }
        }
        if (this.wait > 0) this.wait--;
        if (this.regeneration > 0 && this.wait == 0 && this.count % this.regeneration == 0) {
            this.lastActive = System.currentTimeMillis();
            if (this.rechargeOne(random)) change = true;
        }
        return change;
    }

    /**
     * Devolve um ponto a um aspecto que esteja faltando, sorteado entre os que faltam.
     *
     * <p>É assim no original: não é o primeiro da lista nem o mais vazio — é um qualquer entre os que ainda não
     * encheram.
     */
    private boolean rechargeOne(RandomSource random) {
        List<Aspect> missing = new ArrayList<>();
        for (Aspect aspect : this.base.getAspects()) {
            if (this.aspects.getAmount(aspect) < this.base.getAmount(aspect)) missing.add(aspect);
        }
        if (missing.isEmpty()) return false;
        this.aspects.add(missing.get(random.nextInt(missing.size())), 1);
        return true;
    }

    /** O {@code nodeChange}: o tempo de refazer é recalculado e os vizinhos ficam sabendo. */
    private void nodeChange() {
        this.regeneration = -1;
        this.sync();
    }

    // ---------------------------------------------------------------------------------------------- os tipos

    /**
     * O {@code handleTaintNode}: o maculado pinta de Terra Maculada uma coluna a até oito blocos e espalha fibras em
     * volta; um nó comum dentro da Terra Maculada, uma vez em quinhentos, vira maculado.
     */
    private boolean taint(ServerLevel level, BlockPos pos) {
        RandomSource random = level.getRandom();
        if (this.type == NodeType.TAINTED && this.count % 50 == 0) {
            BlockPos at = new BlockPos(pos.getX() + random.nextInt(8) - random.nextInt(8), pos.getY(),
                    pos.getZ() + random.nextInt(8) - random.nextInt(8));
            if (!level.getBiome(at).is(net.thaumcraft.world.TCBiomes.TAINTED_LAND)) {
                net.thaumcraft.world.BiomePainter.paint(level, at, net.thaumcraft.world.TCBiomes.TAINTED_LAND);
            }
            if (random.nextBoolean()) {
                BlockPos fibre = pos.offset(random.nextInt(5) - random.nextInt(5), random.nextInt(5) - random.nextInt(5),
                        random.nextInt(5) - random.nextInt(5));
                net.thaumcraft.block.TaintFibreBlock.spreadFibres(level, fibre);
            }
        } else if (this.type != NodeType.PURE && this.type != NodeType.TAINTED && this.count % 100 == 0
                && level.getBiome(pos).is(net.thaumcraft.world.TCBiomes.TAINTED_LAND) && random.nextInt(500) == 0) {
            this.type = NodeType.TAINTED;
            this.nodeChange();
        }
        return false;
    }

    /** Os nós que pintam bioma só o fazem na superfície (fora do Nether e do Fim), como no original. */
    private static boolean paintsBiomes(ServerLevel level) {
        return level.dimension() != net.minecraft.world.level.Level.NETHER && level.dimension() != net.minecraft.world.level.Level.END;
    }

    /**
     * O {@code handleDarkNode}: o sombrio pinta de Sinistro uma coluna a até doze blocos e, com alguém a vinte e quatro
     * blocos, às vezes chama um zumbi furioso (até três por perto).
     */
    private boolean dark(ServerLevel level, BlockPos pos) {
        if (this.type != NodeType.DARK || this.count % 50 != 0 || !paintsBiomes(level)) return false;
        RandomSource random = level.getRandom();
        BlockPos at = new BlockPos(pos.getX() + random.nextInt(12) - random.nextInt(12), pos.getY(),
                pos.getZ() + random.nextInt(12) - random.nextInt(12));
        if (!level.getBiome(at).is(net.thaumcraft.world.TCBiomes.EERIE)) {
            net.thaumcraft.world.BiomePainter.paint(level, at, net.thaumcraft.world.TCBiomes.EERIE);
        }
        if (random.nextBoolean() && level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 24.0, false) != null) {
            var near = level.getEntitiesOfClass(net.thaumcraft.entity.GiantBrainyZombieEntity.class,
                    new net.minecraft.world.phys.AABB(pos).inflate(10.0, 6.0, 10.0));
            if (near.size() <= 3) {
                var giant = net.thaumcraft.registry.TCEntities.GIANT_BRAINY_ZOMBIE.create(level, net.minecraft.world.entity.EntitySpawnReason.SPAWNER);
                if (giant != null) {
                    giant.snapTo(pos.getX() + (random.nextDouble() - random.nextDouble()) * 5.0, pos.getY() + random.nextInt(3) - 1,
                            pos.getZ() + (random.nextDouble() - random.nextDouble()) * 5.0, random.nextFloat() * 360.0f, 0.0f);
                    if (giant.checkSpawnRules(level, net.minecraft.world.entity.EntitySpawnReason.SPAWNER) && giant.checkSpawnObstruction(level)) {
                        level.addFreshEntity(giant);
                        level.levelEvent(net.minecraft.world.level.block.LevelEvent.PARTICLES_MOBBLOCK_SPAWN, pos, 0);
                        giant.spawnAnim();
                    }
                }
            }
        }
        return false;
    }

    /**
     * O {@code handlePureNode}: o puro devolve a Floresta Mágica à Terra Maculada em volta; o que mora num tronco de
     * pinheiro-de-prata pinta de Floresta Mágica qualquer bioma perto.
     */
    private boolean pure(ServerLevel level, BlockPos pos, BlockState state) {
        if (this.type != NodeType.PURE || this.count % 50 != 0 || !paintsBiomes(level)) return false;
        RandomSource random = level.getRandom();
        BlockPos at = new BlockPos(pos.getX() + random.nextInt(8) - random.nextInt(8), pos.getY(),
                pos.getZ() + random.nextInt(8) - random.nextInt(8));
        var biome = level.getBiome(at);
        if (biome.is(net.thaumcraft.world.TCBiomes.MAGICAL_FOREST)) return false;
        if (biome.is(net.thaumcraft.world.TCBiomes.TAINTED_LAND) || state.is(TCBlocks.SILVERWOOD_KNOT)) {
            net.thaumcraft.world.BiomePainter.paint(level, at, net.thaumcraft.world.TCBiomes.MAGICAL_FOREST);
        }
        return false;
    }

    /** O {@code handleNodeStability}: o instável solta orbes; travado, às vezes se acalma, e o esmaecido revive. */
    private boolean stability(ServerLevel level, BlockPos pos) {
        if (this.count % 100 != 0) return false;
        boolean change = false;
        RandomSource random = level.getRandom();
        if (this.type == NodeType.UNSTABLE && random.nextBoolean()) {
            if (this.lock == 0) {
                List<Aspect> primals = new ArrayList<>();
                for (Aspect aspect : this.base.getAspects()) if (aspect.isPrimal()) primals.add(aspect);
                if (!primals.isEmpty()) {
                    Aspect aspect = primals.get(random.nextInt(primals.size()));
                    if (this.aspects.reduce(aspect, 1)) {
                        level.addFreshEntity(new net.thaumcraft.entity.AspectOrbEntity(level, pos.getX() + 0.5, pos.getY() + 0.5,
                                pos.getZ() + 0.5, aspect, 1));
                        change = true;
                    }
                }
            } else if (random.nextInt(10000 / this.lock) == 42) {
                this.type = NodeType.NORMAL;
                change = true;
            }
        }
        if (this.modifier == NodeModifier.FADING && this.lock > 0 && random.nextInt(12500 / this.lock) == 69) {
            this.modifier = NodeModifier.PALE;
            change = true;
        }
        return change;
    }

    /**
     * O {@code handleHungryNodeFirst}: dos dois lados. Quem está a quinze blocos é puxado (quem joga, do lado de quem
     * joga; o resto, no servidor); quem chega a menos de dois leva dano e, se morre, alimenta o nó. De quem joga, o nó
     * também mostra migalhas dos blocos em volta vindo para ele.
     */
    private boolean hungryFirst(Level level, BlockPos pos) {
        if (this.type != NodeType.HUNGRY) return false;
        boolean change = false;
        if (level.isClientSide()) {
            for (int a = 0; a < 2; a++) {
                BlockPos target = this.hungryTarget(level, pos);
                if (target != null) {
                    BlockState block = level.getBlockState(target);
                    if (!block.isAir()) {
                        net.thaumcraft.client.NodeClient.hungryFx(level, target, block, pos);
                    }
                }
            }
        }
        Vec3 centre = Vec3.atCenterOf(pos);
        for (Entity entity : level.getEntities((Entity) null, new AABB(pos).inflate(15.0))) {
            if (entity instanceof Player player && player.getAbilities().invulnerable) continue;
            // quem joga se move do lado de quem joga
            if (entity instanceof Player != level.isClientSide()) continue;
            if (level.isClientSide() && !net.thaumcraft.client.NodeClient.isLocalPlayer(entity)) continue;
            if (!level.isClientSide() && entity.isAlive() && !entity.isInvulnerable() && entity.distanceToSqr(centre) < 4.0) {
                change |= this.feed((ServerLevel) level, entity);
            }
            double dx = (centre.x - entity.getX()) / 15.0;
            double dy = (centre.y - entity.getY()) / 15.0;
            double dz = (centre.z - entity.getZ()) / 15.0;
            double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double pull = 1.0 - d;
            if (pull > 0.0) {
                pull *= pull;
                entity.setDeltaMovement(entity.getDeltaMovement().add(dx / d * pull * 0.15, dy / d * pull * 0.25, dz / d * pull * 0.15));
                entity.hurtMarked = true;
            }
        }
        return change;
    }

    /** Quem chegou perto demais do faminto: um de dano, e se morre, um ponto de um dos primordiais de que era feito. */
    private boolean feed(ServerLevel level, Entity entity) {
        entity.hurtServer(level, level.damageSources().fellOutOfWorld(), 1.0f);
        if (entity.isAlive()) return false;
        AspectList found = net.thaumcraft.research.ScanManager.aspectsOf(entity);
        if (found == null || found.size() == 0) return false;
        AspectList primals = DeconstructionTableBlockEntity.reduceToPrimals(found);
        if (primals.size() == 0) return false;
        Aspect aspect = primals.getAspects().get(level.getRandom().nextInt(primals.size()));
        if (this.aspects.getAmount(aspect) < this.base.getAmount(aspect)) {
            this.addToContainer(aspect, 1);
            return true;
        }
        if (level.getRandom().nextInt(1 + this.base.getAmount(aspect) * 2) < primals.getAmount(aspect)) {
            this.base.add(aspect, 1);
            return true;
        }
        return false;
    }

    /** O {@code handleHungryNodeSecond}: de cinquenta em cinquenta tiques, come um bloco em volta, se não for duro. */
    private boolean hungrySecond(ServerLevel level, BlockPos pos) {
        if (this.type != NodeType.HUNGRY || this.count % 50 != 0) return false;
        BlockPos target = this.hungryTarget(level, pos);
        if (target == null) return false;
        BlockState block = level.getBlockState(target);
        if (block.isAir()) return false;
        float hardness = block.getDestroySpeed(level, target);
        if (hardness >= 0.0f && hardness < 5.0f) level.destroyBlock(target, true);
        return false;
    }

    /** Um bloco à vista do nó, numa direção qualquer até quinze blocos, nunca acima do chão daquela coluna. */
    private BlockPos hungryTarget(Level level, BlockPos pos) {
        RandomSource random = level.getRandom();
        int tx = pos.getX() + random.nextInt(16) - random.nextInt(16);
        int ty = pos.getY() + random.nextInt(16) - random.nextInt(16);
        int tz = pos.getZ() + random.nextInt(16) - random.nextInt(16);
        int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, tx, tz);
        if (ty > top) ty = top;
        Vec3 from = Vec3.atCenterOf(pos);
        BlockHitResult hit = level.clip(new ClipContext(from, new Vec3(tx + 0.5, ty + 0.5, tz + 0.5),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, net.minecraft.world.phys.shapes.CollisionContext.empty()));
        if (hit.getType() != HitResult.Type.BLOCK) return null;
        BlockPos at = hit.getBlockPos();
        if (at.equals(pos) || at.distToCenterSqr(from) >= 256.0) return null;
        return at;
    }

    // ---------------------------------------------------------------------------------------------- o que tem

    /** O {@code addToContainer}: põe até o teto; devolve o que sobrou. */
    public int addToContainer(Aspect aspect, int amount) {
        int left = Math.max(0, amount + this.aspects.getAmount(aspect) - this.base.getAmount(aspect));
        if (amount - left > 0) this.aspects.add(aspect, amount - left);
        return left;
    }

    /** O {@code setNodeVisBase}. */
    private void setBase(Aspect aspect, int amount) {
        int have = this.base.getAmount(aspect);
        if (have < amount) this.base.add(aspect, amount - have);
        else this.base.remove(aspect, have - amount);
    }

    /** Tira vis do nó, se houver. */
    public boolean take(Aspect aspect, int amount) {
        if (aspect == null || amount <= 0) return false;
        if (this.aspects.getAmount(aspect) < amount) return false;
        this.aspects.reduce(aspect, amount);
        this.sync();
        return true;
    }

    /** Devolve vis ao nó, sem passar do que ele comporta. */
    public int give(Aspect aspect, int amount) {
        if (aspect == null || amount <= 0) return 0;
        int room = this.base.getAmount(aspect) - this.aspects.getAmount(aspect);
        int given = Math.min(room, amount);
        if (given <= 0) return 0;
        this.aspects.add(aspect, given);
        this.sync();
        return given;
    }

    public AspectList aspects() {
        return this.aspects;
    }

    public AspectList baseAspects() {
        return this.base;
    }

    public NodeType type() {
        return this.type;
    }

    public NodeModifier modifier() {
        return this.modifier;
    }

    /** Quanto de vis o nó tem agora, somando tudo. */
    public int visSize() {
        return this.aspects.visSize();
    }

    public void setup(AspectList base, NodeType type, NodeModifier modifier) {
        this.base = base;
        this.aspects = base.copy();
        this.type = type;
        this.modifier = modifier;
        this.regeneration = -1;
        this.sync();
    }

    /** Para o transdutor: tudo de uma vez, com o que tem agora separado do teto. */
    public void setup(AspectList base, AspectList now, NodeType type, NodeModifier modifier) {
        this.setup(base, type, modifier);
        this.aspects = now.copy();
        this.sync();
    }

    /**
     * A cor do último aspecto que uma varinha bebeu daqui, o {@code drainColor} do original.
     *
     * <p>É para onde a linha da drenagem vai puxando a cor dela. Só o desenho usa isto.
     */
    private int drainColour = 0xFFFFFF;
    /** A cor que a linha mostra agora, andando um quinto por tique até a {@link #drainColour}. */
    private int shownColour = 0xFFFFFF;
    private long shownAt;

    /** Uma varinha acabou de beber este aspecto daqui. */
    public void drained(Aspect aspect) {
        if (this.drainColour == aspect.color()) return;
        this.drainColour = aspect.color();
        this.sync();
    }

    /**
     * A cor da linha da drenagem neste instante.
     *
     * <p>O original não troca de cor de uma vez: a cada tique ele anda um quinto do caminho da cor que
     * estava até a do aspecto bebido, e é isso que dá à linha o degradê quando a varinha passa de um
     * aspecto a outro.
     */
    public int shownColour(long gameTime) {
        int steps = (int) Math.min(20, gameTime - this.shownAt);
        for (int step = 0; step < steps; step++) {
            int r = ((this.drainColour >> 16 & 255) + (this.shownColour >> 16 & 255) * 4) / 5;
            int g = ((this.drainColour >> 8 & 255) + (this.shownColour >> 8 & 255) * 4) / 5;
            int b = ((this.drainColour & 255) + (this.shownColour & 255) * 4) / 5;
            this.shownColour = r << 16 | g << 8 | b;
        }
        this.shownAt = gameTime;
        return this.shownColour;
    }

    protected void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.base = input.read("base", AspectList.CODEC).orElseGet(AspectList::new);
        this.aspects = input.read("aspects", AspectList.CODEC).orElseGet(AspectList::new);
        this.type = input.read("type", NodeType.CODEC).orElse(NodeType.NORMAL);
        this.modifier = input.read("modifier", NodeModifier.CODEC).orElse(null);
        this.drainColour = input.getIntOr("drain", 0xFFFFFF);
        this.lastActive = input.getLongOr("lastActive", 0L);
        // o tempo em que o pedaço de mundo ficou sem ninguém: o nó recupera o que teria refeito nele
        int regen = this.regenerationInterval();
        if (regen > 0 && this.lastActive > 0L && System.currentTimeMillis() > this.lastActive + regen * 75L) this.catchUp = true;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("base", AspectList.CODEC, this.base);
        output.store("aspects", AspectList.CODEC, this.aspects);
        output.store("type", NodeType.CODEC, this.type);
        if (this.modifier != null) output.store("modifier", NodeModifier.CODEC, this.modifier);
        output.putInt("drain", this.drainColour);
        output.putLong("lastActive", this.lastActive);
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
