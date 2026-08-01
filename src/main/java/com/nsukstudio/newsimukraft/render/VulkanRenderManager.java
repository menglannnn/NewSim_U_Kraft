package com.nsukstudio.newsimukraft.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nsukstudio.newsimukraft.api.render.IRenderManager;
import net.minecraft.core.BlockPos;

/**
 * Vulkan 渲染后端实现
 *
 * Minecraft 26.2 通过 Sodium/Iris 兼容层支持 Vulkan 渲染管线。
 * 当检测到 Vulkan 后端时（RenderManagerFactory 判断），使用此实现。
 *
 * 当前版本与 OpenGL 后端逻辑相同（均通过 Blaze3D 抽象层绘制），
 * 若后续 Vulkan 管线提供专有渲染 API，在此覆盖对应方法即可扩展，
 * 不影响 OpenGL 路径，符合开闭原则。
 */
public class VulkanRenderManager implements IRenderManager {

    private static final float[] COLOR_VALID    = {0.0f, 1.0f, 0.2f, 0.5f};
    private static final float[] COLOR_INVALID  = {1.0f, 0.1f, 0.1f, 0.5f};
    private static final float[] COLOR_OUTLINE  = {1.0f, 0.9f, 0.0f, 0.8f};
    private static final float[] COLOR_BOUNDARY = {0.2f, 0.5f, 1.0f, 0.6f};

    @Override
    public void renderBuildPreview(PoseStack poseStack, BlockPos pos, int[] footprint, boolean valid) {
        float[] color = valid ? COLOR_VALID : COLOR_INVALID;
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
    public String getBackendName() { return "vulkan"; }
}
