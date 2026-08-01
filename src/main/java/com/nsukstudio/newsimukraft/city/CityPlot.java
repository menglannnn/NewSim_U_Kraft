package com.nsukstudio.newsimukraft.city;

import com.nsukstudio.newsimukraft.api.building.IBuildingData;
import com.nsukstudio.newsimukraft.api.core.ICityPlot;
import net.minecraft.core.BlockPos;

import java.util.UUID;

/**
 * 城市地块实现 —— 城市网格的最小单元
 *
 * 每个地块对应一块固定大小的区域（由 NSUKConfig.CITY_PLOT_SIZE 决定）。
 * 地块坐标以左下角 origin 为标识，对齐到地块边长倍数。
 */
public class CityPlot implements ICityPlot {

    private final BlockPos origin;   // 地块左下角坐标
    private final UUID cityId;       // 所属城市

    private UUID occupiedBuildingId; // 占用建筑ID，null 表示空地
    private float pollutionLevel;    // 污染值 [0,100]
    private float livabilityScore;   // 宜居度 [0,100]，由城市管理器定期重算

    public CityPlot(BlockPos origin, UUID cityId) {
        this.origin = origin;
        this.cityId = cityId;
        this.pollutionLevel = 0f;
        this.livabilityScore = 50f; // 默认中等宜居度
    }

    @Override public BlockPos getOrigin() { return origin; }
    @Override public boolean isOccupied() { return occupiedBuildingId != null; }
    @Override public UUID getOccupiedBuildingId() { return occupiedBuildingId; }
    @Override public UUID getCityId() { return cityId; }
    @Override public float getLivabilityScore() { return livabilityScore; }
    @Override public float getPollutionLevel() { return pollutionLevel; }

    @Override
    public void setPollutionLevel(float level) {
        // 强制限定在 [0,100] 区间防止溢出
        this.pollutionLevel = Math.max(0f, Math.min(100f, level));
        recalcLivability();
    }

    /** 标记占用（由 CityManager 调用） */
    void occupy(IBuildingData building) {
        this.occupiedBuildingId = building.getBuildingId();
    }

    /** 释放占用（拆除建筑时调用） */
    void release() {
        this.occupiedBuildingId = null;
    }

    /** 根据污染值重算宜居度（污染越高宜居度越低） */
    void recalcLivability() {
        // 基础宜居 50 + 满分 50，污染每增加 1 点扣 0.5 分
        this.livabilityScore = Math.max(0f, 50f - pollutionLevel * 0.5f);
    }
}
