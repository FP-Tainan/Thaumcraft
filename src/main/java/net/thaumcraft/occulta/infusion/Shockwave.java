package net.thaumcraft.occulta.infusion;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * A <b>onda de choque</b> da Infusão do Mundo: a {@code ShockwaveTask} do Witchery.
 *
 * <p>É o poder mais bonito do mod de se ver, e o mais caro: um <b>anel de chão que se levanta e volta a
 * cair</b>, abrindo-se a partir de quem o fez, um bloco de raio por batida. O que estiver na crista do anel
 * leva <b>oito de dano</b> e é <b>atirado para longe</b>.
 *
 * <p>Repare que ele não é um estouro: o chão não se parte, ele <b>sobe e desce</b>. Dois blocos de fundura
 * são levantados um nível na crista e postos de volta atrás dela, de modo que, passada a onda, o terreno
 * está como estava.
 *
 * <h2>Como o anel se desenha</h2>
 *
 * <p>Com o <b>algoritmo do círculo de Bresenham</b>, à letra: anda-se um oitavo do círculo e se espelha nas
 * outras sete partes. É o mesmo desenho que um jogo de 1985 usaria para uma circunferência, e é por isso
 * que a onda tem o aspecto quadrado que tem.
 */
public final class Shockwave {
    /** Quantos blocos de fundura a onda levanta. */
    public static final int FUNDURA = 2;

    /** O raio mínimo dela, e o dano na crista. */
    public static final int MÍNIMO = 2;
    public static final float DANO = 8.0f;

    private static final List<Shockwave> ANDANDO = new ArrayList<>();

    private final ServerLevel level;
    private final ServerPlayer quem;
    private final BlockPos meio;
    private final int teto;
    private int volta;

    private Shockwave(ServerLevel level, ServerPlayer quem, int teto) {
        this.level = level;
        this.quem = quem;
        this.meio = BlockPos.containing(quem.getX(), quem.getY() - 1.0, quem.getZ());
        this.teto = teto + MÍNIMO;
    }

    /** Põe uma onda andando. */
    public static void começa(ServerLevel level, ServerPlayer quem, int raio) {
        ANDANDO.add(new Shockwave(level, quem, raio));
    }

    /** Quantas estão andando agora. */
    public static int quantas() {
        return ANDANDO.size();
    }

    public static void init() {
        /*
         * As ondas em curso ficam numa lista <b>do servidor</b>, e não do mundo. Por isso a batida
         * larga, antes de mais nada, as que não são deste servidor: um mundo fechado com uma onda a meio
         * — coisa que acontece a cada mundo de prova que se cria e se deita fora — deixaria aqui uma onda
         * presa a um nível que já não existe.
         */
        ServerTickEvents.END_SERVER_TICK.register(servidor -> {
            if (ANDANDO.isEmpty()) return;
            ANDANDO.removeIf(onda -> onda.level.getServer() != servidor || onda.bate());
        });
    }

    /** Uma batida da onda. Devolve se ela acabou. */
    private boolean bate() {
        if (this.quem.isRemoved()) return true;
        this.volta++;
        int raio = this.volta + MÍNIMO;

        if (this.volta == 1) {
            anel(this.meio.getY(), raio, 1);
        } else {
            anel(this.meio.getY() + 2, raio, -1);
            anel(this.meio.getY() + 1, raio - 1, -1);
        }

        if (this.volta < this.teto) {
            anel(this.meio.getY(), raio + 1, 2);
        } else {
            anel(this.meio.getY() + 1, raio, -1);
        }

        AABB crista = new AABB(this.meio.getX() - raio, this.meio.getY() + 1, this.meio.getZ() - raio,
                this.meio.getX() + raio, this.meio.getY() + 3, this.meio.getZ() + raio);
        for (LivingEntity bicho : this.level.getEntitiesOfClass(LivingEntity.class, crista)) {
            double longe = Math.sqrt(bicho.distanceToSqr(this.meio.getX(), this.meio.getY(),
                    this.meio.getZ()));
            if (longe > raio + 1 || longe < raio) continue;
            bicho.hurtServer(this.level, this.level.damageSources().playerAttack(this.quem), DANO);
            net.thaumcraft.occulta.rite.Rites.PushCircle.empurra(bicho,
                    this.meio.getX(), this.meio.getY(), this.meio.getZ());
        }

        return this.volta >= this.teto;
    }

    /** O círculo de Bresenham: um oitavo andado e espelhado nos outros sete. */
    private void anel(int y, int raio, int rumo) {
        int x = raio;
        int z = 0;
        int erro = 1 - x;
        while (x >= z) {
            risca(this.meio.getX() + x, y, this.meio.getZ() + z, rumo);
            risca(this.meio.getX() + z, y, this.meio.getZ() + x, rumo);
            risca(this.meio.getX() - x, y, this.meio.getZ() + z, rumo);
            risca(this.meio.getX() - z, y, this.meio.getZ() + x, rumo);
            risca(this.meio.getX() - x, y, this.meio.getZ() - z, rumo);
            risca(this.meio.getX() - z, y, this.meio.getZ() - x, rumo);
            risca(this.meio.getX() + x, y, this.meio.getZ() - z, rumo);
            risca(this.meio.getX() + z, y, this.meio.getZ() - x, rumo);
            z++;
            if (erro < 0) {
                erro += 2 * z + 1;
            } else {
                x--;
                erro += 2 * (z - x + 1);
            }
        }
    }

    /**
     * Um ponto do anel: dois blocos de fundura mexidos um nível no rumo pedido.
     *
     * <p>Com rumo <b>para cima</b> ele desiste se o fundo estiver vazio ou se houver coisa por cima; com
     * rumo <b>para baixo</b>, se o próprio lugar estiver vazio. É o que impede a onda de cavar buracos e de
     * atravessar tetos.
     */
    private void risca(int x, int y, int z, int rumo) {
        BlockPos aqui = new BlockPos(x, y, z);
        if (rumo > 0) {
            if (this.level.getBlockState(aqui.below(FUNDURA - 1)).isAir()) return;
            if (this.level.getBlockState(aqui.above()).isSolid()) return;
            for (int volta = 0; volta < FUNDURA; volta++) mexe(aqui.below(volta), rumo);
            return;
        }
        if (this.level.getBlockState(aqui).isAir()) return;
        if (this.level.getBlockState(aqui.offset(0, rumo - 1, 0)).isSolid()) return;
        for (int volta = FUNDURA - 1; volta >= 0; volta--) mexe(aqui.below(volta), rumo);
    }

    private void mexe(BlockPos daqui, int rumo) {
        BlockState oquê = this.level.getBlockState(daqui);
        if (!net.thaumcraft.occulta.BlockProtect.podeMexer(oquê)) return;
        BlockPos pali = daqui.offset(0, rumo, 0);
        if (!net.thaumcraft.occulta.BlockProtect.podeMexer(this.level, pali)) return;
        this.level.setBlock(daqui, Blocks.AIR.defaultBlockState(),
                net.minecraft.world.level.block.Block.UPDATE_ALL);
        this.level.setBlock(pali, oquê, net.minecraft.world.level.block.Block.UPDATE_ALL);
    }
}
