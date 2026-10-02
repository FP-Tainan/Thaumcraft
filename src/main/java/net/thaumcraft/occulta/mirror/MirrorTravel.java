package net.thaumcraft.occulta.mirror;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.OccultaComponents;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.PowerSources;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Atravessar um espelho: o {@code onEntityWalking} da {@code BlockMirror} do Witchery, e as duas cantigas com que
 * se sai do Mundo do Espelho.
 *
 * <p>São quatro caminhos, tentados nesta ordem — a ordem do original:
 *
 * <ol>
 *   <li><b>De costas</b>: dois espelhos vazados frente a frente do outro lado da parede, até trinta e duas casas
 *       de distância, furam-na;</li>
 *   <li><b>de cela em cela</b>, dentro do Mundo do Espelho, com um espelho posto por quem joga — e se paga três
 *       mil de poder de altar;</li>
 *   <li><b>em prumo</b>: dois espelhos vazados um acima do outro, até dezesseis casas, furam o chão;</li>
 *   <li><b>de mundo</b>: o espelho habitado leva à cela dele no Mundo do Espelho, e o selado da cela traz de
 *       volta.</li>
 * </ol>
 *
 * <p>O que se guarda em quem joga é <b>a cela por onde entrou</b> e a hora das duas cantigas, que têm espera de
 * cinco minutos e de uma hora — os números do {@code ExtendedPlayer} do original.
 */
public final class MirrorTravel {
    /** A quanto da frente do espelho quem atravessa aparece: os sete décimos do original. */
    public static final double STEP_OUT = 0.7;

    /** Até onde se procura o espelho de costas. */
    public static final int PAIR_RANGE = 32;
    /** Até onde se procura o espelho em prumo. */
    public static final int STACK_RANGE = 16;
    /** E até onde se procura a cela vizinha. */
    public static final int CELL_RANGE = 10;

    /** A espera da cantiga que leva de volta à cela: cinco minutos. */
    public static final long HOME_COOLDOWN = 20L * 60L * 5L;
    /** E a da que leva para a cama: uma hora. */
    public static final long GIVE_UP_COOLDOWN = 20L * 60L * 60L;

    /**
     * As cantigas, tal como se dizem.
     *
     * <p><b>Decisão declarada:</b> no original a cantiga é o texto da língua do servidor, e quem joga tem de a
     * dizer nessa língua. Aqui valem as duas: a do original em inglês e a de cá em português, porque o servidor
     * não sabe em que língua está quem escreveu.
     */
    private static final List<String> SEND_ME_HOME = List.of(
            "espelho espelho meu me manda para casa", "espelho espelho meu me leva para casa",
            "mirror mirror send me home");
    private static final List<String> I_GIVE_UP = List.of(
            "espelho espelho meu eu desisto", "mirror mirror i give up");

    private MirrorTravel() {
    }

    // ------------------------------------------------------------------ o que se guarda em quem joga

    /**
     * A cela por onde alguém entrou no Mundo do Espelho, e a hora das cantigas.
     *
     * @param entry  a metade de cima do espelho selado da cela dele, se já tiver uma
     * @param home   quando disse a cantiga de voltar à cela
     * @param giveUp e quando disse a de desistir
     */
    public record Data(Optional<BlockPos> entry, long home, long giveUp) {
        public static final Data EMPTY = new Data(Optional.empty(), Long.MIN_VALUE, Long.MIN_VALUE);

        public static final Codec<Data> CODEC = RecordCodecBuilder.create(i -> i.group(
                BlockPos.CODEC.optionalFieldOf("entry").forGetter(Data::entry),
                Codec.LONG.optionalFieldOf("home", Long.MIN_VALUE).forGetter(Data::home),
                Codec.LONG.optionalFieldOf("give_up", Long.MIN_VALUE).forGetter(Data::giveUp))
                .apply(i, Data::new));
    }

    public static final AttachmentType<Data> DATA = AttachmentRegistry.<Data>builder()
            .initializer(() -> Data.EMPTY)
            .persistent(Data.CODEC)
            .copyOnDeath()
            .buildAndRegister(Thaumcraft.id("mirror_travel"));

    public static Data data(Player quem) {
        return quem.getAttachedOrCreate(DATA);
    }

    /**
     * Se aquele lugar é a cela por onde a pessoa entrou.
     *
     * <p>Quem <b>nunca</b> entrou responde sim a qualquer lugar, como no original — é o que deixa a primeira
     * travessia acontecer.
     */
    public static boolean isEntry(Player quem, BlockPos onde) {
        Optional<BlockPos> tem = data(quem).entry();
        return tem.isEmpty() || tem.get().equals(onde);
    }

    public static void setEntry(Player quem, BlockPos onde) {
        Data era = data(quem);
        quem.setAttached(DATA, new Data(Optional.of(onde), era.home(), era.giveUp()));
    }

    // ------------------------------------------------------------------ a travessia

    /** Quem atravessa: vivos e coisas largadas, menos a cara do próprio espelho. */
    public static boolean transportable(Entity quem) {
        if (quem instanceof MirrorFaceEntity) return false;
        return quem instanceof LivingEntity || quem instanceof ItemEntity;
    }

    /** Um passo de espelho para aquele bicho, se ele estiver no lugar certo olhando para o lado certo. */
    public static void step(ServerLevel level, BlockPos pos, BlockState state, Entity quem) {
        BlockPos alto = MirrorBlock.top(level, pos, state);
        if (alto == null) return;
        BlockState feitio = level.getBlockState(alto);
        if (!(feitio.getBlock() instanceof MirrorBlock espelhoBloco)) return;
        Direction olha = feitio.getValue(MirrorBlock.FACING);
        Direction viagem = olha.getOpposite();

        // vindo da metade de baixo, o bicho baixinho dispara uma casa mais abaixo: o hitZoneyShift do original
        boolean deBaixo = state.getValue(MirrorBlock.HALF) == DoubleBlockHalf.LOWER;
        int desce = deBaixo && quem.getBbHeight() <= 1.0f ? -1 : 0;
        AABB caixa = MirrorBlock.trigger(alto.offset(0, desce, 0), olha);
        if (!quem.getBoundingBox().intersects(caixa)) return;
        if (!(level.getBlockEntity(alto) instanceof MirrorBlockEntity espelho)) return;

        // quem é vivo tem de estar olhando para o vidro
        if (quem instanceof LivingEntity) {
            int para = Mth.floor(quem.getYRot() * 4.0f / 360.0f + 0.5) & 3;
            if (para != viagem.get2DDataValue()) return;
        }

        boolean noEspelho = MirrorWorld.is(level);
        if (!espelhoBloco.sealed()) {
            if ((noEspelho || espelho.hollow()) && throughTheWall(level, alto, feitio, viagem, quem)) return;
            if (noEspelho) {
                nextCell(level, alto, viagem, quem);
                return;
            }
            if (espelho.hollow() && upOrDown(level, alto, feitio, olha, quem)) return;
        }

        crossWorlds(level, alto, espelho, quem, noEspelho);
    }

    /** O primeiro caminho: o espelho de costas do outro lado da parede. */
    private static boolean throughTheWall(ServerLevel level, BlockPos alto, BlockState feitio, Direction viagem,
                                          Entity quem) {
        for (int i = 1; i < PAIR_RANGE; i++) {
            BlockPos alvo = alto.relative(viagem, i);
            BlockState lá = level.getBlockState(alvo);
            if (!lá.is(feitio.getBlock()) || lá.getValue(MirrorBlock.FACING) != viagem) continue;
            splash(level, quem);
            move(level, quem, out(alvo, viagem), alvo.getY() - 1 + 0.01, quem.getYRot());
            splash(level, quem);
            return true;
        }
        return false;
    }

    /** O segundo: a cela vizinha, dentro do Mundo do Espelho, paga a poder de altar. */
    private static void nextCell(ServerLevel level, BlockPos alto, Direction viagem, Entity quem) {
        for (int i = 1; i < CELL_RANGE; i++) {
            BlockPos alvo = alto.relative(viagem, i);
            if (!level.isEmptyBlock(alvo) || !level.isEmptyBlock(alvo.below())) continue;
            boolean minha = quem instanceof Player gente && isEntry(gente, alvo);
            boolean deAlguém = MirrorWorld.claimed(level, alvo);
            if ((!deAlguém || minha) && PowerSources.consume(level, alto, MirrorWorld.HOP_POWER)) {
                splash(level, quem);
                move(level, quem, out(alvo, viagem), alvo.getY() + 0.01, quem.getYRot());
                splash(level, quem);
            }
            return;
        }
    }

    /** O terceiro: o espelho em prumo, acima ou abaixo, que fura o chão. */
    private static boolean upOrDown(ServerLevel level, BlockPos alto, BlockState feitio, Direction olha,
                                    Entity quem) {
        for (int volta = 0; volta < 2; volta++) {
            for (int dy = 2; dy < STACK_RANGE; dy++) {
                BlockPos alvo = alto.offset(0, volta == 0 ? dy : -dy, 0);
                BlockState lá = level.getBlockState(alvo);
                if (!lá.is(feitio.getBlock()) || lá.getValue(MirrorBlock.FACING) != olha) continue;
                int y = lá.getValue(MirrorBlock.HALF) == DoubleBlockHalf.UPPER ? alvo.getY() - 1 : alvo.getY();
                splash(level, quem);
                // quem sai por um espelho em prumo sai de frente para fora dele: o giro de meia-volta do original
                move(level, quem, out(alvo, olha), y + 0.01, quem.getYRot() + 180.0f);
                splash(level, quem);
                return true;
            }
        }
        return false;
    }

    /**
     * O quarto: atravessar de mundo.
     *
     * <p>Do lado de cá, o espelho <b>habitado</b> abre a cela dele e acorda lá o Reflexo; do lado de lá, o selado
     * traz de volta a quem entrou por ele. Um espelho arrancado da parede deixa o de lá <b>desligado</b>, e então
     * ele leva a quem andar com o espelho na mochila — o que o original faz para ninguém ficar preso.
     */
    private static void crossWorlds(ServerLevel level, BlockPos alto, MirrorBlockEntity espelho, Entity quem,
                                    boolean noEspelho) {
        if (!(quem instanceof ServerPlayer jogador)) return;
        if (noEspelho && !isEntry(jogador, alto)) return;

        MirrorLink link = espelho.orClaim(level);
        if (link == null) return;
        ServerLevel destino = level.getServer().getLevel(link.level());
        if (destino == null) return;

        double x = link.pos().getX() + 0.5;
        double y = link.pos().getY() + 0.01;
        double z = link.pos().getZ() + 0.5;
        float face = jogador.getYRot();

        // o espelho do outro lado diz de que lado dele se sai — e se fecha por três segundos
        BlockState lá = destino.getBlockState(link.pos());
        if (lá.getBlock() instanceof MirrorBlock) {
            Direction mside = lá.getValue(MirrorBlock.FACING);
            face = mside.toYRot();
            x += mside.getStepX();
            z += mside.getStepZ();
            if (destino.getBlockEntity(link.pos()) instanceof MirrorBlockEntity outro) {
                if (outro.onCooldown()) return;
                outro.addCooldown(MirrorBlockEntity.COOLDOWN);
            }
        }

        double alvoY = y - 1.0;
        ServerLevel alvoMundo = destino;
        if (!noEspelho) {
            if (!espelho.hollow()) ReflectionEntity.wake(destino, link.pos());
            setEntry(jogador, link.pos());
        } else if (!espelho.connected()) {
            // o espelho de cá foi arrancado: vai-se a quem o carrega
            for (ServerLevel mundo : level.getServer().getAllLevels()) {
                for (ServerPlayer outro : mundo.players()) {
                    if (MirrorWorld.is(mundo) || !carries(outro, level, alto)) continue;
                    alvoMundo = mundo;
                    x = outro.getX();
                    alvoY = outro.getY();
                    z = outro.getZ();
                }
            }
        }

        splash(level, jogador);
        jogador.teleportTo(alvoMundo, x, alvoY, z, Set.of(), face, jogador.getXRot(), false);
        splash(alvoMundo, jogador);
    }

    /** Se aquela pessoa tem na mochila o espelho que aponta para esta alma: o {@code isTargettedBy}. */
    private static boolean carries(Player quem, ServerLevel deste, BlockPos alto) {
        for (int i = 0; i < quem.getInventory().getContainerSize(); i++) {
            ItemStack item = quem.getInventory().getItem(i);
            if (!item.is(OccultaItems.WITCH_MIRROR)) continue;
            MirrorLink.Held trazia = item.getOrDefault(OccultaComponents.MIRROR, MirrorLink.Held.EMPTY);
            if (trazia.link().filter(l -> l.level() == deste.dimension() && l.pos().equals(alto)).isPresent()) {
                return true;
            }
        }
        return false;
    }

    /** Onde se sai de um espelho: sete décimos à frente do vidro dele. */
    private static Vec3 out(BlockPos espelho, Direction lado) {
        return new Vec3(espelho.getX() + 0.5 + lado.getStepX() * STEP_OUT, 0.0,
                espelho.getZ() + 0.5 + lado.getStepZ() * STEP_OUT);
    }

    private static void move(ServerLevel level, Entity quem, Vec3 onde, double y, float face) {
        quem.teleportTo(level, onde.x, y, onde.z, Set.of(), face, quem.getXRot(), false);
    }

    /** O chapinhar de quem atravessa: o {@code ParticleEffect.SPLASH} com o som da água. */
    private static void splash(ServerLevel level, Entity quem) {
        level.sendParticles(ParticleTypes.SPLASH, quem.getX(), quem.getY() + 1.0, quem.getZ(),
                16, 0.5, 0.5, 0.5, 0.0);
        level.playSound(null, quem.blockPosition(), SoundEvents.PLAYER_SPLASH, SoundSource.BLOCKS, 0.5f, 1.0f);
    }

    // ------------------------------------------------------------------ as cantigas

    /**
     * O {@code trySayMirrorMirrorSendMeHome} do original: dentro do Mundo do Espelho, dizer a cantiga leva de
     * volta — à cela por onde se entrou, ou, desistindo de vez, à cama.
     *
     * @return se a cantiga pegou, e por isso a fala não vai para o resto da mesa
     */
    public static boolean chant(ServerPlayer quem, String falou) {
        if (!(quem.level() instanceof ServerLevel level) || !MirrorWorld.is(level)) return false;
        String limpo = falou.toLowerCase().replace("'", "").replace(",", "").trim();

        if (SEND_ME_HOME.stream().anyMatch(limpo::startsWith)) {
            Data agora = data(quem);
            long tempo = level.getGameTime();
            if (tempo < agora.home() + HOME_COOLDOWN) {
                cooldown(quem, agora.home() + HOME_COOLDOWN - tempo);
                return true;
            }
            BlockPos cela = agora.entry().orElse(null);
            if (cela == null) return true;
            quem.setAttached(DATA, new Data(agora.entry(), tempo, agora.giveUp()));
            splash(level, quem);
            quem.teleportTo(level, cela.getX() + 0.5, cela.getY() - 1 + 0.01, cela.getZ() + 0.5, Set.of(),
                    270.0f, quem.getXRot(), false);
            splash(level, quem);
            return true;
        }

        if (I_GIVE_UP.stream().anyMatch(limpo::startsWith)) {
            Data agora = data(quem);
            long tempo = level.getGameTime();
            if (tempo < agora.giveUp() + GIVE_UP_COOLDOWN) {
                cooldown(quem, agora.giveUp() + GIVE_UP_COOLDOWN - tempo);
                return true;
            }
            var volta = quem.getRespawnConfig();
            var cama = volta == null ? null : volta.respawnData();
            ServerLevel casa = cama == null ? null : level.getServer().getLevel(cama.dimension());
            BlockPos onde = cama == null ? null : cama.pos();
            if (casa == null || onde == null) {
                casa = level.getServer().overworld();
                onde = casa.getRespawnData().pos();
            }
            quem.setAttached(DATA, new Data(agora.entry(), agora.home(), tempo));
            splash(level, quem);
            quem.teleportTo(casa, onde.getX() + 0.5, onde.getY() + 1, onde.getZ() + 0.5, Set.of(),
                    quem.getYRot(), quem.getXRot(), false);
            splash(casa, quem);
            return true;
        }
        return false;
    }

    private static void cooldown(ServerPlayer quem, long tiques) {
        quem.sendSystemMessage(Component.translatable("tc.mirror.cooldown", tiques / 20L)
                .withStyle(ChatFormatting.RED));
    }

    /** Onde a cantiga se ouve. */
    public static void init() {
        net.fabricmc.fabric.api.message.v1.ServerMessageEvents.ALLOW_CHAT_MESSAGE.register(
                (mensagem, quem, tipo) -> !chant(quem, mensagem.signedContent()));
    }
}
