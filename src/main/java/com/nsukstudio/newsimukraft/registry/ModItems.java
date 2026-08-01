package com.nsukstudio.newsimukraft.registry;

import com.nsukstudio.newsimukraft.NewSimukraft;
import net.minecraft.world.item.BlockItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 模组物品注册表
 *
 * 使用方式：在 ModRegistrations.register(modEventBus) 中调用
 *   ModItems.ITEMS.register(modEventBus)
 */
public final class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(NewSimukraft.MODID);

    /** 建筑盒物品（对应 BUILD_BOX 方块的 BlockItem） */
    public static final DeferredItem<BlockItem> BUILD_BOX =
            ITEMS.registerSimpleBlockItem("build_box", ModBlocks.BUILD_BOX);

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }

    private ModItems() {}
}
