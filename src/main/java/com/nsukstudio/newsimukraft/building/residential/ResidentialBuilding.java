package com.nsukstudio.newsimukraft.building.residential;

import com.nsukstudio.newsimukraft.api.economy.IResourceProducer;
import com.nsukstudio.newsimukraft.building.BuildingData;
import com.nsukstudio.newsimukraft.city.CityPlot;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 住宅建筑逻辑 —— 人口承载、居民入住、租金核算、宜居度影响
 *
 * 租金模型：基础租金 × 等级倍率 × 宜居度系数
 * 入住逻辑：当前入住人数 < 容量上限时可接受新居民（由 NpcManager 触发）
 *
 * 使用方式：
 *   建造住宅时实例化，通过 IEconomyManager.registerProducer() 注册租金产出。
 *   NPC 模块调用 tryMoveIn() 尝试入住，移出时调用 moveOut()，
 *   两者均通过 ICityManager.updatePopulation() 同步人口。
 */
public class ResidentialBuilding implements IResourceProducer {

    /** 等级对应的租金倍率 */
    private static final float[] RENT_MULTIPLIER = {1.0f, 1.6f, 2.5f};

    private final BuildingData buildingData;
    private final long baseRentPerTick; // 每 tick 基础租金
    private final int capacityPerLevel; // 每级增加的人口容量

    /** 当前入住人数（线程安全） */
    private final AtomicInteger occupants = new AtomicInteger(0);

    /** 关联地块，用于读取宜居度和污染值 */
    private CityPlot associatedPlot;

    public ResidentialBuilding(BuildingData buildingData, long baseRentPerTick, int capacityPerLevel) {
        this.buildingData = buildingData;
        this.baseRentPerTick = baseRentPerTick;
        this.capacityPerLevel = capacityPerLevel;
    }

    // ---- IResourceProducer（租金收入） ----

    @Override
    public long getTickOutput() {
        if (!buildingData.isOperational() || occupants.get() == 0) return 0L;
        int level = buildingData.getLevel();
        float multiplier = level <= RENT_MULTIPLIER.length
                ? RENT_MULTIPLIER[level - 1] : RENT_MULTIPLIER[RENT_MULTIPLIER.length - 1];
        // 宜居度影响租金：宜居度 100 时满额租金，50 时半额
        float livability = associatedPlot != null ? associatedPlot.getLivabilityScore() : 50f;
        float livabilityFactor = livability / 100f;
        // 按实际入住率计算（非满住时按比例收租）
        float occupancyRate = (float) occupants.get() / getMaxCapacity();
        return (long) (baseRentPerTick * multiplier * livabilityFactor * occupancyRate);
    }

    @Override
    public UUID getCityId() { return buildingData.getCityId(); }

    @Override
    public boolean isActive() { return buildingData.isOperational(); }

    // ---- 居民入住/迁出 ----

    /**
     * 尝试入住（NPC 模块调用）
     * @return true = 入住成功，false = 已满
     */
    public boolean tryMoveIn() {
        int cap = getMaxCapacity();
        while (true) {
            int cur = occupants.get();
            if (cur >= cap) return false;
            if (occupants.compareAndSet(cur, cur + 1)) return true;
        }
    }

    /** 居民迁出（NPC 消亡/换住所时调用） */
    public void moveOut() {
        occupants.updateAndGet(v -> Math.max(0, v - 1));
    }

    /** 获取最大住房容量（等级 × 每级容量） */
    public int getMaxCapacity() {
        return capacityPerLevel * buildingData.getLevel();
    }

    public int getOccupantCount() { return occupants.get(); }

    public BuildingData getBuildingData() { return buildingData; }

    public void setAssociatedPlot(CityPlot plot) { this.associatedPlot = plot; }
}
