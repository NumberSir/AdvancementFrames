package net.mehvahdjukaar.advframes.blocks;

import net.mehvahdjukaar.advframes.AdvFrames;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class AdvancementFrameBlockTile extends BaseFrameBlockTile {

    @Nullable
    private ResourceLocation advancementId = null;
    @Nullable
    private DisplayInfo advancementDisplay = null;

    public AdvancementFrameBlockTile(BlockPos pos, BlockState state) {
        super(AdvFrames.ADVANCEMENT_FRAME_TILE.get(), pos, state);
    }

    public void setAdvancement(AdvancementHolder advancement, ServerPlayer player) {
        this.advancementDisplay = advancement.value().display().orElse(null);
        this.advancementId = advancement.id();
        this.setOwner(new ResolvableProfile(player.getGameProfile()));
    }


    @Override
    protected void saveAdditional(CompoundTag cmp, HolderLookup.Provider registries) {
        super.saveAdditional(cmp, registries);
        if (this.advancementDisplay != null) {
            if (this.level instanceof ServerLevel server && this.owner != null && this.advancementId != null && this.owner.isResolved()) {
                AdvancementHolder advancement = server.getServer().getAdvancements().get(this.advancementId);
                Player player = this.level.getPlayerByUUID(this.owner.id().get());
                if (advancement == null || (player instanceof ServerPlayer sp && !sp.getAdvancements()
                        .getOrStartProgress(advancement).isDone())) {
                    return;
                }
            }
            cmp.put("Advancement", DisplayInfo.CODEC.encodeStart(NbtOps.INSTANCE, this.advancementDisplay).getOrThrow());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag cmp, HolderLookup.Provider registries) {
        super.loadAdditional(cmp, registries);
        this.advancementId = null;
        var ops = registries.createSerializationContext(NbtOps.INSTANCE);
        DisplayInfo.CODEC.parse(ops, cmp.getCompound("Advancement"))
                .ifSuccess(a -> this.advancementDisplay = a);
        //remove
        if (level != null) {
            var t = AdvancementFrameBlock.Type.get(advancementDisplay);
            if (getBlockState().getValue(AdvancementFrameBlock.TYPE) != t) {
                level.setBlockAndUpdate(worldPosition, getBlockState().setValue(AdvancementFrameBlock.TYPE, t));
            }
        }

    }

    @Override
    public ChatFormatting getTitleColor() {
        var v = this.getAdvancement().getType();
        if (v == AdvancementType.GOAL) {
            return ChatFormatting.AQUA;
        }
        return v.getChatColor();
    }

    @Override
    public boolean isEmpty() {
        return advancementDisplay != null;
    }

    @Nullable
    public DisplayInfo getAdvancement() {
        return advancementDisplay;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        if (this.advancementDisplay != null) {
            return ClientboundBlockEntityDataPacket.create(this);
        }
        return null;
    }

    @Nullable
    @Override
    public Component getTitle() {
        if (advancementDisplay != null) return advancementDisplay.getTitle();
        return null;
    }

}
