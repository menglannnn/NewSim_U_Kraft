package com.nsukstudio.newsimukraft.api.sound;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * 音效管理器接口 —— 所有场景音效的触发唯一入口
 * 职责：音效触发、音效池管理、音量分级
 */
public interface ISoundManager {

    /** 在指定位置播放建造音效 */
    void playBuildSound(BlockPos pos, ServerLevel level);

    /** 在指定位置播放拆除音效 */
    void playDemolishSound(BlockPos pos, ServerLevel level);

    /** 播放建筑升级音效 */
    void playUpgradeSound(BlockPos pos, ServerLevel level);

    /** 播放 NPC 交互音效（传入 npcType 区分不同类型 NPC 的音色） */
    void playNpcInteractSound(String npcType, BlockPos pos, ServerLevel level);

    /** 播放建筑持续运行音效（工厂轰鸣等循环音效） */
    void playAmbientSound(String buildingTypeId, BlockPos pos, ServerLevel level);

    /** 停止指定位置的循环音效 */
    void stopAmbientSound(BlockPos pos);

    /** 设置某类音效的全局音量倍率（0.0 ~ 1.0） */
    void setVolumeMultiplier(String soundCategory, float multiplier);
}
