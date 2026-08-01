package com.nsukstudio.newsimukraft.api.core;

import net.minecraft.core.BlockPos;
import java.util.UUID;

/**
 * 城市地块数据接口 —— 代表一个城市的最小网格单元
 */
public interface ICityPlot {

    /** 获取地块左下角坐标（以区块为单位对齐） */
    BlockPos getOrigin();

    /** 判断地块是否已被建筑占用 */
    boolean isOccupied();

    /** 获取占用此地块的建筑ID，未占用返回 null */
    UUID getOccupiedBuildingId();

    /** 获取该地块所属的城市ID */
    UUID getCityId();

    /** 获取地块宜居度评分（0-100，影响住宅入住率） */
    float getLivabilityScore();

    /** 获取地块污染值（0-100，由工业建筑写入） */
    float getPollutionLevel();

    /** 设置污染值（工业建筑模块调用） */
    void setPollutionLevel(float level);
}
