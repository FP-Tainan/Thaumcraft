package net.thaumcraft.occulta.brew;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Os efeitos de cozimento que mexem no <b>lugar</b>, e não em quem passa: os {@code BrewAction*} da pasta
 * {@code action/effect} do Witchery.
 *
 * <p>Estes só acontecem quando o frasco se atira, porque é aí que há um lugar onde ele bateu. Bebido, um
 * cozimento destes não faz nada — e é assim no original.
 */
public final class BrewWorldActions {
    private BrewWorldActions() {
    }

    /**
     * Um efeito que só mexe no lugar.
     *
     * <p>Ele gasta espaço no caldeirão como qualquer efeito, mas o que faz está no {@link #applyToBlock}.
     */
    public abstract static class WorldEffect extends BrewAction {
        private final int weight;

        protected WorldEffect(Item key, BrewName.Text namePart, int power, int weight) {
            super(key, namePart, power);
            this.weight = weight;
        }

        @Override
        public final boolean isEffect() {
            return true;
        }

        @Override
        public final boolean augmentCapacity(BrewCapacity espaço) {
            return espaço.consume(this.weight);
        }

        @Override
        public final void applyToEntity(Level level, LivingEntity quem, BrewModifiers temperos) {
            if (temperos.disableEntityTarget) return;
            if (level instanceof ServerLevel server) this.onEntity(server, quem, temperos);
            temperos.reset();
        }

        @Override
        public final void applyToBlock(ServerLevel level, BlockPos onde, Direction lado, int raio,
                                       BrewModifiers temperos) {
            if (temperos.disableBlockTarget) return;
            this.onBlock(level, onde, lado, raio, temperos);
            temperos.reset();
        }

        /** O {@code doApplyToBlock}. */
        protected abstract void onBlock(ServerLevel level, BlockPos onde, Direction lado, int raio,
                                        BrewModifiers temperos);

        /** O {@code doApplyToEntity}: a maioria destes não faz nada em quem passa. */
        protected void onEntity(ServerLevel level, LivingEntity quem, BrewModifiers temperos) {
        }
    }

    // ------------------------------------------------------------------ a derrubada

    /**
     * A Derrubada: o {@code BrewActionFelling}, que vem de um fio na panela.
     *
     * <p>Todo tronco dentro da bola cai — e cai como se alguém o tivesse cortado, largando o que largaria.
     */
    public static class Felling extends WorldEffect {
        private final int strengthReduction;

        public Felling(Item key, int strengthReduction, int power, int weight) {
            super(key, new BrewName.Text("tc.brew.felling"), power, weight);
            this.strengthReduction = strengthReduction;
        }

        @Override
        protected void onBlock(ServerLevel level, BlockPos onde, Direction lado, int raio, BrewModifiers temperos) {
            int alcance = Math.max(raio - (this.strengthReduction - 1) - 1, 1);
            BrewShapes.ball(onde, alcance, lugar -> {
                BlockState oQueTem = level.getBlockState(lugar);
                if (!oQueTem.is(BlockTags.LOGS)) return;
                Block.dropResources(oQueTem, level, lugar);
                level.removeBlock(lugar, false);
            });
        }
    }

    // ------------------------------------------------------------------ a poda

    /** A Poda: o cogumelo marrom na panela leva folha e mato de roldão. */
    public static class Pruning extends WorldEffect {
        public Pruning(Item key, int power, int weight) {
            super(key, new BrewName.Text("tc.brew.pruning"), power, weight);
        }

        @Override
        protected void onBlock(ServerLevel level, BlockPos onde, Direction lado, int raio, BrewModifiers temperos) {
            int alcance = Math.max(raio - 1, 1);
            BrewShapes.ball(onde, alcance, lugar -> {
                BlockState oQueTem = level.getBlockState(lugar);
                boolean folha = oQueTem.is(BlockTags.LEAVES);
                boolean mato = oQueTem.is(BlockTags.REPLACEABLE_BY_TREES) && !oQueTem.isAir();
                if (!folha && !mato) return;
                Block.dropResources(oQueTem, level, lugar);
                level.removeBlock(lugar, false);
            });
        }
    }

    // ------------------------------------------------------------------ a pulverização

    /**
     * A Pulverização: a pederneira desfaz a pedra um degrau de cada vez.
     *
     * <p>Pedra vira pedregulho, pedregulho vira cascalho, cascalho e arenito viram areia — e a areia, que já não
     * tem para onde ir, se solta do chão e vira item.
     */
    public static class Pulverisation extends WorldEffect {
        public Pulverisation(Item key, int power, int weight) {
            super(key, new BrewName.Text("tc.brew.pulverisation"), power, weight);
        }

        @Override
        protected void onBlock(ServerLevel level, BlockPos onde, Direction lado, int raio, BrewModifiers temperos) {
            BrewShapes.ball(onde, Math.max(raio, 1), lugar -> {
                BlockState oQueTem = level.getBlockState(lugar);
                if (oQueTem.is(Blocks.STONE)) {
                    level.setBlockAndUpdate(lugar, Blocks.COBBLESTONE.defaultBlockState());
                } else if (oQueTem.is(Blocks.COBBLESTONE)) {
                    level.setBlockAndUpdate(lugar, Blocks.GRAVEL.defaultBlockState());
                } else if (oQueTem.is(Blocks.GRAVEL) || oQueTem.is(Blocks.SANDSTONE)) {
                    level.setBlockAndUpdate(lugar, Blocks.SAND.defaultBlockState());
                } else if (oQueTem.is(Blocks.SAND)) {
                    level.removeBlock(lugar, false);
                    Block.popResource(level, lugar, new ItemStack(Blocks.SAND));
                }
            });
        }
    }

    // ------------------------------------------------------------------ a vitória-régia

    /** A Vitória-régia: onde há água, nasce folha por cima dela. */
    public static class Lilify extends WorldEffect {
        public Lilify(Item key, int power, int weight) {
            super(key, new BrewName.Text("tc.brew.lilify"), power, weight);
        }

        @Override
        protected void onBlock(ServerLevel level, BlockPos onde, Direction lado, int raio, BrewModifiers temperos) {
            BlockPos lugar = onde.relative(lado);
            // sobe-se até achar água com céu aberto em cima, que é o que o original faz
            while (lugar.getY() < level.getMaxY()
                    && (!level.getBlockState(lugar).is(Blocks.WATER) || !level.isEmptyBlock(lugar.above()))) {
                lugar = lugar.above();
            }
            if (!level.getBlockState(lugar).is(Blocks.WATER) || !level.isEmptyBlock(lugar.above())) return;
            level.setBlockAndUpdate(lugar.above(), Blocks.LILY_PAD.defaultBlockState());
        }

        @Override
        protected void onEntity(ServerLevel level, LivingEntity quem, BrewModifiers temperos) {
            this.onBlock(level, quem.blockPosition(), Direction.UP, 1, temperos);
        }
    }

    // ------------------------------------------------------------------ o plantio

    /**
     * O Plantio: as sementes que estiverem no chão em volta plantam-se sozinhas.
     *
     * <p>Não é o cozimento que traz semente nenhuma — ele só põe no lugar a que já estiver lá largada. É o
     * {@code BrewActionPlanting} do original.
     */
    public static class Planting extends WorldEffect {
        public Planting(Item key, int power, int weight) {
            super(key, new BrewName.Text("tc.brew.planting"), power, weight);
        }

        @Override
        protected void onBlock(ServerLevel level, BlockPos onde, Direction lado, int raio, BrewModifiers temperos) {
            int alcance = raio + temperos.getStrength();
            var caixa = new net.minecraft.world.phys.AABB(onde).inflate(alcance);
            java.util.List<net.minecraft.world.entity.item.ItemEntity> largadas =
                    level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, caixa,
                            item -> item.getItem().getItem() instanceof net.minecraft.world.item.BlockItem);
            if (largadas.isEmpty()) return;

            var quemPlanta = new java.util.ArrayDeque<>(largadas);
            BrewShapes.filledCircle(onde, alcance, lugar -> {
                var semente = quemPlanta.peek();
                if (semente == null) return;
                for (int dy = -2; dy <= 2; dy++) {
                    BlockPos chão = lugar.offset(0, dy, 0);
                    if (!level.getBlockState(chão).isSolidRender() || !level.isEmptyBlock(chão.above())) continue;
                    ItemStack stack = semente.getItem();
                    if (!(stack.getItem() instanceof net.minecraft.world.item.BlockItem bloco)) break;
                    BlockState vai = bloco.getBlock().defaultBlockState();
                    if (!vai.canSurvive(level, chão.above())) break;
                    level.setBlockAndUpdate(chão.above(), vai);
                    stack.shrink(1);
                    if (stack.isEmpty()) {
                        semente.discard();
                        quemPlanta.poll();
                    }
                    break;
                }
            });
        }
    }

    // ------------------------------------------------------------------ a praga

    /**
     * A Praga: o {@code BrewActionBlight}, que vem de uma batata venenosa.
     *
     * <p>O mato some, a flor e a plantação viram arbusto seco, a terra arada vira areia — e o chão em volta
     * apodrece, uma vez em cinco para areia e uma em cinco para terra.
     *
     * <p>Em quem passa: o aldeão vira zumbi, a vaca vira vaca-cogumelo, e os outros bichos apanham vinte de dano.
     */
    public static class Blight extends WorldEffect {
        public Blight(Item key, int power, int weight) {
            super(key, new BrewName.Text("tc.brew.blight"), power, weight);
        }

        @Override
        protected void onBlock(ServerLevel level, BlockPos onde, Direction lado, int raio, BrewModifiers temperos) {
            BlockPos meio = level.getBlockState(onde).canBeReplaced() ? onde.below() : onde;
            BrewShapes.filledCircle(meio.above(), raio, lugar -> {
                BlockState oQueTem = level.getBlockState(lugar);
                BlockState debaixo = level.getBlockState(lugar.below());
                if (oQueTem.is(Blocks.SHORT_GRASS) || oQueTem.is(Blocks.FERN)) {
                    level.removeBlock(lugar, false);
                    this.rot(level, lugar.below(), debaixo);
                } else if (oQueTem.is(BlockTags.SMALL_FLOWERS) || oQueTem.is(BlockTags.CROPS)
                        || oQueTem.is(Blocks.PUMPKIN) || oQueTem.is(Blocks.MELON)
                        || oQueTem.is(Blocks.PUMPKIN_STEM) || oQueTem.is(Blocks.MELON_STEM)
                        || oQueTem.is(Blocks.TALL_GRASS) || oQueTem.is(Blocks.LARGE_FERN)) {
                    level.setBlockAndUpdate(lugar, Blocks.DEAD_BUSH.defaultBlockState());
                    this.rot(level, lugar.below(), debaixo);
                } else if (oQueTem.is(Blocks.FARMLAND)) {
                    level.setBlockAndUpdate(lugar, Blocks.SAND.defaultBlockState());
                } else if (oQueTem.isSolidRender()) {
                    this.rot(level, lugar, oQueTem);
                } else if (debaixo.isSolidRender()) {
                    this.rot(level, lugar.below(), debaixo);
                }
            });
        }

        /** O {@code blightGround}: o chão bom apodrece, uma vez em cinco para cada coisa. */
        private void rot(ServerLevel level, BlockPos onde, BlockState oQueEra) {
            if (!oQueEra.is(Blocks.DIRT) && !oQueEra.is(Blocks.GRASS_BLOCK) && !oQueEra.is(Blocks.MYCELIUM)
                    && !oQueEra.is(Blocks.FARMLAND)) {
                return;
            }
            int sorte = level.getRandom().nextInt(5);
            if (sorte == 0) level.setBlockAndUpdate(onde, Blocks.SAND.defaultBlockState());
            else if (sorte == 1) level.setBlockAndUpdate(onde, Blocks.DIRT.defaultBlockState());
        }

        @Override
        protected void onEntity(ServerLevel level, LivingEntity quem, BrewModifiers temperos) {
            int força = temperos.getStrength();
            if (quem instanceof Villager aldeão && level.getRandom().nextInt(Math.max(10 - força * 2, 1)) == 0) {
                aldeão.convertTo(net.minecraft.world.entity.EntityTypes.ZOMBIE_VILLAGER,
                        net.minecraft.world.entity.ConversionParams.single(aldeão, false, false), virou -> {
                        });
            } else if (quem instanceof Cow vaca && level.getRandom().nextInt(Math.max(20 - força * 3, 1)) == 0) {
                vaca.convertTo(net.minecraft.world.entity.EntityTypes.MOOSHROOM,
                        net.minecraft.world.entity.ConversionParams.single(vaca, false, false), virou -> {
                        });
            } else if (quem instanceof Animal && level.getRandom().nextInt(força > 1 ? 2 : 3) == 0) {
                quem.hurtServer(level, level.damageSources().magic(), 20.0f);
            }
        }
    }
}
