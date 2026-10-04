package net.thaumcraft.occulta.curse;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.occulta.brew.Brew;
import net.thaumcraft.occulta.brew.BrewModifiers;

/**
 * O cozimento que ficou <b>preso na maçaneta</b>: a {@code TileEntityCursedBlock} do Witchery.
 *
 * <p>Ela guarda a receita inteira — a lista do que estava no caldeirão —, quem a atirou, e <b>quantas vezes</b>
 * ela ainda dispara. Um frasco de gatilho posto numa alavanca espera ali, calado, até alguém a puxar.
 *
 * <p>E <b>acumula</b>: acertar a mesma alavanca com o mesmo cozimento outra vez não a troca, aumenta a conta.
 * Uma porta pode estar armada para três pessoas seguidas.
 *
 * <p>Gasta a última carga, o bloco <b>volta a ser o que era</b> — e quem entrar depois não encontra nada.
 * É a diferença entre uma armadilha e uma praga: a armadilha acaba.
 */
public class CursedBlockEntity extends BlockEntity {
    private List<Item> dentro = List.of();
    private int cargas;
    private String quemAtirou = "";

    public CursedBlockEntity(BlockPos onde, BlockState feitio) {
        super(net.thaumcraft.occulta.OccultaBlocks.CURSED_BLOCK_ENTITY, onde, feitio);
    }

    /** Arma este bloco com um cozimento. */
    public void arma(List<Item> oquê, String quem) {
        this.dentro = List.copyOf(oquê);
        this.cargas = 1;
        this.quemAtirou = quem;
        this.setChanged();
    }

    /**
     * E o mesmo cozimento outra vez <b>soma</b>; um diferente <b>troca</b>.
     *
     * <p>É o {@code updateCurse}: o original compara a lista de ingredientes, de modo que dois frascos da
     * mesma receita se juntam e dois de receitas diferentes não.
     */
    public void soma(List<Item> oquê, String quem) {
        if (this.dentro.equals(oquê)) {
            this.cargas++;
        } else {
            this.dentro = List.copyOf(oquê);
            this.cargas = 1;
            this.quemAtirou = quem;
        }
        this.setChanged();
    }

    public int cargas() {
        return this.cargas;
    }

    public List<Item> dentro() {
        return this.dentro;
    }

    /**
     * <b>Dispara</b> em quem mexeu, e diz se ainda sobra alguma carga.
     *
     * <p>Devolvendo falso, quem chamou volta o bloco ao que ele era.
     */
    public boolean disparaEAcaba(ServerLevel level, Entity quem) {
        if (!this.dentro.isEmpty() && quem instanceof LivingEntity vivo) {
            BrewModifiers temperos = new BrewModifiers();
            temperos.quemFez = level.getServer().getPlayerList().getPlayerByName(this.quemAtirou);
            Brew.apply(level, vivo, this.dentro, temperos);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.WITCH,
                    vivo.getX(), vivo.getY() + 1.0, vivo.getZ(), 16, 0.4, 0.6, 0.4, 0.0);
            level.playSound(null, this.getBlockPos(),
                    net.minecraft.sounds.SoundEvents.GENERIC_DRINK.value(),
                    net.minecraft.sounds.SoundSource.BLOCKS, 0.5f, 1.4f);
        }
        return --this.cargas > 0;
    }

    @Override
    protected void saveAdditional(ValueOutput dados) {
        super.saveAdditional(dados);
        dados.putInt("Cargas", this.cargas);
        dados.putString("Quem", this.quemAtirou);
        dados.store("Dentro", BuiltInRegistries.ITEM.byNameCodec().listOf(), this.dentro);
    }

    @Override
    protected void loadAdditional(ValueInput dados) {
        super.loadAdditional(dados);
        this.cargas = dados.getIntOr("Cargas", 0);
        this.quemAtirou = dados.getStringOr("Quem", "");
        this.dentro = dados.read("Dentro", BuiltInRegistries.ITEM.byNameCodec().listOf())
                .map(List::copyOf).orElse(new ArrayList<>());
    }
}
