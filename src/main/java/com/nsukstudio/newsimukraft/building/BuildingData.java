package com.nsukstudio.newsimukraft.building;

import com.nsukstudio.newsimukraft.api.building.IBuildingData;
import net.minecraft.core.BlockPos;

import java.util.UUID;

/**
 * 建筑数据实现 —— 建筑运行时状态的数据容器
 * 由 BuildingManager 创建，通过 IDatabaseManager 持久化
 */
public class BuildingData implements IBuildingData {

    private final UUID buildingId;
    private final String buildingTypeId; // 对应注册表中的类型 ID
    private final UUID cityId;
    private final BlockPos position;     // 建筑左下角世界坐标
    private final int[] footprint;       // 占地范围 [宽, 深]

    private int level;                   // 当前等级，1 起步
    private boolean operational;         // 是否正常运行

    public BuildingData(UUID buildingId, String buildingTypeId, UUID cityId,
                        BlockPos position, int[] footprint) {
        this.buildingId = buildingId;
        this.buildingTypeId = buildingTypeId;
        this.cityId = cityId;
        this.position = position;
        this.footprint = footprint;
        this.level = 1;
        this.operational = true;
    }

    @Override public UUID getBuildingId() { return buildingId; }
    @Override public String getBuildingTypeId() { return buildingTypeId; }
    @Override public BlockPos getPosition() { return position; }
    @Override public UUID getCityId() { return cityId; }
    @Override public int getLevel() { return level; }
    @Override public int[] getFootprint() { return footprint; }
    @Override public boolean isOperational() { return operational; }
    @Override public void setOperational(boolean operational) { this.operational = operational; }

    @Override
    public void upgrade() {
        level++;
    }

    public void setLevel(int level) { this.level = level; }
}
