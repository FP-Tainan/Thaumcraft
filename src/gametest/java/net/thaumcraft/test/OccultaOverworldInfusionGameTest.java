package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.infusion.EarthMetal;
import net.thaumcraft.occulta.infusion.Infusions;
import net.thaumcraft.occulta.infusion.OverworldInfusion;
import net.thaumcraft.occulta.infusion.Shockwave;

/**
 * A <b>Infusão do Mundo</b>, a última das quatro.
 *
 * <p>As outras três fazem coisas que só a magia faz. Esta faz <b>peso</b>: ela pega no chão e no metal e
 * usa-os. A prova que carrega a fatia é a da <b>onda de choque</b> — ela levanta o chão e <b>põe-no de
 * volta</b>, e é essa segunda metade que a torna o poder que é. Uma onda que deixasse cratera seria só uma
 * bomba lenta.
 *
 * <p>E a segunda prova é a do <b>metal</b>: quem anda de ferro voa, quem anda de diamante não. O original
 * fez da armadura boa uma desvantagem, e é a única vez em que ele faz isso.
 */
public class OccultaOverworldInfusionGameTest {
    /** Um jogador da sobrevivência: é o único em que a carga da infusão se gasta de verdade. */
    private static ServerPlayer gente(GameTestHelper helper, BlockPos onde) {
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
        BlockPos ali = helper.absolutePos(onde);
        quem.setPos(ali.getX() + 0.5, ali.getY(), ali.getZ() + 0.5);
        Infusions.infunde(quem, Infusions.daquele(2), Infusions.CARGAS);
        return quem;
    }

    /** Põe-no olhando para aquele lugar, seja qual for o giro da arena. */
    private static void mira(ServerPlayer quem, Vec3 para) {
        Vec3 rumo = para.subtract(quem.getEyePosition());
        double chão = Math.sqrt(rumo.x * rumo.x + rumo.z * rumo.z);
        quem.setYRot((float) (Math.atan2(-rumo.x, rumo.z) * 180.0 / Math.PI));
        quem.setXRot((float) (-Math.atan2(rumo.y, chão) * 180.0 / Math.PI));
    }

    /** Os números dela são os do original. */
    @GameTest
    public void itsNumbersAreTheOriginals(GameTestHelper helper) {
        if (Infusions.quantas() != 5) {
            helper.fail("com a do Mundo são cinco; há " + Infusions.quantas());
        }
        if (!(Infusions.daquele(2) instanceof OverworldInfusion)) {
            helper.fail("a de número dois é a do Mundo");
        }
        if (OverworldInfusion.QUEDA != 3.0f || OverworldInfusion.ESTOURO != 3.0f) {
            helper.fail("mais de três blocos de queda, e um estouro de força três");
        }
        if (OverworldInfusion.CUSTO_ESTOURO != 10 || OverworldInfusion.CUSTO_ARRANCAR != 5) {
            helper.fail("dez para rebentar o chão, cinco para o arrancar");
        }
        if (OverworldInfusion.CUSTO_SOCO_AGACHADO != 4 || OverworldInfusion.CUSTO_SOCO != 2) {
            helper.fail("quatro o soco agachado, dois o soco de pé");
        }
        if (OverworldInfusion.CUSTO_PUXAR != 1 || OverworldInfusion.CUSTO_FUNDIR != 2
                || OverworldInfusion.CUSTO_DESARMAR != 2 || OverworldInfusion.CUSTO_LEVANTAR != 2
                || OverworldInfusion.CUSTO_ATIRAR != 3 || OverworldInfusion.CUSTO_ONDA != 6) {
            helper.fail("um, dois, dois, dois, três e seis por segundo");
        }
        if (OverworldInfusion.COLUNA != 6 || OverworldInfusion.LEVANTA != 3
                || OverworldInfusion.FIRME != 10) {
            helper.fail("uma coluna de seis, levantada três, com dez de chão firme por baixo");
        }
        if (Shockwave.DANO != 8.0f || Shockwave.MÍNIMO != 2 || Shockwave.FUNDURA != 2) {
            helper.fail("oito de dano na crista, raio mínimo dois, dois blocos de fundura");
        }
        helper.succeed();
    }

    /**
     * <b>O rito dela tem o mesmo anel que o da Luz — e é o que se oferece que os separa.</b>
     *
     * <p>Vale uma prova própria porque é a única vez em que dois ritos do mod desenham <b>o mesmo</b>
     * círculo: dezesseis dentro e vinte e oito no meio, os dois. Quem decide qual deles pega é o frasco
     * que está no chão, e nada mais.
     */
    @GameTest
    public void itsRiteSharesTheCircleWithTheLightOne(GameTestHelper helper) {
        var mundo = net.thaumcraft.occulta.rite.RiteRegistry.get("tc.rite.infusionearth");
        var luz = net.thaumcraft.occulta.rite.RiteRegistry.get("tc.rite.infusionlight");
        if (mundo == null) {
            helper.fail("o Rito da Infusão do Mundo devia estar na lista");
            return;
        }
        if (!mundo.inner().equals(luz.inner()) || !mundo.middle().equals(luz.middle())
                || !mundo.outer().equals(luz.outer())) {
            helper.fail("e devia ter o mesmo anel do da Luz");
        }
        if (mundo.sacrifice().equals(luz.sacrifice())) {
            helper.fail("mas não o mesmo que se oferece");
        }
        helper.succeed();
    }

    /** E o que ela chama de metal é o ferro, o ouro e a malha — nunca o diamante. */
    @GameTest
    public void onlyIronGoldAndChainAreMetal(GameTestHelper helper) {
        for (var qual : new net.minecraft.world.item.Item[]{Items.IRON_SWORD, Items.GOLDEN_SHOVEL,
                Items.IRON_INGOT, Items.GOLD_NUGGET, Items.CHAINMAIL_CHESTPLATE,
                Items.GOLDEN_BOOTS}) {
            if (!EarthMetal.é(new ItemStack(qual))) helper.fail(qual + " é metal para ela");
        }
        for (var qual : new net.minecraft.world.item.Item[]{Items.DIAMOND_SWORD,
                Items.DIAMOND_CHESTPLATE, Items.LEATHER_BOOTS, Items.STONE_PICKAXE,
                Items.NETHERITE_HELMET, Items.IRON_NUGGET}) {
            if (EarthMetal.é(new ItemStack(qual))) helper.fail(qual + " não é");
        }

        if (EarthMetal.lingoteDe(Blocks.IRON_ORE.defaultBlockState()) != Items.IRON_INGOT) {
            helper.fail("e o minério de ferro funde-se em ferro");
        }
        if (EarthMetal.lingoteDe(Blocks.DEEPSLATE_GOLD_ORE.defaultBlockState()) != Items.GOLD_INGOT) {
            helper.fail("o de ouro em ouro");
        }
        if (EarthMetal.lingoteDe(Blocks.DIAMOND_ORE.defaultBlockState()) != null) {
            helper.fail("e o de diamante em nada");
        }
        if (!EarthMetal.oQueFica(Blocks.DEEPSLATE_IRON_ORE.defaultBlockState())
                .defaultBlockState().is(Blocks.DEEPSLATE)) {
            helper.fail("e do minério de ardósia fica ardósia");
        }
        helper.succeed();
    }

    /**
     * <b>Cair mais de três blocos em terra mole arranca o bloco de baixo — e a queda não dói.</b>
     *
     * <p>É o único poder dela que não precisa da Mão de Bruxa, e é por ele que ela se nota logo: quem se
     * infunde do Mundo descobre-o caindo.
     */
    @GameTest
    public void fallingTearsTheGroundOut(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos chão = helper.absolutePos(new BlockPos(4, 2, 4));
        level.setBlockAndUpdate(chão, Blocks.GRASS_BLOCK.defaultBlockState());

        ServerPlayer quem = gente(helper, new BlockPos(4, 3, 4));
        double conta = Infusions.de(quem).cai(level, quem, 5.0);

        if (conta != 0.0) helper.fail("a queda deixa de doer; contou " + conta);
        if (!level.getBlockState(chão).isAir()) helper.fail("e o bloco de baixo é arrancado");
        if (Infusions.energia(quem) != Infusions.CARGAS - OverworldInfusion.CUSTO_ARRANCAR) {
            helper.fail("e custa cinco; custou " + (Infusions.CARGAS - Infusions.energia(quem)));
        }

        boolean caiu = !level.getEntitiesOfClass(ItemEntity.class,
                new net.minecraft.world.phys.AABB(chão).inflate(2.0),
                largado -> largado.getItem().is(Blocks.GRASS_BLOCK.asItem())).isEmpty();
        if (!caiu) helper.fail("e fica em item no chão");

        // uma queda curta não faz nada, e uma queda em pedra também não
        level.setBlockAndUpdate(chão, Blocks.STONE.defaultBlockState());
        if (Infusions.de(quem).cai(level, quem, 5.0) != 5.0) helper.fail("em pedra não pega");
        if (Infusions.de(quem).cai(level, quem, 2.0) != 2.0) helper.fail("e dois blocos não bastam");

        level.setBlockAndUpdate(chão, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /**
     * <b>O soco atira longe quem tem metal no corpo — e não mexe com quem não tem.</b>
     *
     * <p>Esta é a segunda prova da fatia. O original olha os cinco lugares de equipamento e basta um deles
     * ser de ferro, de ouro ou de malha; de diamante, ou pelado, ninguém voa. É a única vez em todo o mod em
     * que a <b>boa armadura é um perigo</b>.
     */
    @GameTest
    public void thePunchOnlyThrowsWhoeverWearsMetal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = gente(helper, new BlockPos(2, 2, 2));
        var mão = new ItemStack(OccultaItems.WITCH_HAND);

        var nu = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(3, 2, 3));
        nu.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
        nu.setDeltaMovement(Vec3.ZERO);
        Infusions.de(quem).soca(level, quem, mão, nu);
        if (nu.getDeltaMovement().y > 0.0) helper.fail("de diamante ninguém voa");
        if (Infusions.energia(quem) != Infusions.CARGAS) helper.fail("e não custa nada");

        var ferrado = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(5, 2, 5));
        ferrado.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        ferrado.setDeltaMovement(Vec3.ZERO);
        Infusions.de(quem).soca(level, quem, mão, ferrado);
        if (Math.abs(ferrado.getDeltaMovement().y - OverworldInfusion.SOBE) > 1.0E-6) {
            helper.fail("de ferro voa três décimos para cima; voou "
                    + ferrado.getDeltaMovement().y);
        }
        if (Infusions.energia(quem) != Infusions.CARGAS - OverworldInfusion.CUSTO_SOCO) {
            helper.fail("e custa dois; custou " + (Infusions.CARGAS - Infusions.energia(quem)));
        }

        // e agachado atira-o para cima, com um e meio em vez de três décimos
        quem.setShiftKeyDown(true);
        ferrado.setDeltaMovement(Vec3.ZERO);
        Infusions.de(quem).soca(level, quem, mão, ferrado);
        if (Math.abs(ferrado.getDeltaMovement().y - OverworldInfusion.SOBE_AGACHADO) > 1.0E-6) {
            helper.fail("agachado, um e meio; foi " + ferrado.getDeltaMovement().y);
        }
        quem.setShiftKeyDown(false);

        nu.discard();
        ferrado.discard();
        helper.succeed();
    }

    /**
     * <b>O ímã puxa o metal largado, e deixa o resto onde está.</b>
     *
     * <p>A conta do puxão é a estranha do original — ele divide as três componentes pelo módulo da
     * primeira —, e o que se prova aqui é o que ela faz de observável: o metal <b>mexe-se</b> e o que não é
     * metal não.
     */
    @GameTest
    public void theMagnetPullsMetalOnly(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = gente(helper, new BlockPos(2, 2, 2));

        BlockPos ali = helper.absolutePos(new BlockPos(5, 3, 5));
        var ferro = new ItemEntity(level, ali.getX() + 0.5, ali.getY() + 0.5, ali.getZ() + 0.5,
                new ItemStack(Items.IRON_INGOT));
        var pedra = new ItemEntity(level, ali.getX() + 0.5, ali.getY() + 0.5, ali.getZ() + 0.5,
                new ItemStack(Items.DIAMOND));
        ferro.setNoGravity(true);
        pedra.setNoGravity(true);
        level.addFreshEntity(ferro);
        level.addFreshEntity(pedra);
        Vec3 estava = ferro.position();
        Vec3 estavaPedra = pedra.position();

        OverworldInfusion.puxaOMetal(level, quem);

        if (ferro.position().distanceTo(estava) < 0.5) {
            helper.fail("o lingote devia ter vindo; andou "
                    + ferro.position().distanceTo(estava));
        }
        if (pedra.position().distanceTo(estavaPedra) > 1.0E-6) {
            helper.fail("e o diamante devia ter ficado");
        }
        ferro.discard();
        pedra.discard();
        helper.succeed();
    }

    /**
     * <b>Largar a Mão olhando para um bicho armado de metal desarma-o.</b>
     *
     * <p>Olhando para quem tem uma espada de ferro, ela cai no chão. É o poder mais barato dela e o mais
     * calado: não faz barulho, não faz dano, só tira a arma.
     */
    @GameTest(maxTicks = 40)
    public void theDisarmDropsTheMetal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }

        var zumbi = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(3, 2, 3));
        zumbi.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));

        ServerPlayer quem = gente(helper, new BlockPos(3, 2, 6));
        mira(quem, zumbi.getBoundingBox().getCenter());

        Infusions.de(quem).largou(level, quem, new ItemStack(OccultaItems.WITCH_HAND),
                Infusions.SEGURA);

        if (!zumbi.getMainHandItem().isEmpty()) {
            helper.fail("a espada de ferro devia ter caído; ele ainda tem "
                    + zumbi.getMainHandItem());
        }
        if (Infusions.energia(quem) != Infusions.CARGAS - OverworldInfusion.CUSTO_DESARMAR) {
            helper.fail("e custa dois; custou " + (Infusions.CARGAS - Infusions.energia(quem)));
        }
        zumbi.discard();
        helper.succeed();
    }

    /**
     * <b>E olhando para o topo de um bloco, levanta uma coluna de seis três níveis.</b>
     *
     * <p>Ela precisa de <b>dez blocos de chão firme</b> por baixo — é o que impede que se levante o telhado
     * de uma caverna —, e deixa <b>três de ar</b> em baixo, que é o que a coluna lhe tirou.
     */
    @GameTest
    public void theColumnRisesThreeLevels(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        /*
         * A arena tem oito blocos de altura e uma <b>tampa de barreira</b> por cima, no oitavo nível: de
         * pé no sétimo, os <b>olhos</b> de quem olha já estão dentro da tampa, e o traçado acerta nela e
         * não no chão. Por isso a coluna é montada a meia altura, com quem a levanta no sexto nível — e o
         * chão firme que o poder exige, que são dez blocos, desce por baixo do piso da arena, até o fundo
         * do mundo.
         */
        BlockPos topo = helper.absolutePos(new BlockPos(3, 3, 3));
        level.setBlockAndUpdate(topo, Blocks.COBBLESTONE.defaultBlockState());
        for (int fundo = 1; topo.getY() - fundo >= level.getMinY(); fundo++) {
            level.setBlockAndUpdate(topo.below(fundo), Blocks.STONE.defaultBlockState());
        }

        ServerPlayer quem = gente(helper, new BlockPos(3, 6, 3));
        quem.setXRot(90.0f);
        quem.setYRot(0.0f);

        Infusions.de(quem).largou(level, quem, new ItemStack(OccultaItems.WITCH_HAND),
                Infusions.SEGURA);

        if (!level.getBlockState(topo.above(OverworldInfusion.LEVANTA)).is(Blocks.COBBLESTONE)) {
            helper.fail("o pedregulho devia ter subido três; está em "
                    + level.getBlockState(topo.above(OverworldInfusion.LEVANTA)));
        }
        if (!level.getBlockState(topo.below(4)).isAir()) {
            helper.fail("e devia ficar ar em baixo, que é o que a coluna lhe tirou; está "
                    + level.getBlockState(topo.below(4)));
        }
        if (Infusions.energia(quem) != Infusions.CARGAS - OverworldInfusion.CUSTO_LEVANTAR) {
            helper.fail("e custa dois; custou " + (Infusions.CARGAS - Infusions.energia(quem)));
        }

        for (int volta = -8; volta <= 4; volta++) {
            level.setBlockAndUpdate(topo.above(volta), Blocks.AIR.defaultBlockState());
        }
        helper.succeed();
    }

    /**
     * <b>Agachado, olhar para um minério funde-o em dois lingotes.</b>
     *
     * <p>O dobro do que a fundição de longe dá, que é o que paga o trabalho de ir até lá.
     */
    @GameTest
    public void theCloseSmeltGivesTwoIngots(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos minério = helper.absolutePos(new BlockPos(5, 1, 5));
        level.setBlockAndUpdate(minério, Blocks.IRON_ORE.defaultBlockState());

        ServerPlayer quem = gente(helper, new BlockPos(5, 4, 5));
        quem.setXRot(90.0f);
        quem.setYRot(0.0f);
        quem.setShiftKeyDown(true);

        Infusions.de(quem).largou(level, quem, new ItemStack(OccultaItems.WITCH_HAND),
                Infusions.SEGURA);
        quem.setShiftKeyDown(false);

        if (!level.getBlockState(minério).is(Blocks.STONE)) {
            helper.fail("no lugar do minério fica pedra; ficou " + level.getBlockState(minério));
        }
        int quantos = 0;
        for (var largado : level.getEntitiesOfClass(ItemEntity.class,
                new net.minecraft.world.phys.AABB(minério).inflate(2.0))) {
            if (largado.getItem().is(Items.IRON_INGOT)) quantos += largado.getItem().getCount();
        }
        if (quantos != OverworldInfusion.DOBRO) {
            helper.fail("e saem dois lingotes; saíram " + quantos);
        }
        level.setBlockAndUpdate(minério, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /**
     * <b>A onda de choque levanta o chão e põe-no de volta.</b>
     *
     * <p>Esta é a prova que carrega a fatia, e é sobre a <b>segunda metade</b> do poder: o anel sobe, abre-se
     * e <b>desce outra vez</b>. Passada a onda, o terreno está como estava — e é isso que faz dela um poder
     * de bruxa e não uma bomba lenta.
     *
     * <p>Ela é desenhada com o <b>círculo de Bresenham</b>, e por isso tem o aspecto quadrado que tem.
     */
    @GameTest(maxTicks = 60)
    public void theShockwavePutsTheGroundBack(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 0, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }

        ServerPlayer quem = gente(helper, new BlockPos(3, 2, 3));
        Shockwave.começa(level, quem, 0);
        if (Shockwave.quantas() != 1) helper.fail("devia haver uma onda andando");

        helper.runAfterDelay(6, () -> {
            if (Shockwave.quantas() != 0) helper.fail("e ela devia ter acabado");

            for (int x = 0; x < 8; x++) {
                for (int z = 0; z < 8; z++) {
                    BlockPos aqui = helper.absolutePos(new BlockPos(x, 1, z));
                    if (!level.getBlockState(aqui).is(Blocks.STONE)) {
                        helper.fail("o chão devia estar como estava; em " + x + "," + z + " está "
                                + level.getBlockState(aqui));
                        return;
                    }
                    if (!level.getBlockState(aqui.above()).isAir()) {
                        helper.fail("e nada devia ter ficado levantado em " + x + "," + z);
                        return;
                    }
                }
            }
            helper.succeed();
        });
    }
}
