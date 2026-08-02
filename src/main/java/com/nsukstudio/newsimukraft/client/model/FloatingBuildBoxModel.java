package com.nsukstudio.newsimukraft.client.model;

import com.nsukstudio.newsimukraft.client.renderer.state.FloatingBuildBoxRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** 悬浮建筑盒客户端模型 */
public class FloatingBuildBoxModel extends EntityModel<FloatingBuildBoxRenderState> {

    private static final String PART_BOX = "box";

    public FloatingBuildBoxModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild(
                PART_BOX,
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0f, -4.0f, -4.0f, 8, 8, 8),
                PartPose.offset(0.0f, 0.0f, 0.0f)
        );

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(FloatingBuildBoxRenderState state) {
        super.setupAnim(state);
    }
}
