package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;

import java.util.ArrayList;
import java.util.List;

/**
 * A tabela de aspectos tem de continuar sendo a do Thaumcraft 4.2.3.5. Estes testes são a cerca: se alguém
 * mexer numa cor, num par de composição ou esquecer um símbolo, o build para aqui.
 */
public class AspectGameTest {
    /**
     * Quarenta e oito aspectos do próprio Thaumcraft, seis deles primários — os números do mod original. A conta é
     * dos que a classe {@link Aspects} declara: a tabela inteira pode ter mais, porque os ramos de fora somam os
     * deles a ela, como os addons do original faziam.
     */
    @GameTest
    public void tableMatchesTheOriginal(GameTestHelper helper) {
        List<Aspect> doMod = new ArrayList<>();
        for (java.lang.reflect.Field campo : Aspects.class.getDeclaredFields()) {
            if (campo.getType() != Aspect.class) continue;
            try {
                doMod.add((Aspect) campo.get(null));
            } catch (IllegalAccessException erro) {
                helper.fail("não consegui ler o campo " + campo.getName());
            }
        }
        if (doMod.size() != 48) helper.fail("o mod declara " + doMod.size() + " aspectos; o original tem 48");
        long primal = doMod.stream().filter(Aspect::isPrimal).count();
        if (primal != 6) helper.fail("primários: " + primal + ", esperado 6");
        // e todos eles estão na tabela de verdade
        for (Aspect aspecto : doMod) {
            if (Aspect.ASPECTS.get(aspecto.tag()) != aspecto) helper.fail(aspecto.tag() + " sumiu da tabela");
        }
        helper.succeed();
    }

    /** Uma amostra conferida na mão contra a fonte da 4.2.3.5. */
    @GameTest
    public void spotChecksAgainstTheOriginal(GameTestHelper helper) {
        check(helper, "aer", 0xFFFF7E, null, null);
        check(helper, "perditio", 0x404040, null, null);
        check(helper, "vacuos", 0x888888, "aer", "perditio");
        check(helper, "praecantatio", 0x9700C0, "vacuos", "potentia");
        check(helper, "instrumentum", 0x4040EE, "humanus", "ordo");
        check(helper, "machina", 0x8080A0, "motus", "instrumentum");
        helper.succeed();
    }

    /** Todo aspecto tem nome escrito e símbolo desenhado. */
    @GameTest
    public void everyAspectIsNamedAndDrawn(GameTestHelper helper) {
        List<String> missing = new ArrayList<>();
        for (Aspect aspect : Aspects.all()) {
            String key = "tc.aspect." + aspect.tag();
            if (aspect.name().getString().equals(key)) missing.add("sem nome: " + aspect.tag());
            if (aspect.image() == null) missing.add("sem símbolo: " + aspect.tag());
        }
        if (!missing.isEmpty()) helper.fail(String.join(", ", missing));
        helper.succeed();
    }

    /** A conta de guardar e tirar aspectos, que é a base de tudo o que vem depois. */
    @GameTest
    public void listAddsAndTakes(GameTestHelper helper) {
        AspectList list = new AspectList().add(Aspects.AIR, 5).add(Aspects.AIR, 3).add(Aspects.FIRE, 2);
        if (list.getAmount(Aspects.AIR) != 8) helper.fail("somar deu " + list.getAmount(Aspects.AIR) + ", esperado 8");
        if (list.visSize() != 10) helper.fail("o total deu " + list.visSize() + ", esperado 10");
        // guardar o maior, que é como o thaumômetro anota
        list.merge(Aspects.FIRE, 1);
        if (list.getAmount(Aspects.FIRE) != 2) helper.fail("merge baixou o valor");
        list.merge(Aspects.FIRE, 7);
        if (list.getAmount(Aspects.FIRE) != 7) helper.fail("merge não subiu o valor");
        if (list.reduce(Aspects.FIRE, 99)) helper.fail("tirou mais do que havia");
        if (!list.reduce(Aspects.AIR, 8) || list.getAmount(Aspects.AIR) != 0) helper.fail("tirar não zerou");
        helper.succeed();
    }

    private static void check(GameTestHelper helper, String tag, int color, String first, String second) {
        Aspect aspect = Aspect.of(tag);
        if (aspect == null) {
            helper.fail("falta o aspecto " + tag);
            return;
        }
        if (aspect.color() != color) {
            helper.fail(tag + " está na cor " + Integer.toHexString(aspect.color())
                    + ", o original é " + Integer.toHexString(color));
        }
        if (first == null) {
            if (!aspect.isPrimal()) helper.fail(tag + " devia ser primário");
            return;
        }
        Aspect[] parts = aspect.components();
        if (parts == null || !parts[0].tag().equals(first) || !parts[1].tag().equals(second)) {
            helper.fail(tag + " não nasce de " + first + " com " + second);
        }
    }
}
