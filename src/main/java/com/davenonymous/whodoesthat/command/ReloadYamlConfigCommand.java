package com.davenonymous.whodoesthat.command;

import com.davenonymous.whodoesthat.config.DescriptionConfig;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ReloadYamlConfigCommand implements Command<CommandSourceStack> {
	public static final ReloadYamlConfigCommand INSTANCE = new ReloadYamlConfigCommand();

	public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
		return Commands.literal("reload").executes(INSTANCE);
	}

	@Override
	public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		DescriptionConfig.INSTANCE.load();
		return 0;
	}
}
