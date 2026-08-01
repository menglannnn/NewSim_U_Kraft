package com.nsukstudio.newsimukraft.registry;

import com.nsukstudio.newsimukraft.NewSimukraft;
import com.nsukstudio.newsimukraft.block.BuildBoxBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 模组方块注册表
 *
 * 使用方式：在 ModRegistrations.register(modEventBus) 中调用
 *   ModBlocks.BLOCKS.register(modEventBus)
 */
public final class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(NewSimukraft.MODID);

    /** 建筑系统 - Sim-U 建筑盒放置破坏播放音效 */
    public static final DeferredBlock<BuildBoxBlock> BUILD_BOX =
            BLOCKS.registerBlock("build_box", BuildBoxBlock::new,
                    () -> BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WOOD)
                            .strength(2.0f, 3.0f)
                            .sound(SoundType.WOOD)
                            .requiresCorrectToolForDrops());

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }

    private ModBlocks() {}
}
