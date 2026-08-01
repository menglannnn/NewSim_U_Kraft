package com.nsukstudio.newsimukraft.registry;

import com.nsukstudio.newsimukraft.network.NetworkRegistry;
import com.nsukstudio.newsimukraft.sound.ModSoundEvents;
import net.neoforged.bus.api.IEventBus;

/**
 * NeoForge 内置类型注册统筹 —— 统一管理方块/物品/实体/音效的 DeferredRegister
 *
 * 每个子注册器（方块、物品等）独立在对应模块中声明 DeferredRegister，
 * 此处仅负责将各模块的注册器接入 modEventBus。
 */
public final class ModRegistrations {

    private ModRegistrations() {}

    /**
     * 将所有模块的注册器接入事件总线
     * 在 NewSimukraft 构造函数中调用
     *
     * 如何添加新模块的注册器：
     *   1. 在对应模块包内创建 XXXRegistrations 类和 DeferredRegister 字段
     *   2. 在此处调用 XXXRegistrations.BLOCKS.register(modEventBus) 等
     */
    public static void register(IEventBus modEventBus) {
        // 音效注册
        ModSoundEvents.register(modEventBus);

        // 方块注册
        ModBlocks.register(modEventBus);

        // 物品注册（BlockItem 等）
        ModItems.register(modEventBus);

        // 创造物品栏注册
        ModCreativeTabs.register(modEventBus);

        // 网络数据包注册
        modEventBus.addListener(NetworkRegistry::onRegisterPayloads);
    }
}
