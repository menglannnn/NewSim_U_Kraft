package com.nsukstudio.newsimukraft.api.npc;

import net.minecraft.server.level.ServerLevel;
import java.util.List;
import java.util.UUID;

/**
 * NPC 管理器接口 —— NPC 生命周期、路径寻路、行为驱动的统一入口
 */
public interface INpcManager {

    /** 在指定城市生成一个 NPC（住宅模块入住时触发） */
    UUID spawnNpc(UUID cityId, String npcType, ServerLevel level);

    /** 移除 NPC（城市人口减少/建筑拆除时触发） */
    void despawnNpc(UUID npcId, ServerLevel level);

    /** 获取指定城市的所有 NPC */
    List<INpcData> getNpcsInCity(UUID cityId);

    /** 获取单个 NPC 数据 */
    INpcData getNpc(UUID npcId);

    /** 触发 NPC 寻路到目标坐标 */
    void pathfindTo(UUID npcId, net.minecraft.core.BlockPos target);

    /** 每游戏 tick 由服务端调度，驱动所有 NPC 行为状态机 */
    void tickAll(ServerLevel level);

    /** 获取指定建筑内当前占用的工人/居民数量 */
    int getOccupantCount(UUID buildingId);
}
