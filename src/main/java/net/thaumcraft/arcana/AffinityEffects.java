package net.thaumcraft.arcana;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;

/**
 * O que ter uma Afinidade faz de você: o {@code AffinityHelper} e o {@code AffinityModifiers} do Ars Magica 2.
 *
 * <p>Esta é a <b>recompensa</b> da roda. A {@link AffinityData} guarda o quanto cada Afinidade pegou; aqui é
 * onde isso vira andar mais rápido, respirar debaixo d'água, queimar quem encosta, atravessar a lava a pé.
 *
 * <p>E vira <b>fraqueza</b> também, e essa é a parte que o original acerta: <b>quase toda Afinidade tem um
 * preço</b>. Quem é de Fogo perde um quarto da vida quando está molhado. Quem é de Água perde um quarto quando
 * está pegando fogo. Quem é de Ender perde um quarto sob o sol. Quem é de Arcano leva <b>dez por cento a
 * mais</b> de todo dano. E quem é de Natureza anda <b>dez por cento mais devagar</b>, porque criou raiz.
 *
 * <p><b>As fraquezas param no 0,9.</b> Repare nos intervalos: a fraqueza da água vale de 0,5 a 0,9, e some
 * acima disso. Não é engano — é o original dizendo que quem <i>chegou ao fim</i> de uma Afinidade passou da
 * parte que dói. É o que faz valer a pena ir até lá em vez de ficar no meio.
 *
 * <p>Os números são todos do original.
 */
public final class AffinityEffects {
    private AffinityEffects() {
    }

    // ------------------------------------------------------------------ os modificadores de atributo

    /** <b>Raízes da Natureza</b>: dez por cento mais devagar, a partir de meia Natureza. */
    private static final ResourceKey<Attribute> SPEED_KEY = Attributes.MOVEMENT_SPEED.unwrapKey().orElseThrow();
    private static final ResourceKey<Attribute> HEALTH_KEY = Attributes.MAX_HEALTH.unwrapKey().orElseThrow();

    private static final AttributeModifier NATURE_ROOTS = new AttributeModifier(
            Thaumcraft.id("affinity_nature_roots"), -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    /** <b>Sangue Frio</b>: quem é de Gelo anda devagar <i>fora</i> do gelo, a partir de um décimo. */
    private static final AttributeModifier ICE_COLD_BLOODED = new AttributeModifier(
            Thaumcraft.id("affinity_cold_blooded"), -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    /** <b>Reflexos de Relâmpago</b>: mais que o dobro da velocidade, a partir de 0,65. */
    private static final AttributeModifier LIGHTNING_REFLEXES = new AttributeModifier(
            Thaumcraft.id("affinity_lightning_reflexes"), 1.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    /** <b>Passada de Relâmpago</b>: sobe um bloco inteiro sem pular, a partir de meia. */
    private static final ResourceKey<Attribute> STEP_KEY = Attributes.STEP_HEIGHT.unwrapKey().orElseThrow();

    private static final AttributeModifier LIGHTNING_STEP = new AttributeModifier(
            Thaumcraft.id("affinity_lightning_step"), 0.414, AttributeModifier.Operation.ADD_VALUE);

    /** <b>Fraqueza à água</b>: um quarto da vida, para Fogo, Ender e Relâmpago, quando molhado. */
    private static final AttributeModifier WATER_WEAKNESS = new AttributeModifier(
            Thaumcraft.id("affinity_water_weakness"), -0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    /** <b>Fraqueza ao fogo</b>: um quarto, para quem é de Água, quando arde ou está no Nether. */
    private static final AttributeModifier FIRE_WEAKNESS = new AttributeModifier(
            Thaumcraft.id("affinity_fire_weakness"), -0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    /** <b>Fraqueza ao sol</b>: um quarto, para quem é de Ender, sob o céu aberto de dia. */
    private static final AttributeModifier SUNLIGHT_WEAKNESS = new AttributeModifier(
            Thaumcraft.id("affinity_sunlight_weakness"), -0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    private static void toggle(Player quem, ResourceKey<Attribute> qual, AttributeModifier mod, boolean liga) {
        Holder<Attribute> atributo = net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE
                .get(qual).orElseThrow();
        AttributeInstance conta = quem.getAttribute(atributo);
        if (conta == null) return;
        boolean tem = conta.getModifier(mod.id()) != null;
        if (liga && !tem) {
            conta.addTransientModifier(mod);
        } else if (!liga && tem) {
            conta.removeModifier(mod.id());
        }
    }

    // ------------------------------------------------------------------ a batida

    /**
     * Uma batida de quem tem Afinidade: o {@code onEntityLivingBase} do original, na parte do jogador.
     *
     * <p>Corre <b>a cada batida</b> e não de 20 em 20, porque quase tudo aqui é sobre o que está acontecendo
     * agora: se a pessoa está molhada, se está ao sol, se está agachada.
     */
    public static void tick(ServerPlayer quem) {
        AffinityData conta = AffinityData.of(quem);
        ServerLevel level = quem.level();

        float natureza = conta.depth(Affinity.NATURE);
        float gelo = conta.depth(Affinity.ICE);
        float relâmpago = conta.depth(Affinity.LIGHTNING);
        float água = conta.depth(Affinity.WATER);
        float fogo = conta.depth(Affinity.FIRE);
        float vida = conta.depth(Affinity.LIFE);
        float ender = conta.depth(Affinity.ENDER);
        float terra = conta.depth(Affinity.EARTH);
        float ar = conta.depth(Affinity.AIR);

        velocidade(quem, natureza, gelo, relâmpago);
        saúde(quem, level, ender, água, fogo, relâmpago);

        // o Relâmpago dá passada de bloco inteiro, a partir de meia
        toggle(quem, STEP_KEY, LIGHTNING_STEP, relâmpago >= 0.5f);

        vidaQueVolta(quem, vida);
        if (natureza >= 1.0f) naturezaCheia(quem, level);
        if (ender >= 0.75f) visãoNoturna(quem);
        if (quem.isInWater()) naÁgua(quem, terra, água);
        if (level.isRaining() && ar > 0.5f && ar < 0.85f) sopro(quem, level);
        if (quem.isShiftKeyDown() && gelo >= 0.5f) ponteDeGelo(quem, level, gelo);
        relâmpagoPassivo(quem, level, relâmpago);
    }

    private static void velocidade(Player quem, float natureza, float gelo, float relâmpago) {
        toggle(quem, SPEED_KEY, NATURE_ROOTS, natureza >= 0.5f);
        toggle(quem, SPEED_KEY, LIGHTNING_REFLEXES, relâmpago >= 0.65f);
        toggle(quem, SPEED_KEY, ICE_COLD_BLOODED, gelo >= 0.1f && !noGelo(quem));
    }

    /** Se há gelo debaixo dos pés: o {@code isOnIce} do original. */
    private static boolean noGelo(Player quem) {
        var caixa = quem.getBoundingBox().inflate(0.001).expandTowards(0.0, -1.4, 0.0);
        for (BlockPos onde : BlockPos.betweenClosed(
                BlockPos.containing(caixa.minX, caixa.minY, caixa.minZ),
                BlockPos.containing(caixa.maxX, caixa.maxY, caixa.maxZ))) {
            if (quem.level().getBlockState(onde).is(Blocks.ICE)) return true;
        }
        return false;
    }

    private static void saúde(Player quem, ServerLevel level, float ender, float água, float fogo,
                              float relâmpago) {
        boolean molhado = quem.isInWaterOrRain();
        toggle(quem, HEALTH_KEY, WATER_WEAKNESS, molhado
                && (entre(fogo) || entre(ender) || entre(relâmpago)));

        boolean queimando = quem.isOnFire() || level.dimension() == Level.NETHER;
        toggle(quem, HEALTH_KEY, FIRE_WEAKNESS, entre(água) && queimando);

        long hora = level.getOverworldClockTime() % 24000L;
        boolean dia = hora > 23000L || hora < 12500L;
        toggle(quem, HEALTH_KEY, SUNLIGHT_WEAKNESS,
                ender > 0.65f && ender <= 0.95f && level.canSeeSky(quem.blockPosition()) && dia);
    }

    /** O intervalo em que a fraqueza vale: de meia a nove décimos, e não acima. */
    private static boolean entre(float quanto) {
        return quanto >= 0.5f && quanto <= 0.9f;
    }

    /** A vida que volta sozinha para quem é de Vida: {@code 0,025 × profundidade} por batida. */
    private static void vidaQueVolta(ServerPlayer quem, float vida) {
        if (vida <= 0.0f || quem.getHealth() >= quem.getMaxHealth()) return;
        AffinityPools.LIFE.accumulate(quem, 0.025f * vida, () -> quem.heal(1.0f));
    }

    /**
     * Quem é de Natureza por inteiro come do sol — e passa fome na sombra.
     *
     * <p>E sobe parede: o original empurra a pessoa para cima quando ela está encostada numa, a não ser que
     * esteja agachada. É a única forma de escalada que o ramo tem.
     */
    private static void naturezaCheia(ServerPlayer quem, ServerLevel level) {
        if (level.canSeeSky(quem.blockPosition()) && level.isBrightOutside()) {
            AffinityPools.HUNGER.accumulate(quem, 0.02f, () -> quem.getFoodData().eat(1, 0.025f));
        } else {
            quem.causeFoodExhaustion(0.025f);
        }

        if (quem.horizontalCollision) {
            if (!quem.isShiftKeyDown()) {
                quem.setDeltaMovement(quem.getDeltaMovement().x, 0.25, quem.getDeltaMovement().z);
            } else {
                quem.setDeltaMovement(quem.getDeltaMovement().multiply(1.0, 0.8, 1.0));
            }
            quem.fallDistance = 0.0f;
        }
    }

    private static void visãoNoturna(ServerPlayer quem) {
        var tem = quem.getEffect(MobEffects.NIGHT_VISION);
        if (tem == null || tem.getDuration() <= 220) {
            quem.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 1, true, false));
        }
    }

    private static void naÁgua(ServerPlayer quem, float terra, float água) {
        // quem é de Terra afunda
        if (terra > 0.25f && quem.getDeltaMovement().y > -0.3) {
            quem.setDeltaMovement(quem.getDeltaMovement().add(0.0, -0.01f * terra, 0.0));
        }
        // e quem é de Água nada depressa e não perde o fôlego
        if (água > 0.5f) {
            var tem = quem.getEffect(MobEffects.DOLPHINS_GRACE);
            if (tem == null || tem.getDuration() < 10) {
                quem.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 100,
                        água > 0.75f ? 2 : 1, true, false));
            }
        }
        if (água > 0.4f && quem.getRandom().nextInt(20) < 4) {
            quem.setAirSupply(Math.min(quem.getMaxAirSupply(), quem.getAirSupply() + 1));
        }
    }

    /** O vento leva quem é de Ar quando chove: um empurrão ao acaso, uma vez em dez. */
    private static void sopro(ServerPlayer quem, ServerLevel level) {
        if (quem.isShiftKeyDown() || quem.isInWater() || !quem.isInWaterOrRain()) return;
        if (level.getRandom().nextInt(100) >= 10) return;
        var sorte = level.getRandom();
        quem.push(sorte.nextDouble() - 0.5, sorte.nextDouble() - 0.5, sorte.nextDouble() - 0.5);
        quem.hurtMarked = true;
    }

    /**
     * A <b>ponte de gelo</b>: quem é de Gelo e se agacha congela a água à frente dos pés.
     *
     * <p>Por inteiro, ela também endurece a lava — em obsidiana a fonte e em pedregulho a corrente —, e apaga
     * o fogo que estiver por cima. É o efeito mais vistoso da roda.
     */
    private static void ponteDeGelo(ServerPlayer quem, ServerLevel level, float gelo) {
        BlockPos pé = quem.blockPosition().below();
        Vec3 olho = quem.getLookAngle();
        BlockPos frente = pé.offset((int) Math.round(olho.x), 0, (int) Math.round(olho.z));

        for (BlockPos onde : BlockPos.betweenClosed(frente.offset(-1, 0, -1), frente.offset(1, 0, 1))) {
            BlockState estado = level.getBlockState(onde);
            if (gelo >= 1.0f && estado.is(Blocks.LAVA)) {
                boolean fonte = estado.getFluidState().isSource();
                level.setBlockAndUpdate(onde,
                        (fonte ? Blocks.OBSIDIAN : Blocks.COBBLESTONE).defaultBlockState());
            } else if (estado.is(Blocks.WATER)) {
                level.setBlockAndUpdate(onde, Blocks.ICE.defaultBlockState());
            }
            BlockPos acima = onde.above();
            if (level.getBlockState(acima).is(Blocks.FIRE)) {
                level.setBlockAndUpdate(acima, Blocks.AIR.defaultBlockState());
            }
        }
    }

    /**
     * O que o Relâmpago faz sozinho: o {@code applyFulmintion}.
     *
     * <p>De 0,5 a 0,8, dinamite por perto <b>acende sozinha</b>. De 0,7 a 0,95, creepers por perto <b>ficam carregados</b>. E acima de um quarto, estar molhado
     * <b>queima mana</b> — 100 por batida —, porque um corpo cheio de raio não gosta de água.
     */
    private static void relâmpagoPassivo(ServerPlayer quem, ServerLevel level, float relâmpago) {
        var sorte = quem.getRandom();

        if (relâmpago > 0.5f && relâmpago <= 0.8f) {
            BlockPos onde = quem.blockPosition().offset(
                    sorte.nextInt(11) - 5, sorte.nextInt(11) - 5, sorte.nextInt(11) - 5);
            if (level.getBlockState(onde).is(Blocks.TNT)) {
                level.removeBlock(onde, false);
                var bomba = new net.minecraft.world.entity.item.PrimedTnt(level,
                        onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5, quem);
                bomba.setFuse(1);
                level.addFreshEntity(bomba);
            }
        }

        if (relâmpago >= 0.7f && relâmpago <= 0.95f && sorte.nextDouble() < 0.05) {
            for (var creeper : level.getEntitiesOfClass(net.minecraft.world.entity.monster.Creeper.class,
                    quem.getBoundingBox().inflate(5.0))) {
                if (!creeper.isPowered()) creeper.thunderHit(level, lightning(level, creeper));
            }
        }

        if (relâmpago > 0.25f && quem.isInWaterOrRain()) {
            Mana conta = Mana.of(quem);
            if (conta.mana() > 0.0f) Mana.set(quem, conta.withMana(conta.mana() - 100.0f));
        }
    }

    /** Um raio que só serve para carregar o creeper, e que não bate em nada. */
    private static net.minecraft.world.entity.LightningBolt lightning(ServerLevel level, Entity onde) {
        var raio = EntityTypes.LIGHTNING_BOLT.create(level, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
        if (raio != null) {
            raio.snapTo(onde.getX(), onde.getY(), onde.getZ());
            raio.setVisualOnly(true);
        }
        return raio;
    }

    // ------------------------------------------------------------------ pular e cair

    /** Quem é de Ar pula mais alto: {@code profundidade × 0,35} a mais, a partir de meia. */
    public static void jump(LivingEntity quem) {
        if (!(quem instanceof Player gente)) return;
        float ar = AffinityData.of(gente).depth(Affinity.AIR);
        if (ar < 0.5f) return;
        quem.setDeltaMovement(quem.getDeltaMovement().add(0.0, ar * 0.35f, 0.0));
    }

    /**
     * E cai diferente: quem é de Terra cai <b>mais pesado</b>, quem é de Ar cai <b>mais leve</b>.
     *
     * @return a distância de queda que o jogo deve contar
     */
    public static float fall(LivingEntity quem, float distância) {
        if (!(quem instanceof Player gente)) return distância;
        AffinityData conta = AffinityData.of(gente);

        float terra = conta.depth(Affinity.EARTH);
        if (terra > 0.25f) distância += (float) (1.25 * terra);

        float ar = conta.depth(Affinity.AIR);
        if (ar >= 0.5f) distância = Math.max(0.0f, distância - 2.0f * ar);

        return distância;
    }

    // ------------------------------------------------------------------ bater e levar

    /**
     * O dano que chega e o que sai: o {@code onEntityHurt} do original.
     *
     * <p>É aqui que quase toda a roda se sente. Quem é de Fogo e bate <b>de mão vazia</b> põe fogo em quem
     * levou e soma três; quem é de Relâmpago chama um raio. Quem é de Terra leva menos, quem é de Arcano leva
     * <b>mais</b>. Quem é de Natureza devolve espinhos e quem é de Gelo devolve lentidão.
     *
     * @return o dano depois de a Afinidade mexer nele
     */
    public static float hurt(LivingEntity levou, DamageSource fonte, float dano) {
        if (!(levou.level() instanceof ServerLevel level)) return dano;

        // o que quem bateu faz, se bateu de mão vazia
        if (fonte.getEntity() instanceof Player bateu && bateu.getMainHandItem().isEmpty()) {
            AffinityData dele = AffinityData.of(bateu);

            if (dele.depth(Affinity.FIRE) > 0.8f) {
                levou.igniteForSeconds(4.0f);
                dano += 3.0f;
            }
            if (dele.depth(Affinity.LIGHTNING) > 0.75f) {
                var raio = EntityTypes.LIGHTNING_BOLT.create(level,
                        net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
                if (raio != null) {
                    raio.snapTo(levou.getX(), levou.getY(), levou.getZ());
                    level.addFreshEntity(raio);
                }
            }
        }

        if (!(levou instanceof Player gente)) return dano;
        AffinityData conta = AffinityData.of(gente);

        // a Terra amortece e o Arcano amplifica
        float terra = conta.depth(Affinity.EARTH);
        if (terra > 0.25f) dano -= dano * (0.1f * terra);

        if (conta.depth(Affinity.ARCANE) > 0.25f) dano *= 1.1f;

        // a Água afoga um enderman que encoste
        if (conta.depth(Affinity.WATER) > 0.9f
                && fonte.getEntity() instanceof net.minecraft.world.entity.monster.EnderMan bicho) {
            bicho.hurtServer(level, level.damageSources().drown(), 2.0f);
        }

        // o Fogo resiste ao fogo e o Ender à magia
        if (fonte.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
            dano *= 1.0f - 0.6f * conta.depth(Affinity.FIRE);
        }
        if (fonte.is(DamageTypes.MAGIC) || fonte.is(DamageTypes.INDIRECT_MAGIC)) {
            dano *= 1.0f - 0.75f * conta.depth(Affinity.ENDER);
        }

        // e a Natureza e o Gelo devolvem
        if (fonte.getEntity() instanceof LivingEntity quemBateu) {
            float natureza = conta.depth(Affinity.NATURE);
            float espinhos = natureza >= 1.0f ? 3.0f : natureza >= 0.75f ? 2.0f : natureza >= 0.5f ? 1.0f : 0.0f;
            if (espinhos > 0.0f) {
                quemBateu.hurtServer(level, level.damageSources().cactus(), espinhos);
            }

            float gelo = conta.depth(Affinity.ICE);
            int grau = gelo >= 1.0f ? 2 : gelo >= 0.75f ? 1 : gelo >= 0.5f ? 0 : -1;
            int tempo = gelo >= 1.0f ? 200 : gelo >= 0.75f ? 160 : 100;
            if (grau >= 0) {
                quemBateu.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, tempo, grau));
            }
        }

        return Math.max(0.0f, dano);
    }

    /**
     * O preço de matar, para quem é de Vida: o {@code onEntityDeath}.
     *
     * <p>Quem passou de seis décimos de Vida e mata <b>algo que estava vivo</b> fica cego, faminto, lento para
     * cavar e fraco para bater. Não é bênção nenhuma: é a Afinidade da Vida dizendo que matar cobra caro.
     * Mortos-vivos não contam, porque já estavam mortos.
     */
    public static void killed(LivingEntity morreu, DamageSource fonte) {
        if (morreu.getType().builtInRegistryHolder().is(net.minecraft.tags.EntityTypeTags.UNDEAD)) return;
        if (!(fonte.getEntity() instanceof Player quem)) return;
        if (AffinityData.of(quem).depth(Affinity.LIFE) < 0.6f) return;

        quem.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 1));
        quem.addEffect(new MobEffectInstance(MobEffects.HUNGER, 40, 1));
        quem.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 100, 1));
        quem.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
    }

    /**
     * O desconto do Arcano no preço de um feitiço: o {@code onSpellManaCost}.
     *
     * <p>Cinco por cento a menos de mana <b>e</b> de desgaste, acima de meio Arcano. É pouco, e é de
     * propósito: quem é de Arcano paga com dez por cento a mais de dano levado.
     */
    public static float manaDiscount(LivingEntity quem) {
        if (!(quem instanceof Player gente)) return 1.0f;
        return AffinityData.of(gente).depth(Affinity.ARCANE) > 0.5f ? 0.95f : 1.0f;
    }

    /** Um enderman não encara quem chegou ao fim do Ender: o começo do {@code onEntityLivingBase}. */
    public static boolean endermanIgnores(LivingEntity bicho, LivingEntity alvo) {
        if (!(bicho instanceof net.minecraft.world.entity.monster.EnderMan)) return false;
        if (!(alvo instanceof Player quem)) return false;
        return AffinityData.of(quem).depth(Affinity.ENDER) >= 1.0f;
    }
}
