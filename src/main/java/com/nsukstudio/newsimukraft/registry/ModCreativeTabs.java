package com.nsukstudio.newsimukraft.registry;

import com.nsukstudio.newsimukraft.NewSimukraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 模组创造物品栏注册
 *
 * 图标：建筑盒物品
 * 背景：tab_gui.png
 */
public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NewSimukraft.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB =
            CREATIVE_TABS.<CreativeModeTab>register("main", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.newsimukraft.main"))
                    .icon(() -> new ItemStack(ModItems.BUILD_BOX.get()))
                    .backgroundTexture(Identifier.fromNamespaceAndPath(
                            NewSimukraft.MODID, "textures/gui/tab_gui.png"))
                    .displayItems((params, output) -> {
                        // 建筑系统
                        output.accept(ModItems.BUILD_BOX.get());
                    })
                    .build());

    public static void register(IEventBus bus) {
        CREATIVE_TABS.register(bus);
    }

    private ModCreativeTabs() {}
}
