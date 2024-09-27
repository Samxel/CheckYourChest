package com.samxel.checkyourchest;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;


public class CheckYourChestCommand {

    private static int permissionLevel;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        if (Config.isCommandAdminOnly){
            permissionLevel = 2;
        }else {
            permissionLevel = 0;
        }

        dispatcher.register(Commands.literal("cyc")
                .requires(source -> source.hasPermission(permissionLevel)) // Only allows OPs to execute
                //give stick method
                .executes(CheckYourChestCommand::executeStart)
                //set check interval
                .then(Commands.literal("setCheckInterval")
                        .then(Commands.argument("checkInterval", IntegerArgumentType.integer(1))
                                .executes(CheckYourChestCommand::executeSetInterval)))
                //set webhook URL
                .then(Commands.literal("setWebhookURL")
                        .then(Commands.argument("webhookURL", StringArgumentType.string())
                                .executes(CheckYourChestCommand::executeSetWebhookURL)))
                //set if chest chunk should be force loaded
                .then(Commands.literal("forceload")
                        .then(Commands.argument("state", BoolArgumentType.bool())
                                .executes(CheckYourChestCommand::executeForceLoad)))
                //debug command to show all states
                .then(Commands.literal("debug")
                        .executes(CheckYourChestCommand::executeShowDebugInfo)));

    }

    private static int executeStart(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();

        ItemStack stick = new ItemStack(Items.STICK, 1);
        DataComponentType<Component> customNameComponent = DataComponents.CUSTOM_NAME;
        stick.set(customNameComponent, Component.literal("Marking Stick").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x55FFFF))));

        DataComponentType<Boolean> enchantmentComponent = DataComponents.ENCHANTMENT_GLINT_OVERRIDE;
        stick.set(enchantmentComponent, true);

        player.getInventory().add(stick);
        source.sendSuccess(() -> Component.literal("You have been given the marking stick"), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int executeForceLoad(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();

        boolean loadState = BoolArgumentType.getBool(context, "state");

        //Updates it in Config file & class
        Config.changeLoadedChunk(loadState);

        //check if it should be force loaded
        if (Config.isChunkForceLoaded) {
            CheckYourChest.LOGGER.debug("ChunkForceLoading set to true via command.");
            ChunkLoader.forceLoadChunk(source.getLevel(), ChunkLoader.getChunkPosFromBlockPos(CheckYourChest.selectedChestBlockEntity.getBlockPos()));
        } else {
            CheckYourChest.LOGGER.debug("ChunkForceLoading set to false via command.");
            ChunkLoader.unforceLoadChunk(source.getLevel());
        }

        source.sendSuccess(() -> Component.literal("Set chunk force loading to " + loadState), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int executeSetInterval(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();

        int minutesInterval = IntegerArgumentType.getInteger(context, "checkInterval");

        Config.setCheckInterval(minutesInterval);

        //reset tick counter
        CheckYourChest.tickCounter = 0;
        source.sendSuccess(() -> Component.literal("Set check interval to " + minutesInterval + " minute(s)"), true);
        return Command.SINGLE_SUCCESS;
    }

        private static int executeSetWebhookURL(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();

        String webHookURL = StringArgumentType.getString(context, "webhookURL");

        Config.setWebhookURL(webHookURL);

        source.sendSuccess(() -> Component.literal("Set webhook URL to \n" + webHookURL), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int executeShowDebugInfo(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        BlockPos chestBlockPos = CheckYourChest.selectedChestBlockEntity.getBlockPos();

        // Creating formatted messages
        MutableComponent intervalMessage = Component.literal("Check interval: ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(((Config.checkInterval / 20) / 60) + " minutes").withStyle(ChatFormatting.GREEN));

        MutableComponent forceLoadMessage = Component.literal("Is chunk force loaded? ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(String.valueOf(Config.isChunkForceLoaded)).withStyle(ChatFormatting.AQUA));

        MutableComponent webhookMessage = Component.literal("Webhook URL:\n ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(Config.webhookURL).withStyle(ChatFormatting.BLUE));

        MutableComponent chestPositionMessage = Component.literal("Marked chest at: ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(chestBlockPos.getX() + " " + chestBlockPos.getY() + " " + chestBlockPos.getZ())
                        .withStyle(ChatFormatting.YELLOW));

        // Send messages to the player with color formatting
        source.sendSystemMessage(intervalMessage);
        source.sendSystemMessage(forceLoadMessage);
        source.sendSystemMessage(webhookMessage);
        source.sendSystemMessage(chestPositionMessage);
        return Command.SINGLE_SUCCESS;
    }

}
