package com.nsukstudio.newsimukraft.economy;

import com.nsukstudio.newsimukraft.NewSimukraft;
import com.nsukstudio.newsimukraft.api.database.IDatabaseManager;
import com.nsukstudio.newsimukraft.api.economy.IEconomyManager;
import com.nsukstudio.newsimukraft.api.economy.IResourceConsumer;
import com.nsukstudio.newsimukraft.api.economy.IResourceProducer;
import com.nsukstudio.newsimukraft.city.CityManager;
import com.nsukstudio.newsimukraft.config.NSUKConfig;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 经济管理器 —— 税收结算、资源产出消耗、财政健康度的核心调度器
 *
 * 设计要点：
 *   - 每座城市独立维护一个 FiscalLedger（财政账本）
 *   - 产出/消耗源使用 ConcurrentHashMap 保证并发注册安全
 *   - 税收结算由服务端 tick 驱动，周期由 NSUKConfig.TAX_CYCLE_TICKS 控制
 */
public class EconomyManager implements IEconomyManager {

    /** 每座城市的财政账本，key = cityId */
    private final ConcurrentHashMap<UUID, FiscalLedger> ledgers = new ConcurrentHashMap<>();

    /** 所有激活的资源产出源，key = buildingId */
    private final ConcurrentHashMap<UUID, IResourceProducer> producers = new ConcurrentHashMap<>();

    /** 所有激活的资源消耗源，key = buildingId */
    private final ConcurrentHashMap<UUID, IResourceConsumer> consumers = new ConcurrentHashMap<>();

    private final IDatabaseManager db;
    /** 预留：后续城市扩张、跨城市转账时使用 */
    @SuppressWarnings({"Null", "unused"})
    private final CityManager cityManager;

    public EconomyManager(IDatabaseManager db, CityManager cityManager) {
        this.db = db;
        this.cityManager = cityManager;
        // 从数据库恢复所有城市账本
        loadAllLedgers();
    }

    // ===================== IEconomyManager 接口实现 =====================

    @Override
    public void deposit(UUID cityId, long amount, String reason) {
        if (amount <= 0) return;
        FiscalLedger ledger = getOrCreateLedger(cityId);
        ledger.balance.addAndGet(amount);
        ledger.dailyIncome.addAndGet(amount);
        NewSimukraft.LOGGER.debug("[Economy] 城市 {} 收入 {} ({})", cityId, amount, reason);
    }

    @Override
    public boolean withdraw(UUID cityId, long amount, String reason) {
        if (amount <= 0) return true;
        FiscalLedger ledger = getOrCreateLedger(cityId);
        // 使用 CAS 保证原子性：余额足够才扣除
        while (true) {
            long cur = ledger.balance.get();
            if (cur < amount) {
                NewSimukraft.LOGGER.debug("[Economy] 城市 {} 余额不足，需要 {} 实有 {}", cityId, amount, cur);
                return false;
            }
            if (ledger.balance.compareAndSet(cur, cur - amount)) {
                ledger.dailyExpense.addAndGet(amount);
                return true;
            }
        }
    }

    @Override
    public long getBalance(UUID cityId) {
        FiscalLedger ledger = ledgers.get(cityId);
        return ledger == null ? 0L : ledger.balance.get();
    }

    @Override
    @SuppressWarnings("Null") // 方法引用 onResourceInsufficient 的 Null 类型安全警告，确认实例非空
    public void processDailyTax(UUID cityId) {
        FiscalLedger ledger = ledgers.get(cityId);
        if (ledger == null) return;

        long income = 0L;
        long expense = 0L;

        // 累计所有产出源的 tick 产出 × 结算周期
        int cycleTicks = NSUKConfig.TAX_CYCLE_TICKS.get();
        for (IResourceProducer p : producers.values()) {
            if (p.isActive() && cityId.equals(p.getCityId())) {
                income += p.getTickOutput() * cycleTicks;
            }
        }

        // 累计所有消耗源
        for (IResourceConsumer c : consumers.values()) {
            if (cityId.equals(c.getCityId())) {
                long cost = c.getTickConsumption() * cycleTicks;
                expense += cost;
            }
        }

        // 存入收入，尝试扣除支出
        if (income > 0) deposit(cityId, income, "tax_cycle");
        if (expense > 0) {
            boolean ok = withdraw(cityId, expense, "maintenance");
            if (!ok) {
                // 资金不足，通知所有消耗源停工
                consumers.values().stream()
                        .filter(c -> cityId.equals(c.getCityId()))
                        .forEach(IResourceConsumer::onResourceInsufficient);
            }
        }

        // 持久化财政快照
        long balance = ledger.balance.get();
        db.saveFiscalSnapshot(cityId, balance,
                ledger.dailyIncome.getAndSet(0),
                ledger.dailyExpense.getAndSet(0));

        NewSimukraft.LOGGER.info("[Economy] 城市 {} 税收结算完成，余额: {}", cityId, balance);
    }

    @Override
    public float getEconomicHealth(UUID cityId) {
        FiscalLedger ledger = ledgers.get(cityId);
        if (ledger == null) return 0f;

        long balance = ledger.balance.get();
        // 简单健康度模型：余额/10000 映射到 [0,100]，超过 100 万封顶
        float balanceScore = (float) Math.min(100.0, balance / 10000.0);

        // 收支比：收入/(收入+支出)，0.5 表示收支平衡
        long income = ledger.dailyIncome.get();
        long expense = ledger.dailyExpense.get();
        float cashFlowScore = (income + expense == 0) ? 50f
                : (float) (income * 100.0 / (income + expense));

        return (balanceScore * 0.5f + cashFlowScore * 0.5f);
    }

    @Override
    public void registerProducer(UUID buildingId, IResourceProducer producer) {
        producers.put(buildingId, producer);
    }

    @Override
    public void unregisterProducer(UUID buildingId) {
        producers.remove(buildingId);
    }

    @Override
    public void registerConsumer(UUID buildingId, IResourceConsumer consumer) {
        consumers.put(buildingId, consumer);
    }

    @Override
    public void unregisterConsumer(UUID buildingId) {
        consumers.remove(buildingId);
    }

    @Override
    public long getDailyIncome(UUID cityId) {
        FiscalLedger ledger = ledgers.get(cityId);
        return ledger == null ? 0L : ledger.dailyIncome.get();
    }

    @Override
    public long getDailyExpense(UUID cityId) {
        FiscalLedger ledger = ledgers.get(cityId);
        return ledger == null ? 0L : ledger.dailyExpense.get();
    }

    // ===================== 内部工具 =====================

    private FiscalLedger getOrCreateLedger(UUID cityId) {
        return ledgers.computeIfAbsent(cityId, id -> {
            // 新建城市账本，使用配置中的初始资金
            long startBalance = NSUKConfig.STARTING_BALANCE.get();
            FiscalLedger l = new FiscalLedger();
            l.balance.set(startBalance);
            return l;
        });
    }

    /** 从数据库恢复所有城市的财政快照 */
    private void loadAllLedgers() {
        List<UUID> cityIds = db.loadAllCityIds();
        for (UUID id : cityIds) {
            Map<String, Long> snap = db.loadFiscalSnapshot(id);
            if (!snap.isEmpty()) {
                FiscalLedger l = new FiscalLedger();
                l.balance.set(snap.getOrDefault("balance", NSUKConfig.STARTING_BALANCE.get()));
                ledgers.put(id, l);
            }
        }
    }

    // ===================== 内部数据结构 =====================

    /** 单座城市的财政账本 */
    private static class FiscalLedger {
        final AtomicLong balance = new AtomicLong(0);
        final AtomicLong dailyIncome = new AtomicLong(0);   // 本周期累计收入（结算后清零）
        final AtomicLong dailyExpense = new AtomicLong(0);  // 本周期累计支出（结算后清零）
    }
}
