package com.namefix.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.namefix.server.DeadeyeServer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class ToggleDeadeyeCommand implements DeadeyeCommand {
	@Override
	public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection environment) {
		DeadeyeCommandRegistry.addSubcommand(root -> root.then(Commands.literal("toggle")
			.executes(ctx -> toggle(ctx, ctx.getSource().getPlayerOrException()))
			.then(Commands.argument("target", EntityArgument.player())
				.executes(ctx -> toggle(ctx, EntityArgument.getPlayer(ctx, "target"))))));
	}

	private int toggle(CommandContext<CommandSourceStack> context, ServerPlayer target) {
		DeadeyeServer.toggleDeadeye(target);
		boolean isActive = DeadeyeServer.DeadeyeStates.containsKey(target);
		Component message = Component.translatable("command.deadeye.toggle.%s".formatted(isActive ? "enabled" : "disabled"), target.getDisplayName());
		context.getSource().sendSuccess(() -> message, true);
		return isActive ? 1 : 0;
	}
}
