package net.thaumcraft.occulta.infusion;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.BlockProtect;
import net.thaumcraft.occulta.OccultaEntities;
import org.jetbrains.annotations.Nullable;

/**
 * A <b>Infusão do Mundo</b>: a {@code InfusionOverworld} do Witchery.
 *
 * <p>A última das quatro, e a que menos parece magia. As outras três fazem coisas que só a magia faz —
 * teleportar, apagar a luz, escravizar bichos. Esta faz <b>peso</b>: ela pega no chão e no metal e os usa
 * como um ferreiro usaria, se um ferreiro tivesse quarenta toneladas de braço.
 *
 * <h2>O que ela sabe fazer</h2>
 *
 * <ul>
 *   <li><b>cair</b> mais de três blocos em terra mole: agachado, rebenta o chão como um creeper; de pé,
 *       <b>arranca o bloco de baixo</b> e o deixa em item. E a queda não dói;</li>
 *   <li><b>socar</b> com a Mão quem tiver <b>metal</b> no corpo: ele voa. Agachado voa para cima;</li>
 *   <li><b>segurar agachado</b>: todo o metal largado a seis blocos <b>vem para a mão</b>, e todo o minério
 *       a seis blocos <b>funde-se sozinho</b> em lingote;</li>
 *   <li><b>largar</b> olhando para um bicho: <b>desarma-o</b>; para o topo de um bloco: <b>levanta uma
 *       coluna</b> de seis blocos três níveis, com quem estiver em cima dela; para o lado de um bloco:
 *       <b>atira-o</b>; agachado, funde o minério em <b>dois</b> lingotes;</li>
 *   <li>e <b>largar olhando para o nada</b>, depois de a segurar: a <b>onda de choque</b>.</li>
 * </ul>
 *
 * <h2>O metal é a fraqueza</h2>
 *
 * <p>O soco e o desarmamento só pegam em quem tem <b>ferro, ouro ou malha</b> — e é por isso que esta
 * infusão é a única do mod em que a <b>boa armadura é um perigo</b>. De diamante, ou de couro, ou pelado,
 * ninguém voa. Veja o {@link EarthMetal}.
 *
 * @see Shockwave a onda de choque, que vale a sua própria classe
 */
public class OverworldInfusion extends Infusion {
    /** De quantos blocos a queda tem de ser para valer. */
    public static final float QUEDA = 3.0f;

    /** A força do estouro de quem cai agachado. */
    public static final float ESTOURO = 3.0f;

    /** O que o soco multiplica ao olhar, e o salto que ele dá — de pé e agachado. */
    public static final double SOCO = 0.8 * 3.0;
    public static final double SOBE = 0.3;
    public static final double SOBE_AGACHADO = 1.5;

    /** O alcance de tudo o que ela faz segurada: seis blocos à volta, três acima e abaixo. */
    public static final int VOLTA = 6;
    public static final int ACIMA = 3;

    /** E o alcance do olhar, que é o braço de sempre. */
    public static final double OLHAR = 4.0;

    /** Quanto tempo se tem de segurar para qualquer coisa pegar. */
    public static final int SEGUNDOS = 2;

    /** De quantos blocos é a coluna que ela levanta, e quanto a levanta. */
    public static final int COLUNA = 6;
    public static final int LEVANTA = 3;

    /** E a quanto do chão ela precisa debaixo do que levanta. */
    public static final int FIRME = 10;

    /** Os custos, na ordem em que o original os escreve. */
    public static final int CUSTO_ESTOURO = 10;
    public static final int CUSTO_ARRANCAR = 5;
    public static final int CUSTO_SOCO_AGACHADO = 4;
    public static final int CUSTO_SOCO = 2;
    public static final int CUSTO_PUXAR = 1;
    public static final int CUSTO_FUNDIR = 2;
    public static final int CUSTO_DESARMAR = 2;
    public static final int CUSTO_LEVANTAR = 2;
    public static final int CUSTO_ATIRAR = 3;
    public static final int CUSTO_ONDA = 6;

    /** Quantos lingotes a fundição de perto dá, que é o dobro da de longe. */
    public static final int DOBRO = 2;

    /** De quanto em quanto ela puxa o metal largado. */
    public static final int DE_QUATRO_EM_QUATRO = 4;

    /** A conta do puxão, que é do original, com o oito dele. */
    public static final double OITO = 8.0;

    /** O rótulo do que a terra mole é: o que vale a pena cair em cima. */
    public static final TagKey<Block> MOLE =
            TagKey.create(Registries.BLOCK, Thaumcraft.id("soft_landing"));

    /** E o do que se pode atirar: os vinte e quatro blocos que o original lista à mão. */
    public static final TagKey<Block> ATIRÁVEL =
            TagKey.create(Registries.BLOCK, Thaumcraft.id("throwable_rock"));

    public OverworldInfusion(int id) {
        super(id);
    }

    // ------------------------------------------------------------------ a queda

    /**
     * <b>Cair é um poder.</b>
     *
     * <p>Mais de três blocos em cima de terra, grama, micélio, cascalho, areia ou neve, e a queda não dói:
     * ou <b>rebenta</b> — agachado, com a força de três, que é a de um creeper — ou <b>arranca</b> o bloco
     * de baixo e o deixa em item.
     *
     * <p>A infusão não precisa da Mão para isto. É o único poder dela que funciona de mãos vazias, e é por
     * isso que ela se nota logo: quem se infunde do Mundo descobre-o caindo.
     */
    @Override
    public double cai(ServerLevel level, ServerPlayer quem, double quanto) {
        if (quanto <= QUEDA) return quanto;
        BlockPos debaixo = BlockPos.containing(quem.getX(), quem.getY() - 1.0, quem.getZ());
        if (!level.getBlockState(debaixo).is(MOLE)) return quanto;

        if (quem.isShiftKeyDown()) {
            if (!this.gasta(level, quem, CUSTO_ESTOURO)) return quanto;
            /*
             * O estouro do original <b>quebra blocos sempre</b>: ele chama o estouro cru do jogo, que não
             * pergunta pela regra do vandalismo. Por isso é o estouro da dinamite, e não o do creeper, o
             * que lhe corresponde hoje — a força é a mesma, três.
             */
            level.explode(quem, quem.getX(), debaixo.getY() + 0.5, quem.getZ(), ESTOURO,
                    Level.ExplosionInteraction.TNT);
            return 0.0;
        }

        if (!this.gasta(level, quem, CUSTO_ARRANCAR)) return quanto;
        BlockState oquê = level.getBlockState(debaixo);
        if (BlockProtect.podeMexer(oquê)) {
            level.removeBlock(debaixo, false);
            level.addFreshEntity(new ItemEntity(level, debaixo.getX(), debaixo.getY(), debaixo.getZ(),
                    new ItemStack(oquê.getBlock())));
        }
        return 0.0;
    }

    // ------------------------------------------------------------------ o soco

    /**
     * <b>E socar com a Mão atira longe quem tem metal no corpo.</b>
     *
     * <p>O original olha os <b>cinco</b> lugares de equipamento — a mão e as quatro peças — e basta um
     * deles ser de ferro, de ouro ou de malha. O empurrão é o olhar de quem soca, multiplicado por oito
     * décimos e por três, com três décimos de salto — ou <b>um e meio</b>, se ele estiver agachado.
     */
    @Override
    public void soca(ServerLevel level, ServerPlayer quem, ItemStack mão, Entity noquê) {
        if (!(noquê instanceof LivingEntity bicho) || !temMetal(bicho)) {
            falha(level, quem);
            return;
        }

        boolean agachado = quem.isShiftKeyDown();
        if (!this.gasta(level, quem, agachado ? CUSTO_SOCO_AGACHADO : CUSTO_SOCO)) return;

        Vec3 olhar = quem.getLookAngle();
        bicho.setDeltaMovement(olhar.x * SOCO, agachado ? SOBE_AGACHADO : SOBE, olhar.z * SOCO);
        bicho.hurtMarked = true;
    }

    /** Se aquele bicho tem metal em algum dos cinco lugares que o original olha. */
    public static boolean temMetal(LivingEntity bicho) {
        if (EarthMetal.é(bicho.getMainHandItem())) return true;
        for (EquipmentSlot onde : new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS,
                EquipmentSlot.CHEST, EquipmentSlot.HEAD}) {
            if (EarthMetal.é(bicho.getItemBySlot(onde))) return true;
        }
        return false;
    }

    // ------------------------------------------------------------------ segurar

    /**
     * <b>Segurar a Mão agachado é um ímã.</b>
     *
     * <p>Depois de dois segundos, e de quatro em quatro batidas, tudo o que for <b>metal largado</b> a seis
     * blocos vem para quem segura, e todo o <b>minério</b> no mesmo cubo se funde sozinho em lingote. O ímã
     * custa uma carga por batida; cada minério fundido custa duas.
     *
     * <p>De pé, segurá-la só faz <b>o som</b> — de segundo em segundo, para se saber que ela está pronta.
     */
    @Override
    public void segurando(ServerLevel level, ServerPlayer quem, ItemStack mão, int faltam) {
        int passou = this.quantoSeSegura() - faltam;
        int segundos = passou / 20;
        if (segundos < SEGUNDOS) return;

        if (!quem.isShiftKeyDown()) {
            if (passou % 20 == 0) toca(level, quem, SoundEvents.EXPERIENCE_ORB_PICKUP);
            return;
        }

        if (passou % DE_QUATRO_EM_QUATRO != 0) return;
        if (!this.gasta(level, quem, CUSTO_PUXAR)) return;

        puxaOMetal(level, quem);
        fundeOMinério(level, quem);
    }

    /**
     * O ímã do metal largado, com a <b>conta do original</b>.
     *
     * <p>Ela é estranha e é de propósito que fique estranha: o original divide as <b>três</b> componentes
     * pelo módulo da <b>primeira</b>, o que faz do empurrão em X sempre um bloco e dos outros dois um
     * múltiplo de quantos blocos o item está desalinhado em X. O resultado é o puxão aos saltos que se vê
     * no original, e não o puxão macio que se esperaria.
     *
     * <p><b>Dois cuidados que o original não tem</b> e sem os quais o porte não aguenta: o módulo tem um
     * <b>piso</b>, para não haver divisão por zero com um item exatamente alinhado, e cada componente é
     * <b>cortada ao cubo de seis blocos</b>, para um item quase alinhado não ser atirado para fora do mundo
     * com a posição estragada. Estão no PORTE.md.
     */
    public static void puxaOMetal(ServerLevel level, ServerPlayer quem) {
        AABB cubo = new AABB(quem.getX() - VOLTA, quem.getY() - VOLTA, quem.getZ() - VOLTA,
                quem.getX() + VOLTA, quem.getY() + VOLTA, quem.getZ() + VOLTA);
        for (ItemEntity largado : level.getEntitiesOfClass(ItemEntity.class, cubo)) {
            if (!EarthMetal.é(largado.getItem())) continue;

            double dx = (quem.getX() - largado.getX()) / OITO;
            double dy = (quem.getY() + quem.getEyeHeight() - largado.getY()) / OITO;
            double dz = (quem.getZ() - largado.getZ()) / OITO;
            double porQuanto = Math.max(Math.abs(dx), 1.0E-4);
            double andaX = Math.signum(dx);
            double andaY = corta(dy / porQuanto);
            double andaZ = corta(dz / porQuanto);

            boolean passava = largado.noPhysics;
            largado.noPhysics = true;
            largado.move(net.minecraft.world.entity.MoverType.SELF, new Vec3(andaX, andaY, andaZ));
            largado.noPhysics = passava;
        }
    }

    /** O corte ao cubo do ímã, que é o cuidado declarado acima. */
    private static double corta(double quanto) {
        return Math.max(-VOLTA, Math.min(VOLTA, quanto));
    }

    /**
     * E a fundição à distância, que varre o cubo inteiro e cobra duas cargas por minério.
     *
     * <p><b>Um desvio miúdo:</b> ficando sem carga a meio, ela <b>para</b>, enquanto o original segue a
     * varrer o cubo todo e falha num minério por vez. Como ficar sem carga apaga o que sobrava, o que
     * acontece ao mundo é o mesmo nos dois; a diferença é que o original toca o tambor da falha até mil
     * e cento e oitenta e três vezes seguidas. Está no PORTE.md.
     */
    private void fundeOMinério(ServerLevel level, ServerPlayer quem) {
        BlockPos meio = quem.blockPosition();
        for (int x = meio.getX() - VOLTA; x <= meio.getX() + VOLTA; x++) {
            for (int y = meio.getY() - ACIMA; y <= meio.getY() + ACIMA; y++) {
                for (int z = meio.getZ() - VOLTA; z <= meio.getZ() + VOLTA; z++) {
                    BlockPos aqui = new BlockPos(x, y, z);
                    BlockState feitio = level.getBlockState(aqui);
                    if (feitio.isAir()) continue;
                    Item lingote = EarthMetal.lingoteDe(feitio);
                    if (lingote == null) continue;
                    if (!this.gasta(level, quem, CUSTO_FUNDIR)) return;
                    funde(level, aqui, feitio, lingote, 1);
                }
            }
        }
    }

    /** Põe no lugar do minério o que sobra dele e deixa cair o lingote. */
    private static void funde(ServerLevel level, BlockPos onde, BlockState feitio, Item lingote,
                              int quantos) {
        level.setBlock(onde, EarthMetal.oQueFica(feitio).defaultBlockState(), Block.UPDATE_ALL);
        level.addFreshEntity(new ItemEntity(level, onde.getX(), onde.getY(), onde.getZ(),
                new ItemStack(lingote, quantos)));
    }

    // ------------------------------------------------------------------ largar

    /**
     * <b>E largá-la faz uma de cinco coisas, pela ordem em que o original as pergunta.</b>
     *
     * <p>Olhando para um <b>bicho</b> e de pé: <b>desarma-o</b>, e o que ele tinha na mão cai no chão.
     *
     * <p>Olhando para o <b>topo</b> de um bloco e de pé: levanta uma <b>coluna de seis</b> blocos <b>três
     * níveis</b>, e quem estiver em cima dela sobe com ela. Precisa de dez blocos de chão firme por baixo —
     * é o que impede que se levante o telhado de uma caverna.
     *
     * <p>Olhando para o <b>lado</b> de um bloco e de pé: <b>atira-o</b>, se ele for dos que se atiram e se
     * estiver solto por trás.
     *
     * <p><b>Agachado</b>, olhando para um minério: funde-o em <b>dois</b> lingotes — o dobro do que a
     * fundição de longe dá.
     *
     * <p>E olhando para <b>nada</b>, depois de a segurar dois segundos: a <b>onda de choque</b>, com o raio
     * a crescer com o tempo que se segurou e o preço a crescer com ele.
     */
    @Override
    public void largou(ServerLevel level, ServerPlayer quem, ItemStack mão, int faltam) {
        int passou = this.quantoSeSegura() - faltam;
        HitResult onde = olha(level, quem);

        if (onde instanceof EntityHitResult noBicho) {
            if (!quem.isShiftKeyDown() && noBicho.getEntity() instanceof Mob bicho
                    && this.gasta(level, quem, CUSTO_DESARMAR)) {
                desarma(level, bicho);
                return;
            }
        } else if (onde instanceof BlockHitResult noBloco) {
            noBloco(level, quem, noBloco);
            return;
        }

        int segundos = passou / 20;
        if (segundos >= SEGUNDOS && !quem.isShiftKeyDown()
                && this.gasta(level, quem, CUSTO_ONDA * segundos)) {
            Shockwave.começa(level, quem, 2 * segundos);
        } else {
            falha(level, quem);
        }
    }

    /** O desarmamento: o que ele tem na mão cai, e a mão fica vazia. */
    private static void desarma(ServerLevel level, Mob bicho) {
        ItemStack tinha = bicho.getMainHandItem();
        if (tinha.isEmpty() || !EarthMetal.é(tinha)) return;
        bicho.spawnAtLocation(level, tinha, 2.0f);
        bicho.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
    }

    /** As três coisas que se fazem a um bloco, na ordem do original. */
    private void noBloco(ServerLevel level, ServerPlayer quem, BlockHitResult bateu) {
        BlockPos onde = bateu.getBlockPos();
        Direction face = bateu.getDirection();
        boolean agachado = quem.isShiftKeyDown();

        if (!agachado && face == Direction.UP
                && level.getBlockState(onde.below(FIRME)).isSolid()
                && this.gasta(level, quem, CUSTO_LEVANTAR)) {
            levanta(level, onde);
            return;
        }

        if (!agachado && face != Direction.UP && face != Direction.DOWN) {
            if (atirável(level, onde, face) && this.gasta(level, quem, CUSTO_ATIRAR)) {
                atira(level, quem, onde);
            }
            return;
        }

        if (agachado && this.gasta(level, quem, CUSTO_FUNDIR)) {
            BlockState feitio = level.getBlockState(onde);
            Item lingote = EarthMetal.lingoteDe(feitio);
            if (lingote != null) funde(level, onde, feitio, lingote, DOBRO);
        }
    }

    /**
     * A <b>coluna que se levanta</b>: seis blocos, três níveis para cima, de cima para baixo.
     *
     * <p>Ela é feita de cima para baixo de propósito — o bloco mais alto vai primeiro, para encontrar ar
     * onde cair. E quem estiver <b>em cima dela</b> sobe com ela, teleportado os mesmos três blocos, o que
     * é a diferença entre levantar uma coluna e <b>engolir</b> quem está nela.
     */
    private static void levanta(ServerLevel level, BlockPos onde) {
        for (int volta = 0; volta < COLUNA; volta++) {
            BlockPos daqui = onde.below(volta);
            BlockState oquê = level.getBlockState(daqui);
            if (!BlockProtect.podeMexer(oquê)) continue;

            level.removeBlock(daqui, false);
            BlockPos pali = daqui.above(LEVANTA);
            if (BlockProtect.podeMexer(level, pali)) level.setBlock(pali, oquê, Block.UPDATE_ALL);

            AABB emCima = new AABB(onde.getX(), onde.getY(), onde.getZ(),
                    onde.getX() + 1, onde.getY() + 2, onde.getZ() + 1);
            for (Entity bicho : level.getEntities((Entity) null, emCima, cada -> true)) {
                if (bicho instanceof LivingEntity vivo) {
                    vivo.teleportTo(vivo.getX(), vivo.getY() + LEVANTA, vivo.getZ());
                } else {
                    bicho.setPos(bicho.getX(), bicho.getY() + LEVANTA, bicho.getZ());
                }
            }
        }
    }

    /**
     * Se aquele bloco se pode atirar: tem de ser dos do rótulo <i>e</i> estar <b>solto por trás</b>.
     *
     * <p>A segunda parte é a que faz o poder ser uma escolha e não um botão: só se arranca da parede o
     * bloco que já estava à beira de não ter parede. O original escreve isso com quatro condições
     * espelhadas e os nomes dos lados trocados, que é um engano famoso da 1.7.10 — mas o que elas dizem é
     * uma coisa só, e é esta.
     */
    private static boolean atirável(ServerLevel level, BlockPos onde, Direction face) {
        if (!level.getBlockState(onde).is(ATIRÁVEL)) return false;
        return !level.getBlockState(onde.relative(face.getOpposite())).isSolid();
    }

    /** E o tiro: o bloco desaparece num estouro e sai de lá uma <b>Rocha</b>. */
    private static void atira(ServerLevel level, ServerPlayer quem, BlockPos onde) {
        level.removeBlock(onde, false);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION,
                onde.getX() + 0.5, onde.getY() + 0.5, onde.getZ() + 0.5, 8, 0.5, 0.5, 0.5, 0.0);
        level.playSound(null, onde, SoundEvents.GENERIC_EXPLODE.value(),
                net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);

        var rocha = new RockEntity(OccultaEntities.ROCK, level);
        rocha.setOwner(quem);
        rocha.setPos(onde.getX() + 0.5, onde.getY() + 0.5, onde.getZ() + 0.5);
        rocha.shootFromRotation(quem, quem.getXRot(), quem.getYRot(), 0.0f, 1.5f, 1.0f);
        level.addFreshEntity(rocha);
    }

    // ------------------------------------------------------------------ o olhar

    /**
     * O que ele está olhando, até quatro blocos: <b>bicho ou bloco, o que estiver mais perto</b>.
     *
     * <p>É o {@code doCustomRayTrace} do original, que faz os dois traçados e escolhe o mais perto dos
     * olhos — ao contrário do traçado do jogo, que só vê blocos.
     */
    public static @Nullable HitResult olha(ServerLevel level, ServerPlayer quem) {
        Vec3 olhos = quem.getEyePosition();
        Vec3 rumo = olhos.add(quem.getLookAngle().scale(OLHAR));

        BlockHitResult noBloco = level.clip(new ClipContext(olhos, rumo, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, quem));
        EntityHitResult noBicho = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                quem, olhos, rumo, new AABB(olhos, rumo).inflate(1.0),
                cada -> cada instanceof LivingEntity && !cada.isSpectator(), 0.0);

        if (noBicho == null) {
            return noBloco.getType() == HitResult.Type.MISS ? null : noBloco;
        }
        if (noBloco.getType() == HitResult.Type.MISS) return noBicho;
        return noBicho.getLocation().distanceTo(olhos) < noBloco.getLocation().distanceTo(olhos)
                ? noBicho : noBloco;
    }
}
