package net.thaumcraft.maleficium;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

/**
 * De que são feitas as ferramentas do Maleficium: o {@code TMMaterials} do Tainted Magic 8.1.1, com os números dele.
 *
 * <p>O metal das sombras é o exagero do mod — dois mil e quinhentos usos, corta como nenhum outro e aceita
 * encantamento melhor que o ouro. O osso oco quase não fere, e a lâmina primordial fere como nada mais.
 */
public final class MaleficiumMaterials {
    /** A etiqueta do lingote, que é com o que estas coisas se consertam. */
    public static final TagKey<Item> SHADOWMETAL_INGOT = TagKey.create(Registries.ITEM,
            Identifier.fromNamespaceAndPath("c", "ingots/shadowmetal"));

    /** O osso oco do punhal: duzentos usos, cava depressa e quase não fere. */
    public static final ToolMaterial HOLLOW = new ToolMaterial(
            BlockTags.INCORRECT_FOR_STONE_TOOL, 200, 4.0f, -0.5f, 5, SHADOWMETAL_INGOT);

    /** O metal das sombras: nível de diamante, 2500 usos, velocidade 17, seis de dano, trinta de encantabilidade. */
    public static final ToolMaterial SHADOW = new ToolMaterial(
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 2500, 17.0f, 6.0f, 30, SHADOWMETAL_INGOT);

    /** A lâmina primordial: quinhentos usos e dezesseis e três quartos de dano. */
    public static final ToolMaterial PRIMAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 500, 16.0f, 16.75f, 30, SHADOWMETAL_INGOT);

    private MaleficiumMaterials() {
    }
}
