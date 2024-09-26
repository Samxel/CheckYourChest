package com.samxel.checkyourchest;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;

public class ChunkLoader {
    private static int loadedChunkX;
    private static int loadedChunkZ;


    public static void forceLoadChunk(ServerLevel level, ChunkPos chunkPos) {
        // load the chunk
        level.getChunkSource().getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, true);

        // force load it
        level.setChunkForced(chunkPos.x, chunkPos.z, true);

        //save it
        loadedChunkX = chunkPos.x;
        loadedChunkZ = chunkPos.z;
        CheckYourChest.LOGGER.info("Force Loaded Chunk: " + loadedChunkX + "x " + loadedChunkZ + "z.");
    }

    public static void unforceLoadChunk(ServerLevel level) {
        // unload
        level.setChunkForced(loadedChunkX, loadedChunkZ, false);
        CheckYourChest.LOGGER.info("Stopped force loading for Chunk: " + loadedChunkX + "x " + loadedChunkZ + "z.");


    }

    //shoutout chatgpt
    public static ChunkPos getChunkPosFromBlockPos(BlockPos blockPos) {
        int chunkX = blockPos.getX() >> 4;  // divide by 16 (2^4)
        int chunkZ = blockPos.getZ() >> 4;  // divide by 16 (2^4)
        return new ChunkPos(chunkX, chunkZ);
    }


}
