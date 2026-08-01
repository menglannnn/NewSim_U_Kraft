package com.nsukstudio.newsimukraft;

import com.nsukstudio.newsimukraft.api.building.IBuildingManager;
import com.nsukstudio.newsimukraft.api.core.ICityManager;
import com.nsukstudio.newsimukraft.api.database.IDatabaseManager;
import com.nsukstudio.newsimukraft.api.economy.IEconomyManager;
import com.nsukstudio.newsimukraft.api.npc.INpcManager;
import com.nsukstudio.newsimukraft.api.sound.ISoundManager;

/**
 * 全局模块访问器 —— 提供各模块的静态获取入口
 *
 * 所有模块均在服务端启动后才可用，客户端侧仅音效/渲染模块可用。
 * 使用前务必判空：ModuleHolder.getCity() != null
 */
public final class ModuleHolder {

    private static IDatabaseManager database;
    private static ICityManager city;
    private static IEconomyManager economy;
    private static IBuildingManager building;
    private static INpcManager npc;
    private static ISoundManager sound;

    private ModuleHolder() {}

    public static IDatabaseManager getDatabase() { return database; }
    public static ICityManager getCity() { return city; }
    public static IEconomyManager getEconomy() { return economy; }
    public static IBuildingManager getBuilding() { return building; }
    public static INpcManager getNpc() { return npc; }
    public static ISoundManager getSound() { return sound; }

    static void setDatabase(IDatabaseManager m) { database = m; }
    static void setCity(ICityManager m) { city = m; }
    static void setEconomy(IEconomyManager m) { economy = m; }
    static void setBuilding(IBuildingManager m) { building = m; }
    static void setNpc(INpcManager m) { npc = m; }
    static void setSound(ISoundManager m) { sound = m; }
}
