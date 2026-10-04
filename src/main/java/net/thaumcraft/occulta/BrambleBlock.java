package net.thaumcraft.occulta;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * As <b>sarças</b>: o {@code BlockBramble} do Witchery.
 *
 * <p>São duas, e as duas são a mesma planta com uma diferença: a <b>Sarça Selvagem</b> <b>espinha</b> quem
 * passa por ela, e a <b>Sarça do Fim</b> <b>manda quem passa para longe</b> — até quinhentos blocos, num
 * lugar que ela escolhe e ninguém vê.
 *
 * <p>Elas são <b>duríssimas</b>: vinte de dureza, o mesmo que obsidiana. Não se atravessa uma sarça com
 * pressa.
 *
 * <h2>E cortá-la a espalha</h2>
 *
 * <p>Esta é a parte que ninguém descobre sozinho. Cortar uma Sarça Selvagem faz dela <b>oito</b>: ela tenta
 * nascer nas oito casas à volta, metade das vezes em cada uma, parando na primeira que pegar em dois de cada
 * três casos. Quem a quiser tirar do caminho a multiplica.
 *
 * <p><b>A não ser com um machado de ouro.</b> É a única ferramenta no mundo que a corta sem a espalhar, e o
 * original não o diz em lugar nenhum — nem no livro, nem na dica, nem no nome. Está escrito numa linha de
 * código e em mais lado nenhum.
 */
public class BrambleBlock extends VegetationBlock {
    public static final MapCodec<BrambleBlock> CODEC = simpleCodec(p -> new BrambleBlock(p, false));

    /** O quanto a selvagem espinha, que é o do cato. */
    public static final float ESPINHO = 1.0f;

    /** E até onde a do Fim atira: quinhentos para cada lado. */
    public static final int LONGE = 500;
    public static final int SOBE = 64;
    public static final int TETO = 250;

    /** De duas em duas casas ela pega, e em duas de três ela para na primeira. */
    public static final int PEGA_EM = 2;
    public static final int PARA_EM = 3;

    private static final VoxelShape FORMA = Block.box(2.0, 0.0, 2.0, 14.0, 14.0, 14.0);

    private final boolean doFim;

    public BrambleBlock(Properties properties, boolean doFim) {
        super(properties);
        this.doFim = doFim;
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }

    /** Se esta é a que atira para longe. */
    public boolean doFim() {
        return this.doFim;
    }

    @Override
    protected VoxelShape getShape(BlockState feitio, net.minecraft.world.level.BlockGetter level,
                                  BlockPos onde, CollisionContext quem) {
        return FORMA;
    }

    /**
     * Ela se aguenta em <b>qualquer coisa</b> que não seja ar.
     *
     * <p>O original não lhe pede terra: uma sarça pega na pedra, na areia, no telhado de alguém. É o que
     * faz dela uma praga e não uma planta.
     */
    @Override
    protected boolean mayPlaceOn(BlockState oquê, net.minecraft.world.level.BlockGetter level, BlockPos onde) {
        return !oquê.isAir();
    }

    @Override
    protected boolean canSurvive(BlockState feitio, LevelReader level, BlockPos onde) {
        return !level.getBlockState(onde.below()).isAir();
    }

    /** A selvagem espinha; a do Fim atira. */
    @Override
    protected void entityInside(BlockState feitio, Level level, BlockPos onde, Entity quem,
                                InsideBlockEffectApplier efeitos, boolean dentroMesmo) {
        if (level instanceof ServerLevel mundo) this.toca(mundo, onde, quem);
    }

    /** O que ela faz a quem a toca: serve às provas, que não têm como pisar numa planta à mão. */
    public void toca(ServerLevel level, BlockPos onde, Entity quem) {
        if (this.doFim) {
            if (quem instanceof LivingEntity vivo) atiraLonge(level, onde, vivo);
            return;
        }
        quem.hurtServer(level, level.damageSources().cactus(), ESPINHO);
    }

    /**
     * <b>Para longe</b>: o {@code teleportAway} do original.
     *
     * <p>Ela sorteia um lugar até quinhentos blocos para cada lado, <b>desce</b> até achar chão, e então
     * <b>sobe</b> até caber uma pessoa de pé com dois blocos de ar por cima. Não cabendo em lugar nenhum
     * dentro de sessenta e quatro blocos de altura, ela desiste e quem passou fica onde está.
     *
     * <p>A conta é a mesma da pérola do fim de um mago que não sabe mirar: ela acerta <b>algum</b> lugar.
     */
    public static void atiraLonge(ServerLevel level, BlockPos onde, LivingEntity quem) {
        RandomSource sorte = level.getRandom();
        int x = onde.getX() + sorte.nextInt(2 * LONGE) - LONGE;
        int z = onde.getZ() + sorte.nextInt(2 * LONGE) - LONGE;
        int y = onde.getY();
        int teto = Math.min(y + SOBE, TETO);

        while (!level.getBlockState(new BlockPos(x, y, z)).isSolid() && y >= level.getMinY()) y--;
        while (y < teto && !cabe(level, x, y, z)) y++;
        if (y <= level.getMinY() || y >= teto) return;

        quem.teleportTo(x + 0.5, y + 1.0, z + 0.5);
        level.playSound(null, onde, net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT,
                net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    /** Se cabe uma pessoa de pé em cima desta casa. */
    private static boolean cabe(ServerLevel level, int x, int y, int z) {
        BlockState chão = level.getBlockState(new BlockPos(x, y, z));
        if (!chão.isSolid() || chão.is(Blocks.BEDROCK)) return false;
        for (int acima = 1; acima <= 3; acima++) {
            if (!level.getBlockState(new BlockPos(x, y + acima, z)).isAir()) return false;
        }
        return true;
    }

    /**
     * <b>Cortada, ela se espalha</b> — a não ser com um machado de ouro.
     *
     * <p>Só a selvagem. A do Fim, cortada, simplesmente sai.
     */
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos onde, BlockState feitio, Player quem) {
        if (level instanceof ServerLevel mundo && !this.doFim
                && !quem.getMainHandItem().is(Items.GOLDEN_AXE)) {
            espalha(mundo, onde, feitio);
        }
        return super.playerWillDestroy(level, onde, feitio, quem);
    }

    /** As oito casas à volta, cada uma tentando três alturas. */
    private static void espalha(ServerLevel level, BlockPos onde, BlockState feitio) {
        int[][] lados = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}, {1, 1}, {-1, -1}, {-1, 1}, {1, -1}};
        for (int[] lado : lados) {
            nasce(level, onde.offset(lado[0], 0, lado[1]), feitio);
        }
    }

    private static void nasce(ServerLevel level, BlockPos onde, BlockState feitio) {
        for (int dy = -1; dy <= 1; dy++) {
            BlockPos casa = onde.offset(0, dy, 0);
            BlockState oquê = level.getBlockState(casa);
            boolean vago = oquê.isAir() || oquê.is(Blocks.SNOW) || oquê.is(Blocks.SHORT_GRASS);
            if (!vago) continue;
            if (level.getBlockState(casa.below()).isAir()) continue;
            if (level.getRandom().nextInt(PEGA_EM) != 0) continue;
            level.setBlock(casa, feitio, Block.UPDATE_ALL);
            if (level.getRandom().nextInt(PARA_EM) != 0) return;
        }
    }

    /** E a do Fim larga uma faísca de portal, metade das batidas. */
    @Override
    public void animateTick(BlockState feitio, Level level, BlockPos onde, RandomSource sorte) {
        if (!this.doFim || sorte.nextInt(2) != 0) return;
        level.addParticle(ParticleTypes.PORTAL, onde.getX() + sorte.nextFloat(),
                onde.getY() + 0.65 + sorte.nextFloat() * 0.3, onde.getZ() + sorte.nextFloat(),
                0.0, -1.2, 0.0);
    }
}
