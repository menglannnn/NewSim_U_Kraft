package com.nsukstudio.newsimukraft.building;

import com.nsukstudio.newsimukraft.NewSimukraft;
import com.nsukstudio.newsimukraft.api.building.IBuildingData;
import com.nsukstudio.newsimukraft.api.building.IBuildingManager;
import com.nsukstudio.newsimukraft.api.database.IDatabaseManager;
import com.nsukstudio.newsimukraft.api.economy.IEconomyManager;
import com.nsukstudio.newsimukraft.api.registry.BuildingTypeDef;
import com.nsukstudio.newsimukraft.city.CityManager;
import com.nsukstudio.newsimukraft.config.NSUKConfig;
import com.nsukstudio.newsimukraft.registry.ModRegistryImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 建造管理器 —— 建筑放置/拆除/升级/移动的全流程调度器
 *
 * 职责边界：
 *   - 碰撞检测由本模块负责，不依赖 MC 物理引擎
 *   - 经济校验委托给 IEconomyManager，地块状态委托给 CityManager
 *   - 建筑实例仅存在于内存 + 数据库，不在 MC 世界放置方块（由上层逻辑决定）
 */
public class BuildingManager implements IBuildingManager {

    /** 所有建筑数据，key = buildingId */
    private final ConcurrentHashMap<UUID, BuildingData> buildings = new ConcurrentHashMap<>();

    /** 预览状态，key = playerId */
    private final ConcurrentHashMap<UUID, String> previewPlayers = new ConcurrentHashMap<>();

    private final CityManager cityManager;
    private final IEconomyManager economy;
    private final IDatabaseManager db;

    public BuildingManager(CityManager cityManager, IEconomyManager economy, IDatabaseManager db) {
        this.cityManager = cityManager;
        this.economy = economy;
        this.db = db;
    }

    /**
     * 服务端启动时从数据库加载所有建筑
     * 由外部调用（如 NewSimukraft.onServerStarting），或按需懒加载
     */
    public void loadBuildingsForCity(UUID cityId) {
        List<Map<String, Object>> list = db.loadBuildingsForCity(cityId);
        for (Map<String, Object> data : list) {
            try {
                BuildingData bd = deserialize(data);
                buildings.put(bd.getBuildingId(), bd);
            } catch (Exception e) {
                NewSimukraft.LOGGER.error("[Building] 加载建筑数据失败", e);
            }
        }
    }

    // ===================== IBuildingManager 接口实现 =====================

    @Override
    public boolean placeBuilding(UUID playerId, String typeId, BlockPos pos, ServerLevel level) {
        // 1. 查询类型定义
        Optional<BuildingTypeDef> defOpt = ModRegistryImpl.INSTANCE.getBuildingType(typeId);
        if (defOpt.isEmpty()) {
            NewSimukraft.LOGGER.warn("[Building] 未知建筑类型: {}", typeId);
            return false;
        }
        BuildingTypeDef def = defOpt.get();

        // 2. 确定所属城市（通过坐标反查）
        UUID cityId = resolveCityId(pos, level);
        if (cityId == null) {
            NewSimukraft.LOGGER.debug("[Building] 放置位置不在任何城市范围内: {}", pos);
            return false;
        }

        // 3. 检查城市建筑数量上限
        long cityBuildingCount = buildings.values().stream()
                .filter(b -> cityId.equals(b.getCityId())).count();
        if (cityBuildingCount >= NSUKConfig.MAX_BUILDINGS_PER_CITY.get()) {
            NewSimukraft.LOGGER.debug("[Building] 城市建筑已达上限: {}", cityId);
            return false;
        }

        // 4. 碰撞检测
        if (checkCollision(pos, def.getBaseFootprint(), cityId)) {
            NewSimukraft.LOGGER.debug("[Building] 碰撞检测不通过: {}", pos);
            return false;
        }

        // 5. 经济校验 —— 扣除建造费用
        if (!economy.withdraw(cityId, def.getBaseCost(), "build:" + typeId)) {
            NewSimukraft.LOGGER.debug("[Building] 资金不足，无法建造: {}", typeId);
            return false;
        }

        // 6. 创建建筑实例
        UUID buildingId = UUID.randomUUID();
        BuildingData building = new BuildingData(buildingId, typeId, cityId, pos, def.getBaseFootprint());
        buildings.put(buildingId, building);

        // 7. 占用地块
        cityManager.occupyPlot(pos, building);

        // 8. 持久化
        db.saveBuilding(buildingId, serialize(building));

        NewSimukraft.LOGGER.info("[Building] 建造成功: {} at {} (城市 {})", typeId, pos, cityId);
        return true;
    }

    @Override
    public boolean removeBuilding(UUID buildingId, UUID playerId, ServerLevel level) {
        BuildingData building = buildings.get(buildingId);
        if (building == null) return false;

        // 释放地块
        cityManager.releasePlot(building.getPosition());

        // 从内存移除
        buildings.remove(buildingId);

        // 从数据库删除
        db.deleteBuilding(buildingId);

        NewSimukraft.LOGGER.info("[Building] 拆除建筑: {}", buildingId);
        return true;
    }

    @Override
    public boolean upgradeBuilding(UUID buildingId, UUID playerId) {
        BuildingData building = buildings.get(buildingId);
        if (building == null) return false;

        Optional<BuildingTypeDef> defOpt = ModRegistryImpl.INSTANCE.getBuildingType(building.getBuildingTypeId());
        if (defOpt.isEmpty()) return false;
        BuildingTypeDef def = defOpt.get();

        if (building.getLevel() >= def.getMaxLevel()) {
            NewSimukraft.LOGGER.debug("[Building] 建筑已满级: {}", buildingId);
            return false;
        }

        long cost = def.getUpgradeCost(building.getLevel());
        if (!economy.withdraw(building.getCityId(), cost, "upgrade:" + buildingId)) {
            return false;
        }

        building.upgrade();
        db.saveBuilding(buildingId, serialize(building));
        NewSimukraft.LOGGER.info("[Building] 升级建筑 {} 至 {} 级", buildingId, building.getLevel());
        return true;
    }

    @Override
    public boolean moveBuilding(UUID buildingId, BlockPos newPos, UUID playerId, ServerLevel level) {
        BuildingData building = buildings.get(buildingId);
        if (building == null) return false;

        // 检测新位置碰撞
        if (checkCollision(newPos, building.getFootprint(), building.getCityId())) {
            return false;
        }

        // 释放旧地块，占用新地块
        cityManager.releasePlot(building.getPosition());

        // 创建新位置的建筑数据（position 是 final，需重建对象）
        BuildingData moved = new BuildingData(
                building.getBuildingId(), building.getBuildingTypeId(),
                building.getCityId(), newPos, building.getFootprint());
        moved.setLevel(building.getLevel());
        moved.setOperational(building.isOperational());

        buildings.put(buildingId, moved);
        cityManager.occupyPlot(newPos, moved);
        db.saveBuilding(buildingId, serialize(moved));
        return true;
    }

    @Override
    public boolean checkCollision(BlockPos pos, int[] footprint, UUID cityId) {
        int plotSize = NSUKConfig.CITY_PLOT_SIZE.get();
        // 计算待放置建筑占用的所有地块坐标
        Set<String> targetPlots = new HashSet<>();
        for (int dx = 0; dx < footprint[0]; dx++) {
            for (int dz = 0; dz < footprint[1]; dz++) {
                int px = Math.floorDiv(pos.getX() + dx * plotSize, plotSize);
                int pz = Math.floorDiv(pos.getZ() + dz * plotSize, plotSize);
                targetPlots.add(px + "/" + pz);
            }
        }

        // 检查目标地块是否已被占用
        for (BuildingData existing : buildings.values()) {
            if (!cityId.equals(existing.getCityId())) continue;
            int epx = Math.floorDiv(existing.getPosition().getX(), plotSize);
            int epz = Math.floorDiv(existing.getPosition().getZ(), plotSize);
            for (int dx = 0; dx < existing.getFootprint()[0]; dx++) {
                for (int dz = 0; dz < existing.getFootprint()[1]; dz++) {
                    if (targetPlots.contains((epx + dx) + "/" + (epz + dz))) {
                        return true; // 碰撞
                    }
                }
            }
        }
        return false;
    }

    @Override
    public IBuildingData getBuilding(UUID buildingId) {
        return buildings.get(buildingId);
    }

    @Override
    public void startPreview(UUID playerId, String typeId) {
        previewPlayers.put(playerId, typeId);
    }

    @Override
    public void stopPreview(UUID playerId) {
        previewPlayers.remove(playerId);
    }

    // ===================== 内部工具 =====================

    /**
     * 通过世界坐标反查所属城市ID
     * 遍历所有城市判断坐标是否在城市范围内
     */
    private UUID resolveCityId(BlockPos pos, ServerLevel level) {
        for (UUID cityId : cityManager.getAllActiveCities()) {
            if (cityManager.isInsideCity(pos, cityId)) {
                return cityId;
            }
        }
        return null;
    }

    private Map<String, Object> serialize(BuildingData bd) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("building_id", bd.getBuildingId().toString());
        m.put("city_id", bd.getCityId().toString());
        m.put("type_id", bd.getBuildingTypeId());
        m.put("pos_x", bd.getPosition().getX());
        m.put("pos_y", bd.getPosition().getY());
        m.put("pos_z", bd.getPosition().getZ());
        m.put("footprint_w", bd.getFootprint()[0]);
        m.put("footprint_d", bd.getFootprint()[1]);
        m.put("level", bd.getLevel());
        m.put("operational", bd.isOperational());
        return m;
    }

    private BuildingData deserialize(Map<String, Object> m) {
        UUID id = UUID.fromString(String.valueOf(m.get("building_id")));
        UUID cityId = UUID.fromString(String.valueOf(m.get("city_id")));
        String typeId = String.valueOf(m.get("type_id"));
        int x = ((Number) m.get("pos_x")).intValue();
        int y = ((Number) m.get("pos_y")).intValue();
        int z = ((Number) m.get("pos_z")).intValue();
        int fw = ((Number) m.get("footprint_w")).intValue();
        int fd = ((Number) m.get("footprint_d")).intValue();
        int lv = ((Number) m.getOrDefault("level", 1)).intValue();
        boolean op = (Boolean) m.getOrDefault("operational", true);

        BuildingData bd = new BuildingData(id, typeId, cityId, new BlockPos(x, y, z), new int[]{fw, fd});
        bd.setLevel(lv);
        bd.setOperational(op);
        return bd;
    }
}
