package com.davenonymous.whodoesthat.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class WhoDoesThatCommand {
	public static LiteralArgumentBuilder<CommandSourceStack> register(CommandDispatcher<CommandSourceStack> dispatcher) {
		return Commands.literal("whodoesthat")
			.then(Commands.literal("config")
				.then(RecreateDefaultConfigsCommand.register(dispatcher))
			)
			.then(DumpAllCommand.register(dispatcher))
			.then(ShowScreenCommand.register(dispatcher));
	}
}
