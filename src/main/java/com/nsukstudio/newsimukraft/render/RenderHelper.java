package com.nsukstudio.newsimukraft.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.BlockPos;
import org.joml.Matrix4f;

/**
 * 渲染工具类 —— 提供线框盒子绘制，供 GL/Vulkan 两个后端共用
 *
 * 注意：此类中的方法需在持有 VertexConsumer 的上下文中调用。
 * 由于 IRenderManager 接口不携带 VertexConsumer，实际渲染时
 * 由 RenderEventHandler 在事件回调中提供 MultiBufferSource，
 * 并调用此工具类完成绘制。
 */
public final class RenderHelper {

    private RenderHelper() {}

    /**
     * 绘制线框长方体（供建造预览/选中高亮/城市边界使用）
     *
     * @param poseStack 当前帧姿态栈（由事件回调传入）
     * @param origin    长方体左下角坐标
     * @param w         宽（X轴，格）
     * @param h         高（Y轴，格）
     * @param d         深（Z轴，格）
     * @param color     RGBA 颜色数组，每个分量 [0,1]
     */
    public static void drawBox(PoseStack poseStack, BlockPos origin,
                                int w, int h, int d, float[] color) {
        // 此方法在 RenderEventHandler 中通过 bufferSource.getBuffer(RenderType.LINES) 获取
        // VertexConsumer 后实际绘制，此处作为占位供后续补充实现
        // 实际线框绘制使用 LevelRenderer.renderLineBox() 或手动提交顶点
    }

    /**
     * 向 VertexConsumer 提交单条线段（两端点 + 颜色）
     * 在 RenderEventHandler 中调用
     */
    public static void addLine(VertexConsumer consumer, Matrix4f matrix,
                                float x1, float y1, float z1,
                                float x2, float y2, float z2,
                                float r, float g, float b, float a) {
        // 法线方向统一向上（LINES 渲染类型需要法线）
        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setNormal(0, 1, 0);
        consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a).setNormal(0, 1, 0);
    }
}
