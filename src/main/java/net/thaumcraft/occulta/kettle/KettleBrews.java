package net.thaumcraft.occulta.kettle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

/**
 * O que cada frasco do Caldeirão de Pote faz ao bater: o {@code EntityWitchProjectile} do Witchery.
 *
 * <p>Ao contrário dos cozimentos do Caldeirão da Bruxa, que se montam por ordem e ganham o efeito da mistura,
 * estes são <b>um efeito cada</b>, escrito à mão no original — e são eles que fazem do ofício uma coisa de andar
 * com frascos no cinto.
 *
 * <p><b>Do original fica de fora, declarado:</b> o frasco <b>reforçado</b>, que estende o alcance de cada um
 * destes efeitos. Lá ele depende de se ter um <b>familiar de cozimento</b> acordado, e os familiares não estão
 * portados; sem eles, o frasco é sempre o comum.
 */
public final class KettleBrews {
    private KettleBrews() {
    }

    /** Cada frasco do pote, e o que ele faz onde bate. */
    public enum Kind {
        /** Vinhas que descem e sobem a parede em que ele bate. */
        VINES,
        /** Cactos onde há areia ou terra. */
        THORNS,
        /** Cegueira em roda. */
        INK,
        /** Um galho de tronco e folha que cresce para onde o frasco foi. */
        SPROUTING,
        /** Uma bola de mundo que se come, e ácido em quem apanha. */
        EROSION,
        /** Bichos que se apaixonam. */
        LOVE,
        /** E os mortos que se levantam. */
        RAISING,
        /** Teia de aranha em cruz onde ele bate. */
        WEBS,
        /** Gelo: a agua que congela, o escudo que sobe e a gaiola em volta de quem apanha. */
        ICE,
        /** A pedra que apodrece em pedra-de-bicho, e a doenca em quem apanha. */
        INFECTION,
        /** E a troca: o que esta largado no chao toma o lugar do chao. */
        SUBSTITUTION;

        /** O que este frasco faz. Devolve se houve efeito: não havendo, o frasco cai de volta no chão. */
        public boolean impact(ServerLevel level, HitResult onde, @Nullable LivingEntity quemAtirou) {
            return switch (this) {
                case VINES -> vines(level, onde);
                case THORNS -> thorns(level, onde);
                case INK -> ink(level, onde);
                case SPROUTING -> sprouting(level, onde);
                case EROSION -> erosion(level, onde, quemAtirou);
                case LOVE -> love(level, onde);
                case RAISING -> raising(level, onde);
                case WEBS -> webs(level, onde);
                case ICE -> ice(level, onde, quemAtirou);
                case INFECTION -> infection(level, onde, quemAtirou);
                case SUBSTITUTION -> substitution(level, onde);
            };
        }
    }

    // ------------------------------------------------------------------ as vinhas

    /** Até quantas casas a vinha desce e sobe pela parede. */
    public static final int VINE_REACH = 256;

    /**
     * O {@code impactVines}: batendo numa <b>parede</b>, a vinha nasce nela e desce até onde a parede for, e
     * depois sobe pelo outro lado do mesmo jeito. Batendo no chão ou no teto, não faz nada.
     */
    private static boolean vines(ServerLevel level, HitResult onde) {
        if (!(onde instanceof BlockHitResult bateu) || bateu.getDirection().getAxis().isVertical()) return false;
        Direction lado = bateu.getDirection();
        BlockState vinha = Blocks.VINE.defaultBlockState()
                .setValue(VineBlock.PROPERTY_BY_DIRECTION.get(lado.getOpposite()), true);

        boolean algo = desce(level, bateu.getBlockPos(), lado, vinha);
        return sobe(level, bateu.getBlockPos().above(), lado, vinha) || algo;
    }

    /** A vinha a descer, seguindo a parede enquanto houver parede. */
    private static boolean desce(ServerLevel level, BlockPos parede, Direction lado, BlockState vinha) {
        BlockPos cursor = parede;
        boolean algo = false;
        if (!livre(level, cursor.relative(lado)) || !level.getBlockState(cursor).isSolidRender()) {
            cursor = cursor.relative(lado);
        }
        for (int passo = 0; passo < VINE_REACH; passo++) {
            if (!livre(level, cursor.relative(lado)) || !level.getBlockState(cursor).isSolidRender()
                    || cursor.getY() <= level.getMinY()) {
                break;
            }
            algo |= poe(level, cursor.relative(lado), vinha);
            cursor = cursor.below();
            if (!livre(level, cursor.relative(lado)) || !level.getBlockState(cursor).isSolidRender()) {
                cursor = cursor.relative(lado);
            }
        }
        return algo;
    }

    /** E a subir. */
    private static boolean sobe(ServerLevel level, BlockPos parede, Direction lado, BlockState vinha) {
        BlockPos cursor = parede;
        boolean algo = false;
        if (!level.getBlockState(cursor).isSolidRender()) cursor = cursor.relative(lado.getOpposite());
        for (int passo = 0; passo < VINE_REACH; passo++) {
            if (!livre(level, cursor.relative(lado)) || !level.getBlockState(cursor).isSolidRender()
                    || cursor.getY() >= level.getMaxY()) {
                break;
            }
            algo |= poe(level, cursor.relative(lado), vinha);
            cursor = cursor.above();
            if (!level.getBlockState(cursor).isSolidRender()) cursor = cursor.relative(lado.getOpposite());
        }
        return algo;
    }

    /** Onde a vinha cabe: o {@code isNotSolidOrLeaves} do original. */
    private static boolean livre(ServerLevel level, BlockPos onde) {
        BlockState feitio = level.getBlockState(onde);
        return !feitio.isSolidRender() || feitio.is(BlockTags.LEAVES);
    }

    private static boolean poe(ServerLevel level, BlockPos onde, BlockState feitio) {
        if (level.getBlockState(onde).isSolidRender()) return false;
        level.setBlock(onde, feitio, Block.UPDATE_ALL);
        level.sendParticles(ParticleTypes.EXPLOSION, onde.getX() + 0.5, onde.getY() + 0.5, onde.getZ() + 0.5,
                1, 0.0, 0.0, 0.0, 0.0);
        return true;
    }

    // ------------------------------------------------------------------ os espinhos

    /** Que altura de cacto o frasco planta. */
    public static final int CACTUS_HEIGHT = 3;
    /** E quanto ele planta em volta de quem apanha. */
    public static final int CACTUS_ON_ENTITY = 1;

    /** O {@code impactThorns}: cacto onde bate, ou quatro cactos em volta de quem apanha. */
    private static boolean thorns(ServerLevel level, HitResult onde) {
        if (onde instanceof BlockHitResult bateu) {
            if (bateu.getDirection() != Direction.UP
                    && !level.getBlockState(bateu.getBlockPos()).is(Blocks.CACTUS)) {
                return false;
            }
            return cactus(level, bateu.getBlockPos(), CACTUS_HEIGHT);
        }
        if (!(onde instanceof EntityHitResult apanhou)) return false;
        BlockPos onde2 = apanhou.getEntity().blockPosition();
        boolean algo = false;
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            algo |= cactus(level, onde2.relative(lado), CACTUS_ON_ENTITY);
        }
        return algo;
    }

    /** O {@code plantCactus}: areia onde não houver, e o cacto por cima dela. */
    private static boolean cactus(ServerLevel level, BlockPos onde, int altura) {
        BlockPos chão = onde;
        if (!level.getBlockState(chão).isSolidRender()) chão = chão.below();
        BlockState feitio = level.getBlockState(chão);

        if (feitio.is(Blocks.CACTUS)) {
            while (level.getBlockState(chão).is(Blocks.CACTUS)) chão = chão.above();
            chão = chão.below();
        } else if (feitio.is(BlockTags.DIRT) || feitio.is(BlockTags.SAND) || feitio.is(Blocks.GRASS_BLOCK)
                || feitio.is(BlockTags.BASE_STONE_OVERWORLD) || feitio.is(BlockTags.SNOW)) {
            level.setBlock(chão, Blocks.SAND.defaultBlockState(), Block.UPDATE_ALL);
        } else {
            return false;
        }

        boolean algo = false;
        for (int i = 1; i <= altura; i++) {
            if (!poe(level, chão.above(i), Blocks.CACTUS.defaultBlockState())) break;
            algo = true;
        }
        return algo;
    }

    // ------------------------------------------------------------------ a tinta

    /** A que distância a tinta cega, e por quanto tempo no máximo. */
    public static final double INK_RADIUS = 4.0;
    public static final int INK_TICKS = 400;

    /** O {@code explodeInk}: quem estiver perto fica cego, tanto mais quanto mais perto. */
    private static boolean ink(ServerLevel level, HitResult onde) {
        Entity alvo = onde instanceof EntityHitResult apanhou ? apanhou.getEntity() : null;
        var meio = onde.getLocation();
        AABB roda = new AABB(meio.x - INK_RADIUS, meio.y - 2.0, meio.z - INK_RADIUS,
                meio.x + INK_RADIUS, meio.y + 2.0, meio.z + INK_RADIUS);
        boolean algo = false;
        for (LivingEntity vivo : level.getEntitiesOfClass(LivingEntity.class, roda)) {
            double longe = vivo.distanceToSqr(meio);
            if (longe >= INK_RADIUS * INK_RADIUS) continue;
            double quanto = vivo == alvo ? 1.0 : 1.0 - Math.sqrt(longe) / INK_RADIUS;
            vivo.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, (int) (quanto * INK_TICKS + 0.5), 0));
            if (vivo instanceof Mob bicho) bicho.setTarget(null);
            algo = true;
        }
        return algo;
    }

    // ------------------------------------------------------------------ a brotação

    /** Até onde o galho cresce. */
    public static final int BRANCH = 15;

    /**
     * O {@code growBranch}: um galho de tronco cresce na direção em que o frasco bateu, com folha aqui e ali, e
     * quem estiver por cima sobe com ele.
     */
    private static boolean sprouting(ServerLevel level, HitResult onde) {
        if (!(onde instanceof BlockHitResult bateu)) return false;
        Direction para = bateu.getDirection();
        BlockPos base = bateu.getBlockPos();
        BlockState batido = level.getBlockState(base);

        BlockState tronco;
        BlockState folha;
        if (batido.is(BlockTags.LOGS) || batido.is(BlockTags.PLANKS) || batido.getBlock() instanceof net.minecraft.world.level.block.SaplingBlock
                || batido.is(BlockTags.LEAVES)) {
            tronco = Blocks.OAK_LOG.defaultBlockState();
            folha = Blocks.OAK_LEAVES.defaultBlockState();
        } else {
            boolean doOfício = level.getRandom().nextBoolean();
            tronco = (doOfício ? net.thaumcraft.occulta.OccultaBlocks.ROWAN_LOG : Blocks.OAK_LOG)
                    .defaultBlockState();
            folha = (doOfício ? net.thaumcraft.occulta.OccultaBlocks.ROWAN_LEAVES : Blocks.OAK_LEAVES)
                    .defaultBlockState();
        }
        // o tronco deita-se no sentido em que o galho cresce, como no original
        if (tronco.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS)) {
            tronco = tronco.setValue(
                    net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS,
                    para.getAxis());
        }

        boolean sólidoNaBase = batido.isSolidRender();
        int passo = para == Direction.UP && !sólidoNaBase ? 0 : 1;
        int feitos = 0;
        BlockPos ponta = base;
        for (; passo < BRANCH; passo++) {
            BlockPos lugar = base.relative(para, passo);
            if (lugar.getY() >= level.getMaxY() || !poe(level, lugar, tronco)) break;
            ponta = lugar;
            feitos++;
            BlockPos ramo = folhaDeLado(level, lugar, para);
            if (ramo != null) poe(level, ramo, folha);
        }
        if (feitos == 0) return false;

        // o galho que sobe leva consigo quem estava em cima dele
        if (para == Direction.UP) {
            AABB roda = new AABB(base).inflate(0.0, 2.0, 0.0);
            for (LivingEntity vivo : level.getEntitiesOfClass(LivingEntity.class, roda)) {
                if (level.getBlockState(ponta.above()).isSolidRender()
                        || level.getBlockState(ponta.above(2)).isSolidRender()) {
                    continue;
                }
                vivo.snapTo(ponta.getX() + 0.5, ponta.getY() + 1, ponta.getZ() + 0.5, vivo.getYRot(),
                        vivo.getXRot());
            }
        }
        return true;
    }

    /** Onde uma folha nasce ao lado do galho, ou nada: uma em quatro por eixo, como no original. */
    private static @Nullable BlockPos folhaDeLado(ServerLevel level, BlockPos lugar, Direction para) {
        int lx = para.getStepX() == 0 && level.getRandom().nextInt(4) == 0
                ? level.getRandom().nextInt(3) - 1 : 0;
        int ly = para.getStepY() == 0 && lx == 0 && level.getRandom().nextInt(4) == 0
                ? level.getRandom().nextInt(3) - 1 : 0;
        int lz = para.getStepZ() == 0 && lx == 0 && ly == 0 && level.getRandom().nextInt(4) == 0
                ? level.getRandom().nextInt(3) - 1 : 0;
        return lx == 0 && ly == 0 && lz == 0 ? null : lugar.offset(lx, ly, lz);
    }

    // ------------------------------------------------------------------ a erosão

    /** O raio da bola que ela come. */
    public static final int EROSION_RADIUS = 2;
    /** E o que ela tira de quem apanha. */
    public static final float ACID_DAMAGE = 8.0f;
    /** O gasto que ela dá ao que a pessoa traz vestido. */
    public static final int ACID_WEAR = 100;

    /**
     * O {@code impactErosion}: no chão, come uma bola de dois de raio e devolve em obsidiana o que havia dela; em
     * quem apanha, ácido — e o que ele traz vestido gasta-se.
     */
    private static boolean erosion(ServerLevel level, HitResult onde, @Nullable LivingEntity quemAtirou) {
        if (onde instanceof BlockHitResult bateu) {
            BlockPos meio = bateu.getBlockPos();
            int obsidiana = disco(level, meio, EROSION_RADIUS);
            for (int dy = 1; dy <= EROSION_RADIUS; dy++) {
                obsidiana += disco(level, meio.above(dy), EROSION_RADIUS - dy);
                obsidiana += disco(level, meio.below(dy), EROSION_RADIUS - dy);
            }
            if (obsidiana > 0) {
                Block.popResource(level, meio, new ItemStack(Blocks.OBSIDIAN, obsidiana));
            }
            return true;
        }
        if (!(onde instanceof EntityHitResult apanhou)
                || !(apanhou.getEntity() instanceof LivingEntity vivo)) {
            return false;
        }
        vivo.hurtServer(level, level.damageSources().indirectMagic(quemAtirou, quemAtirou), ACID_DAMAGE);
        for (var casa : net.minecraft.world.entity.EquipmentSlot.values()) {
            if (casa.getType() != net.minecraft.world.entity.EquipmentSlot.Type.HUMANOID_ARMOR) continue;
            ItemStack peça = vivo.getItemBySlot(casa);
            if (peça.isEmpty() || !peça.isDamageableItem()) continue;
            peça.hurtAndBreak(ACID_WEAR, level, null, quebrou -> {
            });
        }
        return true;
    }

    /** Um disco de blocos comidos, pelo método de Bresenham; devolve quanta obsidiana havia nele. */
    private static int disco(ServerLevel level, BlockPos meio, int raio) {
        if (raio < 0) return 0;
        int x = raio;
        int z = 0;
        int erro = 1 - x;
        int obsidiana = 0;
        while (x >= z) {
            obsidiana += linha(level, meio, -x, x, z);
            obsidiana += linha(level, meio, -z, z, x);
            obsidiana += linha(level, meio, -x, x, -z);
            obsidiana += linha(level, meio, -z, z, -x);
            z++;
            if (erro < 0) {
                erro += 2 * z + 1;
            } else {
                x--;
                erro += 2 * (z - x + 1);
            }
        }
        return obsidiana;
    }

    private static int linha(ServerLevel level, BlockPos meio, int de, int até, int dz) {
        int obsidiana = 0;
        for (int dx = de; dx <= até; dx++) {
            BlockPos onde = meio.offset(dx, 0, dz);
            BlockState feitio = level.getBlockState(onde);
            if (feitio.isAir() || feitio.liquid() || feitio.is(Blocks.FIRE)) continue;
            if (feitio.getDestroySpeed(level, onde) < 0.0f) continue;
            if (feitio.is(Blocks.OBSIDIAN)) obsidiana++;
            level.removeBlock(onde, false);
            level.sendParticles(ParticleTypes.SPLASH, onde.getX() + 0.5, onde.getY() + 0.5,
                    onde.getZ() + 0.5, 1, 0.0, 0.0, 0.0, 0.0);
        }
        return obsidiana;
    }

    // ------------------------------------------------------------------ o amor

    /** A que distância ele pega. */
    public static final double LOVE_RADIUS = 4.0;

    /**
     * O {@code impactLove}: os bichos em roda apaixonam-se, e os filhotes crescem de uma vez.
     *
     * <p><b>Do original fica de fora, declarado:</b> o par de <b>aldeões</b> que ele junta à força e o de
     * <b>zumbis escravizados</b>, que pedem a Poção de Escravizar — que não está portada. O aldeão do jogo de hoje
     * namora sozinho quando tem comida, e forçá-lo seria outra coisa.
     */
    private static boolean love(ServerLevel level, HitResult onde) {
        Entity alvo = onde instanceof EntityHitResult apanhou ? apanhou.getEntity() : null;
        var meio = onde.getLocation();
        AABB roda = new AABB(meio.x - LOVE_RADIUS, meio.y - 2.0, meio.z - LOVE_RADIUS,
                meio.x + LOVE_RADIUS, meio.y + 2.0, meio.z + LOVE_RADIUS);
        boolean algo = false;
        for (Animal bicho : level.getEntitiesOfClass(Animal.class, roda)) {
            double longe = bicho.distanceToSqr(meio);
            if (longe >= LOVE_RADIUS * LOVE_RADIUS && bicho != alvo) continue;
            if (bicho.getAge() < 0) continue;
            bicho.setAge(0);
            bicho.setInLove(null);
            algo = true;
        }
        return algo;
    }

    // ------------------------------------------------------------------ erguer os mortos

    /** O {@code impactRaising}: onde ele bate, um morto se levanta. */
    private static boolean raising(ServerLevel level, HitResult onde) {
        BlockPos lugar = onde instanceof BlockHitResult bateu
                ? bateu.getBlockPos().relative(bateu.getDirection())
                : BlockPos.containing(onde.getLocation());
        net.thaumcraft.occulta.brew.BrewWorldActions.Raising.raise(level, lugar);
        return true;
    }
    // ------------------------------------------------------------------ as teias

    /** O {@code explodeWeb}: teia na casa em que ele bateu, nas quatro em volta e nas duas de cima e de baixo. */
    private static boolean webs(ServerLevel level, HitResult onde) {
        BlockPos meio = alvo(level, onde);
        boolean algo = poe(level, meio, Blocks.COBWEB.defaultBlockState());
        for (Direction lado : Direction.values()) {
            algo |= poe(level, meio.relative(lado), Blocks.COBWEB.defaultBlockState());
        }
        return algo;
    }

    // ------------------------------------------------------------------ o gelo

    /** Ate onde a agua congela em volta, e que altura tem as colunas do escudo. */
    public static final int FREEZE_RANGE = 3;
    public static final int SHIELD_HEIGHT = 3;
    /** E que altura tem a gaiola de gelo em volta de quem apanha. */
    public static final int CAGE_HEIGHT = 4;

    /**
     * O {@code impactIce}: havendo <b>agua</b> encostada, ela congela em volta; batendo numa face de cima ou de
     * lado, sobem tres <b>colunas de gelo</b> a frente de quem atirou; e em quem apanha, uma <b>gaiola</b>.
     *
     * <p>Os que o gelo nao segura no original - o blaze, o wither, o golem de ferro, o dragao e o Ent - so
     * recebem agua. E o creeper estoura ali mesmo.
     */
    private static boolean ice(ServerLevel level, HitResult onde, @Nullable LivingEntity quemAtirou) {
        if (onde instanceof EntityHitResult apanhou) {
            return cage(level, apanhou.getEntity());
        }
        if (!(onde instanceof BlockHitResult bateu)) return false;
        BlockPos meio = bateu.getBlockPos();

        for (Direction lado : Direction.values()) {
            if (!level.getBlockState(meio.relative(lado)).is(Blocks.WATER)) continue;
            return freeze(level, meio, meio, FREEZE_RANGE, new java.util.HashSet<>());
        }
        if (bateu.getDirection() == Direction.DOWN) return false;
        return shield(level, meio.relative(bateu.getDirection()), quemAtirou);
    }

    /** O {@code freezeSurroundingWater}: a agua pega-se em gelo de casa em casa, ate onde o alcance for. */
    private static boolean freeze(ServerLevel level, BlockPos onde, BlockPos meio, int alcance,
                                  java.util.Set<BlockPos> vistos) {
        if (Math.abs(meio.getX() - onde.getX()) >= alcance || Math.abs(meio.getY() - onde.getY()) >= alcance
                || Math.abs(meio.getZ() - onde.getZ()) >= alcance) {
            return false;
        }
        boolean algo = false;
        for (Direction lado : Direction.values()) {
            BlockPos vizinho = onde.relative(lado);
            if (!vistos.add(vizinho)) continue;
            if (!level.getBlockState(vizinho).is(Blocks.WATER)) continue;
            level.setBlock(vizinho, Blocks.ICE.defaultBlockState(), Block.UPDATE_ALL);
            algo = true;
            algo |= freeze(level, vizinho, meio, alcance, vistos);
        }
        return algo;
    }

    /** O {@code explodeIceShield}: tres colunas de gelo, uma a frente e duas de lado. */
    private static boolean shield(ServerLevel level, BlockPos onde, @Nullable LivingEntity quemAtirou) {
        BlockPos base = level.getBlockState(onde).isSolidRender() ? onde : onde.below();
        double giro = quemAtirou == null ? 0.0 : -quemAtirou.getYRot() * (Math.PI / 180.0) - Math.PI;
        double dx = Math.sin(giro);
        double dz = Math.cos(giro);
        boolean algo = column(level, base.above(), SHIELD_HEIGHT);
        for (double volta : new double[]{Math.PI / 2.0, -Math.PI / 2.0}) {
            int nx = net.minecraft.util.Mth.floor(base.getX() + 0.5 + (dx * Math.cos(volta) - dz * Math.sin(volta)));
            int nz = net.minecraft.util.Mth.floor(base.getZ() + 0.5 + (dx * Math.sin(volta) + dz * Math.cos(volta)));
            algo |= column(level, new BlockPos(nx, base.getY() + 1, nz), SHIELD_HEIGHT);
        }
        return algo;
    }

    private static boolean column(ServerLevel level, BlockPos base, int altura) {
        boolean algo = false;
        for (int i = 0; i < altura; i++) algo |= poe(level, base.above(i), Blocks.ICE.defaultBlockState());
        return algo;
    }

    /** O {@code explodeIceBlock}: a gaiola de gelo em volta de quem apanha. */
    private static boolean cage(ServerLevel level, Entity quem) {
        BlockPos meio = quem.blockPosition().below();
        if (resistant(quem)) {
            return poe(level, meio.above(), Blocks.WATER.defaultBlockState());
        }
        int[][] roda = {{-2, -1}, {-2, 0}, {-1, 1}, {0, 1}, {1, 0}, {1, -1},
                {0, -2}, {-1, -2}, {-2, -2}, {-2, 1}, {1, 1}, {1, -2}};
        boolean algo = false;
        for (int i = 0; i < CAGE_HEIGHT; i++) {
            for (int[] ponto : roda) {
                algo |= poe(level, meio.offset(ponto[0], i, ponto[1]), Blocks.ICE.defaultBlockState());
            }
        }
        algo |= poe(level, meio, Blocks.ICE.defaultBlockState());
        algo |= poe(level, meio.above(CAGE_HEIGHT - 1), Blocks.ICE.defaultBlockState());
        algo |= poe(level, meio.offset(-1, CAGE_HEIGHT - 1, -1), Blocks.ICE.defaultBlockState());
        algo |= poe(level, meio.offset(-1, CAGE_HEIGHT - 1, 0), Blocks.ICE.defaultBlockState());
        algo |= poe(level, meio.offset(0, CAGE_HEIGHT - 1, -1), Blocks.ICE.defaultBlockState());

        if (quem instanceof net.minecraft.world.entity.monster.Creeper creeper) {
            level.explode(creeper, creeper.getX(), creeper.getY(), creeper.getZ(),
                    creeper.isPowered() ? 6.0f : 3.0f,
                    net.minecraft.world.level.Level.ExplosionInteraction.MOB);
            creeper.discard();
        }
        return algo;
    }

    /** Os que o gelo nao segura, no original: os que ardem, os que nao sao de carne e o dragao. */
    private static boolean resistant(Entity quem) {
        return quem instanceof net.minecraft.world.entity.monster.Blaze
                || quem instanceof net.minecraft.world.entity.boss.wither.WitherBoss
                || quem instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon
                || quem instanceof net.minecraft.world.entity.animal.golem.IronGolem
                || quem instanceof net.thaumcraft.occulta.EntEntity;
    }

    // ------------------------------------------------------------------ a infeccao

    /** O que ela tira de quem apanha, e quanto tempo o deixa lerdo. */
    public static final float WORM_DAMAGE = 1.0f;
    public static final int WORM_SLOW = 100;

    /**
     * O {@code impactInfection}: a pedra apodrece em <b>pedra-de-bicho</b>, o aldeao vira zumbi, e quem mais
     * apanhar leva um golpe e fica lerdo.
     */
    private static boolean infection(ServerLevel level, HitResult onde, @Nullable LivingEntity quemAtirou) {
        if (onde instanceof BlockHitResult bateu) {
            BlockState feitio = level.getBlockState(bateu.getBlockPos());
            BlockState bichado = feitio.is(Blocks.STONE) ? Blocks.INFESTED_STONE.defaultBlockState()
                    : feitio.is(Blocks.COBBLESTONE) ? Blocks.INFESTED_COBBLESTONE.defaultBlockState()
                    : feitio.is(Blocks.STONE_BRICKS) ? Blocks.INFESTED_STONE_BRICKS.defaultBlockState() : null;
            if (bichado == null) return false;
            level.setBlock(bateu.getBlockPos(), bichado, Block.UPDATE_ALL);
            return true;
        }
        if (!(onde instanceof EntityHitResult apanhou)
                || !(apanhou.getEntity() instanceof LivingEntity vivo)) {
            return false;
        }
        if (vivo instanceof net.minecraft.world.entity.npc.villager.Villager aldeao) {
            return aldeao.convertTo(net.minecraft.world.entity.EntityTypes.ZOMBIE_VILLAGER,
                    net.minecraft.world.entity.ConversionParams.single(aldeao, false, false),
                    zumbi -> {
                    }) != null;
        }
        vivo.hurtServer(level, level.damageSources().indirectMagic(quemAtirou, quemAtirou), WORM_DAMAGE);
        vivo.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, WORM_SLOW, 8));
        return true;
    }

    // ------------------------------------------------------------------ a troca

    /** A que distancia ela troca. */
    public static final int SWAP_RADIUS = 4;

    /**
     * O {@code brewSubstitution}: o que estiver <b>largado no chao</b> em roda toma o lugar do bloco em que o
     * frasco bateu - casa por casa, do mais perto para o mais longe, ate acabarem os itens.
     *
     * <p><b>Desvio declarado:</b> no original a troca corre numa <b>espiral</b> desenhada pelo
     * {@code EffectSpiral}, que e o que lhe da o ar de feitico. Aqui ela corre do meio para fora pela distancia,
     * que e a mesma ordem sem o desenho - a espiral do original e um relogio de animacao, e nao uma regra do que
     * se troca.
     */
    private static boolean substitution(ServerLevel level, HitResult onde) {
        if (!(onde instanceof BlockHitResult bateu)) return false;
        BlockState modelo = level.getBlockState(bateu.getBlockPos());
        if (modelo.isAir()) return false;

        var largados = new java.util.ArrayList<net.minecraft.world.entity.item.ItemEntity>();
        for (var item : level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                new AABB(bateu.getBlockPos()).inflate(SWAP_RADIUS))) {
            if (item.getItem().getItem() instanceof net.minecraft.world.item.BlockItem) largados.add(item);
        }
        if (largados.isEmpty()) return false;

        var casas = new java.util.ArrayList<BlockPos>();
        for (BlockPos casa : BlockPos.betweenClosed(
                bateu.getBlockPos().offset(-SWAP_RADIUS, -SWAP_RADIUS, -SWAP_RADIUS),
                bateu.getBlockPos().offset(SWAP_RADIUS, SWAP_RADIUS, SWAP_RADIUS))) {
            if (casa.distSqr(bateu.getBlockPos()) > (double) SWAP_RADIUS * SWAP_RADIUS) continue;
            if (!level.getBlockState(casa).equals(modelo)) continue;
            casas.add(casa.immutable());
        }
        casas.sort(java.util.Comparator.comparingDouble(casa -> casa.distSqr(bateu.getBlockPos())));

        int qual = 0;
        boolean algo = false;
        for (BlockPos casa : casas) {
            while (qual < largados.size() && largados.get(qual).getItem().isEmpty()) qual++;
            if (qual >= largados.size()) break;
            var item = largados.get(qual);
            var bloco = ((net.minecraft.world.item.BlockItem) item.getItem().getItem()).getBlock();
            level.setBlock(casa, bloco.defaultBlockState(), Block.UPDATE_ALL);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, casa.getX() + 0.5, casa.getY() + 1.5,
                    casa.getZ() + 0.5, 2, 0.3, 0.3, 0.3, 0.0);
            item.getItem().shrink(1);
            if (item.getItem().isEmpty()) item.discard();
            algo = true;
        }
        return algo;
    }

    /** A casa em que um frasco bateu: a de fora do bloco, ou a de quem apanhou. */
    private static BlockPos alvo(ServerLevel level, HitResult onde) {
        if (onde instanceof BlockHitResult bateu) {
            BlockPos fora = bateu.getBlockPos().relative(bateu.getDirection());
            return bateu.getDirection() == Direction.UP
                    && !level.getBlockState(bateu.getBlockPos()).isSolidRender() ? fora.below() : fora;
        }
        return BlockPos.containing(onde.getLocation());
    }
}
