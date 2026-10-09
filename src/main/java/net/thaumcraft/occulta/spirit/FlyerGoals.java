package net.thaumcraft.occulta.spirit;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.function.Predicate;

/**
 * As metas de quem <b>flutua</b>: as {@code EntityAIFlyer*} do Witchery.
 *
 * <p>As quatro são a mesma ideia escrita quatro vezes, e vale dizer qual é, porque ela não é a do jogo de
 * hoje: <b>nenhuma delas acha caminho</b>. Elas não pedem à navegação que leve o bicho a um lugar — elas
 * <b>empurram a velocidade dele</b> um bocadinho por vez na direção que querem, e antes de empurrar
 * perguntam se a <b>linha reta</b> até lá está livre, passando a caixa de choque do bicho pelo caminho de
 * metro em metro.
 *
 * <p>É por isso que um espírito do Witchery se mexe como se mexe: ele <b>deriva</b>. Não contorna paredes,
 * não sobe escadas, não desiste — bate, perde o rumo, escolhe outro ponto ao acaso e volta a derivar. Trocar
 * isso pela navegação voadora do jogo de hoje daria um bicho que anda bem e <b>não se parece nada com ele</b>.
 *
 * <p>Por isso as quatro estão aqui como estão lá, velocidade a velocidade.
 */
public final class FlyerGoals {
    /** De quanto em quanto a linha reta é conferida, e quanto o empurrão vale em cada meta. */
    public static final double EMPURRA_CASA = 0.2;
    public static final double EMPURRA_VAGUEIA = 0.1;
    public static final double EMPURRA_POUSA = 0.05;
    public static final double EMPURRA_TENTA = 0.05;

    private FlyerGoals() {
    }

    /**
     * Se a <b>linha reta</b> daqui até ali está livre.
     *
     * <p>O original passa a caixa de choque do bicho pelo caminho, um passo por bloco de distância, e para
     * ao primeiro estorvo. É uma conta grosseira e é de propósito: ela erra para o lado de «não dá», e é
     * esse erro que faz o bicho parecer indeciso.
     */
    public static boolean rumoLivre(Mob quem, double x, double y, double z, double quanto) {
        if (quanto <= 0.0) return true;
        double dx = (x - quem.getX()) / quanto;
        double dy = (y - quem.getY()) / quanto;
        double dz = (z - quem.getZ()) / quanto;
        AABB caixa = quem.getBoundingBox();
        for (int passo = 1; passo < quanto; passo++) {
            caixa = caixa.move(dx, dy, dz);
            if (!quem.level().noCollision(quem, caixa)) return false;
        }
        return true;
    }

    /** O rumo do corpo segue a velocidade, que é o que as quatro fazem no fim de cada batida. */
    static void viraParaOnde(Mob quem) {
        Vec3 v = quem.getDeltaMovement();
        float rumo = -((float) Math.atan2(v.x, v.z)) * 180.0f / (float) Math.PI;
        quem.setYRot(rumo);
        quem.yBodyRot = rumo;
    }

    // ------------------------------------------------------------------ sentar e ficar

    /** <b>Sentado, não faz nada.</b> O {@code EntityAISitAndStay}: ele só ocupa as outras metas. */
    public static class SentaEFica extends Goal {
        private final TamableAnimal quem;

        public SentaEFica(TamableAnimal quem) {
            this.quem = quem;
            this.setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return this.quem.isOrderedToSit();
        }

        @Override
        public void start() {
            this.quem.getNavigation().stop();
        }
    }

    // ------------------------------------------------------------------ vaguear

    /**
     * <b>Vaguear.</b> O {@code EntityAIFlyerWander}.
     *
     * <p>Escolhe um ponto ao acaso — a <b>seis</b> blocos de raio se for selvagem, a <b>dois</b> se tiver
     * dono — e deriva para lá. Começa quando há <b>gente perto</b> e ele não é de ninguém, ou de vez em
     * quando: uma vez em trezentas de dia, uma em cem de noite.
     */
    public static class Vagueia extends Goal {
        private static final double LONGE_DEMAIS = 3600.0;
        private static final double PERTO = 1.0;
        private static final float SELVAGEM = 6.0f;
        private static final float DE_ALGUÉM = 2.0f;
        private static final int DE_DIA = 300;
        private static final int DE_NOITE = 100;
        private static final int PARADO_HÁ = 100;
        private static final int DESISTE = 40;

        private final Mob quem;
        private final double foge;
        private double x;
        private double y;
        private double z;
        private int espera;

        public Vagueia(Mob quem, double foge) {
            this.quem = quem;
            this.foge = foge;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        private boolean deAlguém() {
            return this.quem instanceof TamableAnimal bicho && bicho.isTame();
        }

        private boolean sentado() {
            return this.quem instanceof TamableAnimal bicho && bicho.isOrderedToSit();
        }

        @Override
        public boolean canUse() {
            if (!this.deAlguém()
                    && this.quem.level().getNearestPlayer(this.quem, this.foge) != null) {
                return true;
            }
            if (this.quem.getNoActionTime() >= PARADO_HÁ) return false;
            int quanto = this.quem.level().isBrightOutside() ? DE_DIA : DE_NOITE;
            return this.quem.getRandom().nextInt(quanto) == 0 && !this.sentado();
        }

        @Override
        public boolean canContinueToUse() {
            if (this.quem instanceof TamableAnimal bicho && !bicho.isOrderedToSit()) return true;
            return this.quem.getRandom().nextInt(DESISTE) != 0;
        }

        @Override
        public void tick() {
            double dx = this.x - this.quem.getX();
            double dy = this.y - this.quem.getY();
            double dz = this.z - this.quem.getZ();
            double quanto = dx * dx + dy * dy + dz * dz;
            if (quanto < PERTO || quanto > LONGE_DEMAIS) {
                float raio = this.deAlguém() ? DE_ALGUÉM : SELVAGEM;
                var sorte = this.quem.getRandom();
                this.x = this.quem.getX() + (sorte.nextFloat() * 8.0f - 4.0f) * raio;
                this.y = this.quem.getY() + (sorte.nextFloat() * 2.0f - 1.0f) * raio;
                this.z = this.quem.getZ() + (sorte.nextFloat() * 8.0f - 4.0f) * raio;
                dx = this.x - this.quem.getX();
                dy = this.y - this.quem.getY();
                dz = this.z - this.quem.getZ();
                quanto = dx * dx + dy * dy + dz * dz;
            }

            /*
             * O relógio do original, com o sinal e tudo: ele <b>desconta primeiro</b> e só depois
             * pergunta, de modo que o passar de zero para menos um é o que o dispara — e o que ele soma
             * a seguir vai somado a esse menos um. São duas ou três batidas entre empurrões, e não três
             * ou quatro.
             */
            if (this.espera-- > 0) {
                viraParaOnde(this.quem);
                return;
            }
            this.espera += this.quem.getRandom().nextInt(2) + 2;

            double longe = Math.sqrt(quanto);
            if (rumoLivre(this.quem, this.x, this.y, this.z, longe)) {
                this.quem.setDeltaMovement(this.quem.getDeltaMovement().add(
                        dx / longe * EMPURRA_VAGUEIA,
                        dy / longe * EMPURRA_VAGUEIA,
                        dz / longe * EMPURRA_VAGUEIA));
            } else {
                this.x = this.quem.getX();
                this.y = this.quem.getY();
                this.z = this.quem.getZ();
            }
            viraParaOnde(this.quem);
        }
    }

    // ------------------------------------------------------------------ pousar

    /**
     * <b>Pousar.</b> O {@code EntityAIFlyerLand}.
     *
     * <p>Procura um lugar de pousar — o <b>topo de uma árvore</b>, se lhe pedirem, e senão o <b>chão</b> por
     * baixo —, deriva para lá devagar e, não achando nada, <b>afunda</b> um décimo por batida até tocar em
     * algo. Não começa nem continua por cima de líquido: um espírito não pousa na água.
     */
    public static class Pousa extends Goal {
        private static final int UMA_EM_VINTE = 20;
        private static final int DESISTE_EM = 100;
        private static final int RAIO = 16;
        private static final int ALTURAS = 3;
        private static final int ACIMA = 10;
        private static final int TENTA = 10;
        private static final double AFUNDA = -0.1;
        private static final double CHEGOU = 1.0;

        private final Mob quem;
        private final boolean árvores;
        private int espera;
        private int @Nullable [] onde;

        public Pousa(Mob quem, boolean árvores) {
            this.quem = quem;
            this.árvores = árvores;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        private boolean pousado() {
            var feitio = this.quem.level().getBlockState(
                    net.minecraft.core.BlockPos.containing(this.quem.getX(), this.quem.getY() - 0.01,
                            this.quem.getZ()));
            return folha(feitio) || feitio.isSolid();
        }

        private static boolean folha(net.minecraft.world.level.block.state.BlockState feitio) {
            return feitio.is(net.minecraft.tags.BlockTags.LEAVES);
        }

        private boolean líquidoEmBaixo(double y) {
            return !this.quem.level().getFluidState(net.minecraft.core.BlockPos.containing(
                    this.quem.getX(), y, this.quem.getZ())).isEmpty();
        }

        private boolean podePousar() {
            return !this.pousado()
                    && !this.líquidoEmBaixo(this.quem.getY() - 1.0)
                    && !this.líquidoEmBaixo(this.quem.getY());
        }

        @Override
        public boolean canUse() {
            return this.podePousar() && this.quem.getRandom().nextInt(UMA_EM_VINTE) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return this.podePousar();
        }

        @Override
        public void start() {
            this.espera = DESISTE_EM;
            int x = this.quem.getBlockX();
            int y = this.quem.getBlockY();
            int z = this.quem.getBlockZ();
            this.onde = this.árvores ? this.umaÁrvore(x, y, z) : null;
            if (this.onde == null) this.onde = this.oChão(x, y, z);
        }

        @Override
        public void stop() {
            this.onde = null;
        }

        /** O topo de uma árvore: folhas a dezesseis blocos, e o primeiro ar por cima delas. */
        private int @Nullable [] umaÁrvore(int x0, int y0, int z0) {
            for (int y = Math.max(y0 - ALTURAS, this.quem.level().getMinY() + 1); y <= y0 + ALTURAS; y++) {
                for (int x = x0 - RAIO; x <= x0 + RAIO; x++) {
                    for (int z = z0 - RAIO; z <= z0 + RAIO; z++) {
                        if (!folha(this.quem.level().getBlockState(
                                new net.minecraft.core.BlockPos(x, y, z)))) {
                            continue;
                        }
                        for (int y2 = y; y2 < y0 + ACIMA; y2++) {
                            if (!this.quem.level().getBlockState(
                                    new net.minecraft.core.BlockPos(x, y2, z)).is(Blocks.AIR)) {
                                continue;
                            }
                            double longe = Math.sqrt(this.quem.distanceToSqr(x, y2, z));
                            if (rumoLivre(this.quem, x, y2, z, longe)) {
                                return new int[]{x, y2 + 2, z};
                            }
                        }
                    }
                }
            }
            return null;
        }

        /** E o chão: o primeiro bloco cheio por baixo, ou dez tentativas ao acaso num raio de dez. */
        private int @Nullable [] oChão(int x0, int y0, int z0) {
            for (int y = y0; y > this.quem.level().getMinY() + 1; y--) {
                var feitio = this.quem.level().getBlockState(new net.minecraft.core.BlockPos(x0, y, z0));
                if (feitio.isAir()) continue;
                if (feitio.getFluidState().isEmpty()) return new int[]{x0, y + 1, z0};

                var sorte = this.quem.getRandom();
                for (int volta = 0; volta < TENTA; volta++) {
                    int x = (int) Math.floor(this.quem.getX() + sorte.nextInt(20) - 10.0);
                    int y2 = (int) Math.floor(this.quem.getBoundingBox().minY + sorte.nextInt(6) - 3.0);
                    int z = (int) Math.floor(this.quem.getZ() + sorte.nextInt(20) - 10.0);
                    var ali = this.quem.level().getBlockState(new net.minecraft.core.BlockPos(x, y2, z));
                    if (!folha(ali) && !ali.isSolid()) continue;
                    if (!this.quem.level().getBlockState(
                            new net.minecraft.core.BlockPos(x, y2 + 1, z)).is(Blocks.AIR)) {
                        continue;
                    }
                    double longe = Math.sqrt(this.quem.distanceToSqr(x, y2, z));
                    if (rumoLivre(this.quem, x, y2, z, longe)) return new int[]{x, y2 + 1, z};
                }
            }
            return null;
        }

        @Override
        public void tick() {
            if (this.pousado()) {
                viraParaOnde(this.quem);
                return;
            }
            if (this.onde != null
                    && this.quem.distanceToSqr(this.onde[0], this.quem.getY(), this.onde[2]) > CHEGOU
                    && this.espera-- > 0) {
                double longe = Math.sqrt(
                        this.quem.distanceToSqr(this.onde[0], this.onde[1], this.onde[2]));
                if (rumoLivre(this.quem, this.onde[0], this.onde[1], this.onde[2], longe)) {
                    this.quem.setDeltaMovement(this.quem.getDeltaMovement().add(
                            (this.onde[0] - this.quem.getX()) / longe * EMPURRA_POUSA,
                            (this.onde[1] - this.quem.getY()) / longe * EMPURRA_POUSA,
                            (this.onde[2] - this.quem.getZ()) / longe * EMPURRA_POUSA));
                }
            } else if (!this.líquidoEmBaixo(this.quem.getY() - 1.0)) {
                var v = this.quem.getDeltaMovement();
                this.quem.setDeltaMovement(v.x, AFUNDA, v.z);
            }
            viraParaOnde(this.quem);
        }
    }

    // ------------------------------------------------------------------ ir para casa

    /**
     * <b>Ir para casa.</b> O {@code EntityAIFlyerFlyToWaypoint} do original, na forma dele que não carrega
     * nada.
     *
     * <p>Deriva para o lugar marcado e, enquanto estiver <b>abaixo de trinta e dois blocos acima dele</b>,
     * sobe mais um décimo por batida — é assim que ele passa por cima do que não consegue contornar. Perdendo
     * o rumo, atira-se para um ponto ao acaso a seis blocos e tenta outra vez.
     *
     * <p>Quando para, <b>esquece o lugar e senta-se</b>.
     */
    public static class VaiParaCasa extends Goal {
        private static final double CHEGOU = 1.0;
        private static final double SOBE_ATÉ = 32.0;
        private static final double SOBE = 0.1;
        private static final int RUMO_DE = 10;
        private static final double ESPALHA_LADO = 6.0;
        private static final double ESPALHA_ALTO = 4.0;

        private final SpiritEntity quem;
        private int espera;

        public VaiParaCasa(SpiritEntity quem) {
            this.quem = quem;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return this.quem.temCasa();
        }

        @Override
        public boolean canContinueToUse() {
            if (this.quem.temCasa()) return true;
            return this.quem.distanceToSqr(this.quem.casaX(), this.quem.getY(), this.quem.casaZ()) > CHEGOU
                    || Math.abs(this.quem.getY() - this.quem.casaY()) > 1.0;
        }

        @Override
        public void stop() {
            this.quem.esqueceACasa();
            this.quem.setOrderedToSit(true);
            this.espera = 0;
        }

        @Override
        public void tick() {
            if (this.quem.isOrderedToSit()) return;

            double dx = this.quem.casaX() - this.quem.getX();
            double dy = this.quem.casaY() - this.quem.getY();
            double dz = this.quem.casaZ() - this.quem.getZ();
            double longe = Math.sqrt(dx * dx + dy * dy + dz * dz);

            /* O mesmo relógio: desconta, prende em zero, e age quando chega lá. */
            if (--this.espera < 0) this.espera = 0;
            if (this.espera != 0) {
                viraParaOnde(this.quem);
                return;
            }

            if (!rumoLivre(this.quem, this.quem.casaX(), this.quem.casaY(), this.quem.casaZ(), longe)) {
                var sorte = this.quem.getRandom();
                double nx = this.quem.getX() + (sorte.nextDouble() * 4.0 - 2.0) * ESPALHA_LADO;
                double ny = this.quem.getY() + (sorte.nextDouble() * 2.0 - 1.0) * ESPALHA_ALTO;
                double nz = this.quem.getZ() + (sorte.nextDouble() * 4.0 - 2.0) * ESPALHA_LADO;
                if (sorte.nextInt(2) != 0) {
                    dx = nx - this.quem.getX();
                    dz = nz - this.quem.getZ();
                }
                boolean chegou = this.quem.distanceToSqr(
                        this.quem.casaX(), this.quem.casaY(), this.quem.casaZ()) <= CHEGOU;
                dy = (chegou && this.quem.getY() > this.quem.casaY() && ny > 0.0 ? -ny : ny)
                        - this.quem.getY();
                longe = Math.sqrt(dx * dx + dy * dy + dz * dz);
            }
            if (longe <= 0.0) return;

            double sobe = this.quem.getY() < Math.min(this.quem.casaY() + SOBE_ATÉ,
                    this.quem.level().getMaxY()) ? SOBE : 0.0;
            this.quem.setDeltaMovement(this.quem.getDeltaMovement().add(
                    dx / longe * EMPURRA_CASA,
                    dy / longe * EMPURRA_CASA + sobe,
                    dz / longe * EMPURRA_CASA));
            this.espera = RUMO_DE;
            viraParaOnde(this.quem);
        }
    }

    // ------------------------------------------------------------------ a tentação

    /**
     * <b>A tentação.</b> O {@code EntityAIFlyingTempt}.
     *
     * <p>Com uma coisa certa na mão de alguém a <b>dez blocos</b>, ele deriva para lá — e <b>sobe um
     * bocadinho a mais</b> enquanto estiver abaixo da altura da mão, que é o que o faz parecer que vem
     * cheirar o que lhe oferecem. Perdendo o interesse, fica <b>cem batidas</b> sem se deixar tentar.
     *
     * <p>E se quem o tenta <b>se mexer</b>, ele perde o interesse. Um espírito não segue ninguém: ele vem
     * ver.
     */
    public static class Tentação extends Goal {
        private static final double OLHA = 10.0;
        private static final double PERTO = 3.0;
        private static final double ASSUSTA = 36.0;
        private static final double MEXEU = 0.010000000000000002;
        private static final double SOBE = 0.025;
        private static final int ESQUECE = 100;

        private final Mob quem;
        private final Predicate<ItemStack> oquê;
        private final boolean assustadiço;
        private @Nullable Player quemTenta;
        private double x;
        private double y;
        private double z;
        private int espera;

        public Tentação(Mob quem, Predicate<ItemStack> oquê, boolean assustadiço) {
            this.quem = quem;
            this.oquê = oquê;
            this.assustadiço = assustadiço;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.quem instanceof TamableAnimal bicho && bicho.isTame()) return false;
            if (this.espera > 0) {
                this.espera--;
                return false;
            }
            this.quemTenta = this.quem.level().getNearestPlayer(this.quem, OLHA);
            if (this.quemTenta == null) return false;
            return this.oquê.test(this.quemTenta.getMainHandItem());
        }

        @Override
        public boolean canContinueToUse() {
            if (this.quemTenta == null) return false;
            if (this.assustadiço) {
                if (this.quem.distanceToSqr(this.quemTenta) < ASSUSTA) {
                    if (this.quemTenta.distanceToSqr(this.x, this.y, this.z) > MEXEU) return false;
                } else {
                    this.x = this.quemTenta.getX();
                    this.y = this.quemTenta.getY();
                    this.z = this.quemTenta.getZ();
                }
            }
            return this.canUse();
        }

        @Override
        public void stop() {
            this.quemTenta = null;
            this.espera = ESQUECE;
        }

        /** Quem o está tentando agora, para as provas e para o desenho. */
        public @Nullable LivingEntity quemTenta() {
            return this.quemTenta;
        }

        @Override
        public void tick() {
            if (this.quemTenta == null) return;
            if (this.quem.distanceToSqr(this.quemTenta) < PERTO) return;

            double dx = this.x - this.quem.getX();
            double dy = this.y - this.quem.getY();
            double dz = this.z - this.quem.getZ();
            double longe = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (longe <= 0.0) return;

            if (rumoLivre(this.quem, this.x, this.y, this.z, longe)) {
                double sobe = this.quem.getY() < this.y + 1.0 ? SOBE : 0.0;
                this.quem.setDeltaMovement(this.quem.getDeltaMovement().add(
                        dx / longe * EMPURRA_TENTA,
                        dy / longe * EMPURRA_TENTA + sobe,
                        dz / longe * EMPURRA_TENTA));
            }
            viraParaOnde(this.quem);
        }
    }
}
