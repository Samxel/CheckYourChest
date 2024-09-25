package com.samxel.checkyourchest;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = CheckYourChest.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();


    private static final ForgeConfigSpec.IntValue CHECK_INTERVAL = BUILDER
            .comment("How often should your marked chest be checked? (minutes)")
            .defineInRange("checkInterval", 30, 1, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.ConfigValue<String> DISCORD_WEBHOOK = BUILDER
            .comment("Discord webhook url:")
            .define("webhookURL", "https://discord.com/api/webhooks/.../...");


    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static int checkInterval;
    public static String webhookURL;


    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        checkInterval = (CHECK_INTERVAL.get() * 60) * 20; //directly calculate the ticks
        webhookURL = DISCORD_WEBHOOK.get();
    }
}
