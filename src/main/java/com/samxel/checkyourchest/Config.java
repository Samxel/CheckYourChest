package com.samxel.checkyourchest;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = CheckYourChest.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.IntValue CHECK_INTERVAL = BUILDER
            .comment("How often should your marked chest be checked? (minutes)")
            .defineInRange("checkInterval", 30, 1, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.ConfigValue<String> DISCORD_WEBHOOK = BUILDER
            .comment("Discord webhook url:")
            .define("webhookURL", "https://discord.com/api/webhooks/.../...");

    private static final ForgeConfigSpec.BooleanValue FORCE_LOAD = BUILDER
            .comment("Do you want to ForceLoad the chests chunk? (true, false)")
            .define("isChunkForceLoaded", false);


    private static final ForgeConfigSpec.BooleanValue ADMIN_ONLY = BUILDER
            .comment("Do you want the commands to be admin only? (true, false)")
            .define("isCommandAdminOnly", true);


    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static int checkInterval;
    public static String webhookURL;
    public static boolean isChunkForceLoaded;
    public static boolean isCommandAdminOnly;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        checkInterval = (CHECK_INTERVAL.get() * 60) * 20; //directly calculate the ticks
        webhookURL = DISCORD_WEBHOOK.get();
        isChunkForceLoaded = FORCE_LOAD.get();
        isCommandAdminOnly = ADMIN_ONLY.get();
    }

    /**
     * Changes the chunk load state and updates the config file.
     *
     * @param loadChunk The new state for chunk force-loading.
     */
    public static void changeLoadedChunk(Boolean loadChunk) {
        // Update the in-memory config value
        FORCE_LOAD.set(loadChunk);

        // Save the updated config value
        isChunkForceLoaded = loadChunk;

    }

    public static void setCheckInterval(int minuteCheckInterval) {
        // Update the in-memory config value
        CHECK_INTERVAL.set(minuteCheckInterval);

        // Save the updated config value
        checkInterval = (minuteCheckInterval * 60) * 20;
    }


    public static void setWebhookURL(String newWebhookURL) {
        // Update the in-memory config value
        DISCORD_WEBHOOK.set(newWebhookURL);

        // Save the updated config value
        webhookURL = newWebhookURL;
    }
}
