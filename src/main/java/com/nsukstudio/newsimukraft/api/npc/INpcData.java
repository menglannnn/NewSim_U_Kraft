package com.nsukstudio.newsimukraft.api.npc;

import net.minecraft.core.BlockPos;
import java.util.UUID;

/**
 * NPC 数据接口 —— 居民/商贩/工人等所有 NPC 的公共属性
 */
public interface INpcData {

    /** 获取 NPC 唯一ID */
    UUID getNpcId();

    /** 获取 NPC 类型（resident/merchant/worker） */
    String getNpcType();

    /** 获取所属城市ID */
    UUID getCityId();

    /** 获取当前所在坐标 */
    BlockPos getPosition();

    /** 获取 NPC 居住的住宅建筑ID，无家游民返回 null */
    UUID getHomeId();

    /** 获取 NPC 工作的建筑ID，失业返回 null */
    UUID getWorkplaceId();

    /** 设置住宅（住宅模块调用） */
    void setHome(UUID buildingId);

    /** 设置工作地点（工业/商业模块调用） */
    void setWorkplace(UUID buildingId);

    /** 获取 NPC 当前行为状态（idle/working/traveling/sleeping） */
    String getBehaviorState();

    /** 更新行为状态 */
    void setBehaviorState(String state);
}
