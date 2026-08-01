package com.nsukstudio.newsimukraft.api.economy;

import java.util.UUID;

/**
 * 经济模块接口 —— 所有资源产出/消耗的唯一入口
 * 职责：税收核算、财政收支、经济波动模拟
 */
public interface IEconomyManager {

    /** 向城市财政存入金额（建筑产出税收时调用） */
    void deposit(UUID cityId, long amount, String reason);

    /** 从城市财政扣除金额（建造/升级/维护时调用） */
    boolean withdraw(UUID cityId, long amount, String reason);

    /** 获取城市当前资金余额 */
    long getBalance(UUID cityId);

    /** 触发周期性税收结算（由服务端 tick 调用，每游戏日一次） */
    void processDailyTax(UUID cityId);

    /** 查询城市经济健康度（0-100） */
    float getEconomicHealth(UUID cityId);

    /** 注册资源产出源（建筑模块建造完成时调用） */
    void registerProducer(UUID buildingId, IResourceProducer producer);

    /** 注销资源产出源（建筑拆除时调用） */
    void unregisterProducer(UUID buildingId);

    /** 注册资源消耗源（工业建筑消耗原材料时调用） */
    void registerConsumer(UUID buildingId, IResourceConsumer consumer);

    /** 注销资源消耗源 */
    void unregisterConsumer(UUID buildingId);

    /** 获取指定城市的日收入统计 */
    long getDailyIncome(UUID cityId);

    /** 获取指定城市的日支出统计 */
    long getDailyExpense(UUID cityId);
}
