package com.nsukstudio.newsimukraft.network;

import com.nsukstudio.newsimukraft.ModuleHolder;
import com.nsukstudio.newsimukraft.NewSimukraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/**
 * 建造请求包（客户端 → 服务端）
 *
 * 触发时机：玩家点击确认放置建筑时由客户端发送。
 * 携带数据：建筑类型ID + 放置坐标。
 * 服务端收到后调用 IBuildingManager.placeBuilding() 校验并执行。
 */
public record BuildRequestPacket(String typeId, BlockPos pos) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<BuildRequestPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    Identifier.fromNamespaceAndPath(NewSimukraft.MODID, "build_request"));

    public static final StreamCodec<FriendlyByteBuf, BuildRequestPacket> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public BuildRequestPacket decode(FriendlyByteBuf buf) {
                    String typeId = buf.readUtf(128);
                    BlockPos pos = buf.readBlockPos();
                    return new BuildRequestPacket(typeId, pos);
                }

                @Override
                public void encode(FriendlyByteBuf buf, BuildRequestPacket pkt) {
                    buf.writeUtf(pkt.typeId, 128);
                    buf.writeBlockPos(pkt.pos);
                }
            };

    /** 服务端接收处理 —— 执行建造逻辑 */
    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            UUID playerId = context.player().getUUID();
            // MC 26.2：ServerPlayer 无 serverLevel()，直接转型 level() 结果（服务端 level 必为 ServerLevel）
            ServerLevel level = (ServerLevel) context.player().level();
            boolean success = false;
            if (ModuleHolder.getBuilding() != null) {
                success = ModuleHolder.getBuilding().placeBuilding(playerId, typeId, pos, level);
            }
            NewSimukraft.LOGGER.debug("[Net] 建造请求 player={} type={} pos={} result={}",
                    playerId, typeId, pos, success);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
