package com.nsukstudio.newsimukraft.npc;

import com.nsukstudio.newsimukraft.NewSimukraft;
import com.nsukstudio.newsimukraft.api.database.IDatabaseManager;
import com.nsukstudio.newsimukraft.api.npc.INpcData;
import com.nsukstudio.newsimukraft.api.npc.INpcManager;
import com.nsukstudio.newsimukraft.api.registry.NpcTypeDef;
import com.nsukstudio.newsimukraft.city.CityManager;
import com.nsukstudio.newsimukraft.config.NSUKConfig;
import com.nsukstudio.newsimukraft.registry.ModRegistryImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NPC 管理器 —— NPC 生命周期、行为状态机、寻路调度
 *
 * 行为状态机（每 tick 驱动）：
 *   idle → traveling（有工作地/家时开始移动）
 *   traveling → working/sleeping（到达目的地时）
 *   working/sleeping → idle（休息/下班时）
 *
 * 寻路：当前版本使用简化直线寻路（每 tick 向目标移动一步），
 * 后续可替换为 MC PathFinder 接入。
 */
public class NpcManager implements INpcManager {

    /** 所有 NPC 数据，key = npcId */
    private final ConcurrentHashMap<UUID, NpcData> npcs = new ConcurrentHashMap<>();

    /** 各 NPC 的寻路目标，key = npcId */
    private final ConcurrentHashMap<UUID, BlockPos> pathTargets = new ConcurrentHashMap<>();

    private final CityManager cityManager;
    private final IDatabaseManager db;

    public NpcManager(CityManager cityManager, IDatabaseManager db) {
        this.cityManager = cityManager;
        this.db = db;
    }

    // ===================== INpcManager 接口实现 =====================

    @Override
    public UUID spawnNpc(UUID cityId, String npcType, ServerLevel level) {
        // 检查类型是否注册
        Optional<NpcTypeDef> defOpt = ModRegistryImpl.INSTANCE.getNpcType("newsimukraft:" + npcType);
        if (defOpt.isEmpty()) {
            NewSimukraft.LOGGER.warn("[NPC] 未知 NPC 类型: {}", npcType);
            return null;
        }
        NpcTypeDef def = defOpt.get();

        // 检查单城市 NPC 上限（配置 + 类型双重限制）
        long cityNpcCount = npcs.values().stream()
                .filter(n -> cityId.equals(n.getCityId())).count();
        if (cityNpcCount >= NSUKConfig.MAX_NPC_PER_CITY.get()
                || cityNpcCount >= def.getMaxPerCity()) {
            NewSimukraft.LOGGER.debug("[NPC] 城市 {} NPC 已达上限", cityId);
            return null;
        }

        // 以城市原点为生成坐标（后续可改为随机空地）
        BlockPos spawnPos = Optional.ofNullable(cityManager.getCityData(cityId))
                .map(c -> c.getOriginBlock())
                .orElse(BlockPos.ZERO);

        UUID npcId = UUID.randomUUID();
        NpcData npc = new NpcData(npcId, npcType, cityId, spawnPos);
        npcs.put(npcId, npc);

        // 持久化
        db.saveNpc(npcId, serialize(npc));

        // 更新城市人口
        cityManager.updatePopulation(cityId, 1);

        NewSimukraft.LOGGER.debug("[NPC] 生成 {} ({}) in 城市 {}", npcType, npcId, cityId);
        return npcId;
    }

    @Override
    public void despawnNpc(UUID npcId, ServerLevel level) {
        NpcData npc = npcs.remove(npcId);
        if (npc == null) return;

        pathTargets.remove(npcId);
        db.deleteNpc(npcId);
        cityManager.updatePopulation(npc.getCityId(), -1);
        NewSimukraft.LOGGER.debug("[NPC] 移除 NPC {}", npcId);
    }

    @Override
    public List<INpcData> getNpcsInCity(UUID cityId) {
        List<INpcData> result = new ArrayList<>();
        for (NpcData n : npcs.values()) {
            if (cityId.equals(n.getCityId())) result.add(n);
        }
        return result;
    }

    @Override
    public INpcData getNpc(UUID npcId) {
        return npcs.get(npcId);
    }

    @Override
    public void pathfindTo(UUID npcId, BlockPos target) {
        if (npcs.containsKey(npcId)) {
            pathTargets.put(npcId, target);
            NpcData npc = npcs.get(npcId);
            if (npc != null) npc.setBehaviorState("traveling");
        }
    }

    @Override
    public void tickAll(ServerLevel level) {
        for (NpcData npc : npcs.values()) {
            tickNpc(npc, level);
        }
    }

    @Override
    public int getOccupantCount(UUID buildingId) {
        int count = 0;
        for (NpcData n : npcs.values()) {
            if (buildingId.equals(n.getHomeId()) || buildingId.equals(n.getWorkplaceId())) {
                count++;
            }
        }
        return count;
    }

    // ===================== 内部行为驱动 =====================

    /**
     * 驱动单个 NPC 的行为状态机（每 tick 调用）
     * 简化版：直线移动，每 tick 步进 1 格
     */
    private void tickNpc(NpcData npc, ServerLevel level) {
        BlockPos target = pathTargets.get(npc.getNpcId());

        switch (npc.getBehaviorState()) {
            case "traveling" -> {
                if (target == null) {
                    npc.setBehaviorState("idle");
                    return;
                }
                BlockPos cur = npc.getPosition();
                if (cur.closerThan(target, 1.5)) {
                    // 到达目标
                    npc.setPosition(target);
                    pathTargets.remove(npc.getNpcId());
                    npc.setBehaviorState(resolveArrivalState(npc));
                } else {
                    // 简化直线步进（每 tick 移动 1 格）
                    npc.setPosition(stepTowards(cur, target));
                }
            }
            case "idle" -> {
                // 空闲时自动派发任务（早晨去工作，晚上回家）
                dispatchTask(npc, level);
            }
            // working / sleeping 状态不需要额外处理（等待计时）
        }
    }

    /** 根据目标建筑判断到达后的状态 */
    private String resolveArrivalState(NpcData npc) {
        BlockPos target = pathTargets.get(npc.getNpcId());
        if (target == null) return "idle";
        // 如果目标是工作地点则进入 working，否则 sleeping
        if (npc.getWorkplaceId() != null) return "working";
        if (npc.getHomeId() != null) return "sleeping";
        return "idle";
    }

    /**
     * 简单任务派发：优先派发去工作，无工作则回家
     * 实际游戏日时间判断可通过 level.getDayTime() 实现，此处简化
     */
    private void dispatchTask(NpcData npc, ServerLevel level) {
        // MC 26.2：Level.getGameTime() % 24000 近似当前日内时间（0-11999 白天，12000-23999 夜晚）
        long dayTime = level.getGameTime() % 24000L;
        if (dayTime >= 0 && dayTime < 12000 && npc.getWorkplaceId() != null) {
            // 白天去工作
            pathfindTo(npc.getNpcId(), workplacePos(npc));
        } else if (npc.getHomeId() != null) {
            // 晚上回家
            pathfindTo(npc.getNpcId(), homePos(npc));
        }
    }

    /** 直线步进辅助（每次移动一格） */
    private BlockPos stepTowards(BlockPos from, BlockPos to) {
        int dx = Integer.signum(to.getX() - from.getX());
        int dz = Integer.signum(to.getZ() - from.getZ());
        return from.offset(dx, 0, dz);
    }

    /** 从建筑 ID 反查坐标（通过 BuildingManager 中的数据，此处简化返回 ZERO） */
    private BlockPos workplacePos(NpcData npc) {
        // 实际实现：ModuleHolder.getBuilding().getBuilding(npc.getWorkplaceId()).getPosition()
        return BlockPos.ZERO;
    }

    private BlockPos homePos(NpcData npc) {
        return BlockPos.ZERO;
    }

    // ===================== 序列化辅助 =====================

    private Map<String, Object> serialize(NpcData npc) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("npc_id", npc.getNpcId().toString());
        m.put("city_id", npc.getCityId().toString());
        m.put("npc_type", npc.getNpcType());
        m.put("pos_x", npc.getPosition().getX());
        m.put("pos_y", npc.getPosition().getY());
        m.put("pos_z", npc.getPosition().getZ());
        if (npc.getHomeId() != null) m.put("home_id", npc.getHomeId().toString());
        if (npc.getWorkplaceId() != null) m.put("workplace_id", npc.getWorkplaceId().toString());
        m.put("behavior", npc.getBehaviorState());
        return m;
    }
}
