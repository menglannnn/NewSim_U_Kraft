package com.nsukstudio.newsimukraft.api.core;

import com.nsukstudio.newsimukraft.api.building.IBuildingData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.List;
import java.util.UUID;

/**
 * 城市核心管理器接口 —— 所有模块对城市基础数据的读写必须通过此接口
 * 职责：地块网格化管理、区域权属划分、全局状态同步
 */
public interface ICityManager {

    /** 获取指定坐标所属的地块数据 */
    ICityPlot getPlot(BlockPos pos);

    /** 注册新城市（玩家建立城市时调用） */
    boolean createCity(UUID playerId, BlockPos origin, ServerLevel level);

    /** 标记地块为已占用（建造模块调用） */
    void occupyPlot(BlockPos plotPos, IBuildingData building);

    /** 释放地块占用（拆除建筑时调用） */
    void releasePlot(BlockPos plotPos);

    /** 获取城市当前总人口（住宅模块写入，NPC模块读取） */
    int getTotalPopulation(UUID cityId);

    /** 更新城市总人口（住宅模块调用） */
    void updatePopulation(UUID cityId, int delta);

    /** 强制同步城市全局状态到所有客户端 */
    void syncToClients(ServerLevel level);

    /** 判断指定坐标是否在某城市的管辖范围内 */
    boolean isInsideCity(BlockPos pos, UUID cityId);

    /** 获取所有激活中的城市ID列表 */
    List<UUID> getAllActiveCities();
}
