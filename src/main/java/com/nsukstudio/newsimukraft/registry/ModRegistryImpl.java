package com.nsukstudio.newsimukraft.registry;

import com.nsukstudio.newsimukraft.api.registry.BuildingTypeDef;
import com.nsukstudio.newsimukraft.api.registry.IModRegistry;
import com.nsukstudio.newsimukraft.api.registry.NpcTypeDef;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 注册管理模块实现 —— 线程安全的全局类型索引
 * 使用 ConcurrentHashMap 保证并发注册安全
 */
public final class ModRegistryImpl implements IModRegistry {

    /** 全局单例 */
    public static final ModRegistryImpl INSTANCE = new ModRegistryImpl();

    // ConcurrentHashMap 保证并发读写安全
    private final ConcurrentHashMap<String, BuildingTypeDef> buildingTypes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, NpcTypeDef> npcTypes = new ConcurrentHashMap<>();

    private ModRegistryImpl() {}

    /**
     * 初始化：注册所有内置类型
     * 在 FMLCommonSetupEvent 中调用，此时模组加载完成
     */
    public void init() {
        // 内置建筑类型注册
        registerBuildingType("newsimukraft:shop_level1",
                new BuildingTypeDef("newsimukraft:shop_level1", "commercial",
                        new int[]{2, 2}, 3, 500L));
        registerBuildingType("newsimukraft:factory_level1",
                new BuildingTypeDef("newsimukraft:factory_level1", "industrial",
                        new int[]{3, 3}, 3, 1500L));
        registerBuildingType("newsimukraft:house_level1",
                new BuildingTypeDef("newsimukraft:house_level1", "residential",
                        new int[]{2, 2}, 3, 300L));

        // 内置 NPC 类型注册
        registerNpcType("newsimukraft:resident",
                new NpcTypeDef("newsimukraft:resident", "居民", 200, 3.0f));
        registerNpcType("newsimukraft:merchant",
                new NpcTypeDef("newsimukraft:merchant", "商贩", 50, 2.5f));
        registerNpcType("newsimukraft:worker",
                new NpcTypeDef("newsimukraft:worker", "工人", 100, 3.5f));
    }

    @Override
    public void registerBuildingType(String typeId, BuildingTypeDef def) {
        if (buildingTypes.containsKey(typeId)) {
            throw new IllegalStateException("[Registry] 建筑类型重复注册: " + typeId);
        }
        buildingTypes.put(typeId, def);
    }

    @Override
    public Optional<BuildingTypeDef> getBuildingType(String typeId) {
        return Optional.ofNullable(buildingTypes.get(typeId));
    }

    @Override
    public Collection<String> getAllBuildingTypeIds() {
        return Collections.unmodifiableSet(buildingTypes.keySet());
    }

    @Override
    public void registerNpcType(String typeId, NpcTypeDef def) {
        if (npcTypes.containsKey(typeId)) {
            throw new IllegalStateException("[Registry] NPC 类型重复注册: " + typeId);
        }
        npcTypes.put(typeId, def);
    }

    @Override
    public Optional<NpcTypeDef> getNpcType(String typeId) {
        return Optional.ofNullable(npcTypes.get(typeId));
    }

    @Override
    public boolean isRegistered(String typeId) {
        return buildingTypes.containsKey(typeId) || npcTypes.containsKey(typeId);
    }
}
