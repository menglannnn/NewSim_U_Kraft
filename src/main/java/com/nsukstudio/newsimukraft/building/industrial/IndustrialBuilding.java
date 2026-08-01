package com.nsukstudio.newsimukraft.building.industrial;

import com.nsukstudio.newsimukraft.api.economy.IResourceConsumer;
import com.nsukstudio.newsimukraft.api.economy.IResourceProducer;
import com.nsukstudio.newsimukraft.building.BuildingData;
import com.nsukstudio.newsimukraft.city.CityPlot;

import java.util.UUID;

/**
 * 工业建筑逻辑 —— 生产逻辑、污染排放、产能效率
 *
 * 工业建筑同时实现 IResourceProducer（成品产出）和 IResourceConsumer（原材料消耗）。
 * 污染值通过 CityPlot.setPollutionLevel() 写入地块，影响周边宜居度。
 *
 * 使用方式：建造工业建筑时实例化，分别通过
 *   IEconomyManager.registerProducer() 和 registerConsumer() 注册两端。
 */
public class IndustrialBuilding implements IResourceProducer, IResourceConsumer {

    /** 等级对应的产能效率倍率 */
    private static final float[] EFFICIENCY = {1.0f, 1.4f, 2.0f};

    private final BuildingData buildingData;
    private final long baseOutput;       // 每 tick 基础成品产出（换算为资金）
    private final long baseMaterialCost; // 每 tick 原材料消耗（资金）
    private final float basePollution;   // 每 tick 污染排放量（地块污染值）

    /** 关联的地块，用于写入污染值 */
    private CityPlot associatedPlot;

    public IndustrialBuilding(BuildingData buildingData, long baseOutput,
                              long baseMaterialCost, float basePollution) {
        this.buildingData = buildingData;
        this.baseOutput = baseOutput;
        this.baseMaterialCost = baseMaterialCost;
        this.basePollution = basePollution;
    }

    // ---- IResourceProducer ----

    @Override
    public long getTickOutput() {
        if (!buildingData.isOperational()) return 0L;
        return (long) (baseOutput * getEfficiency());
    }

    @Override
    public UUID getCityId() { return buildingData.getCityId(); }

    @Override
    public boolean isActive() { return buildingData.isOperational(); }

    // ---- IResourceConsumer ----

    @Override
    public long getTickConsumption() {
        if (!buildingData.isOperational()) return 0L;
        return (long) (baseMaterialCost * getEfficiency());
    }

    @Override
    public void onResourceInsufficient() {
        // 原材料不足时停工，停止产出并停止排污
        buildingData.setOperational(false);
    }

    // ---- 污染排放 ----

    /**
     * 每游戏 tick 由 NpcManager/ServerTick 调用，更新地块污染值
     * 停工状态下不排污
     */
    public void tickPollution() {
        if (associatedPlot == null || !buildingData.isOperational()) return;
        float current = associatedPlot.getPollutionLevel();
        associatedPlot.setPollutionLevel(current + basePollution * getEfficiency());
    }

    public void setAssociatedPlot(CityPlot plot) { this.associatedPlot = plot; }

    public BuildingData getBuildingData() { return buildingData; }

    /** 当前等级效率倍率 */
    private float getEfficiency() {
        int level = buildingData.getLevel();
        return level <= EFFICIENCY.length
                ? EFFICIENCY[level - 1] : EFFICIENCY[EFFICIENCY.length - 1];
    }

    /** 解锁条件检查（等级 2 需要城市人口 ≥ 50，可后续扩展） */
    public boolean isUnlocked(int cityPopulation) {
        return switch (buildingData.getLevel()) {
            case 2 -> cityPopulation >= 50;
            case 3 -> cityPopulation >= 200;
            default -> true;
        };
    }
}
