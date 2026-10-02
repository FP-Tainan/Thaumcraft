package net.thaumcraft.arcana;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.Thaumcraft;

/**
 * A mana de quem lança: a parte do {@code ExtendedProperties} do Ars Magica 2 que conta o que um mago tem.
 *
 * <p>A lore separa as duas coisas e é bom que separe: <b>Vis</b> é a energia que existe no mundo; <b>Mana</b> é
 * o quanto um corpo consegue puxar e organizar dela de uma vez. Um arcanista pode secar a mana no meio de uma
 * aura cheia — a energia existe, o cano é que chegou ao fim.
 *
 * <p>Há <b>três</b> números. O <b>nível</b>, de zero a 99, que diz quanto o mago cresceu; a
 * <b>mana</b>, que se gasta e volta; e o <b>desgaste</b> — o {@code fatigue} do original —, que sobe a cada
 * feitiço e que, cheio, impede de lançar. É o que impede alguém de despejar feitiços sem parar por ter mana
 * de sobra.
 *
 * @param level   de zero a 99
 * @param mana    quanto há agora
 * @param burnout e quanto de desgaste há agora
 */
public record Mana(int level, float mana, float burnout, float xp) {
    /** A conta velha, sem experiência: continua valendo, e a experiência começa em zero. */
    public Mana(int level, float mana, float burnout) {
        this(level, mana, burnout, 0.0f);
    }

    /** O nível mais alto que se chega: o 99 do original. */
    public static final int MAX_LEVEL = 99;

    public static final Mana NONE = new Mana(0, 0.0f, 0.0f);

    public static final Codec<Mana> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("level", 0).forGetter(Mana::level),
            Codec.FLOAT.optionalFieldOf("mana", 0.0f).forGetter(Mana::mana),
            Codec.FLOAT.optionalFieldOf("burnout", 0.0f).forGetter(Mana::burnout),
            Codec.FLOAT.optionalFieldOf("xp", 0.0f).forGetter(Mana::xp))
            .apply(i, Mana::new));

    /** O mesmo, para ir pela rede: o Óculus e a barra desenham do lado de quem joga. */
    public static final net.minecraft.network.codec.StreamCodec<
            net.minecraft.network.RegistryFriendlyByteBuf, Mana> STREAM_CODEC =
            net.minecraft.network.codec.ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public static final AttachmentType<Mana> DATA = AttachmentRegistry.<Mana>builder()
            .initializer(() -> NONE)
            .persistent(CODEC)
            // o Óculus e a barra de mana leem isto do lado de cá
            .syncWith(STREAM_CODEC,
                    net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate.targetOnly())
            .copyOnDeath()
            .buildAndRegister(Thaumcraft.id("mana"));

    public static Mana of(Player quem) {
        return quem.getAttachedOrCreate(DATA);
    }

/**
     * Sem uso fora do porte: <b>obriga a classe a carregar</b>, e com ela o anexo a se registrar.
     *
     * <p>Isto não é enfeite. Um anexo que só se registra quando alguém o usa fica registrado <b>tarde demais</b>
     * do lado de quem joga: o servidor manda a mana no primeiro segundo, o cliente ainda não conhece o nome, e o pacote cai no chão
     * sem um pio. Foi assim que o Óculus mostrou nível zero a quem tinha cinquenta, e só uma foto disse.
     */
    public static void init() {
    }

    public static void set(Player quem, Mana agora) {
        quem.setAttached(DATA, agora);
    }

    /**
     * Quanta mana um mago daquele nível aguenta: a conta do {@code setMagicLevelWithMana}, tal e qual.
     *
     * <p>{@code nível^1,5 × (85 × nível/99) + 500}. Ela é lenta no começo e dispara no fim — um mago de nível
     * dez aguenta pouco mais do que um de nível zero, e um de 99 aguenta dezenas de vezes mais.
     */
    public static float maxManaFor(int level) {
        int nível = Math.clamp(level, 0, MAX_LEVEL);
        return (float) (Math.pow(nível, 1.5) * (85.0f * ((float) nível / MAX_LEVEL)) + 500.0);
    }

    /** E quanto desgaste ele aguenta antes de ter de parar: a mesma conta, que é o que o original usa. */
    public static float maxBurnoutFor(int level) {
        return maxManaFor(level);
    }

    public float maxMana() {
        return maxManaFor(this.level);
    }

    public float maxBurnout() {
        return maxBurnoutFor(this.level);
    }

    /** Se ela dá para o que se quer lançar. */
    public boolean has(float quanto) {
        return this.mana >= quanto;
    }

    /** E se ainda há espaço de desgaste para o que o feitiço deixa. */
    public boolean canBurn(float quanto) {
        return this.burnout + quanto <= this.maxBurnout();
    }

    public Mana withLevel(int level) {
        int novo = Math.clamp(level, 0, MAX_LEVEL);
        return new Mana(novo, Math.min(this.mana, maxManaFor(novo)), this.burnout, this.xp);
    }

    public Mana withMana(float mana) {
        return new Mana(this.level, Math.clamp(mana, 0.0f, this.maxMana()), this.burnout, this.xp);
    }

    public Mana withBurnout(float burnout) {
        return new Mana(this.level, this.mana, Math.clamp(burnout, 0.0f, this.maxBurnout()), this.xp);
    }

    /** Gasta o que o feitiço pede e soma o desgaste que ele deixa. */
    public Mana spend(float mana, float burnout) {
        return this.withMana(this.mana - mana).withBurnout(this.burnout + burnout);
    }

    /** Sobe um nível e enche a mana, como o original faz — e a experiência volta a zero. */
    public Mana levelUp() {
        int novo = Math.min(this.level + 1, MAX_LEVEL);
        return new Mana(novo, maxManaFor(novo), 0.0f, 0.0f);
    }

    /**
     * Quanta experiência falta para o nível seguinte: o {@code getXPToNextLevel} do original.
     *
     * <p>{@code (nível × 0,25)^1,5}. Ela cresce devagar no começo — do nível um para o dois basta um oitavo
     * de ponto — e vira uma parede no fim: do noventa e oito para o noventa e nove são quase 120.
     */
    public float xpToNextLevel() {
        return (float) Math.pow(this.level * 0.25f, 1.5);
    }

    /**
     * Soma experiência mágica, subindo <b>um</b> nível se der.
     *
     * <p><b>O que sobra se perde</b>, e é o original: o {@code addMagicXP} zera a experiência ao subir, sem
     * guardar o excesso e sem tornar a olhar. Quem ganhasse de uma vez o bastante para dois níveis só
     * subiria um.
     *
     * <p>Na prática quase nunca se nota, porque a experiência chega de cinco em cinco centésimos — mas nos
     * primeiros níveis, em que o que falta é um oitavo de ponto, ela chega a perder metade do que se ganhou.
     * Fica assim porque mexer nisso mudaria o ritmo de todo o começo do ramo.
     *
     * <p>No noventa e nove ela para: o original não deixa passar dali.
     */
    public Mana addXp(float quanto) {
        if (this.level >= MAX_LEVEL || quanto <= 0.0f) return this;
        Mana agora = new Mana(this.level, this.mana, this.burnout, this.xp + quanto);
        return agora.xp() >= agora.xpToNextLevel() ? agora.levelUp() : agora;
    }
}
