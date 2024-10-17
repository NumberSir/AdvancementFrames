package net.mehvahdjukaar.advframes.network;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.mehvahdjukaar.advframes.AdvFrames;
import net.mehvahdjukaar.advframes.AdvFramesClient;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;

public class ClientBoundSendStatsPacket implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundSendStatsPacket> CODEC = Message.makeType(
            AdvFrames.res("send_stats"), ClientBoundSendStatsPacket::new);

    private final Object2IntMap<Stat<?>> stats;

    public ClientBoundSendStatsPacket(Object2IntMap<Stat<?>> stats) {
        this.stats = stats;
    }

    public ClientBoundSendStatsPacket(RegistryFriendlyByteBuf buf) {
        this.stats = buf.readMap(Object2IntOpenHashMap::new, (friendlyByteBuf2) -> {
            StatType<?> statType = friendlyByteBuf2.readById(BuiltInRegistries.STAT_TYPE::byId);
            return ServerBoundRequestStatsPacket.readStatCap(buf, statType);
        }, FriendlyByteBuf::readVarInt);
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeMap(this.stats, ServerBoundRequestStatsPacket::writeStatCap, FriendlyByteBuf::writeVarInt);
    }

    @Override
    public void handle(Context context) {
        AdvFramesClient.updatePlayerStats(this.stats);
    }


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}
