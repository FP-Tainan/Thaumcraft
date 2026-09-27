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
