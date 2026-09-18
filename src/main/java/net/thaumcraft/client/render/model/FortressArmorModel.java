package net.thaumcraft.client.render.model;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.HumanoidModel;

/**
 * As peças da armadura de fortaleza de táumio, tiradas do {@code ModelFortressArmor} da 4.2.3.5.
 *
 * <p><strong>Este arquivo é gerado</strong> pelo {@code scratchpad/fortaleza-modelo.js}, que lê o modelo
 * descompilado do jar: 66 caixas, cada uma presa à parte do corpo a que o original a prende. As partes do corpo
 * ficam sem caixa própria, porque o original apaga as do {@code ModelBiped}. Não mexer na mão.
 */
public final class FortressArmorModel {
    private FortressArmorModel() {
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();
        // as partes do corpo, nos lugares de sempre, sem caixa
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
        PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
        PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
        PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));
        head.addOrReplaceChild("ornamentl", CubeListBuilder.create().texOffs(78, 8).mirror().addBox(1.5F, -9.0F, -6.5F, 2.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396263F, 0.0F, 0.0F));
        head.addOrReplaceChild("ornamentl2", CubeListBuilder.create().texOffs(78, 8).mirror().addBox(3.5F, -10.0F, -6.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396263F, 0.0F, 0.0F));
        head.addOrReplaceChild("ornamentr", CubeListBuilder.create().texOffs(78, 8).addBox(-3.5F, -9.0F, -6.5F, 2.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396263F, 0.0F, 0.0F));
        head.addOrReplaceChild("ornamentr2", CubeListBuilder.create().texOffs(78, 8).addBox(-4.5F, -10.0F, -6.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396263F, 0.0F, 0.0F));
        head.addOrReplaceChild("helmet", CubeListBuilder.create().texOffs(41, 8).addBox(-4.5F, -9.0F, -4.5F, 9.0F, 4.0F, 9.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("helmetr", CubeListBuilder.create().texOffs(21, 13).addBox(-6.5F, -3.0F, -4.5F, 1.0F, 5.0F, 9.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5235988F));
        head.addOrReplaceChild("helmetl", CubeListBuilder.create().texOffs(21, 13).mirror().addBox(5.5F, -3.0F, -4.5F, 1.0F, 5.0F, 9.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.5235988F));
        head.addOrReplaceChild("helmetb", CubeListBuilder.create().texOffs(41, 21).addBox(-4.5F, -3.0F, 5.5F, 9.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.5235988F, 0.0F, 0.0F));
        head.addOrReplaceChild("capsthingy", CubeListBuilder.create().texOffs(21, 0).addBox(-4.5F, -6.0F, -6.5F, 9.0F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("flapr", CubeListBuilder.create().texOffs(59, 10).addBox(-10.0F, -2.0F, -1.0F, 3.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.5235988F, 0.5235988F));
        head.addOrReplaceChild("flapl", CubeListBuilder.create().texOffs(59, 10).mirror().addBox(7.0F, -2.0F, -1.0F, 3.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.5235988F, -0.5235988F));
        head.addOrReplaceChild("gemornament", CubeListBuilder.create().texOffs(68, 11).addBox(-1.5F, -9.0F, -7.0F, 3.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396263F, 0.0F, 0.0F));
        head.addOrReplaceChild("gem", CubeListBuilder.create().texOffs(72, 8).addBox(-1.0F, -8.5F, -7.5F, 2.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396263F, 0.0F, 0.0F));
        head.addOrReplaceChild("goggles", CubeListBuilder.create().texOffs(100, 18).addBox(-4.5F, -5.0F, -4.25F, 9.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("mask0", CubeListBuilder.create().texOffs(52, 2).addBox(-4.5F, -5.0F, -4.6F, 9.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("mask1", CubeListBuilder.create().texOffs(76, 2).addBox(-4.5F, -5.0F, -4.6F, 9.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("mask2", CubeListBuilder.create().texOffs(100, 2).addBox(-4.5F, -5.0F, -4.6F, 9.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("mbelt", CubeListBuilder.create().texOffs(56, 55).addBox(-4.0F, 8.0F, -3.0F, 8.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("mbeltl", CubeListBuilder.create().texOffs(76, 44).addBox(4.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("mbeltr", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("beltr", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("beltl", CubeListBuilder.create().texOffs(76, 44).addBox(4.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("chestplate", CubeListBuilder.create().texOffs(56, 45).addBox(-4.0F, 1.0F, -4.0F, 8.0F, 7.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("scroll", CubeListBuilder.create().texOffs(34, 27).addBox(-2.0F, 9.5F, 4.0F, 8.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1919862F));
        body.addOrReplaceChild("backplate", CubeListBuilder.create().texOffs(36, 45).addBox(-4.0F, 1.0F, 2.0F, 8.0F, 11.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("book", CubeListBuilder.create().texOffs(100, 8).addBox(1.0F, -0.3F, 4.0F, 5.0F, 7.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7679449F));
        rightArm.addOrReplaceChild("shoulderr", CubeListBuilder.create().texOffs(56, 35).addBox(-3.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        rightArm.addOrReplaceChild("gauntletr", CubeListBuilder.create().texOffs(100, 26).addBox(-3.5F, 3.5F, -2.5F, 2.0F, 6.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        rightArm.addOrReplaceChild("gauntletstrapr1", CubeListBuilder.create().texOffs(84, 31).addBox(-1.5F, 3.5F, -2.5F, 3.0F, 1.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        rightArm.addOrReplaceChild("gauntletstrapr2", CubeListBuilder.create().texOffs(84, 31).addBox(-1.5F, 6.5F, -2.5F, 3.0F, 1.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        rightArm.addOrReplaceChild("shoulderplatertop", CubeListBuilder.create().texOffs(110, 37).addBox(-5.5F, -2.5F, -3.5F, 2.0F, 1.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363323F));
        rightArm.addOrReplaceChild("shoulderplater1", CubeListBuilder.create().texOffs(110, 45).addBox(-4.5F, -1.5F, -3.5F, 1.0F, 4.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363323F));
        rightArm.addOrReplaceChild("shoulderplater2", CubeListBuilder.create().texOffs(94, 45).addBox(-3.5F, 1.5F, -3.5F, 1.0F, 3.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363323F));
        rightArm.addOrReplaceChild("shoulderplater3", CubeListBuilder.create().texOffs(94, 45).addBox(-2.5F, 3.5F, -3.5F, 1.0F, 3.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363323F));
        leftArm.addOrReplaceChild("shoulderl", CubeListBuilder.create().texOffs(56, 35).mirror().addBox(-1.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        leftArm.addOrReplaceChild("gauntletl", CubeListBuilder.create().texOffs(114, 26).addBox(1.5F, 3.5F, -2.5F, 2.0F, 6.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        leftArm.addOrReplaceChild("gauntletstrapl1", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 3.5F, -2.5F, 3.0F, 1.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        leftArm.addOrReplaceChild("gauntletstrapl2", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 6.5F, -2.5F, 3.0F, 1.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        leftArm.addOrReplaceChild("shoulderplateltop", CubeListBuilder.create().texOffs(110, 37).mirror().addBox(3.5F, -2.5F, -3.5F, 2.0F, 1.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));
        leftArm.addOrReplaceChild("shoulderplatel1", CubeListBuilder.create().texOffs(110, 45).mirror().addBox(3.5F, -1.5F, -3.5F, 1.0F, 4.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));
        leftArm.addOrReplaceChild("shoulderplatel2", CubeListBuilder.create().texOffs(94, 45).mirror().addBox(2.5F, 1.5F, -3.5F, 1.0F, 3.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));
        leftArm.addOrReplaceChild("shoulderplatel3", CubeListBuilder.create().texOffs(94, 45).mirror().addBox(1.5F, 3.5F, -3.5F, 1.0F, 3.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));
        rightLeg.addOrReplaceChild("legpanelr1", CubeListBuilder.create().texOffs(0, 51).addBox(-1.0F, 0.5F, -3.5F, 3.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("legpanelr2", CubeListBuilder.create().texOffs(8, 51).addBox(-1.0F, 3.5F, -2.5F, 3.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("legpanelr3", CubeListBuilder.create().texOffs(0, 56).addBox(-1.0F, 6.5F, -1.5F, 3.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("legpanelr4", CubeListBuilder.create().texOffs(0, 43).addBox(-3.0F, 0.5F, -3.5F, 2.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("legpanelr5", CubeListBuilder.create().texOffs(0, 47).addBox(-3.0F, 2.5F, -2.5F, 2.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("legpanelr6", CubeListBuilder.create().texOffs(6, 43).addBox(-3.0F, 4.5F, -1.5F, 2.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("sidepanelr1", CubeListBuilder.create().texOffs(0, 22).addBox(-2.5F, 0.5F, -2.5F, 1.0F, 4.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363323F));
        rightLeg.addOrReplaceChild("sidepanelr2", CubeListBuilder.create().texOffs(0, 31).addBox(-1.5F, 3.5F, -2.5F, 1.0F, 3.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363323F));
        rightLeg.addOrReplaceChild("sidepanelr3", CubeListBuilder.create().texOffs(12, 31).addBox(-0.5F, 5.5F, -2.5F, 1.0F, 3.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363323F));
        rightLeg.addOrReplaceChild("backpanelr1", CubeListBuilder.create().texOffs(0, 18).addBox(-3.0F, 0.5F, 2.5F, 5.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363323F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("backpanelr2", CubeListBuilder.create().texOffs(0, 18).addBox(-3.0F, 2.5F, 1.5F, 5.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363323F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("backpanelr3", CubeListBuilder.create().texOffs(0, 18).addBox(-3.0F, 4.5F, 0.5F, 5.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363323F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("backpanell3", CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-2.0F, 4.5F, 0.5F, 5.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363323F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("legpanell1", CubeListBuilder.create().texOffs(0, 51).mirror().addBox(-2.0F, 0.5F, -3.5F, 3.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("legpanell2", CubeListBuilder.create().texOffs(8, 51).mirror().addBox(-2.0F, 3.5F, -2.5F, 3.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("legpanell3", CubeListBuilder.create().texOffs(0, 56).mirror().addBox(-2.0F, 6.5F, -1.5F, 3.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("legpanell4", CubeListBuilder.create().texOffs(0, 43).mirror().addBox(1.0F, 0.5F, -3.5F, 2.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("legpanell5", CubeListBuilder.create().texOffs(0, 47).mirror().addBox(1.0F, 2.5F, -2.5F, 2.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("legpanell6", CubeListBuilder.create().texOffs(6, 43).mirror().addBox(1.0F, 4.5F, -1.5F, 2.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("sidepanell1", CubeListBuilder.create().texOffs(0, 22).mirror().addBox(1.5F, 0.5F, -2.5F, 1.0F, 4.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));
        leftLeg.addOrReplaceChild("sidepanell2", CubeListBuilder.create().texOffs(0, 31).mirror().addBox(0.5F, 3.5F, -2.5F, 1.0F, 3.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));
        leftLeg.addOrReplaceChild("sidepanell3", CubeListBuilder.create().texOffs(12, 31).mirror().addBox(-0.5F, 5.5F, -2.5F, 1.0F, 3.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));
        leftLeg.addOrReplaceChild("backpanell1", CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-2.0F, 0.5F, 2.5F, 5.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363323F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("backpanell2", CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-2.0F, 2.5F, 1.5F, 5.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363323F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }
}
