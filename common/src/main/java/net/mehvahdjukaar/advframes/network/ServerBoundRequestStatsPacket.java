package net.mehvahdjukaar.advframes.network;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.mehvahdjukaar.advframes.AdvFrames;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;

import java.util.HashSet;
import java.util.Set;

public class ServerBoundRequestStatsPacket implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ServerBoundRequestStatsPacket> CODEC = Message.makeType(
            AdvFrames.res("request_stats"), ServerBoundRequestStatsPacket::new);

    private final Set<Stat<?>> stats;

    public ServerBoundRequestStatsPacket(Set<Stat<?>> stats) {
        this.stats = new HashSet<>(stats);
    }

    public ServerBoundRequestStatsPacket(RegistryFriendlyByteBuf buf) {
        this.stats = buf.readCollection(HashSet::new, (b) -> {
            StatType<?> statType = b.readById(BuiltInRegistries.STAT_TYPE::byId);
            return readStatCap(buf, statType);
        });
    }

    public static <T> Stat<T> readStatCap(FriendlyByteBuf buffer, StatType<T> statType) {
        return statType.get(buffer.readById(statType.getRegistry()::byId));
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeCollection(this.stats, ServerBoundRequestStatsPacket::writeStatCap);
    }

    public static <T> void writeStatCap(FriendlyByteBuf buffer, Stat<T> stat) {
        buffer.writeById(BuiltInRegistries.STAT_TYPE::getId, stat.getType());
        buffer.writeById(stat.getType().getRegistry()::getId, stat.getValue());
    }

    @Override
    public void handle(Context context) {
        if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
            ServerStatsCounter counter = serverPlayer.getStats();
            Object2IntMap<Stat<?>> object2IntMap = new Object2IntOpenHashMap<>();

            Set<Stat<?>> dirty = counter.dirty;
            stats.retainAll(dirty);
            for (Stat<?> stat : stats) {
                object2IntMap.put(stat, counter.getValue(stat));
            }
            dirty.removeAll(stats);

            NetworkHelper.sendToClientPlayer(serverPlayer, new ClientBoundSendStatsPacket(object2IntMap));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}
