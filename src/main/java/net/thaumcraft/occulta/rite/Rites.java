package net.thaumcraft.occulta.rite;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

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

        /**
         * O que acontece em cada bloco do anel.
         *
         * <p>O <b>mordeFundo</b> é o {@code enhanced} do original: quem tem a <b>maestria da maldição</b> — a
         * do familiar gato — faz os ritos de maldição morderem mais fundo. Vale só para os que são maldição.
         */
        protected abstract void onBlock(ServerLevel level, BlockPos onde, int raio, Player quem,
                                        boolean mordeFundo);

        /** E o que acontece a cada passo, no anel inteiro; devolvendo falso, o rito desiste. */
        protected boolean onRing(ServerLevel level, BlockPos meio, int raio, Player quem,
                                 boolean mordeFundo) {
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
                boolean mordeFundo = this.rite.curse
                        && net.thaumcraft.occulta.familiar.Familiars.temMaestriaDeMaldicao(quem);
                if (raio <= teto) {
                    if (!this.rite.onRing(level, onde, raio, quem, mordeFundo)) return Result.ABORTED;
                    ring(level, onde, raio, this.rite, quem, mordeFundo);
                }
                boolean cheio = raio >= teto;
                return this.stage <= 250 && !this.rite.done(level, onde, raio, cheio, ticks)
                        ? Result.UPKEEP : Result.COMPLETED;
            }
        }

        /** O anel riscado, e o que ele faz ao primeiro chão que houver em cada ponto. */
        private static void ring(ServerLevel level, BlockPos meio, int raio, Expanding rite, Player quem,
                                 boolean mordeFundo) {
            int x = raio;
            int z = 0;
            int erro = 1 - x;
            while (x >= z) {
                int[][] pontos = {{x, z}, {z, x}, {-x, z}, {-z, x}, {-x, -z}, {-z, -x}, {x, -z}, {z, -x}};
                for (int[] p : pontos) pixel(level, meio.offset(p[0], 0, p[1]), raio, rite, quem, mordeFundo);
                z++;
                if (erro < 0) {
                    erro += 2 * z + 1;
                } else {
                    x--;
                    erro += 2 * (z - x + 1);
                }
            }
        }

        /**
         * O {@code drawPixel}: procura o chão e faz nele o que o rito manda.
         *
         * <p><b>Ele procura para os dois lados.</b> Sobe até {@code height} e desce o mesmo tanto, parando no
         * primeiro sólido com ar em cima — e é isso que faz o anel acompanhar a encosta em vez de passar por
         * dentro dela. Esta metade de baixo <b>faltava</b> neste porte desde a fatia dos círculos: o anel só
         * subia, e descendo um barranco ele simplesmente não tocava no chão.
         */
        private static void pixel(ServerLevel level, BlockPos onde, int raio, Expanding rite, Player quem,
                                  boolean mordeFundo) {
            for (int i = 0; i < rite.height; i++) {
                if (chão(level, onde.above(i), raio, rite, quem, mordeFundo)) return;
                if (i > 0 && chão(level, onde.below(i), raio, rite, quem, mordeFundo)) return;
            }
        }

        /** Uma casa: se tem chão aqui, faz-se nela o que o rito manda. */
        private static boolean chão(ServerLevel level, BlockPos aqui, int raio, Expanding rite, Player quem,
                                    boolean mordeFundo) {
            if (level.getBlockState(aqui).isAir() || !level.isEmptyBlock(aqui.above())) return false;
            level.sendParticles(rite.curse ? ParticleTypes.WITCH : ParticleTypes.HAPPY_VILLAGER,
                    aqui.getX() + 0.5, aqui.getY() + 1.0, aqui.getZ() + 0.5, 2, 0.3, 0.3, 0.3, 0.0);
            rite.onBlock(level, aqui, raio, quem, mordeFundo);
            return true;
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
        protected void onBlock(ServerLevel level, BlockPos onde, int raio, Player quem,
                               boolean mordeFundo) {
            net.minecraft.world.item.BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL), level, onde);
        }

        @Override
        protected boolean onRing(ServerLevel level, BlockPos meio, int raio, Player quem,
                                 boolean mordeFundo) {
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

    /**
     * O Rito do Vulcão: o {@code RiteRaiseVolcano} do Witchery.
     *
     * <p>É o maior estrago que o ofício faz, e não se faz em qualquer lugar: o círculo tem de ter <b>lava por
     * baixo</b> — uma poça de verdade, com lava em volta dela, e não um pingo. Não achando, o rito desiste e
     * devolve o que se ofereceu.
     *
     * <p>Achando, ele levanta um <b>cone</b> de quinze em quinze batidas, camada a camada, com a borda de baixo
     * salpicada de relva; quem estiver em cima sobe com ele. Erguido o cone, a lava <b>sobe por dentro</b> até
     * o alto e transborda — e o cume se rompe por um dos lados, a esmo. No fim, a coluna de lava que veio de
     * baixo é <b>drenada</b>, e o que fica é um monte com uma cratera.
     *
     * @param radius o raio da base, que cresce dois por bruxa do coven
     * @param height e a altura, que cresce quatro
     */
    public record Volcano(int radius, int height) implements Rite {
        /** Quantas batidas entre uma camada e a seguinte. */
        public static final int EVERY = 15;
        /** Quantas casas para baixo ele procura lava. */
        public static final int LOOK_DOWN = 256;

        @Override
        public List<RiteStep> steps(int coven) {
            return List.of((level, onde, ticks, rito) -> {
                if (ticks % EVERY != 0L) return RiteStep.Result.STARTING;

                int fase = rito.advance();
                if (fase == 1 && !lavaBelow(level, onde)) {
                    var quem = rito.starter(level);
                    if (quem != null) {
                        quem.sendSystemMessage(net.minecraft.network.chat.Component
                                .translatable("tc.rite.missinglava"));
                    }
                    level.playSound(null, onde, SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.BLOCKS,
                            1.0f, 0.7f);
                    return RiteStep.Result.ABORTED_REFUND;
                }
                if (fase == 1) {
                    level.sendParticles(ParticleTypes.PORTAL, onde.getX() + 0.5, onde.getY() + 1.0,
                            onde.getZ() + 0.5, 64, 1.0, 1.0, 1.0, 0.5);
                    level.playSound(null, onde, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 0.6f);
                }

                int alto = this.height + 4 * coven;
                float raio = this.radius + 2 * coven;

                if (fase <= alto) {
                    cone(level, onde, fase, alto, raio);
                    return RiteStep.Result.UPKEEP;
                }
                if (fase >= alto * 2) {
                    drain(level, onde);
                    return RiteStep.Result.COMPLETED;
                }
                erupt(level, onde, fase, alto, raio);
                return RiteStep.Result.UPKEEP;
            });
        }

        /** Se há uma poça de lava de verdade por baixo do círculo. */
        public static boolean lavaBelow(ServerLevel level, BlockPos onde) {
            for (int y = onde.getY(); y > level.getMinY() && onde.getY() - y < LOOK_DOWN; y--) {
                BlockPos casa = new BlockPos(onde.getX(), y, onde.getZ());
                var feitio = level.getBlockState(casa);
                if (feitio.is(net.minecraft.world.level.block.Blocks.BEDROCK)) return false;
                if (feitio.is(net.minecraft.world.level.block.Blocks.LAVA) && lavaAround(level, casa, 2)) {
                    return true;
                }
            }
            return false;
        }

        /** A conta do {@code surroundedByBlocks}, casa por casa e na mesma ordem. */
        private static boolean lavaAround(ServerLevel level, BlockPos casa, int quantas) {
            int conta = 0;
            BlockPos[] olhar = {
                    casa.below(), casa.west(), casa.east().below(),
                    casa.north(), casa.south(), casa.above().south()};
            for (BlockPos vizinha : olhar) {
                if (level.getBlockState(vizinha).is(net.minecraft.world.level.block.Blocks.LAVA)) conta++;
            }
            return conta >= quantas;
        }

        /** Uma camada do cone, com as de baixo já postas: o cone cresce inteiro a cada batida. */
        private static void cone(ServerLevel level, BlockPos onde, int fase, int alto, float raio) {
            for (int y = 1; y <= fase; y++) {
                float r = raio - (alto - fase - 1 + y) * raio / alto;
                circle(level, onde.getX(), y + onde.getY() - 1, onde.getZ(),
                        Math.max((int) Math.ceil(r), 1), y, true);
                if (fase == alto) {
                    int abaixo = onde.getY() - 1;
                    for (int corta = 0; abaixo > onde.getY() - 5; corta++) {
                        circle(level, onde.getX(), abaixo, onde.getZ(),
                                Math.max((int) raio - corta, 2), y, false);
                        abaixo--;
                    }
                }
                sobe(level, onde, y, r);
            }
        }

        /** Quem ficou dentro de pedra sobe uma casa, como no Erguer a Terra. */
        private static void sobe(ServerLevel level, BlockPos onde, int y, float r) {
            AABB roda = new AABB(onde.getX() - r, y + onde.getY(), onde.getZ() - r,
                    onde.getX() + r, y + onde.getY() + 1, onde.getZ() + r);
            for (var quem : level.getEntities((net.minecraft.world.entity.Entity) null, roda, e -> true)) {
                double dx = quem.getX() - onde.getX();
                double dz = quem.getZ() - onde.getZ();
                if (dx * dx + dz * dz > (double) r * r) continue;
                if (!level.getBlockState(quem.blockPosition()).isSolidRender()) continue;
                quem.teleportTo(quem.getX(), quem.getY() + 1.0, quem.getZ());
            }
        }

        /** A lava a subir por dentro, e o cume a romper-se. */
        private static void erupt(ServerLevel level, BlockPos onde, int fase, int alto, float raio) {
            var pedra = net.minecraft.world.level.block.Blocks.STONE.defaultBlockState();
            var lava = net.minecraft.world.level.block.Blocks.LAVA.defaultBlockState();
            var corrente = lava.setValue(net.minecraft.world.level.block.LiquidBlock.LEVEL, 1);

            if (fase == alto * 2 - 1) {
                level.setBlock(onde.above(fase - alto), corrente, Block.UPDATE_ALL);
                level.setBlock(onde.above(), lava, Block.UPDATE_ALL);
                if (raio >= 16.0f) {
                    if (level.getRandom().nextInt(4) == 0) {
                        level.setBlock(onde.above(1 + fase - alto), corrente, Block.UPDATE_ALL);
                    }
                    return;
                }
                BlockPos cume = onde.above(alto - 1);
                BlockPos rompe = switch (level.getRandom().nextInt(8)) {
                    case 0 -> cume.east();
                    case 1 -> cume.south();
                    case 2 -> cume.west();
                    case 3 -> cume.north();
                    default -> null;
                };
                if (rompe != null) {
                    level.setBlock(rompe, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),
                            Block.UPDATE_ALL);
                }
                return;
            }
            level.setBlock(onde.above(), pedra, Block.UPDATE_ALL);
            level.setBlock(onde.above(fase - alto), lava, Block.UPDATE_ALL);
        }

        /** E a coluna que veio de baixo é fechada: o que fica é um monte, e não um cano de lava. */
        private static void drain(ServerLevel level, BlockPos onde) {
            for (int y = onde.getY(); y > level.getMinY(); y--) {
                BlockPos casa = new BlockPos(onde.getX(), y, onde.getZ());
                var feitio = level.getBlockState(casa);
                if (feitio.is(net.minecraft.world.level.block.Blocks.BEDROCK)) return;
                if (feitio.is(net.minecraft.world.level.block.Blocks.LAVA)) {
                    while (level.getBlockState(casa).is(net.minecraft.world.level.block.Blocks.LAVA)) {
                        apaga(level, casa);
                        apaga(level, casa.east());
                        apaga(level, casa.west());
                        apaga(level, casa.south());
                        apaga(level, casa.north());
                        casa = casa.below();
                    }
                    return;
                }
                level.setBlock(casa, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),
                        Block.UPDATE_ALL);
            }
        }

        private static void apaga(ServerLevel level, BlockPos casa) {
            if (!level.getBlockState(casa).is(net.minecraft.world.level.block.Blocks.LAVA)) return;
            level.setBlock(casa, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),
                    Block.UPDATE_ALL);
        }

        /** Um círculo cheio de pedra, com a beira de baixo salpicada de relva. */
        private static void circle(ServerLevel level, int x0, int y, int z0, int raio, int altura,
                                   boolean troca) {
            int x = raio;
            int z = 0;
            int erro = 1 - x;
            while (x >= z) {
                line(level, -x + x0, x + x0, z + z0, y, x0, z0, raio, altura, troca);
                line(level, -z + x0, z + x0, x + z0, y, x0, z0, raio, altura, troca);
                line(level, -x + x0, x + x0, -z + z0, y, x0, z0, raio, altura, troca);
                line(level, -z + x0, z + x0, -x + z0, y, x0, z0, raio, altura, troca);
                z++;
                if (erro < 0) {
                    erro += 2 * z + 1;
                } else {
                    x--;
                    erro += 2 * (z - x + 1);
                }
            }
        }

        private static void line(ServerLevel level, int x1, int x2, int z, int y, int meioX, int meioZ,
                                 int raio, int altura, boolean troca) {
            int de = raio > 1 && level.getRandom().nextInt(5) == 0 ? x1 + 1 : x1;
            int até = raio > 1 && level.getRandom().nextInt(5) == 0 ? x2 - 1 : x2;
            boolean beiraZ = meioZ + raio == z || meioZ - raio == z;

            for (int x = de; x <= até; x++) {
                if (x == meioX && z == meioZ) continue;
                boolean baixa = (x == de || x == até || beiraZ) && altura < 3;
                pixel(level, x, z, y, baixa, troca);
            }
        }

        private static void pixel(ServerLevel level, int x, int z, int y, boolean baixa, boolean troca) {
            BlockPos casa = new BlockPos(x, y, z);
            var feitio = level.getBlockState(casa);
            boolean vago = feitio.isAir();
            if (!vago && !(troca && feitio.getDestroySpeed(level, casa) >= 0.0f)) return;

            var põe = baixa && level.getRandom().nextInt(5) != 0
                    ? net.minecraft.world.level.block.Blocks.GRASS_BLOCK.defaultBlockState()
                    : net.minecraft.world.level.block.Blocks.STONE.defaultBlockState();
            level.setBlock(casa, põe, Block.UPDATE_CLIENTS);
        }
    }

    /**
     * Os ritos de proteção: a {@code RiteProtectionCircleBarrier} sobre a {@code RiteProtectionCircle} do
     * Witchery.
     *
     * <p>Eles não acontecem e acabam: <b>sustentam-se</b>. De vinte em vinte batidas o rito volta a desenhar uma
     * <b>cúpula de barreira</b> em volta do círculo — chão, parede cilíndrica e teto — e cada casa dela dura
     * trinta batidas. Parado o rito, a parede se desfaz sozinha em segundo e meio.
     *
     * <p>E ele <b>paga por batida</b>: sem um Altar por perto com poder de sobra, o rito morre. É o que faz de
     * uma barreira uma coisa que se mantém, e não uma coisa que se faz.
     *
     * @param radius         o raio da cúpula
     * @param height         a altura dela
     * @param upkeep         quanto poder ela come por batida
     * @param blocksPlayers  se trava gente também, e não só o que não é gente
     * @param ticksToLive    quantas batidas ela vive, ou zero para viver enquanto houver poder
     */
    public record Barrier(int radius, int height, float upkeep, boolean blocksPlayers, int ticksToLive)
            implements Rite {
        /** De quantas em quantas batidas ela se redesenha. */
        public static final int EVERY = 20;

        @Override
        public List<RiteStep> steps(int coven) {
            return List.of((level, onde, ticks, rito) -> {
                if (rito.stage() == 0) {
                    if (ticks % EVERY != 0L) return RiteStep.Result.STARTING;
                    rito.advance();
                    level.playSound(null, onde, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 0.9f);
                }

                if (this.upkeep > 0.0f) {
                    var altar = net.thaumcraft.occulta.PowerSources.closest(level, onde);
                    if (altar == null || !altar.consume(this.upkeep)) return RiteStep.Result.ABORTED;
                }

                if (this.ticksToLive > 0 && ticks % EVERY == 0L
                        && rito.advance() >= this.ticksToLive) {
                    return RiteStep.Result.COMPLETED;
                }

                if (ticks % EVERY == 0L) {
                    var dono = rito.starter();
                    disc(level, onde.below(), this.radius, dono);
                    cylinder(level, onde, this.radius, dono);
                    disc(level, onde.above(this.height), this.radius, dono);
                }
                return RiteStep.Result.UPKEEP;
            });
        }

        /** Uma casa de barreira, se ali couber. */
        private void put(ServerLevel level, BlockPos casa, java.util.UUID dono) {
            var feitio = level.getBlockState(casa);
            if (!feitio.isAir() && !feitio.canBeReplaced()
                    && !feitio.is(net.thaumcraft.occulta.OccultaBlocks.BARRIER)) {
                return;
            }
            net.thaumcraft.occulta.BarrierBlock.put(level, casa,
                    net.thaumcraft.occulta.BarrierBlock.TICKS_TO_LIVE, this.blocksPlayers, dono);
        }

        /** A parede: a coluna de cada ponto do círculo. */
        private void cylinder(ServerLevel level, BlockPos meio, int raio, java.util.UUID dono) {
            int x = raio;
            int z = 0;
            int erro = 1 - x;
            while (x >= z) {
                column(level, meio, x, z, dono);
                column(level, meio, z, x, dono);
                column(level, meio, -x, z, dono);
                column(level, meio, -z, x, dono);
                column(level, meio, -x, -z, dono);
                column(level, meio, -z, -x, dono);
                column(level, meio, x, -z, dono);
                column(level, meio, z, -x, dono);
                z++;
                if (erro < 0) {
                    erro += 2 * z + 1;
                } else {
                    x--;
                    erro += 2 * (z - x + 1);
                }
            }
        }

        private void column(ServerLevel level, BlockPos meio, int dx, int dz, java.util.UUID dono) {
            for (int dy = 0; dy < this.height; dy++) {
                put(level, meio.offset(dx, dy, dz), dono);
            }
        }

        /** E o chão e o teto: um disco cheio. */
        private void disc(ServerLevel level, BlockPos meio, int raio, java.util.UUID dono) {
            int x = raio;
            int z = 0;
            int erro = 1 - x;
            while (x >= z) {
                row(level, meio, -x, x, z, dono);
                row(level, meio, -z, z, x, dono);
                row(level, meio, -x, x, -z, dono);
                row(level, meio, -z, z, -x, dono);
                z++;
                if (erro < 0) {
                    erro += 2 * z + 1;
                } else {
                    x--;
                    erro += 2 * (z - x + 1);
                }
            }
        }

        private void row(ServerLevel level, BlockPos meio, int de, int até, int dz, java.util.UUID dono) {
            for (int dx = de; dx <= até; dx++) {
                put(level, meio.offset(dx, 0, dz), dono);
            }
        }
    }

    // ------------------------------------------------------------------ as maldições que se abrem em roda

    /**
     * A Maldição da Cegueira: a {@code RiteBlindness} do Witchery.
     *
     * <p>É a primeira das maldições que <b>se abrem em roda</b> deste porte, e usa a mesma base que a
     * Fertilidade: um anel que cresce de cinco em cinco batidas, do círculo até oitenta casas, fazendo o que
     * tem a fazer a quem estiver <b>naquele anel</b> — nem no que já ficou para trás, nem no que ainda vem.
     *
     * <p>Dois minutos de escuro em cada um, e quem já estiver cego não leva mais.
     *
     * <p>Ela pára de vez se alguém no anel trouxer uma <b>boneca de proteção contra vodu</b>: a boneca se gasta
     * e o rito morre. É a única defesa que há contra ela, e é a que o original dá.
     *
     * <p><b>E quem tem gato vê o escuro durar mais:</b> dois minutos viram <b>cinco</b>. É o
     * {@code hasActiveCurseMasteryFamiliar} do original, e é a maestria que o gato dá. Esteve escrito aqui como
     * buraco enquanto os familiares não existiam; agora existe.
     *
     * <p><b>Do original fica de fora, declarado:</b> o <b>Caçador de Bruxas</b>, que lá é avisado de que alguém
     * fez magia negra e vem atrás de quem a fez. Esse não está portado.
     */
    public static class CurseOfBlindness extends Expanding {
        /** Quanto tempo o escuro dura: os dois minutos do original. */
        public static final int BLIND_TICKS = 2 * 1200;

        /** E os cinco de quem tem gato por familiar. */
        public static final int BLIND_TICKS_WITH_CAT = 5 * 1200;

        public CurseOfBlindness(int maxRadius, int height) {
            super(maxRadius, height, true);
        }

        @Override
        protected void onBlock(ServerLevel level, BlockPos onde, int raio, Player quem,
                               boolean mordeFundo) {
        }

        @Override
        protected boolean onRing(ServerLevel level, BlockPos meio, int raio, Player quem,
                                 boolean mordeFundo) {
            double fora = (double) raio * raio;
            double dentro = Math.max(0, (raio - 1.0) * (raio - 1.0));
            AABB roda = new AABB(meio).inflate(raio, this.height, raio);

            for (LivingEntity vítima : level.getEntitiesOfClass(LivingEntity.class, roda)) {
                double d = vítima.distanceToSqr(meio.getX() + 0.5, meio.getY() + 0.5, meio.getZ() + 0.5);
                if (d <= dentro || d > fora) continue;

                if (vítima instanceof Player gente && gente != quem
                        && net.thaumcraft.occulta.Poppets.spend(level, gente,
                                net.thaumcraft.occulta.PoppetItem.Kind.VOODOO_PROTECTION)) {
                    return false;
                }
                if (vítima.hasEffect(MobEffects.BLINDNESS)) continue;
                int quanto = net.thaumcraft.occulta.familiar.Familiars.temMaestriaDeMaldicao(quem)
                        ? BLIND_TICKS_WITH_CAT : BLIND_TICKS;
                vítima.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, quanto, 0));
            }
            return true;
        }
    }

    // ------------------------------------------------------------------ os ritos do tempo e da terra

    /**
     * O Rito da Tempestade: o {@code RiteWeatherCallStorm} do Witchery.
     *
     * <p>De trinta em trinta batidas cai um raio num anel em volta do círculo — nunca em cima dele, que é o que
     * o raio de dentro serve para garantir. Na <b>quarta</b> vez, o céu se fecha: começa uma trovoada que dura
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
     * a cada vez — e se perde ao desligar o mundo. Aqui ele sai de uma <b>sorte semeada pelo lugar do
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
     * <b>Infundir quem estiver no círculo</b>: o {@code RiteInfusePlayers} do original.
     *
     * <p>É o rito mais violento do mod, e o mais curto de ler: ele faz <b>cem de dano mágico</b> a tudo o
     * que for gente num raio de quatro blocos, e <b>infunde quem sobreviver</b>.
     *
     * <p>Cem. Um jogador de armadura cheia e coração cheio tem vinte. O que salva quem se infunde não é
     * aguentar o golpe — é <b>ter mais vida do que o golpe tira</b>, o que só se consegue com cozimentos,
     * com absorção ou com resistência. A infusão é uma coisa que se sobrevive, e o original nunca fingiu
     * o contrário.
     *
     * <p>E ela corre <b>de segundo em segundo</b>, e não a cada batida: o passo devolve «ainda estou
     * começando» vinte vezes antes de fazer o que faz.
     */
    public record InfusePlayers(net.thaumcraft.occulta.infusion.Infusion qual, int cargas, int alcance)
            implements Rite {
        @Override
        public List<RiteStep> steps(int coven) {
            return List.of((level, onde, ticks, rito) -> {
                if (ticks % 20L != 0L) return RiteStep.Result.STARTING;

                var caixa = new net.minecraft.world.phys.AABB(
                        onde.getX() - this.alcance, onde.getY(), onde.getZ() - this.alcance,
                        onde.getX() + this.alcance, onde.getY() + 1, onde.getZ() + this.alcance);
                for (var quem : level.getEntitiesOfClass(
                        net.minecraft.server.level.ServerPlayer.class, caixa)) {
                    if (quem.distanceToSqr(onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5)
                            > (double) this.alcance * this.alcance) {
                        continue;
                    }
                    quem.hurtServer(level, level.damageSources().magic(),
                            net.thaumcraft.occulta.infusion.Infusions.DANO);
                    if (quem.getHealth() > 0.1f) {
                        net.thaumcraft.occulta.infusion.Infusions.infunde(quem, this.qual, this.cargas);
                    }
                }

                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, onde.getX() + 0.5,
                        onde.getY() + 1.0, onde.getZ() + 0.5, 1, 3.0, 3.0, 3.0, 0.0);
                level.playSound(null, onde, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 1.0f);
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
                ItemStack oquê = this.what.get();
                /*
                 * E há uma exceção, que é a do original: se o que aparece for a <b>Bola de Cristal</b>,
                 * quem começou o rito fica <b>vidente</b>. O rito não dá só o objeto — dá o ofício.
                 */
                if (oquê.is(net.thaumcraft.occulta.OccultaItems.CRYSTAL_BALL)
                        && rito.starter(level) instanceof net.minecraft.server.level.ServerPlayer quem) {
                    net.thaumcraft.occulta.divine.Predictions.ensina(quem);
                }
                Block.popResource(level, onde.above(), oquê);
                level.sendParticles(ParticleTypes.PORTAL, onde.getX() + 0.5, onde.getY() + 1.0,
                        onde.getZ() + 0.5, 48, 0.5, 1.0, 0.5, 0.1);
                level.playSound(null, onde, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0f, 0.8f);
                return RiteStep.Result.COMPLETED;
            });
        }
    }

    /** A lista da primeira leva, posta na tabela. */
    /**
     * A <b>Maldição da Praga</b>: o {@code RiteBlight} do Witchery.
     *
     * <p>Um anel de oitenta blocos de raio que cresce a partir do círculo e <b>mata o que encontra</b>:
     *
     * <ul>
     *   <li>quem está na faixa do anel fica <b>cego</b>, dois minutos;</li>
     *   <li>um aldeão em cada dez vira <b>zumbi</b>, com a mesma cara e o mesmo tamanho;</li>
     *   <li>uma vaca em cada vinte vira <b>cogumelada</b>, e um bicho em cada três <b>morre</b>;</li>
     *   <li>e o chão <b>seca</b>: a relva vai embora, a flor e a plantação viram arbusto morto, a terra arada
     *       vira areia, e o que era relva, terra ou micélio vira areia ou terra pelada.</li>
     * </ul>
     *
     * <p><b>Com o gato é um em cada quatro, e não um em cada cinco.</b> A maestria da maldição não muda o que
     * o rito faz: muda <b>quanto</b> ele faz. É a segunda coisa que o gato destranca neste porte, e a primeira
     * que se vê no chão.
     *
     * <p><b>A faixa é só a do anel.</b> Quem está dentro do círculo, no miolo já percorrido, não é atingido
     * outra vez — o rito compara a distância com o anel de agora e com o de antes. Sem isso, quem ficasse no
     * meio apanhava a praga uma vez por volta.
     */
    public static class Blight extends Expanding {
        /** Quanto tempo a cegueira dura: os dois minutos do original. */
        public static final int CEGUEIRA = 2400;
        /** Um aldeão em cada dez, uma vaca em cada vinte, um bicho em cada três. */
        public static final int ALDEÃO = 10;
        public static final int VACA = 20;
        public static final int BICHO = 3;
        /** E o chão: um em cada cinco, ou em cada quatro com o gato. */
        public static final int CHÃO = 5;
        public static final int CHÃO_COM_GATO = 4;

        public Blight(int radius, int height) {
            super(radius, height, true);
        }

        @Override
        protected boolean onRing(ServerLevel level, BlockPos meio, int raio, Player quem,
                                 boolean mordeFundo) {
            double fora = (double) raio * raio;
            double dentro = Math.max(0, (raio - 1) * (raio - 1));
            double x = meio.getX() + 0.5;
            double y = meio.getY() + 0.5;
            double z = meio.getZ() + 0.5;

            for (Player vítima : level.players()) {
                double quão = vítima.distanceToSqr(x, y, z);
                if (quão <= dentro || quão > fora) continue;
                if (vítima.hasEffect(MobEffects.BLINDNESS)) continue;
                vítima.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, CEGUEIRA, 1));
            }

            var sorte = level.getRandom();
            var caixa = new AABB(meio).inflate(raio + 1.0);
            List<net.minecraft.world.entity.Mob> matar = new ArrayList<>();
            for (var bicho : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, caixa)) {
                double quão = bicho.distanceToSqr(x, y, z);
                if (quão <= dentro || quão > fora) continue;

                if (bicho instanceof net.minecraft.world.entity.npc.villager.Villager aldeão) {
                    if (sorte.nextInt(ALDEÃO) == 0) zumbifica(level, aldeão);
                } else if (bicho.getType() == net.minecraft.world.entity.EntityTypes.COW) {
                    if (sorte.nextInt(VACA) == 0) cogumela(level, bicho);
                    else if (sorte.nextInt(BICHO) == 0) matar.add(bicho);
                } else if (bicho instanceof net.minecraft.world.entity.animal.Animal) {
                    if (sorte.nextInt(BICHO) == 0) matar.add(bicho);
                }
            }
            for (var bicho : matar) {
                bicho.hurtServer(level, level.damageSources().magic(), 20.0f);
            }
            return true;
        }

        /** O aldeão vira zumbi, com a mesma cara e o mesmo tamanho. */
        public static void zumbifica(ServerLevel level, net.minecraft.world.entity.npc.villager.Villager aldeão) {
            var zumbi = net.minecraft.world.entity.EntityTypes.ZOMBIE_VILLAGER.create(level,
                    net.minecraft.world.entity.EntitySpawnReason.CONVERSION);
            if (zumbi == null) return;
            zumbi.copyPosition(aldeão);
            zumbi.finalizeSpawn(level, level.getCurrentDifficultyAt(zumbi.blockPosition()),
                    net.minecraft.world.entity.EntitySpawnReason.CONVERSION, null);
            zumbi.setVillagerData(aldeão.getVillagerData());
            if (aldeão.isBaby()) zumbi.setBaby(true);
            aldeão.discard();
            level.addFreshEntity(zumbi);
            level.levelEvent(null, 1026, zumbi.blockPosition(), 0);
        }

        /** E a vaca vira cogumelada. */
        public static void cogumela(ServerLevel level, net.minecraft.world.entity.Mob vaca) {
            var cogumelada = net.minecraft.world.entity.EntityTypes.MOOSHROOM.create(level,
                    net.minecraft.world.entity.EntitySpawnReason.CONVERSION);
            if (cogumelada == null) return;
            cogumelada.copyPosition(vaca);
            cogumelada.finalizeSpawn(level, level.getCurrentDifficultyAt(cogumelada.blockPosition()),
                    net.minecraft.world.entity.EntitySpawnReason.CONVERSION, null);
            vaca.discard();
            level.addFreshEntity(cogumelada);
            level.levelEvent(null, 1026, cogumelada.blockPosition(), 0);
        }

        @Override
        protected void onBlock(ServerLevel level, BlockPos onde, int raio, Player quem,
                               boolean mordeFundo) {
            var aqui = level.getBlockState(onde);
            var debaixo = level.getBlockState(onde.below());

            if (aqui.is(Blocks.SHORT_GRASS) || aqui.is(Blocks.TALL_GRASS) || aqui.is(Blocks.FERN)) {
                level.removeBlock(onde, false);
                seca(level, onde.below(), debaixo, mordeFundo);
                return;
            }
            if (aqui.is(net.minecraft.tags.BlockTags.SMALL_FLOWERS) || aqui.is(Blocks.WHEAT)
                    || aqui.is(Blocks.CARROTS) || aqui.is(Blocks.POTATOES) || aqui.is(Blocks.BEETROOTS)
                    || aqui.is(Blocks.MELON) || aqui.is(Blocks.PUMPKIN)
                    || aqui.is(Blocks.MELON_STEM) || aqui.is(Blocks.PUMPKIN_STEM)) {
                level.setBlockAndUpdate(onde, Blocks.DEAD_BUSH.defaultBlockState());
                seca(level, onde.below(), debaixo, mordeFundo);
                return;
            }
            if (aqui.is(Blocks.FARMLAND)) {
                level.setBlockAndUpdate(onde, Blocks.SAND.defaultBlockState());
                return;
            }
            if (aqui.isSolid()) seca(level, onde, aqui, mordeFundo);
            else if (debaixo.isSolid()) seca(level, onde.below(), debaixo, mordeFundo);
        }

        /** O chão que seca: relva, terra, micélio e terra arada viram areia ou terra pelada. */
        public static void seca(ServerLevel level, BlockPos onde,
                                net.minecraft.world.level.block.state.BlockState qualé,
                                boolean mordeFundo) {
            if (!qualé.is(Blocks.DIRT) && !qualé.is(Blocks.GRASS_BLOCK) && !qualé.is(Blocks.MYCELIUM)
                    && !qualé.is(Blocks.FARMLAND)) {
                return;
            }
            int sorte = level.getRandom().nextInt(mordeFundo ? CHÃO_COM_GATO : CHÃO);
            if (sorte == 0) level.setBlockAndUpdate(onde, Blocks.SAND.defaultBlockState());
            else if (sorte == 1) level.setBlockAndUpdate(onde, Blocks.DIRT.defaultBlockState());
        }
    }

    // ================================================================= o prado, e as bonecas corrompidas

    /**
     * O <b>Poder da Natureza</b>: o {@code RiteNaturesPower} do Witchery.
     *
     * <p>De segundo em segundo ele escolhe um ponto ao acaso dentro do raio, procura o chão, e <b>enche um
     * círculo de três blocos</b> com relva — virando pedra, areia e cascalho em terra viva, e plantando em
     * cima mudas, flores, cogumelos e relva alta. Cento e cinquenta voltas, mais cinco por bruxa.
     *
     * <p>É o contrário exato da Praga, e eles são a mesma ideia escrita ao avesso: um seca o mundo em volta,
     * o outro planta-o.
     *
     * <p>Três coisas dele que valem ser ditas:
     *
     * <ol>
     *   <li><b>Ele faz água.</b> Dois por cento das casas viram água — mas <b>setenta</b> por cento se a casa
     *       tiver água ao lado. É assim que nascem as poças em vez de pingos soltos.</li>
     *   <li><b>A borda é esfarrapada.</b> Ao riscar cada linha do círculo, uma vez em cinco ele encolhe-a de
     *       um lado. É o que faz o prado não ter cara de círculo desenhado.</li>
     *   <li><b>E ele não planta debaixo de folhas.</b> Onde já houver copa, ele faz o chão e não põe nada em
     *       cima — senão o prado crescia por baixo da floresta.</li>
     * </ol>
     *
     * @param radius   até onde ele escolhe os pontos, antes do coven
     * @param height   quantos blocos ele procura chão, para cima e para baixo
     * @param duration quantas voltas, antes do coven
     * @param expanse  o raio de cada remendo de relva, menos um
     */
    public record NaturesPower(int radius, int height, int duration, int expanse) implements Rite {
        public static final int EVERY = 20;
        /** Quanto o coven soma ao raio e às voltas, por bruxa. */
        public static final int RAIO_POR_BRUXA = 2;
        public static final int VOLTAS_POR_BRUXA = 5;
        /** A chance de uma casa virar água: dois por cento, ou setenta se já houver água ao lado. */
        public static final double ÁGUA = 0.02;
        public static final double ÁGUA_AO_LADO = 0.7;

        @Override
        public List<RiteStep> steps(int coven) {
            return List.of((level, onde, ticks, rito) -> {
                if (ticks % EVERY != 0L) return RiteStep.Result.STARTING;

                int volta = rito.advance();
                if (volta >= this.duration + rito.coven() * VOLTAS_POR_BRUXA) {
                    return RiteStep.Result.COMPLETED;
                }

                int raio = this.radius + rito.coven() * RAIO_POR_BRUXA;
                var sorte = level.getRandom();
                int x = onde.getX() - raio + sorte.nextInt(raio * 2);
                int z = onde.getZ() - raio + sorte.nextInt(raio * 2);
                int chão = this.achaChão(level, x, onde.getY() - 1, z);
                if (chão != Integer.MIN_VALUE) {
                    this.remendo(level, new BlockPos(x, chão, z), this.expanse + 1);
                }
                return RiteStep.Result.UPKEEP;
            });
        }

        /** Procura o primeiro sólido com ar em cima, a partir de uma altura, para os dois lados. */
        private int achaChão(ServerLevel level, int x, int y, int z) {
            if (sólidoComArEmCima(level, new BlockPos(x, y, z))) return y;
            for (int h = 1; h < this.height; h++) {
                if (sólidoComArEmCima(level, new BlockPos(x, y + h, z))) return y + h;
                BlockPos abaixo = new BlockPos(x, y - h, z);
                if (!level.getBlockState(abaixo).isSolid()) continue;
                // e aqui a neve conta como ar, que é do original
                var emCima = level.getBlockState(abaixo.above());
                if (emCima.isAir() || emCima.is(Blocks.SNOW)) return y - h;
            }
            return Integer.MIN_VALUE;
        }

        private static boolean sólidoComArEmCima(ServerLevel level, BlockPos onde) {
            return level.getBlockState(onde).isSolid() && level.getBlockState(onde.above()).isAir();
        }

        /** Um remendo de relva: o círculo cheio, com as linhas esfarrapadas. */
        private void remendo(ServerLevel level, BlockPos meio, int raio) {
            int x = raio;
            int z = 0;
            int erro = 1 - raio;
            while (x >= z) {
                this.linha(level, meio, -x, x, z, raio);
                this.linha(level, meio, -z, z, x, raio);
                this.linha(level, meio, -x, x, -z, raio);
                this.linha(level, meio, -z, z, -x, raio);
                z++;
                if (erro < 0) {
                    erro += 2 * z + 1;
                } else {
                    x--;
                    erro += 2 * (z - x + 1);
                }
            }
        }

        /** Uma linha do remendo. Uma vez em cinco ela encolhe de um lado, e é o que esfarrapa a borda. */
        private void linha(ServerLevel level, BlockPos meio, int de, int até, int dz, int raio) {
            var sorte = level.getRandom();
            int x1 = raio > 1 && sorte.nextInt(5) == 0 ? de + 1 : de;
            int x2 = raio > 1 && sorte.nextInt(5) == 0 ? até - 1 : até;
            for (int dx = x1; dx <= x2; dx++) {
                this.casa(level, meio.offset(dx, 0, dz));
            }
        }

        /** E uma casa: o chão que vira relva ou água, e o que nasce em cima. */
        private void casa(ServerLevel level, BlockPos onde) {
            var emCima = level.getBlockState(onde.above());
            if (emCima.isSolid()) return;

            var sorte = level.getRandom();
            var aqui = level.getBlockState(onde);
            boolean debaixoDeFolha = emCima.is(net.minecraft.tags.BlockTags.LEAVES);

            if ((aqui.is(Blocks.STONE) || aqui.is(Blocks.SAND) || aqui.is(Blocks.GRAVEL)
                    || aqui.is(Blocks.DIRT) || aqui.is(Blocks.COARSE_DIRT) || aqui.is(Blocks.PODZOL))
                    && sorte.nextInt(8) != 0) {
                double chance = temÁguaAoLado(level, onde) ? ÁGUA_AO_LADO : ÁGUA;
                if (!debaixoDeFolha && sorte.nextDouble() <= chance) {
                    level.setBlockAndUpdate(onde, Blocks.WATER.defaultBlockState());
                    return;
                }
                level.setBlockAndUpdate(onde, Blocks.GRASS_BLOCK.defaultBlockState());
                aqui = level.getBlockState(onde);
            }

            if (debaixoDeFolha || aqui.isAir() || aqui.is(net.minecraft.tags.BlockTags.LEAVES)) return;
            if (sorte.nextInt(4) != 0) return;
            level.setBlockAndUpdate(onde.above(), nasce(level).defaultBlockState());
        }

        private static boolean temÁguaAoLado(ServerLevel level, BlockPos onde) {
            for (var lado : net.minecraft.core.Direction.Plane.HORIZONTAL) {
                if (level.getBlockState(onde.relative(lado)).is(Blocks.WATER)) return true;
            }
            return false;
        }

        /**
         * O que nasce em cima: a lista do original, com os pesos dele.
         *
         * <p>Repare que a <b>relva alta aparece seis vezes</b> na lista de vinte e tal, e as flores uma vez
         * cada: é assim que um prado fica com cara de prado, e não de canteiro.
         */
        private static Block nasce(ServerLevel level) {
            List<Block> quais = List.of(
                    Blocks.OAK_SAPLING, Blocks.SPRUCE_SAPLING, Blocks.BIRCH_SAPLING, Blocks.JUNGLE_SAPLING,
                    net.thaumcraft.occulta.OccultaBlocks.ROWAN_SAPLING,
                    net.thaumcraft.occulta.OccultaBlocks.ALDER_SAPLING,
                    net.thaumcraft.occulta.OccultaBlocks.HAWTHORN_SAPLING,
                    net.thaumcraft.occulta.OccultaBlocks.EMBER_MOSS,
                    Blocks.SHORT_GRASS, Blocks.FERN,
                    Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM,
                    Blocks.POPPY, Blocks.DANDELION,
                    Blocks.SHORT_GRASS, Blocks.FERN,
                    Blocks.SHORT_GRASS, Blocks.FERN,
                    Blocks.SHORT_GRASS, Blocks.FERN);
            return quais.get(level.getRandom().nextInt(quais.size()));
        }
    }

    /**
     * Corromper as bonecas de proteção: o {@code RiteCursePoppets} do Witchery.
     *
     * <p>Ele quebra até <b>dez</b> Bonecas de Proteção contra Vodu de quem o vínculo prender — e é assim que
     * se desarma alguém que se escondeu atrás delas.
     *
     * <p><b>E ele exige a maestria da maldição.</b> Sem o familiar gato, o rito <b>recusa</b> e devolve o que
     * se ofereceu, com um recado. É o único rito deste porte que pede um familiar para correr, e é a quarta
     * coisa que o gato destranca.
     *
     * @param level quantas bonecas ele quebra, por grau
     */
    public record CursePoppets(int level) implements Rite {
        /** Quantas bonecas ele quebra: os dez do original. */
        public static final int QUANTAS = 10;

        @Override
        public List<RiteStep> steps(int coven) {
            return List.of((level, onde, ticks, rito) -> {
                if (ticks % 20L != 0L) return RiteStep.Result.STARTING;

                Player quemFaz = rito.starter(level);
                if (!net.thaumcraft.occulta.familiar.Familiars.temMaestriaDeMaldicao(quemFaz)) {
                    avisa(level, onde, rito, "message.thaumcraft.requires_curse_mastery");
                    return RiteStep.Result.ABORTED_REFUND;
                }

                LivingEntity alvo = null;
                for (var oferecido : rito.offered()) {
                    if (!oferecido.stack().is(net.thaumcraft.occulta.OccultaItems.TAGLOCK)) continue;
                    alvo = net.thaumcraft.occulta.Voodoo.bound(level, oferecido.stack());
                    if (alvo != null) break;
                }
                if (!(alvo instanceof Player vítima)) return RiteStep.Result.ABORTED_REFUND;

                // a primeira boneca de proteção contra vodu gasta-se a guardar as outras: é a ordem do
                // original, e é o que dá a quem se guardou uma chance de sobreviver ao rito
                if (net.thaumcraft.occulta.Voodoo.guarded(level, vítima)) {
                    level.sendParticles(ParticleTypes.HAPPY_VILLAGER, vítima.getX(), vítima.getY() + 1.0,
                            vítima.getZ(), 16, 0.4, 0.6, 0.4, 0.0);
                    return RiteStep.Result.COMPLETED;
                }

                for (int n = 0; n < QUANTAS; n++) {
                    if (!net.thaumcraft.occulta.Poppets.spend(level, vítima,
                            net.thaumcraft.occulta.PoppetItem.Kind.VOODOO_PROTECTION)) {
                        break;
                    }
                }

                level.sendParticles(ParticleTypes.FLAME, onde.getX() + 0.5, onde.getY() + 0.1,
                        onde.getZ() + 0.5, 32, 1.0, 1.0, 1.0, 0.05);
                level.playSound(null, onde, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.BLOCKS, 1.0f, 1.0f);
                return RiteStep.Result.COMPLETED;
            });
        }
    }

    // ================================================================= os do demônio

    /**
     * <b>Banir</b>: o {@code RiteBanishDemon} do Witchery.
     *
     * <p>De segundo em segundo, tudo o que é <b>de lá</b> a nove blocos do círculo <b>deixa de existir</b> —
     * sem dano, sem luta, sem queda: some, e no lugar fica um estouro de partículas.
     *
     * <p>E a lista do que ele apanha é a lista das coisas que o ofício pode chamar e não consegue despedir:
     * o <b>Demônio</b>, a <b>Morte</b>, o <b>Senhor do Tormento</b>, o <b>Imp</b> e o <b>Reflexo</b>. É o
     * botão de desfazer de quem chamou mais do que devia — e por isso ele é barato: pó de blaze e uma pedra.
     *
     * <p><b>Declarado:</b> deste porte entram o Demônio e o Reflexo, que são os dois que existem aqui. A
     * Morte, o Senhor do Tormento e o Imp ainda não estão portados, e a lista já os espera.
     *
     * @param radius a que distância ele alcança
     */
    public record BanishDemon(int radius) implements Rite {
        @Override
        public List<RiteStep> steps(int coven) {
            return List.of((level, onde, ticks, rito) -> {
                if (ticks % 20L != 0L) return RiteStep.Result.STARTING;
                level.playSound(null, onde, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 1.0f);

                var caixa = new AABB(onde).inflate(this.radius);
                for (var bicho : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, caixa)) {
                    if (!deLá(bicho)) continue;
                    if (bicho.distanceToSqr(onde.getX(), onde.getY(), onde.getZ())
                            >= (double) this.radius * this.radius) {
                        continue;
                    }
                    level.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION,
                            bicho.getX(), bicho.getY() + 1.0, bicho.getZ(), 8, 0.5, 1.0, 0.5, 0.0);
                    bicho.discard();
                }
                return RiteStep.Result.COMPLETED;
            });
        }

        /** O que é de lá, e por isso se despede. */
        public static boolean deLá(net.minecraft.world.entity.Entity quem) {
            return quem instanceof net.thaumcraft.occulta.demon.DemonEntity
                    || quem instanceof net.thaumcraft.occulta.mirror.ReflectionEntity;
        }
    }

    /**
     * <b>O inferno na terra</b>: o {@code RiteHellOnEarth} do Witchery, e o rito mais caro que ele tem.
     *
     * <p>Ele é um rito que <b>se abre em círculo</b>, e o que ele faz enquanto cresce é <b>estragar o
     * chão</b>: onde o anel passa, a terra, a grama, o micélio, a terra arada e a areia viram
     * <b>pedra do Nether</b> — e viram mais perto do meio do que na borda, que é o que deixa no fim uma
     * mancha de inferno densa no centro e esfarrapada nas pontas.
     *
     * <p>Com a <b>maestria da maldição</b>, a grama alta e as flores que o anel atravessa <b>pegam fogo</b>
     * em vez de só sumirem.
     *
     * <p>E quando o círculo chega ao tamanho dele, o rito <b>não acaba</b>: de duas em duas segundas ele
     * cospe uma criatura do Nether no meio, <b>para sempre</b>, duzentos de poder por vez. O que sai não é a
     * esmo: <b>dois em cem</b> é um <b>Demônio</b>, oito um ghast, trinta um blaze, vinte um cubo de magma, e
     * o resto zumbis-porcos.
     *
     * <p>Dois por cento é de propósito. Quem quiser um coração <b>compra</b>; quem quiser um demônio
     * <b>espera</b> — e o demônio que sai daqui <b>não foi chamado por ninguém</b>, de modo que vai embora se
     * ninguém estiver olhando.
     *
     * <p>O preço de acendê-lo diz o resto: Sopa de Pedra Vermelha, um <b>Coração de Demônio</b>, uma Pedra
     * de Caminho, uma <b>Estrela do Nether</b>, um <b>aldeão vivo</b> e cinco mil de poder. Ele pede um
     * coração para dar corações.
     *
     * <p><b>Declarado:</b> o original tranca o fogo atrás de uma opção de configuração; este porte não tem
     * arquivo de configuração e deixa o fogo sempre ligado para quem tem a maestria, que é o que a opção faz
     * por omissão.
     */
    public static final class HellOnEarth extends Expanding {
        /** De quantas em quantas batidas sai um, e as quatro fatias da sorte. */
        public static final int DE = 40;
        public static final double DEMÔNIO = 0.02;
        public static final double GHAST = 0.1;
        public static final double BLAZE = 0.4;
        public static final double MAGMA = 0.6;

        /** E de quantas em quantas casas o chão estraga: mais perto do meio, mais fundo. */
        public static final int PERTO = 2;
        public static final int MEIO = 4;
        public static final int LONGE = 6;

        private final float upkeep;

        public HellOnEarth(int radius, int height, float upkeep) {
            super(radius, height, true);
            this.upkeep = upkeep;
        }

        public float upkeep() {
            return this.upkeep;
        }

        /**
         * O chão, casa por casa.
         *
         * <p>A grama alta e as plantas de lavoura <b>queimam</b>, e o que está debaixo delas é que estraga; o
         * resto estraga onde está, se for sólido, ou um abaixo, se não for.
         */
        @Override
        public void onBlock(ServerLevel level, BlockPos onde, int raio, Player quem, boolean mordeFundo) {
            net.minecraft.world.level.block.state.BlockState aqui = level.getBlockState(onde);
            if (aqui.is(net.minecraft.world.level.block.Blocks.SHORT_GRASS) || éLavoura(aqui)) {
                if (mordeFundo) {
                    level.setBlockAndUpdate(onde, net.minecraft.world.level.block.Blocks.FIRE
                            .defaultBlockState());
                }
                estraga(level, onde.below(), raio);
            } else if (aqui.isSolid()) {
                estraga(level, onde, raio);
            } else {
                BlockPos abaixo = onde.below();
                if (level.getBlockState(abaixo).isSolid()) estraga(level, abaixo, raio);
            }
        }

        /** As plantas que o anel queima em vez de atravessar. */
        private static boolean éLavoura(net.minecraft.world.level.block.state.BlockState oquê) {
            return oquê.is(net.minecraft.tags.BlockTags.SMALL_FLOWERS)
                    || oquê.is(net.minecraft.world.level.block.Blocks.CARROTS)
                    || oquê.is(net.minecraft.world.level.block.Blocks.WHEAT)
                    || oquê.is(net.minecraft.world.level.block.Blocks.POTATOES)
                    || oquê.is(net.minecraft.world.level.block.Blocks.MELON_STEM)
                    || oquê.is(net.minecraft.world.level.block.Blocks.ATTACHED_MELON_STEM)
                    || oquê.is(net.minecraft.world.level.block.Blocks.PUMPKIN_STEM)
                    || oquê.is(net.minecraft.world.level.block.Blocks.ATTACHED_PUMPKIN_STEM)
                    || oquê.is(net.minecraft.world.level.block.Blocks.MELON)
                    || oquê.is(net.minecraft.world.level.block.Blocks.PUMPKIN)
                    || oquê.is(net.minecraft.world.level.block.Blocks.CARVED_PUMPKIN);
        }

        /**
         * E o estrago: uma casa em duas no terço de dentro, uma em quatro na metade, uma em seis no resto.
         *
         * <p>Só a terra, a grama, o micélio, a terra arada e a areia estragam. Pedra não; madeira não. O
         * inferno na terra <b>come o que é vivo</b> e deixa o que é construído.
         */
        private void estraga(ServerLevel level, BlockPos onde, int raio) {
            net.minecraft.world.level.block.state.BlockState oquê = level.getBlockState(onde);
            if (!oquê.is(net.minecraft.world.level.block.Blocks.DIRT)
                    && !oquê.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK)
                    && !oquê.is(net.minecraft.world.level.block.Blocks.MYCELIUM)
                    && !oquê.is(net.minecraft.world.level.block.Blocks.FARMLAND)
                    && !oquê.is(net.minecraft.world.level.block.Blocks.SAND)) {
                return;
            }
            int uma = raio < this.maxRadius / 3 ? PERTO : raio < this.maxRadius / 2 ? MEIO : LONGE;
            if (level.getRandom().nextInt(uma) != 0) return;
            level.setBlockAndUpdate(onde, net.minecraft.world.level.block.Blocks.NETHERRACK
                    .defaultBlockState());
        }

        /** E, cheio, ele cospe o Nether pela porta que abriu — até o altar secar. */
        @Override
        protected boolean done(ServerLevel level, BlockPos meio, int raio, boolean cheio, long ticks) {
            if (!cheio || ticks % DE != 0L) return false;

            var altar = net.thaumcraft.occulta.PowerSources.closest(level, meio);
            if (altar == null || !altar.consume(this.upkeep)) return true;

            var qual = sorteia(level.getRandom().nextDouble());
            var bicho = qual.create(level, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
            if (bicho == null) return false;

            bicho.snapTo(meio.getX() + 0.5, meio.getY() + 2.0, meio.getZ() + 0.5, 0.0f, 0.0f);
            if (bicho instanceof net.minecraft.world.entity.Mob mob) {
                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(meio),
                        net.minecraft.world.entity.EntitySpawnReason.TRIGGERED, null);
            }
            level.addFreshEntity(bicho);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,
                    meio.getX() + 0.5, meio.getY() + 2.0, meio.getZ() + 0.5, 1, 1.0, 2.0, 1.0, 0.0);
            level.playSound(null, meio, SoundEvents.BLAZE_DEATH, SoundSource.BLOCKS, 1.0f, 1.0f);
            return false;
        }

        /** O que sai, pela ordem das fatias do original. */
        public static net.minecraft.world.entity.EntityType<?> sorteia(double sorte) {
            if (sorte < DEMÔNIO) return net.thaumcraft.occulta.OccultaEntities.DEMON;
            if (sorte < GHAST) return net.minecraft.world.entity.EntityTypes.GHAST;
            if (sorte < BLAZE) return net.minecraft.world.entity.EntityTypes.BLAZE;
            if (sorte < MAGMA) return net.minecraft.world.entity.EntityTypes.MAGMA_CUBE;
            return net.minecraft.world.entity.EntityTypes.ZOMBIFIED_PIGLIN;
        }
    }

    /**
     * <b>A Expansão Gelada</b>: o {@code RiteSphereEffect} do Witchery, com o Gelo Perpétuo dentro.
     *
     * <p>Ele abre uma <b>bola oca de gelo que não derrete</b> à volta do círculo, e cresce de cinco em cinco
     * batidas até o tamanho que o coven der: <b>oito</b> com duas bruxas, <b>doze</b> até cinco,
     * <b>dezesseis</b> acima disso. Sozinha, ninguém o faz — e é o único rito deste porte que <b>desiste e
     * devolve</b> o que se ofereceu quando o coven é pequeno demais.
     *
     * <p>A cada passo <b>par</b> ele risca a casca no raio de agora e risca <b>ar</b> dois raios para dentro,
     * de modo que a bola se abre por fora e se esvazia por dentro ao mesmo tempo. No último passo, o que
     * sobrou de água lá dentro vira ar.
     *
     * <p>É a maneira do ofício de <b>fazer um lugar</b>: uma bolha no fundo de um lago, uma cúpula no meio de
     * um campo. O preço diz o que ele vale — uma <b>espada de diamante</b>, um <b>Coração Congelado</b> e uma
     * <b>Pedra Sintonizada Carregada</b> —, e o que fica é uma casa.
     *
     * @param radius o tamanho com duas bruxas; o resto sai dele
     */
    public record IceShell(int radius) implements Rite {
        /** Quantas bruxas ele pede, e como o coven estica a bola. */
        public static final int COVEN = 2;
        public static final int MÉDIO = 5;
        public static final double VEZ_E_MEIA = 1.5;
        public static final double DUAS_VEZES = 2.0;

        /** O raio começa em quatro e cresce de cinco em cinco batidas. */
        public static final int DE = 5;
        public static final int COMEÇA = 4;
        public static final int OCO = 2;
        public static final int TETO = 250;

        /** Até onde a bola chega com este tanto de bruxas. */
        public static int até(int radius, int coven) {
            if (coven <= COVEN) return radius;
            return (int) ((coven <= MÉDIO ? VEZ_E_MEIA : DUAS_VEZES) * radius);
        }

        @Override
        public List<RiteStep> steps(int coven) {
            return List.of(new Passo(this));
        }

        /** O passo que faz a bola crescer. */
        private static final class Passo implements RiteStep {
            private final IceShell rite;
            private int stage;
            private boolean começou;

            private Passo(IceShell rite) {
                this.rite = rite;
            }

            @Override
            public Result run(ServerLevel level, BlockPos onde, long ticks, ActiveRite rito) {
                if (!this.começou) {
                    if (ticks % 20L != 0L) return Result.STARTING;
                    this.começou = true;
                    level.playSound(null, onde, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 1.0f);
                }
                if (ticks % DE != 0L) return Result.UPKEEP;

                if (rito.coven() < COVEN) {
                    avisa(level, onde, rito, "message.thaumcraft.coven_too_small");
                    return Result.ABORTED_REFUND;
                }

                this.stage++;
                int teto = até(this.rite.radius, rito.coven());
                int raio = this.stage + COMEÇA;
                if (raio <= teto) {
                    if (this.stage % 2 == 0) {
                        net.thaumcraft.occulta.ice.IceSphere.casca(onde, raio, casa -> {
                            if (level.getBlockState(casa).canBeReplaced()) {
                                level.setBlockAndUpdate(casa, net.thaumcraft.occulta.OccultaBlocks
                                        .PERPETUAL_ICE.defaultBlockState());
                            }
                        });
                        net.thaumcraft.occulta.ice.IceSphere.casca(onde, raio - OCO, casa -> {
                            if (level.getBlockState(casa).is(
                                    net.thaumcraft.occulta.OccultaBlocks.PERPETUAL_ICE)) {
                                level.setBlockAndUpdate(casa, Blocks.AIR.defaultBlockState());
                            }
                        });
                    }
                    if (raio == teto) {
                        net.thaumcraft.occulta.ice.IceSphere.enche(level, onde, teto, Blocks.AIR,
                                net.thaumcraft.occulta.OccultaBlocks.PERPETUAL_ICE);
                    }
                }
                return this.stage <= TETO && raio < teto ? Result.UPKEEP : Result.COMPLETED;
            }
        }
    }

    // ================================================================= os que empurram e puxam

    /**
     * O anel que <b>empurra</b> ou <b>puxa</b> o que estiver dentro dele: os
     * {@code RiteProtectionCircleRepulsive} e {@code RiteProtectionCircleAttractive} do Witchery.
     *
     * <p>São dois ritos com a mesma conta e o sinal trocado, e por isso são um só aqui. O de <b>Proteção</b>
     * empurra tudo para fora de quatro blocos; o de <b>Aprisionamento</b> puxa tudo de volta para dentro. Os
     * dois custam <b>0,8 de poder de altar por batida</b> e correm <b>para sempre</b>, até o altar secar.
     *
     * <p><b>Gente não se mexe, e o dragão também não.</b> É do original, e é o que torna estes ritos
     * utilizáveis: um anel que empurrasse quem o fez seria uma armadilha para o dono.
     *
     * <p>O empurrão tem uma conta esquisita e ela fica como está: calcula-se a direção pela distância ao
     * <b>quadrado do quadrado</b>, e depois o resultado é <b>jogado fora</b> e trocado por um valor fixo —
     * 0,22 na horizontal e 0,12 na vertical. Ou seja: a conta elaborada só serve para decidir o <b>sinal</b>.
     *
     * <p>E o de puxar só puxa quem está <b>na borda</b>, a partir de raio menos um. Quem já está no meio fica
     * quieto — senão o anel cuspia os bichos para o centro e eles saltavam para sempre.
     *
     * @param radius o raio
     * @param upkeep o que ele come por batida
     * @param pull   se puxa em vez de empurrar
     */
    public record PushCircle(int radius, float upkeep, boolean pull) implements Rite {
        /** O valor fixo com que o original troca a conta que acabou de fazer. */
        public static final double NA_HORIZONTAL = 0.22;
        public static final double NA_VERTICAL = 0.12;

        @Override
        public List<RiteStep> steps(int coven) {
            return List.of((level, onde, ticks, rito) -> {
                if (rito.stage() == 0) {
                    if (ticks % 20L != 0L) return RiteStep.Result.STARTING;
                    rito.advance();
                    level.playSound(null, onde, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 1.0f);
                }

                if (this.upkeep > 0.0f) {
                    var altar = net.thaumcraft.occulta.PowerSources.closest(level, onde);
                    if (altar == null || !altar.consume(this.upkeep)) return RiteStep.Result.ABORTED;
                }

                double meioX = onde.getX();
                double meioY = onde.getY();
                double meioZ = onde.getZ();
                var caixa = new AABB(onde).inflate(this.radius);
                for (var bicho : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, caixa)) {
                    if (bicho.distanceToSqr(meioX, meioY, meioZ) >= (double) this.radius * this.radius) {
                        continue;
                    }
                    if (this.pull) puxa(bicho, meioX, meioY, meioZ, this.radius);
                    else empurra(bicho, meioX, meioY, meioZ);
                }
                return RiteStep.Result.UPKEEP;
            });
        }

        /** O empurrão do original, com a conta que só decide o sinal. */
        public static void empurra(net.minecraft.world.entity.Entity bicho, double x, double y, double z) {
            var quanto = sinal(bicho, x, y, z);
            if (quanto == null) return;
            bicho.setDeltaMovement(bicho.getDeltaMovement().add(quanto));
            bicho.hurtMarked = true;
        }

        /** E o puxão, que é o mesmo vetor virado de meia-volta — e só para quem está na borda. */
        public static void puxa(net.minecraft.world.entity.Entity bicho, double x, double y, double z,
                                int raio) {
            var anda = bicho.getDeltaMovement();
            double depois = Math.sqrt(bicho.distanceToSqr(x - anda.x, y - anda.y, z - anda.z));
            if (depois < raio - 1.0) return;

            var quanto = sinal(bicho, x, y, z);
            if (quanto == null) return;
            // meia-volta em torno do Y: o x e o z trocam de sinal, e o y fica zero
            bicho.setDeltaMovement(-quanto.x, 0.0, -quanto.z);
            bicho.hurtMarked = true;
        }

        /**
         * A conta do original, tal e qual.
         *
         * <p>Ela calcula a direção com a distância elevada à quarta, confere que não passa de 6⁴ — e então
         * <b>deita o número fora</b> e usa 0,22 e 0,12 pelo sinal que saiu. Fica como está.
         */
        private static Vec3 sinal(net.minecraft.world.entity.Entity bicho, double x, double y, double z) {
            if (bicho instanceof Player) return null;
            if (bicho instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon) return null;

            double dx = x - bicho.getX();
            double dy = y - bicho.getY();
            double dz = z - bicho.getZ();
            double quão = dx * dx + dy * dy + dz * dz;
            quão *= quão;
            if (quão > Math.pow(6.0, 4.0)) return null;

            double vx = -(dx * 0.01999999955296516 / quão) * Math.pow(6.0, 3.0);
            double vy = -(dy * 0.01999999955296516 / quão) * Math.pow(6.0, 3.0);
            double vz = -(dz * 0.01999999955296516 / quão) * Math.pow(6.0, 3.0);

            if (vx > 0.0) vx = NA_HORIZONTAL;
            else if (vx < 0.0) vx = -NA_HORIZONTAL;
            // e o de cima é o engano do original: os dois lados dão o MESMO valor, para cima
            if (vy > 0.2) vy = NA_VERTICAL;
            else if (vy < -0.1) vy = NA_VERTICAL;
            if (vz > 0.0) vz = NA_HORIZONTAL;
            else if (vz < 0.0) vz = -NA_HORIZONTAL;

            return new Vec3(vx, vy, vz);
        }
    }

    /**
     * Os minérios que sobem: o {@code RiteTransposeOres} do Witchery.
     *
     * <p>De dez em dez batidas ele desce <b>uma camada</b> por baixo do círculo, varre um quadrado de oito
     * blocos de lado e <b>arranca</b> de lá o que for do feitio pedido — pondo o bloco como item em cima do
     * círculo. Trinta camadas, mais cinco por bruxa do coven, ou até chegar à rocha-mãe.
     *
     * <p><b>E com o coven cheio ele leva dois feitios em vez de um.</b> É o
     * {@code covenSize == 6 ? 2 : 1} do original: sozinha, uma bruxa traz só ferro; com seis, traz ouro
     * também.
     *
     * @param radius  metade do lado do quadrado que ele varre
     * @param pulses  quantas camadas, antes do coven
     * @param blocks  o que ele arranca, por ordem — o segundo só com coven cheio
     */
    public record TransposeOres(int radius, int pulses, List<Block> blocks) implements Rite {
        public static final int EVERY = 10;
        /** A camada mais funda a que ele chega. */
        public static final int FUNDO = 2;
        /** Quantas camadas a mais por bruxa. */
        public static final int POR_BRUXA = 5;

        @Override
        public List<RiteStep> steps(int coven) {
            return List.of((level, onde, ticks, rito) -> {
                if (ticks % EVERY != 0L) return RiteStep.Result.STARTING;

                int camada = rito.advance();
                int y = onde.getY() - camada;
                int quantos = rito.coven() >= 6 ? 2 : 1;

                for (int x = onde.getX() - this.radius; x <= onde.getX() + this.radius; x++) {
                    for (int z = onde.getZ() - this.radius; z <= onde.getZ() + this.radius; z++) {
                        BlockPos casa = new BlockPos(x, y, z);
                        var qualé = level.getBlockState(casa);
                        for (int t = 0; t < quantos && t < this.blocks.size(); t++) {
                            if (!qualé.is(this.blocks.get(t))) continue;
                            level.removeBlock(casa, false);
                            var sorte = level.getRandom();
                            Block.popResource(level, new BlockPos(
                                            onde.getX() - this.radius + sorte.nextInt(2 * this.radius + 1),
                                            onde.getY() + 2,
                                            onde.getZ() - this.radius + sorte.nextInt(2 * this.radius + 1)),
                                    new ItemStack(this.blocks.get(t)));
                        }
                    }
                }

                boolean segue = camada < this.pulses + POR_BRUXA * rito.coven() && y > FUNDO;
                return segue ? RiteStep.Result.UPKEEP : RiteStep.Result.COMPLETED;
            });
        }
    }

    /**
     * Repintar um anel de glifos: o {@code RiteGlyphicTransformation} do Witchery.
     *
     * <p>Larga-se giz de uma cor dentro do círculo e <b>um anel inteiro muda de giz</b>. Qual deles muda
     * depende de <b>quantos gizes</b> se largou: um muda o de dentro, dois o do meio, três o de fora.
     *
     * <p>É o rito mais prestável do ofício, e o menos espalhafatoso: sem ele, trocar o giz de um anel de
     * quarenta glifos é quarenta picaretadas e quarenta riscos.
     *
     * <p><b>Só um giz de cada vez.</b> Largando duas cores, o rito conta a primeira que achar e ignora as
     * outras — é o que os três {@code if} encadeados do original fazem. E ele gasta <b>um</b> giz da pilha,
     * seja a pilha de que tamanho for: o resto fica no chão.
     *
     * <p>O desenho dos três anéis é o do original, e com ele vem o engano de sempre: a varredura vai até o
     * <b>penúltimo</b> z, e a fila de trás do desenho nunca é olhada.
     */
    public record GlyphicTransformation() implements Rite {
        public static final int EVERY = 30;
        /** Até onde ele procura o giz largado. */
        public static final double ALCANCE = 4.0;

        /** Os três anéis: 1 o de dentro, 2 o do meio, 3 o de fora. */
        private static final int[][] ANÉIS = {
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 3, 3, 3, 3, 3, 3, 3, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0},
                {0, 0, 0, 3, 0, 0, 2, 2, 2, 2, 2, 0, 0, 3, 0, 0, 0},
                {0, 0, 3, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 3, 0, 0},
                {0, 3, 0, 0, 2, 0, 0, 1, 1, 1, 0, 0, 2, 0, 0, 3, 0},
                {0, 3, 0, 2, 0, 0, 1, 0, 0, 0, 1, 0, 0, 2, 0, 3, 0},
                {0, 3, 0, 2, 0, 1, 0, 0, 0, 0, 0, 1, 0, 2, 0, 3, 0},
                {0, 3, 0, 2, 0, 1, 0, 0, 4, 0, 0, 1, 0, 2, 0, 3, 0},
                {0, 3, 0, 2, 0, 1, 0, 0, 0, 0, 0, 1, 0, 2, 0, 3, 0},
                {0, 3, 0, 2, 0, 0, 1, 0, 0, 0, 1, 0, 0, 2, 0, 3, 0},
                {0, 3, 0, 0, 2, 0, 0, 1, 1, 1, 0, 0, 2, 0, 0, 3, 0},
                {0, 0, 3, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 3, 0, 0},
                {0, 0, 0, 3, 0, 0, 2, 2, 2, 2, 2, 0, 0, 3, 0, 0, 0},
                {0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 3, 3, 3, 3, 3, 3, 3, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };

        /** Os três gizes, na ordem em que o original os procura, com o glifo de cada um. */
        private static List<net.minecraft.world.item.Item> gizes() {
            return List.of(net.thaumcraft.occulta.OccultaItems.RITUAL_CHALK,
                    net.thaumcraft.occulta.OccultaItems.OTHERWHERE_CHALK,
                    net.thaumcraft.occulta.OccultaItems.INFERNAL_CHALK);
        }

        private static Block glifo(int qual) {
            return switch (qual) {
                case 1 -> net.thaumcraft.occulta.OccultaBlocks.OTHERWHERE_GLYPH;
                case 2 -> net.thaumcraft.occulta.OccultaBlocks.INFERNAL_GLYPH;
                default -> net.thaumcraft.occulta.OccultaBlocks.RITUAL_GLYPH;
            };
        }

        @Override
        public List<RiteStep> steps(int coven) {
            return List.of((level, onde, ticks, rito) -> {
                if (ticks % EVERY != 0L) return RiteStep.Result.STARTING;

                var caixa = new AABB(onde).inflate(ALCANCE, 2.0, ALCANCE);
                var largados = level.getEntitiesOfClass(ItemEntity.class, caixa);

                int qualGiz = -1;
                int quantos = 0;
                for (ItemEntity largado : largados) {
                    for (int g = 0; g < 3; g++) {
                        if (!largado.getItem().is(gizes().get(g))) continue;
                        // o primeiro giz que aparece é o que manda: os outros são ignorados
                        if (qualGiz != -1 && qualGiz != g) continue;
                        boolean primeiro = qualGiz == -1;
                        qualGiz = g;
                        quantos += largado.getItem().getCount();
                        if (primeiro) {
                            largado.getItem().shrink(1);
                            if (largado.getItem().isEmpty()) largado.discard();
                        }
                        level.sendParticles(ParticleTypes.SMOKE, largado.getX(), largado.getY() + 0.3,
                                largado.getZ(), 8, 0.2, 0.2, 0.2, 0.01);
                    }
                }
                if (qualGiz == -1) return RiteStep.Result.ABORTED_REFUND;

                int anel = Math.min(quantos, 3);
                Block vira = glifo(qualGiz);
                int meio = (ANÉIS.length - 1) / 2;
                for (int z = 0; z < ANÉIS.length - 1; z++) {
                    for (int x = 0; x < ANÉIS[z].length; x++) {
                        if (ANÉIS[ANÉIS.length - 1 - z][x] != anel) continue;
                        BlockPos casa = new BlockPos(onde.getX() - meio + x, onde.getY(),
                                onde.getZ() - meio + z);
                        var qualé = level.getBlockState(casa);
                        if (!(qualé.getBlock() instanceof net.thaumcraft.occulta.GlyphBlock)) continue;
                        if (qualé.is(vira)) continue;
                        level.setBlockAndUpdate(casa, vira.defaultBlockState()
                                .setValue(net.thaumcraft.occulta.GlyphBlock.SHAPE,
                                        qualé.getValue(net.thaumcraft.occulta.GlyphBlock.SHAPE)));
                        level.sendParticles(ParticleTypes.SMOKE, casa.getX() + 0.5, casa.getY() + 1.0,
                                casa.getZ() + 0.5, 4, 0.2, 0.2, 0.2, 0.01);
                    }
                }
                return RiteStep.Result.COMPLETED;
            });
        }
    }

    // ================================================================= as maldições

    /**
     * Pôr ou tirar uma maldição: o {@code RiteCurseCreature} do Witchery.
     *
     * <p>Ele não olha quem está no círculo — ele olha o <b>vínculo</b> que se ofereceu. Quem tem o sangue, o
     * cabelo ou o nome de alguém num frasco amaldiçoa essa pessoa <b>do outro lado do mundo</b>, e é isso que
     * faz o vínculo valer o que vale.
     *
     * <h2>O grau sobe com quem está em volta</h2>
     *
     * <p>Ao grau que o rito tem somam-se: <b>um</b> se quem o faz tem a maestria da maldição (o gato),
     * <b>um</b> se o coven tem três ou mais, e <b>dois</b> se tem seis. Um coven cheio com gato põe uma
     * maldição de grau <b>quatro</b> onde uma bruxa sozinha põe uma de grau um.
     *
     * <h2>E tirar é uma aposta</h2>
     *
     * <p>Esta é a parte boa, e é fácil portar errado. <b>Tirar uma maldição pode deixá-la pior.</b> O rito
     * compara a força que traz com a força que a maldição tem:
     *
     * <table border="1">
     *   <caption>O que acontece ao tentar tirar</caption>
     *   <tr><th>o rito contra a maldição</th><th>o que sai</th></tr>
     *   <tr><td>mais forte</td><td>sai — menos uma vez em vinte, em que <b>sobe um grau</b></td></tr>
     *   <tr><td>mais fraco</td><td><b>sobe um grau</b> — a não ser uma vez em quatro, em que sai</td></tr>
     *   <tr><td>igual</td><td>sai três vezes em quatro; na quarta, <b>sobe</b></td></tr>
     * </table>
     *
     * <p>Quem tenta tirar uma maldição de grau cinco com um rito de grau um quase sempre a piora. É por isso
     * que o rito de tirar também quer coven e gato: não para pôr, para <b>conseguir tirar</b>.
     *
     * <p>E, saindo, ela leva consigo os cinco efeitos que o azar deixa: veneno, fraqueza, cegueira, pancada e
     * lentidão.
     *
     * @param curse se põe (verdadeiro) ou tira
     * @param qual  qual das quatro
     * @param level o grau que este rito traz
     */
    public record CurseCreature(boolean curse, net.thaumcraft.occulta.curse.Curse qual, int level)
            implements Rite {
        /** O que a maestria da maldição soma, e o que o coven soma. */
        public static final int GATO = 1;
        public static final int COVEN_TRES = 1;
        public static final int COVEN_CHEIO = 2;

        /** Os sorteios do tirar: um em vinte, um em quatro. */
        public static final int SORTE_MAIS_FORTE = 20;
        public static final int SORTE_IGUAL_OU_MAIS_FRACO = 4;

        @Override
        public List<RiteStep> steps(int coven) {
            return List.of((level, onde, ticks, rito) -> {
                if (ticks % 20L != 0L) return RiteStep.Result.STARTING;

                Player quemFaz = rito.starter(level);
                LivingEntity alvo = doVinculo(level, rito);
                if (alvo == null) return RiteStep.Result.ABORTED_REFUND;

                int soma = (net.thaumcraft.occulta.familiar.Familiars.temMaestriaDeMaldicao(quemFaz)
                        ? GATO : 0)
                        + (rito.coven() >= 6 ? COVEN_CHEIO : (rito.coven() >= 3 ? COVEN_TRES : 0));

                boolean pegou = this.curse
                        ? this.poe(level, alvo, quemFaz, soma)
                        : this.tira(level, alvo, soma);

                level.sendParticles(pegou ? ParticleTypes.FLAME : ParticleTypes.WITCH,
                        onde.getX() + 0.5, onde.getY() + 0.1, onde.getZ() + 0.5, 32, 1.0, 1.0, 1.0, 0.05);
                level.playSound(null, onde,
                        pegou ? SoundEvents.ENDER_DRAGON_GROWL : SoundEvents.PLAYER_LEVELUP,
                        SoundSource.BLOCKS, 1.0f, 1.0f);
                return RiteStep.Result.COMPLETED;
            });
        }

        /** Quem o vínculo oferecido prende. */
        private static LivingEntity doVinculo(ServerLevel level, ActiveRite rito) {
            for (var oferecido : rito.offered()) {
                if (!oferecido.stack().is(net.thaumcraft.occulta.OccultaItems.TAGLOCK)) continue;
                // no original o vínculo cheio é outro item (metadado 1); aqui é o mesmo item com um
                // componente, e por isso quem decide é o Voodoo.bound: vazio, não prende ninguém.
                var quem = net.thaumcraft.occulta.Voodoo.bound(level, oferecido.stack());
                if (quem != null) return quem;
            }
            return null;
        }

        /**
         * Põe. Quem está guardado por uma boneca de vodu não apanha, e quem tentou leva o troco.
         *
         * <p>E <b>quem amaldiçoa é notado</b>: é o {@code blackMagicPerformed} do original, que corre antes
         * de se saber se a maldição pegou. Tentar já conta.
         */
        private boolean poe(ServerLevel level, LivingEntity alvo, Player quemFaz, int soma) {
            net.thaumcraft.occulta.hunter.WitchHunters.magiaNegra(quemFaz);
            if (net.thaumcraft.occulta.Voodoo.guarded(level, alvo)) {
                if (quemFaz != null) net.thaumcraft.occulta.Voodoo.backfire(level, quemFaz);
                return false;
            }
            net.thaumcraft.occulta.curse.Curse.put(alvo, this.qual, this.level + soma);
            return true;
        }

        /** O sorteio de tirar, à mão: é o que a prova chama, porque prova nenhuma tem vínculo. */
        public boolean tiraParaProva(ServerLevel level, LivingEntity alvo, int soma) {
            return this.tira(level, alvo, soma);
        }

        /** E tira — ou piora, que é o que o original deixa acontecer. */
        private boolean tira(ServerLevel level, LivingEntity alvo, int soma) {
            int tem = net.thaumcraft.occulta.curse.Curse.level(alvo, this.qual);
            if (tem <= 0) return false;

            int força = this.level + soma;
            int novo;
            if (força > tem) {
                novo = level.getRandom().nextInt(SORTE_MAIS_FORTE) == 0 ? tem + 1 : 0;
            } else if (força < tem) {
                novo = level.getRandom().nextInt(SORTE_IGUAL_OU_MAIS_FRACO) == 0 ? 0 : tem + 1;
            } else {
                novo = level.getRandom().nextInt(SORTE_IGUAL_OU_MAIS_FRACO) == 0 ? tem + 1 : 0;
            }

            if (novo != 0) {
                net.thaumcraft.occulta.curse.Curse.put(alvo, this.qual, novo);
                return true;
            }

            net.thaumcraft.occulta.curse.Curse.remove(alvo, this.qual);
            // e saindo ela leva os cinco que o azar deixa
            alvo.removeEffect(MobEffects.POISON);
            alvo.removeEffect(MobEffects.WEAKNESS);
            alvo.removeEffect(MobEffects.BLINDNESS);
            alvo.removeEffect(MobEffects.MINING_FATIGUE);
            alvo.removeEffect(MobEffects.SLOWNESS);
            return false;
        }
    }

    // ================================================================= os que chamam

    /**
     * Chamar uma criatura: o {@code RiteSummonCreature} do Witchery.
     *
     * <p>Antes de chamar, ele <b>olha o teto</b>. São três camadas de sete por sete em cima do círculo, com os
     * cantos de fora — e o que estiver sólido ali conta. <b>Mais de um estorvo e o rito desiste</b>, devolvendo
     * o que se ofereceu; e o bloco <b>do meio</b> conta por cem, ou seja: pôr uma laje em cima do glifo já
     * chega para ele recusar. É o que impede alguém de chamar um Wither dentro de uma caixa de obsidiana.
     *
     * <p><b>Um engano do original que fica:</b> ele percorre o desenho do teto até o <b>penúltimo</b> z, e
     * por isso a fila de trás nunca é olhada. O teto que ele mede é de sete por seis, e não de sete por sete.
     *
     * @param tipo  quem vem
     * @param coven quantas bruxas ele pede, porque alguns só se fazem em grupo
     */
    public record SummonCreature(java.util.function.Supplier<EntityType<? extends Mob>> tipo, int coven)
            implements Rite {
        /** O desenho do teto: 1 conta um, 2 conta cem. */
        private static final int[][] TETO = {
                {0, 0, 1, 1, 1, 0, 0},
                {0, 1, 1, 1, 1, 1, 0},
                {1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 2, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 1},
                {0, 1, 1, 1, 1, 1, 0},
                {0, 0, 1, 1, 1, 0, 0},
        };

        /** E quantos estorvos ele aguenta. */
        public static final int ESTORVOS = 1;

        /** Conta o que há de sólido nas três camadas em cima do círculo. */
        public static int estorvos(ServerLevel level, BlockPos onde) {
            int conta = 0;
            for (int y = 1; y <= 3; y++) {
                for (int z = 0; z < TETO.length - 1; z++) {
                    for (int x = 0; x < TETO[z].length; x++) {
                        int quanto = TETO[TETO.length - 1 - z][x];
                        if (quanto == 0) continue;
                        BlockPos casa = onde.offset(x - 3, y, z - 3);
                        if (!level.getBlockState(casa).isSolid()) continue;
                        conta += quanto == 2 ? 100 : 1;
                    }
                }
            }
            return conta;
        }

        @Override
        public List<RiteStep> steps(int coven) {
            return List.of((level, onde, ticks, rito) -> {
                if (ticks % 20L != 0L) return RiteStep.Result.STARTING;

                if (rito.coven() < this.coven) {
                    avisa(level, onde, rito, "message.thaumcraft.coven_too_small");
                    return RiteStep.Result.ABORTED_REFUND;
                }
                if (estorvos(level, onde) > ESTORVOS) {
                    level.sendParticles(ParticleTypes.LARGE_SMOKE, onde.getX() + 0.5, onde.getY() + 1.0,
                            onde.getZ() + 0.5, 32, 0.5, 2.0, 0.5, 0.02);
                    avisa(level, onde, rito, "message.thaumcraft.obstructed_circle");
                    return RiteStep.Result.ABORTED_REFUND;
                }

                Mob quem = this.tipo.get().create(level,
                        net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
                if (quem == null) return RiteStep.Result.ABORTED_REFUND;
                quem.snapTo(onde.getX() + 0.5, onde.getY() + 1.0, onde.getZ() + 0.5, 1.0f, 0.0f);
                quem.finalizeSpawn(level, level.getCurrentDifficultyAt(quem.blockPosition()),
                        net.minecraft.world.entity.EntitySpawnReason.TRIGGERED, null);
                /*
                 * E quem é chamado por alguém <b>fica</b>: o original marca o demônio como feito por mão de
                 * gente, e é essa marca que o impede de sumir quando ninguém está olhando.
                 */
                if (quem instanceof net.thaumcraft.occulta.demon.DemonEntity demônio) {
                    demônio.marcaComoChamado();
                }
                level.addFreshEntity(quem);

                level.sendParticles(ParticleTypes.PORTAL, onde.getX() + 0.5, onde.getY() + 1.0,
                        onde.getZ() + 0.5, 48, 0.5, 1.0, 0.5, 0.1);
                level.playSound(null, onde, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 1.0f);
                return RiteStep.Result.COMPLETED;
            });
        }
    }

    /**
     * Chamar os bichos do mato: o {@code RiteCallCreatures} do Witchery.
     *
     * <p>Ele não cria nada — ele <b>traz</b>. De sessenta em sessenta batidas olha <b>um oitavo</b> do mundo em
     * volta, uma caixa de cento e vinte e oito blocos num dos oito cantos, e teleporta até <b>dois</b> dos
     * bichos que achar ali para junto do círculo. Rodando os oito cantos, ele acaba por varrer tudo à volta.
     *
     * <p>É um rito de <b>sustento</b>: duzentas e cinquenta voltas, e só então para. E pede <b>três bruxas</b>
     * — sozinha, ninguém chama o mato inteiro.
     */
    public record CallCreatures(java.util.function.Supplier<List<EntityType<?>>> quais) implements Rite {
        /** Até onde o chamado chega, e de quanto em quanto ele olha. */
        public static final double ALCANCE = 128.0;
        public static final int EVERY = 60;
        /** Quantos de cada vez, e quantas voltas ao todo. */
        public static final int DE_CADA_VEZ = 2;
        public static final int VOLTAS = 250;
        /** O coven que ele pede. */
        public static final int COVEN = 3;
        /** E nada que esteja a menos disto é chamado: já está perto. */
        public static final double PERTO_DEMAIS = 32.0;

        @Override
        public List<RiteStep> steps(int coven) {
            return List.of((level, onde, ticks, rito) -> {
                if (ticks % EVERY != 0L) return RiteStep.Result.STARTING;
                if (rito.coven() < COVEN) {
                    avisa(level, onde, rito, "message.thaumcraft.coven_too_small");
                    return RiteStep.Result.ABORTED_REFUND;
                }

                int volta = rito.advance();
                chama(level, onde, this.quais.get(), volta % 8);
                return volta < VOLTAS ? RiteStep.Result.UPKEEP : RiteStep.Result.COMPLETED;
            });
        }

        /** Um dos oito cantos: quatro por baixo do círculo e quatro por cima. */
        private static void chama(ServerLevel level, BlockPos onde, List<EntityType<?>> quais, int canto) {
            double x = onde.getX();
            double y = onde.getY();
            double z = onde.getZ();
            boolean porCima = canto >= 4;
            boolean paraLeste = canto == 0 || canto == 2 || canto == 5 || canto == 7;
            boolean paraSul = canto == 2 || canto == 3 || canto == 5 || canto == 6;

            var caixa = new net.minecraft.world.phys.AABB(
                    paraLeste ? x : x - ALCANCE, porCima ? y + 1.0 : y - 10.0, paraSul ? z : z - ALCANCE,
                    paraLeste ? x + ALCANCE : x, porCima ? y + 10.0 : y, paraSul ? z + ALCANCE : z);

            int trazidos = 0;
            for (Mob bicho : level.getEntitiesOfClass(Mob.class, caixa)) {
                if (!quais.contains(bicho.getType())) continue;
                if (bicho.distanceToSqr(x, y, z) <= PERTO_DEMAIS) continue;
                var sorte = level.getRandom();
                bicho.snapTo(x - 2.0 + sorte.nextInt(5), y + 1.0, z - 2.0 + sorte.nextInt(5),
                        bicho.getYRot(), bicho.getXRot());
                level.sendParticles(ParticleTypes.PORTAL, bicho.getX(), bicho.getY() + 0.5, bicho.getZ(),
                        16, 0.3, 0.5, 0.3, 0.1);
                if (++trazidos >= DE_CADA_VEZ) return;
            }
        }
    }

    /**
     * A Chuva de Sapos: o {@code RiteRainOfToads} do Witchery.
     *
     * <p>Quatro raios, um de trinta em trinta batidas, e ao <b>quarto o céu fecha</b> — de cinco a quinze
     * minutos de chuva. Daí em diante <b>caem sapos</b>, de oito a dezessete de cada vez, num anel entre cinco
     * e dezesseis blocos do círculo e de oito a catorze blocos acima do chão.
     *
     * <p>Os sapos <b>têm hora para acabar</b>: meio minuto, e somem. É o {@code setTimeToLive} do original, e
     * sem ele a brincadeira deixava o mapa cheio de sapos para sempre.
     *
     * <p>Pede <b>uma bruxa</b> no coven: é o mais barato dos que pedem gente.
     */
    public record RainOfToads(int minRadius, int maxRadius, int bolts) implements Rite {
        public static final int EVERY = 30;
        public static final int VOLTAS = 200;
        public static final int COVEN = 1;
        /** Quantas voltas de raio antes de começarem a cair sapos. */
        public static final int RAIOS = 4;
        /** E quanto tempo um sapo chovido dura: os trinta segundos do original. */
        public static final int VIDA_DO_SAPO = 600;

        @Override
        public List<RiteStep> steps(int coven) {
            return List.of((level, onde, ticks, rito) -> {
                if (ticks % EVERY != 0L) return RiteStep.Result.STARTING;
                if (rito.coven() < COVEN) {
                    avisa(level, onde, rito, "message.thaumcraft.coven_too_small");
                    return RiteStep.Result.ABORTED_REFUND;
                }

                int fase = rito.advance();
                if (fase <= RAIOS) {
                    if (fase == RAIOS && !level.isRaining()) {
                        // o tempo do mundo de hoje mora num guardado à parte, e não no ServerLevel
                        int quanto = (300 + level.getRandom().nextInt(600)) * 20;
                        var tempo = level.getWeatherData();
                        tempo.setClearWeatherTime(0);
                        tempo.setRainTime(quanto);
                        tempo.setRaining(true);
                    }
                    this.raio(level, onde);
                    return RiteStep.Result.STARTING;
                }

                int quantos = level.getRandom().nextInt(this.bolts) + 8;
                for (int n = 0; n < quantos; n++) {
                    BlockPos casa = this.umaCasa(level, onde);
                    if (!level.getBlockState(casa).isAir()) continue;
                    var sapo = net.thaumcraft.occulta.OccultaEntities.TOAD.create(level,
                            net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
                    if (sapo == null) continue;
                    sapo.snapTo(casa.getX() + 0.5, casa.getY() + 8 + level.getRandom().nextInt(7),
                            casa.getZ() + 0.5, 0.0f, 0.0f);
                    sapo.choveu(VIDA_DO_SAPO);
                    level.addFreshEntity(sapo);
                }
                return fase < VOLTAS ? RiteStep.Result.UPKEEP : RiteStep.Result.COMPLETED;
            });
        }

        /** Uma casa do anel: entre o raio de dentro e o de fora, nunca no meio. */
        private BlockPos umaCasa(ServerLevel level, BlockPos onde) {
            int vão = this.maxRadius - this.minRadius;
            int ax = level.getRandom().nextInt(vão * 2 + 1);
            if (ax > vão) ax += this.minRadius * 2;
            int az = level.getRandom().nextInt(vão * 2 + 1);
            if (az > vão) az += this.minRadius * 2;
            int x = onde.getX() - this.maxRadius + ax;
            int z = onde.getZ() - this.maxRadius + az;
            return new BlockPos(x, level.getHeight(
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, x, z), z);
        }

        private void raio(ServerLevel level, BlockPos onde) {
            BlockPos casa = this.umaCasa(level, onde);
            var raio = net.minecraft.world.entity.EntityTypes.LIGHTNING_BOLT.create(level,
                    net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
            if (raio == null) return;
            raio.snapTo(casa.getX() + 0.5, onde.getY(), casa.getZ() + 0.5, 0.0f, 0.0f);
            level.addFreshEntity(raio);
        }
    }

    /**
     * O recado de quando um rito desiste: o {@code RiteRegistry.RiteError} do original.
     *
     * <p>Um tambor, e a frase em vermelho para quem o começou. Sem ele, um rito que recusa parece um rito
     * quebrado — e metade dos que recusam, recusam por coisas que se arranjam.
     */
    private static void avisa(ServerLevel level, BlockPos onde, ActiveRite rito, String oquê) {
        level.playSound(null, onde, SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
        Player gente = rito.starter(level);
        if (gente != null) {
            gente.sendSystemMessage(net.minecraft.network.chat.Component.translatable(oquê)
                    .withStyle(net.minecraft.ChatFormatting.RED));
        }
    }

    public static void register() {
        /*
         * Os dois do demônio: o que o despede e o que o chama.
         *
         * O de banir vem em duas — a de círculo, que paga em poder, e a <b>portátil</b>, que paga numa Pedra
         * Sintonizada Carregada. É a mesma escada que os outros ritos portáteis do mod usam, e aqui ela
         * importa mais do que nos outros: quem precisa de banir um demônio raramente está ao pé do altar.
         */
        RiteRegistry.register("tc.rite.banishdemon", new BanishDemon(9),
                new Sacrifice.Both(
                        new Sacrifice.Items(Items.BLAZE_POWDER,
                                net.thaumcraft.occulta.OccultaItems.WAYSTONE),
                        new Sacrifice.Power(2000.0f, 20)),
                new RiteRegistry.Ring(16, 0, 0), RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE);

        RiteRegistry.register("tc.rite.banishdemonportable", new BanishDemon(9),
                new Sacrifice.Items(Items.BLAZE_POWDER,
                        net.thaumcraft.occulta.OccultaItems.WAYSTONE,
                        net.thaumcraft.occulta.OccultaItems.ATTUNED_STONE_CHARGED),
                new RiteRegistry.Ring(16, 0, 0), RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE);

        /*
         * E o Inferno na Terra, que é o rito mais caro do mod: ele pede um Coração de Demônio para fazer
         * nascer demônios, e um aldeão vivo. Dois por cento do que sai dele é um demônio; o resto é o
         * Nether entrando pela porta que ele abriu.
         */
        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.hellonearth",
                new HellOnEarth(20, 15, 200.0f),
                new Sacrifice.Both(
                        new Sacrifice.Items(net.thaumcraft.occulta.OccultaItems.REDSTONE_SOUP,
                                net.thaumcraft.occulta.OccultaItems.DEMON_HEART,
                                net.thaumcraft.occulta.OccultaItems.WAYSTONE,
                                Items.NETHER_STAR),
                        new Sacrifice.Living(net.minecraft.world.entity.EntityTypes.VILLAGER),
                        new Sacrifice.Power(5000.0f, 20)),
                new RiteRegistry.Ring(0, 0, 16), new RiteRegistry.Ring(0, 28, 0),
                new RiteRegistry.Ring(0, 0, 40),
                java.util.EnumSet.of(RiteRegistry.When.OVERWORLD, RiteRegistry.When.NIGHT)));

        /*
         * E os dois jeitos de chamar um demônio, que são os dois ritos que o original tem para isso. O
         * primeiro pede um <b>aldeão vivo</b>; o segundo troca o aldeão por <b>duas pedras sintonizadas</b>,
         * uma delas carregada, e é por isso que se chama caro: as pedras custam mais do que um aldeão custa
         * a quem não se importa com aldeões.
         */
        /*
         * E o Rito da Expansão Gelada, que é o único que desiste e devolve o que se ofereceu quando o coven
         * é pequeno demais. Ele faz uma casa de gelo onde não havia casa nenhuma.
         */
        RiteRegistry.register("tc.rite.iceshell", new IceShell(8),
                new Sacrifice.Items(Items.DIAMOND_SWORD,
                        net.thaumcraft.occulta.OccultaItems.FROZEN_HEART,
                        net.thaumcraft.occulta.OccultaItems.ATTUNED_STONE_CHARGED),
                new RiteRegistry.Ring(16, 0, 0), RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE);

        RiteRegistry.register("tc.rite.summondemon",
                new SummonCreature(() -> net.thaumcraft.occulta.OccultaEntities.DEMON, 0),
                new Sacrifice.Both(
                        new Sacrifice.Items(net.thaumcraft.occulta.OccultaItems.REFINED_EVIL,
                                Items.BLAZE_POWDER, Items.ENDER_PEARL),
                        new Sacrifice.Living(net.minecraft.world.entity.EntityTypes.VILLAGER),
                        new Sacrifice.Power(3000.0f, 20)),
                RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE, new RiteRegistry.Ring(0, 0, 40));

        RiteRegistry.register("tc.rite.summondemonexpensive",
                new SummonCreature(() -> net.thaumcraft.occulta.OccultaEntities.DEMON, 0),
                new Sacrifice.Both(
                        new Sacrifice.Items(net.thaumcraft.occulta.OccultaItems.REFINED_EVIL,
                                Items.BLAZE_ROD, Items.ENDER_PEARL,
                                net.thaumcraft.occulta.OccultaItems.ATTUNED_STONE,
                                net.thaumcraft.occulta.OccultaItems.ATTUNED_STONE_CHARGED),
                        new Sacrifice.Power(3000.0f, 20)),
                RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE, new RiteRegistry.Ring(0, 0, 40));

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
                        new Sacrifice.Items(Items.STONE_AXE, net.thaumcraft.occulta.OccultaItems.QUICKLIME),
                        new Sacrifice.Power(3000.0f, 20)),
                new RiteRegistry.Ring(16, 0, 0), RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE,
                java.util.EnumSet.of(RiteRegistry.When.DAY)));

        // e a primeira das maldições que se abrem em roda
        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.curseblindness",
                new CurseOfBlindness(80, 15),
                new Sacrifice.Items(net.thaumcraft.occulta.OccultaItems.ATTUNED_STONE_CHARGED,
                        net.thaumcraft.occulta.OccultaItems.REDSTONE_SOUP,
                        net.thaumcraft.occulta.OccultaItems.REEK_OF_MISFORTUNE,
                        net.thaumcraft.occulta.OccultaItems.EXHALE_OF_THE_HORNED_ONE,
                        net.thaumcraft.occulta.OccultaItems.BREW_OF_INK,
                        Items.POISONOUS_POTATO, Items.FERMENTED_SPIDER_EYE, Items.DIAMOND),
                RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE, new RiteRegistry.Ring(16, 0, 0),
                java.util.EnumSet.noneOf(RiteRegistry.When.class)));

        // as versões maiores e as portáteis: o mesmo rito com outra escala e outro preço
        // A escada das ferramentas é a do original e é deliberada: pau para o pequeno, pedra para o maior,
        // ferro para o portátil — e o portátil troca o poder do Altar pela Pedra Sintonizada Carregada.
        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.stormlarge", new Storm(3, 7, 18),
                new Sacrifice.Both(
                        new Sacrifice.Items(Items.STONE_SWORD, net.thaumcraft.occulta.OccultaItems.WOOD_ASH),
                        new Sacrifice.Power(2000.0f, 20)),
                new RiteRegistry.Ring(16, 0, 0), RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE,
                java.util.EnumSet.noneOf(RiteRegistry.When.class)));

        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.stormportable", new Storm(3, 7, 18),
                new Sacrifice.Items(Items.IRON_SWORD, net.thaumcraft.occulta.OccultaItems.WOOD_ASH,
                        net.thaumcraft.occulta.OccultaItems.ATTUNED_STONE_CHARGED),
                new RiteRegistry.Ring(16, 0, 0), RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE,
                java.util.EnumSet.noneOf(RiteRegistry.When.class)));

        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.eclipseportable", new Eclipse(),
                new Sacrifice.Items(Items.IRON_AXE, net.thaumcraft.occulta.OccultaItems.QUICKLIME,
                        net.thaumcraft.occulta.OccultaItems.ATTUNED_STONE_CHARGED),
                new RiteRegistry.Ring(16, 0, 0), RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE,
                java.util.EnumSet.of(RiteRegistry.When.DAY)));

        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.fertilityportable", new Fertility(50, 15),
                new Sacrifice.Items(Items.BONE_MEAL,
                        net.thaumcraft.occulta.OccultaItems.HINT_OF_REBIRTH,
                        net.thaumcraft.occulta.OccultaItems.DIAMOND_VAPOUR,
                        net.thaumcraft.occulta.OccultaItems.QUICKLIME,
                        net.thaumcraft.occulta.OccultaItems.GYPSUM,
                        net.thaumcraft.occulta.OccultaItems.MUTANDIS_EXTREMIS,
                        net.thaumcraft.occulta.OccultaItems.ATTUNED_STONE_CHARGED),
                new RiteRegistry.Ring(16, 0, 0), RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE,
                java.util.EnumSet.noneOf(RiteRegistry.When.class)));

        // as três barreiras, que se sustentam enquanto houver poder
        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.barrier",
                new Barrier(4, 5, 1.2f, false, 0),
                new Sacrifice.Both(
                        new Sacrifice.Items(Items.OBSIDIAN, Items.REDSTONE),
                        new Sacrifice.Power(500.0f, 20)),
                new RiteRegistry.Ring(16, 0, 0), RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE,
                java.util.EnumSet.noneOf(RiteRegistry.When.class)));

        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.barrierlarge",
                new Barrier(6, 6, 1.4f, true, 0),
                new Sacrifice.Both(
                        new Sacrifice.Items(Items.OBSIDIAN, Items.GLOWSTONE_DUST),
                        new Sacrifice.Power(1000.0f, 20)),
                RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE, new RiteRegistry.Ring(28, 0, 0),
                java.util.EnumSet.noneOf(RiteRegistry.When.class)));

        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.barrierportable",
                new Barrier(6, 4, 0.0f, true, 60),
                new Sacrifice.Items(Items.OBSIDIAN,
                        net.thaumcraft.occulta.OccultaItems.ATTUNED_STONE_CHARGED),
                new RiteRegistry.Ring(16, 0, 0), RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE,
                java.util.EnumSet.noneOf(RiteRegistry.When.class)));

        // a Pedra Sintonizada Carregada, que é o que os ritos grandes pedem
        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.chargestone",
                new SummonItem(() -> new ItemStack(net.thaumcraft.occulta.OccultaItems.ATTUNED_STONE_CHARGED)),
                new Sacrifice.Both(
                        new Sacrifice.Items(net.thaumcraft.occulta.OccultaItems.ATTUNED_STONE,
                                Items.GLOWSTONE_DUST, Items.REDSTONE,
                                net.thaumcraft.occulta.OccultaItems.WOOD_ASH,
                                net.thaumcraft.occulta.OccultaItems.QUICKLIME),
                        new Sacrifice.Power(2000.0f, 20)),
                new RiteRegistry.Ring(16, 0, 0), RiteRegistry.Ring.NONE, new RiteRegistry.Ring(28, 0, 0),
                java.util.EnumSet.noneOf(RiteRegistry.When.class)));

        // e o maior estrago que o ofício faz
        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.volcano", new Volcano(8, 8),
                new Sacrifice.Items(Items.STONE, Items.MAGMA_CREAM, Items.GOLDEN_SWORD,
                        net.thaumcraft.occulta.OccultaItems.ATTUNED_STONE_CHARGED),
                RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE, new RiteRegistry.Ring(16, 0, 0),
                java.util.EnumSet.noneOf(RiteRegistry.When.class)));

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

        /*
         * O Rito da Infusão do Céu, que é de onde vem a vassoura que voa.
         *
         * <p>No original ele faz duas coisas: dá a Vassoura Encantada <b>e</b> infunde quem o faz com a
         * Infusão do Céu, que é um ramo inteiro de poderes. <b>Só a vassoura está portada</b>, e a infusão
         * fica declarada de fora no PORTE.md — ela precisa do sistema de infusões, que é outra coisa.
         *
         * <p>Os números são os dele: dois anéis, de dezesseis e vinte e oito glifos de ritual, só de noite,
         * com uma vassoura e um Unguento do Voo no chão e três mil de poder de altar.
         */
        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.infusionsky",
                new SummonItem(() -> new ItemStack(net.thaumcraft.occulta.OccultaItems.ENCHANTED_BROOM)),
                new Sacrifice.Both(
                        new Sacrifice.Items(net.thaumcraft.occulta.OccultaItems.BROOM,
                                net.thaumcraft.occulta.OccultaItems.FLYING_OINTMENT),
                        new Sacrifice.Power(3000.0f, 20)),
                new RiteRegistry.Ring(16, 0, 0), RiteRegistry.Ring.NONE, new RiteRegistry.Ring(28, 0, 0),
                java.util.EnumSet.of(RiteRegistry.When.NIGHT)));

        /*
         * A Maldição da Praga: um anel de oitenta blocos que seca tudo o que encontra. Pede a Pedra
         * Sintonizada Carregada, a Sopa de Redstone, o Fedor do Azar, olho de aranha, creme de magma, carne
         * podre e um diamante — e um anel de vinte e oito glifos no de fora.
         */
        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.curseblight",
                new Blight(80, 15),
                new Sacrifice.Items(net.thaumcraft.occulta.OccultaItems.ATTUNED_STONE_CHARGED,
                        net.thaumcraft.occulta.OccultaItems.REDSTONE_SOUP,
                        net.thaumcraft.occulta.OccultaItems.REEK_OF_MISFORTUNE,
                        Items.SPIDER_EYE, Items.MAGMA_CREAM, Items.ROTTEN_FLESH, Items.DIAMOND),
                RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE, new RiteRegistry.Ring(28, 0, 0),
                java.util.EnumSet.noneOf(RiteRegistry.When.class)));

        // ---------------------------------------------------------- o prado, e as bonecas corrompidas

        /*
         * O Poder da Natureza: o contrário da Praga. Cento e cinquenta voltas plantando relva, mudas e
         * flores num raio de catorze. Pede o de Brotação e as sete mudas, num anel de vinte e oito.
         */
        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.naturespower",
                new NaturesPower(14, 8, 150, 2),
                new Sacrifice.Items(net.thaumcraft.occulta.OccultaItems.BREW_OF_SPROUTING,
                        net.thaumcraft.occulta.OccultaItems.WOOD.get("rowan_sapling"),
                        net.thaumcraft.occulta.OccultaItems.WOOD.get("alder_sapling"),
                        net.thaumcraft.occulta.OccultaItems.WOOD.get("hawthorn_sapling"),
                        Items.OAK_SAPLING, Items.SPRUCE_SAPLING, Items.BIRCH_SAPLING,
                        Items.JUNGLE_SAPLING),
                new RiteRegistry.Ring(28, 0, 0), RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE,
                java.util.EnumSet.noneOf(RiteRegistry.When.class)));

        /*
         * E o que corrompe as bonecas de proteção de quem o vínculo prender — dez delas. Pede o gato, e sem
         * ele recusa. Sete mil de poder, num anel de vinte e oito no de fora.
         */
        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.corruptvoodooprotection",
                new CursePoppets(1),
                new Sacrifice.Both(
                        new Sacrifice.Items(net.thaumcraft.occulta.OccultaItems.TAGLOCK,
                                net.thaumcraft.occulta.OccultaItems.EXHALE_OF_THE_HORNED_ONE,
                                net.thaumcraft.occulta.OccultaItems.VOODOO_PROTECTION_POPPET,
                                Items.BLAZE_POWDER,
                                net.thaumcraft.occulta.OccultaItems.SPECTRAL_DUST),
                        new Sacrifice.Power(7000.0f, 20)),
                RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE, new RiteRegistry.Ring(28, 0, 0),
                java.util.EnumSet.noneOf(RiteRegistry.When.class)));

        // ---------------------------------------------------------- os que empurram, puxam e repintam

        /*
         * O Rito da Proteção: um anel de quatro blocos que empurra tudo para fora, por 0,8 de poder por
         * batida, para sempre. Uma pena e um pó de redstone, num anel de dezesseis glifos.
         */
        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.protection",
                new PushCircle(4, 0.8f, false),
                new Sacrifice.Both(
                        new Sacrifice.Items(Items.FEATHER, Items.REDSTONE),
                        new Sacrifice.Power(500.0f, 20)),
                new RiteRegistry.Ring(16, 0, 0), RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE,
                java.util.EnumSet.noneOf(RiteRegistry.When.class)));

        /* E o do Aprisionamento, que é o mesmo com o sinal trocado: puxa tudo para dentro. */
        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.imprisonment",
                new PushCircle(4, 0.8f, true),
                new Sacrifice.Both(
                        new Sacrifice.Items(Items.SLIME_BALL, Items.REDSTONE),
                        new Sacrifice.Power(500.0f, 20)),
                new RiteRegistry.Ring(16, 0, 0), RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE,
                java.util.EnumSet.noneOf(RiteRegistry.When.class)));

        /*
         * O dos minérios que sobem: oito blocos de lado, trinta camadas, ferro — e ouro também, se o coven
         * estiver cheio. Num anel de quarenta glifos no meio.
         */
        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.teleportironore",
                new TransposeOres(8, 30, List.of(Blocks.IRON_ORE, Blocks.GOLD_ORE)),
                new Sacrifice.Items(Items.ENDER_PEARL, Items.IRON_INGOT, Items.BLAZE_POWDER,
                        net.thaumcraft.occulta.OccultaItems.DIAMOND_VAPOUR,
                        net.thaumcraft.occulta.OccultaItems.ATTUNED_STONE_CHARGED),
                RiteRegistry.Ring.NONE, new RiteRegistry.Ring(40, 0, 0), RiteRegistry.Ring.NONE,
                java.util.EnumSet.noneOf(RiteRegistry.When.class)));

        /*
         * E o que repinta um anel de glifos com o giz que se largar: gesso e a Arthana, mil de poder, e
         * nenhum anel pedido — porque o anel que ele muda é o que já lá estiver.
         */
        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.glyphictransform",
                new GlyphicTransformation(),
                new Sacrifice.Both(
                        new Sacrifice.Items(net.thaumcraft.occulta.OccultaItems.GYPSUM,
                                net.thaumcraft.occulta.OccultaItems.ARTHANA),
                        new Sacrifice.Power(1000.0f, 20)),
                RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE,
                java.util.EnumSet.noneOf(RiteRegistry.When.class)));

        // ---------------------------------------------------------- as maldições
        // Todas pedem um VÍNCULO: é por ele que elas atravessam o mundo e pegam em quem não está lá.
        // Pôr pede o Bafo do Cornudo e um anel de vinte e oito glifos NO DE FORA; tirar pede o Sopro da
        // Deusa e um anel de dezesseis NO DE DENTRO. Dois mil de poder, menos o Pesadelo, que pede dez mil.
        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.cursecreature",
                new CurseCreature(true, net.thaumcraft.occulta.curse.Curse.CURSED, 1),
                new Sacrifice.Both(
                        new Sacrifice.Items(net.thaumcraft.occulta.OccultaItems.TAGLOCK,
                                net.thaumcraft.occulta.OccultaItems.EXHALE_OF_THE_HORNED_ONE,
                                Items.SPIDER_EYE, Items.GUNPOWDER,
                                net.thaumcraft.occulta.OccultaItems.BREW_GROTESQUE),
                        new Sacrifice.Power(2000.0f, 20)),
                RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE, new RiteRegistry.Ring(28, 0, 0),
                java.util.EnumSet.noneOf(RiteRegistry.When.class)));

        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.curseinsanity",
                new CurseCreature(true, net.thaumcraft.occulta.curse.Curse.INSANITY, 1),
                new Sacrifice.Both(
                        new Sacrifice.Items(net.thaumcraft.occulta.OccultaItems.TAGLOCK,
                                net.thaumcraft.occulta.OccultaItems.EXHALE_OF_THE_HORNED_ONE,
                                Items.POISONOUS_POTATO, Items.SUGAR,
                                net.thaumcraft.occulta.OccultaItems.BREW_GROTESQUE),
                        new Sacrifice.Power(2000.0f, 20)),
                RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE, new RiteRegistry.Ring(28, 0, 0),
                java.util.EnumSet.noneOf(RiteRegistry.When.class)));

        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.cursenightmare",
                new CurseCreature(true, net.thaumcraft.occulta.curse.Curse.WAKING_NIGHTMARE, 1),
                new Sacrifice.Both(
                        new Sacrifice.Items(net.thaumcraft.occulta.OccultaItems.TAGLOCK,
                                net.thaumcraft.occulta.OccultaItems.EXHALE_OF_THE_HORNED_ONE,
                                net.thaumcraft.occulta.OccultaItems.MELLIFLUOUS_HUNGER,
                                net.thaumcraft.occulta.OccultaItems.TORMENTED_TWINE, Items.DIAMOND),
                        new Sacrifice.Power(10000.0f, 20)),
                RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE, new RiteRegistry.Ring(28, 0, 0),
                java.util.EnumSet.noneOf(RiteRegistry.When.class)));

        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.cursesinking",
                new CurseCreature(true, net.thaumcraft.occulta.curse.Curse.SINKING, 1),
                new Sacrifice.Both(
                        new Sacrifice.Items(net.thaumcraft.occulta.OccultaItems.TAGLOCK,
                                net.thaumcraft.occulta.OccultaItems.EXHALE_OF_THE_HORNED_ONE,
                                Items.INK_SAC, Items.NETHER_WART,
                                net.thaumcraft.occulta.OccultaItems.BREW_GROTESQUE),
                        new Sacrifice.Power(2000.0f, 20)),
                RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE, new RiteRegistry.Ring(28, 0, 0),
                java.util.EnumSet.noneOf(RiteRegistry.When.class)));

        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.removecurse",
                new CurseCreature(false, net.thaumcraft.occulta.curse.Curse.CURSED, 1),
                new Sacrifice.Both(
                        new Sacrifice.Items(net.thaumcraft.occulta.OccultaItems.TAGLOCK,
                                net.thaumcraft.occulta.OccultaItems.BREATH_OF_THE_GODDESS,
                                Items.GHAST_TEAR, Items.GUNPOWDER,
                                net.thaumcraft.occulta.OccultaItems.BREW_OF_LOVE),
                        new Sacrifice.Power(2000.0f, 20)),
                new RiteRegistry.Ring(16, 0, 0), RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE,
                java.util.EnumSet.noneOf(RiteRegistry.When.class)));

        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.removeinsanity",
                new CurseCreature(false, net.thaumcraft.occulta.curse.Curse.INSANITY, 1),
                new Sacrifice.Both(
                        new Sacrifice.Items(net.thaumcraft.occulta.OccultaItems.TAGLOCK,
                                net.thaumcraft.occulta.OccultaItems.BREATH_OF_THE_GODDESS,
                                Items.BAKED_POTATO, Items.SUGAR,
                                net.thaumcraft.occulta.OccultaItems.BREW_OF_LOVE),
                        new Sacrifice.Power(2000.0f, 20)),
                new RiteRegistry.Ring(16, 0, 0), RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE,
                java.util.EnumSet.noneOf(RiteRegistry.When.class)));

        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.removesinking",
                new CurseCreature(false, net.thaumcraft.occulta.curse.Curse.SINKING, 1),
                new Sacrifice.Both(
                        new Sacrifice.Items(net.thaumcraft.occulta.OccultaItems.TAGLOCK,
                                net.thaumcraft.occulta.OccultaItems.BREATH_OF_THE_GODDESS,
                                Items.BONE_MEAL, Items.NETHER_WART,
                                net.thaumcraft.occulta.OccultaItems.BREW_OF_THE_DEPTHS),
                        new Sacrifice.Power(2000.0f, 20)),
                new RiteRegistry.Ring(16, 0, 0), RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE,
                java.util.EnumSet.noneOf(RiteRegistry.When.class)));

        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.curenightmare",
                new CurseCreature(false, net.thaumcraft.occulta.curse.Curse.WAKING_NIGHTMARE, 1),
                new Sacrifice.Both(
                        new Sacrifice.Items(net.thaumcraft.occulta.OccultaItems.TAGLOCK,
                                net.thaumcraft.occulta.OccultaItems.BREATH_OF_THE_GODDESS,
                                Items.GOLDEN_CARROT,
                                net.thaumcraft.occulta.OccultaItems.TORMENTED_TWINE,
                                net.thaumcraft.occulta.OccultaItems.BREW_OF_LOVE),
                        new Sacrifice.Power(2000.0f, 20)),
                new RiteRegistry.Ring(16, 0, 0), RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE,
                java.util.EnumSet.noneOf(RiteRegistry.When.class)));

        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.cureoverheating",
                new CurseCreature(false, net.thaumcraft.occulta.curse.Curse.OVERHEATING, 1),
                new Sacrifice.Both(
                        new Sacrifice.Items(net.thaumcraft.occulta.OccultaItems.TAGLOCK,
                                net.thaumcraft.occulta.OccultaItems.BREATH_OF_THE_GODDESS,
                                net.thaumcraft.occulta.OccultaItems.ICY_NEEDLE, Items.BLAZE_POWDER,
                                net.thaumcraft.occulta.OccultaItems.BREW_OF_THE_DEPTHS),
                        new Sacrifice.Power(2000.0f, 20)),
                new RiteRegistry.Ring(16, 0, 0), RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE,
                java.util.EnumSet.noneOf(RiteRegistry.When.class)));

        // ---------------------------------------------------------- os que chamam

        /*
         * Chamar uma Bruxa: a do próprio jogo, e não a do coven. Dois mil de poder e um anel de dezesseis
         * glifos, no anel de FORA — que é o que o torna mais caro de desenhar do que de pagar.
         */
        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.summonwitch",
                new SummonCreature(() -> net.minecraft.world.entity.EntityTypes.WITCH, 0),
                new Sacrifice.Both(
                        new Sacrifice.Items(net.thaumcraft.occulta.OccultaItems.DIAMOND_VAPOUR,
                                net.thaumcraft.occulta.OccultaItems.EXHALE_OF_THE_HORNED_ONE,
                                Items.GHAST_TEAR, net.thaumcraft.occulta.OccultaItems.ARTHANA,
                                Items.SPIDER_EYE),
                        new Sacrifice.Power(2000.0f, 20)),
                RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE, new RiteRegistry.Ring(16, 0, 0),
                java.util.EnumSet.noneOf(RiteRegistry.When.class)));

        /*
         * Chamar o Wither. Pede uma caveira de wither, Vapor de Diamante, uma pérola — e um aldeão vivo
         * dentro do círculo. Quatro mil de poder, e dois anéis: vinte e oito e quarenta.
         */
        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.summonwither",
                new SummonCreature(() -> net.minecraft.world.entity.EntityTypes.WITHER, 0),
                new Sacrifice.Both(
                        new Sacrifice.Both(
                                new Sacrifice.Items(Items.WITHER_SKELETON_SKULL,
                                        net.thaumcraft.occulta.OccultaItems.DIAMOND_VAPOUR,
                                        Items.ENDER_PEARL),
                                new Sacrifice.Living(net.minecraft.world.entity.EntityTypes.VILLAGER)),
                        new Sacrifice.Power(4000.0f, 20)),
                RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE, new RiteRegistry.Ring(28, 0, 0),
                java.util.EnumSet.noneOf(RiteRegistry.When.class)));

        /*
         * Chamar os Bichos: leite, feno, maçã, carne, peixe, cogumelo, cenoura e semente — a despensa toda —
         * e seis mil de poder, num anel de quarenta glifos no MEIO. Pede três bruxas.
         */
        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.callbeasts",
                new CallCreatures(() -> List.of(
                        net.minecraft.world.entity.EntityTypes.PIG,
                        net.minecraft.world.entity.EntityTypes.CHICKEN,
                        net.minecraft.world.entity.EntityTypes.COW,
                        net.minecraft.world.entity.EntityTypes.SHEEP,
                        net.minecraft.world.entity.EntityTypes.MOOSHROOM,
                        net.minecraft.world.entity.EntityTypes.WOLF,
                        net.minecraft.world.entity.EntityTypes.CAT)),
                new Sacrifice.Both(
                        new Sacrifice.Items(Items.MILK_BUCKET, Items.HAY_BLOCK, Items.APPLE,
                                Items.BEEF, Items.COD, Items.BROWN_MUSHROOM, Items.CARROT,
                                Items.WHEAT_SEEDS),
                        new Sacrifice.Power(6000.0f, 20)),
                RiteRegistry.Ring.NONE, new RiteRegistry.Ring(40, 0, 0), RiteRegistry.Ring.NONE,
                java.util.EnumSet.noneOf(RiteRegistry.When.class)));

        /*
         * A Chuva de Sapos: quatro raios, o céu que fecha, e sapos a cair num anel de cinco a dezesseis
         * blocos. Um anel de vinte e oito glifos no de fora, e uma bruxa de coven.
         */
        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.rainoftoads",
                new RainOfToads(5, 16, 10),
                new Sacrifice.Items(net.thaumcraft.occulta.OccultaItems.ATTUNED_STONE_CHARGED,
                        net.thaumcraft.occulta.OccultaItems.REDSTONE_SOUP,
                        net.thaumcraft.occulta.OccultaItems.REEK_OF_MISFORTUNE,
                        net.thaumcraft.occulta.OccultaItems.TOE_OF_FROG,
                        Items.WATER_BUCKET,
                        net.thaumcraft.occulta.OccultaItems.BELLADONNA_FLOWER),
                RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE, new RiteRegistry.Ring(28, 0, 0),
                java.util.EnumSet.noneOf(RiteRegistry.When.class)));

        /*
         * O <b>Rito da Infusão da Luz</b>, que é o mais barato dos quatro: dois mil de poder em vez de
         * quatro mil. É por ele que quase toda gente começa.
         */
        RiteRegistry.register("tc.rite.infusionlight",
                new InfusePlayers(net.thaumcraft.occulta.infusion.Infusions.daquele(1),
                        net.thaumcraft.occulta.infusion.Infusions.CARGAS,
                        net.thaumcraft.occulta.infusion.Infusions.ALCANCE),
                new Sacrifice.Both(
                        new Sacrifice.Items(
                                net.thaumcraft.occulta.OccultaItems.GHOST_OF_THE_LIGHT),
                        new Sacrifice.Power(2000.0f, 20)),
                new RiteRegistry.Ring(16, 0, 0), new RiteRegistry.Ring(28, 0, 0),
                RiteRegistry.Ring.NONE);

        /*
         * O <b>Rito da Infusão do Outro Lugar</b>, que é o primeiro dos quatro ritos que mudam o
         * próprio corpo de quem os faz. Ele <b>mata quase</b>: cem de dano mágico a tudo o que for gente
         * num raio de quatro blocos, e só quem sobreviver fica infundido.
         */
        RiteRegistry.register("tc.rite.infusionender",
                new InfusePlayers(net.thaumcraft.occulta.infusion.Infusions.daquele(3),
                        net.thaumcraft.occulta.infusion.Infusions.CARGAS,
                        net.thaumcraft.occulta.infusion.Infusions.ALCANCE),
                new Sacrifice.Both(
                        new Sacrifice.Items(
                                net.thaumcraft.occulta.OccultaItems.SPIRIT_OF_OTHERWHERE),
                        new Sacrifice.Power(4000.0f, 20)),
                new RiteRegistry.Ring(0, 16, 0), new RiteRegistry.Ring(0, 28, 0),
                RiteRegistry.Ring.NONE);

        /*
         * O Rito da Infusão do Futuro, que faz aparecer a Bola de Cristal — e, de caminho, ensina a
         * <b>ler a sorte</b> a quem o fez. É o único rito do mod que muda alguma coisa <b>em quem o
         * faz</b>, e não no mundo: a bola que sai dele é só um objeto, e qualquer um a pode roubar; o
         * que não se rouba é saber usá-la.
         */
        RiteRegistry.register(new RiteRegistry.Entry("tc.rite.infusionfuture",
                new SummonItem(() -> new ItemStack(net.thaumcraft.occulta.OccultaItems.CRYSTAL_BALL)),
                new Sacrifice.Both(
                        new Sacrifice.Items(net.thaumcraft.occulta.OccultaItems.QUARTZ_SPHERE,
                                Items.GOLD_INGOT, net.thaumcraft.occulta.OccultaItems.HAPPENSTANCE_OIL),
                        new Sacrifice.Power(2000.0f, 20)),
                new RiteRegistry.Ring(28, 0, 0), RiteRegistry.Ring.NONE, RiteRegistry.Ring.NONE,
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
