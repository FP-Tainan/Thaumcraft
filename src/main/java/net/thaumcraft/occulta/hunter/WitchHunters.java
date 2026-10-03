package net.thaumcraft.occulta.hunter;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.OccultaEntities;
import org.jetbrains.annotations.Nullable;

/**
 * <b>Quem faz magia negra é notado</b>: o {@code blackMagicPerformed} e o {@code handleWitchHunterEffects} do
 * {@code EntityWitchHunter}.
 *
 * <p>É a melhor ideia do Witchery inteiro, e funciona assim:
 *
 * <ol>
 *   <li>Alguém espeta uma boneca de vodu, amaldiçoa um vizinho, faz nascer um morto. <b>Uma vez em dez</b>,
 *       isso é <b>notado</b> — e o relógio começa a correr.</li>
 *   <li><b>Dois minutos depois</b>, e a partir daí, há <b>uma chance em cem</b> por volta de o mundo mandar
 *       alguém. Pode demorar.</li>
 *   <li>E então <b>dois caçadores</b> aparecem a três ou oito blocos de distância, já sabendo o nome de quem
 *       vieram buscar — e quem os chamou ouve <b>um som que não ouviu antes</b>.</li>
 * </ol>
 *
 * <p>O que torna isso bom não é o perigo: é a <b>demora</b>. Entre o feitiço e a batida à porta passam
 * minutos, e às vezes nada acontece. Quem joga não liga uma coisa à outra na primeira vez — liga na terceira,
 * e aí já é tarde para desaprender a magia.
 *
 * <p><b>Fica de fora, declarado:</b> o original manda caçadores por uma segunda razão — um <b>vampiro de grau
 * dez</b> malvisto numa aldeia. Isso pede a vampirice de jogador, que este porte ainda não tem; quando vier,
 * é aqui que se pergunta, ao lado do relógio.
 */
public final class WitchHunters {
    /** Uma vez em dez, a magia negra é notada: o {@code HUNTER_NOTICE_CHANCE}. */
    public static final double CHANCE_DE_SER_NOTADO = 0.1;

    /** E dois minutos depois é que o mundo pode mandar alguém: o {@code HUNTER_DELAY}. */
    public static final int DEMORA = 2400;

    /** Com uma chance em cem por volta: o {@code HUNTER_TRIGGER_CHANCE}. */
    public static final double CHANCE_DE_VIREM = 0.01;

    /** Quantos vêm, e quantas vezes o mundo tenta achar onde os pôr. */
    public static final int QUANTOS = 2;
    public static final int TENTATIVAS = 3;

    /** E a que distância de quem os chamou: o três e o oito do original. */
    public static final int PERTO = 3;
    public static final int LONGE = 8;

    /** De quanto em quanto se olha o relógio. */
    public static final int DE_QUANTO_EM_QUANTO = 100;

    /**
     * Quando a magia negra desta pessoa foi notada, em batidas do mundo — ou menos um, se não foi.
     *
     * <p>É o {@code WITCHunterTrigger} do original, e ele <b>não se apaga na morte</b>: quem fez o que fez,
     * fez.
     */
    public static final AttachmentType<Long> RELÓGIO = AttachmentRegistry.<Long>builder()
            .initializer(() -> -1L)
            .persistent(com.mojang.serialization.Codec.LONG)
            .copyOnDeath()
            .buildAndRegister(Thaumcraft.id("witch_hunter_trigger"));

    private WitchHunters() {
    }

    /** Põe o relógio a correr e, com ele, a classe a carregar. */
    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % DE_QUANTO_EM_QUANTO != 7) return;
            for (ServerLevel level : server.getAllLevels()) {
                for (Player quem : level.players()) olha(level, quem);
            }
        });
    }

    /**
     * <b>Alguém fez magia negra.</b> Uma vez em dez, isso é notado.
     *
     * <p>Chamar isto duas vezes não adianta nada a quem já foi notado: o relógio só se põe a correr se ainda
     * não estiver.
     */
    public static void magiaNegra(@Nullable Player quem) {
        if (quem == null || !(quem.level() instanceof ServerLevel level)) return;
        if (quem.getAttachedOrCreate(RELÓGIO) > 0L) return;
        if (level.getRandom().nextDouble() >= CHANCE_DE_SER_NOTADO) return;
        // nunca zero: zero é o que o relógio parado diz, e um mundo acabado de nascer tem o tempo em zero
        quem.setAttached(RELÓGIO, Math.max(1L, level.getGameTime()));
    }

    /** Se o relógio desta pessoa está a correr. */
    public static boolean notado(Player quem) {
        return quem.getAttachedOrCreate(RELÓGIO) > 0L;
    }

    /** E o apaga, que é o que acontece quando eles finalmente vêm. */
    public static void esquece(Player quem) {
        quem.removeAttached(RELÓGIO);
    }

    /** A volta do relógio: passada a demora, há uma chance em cem de virem. */
    private static void olha(ServerLevel level, Player quem) {
        long quando = quem.getAttachedOrCreate(RELÓGIO);
        if (quando <= 0L) return;
        if (level.getGameTime() < quando + DEMORA) return;
        if (level.getRandom().nextDouble() >= CHANCE_DE_VIREM) return;
        manda(level, quem);
    }

    /**
     * <b>Manda-os.</b> Devolve quantos vieram.
     *
     * <p>Fica à parte do relógio de propósito: é por aqui que uma prova, um rito ou um comando os pode chamar
     * sem esperar duas horas de jogo.
     */
    public static int manda(ServerLevel level, Player quem) {
        esquece(quem);
        int vieram = 0;
        for (int tentativa = 0; tentativa < TENTATIVAS && vieram < QUANTOS; tentativa++) {
            if (nasce(level, quem)) vieram++;
        }
        if (vieram > 0) {
            level.playSound(null, quem.blockPosition(), SoundEvents.RAID_HORN.value(), SoundSource.HOSTILE,
                    1.0f, 1.0f);
        }
        return vieram;
    }

    /** Um caçador, num lugar de pé entre três e oito blocos de quem o chamou. */
    private static boolean nasce(ServerLevel level, Player quem) {
        BlockPos onde = ondeCabe(level, quem.blockPosition());
        if (onde == null) return false;

        var caçador = OccultaEntities.WITCH_HUNTER.create(level, EntitySpawnReason.EVENT);
        if (caçador == null) return false;
        caçador.snapTo(onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5,
                level.getRandom().nextFloat() * 360.0f, 0.0f);
        caçador.finalizeSpawn(level, level.getCurrentDifficultyAt(onde), EntitySpawnReason.EVENT, null);
        caçador.setPersistenceRequired();
        caçador.vemBuscar(quem);
        caçador.setTarget(quem);
        level.addFreshEntity(caçador);

        level.sendParticles(ParticleTypes.SMOKE, onde.getX() + 0.5, onde.getY() + 1.0, onde.getZ() + 0.5,
                24, 0.4, 0.6, 0.4, 0.0);
        return true;
    }

    /** O {@code Infusion.spawnCreature}: um lugar de pé num anel entre o perto e o longe. */
    @Nullable
    private static BlockPos ondeCabe(ServerLevel level, BlockPos meio) {
        for (int volta = 0; volta < 16; volta++) {
            int dx = level.getRandom().nextInt(LONGE * 2 + 1) - LONGE;
            int dz = level.getRandom().nextInt(LONGE * 2 + 1) - LONGE;
            if (Math.abs(dx) < PERTO && Math.abs(dz) < PERTO) continue;

            for (int dy = 2; dy >= -2; dy--) {
                BlockPos tenta = meio.offset(dx, dy, dz);
                if (!level.getBlockState(tenta.below()).isSolidRender()) continue;
                if (!level.isEmptyBlock(tenta) || !level.isEmptyBlock(tenta.above())) continue;
                return tenta;
            }
        }
        return null;
    }
}
