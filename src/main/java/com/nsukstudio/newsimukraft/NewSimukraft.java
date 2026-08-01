package com.nsukstudio.newsimukraft;

import com.mojang.logging.LogUtils;
import com.nsukstudio.newsimukraft.config.NSUKConfig;
import com.nsukstudio.newsimukraft.database.DatabaseManager;
import com.nsukstudio.newsimukraft.economy.EconomyManager;
import com.nsukstudio.newsimukraft.city.CityManager;
import com.nsukstudio.newsimukraft.building.BuildingManager;
import com.nsukstudio.newsimukraft.npc.NpcManager;
import com.nsukstudio.newsimukraft.sound.SoundManager;
import com.nsukstudio.newsimukraft.registry.ModRegistryImpl;
import com.nsukstudio.newsimukraft.registry.ModRegistrations;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

/**
 * NewSimukraft 模组主入口 —— 负责各模块的初始化顺序管理
 *
 * 模块初始化顺序：
 *   注册管理 → 数据库 → 城市核心 → 经济 → 建造 → NPC → 音效
 */
@Mod(NewSimukraft.MODID)
public class NewSimukraft {

    public static final String MODID = "newsimukraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    // 各模块单例，由 ModuleHolder 统一管理，避免循环依赖
    private DatabaseManager databaseManager;
    private CityManager cityManager;
    private EconomyManager economyManager;
    private BuildingManager buildingManager;
    private NpcManager npcManager;
    private SoundManager soundManager;

    public NewSimukraft(IEventBus modEventBus, ModContainer modContainer) {
        // 注册配置文件（服务端+客户端各一份）
        modContainer.registerConfig(ModConfig.Type.COMMON, NSUKConfig.COMMON_SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, NSUKConfig.CLIENT_SPEC);

        // 注册 NeoForge 内置类型（方块/物品/实体/音效）
        ModRegistrations.register(modEventBus);

        // 公共初始化
        modEventBus.addListener(this::onCommonSetup);

        // 服务端事件
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);
        NeoForge.EVENT_BUS.addListener(this::onServerTick);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        // 初始化注册管理模块（必须最先，其他模块依赖类型定义）
        ModRegistryImpl.INSTANCE.init();
        LOGGER.info("[NewSimukraft] 注册管理模块初始化完成");
    }

    /** 服务端启动时按序初始化所有功能模块 */
    private void onServerStarting(ServerStartingEvent event) {
        String worldPath = event.getServer().getWorldPath(
                net.minecraft.world.level.storage.LevelResource.ROOT).toString();

        // 1. 数据库模块（最先初始化，其他模块依赖持久化）
        databaseManager = new DatabaseManager();
        databaseManager.initialize(worldPath);
        ModuleHolder.setDatabase(databaseManager);

        // 2. 城市核心模块
        cityManager = new CityManager(databaseManager);
        cityManager.loadAll();
        ModuleHolder.setCity(cityManager);

        // 3. 经济模块
        economyManager = new EconomyManager(databaseManager, cityManager);
        ModuleHolder.setEconomy(economyManager);

        // 4. 建造模块
        buildingManager = new BuildingManager(cityManager, economyManager, databaseManager);
        ModuleHolder.setBuilding(buildingManager);

        // 5. NPC 模块
        npcManager = new NpcManager(cityManager, databaseManager);
        ModuleHolder.setNpc(npcManager);

        // 6. 音效模块（无服务端状态，直接初始化）
        soundManager = new SoundManager();
        ModuleHolder.setSound(soundManager);

        LOGGER.info("[NewSimukraft] 所有模块启动完成，世界路径: {}", worldPath);
    }

    /** 服务端关闭时安全释放所有模块资源 */
    private void onServerStopping(ServerStoppingEvent event) {
        if (databaseManager != null) {
            databaseManager.backup("shutdown");
            databaseManager.close();
        }
        LOGGER.info("[NewSimukraft] 所有模块已安全关闭");
    }

    /** 服务端每 tick 驱动：经济结算、NPC 行为、污染衰减 */
    private void onServerTick(ServerTickEvent.Post event) {
        var server = event.getServer();
        if (economyManager == null || cityManager == null) return;

        int taxCycle = com.nsukstudio.newsimukraft.config.NSUKConfig.TAX_CYCLE_TICKS.get();
        long tick = server.getTickCount();

        // 经济结算（每 taxCycle ticks 一次）
        if (tick % taxCycle == 0) {
            for (java.util.UUID cityId : cityManager.getAllActiveCities()) {
                economyManager.processDailyTax(cityId);
            }
        }

        // NPC 行为驱动（每 tick，限主世界）
        if (npcManager != null) {
            var overworld = server.overworld();
            npcManager.tickAll(overworld);
        }
    }
}
