package net.thaumcraft.world.outer;

/**
 * Uma casa do labirinto das Terras de Fora: o {@code Cell} da 4.2.3.5. Cada casa é um chunk; guarda por onde se sai
 * (norte, sul, leste, oeste, e os de cima e de baixo que o original nunca usou) e o que há nela ({@code feature}): 1 o
 * portal, 2 a 5 as quatro partes da sala do chefe, 6 a sala da chave, 7 o ninho, 8 a biblioteca, 9 e 10 corredores
 * comuns, 11 com pedras rúnicas, 12 incrustado, 13 maculado e 14 o das aranhas da mente. Numa palavra só de dezesseis
 * bits: as saídas nos seis de baixo, a casa nos oito de cima.
 */
public final class Cell {
    public boolean north, south, east, west, above, below;
    public byte feature;

    public Cell() {
    }

    public Cell(short data) {
        this.north = (data & 1) != 0;
        this.south = (data & 2) != 0;
        this.east = (data & 4) != 0;
        this.west = (data & 8) != 0;
        this.above = (data & 16) != 0;
        this.below = (data & 32) != 0;
        this.feature = (byte) (data >> 8);
    }

    public short pack() {
        int out = 0;
        if (this.north) out |= 1;
        if (this.south) out |= 2;
        if (this.east) out |= 4;
        if (this.west) out |= 8;
        if (this.above) out |= 16;
        if (this.below) out |= 32;
        out |= this.feature << 8;
        return (short) out;
    }
}
