package net.mehvahdjukaar.advframes.network;

import net.mehvahdjukaar.advframes.AdvFrames;
import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlock;
import net.mehvahdjukaar.advframes.blocks.StatFrameBlockTile;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.StatType;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ServerBoundSetStatFramePacket implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ServerBoundSetStatFramePacket> CODEC = Message.makeType(
            AdvFrames.res("set_stat_frame"), ServerBoundSetStatFramePacket::new);

    private final BlockPos pos;
    public final ResourceLocation statValue;
    public final ResourceLocation statType;

    public ServerBoundSetStatFramePacket(RegistryFriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.statValue = buf.readResourceLocation();
        this.statType = buf.readResourceLocation();
    }

    public <T> ServerBoundSetStatFramePacket(BlockPos pos, StatType<T> stat, T obj) {
        this.pos = pos;
        this.statValue = stat.getRegistry().getKey(obj);
        this.statType = BuiltInRegistries.STAT_TYPE.getKey(stat);
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeResourceLocation(this.statValue);
        buf.writeResourceLocation(this.statType);
    }

    @Override
    public void handle(Context context) {
        if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
            ServerLevel level = (ServerLevel) serverPlayer.level();
            BlockPos pos = this.pos;
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof StatFrameBlockTile te) {
               var stat =  BuiltInRegistries.STAT_TYPE.get(statType);
               if(stat != null) {
                   te.setStat(stat, statValue, serverPlayer);
                   te.updateStatValue();
                   //updates client
                   tile.setChanged();
                   level.sendBlockUpdated(pos,tile.getBlockState(),tile.getBlockState(), 3);
               }
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}