package net.thaumcraft.occulta;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import org.jetbrains.annotations.Nullable;

/**
 * A <b>poça de doença</b>: o {@code BlockDisease} do Witchery.
 *
 * <p>É o rasto de quem está <b>doente</b>. Quem traz a doença vai largando poças por onde passa, e cada poça
 * contagia quem pisar nela. É assim que uma doença deixa de ser de uma pessoa e passa a ser de um lugar: uma
 * casa, um curral, uma aldeia.
 *
 * <p>Ela <b>escorre para baixo</b> e <b>não se abre para os lados</b>. No original ela é um fluido do Forge
 * com uma casa de altura só — uma casa é tudo o que ela tem para dar, e por isso não sobra nada para
 * espalhar. O que ela faz é cair até achar chão, e ficar.
 *
 * <p>E ela <b>seca</b>: a cada quinze batidas há uma chance em dez de ela sumir. É a única coisa que a tira
 * do mapa, porque ela não se quebra — cem de dureza, como a nuvem e a poça de cozimento.
 *
 * <p>Nem todo mundo apanha. <b>Morto-vivo, coisa do inferno, lobisomem em bicho, chefe e golem</b> passam por
 * ela sem sentir: não há o que adoecer em quem já não está vivo, e o resto é forte demais.
 *
 * <p><b>Declarado:</b> o original tem ainda uma segunda maneira de a tirar — o <b>Leite Purificado</b>, no
 * terceiro grau, limpa todas as poças num cubo de dezenove casas à volta de quem bebeu. Esse cozimento
 * inteiro ainda não está no porte, e quando vier é esse pedaço que entra com ele.
 */
public class DiseaseBlock extends Block {
    public static final MapCodec<DiseaseBlock> CODEC = simpleCodec(DiseaseBlock::new);

    /** De quantas em quantas batidas ela mexe: a viscosidade do fluido dividida por duzentos. */
    public static final int TICK = 15;

    /** E a chance de sumir em cada uma delas: o {@code DiseaseBlockRemovalChance} do original. */
    public static final int SECA_UMA_EM = 10;

    /** Uma vez em três, quem pisa apanha. */
    public static final int PEGA_UMA_EM = 3;

    /** E apanha de um a quatro minutos. */
    public static final int MINUTOS = 4;

    public DiseaseBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    /**
     * <b>Não se esbarra nela</b>: atravessa-se como qualquer líquido. A caixa de <b>ver</b> continua sendo o
     * bloco inteiro, que é o que o original lhe dá, e é dela que vem a conta de quem está dentro.
     */
    @Override
    protected net.minecraft.world.phys.shapes.VoxelShape getCollisionShape(BlockState feitio,
            net.minecraft.world.level.BlockGetter level, BlockPos onde,
            net.minecraft.world.phys.shapes.CollisionContext contexto) {
        return net.minecraft.world.phys.shapes.Shapes.empty();
    }

    @Override
    protected void onPlace(BlockState feitio, Level level, BlockPos onde, BlockState antes, boolean mexeu) {
        level.scheduleTick(onde, this, TICK);
    }

    @Override
    protected void neighborChanged(BlockState feitio, Level level, BlockPos onde, Block qual,
                                   @Nullable Orientation lado, boolean mexeu) {
        level.scheduleTick(onde, this, TICK);
    }

    /** O {@code updateTick}: primeiro ela cai, e depois ela tem a chance de secar. */
    @Override
    protected void tick(BlockState feitio, ServerLevel level, BlockPos onde, RandomSource sorte) {
        BlockPos abaixo = onde.below();
        BlockState chão = level.getBlockState(abaixo);
        if (chão.canBeReplaced() && chão.getFluidState().isEmpty() && !chão.is(this)) {
            level.removeBlock(onde, false);
            level.setBlock(abaixo, feitio, Block.UPDATE_ALL);
            return;
        }

        if (sorte.nextInt(SECA_UMA_EM) == 0) {
            level.setBlock(onde, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            return;
        }
        level.scheduleTick(onde, this, TICK);
    }

    /** O {@code onEntityCollidedWithBlock}: quem pisa nela apanha, se tiver o que apanhar. */
    @Override
    protected void entityInside(BlockState feitio, Level level, BlockPos onde, Entity quem,
                                InsideBlockEffectApplier applier, boolean dentroMesmo) {
        if (!(level instanceof ServerLevel server) || !(quem instanceof LivingEntity vivo)) return;
        if (imune(vivo) || vivo.hasEffect(OccultaEffects.DISEASED)) return;
        if (server.getRandom().nextInt(PEGA_UMA_EM) != 0) return;
        int quanto = (1 + server.getRandom().nextInt(MINUTOS)) * 60 * 20;
        vivo.addEffect(new MobEffectInstance(OccultaEffects.DISEASED, quanto));
    }

    /**
     * Quem não apanha: o {@code CreatureUtil.isImmuneToDisease} do original.
     *
     * <p><b>Mudança declarada, a mesma dos símbolos:</b> o original pergunta {@code instanceof
     * IBossDisplayData} para saber o que é chefe, que é a interface da barra de chefe de 2014. Hoje são os
     * dois chefes do jogo e os do próprio mod.
     */
    public static boolean imune(LivingEntity vivo) {
        if (net.thaumcraft.occulta.Undead.é(vivo)) return true;
        if (net.thaumcraft.occulta.torment.Demonic.é(vivo)) return true;
        if (net.thaumcraft.occulta.wolf.Werewolf.emBicho(vivo)) return true;
        if (vivo instanceof net.minecraft.world.entity.animal.golem.AbstractGolem) return true;
        return vivo instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon
                || vivo instanceof net.minecraft.world.entity.boss.wither.WitherBoss
                || vivo instanceof net.thaumcraft.occulta.baba.BabaYagaEntity
                || vivo instanceof net.thaumcraft.occulta.goblin.GoblinGodEntity;
    }
}
