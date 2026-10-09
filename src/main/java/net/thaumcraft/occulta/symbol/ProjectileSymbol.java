package net.thaumcraft.occulta.symbol;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Um símbolo que <b>atira uma coisa</b>: a {@code SymbolEffectProjectile} do Witchery.
 *
 * <p>A maior parte dos símbolos é assim: eles não fazem nada a quem os lança, fazem alguma coisa <b>onde a
 * bola bate</b>. A bola é a mesma para todos — o que lhe muda é a <b>cor</b> e o <b>tamanho</b> —, e é por ela que
 * se reconhece de longe qual feitiço vem vindo.
 *
 * <p>Ela sai <b>um bloco e meio à frente</b> dos olhos de quem a lança, para não lhe bater na cara, e anda
 * com aceleração própria como uma bola de fogo.
 */
public abstract class ProjectileSymbol extends Symbol {
    /** A cor dela, e o tamanho. */
    private int cor = 0xFF0000;
    private float tamanho = 1.0f;

    /** E quanto tempo ela vive, se tiver prazo. */
    private int vive = -1;

    /** Quão à frente ela nasce. */
    public static final double ADIANTE = 1.5;

    protected ProjectileSymbol(int id, String nome) {
        super(id, nome);
    }

    protected ProjectileSymbol(int id, String nome, int custo, boolean maldição, boolean imperdoável,
                               int trava) {
        super(id, nome, custo, maldição, imperdoável, trava);
    }

    protected ProjectileSymbol(int id, String nome, int custo, boolean maldição, boolean imperdoável,
                               int trava, @org.jetbrains.annotations.Nullable String chave) {
        super(id, nome, custo, maldição, imperdoável, trava, chave);
    }

    public ProjectileSymbol cor(int cor) {
        this.cor = cor;
        return this;
    }

    public ProjectileSymbol tamanho(float tamanho) {
        this.tamanho = tamanho;
        return this;
    }

    public ProjectileSymbol vive(int batidas) {
        this.vive = batidas;
        return this;
    }

    public int cor() {
        return this.cor;
    }

    public float tamanho() {
        return this.tamanho;
    }

    @Override
    public void lança(ServerLevel level, ServerPlayer quem, int grau) {
        level.levelEvent(null, 1018, BlockPos.containing(quem.position()), 0);
        Vec3 rumo = quem.getLookAngle();
        SpellEffectEntity bola = new SpellEffectEntity(level, quem, rumo, this, grau);
        if (this.vive > 0) bola.vive(this.vive);
        bola.setPos(quem.getX() + rumo.x * ADIANTE,
                quem.getY() + quem.getEyeHeight() - 0.1 + rumo.y * ADIANTE,
                quem.getZ() + rumo.z * ADIANTE);
        level.addFreshEntity(bola);
    }

    /** <b>E o que ela faz onde bate.</b> */
    public abstract void aoBater(ServerLevel level, LivingEntity quem, HitResult onde, int grau);
}
