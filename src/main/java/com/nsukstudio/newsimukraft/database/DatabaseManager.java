package com.nsukstudio.newsimukraft.database;

import com.nsukstudio.newsimukraft.NewSimukraft;
import com.nsukstudio.newsimukraft.api.database.IDatabaseManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * SQLite 数据库管理器 —— 负责所有模块的持久化读写
 *
 * 线程安全：使用 ReadWriteLock 保证并发读写安全。
 * 读操作允许并发，写操作互斥。
 */
public class DatabaseManager implements IDatabaseManager {

    /** SQLite 数据库文件名 */
    private static final String DB_FILENAME = "newsimukraft.db";

    /** 读写锁：允许并发读，写时互斥 */
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    private Path dbPath;
    private Path backupDir;
    private Connection connection;

    @Override
    public void initialize(String worldSavePath) {
        try {
            // 加载 SQLite JDBC 驱动
            Class.forName("org.sqlite.JDBC");

            dbPath = Paths.get(worldSavePath, DB_FILENAME);
            backupDir = Paths.get(worldSavePath, "newsimukraft_backup");
            Files.createDirectories(backupDir);

            // 创建/打开数据库，启用 WAL 模式提升并发性能
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL");
                stmt.execute("PRAGMA foreign_keys=ON");
            }

            createTables();
            NewSimukraft.LOGGER.info("[DB] 数据库初始化完成: {}", dbPath);
        } catch (Exception e) {
            NewSimukraft.LOGGER.error("[DB] 数据库初始化失败", e);
            throw new RuntimeException("数据库初始化失败", e);
        }
    }

    /** 建表：幂等操作，表已存在则跳过 */
    private void createTables() throws SQLException {
        String[] ddl = {
            // 城市基础数据表
            """
            CREATE TABLE IF NOT EXISTS cities (
                city_id TEXT PRIMARY KEY,
                data TEXT NOT NULL,
                updated_at INTEGER NOT NULL
            )""",
            // 建筑数据表
            """
            CREATE TABLE IF NOT EXISTS buildings (
                building_id TEXT PRIMARY KEY,
                city_id TEXT NOT NULL,
                data TEXT NOT NULL,
                updated_at INTEGER NOT NULL
            )""",
            // NPC 数据表
            """
            CREATE TABLE IF NOT EXISTS npcs (
                npc_id TEXT PRIMARY KEY,
                city_id TEXT NOT NULL,
                data TEXT NOT NULL,
                updated_at INTEGER NOT NULL
            )""",
            // 财政快照表（每次结算追加一条）
            """
            CREATE TABLE IF NOT EXISTS fiscal_snapshots (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                city_id TEXT NOT NULL,
                balance INTEGER NOT NULL,
                income INTEGER NOT NULL,
                expense INTEGER NOT NULL,
                snapshot_at INTEGER NOT NULL
            )"""
        };

        try (Statement stmt = connection.createStatement()) {
            for (String sql : ddl) {
                stmt.execute(sql);
            }
        }
    }

    // ===================== 城市数据 =====================

    @Override
    public void saveCity(UUID cityId, Map<String, Object> data) {
        rwLock.writeLock().lock();
        try {
            String sql = "INSERT OR REPLACE INTO cities(city_id, data, updated_at) VALUES(?,?,?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, cityId.toString());
                ps.setString(2, mapToJson(data));
                ps.setLong(3, System.currentTimeMillis());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            NewSimukraft.LOGGER.error("[DB] 保存城市数据失败: {}", cityId, e);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    @SuppressWarnings("Null") // rs.getString 可能返回 null，jsonToMap 内部做了空值处理
    public Map<String, Object> loadCity(UUID cityId) {
        rwLock.readLock().lock();
        try {
            String sql = "SELECT data FROM cities WHERE city_id=?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, cityId.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return jsonToMap(rs.getString("data"));
                }
            }
        } catch (SQLException e) {
            NewSimukraft.LOGGER.error("[DB] 加载城市数据失败: {}", cityId, e);
        } finally {
            rwLock.readLock().unlock();
        }
        return Collections.emptyMap();
    }

    @Override
    public List<UUID> loadAllCityIds() {
        rwLock.readLock().lock();
        try {
            List<UUID> ids = new ArrayList<>();
            String sql = "SELECT city_id FROM cities";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    ids.add(UUID.fromString(rs.getString("city_id")));
                }
            }
            return ids;
        } catch (SQLException e) {
            NewSimukraft.LOGGER.error("[DB] 加载城市列表失败", e);
            return Collections.emptyList();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    // ===================== 建筑数据 =====================

    @Override
    public void saveBuilding(UUID buildingId, Map<String, Object> data) {
        rwLock.writeLock().lock();
        try {
            // city_id 必须包含在 data map 中
            String cityId = String.valueOf(data.getOrDefault("city_id", ""));
            String sql = "INSERT OR REPLACE INTO buildings(building_id, city_id, data, updated_at) VALUES(?,?,?,?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, buildingId.toString());
                ps.setString(2, cityId);
                ps.setString(3, mapToJson(data));
                ps.setLong(4, System.currentTimeMillis());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            NewSimukraft.LOGGER.error("[DB] 保存建筑数据失败: {}", buildingId, e);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public Map<String, Object> loadBuilding(UUID buildingId) {
        rwLock.readLock().lock();
        try {
            String sql = "SELECT data FROM buildings WHERE building_id=?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, buildingId.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return jsonToMap(rs.getString("data"));
                }
            }
        } catch (SQLException e) {
            NewSimukraft.LOGGER.error("[DB] 加载建筑数据失败: {}", buildingId, e);
        } finally {
            rwLock.readLock().unlock();
        }
        return Collections.emptyMap();
    }

    @Override
    public List<Map<String, Object>> loadBuildingsForCity(UUID cityId) {
        rwLock.readLock().lock();
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            String sql = "SELECT data FROM buildings WHERE city_id=?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, cityId.toString());
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    result.add(jsonToMap(rs.getString("data")));
                }
            }
            return result;
        } catch (SQLException e) {
            NewSimukraft.LOGGER.error("[DB] 加载城市建筑列表失败: {}", cityId, e);
            return Collections.emptyList();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public void deleteBuilding(UUID buildingId) {
        rwLock.writeLock().lock();
        try {
            String sql = "DELETE FROM buildings WHERE building_id=?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, buildingId.toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            NewSimukraft.LOGGER.error("[DB] 删除建筑数据失败: {}", buildingId, e);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    // ===================== NPC 数据 =====================

    @Override
    public void saveNpc(UUID npcId, Map<String, Object> data) {
        rwLock.writeLock().lock();
        try {
            String cityId = String.valueOf(data.getOrDefault("city_id", ""));
            String sql = "INSERT OR REPLACE INTO npcs(npc_id, city_id, data, updated_at) VALUES(?,?,?,?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, npcId.toString());
                ps.setString(2, cityId);
                ps.setString(3, mapToJson(data));
                ps.setLong(4, System.currentTimeMillis());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            NewSimukraft.LOGGER.error("[DB] 保存 NPC 数据失败: {}", npcId, e);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public Map<String, Object> loadNpc(UUID npcId) {
        rwLock.readLock().lock();
        try {
            String sql = "SELECT data FROM npcs WHERE npc_id=?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, npcId.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return jsonToMap(rs.getString("data"));
                }
            }
        } catch (SQLException e) {
            NewSimukraft.LOGGER.error("[DB] 加载 NPC 数据失败: {}", npcId, e);
        } finally {
            rwLock.readLock().unlock();
        }
        return Collections.emptyMap();
    }

    @Override
    public void deleteNpc(UUID npcId) {
        rwLock.writeLock().lock();
        try {
            String sql = "DELETE FROM npcs WHERE npc_id=?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, npcId.toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            NewSimukraft.LOGGER.error("[DB] 删除 NPC 数据失败: {}", npcId, e);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    // ===================== 财政数据 =====================

    @Override
    public void saveFiscalSnapshot(UUID cityId, long balance, long income, long expense) {
        rwLock.writeLock().lock();
        try {
            String sql = "INSERT INTO fiscal_snapshots(city_id,balance,income,expense,snapshot_at) VALUES(?,?,?,?,?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, cityId.toString());
                ps.setLong(2, balance);
                ps.setLong(3, income);
                ps.setLong(4, expense);
                ps.setLong(5, System.currentTimeMillis());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            NewSimukraft.LOGGER.error("[DB] 保存财政快照失败: {}", cityId, e);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public Map<String, Long> loadFiscalSnapshot(UUID cityId) {
        rwLock.readLock().lock();
        try {
            // 取最新一条快照
            String sql = "SELECT balance,income,expense FROM fiscal_snapshots WHERE city_id=? ORDER BY snapshot_at DESC LIMIT 1";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, cityId.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Map<String, Long> snap = new HashMap<>();
                    snap.put("balance", rs.getLong("balance"));
                    snap.put("income", rs.getLong("income"));
                    snap.put("expense", rs.getLong("expense"));
                    return snap;
                }
            }
        } catch (SQLException e) {
            NewSimukraft.LOGGER.error("[DB] 加载财政快照失败: {}", cityId, e);
        } finally {
            rwLock.readLock().unlock();
        }
        return Collections.emptyMap();
    }

    // ===================== 备份与恢复 =====================

    @Override
    public void backup(String backupTag) {
        rwLock.writeLock().lock();
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path dest = backupDir.resolve(backupTag + "_" + timestamp + ".db");
            Files.copy(dbPath, dest, StandardCopyOption.REPLACE_EXISTING);
            NewSimukraft.LOGGER.info("[DB] 备份完成: {}", dest);

            // 保留最近 10 份备份，防止磁盘占用过高
            pruneOldBackups(10);
        } catch (IOException e) {
            NewSimukraft.LOGGER.error("[DB] 备份失败", e);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    @SuppressWarnings("null") // File::lastModified 的 @Nonnull 类型安全警告，备份文件数组元素确认非空
    public boolean restore(String backupTag) {
        rwLock.writeLock().lock();
        try {
            File[] files = backupDir.toFile().listFiles(
                    f -> f.getName().startsWith(backupTag) && f.getName().endsWith(".db"));
            if (files == null || files.length == 0) {
                NewSimukraft.LOGGER.warn("[DB] 未找到备份文件: {}", backupTag);
                return false;
            }
            // 选最新的备份文件
            Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
            close();
            Files.copy(files[0].toPath(), dbPath, StandardCopyOption.REPLACE_EXISTING);
            initialize(dbPath.getParent().toString());
            NewSimukraft.LOGGER.info("[DB] 恢复完成: {}", files[0].getName());
            return true;
        } catch (IOException e) {
            NewSimukraft.LOGGER.error("[DB] 恢复失败", e);
            return false;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                NewSimukraft.LOGGER.info("[DB] 数据库连接已关闭");
            }
        } catch (SQLException e) {
            NewSimukraft.LOGGER.error("[DB] 关闭数据库失败", e);
        }
    }

    // ===================== 内部工具 =====================

    @SuppressWarnings("null") // File::lastModified 的 @Nonnull 类型安全警告，File 数组元素确认非空
    private void pruneOldBackups(int maxKeep) {
        File[] files = backupDir.toFile().listFiles(f -> f.getName().endsWith(".db"));
        if (files == null || files.length <= maxKeep) return;
        Arrays.sort(files, Comparator.comparingLong(File::lastModified));
        for (int i = 0; i < files.length - maxKeep; i++) {
            if (!files[i].delete()) {
                NewSimukraft.LOGGER.warn("[DB] 无法删除旧备份: {}", files[i].getName());
            }
        }
    }

    /**
     * 极简 Map→JSON 序列化（仅支持 String/Number/Boolean/null 值，不引入第三方 JSON 库）
     * 若后续需要复杂嵌套结构，可替换为 Gson（NeoForge 已携带）
     */
    static String mapToJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) return "{}";
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append('"').append(e.getKey().replace("\"", "\\\"")).append("\":");
            Object v = e.getValue();
            if (v == null) {
                sb.append("null");
            } else if (v instanceof Number || v instanceof Boolean) {
                sb.append(v);
            } else {
                sb.append('"').append(v.toString().replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
            }
        }
        sb.append('}');
        return sb.toString();
    }

    /**
     * 极简 JSON→Map 反序列化（与上面的 mapToJson 对应，仅解析扁平结构）
     */
    @SuppressWarnings("Null") // jsonToMap 返回值 null 安全，确认不会返回 null
    static Map<String, Object> jsonToMap(String json) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (json == null || json.isBlank() || json.equals("{}")) return map;
        // 去掉首尾 {}
        String content = json.trim();
        if (content.startsWith("{")) content = content.substring(1);
        if (content.endsWith("}")) content = content.substring(0, content.length() - 1);

        // 逐对解析 "key":value，不支持嵌套对象
        int i = 0;
        while (i < content.length()) {
            // 跳过空白和逗号
            while (i < content.length() && (content.charAt(i) == ',' || content.charAt(i) == ' ')) i++;
            if (i >= content.length()) break;
            // 读 key
            if (content.charAt(i) != '"') { i++; continue; }
            int keyStart = i + 1;
            int keyEnd = content.indexOf('"', keyStart);
            if (keyEnd < 0) break;
            String key = content.substring(keyStart, keyEnd);
            i = keyEnd + 1;
            // 跳过 :
            while (i < content.length() && content.charAt(i) != ':') i++;
            i++;
            // 读 value
            while (i < content.length() && content.charAt(i) == ' ') i++;
            if (i >= content.length()) break;
            char c = content.charAt(i);
            if (c == '"') {
                int valEnd = i + 1;
                while (valEnd < content.length()) {
                    if (content.charAt(valEnd) == '\\') { valEnd += 2; continue; }
                    if (content.charAt(valEnd) == '"') break;
                    valEnd++;
                }
                map.put(key, content.substring(i + 1, valEnd)
                        .replace("\\\"", "\"").replace("\\\\", "\\"));
                i = valEnd + 1;
            } else if (content.startsWith("null", i)) {
                map.put(key, null);
                i += 4;
            } else if (content.startsWith("true", i)) {
                map.put(key, true);
                i += 4;
            } else if (content.startsWith("false", i)) {
                map.put(key, false);
                i += 5;
            } else {
                // 数字
                int numEnd = i;
                while (numEnd < content.length() && ",} ".indexOf(content.charAt(numEnd)) < 0) numEnd++;
                String numStr = content.substring(i, numEnd).trim();
                try {
                    if (numStr.contains(".")) {
                        map.put(key, Double.parseDouble(numStr));
                    } else {
                        map.put(key, Long.parseLong(numStr));
                    }
                } catch (NumberFormatException ex) {
                    map.put(key, numStr);
                }
                i = numEnd;
            }
        }
        return map;
    }
}
