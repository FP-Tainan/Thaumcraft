package net.thaumcraft.occulta.divine;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.OccultaEntities;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * O <b>gerente das profecias</b>: o {@code PredictionManager} do Witchery.
 *
 * <p>Guarda as dezessete, sorteia uma quando alguém lê a sorte de alguém, e depois <b>persegue</b> o jogador
 * com ela até ela se cumprir.
 *
 * <h2>As regras</h2>
 *
 * <ul>
 *   <li><b>Uma de cada vez.</b> Quem já tem uma por cumprir não ganha outra — bater na bola outra vez só
 *       <b>repete o recado</b> da que ele já tem. É de propósito: a profecia é para se viver, não para se
 *       colecionar;</li>
 *   <li><b>só um vidente a lê.</b> Quem não passou pelo rito que faz aparecer a Bola de Cristal não sabe ler
 *       nada, e a bola lhe diz isso. No <b>criativo</b> qualquer um lê;</li>
 *   <li>e ela <b>não se diz a si próprio por acidente</b>: a bola procura quem estiver perto, e só se não
 *       houver ninguém é que lê a sorte de quem bateu nela.</li>
 * </ul>
 *
 * <h2>Onde ela mora</h2>
 *
 * <p>No original, na etiqueta do jogador: uma <b>lista</b> de {@code {id, tempo}} que nunca tem mais do que
 * um, e um {@code boolean} de vidente. Aqui são dois <b>apegos</b> — um {@link Prophecy} e uma marca — que
 * atravessam a morte, como no original, porque a profecia dita a um homem não morre com ele.
 */
public final class Predictions {
    /** Quanto tempo uma profecia tem antes de ficar atrasada: oito minutos. */
    public static final long PRAZO = 9600L;

    /** E quando ela fica muito velha: meia hora. */
    public static final long MUITO_VELHA = 36000L;

    /** Quão longe de quem foi avisado nasce o que ela faz nascer. */
    public static final int LONGE = 4;

    /** E o salto que faz o anel ter um buraco no meio. */
    public static final int SALTO = 4;

    /** Quanto o altar paga por cada leitura. */
    public static final float CUSTO = 500.0f;

    /** E a que distância a bola acha um altar. */
    public static final int ALCANCE = 16;

    /** <b>Quem sabe ler a sorte</b>: o {@code WITCFTeller} do original. */
    public static final AttachmentType<Unit> VIDENTE = AttachmentRegistry.<Unit>builder()
            .initializer(() -> null)
            .persistent(Unit.CODEC)
            .copyOnDeath()
            .buildAndRegister(Thaumcraft.id("fortune_teller"));

    /** <b>E o que lhe foi dito</b>: o {@code WITCPreList} do original, com um elemento só. */
    public static final AttachmentType<Prophecy> PROFECIA = AttachmentRegistry.<Prophecy>builder()
            .persistent(Prophecy.CODEC)
            .copyOnDeath()
            .buildAndRegister(Thaumcraft.id("prophecy"));

    /** <b>Se ele já esteve no Nether</b>: o {@code WITCVisitedNether} do original. */
    public static final AttachmentType<Unit> VIU_O_NETHER = AttachmentRegistry.<Unit>builder()
            .initializer(() -> null)
            .persistent(Unit.CODEC)
            .copyOnDeath()
            .buildAndRegister(Thaumcraft.id("visited_nether"));

    private static final List<Prediction> TODAS = new ArrayList<>();

    static {
        // os números, os pesos e as chaves são os do original, um a um
        põe(new FightPrediction(1, 13, 0.05, "zombie", EntityTypes.ZOMBIE, false));
        põe(new ArrowPrediction(2, 13, 0.05, "arrowhit"));
        põe(new FightPrediction(3, 3, 0.05, "ent", OccultaEntities.ENT, false));
        põe(new FallPrediction(4, 13, 0.05, "fall"));
        põe(new MultiMinePrediction(5, 8, 0.05, "iron", 1212, 0.01,
                Blocks.IRON_ORE, Blocks.IRON_ORE.asItem(), 8, 20));
        põe(new MultiMinePrediction(6, 3, 0.05, "diamond", 1208, 0.01,
                Blocks.STONE, Items.DIAMOND, 1, 1));
        põe(new MultiMinePrediction(7, 3, 0.05, "emerald", 1208, 0.01,
                Blocks.STONE, Items.EMERALD, 1, 1));
        põe(new BuriedTreasurePrediction(8, 2, 0.05, "treasure", 1210, 0.01,
                BuiltInLootTables.ABANDONED_MINESHAFT));
        põe(new FallInLovePrediction(9, 2, 0.05, "love", 1210, 0.01));
        põe(new FightPrediction(10, 2, 0.05, "bababad", OccultaEntities.BABA_YAGA, false));
        põe(new FightPrediction(11, 2, 0.05, "babagood", OccultaEntities.BABA_YAGA, true));
        põe(new FightPrediction(12, 3, 0.05, "friend", EntityTypes.WOLF, true));
        põe(new RescuePrediction(13, 13, 0.05, "rescued", 1208, 0.01, OccultaEntities.OWL));
        põe(new RescuePrediction(14, 13, 0.05, "rescued", 1208, 0.01, EntityTypes.WOLF));
        põe(new WetPrediction(15, 13, 0.05, "wet"));
        põe(new NetherTripPrediction(16, 3, 0.05, "tothenether"));
        põe(new MultiMinePrediction(17, 13, 0.05, "coal", 1208, 0.01,
                Blocks.COAL_ORE, Items.COAL, 10, 20));
    }

    private Predictions() {
    }

    private static void põe(Prediction qual) {
        TODAS.add(qual);
    }

    /** Sem uso fora do porte: obriga a classe a carregar, e com ela os apegos e a lista. */
    public static void init() {
    }

    /** A profecia daquele número, ou nada. */
    public static @Nullable Prediction daquele(int id) {
        for (Prediction cada : TODAS) {
            if (cada.id == id) return cada;
        }
        return null;
    }

    /** Quantas há. */
    public static int quantas() {
        return TODAS.size();
    }

    // ------------------------------------------------------------------ ler a sorte

    /** Se ele sabe ler a sorte. */
    public static boolean vidente(ServerPlayer quem) {
        return quem.hasAttached(VIDENTE);
    }

    /** E o rito que faz aparecer a bola lhe ensina. */
    public static void ensina(ServerPlayer quem) {
        quem.setAttached(VIDENTE, Unit.INSTANCE);
    }

    /** Se ele já esteve no Nether. */
    public static boolean jáFoiAoNether(ServerPlayer quem) {
        return quem.hasAttached(VIU_O_NETHER);
    }

    /**
     * <b>Lê a sorte.</b>
     *
     * <p>Se ele já tem uma por cumprir, <b>repete-a</b>. Se não, sorteia uma das possíveis com o peso de cada
     * uma. Se nenhuma for possível, diz que não vê nada.
     */
    public static void lê(ServerLevel level, ServerPlayer alvo, ServerPlayer vidente, boolean recado) {
        if (!alvo.getAbilities().instabuild && !vidente(vidente)) {
            alvo.sendSystemMessage(Component.translatable("tc.occulta.prediction.unskilled")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        Prophecy tem = alvo.getAttached(PROFECIA);
        if (tem != null) {
            Prediction qual = daquele(tem.id());
            if (qual != null) {
                if (recado) diz(alvo, qual);
                return;
            }
            alvo.removeAttached(PROFECIA);
        }

        List<Prediction> podem = new ArrayList<>();
        int peso = 0;
        for (Prediction cada : TODAS) {
            if (!cada.possível(level, alvo)) continue;
            podem.add(cada);
            peso += cada.peso;
        }
        if (podem.isEmpty() || peso <= 0) {
            if (recado) {
                alvo.sendSystemMessage(Component.translatable("tc.occulta.prediction.none",
                        alvo.getName()).withStyle(ChatFormatting.DARK_PURPLE));
            }
            return;
        }

        int sorte = level.getRandom().nextInt(peso);
        for (Prediction cada : podem) {
            sorte -= cada.peso;
            if (sorte < 0) {
                alvo.setAttached(PROFECIA, new Prophecy(cada.id, level.getGameTime()));
                if (recado) diz(alvo, cada);
                return;
            }
        }
    }

    private static void diz(ServerPlayer quem, Prediction qual) {
        quem.sendSystemMessage(Component.translatable("tc.occulta.prediction." + qual.recado,
                quem.getName()).withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    // ------------------------------------------------------------------ os ganchos

    /** O gancho do dano levado. */
    public static void levouDano(ServerLevel level, ServerPlayer quem, DamageSource fonte) {
        corre(level, quem, (qual, atrasada, velha) ->
                qual.cumprida(level, quem, fonte, atrasada, velha));
    }

    /**
     * O gancho da batida, que é também onde ela se força.
     *
     * <p>É a única porta por onde a profecia <b>se faz sozinha</b>: passado o prazo, a cada batida há uma
     * chance de o mod abrir o chão, mandar um bicho ou um recado.
     */
    public static void batida(ServerLevel level, ServerPlayer quem) {
        if (quem.level().dimension() == Level.NETHER) quem.setAttached(VIU_O_NETHER, Unit.INSTANCE);
        corre(level, quem, (qual, atrasada, velha) -> {
            if (qual.cumprida(level, quem, atrasada, velha)) return true;
            return atrasada && qual.tentaForçar(level) && qual.força(level, quem);
        });
    }

    /** O gancho do bloco partido, que pode pôr mais coisas no chão. */
    public static void partiu(ServerLevel level, ServerPlayer quem, BlockState oquê, BlockPos onde) {
        List<ItemStack> cai = new ArrayList<>();
        corre(level, quem, (qual, atrasada, velha) ->
                qual.cumprida(level, quem, oquê, onde, cai, atrasada, velha));
        for (ItemStack cada : cai) {
            if (cada.isEmpty()) continue;
            level.addFreshEntity(new ItemEntity(level,
                    onde.getX() + 0.5, onde.getY() + 0.5, onde.getZ() + 0.5, cada));
        }
    }

    /** O que todos os ganchos fazem igual: achar a profecia dele, correr a pergunta e largá-la se cumprir. */
    private static void corre(ServerLevel level, ServerPlayer quem, Pergunta oquê) {
        Prophecy tem = quem.getAttached(PROFECIA);
        if (tem == null) return;
        Prediction qual = daquele(tem.id());
        if (qual == null) {
            quem.removeAttached(PROFECIA);
            return;
        }
        long agora = level.getGameTime();
        boolean atrasada = agora - tem.quando() > qual.prazo();
        boolean velha = agora - tem.quando() > MUITO_VELHA;
        if (oquê.corre(qual, atrasada, velha)) quem.removeAttached(PROFECIA);
    }

    @FunctionalInterface
    private interface Pergunta {
        boolean corre(Prediction qual, boolean atrasada, boolean velha);
    }

    // ------------------------------------------------------------------ o que elas partilham

    /**
     * <b>Um lugar perto dele, mas não colado.</b>
     *
     * <p>A conta é a do original e é esquisita de propósito: sorteia-se um número de zero a quatro e, se ele
     * passar de dois, dá-se-lhe um salto. O que sai é um anel com um <b>buraco no meio</b> — o bicho nunca
     * nasce em cima de quem foi avisado.
     *
     * <p>Depois disso procura-se o chão: sobe-se até sair de dentro do que estiver cheio, desce-se até bater
     * em alguma coisa, e mede-se quanto céu há por cima. Se não couber o bicho, não nasce nada.
     */
    public static @Nullable BlockPos lugarPerto(ServerLevel level, ServerPlayer quem, int longe, int salto,
                                                float altura) {
        RandomSource sorte = level.getRandom();
        BlockPos dele = quem.blockPosition();
        int nx = anel(sorte, dele.getX(), longe, salto);
        int nz = anel(sorte, dele.getZ(), longe, salto);
        int ny = dele.getY();

        while (!level.getBlockState(new BlockPos(nx, ny, nz)).isAir() && ny < dele.getY() + 8) ny++;
        while (level.getBlockState(new BlockPos(nx, ny, nz)).isAir() && ny > level.getMinY()) ny--;

        int céu = 0;
        while (level.getBlockState(new BlockPos(nx, ny + céu + 1, nz)).isAir() && céu < 6) céu++;
        return céu >= altura ? new BlockPos(nx, ny + 1, nz) : null;
    }

    private static int anel(RandomSource sorte, int base, int longe, int salto) {
        int a = sorte.nextInt(5);
        if (a > 2) a += salto;
        return base - longe + a;
    }

    /** O pó que anuncia o que nasceu. */
    public static void fumo(ServerLevel level, Entity quem) {
        level.sendParticles(ParticleTypes.SMOKE, quem.getX(), quem.getY() + 1.0, quem.getZ(),
                16, 0.5, 2.0, 0.5, 0.0);
    }
}
