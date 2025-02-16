package com.davenonymous.whodoesthat.command;

import com.davenonymous.whodoesthat.gui.ModOverviewScreen;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ShowScreenCommand implements Command<CommandSourceStack> {
	public static final ShowScreenCommand INSTANCE = new ShowScreenCommand();

	public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
		return Commands.literal("show").executes(INSTANCE);
	}

	@Override
	public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		Minecraft.getInstance().setScreen(new ModOverviewScreen());
		return 0;
	}
}
