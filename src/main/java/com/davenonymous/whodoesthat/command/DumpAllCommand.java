package com.davenonymous.whodoesthat.command;

import com.davenonymous.whodoesthat.config.PathConfig;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

public class DumpAllCommand implements Command<CommandSourceStack> {
	private static final DumpAllCommand INSTANCE = new DumpAllCommand();

	public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
		return Commands.literal("dump").executes(INSTANCE);
	}

	public static Component clickableFileComponent(Path path) {
		File f = path.toFile();
		MutableComponent fileComponent = Component.literal(f.getName()).withStyle(ChatFormatting.UNDERLINE);
		fileComponent.withStyle((style) -> style
			.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, f.getAbsolutePath()))
			.withColor(ChatFormatting.BLUE)
			.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to open file.")))
		);
		return fileComponent;
	}

	private void sendReply(CommandContext<CommandSourceStack> context, Optional<String> errors, long duration) {
		if(context.getSource() == null) {
			return;
		}

		if(errors.isEmpty()) {
			var message = Component.literal("Analysis done! Took " + duration + "ms.")
				.append(clickableFileComponent(PathConfig.outputFileJson));

			context.getSource().sendSuccess(() -> message, false);
		} else {
			StringBuilder errorMessage = new StringBuilder("Errors occurred during analysis:\n");
			errorMessage.append(errors.get());
			context.getSource().sendFailure(Component.literal(errorMessage.toString()));
		}
	}

	@Override
	public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		final long startTime = System.nanoTime();

		Optional<String> errors = Optional.empty();
		final long endTime = System.nanoTime();
		final long duration = (endTime - startTime) / 1000000;
		sendReply(context, errors, duration);
		return 0;
	}
}
