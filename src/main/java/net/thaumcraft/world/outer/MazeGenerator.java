package net.thaumcraft.world.outer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * O traçado do labirinto: o {@code MazeGenerator} da 4.2.3.5, na mesma ordem de sorteios. Numa grade de {@code width} por
 * {@code height} casas: a sala do chefe (dois por dois) num canto, o portal no meio, uns blocos mortos espalhados, e o
 * labirinto cavado a partir do portal (o "growing tree", ora a última casa, ora uma qualquer, ora a primeira). Depois a
 * sala do chefe é ligada a ele, um beco vira a sala da chave, metade dos outros becos vira ninho, biblioteca ou corredor
 * rúnico, e um em vinte e cinco corredores ganha um enfeite. Se não der certo, quem chama tenta com a semente seguinte.
 */
public final class MazeGenerator {
    public static final int N = 1, S = 2, E = 4, W = 8;

    private final int width, height;
    private final Random rand;
    public final int[][] grid;

    public MazeGenerator(int width, int height, long seed) {
        this.width = width;
        this.height = height;
        this.rand = new Random(seed);
        this.grid = new int[height][width];
    }

    static int opp(int in) {
        return switch (in) {
            case 1 -> 2;
            case 2 -> 1;
            case 4 -> 8;
            case 8 -> 4;
            default -> -99;
        };
    }

    static int dx(int in) {
        return switch (in) {
            case 1, 2 -> 0;
            case 4 -> 1;
            case 8 -> -1;
            default -> -99;
        };
    }

    static int dy(int in) {
        return switch (in) {
            case 1 -> -1;
            case 2 -> 1;
            case 4, 8 -> 0;
            default -> -99;
        };
    }

    private boolean inside(int x, int y) {
        return 0 < x && x < this.width - 1 && 0 < y && y < this.height - 1;
    }

    public boolean generate() {
        int bx = 0, by = 0;
        switch (this.rand.nextInt(4)) {
            case 1 -> {
                bx = this.width - 2;
                by = this.height - 2;
            }
            case 2 -> bx = this.width - 2;
            case 3 -> by = this.height - 2;
            default -> {
            }
        }
        this.grid[by][bx] = 512;
        this.grid[by][bx + 1] = 768;
        this.grid[by + 1][bx] = 1024;
        this.grid[by + 1][bx + 1] = 1280;
        int px = 1 + this.width / 2;
        int py = 1 + this.height / 2;
        this.grid[py][px] = 256;
        List<int[]> cells = new ArrayList<>();
        int l = (this.width + this.height) / 4;
        for (int z = 0; z < l; z++) {
            int w = 1 + this.rand.nextInt(3);
            if (w > 2) l--;
            int qq = this.rand.nextInt(this.width - w);
            int ww = this.rand.nextInt(this.height - w);
            for (int a = qq; a < qq + w; a++) {
                for (int b = ww; b < ww + w; b++) {
                    if (this.grid[b][a] == 0) this.grid[b][a] = -1;
                }
            }
        }
        List<Integer> directions = Arrays.asList(1, 2, 4, 8);
        Collections.shuffle(directions, this.rand);
        int xx = px + dx(directions.get(0));
        int yy = py + dy(directions.get(0));
        this.grid[py][px] |= directions.get(0);
        if (this.grid[yy][xx] < 0) this.grid[yy][xx] = 0;
        this.grid[yy][xx] |= opp(directions.get(0));
        cells.add(new int[]{xx, yy});
        boolean success = false;
        while (!cells.isEmpty()) {
            int index = this.nextIndex(cells.size());
            int x = cells.get(index)[0];
            int y = cells.get(index)[1];
            Collections.shuffle(directions, this.rand);
            boolean carved = false;
            for (int dir : directions) {
                int nx = x + dx(dir);
                int ny = y + dy(dir);
                if (this.inside(nx, ny)) {
                    if (this.grid[ny][nx] == 0) {
                        this.grid[y][x] |= dir;
                        this.grid[ny][nx] |= opp(dir);
                        cells.add(new int[]{nx, ny});
                        carved = true;
                    }
                    if (carved) {
                        success = true;
                        break;
                    }
                }
            }
            if (!carved) cells.remove(index);
        }
        if (!success) return false;
        for (int aa = 0; aa < this.height; aa++) {
            for (int bb = 0; bb < this.width; bb++) {
                if (this.grid[aa][bb] < 0) this.grid[aa][bb] = 0;
            }
        }
        Collections.shuffle(directions, this.rand);
        for (int dir : directions) {
            int nx = px + dx(dir);
            int ny = py + dy(dir);
            if (this.inside(nx, ny) && this.grid[ny][nx] > 0 && this.rand.nextBoolean()) {
                this.grid[ny][nx] |= opp(dir);
                this.grid[py][px] |= dir;
            }
        }
        Collections.shuffle(directions, this.rand);
        boolean connected = false;
        connect:
        for (int ax = 0; ax < 2; ax++) {
            for (int ay = 0; ay < 2; ay++) {
                for (int dir : directions) {
                    int nx = bx + ax + dx(dir);
                    int ny = by + ay + dy(dir);
                    if (this.inside(nx, ny) && this.grid[ny][nx] > 0 && new Cell((short) this.grid[ny][nx]).feature == 0) {
                        this.grid[ny][nx] |= opp(dir);
                        this.grid[by + ay][bx + ax] |= dir;
                        connected = true;
                        break connect;
                    }
                }
            }
        }
        if (!connected) {
            List<Integer> directions2 = Arrays.asList(1, 2, 4, 8);
            Collections.shuffle(directions2, this.rand);
            success = false;
            tunnel:
            for (int ax = 0; ax < 2; ax++) {
                for (int ay = 0; ay < 2; ay++) {
                    for (int dir2 : directions2) {
                        int qx = bx + ax + dx(dir2);
                        int qy = by + ay + dy(dir2);
                        if (this.inside(qx, qy) && this.grid[qy][qx] == 0) {
                            cells.add(new int[]{qx, qy});
                            while (!cells.isEmpty()) {
                                int index = this.nextIndex(cells.size());
                                int x = cells.get(index)[0];
                                int y = cells.get(index)[1];
                                Collections.shuffle(directions, this.rand);
                                boolean carved = false;
                                for (int dir : directions) {
                                    int nx = x + dx(dir);
                                    int ny = y + dy(dir);
                                    if (this.inside(nx, ny)) {
                                        if (this.grid[ny][nx] == 0) {
                                            this.grid[y][x] |= dir;
                                            this.grid[y][x] |= 25344;
                                            this.grid[ny][nx] |= opp(dir);
                                            this.grid[ny][nx] |= 25344;
                                            cells.add(new int[]{nx, ny});
                                            carved = true;
                                        } else if (new Cell((short) this.grid[ny][nx]).feature == 0) {
                                            this.grid[y][x] |= dir;
                                            this.grid[ny][nx] |= opp(dir);
                                            this.grid[qy][qx] |= opp(dir2);
                                            this.grid[by + ay][bx + ax] |= dir2;
                                            success = true;
                                            break tunnel;
                                        }
                                        if (carved) break;
                                    }
                                }
                                if (!carved) cells.remove(index);
                            }
                        }
                    }
                }
            }
            if (!success) return false;
        }
        for (int aa = 0; aa < this.height; aa++) {
            for (int bb = 0; bb < this.width; bb++) {
                Cell c = new Cell((short) this.grid[aa][bb]);
                if (c.feature == 99) {
                    c.feature = 0;
                    this.grid[aa][bb] = c.pack();
                }
            }
        }
        // os becos: a sala da chave num, e metade dos outros com enfeite
        List<int[]> deadEnds = new ArrayList<>();
        for (int aa = 0; aa < this.height; aa++) {
            for (int bb = 0; bb < this.width; bb++) {
                Cell c = new Cell((short) this.grid[aa][bb]);
                int exits = (c.north ? 1 : 0) + (c.south ? 1 : 0) + (c.east ? 1 : 0) + (c.west ? 1 : 0);
                if (exits == 1 && c.feature == 0) deadEnds.add(new int[]{aa, bb});
            }
        }
        if (deadEnds.isEmpty()) return false;
        int r = this.rand.nextInt(deadEnds.size());
        int[] ll = deadEnds.get(r);
        Cell c = new Cell((short) this.grid[ll[0]][ll[1]]);
        c.feature = 6;
        this.grid[ll[0]][ll[1]] = c.pack();
        deadEnds.remove(r);
        if (!deadEnds.isEmpty()) {
            r = 0;
            while (r < deadEnds.size() / 2) {
                int rx = this.rand.nextInt(deadEnds.size());
                int[] llx = deadEnds.get(rx);
                Cell cx = new Cell((short) this.grid[llx[0]][llx[1]]);
                if (cx.feature == 0) {
                    cx.feature = (byte) (7 + this.rand.nextInt(3));
                    this.grid[llx[0]][llx[1]] = cx.pack();
                    deadEnds.remove(rx);
                    r++;
                }
            }
        }
        for (int aa = 0; aa < this.height; aa++) {
            for (int bb = 0; bb < this.width; bb++) {
                c = new Cell((short) this.grid[aa][bb]);
                if (c.feature == 0 && (c.north || c.south || c.west || c.east) && this.rand.nextInt(25) == 0) {
                    c.feature = switch (this.rand.nextInt(8)) {
                        case 0 -> 8;
                        case 1 -> 10;
                        case 2, 3 -> 11;
                        case 4, 5 -> 12;
                        case 6 -> 13;
                        default -> 14;
                    };
                    this.grid[aa][bb] = c.pack();
                }
            }
        }
        return true;
    }

    private int nextIndex(int ceil) {
        float r = this.rand.nextFloat();
        if (r <= 0.45f) return ceil - 1;
        return r <= 0.9f ? this.rand.nextInt(ceil) : 0;
    }
}
