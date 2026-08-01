package com.nsukstudio.newsimukraft.render;

import com.nsukstudio.newsimukraft.api.render.IRenderManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * 渲染管理器工厂 —— 根据当前渲染后端自动选择实现
 *
 * 使用方式：
 *   客户端代码统一通过 RenderManagerFactory.get() 获取当前实例，
 *   不直接 new OpenGLRenderManager() 或 VulkanRenderManager()。
 *
 * 后端切换逻辑：
 *   检查系统属性 "sodium.vulkan" 或环境变量 VULKAN_ENABLED 判断。
 *   若检测到 Vulkan 支持，返回 VulkanRenderManager，否则 OpenGLRenderManager。
 *   这保证后端可在不修改业务代码的情况下切换（开闭原则）。
 */
@OnlyIn(Dist.CLIENT)
public final class RenderManagerFactory {

    private static volatile IRenderManager instance;

    /**
     * 获取当前渲染后端实例（单例，首次调用时自动检测并初始化）
     */
    public static IRenderManager get() {
        if (instance == null) {
            synchronized (RenderManagerFactory.class) {
                if (instance == null) {
                    instance = detectBackend();
                }
            }
        }
        return instance;
    }

    /** 检测当前渲染后端类型 */
    private static IRenderManager detectBackend() {
        // 通过系统属性判断是否启用 Vulkan（由启动参数或 Sodium/Iris 注入）
        boolean useVulkan = Boolean.getBoolean("sodium.vulkan")
                || "true".equalsIgnoreCase(System.getenv("NSUK_VULKAN"));
        if (useVulkan) {
            return new VulkanRenderManager();
        }
        return new OpenGLRenderManager();
    }

    private RenderManagerFactory() {}
}
