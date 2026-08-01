package com.nsukstudio.newsimukraft.api.building;

import net.minecraft.core.BlockPos;
import java.util.UUID;

/**
 * 建筑数据接口 —— 所有建筑类型的公共属性
 */
public interface IBuildingData {

    /** 获取建筑唯一ID */
    UUID getBuildingId();

    /** 获取建筑类型标识（对应注册管理模块的类型ID） */
    String getBuildingTypeId();

    /** 获取建筑放置位置（左下角） */
    BlockPos getPosition();

    /** 获取建筑所属城市ID */
    UUID getCityId();

    /** 获取建筑当前等级（1起步） */
    int getLevel();

    /** 升级建筑（等级+1，触发属性重算） */
    void upgrade();

    /** 获取建筑占用的地块范围（宽x深，单位：地块） */
    int[] getFootprint();

    /** 判断建筑当前是否正常运行（非停工/被破坏状态） */
    boolean isOperational();

    /** 设置运行状态 */
    void setOperational(boolean operational);
}
