package net.thaumcraft.occulta.familiar;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * <b>Ir ter com o minério</b>: o {@code EntityAIFamiliarFindDiamonds} do Witchery.
 *
 * <p>É a meta mais estranha de todo o porte, e vale lê-la devagar. O
 * {@linkplain SpectralFamiliarEntity Familiar Espectral} <b>não cava</b>, não aponta e não desenha nada
 * no mundo: ele <b>anda até lá</b>. Achando o minério que lhe pediram num poço de quatro blocos à volta
 * dele e até a fundura daquele minério, ele caminha para cima do bloco, <b>senta-se</b>, e grunhe ao dono
 * as três coordenadas. Daí em diante fica ali sentado, até alguém o mandar levantar.
 *
 * <p>É por isso que ele é um porco. Um ponteiro mágico seria um item; <b>um bicho que vai sentar-se em
 * cima do diamante</b> é uma personagem.
 *
 * <h2>Os números do original</h2>
 *
 * <p>A meta só começa com <b>uma chance em dez</b> por olhada, e só com o dono a menos de dez blocos. A
 * busca varre um quadrado de oito por oito à volta dele, subindo do fundo do mundo até à fundura do
 * pedido — e pára no <b>primeiro</b> bloco que servir, que é por isso que ele acha o mais fundo e não o
 * mais perto.
 *
 * <p>Depois tem <b>dois relógios</b>: um teto de paciência entre vinte e sessenta segundos, e uma conta de
 * sessenta tentativas de caminho. Perdido o caminho sessenta vezes, ele desiste.
 *
 * <p><b>Desvio declarado:</b> em 2014 o mundo começava no zero e a busca subia de um até à fundura. Hoje
 * começa em menos sessenta e quatro, e a mesma busca sobe <b>do fundo do mundo</b> até à fundura acima
 * dele. A tabela de funduras do original lê-se agora como «as N camadas do fundo».
 */
public class FindOreGoal extends Goal {
    /** A chance de ele sequer olhar, e a que distância o dono tem de estar. */
    public static final double OLHA = 0.1;
    public static final double PERTO_DO_DONO = 100.0;

    /** O quadrado que ele varre à volta de si. */
    public static final int LARGURA = 4;

    /** E os dois relógios: o teto de paciência e as tentativas de caminho. */
    public static final int PACIÊNCIA = 1200;
    public static final int DESISTE_AOS = 60;

    /** A que distância do bloco ele se considera chegado. */
    public static final double CHEGOU = 2.0;

    private final SpectralFamiliarEntity porco;
    private final double pressa;
    private int tique;
    private int perdido;
    private int paciência;
    private @Nullable BlockPos achado;

    public FindOreGoal(SpectralFamiliarEntity porco, double pressa) {
        this.porco = porco;
        this.pressa = pressa;
        this.setFlags(EnumSet.of(Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        LivingEntity dono = this.porco.getOwner();
        if (!this.porco.isTame() || this.porco.isOrderedToSit()) return false;
        if (this.porco.oQueProcura() == null) return false;
        if (dono == null || this.porco.distanceToSqr(dono) >= PERTO_DO_DONO) return false;
        if (this.porco.getRandom().nextDouble() > OLHA) return false;
        return this.procura();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity dono = this.porco.getOwner();
        if (this.achado == null || this.tique > this.paciência || this.perdido > DESISTE_AOS) return false;
        if (dono == null || this.porco.distanceToSqr(dono) >= PERTO_DO_DONO) return false;
        return this.porco.éOQueProcura(this.porco.level().getBlockState(this.achado));
    }

    @Override
    public void start() {
        this.vaiLá();
        this.tique = 0;
        this.perdido = 0;
        var sorte = this.porco.getRandom();
        this.paciência = sorte.nextInt(sorte.nextInt(PACIÊNCIA) + PACIÊNCIA) + PACIÊNCIA;
        this.porco.setInSittingPose(false);
    }

    @Override
    public void stop() {
        this.porco.setOrderedToSit(false);
    }

    @Override
    public void tick() {
        if (this.achado == null) return;
        this.tique++;
        this.porco.setInSittingPose(false);

        double quão = this.porco.distanceToSqr(this.achado.getX() + 0.5, this.porco.getY(),
                this.achado.getZ() + 0.5);
        if (quão > CHEGOU) {
            this.porco.setOrderedToSit(false);
            if (!this.vaiLá()) this.perdido++;
            return;
        }
        if (!this.porco.isOrderedToSit()) {
            this.grunhe();
            this.porco.procura(-1);
            this.porco.setOrderedToSit(true);
            return;
        }
        this.perdido--;
    }

    /** O caminho até lá, e o de recurso à altura dele se o de cima não der. */
    private boolean vaiLá() {
        if (this.achado == null) return false;
        var caminho = this.porco.getNavigation();
        if (caminho.moveTo(this.achado.getX() + 0.5, this.achado.getY() + 1,
                this.achado.getZ() + 0.5, this.pressa)) {
            return true;
        }
        return caminho.moveTo(this.achado.getX() + 0.5, this.porco.getY(),
                this.achado.getZ() + 0.5, this.pressa);
    }

    /** «OINK! 142, 11, -308.» — e um estalo de bolinha de experiência. */
    private void grunhe() {
        if (!(this.porco.getOwner() instanceof Player dono)) return;
        BlockPos onde = this.porco.blockPosition();
        dono.sendSystemMessage(Component.translatable("tc.familiar.foundsomething",
                onde.getX(), onde.getY(), onde.getZ()).withStyle(ChatFormatting.LIGHT_PURPLE));
        dono.level().playSound(null, dono.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.NEUTRAL, 0.5f, 1.0f);
    }

    /**
     * A busca: do fundo do mundo para cima, e pára no primeiro que servir.
     *
     * <p>Guarda o lugar em {@link #achado}, e devolve se achou.
     */
    public boolean procura() {
        var level = this.porco.level();
        int fundo = level.getMinY() + 1;
        int teto = Math.min(fundo + this.porco.fundura(), level.getMaxY());
        int meioX = net.minecraft.util.Mth.floor(this.porco.getX());
        int meioZ = net.minecraft.util.Mth.floor(this.porco.getZ());

        for (int y = fundo; y < teto; y++) {
            for (int x = meioX - LARGURA; x < meioX + LARGURA; x++) {
                for (int z = meioZ - LARGURA; z < meioZ + LARGURA; z++) {
                    BlockPos onde = new BlockPos(x, y, z);
                    if (!this.porco.éOQueProcura(level.getBlockState(onde))) continue;
                    this.achado = onde;
                    return true;
                }
            }
        }
        this.achado = null;
        return false;
    }

    /** O que ela achou da última vez, para as provas. */
    public @Nullable BlockPos achado() {
        return this.achado;
    }
}
