package io.github.natanfudge.hardcraft.mixin;

import io.github.natanfudge.hardcraft.mixinhandler.ServerEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkSectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThreadedAnvilChunkStorage.class)
public class ThreadedAnvilChunkStorageMixin {
    @Inject(method = "handlePlayerAddedOrRemoved(Lnet/minecraft/server/network/ServerPlayerEntity;Z)V", at = @At("HEAD"))
    public void handlePlayerAddedOrRemovedHook(ServerPlayerEntity player, boolean added, CallbackInfo ci) {
        if (added){
            int chunkX = ChunkSectionPos.getSectionCoord(player.getBlockX());
            int chunkZ = ChunkSectionPos.getSectionCoord(player.getBlockZ());
            ServerEvents.onPlayerLoadChunks(player);
        }
    }
}
