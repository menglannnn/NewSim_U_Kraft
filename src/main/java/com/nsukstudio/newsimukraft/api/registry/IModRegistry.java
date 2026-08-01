package com.nsukstudio.newsimukraft.api.registry;

import java.util.Collection;
import java.util.Optional;

/**
 * 模组注册管理器接口 —— 建筑类型、NPC类型、资源类型的统一注册与查询
 * 职责：防止重复注册、提供全局类型索引、支持模组可扩展性
 */
public interface IModRegistry {

    /**
     * 注册建筑类型定义
     * @param typeId   唯一字符串ID，如 "newsimukraft:shop_level1"
     * @param def      建筑类型定义对象
     */
    void registerBuildingType(String typeId, BuildingTypeDef def);

    /** 查询建筑类型定义，不存在返回 empty */
    Optional<BuildingTypeDef> getBuildingType(String typeId);

    /** 获取所有已注册建筑类型ID */
    Collection<String> getAllBuildingTypeIds();

    /**
     * 注册 NPC 类型
     * @param typeId   唯一字符串ID，如 "newsimukraft:resident"
     * @param def      NPC 类型定义
     */
    void registerNpcType(String typeId, NpcTypeDef def);

    /** 查询 NPC 类型定义 */
    Optional<NpcTypeDef> getNpcType(String typeId);

    /** 判断某类型 ID 是否已注册（防重复注册保护） */
    boolean isRegistered(String typeId);
}
