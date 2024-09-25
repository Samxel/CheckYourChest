package com.samxel.checkyourchest;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

import java.io.File;
import java.io.IOException;

public class EntityDataManager {

    public static final File DATA_FILE = new File("marked_data.dat");

    public static void saveData(BlockPos chestPos) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("chestX", chestPos.getX());
        tag.putInt("chestY", chestPos.getY());
        tag.putInt("chestZ", chestPos.getZ());


        try {
            NbtIo.writeCompressed(tag, DATA_FILE);
        } catch (IOException e) {
            CheckYourChest.LOGGER.error(e.getLocalizedMessage());
        }
    }

    public static void loadData(ServerLevel level) {
        if (DATA_FILE.exists()) {
            try {
                CompoundTag tag = NbtIo.readCompressed(DATA_FILE);
                BlockPos chestPos = new BlockPos(tag.getInt("chestX"), tag.getInt("chestY"), tag.getInt("chestZ"));

                CheckYourChest.selectedChestBlockEntity = (ChestBlockEntity) level.getBlockEntity(chestPos);
                CheckYourChest.LOGGER.info("Loaded selectedChestBlockEntity.");
            } catch (IOException e) {
                CheckYourChest.LOGGER.error(e.getLocalizedMessage());
            }
        }
    }


}
