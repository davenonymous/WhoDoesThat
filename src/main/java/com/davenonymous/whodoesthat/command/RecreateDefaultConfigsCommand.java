package com.davenonymous.whodoesthat.command;

import com.davenonymous.whodoesthat.config.PathConfig;
import com.davenonymous.whodoesthat.util.JarHelper;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.file.Files;

public class RecreateDefaultConfigsCommand implements Command<CommandSourceStack> {
	public static final RecreateDefaultConfigsCommand INSTANCE = new RecreateDefaultConfigsCommand();

	public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
		return Commands.literal("recreate-defaults").executes(INSTANCE);
	}

	@Override
	public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		if(!Files.exists(PathConfig.configPath)) {
			try {
				JarHelper.extractDefaultConfigs();
				context.getSource().sendSuccess(() -> Component.literal("Default configs recreated"), true);
			} catch (IOException e) {
				context.getSource().sendFailure(Component.literal(e.getMessage()));
				return 1;
			}
		}

		return 0;
	}
}
