package net.mehvahdjukaar.advframes.network;

import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;

public class ModMessages {

    public static void init() {
        NetworkHelper.addNetworkRegistration(ModMessages::registerMessages, 1);
    }

    private static void registerMessages(NetworkHelper.RegisterMessagesEvent event) {
        event.registerClientBound(ClientBoundSendStatsPacket.CODEC);
        event.registerServerBound(ServerBoundRequestStatsPacket.CODEC);
        event.registerServerBound(ServerBoundSetStatFramePacket.CODEC);
        event.registerServerBound(ServerBoundSetAdvancementFramePacket.CODEC);
    }
}