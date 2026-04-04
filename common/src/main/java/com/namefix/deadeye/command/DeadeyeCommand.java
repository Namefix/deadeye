package com.namefix.deadeye.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

@FunctionalInterface
public interface DeadeyeCommand {
	void register(CommandDispatcher<CommandSourceStack> dispatcher,
	             CommandBuildContext context,
	             Commands.CommandSelection environment);
}
