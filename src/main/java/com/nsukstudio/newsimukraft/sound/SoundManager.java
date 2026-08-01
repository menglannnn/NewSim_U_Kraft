package com.nsukstudio.newsimukraft.sound;

import com.nsukstudio.newsimukraft.NewSimukraft;
import com.nsukstudio.newsimukraft.api.sound.ISoundManager;
import com.nsukstudio.newsimukraft.config.NSUKConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 音效管理器 —— 统筹所有音效的触发与音量管理
 *
 * 音效资源需在 sounds.json 中注册，并通过 ModRegistrations 注册 SoundEvent。
 * 循环音效（工厂轰鸣）记录在 ambientSounds 中，避免重复触发。
 *
 * 音量分级：
 *   "build" / "demolish" / "upgrade" / "npc" / "ambient"
 *   通过 setVolumeMultiplier() 分类调节，默认 1.0
 */
public class SoundManager implements ISoundManager {

    /** 各类音效的音量倍率，key = 音效分类 */
    private final ConcurrentHashMap<String, Float> volumeMultipliers = new ConcurrentHashMap<>();

    /** 当前激活的循环音效，key = 坐标字符串，防止重复触发 */
    private final ConcurrentHashMap<String, Boolean> ambientSounds = new ConcurrentHashMap<>();

    public SoundManager() {
        // 初始化各类默认音量
        volumeMultipliers.put("build", 1.0f);
        volumeMultipliers.put("demolish", 1.0f);
        volumeMultipliers.put("upgrade", 1.0f);
        volumeMultipliers.put("npc", 1.0f);
        volumeMultipliers.put("ambient", 1.0f);
    }

    @Override
    public void playBuildSound(BlockPos pos, ServerLevel level) {
        if (!NSUKConfig.ENABLE_AMBIENT_SOUND.get()) return;
        playAt(pos, level, ModSoundEvents.BUILD.get(), "build", 1.0f);
    }

    @Override
    public void playDemolishSound(BlockPos pos, ServerLevel level) {
        playAt(pos, level, ModSoundEvents.DEMOLISH.get(), "demolish", 1.0f);
    }

    @Override
    public void playUpgradeSound(BlockPos pos, ServerLevel level) {
        playAt(pos, level, ModSoundEvents.UPGRADE.get(), "upgrade", 1.0f);
    }

    @Override
    public void playNpcInteractSound(String npcType, BlockPos pos, ServerLevel level) {
        SoundEvent se = switch (npcType) {
            case "merchant" -> ModSoundEvents.NPC_MERCHANT.get();
            case "worker"   -> ModSoundEvents.NPC_WORKER.get();
            default         -> ModSoundEvents.NPC_RESIDENT.get();
        };
        playAt(pos, level, se, "npc", 0.6f);
    }

    @Override
    public void playAmbientSound(String buildingTypeId, BlockPos pos, ServerLevel level) {
        if (!NSUKConfig.ENABLE_AMBIENT_SOUND.get()) return;
        String key = posKey(pos);
        // 防止同一坐标重复注册循环音效
        if (ambientSounds.putIfAbsent(key, true) != null) return;
        playAt(pos, level, ModSoundEvents.AMBIENT_FACTORY.get(), "ambient", 0.5f);
    }

    @Override
    public void stopAmbientSound(BlockPos pos) {
        ambientSounds.remove(posKey(pos));
        // MC 端循环音效停止由客户端处理（服务端不直接停止），此处仅清除记录
    }

    @Override
    public void setVolumeMultiplier(String soundCategory, float multiplier) {
        float clamped = Math.max(0f, Math.min(1f, multiplier));
        volumeMultipliers.put(soundCategory, clamped);
        NewSimukraft.LOGGER.debug("[Sound] 音效分类 {} 音量设置为 {}", soundCategory, clamped);
    }

    // ===================== 内部工具 =====================

    private void playAt(BlockPos pos, ServerLevel level, SoundEvent sound,
                        String category, float basePitch) {
        float vol = volumeMultipliers.getOrDefault(category, 1.0f);
        if (vol <= 0f) return;
        level.playSound(null, pos, sound, SoundSource.BLOCKS, vol, basePitch);
    }

    private String posKey(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }
}
