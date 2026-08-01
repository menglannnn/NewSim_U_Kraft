package com.nsukstudio.newsimukraft.api.building;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import java.util.UUID;

/**
 * 建造管理器接口 —— 建筑放置、拆除、升级、移动的唯一入口
 * 职责：碰撞检测、建造预览、权限校验、地块占用同步
 */
public interface IBuildingManager {

    /** 尝试放置建筑，返回是否成功（含碰撞检测+权限校验） */
    boolean placeBuilding(UUID playerId, String typeId, BlockPos pos, ServerLevel level);

    /** 拆除建筑，释放地块，触发资源退还逻辑 */
    boolean removeBuilding(UUID buildingId, UUID playerId, ServerLevel level);

    /** 升级建筑，校验资源是否充足 */
    boolean upgradeBuilding(UUID buildingId, UUID playerId);

    /** 移动建筑到新位置，含碰撞检测 */
    boolean moveBuilding(UUID buildingId, BlockPos newPos, UUID playerId, ServerLevel level);

    /** 检测指定位置和占地是否与现有建筑碰撞 */
    boolean checkCollision(BlockPos pos, int[] footprint, UUID cityId);

    /** 获取建筑数据 */
    IBuildingData getBuilding(UUID buildingId);

    /** 开启建造预览模式（客户端渲染用） */
    void startPreview(UUID playerId, String typeId);

    /** 关闭建造预览 */
    void stopPreview(UUID playerId);
}
