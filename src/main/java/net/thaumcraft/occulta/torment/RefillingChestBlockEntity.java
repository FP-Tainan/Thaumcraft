package net.thaumcraft.occulta.torment;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaBlocks;

import java.util.ArrayList;
import java.util.List;

/**
 * O que um <b>Baú de Reabastecimento</b> faz: a {@code TileEntityRefillingChest} do Witchery.
 *
 * <p>Ele é um baú comum em tudo menos numa coisa: <b>de hora em hora</b>, se estiver <b>vazio</b> e se
 * estiver <b>no Tormento</b>, enche-se outra vez com duas a cinco coisas de calabouço.
 *
 * <p>São três por andar do labirinto — um em cada sala de fundo e um na sala do meio —, e eles são a razão
 * pela qual o Tormento não é só castigo. Quem for mandado para lá e souber atravessar o labirinto sai com
 * o que havia nos três; e quem for mandado outra vez, uma hora depois, os acha cheios de novo.
 *
 * <p>Os três requisitos são do original e os três importam: <b>vazio</b>, porque senão ele transbordaria;
 * <b>no Tormento</b>, porque um baú destes no mundo de cima seria dinheiro infinito; e <b>de hora em
 * hora</b>, porque é o tempo de uma visita.
 *
 * <p><b>Declarado:</b> o original tira as coisas do {@code ChestGenHooks.getInfo("dungeonChest")}, que era
 * a tabela do Forge; aqui elas saem da tabela de despojo do <b>calabouço simples</b> do jogo, que é a mesma
 * coisa com outro nome. A conta de quantas — duas mais até três — é a do original.
 */
public class RefillingChestBlockEntity extends ChestBlockEntity {
    /** De quanto em quanto ele se enche: a hora do {@code TimeUtil.secondsElapsed(3600, ticks)}. */
    public static final int DE_HORA_EM_HORA = 20 * 3600;

    /** E quantas coisas: duas mais até três. */
    public static final int DE = 2;
    public static final int ATÉ = 4;

    /** Quanto ele pode ter dentro para ainda contar como vazio: nada. */
    public static final int VAZIO = 0;

    private long batidas;

    public RefillingChestBlockEntity(BlockPos onde, BlockState feitio) {
        super(OccultaBlocks.REFILLING_CHEST_ENTITY, onde, feitio);
    }

    /**
     * O {@code doUpdate} do original, uma vez por batida.
     *
     * <p>O contador dele começa em zero e nunca volta a zero: é por isso que a primeira conta de hora cai
     * na primeira hora de vida do baú e não na primeira hora de quem entra.
     */
    public static void serverTick(net.minecraft.world.level.Level level, BlockPos onde, BlockState feitio,
                                  RefillingChestBlockEntity baú) {
        baú.batidas++;
        if (!(level instanceof ServerLevel server)) return;
        if (!Torment.is(server)) return;
        if (baú.batidas % DE_HORA_EM_HORA != 0) return;
        if (baú.quanto() > VAZIO) return;
        baú.enche(server, onde);
    }

    /** Quantas coisas ele tem dentro: o {@code InvUtil.getItemStackCount}. */
    public int quanto() {
        int conta = 0;
        for (int lugar = 0; lugar < this.getContainerSize(); lugar++) {
            if (!this.getItem(lugar).isEmpty()) conta++;
        }
        return conta;
    }

    /** Enche-o com duas a cinco coisas de calabouço, em lugares ao acaso. */
    public void enche(ServerLevel level, BlockPos onde) {
        int quantas = DE + level.getRandom().nextInt(ATÉ);
        var tabela = level.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.SIMPLE_DUNGEON);
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(onde))
                .create(LootContextParamSets.CHEST);

        List<ItemStack> saiu = new ArrayList<>();
        // o original tira do saco de calabouço tantas vezes quantas precisa; aqui a tabela é rolada até
        // dar o que ele pediu, e o resto fica de fora
        for (int volta = 0; volta < quantas && saiu.size() < quantas; volta++) {
            saiu.addAll(tabela.getRandomItems(params, level.getRandom().nextLong()));
        }

        List<Integer> livres = new ArrayList<>();
        for (int lugar = 0; lugar < this.getContainerSize(); lugar++) {
            if (this.getItem(lugar).isEmpty()) livres.add(lugar);
        }
        for (int k = 0; k < quantas && k < saiu.size() && !livres.isEmpty(); k++) {
            int qual = livres.remove(level.getRandom().nextInt(livres.size()));
            this.setItem(qual, saiu.get(k));
        }
        this.setChanged();
    }

    /** Sem uso fora do porte: serve à prova para adiantar o relógio dele. */
    public void adianta(long quantas) {
        this.batidas = quantas;
    }

    /** E para a prova saber em que conta ele está. */
    public long batidas() {
        return this.batidas;
    }

    @Override
    protected void saveAdditional(ValueOutput saída) {
        super.saveAdditional(saída);
        saída.putLong("WITCLifeTicks", this.batidas);
    }

    @Override
    protected void loadAdditional(ValueInput entrada) {
        super.loadAdditional(entrada);
        this.batidas = entrada.getLongOr("WITCLifeTicks", 0L);
    }
}
