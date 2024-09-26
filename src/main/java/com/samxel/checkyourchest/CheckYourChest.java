package com.samxel.checkyourchest;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Objects;

import static com.samxel.checkyourchest.EntityDataManager.loadData;
import static org.apache.commons.lang3.StringEscapeUtils.escapeJson;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(CheckYourChest.MODID)
public class CheckYourChest {
    public static final String MODID = "checkyourchest";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();


    public static int tickCounter = 0;
    public static ChestBlockEntity selectedChestBlockEntity;
    public static ChestBlockEntity connectedChestBlockEntity;

    HashMap<String, Integer> itemMap = new HashMap<>();

    public CheckYourChest() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::onLoadComplete);

        //register config
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        LOGGER.info("Registering CheckYourChest command.");
        CheckYourChestCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        LOGGER.info("Loading recent marked chest.");
        loadData(event.getServer().overworld());
        LOGGER.info("Loading force loaded chunks");
        if (Config.isChunkForceLoaded) {
            ChunkLoader.forceLoadChunk(event.getServer().overworld(), ChunkLoader.getChunkPosFromBlockPos(selectedChestBlockEntity.getBlockPos()));
        }
    }

    private void onLoadComplete(FMLLoadCompleteEvent event) {
        LOGGER.info(MODID + " has been loaded successfully.");
        LOGGER.info("Checking the chest every " + Config.checkInterval + " ticks (" + (Config.checkInterval / 20) + " seconds)");
        LOGGER.info("Sending chest content to the following webhook:\n" + Config.webhookURL);
    }

    private static void renderChestParticles(ServerLevel serverLevel, BlockPos blockPos) {

        // Berechne die Position für die Partikel mittig über dem Block
        double x = blockPos.getX() + 0.5;
        double y = blockPos.getY() + 1.5; // 1 Block höher
        double z = blockPos.getZ() + 0.5;

        // Erzeuge Partikel (hier: Beispiel mit Redstone-Partikeln)
        assert serverLevel != null;
        serverLevel.sendParticles(ParticleTypes.ENCHANT, x, y, z,
                5, 0.2, 0.2, 0.2, 0.01); // Typ, Position, Anzahl, XYZ-Offset, Geschwindigkeit

    }

    private static void countChestContent(ChestBlockEntity chestBlockEntity, HashMap<String, Integer> itemMap) {
        for (int i = 0; i < chestBlockEntity.getContainerSize(); i++) {
            ItemStack itemStack = chestBlockEntity.getItem(i);

            // proceed if slot is empty
            if (!itemStack.isEmpty()) {
                String itemName = chestBlockEntity.getItem(i).getDisplayName().getString();
                itemName = itemName.replace("[", "").replace("]", ""); // Entfernt die Klammern

                // stack count in slot
                int itemCount = itemStack.getCount();

                itemMap.put(itemName, itemMap.getOrDefault(itemName, 0) + itemCount);
            }
        }
    }


    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) { // Sicherstellen, dass wir im End-Tick sind
            tickCounter++; // Tick-Zähler erhöhen
            if (selectedChestBlockEntity != null) {
                ServerLevel serverLevel = (ServerLevel) selectedChestBlockEntity.getLevel();
                BlockPos chestPos = selectedChestBlockEntity.getBlockPos();

                assert serverLevel != null;
                renderChestParticles(serverLevel, chestPos);

                if (connectedChestBlockEntity != null) {
                    System.out.println("Rendering");
                    BlockPos connectedChestPos = connectedChestBlockEntity.getBlockPos();
                    renderChestParticles(serverLevel, connectedChestPos);

                }
            }
            // checkInterval provides the ticks from Config file
            if (tickCounter >= (Config.checkInterval)) {

                if (selectedChestBlockEntity != null) {
                    BlockEntity blockEntity = selectedChestBlockEntity;

                    if (blockEntity instanceof ChestBlockEntity) {

                        // Check if it's a double chest
                        ChestType chestType = selectedChestBlockEntity.getBlockState().getValue(ChestBlock.TYPE);

                        if (chestType != ChestType.SINGLE) {
                            // This chest is part of a double chest
                            Direction connectedDirection = ChestBlock.getConnectedDirection(selectedChestBlockEntity.getBlockState());
                            BlockPos connectedPos = selectedChestBlockEntity.getBlockPos().relative(connectedDirection);
                            connectedChestBlockEntity = (ChestBlockEntity) event.getServer().overworld().getBlockEntity(connectedPos);

                            if (connectedChestBlockEntity instanceof ChestBlockEntity) {
                                LOGGER.debug("This is a double chest");
                            }
                        } else {
                            LOGGER.debug("This is a single chest");
                        }
                    }

                    // reset item map from past calculations
                    itemMap.clear();
                    countChestContent(selectedChestBlockEntity, itemMap);
                    if (connectedChestBlockEntity != null) {
                        countChestContent(connectedChestBlockEntity, itemMap);
                    }

                    // full string for webhook description
                    StringBuilder descriptionBuilder = new StringBuilder();
                    if (!itemMap.isEmpty()) {
                        for (String itemName : itemMap.keySet()) {
                            descriptionBuilder.append(itemName)
                                    .append(": ")
                                    .append(itemMap.get(itemName))
                                    .append("\n");
                        }
                    }

                    // send all items via embed
                    if (!descriptionBuilder.isEmpty()) {
                        sendWebhookWithEmbed("Marked Chest", "https://minecraft.wiki/images/Invicon_Chest.png", descriptionBuilder.toString());
                    }


                }

                // reset tick counter
                tickCounter = 0;
            }
        }
    }

    public static void sendWebhookWithEmbed(String title, String imageLink, String text) {
        try {
            // URL for WebHook - FOUND IN CONFIG FILE
            URL url = new URL(Config.webhookURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // HTTP Request-Method and Header
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // JSON-Body for embed
            String jsonPayload = "{"
                    + "\"embeds\": [{"
                    + "\"title\": \"" + escapeJson(title) + "\","
                    + "\"thumbnail\": { \"url\": \"" + escapeJson(imageLink) + "\" },"
                    + "\"description\": \"" + escapeJson(text) + "\","
                    + "\"color\": 65280"  // Green color
                    + "}]"
                    + "}";

            // send payload to webhook
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // check response code (should be 204)
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                System.out.println("Embed sent successfully!");
            } else {
                System.out.println("Failed to send Embed. Response Code: " + responseCode);
            }

        } catch (Exception e) {
            LOGGER.error("Failed to send Embed via WebHook - Check if the WebHook URL in the config file is correct");
            LOGGER.error(e.getLocalizedMessage());
        }
    }

    @SubscribeEvent
    public void onMarkingStickUse(PlayerInteractEvent.RightClickBlock event) {
        BlockState blockState = event.getLevel().getBlockState(event.getPos());
        //Check if player wants to mark a chest
        if (event.getItemStack().getHoverName().getString().equals("Marking Stick") && blockState.getBlock().equals(Blocks.CHEST) && event.getEntity().isCrouching() && selectedChestBlockEntity == null) {
            event.setCanceled(true);

            selectedChestBlockEntity = (ChestBlockEntity) event.getLevel().getBlockEntity(event.getPos());


            event.getEntity().sendSystemMessage(
                    Component.literal("Marked Chest at " + event.getPos().getX() + " " + event.getPos().getY() + " " + event.getPos().getZ())
            );

            // immediately check if it's a double chest
            ChestType chestType = selectedChestBlockEntity.getBlockState().getValue(ChestBlock.TYPE);
            if (chestType != ChestType.SINGLE) {
                Direction connectedDirection = ChestBlock.getConnectedDirection(selectedChestBlockEntity.getBlockState());
                BlockPos connectedPos = selectedChestBlockEntity.getBlockPos().relative(connectedDirection);
                connectedChestBlockEntity = (ChestBlockEntity) Objects.requireNonNull(selectedChestBlockEntity.getLevel()).getBlockEntity(connectedPos);

                if (connectedChestBlockEntity != null) {
                    event.getEntity().sendSystemMessage(
                            Component.literal("Double Chest connected at " + connectedPos.getX() + " " + connectedPos.getY() + " " + connectedPos.getZ())
                    );
                }
            }

            //saving pos and entity
            EntityDataManager.saveData(selectedChestBlockEntity.getBlockPos());

            LOGGER.debug("Marked Chest at " + event.getPos().getX() + " " + event.getPos().getY() + " " + event.getPos().getZ());

            //Check if player wants to unmark a chest
        } else if (event.getItemStack().getHoverName().getString().equals("Marking Stick") && event.getEntity().isCrouching()) {

            if (selectedChestBlockEntity != null) {
                event.setCanceled(true);
                selectedChestBlockEntity = null;
                connectedChestBlockEntity = null;
                event.getEntity().sendSystemMessage(
                        Component.literal("Removed marked chest.")
                );
            }
        }
    }
}
