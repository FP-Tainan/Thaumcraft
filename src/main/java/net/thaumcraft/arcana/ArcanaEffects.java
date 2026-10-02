package net.thaumcraft.arcana;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;

/**
 * Os efeitos do Ars Arcana: o {@code BuffList} do Ars Magica 2.
 *
 * <p>São <b>vinte e quatro</b>, e são os que alguma Essência põe em alguém. O original tem mais cinco — a
 * Agilidade, a Clareza, a Regeneração de Mana, o Aumento de Mana e a Redução de Desgaste —, mas quem os dá são
 * as <b>máquinas</b> dele: o Obelisco, o Prisma Celeste, as garrafas. Essa metade do mod não é deste ramo, e
 * por isso esses cinco não entram: um efeito que ninguém pode ganhar é peso morto.
 *
 * <p><b>Onde eles moram é diferente do original, e vale dizer.</b> No Ars Magica 2 cada efeito é uma classe
 * `BuffEffect` com um `applyEffect` e um `stopEffect`, e quase todos são <b>marcos vazios</b>: o que eles fazem
 * de verdade está escrito no `AMEventHandler`, num punhado de métodos gigantes que olham para todos os efeitos
 * de uma vez. Aqui o efeito é o que o jogo de hoje chama de efeito, e o que ele faz está <b>junto dele</b>, nos
 * quatro lugares por onde um efeito pode mexer em alguém: a <b>batida</b>, o <b>dano</b>, a <b>queda</b> e o
 * <b>pulo</b>. Os números são todos do original.
 *
 * <p><b>O ícone do Embaralhar de Sinapses sai em branco</b>, e é assim no original: o `BuffList` manda buscá-lo
 * à linha 1, coluna 7 da segunda folha, e essa casa da folha está vazia. Quem joga o Ars Magica 2 vê um
 * quadrado vazio na barra de efeitos, e quem jogar este vê o mesmo.
 */
public final class ArcanaEffects {
    // ------------------------------------------------------------------ os que mexem no andar

    /**
     * <b>Pressa</b>: anda mais depressa, e quanto mais forte mais.
     *
     * <p>Os três degraus do original são <b>0,2</b>, <b>0,45</b> e <b>0,9</b> — e repare que não é uma escada
     * de passos iguais: o segundo vale mais do que dois primeiros, e o terceiro vale o dobro do segundo. É por
     * isso que ele não pode ser um modificador de atributo comum, que o jogo multiplicaria pelo grau.
     */
    public static final Holder<MobEffect> HASTE = register("haste",
            new Degraus("haste", MobEffectCategory.BENEFICIAL, 0x4DE8E8, new double[]{0.2, 0.45, 0.9}));

    /** <b>Gelado</b>: o contrário da Pressa, com os degraus do original — 0,2, 0,5 e 0,8 para trás. */
    public static final Holder<MobEffect> FROST_SLOW = register("frost_slow",
            new Degraus("frost_slow", MobEffectCategory.HARMFUL, 0x9BD7F0, new double[]{-0.2, -0.5, -0.8}));

    /**
     * Um efeito de <b>degraus</b>: o valor dele não é uma conta do grau, é uma tabela.
     *
     * <p>O jogo de hoje sabe pôr um modificador de atributo num efeito, mas o multiplica pelo grau. O original
     * não multiplica nada: ele escolhe um de três números escritos à mão. É isto.
     */
    private static class Degraus extends MobEffect {
        private final net.minecraft.resources.Identifier marca;
        private final double[] degraus;

        Degraus(String nome, MobEffectCategory categoria, int cor, double[] degraus) {
            super(categoria, cor);
            this.marca = Thaumcraft.id("aa_" + nome);
            this.degraus = degraus;
        }

        /**
         * Põe o modificador do degrau em que o efeito está.
         *
         * <p>É aqui e não no {@code createModifiers} porque o jogo <b>não passa por lá</b> ao aplicar um
         * efeito: ele percorre a lista de modificadores que o efeito declarou de uma vez por todas, e essa
         * lista não sabe o grau. Este é o gancho que sabe.
         */
        @Override
        public void addAttributeModifiers(net.minecraft.world.entity.ai.attributes.AttributeMap atributos,
                                          int grau) {
            var qual = atributos.getInstance(Attributes.MOVEMENT_SPEED);
            if (qual == null) return;
            qual.removeModifier(this.marca);
            qual.addTransientModifier(new AttributeModifier(this.marca,
                    this.degraus[Math.clamp(grau, 0, this.degraus.length - 1)],
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }

        @Override
        public void removeAttributeModifiers(net.minecraft.world.entity.ai.attributes.AttributeMap atributos) {
            var qual = atributos.getInstance(Attributes.MOVEMENT_SPEED);
            if (qual != null) qual.removeModifier(this.marca);
        }
    }

    /** <b>Salto</b>: pula mais alto e para onde olha, e não se machuca ao cair do que pulou. */
    public static final Holder<MobEffect> LEAP = register("leap",
            new MobEffect(MobEffectCategory.BENEFICIAL, 0x7FE87F) {
            });

    /** <b>Poço de Gravidade</b>: cai mais depressa, e a queda dói uma vez e meia. */
    public static final Holder<MobEffect> GRAVITY_WELL = register("gravity_well",
            new MobEffect(MobEffectCategory.HARMFUL, 0x6B4E8C) {
            });

    /** <b>Queda de Pena</b>: desce devagar, e chegar ao chão não dói. */
    public static final Holder<MobEffect> SLOWFALL = register("slowfall",
            new MobEffect(MobEffectCategory.BENEFICIAL, 0xE8E8C8) {
            });

    /** <b>Voo</b>: quem o tem voa, e é tudo o que ele faz. */
    public static final Holder<MobEffect> FLIGHT = register("flight",
            new MobEffect(MobEffectCategory.BENEFICIAL, 0xBFE8FF) {
            });

    /** <b>Levitação</b>: voa, mas devagar e mal — é o voo de quem não sabe voar. */
    public static final Holder<MobEffect> LEVITATION = register("levitation",
            new MobEffect(MobEffectCategory.BENEFICIAL, 0xD7BFFF) {
            });

    /** <b>Enredado</b>: não se mexe. Nem para cima, nem para o lado. */
    public static final Holder<MobEffect> ENTANGLED = register("entangled",
            new MobEffect(MobEffectCategory.HARMFUL, 0x4E8C3A) {
            });

    /** <b>Nado Rápido</b>: dentro d'água, cada empurrão vale mais. */
    public static final Holder<MobEffect> SWIFT_SWIM = register("swift_swim",
            new MobEffect(MobEffectCategory.BENEFICIAL, 0x3A6B8C) {
            });

    /** <b>Túmulo de Água</b>: dentro d'água, puxa para o fundo. */
    public static final Holder<MobEffect> WATERY_GRAVE = register("watery_grave",
            new MobEffect(MobEffectCategory.HARMFUL, 0x1E3A5C) {
            });

    /** <b>Encolhido</b>: fica pequeno, e quem é pequeno não se machuca ao cair. */
    public static final Holder<MobEffect> SHRINK = register("shrink",
            new MobEffect(MobEffectCategory.HARMFUL, 0xC8A0E8) {
            });

    // ------------------------------------------------------------------ os que mexem no corpo

    /**
     * <b>Regeneração</b>: cura um de vida de tantas em tantas batidas.
     *
     * <p>Oitenta batidas no primeiro grau, quarenta no segundo, vinte no terceiro — o original divide por dois
     * a cada grau, e é isso que a conta faz.
     */
    public static final Holder<MobEffect> REGENERATION = register("regeneration",
            new MobEffect(MobEffectCategory.BENEFICIAL, 0xE85C8C) {
                @Override
                public boolean applyEffectTick(ServerLevel level, LivingEntity quem, int grau) {
                    quem.heal(1.0f);
                    return true;
                }

                @Override
                public boolean shouldApplyEffectTickThisTick(int quanto, int grau) {
                    int de = 80 >> grau;
                    return de <= 0 || quanto % de == 0;
                }
            });

    /** <b>Respirar na Água</b>: o ar não desce enquanto se está dentro dela. */
    public static final Holder<MobEffect> WATER_BREATHING = register("water_breathing",
            new MobEffect(MobEffectCategory.BENEFICIAL, 0x3AA0C8) {
            });

    /** <b>Fúria</b>: cada golpe de quem a tem fere <b>quatro</b> a mais. */
    public static final Holder<MobEffect> FURY = register("fury",
            new MobEffect(MobEffectCategory.BENEFICIAL, 0xE83A3A) {
            });

    /** <b>Escudo Arcano</b>: tudo o que dói passa a doer <b>um quarto</b>. */
    public static final Holder<MobEffect> MAGIC_SHIELD = register("magic_shield",
            new MobEffect(MobEffectCategory.BENEFICIAL, 0xE8D74D) {
            });

    /**
     * <b>Escudo de Mana</b>: a mana leva a pancada em vez do corpo.
     *
     * <p><b>250 de mana por cada ponto de dano</b>, e se houver mana para tudo a pancada é aparada de vez — e
     * aí custa só <b>cem</b> por ponto. Os dois números são do original e a diferença entre eles é dele
     * também: ele bloqueia no primeiro gancho, mais barato, e absorve no segundo, mais caro.
     */
    public static final Holder<MobEffect> MANA_SHIELD = register("mana_shield",
            new MobEffect(MobEffectCategory.BENEFICIAL, 0x4D8CE8) {
            });

    /** <b>Refletir Feitiço</b>: o próximo projétil de feitiço volta para trás — e o efeito vai com ele. */
    public static final Holder<MobEffect> SPELL_REFLECT = register("spell_reflect",
            new MobEffect(MobEffectCategory.BENEFICIAL, 0xD7D7E8) {
            });

    // ------------------------------------------------------------------ os que mexem na cabeça

    /** <b>Silêncio</b>: não se lança feitiço nenhum. É o pior que se pode fazer a um arcanista. */
    public static final Holder<MobEffect> SILENCE = register("silence",
            new MobEffect(MobEffectCategory.HARMFUL, 0x5C5C6B) {
            });

    /** <b>Distorção Astral</b>: não se sai dali por magia — nem se chega. */
    public static final Holder<MobEffect> ASTRAL_DISTORTION = register("astral_distortion",
            new MobEffect(MobEffectCategory.HARMFUL, 0x8C3AE8) {
            });

    /** <b>Enfeitiçado</b>: quem o tem luta do lado de quem o lançou. */
    public static final Holder<MobEffect> CHARMED = register("charmed",
            new MobEffect(MobEffectCategory.HARMFUL, 0xE84DA0) {
            });

    /** <b>Visão Verdadeira</b>: se vê o que se esconde. */
    public static final Holder<MobEffect> TRUE_SIGHT = register("true_sight",
            new MobEffect(MobEffectCategory.BENEFICIAL, 0xFFE8A0) {
            });

    /** <b>Embaralhar Sinapses</b>: a mão vai para onde não se mandou. */
    public static final Holder<MobEffect> SCRAMBLE_SYNAPSES = register("scramble_synapses",
            new MobEffect(MobEffectCategory.HARMFUL, 0xE8E84D) {
            });

    /** <b>Âncora do Tempo</b>: guarda onde se estava e como se estava, e devolve tudo quando acaba. */
    public static final Holder<MobEffect> CHRONO_ANCHOR = register("chrono_anchor",
            new MobEffect(MobEffectCategory.BENEFICIAL, 0xA0E8E8) {
            });

    /** <b>Iluminado</b>: se anda com luz à volta. */
    public static final Holder<MobEffect> ILLUMINATION = register("illumination",
            new MobEffect(MobEffectCategory.BENEFICIAL, 0xFFFFC8) {
            });

    // ------------------------------------------------------------------ os quatro lugares por onde eles mexem

    /**
     * A batida de quem tem efeitos do ramo: o que o {@code onLivingUpdate} do original faz com eles.
     *
     * <p>Ela corre em <b>toda</b> criatura viva do mundo, a cada batida — e por isso a primeira coisa que faz
     * é sair quando não há nada a fazer. Sem essa saída seriam oito perguntas por bicho por batida, numa
     * coisa que num servidor cheio corre milhares de vezes por segundo.
     *
     * <p>As duas listas entram na conta de propósito: quem recebeu asas ou quem deixou uma luz pelo caminho
     * ainda tem de ser arrumado <b>depois</b> de o efeito acabar, e nessa batida ele já não tem efeito nenhum.
     */
    public static void tick(ServerLevel level, LivingEntity quem) {
        if (quem.getActiveEffects().isEmpty()
                && !COM_ASAS.contains(quem.getUUID())
                && !LUZES.containsKey(quem.getUUID())) {
            return;
        }

        voo(quem);

        if (quem.hasEffect(ENTANGLED)) {
            // o original zera as três, e não só a de subir
            quem.setDeltaMovement(Vec3.ZERO);
        }

        if (quem.hasEffect(WATERY_GRAVE) && quem.isInWater()) {
            int grau = grau(quem, WATERY_GRAVE) + 1;
            double fundo = -0.5 * grau;
            Vec3 anda = quem.getDeltaMovement();
            if (anda.y > fundo) quem.setDeltaMovement(anda.x, anda.y - 0.1, anda.z);
        }

        if (quem.hasEffect(SWIFT_SWIM) && quem.isInWater()) {
            double quanto = 1.133 + 0.03 * grau(quem, SWIFT_SWIM);
            Vec3 anda = quem.getDeltaMovement();
            quem.setDeltaMovement(anda.x * quanto, anda.y > 0.0 ? anda.y * 1.134 : anda.y, anda.z * quanto);
        }

        if (quem.hasEffect(WATER_BREATHING) && quem.isInWater()) {
            quem.setAirSupply(quem.getMaxAirSupply());
        }

        Vec3 anda = quem.getDeltaMovement();
        if (quem.hasEffect(GRAVITY_WELL) && anda.y < 0.0 && anda.y > -3.0) {
            quem.setDeltaMovement(anda.x, anda.y * 1.6, anda.z);
        } else if ((quem.hasEffect(SLOWFALL) || quem.hasEffect(SHRINK))
                && !quem.onGround() && anda.y < 0.0) {
            quem.setDeltaMovement(anda.x, anda.y * 0.8, anda.z);
        }

        if (quem.hasEffect(ILLUMINATION)) ilumina(level, quem);
        else if (LUZES.containsKey(quem.getUUID())) apaga(level, quem);
    }

    /**
     * Quem voa e quem deixa de voar: o pedaço do {@code onLivingUpdate} que mexe nas asas.
     *
     * <p>O original guarda um {@code hadFlight} em quem voa, e é por uma razão que se percebe mal e custa
     * caro: <b>não se tira o voo a quem não foi este feitiço que o deu</b>. Sem essa marca, um par de botas
     * de outro mod, ou o criativo, ou qualquer outra coisa que dê asas, perderia as asas na batida seguinte.
     * Aqui a marca é esta lista.
     */
    private static final java.util.Set<java.util.UUID> COM_ASAS =
            java.util.concurrent.ConcurrentHashMap.newKeySet();

    private static void voo(LivingEntity quem) {
        if (!(quem instanceof Player gente)) return;
        boolean voa = quem.hasEffect(FLIGHT) || quem.hasEffect(LEVITATION);

        if (voa) {
            COM_ASAS.add(gente.getUUID());
            if (!gente.getAbilities().mayfly) {
                gente.getAbilities().mayfly = true;
                gente.onUpdateAbilities();
            }
            // a Levitação é um voo manso: quem a tem quase não anda, e quase não sobe
            if (quem.hasEffect(LEVITATION) && gente.getAbilities().flying) {
                Vec3 anda = quem.getDeltaMovement();
                quem.setDeltaMovement(anda.x * 0.4, anda.y * 1.0e-4, anda.z * 0.4);
            }
        } else if (COM_ASAS.remove(gente.getUUID())
                && !gente.isCreative() && !gente.isSpectator()) {
            gente.getAbilities().mayfly = false;
            gente.getAbilities().flying = false;
            gente.fallDistance = 0.0f;
            gente.onUpdateAbilities();
        }
    }

    /**
     * A luz que o Iluminado deixa: de dez em dez batidas, uma luz onde a cabeça dele está.
     *
     * <p>O original tem para isto um bloco próprio, o {@code invisibleUtility}, que se apaga sozinho cinco
     * batidas depois. Aqui se usa o <b>bloco de luz</b> do jogo, que faz o mesmo serviço e já existe — e quem
     * apaga o anterior é esta conta, que guarda onde o pôs.
     */
    private static final java.util.Map<java.util.UUID, net.minecraft.core.BlockPos> LUZES =
            new java.util.concurrent.ConcurrentHashMap<>();

    private static void ilumina(ServerLevel level, LivingEntity quem) {
        if (quem.tickCount % 10 != 0) return;
        var onde = net.minecraft.core.BlockPos.containing(quem.getX(), quem.getEyeY(), quem.getZ());
        var antes = LUZES.get(quem.getUUID());
        if (onde.equals(antes)) return;
        apaga(level, quem);
        if (!level.getBlockState(onde).isAir()) return;
        level.setBlock(onde, net.minecraft.world.level.block.Blocks.LIGHT.defaultBlockState(), 2);
        LUZES.put(quem.getUUID(), onde);
    }

    /**
     * Esquece esta pessoa: apaga a luz dela e a tira das listas.
     *
     * <p>É chamado quando ela sai do mundo — morreu, foi embora, foi desfeita. Sem isto, quem morresse
     * iluminado deixaria <b>um bloco de luz invisível aceso para sempre</b>, e ninguém saberia que ele estava
     * lá nem como o tirar. O bloco do original se apaga sozinho cinco batidas depois e nunca tem este
     * problema; o do jogo de hoje não, e por isso há esta conta.
     */
    public static void esquece(ServerLevel level, LivingEntity quem) {
        apaga(level, quem);
        COM_ASAS.remove(quem.getUUID());
    }

    /** E apaga a que ficou para trás, quando ela já não serve. */
    private static void apaga(ServerLevel level, LivingEntity quem) {
        var antes = LUZES.remove(quem.getUUID());
        if (antes == null) return;
        if (level.getBlockState(antes).is(net.minecraft.world.level.block.Blocks.LIGHT)) {
            level.removeBlock(antes, false);
        }
    }

    /**
     * O dano que passa pelos escudos: o {@code onEntityHurt} e o {@code onEntityAttacked} do original.
     *
     * @return quanto dano fica depois de eles o comerem
     */
    public static float hurt(LivingEntity quem, DamageSource fonte, float dano) {
        if (quem.hasEffect(MAGIC_SHIELD)) dano *= 0.25f;

        if (quem.hasEffect(MANA_SHIELD) && quem instanceof Player gente) {
            Mana conta = Mana.of(gente);
            if (conta.mana() >= dano * 250.0f) {
                // bloqueia tudo, e o original cobra cem por ponto neste caminho
                Mana.set(gente, conta.withMana(conta.mana() - dano * 100.0f));
                return 0.0f;
            }
            float come = Math.min(conta.mana(), dano * 250.0f);
            Mana.set(gente, conta.withMana(conta.mana() - come));
            dano -= come / 250.0f;
        }
        return dano;
    }

    /**
     * A queda: o {@code onEntityFall}.
     *
     * <p>A Queda de Pena e o Encolhido <b>apagam</b> a queda inteira; o Poço de Gravidade faz doer <b>uma vez
     * e meia</b>; e o Salto guarda uma almofada — oito, vinte ou 45 blocos, pelo grau — que é o que faz pular
     * alto e não morrer disso.
     */
    public static float fall(LivingEntity quem, float distância) {
        if (quem.hasEffect(SLOWFALL) || quem.hasEffect(SHRINK)) return 0.0f;
        if (quem.hasEffect(GRAVITY_WELL)) distância *= 1.5f;
        if (quem.hasEffect(LEAP)) {
            distância -= switch (grau(quem, LEAP)) {
                case 0 -> 8.0f;
                case 1 -> 20.0f;
                default -> 45.0f;
            };
        }
        return Math.max(0.0f, distância);
    }

    /** E o pulo: o {@code onEntityJump}, com os três degraus do Salto. */
    public static void jump(LivingEntity quem) {
        if (!quem.hasEffect(LEAP)) return;
        Vec3 anda = quem.getDeltaMovement();
        Vec3 rumo = quem.getLookAngle().normalize();
        double sobe = switch (grau(quem, LEAP)) {
            case 0 -> 0.4;
            case 1 -> 0.7;
            default -> 1.0;
        };
        double vezes = switch (grau(quem, LEAP)) {
            case 0 -> 1.08;
            case 1 -> 1.25;
            default -> 1.5;
        };
        quem.setDeltaMovement(anda.add(
                anda.x * vezes * Math.abs(rumo.x), sobe, anda.z * vezes * Math.abs(rumo.z)));
    }

    // ------------------------------------------------------------------ o que os outros perguntam

    /** Se esta pessoa está calada: quem tem Silêncio não lança nada. */
    public static boolean silenced(LivingEntity quem) {
        return quem.hasEffect(SILENCE);
    }

    /** Se ela está presa onde está: a Distorção Astral não deixa ninguém sair dali por magia. */
    public static boolean blocksTeleport(LivingEntity quem) {
        return quem.hasEffect(ASTRAL_DISTORTION);
    }

    /** O que a Fúria soma a cada golpe: os quatro do original. */
    public static float extraDamage(LivingEntity quem) {
        return quem.hasEffect(FURY) ? 4.0f : 0.0f;
    }

    /** O grau em que um efeito está naquela pessoa, ou zero. */
    public static int grau(LivingEntity quem, Holder<MobEffect> qual) {
        var tem = quem.getEffect(qual);
        return tem == null ? 0 : tem.getAmplifier();
    }

    private ArcanaEffects() {
    }

    private static Holder<MobEffect> register(String nome, MobEffect efeito) {
        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Thaumcraft.id("aa_" + nome), efeito);
    }

    /** Sem uso fora do porte: obriga a classe a carregar, e com ela os efeitos a se registrarem. */
    public static void init() {
    }
}
