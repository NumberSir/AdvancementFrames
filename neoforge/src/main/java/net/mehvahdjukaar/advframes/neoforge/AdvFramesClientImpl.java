package net.mehvahdjukaar.advframes.neoforge;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.ClientHooks;

public class AdvFramesClientImpl {

    public static void clearForgeGuiLayers(Minecraft minecraft) {
        ClientHooks.clearGuiLayers(minecraft);
    }


}
