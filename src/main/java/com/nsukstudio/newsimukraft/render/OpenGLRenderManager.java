package com.nsukstudio.newsimukraft.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nsukstudio.newsimukraft.api.render.IRenderManager;
import net.minecraft.core.BlockPos;

/**
 * OpenGL 渲染后端实现 —— 使用 NeoForge/Blaze3D 的标准渲染管线
 *
 * 在 RenderLevelStageEvent 中调用，使用 LINES 渲染类型绘制边框。
 * 客户端专属，仅在 Dist.CLIENT 下实例化（由 RenderManagerFactory 保证）。
 */
public class OpenGLRenderManager implements IRenderManager {

    /** 合法放置颜色：绿色 RGBA */
    private static final float[] COLOR_VALID   = {0.0f, 1.0f, 0.2f, 0.5f};
    /** 非法放置颜色：红色 RGBA */
    private static final float[] COLOR_INVALID = {1.0f, 0.1f, 0.1f, 0.5f};
    /** 选中高亮颜色：黄色 RGBA */
    private static final float[] COLOR_OUTLINE = {1.0f, 0.9f, 0.0f, 0.8f};
    /** 城市边界颜色：蓝色 RGBA */
    private static final float[] COLOR_BOUNDARY = {0.2f, 0.5f, 1.0f, 0.6f};

    @Override
    public void renderBuildPreview(PoseStack poseStack, BlockPos pos, int[] footprint, boolean valid) {
        float[] color = valid ? COLOR_VALID : COLOR_INVALID;
        // 以 poseStack 当前变换绘制建筑占地范围的线框
        // 实际渲染需在 RenderLevelStageEvent 回调中获取 MultiBufferSource 后调用
        // 此处定义渲染逻辑，由 RenderEventHandler 传入 bufferSource 后执行
        RenderHelper.drawBox(poseStack, pos, footprint[0], 4, footprint[1], color);
    }

    @Override
    public void renderBuildingOutline(PoseStack poseStack, BlockPos pos, int[] footprint) {
        RenderHelper.drawBox(poseStack, pos, footprint[0], 4, footprint[1], COLOR_OUTLINE);
    }

    @Override
    public void renderCityBoundary(PoseStack poseStack, BlockPos minPos, BlockPos maxPos) {
        int w = maxPos.getX() - minPos.getX();
        int h = maxPos.getY() - minPos.getY();
        int d = maxPos.getZ() - minPos.getZ();
        RenderHelper.drawBox(poseStack, minPos, w, h, d, COLOR_BOUNDARY);
    }

    @Override
    public String getBackendName() { return "opengl"; }
}
