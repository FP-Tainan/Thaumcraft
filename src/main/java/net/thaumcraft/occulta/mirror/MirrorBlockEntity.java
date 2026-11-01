package net.thaumcraft.occulta.mirror;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaComponents;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * A alma de um espelho: a {@code TileEntityMirror} do Witchery.
 *
 * <p>Ela guarda três coisas: <b>para onde</b> o espelho leva, se o <b>Reflexo</b> dele já morreu, e quem já lhe
 * passou à frente — que é o que o espelho tem a contar a quem lhe pergunta.
 *
 * <p>Também conta, de duas em duas segundas, <b>quantos vivos</b> estão à frente do vidro. É por esse número que o
 * desenhista sabe se pinta a moldura vazia ou o vidro com gente dentro; a conta corre dos dois lados, como no
 * original, e por isso não precisa de viajar pela rede.
 */
public class MirrorBlockEntity extends BlockEntity {
    /** De quantas em quantas batidas se conta quem está à frente: as do original, por lado. */
    private static final int COUNT_SERVER = 40;
    private static final int COUNT_CLIENT = 10;

    /** Quanto tempo o outro lado fica fechado depois de alguém passar: os sessenta tiques do original. */
    public static final int COOLDOWN = 60;

    /** A quanto se ouve a pergunta ao espelho: os sessenta e quatro blocos do original. */
    public static final double HEARD = 64.0;

    private @Nullable MirrorLink link;
    private boolean connected;
    private boolean hollow;
    private int men;
    private long ticks;
    private long cooldown;
    private @Nullable UUID favorite;
    private String favoriteName = "";

    /** E a <b>Mais Bela</b> que ele fez nascer, se fez: quem ela é, e há quanto tempo. */
    private @Nullable UUID fairest;
    private long lastFairestSpawn = Long.MIN_VALUE;
    private final Set<String> seen = new LinkedHashSet<>();

    public MirrorBlockEntity(BlockPos pos, BlockState state) {
        super(OccultaBlocks.WITCH_MIRROR_ENTITY, pos, state);
    }

    // ------------------------------------------------------------------ o que ela guarda

    public @Nullable MirrorLink link() {
        return this.link;
    }

    public void linkTo(@Nullable MirrorLink link) {
        this.link = link;
        this.setChanged();
    }

    /** Se o Reflexo deste espelho já morreu, e ele passou a ser ponte. */
    public boolean hollow() {
        return this.hollow;
    }

    public void setHollow(boolean hollow) {
        this.hollow = hollow;
        this.sync();
    }

    /** Se o espelho do outro lado foi assentado de novo, e por isso mudou de lugar. */
    public boolean connected() {
        return this.connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
        this.sync();
    }

    /** Quantos vivos estão à frente do vidro. */
    public int men() {
        return this.men;
    }

    public boolean onCooldown() {
        return this.ticks < this.cooldown;
    }

    public void addCooldown(int quanto) {
        this.cooldown = this.ticks + quanto;
    }

    /** Para onde este espelho leva — abrindo a cela dele, se ainda não tiver uma. */
    public @Nullable MirrorLink orClaim(ServerLevel level) {
        if (this.link != null) return this.link;
        if (MirrorWorld.is(level)) return null;
        BlockPos cela = MirrorWorld.claimCell(level, this.worldPosition);
        if (cela == null) return null;
        this.linkTo(new MirrorLink(MirrorWorld.LEVEL, cela));
        return this.link;
    }

    // ------------------------------------------------------------------ o item

    /** Escreve no item o que ele tem de levar: a ligação e o vazado. */
    public void writeToItem(ItemStack item) {
        item.set(OccultaComponents.MIRROR,
                new MirrorLink.Held(Optional.ofNullable(this.link), this.hollow));
    }

    /**
     * Lê do item o que ele trazia: o {@code loadFromItem} do original.
     *
     * <p>Se a ligação aponta para um espelho selado do Mundo do Espelho, avisa-se o de lá que o de cá mudou de
     * lugar — é assim que um espelho arrancado e assentado noutra parede continua sendo o mesmo.
     */
    public void readFromItem(ServerLevel level, ItemStack item) {
        MirrorLink.Held trazia = item.getOrDefault(OccultaComponents.MIRROR, MirrorLink.Held.EMPTY);
        this.link = trazia.link().orElse(null);
        this.hollow = trazia.hollow();
        this.setChanged();
        if (this.link == null || MirrorWorld.is(level)) return;

        MirrorBlockEntity outro = other(level, this.link);
        if (outro == null) return;
        outro.link = new MirrorLink(level.dimension(), this.worldPosition);
        outro.connected = true;
        outro.sync();
    }

    /** O espelho do outro lado deixou de estar de pé: o {@code isConnected = false} do {@code getDrops}. */
    public void unlinkOther() {
        if (this.link == null || !(this.level instanceof ServerLevel level)) return;
        if (MirrorWorld.is(level)) return;
        MirrorBlockEntity outro = other(level, this.link);
        if (outro == null) return;
        outro.connected = false;
        outro.sync();
    }

    /** A alma do espelho do outro lado, se ela lá estiver. */
    public static @Nullable MirrorBlockEntity other(ServerLevel daqui, MirrorLink link) {
        ServerLevel lá = daqui.getServer().getLevel(link.level());
        if (lá == null) return null;
        if (!lá.isLoaded(link.pos())) lá.getChunk(link.pos());
        return lá.getBlockEntity(link.pos()) instanceof MirrorBlockEntity alma ? alma : null;
    }

    // ------------------------------------------------------------------ a batida

    public void tick() {
        if (this.level == null) return;
        this.ticks++;
        int cada = this.level.isClientSide() ? COUNT_CLIENT : COUNT_SERVER;
        if (this.ticks % cada != 1) return;

        Direction olha = this.getBlockState().getValue(MirrorBlock.FACING);
        AABB frente = front(this.worldPosition, olha);
        List<LivingEntity> quantos = this.level.getEntitiesOfClass(LivingEntity.class, frente);
        this.men = quantos.size();
        if (this.level.isClientSide()) return;
        for (LivingEntity vivo : quantos) {
            if (vivo instanceof Player gente) this.seen.add(gente.getGameProfile().name());
        }
    }

    /**
     * A caixa de quem está à frente do vidro: uma casa para cada lado e <b>quatro</b> para a frente, que é o que o
     * original olha.
     */
    public static AABB front(BlockPos onde, Direction olha) {
        int xMin = -1;
        int xMax = 1;
        int zMin = -1;
        int zMax = 1;
        switch (olha) {
            case NORTH -> {
                zMin = -4;
                zMax = 0;
            }
            case SOUTH -> {
                zMin = 0;
                zMax = 4;
            }
            case WEST -> {
                xMin = -4;
                xMax = 0;
            }
            default -> {
                xMin = 0;
                xMax = 4;
            }
        }
        return new AABB(onde.getX() + xMin, onde.getY(), onde.getZ() + zMin,
                onde.getX() + xMax + 1, onde.getY() + 1, onde.getZ() + zMax + 1);
    }

    // ------------------------------------------------------------------ espelho, espelho meu

    /**
     * O clique no espelho: o terceiro caminho do {@code depolyDemon} do original.
     *
     * <p>Aparece a <b>cara</b> no vidro, e todos por perto ouvem a pergunta. O espelho responde quem é a mais bela
     * — e diz também quem mais lhe passou à frente desde a última vez.
     *
     * <p><b>Do original ficam de fora, declarados</b>, os outros dois caminhos: com um Frasco de Vínculo na mão o
     * espelho <b>veste</b> quem joga com a pele de outra pessoa (isso é a Dobra, que este porte ainda não tem), e
     * com uma Esfera de Quartzo ele faz a Granada Duplicadora (que também não existe aqui). E a <b>mais bela</b>
     * nunca é uma Seguidora nascida na hora, por a Seguidora não estar portada: é sempre alguém que joga.
     */
    public void askTheMirror(Player quem) {
        if (!(this.level instanceof ServerLevel level)) return;
        if (this.hollow || MirrorWorld.is(level)) return;

        Direction olha = this.getBlockState().getValue(MirrorBlock.FACING);
        if (!level.getEntitiesOfClass(MirrorFaceEntity.class,
                MirrorBlock.trigger(this.worldPosition, olha)).isEmpty()) {
            return;
        }

        MirrorFaceEntity.show(level, this.worldPosition, olha);
        level.sendParticles(ParticleTypes.WITCH, this.worldPosition.getX() + 0.5,
                this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5, 16, 0.5, 0.5, 0.5, 0.0);
        level.playSound(null, this.worldPosition, net.thaumcraft.occulta.OccultaSounds.REFLECTION_SPEECH.value(), SoundSource.BLOCKS,
                1.0f, 1.0f);

        for (Player outro : level.players()) {
            if (outro.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5,
                    this.worldPosition.getZ() + 0.5) > HEARD * HEARD) {
                continue;
            }
            outro.sendSystemMessage(Component.translatable("tc.mirror.ask",
                    quem.getGameProfile().name()));
        }

        this.sayTheFairest(level, quem);
        this.saySeen(quem);
        if (this.isFavorite(quem)) this.seen.clear();
        this.setChanged();
    }

    /** <b>Quantas vezes em cem</b> o espelho faz nascer uma Mais Bela, e de quanto em quanto tempo. */
    public static final double CHANCE_DA_MAIS_BELA = 0.01;
    public static final int ESPERA_DA_MAIS_BELA = 2400;

    /** A que distância dele ela nasce, e em quantas tentativas ele procura chão. */
    public static final int LONGE = 50;
    public static final int TENTATIVAS = 25;
    public static final int ACIMA = 20;
    public static final int ABAIXO = 20;

    /** E a caixa em que ele a procura depois: cem para os lados, e o céu e o chão do original. */
    public static final double PROCURA_XZ = 100.0;
    public static final double PROCURA_ACIMA = 32.0;
    public static final double PROCURA_ABAIXO = 100.0;

    /**
     * Quem é a mais bela: quem joga, aquela outra pessoa que o espelho guardou — ou <b>a estranha</b>.
     *
     * <p>É aqui que o espelho faz a coisa mais estranha do mod. Não havendo outra pessoa a quem ele já
     * tenha dito que sim, uma vez em cem ele <b>faz nascer alguém</b> a cinquenta blocos dali, com nome de
     * conto de fadas, e diz a quem perguntou que essa é mais bela. O espelho não mente: o preço de
     * perguntar é que o mundo passa a ter alguém mais belo do que você andando por ele.
     */
    private void sayTheFairest(ServerLevel level, Player quem) {
        if (this.aMaisBelaAindaAnda(level, quem)) return;

        this.fairest = null;
        if (this.favorite != null && !this.isFavorite(quem)) {
            quem.sendSystemMessage(Component.translatable("tc.mirror.another")
                    .withStyle(ChatFormatting.AQUA));
            Player outro = level.getPlayerByUUID(this.favorite);
            if (outro != null) this.sayBearing(quem, outro);
            return;
        }
        if (this.fazUmaMaisBela(level, quem)) return;

        this.favorite = quem.getUUID();
        this.favoriteName = quem.getGameProfile().name();
        quem.sendSystemMessage(Component.translatable("tc.mirror.you").withStyle(ChatFormatting.AQUA));
    }

    /** Se a que ele fez nascer ainda anda por perto e viva, é ela outra vez. */
    private boolean aMaisBelaAindaAnda(ServerLevel level, Player quem) {
        if (this.fairest == null) return false;
        var caixa = new net.minecraft.world.phys.AABB(
                this.worldPosition.getX() - PROCURA_XZ, this.worldPosition.getY() - PROCURA_ABAIXO,
                this.worldPosition.getZ() - PROCURA_XZ, this.worldPosition.getX() + PROCURA_XZ,
                this.worldPosition.getY() + PROCURA_ACIMA, this.worldPosition.getZ() + PROCURA_XZ);
        for (var ela : level.getEntitiesOfClass(
                net.thaumcraft.occulta.vampire.FollowerEntity.class, caixa)) {
            if (!ela.getUUID().equals(this.fairest) || !ela.isAlive()) continue;
            this.sayNotFairest(quem, ela);
            return true;
        }
        return false;
    }

    /**
     * Faz nascer uma, se a sorte deixar — e se achar chão para ela.
     *
     * <p>Não achando chão em vinte e cinco tentativas, a estranha <b>não nasce</b> e quem perguntou é o
     * mais belo. É o original: o mundo tem a última palavra.
     */
    private boolean fazUmaMaisBela(ServerLevel level, Player quem) {
        if (level.getGameTime() <= this.lastFairestSpawn + ESPERA_DA_MAIS_BELA) return false;
        if (level.getRandom().nextDouble() >= CHANCE_DA_MAIS_BELA) return false;
        return nasce(level, this.worldPosition, quem, ela -> {
            this.fairest = ela.getUUID();
            this.lastFairestSpawn = level.getGameTime();
            this.sayNotFairest(quem, ela);
        });
    }

    /**
     * O nascimento em si, à parte da sorte — para que a prova possa pedi-lo sem esperar cem perguntas.
     *
     * @return falso se o mundo não lhe deu chão
     */
    public static boolean nasce(ServerLevel level, BlockPos espelho, Player quem,
                                java.util.function.Consumer<net.thaumcraft.occulta.vampire.FollowerEntity>
                                        depois) {
        BlockPos onde = ondeElaCabe(level, espelho);
        if (onde == null) return false;

        int tipo = 1 + level.getRandom().nextInt(net.thaumcraft.occulta.vampire.FollowerEntity.BELAS);
        var ela = net.thaumcraft.occulta.OccultaEntities.FOLLOWER.create(level,
                net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
        if (ela == null) return false;
        ela.tipo(tipo);
        ela.setCustomName(Component.literal(net.thaumcraft.occulta.vampire.FollowerNames
                .sorteia(tipo, level.getRandom())));
        ela.setPersistenceRequired();
        ela.snapTo(0.5 + onde.getX(), 1.01 + onde.getY(), 0.5 + onde.getZ(), 0.0f, 0.0f);
        level.addFreshEntity(ela);
        depois.accept(ela);
        return true;
    }

    /**
     * Onde ela cabe: a cinquenta a noventa e nove blocos dali, no primeiro bloco firme com duas casas de
     * ar por cima — procurando de cima para baixo, vinte acima do espelho até vinte abaixo.
     */
    public static @Nullable BlockPos ondeElaCabe(ServerLevel level, BlockPos espelho) {
        var sorte = level.getRandom();
        for (int n = 0; n < TENTATIVAS; n++) {
            int x = espelho.getX() + (sorte.nextBoolean() ? 1 : -1) * (LONGE + sorte.nextInt(LONGE));
            int z = espelho.getZ() + (sorte.nextBoolean() ? 1 : -1) * (LONGE + sorte.nextInt(LONGE));
            int alto = Math.min(espelho.getY() + ACIMA, level.getMaxY() - 2);
            int baixo = Math.max(espelho.getY() - ABAIXO, level.getMinY() + 1);
            for (int y = alto; y >= baixo; y--) {
                BlockPos chão = new BlockPos(x, y, z);
                if (!level.getBlockState(chão).isSolidRender()) continue;
                if (!level.isEmptyBlock(chão.above()) || !level.isEmptyBlock(chão.above(2))) continue;
                return chão;
            }
        }
        return null;
    }

    /** «Há outra mais bela do que tu» — e para que lado ela está. */
    private void sayNotFairest(Player quem, net.thaumcraft.occulta.vampire.FollowerEntity ela) {
        quem.sendSystemMessage(Component.translatable(
                        ela.tipo() == net.thaumcraft.occulta.vampire.FollowerEntity.O_HOMEM
                                ? "tc.mirror.anotherm" : "tc.mirror.anotherf")
                .withStyle(ChatFormatting.AQUA));
        this.sayBearing(quem, ela);
    }

    /** Para que lado a mais bela está: os oito rumos do original. */
    private void sayBearing(Player quem, LivingEntity outro) {
        double rad = Math.atan2(0.5 + this.worldPosition.getZ() - outro.getZ(),
                0.5 + this.worldPosition.getX() - outro.getX());
        double rumo = (Math.toDegrees(rad) + 180.0 + 90.0) % 360.0;
        if (rumo < 0.0) rumo += 360.0;
        int qual = (int) rumo / 45;
        if (qual > 7 || qual < 0) qual = 0;
        quem.sendSystemMessage(Component.translatable("tc.mirror.bearing" + qual).withStyle(ChatFormatting.AQUA));
    }

    /** E quem mais lhe passou à frente. */
    private void saySeen(Player quem) {
        List<String> nomes = new ArrayList<>(this.seen);
        nomes.remove(quem.getGameProfile().name());
        Collections.sort(nomes);
        if (nomes.isEmpty()) {
            quem.sendSystemMessage(Component.translatable("tc.mirror.seen.none").withStyle(ChatFormatting.AQUA));
            return;
        }
        quem.sendSystemMessage(Component.translatable("tc.mirror.seen", String.join(", ", nomes))
                .withStyle(ChatFormatting.AQUA));
    }

    public boolean isFavorite(Player quem) {
        return this.favorite != null && this.favorite.equals(quem.getUUID());
    }

    // ------------------------------------------------------------------ guardar e mandar

    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.link = input.read("link", MirrorLink.CODEC).orElse(null);
        this.connected = input.getBooleanOr("connected", false);
        this.hollow = input.getBooleanOr("hollow", false);
        this.favorite = input.read("favorite", net.minecraft.core.UUIDUtil.CODEC).orElse(null);
        this.favoriteName = input.getStringOr("favorite_name", "");
        this.fairest = input.read("fairest", net.minecraft.core.UUIDUtil.CODEC).orElse(null);
        this.lastFairestSpawn = input.getLongOr("fairest_at", Long.MIN_VALUE);
        this.seen.clear();
        this.seen.addAll(input.read("seen",
                com.mojang.serialization.Codec.STRING.listOf()).orElse(List.of()));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.link != null) output.store("link", MirrorLink.CODEC, this.link);
        output.putBoolean("connected", this.connected);
        output.putBoolean("hollow", this.hollow);
        if (this.favorite != null) output.store("favorite", net.minecraft.core.UUIDUtil.CODEC, this.favorite);
        output.putString("favorite_name", this.favoriteName);
        if (this.fairest != null) output.store("fairest", net.minecraft.core.UUIDUtil.CODEC, this.fairest);
        output.putLong("fairest_at", this.lastFairestSpawn);
        output.store("seen", com.mojang.serialization.Codec.STRING.listOf(), List.copyOf(this.seen));
    }

    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener>
            getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }
}
