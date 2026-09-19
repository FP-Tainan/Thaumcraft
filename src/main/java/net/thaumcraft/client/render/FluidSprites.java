package net.thaumcraft.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/** O ícone parado de um líquido (o {@code Fluid.getIcon()} de então) e a cor com que ele é pintado. */
public final class FluidSprites {
    private FluidSprites() {
    }

    public static TextureAtlasSprite still(Fluid fluid) {
        Identifier id = BuiltInRegistries.FLUID.getKey(fluid);
        Identifier sprite = id.withPath("block/" + id.getPath() + "_still");
        return Minecraft.getInstance().getAtlasManager().get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, sprite));
    }

    /** A água é cinza na textura e ganha o azul da tinta; o resto vem com a cor própria. */
    public static int tint(Fluid fluid) {
        return fluid.isSame(Fluids.WATER) ? 0xFF3F76E4 : 0xFFFFFFFF;
    }
}
