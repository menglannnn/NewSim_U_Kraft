package com.nsukstudio.newsimukraft.registry;

import com.nsukstudio.newsimukraft.NewSimukraft;
import com.nsukstudio.newsimukraft.entity.FloatingBuildBoxEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** 实体类型注册 */
public final class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, NewSimukraft.MODID);

    private static final ResourceKey<EntityType<?>> FLOATING_BUILD_BOX_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(NewSimukraft.MODID, "floating_build_box"));

    /** 悬浮建筑盒 —— 建筑系统装饰性实体，免疫一切伤害 */
    public static final DeferredHolder<EntityType<?>, EntityType<FloatingBuildBoxEntity>> FLOATING_BUILD_BOX =
            ENTITY_TYPES.register("floating_build_box",
                    () -> EntityType.Builder.<FloatingBuildBoxEntity>of(FloatingBuildBoxEntity::new, MobCategory.MISC)
                            .sized(1.0f, 1.0f)
                            .clientTrackingRange(10)
                            .updateInterval(3)
                            .fireImmune()
                            .build(FLOATING_BUILD_BOX_KEY));

    public static void register(IEventBus bus) {
        ENTITY_TYPES.register(bus);
    }

    private ModEntities() {}
}
