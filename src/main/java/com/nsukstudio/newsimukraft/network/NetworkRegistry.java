package com.nsukstudio.newsimukraft.network;

import com.nsukstudio.newsimukraft.NewSimukraft;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 网络模块注册中心 —— 统一注册所有客户端-服务端数据包
 *
 * 使用方式：
 *   在 NewSimukraft 构造器中：modEventBus.addListener(NetworkRegistry::onRegisterPayloads)
 *
 * 新增数据包步骤：
 *   1. 在 packet/ 包下创建实现 CustomPacketPayload 的数据包类
 *   2. 在此方法的 registrar 中注册 handler
 */
public final class NetworkRegistry {

    private NetworkRegistry() {}

    @SuppressWarnings("null") // 方法引用 ::handle 的 @Nonnull 类型安全警告，确认为空安全
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(NewSimukraft.MODID);

        // 服务端 → 客户端：城市状态同步包
        registrar.playToClient(
                CitySyncPacket.TYPE,
                CitySyncPacket.STREAM_CODEC,
                CitySyncPacket::handle);

        // 客户端 → 服务端：建造请求包
        registrar.playToServer(
                BuildRequestPacket.TYPE,
                BuildRequestPacket.STREAM_CODEC,
                BuildRequestPacket::handle);
    }
}
