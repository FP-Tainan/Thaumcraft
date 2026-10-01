package net.thaumcraft.arcana;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * A magia que <b>fica</b>: a {@code EntitySpellEffect} do Ars Magica 2.
 *
 * <p>Enquanto o Projétil leva o feitiço a um lugar e morre, esta entidade <b>mora</b> num lugar e corre o
 * feitiço vezes sem conta enquanto durar. É dela que saem as três Formas que criam área: a <b>Zona</b>, que é
 * um disco parado; a <b>Parede</b>, que é uma linha; e a <b>Onda</b>, que é a mesma linha andando.
 *
 * <p><b>Ela leva o que sobra da frase, e não a frase inteira.</b> As três Formas tiram a etapa delas antes de
 * entregar — por isso são <i>principum</i>, e por isso pedem outra Forma depois. Uma Zona seguida de Toque e
 * Dano de Fogo é uma Zona que, de segundo em segundo, corre "Toque + Dano de Fogo" no lugar onde está.
 *
 * <p>E ela faz <b>duas coisas</b> de cada vez: manda as Essências do que sobrou em cada bicho que estiver
 * dentro dela, <b>e</b> lança o que sobrou dali. É o que faz uma Zona ferir quem entra e, ao mesmo tempo,
 * acender o chão embaixo dela.
 */
public class SpellEffectEntity extends Entity {
    /** O que ela é: as cinco que este porte traz. */
    public enum Kind {
        /** Um disco parado, que pega tudo à volta. */
        ZONE,
        /** Uma linha atravessada, que pega quem a cruza. */
        WALL,
        /** A mesma linha, andando para a frente. */
        WAVE,
        /**
         * A <b>Nevasca</b>: um temporal de gelo parado num lugar.
         *
         * <p>Ao contrário das três de cima, ela não corre o feitiço em ninguém — ela <b>faz o que faz</b>, a
         * cada batida: fere de gelo, prende, e vai deixando neve no chão. É por isso que ela é uma das dez
         * perícias prateadas: não é uma Forma, é um feitiço inteiro numa peça só.
         */
        BLIZZARD,
        /** E a <b>Chuva de Fogo</b>, a irmã dela: fere de fogo e vai pondo fogo no chão. */
        FIRE_RAIN
    }

    /** A Nevasca e a Chuva de Fogo agem <b>a cada batida</b>, como no original. */
    public static final int WEATHER_RATE = 1;

    /** O que a Nevasca fere por batida, antes do modificador de dano. */
    public static final float BLIZZARD_DAMAGE = 1.0f;

    /** E a Chuva de Fogo, que fere menos. */
    public static final float FIRE_RAIN_DAMAGE = 0.75f;

    /** Quantas batidas o gelo prende quem ele pega, e com que força. */
    public static final int BLIZZARD_HOLD = 80;
    public static final int BLIZZARD_HOLD_LEVEL = 3;

    /** De quantas em quantas batidas, mais ou menos, elas deixam alguma coisa no chão: duas em dez. */
    public static final int LEAVES_BEHIND = 2;

    /** De que altura cai o que se vê delas, e com que pressa. */
    public static final double CAI_DE = 10.0;
    public static final double VELOCIDADE = 0.6;

    /** De quantas em quantas batidas a Zona age: as 20 do original. */
    public static final int ZONE_RATE = 20;

    /** E a Parede, que é mais apertada: de cinco em cinco. */
    public static final int WALL_RATE = 5;

    /** A Onda age <b>a cada batida</b>, porque anda e não pode deixar buraco por onde passou. */
    public static final int WAVE_RATE = 1;

    /** Quão perto da linha um bicho tem de estar para a Parede o pegar. */
    public static final double WALL_REACH = 0.75;

    /** E quão perto na vertical. */
    public static final double WALL_HEIGHT = 2.0;

    private Spell spell = Spell.EMPTY;
    private Kind kind = Kind.ZONE;
    private float radius = 3.0f;
    private double gravity;
    private double speed;
    private int life = 100;
    private int untilNext;
    private @Nullable LivingEntity caster;
    private int casterId = -1;

    /** Se a primeira volta já correu: o {@code firstApply} do original. */
    private boolean firstApply = true;

    public SpellEffectEntity(EntityType<? extends SpellEffectEntity> type, Level level) {
        super(type, level);
        this.noPhysics = false;
    }

    public SpellEffectEntity(ServerLevel level, LivingEntity quem, Spell sobra, Kind qual) {
        this(ArcanaEntities.SPELL_EFFECT, level);
        this.caster = quem;
        this.casterId = quem.getId();
        this.spell = sobra;
        this.kind = qual;
        this.untilNext = rate();
    }

    private int rate() {
        return switch (this.kind) {
            case ZONE -> ZONE_RATE;
            case WALL -> WALL_RATE;
            case WAVE, BLIZZARD, FIRE_RAIN -> WEATHER_RATE;
        };
    }

    /** O quanto o dano dela foi multiplicado: o {@code damageBonus} do original. */
    private float damageBonus = 1.0f;

    public void setDamageBonus(float quanto) {
        this.damageBonus = quanto;
    }

    /** Faz dela uma Nevasca, ou uma Chuva de Fogo. */
    public void setWeather(Kind qual) {
        this.kind = qual;
        this.untilNext = WEATHER_RATE;
    }

    public Spell spell() {
        return this.spell;
    }

    public Kind kind() {
        return this.kind;
    }

    public float radius() {
        return this.radius;
    }

    public void setRadius(float quanto) {
        this.radius = quanto;
    }

    public void setGravity(double quanto) {
        this.gravity = quanto;
    }

    public void setLife(int batidas) {
        this.life = batidas;
    }

    /** A velocidade com que a Onda anda, e o giro para onde ela vai. */
    public void setWave(float giro, double velocidade) {
        this.kind = Kind.WAVE;
        this.speed = velocidade;
        this.untilNext = WAVE_RATE;
        this.setYRot(giro);
    }

    /** O giro da Parede, que é o que decide para que lado ela se estende. */
    public void setWall(float giro) {
        this.kind = Kind.WALL;
        this.untilNext = WALL_RATE;
        this.setYRot(giro);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    // ------------------------------------------------------------------ a vida dela

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) return;

        if (this.tickCount >= this.life || this.spell.isEmpty()) {
            this.discard();
            return;
        }

        switch (this.kind) {
            case ZONE -> zone(level);
            case WALL -> wall(level);
            case WAVE -> wave(level);
            case BLIZZARD, FIRE_RAIN -> temporal(level);
        }
    }

    /**
     * A <b>Nevasca</b> e a <b>Chuva de Fogo</b>: as duas que não correm feitiço nenhum.
     *
     * <p>Elas são a exceção desta entidade. As outras três acham quem está lá e passam-lhe o resto da frase;
     * estas duas <b>fazem o que fazem</b> e mais nada — ferem, prendem ou queimam, e vão deixando neve ou
     * fogo no chão. No original são duas perícias prateadas, e é isso que elas são: um feitiço inteiro numa
     * peça só.
     *
     * <p>E as duas fazem uma coisa que nenhuma outra faz: <b>desfazem o empurrão</b> que a pancada daria.
     * Sem isso, quem estivesse dentro saltaria para fora na primeira batida e a nevasca não seria nevasca
     * nenhuma.
     */
    private void temporal(ServerLevel level) {
        boolean gelo = this.kind == Kind.BLIZZARD;
        chuva(level, gelo);

        float dano = (gelo ? BLIZZARD_DAMAGE : FIRE_RAIN_DAMAGE) * this.damageBonus;

        var caixa = this.getBoundingBox().inflate(this.radius, 1.0, this.radius);
        for (LivingEntity quem : level.getEntitiesOfClass(LivingEntity.class, caixa)) {
            if (quem == this.caster) continue;

            Vec3 antes = quem.getDeltaMovement();
            if (gelo) {
                quem.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        ArcanaEffects.FROST_SLOW, BLIZZARD_HOLD, BLIZZARD_HOLD_LEVEL));
            }
            var fonte = gelo
                    ? ArcanaDamage.frost(level, this.caster == null ? quem : this.caster)
                    : ArcanaDamage.fire(level, this.caster == null ? quem : this.caster);
            if (quem.hurtServer(level, fonte, dano) && !(quem instanceof net.minecraft.world.entity.player.Player)) {
                quem.invulnerableTime = gelo ? 15 : 10;
            }
            // desfaz o empurrão que a pancada deu
            quem.setDeltaMovement(antes);
        }

        if (level.getRandom().nextInt(10) >= LEAVES_BEHIND) return;
        int raio = Math.max(1, (int) Math.ceil(this.radius));
        BlockPos onde = BlockPos.containing(
                this.getX() - raio + level.getRandom().nextInt(raio * 2),
                this.getY() + (gelo ? level.getRandom().nextInt(2) : 0),
                this.getZ() - raio + level.getRandom().nextInt(raio * 2));

        if (!level.getBlockState(onde).isAir()) return;
        if (gelo) {
            if (level.getBlockState(onde.below()).isAir()) return;
            level.setBlockAndUpdate(onde, net.minecraft.world.level.block.Blocks.SNOW.defaultBlockState());
        } else {
            level.setBlockAndUpdate(onde, net.minecraft.world.level.block.Blocks.FIRE.defaultBlockState());
        }
    }

    /**
     * A <b>Zona</b>: um disco parado que, de segundo em segundo, corre o que sobrou da frase.
     *
     * <p>Ela sobe ou desce pela gravidade que lhe deram — e há uma manha do original aqui: quando a gravidade
     * é <b>negativa</b> e não é a primeira volta, ela lança o feitiço <b>um bloco abaixo</b> de si. É o que faz
     * uma Zona que afunda ir deixando efeito no chão por onde passa em vez de no ar onde está.
     */
    private void zone(ServerLevel level) {
        if (this.gravity != 0.0) this.setPos(this.getX(), this.getY() + this.gravity, this.getZ());
        SpellFx.zone(level, this.spell, this.position(), this.radius, this.tickCount);

        if (--this.untilNext > 0) return;
        this.untilNext = ZONE_RATE;

        for (Entity quem : level.getEntities(this, caixa())) {
            if (quem instanceof LivingEntity) SpellCast.onEntity(level, this.spell, quemLançou(), quem);
        }

        double y = this.gravity < 0.0 && !this.firstApply ? this.getY() - 1.0 : this.getY();
        SpellCast.cast(level, this.spell, quemLançou(), null, new Vec3(this.getX(), y, this.getZ()));
        this.firstApply = false;
    }

    /**
     * A <b>Parede</b>: uma linha atravessada no caminho, que corre o feitiço em quem a cruzar.
     *
     * <p>Ela não é uma caixa: é um <b>segmento de reta</b>, e cada bicho que entra na caixa larga é medido
     * contra ela. Só pega quem estiver a menos de três quartos de bloco da linha e a menos de dois de altura —
     * quem passa por cima ou por longe atravessa sem sentir nada.
     */
    private void wall(ServerLevel level) {
        if (--this.untilNext > 0) return;
        this.untilNext = this.kind == Kind.WAVE ? WAVE_RATE : WALL_RATE;

        double dx = Math.cos(Math.toRadians(this.getYRot()));
        double dz = Math.sin(Math.toRadians(this.getYRot()));
        Vec3 a = new Vec3(this.getX() - dx * this.radius, this.getY(), this.getZ() - dz * this.radius);
        Vec3 b = new Vec3(this.getX() + dx * this.radius, this.getY(), this.getZ() + dz * this.radius);
        SpellFx.line(level, this.spell, a, b);

        for (Entity quem : level.getEntities(this, caixa())) {
            if (!(quem instanceof LivingEntity)) continue;
            if (quem.getId() == this.casterId) continue;

            Vec3 perto = naLinha(a, b, quem.position());
            double deLado = Math.hypot(perto.x - quem.getX(), perto.z - quem.getZ());
            double deAltura = Math.abs(this.getY() - quem.getY());
            if (deLado < WALL_REACH && deAltura < WALL_HEIGHT) {
                SpellCast.cast(level, this.spell, quemLançou(), quem, this.position());
            }
        }
    }

    /**
     * A <b>Onda</b>: a mesma Parede, andando — e deixando o feitiço no chão por onde passa.
     *
     * <p>É a única das três que mexe no mundo: ela corre o feitiço em <b>cada bloco</b> da linha que acabou de
     * atravessar, e é por isso que uma Onda de Escavar abre uma vala e uma Onda de Luz deixa um rastro aceso.
     */
    private void wave(ServerLevel level) {
        wall(level);
        if (this.isRemoved()) return;

        double dx = Math.cos(Math.toRadians(this.getYRot() + 90.0f));
        double dz = Math.sin(Math.toRadians(this.getYRot() + 90.0f));
        this.setPos(this.getX() + dx * this.speed, this.getY() + this.gravity, this.getZ() + dz * this.speed);

        double hx = Math.cos(Math.toRadians(this.getYRot()));
        double hz = Math.sin(Math.toRadians(this.getYRot()));
        Vec3 a = new Vec3(this.getX() - hx * this.radius, this.getY(), this.getZ() - hz * this.radius);
        Vec3 b = new Vec3(this.getX() + hx * this.radius, this.getY(), this.getZ() + hz * this.radius);

        for (BlockPos onde : entre(a, b)) {
            SpellCast.onBlock(level, this.spell, quemLançou(), onde, Direction.UP, Vec3.atCenterOf(onde));
        }
    }

    // ------------------------------------------------------------------ a geometria

    /** A caixa em que ela procura: o raio em volta, um bloco abaixo e três acima. */
    private AABB caixa() {
        return new AABB(this.getX() - this.radius, this.getY() - 1.0, this.getZ() - this.radius,
                this.getX() + this.radius, this.getY() + 3.0, this.getZ() + this.radius);
    }

    /** O ponto do segmento {@code a—b} mais perto daquele: o {@code closestPointOnLine} do original. */
    public static Vec3 naLinha(Vec3 a, Vec3 b, Vec3 onde) {
        Vec3 reta = b.subtract(a);
        double comprimento = reta.lengthSqr();
        if (comprimento == 0.0) return a;
        double t = Math.clamp(onde.subtract(a).dot(reta) / comprimento, 0.0, 1.0);
        return a.add(reta.scale(t));
    }

    /** Os blocos que a linha atravessa: o {@code getAllBlockLocationsBetween}. */
    public static java.util.List<BlockPos> entre(Vec3 a, Vec3 b) {
        var saco = new java.util.LinkedHashSet<BlockPos>();
        double comprimento = a.distanceTo(b);
        int passos = Math.max(1, (int) Math.ceil(comprimento * 2.0));
        for (int i = 0; i <= passos; i++) {
            Vec3 onde = a.add(b.subtract(a).scale((double) i / passos));
            saco.add(BlockPos.containing(onde));
        }
        return java.util.List.copyOf(saco);
    }

    /**
     * Quem lançou, ou um substituto.
     *
     * <p>No original isto é um {@code DummyEntityPlayer}: um jogador de mentira que existe só para o feitiço
     * ter de quem partir depois de quem o lançou já ter ido embora. Aqui, se quem lançou sumiu, a área morre —
     * o que é mais simples e não deixa um jogador fantasma no mundo.
     */
    private LivingEntity quemLançou() {
        if (this.caster == null || this.caster.isRemoved()) {
            if (this.level() instanceof ServerLevel level && this.casterId >= 0
                    && level.getEntity(this.casterId) instanceof LivingEntity achado) {
                this.caster = achado;
            }
        }
        if (this.caster == null || this.caster.isRemoved()) {
            this.discard();
            return null;
        }
        return this.caster;
    }

    /** Ela não se toca e não se fere: é um ponto invisível, e não uma coisa no mundo. */
    @Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel level,
                              net.minecraft.world.damagesource.DamageSource fonte, float dano) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    // ------------------------------------------------------------------ guardar

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.store("spell", Spell.CODEC, this.spell);
        output.putString("kind", this.kind.name().toLowerCase(java.util.Locale.ROOT));
        output.putFloat("radius", this.radius);
        output.putDouble("gravity", this.gravity);
        output.putDouble("speed", this.speed);
        output.putInt("life", this.life);
        output.putInt("until_next", this.untilNext);
        output.putInt("caster", this.casterId);
        output.putBoolean("first_apply", this.firstApply);
        output.putFloat("damage_bonus", this.damageBonus);
    }

    /**
     * O que se vê de uma Nevasca ou de uma Chuva de Fogo: coisa a cair de dez blocos acima.
     *
     * <p>O original lança <b>vinte</b> partículas por batida na Nevasca e <b>dez</b> na Chuva de Fogo, de
     * {@code y + 10} para baixo, espalhadas pelo raio. É o que faz as duas parecerem tempo e não um círculo
     * desenhado no chão — e sem isto elas seriam invisíveis, porque esta entidade não tem desenho nenhum.
     */
    private void chuva(ServerLevel level, boolean gelo) {
        int quantas = gelo ? 20 : 10;
        var qual = gelo ? net.minecraft.core.particles.ParticleTypes.SNOWFLAKE
                : net.minecraft.core.particles.ParticleTypes.FLAME;

        // uma a uma, e cada uma com rumo próprio: mandá-las em monte deixa-as paradas no ar, e o que faz
        // isto parecer tempo é elas caírem
        for (int i = 0; i < quantas; i++) {
            double x = this.getX() - this.radius + level.getRandom().nextDouble() * this.radius * 2.0;
            double z = this.getZ() - this.radius + level.getRandom().nextDouble() * this.radius * 2.0;
            level.sendParticles(qual, x, this.getY() + CAI_DE, z, 0, 0.0, -VELOCIDADE, 0.0, 1.0);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.spell = input.read("spell", Spell.CODEC).orElse(Spell.EMPTY);
        this.kind = switch (input.getStringOr("kind", "zone")) {
            case "wall" -> Kind.WALL;
            case "wave" -> Kind.WAVE;
            case "blizzard" -> Kind.BLIZZARD;
            case "fire_rain" -> Kind.FIRE_RAIN;
            default -> Kind.ZONE;
        };
        this.damageBonus = input.getFloatOr("damage_bonus", 1.0f);
        this.radius = input.getFloatOr("radius", 3.0f);
        this.gravity = input.getDoubleOr("gravity", 0.0);
        this.speed = input.getDoubleOr("speed", 0.0);
        this.life = input.getIntOr("life", 100);
        this.untilNext = input.getIntOr("until_next", rate());
        this.casterId = input.getIntOr("caster", -1);
        this.firstApply = input.getBooleanOr("first_apply", true);
    }
}
