package net.thaumcraft.occulta.rite;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * Os ritos do ofício: as classes {@code Rite*} do Witchery.
 *
 * <p>Esta é a primeira leva — os que o mod de hoje já consegue pedir. A tabela de ritos do original tem
 * noventa e seis entradas, e quase todas pedem coisa que ainda não existe aqui (a Pedra Sintonizada, a Sopa de
 * Redstone, o Dedo de Sapo); elas entram à medida que os itens chegarem.
 */
public final class Rites {
    private Rites() {
    }

    // ------------------------------------------------------------------ o que cresce em volta

    /**
     * Um rito que se <b>abre em círculo</b>, crescendo de cinco em cinco batidas: o {@code RiteExpandingEffect}.
     *
     * <p>O raio é o passo mais três, e ele cresce até o que o rito pedir. Em cada passo, o anel inteiro recebe o
     * que o rito faz — e o anel é riscado pelo método de Bresenham, como no original.
     */
    public abstract static class Expanding implements Rite {
        protected final int maxRadius;
        protected final int height;
        protected final boolean curse;

        protected Expanding(int maxRadius, int height, boolean curse) {
            this.maxRadius = maxRadius;
            this.height = height;
            this.curse = curse;
        }

        @Override
        public List<RiteStep> steps(int coven) {
            return List.of(new Step(this, coven));
        }

        /** O que acontece em cada bloco do anel. */
        protected abstract void onBlock(ServerLevel level, BlockPos onde, int raio, Player quem);

        /** E o que acontece a cada passo, no anel inteiro; devolvendo falso, o rito desiste. */
        protected boolean onRing(ServerLevel level, BlockPos meio, int raio, Player quem) {
            return true;
        }

        /** Se o rito já acabou — por omissão, quando o círculo chegou ao tamanho dele. */
        protected boolean done(ServerLevel level, BlockPos meio, int raio, boolean cheio, long ticks) {
            return cheio;
        }

        /** O passo que faz o círculo crescer. */
        private static final class Step implements RiteStep {
            private final Expanding rite;
            private final int coven;
            private int stage;
            private boolean started;

            private Step(Expanding rite, int coven) {
                this.rite = rite;
                this.coven = coven;
            }

            @Override
            public Result run(ServerLevel level, BlockPos onde, long ticks, ActiveRite rito) {
                if (!this.started) {
                    if (ticks % 20 != 0) return Result.STARTING;
                    this.started = true;
                    level.playSound(null, onde, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 1.0f);
                }
                if (ticks % 5 != 0) return Result.UPKEEP;

                this.stage++;
                int raio = this.stage + 3;
                float teto = this.rite.maxRadius + 2.0f * this.coven;
                Player quem = rito.starter(level);
                if (raio <= teto) {
                    if (!this.rite.onRing(level, onde, raio, quem)) return Result.ABORTED;
                    ring(level, onde, raio, this.rite, quem);
                }
                boolean cheio = raio >= teto;
                return this.stage <= 250 && !this.rite.done(level, onde, raio, cheio, ticks)
                        ? Result.UPKEEP : Result.COMPLETED;
            }
        }

        /** O anel riscado, e o que ele faz ao primeiro chão que houver em cada ponto. */
        private static void ring(ServerLevel level, BlockPos meio, int raio, Expanding rite, Player quem) {
            int x = raio;
            int z = 0;
            int erro = 1 - x;
            while (x >= z) {
                int[][] pontos = {{x, z}, {z, x}, {-x, z}, {-z, x}, {-x, -z}, {-z, -x}, {x, -z}, {z, -x}};
                for (int[] p : pontos) pixel(level, meio.offset(p[0], 0, p[1]), raio, rite, quem);
                z++;
                if (erro < 0) {
                    erro += 2 * z + 1;
                } else {
                    x--;
                    erro += 2 * (z - x + 1);
                }
            }
        }

        /** O {@code drawPixel}: procura o chão de cima para baixo e faz nele o que o rito manda. */
        private static void pixel(ServerLevel level, BlockPos onde, int raio, Expanding rite, Player quem) {
            for (int i = 0; i < rite.height; i++) {
                BlockPos aqui = onde.above(i);
                if (level.getBlockState(aqui).isAir() || !level.isEmptyBlock(aqui.above())) continue;
                level.sendParticles(rite.curse ? ParticleTypes.WITCH : ParticleTypes.HAPPY_VILLAGER,
                        aqui.getX() + 0.5, aqui.getY() + 1.0, aqui.getZ() + 0.5, 2, 0.3, 0.3, 0.3, 0.0);
                rite.onBlock(level, aqui, raio, quem);
                return;
            }
        }
    }

    // ------------------------------------------------------------------ os três primeiros ritos

    /**
     * Cozer: o {@code RiteCookItem}.
     *
     * <p>O que estiver no chão dentro do círculo e for comida que a fornalha coze sai cozido — e uma parte
     * queima, virando carvão. Sem comida nenhuma no chão, o rito desiste.
     */
    public record Cook(float radius, double burnChance) implements Rite {
        @Override
        public List<RiteStep> steps(int coven) {
            return List.of((level, onde, ticks, rito) -> {
                if (ticks % 20 != 0) return RiteStep.Result.STARTING;
                AABB dentro = new AABB(onde).inflate(this.radius, 1.0, this.radius);
                int quantos = 0;
                for (ItemEntity largado : level.getEntitiesOfClass(ItemEntity.class, dentro)) {
                    ItemStack cru = largado.getItem();
                    var receita = level.recipeAccess()
                            .getRecipeFor(net.minecraft.world.item.crafting.RecipeType.SMELTING,
                                    new SingleRecipeInput(cru), level);
                    if (receita.isEmpty()) continue;
                    ItemStack cozido = receita.get().value().assemble(new SingleRecipeInput(cru));
                    if (cozido.isEmpty() || cozido.get(net.minecraft.core.component.DataComponents.FOOD) == null) {
                        continue;
                    }

                    int quantidade = cru.getCount();
                    int queimados = 0;
                    for (int i = 0; i < quantidade; i++) {
                        if (level.getRandom().nextDouble() < this.burnChance) queimados++;
                    }
                    largado.discard();
                    if (quantidade - queimados > 0) {
                        Block.popResource(level, onde, cozido.copyWithCount(quantidade - queimados));
                    }
                    if (queimados > 0) {
                        Block.popResource(level, onde, new ItemStack(Items.CHARCOAL, queimados));
                    }
                    quantos++;
                }
                if (quantos == 0) return RiteStep.Result.ABORTED_REFUND;
                level.sendParticles(ParticleTypes.FLAME, onde.getX() + 0.5, onde.getY() + 0.5,
                        onde.getZ() + 0.5, 16, 0.5, 0.5, 0.5, 0.05);
                level.playSound(null, onde, SoundEvents.GHAST_SHOOT, SoundSource.BLOCKS, 1.0f, 1.0f);
                return RiteStep.Result.COMPLETED;
            });
        }
    }

    /**
     * Fertilidade: o {@code RiteFertility}.
     *
     * <p>Onde o círculo passa, o chão recebe farinha de osso; e quem estiver no anel perde a fome, a cegueira e
     * o veneno. É o rito que faz de uma clareira uma horta.
     *
     * <p><b>Do original fica de fora, declarado:</b> curar o aldeão zumbi, que no Witchery vem com o mesmo rito —
     * aqui ele pediria o caminho de cura do jogo de hoje, que é outra coisa (maçã dourada e fraqueza), e não o
     * mesmo rito.
     */
    public static class Fertility extends Expanding {
        public Fertility(int maxRadius, int height) {
            super(maxRadius, height, false);
        }

        @Override
        protected void onBlock(ServerLevel level, BlockPos onde, int raio, Player quem) {
            net.minecraft.world.item.BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL), level, onde);
        }

        @Override
        protected boolean onRing(ServerLevel level, BlockPos meio, int raio, Player quem) {
            double fora = raio * raio;
            double dentro = Math.max(0, (raio - 1) * (raio - 1));
            for (Player gente : level.players()) {
                double distância = gente.distanceToSqr(meio.getX() + 0.5, meio.getY() + 0.5, meio.getZ() + 0.5);
                if (distância <= dentro || distância > fora) continue;
                gente.removeEffect(MobEffects.HUNGER);
                gente.removeEffect(MobEffects.BLINDNESS);
                gente.removeEffect(MobEffects.POISON);
            }
            return true;
        }
    }

    /**
     * Eclipse: o {@code RiteEclipse}.
     *
     * <p>De dia, e só de dia: o céu escurece de uma vez. No original ele adianta o mundo até a noite; aqui é o
     * mesmo — o tempo salta para o pôr do sol, e quem estava perto sente.
     */
    public record Eclipse() implements Rite {
        @Override
        public List<RiteStep> steps(int coven) {
            return List.of((level, onde, ticks, rito) -> {
                // o relógio do mundo de hoje anda por marcas, e não por número de batidas
                level.dimensionType().defaultClock().ifPresent(relógio ->
                        level.clockManager().moveToTimeMarker(relógio,
                                net.minecraft.world.clock.ClockTimeMarkers.NIGHT));
                level.sendParticles(ParticleTypes.LARGE_SMOKE, onde.getX() + 0.5, onde.getY() + 1.0,
                        onde.getZ() + 0.5, 64, 2.0, 1.0, 2.0, 0.02);
                level.playSound(null, onde, SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 0.6f, 1.4f);
                for (Player gente : level.players()) {
                    if (gente.distanceToSqr(onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5) > 256.0) continue;
                    gente.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0));
                }
                return RiteStep.Result.COMPLETED;
            });
        }
    }

    // ------------------------------------------------------------------ os ritos do tempo e da terra

    /**
     * O Rito da Tempestade: o {@code RiteWeatherCallStorm} do Witchery.
     *
     * <p>De trinta em trinta batidas cai um raio num anel em volta do círculo — nunca em cima dele, que é o que
     * o raio de dentro serve para garantir. Na <b>quarta</b> vez, o céu fecha-se: começa uma trovoada que dura
     * de cinco a quinze minutos. Depois disso caem raios a esmo até a conta chegar ao fim.
     *
     * @param minRadius de que distância para fora o raio pode cair
     * @param maxRadius e até onde
     * @param bolts     quantas fases o rito corre
     */
    public record Storm(int minRadius, int maxRadius, int bolts) implements Rite {
        @Override
        public List<RiteStep> steps(int coven) {
            return List.of((level, onde, ticks, rito) -> {
                if (ticks % 30L != 0L) return RiteStep.Result.STARTING;

                int fase = rito.advance();
                switch (fase) {
                    case 1, 2 -> bolt(level, onde);
                    case 3 -> {
                        bolt(level, onde);
                        bolt(level, onde);
                    }
                    case 4 -> {
                        if (!level.isThundering()) {
                            // o tempo do mundo de hoje mora num guardado à parte, e não no ServerLevel
                            int quanto = (300 + level.getRandom().nextInt(600)) * 20;
                            var tempo = level.getWeatherData();
                            tempo.setClearWeatherTime(0);
                            tempo.setRainTime(quanto);
                            tempo.setThunderTime(quanto);
                            tempo.setRaining(true);
                            tempo.setThundering(true);
                        }
                        bolt(level, onde);
                    }
                    default -> {
                        int quantos = level.getRandom().nextInt(4);
                        for (int i = 0; i < quantos; i++) {
                            bolt(level, onde);
                            if (i > 0) rito.advance();
                        }
                    }
                }
                return rito.stage() < this.bolts ? RiteStep.Result.STARTING : RiteStep.Result.COMPLETED;
            });
        }

        /** Um raio num ponto do anel: a mesma conta do {@code spawnBolt}. */
        private void bolt(ServerLevel level, BlockPos onde) {
            int faixa = this.maxRadius - this.minRadius;
            int dx = level.getRandom().nextInt(faixa * 2 + 1);
            if (dx > faixa) dx += this.minRadius * 2;
            int dz = level.getRandom().nextInt(faixa * 2 + 1);
            if (dz > faixa) dz += this.minRadius * 2;

            BlockPos casa = new BlockPos(onde.getX() - this.maxRadius + dx, onde.getY(),
                    onde.getZ() - this.maxRadius + dz);
            var raio = net.minecraft.world.entity.EntityTypes.LIGHTNING_BOLT.create(level,
                    net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
            if (raio == null) return;
            raio.snapTo(net.minecraft.world.phys.Vec3.atBottomCenterOf(casa));
            level.addFreshEntity(raio);
        }
    }

    /**
     * O Rito de Cozer: o {@code RiteCookItem} do Witchery.
     *
     * <p>Tudo o que for <b>comida</b> e estiver largado a cinco do círculo sai cozido. E parte queima: cada
     * unidade tem oito por cento de virar <b>carvão vegetal</b>, que é o preço de cozer sem forno.
     *
     * <p>Não havendo nada que se coza, o rito desiste e devolve o que se ofereceu.
     */
    public record CookFood(double radius, double burnChance) implements Rite {
        @Override
        public List<RiteStep> steps(int coven) {
            return List.of((level, onde, ticks, rito) -> {
                if (ticks % 20L != 0L) return RiteStep.Result.STARTING;

                int cozidos = 0;
                AABB roda = new AABB(onde).inflate(this.radius);
                for (ItemEntity largado : level.getEntitiesOfClass(ItemEntity.class, roda)) {
                    ItemStack cru = largado.getItem();
                    if (cru.isEmpty()) continue;
                    var receita = level.recipeAccess().getRecipeFor(
                            net.minecraft.world.item.crafting.RecipeType.SMELTING,
                            new SingleRecipeInput(cru), level);
                    if (receita.isEmpty()) continue;
                    ItemStack cozido = receita.get().value().assemble(new SingleRecipeInput(cru));
                    if (cozido.isEmpty() || cozido.get(net.minecraft.core.component.DataComponents.FOOD) == null) {
                        continue;
                    }

                    int quantos = cru.getCount();
                    int queimados = 0;
                    for (int i = 0; i < quantos; i++) {
                        if (level.getRandom().nextDouble() < this.burnChance) queimados++;
                    }
                    largado.discard();

                    if (quantos - queimados > 0) {
                        solta(level, onde, cozido.copyWithCount(quantos - queimados));
                    }
                    if (queimados > 0) {
                        solta(level, onde, new ItemStack(Items.CHARCOAL, queimados));
                    }
                    cozidos++;
                }

                if (cozidos == 0) return RiteStep.Result.ABORTED_REFUND;
                level.sendParticles(ParticleTypes.FLAME, onde.getX() + 0.5, onde.getY() + 1.0,
                        onde.getZ() + 0.5, 48, 1.5, 1.0, 1.5, 0.02);
                level.playSound(null, onde, SoundEvents.GHAST_SHOOT, SoundSource.BLOCKS, 0.8f, 1.2f);
                return RiteStep.Result.COMPLETED;
            });
        }

        /** O que sai fica parado no meio do círculo, como no original. */
        private static void solta(ServerLevel level, BlockPos onde, ItemStack coisa) {
            var caiu = new ItemEntity(level, onde.getX() + 0.5, onde.getY() + 0.05, onde.getZ() + 0.5, coisa);
            caiu.setDeltaMovement(0.0, 0.0, 0.0);
            level.addFreshEntity(caiu);
        }
    }

    /**
     * O Rito de Erguer a Terra: o {@code RiteRaiseColumn} do Witchery.
     *
     * <p>De cinco em cinco batidas, um cilindro de terra <b>sobe uma casa</b> — bloco por bloco, de cima para
     * baixo, com quem estiver em cima a subir com ele. Corre oito vezes, e no fim fica uma coluna.
     *
     * <p>A borda sai <b>desigual de propósito</b>: um bloco de beira em cada sete fica para trás, e é isso que
     * faz a coluna parecer arrancada do chão e não cortada à régua.
     *
     * @param radius o raio, que cresce com o coven
     * @param height quantas casas ela sobe
     */
    public record RaiseEarth(int radius, int height) implements Rite {
        @Override
        public List<RiteStep> steps(int coven) {
            return List.of((level, onde, ticks, rito) -> {
                if (ticks % 5L != 0L) return RiteStep.Result.STARTING;

                int fase = rito.advance();
                if (fase == 1) {
                    level.sendParticles(ParticleTypes.PORTAL, onde.getX() + 0.5, onde.getY() + 1.0,
                            onde.getZ() + 0.5, 64, 1.0, 1.0, 1.0, 0.5);
                    level.playSound(null, onde, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 0.8f);
                }

                int raio = this.radius + coven * 2;
                int ar = this.radius * 2;
                for (int y = onde.getY() + ar; y >= onde.getY() - this.height; y--) {
                    circle(level, onde.getX(), y, onde.getZ(), raio, y == onde.getY() - 1);
                }

                AABB roda = new AABB(onde.getX() - raio, onde.getY(), onde.getZ() - raio,
                        onde.getX() + raio, onde.getY() + ar, onde.getZ() + raio);
                for (var quem : level.getEntities((net.minecraft.world.entity.Entity) null, roda, e -> true)) {
                    double dx = quem.getX() - (onde.getX() + 0.5);
                    double dz = quem.getZ() - (onde.getZ() + 0.5);
                    if (dx * dx + dz * dz > (double) raio * raio) continue;
                    quem.teleportTo(quem.getX(), quem.getY() + 1.0, quem.getZ());
                }
                return rito.stage() < this.height - 1 ? RiteStep.Result.UPKEEP : RiteStep.Result.COMPLETED;
            });
        }

        /** Um círculo cheio de blocos que sobem uma casa: o rasterizador do original, ponto por ponto. */
        private static void circle(ServerLevel level, int x0, int y, int z0, int raio, boolean topo) {
            int x = raio;
            int z = 0;
            int erro = 1 - x;
            while (x >= z) {
                line(level, -x + x0, x + x0, y, z + z0, topo, raio, z0);
                line(level, -z + x0, z + x0, y, x + z0, topo, raio, z0);
                line(level, -x + x0, x + x0, y, -z + z0, topo, raio, z0);
                line(level, -z + x0, z + x0, y, -x + z0, topo, raio, z0);
                z++;
                if (erro < 0) {
                    erro += 2 * z + 1;
                } else {
                    x--;
                    erro += 2 * (z - x + 1);
                }
            }
        }

        private static void line(ServerLevel level, int x1, int x2, int y, int z, boolean topo, int raio,
                                 int meioZ) {
            for (int x = x1; x <= x2; x++) {
                BlockPos casa = new BlockPos(x, y, z);
                var feitio = level.getBlockState(casa);
                if (feitio.isAir() || feitio.hasBlockEntity()) continue;
                if (feitio.getDestroySpeed(level, casa) < 0.0f) continue;
                if (level.getBlockState(casa.above()).getDestroySpeed(level, casa.above()) < 0.0f) continue;

                boolean beira = meioZ + raio == z || meioZ - raio == z;
                boolean fica = !topo && (beira || x == x1 || x == x2) && level.getRandom().nextInt(7) == 0;
                if (fica) continue;

                level.setBlock(casa.above(), feitio, Block.UPDATE_CLIENTS);
                level.setBlock(casa, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),
                        Block.UPDATE_CLIENTS);
            }
        }
    }

    /**
     * O Rito de Partir a Terra: o {@code RitePartEarth} do Witchery.
     *
     * <p>Ele abre uma <b>vala torta</b> a partir do círculo. Primeiro traça um caminho de sessenta passos, que
     * anda quase sempre em frente e de vez em quando dobra; depois, batida a batida, cava um buraco redondo em
     * cada ponto dele, de profundidade que varia. O que fica é uma rachadura no chão, e não um túnel de régua.
     *
     * <p><b>Desvio declarado:</b> no original o caminho sai do relógio de sorte do mundo, e por isso é diferente
     * a cada vez — e perde-se ao desligar o mundo. Aqui ele sai de uma <b>sorte semeada pelo lugar do
     * círculo</b>: é sempre o mesmo caminho para o mesmo círculo, e é isso que deixa o rito continuar de onde
     * estava. O que se vê é igual; o que muda é que a mesma pedra dá sempre a mesma rachadura.
     *
     * @param length quantos passos tem o caminho
     * @param width  o raio do buraco de cada passo, que cresce com o coven
     * @param depth  e quão fundo ele vai
     */
    public record PartEarth(int length, int width, int depth) implements Rite {
        /** Quantos passos do caminho ficam para trás do que se cava: o {@code DELAY} do original. */
        public static final int DELAY = 4;

        @Override
        public List<RiteStep> steps(int coven) {
            return List.of((level, onde, ticks, rito) -> {
                if (rito.stage() == 0 && ticks % 20L != 0L) return RiteStep.Result.STARTING;

                int largura = this.width + (coven > 2 ? 2 : 0);
                List<BlockPos> caminho = path(onde, this.length);
                int fase = rito.advance();

                int i = fase + DELAY;
                if (i >= caminho.size()) return RiteStep.Result.COMPLETED;

                var sorte = net.minecraft.util.RandomSource.create(onde.asLong() + fase);
                BlockPos ponto = caminho.get(i);
                dig(level, ponto, largura + (sorte.nextInt(3) == 0 ? 1 : 0),
                        this.depth - 2 + sorte.nextInt(5));
                return fase >= caminho.size() - DELAY - 1 ? RiteStep.Result.COMPLETED : RiteStep.Result.UPKEEP;
            });
        }

        /** O caminho torto, semeado pelo lugar do círculo: o {@code move} do original, oito rumos. */
        public static List<BlockPos> path(BlockPos meio, int passos) {
            var sorte = net.minecraft.util.RandomSource.create(meio.asLong());
            List<BlockPos> caminho = new ArrayList<>();
            BlockPos cursor = meio.below();
            caminho.add(cursor);

            int rumo = 0;
            for (int l = 0; l < passos - 1; l++) {
                int chance = Math.max(20 - l / 2, 6);
                int tirou = sorte.nextInt(chance);
                if (tirou == 0) rumo = (rumo + 1) & 7;
                else if (tirou == 1) rumo = (rumo + 7) & 7;
                cursor = cursor.offset(RUMOS[rumo][0], 0, RUMOS[rumo][1]);
                caminho.add(cursor);
            }
            return caminho;
        }

        /** Os oito rumos, do norte para a direita. */
        private static final int[][] RUMOS = {
                {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}};

        /** Um buraco redondo e fundo naquele ponto. */
        private static void dig(ServerLevel level, BlockPos meio, int raio, int fundo) {
            int x = raio;
            int z = 0;
            int erro = 1 - x;
            while (x >= z) {
                digLine(level, -x + meio.getX(), x + meio.getX(), z + meio.getZ(), meio.getY(), fundo);
                digLine(level, -z + meio.getX(), z + meio.getX(), x + meio.getZ(), meio.getY(), fundo);
                digLine(level, -x + meio.getX(), x + meio.getX(), -z + meio.getZ(), meio.getY(), fundo);
                digLine(level, -z + meio.getX(), z + meio.getX(), -x + meio.getZ(), meio.getY(), fundo);
                z++;
                if (erro < 0) {
                    erro += 2 * z + 1;
                } else {
                    x--;
                    erro += 2 * (z - x + 1);
                }
            }
        }

        private static void digLine(ServerLevel level, int x1, int x2, int z, int y, int fundo) {
            for (int x = x1; x <= x2; x++) {
                for (int d = 0; d < fundo; d++) {
                    BlockPos casa = new BlockPos(x, y - d, z);
                    var feitio = level.getBlockState(casa);
                    if (feitio.isAir() || feitio.hasBlockEntity()) continue;
                    if (feitio.getDestroySpeed(level, casa) < 0.0f) continue;
                    level.setBlock(casa, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),
                            Block.UPDATE_CLIENTS);
                }
            }
        }
    }

    /**
     * Dar crédito de manifestação a quem começou o rito: o {@code RiteSetNBT} do original sobre o
     * {@code WITCManifestDuration}.
     *
     * <p>Ele não abre porta nenhuma e não faz nada de visível: o que sai dele é o <b>direito</b> de atravessar
     * um Portal do Espírito e voltar ao mundo de cá em fantasma, por cento e cinquenta segundos.
     */
    public record Manifest() implements Rite {
        @Override
        public List<RiteStep> steps(int coven) {
            return List.of((level, onde, ticks, rito) -> {
                if (rito.starter(level) instanceof net.minecraft.server.level.ServerPlayer quem) {
                    net.thaumcraft.occulta.spirit.SpiritManifest.grant(quem,
                            net.thaumcraft.occulta.spirit.SpiritManifest.GRANTED);
                    quem.sendSystemMessage(net.minecraft.network.chat.Component
                            .translatable("tc.rite.manifest.granted",
                                    net.thaumcraft.occulta.spirit.SpiritManifest.GRANTED)
                            .withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
                }
                level.sendParticles(ParticleTypes.SOUL, onde.getX() + 0.5, onde.getY() + 1.0,
                        onde.getZ() + 0.5, 64, 0.8, 1.0, 0.8, 0.05);
                level.playSound(null, onde, SoundEvents.SOUL_ESCAPE.value(), SoundSource.BLOCKS, 1.0f, 0.7f);
                return RiteStep.Result.COMPLETED;
            });
        }
    }

    /**
     * Fazer aparecer uma coisa: o {@code RiteSummonItem} do original.
     *
     * <p>É o rito mais simples que há: o que se ofereceu some, e no meio do círculo fica <b>aquilo</b>. É assim
     * que se faz um Espelho, que não sai de bancada nenhuma.
     */
    public record SummonItem(java.util.function.Supplier<ItemStack> what) implements Rite {
        @Override
        public List<RiteStep> steps(int coven) {
            return List.of((level, onde, ticks, rito) -> {
                Block.popResource(level, onde.above(), this.what.get());
                level.sendParticles(ParticleTypes.PORTAL, onde.getX() + 0.5, onde.getY() + 1.0,
                        onde.getZ() + 0.5, 48, 0.5, 1.0, 0.5, 0.1);
                level.playSound(null, onde, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0f, 0.8f);
                return RiteStep.Result.COMPLETED;
            });
        }
    }

    /** A lista da primeira leva, posta na tabela. */
    public static void register() {
        RiteRegistry.register("tc.rite.cook", new Cook(5.0f, 0.08),
                new Sacrifice.Both(
                        new Sacrifice.Items(Items.BLAZE_ROD, net.thaumcraft.occulta.OccultaItems.WOOD_ASH,
                                Items.COAL),
                        new Sacrifice.Power(1000.0f, 20)),
                RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE, new RiteRegistry.Ring(0, 0, 16));

        RiteRegistry.register("tc.rite.fertility", new Fertility(50, 15),
                new Sacrifice.Both(
                        new Sacrifice.Items(Items.BONE_MEAL,
                                net.thaumcraft.occulta.OccultaItems.HINT_OF_REBIRTH,
                                net.thaumcraft.occulta.OccultaItems.DIAMOND_VAPOUR,
                                net.thaumcraft.occulta.OccultaItems.QUICKLIME,
                                net.thaumcraft.occulta.OccultaItems.GYPSUM,
                                net.thaumcraft.occulta.OccultaItems.MUTANDIS),
                        new Sacrifice.Power(3000.0f, 20)),
                new RiteRegistry.Ring(16, 0, 0), RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE);

        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.eclipse", new Eclipse(),
                new Sacrifice.Both(
                        new Sacrifice.Items(Items.STONE_SWORD, net.thaumcraft.occulta.OccultaItems.QUICKLIME),
                        new Sacrifice.Power(3000.0f, 20)),
                new RiteRegistry.Ring(16, 0, 0), RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE,
                java.util.EnumSet.of(RiteRegistry.When.DAY)));

        // os ritos do tempo e da terra
        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.storm", new Storm(0, 3, 8),
                new Sacrifice.Both(
                        new Sacrifice.Items(Items.WOODEN_SWORD, net.thaumcraft.occulta.OccultaItems.WOOD_ASH),
                        new Sacrifice.Power(1000.0f, 20)),
                new RiteRegistry.Ring(16, 0, 0), RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE,
                java.util.EnumSet.noneOf(RiteRegistry.When.class)));

        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.cookfood", new CookFood(5.0, 0.08),
                new Sacrifice.Both(
                        new Sacrifice.Items(Items.BLAZE_ROD, net.thaumcraft.occulta.OccultaItems.WOOD_ASH,
                                Items.COAL),
                        new Sacrifice.Power(1000.0f, 20)),
                RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE, new RiteRegistry.Ring(16, 0, 0),
                java.util.EnumSet.noneOf(RiteRegistry.When.class)));

        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.raiseearth", new RaiseEarth(4, 8),
                new Sacrifice.Items(net.thaumcraft.occulta.OccultaItems.BREW_OF_SPROUTING,
                        Items.CACTUS, Items.GUNPOWDER),
                new RiteRegistry.Ring(16, 0, 0), RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE,
                java.util.EnumSet.noneOf(RiteRegistry.When.class)));

        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.partearth", new PartEarth(60, 1, 10),
                new Sacrifice.Items(net.thaumcraft.occulta.OccultaItems.BREW_OF_EROSION),
                new RiteRegistry.Ring(16, 0, 0), RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE,
                java.util.EnumSet.noneOf(RiteRegistry.When.class)));

        // o Rito da Manifestação, que não abre porta nenhuma: dá crédito de corpo no mundo de cá
        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.manifest", new Manifest(),
                new Sacrifice.Both(
                        new Sacrifice.Items(net.thaumcraft.occulta.OccultaItems.SPECTRAL_DUST,
                                net.thaumcraft.occulta.OccultaItems.MELLIFLUOUS_HUNGER,
                                net.thaumcraft.occulta.OccultaItems.NECROTIC_STONE,
                                Items.GOLDEN_PICKAXE, net.thaumcraft.occulta.OccultaItems.ARTHANA,
                                Items.GUNPOWDER),
                        new Sacrifice.Power(5000.0f, 20)),
                new RiteRegistry.Ring(16, 0, 0), RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE,
                java.util.EnumSet.noneOf(RiteRegistry.When.class)));

        // o Rito de Necromancia, que faz a Pedra Necrótica — e é dele que o Braseiro nasce
        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.necrostone",
                new SummonItem(() -> new ItemStack(net.thaumcraft.occulta.OccultaItems.NECROTIC_STONE)),
                new Sacrifice.Both(
                        new Sacrifice.Items(net.thaumcraft.occulta.OccultaItems.ATTUNED_STONE,
                                Items.BONE, Items.ROTTEN_FLESH, net.thaumcraft.occulta.OccultaItems.WOOD_ASH,
                                Items.IRON_SWORD, net.thaumcraft.occulta.OccultaItems.SPECTRAL_DUST),
                        new Sacrifice.Power(1000.0f, 20)),
                new RiteRegistry.Ring(16, 0, 0), RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE,
                java.util.EnumSet.of(RiteRegistry.When.NIGHT)));

        // o Rito de Infusão, que prende um demônio num espelho — e é de onde todo espelho vem
        RiteRegistry.register("tc.rite.mirror",
                new SummonItem(() -> new ItemStack(net.thaumcraft.occulta.OccultaItems.WITCH_MIRROR)),
                new Sacrifice.Both(
                        new Sacrifice.Items(net.thaumcraft.occulta.OccultaItems.BREW_OF_HOLLOW_TEARS,
                                Items.GOLD_INGOT, Items.GLASS_PANE),
                        new Sacrifice.Power(2000.0f, 20)),
                RiteRegistry.Ring.NONE, new RiteRegistry.Ring(28, 0, 0), RiteRegistry.Ring.NONE);
    }

    /** As coisas que um rito pede, para o livro. */
    public static List<ItemStack> shown(RiteRegistry.Entry rito) {
        return new ArrayList<>(rito.sacrifice().shown());
    }
}
