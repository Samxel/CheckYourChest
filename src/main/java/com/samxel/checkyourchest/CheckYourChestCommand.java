package com.samxel.checkyourchest;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

public class CheckYourChestCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cyc")
                .executes(CheckYourChestCommand::executeStart));
    }

    private static int executeStart(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();

        ItemStack stick = new ItemStack(Items.STICK, 1);

        //set name
        stick.setHoverName(Component.literal("Marking Stick").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x55FFFF))));


        CompoundTag tag = stick.getOrCreateTag();
        tag.put("Enchantments", new ListTag());  // add empty tag


        stick.addTagElement("HideFlags", IntTag.valueOf(1)); //hide to get enchantment glint
        stick.enchant(Enchantments.UNBREAKING, 1);

        // add description
        CompoundTag displayTag = tag.getCompound("display");
        ListTag loreTag = new ListTag();
        loreTag.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal("Shift+Rightclick on a Chest"))));
        displayTag.put("Lore", loreTag);
        tag.put("display", displayTag);

        stick.setTag(tag);


        player.getInventory().add(stick);
        source.sendSuccess(() -> Component.literal("You have been given the marking stick"), true);

        return Command.SINGLE_SUCCESS;
    }
}
