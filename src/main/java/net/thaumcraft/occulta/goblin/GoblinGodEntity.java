package net.thaumcraft.occulta.goblin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * O que o <b>Mog</b> e o <b>Gulg</b> têm em comum: os dois {@code EntityGoblinMog} e {@code EntityGoblinGulg}
 * do Witchery, que são a mesma classe escrita duas vezes com dez linhas de diferença.
 *
 * <h2>A ideia</h2>
 *
 * <p>São <b>dois chefes de quatrocentos de vida</b>, e a graça deles não está em nenhum dos dois: está na
 * <b>distância entre eles</b>.
 *
 * <ul>
 *   <li>a <b>três blocos ou menos</b> um do outro, os dois são <b>invencíveis</b> — nenhum dano passa;</li>
 *   <li>a seis, só um quinto passa; a nove, metade; a dezesseis, quatro quintos;</li>
 *   <li>e, longe um do outro, tudo passa — mas nunca mais de <b>quinze por golpe</b>.</li>
 * </ul>
 *
 * <p>E o espelho disso: o <b>murro do Gulg</b> cresce com a mesma proximidade. Juntos, eles são uma parede
 * que mata; separados, são dois bichos grandes que se matam. A luta inteira é <b>sobre separá-los</b> — e o
 * Gulg tem uma vontade que o leva de volta para junto do Mog, e o Mog salta para longe de quem o persegue.
 *
 * <p>É o melhor desenho de chefe que o Witchery tem, e não há nele um só poder novo: só uma conta de
 * distância, escrita duas vezes com o sinal trocado.
 */
public abstract class GoblinGodEntity extends Monster {
    /** Os quadrados de distância do original, que são três, seis, nove e dezesseis blocos. */
    public static final double INVENCÍVEL = 9.0;
    public static final double UM_QUINTO = 36.0;
    public static final double METADE = 81.0;
    public static final double QUATRO_QUINTOS = 256.0;

    /** O teto de dano por golpe, que nenhuma distância levanta. */
    public static final float TETO = 15.0f;

    /** A que distância ele procura o par. */
    public static final double PROCURA_O_PAR = 16.0;

    /** Quanto dura o despertar, e quanto ele cura por décimo de segundo enquanto dura. */
    public static final int DESPERTAR = 150;
    public static final float CURA_DESPERTANDO = 20.0f;

    /** E quanto ele cura por segundo depois, que é o que faz dele uma parede. */
    public static final float CURA = 1.0f;

    /** De quantas em quantas batidas ele pode saltar, quando está preso. */
    public static final int SALTO = 300;

    /** A que distância ele salta, e o quanto o salto varia. */
    public static final double LONGE = 16.0;
    public static final double VARIA = 8.0;

    /** Quantas pepitas de koboldite ele larga, no mínimo e no máximo. */
    public static final int PEPITAS = 3;

    /** E com que nível a malha que ele larga vem encantada. */
    public static final int ENCANTO = 30;

    private int despertando;
    private long últimoSalto;
    protected int murro;

    private final ServerBossEvent barra = new ServerBossEvent(this.getUUID(), Component.empty(),
            BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS);

    protected GoblinGodEntity(EntityType<? extends GoblinGodEntity> tipo, Level level) {
        super(tipo, level);
        this.xpReward = 35;
        this.setPersistenceRequired();
        this.getNavigation().setCanFloat(true);
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 400.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.FOLLOW_RANGE, 50.0);
    }

    /** Qual dos dois é o par deste. */
    protected abstract Class<? extends GoblinGodEntity> oPar();

    /** E a peça de roupa que ele larga, metade das vezes. */
    protected abstract net.minecraft.world.item.Item aSuaPeça();

    // ------------------------------------------------------------------ o despertar

    /** <b>O despertar</b>: cento e cinquenta batidas de invencibilidade, como o do Wither. */
    public void desperta() {
        this.despertando = DESPERTAR;
        this.setHealth(this.getMaxHealth() / 4.0f);
    }

    public int despertando() {
        return this.despertando;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (this.despertando > 0) {
            this.despertando--;
            if (this.despertando <= 0) {
                level.levelEvent(1023, this.blockPosition(), 0);
            }
            if (this.tickCount % 10 == 0) this.heal(CURA_DESPERTANDO);
            return;
        }

        super.customServerAiStep(level);
        if (this.tickCount % 20 == 0) this.heal(CURA);

        /*
         * <b>E ele não se deixa encurralar.</b> Preso sem caminho até quem persegue, de cinco em cinco
         * segundos ele salta dezesseis blocos para trás dele, como um enderman. É o que impede que se o
         * mate de cima de uma torre.
         */
        if (this.getNavigation().isDone() && this.getTarget() != null
                && this.tickCount - this.últimoSalto > SALTO) {
            this.últimoSalto = this.tickCount;
            this.saltaPara(level, this.getTarget());
        }

        this.barra.setProgress(this.getHealth() / this.getMaxHealth());
    }

    /** O salto do original, que é o do enderman com a conta dele. */
    protected boolean saltaPara(ServerLevel level, LivingEntity quem) {
        Vec3 rumo = new Vec3(this.getX() - quem.getX(),
                this.getBoundingBox().minY + this.getBbHeight() / 2.0 - quem.getY() - quem.getEyeHeight(),
                this.getZ() - quem.getZ()).normalize();
        double x = this.getX() + (this.random.nextDouble() - 0.5) * VARIA - rumo.x * LONGE;
        double y = this.getY() + (this.random.nextInt(16) - 8) - rumo.y * LONGE;
        double z = this.getZ() + (this.random.nextDouble() - 0.5) * VARIA - rumo.z * LONGE;
        return this.salta(level, x, y, z);
    }

    /** E o pouso: desce até achar chão, e desiste se não couber. */
    protected boolean salta(ServerLevel level, double x, double y, double z) {
        Vec3 antes = this.position();
        BlockPos onde = BlockPos.containing(x, y, z);
        while (onde.getY() > level.getMinY() && !level.getBlockState(onde.below()).isSolid()) {
            onde = onde.below();
        }
        if (onde.getY() <= level.getMinY()) return false;

        this.setPos(x, onde.getY(), z);
        if (!level.noCollision(this) || level.containsAnyLiquid(this.getBoundingBox())) {
            this.setPos(antes.x, antes.y, antes.z);
            return false;
        }

        for (int volta = 0; volta < 128; volta++) {
            double quanto = volta / 127.0;
            level.sendParticles(ParticleTypes.PORTAL,
                    antes.x + (this.getX() - antes.x) * quanto
                            + (this.random.nextDouble() - 0.5) * this.getBbWidth() * 2.0,
                    antes.y + (this.getY() - antes.y) * quanto
                            + this.random.nextDouble() * this.getBbHeight(),
                    antes.z + (this.getZ() - antes.z) * quanto
                            + (this.random.nextDouble() - 0.5) * this.getBbWidth() * 2.0,
                    1, 0.0, 0.0, 0.0, 0.2);
        }
        level.playSound(null, antes.x, antes.y, antes.z, SoundEvents.ENDERMAN_TELEPORT,
                this.getSoundSource(), 1.0f, 1.0f);
        this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0f, 1.0f);
        return true;
    }

    // ------------------------------------------------------------------ a conta da distância

    /**
     * <b>A distância ao par, ao quadrado</b>, ou o infinito se não houver par por perto.
     *
     * <p>Sozinho, um deus goblin é um bicho comum de quatrocentos de vida. É de propósito.
     */
    public double aoPar() {
        double menos = Double.MAX_VALUE;
        AABB caixa = this.getBoundingBox().inflate(PROCURA_O_PAR);
        for (GoblinGodEntity outro : this.level().getEntitiesOfClass(GoblinGodEntity.class, caixa,
                quem -> this.oPar().isInstance(quem))) {
            menos = Math.min(menos, this.distanceToSqr(outro));
        }
        return menos;
    }

    /** O quanto do dano passa, conforme a distância ao par. */
    public static double quantoPassa(double longe) {
        if (longe <= INVENCÍVEL) return 0.0;
        if (longe <= UM_QUINTO) return 0.2;
        if (longe <= METADE) return 0.5;
        if (longe <= QUATRO_QUINTOS) return 0.8;
        return 1.0;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource fonte, float quanto) {
        if (this.despertando > 0 && !fonte.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        double passa = quantoPassa(this.aoPar());
        if (passa <= 0.0) return false;
        return super.hurtServer(level, fonte, (float) Math.min(quanto * passa, TETO));
    }

    // ------------------------------------------------------------------ o resto

    /** Eles não se mordem: nem entre si, nem aos goblins comuns. */
    @Override
    public void setTarget(@Nullable LivingEntity quem) {
        if (quem instanceof GoblinGodEntity || quem instanceof GoblinEntity) return;
        super.setTarget(quem);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double longe) {
        return false;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.murro > 0) this.murro--;
    }

    /** O que eles largam: pepitas de koboldite e uma peça de malha encantada. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource fonte, boolean quemMatou) {
        super.dropCustomDeathLoot(level, fonte, quemMatou);
        this.spawnAtLocation(level,
                new ItemStack(net.thaumcraft.occulta.OccultaItems.KOBOLDITE_NUGGET,
                        this.random.nextInt(PEPITAS) + 1), 0.0f);

        ItemStack malha = new ItemStack(switch (this.random.nextInt(4)) {
            case 0 -> Items.CHAINMAIL_BOOTS;
            case 1 -> Items.CHAINMAIL_LEGGINGS;
            case 2 -> Items.CHAINMAIL_CHESTPLATE;
            default -> Items.CHAINMAIL_HELMET;
        });
        this.spawnAtLocation(level, net.minecraft.world.item.enchantment.EnchantmentHelper
                .enchantItem(this.random, malha, ENCANTO, level.registryAccess(),
                        java.util.Optional.empty()), 0.0f);

        /*
         * E <b>metade das vezes</b>, a peça de roupa dele: a Aljava do Mog ou a Cinta do Gulg. Elas não
         * se fabricam, e é esta a única porta delas — o que quer dizer que a conta de distância que os
         * torna invencíveis <b>passa para quem os matou</b>, e só se os dois forem mortos.
         */
        if (this.random.nextInt(2) == 0) {
            this.spawnAtLocation(level, new ItemStack(this.aSuaPeça()), 0.0f);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput saída) {
        super.addAdditionalSaveData(saída);
        saída.putInt("Invul", this.despertando);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput entrada) {
        super.readAdditionalSaveData(entrada);
        this.despertando = entrada.getIntOr("Invul", 0);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer quem) {
        super.startSeenByPlayer(quem);
        this.barra.addPlayer(quem);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer quem) {
        super.stopSeenByPlayer(quem);
        this.barra.removePlayer(quem);
    }

    @Override
    public void setCustomName(@Nullable Component nome) {
        super.setCustomName(nome);
        this.barra.setName(this.getDisplayName());
    }
}
