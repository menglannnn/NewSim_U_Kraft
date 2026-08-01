package com.nsukstudio.newsimukraft.api.economy;

import java.util.UUID;

/**
 * 资源产出源接口 —— 任何能产生收益的建筑需实现此接口
 */
public interface IResourceProducer {

    /** 每游戏 tick 产出的资金量（可受等级/状态影响） */
    long getTickOutput();

    /** 所属城市ID */
    UUID getCityId();

    /** 当前是否激活产出（停电/停工等状态返回 false） */
    boolean isActive();
}
