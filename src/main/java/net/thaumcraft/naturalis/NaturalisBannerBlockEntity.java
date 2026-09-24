package net.thaumcraft.naturalis;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.entity.BannerBlockEntity;
import net.thaumcraft.block.entity.BannerSheeted;

/**
 * O Estandarte do Magia Naturalis: o {@code CustomBannerBlockEntity} do original, que é o estandarte do Thaumcraft
 * com a figura do ramo no pano.
 */
public class NaturalisBannerBlockEntity extends BannerBlockEntity implements BannerSheeted {
    private static final Identifier SHEET = Thaumcraft.id("textures/models/banner_naturalis.png");

    public NaturalisBannerBlockEntity(BlockPos pos, BlockState state) {
        super(NaturalisBlocks.BANNER_ENTITY, pos, state);
    }

    @Override
    public Identifier bannerSheet() {
        return SHEET;
    }
}
