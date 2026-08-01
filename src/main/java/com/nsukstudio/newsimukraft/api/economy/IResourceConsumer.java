package com.nsukstudio.newsimukraft.api.economy;

import java.util.UUID;

/**
 * 资源消耗源接口 —— 任何消耗城市资源的建筑需实现此接口
 */
public interface IResourceConsumer {

    /** 每游戏 tick 消耗的资金量 */
    long getTickConsumption();

    /** 所属城市ID */
    UUID getCityId();

    /** 资源不足时的回调（可触发建筑停工逻辑） */
    void onResourceInsufficient();
}
