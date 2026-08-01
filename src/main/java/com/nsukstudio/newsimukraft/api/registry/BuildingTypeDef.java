package com.nsukstudio.newsimukraft.api.registry;

/**
 * 建筑类型定义 —— 描述一种建筑的静态属性（注册时传入，运行时只读）
 *
 * 使用方式：
 *   在模组初始化阶段通过 IModRegistry.registerBuildingType() 注册，
 *   建造模块实例化建筑时通过 getBuildingType() 读取此配置。
 */
public class BuildingTypeDef {

    private final String typeId;       // 唯一类型ID
    private final String category;     // 分类：commercial / industrial / residential / other
    private final int[] baseFootprint; // 基础占地 [宽, 深]（单位：地块）
    private final int maxLevel;        // 最大升级等级
    private final long baseCost;       // 建造基础费用

    public BuildingTypeDef(String typeId, String category, int[] baseFootprint,
                           int maxLevel, long baseCost) {
        this.typeId = typeId;
        this.category = category;
        this.baseFootprint = baseFootprint;
        this.maxLevel = maxLevel;
        this.baseCost = baseCost;
    }

    public String getTypeId() { return typeId; }
    public String getCategory() { return category; }
    public int[] getBaseFootprint() { return baseFootprint; }
    public int getMaxLevel() { return maxLevel; }
    public long getBaseCost() { return baseCost; }

    /** 计算指定等级的升级费用（线性倍增，子类可重写） */
    public long getUpgradeCost(int currentLevel) {
        return baseCost * currentLevel;
    }
}
