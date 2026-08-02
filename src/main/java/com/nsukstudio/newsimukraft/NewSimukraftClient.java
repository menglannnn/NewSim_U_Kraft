package com.nsukstudio.newsimukraft;

import com.nsukstudio.newsimukraft.client.model.FloatingBuildBoxModel;
import com.nsukstudio.newsimukraft.client.model.ModModelLayers;
import com.nsukstudio.newsimukraft.client.renderer.FloatingBuildBoxRenderer;
import com.nsukstudio.newsimukraft.registry.ModEntities;
import com.nsukstudio.newsimukraft.render.RenderManagerFactory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * 客户端入口 —— 注册渲染事件和配置界面
 *
 * 渲染管线：
 *   RenderManagerFactory.get() 自动检测 OpenGL/Vulkan 后端，
 *   在 RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS 阶段
 *   调用渲染管理器绘制建造预览和城市边界。
 */
@Mod(value = NewSimukraft.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = NewSimukraft.MODID, value = Dist.CLIENT)
public class NewSimukraftClient {

    public NewSimukraftClient(ModContainer container) {
        // 注册 NeoForge 配置界面（Cloth Config 样式）
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // 预初始化渲染后端（触发后端检测和单例创建）
        NewSimukraft.LOGGER.info("[Client] 渲染后端: {}", RenderManagerFactory.get().getBackendName());
    }

    /** 注册自定义实体模型层 */
    @SubscribeEvent
    static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.FLOATING_BUILD_BOX,
                FloatingBuildBoxModel::createBodyLayer);
    }

    /** 注册自定义实体渲染器 */
    @SubscribeEvent
    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.FLOATING_BUILD_BOX.get(),
                FloatingBuildBoxRenderer::new);
    }

    /**
     * 渲染关卡阶段事件 —— 绘制建造预览和城市边界
     * MC 26.2：RenderLevelStageEvent 改为子事件系统，直接订阅具体阶段子类，无需 getStage() 判断
     * 在透明方块之后绘制，保证叠加显示效果
     */
    @SubscribeEvent
    static void onRenderLevelStage(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        // 从 BuildingManager 获取当前玩家预览状态，调用 RenderManagerFactory.get().renderBuildPreview(...)
    }
}

