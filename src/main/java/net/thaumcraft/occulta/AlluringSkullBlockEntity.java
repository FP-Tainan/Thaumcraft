package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

/**
 * A alma da Caveira do Chamado: a {@code TileEntityAlluringSkull} do Witchery.
 *
 * <p>Ela guarda <b>uma coisa só</b>: em que <b>oitavo do mundo</b> vai o chamado. De cinco em cinco segundos
 * o número sobe, dá a volta no oito, e o quadrante correspondente é acordado.
 *
 * <p>Os oito quadrantes do original, e vale copiá-los à letra porque a ordem é esquisita: os quatro
 * primeiros são os de <b>baixo</b> — dez blocos abaixo dela, sessenta e quatro para cada lado — e os quatro
 * últimos são os de <b>cima</b>. E os de cima não estão na mesma ordem dos de baixo: o quinto é o canto
 * noroeste, o sexto o sudeste, o sétimo o sudoeste e o oitavo o nordeste. É assim no original, e aqui também.
 */
public class AlluringSkullBlockEntity extends BlockEntity {
    private int quadrante;
    private long batidas;

    public AlluringSkullBlockEntity(BlockPos onde, BlockState feitio) {
        super(OccultaBlocks.ALLURING_SKULL_ENTITY, onde, feitio);
    }

    /** Uma batida. De cem em cem, e só acordada, ela chama um quadrante. */
    public void bate(ServerLevel level) {
        this.batidas++;
        if (!this.getBlockState().getValue(AlluringSkullBlock.ACORDADA)) return;
        if (this.batidas % AlluringSkullBlock.VOLTA != 0L) return;

        if (++this.quadrante >= AlluringSkullBlock.QUADRANTES) this.quadrante = 0;
        chama(level, this.worldPosition, this.quadrante);
    }

    /** Em que oitavo vai o chamado. */
    public int quadrante() {
        return this.quadrante;
    }

    /**
     * <b>Acorda um oitavo do mundo.</b>
     *
     * <p>Tudo o que ali for morto-vivo e souber andar passa a andar para a caveira. Devolve <b>quantos</b>
     * ouviu o chamado naquele oitavo. Quem já estiver indo
     * para lá não é incomodado — o original só manda caminho novo a quem não conseguiu ir sozinho.
     */
    public static int chama(ServerLevel level, BlockPos onde, int quadrante) {
        double x = onde.getX() + 0.5;
        double y = onde.getY() + 0.5;
        double z = onde.getZ() + 0.5;
        double r = AlluringSkullBlock.ALCANCE;
        double f = AlluringSkullBlock.FUNDO;

        AABB caixa = switch (quadrante) {
            case 0 -> new AABB(x, y - f, z - r, x + r, y, z);
            case 1 -> new AABB(x - r, y - f, z - r, x, y, z);
            case 2 -> new AABB(x, y - f, z, x + r, y, z + r);
            case 3 -> new AABB(x - r, y - f, z, x, y, z + r);
            case 4 -> new AABB(x - r, y + 1.0, z - r, x, y + f, z);
            case 5 -> new AABB(x, y + 1.0, z, x + r, y + f, z + r);
            case 6 -> new AABB(x - r, y + 1.0, z, x, y + f, z + r);
            default -> new AABB(x, y + 1.0, z - r, x + r, y + f, z);
        };

        int quantos = 0;
        for (PathfinderMob bicho : level.getEntitiesOfClass(PathfinderMob.class, caixa)) {
            if (!éMorto(bicho)) continue;
            quantos++;
            if (bicho.getNavigation().moveTo(x, y, z, 1.0)) continue;
            var caminho = bicho.getNavigation().createPath(onde, 1);
            if (caminho != null) bicho.getNavigation().moveTo(caminho, 1.0);
        }
        return quantos;
    }

    /**
     * Se o bicho é morto-vivo.
     *
     * <p>O original pergunta pelo <b>atributo de criatura</b> dele, que era um dos cinco que a versão de
     * 2014 tinha. Hoje a mesma pergunta é feita pelo <b>rótulo {@code #minecraft:undead}</b>, que é o que o
     * encanto do Golpe Sagrado também usa — e que quem jogar pode mexer.
     */
    private static boolean éMorto(PathfinderMob bicho) {
        return bicho.getType().builtInRegistryHolder().is(net.minecraft.tags.EntityTypeTags.UNDEAD);
    }

    @Override
    protected void saveAdditional(ValueOutput dados) {
        super.saveAdditional(dados);
        dados.putInt("Quad", this.quadrante);
        dados.putLong("WITCLifeTicks", this.batidas);
    }

    @Override
    protected void loadAdditional(ValueInput dados) {
        super.loadAdditional(dados);
        this.quadrante = dados.getIntOr("Quad", 0);
        this.batidas = dados.getLongOr("WITCLifeTicks", 0L);
    }
}
