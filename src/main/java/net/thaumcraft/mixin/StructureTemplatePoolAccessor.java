package net.thaumcraft.mixin;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * As duas listas de uma piscina de encaixe, para se lhes poder <b>somar</b> peças.
 *
 * <p>O Witchery põe prédios seus dentro da aldeia do próprio jogo. No jogo de 2014 isso era uma chamada —
 * {@code registerVillageCreationHandler} —; hoje a aldeia é um salto-de-encaixe e as peças dela vivem em
 * <b>piscinas</b> carregadas do disco, que não têm por onde se acrescentar.
 *
 * <p>A alternativa seria <b>copiar o arquivo da piscina do jogo inteiro</b> e juntar-lhe as nossas peças no fim:
 * dez mil bytes de dados do jogo duplicados por variante, que ficam velhos no dia em que a Mojang mexer numa
 * casa. Somar à lista carregada é menor e não copia nada.
 *
 * <p><b>São duas listas, e as duas têm de mudar.</b> A {@code templates} é a lista já esticada pelo peso, de
 * onde se sorteia; a {@code rawTemplates} é a de pares peça-peso, que é a que o {@code getMaxSize} lê para
 * saber de quanto espaço a aldeia precisa. Mexer só na primeira faz a peça nascer e ficar cortada.
 */
@Mixin(StructureTemplatePool.class)
public interface StructureTemplatePoolAccessor {
    /** A lista esticada pelo peso, de onde o sorteio tira. */
    @Accessor("templates")
    ObjectArrayList<StructurePoolElement> thaumcraft$templates();

    /** E a de pares peça-peso, que é a que diz o tamanho. */
    @Accessor("rawTemplates")
    List<Pair<StructurePoolElement, Integer>> thaumcraft$rawTemplates();

    @Accessor("rawTemplates")
    @Mutable
    void thaumcraft$setRawTemplates(List<Pair<StructurePoolElement, Integer>> lista);
}
