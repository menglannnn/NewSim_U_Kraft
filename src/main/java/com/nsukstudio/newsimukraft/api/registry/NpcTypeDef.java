package com.nsukstudio.newsimukraft.api.registry;

/**
 * NPC 类型定义 —— 描述一种 NPC 角色的静态属性
 *
 * 使用方式：
 *   在模组初始化阶段通过 IModRegistry.registerNpcType() 注册。
 *   可通过继承扩展特定类型（如 ResidentNpcTypeDef）。
 */
public class NpcTypeDef {

    private final String typeId;       // 唯一类型ID，如 "newsimukraft:resident"
    private final String displayName;  // 显示名称
    private final int maxPerCity;      // 单城市最大数量上限（防止 OOM）
    private final float moveSpeed;     // 移动速度（格/秒）

    public NpcTypeDef(String typeId, String displayName, int maxPerCity, float moveSpeed) {
        this.typeId = typeId;
        this.displayName = displayName;
        this.maxPerCity = maxPerCity;
        this.moveSpeed = moveSpeed;
    }

    public String getTypeId() { return typeId; }
    public String getDisplayName() { return displayName; }
    public int getMaxPerCity() { return maxPerCity; }
    public float getMoveSpeed() { return moveSpeed; }
}
