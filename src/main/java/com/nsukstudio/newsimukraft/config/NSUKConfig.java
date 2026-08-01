package com.nsukstudio.newsimukraft.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 模组配置 —— 分 COMMON（服务端）和 CLIENT（客户端）两份
 *
 * 修改配置后需同步更新：
 *   1. 此文件中的字段定义
 *   2. src/main/resources/assets/newsimukraft/lang/zh_cn.json 中的翻译
 *   3. NewSimukraftClient 中的配置界面绑定（如有新分组）
 */
public final class NSUKConfig {

    // ---- COMMON 配置（服务端生效）----

    public static final ModConfigSpec COMMON_SPEC;
    public static final ModConfigSpec.IntValue CITY_PLOT_SIZE;         // 单个地块边长（格）
    public static final ModConfigSpec.IntValue MAX_BUILDINGS_PER_CITY; // 单城市建筑上限（防止性能问题）
    public static final ModConfigSpec.LongValue STARTING_BALANCE;      // 新建城市初始资金
    public static final ModConfigSpec.IntValue TAX_CYCLE_TICKS;        // 税收结算周期（ticks，默认 24000 = 1游戏日）
    public static final ModConfigSpec.IntValue MAX_NPC_PER_CITY;       // 单城市 NPC 上限（防 OOM）
    public static final ModConfigSpec.DoubleValue POLLUTION_DECAY_RATE; // 污染值每 tick 自然衰减率

    static {
        ModConfigSpec.Builder commonBuilder = new ModConfigSpec.Builder();

        commonBuilder.comment("城市地块配置").push("city");
        // 修改后需重启服务端生效；调大此值会显著增加内存占用
        CITY_PLOT_SIZE = commonBuilder
                .comment("单个地块的边长（格），推荐 16（与区块对齐），修改后需重新生成城市")
                .defineInRange("plotSize", 16, 8, 64);
        MAX_BUILDINGS_PER_CITY = commonBuilder
                .comment("单城市最大建筑数量上限，超出后无法新建")
                .defineInRange("maxBuildingsPerCity", 200, 10, 1000);
        commonBuilder.pop();

        commonBuilder.comment("经济配置").push("economy");
        STARTING_BALANCE = commonBuilder
                .comment("新建城市的初始资金")
                .defineInRange("startingBalance", 10000L, 0L, Long.MAX_VALUE);
        TAX_CYCLE_TICKS = commonBuilder
                .comment("税收结算周期（游戏 tick 数，20 tick = 1秒，24000 = 1游戏日）")
                .defineInRange("taxCycleTicks", 24000, 200, 240000);
        commonBuilder.pop();

        commonBuilder.comment("NPC 配置").push("npc");
        MAX_NPC_PER_CITY = commonBuilder
                .comment("单城市 NPC 数量上限，过高会影响服务端性能")
                .defineInRange("maxNpcPerCity", 100, 10, 500);
        commonBuilder.pop();

        commonBuilder.comment("环境配置").push("environment");
        POLLUTION_DECAY_RATE = commonBuilder
                .comment("污染值每 tick 的自然衰减率（0.0001 表示约 10000 tick 衰减到 0）")
                .defineInRange("pollutionDecayRate", 0.0001, 0.0, 1.0);
        commonBuilder.pop();

        COMMON_SPEC = commonBuilder.build();
    }

    // ---- CLIENT 配置（客户端生效）----

    public static final ModConfigSpec CLIENT_SPEC;
    public static final ModConfigSpec.BooleanValue SHOW_BUILD_PREVIEW; // 是否显示建造预览
    public static final ModConfigSpec.BooleanValue ENABLE_AMBIENT_SOUND; // 建筑环境音
    public static final ModConfigSpec.DoubleValue UI_SCALE;             // 城市 HUD 缩放比例

    static {
        ModConfigSpec.Builder clientBuilder = new ModConfigSpec.Builder();

        clientBuilder.comment("客户端显示配置").push("client");
        SHOW_BUILD_PREVIEW = clientBuilder
                .comment("是否在放置建筑时显示预览轮廓（关闭可提升低配机器性能）")
                .define("showBuildPreview", true);
        ENABLE_AMBIENT_SOUND = clientBuilder
                .comment("是否播放建筑环境音效（工厂轰鸣等）")
                .define("enableAmbientSound", true);
        UI_SCALE = clientBuilder
                .comment("城市 HUD 界面缩放比例（1.0 为默认）")
                .defineInRange("uiScale", 1.0, 0.5, 2.0);
        clientBuilder.pop();

        CLIENT_SPEC = clientBuilder.build();
    }

    private NSUKConfig() {}
}
