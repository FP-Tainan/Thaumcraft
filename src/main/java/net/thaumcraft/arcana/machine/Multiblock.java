package net.thaumcraft.arcana.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Uma <b>figura feita de blocos</b>: o {@code MultiblockStructureDefinition} da 1.4.0.009.
 *
 * <p>Serve a duas coisas do Ars Magica 2, e as duas de maneiras diferentes. O <b>Obelisco</b> pergunta-lhe
 * <i>que partes estão montadas</i>, para saber quanto multiplicar; os <b>Rituais</b> perguntam-lhe <i>está
 * tudo montado?</i>, e depois mandam-na apagar o que desenhou.
 *
 * <p>A figura divide-se em <b>grupos</b>, e cada grupo tem um número de exclusão — o {@code mutex}. Um
 * grupo é um pedaço opcional que se pode montar ou não (os pilares do Obelisco), e grupos com o mesmo
 * número são <b>alternativas</b> entre si: basta um deles estar montado para aquele número contar como
 * satisfeito. A figura inteira está montada quando <b>todos</b> os números têm pelo menos um grupo
 * montado.
 *
 * <p><b>Os metadados saíram.</b> O original guarda bloco <i>e</i> metadado, porque em 2014 tijolo de
 * pedra e tijolo cinzelado eram o mesmo bloco com números diferentes. Hoje são dois blocos, e o que se
 * guarda é o bloco.
 */
public class Multiblock {
    /** O número de exclusão do grupo principal, que é sempre obrigatório. */
    public static final int MAIN_MUTEX = 1;

    private final String id;
    private final List<Group> groups = new ArrayList<>();
    private final Set<Integer> mutexes = new LinkedHashSet<>();
    private final Group main;

    public Multiblock(String id) {
        this.id = id;
        this.main = this.createGroup("main", MAIN_MUTEX);
    }

    public String id() {
        return this.id;
    }

    public Group main() {
        return this.main;
    }

    public Set<Integer> mutexes() {
        return this.mutexes;
    }

    public Group createGroup(String nome, int mutex) {
        this.mutexes.add(mutex);
        Group grupo = new Group(nome, mutex);
        this.groups.add(grupo);
        return grupo;
    }

    /** Um lugar da figura, no grupo principal. */
    public Multiblock add(int x, int y, int z, Block... quais) {
        return this.add(this.main, x, y, z, quais);
    }

    /** Um lugar da figura, num grupo. Vários blocos no mesmo lugar são alternativas. */
    public Multiblock add(Group grupo, int x, int y, int z, Block... quais) {
        grupo.allowed.computeIfAbsent(new BlockPos(x, y, z), onde -> new ArrayList<>()).addAll(List.of(quais));
        return this;
    }

    /** Os grupos daquele número de exclusão que estão montados aqui. */
    public List<Group> matchedGroups(int mutex, LevelAccessor level, BlockPos origem) {
        List<Group> feito = new ArrayList<>();
        for (Group grupo : this.groups) {
            if ((grupo.mutex & mutex) == grupo.mutex && grupo.matches(level, origem)) feito.add(grupo);
        }
        return feito;
    }

    /** Se a figura inteira está montada: cada número com pelo menos um grupo seu montado. */
    public boolean checkStructure(LevelAccessor level, BlockPos origem) {
        for (int mutex : this.mutexes) {
            boolean algum = false;
            for (Group grupo : this.groups) {
                if ((grupo.mutex & mutex) == grupo.mutex && grupo.matches(level, origem)) {
                    algum = true;
                    break;
                }
            }
            if (!algum) return false;
        }
        return true;
    }

    /** Apaga do mundo tudo o que os grupos daquele número desenham. É o que um rito faz ao pegar. */
    public void removeMutex(int mutex, Level level, BlockPos origem) {
        for (Group grupo : this.groups) {
            if (grupo.mutex == mutex) grupo.erase(level, origem);
        }
    }

    /** Apaga tudo o que a figura desenha, de todos os números. */
    public void consume(Level level, BlockPos origem) {
        for (int mutex : this.mutexes) this.removeMutex(mutex, level, origem);
    }

    /** Um pedaço da figura, que pode estar montado ou não. */
    public static final class Group {
        private final String nome;
        private final int mutex;
        private final Map<BlockPos, List<Block>> allowed = new LinkedHashMap<>();

        private Group(String nome, int mutex) {
            this.nome = nome;
            this.mutex = mutex;
        }

        public String nome() {
            return this.nome;
        }

        public Map<BlockPos, List<Block>> allowed() {
            return this.allowed;
        }

        boolean matches(LevelAccessor level, BlockPos origem) {
            for (var entrada : this.allowed.entrySet()) {
                Block tem = level.getBlockState(origem.offset(entrada.getKey())).getBlock();
                if (!entrada.getValue().contains(tem)) return false;
            }
            return true;
        }

        void erase(Level level, BlockPos origem) {
            for (BlockPos onde : this.allowed.keySet()) {
                level.setBlockAndUpdate(origem.offset(onde), Blocks.AIR.defaultBlockState());
            }
        }
    }
}
