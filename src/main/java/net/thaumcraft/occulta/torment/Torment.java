package net.thaumcraft.occulta.torment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.dimension.DimensionType;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.world.DynamicDimensions;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * O <b>Tormento</b>: o {@code WorldProviderTorment} do Witchery.
 *
 * <p>É a última dimensão do ofício, e a única que não é um lugar para onde se vai — é um lugar para onde se
 * é <b>mandado</b>. Não há portal de entrada: há o símbolo <b>Tormentum</b>, que manda alguém para lá, e há o
 * <b>Senhor do Tormento</b>, que, ferido no mundo de cima, foge e arrasta consigo quem o feriu.
 *
 * <p>Lá dentro é escuro, não é dia nunca, não chove, não cai raio e o céu é <b>vermelho</b>. O chão é um
 * labirinto de paredes que não se veem, e a única saída é o <b>portal</b> do outro extremo dele — que, em uma
 * de cada vinte vezes, em vez de mandar para casa, manda para <b>outro andar</b>.
 *
 * <h2>Como se entra e como se sai</h2>
 *
 * <p>O original não teleporta na hora: ele <b>escreve um mandado</b> no jogador e o cumpre de vinte em vinte
 * batidas, e é por isso que quem é tormentado some um instante depois de ser ferido e não no mesmo golpe.
 * Esse adiamento fica, porque é o que dá tempo ao bicho que mandou de desaparecer primeiro.
 *
 * <p>O mandado tem quatro valores: <b>nada</b>, <b>começa</b>, <b>começa com o chefe</b> e <b>acaba</b>.
 * Começando, o andar é sorteado entre os seis — ou vem escrito, quando foi o Senhor que mandou, para que
 * todos os que ele arrastou caiam no <b>mesmo</b>. Acabando, volta-se para a <b>cama</b> de quem é, ou para
 * o nascimento do mundo, subindo ou descendo até achar chão firme com dois de ar em cima.
 */
public final class Torment {
    /** O mundo do Tormento. */
    public static final ResourceKey<Level> LEVEL = DynamicDimensions.key("torment");

    /** E o feitio dele: escuro, sem céu, hora parada e nada de chuva. */
    public static final ResourceKey<DimensionType> TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, Thaumcraft.id("torment"));

    // ------------------------------------------------------------------ o mandado

    /** Mandado nenhum: o {@code TORMENT_NONE}. */
    public static final int NADA = 0;
    /** Vai, e num andar ao acaso: o {@code TORMENT_BEGIN}. */
    public static final int COMEÇA = 1;
    /** Vai, naquele andar, e com o chefe à espera: o {@code TORMENT_BEGIN_WITH_BOSS}. */
    public static final int COM_O_CHEFE = 2;
    /** Volta para casa: o {@code TORMENT_END}. */
    public static final int ACABA = 3;

    /** De quantas em quantas batidas o mandado se cumpre. */
    public static final int DE_QUANTO_EM_QUANTO = 20;

    /** Até onde se procura chão firme para pousar quem volta. */
    public static final int PROCURA_ATÉ = 255;

    /** E quantas casas de ar o chão de volta precisa de ter em cima. */
    public static final int AR_EM_CIMA = 2;

    /** A que distância, acima e abaixo do ponto de chegada, se procura um Senhor que já esteja lá. */
    public static final int DE_BAIXO = 2;
    public static final int ATÉ_ACIMA = 4;

    /** E com quanta vida o que espera lá embaixo nasce: metade. */
    public static final float METADE = 0.5f;

    /** O que fica escrito em quem foi mandado: o {@code WITCForceTorment} e o nível dele. */
    public record Mandado(int oquê, int andar) {
        public static final Mandado NENHUM = new Mandado(NADA, -1);

        public static final Codec<Mandado> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("what").forGetter(Mandado::oquê),
                Codec.INT.optionalFieldOf("level", -1).forGetter(Mandado::andar))
                .apply(i, Mandado::new));
    }

    public static final AttachmentType<Mandado> MANDADO = AttachmentRegistry.<Mandado>builder()
            .initializer(() -> Mandado.NENHUM)
            .persistent(Mandado.CODEC)
            .copyOnDeath()
            .buildAndRegister(Thaumcraft.id("torment_order"));

    private Torment() {
    }

    /** Se aquele mundo é o Tormento. */
    public static boolean is(Level level) {
        return level.dimension() == LEVEL;
    }

    /** O Tormento, abrindo-o se ainda não houver. */
    public static @Nullable ServerLevel level(MinecraftServer server) {
        var biomas = new FixedBiomeSource(server.registryAccess()
                .lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.NETHER_WASTES));
        return DynamicDimensions.getOrCreate(server, LEVEL, TYPE,
                new TormentChunkGenerator(biomas, server.overworld().getSeed()));
    }

    /** O labirinto deste mundo, se ele já estiver aberto. */
    public static @Nullable TormentMaze maze(MinecraftServer server) {
        ServerLevel mundo = level(server);
        if (mundo == null) return null;
        return mundo.getChunkSource().getGenerator() instanceof TormentChunkGenerator gerador
                ? gerador.maze() : null;
    }

    // ------------------------------------------------------------------ mandar e cumprir

    /** Escreve o mandado: o {@code setPlayerMustTorment}. */
    public static void order(ServerPlayer quem, int oquê, int andar) {
        quem.setAttached(MANDADO, new Mandado(oquê, andar));
    }

    /** O mandado que aquela pessoa tem: o {@code getPlayerMustTorment}. */
    public static Mandado order(ServerPlayer quem) {
        Mandado tem = quem.getAttachedOrCreate(MANDADO);
        return tem == null ? Mandado.NENHUM : tem;
    }

    /** Um andar ao acaso: o {@code getRandomTormentLevel}. */
    public static int randomLevel(Level level) {
        return level.getRandom().nextInt(TormentMaze.LEVELS);
    }

    /**
     * Cumpre o mandado, se houver um: o {@code updatePlayerEffects}.
     *
     * <p>Corre de vinte em vinte batidas, que é a conta do original, e é por isso que ela recebe o contador
     * de batidas do mundo em vez de o perguntar.
     */
    public static void tick(ServerLevel level, ServerPlayer quem) {
        if (level.getGameTime() % DE_QUANTO_EM_QUANTO != 0) return;
        Mandado mandado = order(quem);
        if (mandado.oquê() == NADA) return;

        if (mandado.oquê() == ACABA) {
            order(quem, NADA, -1);
            goHome(quem);
            return;
        }
        if (mandado.oquê() != COMEÇA && mandado.oquê() != COM_O_CHEFE) return;

        int andar = mandado.oquê() == COM_O_CHEFE && mandado.andar() >= 0
                ? Math.min(mandado.andar(), TormentMaze.LEVELS - 1) : randomLevel(level);
        order(quem, NADA, -1);
        send(quem, andar, mandado.oquê() == COM_O_CHEFE);
    }

    /**
     * Manda alguém para o andar daquele número, abrindo-lhe espaço onde cai.
     *
     * <p>O original limpa o três por três em volta do lugar de chegada, em duas alturas, antes de pousar
     * ninguém: a câmara de entrada já é toda passagem, mas um Senhor do Tormento que lá estivesse teria
     * levantado parede, e mais vale limpar.
     *
     * <p>Com o <b>chefe</b>, um <b>Senhor do Tormento com metade da vida</b> fica à espera na sala do meio
     * daquele andar — se já não houver um lá.
     *
     * @return se foi
     */
    public static boolean send(ServerPlayer quem, int andar, boolean comOChefe) {
        MinecraftServer server = quem.level().getServer();
        if (server == null) return false;
        ServerLevel lá = level(server);
        if (lá == null) return false;

        quem.stopRiding();
        pó(quem);

        int y = TormentMaze.floorOf(andar) + TormentMaze.DOOR_UP;
        for (int x = TormentMaze.DOOR_X - 1; x <= TormentMaze.DOOR_X + 1; x++) {
            for (int z = TormentMaze.DOOR_Z - 1; z <= TormentMaze.DOOR_Z + 1; z++) {
                for (int h = 0; h <= 1; h++) {
                    BlockPos casa = new BlockPos(x, y + h, z);
                    if (!lá.getBlockState(casa).isAir()) {
                        lá.setBlock(casa, Blocks.AIR.defaultBlockState(),
                                net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
                    }
                }
            }
        }

        quem.teleportTo(lá, TormentMaze.DOOR_X + 0.5, y, TormentMaze.DOOR_Z + 0.5, Set.of(),
                quem.getYRot(), quem.getXRot(), false);
        quem.clearFire();
        pó(quem);
        if (comOChefe) waitFor(lá, andar);
        return true;
    }

    /**
     * Põe um <b>Senhor do Tormento com metade da vida</b> à espera na sala do meio daquele andar, se já não
     * houver um lá.
     *
     * <p>É o fim do {@code updatePlayerEffects} do original: quando o mandado é «começa com o chefe», ele
     * varre o mundo à procura de um Senhor <b>à altura daquele andar</b> — de dois abaixo a quatro acima do
     * ponto de chegada — e só põe outro se não achar nenhum. É o que faz um coven inteiro cair no mesmo
     * andar e achar <b>um</b> chefe, e não um por pessoa.
     *
     * @return o que ficou à espera, ou o que já lá estava
     */
    public static @Nullable LordOfTormentEntity waitFor(ServerLevel level, int andar) {
        int y = TormentMaze.floorOf(andar) + TormentMaze.DOOR_UP;
        var caixa = new net.minecraft.world.phys.AABB(
                TormentMaze.ORIGIN_X, y - DE_BAIXO, TormentMaze.ORIGIN_Z,
                TormentMaze.ORIGIN_X + TormentMaze.SPAN_X, y + ATÉ_ACIMA,
                TormentMaze.ORIGIN_Z + TormentMaze.SPAN_Z);
        for (var tem : level.getEntitiesOfClass(LordOfTormentEntity.class, caixa)) {
            return tem;
        }

        var ele = OccultaEntities.LORD_OF_TORMENT.create(level, EntitySpawnReason.TRIGGERED);
        if (ele == null) return null;
        ele.snapTo(TormentMaze.LORD_X + 0.5, y - 1.0, TormentMaze.LORD_Z + 0.5, 0.0f, 0.0f);
        ele.setPersistenceRequired();
        ele.setHealth(ele.getMaxHealth() * METADE);
        level.addFreshEntity(ele);
        return ele;
    }

    /**
     * Manda alguém para casa: a cama dele, ou o nascimento do mundo.
     *
     * <p>A subida à procura de chão seguro é a do original, com o vaivém dele: tenta em cima, tenta em
     * baixo, e vai abrindo a conta até achar um bloco sólido com duas casas de ar por cima.
     *
     * @return se foi
     */
    public static boolean goHome(ServerPlayer quem) {
        MinecraftServer server = quem.level().getServer();
        if (server == null) return false;
        ServerLevel casa = server.overworld();

        quem.stopRiding();
        var dormiu = quem.getRespawnConfig();
        BlockPos onde = dormiu != null && dormiu.respawnData().dimension() == casa.dimension()
                ? dormiu.respawnData().pos() : casa.getRespawnData().pos();

        int origem = onde.getY();
        int mexe = 0;
        BlockPos achado = onde;
        while (!safe(casa, achado) && achado.getY() > 1 && achado.getY() < PROCURA_ATÉ) {
            achado = new BlockPos(onde.getX(), origem + mexe, onde.getZ());
            if (origem - mexe > 1) mexe = -mexe;
            if (mexe >= 0) mexe++;
        }

        pó(quem);
        quem.teleportTo(casa, achado.getX() + 0.5, achado.getY() + 1, achado.getZ() + 0.5, Set.of(),
                quem.getYRot(), quem.getXRot(), false);
        quem.clearFire();
        pó(quem);
        return true;
    }

    /** Chão firme com duas casas de ar em cima: o {@code isSafeBlock}. */
    private static boolean safe(ServerLevel level, BlockPos onde) {
        if (!level.getBlockState(onde).isSolid()) return false;
        for (int h = 1; h <= AR_EM_CIMA; h++) {
            if (level.getBlockState(onde.above(h)).isSolid()) return false;
        }
        return true;
    }

    /** O chiado de ir e de chegar: o {@code ParticleEffect.PORTAL} com o som do Alhures. */
    private static void pó(ServerPlayer quem) {
        if (!(quem.level() instanceof ServerLevel level)) return;
        level.sendParticles(ParticleTypes.PORTAL, quem.getX(), quem.getY() + 1.0, quem.getZ(),
                32, 0.5, 1.0, 0.5, 0.0);
        level.playSound(null, quem.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
