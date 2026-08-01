package com.nsukstudio.newsimukraft.api.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;

/**
 * 渲染管理器接口 —— GL 与 Vulkan 双后端的统一渲染抽象
 * 职责：建造预览渲染、UI 叠加层、建筑轮廓高亮
 *
 * 使用方式：
 *   客户端通过 RenderManagerFactory.get() 获取当前后端实例，
 *   不应直接 new 任何实现类，保证后端可切换。
 */
public interface IRenderManager {

    /**
     * 渲染建造预览框（合法放置区域显示绿色，非法显示红色）
     * 在 RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS 阶段调用
     *
     * @param poseStack  当前帧姿态栈
     * @param pos        目标放置位置
     * @param footprint  占地范围 [宽, 深]
     * @param valid      true=绿色合法, false=红色非法
     */
    void renderBuildPreview(PoseStack poseStack, BlockPos pos, int[] footprint, boolean valid);

    /**
     * 渲染建筑选择高亮轮廓
     *
     * @param poseStack  当前帧姿态栈
     * @param pos        建筑位置
     * @param footprint  占地范围
     */
    void renderBuildingOutline(PoseStack poseStack, BlockPos pos, int[] footprint);

    /**
     * 渲染城市范围边界线（城市模块调用）
     *
     * @param poseStack 当前帧姿态栈
     * @param minPos    边界最小坐标
     * @param maxPos    边界最大坐标
     */
    void renderCityBoundary(PoseStack poseStack, BlockPos minPos, BlockPos maxPos);

    /** 获取当前渲染后端名称（"opengl" 或 "vulkan"） */
    String getBackendName();
}
