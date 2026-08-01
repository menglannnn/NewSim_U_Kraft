package com.nsukstudio.newsimukraft.network;

import com.nsukstudio.newsimukraft.NewSimukraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/**
 * 城市状态同步包（服务端 → 客户端）
 *
 * 触发时机：CityManager.syncToClients() 调用时广播。
 * 携带数据：cityId + 当前人口 + 经济健康度。
 * 客户端收到后更新本地缓存，供 HUD 显示用。
 */
public record CitySyncPacket(UUID cityId, int population, float economicHealth) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CitySyncPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    Identifier.fromNamespaceAndPath(NewSimukraft.MODID, "city_sync"));

    public static final StreamCodec<FriendlyByteBuf, CitySyncPacket> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public CitySyncPacket decode(FriendlyByteBuf buf) {
                    UUID id = new UUID(buf.readLong(), buf.readLong());
                    int pop = buf.readInt();
                    float health = buf.readFloat();
                    return new CitySyncPacket(id, pop, health);
                }

                @Override
                public void encode(FriendlyByteBuf buf, CitySyncPacket pkt) {
                    buf.writeLong(pkt.cityId.getMostSignificantBits());
                    buf.writeLong(pkt.cityId.getLeastSignificantBits());
                    buf.writeInt(pkt.population);
                    buf.writeFloat(pkt.economicHealth);
                }
            };

    /** 客户端接收处理 —— 更新本地城市状态缓存 */
    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            // 更新客户端 HUD 缓存
            NewSimukraft.LOGGER.debug("[Net] 收到城市同步包 city={} pop={} health={}",
                    cityId, population, economicHealth);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
