package com.nsukstudio.newsimukraft.api.database;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 数据库管理器接口 —— 所有模块持久化数据的读写入口
 * 职责：保证多模块并发读写安全、支持存档导入导出
 */
public interface IDatabaseManager {

    /** 初始化数据库连接和表结构（模组加载时调用） */
    void initialize(String worldSavePath);

    /** 关闭数据库连接（世界卸载时调用） */
    void close();

    // ---- 城市数据 ----

    /** 保存城市基础数据 */
    void saveCity(UUID cityId, Map<String, Object> data);

    /** 加载城市基础数据 */
    Map<String, Object> loadCity(UUID cityId);

    /** 加载所有城市 ID 列表 */
    List<UUID> loadAllCityIds();

    // ---- 建筑数据 ----

    /** 保存单个建筑数据 */
    void saveBuilding(UUID buildingId, Map<String, Object> data);

    /** 加载单个建筑数据 */
    Map<String, Object> loadBuilding(UUID buildingId);

    /** 加载指定城市下的所有建筑 */
    List<Map<String, Object>> loadBuildingsForCity(UUID cityId);

    /** 删除建筑记录（拆除时调用） */
    void deleteBuilding(UUID buildingId);

    // ---- NPC 数据 ----

    /** 保存 NPC 数据 */
    void saveNpc(UUID npcId, Map<String, Object> data);

    /** 加载 NPC 数据 */
    Map<String, Object> loadNpc(UUID npcId);

    /** 删除 NPC 记录 */
    void deleteNpc(UUID npcId);

    // ---- 经济数据 ----

    /** 保存城市财政快照 */
    void saveFiscalSnapshot(UUID cityId, long balance, long income, long expense);

    /** 加载最新财政快照 */
    Map<String, Long> loadFiscalSnapshot(UUID cityId);

    // ---- 通用工具 ----

    /** 执行数据备份（复制到 backup 目录） */
    void backup(String backupTag);

    /** 从备份恢复（危险操作，需用户二次确认） */
    boolean restore(String backupTag);
}
