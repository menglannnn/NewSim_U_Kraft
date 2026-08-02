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

    /** 控制盒物品（对应 RESIDENTIAL_CONTROL_BOX 方块的 BlockItem） */
    public static final DeferredItem<BlockItem> RESIDENTIAL_CONTROL_BOX =
            ITEMS.registerSimpleBlockItem("residential_control_box", ModBlocks.RESIDENTIAL_CONTROL_BOX);

    /** 城市核心物品（对应 CITY_CORE 方块的 BlockItem） */
    public static final DeferredItem<BlockItem> CITY_CORE =
            ITEMS.registerSimpleBlockItem("city_core", ModBlocks.CITY_CORE);

    /** 银行控制柜物品（对应 BANK_CONTROL_BOX 方块的 BlockItem） */
    public static final DeferredItem<BlockItem> BANK_CONTROL_BOX =
            ITEMS.registerSimpleBlockItem("bank_control_box", ModBlocks.BANK_CONTROL_BOX);

    /** 蓝色霓虹灯物品（对应 BLUE_LIGHT_BLOCK 方块的 BlockItem） */
    public static final DeferredItem<BlockItem> BLUE_LIGHT_BLOCK =
            ITEMS.registerSimpleBlockItem("blue_light_block", ModBlocks.BLUE_LIGHT_BLOCK);

    /** 奶酪块物品（对应 CHEESE_BLOCK 方块的 BlockItem） */
    public static final DeferredItem<BlockItem> CHEESE_BLOCK =
            ITEMS.registerSimpleBlockItem("cheese_block", ModBlocks.CHEESE_BLOCK);

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }

    private ModItems() {}
}
