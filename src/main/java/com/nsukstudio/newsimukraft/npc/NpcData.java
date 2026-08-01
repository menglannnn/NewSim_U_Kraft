package com.nsukstudio.newsimukraft.npc;

import com.nsukstudio.newsimukraft.api.npc.INpcData;
import net.minecraft.core.BlockPos;

import java.util.UUID;
@SuppressWarnings("Null")
/**
 * NPC 数据实现 —— 居民/商贩/工人的运行时状态容器
 * 由 NpcManager 创建和管理，字段更新均在服务端线程执行
 */
public class NpcData implements INpcData {

    private final UUID npcId;
    private final String npcType;  // "resident" / "merchant" / "worker"
    private final UUID cityId;

    private volatile BlockPos position;
    private volatile UUID homeId;       // 住宅建筑ID
    private volatile UUID workplaceId;  // 工作建筑ID
    private volatile String behaviorState; // idle / working / traveling / sleeping

    public NpcData(UUID npcId, String npcType, UUID cityId, BlockPos spawnPos) {
        this.npcId = npcId;
        this.npcType = npcType;
        this.cityId = cityId;
        this.position = spawnPos;
        this.behaviorState = "idle";
    }

    @Override public UUID getNpcId() { return npcId; }
    @Override public String getNpcType() { return npcType; }
    @Override public UUID getCityId() { return cityId; }
    @Override public BlockPos getPosition() { return position; }
    @Override public UUID getHomeId() { return homeId; }
    @Override public UUID getWorkplaceId() { return workplaceId; }
    @Override public String getBehaviorState() { return behaviorState; }

    @Override public void setHome(UUID buildingId) { this.homeId = buildingId; }
    @Override public void setWorkplace(UUID buildingId) { this.workplaceId = buildingId; }
    @Override public void setBehaviorState(String state) { this.behaviorState = state; }

    public void setPosition(BlockPos pos) { this.position = pos; }
}
