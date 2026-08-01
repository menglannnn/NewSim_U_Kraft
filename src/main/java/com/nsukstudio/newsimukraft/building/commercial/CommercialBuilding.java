package com.nsukstudio.newsimukraft.building.commercial;

import com.nsukstudio.newsimukraft.api.economy.IResourceProducer;
import com.nsukstudio.newsimukraft.building.BuildingData;

import java.util.UUID;

/**
 * 商业建筑逻辑 —— 营收计算、客流承载、商品供需模拟
 *
 * 营收模型：基础产出 × 等级倍率 × 客流系数（由宜居度决定）
 * 使用方式：建造商业建筑时实例化，并通过 IEconomyManager.registerProducer() 注册
 */
public class CommercialBuilding implements IResourceProducer {

    /** 等级对应的营收倍率（index = level-1） */
    private static final float[] LEVEL_MULTIPLIER = {1.0f, 1.5f, 2.2f};

    private final BuildingData buildingData;

    /** 基础每 tick 营收（由建筑类型配置决定，此处示例值） */
    private final long baseTickOutput;

    /** 当前客流系数 [0.0, 1.0]，由宜居度/周边配套驱动，定期由城市模块更新 */
    private volatile float trafficFactor = 0.8f;

    public CommercialBuilding(BuildingData buildingData, long baseTickOutput) {
        this.buildingData = buildingData;
        this.baseTickOutput = baseTickOutput;
    }

    @Override
    public long getTickOutput() {
        if (!buildingData.isOperational()) return 0L;
        int level = buildingData.getLevel();
        float multiplier = level <= LEVEL_MULTIPLIER.length
                ? LEVEL_MULTIPLIER[level - 1] : LEVEL_MULTIPLIER[LEVEL_MULTIPLIER.length - 1];
        return (long) (baseTickOutput * multiplier * trafficFactor);
    }

    @Override
    public UUID getCityId() { return buildingData.getCityId(); }

    @Override
    public boolean isActive() { return buildingData.isOperational(); }

    /**
     * 更新客流系数（由城市宜居度计算后调用）
     * @param factor 0.0（无客流）～ 1.0（满客流）
     */
    public void setTrafficFactor(float factor) {
        this.trafficFactor = Math.max(0f, Math.min(1f, factor));
    }

    public BuildingData getBuildingData() { return buildingData; }

    /** 获取该商业建筑的最大客流承载（等级 × 基础容量） */
    public int getMaxCapacity() {
        return 20 * buildingData.getLevel(); // 每级增加 20 名顾客容量
    }
}
