package net.mehvahdjukaar.advframes.blocks;

import com.mojang.authlib.properties.PropertyMap;
import net.mehvahdjukaar.advframes.AdvFrames;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

import static net.minecraft.world.level.block.entity.SkullBlockEntity.CHECKED_MAIN_THREAD_EXECUTOR;

public abstract class BaseFrameBlockTile extends BlockEntity {

    protected ResolvableProfile owner;

    protected BaseFrameBlockTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.owner != null) {
            tag.put("profile", ResolvableProfile.CODEC.encodeStart(NbtOps.INSTANCE, this.owner).getOrThrow());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        //TODO: remove
        if (tag.contains("PlayerID")) {
            UUID id = tag.getUUID("PlayerID");
            this.setOwner(new ResolvableProfile(Optional.empty(), Optional.of(id), new PropertyMap()));
        }
        if (tag.contains("profile")) {
            ResolvableProfile.CODEC.parse(NbtOps.INSTANCE, tag.get("profile")).resultOrPartial((string) -> {
                AdvFrames.LOGGER.error("Failed to load profile from player head: {}", string);
            }).ifPresent(this::setOwner);
        }
    }

    public ResolvableProfile getOwner() {
        return owner;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    public void setOwner(@Nullable ResolvableProfile owner) {
        synchronized (this) {
            this.owner = owner;
        }

        if (this.owner != null && !this.owner.isResolved()) {
            this.owner.resolve().thenAcceptAsync((resolvableProfile) -> {
                this.owner = resolvableProfile;
                this.setChanged();
            }, CHECKED_MAIN_THREAD_EXECUTOR);
        } else {
            this.setChanged();
        }
    }

    @Nullable
    public Component getOwnerName() {
        if (owner != null) {
            return owner.name().map(Component::literal).orElse(null);
        }
        return null;
    }

    @Nullable
    public abstract Component getTitle();

    public abstract ChatFormatting getTitleColor();

    public abstract boolean isEmpty();

}
