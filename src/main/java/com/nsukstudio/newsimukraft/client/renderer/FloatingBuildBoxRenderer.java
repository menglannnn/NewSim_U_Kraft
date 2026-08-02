package com.nsukstudio.newsimukraft.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nsukstudio.newsimukraft.NewSimukraft;
import com.nsukstudio.newsimukraft.client.model.FloatingBuildBoxModel;
import com.nsukstudio.newsimukraft.client.model.ModModelLayers;
import com.nsukstudio.newsimukraft.client.renderer.state.FloatingBuildBoxRenderState;
import com.nsukstudio.newsimukraft.entity.FloatingBuildBoxEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

/** 悬浮建筑盒渲染器 */
public class FloatingBuildBoxRenderer extends MobRenderer<FloatingBuildBoxEntity, FloatingBuildBoxRenderState, FloatingBuildBoxModel> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            NewSimukraft.MODID, "textures/entity/floating_build_box.png");

    public FloatingBuildBoxRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new FloatingBuildBoxModel(ctx.bakeLayer(ModModelLayers.FLOATING_BUILD_BOX)), 0.5f);
    }

    @Override
    public Identifier getTextureLocation(FloatingBuildBoxRenderState state) {
        return TEXTURE;
    }

    @Override
    public FloatingBuildBoxRenderState createRenderState() {
        return new FloatingBuildBoxRenderState();
    }

    @Override
    public void extractRenderState(FloatingBuildBoxEntity entity, FloatingBuildBoxRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.floatHeight = entity.getFloatHeight();
        state.floatSpeed = entity.getFloatSpeed();
    }

    @Override
    protected void setupRotations(FloatingBuildBoxRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);
        float floatOffset = (float) Math.sin(state.ageInTicks * state.floatSpeed * 5.0f) * (state.floatHeight * 0.1f);
        poseStack.translate(0.0f, -floatOffset, 0.0f);
    }
}
