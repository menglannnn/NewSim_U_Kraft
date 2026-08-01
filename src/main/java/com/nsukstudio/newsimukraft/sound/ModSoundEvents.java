package com.nsukstudio.newsimukraft.sound;

import com.nsukstudio.newsimukraft.NewSimukraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 模组音效事件注册表
 *
 * 使用方式：
 *   在 ModRegistrations.register(modEventBus) 中调用
 *   SOUND_EVENTS.register(modEventBus) 即可完成注册。
 *
 *   对应音效文件放置于：
 *   src/main/resources/assets/newsimukraft/sounds/<name>.ogg
 *   并在 sounds.json 中声明。
 */
public final class ModSoundEvents {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, NewSimukraft.MODID);

    /** 建造音效 */
    public static final DeferredHolder<SoundEvent, SoundEvent> BUILD =
            register("build");

    /** 拆除音效 */
    public static final DeferredHolder<SoundEvent, SoundEvent> DEMOLISH =
            register("demolish");

    /** 升级音效 */
    public static final DeferredHolder<SoundEvent, SoundEvent> UPGRADE =
            register("upgrade");

    /** 居民 NPC 交互音效 */
    public static final DeferredHolder<SoundEvent, SoundEvent> NPC_RESIDENT =
            register("npc_resident");

    /** 商贩 NPC 交互音效 */
    public static final DeferredHolder<SoundEvent, SoundEvent> NPC_MERCHANT =
            register("npc_merchant");

    /** 工人 NPC 交互音效 */
    public static final DeferredHolder<SoundEvent, SoundEvent> NPC_WORKER =
            register("npc_worker");

    /** 工厂环境音（循环） */
    public static final DeferredHolder<SoundEvent, SoundEvent> AMBIENT_FACTORY =
            register("ambient_factory");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(NewSimukraft.MODID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    /** 在 ModEventBus 上注册所有音效 */
    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }

    private ModSoundEvents() {}
}
