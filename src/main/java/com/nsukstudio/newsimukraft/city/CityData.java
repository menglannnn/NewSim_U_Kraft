package com.nsukstudio.newsimukraft.city;

import net.minecraft.core.BlockPos;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 城市数据容器 —— 持有一座城市的所有运行时状态
 *
 * 由 CityManager 创建和管理，持久化通过 IDatabaseManager 完成。
 * 字段读写线程安全：基本类型使用 Atomic*，集合使用 ConcurrentHashMap。
 */
public class CityData {

    private final UUID cityId;
    private final UUID ownerPlayerId;    // 城市所有者
    private final BlockPos originBlock;  // 城市原点（世界坐标）
    private final String dimensionKey;   // 所在维度，如 "minecraft:overworld"

    /** 城市内所有地块，key = "x,z"（地块坐标，非世界坐标） */
    private final ConcurrentHashMap<String, CityPlot> plots = new ConcurrentHashMap<>();

    /** 当前总人口（住宅模块写入） */
    private final AtomicInteger population = new AtomicInteger(0);

    /** 城市是否激活（false = 暂停所有逻辑） */
    private volatile boolean active = true;

    public CityData(UUID cityId, UUID ownerPlayerId, BlockPos originBlock, String dimensionKey) {
        this.cityId = cityId;
        this.ownerPlayerId = ownerPlayerId;
        this.originBlock = originBlock;
        this.dimensionKey = dimensionKey;
    }

    // ---- 基本属性 ----

    public UUID getCityId() { return cityId; }
    public UUID getOwnerPlayerId() { return ownerPlayerId; }
    public BlockPos getOriginBlock() { return originBlock; }
    public String getDimensionKey() { return dimensionKey; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    // ---- 人口 ----

    public int getPopulation() { return population.get(); }

    /** 线程安全的人口增减 */
    public void addPopulation(int delta) {
        population.updateAndGet(v -> Math.max(0, v + delta));
    }

    // ---- 地块管理 ----

    /** 获取地块（按地块坐标字符串，不是世界坐标） */
    public CityPlot getPlot(String key) { return plots.get(key); }

    public void putPlot(String key, CityPlot plot) { plots.put(key, plot); }

    public ConcurrentHashMap<String, CityPlot> getAllPlots() { return plots; }

    public int getPlotCount() { return plots.size(); }
}
