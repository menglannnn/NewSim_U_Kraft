package com.nsukstudio.newsimukraft.city;

import com.nsukstudio.newsimukraft.NewSimukraft;
import com.nsukstudio.newsimukraft.api.building.IBuildingData;
import com.nsukstudio.newsimukraft.api.core.ICityManager;
import com.nsukstudio.newsimukraft.api.core.ICityPlot;
import com.nsukstudio.newsimukraft.api.database.IDatabaseManager;
import com.nsukstudio.newsimukraft.config.NSUKConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 城市核心管理器 —— 地块网格管理、全局状态同步的调度中枢
 *
 * 设计要点：
 *   - 内存中以 ConcurrentHashMap 维护所有激活城市的运行时数据
 *   - 持久化操作委托给 IDatabaseManager，避免城市模块直接依赖 SQLite
 *   - 地块坐标以地块边长对齐到世界坐标（plotKey = "x/z" 地块单位）
 */
public class CityManager implements ICityManager {

    /** 所有激活中的城市，key = cityId */
    private final ConcurrentHashMap<UUID, CityData> cities = new ConcurrentHashMap<>();

    private final IDatabaseManager db;

    public CityManager(IDatabaseManager db) {
        this.db = db;
    }

    /**
     * 服务端启动时从数据库加载所有城市
     * 由 NewSimukraft.onServerStarting 调用
     */
    public void loadAll() {
        List<UUID> cityIds = db.loadAllCityIds();
        for (UUID id : cityIds) {
            Map<String, Object> data = db.loadCity(id);
            if (data.isEmpty()) continue;
            try {
                CityData city = deserializeCity(id, data);
                cities.put(id, city);
            } catch (Exception e) {
                NewSimukraft.LOGGER.error("[City] 加载城市失败: {}", id, e);
            }
        }
        NewSimukraft.LOGGER.info("[City] 加载了 {} 座城市", cities.size());
    }

    // ===================== ICityManager 接口实现 =====================

    @Override
    public ICityPlot getPlot(BlockPos pos) {
        // 遍历所有城市查找包含该坐标的地块（通常城市数量不大，可接受）
        int plotSize = NSUKConfig.CITY_PLOT_SIZE.get();
        int px = Math.floorDiv(pos.getX(), plotSize);
        int pz = Math.floorDiv(pos.getZ(), plotSize);
        String key = px + "/" + pz;

        for (CityData city : cities.values()) {
            CityPlot plot = city.getPlot(key);
            if (plot != null) return plot;
        }
        return null;
    }

    @Override
    public boolean createCity(UUID playerId, BlockPos origin, ServerLevel level) {
        UUID cityId = UUID.randomUUID();
        // MC 26.2：ResourceKey.location() 已改名为 identifier()
        String dimKey = level.dimension().identifier().toString();
        CityData city = new CityData(cityId, playerId, origin, dimKey);

        // 写入内存
        cities.put(cityId, city);

        // 持久化
        Map<String, Object> data = serializeCity(city);
        db.saveCity(cityId, data);

        NewSimukraft.LOGGER.info("[City] 玩家 {} 建立了新城市 {}", playerId, cityId);
        return true;
    }

    @Override
    public void occupyPlot(BlockPos plotPos, IBuildingData building) {
        CityPlot plot = findPlot(plotPos);
        if (plot == null) {
            NewSimukraft.LOGGER.warn("[City] occupyPlot: 找不到地块 {}", plotPos);
            return;
        }
        plot.occupy(building);
    }

    @Override
    public void releasePlot(BlockPos plotPos) {
        CityPlot plot = findPlot(plotPos);
        if (plot != null) plot.release();
    }

    @Override
    public int getTotalPopulation(UUID cityId) {
        CityData city = cities.get(cityId);
        return city == null ? 0 : city.getPopulation();
    }

    @Override
    public void updatePopulation(UUID cityId, int delta) {
        CityData city = cities.get(cityId);
        if (city == null) return;
        city.addPopulation(delta);
        // 同步城市数据到数据库
        db.saveCity(cityId, serializeCity(city));
    }

    @Override
    public void syncToClients(ServerLevel level) {
        // 后续接入网络模块，向全体玩家广播城市状态包
        // PacketDistributor.sendToAllPlayers(new CitySyncPacket(...));
        NewSimukraft.LOGGER.debug("[City] 城市状态同步（网络模块待接入）");
    }

    @Override
    public boolean isInsideCity(BlockPos pos, UUID cityId) {
        CityData city = cities.get(cityId);
        if (city == null) return false;

        int plotSize = NSUKConfig.CITY_PLOT_SIZE.get();
        int px = Math.floorDiv(pos.getX(), plotSize);
        int pz = Math.floorDiv(pos.getZ(), plotSize);
        return city.getPlot(px + "/" + pz) != null;
    }

    @Override
    public List<UUID> getAllActiveCities() {
        return new ArrayList<>(cities.keySet());
    }

    // ===================== 内部工具 =====================

    /**
     * 根据世界坐标找到对应地块
     * 如果地块尚未分配，自动创建（懒加载策略）
     */
    private CityPlot findPlot(BlockPos pos) {
        int plotSize = NSUKConfig.CITY_PLOT_SIZE.get();
        int px = Math.floorDiv(pos.getX(), plotSize);
        int pz = Math.floorDiv(pos.getZ(), plotSize);
        String key = px + "/" + pz;

        for (CityData city : cities.values()) {
            CityPlot plot = city.getPlot(key);
            if (plot != null) return plot;
        }
        return null;
    }

    /**
     * 向城市添加地块（建立城市时或城市扩张时调用）
     * 仅供 CityManager 内部及建造模块通过此管理器调用
     */
    public CityPlot getOrCreatePlot(UUID cityId, BlockPos worldPos) {
        CityData city = cities.get(cityId);
        if (city == null) return null;

        int plotSize = NSUKConfig.CITY_PLOT_SIZE.get();
        int px = Math.floorDiv(worldPos.getX(), plotSize);
        int pz = Math.floorDiv(worldPos.getZ(), plotSize);
        String key = px + "/" + pz;

        return city.getAllPlots().computeIfAbsent(key, k -> {
            BlockPos plotOrigin = new BlockPos(px * plotSize, worldPos.getY(), pz * plotSize);
            return new CityPlot(plotOrigin, cityId);
        });
    }

    /** 获取城市数据（供经济/建造等模块内部使用） */
    public CityData getCityData(UUID cityId) {
        return cities.get(cityId);
    }

    // ===================== 序列化辅助 =====================

    private Map<String, Object> serializeCity(CityData city) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("city_id", city.getCityId().toString());
        m.put("owner", city.getOwnerPlayerId().toString());
        m.put("origin_x", city.getOriginBlock().getX());
        m.put("origin_y", city.getOriginBlock().getY());
        m.put("origin_z", city.getOriginBlock().getZ());
        m.put("dimension", city.getDimensionKey());
        m.put("population", city.getPopulation());
        m.put("active", city.isActive());
        return m;
    }

    private CityData deserializeCity(UUID id, Map<String, Object> m) {
        UUID owner = UUID.fromString(String.valueOf(m.get("owner")));
        int ox = ((Number) m.getOrDefault("origin_x", 0)).intValue();
        int oy = ((Number) m.getOrDefault("origin_y", 64)).intValue();
        int oz = ((Number) m.getOrDefault("origin_z", 0)).intValue();
        String dim = String.valueOf(m.getOrDefault("dimension", "minecraft:overworld"));
        CityData city = new CityData(id, owner, new BlockPos(ox, oy, oz), dim);
        city.addPopulation(((Number) m.getOrDefault("population", 0)).intValue());
        city.setActive((Boolean) m.getOrDefault("active", true));
        return city;
    }
}
