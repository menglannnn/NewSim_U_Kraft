package com.nsukstudio.newsimukraft.registry;

import com.nsukstudio.newsimukraft.NewSimukraft;
import com.nsukstudio.newsimukraft.block.BankControlBoxBlock;
import com.nsukstudio.newsimukraft.block.BuildBoxBlock;
import com.nsukstudio.newsimukraft.block.CityCoreBlock;
import com.nsukstudio.newsimukraft.block.ResidentialControlBoxBlock;
import net.minecraft.world.level.block.SlimeBlock;
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

    /** 建筑系统 - New-Sim-U 控制盒，统一管理建筑分类（商业/工业/住宅/公共/其他） */
    public static final DeferredBlock<ResidentialControlBoxBlock> RESIDENTIAL_CONTROL_BOX =
            BLOCKS.registerBlock("residential_control_box", ResidentialControlBoxBlock::new,
                    () -> BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WOOD)
                            .strength(2.0f, 3.0f)
                            .sound(SoundType.WOOD)
                            .requiresCorrectToolForDrops());

    /** 城市核心系统 - New-Sim-U 城市核心，统一管理所有建筑 NPC 及城市 */
    public static final DeferredBlock<CityCoreBlock> CITY_CORE =
            BLOCKS.registerBlock("city_core", CityCoreBlock::new,
                    () -> BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WOOD)
                            .strength(2.0f, 3.0f)
                            .sound(SoundType.WOOD)
                            .requiresCorrectToolForDrops());

    /** 经济系统 - New-Sim-U 银行控制柜，管理城市经济与配给 */
    public static final DeferredBlock<BankControlBoxBlock> BANK_CONTROL_BOX =
            BLOCKS.registerBlock("bank_control_box", BankControlBoxBlock::new,
                    () -> BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(5.0f, 6.0f)
                            .sound(SoundType.METAL)
                            .requiresCorrectToolForDrops());

    /** 建筑系统 - Sim-U 蓝色霓虹灯，玻璃质感灯块装饰 */
    public static final DeferredBlock<net.minecraft.world.level.block.Block> BLUE_LIGHT_BLOCK =
            BLOCKS.registerSimpleBlock("blue_light_block",
                    () -> BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_BLUE)
                            .strength(0.3f)
                            .sound(SoundType.GLASS)
                            .lightLevel(state -> 15)
                            .noOcclusion()
                            .isValidSpawn((state, level, pos, type) -> false)
                            .isRedstoneConductor((state, level, pos) -> false)
                            .isSuffocating((state, level, pos) -> false)
                            .isViewBlocking((state, level, pos) -> false));

    /** 食物系统 - Sim-U 奶酪块，工业建筑产出的粘性食材方块 */
    public static final DeferredBlock<SlimeBlock> CHEESE_BLOCK =
            BLOCKS.registerBlock("cheese_block", SlimeBlock::new,
                    () -> BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_YELLOW)
                            .strength(0.5f)
                            .sound(SoundType.GRAVEL));

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }

    private ModBlocks() {}
}
